# Especificação de Implementação — App Satélite de Precificação

A solução será composta por:

- backend em Java com Spring Boot;
- aplicativo móvel em Angular, Ionic e Capacitor;
- integração com banco legado MySQL 5.5;
- comunicação exclusivamente pela rede local da loja.

---

# 2. Contexto do sistema

O mercado já possui um sistema legado desenvolvido em VB.NET.

Esse sistema utiliza um banco de dados MySQL 5.5 instalado localmente na máquina ou servidor do estabelecimento.

Ele funcionará como uma extensão móvel especializada em consulta de produtos, alteração de preços e entrada no estoque.

O aplicativo móvel nunca deve acessar diretamente o banco de dados.

---

# 4. Stack tecnológica

## 4.1 Backend

- Java 21 recomendado.
- Spring Boot 3.x.
- Maven.
- Spring Web.
- Spring Data JPA.
- Hibernate.
- MySQL JDBC Driver compatível com o banco legado.
- Jakarta Bean Validation.
- Spring Boot Actuator.
- JUnit 5.
- Mockito.
- Springdoc OpenAPI.
- JaCoCo para cobertura de testes.

## 4.2 Aplicativo móvel

- Angular com componentes standalone.
- TypeScript.
- Ionic Framework.
- Capacitor.
- SCSS.
- Reactive Forms.
- Angular Signals para estado local.
- HttpClient.
- Plugin de leitura de código de barras compatível com a versão adotada do Capacitor.
- Android como plataforma inicial.
- Jasmine/Karma ou Vitest, conforme o padrão oficial da versão adotada do Angular.
- ESLint.
- Prettier opcional.

Antes de instalar o plugin de scanner, verificar qual pacote possui suporte ativo e compatibilidade com a versão do Capacitor utilizada.

---

# 5. Estrutura geral do repositório

```text
app-precificacao/
├── backend/
├── mobile/
├── docs/
├── .editorconfig
├── .gitignore
├── README.md
└── AGENTS.md
```

## 5.1 Pasta `backend`

Contém a API Spring Boot.

## 5.2 Pasta `mobile`

Contém o aplicativo Ionic/Angular.



# 7. Estrutura de pastas do backend

Usar a seguinte estrutura:

```text
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
```

---

# 14. Mapeamento do banco legado

## 14.1 Tabela `produtos`

```text
ID                int(10) unsigned, PK, auto increment
CODIGO_PRODUTO    varchar(13), not null, index não único (Index_2)
DESCRICAO         varchar(60), not null (Index_3)
UN                varchar(3), nulo permitido
PRECO_UNITARIO    decimal(10,2), nulo permitido
PRECO_CUSTO       decimal(10,2), nulo permitido
CODIGO_NCM        varchar(8), nulo permitido
ESTOQUE_ATUAL     decimal(10,3), nulo permitido (quantidade atual em estoque)
DATA_ATUALIZACAO  date, nulo permitido (somente escrita, seção 15)
DATA_VENCIMENTO   date, nulo permitido (somente escrita, opcional, seção 15)
```

Schema real validado (`SHOW CREATE TABLE produtos`, MySQL 5.5, `InnoDB`, `latin1`). O índice de `CODIGO_PRODUTO` **não é único**: a unicidade não deve ser presumida sem validação.

Colunas do schema real que o app **não utiliza** e que **não devem ser mapeadas, lidas nem gravadas**:

```text
ID_GRUPO, ESTOQUE_MIN, ALIQUOTA, ALIQUOTA_IBPT, DATA_ULTIMA_COMPRA,
CODIGO_BAIXA, QTDE_BAIXA, PRODUTO_PADARIA,
ATACADO_QTDE, ATACADO_VALOR, ATACADO_HABILITA
```

Colunas lidas pelo app: `ID`, `CODIGO_PRODUTO`, `DESCRICAO`, `UN`, `PRECO_UNITARIO`, `PRECO_CUSTO`, `CODIGO_NCM` e `ESTOQUE_ATUAL`. `DATA_ATUALIZACAO` e `DATA_VENCIMENTO` são **somente escritas** (não são lidas, não entram em entidade JPA de leitura, no modelo de domínio nem em respostas da API). Qualquer outra coluna só pode ser utilizada com autorização expressa e registro nesta especificação.

Escritas autorizadas no legado (sempre pelo `ID`, por `UPDATE` explícito):

