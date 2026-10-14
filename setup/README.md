# Ferramentas de execução

Os executáveis desta pasta devem permanecer dentro de `setup`, diretamente abaixo da raiz do projeto. Eles localizam `backend` e `mobile` usando essa posição relativa e não armazenam credenciais.

- `Inicializar Backend.exe`: executa `mvn.cmd spring-boot:run` dentro de `backend`, usando o repositório Maven do perfil atual.
- `Inicializar Frontend.exe`: executa `npm start` dentro de `mobile`.
- `Gerar APK.exe`: gera o build Angular, sincroniza o Capacitor e executa `gradlew.bat assembleDebug` com o Java 21 do Android Studio. O resultado fica em `mobile\android\app\build\outputs\apk\debug\app-debug.apk`.
- `Executar Testes Frontend.exe`: executa `npm run test:ci`.
- `Executar Testes Backend.exe`: executa `mvn.cmd test` dentro de `backend`, sem acessar o banco real.

Cada ferramenta mantém a janela aberta ao terminar para permitir a leitura do resultado. Nos inicializadores, use `Ctrl+C` para encerrar o servidor.

## Pré-requisitos

- `.env` configurado na raiz do projeto;
- Node.js e npm disponíveis no `PATH`;
- Android Studio instalado para gerar o APK;
- navegador Chrome instalado para os testes do frontend;
- acesso ao Maven e ao cache de dependências já configurado.

Os executáveis são compilados a partir de `setup/src/SetupLauncher.cs` e não adicionam bibliotecas ao projeto.
