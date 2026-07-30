# Especificação de Implementação — App Satélite de Precificação

## 1. Objetivo deste documento

Este documento deve ser utilizado pelo Codex como fonte principal para implementar o projeto completo do **App Satélite de Precificação**.

O sistema será uma aplicação móvel interna utilizada por atendentes de um mercado para:

1. escanear o código de barras de um produto;
2. consultar os dados atuais do produto;
3. informar um novo preço de venda;
4. atualizar exclusivamente o preço de venda no banco legado;
5. refletir a alteração imediatamente no sistema de PDV já existente.

A solução será composta por:

- backend em Java com Spring Boot;
- aplicativo móvel em Angular, Ionic e Capacitor;
- integração com banco legado MySQL 5.5;
- comunicação exclusivamente pela rede local da loja.

O Codex deve seguir as decisões arquiteturais, restrições, padrões de código, testes e critérios de aceite descritos neste documento.

---

# 2. Contexto do sistema

O mercado já possui um sistema legado desenvolvido em VB.NET.

Esse sistema utiliza um banco de dados MySQL 5.5 instalado localmente na máquina ou servidor do estabelecimento.

O novo aplicativo não substituirá o sistema existente.

Ele funcionará como uma extensão móvel especializada em consulta de produtos e alteração de preços.

Arquitetura operacional:

```text
Celular Android do atendente
        |
        | Wi-Fi da loja
        v
API Spring Boot no servidor local
        |
        v
MySQL 5.5 legado
        |
        v
Sistema legado VB.NET / PDV
```

O aplicativo móvel nunca deve acessar diretamente o banco de dados.

Toda leitura e atualização deve ocorrer por meio da API Spring Boot.

---

# 3. Escopo funcional

A primeira versão deve permitir:

- abrir o aplicativo;
- verificar se a API local está disponível;
- iniciar o leitor de código de barras;
- solicitar permissão de câmera;
- capturar um EAN;
- consultar produto pelo código principal;
- consultar produto por código similar;
- exibir produto encontrado;
- exibir preço atual;
- informar novo preço;
- validar o novo preço;
- atualizar o preço pelo ID do produto;
- exibir feedback de sucesso;
- exibir mensagens de erro adequadas;
- retornar à tela de leitura após uma atualização concluída.

A primeira versão não deve implementar:

- cadastro de produtos;
- exclusão de produtos;
- alteração de descrição;
- alteração de preço de custo;
- alteração de estoque;
- alteração de código de barras;
- sincronização offline;
- acesso direto do app ao MySQL;
- migrations ou alteração automática do banco legado;
- publicação pública na Play Store;
- gerenciamento complexo de estado;
- microsserviços;
- mensageria;
- cache distribuído.

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

MapStruct é opcional. Não adicionar se os mapeamentos forem pequenos e claros.

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

Criar um único repositório com a seguinte estrutura:

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

## 5.3 Pasta `docs`

Criar inicialmente:

```text
docs/
├── arquitetura.md
├── requisitos.md
├── api.md
├── implantacao.md
└── testes.md
```

O presente documento pode ser salvo como:

```text
docs/especificacao-codex.md
```

---

# 6. Arquitetura do backend

Utilizar **Arquitetura Hexagonal moderada**.

O objetivo é manter domínio e casos de uso independentes de detalhes externos, sem criar abstrações desnecessárias.

Fluxo de dependências:

```text
Controller REST
      |
      v
Porta de entrada
      |
      v
Caso de uso
      |
      v
Domínio
      |
      v
Porta de saída
      |
      v
Adapter JPA
      |
      v
MySQL legado
```

As dependências devem apontar para as camadas internas.

O domínio não deve conhecer:

- Spring;
- JPA;
- Hibernate;
- HTTP;
- JSON;
- banco de dados;
- controllers;
- repositories.

---

# 7. Estrutura de pastas do backend

Usar a seguinte estrutura:

