# Plano — Feature "Entrada no Estoque" (v3)

Fluxo: **Home → card "Entrada no Estoque" → scanner → tela do produto (código, descrição, UN, preço unitário, preço de custo, estoque atual) → informar quantidade comprada + novo preço de custo + (opcional) data de vencimento → salvar → tela de sucesso.**

> [!NOTE]
> **v3:** `DATA_ATUALIZACAO` passa a ser gravada em toda entrada; `DATA_VENCIMENTO` é opcional e só é gravada se o usuário informar.

> [!NOTE]
> **Status:** somente planejamento. As especificações já foram atualizadas (`AGENTS.md` e `especificacao-codex-app-precificacao.md`, seções 2, 14, **15 (nova)** e 50). Nenhum código foi alterado. `README.md` e `docs/*` serão atualizados na implementação (item 6).

## 1. Decisões fechadas

| # | Decisão |
|---|---|
| 1 | Escrita em `ESTOQUE_ATUAL` e `PRECO_CUSTO` **autorizada**. |
| 2 | Schema real recebido e registrado na spec (§14.1). |
| 3 | Quantidade fracionada: até 3 casas decimais, `> 0`, teto `9999999.999`. |
| 4 | Custo obrigatório, `> 0`, até 2 casas decimais. Se custo > preço unitário: só **aviso**, sem bloquear. |
| 5 | O card **"Etiquetas" é substituído** por "Entrada no Estoque" (ativo). "Inventário" permanece desativado. |
| 6 | **Nenhuma alteração em logs** nesta fase (nem novos logs, nem usuário no log). |
| 7 | `DATA_ATUALIZACAO` **gravada** em toda entrada bem-sucedida (data atual, `America/Sao_Paulo`). |
| 8 | `DATA_VENCIMENTO` **opcional**: sem input do usuário, a coluna não é tocada; com input, é gravada. |
| — | **Somente colunas autorizadas** (ver §2). |
| — | Após erro, o app **recarrega o produto atualizado** para evitar duplicidade (ver §5). |

## 2. Colunas do banco

Schema real (resumo do `SHOW CREATE TABLE produtos` informado):

| Coluna | Tipo real | Uso na feature |
|---|---|---|
| `ID` | `int(10) unsigned` | chave do `UPDATE` |
| `CODIGO_PRODUTO` | `varchar(13) NOT NULL`, índice **não único** | leitura |
| `DESCRICAO` | `varchar(60) NOT NULL` | leitura |
| `UN` | `varchar(3) NULL` | leitura |
| `PRECO_UNITARIO` | `decimal(10,2) NULL` | leitura (somente exibição e aviso) |
| `PRECO_CUSTO` | `decimal(10,2) NULL` | **leitura + escrita** |
| `ESTOQUE_ATUAL` | `decimal(10,3) NULL` | **leitura + escrita** |
| `DATA_ATUALIZACAO` | `date NULL` | **somente escrita**, sempre (data atual) |
| `DATA_VENCIMENTO` | `date NULL` | **somente escrita**, só se o usuário informar |
| `CODIGO_NCM` | `varchar(8) NULL` | já mapeado pela consulta; não usado nem exibido na entrada |

**Não mapear, ler nem gravar:** `ID_GRUPO, ESTOQUE_MIN, ALIQUOTA, ALIQUOTA_IBPT, DATA_ULTIMA_COMPRA, CODIGO_BAIXA, QTDE_BAIXA, PRODUTO_PADARIA, ATACADO_QTDE, ATACADO_VALOR, ATACADO_HABILITA`.
Em especial, a entrada **não** atualiza `DATA_ULTIMA_COMPRA`. A alteração de preço (fluxo existente) continua gravando só `PRECO_UNITARIO`.

**`DATA_ATUALIZACAO` e `DATA_VENCIMENTO` são somente escrita:** não entram em `ProdutoJpaEntity`, no modelo `Produto` nem nas respostas. Por isso as queries de entrada são **nativas** (`@Query(nativeQuery = true)`): mapear essas colunas na entidade faria o Hibernate lê-las em todo `SELECT`, contrariando "não ler".

