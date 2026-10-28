# 04 — Plano de implementação do backend (API)

> Pasta `melhorias/` · Documento 4 de 5. As regras de negócio estão na [especificação funcional](02-especificacao-funcional.md); o consumo pelo app, no [plano de frontend](03-plano-frontend.md); a coordenação e os agentes, no [plano mestre](00-master.md).
>
> **Agente responsável:** `springboot`. Alternativa: `agente-spring_boot-foursys`, informando logo no início "Skill: Hexagonal conforme `AGENTS.md` do projeto", porque esse agente pergunta a skill antes de agir. Os guardrails da seção 2 valem para ambos.

## 1. Objetivo e premissas

- Implementar na API os contratos e regras das Fases 0 a 3, preservando o legado: a única escrita continua sendo `produtos.PRECO_UNITARIO`, pelo `ID`.
- Base atual:
  - Spring Boot 3.5.4, alvo Java 17, Maven Wrapper;
  - JPA com `ddl-auto: none`, Connector/J 5.1.49 e `MySQLLegacyDialect`;
  - JUnit 5, Mockito, MockMvc (`standaloneSetup`) e JaCoCo com 100% de linhas nas classes medidas.
- Nenhum teste acessa banco real.
- Dados próprios (auditoria, lotes de etiquetas, relatórios) ficam em **arquivos no servidor**, fora do banco legado, conforme D-01. Não há migrations nem tabelas novas.

## 2. Guardrails para o agente

| # | Regra | Motivo |
|---|---|---|
| G-01 | **A estrutura de pacotes do `AGENTS.md` prevalece** sobre a instrução global de arquitetura hexagonal (`br.com.bradesco`, `core/domain/model`, `adapter/output/repository`). Pacote base: `br.com.mercado.precificacao`, com `adapter/{config,exception,input,output}`, `config`, `core/{exception,mapper,model,service,usecase}`, `port/{input,output}` e `util`. | Precedência das instruções do projeto |
| G-02 | Não usar `agente-validacao_dupe_hexagonal` nem `agente-organizacao-dupe-hexagonal` para reorganizar pacotes. | Eles moveriam o projeto para outro padrão |
| G-03 | `core` e `port` em Java puro: sem Spring, JPA, Jackson ou Servlet. `java.time.Clock` é permitido e deve ser injetado onde houver tempo. | `AGENTS.md` |
| G-04 | Regras de negócio **somente** em `*UseCase` e `*Service`. Controllers e adapters só traduzem. | `AGENTS.md` |
| G-05 | Um `UseCase` implementa **um** `InputPort` e não depende de outro `UseCase`. Toda montagem em `config/BeanConfiguration`. | Padrão existente |
| G-06 | Cada controller tem uma **interface Swagger própria** em `adapter/input/swagger`, onde também ficam as restrições de validação dos parâmetros (`@Valid`, `@Positive`…). Parâmetros de infraestrutura (contexto da requisição) usam `@Parameter(hidden = true)`. | `AGENTS.md` e padrão existente |
| G-07 | Escrita no legado **apenas** pelas queries JPQL dedicadas de `PRECO_UNITARIO`. Nenhum `save()`, `saveAll()` ou `delete*()` em repositórios do legado. Colunas lidas por JDBC nunca são escritas. | Restrição central |
| G-08 | Preço sempre em `BigDecimal` com escala 2 e `RoundingMode.HALF_UP`; comparações com `compareTo`. | Especificação, seção 50 |
| G-09 | Nunca registrar em log senha, senha do caixa, token, SQL ou stack trace em resposta HTTP. | Especificação, seção 40 |
| G-10 | Dependência nova somente com D-07 aprovada. A única candidata neste plano é `com.google.zxing:core` (F3-08). | `AGENTS.md` |
| G-11 | Testes AAA, Mockito e MockMvc `standaloneSetup` com o `GlobalExceptionHandler`. `@TempDir` para adapters de arquivo. `.\mvnw.cmd clean verify` verde, com a regra JaCoCo mantida. | `docs/testes.md` |
| G-12 | Mudou contrato ou operação, atualizar `docs/api.md`, `docs/arquitetura.md`, `docs/implantacao.md`, `docs/testes.md`, `README.md` e `env.example` no mesmo PR. | `AGENTS.md` |

## 3. Visão geral dos componentes

Legenda: `[novo]`, `[alterado]`, `[removido]`. Os testes acompanham cada classe.

```text
br/com/mercado/precificacao/
├── adapter/
│   ├── config/
│   │   ├── CorsConfiguration                      [alterado] Authorization, X-App-Version, X-Device-Id; expõe Retry-After
│   │   ├── OpenApiConfiguration                   [alterado] esquema Bearer
│   │   ├── WebMvcConfiguration                    [novo] interceptors, resolver de contexto, rotas das páginas estáticas
│   │   └── PaginasEstaticasSegurancaFilter        [novo] CSP para /etiquetas/** e /pareamento/**
│   ├── exception/GlobalExceptionHandler           [alterado] novos códigos, detalhes, Retry-After
│   ├── input/
│   │   ├── controller/
│   │   │   ├── AuthController                     [alterado] token, logout, sessão
│   │   │   ├── ConfiguracaoController             [alterado] contexto (tentativas, auditoria)
│   │   │   ├── ProdutoController                  [alterado] por ID, busca, simulação, histórico, PATCH estendido
│   │   │   ├── VersaoController                   [novo] F0-08
│   │   │   ├── ParametroController                [novo] /api/parametros
│   │   │   ├── EtiquetaController                 [novo] F2-01
│   │   │   ├── RelatorioController                [novo] F2-06
│   │   │   └── PareamentoController               [novo] F3-08
│   │   ├── security/
│   │   │   ├── SessaoInterceptor                  [novo] F0-01
│   │   │   ├── VersaoAppInterceptor               [novo] F0-08
│   │   │   ├── ContextoOperacaoArgumentResolver   [novo]
│   │   │   └── RotasPublicas                      [novo] lista única de rotas sem token
│   │   ├── request/                               [novos DTOs] AtualizarPrecoRequest (alterado), SimulacaoPrecoRequest,
│   │   │                                          CriarLoteEtiquetasRequest, EnviarRelatorioRequest
│   │   ├── response/                              [novos DTOs] LoginResponse (alterado), SessaoResponse, ProdutoResponse (alterado),
│   │   │                                          ProdutoResumoResponse, BuscaProdutosResponse, AvaliacaoPrecoResponse,
│   │   │                                          VersaoResponse, ParametrosResponse, LoteEtiquetasResponse,
│   │   │                                          LoteResumoResponse, HistoricoPrecoResponse, EnviarRelatorioResponse,
│   │   │                                          PareamentoResponse, ApiErrorResponse (alterado: detalhes)
│   │   ├── mapper/                                ProdutoInputMapper (alterado), EtiquetaInputMapper [novo]
│   │   └── swagger/                               uma interface por controller [novos e alterados]
│   └── output/
│       ├── persistence/
│       │   ├── ProdutoPersistenceAdapter          [alterado] listas, por IDs, busca, atualização condicional
│       │   ├── ConfiguracaoPersistenceAdapter     [alterado] verificação de leitura das tabelas
│       │   ├── UsuarioPersistenceAdapter          [alterado] credenciais por nome (D-09)
│       │   ├── CustoProdutoJdbcAdapter            [novo] leitura isolada de PRECO_CUSTO (D-03)
│       │   └── repository/                        ProdutoJpaRepository, ProdutoSimilarJpaRepository,
│       │                                          UsuarioJpaRepository, SenhaCaixaJpaRepository [alterados]
│       ├── sessao/SessaoMemoriaAdapter            [novo]
│       ├── sessao/TokenSeguroAdapter              [novo]
│       ├── seguranca/TentativasMemoriaAdapter     [novo]
│       ├── auditoria/AuditoriaArquivoAdapter      [novo]
│       ├── etiquetas/LoteEtiquetasMemoriaAdapter  [novo] (padrão)
│       ├── etiquetas/LoteEtiquetasArquivoAdapter  [novo] (se D-01 = servidor)
│       ├── etiquetas/CodigoLoteAleatorioAdapter   [novo]
│       ├── relatorios/RelatorioArquivoAdapter     [novo]
│       ├── versao/VersaoBuildAdapter              [novo]
│       ├── rede/InterfacesRedeAdapter             [novo] F3-08
│       └── qr/QrCodeZxingAdapter                  [novo] F3-08 (D-07)
├── config/
│   ├── AppProperties                              [novo] @ConfigurationProperties("app")
│   └── BeanConfiguration                          [alterado] novos use cases, services e parâmetros; Clock
├── core/
│   ├── exception/                                 SessaoAusente, SessaoExpirada, MuitasTentativas, PrecoAlterado,
│   │                                              ConfirmacaoNecessaria, CodigoDuplicado, PermissaoNegada,
│   │                                              VersaoAppIncompativel, LoteNaoEncontrado, RelatorioInvalido [novas]
│   ├── model/                                     Sessao, SessaoAutenticada, ContextoOperacao, Permissao, OrigemAlteracao,
│   │                                              AlertaPreco, AvaliacaoPreco, AlteracaoPrecoComando, ResultadoAlteracaoPreco,
│   │                                              ProdutoConsultado, FormaEncontrada, LeituraBalanca, ResultadoBusca,
│   │                                              EventoAuditoria, LoteEtiquetas, ItemEtiqueta, Relatorio, Parametros* [novos]
│   ├── service/                                   ProdutoService (alterado), ControleTentativasService, PoliticaPermissaoService,
│   │                                              CodigoBarrasService, VersaoService, CsvService, SenhaService [novos]
│   └── usecase/                                   ver cada item
└── port/
    ├── input/                                     um InputPort por UseCase
    └── output/                                    VerificarUsuarioReferenciaOutputPort [removido]; demais em cada item
```