```text
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/mercado/precificacao/
│   │   │       ├── PrecificacaoApplication.java
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   │   └── Produto.java
│   │   │       │   └── exception/
│   │   │       │       ├── ProdutoNaoEncontradoException.java
│   │   │       │       └── PrecoInvalidoException.java
│   │   │       ├── application/
│   │   │       │   ├── port/
│   │   │       │   │   ├── in/
│   │   │       │   │   │   ├── BuscarProdutoPorCodigoUseCase.java
│   │   │       │   │   │   └── AtualizarPrecoProdutoUseCase.java
│   │   │       │   │   └── out/
│   │   │       │   │       ├── BuscarProdutoPort.java
│   │   │       │   │       ├── BuscarProdutoSimilarPort.java
│   │   │       │   │       └── AtualizarPrecoProdutoPort.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── BuscarProdutoService.java
│   │   │       │   │   └── AtualizarPrecoProdutoService.java
│   │   │       │   └── dto/
│   │   │       │       ├── ProdutoResult.java
│   │   │       │       └── AtualizacaoPrecoCommand.java
│   │   │       ├── adapter/
│   │   │       │   ├── in/
│   │   │       │   │   └── web/
│   │   │       │   │       ├── controller/
│   │   │       │   │       │   └── ProdutoController.java
│   │   │       │   │       ├── request/
│   │   │       │   │       │   └── AtualizarPrecoRequest.java
│   │   │       │   │       ├── response/
│   │   │       │   │       │   ├── ProdutoResponse.java
│   │   │       │   │       │   └── ApiErrorResponse.java
│   │   │       │   │       └── mapper/
│   │   │       │   │           └── ProdutoWebMapper.java
│   │   │       │   └── out/
│   │   │       │       └── persistence/
│   │   │       │           ├── entity/
│   │   │       │           │   ├── ProdutoJpaEntity.java
│   │   │       │           │   └── ProdutoSimilarJpaEntity.java
│   │   │       │           ├── repository/
│   │   │       │           │   ├── ProdutoJpaRepository.java
│   │   │       │           │   └── ProdutoSimilarJpaRepository.java
│   │   │       │           ├── mapper/
│   │   │       │           │   └── ProdutoPersistenceMapper.java
│   │   │       │           └── ProdutoPersistenceAdapter.java
│   │   │       └── infrastructure/
│   │   │           ├── config/
│   │   │           │   ├── BeanConfiguration.java
│   │   │           │   ├── CorsConfiguration.java
│   │   │           │   └── OpenApiConfiguration.java
│   │   │           ├── exception/
│   │   │           │   └── GlobalExceptionHandler.java
│   │   │           └── health/
│   │   │               └── DatabaseHealthIndicator.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── br/com/mercado/precificacao/
│               ├── domain/
│               ├── application/
│               ├── adapter/
│               └── architecture/
```

---

# 8. Regras de negócio obrigatórias

## RN01 — Imutabilidade estrutural

A API deve atuar apenas como leitor de produto e editor de preço.

A API não pode:

- inserir produtos;
- excluir produtos;
- alterar descrição;
- alterar código;
- alterar preço de custo;
- alterar estoque;
- alterar qualquer coluna diferente de `PRECO_UNITARIO`.

Não criar endpoints `POST`, `PUT` ou `DELETE` para produtos.

A atualização deve ser feita por uma query explícita que altere somente `PRECO_UNITARIO`.

## RN02 — Resolução de código de barras

Ao receber um EAN:

1. buscar o valor em `produtos.CODIGO_PRODUTO`;
2. se encontrado, retornar o produto;
3. se não encontrado, buscar em `produtos_similares.CODIGO_SIMILAR`;
4. se encontrado, obter o `CODIGO_PRODUTO` principal;
5. buscar o produto principal em `produtos`;
6. se não encontrado em nenhuma etapa, retornar HTTP 404.

## RN03 — Validação do preço

O novo preço:

- é obrigatório;
- deve ser numérico;
- deve ser maior que zero;
- deve aceitar no máximo duas casas decimais;
- deve respeitar o limite da coluna `decimal(10,2)`.

Valores inválidos devem retornar HTTP 400.

Usar `BigDecimal` no backend.

Nunca usar `double` ou `float` para preço.

## RN04 — Atualização por ID

A alteração do preço deve ser executada sempre pelo campo `ID` da tabela `produtos`.

O código lido serve apenas para localizar o produto.

O endpoint de atualização não deve atualizar por EAN.

## RN05 — Não alterar o schema legado

Configurar obrigatoriamente:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

Não adicionar Flyway ou Liquibase para alterar o banco legado nesta versão.

## RN06 — Atualização atômica

A atualização do preço deve ocorrer em transação.

Caso nenhuma linha seja atualizada, retornar produto não encontrado.

