# Testes

Backend: `mvn clean verify` executa JUnit 5/Mockito, testes web com MockMvc e a verificação JaCoCo. O limite é de 100% das linhas consideradas pelo relatório, incluindo integralmente os Use Cases e Services. Classes puramente estruturais ou de infraestrutura, como configurações, entidades JPA, requests, responses e bootstrap, não entram no cálculo.

Os testes unitários seguem AAA (Arrange, Act, Assert). Casos de uso, serviços, mappers, controllers e adapters possuem testes sem acesso ao banco real. Repositórios, portas externas e infraestrutura são substituídos por mocks.

Mobile: `npm run lint`, `npm run test:ci` e `npm run build`. Login, configuração persistida da API, IPv4, timeout de conexão, modo interno de configuração, senha do caixa, scanner, HttpClient, navegação, tela de Sucesso, toasts e rota são mockados nos testes; a câmera e a primeira configuração devem ser validadas manualmente em um Android depois de `npx cap sync android`.
