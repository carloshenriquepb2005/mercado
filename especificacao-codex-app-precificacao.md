# Especificação de Implementação — App Satélite de Precificação

A solução será composta por:

- backend em Java com Spring Boot;
- aplicativo móvel em Angular, Ionic e Capacitor;
- integração com banco legado MySQL 5.5;
- comunicação exclusivamente pela rede local da loja.

---

# 2. Contexto do sistema

O mercado já possui um sistema legado desenvolvido em VB.NET.

Esse sistema utiliza um banco de dados MySQL 5.5 instalado localmente na máquina ou servidor do estabelecimento.

Ele funcionará como uma extensão móvel especializada em consulta de produtos e alteração de preços.

O aplicativo móvel nunca deve acessar diretamente o banco de dados.

---

# 4. Stack tecnológica

## 4.1 Backend

- Java 21 recomendado.
- Spring Boot 3.x.
- Maven.
- Spring Web.
- Spring Data JPA.
- Hibernate.
- MySQL JDBC Driver compatível com o banco legado.
- Jakarta Bean Validation.
- Spring Boot Actuator.
- JUnit 5.
- Mockito.
- Springdoc OpenAPI.
- JaCoCo para cobertura de testes.

## 4.2 Aplicativo móvel

- Angular com componentes standalone.
- TypeScript.
- Ionic Framework.
- Capacitor.
- SCSS.
- Reactive Forms.
- Angular Signals para estado local.
- HttpClient.
- Plugin de leitura de código de barras compatível com a versão adotada do Capacitor.
- Android como plataforma inicial.
- Jasmine/Karma ou Vitest, conforme o padrão oficial da versão adotada do Angular.
- ESLint.
- Prettier opcional.

Antes de instalar o plugin de scanner, verificar qual pacote possui suporte ativo e compatibilidade com a versão do Capacitor utilizada.

---

# 5. Estrutura geral do repositório

Criar um único repositório com a seguinte estrutura:

```text
app-precificacao/
├── backend/
├── mobile/
├── docs/
├── .editorconfig
├── .gitignore
├── README.md
└── AGENTS.md
```

## 5.1 Pasta `backend`

Contém a API Spring Boot.

## 5.2 Pasta `mobile`

Contém o aplicativo Ionic/Angular.



# 7. Estrutura de pastas do backend

Usar a seguinte estrutura:

```text
src/main/java/br/com/mercado/precificacao/

├── adapter/
│   ├── config/
│   ├── exception/
│   ├── input/
│   └── output/
│
├── config/
│
├── core/
│   ├── exception/
│   ├── mapper/
│   ├── model/
│   ├── service/
│   └── usecase/
│
├── port/
│   ├── input/
│   └── output/
│
├── util/
│
└── Application.java
```

---

# 14. Mapeamento do banco legado

## 14.1 Tabela `produtos`

```text
ID                int, PK, auto increment
CODIGO_PRODUTO    varchar(13), unique ou index
DESCRICAO         varchar(60)
UN                unidade comercial do produto
PRECO_UNITARIO    decimal(10,2)
PRECO_CUSTO       decimal(10,2)
CODIGO_NCM        código NCM do produto
ESTOQUE_ATUAL     quantidade atual em estoque
```

## 14.2 Tabela `produtos_similares`

```text
CODIGO_SIMILAR    varchar(13)
CODIGO_PRODUTO    varchar(13)
```

---

# 35. Conectividade

Criar `ConnectivityService`.

Responsabilidades:

- consultar `/actuator/health`;
- expor estado da API;
- permitir nova tentativa;
- diferenciar erro de rede de produto não encontrado.

Não implementar cache offline.

---

# 39. Padrões de qualidade

O Codex deve:

- usar nomes claros com sufixo do pacote pertencente a classe, exceto model. Ex para ports: NomeOutputPort;
- não expor entidades JPA;
- não versionar secrets;
- manter métodos coesos.

---

# 40. Logs

Registrar:

- inicialização;
- indisponibilidade do banco;
- tentativas de atualização;
- atualizações do banco concluídas com sucesso;
- erros inesperados.

Não registrar:

- senha;
- credenciais;
- SQL sensível;
- tokens;
- stack trace em resposta HTTP.

---

# 42. Build do backend

```bash
./mvnw clean test
./mvnw clean verify
./mvnw clean package
```

O build deve falhar se os testes falharem.

---

# 43. Build do aplicativo

```bash
npm install
npm run lint
npm test
ionic build
npx cap sync android
npx cap open android
```

Gerar APK assinado.

---

# 50. Restrições

O Codex não deve:

- alterar tabelas;
- criar migrations;
- cadastrar produtos;
- excluir produtos;
- alterar preço de custo;
- acessar banco diretamente pelo app;
- usar `ddl-auto=update`, `create` ou `create-drop`;
- usar produção em testes;
- usar `double` para preço;
- acoplar domínio a Spring ou JPA;
- adicionar NgRx;
- inventar colunas;
- presumir unicidade sem validar;
- ignorar testes falhos.

Em divergência com o schema real:

1. preservar o legado;
2. não alterar automaticamente;
3. documentar;
4. adaptar o mapeamento;
5. solicitar decisão humana quando a regra mudar.

---