## RN07 — Normalização do código de barras

Antes da busca:

- remover espaços nas extremidades;
- rejeitar valor vazio;
- aceitar somente caracteres numéricos;
- validar tamanho compatível com os códigos existentes, preferencialmente até 13 caracteres conforme o legado.

Não converter o código de barras para número, pois zeros à esquerda devem ser preservados.

---

# 9. Modelagem do domínio

Criar `Produto` como classe Java pura.

Campos:

```text
id: Long
codigoBarras: String
descricao: String
precoVenda: BigDecimal
```

Responsabilidades:

- garantir campos obrigatórios;
- validar alteração de preço;
- impedir preço menor ou igual a zero.

Não adicionar anotações do Spring ou JPA nessa classe.

---

# 10. Portas de entrada

## 10.1 Buscar produto

```java
public interface BuscarProdutoPorCodigoUseCase {
    ProdutoResult executar(String codigoBarras);
}
```

## 10.2 Atualizar preço

```java
public interface AtualizarPrecoProdutoUseCase {
    void executar(Long produtoId, AtualizacaoPrecoCommand command);
}
```

---

# 11. Portas de saída

## 11.1 Busca de produto

```java
public interface BuscarProdutoPort {
    Optional<Produto> buscarPorCodigoPrincipal(String codigoBarras);
    Optional<Produto> buscarPorId(Long id);
}
```

## 11.2 Busca de código similar

```java
public interface BuscarProdutoSimilarPort {
    Optional<String> buscarCodigoPrincipal(String codigoSimilar);
}
```

## 11.3 Atualização do preço

```java
public interface AtualizarPrecoProdutoPort {
    void atualizarPreco(Long produtoId, BigDecimal novoPreco);
}
```

---

# 12. Casos de uso

## 12.1 `BuscarProdutoService`

Responsabilidades:

- normalizar o código;
- validar o código;
- buscar primeiro na tabela principal;
- buscar depois na tabela de similares;
- lançar `ProdutoNaoEncontradoException` quando necessário;
- retornar `ProdutoResult`.

Não acessar JPA diretamente.

Não retornar entidade JPA.

## 12.2 `AtualizarPrecoProdutoService`

Responsabilidades:

- validar o ID;
- buscar o produto por ID;
- validar o novo preço por meio do domínio;
- solicitar atualização pela porta de saída;
- garantir que a atualização ocorra pelo ID.

---

# 13. Registro dos casos de uso no Spring

Manter os serviços de aplicação como classes Java puras.

Não adicionar `@Service` nos casos de uso.

Registrá-los em uma configuração Spring por meio de `@Bean`.

O uso de `@Bean` deve permanecer na infraestrutura, mantendo a camada de aplicação sem dependência direta do Spring.

---

# 14. Mapeamento do banco legado

## 14.1 Tabela `produtos`

```text
ID                int, PK, auto increment
CODIGO_PRODUTO    varchar(13), unique ou index
DESCRICAO         varchar(60)
PRECO_UNITARIO    decimal(10,2)
PRECO_CUSTO       decimal(10,2)
```

Criar `ProdutoJpaEntity`.

Não usar `@GeneratedValue`, pois a API não cria produtos.

Mapear somente os campos necessários, salvo necessidade técnica adicional.

## 14.2 Tabela `produtos_similares`

```text
CODIGO_SIMILAR    varchar(13)
CODIGO_PRODUTO    varchar(13)
```

Criar `ProdutoSimilarJpaEntity`.

Usar `CODIGO_SIMILAR` como identificador JPA apenas se ele for realmente único no banco.

Caso não seja único, verificar o schema real e implementar chave composta com `@EmbeddedId`.

Não inventar restrição inexistente no banco.

## 14.3 Relacionamentos

Evitar `@ManyToOne` e relacionamentos JPA automáticos neste contexto.

Manter `CODIGO_PRODUTO` como `String`.

A resolução deve ser explícita nos repositories e no caso de uso.

---

# 15. Repositories JPA

## 15.1 Produto

Criar método:

```java
Optional<ProdutoJpaEntity> findByCodigoProduto(String codigoProduto);
```