## 4. Configuração e parâmetros

### 4.1 `AppProperties` (pacote `config`)

`@ConfigurationProperties(prefix = "app")`, com valores padrão nos placeholders. O `BeanConfiguration` converte as propriedades em **records puros do core** (`ParametrosSeguranca`, `ParametrosPreco`, `ParametrosBalanca`, `ParametrosOperacao`, `ParametrosEtiquetas`, `ParametrosVersao`). O `core` nunca lê `AppProperties`.

Trecho a acrescentar em `application.yml`:

```yaml
app:
  loja:
    nome: ${APP_LOJA_NOME:}
  versao:
    minima-app: ${APP_VERSAO_MINIMA_APP:1.0.0}
  seguranca:
    exigir-token: ${APP_SEGURANCA_EXIGIR_TOKEN:false}
    sessao-duracao-horas: ${APP_SESSAO_DURACAO_HORAS:10}
    senha-comparacao-exata: ${APP_SENHA_COMPARACAO_EXATA:false}
    tentativas:
      maximo: ${APP_TENTATIVAS_MAXIMAS:5}
      maximo-por-aparelho: ${APP_TENTATIVAS_MAXIMAS_APARELHO:20}
      janela-minutos: ${APP_TENTATIVAS_JANELA_MINUTOS:15}
      bloqueio-minutos: ${APP_TENTATIVAS_BLOQUEIO_MINUTOS:15}
  permissoes:
    perfil-1: ${APP_PERMISSOES_PERFIL_1:TODAS}
    perfil-2: ${APP_PERMISSOES_PERFIL_2:TODAS}
    perfil-3: ${APP_PERMISSOES_PERFIL_3:TODAS}
  preco:
    limite-variacao-percentual: ${APP_PRECO_LIMITE_VARIACAO_PERCENTUAL:30}
    custo-habilitado: ${APP_CUSTO_HABILITADO:false}
    bloquear-codigo-duplicado: ${APP_BLOQUEAR_ALTERACAO_CODIGO_DUPLICADO:true}
  codigos:
    alternativo-habilitado: ${APP_CODIGO_ALTERNATIVO_HABILITADO:false}
  balanca:
    habilitada: ${APP_BALANCA_HABILITADA:false}
    prefixos: ${APP_BALANCA_PREFIXOS:2}
    inicio-codigo: ${APP_BALANCA_INICIO_CODIGO:2}
    tamanho-codigo: ${APP_BALANCA_TAMANHO_CODIGO:5}
    inicio-valor: ${APP_BALANCA_INICIO_VALOR:7}
    tamanho-valor: ${APP_BALANCA_TAMANHO_VALOR:6}
    tipo-valor: ${APP_BALANCA_TIPO_VALOR:PRECO}
    casas-decimais: ${APP_BALANCA_CASAS_DECIMAIS:2}
    remover-zeros-codigo: ${APP_BALANCA_REMOVER_ZEROS_CODIGO:false}
  operacao:
    minutos-inatividade: ${APP_INATIVIDADE_MINUTOS:10}
    dias-alerta-validade: ${APP_VALIDADE_DIAS_ALERTA:7}
    sugestoes-rebaixa: "${APP_VALIDADE_SUGESTOES_REBAIXA:7:10,3:30,1:50}"
    contagem-cega: ${APP_INVENTARIO_CONTAGEM_CEGA:true}
  etiquetas:
    validade-horas: ${APP_ETIQUETAS_VALIDADE_HORAS:24}
    persistencia: ${APP_ETIQUETAS_PERSISTENCIA:memoria}
  auditoria:
    diretorio: ${APP_AUDITORIA_DIRETORIO:logs/auditoria}
    retencao-dias: ${APP_AUDITORIA_RETENCAO_DIAS:365}
  dados:
    diretorio: ${APP_DADOS_DIRETORIO:dados}
  relatorios:
    diretorio: ${APP_RELATORIOS_DIRETORIO:relatorios}
```

### 4.2 Variáveis de ambiente novas (`env.example`)

| Variável | Padrão | Item | Decisão |
|---|---|---|---|
| `APP_LOJA_NOME` | vazio | F0-08, F0-09 | — |
| `APP_VERSAO_MINIMA_APP` | `1.0.0` | F0-08 | Sequência de implantação |
| `APP_SEGURANCA_EXIGIR_TOKEN` | `false` | F0-01 | Sequência de implantação |
| `APP_SESSAO_DURACAO_HORAS` | `10` | F0-01 | D-08 |
| `APP_SENHA_COMPARACAO_EXATA` | `false` | F0-03 | D-09 |
| `APP_TENTATIVAS_*` | 5 / 20 / 15 / 15 | F0-03 | D-08 |
| `APP_PERMISSOES_PERFIL_1..3` | `TODAS` | F3-07 | D-03 |
| `APP_PRECO_LIMITE_VARIACAO_PERCENTUAL` | `30` | F1-01 | D-08 |
| `APP_CUSTO_HABILITADO` | `false` | F1-01 | D-03 (e validação da coluna) |
| `APP_BLOQUEAR_ALTERACAO_CODIGO_DUPLICADO` | `true` | F0-06 | D-06 |
| `APP_CODIGO_ALTERNATIVO_HABILITADO` | `false` | F3-02 | D-06 |
| `APP_BALANCA_*` | ver §4.1 | F3-02 | D-06 |
| `APP_INATIVIDADE_MINUTOS`, `APP_VALIDADE_DIAS_ALERTA`, `APP_VALIDADE_SUGESTOES_REBAIXA`, `APP_INVENTARIO_CONTAGEM_CEGA` | 10 / 7 / `7:10,3:30,1:50` / `true` | F1-07, F3-03, F2-04 | D-08 |
| `APP_ETIQUETAS_VALIDADE_HORAS`, `APP_ETIQUETAS_PERSISTENCIA` | `24` / `memoria` | F2-01 | D-01, D-08 |
| `APP_AUDITORIA_DIRETORIO`, `APP_AUDITORIA_RETENCAO_DIAS` | `logs/auditoria` / `365` | F0-02 | D-08 |
| `APP_DADOS_DIRETORIO` | `dados` | F2-01 (arquivo) | D-01 |
| `APP_RELATORIOS_DIRETORIO` | `relatorios` | F2-06 | D-01 |

**Perfil de produção:**

- `SPRING_PROFILES_ACTIVE=prod` deve ser **variável de ambiente do serviço do Windows** (ou `--spring.profiles.active=prod` na linha de comando), e **não** uma linha do `.env`.
- Motivo: o `.env` é importado como arquivo de propriedades, e nesse formato a chave `SPRING_PROFILES_ACTIVE` não é convertida para `spring.profiles.active` (a conversão de maiúsculas com sublinhado vale só para variáveis de ambiente).
- Documentar em `env.example` (como comentário) e em `docs/implantacao.md`.

### 4.3 Outras configurações

- `@EnableScheduling` em `config` (limpeza de sessões, de lotes e da auditoria).
- Bean `Clock` (`Clock.system(ZoneId.of("America/Sao_Paulo"))`), injetado nos use cases e services que usam tempo.
- `spring-boot-maven-plugin` com o goal `build-info`: gera `META-INF/build-info.properties` e o bean `BuildProperties`, fonte da `versaoApi`. Não é dependência nova.

## 5. Desenho por item

### 5.1 F0-01 — Sessão autenticada

**Core**