**Consequências do schema real**
- `ESTOQUE_ATUAL`, `PRECO_CUSTO`, `PRECO_UNITARIO` e `UN` aceitam `NULL`. A API e o app tratam nulo (exibe "—"; estoque nulo conta como `0` no incremento).
- `ProdutoJpaEntity.precoUnitario` está com `nullable = false`, o que diverge do schema real. Corrijo a anotação junto com o mapeamento, sem alterar o banco. O mesmo vale para o tipo `Integer` do `ID` (unsigned) — suficiente, pois `AUTO_INCREMENT=12464`.
- `ESTOQUE_ATUAL decimal(10,3)` limita o valor a `9999999.999`. Em MySQL 5.5 sem modo estrito, estouro pode ser truncado em silêncio. Por isso o limite é verificado **dentro do próprio `UPDATE`** (ver §3).

## 3. Backend (`br.com.mercado.precificacao`)

Padrão hexagonal do projeto, sem Spring/JPA em `core`/`port`. Regras de negócio apenas em UseCase e Service. Nomes com sufixo do pacote.

**Contrato** (spec §15.4)

```
POST /api/produtos/{id}/entrada-estoque
Body:  { "quantidade": 12.500, "precoCusto": 7.35, "dataVencimento": "2027-03-15" }
       (dataVencimento opcional; omitido/nulo = coluna não alterada)
200:   { "produtoId":150, "estoqueAnterior":18.500, "estoqueAtual":31.000,
         "precoCustoAnterior":6.90, "precoCustoAtual":7.35 }
400 DADOS_INVALIDOS | 404 PRODUTO_NAO_ENCONTRADO | 503 BANCO_INDISPONIVEL
```

`GET /api/produtos/{ean}` passa a devolver também `precoCusto`.

**Novos arquivos**

| Pacote | Arquivo |
|---|---|
| `port/input` | `RegistrarEntradaEstoqueInputPort` |
| `port/output` | `RegistrarEntradaEstoqueOutputPort` (`boolean registrarEntrada(Long id, BigDecimal quantidade, BigDecimal precoCusto, LocalDate dataAtualizacao, LocalDate dataVencimento)`; `dataVencimento` nula = não gravar) |
| `core/usecase` | `RegistrarEntradaEstoqueUseCase` |
| `core/model` | `EntradaEstoque` (estoque e custo, anterior e atual) |
| `core/exception` | `QuantidadeInvalidaException`, `EstoqueLimiteExcedidoException` |
| `adapter/input/controller` | `EntradaEstoqueController` |
| `adapter/input/swagger` | `EntradaEstoqueControllerSwagger` (individual, conforme `AGENTS.md`) |
| `adapter/input/request` | `RegistrarEntradaEstoqueRequest` (`@NotNull/@DecimalMin/@DecimalMax/@Digits`) |
| `adapter/input/response` | `EntradaEstoqueResponse` |
| `adapter/input/mapper` | `EntradaEstoqueInputMapper` |

**Alterados**

- `Produto`, `ProdutoJpaEntity`, `ProdutoPersistenceMapper`, `ProdutoInputMapper`, `ProdutoResponse`: adicionar `precoCusto` (`BigDecimal`). Os construtores mudam; ajustar os testes existentes.
- `ProdutoService`: `validarQuantidadeEntrada` e `validarPrecoCusto`, reaproveitando a validação de preço de forma genérica (sem duplicar).
- `ProdutoJpaRepository`: **duas** queries nativas explícitas (`@Modifying`), alterando **somente** as colunas autorizadas. O adapter escolhe uma conforme `dataVencimento` seja nula ou não; isso evita `COALESCE(:param, coluna)` com parâmetro nulo (inferência de tipo) e garante que, sem input, `DATA_VENCIMENTO` nem aparece no `SET`:
  ```sql
  -- sem data de vencimento
  UPDATE produtos
     SET ESTOQUE_ATUAL    = COALESCE(ESTOQUE_ATUAL, 0) + :quantidade,
         PRECO_CUSTO      = :precoCusto,
         DATA_ATUALIZACAO = :dataAtualizacao
   WHERE ID = :id
     AND COALESCE(ESTOQUE_ATUAL, 0) + :quantidade <= :limiteEstoque

  -- com data de vencimento (mesmo UPDATE + uma coluna)
  UPDATE produtos
     SET ESTOQUE_ATUAL    = COALESCE(ESTOQUE_ATUAL, 0) + :quantidade,
         PRECO_CUSTO      = :precoCusto,
         DATA_ATUALIZACAO = :dataAtualizacao,
         DATA_VENCIMENTO  = :dataVencimento
   WHERE ID = :id
     AND COALESCE(ESTOQUE_ATUAL, 0) + :quantidade <= :limiteEstoque
  ```