Criar query explícita para atualização:

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("""
    update ProdutoJpaEntity p
       set p.precoUnitario = :novoPreco
     where p.id = :id
""")
int atualizarPrecoPorId(
        @Param("id") Long id,
        @Param("novoPreco") BigDecimal novoPreco
);
```

## 15.2 Produto similar

Criar query que retorne apenas o código principal:

```java
@Query("""
    select ps.codigoProduto
      from ProdutoSimilarJpaEntity ps
     where ps.codigoSimilar = :codigoSimilar
""")
Optional<String> buscarCodigoPrincipal(
        @Param("codigoSimilar") String codigoSimilar
);
```

---

# 16. Adapter de persistência

Criar `ProdutoPersistenceAdapter` implementando:

- `BuscarProdutoPort`;
- `BuscarProdutoSimilarPort`;
- `AtualizarPrecoProdutoPort`.

Responsabilidades:

- chamar repositories;
- mapear entidade JPA para domínio;
- executar atualização transacional;
- verificar quantidade de linhas alteradas;
- nunca expor entidade JPA para outras camadas.

Adicionar `@Component` no adapter.

Adicionar `@Transactional` somente onde necessário.

---

# 17. Contratos REST

Base path:

```text
/api/produtos
```

## 17.1 Buscar produto

```http
GET /api/produtos/{ean}
```

Resposta 200:

```json
{
  "id": 150,
  "codigoBarras": "7891010101010",
  "descricao": "COCA COLA 2L",
  "precoVenda": 9.50
}
```

Possíveis respostas:

- 200: produto encontrado;
- 400: código inválido;
- 404: produto não encontrado;
- 500: erro interno;
- 503: banco indisponível.

## 17.2 Atualizar preço

```http
PATCH /api/produtos/{id}
Content-Type: application/json
```

Payload:

```json
{
  "novoPreco": 10.50
}
```

Resposta de sucesso:

```http
204 No Content
```

Possíveis respostas:

- 204: preço atualizado;
- 400: ID ou preço inválido;
- 404: produto não encontrado;
- 500: erro interno;
- 503: banco indisponível.

---

# 18. Validação HTTP

Criar `AtualizarPrecoRequest` como `record`.

Validar:

- `novoPreco` obrigatório;
- valor mínimo de `0.01`;
- no máximo oito dígitos inteiros;
- no máximo duas casas decimais;
- ID positivo;
- EAN numérico e dentro do tamanho aceito.

Não confiar apenas na validação do frontend.

---

# 19. Tratamento global de erros

Criar `GlobalExceptionHandler` com `@RestControllerAdvice`.

Padronizar o erro:

```json
{
  "timestamp": "2026-07-29T11:00:00-03:00",
  "status": 404,
  "error": "Not Found",
  "code": "PRODUTO_NAO_ENCONTRADO",
  "message": "Produto não encontrado.",
  "path": "/api/produtos/7891010101010"
}
```

Tratar ao menos:

- `ProdutoNaoEncontradoException`;
- `PrecoInvalidoException`;
- `MethodArgumentNotValidException`;
- `ConstraintViolationException`;
- erros de banco;
- exceções inesperadas.

Não retornar stack trace ao cliente.

Não retornar credenciais, SQL ou detalhes internos.

---

# 20. Documentação OpenAPI

Documentar os endpoints com Springdoc OpenAPI.

Adicionar:

- título;
- descrição;
- versão;
- exemplos de request;
- exemplos de response;
- respostas 400, 404, 500 e 503;
- descrição dos campos.

---

# 21. Configuração do backend

Configurar:

```yaml
spring:
  application:
    name: app-precificacao-api
  jpa:
    hibernate:
      ddl-auto: none
    open-in-view: false

server:
  port: ${SERVER_PORT:8080}
  address: ${SERVER_ADDRESS:0.0.0.0}

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Variáveis esperadas:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SERVER_PORT
SERVER_ADDRESS
LOG_FILE
```

Não versionar credenciais.

---

# 22. Configuração de CORS

Durante desenvolvimento, liberar apenas as origens necessárias.

Em produção mobile nativo, revisar o comportamento do Capacitor e configurar origens de forma restrita.

Não usar `*` indiscriminadamente junto com credenciais.

---

# 23. Health check

Adicionar Spring Boot Actuator.

Endpoint esperado:

```http
GET /actuator/health
```

O aplicativo pode utilizar esse endpoint para verificar disponibilidade inicial.

---

# 24. Segurança no banco

Documentar a criação de um usuário MySQL exclusivo com permissões mínimas:

- `SELECT` em `produtos`;
- `SELECT` em `produtos_similares`;
- `UPDATE (PRECO_UNITARIO)` em `produtos`.

Não executar os comandos automaticamente.

---

# 25. Arquitetura do frontend

Utilizar arquitetura baseada em funcionalidades.

Estrutura conceitual:

```text
Page
  |
  v