```java
public enum Permissao { PRODUTO_CONSULTAR, PRECO_ALTERAR, PRECO_ABAIXO_CUSTO, CUSTO_VISUALIZAR,
                        ETIQUETA_IMPRIMIR, HISTORICO_VISUALIZAR, RELATORIO_ENVIAR }

public record Sessao(Usuario usuario, Instant criadaEm, Instant expiraEm) {}
public record SessaoAutenticada(String token, Usuario usuario, Instant expiraEm, Set<Permissao> permissoes) {}

// usuario == null quando a requisição veio sem token na transição (operador não identificado)
public record ContextoOperacao(Usuario usuario, String ipOrigem, String aparelhoId, String versaoApp) {
    public Optional<Usuario> operador() { return Optional.ofNullable(usuario); }
}
```

| Tipo | Nome | Responsabilidade |
|---|---|---|
| InputPort/UseCase | `AutenticarUsuarioInputPort` / `AutenticarUsuarioUseCase` (alterado) | Verifica tentativas (F0-03), busca o usuário, gera token, grava a sessão com `expiraEm = agora + duração` e devolve `SessaoAutenticada` com as permissões (F3-07) |
| InputPort/UseCase | `ValidarSessaoInputPort` / `ValidarSessaoUseCase` | Token → `Sessao` válida; ausente no armazenamento ou expirada → `SessaoExpiradaException` |
| InputPort/UseCase | `EncerrarSessaoInputPort` / `EncerrarSessaoUseCase` | Remove a sessão (idempotente) |
| InputPort/UseCase | `ConsultarSessaoInputPort` / `ConsultarSessaoUseCase` | Devolve usuário, expiração e permissões da sessão atual |
| OutputPort | `SessaoOutputPort` | `salvar(token, sessao)`, `buscar(token)`, `remover(token)`, `removerExpiradas(Instant)` |
| OutputPort | `GeradorTokenOutputPort` | `gerar()` |

**Adapters**

- **`TokenSeguroAdapter`:** `SecureRandom` com 32 bytes → Base64 URL sem padding.
- **`SessaoMemoriaAdapter`:**
  - `ConcurrentHashMap<String, Sessao>` com chave **SHA-256 do token** (hex), para que o token original não fique no mapa;
  - `@Scheduled(fixedDelay = 15 min)` chama `removerExpiradas`.
- **`SessaoInterceptor`** (`HandlerInterceptor.preHandle`):
  1. Monta o contexto de origem: IP (`request.getRemoteAddr()`), `X-Device-Id` (até 64 caracteres) e `X-App-Version` (até 20).
  2. Rotas de `RotasPublicas` → segue sem exigir token.
  3. Com `Authorization: Bearer …`:
     - `ValidarSessaoUseCase` → guarda `ContextoOperacao` com o usuário no atributo da requisição;
     - token inválido ou expirado → `SessaoExpiradaException` (**mesmo com a exigência desligada**).
  4. Sem token:
     - `exigirToken = true` → `SessaoAusenteException`;
     - `exigirToken = false` → `ContextoOperacao` sem usuário.
  5. As exceções lançadas no `preHandle` são tratadas pelo `GlobalExceptionHandler`, no formato padrão.
- **`RotasPublicas`** (lista única, coberta por teste):
  - `POST /api/auth/login`;
  - `GET /api/configuracoes/conexao`;
  - `POST /api/configuracoes/validar-senha-caixa`;
  - `GET /api/versao`;
  - `GET /api/etiquetas/lotes`, `GET /api/etiquetas/lotes/{codigo}` e `POST /api/etiquetas/lotes/{codigo}/impressao`;
  - `GET /api/pareamento` (este restrito a loopback, §5.18);
  - Swagger e Actuator ficam fora de `/api/**` e não passam pelo interceptor.
- **`ContextoOperacaoArgumentResolver`:** injeta `ContextoOperacao` nos métodos de controller. Nas interfaces Swagger, o parâmetro leva `@Parameter(hidden = true)`.
- **`AuthController`:**
  - `POST /api/auth/login` → `LoginResponse(codigo, username, displayName, perfil, token, expiraEm, permissoes)`;
  - `POST /api/auth/logout` → 204 (lê o token do cabeçalho; sem token também 204);
  - `GET /api/auth/sessao` → `SessaoResponse`.
- **`CorsConfiguration`:**
  - cabeçalhos permitidos: `Content-Type`, `Accept`, `Authorization`, `X-App-Version` e `X-Device-Id`;
  - `exposedHeaders`: `Retry-After`.
- **`OpenApiConfiguration`:** esquema de segurança `bearerAuth`, aplicado às operações protegidas nas interfaces Swagger.

**Exceções e códigos:** `SessaoAusenteException` → 401 `SESSAO_AUSENTE`; `SessaoExpiradaException` → 401 `SESSAO_EXPIRADA`.

**Testes**

- use cases com `Clock` fixo (expiração exata, sessão removida);
- adapter (chave por hash, limpeza);
- interceptor (rotas públicas, exigência ligada e desligada, token inválido com exigência desligada, cabeçalhos longos truncados);
- controller (login com token e sem senha na resposta, logout idempotente, sessão);
- CORS (cabeçalhos).

**Documentação:** `docs/api.md` (cabeçalho `Authorization`, novos endpoints, transição); `docs/arquitetura.md` (interceptor e sessão em memória).

### 5.2 F0-02 — Auditoria

**Core**

```java
public enum TipoEventoAuditoria { PRECO_ALTERACAO, SENHA_CAIXA_VALIDACAO, LOGIN_BLOQUEADO }
public enum ResultadoAuditoria { SUCESSO, SEM_ALTERACAO, CONFLITO, CONFIRMACAO_PENDENTE, NAO_ENCONTRADO,
                                 CODIGO_DUPLICADO, PERMISSAO_NEGADA, FALHA_BANCO, NEGADO, BLOQUEADO }
public enum OrigemAlteracao { ALTERACAO, CONFERENCIA, PROMOCAO, PROMOCAO_FIM, VALIDADE, DESFAZER }

public record EventoAuditoria(Instant dataHora, TipoEventoAuditoria tipo, ResultadoAuditoria resultado,
                              Usuario operador, Long produtoId, String codigoProduto, String descricao,
                              BigDecimal precoAnterior, BigDecimal precoNovo, BigDecimal variacaoPercentual,
                              OrigemAlteracao origem, Set<AlertaPreco> alertasConfirmados,
                              String ipOrigem, String aparelhoId, String versaoApp, String detalhe) {}
```

| Tipo | Nome | Responsabilidade |
|---|---|---|
| OutputPort | `RegistrarAuditoriaOutputPort` | `registrar(EventoAuditoria)` |
| OutputPort | `ConsultarAuditoriaOutputPort` | `ultimasAlteracoesPreco(Long produtoId, int limite)` (F3-06) |

Os use cases de alteração de preço, validação da senha do caixa e autenticação (bloqueio) registram eventos em **todas** as saídas relevantes: sucesso e cada exceção de negócio.

**`AuditoriaArquivoAdapter`**

- Um arquivo por dia: `${APP_AUDITORIA_DIRETORIO}/auditoria-AAAA-MM-DD.jsonl`, em UTF-8, com uma linha JSON por evento (`ObjectMapper` do Spring, datas ISO-8601).
- Escrita `synchronized` com `Files.writeString(…, CREATE, APPEND)`.
- Diretório criado na inicialização.
- **Falha de escrita** não desfaz a alteração (não há transação distribuída com o arquivo): registra no log técnico, em ERROR, o evento completo com o prefixo `AUDITORIA_FALLBACK`, sem dados sensíveis, para não perder a informação.
- Retenção: `@Scheduled(cron = "0 30 3 * * *")` remove arquivos com mais de `retencao-dias`.
- Leitura (F3-06): percorre os arquivos do mais novo ao mais antigo, lê as linhas de trás para frente e filtra por `tipo = PRECO_ALTERACAO`, `resultado = SUCESSO` e `produtoId`, até o limite.

**Testes** (`@TempDir`): escrita e rotação por data (`Clock` fixo); conteúdo sem senha ou token; fallback em falha de escrita; retenção; leitura filtrada e limitada.

### 5.3 F0-03 — Tentativas, bloqueio e comparação de senha

**Core**

- `ControleTentativasService` (com `Clock`, `ParametrosTentativas` e `TentativasOutputPort`):
  - `verificar(chave)` → lança `MuitasTentativasException(segundosRestantes)` se a chave estiver bloqueada;
  - `registrarFalha(chave, limite)` → acumula na janela e bloqueia ao atingir o limite;
  - `registrarSucesso(chave)` → zera a chave.
