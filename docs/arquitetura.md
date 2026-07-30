# Arquitetura

```text
Ionic/Angular no Android
        │ HTTP na Wi-Fi local
        ▼
Controller REST Spring Boot
        ▼
Portas de entrada → casos de uso → domínio
        ▼
Portas de saída → adapter Spring Data JPA
        ▼
MySQL 5.5 legado → PDV VB.NET
```

O domínio e os casos de uso são Java puro. Controllers, validação HTTP, JPA, CORS, OpenAPI e health check ficam nos adapters e na infraestrutura. O frontend é organizado por funcionalidades; páginas consomem serviços e o scanner nativo fica encapsulado. O fluxo visual é `Login → Home → Scanner → Produto → Sucesso`.

O login simples consulta a tabela legada `usuarios` por `nome` e `senha`, sem hash, e aceita igualmente os perfis 1, 2 e 3. O aplicativo guarda somente código, nome e perfil na sessão local; a senha não é persistida. Esse acesso funciona como controle de entrada da interface e não adiciona token aos endpoints de produto. A tabela de usuários é somente leitura.

A API resolve primeiro `produtos.CODIGO_PRODUTO`; caso não encontre, resolve `produtos_similares.CODIGO_SIMILAR` para o código principal. A única escrita é uma transação que altera `produtos.PRECO_UNITARIO` pelo `ID`.

Para compatibilidade com o MySQL 5.5, o backend usa Connector/J 5.1.49 e o dialeto legado da comunidade Hibernate. O schema fica integralmente sob responsabilidade do sistema existente (`ddl-auto: none`).