Feature service / data access
  |
  v
HttpClient
  |
  v
Backend
```

A página não deve conhecer a URL completa da API.

O plugin do scanner não deve ser chamado diretamente pelos componentes de página.

---

# 26. Estrutura de pastas do frontend

```text
mobile/
├── angular.json
├── capacitor.config.ts
├── ionic.config.json
├── package.json
├── tsconfig.json
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.routes.ts
│   │   ├── app.config.ts
│   │   ├── core/
│   │   │   ├── config/
│   │   │   ├── interceptors/
│   │   │   ├── services/
│   │   │   └── models/
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   ├── pipes/
│   │   │   ├── directives/
│   │   │   └── validators/
│   │   ├── features/
│   │   │   ├── scanner/
│   │   │   │   ├── pages/
│   │   │   │   ├── services/
│   │   │   │   └── scanner.routes.ts
│   │   │   └── product-pricing/
│   │   │       ├── pages/
│   │   │       ├── components/
│   │   │       ├── data-access/
│   │   │       ├── models/
│   │   │       └── product-pricing.routes.ts
│   │   └── layout/
│   ├── environments/
│   ├── assets/
│   ├── theme/
│   ├── global.scss
│   └── main.ts
├── android/
└── tests/
```

---

# 27. Responsabilidades do frontend

## `core`

- configuração da API;
- interceptor de erros;
- conectividade;
- modelos globais.

## `shared`

- componentes reutilizáveis;
- pipes;
- directives;
- validators.

## `features/scanner`

- permissão de câmera;
- abertura do scanner;
- leitura do EAN;
- cancelamento;
- navegação.

## `features/product-pricing`

- consulta do produto;
- exibição dos dados;
- formulário;
- atualização;
- feedback;
- retorno ao scanner.

---

# 28. Fluxo de navegação

Rotas mínimas:

```text
/scanner
/produto/:codigo
```

Fluxo:

```text
ScannerPage
    |
    v
Captura EAN
    |
    v
/produto/:codigo
    |
    v
GET /api/produtos/{ean}
    |
    v
Exibe produto
    |
    v
PATCH /api/produtos/{id}
    |
    v
Toast de sucesso
    |
    v