- Chaves:
  - `login:{ip}:{nome em minúsculas, sem espaços nas pontas}` (limite `maximo`);
  - `login-aparelho:{ip}` (limite `maximo-por-aparelho`);
  - `caixa:{ip}` (limite `maximo`).
- `AutenticarUsuarioUseCase` e `ValidarSenhaCaixaUseCase` recebem `ContextoOperacao` e aplicam a política. O bloqueio gera o evento de auditoria `LOGIN_BLOQUEADO` ou `SENHA_CAIXA_VALIDACAO/BLOQUEADO`.
- **D-09** (com `senha-comparacao-exata = true`):
  - `BuscarUsuarioOutputPort.buscarCredenciaisPorNome(nome)` → `List<CredencialUsuario(usuario, senhaArmazenada)>`;
  - `SenhaService.confere(informada, armazenada)` compara em tempo constante (`MessageDigest.isEqual` sobre UTF-8);
  - mesma lógica para a senha do caixa (`ValidarSenhaCaixaOutputPort.listarSenhas()`);
  - com `false`, mantém a comparação atual no SQL.

**Adapter:** `TentativasMemoriaAdapter`, `ConcurrentHashMap` com limpeza periódica de chaves vencidas.

**HTTP:** `MuitasTentativasException` → 429 `MUITAS_TENTATIVAS`, cabeçalho `Retry-After: <segundos>` e `detalhes.segundosRestantes`.

**Testes:**

- janela deslizante e bloqueio exatamente no limite;
- desbloqueio após o tempo (`Clock` mutável de teste);
- sucesso zera a chave;
- limite por aparelho;
- `Retry-After`;
- comparação exata (maiúsculas e espaços) e em tempo constante (chamada ao método).

### 5.4 F0-04 — Teste de conexão sem usuário de referência

- **Remover** `VerificarUsuarioReferenciaOutputPort`, a constante `USUARIO_REFERENCIA` e `UsuarioJpaRepository.existsByNomeIgnoreCase`.
- **Novo** `VerificarAcessoLegadoOutputPort.tabelasLegiveis(): boolean`, implementado em `ConfiguracaoPersistenceAdapter` com `@Transactional(readOnly = true)`. Executa leituras com limite 1 nas tabelas, usando as entidades mapeadas, o que valida tabelas e colunas:
  - `produtoRepository.findAll(PageRequest.of(0, 1))`;
  - `similarRepository.findAll(PageRequest.of(0, 1))`;
  - `usuarioRepository.findAll(PageRequest.of(0, 1))`;
  - `senhaCaixaRepository.findFirstByOrderByIdAsc()` (novo método de leitura).
- `DataAccessException` → `false`, com log WARN apenas do nome da classe da exceção. O conteúdo lido é descartado e nunca registrado.
- `TestarConexaoServidorUseCase` usa a nova porta. O contrato HTTP (204/503) não muda.

**Testes:** adapter com repositórios dublados (sucesso, tabela vazia, exceção em cada tabela); use case. **Documentação:** `docs/api.md` (descrição do teste de conexão).

### 5.5 F0-05 — Alteração condicional e idempotente

**Contrato:**

```java
public record AlteracaoPrecoComando(Long produtoId, BigDecimal novoPreco, BigDecimal precoAnterior /* opcional */,
                                    OrigemAlteracao origem, Set<AlertaPreco> alertasConfirmados) {}
public record ResultadoAlteracaoPreco(Produto produto, BigDecimal precoAnterior, BigDecimal precoNovo, boolean semAlteracao) {}

public interface AtualizarPrecoProdutoInputPort {
    ResultadoAlteracaoPreco executar(AlteracaoPrecoComando comando, ContextoOperacao contexto);
}
public interface AtualizarPrecoProdutoOutputPort {
    boolean atualizarPreco(Long produtoId, BigDecimal novoPreco);                                   // transição
    boolean atualizarPrecoSeIgual(Long produtoId, BigDecimal novoPreco, BigDecimal precoEsperado);  // novo
}
```

**Repositório:**

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("""
        update ProdutoJpaEntity p
           set p.precoUnitario = :novoPreco
         where p.id = :id
           and p.precoUnitario = :precoEsperado
        """)
int atualizarPrecoSeIgual(@Param("id") Integer id, @Param("novoPreco") BigDecimal novoPreco,
                          @Param("precoEsperado") BigDecimal precoEsperado);
```

**Fluxo do `AtualizarPrecoProdutoUseCase`:**

1. Valida o ID (> 0) e o novo preço (`ProdutoService.validarNovoPreco`); valida o `precoAnterior` com a mesma regra, quando presente.
2. Exige a permissão `PRECO_ALTERAR` (F3-07).
3. Busca o produto por ID → `ProdutoNaoEncontradoException`.
4. Se `bloquear-codigo-duplicado` estiver ligado e o código do produto existir em mais de um produto → `CodigoDuplicadoException` (F0-06).
5. Avalia a alteração (F1-01). Alertas não confirmados → `ConfirmacaoNecessariaException(alertas)`. `ABAIXO_DO_CUSTO` confirmado sem a permissão `PRECO_ABAIXO_CUSTO` → `PermissaoNegadaException`.
6. Com `precoAnterior` informado:
   - preço atual = novo → `SEM_ALTERACAO` (idempotente, sem escrita);
   - preço atual ≠ anterior → `PrecoAlteradoException(precoAtual)`;
   - `atualizarPrecoSeIgual(...)` devolveu 0 linhas → relê: igual ao novo → `SEM_ALTERACAO`; senão → `PrecoAlteradoException(precoRelido)`.
7. Sem `precoAnterior` (APK antigo): comportamento atual (`atualizarPreco`; 0 linhas → `ProdutoNaoEncontradoException`).
8. Registra a auditoria e devolve o resultado. O controller responde 204 em todos os casos de sucesso.

**DTO:**

```java
public record AtualizarPrecoRequest(
        @NotNull @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2) BigDecimal novoPreco,
        @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2) BigDecimal precoAnterior,
        OrigemAlteracao origem,                       // ausente → ALTERACAO
        Set<AlertaPreco> alertasConfirmados) {}       // ausente → vazio
