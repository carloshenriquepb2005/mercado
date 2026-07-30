# Orientações para agentes

- Leia `especificacao-codex-app-precificacao.md` antes de alterar o projeto.
- Trate `backend-antigo/` somente como referência de leitura; nunca edite essa pasta.
- Não crie cadastro ou exclusão de produtos.
- Não altere o schema legado e não adicione migrations.
- A única escrita permitida no legado é `produtos.PRECO_UNITARIO`, sempre pelo `ID`.
- Não utilize o banco real em testes.
- Preserve a arquitetura hexagonal moderada e mantenha domínio/aplicação sem Spring/JPA.
- Crie ou atualize testes para toda mudança de comportamento e execute-os.
- Atualize a documentação quando contratos ou operação mudarem.
- Não adicione dependências sem necessidade e justificativa.
- Nunca registre ou versione credenciais; use as variáveis descritas em `env.example`.