/scanner
```

---

# 29. Serviço de API do produto

Criar `ProdutoApiService`.

Métodos:

```typescript
buscarPorCodigo(ean: string): Observable<ProdutoResponse>
```

```typescript
atualizarPreco(
  produtoId: number,
  novoPreco: number
): Observable<void>
```

A URL deve vir de `environment`.

Não espalhar IPs ou URLs pelo código.

---

# 30. Serviço do scanner

Criar `BarcodeScannerService`.

Responsabilidades:

- verificar permissão;
- solicitar permissão;
- iniciar leitura;
- interromper leitura;
- restaurar interface;
- retornar `string | null`;
- encapsular completamente o plugin.

Usar `try/finally`.

---

# 31. Formulário de preço

Utilizar Reactive Forms.

Validações:

- obrigatório;
- maior que zero;
- no máximo duas casas decimais;
- limite compatível com `decimal(10,2)`.

Desabilitar salvar quando:

- formulário inválido;
- requisição em andamento;
- produto não carregado.

Impedir múltiplos envios simultâneos.

---

# 32. Estado de tela

Usar Signals ou estado local simples.

Não adicionar NgRx nesta versão.

---

# 33. Tratamento de erros no frontend

Mapeamento mínimo:

- status 0: não foi possível conectar ao servidor;
- 400: dados inválidos;
- 404: produto não encontrado;
- 503: servidor ou banco indisponível;
- 500: erro inesperado.

Não mostrar detalhes técnicos ao atendente.

---

# 34. Experiência do usuário

## Tela inicial

- título;
- indicação de conexão;
- botão “Escanear produto”;
- orientação;
- loading;
- aviso de indisponibilidade.

## Tela de produto

- descrição;
- código;
- preço atual;
- novo preço;
- salvar;
- cancelar ou voltar;
- loading.

## Feedback

Toast de sucesso:

```text
Preço atualizado com sucesso.
```

A interface deve ser simples e possuir botões grandes.

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

# 36. Testes unitários obrigatórios — backend

Todos os testes devem ser executáveis sem acesso ao banco real.

Os casos de uso devem ser testados sem iniciar o contexto Spring.

Usar JUnit 5 e Mockito.

## 36.1 Testes de domínio

Criar `ProdutoTest`.

Cenários mínimos:

- criar produto válido;
- alterar preço válido;
- rejeitar preço nulo;
- rejeitar preço zero;
- rejeitar preço negativo;
- preservar demais dados ao alterar preço.

## 36.2 Testes de `BuscarProdutoService`

Cenários mínimos:

- retornar produto por código principal;
- não consultar similares quando principal existir;
- consultar similar quando principal não existir;
- retornar produto associado ao similar;
- lançar exceção quando código não existir;
- lançar exceção quando similar apontar para produto inexistente;
- remover espaços;
- rejeitar código vazio;
- rejeitar código não numérico;
- preservar zeros à esquerda.

## 36.3 Testes de `AtualizarPrecoProdutoService`

Cenários mínimos:

- buscar produto pelo ID;
- alterar preço válido;
- chamar atualização com ID e preço corretos;
- rejeitar preço nulo;
- rejeitar preço zero;
- rejeitar preço negativo;
- não atualizar produto inexistente;
- lançar exceção para ID inexistente.

## 36.4 Testes de mapper

Testar:

- entidade JPA para domínio;
- domínio para response;
- campos corretos;
- ausência de preço de custo na resposta.

## 36.5 Testes do controller

Usar `@WebMvcTest` ou MockMvc standalone.

### GET

- 200 com JSON correto;
- 400 para EAN inválido;
- 404 para produto inexistente.

### PATCH

- 204 para atualização válida;
- 400 para preço nulo;
- 400 para preço zero;
- 400 para preço negativo;
- 400 para mais de duas casas;
- 404 para ID inexistente.

## 36.6 Testes de exceções

Validar:

- status;
- código;
- mensagem;
- path;
- ausência de stack trace;
- ausência de SQL.

## 36.7 Testes do adapter

Com repositories mockados:

- busca principal encontrada;
- busca principal não encontrada;
- busca por ID;
- busca similar;
- atualização com uma linha;
- atualização com zero linhas.

## 36.8 Testes de repository

Criar integração apenas em banco isolado.

Não usar banco real.

Não usar H2 para mascarar incompatibilidades específicas do MySQL.

## 36.9 Cobertura

Configurar JaCoCo.

Metas:

- global: mínimo 80%;
- casos de uso: 90% ou mais;
- regras críticas do domínio: 100%.

---

# 37. Testes unitários obrigatórios — frontend

## 37.1 `ProdutoApiService`

Testar:

- URL correta no GET;
- EAN codificado;
- PATCH pelo ID;
- body correto;
- resposta sem conteúdo;
- propagação de erro.

## 37.2 `BarcodeScannerService`

Mockar o plugin.

Testar:

- permissão concedida;
- permissão negada;
- leitura com conteúdo;
- cancelamento;
- erro;
- finalização do scanner;
- restauração da interface.

## 37.3 `ScannerPageComponent`

Testar:

- botão de leitura;
- início do scanner;
- navegação com código;
- cancelamento;
- permissão negada;
- servidor indisponível.

## 37.4 `ProductPricePageComponent`

Testar:

- consulta usando rota;
- loading;
- preenchimento do formulário;
- validações;
- botão desabilitado;
- atualização com ID correto;
- Toast de sucesso;
- retorno ao scanner;
- falha sem navegação;
- bloqueio de duplo envio.

## 37.5 Interceptor

Testar status:

- 0;
- 400;
- 404;
- 500;
- 503.

## 37.6 Validator de preço

Testar:

- válido;
- zero;
- negativo;
- mais de duas casas;
- nulo;
- valor máximo.

## 37.7 Cobertura frontend

Metas:

- global: mínimo 80%;
- serviços e validações críticas: 90% ou mais.

---

# 38. Testes de integração

Backend:

- controller com casos de uso;
- adapter com banco isolado, quando viável.

Frontend:

- scanner mockado → consulta → atualização;
- produto não encontrado;
- servidor indisponível.

E2E não é obrigatório nesta primeira versão.

---

# 39. Padrões de qualidade

O Codex deve:

- usar nomes claros;
- evitar métodos longos;
- aplicar responsabilidade única;
- não criar abstrações sem uso;
- não expor entidades JPA;
- não usar `Optional.get()` sem verificação;
- não usar `BigDecimal.equals()` para comparação numérica;
- não usar `double` para preço;
- usar injeção por construtor;
- não usar field injection;
- não usar `@Data` em entidade;
- não registrar credenciais;
- não versionar secrets;
- manter métodos coesos.

---

# 40. Logs

Registrar:

- inicialização;
- indisponibilidade do banco;
- tentativas de atualização;
- atualização concluída;
- erros inesperados.

Não registrar:

- senha;
- credenciais;
- SQL sensível;
- tokens;
- stack trace em resposta HTTP.

---

# 41. Implantação local

Documentar em `docs/implantacao.md`.

Fluxo:

1. instalar Java 21;
2. gerar JAR;
3. copiar para o servidor;
4. configurar variáveis;
5. configurar IP fixo;
6. liberar porta 8080;
7. executar como serviço do Windows;
8. validar health check;
9. configurar URL mobile;
10. gerar APK assinado;
11. instalar APK;
12. conceder câmera;
13. conectar ao Wi-Fi;
14. testar ponta a ponta.

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

# 44. README principal

Incluir:

- visão geral;
- arquitetura;
- requisitos;
- tecnologias;
- estrutura;
- execução;
- variáveis;
- testes;
- build;
- limitações;
- cuidados com legado.

---

# 45. AGENTS.md

Incluir:

- ler esta especificação;
- não criar cadastro ou exclusão;
- não alterar schema;
- não adicionar migrations;
- não usar banco real em testes;
- manter arquitetura;
- criar testes para mudanças;
- executar testes;
- atualizar documentação;
- não adicionar dependências sem justificativa;
- não inserir credenciais.

---

# 46. Ordem de implementação

1. Fundação.
2. Domínio e aplicação.
3. Persistência.
4. API REST.
5. Frontend base.
6. Scanner.
7. Precificação.
8. Integração.
9. Implantação.

Executar em etapas pequenas e manter testes passando a cada etapa.

---

# 47. Critérios de aceite do backend

- inicia corretamente;
- conecta ao MySQL configurado;
- não altera schema;
- busca por código principal;
- busca por similar;
- retorna 404;
- valida preço;
- atualiza somente `PRECO_UNITARIO`;
- atualiza por ID;
- retorna 204;
- trata erros;
- possui Swagger;
- possui health check;
- não expõe credenciais;
- possui testes unitários;
- passa em `mvn verify`.

---

# 48. Critérios de aceite do aplicativo

- abre em Android;
- solicita câmera;
- lê código;
- consulta API;
- exibe produto;
- valida preço;
- atualiza pelo ID;
- mostra sucesso;
- retorna ao scanner;
- trata 404;
- trata indisponibilidade;
- impede duplo envio;
- usa environment;
- possui testes unitários;
- passa em lint, testes e build.

---

# 49. Definition of Done

Uma tarefa só é concluída quando:

- código implementado;
- arquitetura respeitada;
- testes criados ou atualizados;
- testes passando;
- lint passando;
- build passando;
- documentação atualizada;
- nenhuma credencial versionada;
- nenhuma regra do legado violada;
- comportamento manual validado quando envolver câmera.

---

# 50. Restrições finais

O Codex não deve:

- alterar tabelas;
- criar migrations;
- cadastrar produtos;
- excluir produtos;
- alterar preço de custo;
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

# 51. Resultado esperado

```text
app-precificacao/
├── backend/
│   ├── Spring Boot
│   ├── arquitetura hexagonal moderada
│   ├── integração MySQL
│   ├── Swagger
│   ├── health check
│   └── testes unitários
├── mobile/
│   ├── Ionic/Angular
│   ├── scanner
│   ├── consulta
│   ├── atualização
│   ├── erros
│   └── testes unitários
├── docs/
├── README.md
└── AGENTS.md
```

O resultado deve ser simples de manter, seguro para o banco legado, testável e adequado ao uso real dos atendentes na rede local do mercado.
