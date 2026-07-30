# API

## `GET /api/produtos/{ean}`

Retorna `200` com `id`, `codigoBarras`, `descricao` e `precoVenda`. Retorna `400` para código inválido e `404` quando principal/similar não forem encontrados.

## `PATCH /api/produtos/{id}`

Corpo: `{ "novoPreco": 10.50 }`. Atualiza apenas `PRECO_UNITARIO` pelo ID e retorna `204`. ID/preço inválidos retornam `400`; produto inexistente retorna `404`.

Erros seguem o contrato `timestamp`, `status`, `error`, `code`, `message` e `path`. Indisponibilidade de banco retorna `503`. A documentação interativa fica em `/swagger-ui.html`.