```

**HTTP:** `PrecoAlteradoException` → 409 `PRECO_ALTERADO` com `detalhes.precoAtual`. Um enum inválido no corpo cai em `HttpMessageNotReadableException` → 400 (já tratado).

**Testes:**

- matriz completa do fluxo (anterior igual, diferente, já igual ao novo, corrida com 0 linhas e releitura igual ou diferente, sem anterior);
- comparação com escalas diferentes (`9.5` × `9.50`);
- auditoria registrada em cada saída;
- controller (409 com `detalhes.precoAtual`, padrão `ALTERACAO`).

### 5.6 F0-06 e F0-10 — Dados inconsistentes e catálogo de erros

**Duplicidade:**

- `BuscarProdutoOutputPort`:
  - `buscarTodosPorCodigoPrincipal(String) : List<Produto>` (substitui o `Optional`);
  - `buscarPorId(Long) : Optional<Produto>`;
  - `buscarPorIds(Collection<Long>) : List<Produto>` (para lotes).
- `BuscarProdutoSimilarOutputPort`:
  - `buscarCodigosPrincipais(String codigoSimilar) : List<String>` (`select distinct`);
  - `listarSimilares(String codigoPrincipal) : List<String>` (F1-05).
- Repositório: `List<ProdutoJpaEntity> findAllByCodigoProduto(String)`; consultas JPQL com `distinct`.
- `BuscarProdutoPorCodigoUseCase` (ver também §5.13):
  - 1 resultado → encontrado;
  - mais de 1 → `CodigoDuplicadoException(candidatos)`;
  - similar com mais de 1 principal distinto → reúne os produtos de cada principal → `CodigoDuplicadoException`.
- Log WARN: `"Código com mais de um produto no cadastro legado: {} ({} produtos)"`.
- `ProdutoPersistenceAdapter.buscarPorId`: `Math.toIntExact` fora do intervalo → `Optional.empty()` (404), sem `ArithmeticException`.

**`ApiErrorResponse`** ganha `Map<String, Object> detalhes`, anotado com `@JsonInclude(JsonInclude.Include.NON_NULL)` para não mudar as respostas existentes.

**`GlobalExceptionHandler`:** um handler por exceção nova (catálogo no §7), cabeçalho `Retry-After` para 429 e `detalhes` quando houver. O handler de `DataAccessException` continua retornando 503, agora sem englobar a duplicidade.

**Testes:** use case com duplicidade no principal e no similar; adapter com ID fora do intervalo; handler (um teste por código, sem `stackTrace` nem SQL no corpo).

### 5.7 F0-07 — Exposição mínima em produção

`application-prod.yml`:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

- Documentar o perfil (§4.2) e verificar manualmente, iniciando com `--spring.profiles.active=prod`, que `/swagger-ui.html` e `/v3/api-docs` respondem 404.
- O Actuator segue com `health` e `info` sem detalhes.

### 5.8 F0-08 e F0-09 — Versão, compatibilidade e identificação da loja

| Tipo | Nome | Responsabilidade |
|---|---|---|
| InputPort/UseCase | `ConsultarVersaoInputPort` / `ConsultarVersaoUseCase` | Devolve `versaoApi`, `versaoMinimaApp` e `nomeLoja` |
| OutputPort | `VersaoApiOutputPort` | `versaoAtual()`. O `VersaoBuildAdapter` lê `BuildProperties` (fallback `"desconhecida"`) |
| Service | `VersaoService` | Comparação semver pura (`compativel(versaoApp, minima)`); versões malformadas são tratadas como desconhecidas (aceitas) |

- **`VersaoController`:** `GET /api/versao` (pública) → `VersaoResponse`.
- **`VersaoAppInterceptor`:** em `/api/**`, exceto `/api/versao`, se `X-App-Version` estiver presente e for menor que `versao.minima-app`, lança `VersaoAppIncompativelException` → 426 `VERSAO_APP_INCOMPATIVEL`. Sem o cabeçalho (APK antigo), segue; a migração é controlada pela exigência de token.
- **`ParametroController`:** `GET /api/parametros` (com sessão) → `ParametrosResponse`, a partir de `ParametrosOperacao`, `ParametrosPreco`, `ParametrosEtiquetas` e `ParametrosBalanca` (apenas `habilitada`).

**Testes:** semver (iguais, maior, menor, malformada); interceptor (com e sem cabeçalho, rota de versão liberada); controllers.

### 5.9 F1-01 — Avaliação, simulação e confirmação de preço

**Custo isolado (D-03):**

- `PRECO_CUSTO` **não** entra na entidade JPA: uma coluna inexistente no banco real quebraria todas as consultas de produto.
- `BuscarCustoProdutoOutputPort.buscarCusto(Long id): Optional<BigDecimal>`, implementado por `CustoProdutoJdbcAdapter` com `JdbcTemplate`: `SELECT PRECO_CUSTO FROM produtos WHERE ID = ?`, somente leitura.
- Só executa com `custo-habilitado = true`. Erro de SQL → `Optional.empty()` com log WARN (nome da exceção). Nunca há escrita dessa coluna.

**`ProdutoService`** (alterado):

```java
public AvaliacaoPreco avaliarAlteracao(BigDecimal precoAtual, BigDecimal novoPreco,
                                       BigDecimal custo /* pode ser null */, ParametrosPreco parametros)
```

| Cálculo | Regra |
|---|---|
| `variacaoValor` | `novo − atual` |
| `variacaoPercentual` | `(novo − atual) ÷ atual × 100`, escala 2, `HALF_UP`. Com `atual = 0`: `null` e sem alerta de variação |
| `margemAtual` / `margemNova` | `(preço − custo) ÷ preço × 100`, escala 2, `HALF_UP`. `null` sem custo |
| `ABAIXO_DO_CUSTO` | `custo != null && novo < custo` |
| `VARIACAO_ALTA` | `\|variacaoPercentual\| > limite` (estritamente maior) |

**Use cases:**

| InputPort/UseCase | Responsabilidade |
|---|---|
| `SimularAlteracaoPrecoInputPort` / `SimularAlteracaoPrecoUseCase` | Busca o produto por ID e o custo (se habilitado), avalia e devolve `AvaliacaoPreco`. O custo e as margens só voltam com `CUSTO_VISUALIZAR`; sem ela, `ABAIXO_DO_CUSTO` é devolvido sem o valor |
| `AtualizarPrecoProdutoUseCase` (§5.5) | Reavalia na gravação e exige que `alertasConfirmados` contenha todos os alertas → `ConfirmacaoNecessariaException(alertas)` |

**HTTP:**

- `POST /api/produtos/{id}/simulacao-preco` com `SimulacaoPrecoRequest(novoPreco)` → `AvaliacaoPrecoResponse`;
- `ConfirmacaoNecessariaException` → 422 `CONFIRMACAO_NECESSARIA` com `detalhes.alertas`.

**`GET /api/produtos/{ean}` e `/id/{id}`:** `precoCusto` e `margemPercentual` só com `custo-habilitado` **e** `CUSTO_VISUALIZAR`; caso contrário, os campos ficam ausentes (`NON_NULL`). O teste atual "não contém `precoCusto`" continua válido no padrão.

**Testes:**

- tabela de casos do serviço (limite exato de 30% sem alerta e 30,01% com alerta, custo igual ao novo sem alerta, atual zero, arredondamentos);
- adapter JDBC com `JdbcTemplate` dublado (valor, nulo, exceção);
- use cases (permissões de custo, confirmações completas e parciais);
- controller (422 com alertas).

### 5.10 F1-05 — Consulta enriquecida

```java
public enum FormaEncontrada { CODIGO_PRINCIPAL, CODIGO_SIMILAR, CODIGO_ALTERNATIVO, ETIQUETA_BALANCA }
public record ProdutoConsultado(Produto produto, List<String> codigosSimilares, FormaEncontrada encontradoPor,
                                String codigoLido, LeituraBalanca etiquetaBalanca, BigDecimal precoCusto,
                                BigDecimal margemPercentual) {}
```

- `BuscarProdutoPorCodigoUseCase` passa a devolver `ProdutoConsultado`, com os similares de `listarSimilares(codigoPrincipal)`.
- Novo `BuscarProdutoPorIdInputPort` / `BuscarProdutoPorIdUseCase` → `GET /api/produtos/id/{id}` (sem `encontradoPor`).
- `ProdutoInputMapper` monta `ProdutoResponse` com os campos opcionais.

### 5.11 F2-01 — Lotes de etiquetas

**Core**

```java
public enum SituacaoLote { NOVO, IMPRESSO }
public record ItemEtiqueta(Long produtoId, String codigoBarras, String descricao, String unidade,
                           BigDecimal preco, int quantidade, boolean precoDiferenteDaFila) {}
public record ItemIgnorado(Long produtoId, String motivo) {}
public record LoteEtiquetas(String codigo, Instant criadoEm, String aparelho, Usuario operador,
                            List<ItemEtiqueta> itens, List<ItemIgnorado> ignorados,
                            SituacaoLote situacao, Instant impressoEm) {}
public record SolicitacaoLote(String aparelho, List<ItemSolicitado> itens) {}
public record ItemSolicitado(Long produtoId, int quantidade, BigDecimal precoNaFila) {}
```

| InputPort/UseCase | Regras |
|---|---|
| `CriarLoteEtiquetasUseCase` | Permissão `ETIQUETA_IMPRIMIR`; de 1 a 300 itens; quantidade de 1 a 50; `produtoId` distintos; nome do aparelho de 1 a 40 caracteres. Busca os produtos atuais com `buscarPorIds` (uma consulta). Ausentes vão para `ignorados`. `precoDiferenteDaFila = precoNaFila.compareTo(preco) != 0`. Gera um código único (até 5 tentativas em caso de colisão) e salva |
| `ListarLotesEtiquetasUseCase` | Lotes com `criadoEm ≥ agora − validade-horas`, do mais recente ao mais antigo |
| `ConsultarLoteEtiquetasUseCase` | Por código (sem diferenciar maiúsculas) → `LoteNaoEncontradoException` (404 `LOTE_NAO_ENCONTRADO`) se ausente ou vencido |
| `MarcarLoteImpressoUseCase` | Situação `IMPRESSO`, `impressoEm = agora` (idempotente) |

| OutputPort | Implementações |
|---|---|
| `LoteEtiquetasOutputPort` (`salvar`, `buscar`, `listarDesde`, `atualizar`, `removerAnterioresA`) | `LoteEtiquetasMemoriaAdapter` (padrão) e `LoteEtiquetasArquivoAdapter` (`APP_ETIQUETAS_PERSISTENCIA=arquivo`, D-01): JSON em `${APP_DADOS_DIRETORIO}/etiquetas/lotes.json`, gravado de forma atômica (arquivo temporário + `Files.move(ATOMIC_MOVE)`) |
| `GeradorCodigoLoteOutputPort` | `CodigoLoteAleatorioAdapter`: 6 caracteres do alfabeto `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, com `SecureRandom` |

A limpeza agendada (a cada hora) remove os lotes vencidos.

**HTTP (`EtiquetaController` + `EtiquetaControllerSwagger`):**

