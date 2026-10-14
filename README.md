# App Satélite de Precificação

Aplicação interna com login, Home operacional, leitura de código de barras, consulta de produtos e alteração exclusiva do preço de venda no MySQL legado. O celular comunica-se com uma API Spring Boot pela rede Wi-Fi local; o aplicativo nunca acessa o banco diretamente.

## Estrutura

- `backend/`: API Java/Spring Boot em arquitetura hexagonal moderada.
- `mobile/`: Ionic + Angular standalone + Capacitor para Android.
- `docs/`: arquitetura, contratos, implantação, requisitos e testes.
No backend, o código é dividido entre `adapter`, `config`, `core` e `port`. As portas possuem os sufixos `InputPort` e `OutputPort`, regras de negócio ficam em Use Cases e Services, e cada controller possui sua própria interface Swagger.

## Requisitos

- JDK 17 ou superior para o backend; JDK 21 ou superior para o build Android do Capacitor 8.
- Maven 3.9+.
- Node.js 22+ e npm 10+.
- Android Studio/JDK compatível para gerar o APK.
- Acesso de rede ao MySQL 5.5 somente a partir do servidor da API.

## Configuração

Copie `env.example` para `.env` e substitua os exemplos. O backend carrega automaticamente o `.env` da raiz. `DB_URL` é opcional e, quando omitida, é montada com `DB_HOST`, `DB_PORT` e `DB_DATABASE`.

O build mobile executa `mobile/scripts/generate-environment.mjs` e usa `MOBILE_API_URL`. Sem essa variável, usa `http://localhost:8080`, adequado apenas ao navegador local.

O login consulta somente a tabela legada `usuarios`, usando os campos `codigo`, `nome`, `senha` e `perfil`. A senha é comparada em texto puro, conforme o requisito atual, e nunca é colocada no código do aplicativo. Os perfis 1 (usuário), 2 (gerencial) e 3 (administrador) possuem o mesmo acesso nesta versão. Como a API local usa HTTP, esse login é apropriado somente para a rede interna confiável; para acesso externo, utilize HTTPS e autenticação com sessão/token.

## Executar e validar

```powershell
cd backend
mvn clean verify
mvn spring-boot:run
```

Swagger: `http://localhost:8080/swagger-ui.html`  
Health check: `http://localhost:8080/actuator/health`

Ao abrir o aplicativo, faça login com o `nome` e a `senha` existentes na tabela `usuarios`. A Home mostra a conectividade e encaminha para o scanner. Depois de salvar um preço, a tela de Sucesso exibe produto, código, preço anterior e novo preço.

```powershell
cd mobile
npm install
npm run lint
npm run test:ci
npm run build
npx cap sync android
```

## Executar em um celular Android físico

O computador e o celular precisam estar conectados à mesma rede Wi-Fi. O emulador pode funcionar mesmo quando o Firewall do Windows está bloqueando o acesso do celular físico.

### 1. Descobrir o IP do computador

No PowerShell:

```powershell
ipconfig
```

Localize o endereço IPv4 da interface Wi-Fi, por exemplo `192.168.18.200`. Não utilize `localhost` no APK.

### 2. Configurar o `.env`

Na raiz do projeto:

```powershell
Copy-Item env.example .env
notepad .env
```

Configure pelo menos:

```dotenv
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0
MOBILE_API_URL=http://192.168.18.200:8080
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8100,http://localhost,capacitor://localhost
```

Substitua `192.168.18.200` pelo IPv4 real do computador e preencha também as variáveis `DB_*`.

### 3. Iniciar e testar o backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No navegador do computador, confirme:

```text
http://localhost:8080/actuator/health
```

A resposta esperada é `{"status":"UP"}`.

### 4. Liberar a API no Firewall do Windows

Abra o PowerShell como **Administrador** e crie uma regra limitada à rede local:

```powershell
New-NetFirewallRule `
  -DisplayName "App Precificacao API (TCP 8080)" `
  -Description "Permite acesso à API somente pela rede local." `
  -Direction Inbound `
  -Action Allow `
  -Protocol TCP `
  -LocalPort 8080 `
  -RemoteAddress LocalSubnet `
  -Profile Public,Private
```

O Windows pode ter regras explícitas de bloqueio para o Java. Elas têm prioridade sobre a regra de liberação da porta. Para desativar **somente** os bloqueios do JDK que executa este backend:

```powershell
Get-NetFirewallRule `
  -Enabled True `
  -Direction Inbound `
  -Action Block |
ForEach-Object {
  $rule = $_
  $app = Get-NetFirewallApplicationFilter -AssociatedNetFirewallRule $rule

  if ($app.Program -ieq "C:\Program Files\Java\jdk-17\bin\java.exe") {
    $rule | Disable-NetFirewallRule
  }
}
```

Esse comando não altera as regras dos runtimes Java internos do VS Code.

### 5. Testar pelo próprio celular

Antes de instalar ou abrir o aplicativo, acesse no Chrome do Android:

```text
http://IP-DO-COMPUTADOR:8080/actuator/health
```

Somente prossiga quando o celular receber `{"status":"UP"}`. Se houver timeout, o problema ainda está na rede ou no Firewall; reinstalar o APK não resolverá.

### 6. Gerar e sincronizar o Android

```powershell
cd mobile
npm install
npm run build
npx cap sync android
npx cap open android
```

No Android Studio:

1. use JDK 21 ou superior como Gradle JDK;
2. selecione o celular físico, e não o emulador;
3. aceite a autorização de depuração USB no aparelho;
4. clique em **Run 'app'**.

O APK debug também pode ser encontrado em:

```text
mobile/android/app/build/outputs/apk/debug/app-debug.apk
```

Conceda a permissão permanente de câmera em **Configurações → Apps → Precificação → Permissões → Câmera → Permitir durante o uso**.

### 7. Diagnóstico rápido de “Servidor indisponível”

Verifique nesta ordem:

1. o backend continua aberto e sem erro;
2. `/actuator/health` retorna `UP` no computador;
3. o mesmo endereço abre no navegador do celular;
4. `MOBILE_API_URL` contém o IP atual do computador;
5. computador e celular estão na mesma rede;
6. a porta 8080 e o executável do JDK não estão bloqueados no Firewall;
7. o aplicativo foi reconstruído e sincronizado depois de alterar o `.env`.

## Limites e segurança do legado

Não há endpoints para criar, substituir ou excluir produtos ou usuários. A atualização usa uma query explícita que modifica apenas `PRECO_UNITARIO` pelo `ID`; o acesso a `usuarios` é somente leitura. Hibernate está com `ddl-auto: none`; não há migrations. Senha, SQL interno e stack traces não são enviados ao aplicativo.

Consulte [arquitetura](docs/arquitetura.md), [API](docs/api.md), [implantação](docs/implantacao.md) e [testes](docs/testes.md).