- `BeanConfiguration`: bean `Clock` (`America/Sao_Paulo`) injetado no UseCase, que calcula `dataAtualizacao` (assim o teste fixa a data).
- `ProdutoService`: `validarDataVencimento` (opcional; vazio = ausente).
- `RegistrarEntradaEstoqueRequest`: `dataVencimento` como `LocalDate` opcional (`yyyy-MM-dd`); data inválida cai em `HttpMessageNotReadableException` → `400`. Sem restrição de data passada/futura.
- `ProdutoPersistenceAdapter`: implementa o novo port com `@Transactional`.
- `BeanConfiguration`: bean do novo UseCase.
- `GlobalExceptionHandler`: mapear `QuantidadeInvalidaException` e `EstoqueLimiteExcedidoException` para `400` (`DADOS_INVALIDOS`). **Sem tocar na política de logs.**

> [!IMPORTANT]
> **Soma atômica no banco.** O sistema legado VB.NET também altera `ESTOQUE_ATUAL` (vendas). Por isso o incremento é um único `UPDATE ... estoque = estoque + :qtd`, e não "ler, somar em Java, gravar". Todas as colunas da entrada (estoque, custo, `DATA_ATUALIZACAO` e, se houver, `DATA_VENCIMENTO`) mudam na mesma instrução: ou todas, ou nenhuma. Explicação detalhada na resposta desta revisão.

**Fluxo do UseCase**
1. Validar `id` (> 0), quantidade e custo via `ProdutoService`.
2. `buscarPorId`: se não existir, `ProdutoNaoEncontradoException`. Guarda estoque e custo anteriores.
3. `registrarEntrada` (UPDATE atômico com o limite na cláusula `WHERE`).
4. Se retornar `0` linhas: reler o produto. Se ainda existir → `EstoqueLimiteExcedidoException`; se não → `ProdutoNaoEncontradoException`.
5. Reler o produto para devolver o `estoqueAtual` real (já considerando vendas concorrentes) e montar `EntradaEstoque`.

## 4. App móvel (Angular/Ionic)

