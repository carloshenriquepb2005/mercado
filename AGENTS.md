# Orientações para agentes

- Leia `especificacao-codex-app-precificacao.md` antes de alterar o projeto.
- Não altere o schema legado e não adicione migrations.
- A única escrita permitida no legado é `produtos.PRECO_UNITARIO`, sempre pelo `ID`.
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

