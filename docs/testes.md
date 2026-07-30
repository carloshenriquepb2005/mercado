# Testes

Backend: `mvn clean verify` executa JUnit 5/Mockito, testes web com MockMvc e o limite de cobertura JaCoCo. Nenhum teste utiliza o banco real.

Mobile: `npm run lint`, `npm test -- --watch=false` e `npm run build`. Scanner, HttpClient, navegação, toasts e rota são mockados nos testes; câmera real deve ser validada manualmente em um Android depois de `npx cap sync android`.