**Rotas** ([app.routes.ts](file:///c:/Users/chpb2/OneDrive/%C3%81rea%20de%20Trabalho/PASTAS/Arquivos/Projeto%20Tio%20Bruno/Mercado/mobile/src/app/app.routes.ts)), com `configurationGuard` e `authGuard`:
- `scanner/entrada`: reaproveita `ScannerPage` com o novo modo (eyebrow "ENTRADA NO ESTOQUE");
- `entrada-estoque/:codigo`: nova `StockEntryPage`;
- `entrada-estoque-sucesso`: nova `StockEntrySuccessPage`.

**Mudanças**
- **Home** ([home.page.html](file:///c:/Users/chpb2/OneDrive/%C3%81rea%20de%20Trabalho/PASTAS/Arquivos/Projeto%20Tio%20Bruno/Mercado/mobile/src/app/features/home/pages/home/home.page.html)): **remover** o card desativado "Etiquetas" e adicionar `<app-home-action-card title="Entrada no Estoque" icon="download-outline" link="/scanner/entrada" />`. Registrar `downloadOutline` em `addIcons`. A grade mantém a mesma quantidade de cards, então o layout não muda.
- **Scanner:** tipo `'consulta' | 'alteracao' | 'entrada'`; mapa `{ consulta: '/consulta-estoque', alteracao: '/produto', entrada: '/entrada-estoque' }` no lugar do ternário.
- **`ProdutoResponse` (TS):** `precoCusto: number | null`.
- **`ProdutoApiService`:** `registrarEntradaEstoque(id, quantidade, precoCusto)`.
- **`StockEntryPage`** (`features/stock-entry/`):
  - exibe **somente** código, descrição, UN, preço unitário, preço de custo e estoque atual (componente novo, ou `ProductCardComponent` estendido sem exibir o NCM);
  - Reactive Forms: `quantidade` (novo `quantity.validator.ts`, até 3 casas), `precoCusto` (máscara de centavos + `priceValidator`) e `dataVencimento` **opcional** (input de data, exibida `dd/MM/yyyy`, enviada `yyyy-MM-dd`; vazio = campo omitido do corpo, nunca `null` nem string vazia);
  - após erro, a recarga do produto limpa também `dataVencimento`;
  - prévia apenas visual "Estoque: X → Y";
  - aviso (não bloqueante) quando custo > preço unitário e o preço unitário não é nulo;
  - botão "Registrar entrada" desabilitado enquanto inválido ou salvando;
  - erros do `AppError` mostrados na tela (padrão atual).
- **Sucesso:** toast e tela com estoque anterior→atual e custo anterior→atual, usando os valores **devolvidos pela API**. Botões "Nova entrada" e "Home". Segue o padrão do `PricingSuccessStore`.
- **Refatoração mínima:** extrair o `formatPrice` para `shared/utils/` e reutilizar nas duas telas.

## 5. Proteção contra duplicidade (spec §15.5)

Como não há tabela de idempotência (e não é permitido criar uma), após qualquer erro **sem resposta de sucesso** (rede, timeout, `500`, `503`) o app:

1. **recarrega o produto** e exibe estoque e custo **atualizados**;
2. limpa o campo de quantidade e mostra o aviso "Confira o estoque atual antes de registrar novamente";
3. só permite novo envio após o atendente informar e confirmar de novo;
4. **nunca reenvia automaticamente**.

Erros `400` e `404` não disparam recarga automática, pois a entrada não foi gravada.

## 6. Testes (sem banco real) e documentação

**Backend** (JUnit 5 + Mockito, padrão Arrange/Act/Assert dos testes atuais)
- `RegistrarEntradaEstoqueUseCaseTest`: sucesso (com `Clock` fixo, verifica `DATA_ATUALIZACAO` = data atual); **com e sem `dataVencimento`** (sem: port recebe `null`); ID inválido; quantidade nula, zero, negativa ou com mais de 3 casas; custo inválido; produto inexistente (na busca e no update com 0 linhas); limite de estoque excedido; estoque anterior nulo; nada gravado quando a validação falha.
- `ProdutoServiceTest`: novas validações e limites.
- `EntradaEstoqueControllerTest` (MockMvc): `200`, `400`, `404`, `503` e corpo inválido.
- `ProdutoPersistenceAdapterTest` e teste do mapper: mapeamento de `precoCusto` (incluindo nulo); adapter escolhe a query **sem** vencimento quando nulo e **com** vencimento quando informado (mocks do repository).
- `EntradaEstoqueControllerTest`: `dataVencimento` omitida, nula, válida e inválida (`400`).
- Atualizar `ProdutoTest`, `ProdutoInputMapperTest` e `ProdutoControllerTest` para o novo campo.

**Mobile** (Jasmine/Karma)
- `stock-entry.page.spec.ts`: validação do formulário, chamada à API, sucesso, **recarga do produto após erro de rede**, limpeza do campo de quantidade, bloqueio de duplo envio, valores nulos exibidos como "—".
- `quantity.validator.spec.ts` e a util de máscara extraída.
- `produto-api.service.spec.ts`: endpoint e corpo.
- `scanner.page.spec.ts` (modo `entrada`) e spec da home (card "Etiquetas" removido; novo card ativo com o link correto).

**Execução:** `./mvnw clean verify` (backend) e `npm run lint && npm test && ionic build` (mobile).

**Documentação** (na implementação): `README.md` (l.183 e descrição da Home), `docs/requisitos.md`, `docs/api.md`, `docs/arquitetura.md` e `docs/testes.md`.

## 7. Ordem de execução

1. Backend: modelo/entidade/mapper (+ `precoCusto`) → `ProdutoService` → ports e UseCase → repository e adapter → controller/swagger → handler → testes → `verify`.
2. Mobile: modelo e API service → validator e util → scanner e rotas → página de entrada → sucesso → home → testes → lint e build.
3. Documentação (`README.md` e `docs/*`).
4. Verificação manual contra um banco **de teste** (nunca o real): `ESTOQUE_ATUAL` somado, `PRECO_CUSTO` e `DATA_ATUALIZACAO` atualizados; `DATA_VENCIMENTO` alterada só quando informada (e intacta quando não); nenhuma outra coluna alterada (em especial `DATA_ULTIMA_COMPRA`).

## 8. Fora de escopo

Logs (nenhuma alteração), histórico de entradas, cadastro ou exclusão de produtos, alteração de `PRECO_UNITARIO` na entrada, cache offline, mudança de schema, migrations e dependências novas.