| Método e rota | Sessão | Resposta |
|---|---|---|
| `POST /api/etiquetas/lotes` | Sim | 201 `LoteEtiquetasResponse` |
| `GET /api/etiquetas/lotes` | Não (página do PC) | 200 `LoteResumoResponse[]` |
| `GET /api/etiquetas/lotes/{codigo}` | Não | 200 `LoteEtiquetasResponse` |
| `POST /api/etiquetas/lotes/{codigo}/impressao` | Não | 204 |

**Página do PC** (`src/main/resources/static/etiquetas/`):

- Arquivos: `index.html`, `etiquetas.js` e `etiquetas.css`.
- `WebMvcConfiguration` encaminha `/etiquetas/` para `forward:/etiquetas/index.html`.
- `etiquetas.js`:
  - lista os lotes (`fetch`) e, ao escolher um, renderiza a grade;
  - **todo texto via `textContent`** (nunca `innerHTML`), para impedir XSS com descrições vindas do legado;
  - "Imprimir" chama `window.print()`; "Marcar como impresso" faz `POST …/impressao`;
  - aviso "Este produto também está no lote {código}" calculado no cliente a partir dos lotes `NOVO`.
- `etiquetas.css`:
  - `@media print` com `@page { size: A4; margin: 8mm; }`;
  - grade padrão 3 × 8 (70 × 37 mm — **confirmar o papel em D-05**);
  - preço com fonte grande, descrição em até 2 linhas com corte por reticências, unidade, código e data de emissão.
- `PaginasEstaticasSegurancaFilter` envia em `/etiquetas/**` e `/pareamento/**`:
  - `Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; frame-ancestors 'none'`;
  - `X-Content-Type-Options: nosniff`.

**Testes:**

- use case (preços atuais, ignorados, limites, permissão, colisão de código);
- gerador (alfabeto e tamanho);
- adapters (validade, gravação atômica com `@TempDir`);
- controller (rotas públicas sem token, criação com token, 404 de lote vencido);
- filtro (cabeçalhos).

**Teste manual:** impressão em Chrome e Edge.

### 5.12 F3-01 — Busca por descrição

| Tipo | Nome | Regras |
|---|---|---|
| InputPort/UseCase | `BuscarProdutosPorDescricaoInputPort` / `UseCase` | Normaliza (apara e reduz espaços); de 3 a 60 caracteres sem espaços; até 5 palavras; `limite` de 1 a 20 (padrão 20); consulta com `limite + 1` para calcular `truncado` |
| OutputPort | `BuscarProdutosPorDescricaoOutputPort.buscar(List<String> palavras, int limite)` | — |

**Adapter (JPA):**

- Para cada palavra, `lower(p.descricao) like :palavraN escape '\'`, com `%palavra%` e **escape de `%`, `_` e `\`** (impede curingas vindos do usuário).
- Ordenação: `case when lower(p.descricao) like :prefixo escape '\' then 0 else 1 end`, depois `p.descricao`.
- `Pageable` com o limite.

**HTTP:** `GET /api/produtos?descricao={termo}&limite={n}` → `BuscaProdutosResponse { itens, truncado }`. Termo inválido → 400 `DADOS_INVALIDOS` com mensagem para o usuário ("Digite pelo menos 3 letras.").

**Desempenho:** `LIKE '%…%'` não usa índice. Medir na homologação com o volume real (01, §12). Se ficar lento, oferecer modo "começa com" (`LIKE 'termo%'`), que pode usar índice em `DESCRICAO`.

**Testes:** normalização e limites; escape de curingas; truncado; ordenação (adapter com repositório dublado e teste da montagem da consulta).

### 5.13 F3-02 — Balança e códigos alternativos

**`CodigoBarrasService`** (core, puro):

- `alternativas(codigo)`:
  - 12 dígitos → `["0" + codigo]`;
  - 13 dígitos iniciados por `0` → `[codigo.substring(1)]`;
  - demais → vazio.
- `interpretarBalanca(codigo, ParametrosBalanca) → Optional<LeituraBalanca(codigoInterno, valor, tipo)>`:
  - exige 13 dígitos, prefixo configurado e **dígito verificador EAN-13 válido**;
  - extrai código e valor pelas posições (base 1);
  - valor com `casas-decimais`;
  - remove zeros do código interno se `remover-zeros-codigo`.

**Ordem de resolução em `BuscarProdutoPorCodigoUseCase`:**

1. Código principal.
2. Similar.
3. Alternativo (se `codigos.alternativo-habilitado`).
4. Balança (se `balanca.habilitada`).
5. `ProdutoNaoEncontradoException`, com `detalhes.pareceEtiquetaBalanca = true` quando o código tiver formato de balança.

Cada etapa trata a duplicidade (§5.6).

**Testes:** tabelas de códigos (UPC-A, EAN-13 com zero, balança com preço e com peso, dígito inválido, prefixo diferente); ordem de resolução; detalhe `pareceEtiquetaBalanca`.

### 5.14 F2-06 (e F2-04, F2-02, F3-04, F3-05) — Relatórios no computador da loja

| Tipo | Nome | Regras |
|---|---|---|
| Model | `Relatorio(tipo, titulo, aparelho, colunas, linhas, operador, criadoEm)` | `tipo ∈ {INVENTARIO, CONFERENCIA, REPOSICAO, PENDENCIAS_CADASTRO}` |
| Service | `CsvService` | Separador `;`; aspas quando houver `;`, aspas ou quebra de linha; aspas duplicadas. **Neutralização de fórmulas:** células iniciadas por `=`, `+`, `-`, `@`, TAB ou CR recebem o prefixo `'`. Linha de cabeçalho com as colunas |
| InputPort/UseCase | `EnviarRelatorioInputPort` / `UseCase` | Permissão `RELATORIO_ENVIAR`; de 1 a 20 colunas; até 5.000 linhas; células de até 200 caracteres; título de até 80; todas as linhas com o número de colunas. Nome do arquivo `{tipo}_{AAAA-MM-DD_HHmm}_{operador}.csv`, com apenas `[a-z0-9_-]` |
| OutputPort | `RelatorioOutputPort.salvar(nomeSugerido, conteudo) → nomeFinal` | `RelatorioArquivoAdapter`: grava em `APP_RELATORIOS_DIRETORIO` com BOM UTF-8; nunca sobrescreve (acrescenta `-2`, `-3`…); o diretório nunca vem do cliente (sem path traversal) |

**HTTP:** `POST /api/relatorios` → 201 `EnviarRelatorioResponse { nomeArquivo, pasta }`. A `pasta` é o nome lógico "Relatórios", e não o caminho absoluto do servidor.

**Testes:** CSV (escapes e neutralização); validações; nome sanitizado; não sobrescrever (`@TempDir`).

### 5.15 F2-05 — Promoções com vigência (condicional a D-04)

**Não implementar sem D-04 aprovada.** Esboço para a estimativa de impacto:

- Model `Promocao(id, produtoId, precoBase, precoPromocional, inicio, fim, situacao, responsavel)`, persistida conforme D-01 (arquivo JSON, como os lotes).
- `AplicarPromocoesPendentesUseCase` (agendado a cada minuto e na inicialização, para compensar o servidor desligado):
  - **início:** aplica via `atualizarPrecoSeIgual(id, promo, base)`; sem linhas → situação `CONFLITO`;
  - **fim:** `atualizarPrecoSeIgual(id, base, promo)`; sem linhas → `CONFLITO`, sem sobrescrever.
- Auditoria com origens `PROMOCAO` e `PROMOCAO_FIM`. Itens com conflito aparecem para o app em `GET /api/promocoes?situacao=CONFLITO`.
- Endpoints mínimos: `POST /api/promocoes`, `GET /api/promocoes`, `POST /api/promocoes/{id}/cancelamento`.

### 5.16 F3-06 — Histórico de preços

- `ConsultarHistoricoPrecoInputPort` / `UseCase`: permissão `HISTORICO_VISUALIZAR`; `limite` de 1 a 50 (padrão 20); usa `ConsultarAuditoriaOutputPort` (§5.2).
- `GET /api/produtos/{id}/historico-precos?limite=20` → `HistoricoPrecoResponse[]`. O `operador` é o nome, ou `null` quando "não identificado".

### 5.17 F3-07 — Permissões por perfil

- `PoliticaPermissaoService(Map<Integer, Set<Permissao>> matriz)`, montado a partir de `app.permissoes.perfil-N` (lista separada por vírgulas ou `TODAS`):
  - `permissoesDe(perfil)`;
  - `exigir(contexto, permissao)` → `PermissaoNegadaException` (403 `PERMISSAO_NEGADA`).