- alteração de preço: somente `PRECO_UNITARIO`;
- entrada no estoque (seção 15): somente `ESTOQUE_ATUAL`, `PRECO_CUSTO` e `DATA_ATUALIZACAO`, mais `DATA_VENCIMENTO` **apenas quando informada pelo usuário**, tudo no mesmo `UPDATE`.

## 14.2 Tabela `produtos_similares`

```text
CODIGO_SIMILAR    varchar(13)
CODIGO_PRODUTO    varchar(13)
```

---

# 15. Feature — Entrada no Estoque

## 15.1 Fluxo

1. Na Home, o atendente toca no card **Entrada no Estoque**, que substitui o card "Etiquetas" e fica ativo.
2. O app abre o scanner no modo `entrada` (câmera ou digitação manual do código, como nos demais modos).
3. O app exibe `CODIGO_PRODUTO`, `DESCRICAO`, `UN`, `PRECO_UNITARIO`, `PRECO_CUSTO` e `ESTOQUE_ATUAL` do produto. Valores nulos são exibidos como "—".
4. O atendente informa a **quantidade comprada**, o **novo preço de custo** e, opcionalmente, a **data de vencimento**.
5. Ao confirmar, a API grava `ESTOQUE_ATUAL = ESTOQUE_ATUAL + quantidade`, `PRECO_CUSTO = novo preço de custo` e `DATA_ATUALIZACAO = data atual`; grava `DATA_VENCIMENTO` somente se a data foi informada.
6. O app exibe o sucesso da operação com os valores anteriores e atuais devolvidos pela API.

## 15.2 Escrita no legado

- Uma única instrução `UPDATE` explícita em `produtos`, pelo `ID`, alterando **somente** `ESTOQUE_ATUAL`, `PRECO_CUSTO`, `DATA_ATUALIZACAO` e, condicionalmente, `DATA_VENCIMENTO`.
- `DATA_ATUALIZACAO` é gravada em toda entrada bem-sucedida, com a data atual no fuso `America/Sao_Paulo`, obtida pelo UseCase por `Clock` injetável (testável) e repassada ao port de saída.
- `DATA_VENCIMENTO` só entra no `SET` quando o usuário informou a data. Sem input, a coluna **não aparece no `UPDATE`** e mantém o valor atual (inclusive `NULL`). Nunca gravar `NULL` por omissão. Como não há opção de limpar a data, o app não envia `null` explícito.
- Implementação: duas instruções explícitas (com e sem `DATA_VENCIMENTO`), escolhidas pelo adapter conforme o dado recebido, para evitar o `COALESCE` com parâmetro nulo (inferência de tipo). Como `DATA_ATUALIZACAO` e `DATA_VENCIMENTO` não devem ser lidas nem mapeadas na entidade, as duas instruções são `@Query(nativeQuery = true)`.
- O incremento do estoque deve ser feito no banco (`ESTOQUE_ATUAL = COALESCE(ESTOQUE_ATUAL, 0) + :quantidade`), nunca por "ler, somar em memória e gravar", pois o sistema legado também altera `ESTOQUE_ATUAL` (vendas).
- Estoque e custo são alterados na mesma instrução: ou ambos, ou nenhum.
- Não alterar `PRECO_UNITARIO` nem qualquer outra coluna nesta operação. Em especial, **não** atualizar `DATA_ULTIMA_COMPRA`. A alteração de preço (fluxo existente) continua gravando somente `PRECO_UNITARIO`, sem `DATA_ATUALIZACAO`.
- Proibido: `INSERT`, `DELETE`, alteração de schema e migrations.

## 15.3 Regras de negócio (somente em UseCase e Service)

- `quantidade`: obrigatória, maior que zero, no máximo 3 casas decimais e no máximo `9999999.999` (`decimal(10,3)`).
- `precoCusto`: obrigatório, maior que zero, no máximo 2 casas decimais e no máximo `99999999.99` (`decimal(10,2)`).
- `dataVencimento`: opcional. Se informada, deve ser uma data válida no formato `yyyy-MM-dd`; texto vazio é tratado como "não informada". Não há restrição de data passada ou futura.
- `ESTOQUE_ATUAL` nulo é tratado como `0` no incremento.
- O estoque resultante não pode exceder `9999999.999`. A condição é verificada no próprio `UPDATE` (cláusula `WHERE`) para ser atômica; sem linha afetada, o UseCase distingue produto inexistente de limite excedido.
- Produto inexistente retorna `404`; dados inválidos retornam `400`; banco indisponível retorna `503`.
- Valores monetários e quantidades usam `BigDecimal` (nunca `double`).
- Se `PRECO_CUSTO` informado for maior que `PRECO_UNITARIO` (e `PRECO_UNITARIO` não for nulo), o app exibe apenas um **aviso**, sem bloquear.

