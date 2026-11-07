# API

## `POST /api/auth/login`

Corpo: `{ "username": "Carlos", "password": "senha" }`. Valida `username` contra `usuarios.nome` e `password` contra `usuarios.senha`, retornando `codigo`, `username`, `displayName` e `perfil`. Os perfis 1, 2 e 3 são aceitos sem distinção. Campos inválidos retornam `400`; credenciais incorretas retornam `401` com o código `CREDENCIAIS_INVALIDAS`.

O requisito atual usa comparação simples, sem hash. A senha não é devolvida pela API, e não existem endpoints para cadastrar ou alterar usuários.

## `GET /api/configuracoes/conexao`

Valida a comunicação entre a API e o banco legado consultando internamente o usuário de referência. Retorna `204` quando a conexão estiver validada e `503` em caso de indisponibilidade, sem expor o registro consultado.

## `POST /api/configuracoes/validar-senha-caixa`

Corpo: `{ "senha": "1234" }`. Verifica somente para leitura se o valor existe em `senha.SENHA_CAIXA`. Retorna `204` quando válida, `400` para campo vazio ou maior que 10 caracteres e `401` com o código `SENHA_CAIXA_INVALIDA` quando não encontrada. A senha não é persistida nem devolvida.

## `GET /api/produtos/{ean}`

Retorna `200` com `id`, `codigoBarras`, `descricao`, `unidade`, `precoVenda`, `precoCusto`, `codigoNcm` e `estoqueAtual`. Os campos correspondem, respectivamente, a `ID`, `CODIGO_PRODUTO`, `DESCRICAO`, `UN`, `PRECO_UNITARIO`, `PRECO_CUSTO`, `CODIGO_NCM` e `ESTOQUE_ATUAL` da tabela legada `produtos`; `unidade`, `precoVenda`, `precoCusto`, `codigoNcm` e `estoqueAtual` podem ser nulos. A operação é somente leitura. Retorna `400` para código inválido e `404` quando principal/similar não forem encontrados.

## `PATCH /api/produtos/{id}`

Corpo: `{ "novoPreco": 10.50 }`. Atualiza apenas `PRECO_UNITARIO` pelo ID e retorna `204`. ID/preço inválidos retornam `400`; produto inexistente retorna `404`.

## `POST /api/produtos/{id}/entrada-estoque`

Corpo: `{ "quantidade": 12.500, "precoCusto": 7.35, "dataVencimento": "2027-03-15" }`. `dataVencimento` é opcional (`yyyy-MM-dd`); omitida, nula ou vazia significa "não informada" e a coluna não é alterada.

Em **uma única instrução `UPDATE`** pelo `ID`, soma `quantidade` a `ESTOQUE_ATUAL` (nulo conta como `0`), grava `PRECO_CUSTO` e `DATA_ATUALIZACAO` (data atual em `America/Sao_Paulo`) e, somente se informada, `DATA_VENCIMENTO`. Nenhuma outra coluna é alterada. A soma é feita no banco, portanto não sobrescreve vendas registradas ao mesmo tempo pelo sistema legado, e a instrução só altera a linha se o estoque resultante não passar de `9999999.999`.

Regras: `quantidade` obrigatória, maior que zero, até 3 casas decimais e no máximo `9999999.999`; `precoCusto` obrigatório, maior que zero, até 2 casas decimais e no máximo `99999999.99`.

Retorna `200` com `produtoId`, `estoqueAnterior`, `estoqueAtual`, `precoCustoAnterior` (pode ser nulo) e `precoCustoAtual`; `estoqueAtual` e `precoCustoAtual` são relidos do banco após a gravação. Dados inválidos (inclusive data inexistente ou estoque acima do limite) retornam `400` com `DADOS_INVALIDOS`; produto inexistente retorna `404`; banco indisponível retorna `503`. A data de vencimento e a de atualização não são devolvidas.

Erros seguem o contrato `timestamp`, `status`, `error`, `code`, `message` e `path`. Indisponibilidade de banco retorna `503`. A documentação interativa fica em `/swagger-ui.html`.
