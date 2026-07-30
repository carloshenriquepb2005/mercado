# Testes

Backend: `mvn clean verify` executa JUnit 5/Mockito, testes web com MockMvc e o limite de cobertura JaCoCo. O caso de uso e o endpoint de login também são testados. Nenhum teste utiliza o banco real.

Mobile: `npm run lint`, `npm run test:ci` e `npm run build`. Login, scanner, HttpClient, navegação, tela de Sucesso, toasts e rota são mockados nos testes; câmera real deve ser validada manualmente em um Android depois de `npx cap sync android`.