- **Contexto sem usuário** (transição) recebe `TODAS`, preservando o comportamento atual. **Restringir a matriz só depois de `APP_SEGURANCA_EXIGIR_TOKEN=true`**, senão o acesso anônimo contorna a restrição. A aplicação registra WARN na inicialização se a matriz estiver restrita com a exigência desligada.
- Os use cases chamam `exigir(...)` nas ações protegidas (tabela da especificação, F3-07). O login e a sessão devolvem `permissoes`.

**Testes:** parsing da matriz (`TODAS`, listas, valor inválido → falha na inicialização com mensagem clara); exigência por use case; WARN de configuração inconsistente.

### 5.18 F3-08 — Pareamento por QR code

| Tipo | Nome | Responsabilidade |
|---|---|---|
| InputPort/UseCase | `ConsultarPareamentoInputPort` / `UseCase` | Monta `{ nomeLoja, versaoApi, porta, enderecos[] }` com conteúdo `precificacao://servidor?ip={ip}&porta={porta}&loja={nome}` (URL-encoded) e a imagem do QR |
| OutputPort | `InterfacesRedeOutputPort` | `InterfacesRedeAdapter`: IPv4 privados (`isSiteLocalAddress`) de interfaces ativas, não loopback e não virtuais |
| OutputPort | `GeradorQrOutputPort` | `QrCodeZxingAdapter` (**D-07**: `com.google.zxing:core`, Java puro, licença Apache 2.0, sem dependências transitivas): PNG em Base64 de 280 px |

- `PareamentoController`: `GET /api/pareamento` aceito **somente de loopback** (`127.0.0.1`/`::1`); outros IPs recebem 403 `PERMISSAO_NEGADA`.
- Página `static/pareamento/` com `textContent` e `<img src="data:image/png;base64,…">`. Sem a aprovação de D-07, a página mostra apenas os IPs em texto grande.

**Testes:** use case (montagem e codificação do conteúdo); adapter de rede com interfaces dubladas; restrição a loopback no controller.

## 6. Contratos de API consolidados

Legenda: **Sessão** — "Sim" exige token quando `APP_SEGURANCA_EXIGIR_TOKEN=true` e valida o token sempre que presente.

| Método | Rota | Sessão | Permissão | Corpo | Sucesso | Erros |
|---|---|---|---|---|---|---|
| POST | `/api/auth/login` | Não | — | `LoginRequest` | 200 `LoginResponse` | 400, 401 `CREDENCIAIS_INVALIDAS`, 429, 503 |
| POST | `/api/auth/logout` | Opcional | — | — | 204 | — |
| GET | `/api/auth/sessao` | Sim | — | — | 200 `SessaoResponse` | 401 |
| GET | `/api/versao` | Não | — | — | 200 `VersaoResponse` | — |
| GET | `/api/parametros` | Sim | — | — | 200 `ParametrosResponse` | 401 |
| GET | `/api/configuracoes/conexao` | Não | — | — | 204 | 503 `CONEXAO_NAO_VALIDADA` |
| POST | `/api/configuracoes/validar-senha-caixa` | Não | — | `SenhaCaixaRequest` | 204 | 400, 401 `SENHA_CAIXA_INVALIDA`, 429 |
| GET | `/api/produtos/{ean}` | Sim | `PRODUTO_CONSULTAR` | — | 200 `ProdutoResponse` | 400, 401, 404, 409 `CODIGO_DUPLICADO`, 503 |
| GET | `/api/produtos/id/{id}` | Sim | `PRODUTO_CONSULTAR` | — | 200 `ProdutoResponse` | 400, 401, 404, 503 |
| GET | `/api/produtos?descricao=&limite=` | Sim | `PRODUTO_CONSULTAR` | — | 200 `BuscaProdutosResponse` | 400, 401, 503 |
| POST | `/api/produtos/{id}/simulacao-preco` | Sim | `PRECO_ALTERAR` | `SimulacaoPrecoRequest` | 200 `AvaliacaoPrecoResponse` | 400, 401, 403, 404, 503 |
| PATCH | `/api/produtos/{id}` | Sim | `PRECO_ALTERAR` | `AtualizarPrecoRequest` | 204 | 400, 401, 403, 404, 409 `PRECO_ALTERADO`/`CODIGO_DUPLICADO`, 422, 503 |
| GET | `/api/produtos/{id}/historico-precos?limite=` | Sim | `HISTORICO_VISUALIZAR` | — | 200 `HistoricoPrecoResponse[]` | 401, 403, 404 |
| POST | `/api/etiquetas/lotes` | Sim | `ETIQUETA_IMPRIMIR` | `CriarLoteEtiquetasRequest` | 201 `LoteEtiquetasResponse` | 400, 401, 403, 503 |
| GET | `/api/etiquetas/lotes` | Não | — | — | 200 `LoteResumoResponse[]` | — |
| GET | `/api/etiquetas/lotes/{codigo}` | Não | — | — | 200 `LoteEtiquetasResponse` | 404 `LOTE_NAO_ENCONTRADO` |
| POST | `/api/etiquetas/lotes/{codigo}/impressao` | Não | — | — | 204 | 404 |
| POST | `/api/relatorios` | Sim | `RELATORIO_ENVIAR` | `EnviarRelatorioRequest` | 201 `EnviarRelatorioResponse` | 400, 401, 403 |
| GET | `/api/pareamento` | Não (só loopback) | — | — | 200 `PareamentoResponse` | 403 |
| GET | `/etiquetas/`, `/pareamento/` | Não | — | — | Páginas HTML estáticas | — |

Todas as respostas de `/api/**` podem retornar 426 `VERSAO_APP_INCOMPATIVEL` quando o app informar uma versão abaixo da mínima (exceto `/api/versao`).

**Exemplos:**

```http
POST /api/auth/login
{ "username": "Carlos", "password": "••••" }

200
{ "codigo": 7, "username": "Carlos", "displayName": "Carlos", "perfil": 2,
  "token": "q4V0…", "expiraEm": "2026-10-28T22:15:00-03:00",
  "permissoes": ["PRODUTO_CONSULTAR","PRECO_ALTERAR","PRECO_ABAIXO_CUSTO","CUSTO_VISUALIZAR",
                 "ETIQUETA_IMPRIMIR","HISTORICO_VISUALIZAR","RELATORIO_ENVIAR"] }
```

```http
PATCH /api/produtos/15
Authorization: Bearer q4V0…
X-App-Version: 1.2.0
{ "novoPreco": 95.00, "precoAnterior": 9.50, "origem": "ALTERACAO", "alertasConfirmados": [] }

422
{ "timestamp": "2026-10-28T12:01:10-03:00", "status": 422, "error": "Unprocessable Entity",
  "code": "CONFIRMACAO_NECESSARIA", "message": "Confirme os alertas antes de alterar o preço.",
  "path": "/api/produtos/15", "detalhes": { "alertas": ["VARIACAO_ALTA"] } }
```

```http
PATCH /api/produtos/15  (preço mudou no PDV)
409
{ …, "code": "PRECO_ALTERADO", "message": "O preço deste produto mudou enquanto você editava.",
  "detalhes": { "precoAtual": 9.90 } }
```

```http
GET /api/produtos/7891000100103  (código duplicado)
409
{ …, "code": "CODIGO_DUPLICADO",
  "message": "Este código está cadastrado em mais de um produto.",
  "detalhes": { "produtos": [ { "id": 15, "codigoBarras": "7891000100103", "descricao": "ARROZ T1 5KG", "unidade": "UN", "precoVenda": 24.90 },
                              { "id": 98, "codigoBarras": "7891000100103", "descricao": "ARROZ T1 5KG PROMO", "unidade": "UN", "precoVenda": 22.90 } ] } }
```

```http
POST /api/etiquetas/lotes
{ "aparelho": "Aparelho 3 — Corredores",
  "itens": [ { "produtoId": 15, "quantidade": 2, "precoNaFila": 10.50 } ] }

201
{ "codigo": "K7P2QX", "criadoEm": "2026-10-28T15:32:00-03:00", "aparelho": "Aparelho 3 — Corredores",
  "operador": "Carlos", "situacao": "NOVO", "impressoEm": null,
  "itens": [ { "produtoId": 15, "codigoBarras": "7891000100103", "descricao": "ARROZ T1 5KG",
               "unidade": "UN", "preco": 10.90, "quantidade": 2, "precoDiferenteDaFila": true } ],
  "ignorados": [] }
```

## 7. Catálogo de erros