## 15.4 Contrato da API

```text
POST /api/produtos/{id}/entrada-estoque
Body: { "quantidade": 12.500, "precoCusto": 7.35, "dataVencimento": "2027-03-15" }
      ("dataVencimento" é opcional; omitido ou nulo = coluna não alterada)

200: { "produtoId": 150,
       "estoqueAnterior": 18.500, "estoqueAtual": 31.000,
       "precoCustoAnterior": 6.90, "precoCustoAtual": 7.35 }
400 DADOS_INVALIDOS | 404 PRODUTO_NAO_ENCONTRADO | 503 BANCO_INDISPONIVEL
```

`GET /api/produtos/{ean}` passa a retornar também `precoCusto`, mapeado de `PRECO_CUSTO`.

## 15.5 Proteção contra duplicidade

Não existe tabela de idempotência e não é permitido criá-la. Para evitar somar a mesma entrada duas vezes:

- o botão de salvar fica desabilitado durante o envio;
- após erro **sem resposta HTTP de sucesso** (rede, timeout, `503`, `500`), o app **recarrega o produto** com as informações atualizadas, exibe o estoque e o custo atuais, limpa o campo de quantidade e exige nova confirmação do atendente antes de permitir novo envio;
- o app nunca reenvia automaticamente.

## 15.6 Fora de escopo desta feature

- Alterações em logs: nenhum log novo é criado nesta fase, e os logs existentes são mantidos como estão. O registro em log do sucesso da entrada fica para uma etapa futura.
- Histórico de entradas em banco, cadastro/exclusão de produtos, alteração de `PRECO_UNITARIO`, cache offline.

---

# 35. Conectividade

Criar `ConnectivityService`.

Responsabilidades:

- consultar `/actuator/health`;
- expor estado da API;
- permitir nova tentativa;
- diferenciar erro de rede de produto não encontrado.

Não implementar cache offline.

---

# 39. Padrões de qualidade

- usar nomes claros com sufixo do pacote pertencente a classe, exceto model. Ex para ports: NomeOutputPort;
- não expor entidades JPA;
- não versionar secrets;
- manter métodos coesos.

---

# 40. Logs

Registrar:

- inicialização;
- indisponibilidade do banco;
- tentativas de atualização;
- atualizações do banco concluídas com sucesso;
- erros inesperados.

Não registrar:

- senha;
- credenciais;
- SQL sensível;
- tokens;
- stack trace em resposta HTTP.

---

# 42. Build do backend

```bash
./mvnw clean test
./mvnw clean verify
./mvnw clean package
```

O build deve falhar se os testes falharem.

---

# 43. Build do aplicativo

```bash
npm install
npm run lint
npm test
ionic build
npx cap sync android
npx cap open android
```

Gerar APK assinado.

---

# 50. Restrições

Não se deve:

- alterar tabelas;
- criar migrations;
- cadastrar produtos;
- excluir produtos;
- alterar preço de custo fora do fluxo de Entrada no Estoque (seção 15);
- gravar em colunas diferentes de `PRECO_UNITARIO` (alteração de preço) e de `ESTOQUE_ATUAL`/`PRECO_CUSTO`/`DATA_ATUALIZACAO`/`DATA_VENCIMENTO` (entrada no estoque);
- gravar `DATA_VENCIMENTO` quando o usuário não informou a data (a coluna deve permanecer inalterada);
- mapear, ler ou gravar colunas do legado não autorizadas na seção 14;
- somar estoque em memória e gravar o resultado (o incremento deve ser atômico no banco);
- acessar banco diretamente pelo app;
- usar `ddl-auto=update`, `create` ou `create-drop`;
- usar produção em testes;
- usar `double` para preço;
- acoplar domínio a Spring ou JPA;
- adicionar NgRx;
- inventar colunas;
- presumir unicidade sem validar;
- ignorar testes falhos.

Em divergência com o schema real:

1. preservar o legado;
2. não alterar automaticamente;
3. documentar;
4. adaptar o mapeamento;
5. solicitar decisão humana quando a regra mudar.

---
