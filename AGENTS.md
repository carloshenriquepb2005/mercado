# Orientações para agentes

- Leia `especificacao-codex-app-precificacao.md` antes de alterar o projeto.
- Não altere o schema legado e não adicione migrations.
- As únicas escritas permitidas no legado são, sempre pelo `ID` de `produtos`: `PRECO_UNITARIO` (alteração de preço) e `ESTOQUE_ATUAL` + `PRECO_CUSTO` + `DATA_ATUALIZACAO` e, somente quando o usuário informar, `DATA_VENCIMENTO` (entrada no estoque, seção 15 da especificação). Não utilize outras colunas sem autorização expressa.
- Não utilize o banco real em testes.
- Preserve a arquitetura hexagonal moderada e mantenha domínio/aplicação sem Spring/JPA.
- Controllers devem possuir classe de swagger individuais.
- Regras de negócio apenas em UseCases e Services.

# 1. Arquitetura desejada

A estrutura principal deve ser:

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

- Crie ou atualize testes para toda mudança de comportamento e execute-os.
- Atualize a documentação quando contratos ou operação mudarem.
- Não adicione dependências sem necessidade e justificativa.
- Nunca registre ou versione credenciais; use as variáveis descritas em `env.example`.