| Código | HTTP | Exceção | Mensagem (`message`) | `detalhes` |
|---|---|---|---|---|
| `DADOS_INVALIDOS` | 400 | Validação, `PrecoInvalido`, `CodigoBarrasInvalido`, `RelatorioInvalido`, corpo ilegível | Mensagem específica para o usuário | — |
| `CREDENCIAIS_INVALIDAS` | 401 | `CredenciaisInvalidasException` | "Usuário ou senha inválidos." | — |
| `SENHA_CAIXA_INVALIDA` | 401 | `SenhaCaixaInvalidaException` | (atual) | — |
| `SESSAO_AUSENTE` | 401 | `SessaoAusenteException` | "Faça login para continuar." | — |
| `SESSAO_EXPIRADA` | 401 | `SessaoExpiradaException` | "Sua sessão terminou." | — |
| `PERMISSAO_NEGADA` | 403 | `PermissaoNegadaException` | "Seu perfil não permite esta ação." | `permissao` |
| `PRODUTO_NAO_ENCONTRADO` | 404 | `ProdutoNaoEncontradoException` | "Produto não encontrado." | `pareceEtiquetaBalanca` (opcional) |
| `LOTE_NAO_ENCONTRADO` | 404 | `LoteNaoEncontradoException` | "Lote de etiquetas não encontrado ou vencido." | — |
| `PRECO_ALTERADO` | 409 | `PrecoAlteradoException` | "O preço deste produto mudou enquanto você editava." | `precoAtual` |
| `CODIGO_DUPLICADO` | 409 | `CodigoDuplicadoException` | "Este código está cadastrado em mais de um produto." | `produtos[]` |
| `CONFIRMACAO_NECESSARIA` | 422 | `ConfirmacaoNecessariaException` | "Confirme os alertas antes de alterar o preço." | `alertas[]` |
| `VERSAO_APP_INCOMPATIVEL` | 426 | `VersaoAppIncompativelException` | "Atualize o aplicativo." | `versaoMinimaApp` |
| `MUITAS_TENTATIVAS` | 429 | `MuitasTentativasException` | "Muitas tentativas seguidas." | `segundosRestantes` + cabeçalho `Retry-After` |
| `ERRO_INTERNO` | 500 | `Exception` | (atual) | — |
| `BANCO_INDISPONIVEL` | 503 | `DataAccessException` | (atual) | — |
| `CONEXAO_NAO_VALIDADA` | 503 | `ServidorNaoValidadoException` | (atual) | — |

## 8. Testes

| Camada | Abordagem | Observações |
|---|---|---|
| Services (core) | Tabelas de casos, sem mocks | `ProdutoService`, `ControleTentativasService`, `CodigoBarrasService`, `VersaoService`, `CsvService`, `SenhaService`, `PoliticaPermissaoService` |
| Use cases (core) | Mockito nas portas de saída, `Clock` fixo, AAA | Cobrir **todas** as saídas, inclusive o registro de auditoria em cada uma |
| Controllers | MockMvc `standaloneSetup` com `GlobalExceptionHandler` e o resolver de contexto | Validar corpo, códigos, `detalhes`, cabeçalhos e ausência de `stackTrace` e SQL |
| Interceptors e filtros | `MockHttpServletRequest`/`Response` | Rotas públicas, exigência ligada e desligada, versão, CSP |
| Adapters JPA | Repositórios dublados | Como hoje, sem banco real |
| Adapters de arquivo | `@TempDir` | Auditoria, lotes (arquivo) e relatórios |
| Adapter JDBC (custo) | `JdbcTemplate` dublado | Valor, nulo e exceção |

**JaCoCo:** a regra de 100% de linhas continua.

- Novas exclusões apenas para classes **puramente estruturais**: `config/AppProperties`, DTOs em `adapter/input/request` e `adapter/input/response` (já excluídos) e interfaces Swagger (já excluídas).
- Interceptors, adapters, services e use cases **entram** na medição.

**Teste manual de homologação** (com o `quality-assurance`, em ambiente de homologação com cópia do banco, nunca produção):

1. Transição do token (desligado → ligado) com APK antigo e novo.
2. Conflito com alteração feita no PDV.
3. Lote de etiquetas impresso no PC.
4. Relatórios abertos no Excel (acentos, separador e fórmulas neutralizadas).
5. Swagger desativado no perfil `prod`.

## 9. Engenharia, operação e documentação (F0-13)

1. **Launchers** ([SetupLauncher.cs](../setup/src/SetupLauncher.cs#L209-L213)): trocar `mvn.cmd` por `mvnw.cmd`, executado em `backend`, mantendo a opção de repositório local. Recompilar os executáveis conforme [setup/README.md](../setup/README.md) e atualizar a descrição no README.
2. **`pom.xml`:** execução `build-info` no `spring-boot-maven-plugin`; `com.google.zxing:core` somente com D-07.
3. **`env.example`:** todas as variáveis do §4.2, com comentários e a observação sobre `SPRING_PROFILES_ACTIVE`.
4. **Documentação a atualizar:**

   | Documento | Conteúdo |
   |---|---|
   | `docs/api.md` | Todos os endpoints, cabeçalhos e o catálogo de erros |
   | `docs/arquitetura.md` | Interceptors, sessão em memória, arquivos próprios (auditoria, lotes, relatórios) e páginas estáticas |
   | `docs/implantacao.md` | Perfil `prod`, IP fixo, serviço do Windows, pastas `logs/auditoria`, `dados` e `relatorios` (permissões e backup), sequência da exigência de token, Wi-Fi operacional separado |
   | `docs/testes.md` | Novas camadas de teste e checklist manual |
   | `README.md` | Swagger só em desenvolvimento; Java 17 mínimo e 21 recomendado |

5. **Backup:** incluir `logs/auditoria`, `dados` e `relatorios` na rotina de backup do servidor da loja.

## 10. Ordem de implementação no backend

Cada passo termina com `.\mvnw.cmd clean verify` verde (conferir `BUILD SUCCESS`; no PowerShell, avisos no stderr podem gerar exit code 1). A complexidade relativa está entre parênteses.

| Onda | Passo | Itens |
|---|---|---|
| 1 | `AppProperties`, `Clock`, `@EnableScheduling`, `build-info`, `ApiErrorResponse.detalhes` (baixa) | Base |
| 1 | Versão, parâmetros e interceptor de versão (baixa) | F0-08 |
| 1 | Teste de conexão sem usuário fixo (baixa) | F0-04 |
| 1 | Duplicidade, ID fora do intervalo e catálogo de erros (média) | F0-06, F0-10 |
| 1 | Sessão (token, interceptor, logout, sessão atual) com exigência **desligada** (alta) | F0-01 |
| 1 | Tentativas e comparação exata opcional (média) | F0-03 |
| 1 | Auditoria em arquivo (média) | F0-02 |
| 1 | Alteração condicional e idempotente (média) | F0-05 |
| 1 | Perfil `prod` sem Swagger (baixa) | F0-07 |
| 1 | Launchers e documentação (baixa) | F0-13 |
| 2 | Custo isolado, simulação e confirmações (média) | F1-01 |
| 2 | Similares, consulta por ID e `ProdutoConsultado` (baixa) | F1-05 |
| 3 | Lotes de etiquetas, página do PC e filtro de segurança (alta) | F2-01 |
| 3 | Relatórios CSV (média) | F2-06 |
| 4 | Busca por descrição (média) | F3-01 |
| 4 | Balança e alternativos (média) | F3-02 |
| 4 | Histórico de preços (baixa) | F3-06 |
| 4 | Permissões efetivas (média) | F3-07 |
| 4 | Pareamento por QR (baixa) | F3-08 |
| — | Promoções com vigência, somente com D-04 (alta) | F2-05 |

## 11. Definição de pronto (backend)

- [ ] Regras do item conferidas com a [especificação](02-especificacao-funcional.md) e contrato idêntico ao do §6 (o frontend depende dele).
- [ ] `core` e `port` sem dependências de framework; um `InputPort` por `UseCase`; beans em `BeanConfiguration`.
- [ ] Interface Swagger para cada controller novo ou alterado, com as validações nela declaradas.
- [ ] Nenhuma escrita no legado além de `PRECO_UNITARIO` pelo `ID` (revisar `@Modifying` e ausência de `save`/`delete`).
- [ ] Nenhum dado sensível em log ou em resposta; auditoria sem senha e sem token.
- [ ] Testes AAA de todas as saídas; `.\mvnw.cmd clean verify` com `BUILD SUCCESS` e JaCoCo 100% nas classes medidas.
- [ ] `docs/*`, `README.md` e `env.example` atualizados quando houver mudança de contrato ou operação.
- [ ] Revisão com a skill `springboot-review` (ou `code-review`) sem pendências bloqueantes.
