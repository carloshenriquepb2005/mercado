# 03 — Plano de implementação do frontend (mobile)

> Pasta `melhorias/` · Documento 3 de 5. As regras de negócio estão na [especificação funcional](02-especificacao-funcional.md); os contratos de API, no [plano de backend](04-plano-backend.md#6-contratos-de-api-consolidados); a coordenação e os agentes, no [plano mestre](00-master.md).
>
> **Agente responsável:** `agente-angular-foursys`, com os guardrails da seção 2. O agente `agente-android-foursys` apoia os itens nativos (seção 4.3 e seção 9). Os testes em aparelho ficam com o `quality-assurance`.

## 1. Objetivo e premissas

- Implementar no app Ionic/Angular todas as telas e comportamentos das Fases 0 a 3 da especificação.
- Base atual:
  - Angular 21.2 (standalone, signals, `OnPush`, Reactive Forms);
  - Ionic 8.8 (modo `md`) e Capacitor 8.4 (Android);
  - TypeScript 5.9, ESLint, Karma/Jasmine.
- A estrutura descrita em [docs/arquitetura.md](../docs/arquitetura.md) continua valendo: `core/` (infraestrutura global), `features/` (fluxos verticais com `pages`, `components`, `data-access`, `models` e `state`) e `shared/` (UI, pipes, utils e validators).
- Os contratos de API usados aqui são os definidos no [plano de backend](04-plano-backend.md#6-contratos-de-api-consolidados). Enquanto o backend não estiver pronto, o front trabalha com `HttpTestingController` e dublês, sem servidor real.

## 2. Guardrails para o agente

| # | Regra | Motivo |
|---|---|---|
| G-01 | **A estrutura `core/` · `features/` · `shared/` prevalece** sobre a instrução global "Vertical Slice DUPE". Não renomear pastas para `feat-*`, não criar domínios no lugar de `features/`, não mover `core/`. | `AGENTS.md` e `docs/arquitetura.md` do projeto têm precedência |
| G-02 | **Não usar o Liquid Design System.** O visual é Ionic com os tokens de [variables.scss](../mobile/src/theme/variables.scss) e os estilos de `src/styles`. | Identidade visual atual do app |
| G-03 | Componentes standalone, `ChangeDetectionStrategy.OnPush`, `input()`/`output()`/`model()`, control flow nativo (`@if`, `@for` com `track`, `@switch`). | Padrão existente |
| G-04 | `inject()` em campo. Membros usados no template seguem o padrão do arquivo (hoje `readonly` públicos); em arquivos novos, `protected readonly`. | Consistência e compilação de templates |
| G-05 | Estado com `signal`/`computed` em stores (`state/`) por funcionalidade. **Sem NgRx** e sem `BehaviorSubject` para estado simples. | Especificação, seção 50 |
| G-06 | HTTP com `HttpClient` em serviços de `data-access` (Observables), como hoje. `httpResource` não será adotado nesta evolução. | Não misturar padrões; manter os testes com `HttpTestingController` |
| G-07 | Formulários com Reactive Forms (Signal Forms ainda é experimental). | Padrão existente |
| G-08 | **Dinheiro em centavos inteiros** (`shared/utils/money.util.ts`). Proibido somar, multiplicar ou arredondar preço em `number` com casas decimais. | RG-03 |
| G-09 | Nenhuma dependência nova sem aprovação de D-07. Pré-aprovada condicionalmente: `@capacitor/app` (seção 4.3). | `AGENTS.md` |
| G-10 | Sem cache offline de produto: listas locais guardam referência e dados de exibição com data. Toda ação reconsulta o servidor. | Especificação, seção 35, e RG-04/RG-05 |
| G-11 | Textos exatamente como no [catálogo de mensagens](02-especificacao-funcional.md#5-catálogo-de-mensagens-padronizadas) e nas regras de cada item. | Consistência de experiência |
| G-12 | Acessibilidade WCAG AA: alvos ≥ 48 px, foco visível e gerenciado, `aria-live` para resultados, `role="alert"` para erros, sem depender só de cor. | RG-11 |
| G-13 | Testes ao lado dos arquivos; `npm run lint`, `npm run test:ci` e `npm run build` verdes a cada entrega. | `docs/testes.md` |
| G-14 | Toda tela nova é lazy (`loadComponent`). O bundle inicial não deve crescer mais do que o necessário para `core/` (atual: cerca de 726 kB brutos). | Desempenho em aparelhos simples |

## 3. Estrutura de pastas proposta

Legenda: `[novo]`, `[alterado]`, `[movido]`. Arquivos `.spec.ts` acompanham cada arquivo e não estão listados.

```text
src/app/
├── app.component.ts                         [alterado] bloqueio por inatividade
├── app.config.ts                            [alterado] interceptors, inicialização, @capacitor/app
├── app.routes.ts                            [alterado] novas rotas e guards
├── core/
│   ├── auth/
│   │   ├── auth-session.service.ts          [alterado] token, expiração, permissões
│   │   ├── auth-user.ts                     [alterado] AuthSession, Permissao
│   │   ├── auth.guard.ts
│   │   ├── permission.guard.ts              [novo] F3-07
│   │   ├── session-api.service.ts           [novo] logout e sessão atual
│   │   ├── reauth.service.ts                [novo] F0-01
│   │   ├── reauth-sheet.component.*         [novo] F0-01
│   │   ├── inactivity.service.ts            [novo] F1-07
│   │   └── lock-screen.component.*          [novo] F1-07
│   ├── config/
│   │   ├── api-config.service.ts            [alterado] salvar após recuperação
│   │   ├── app-parameters.service.ts        [novo] GET /api/parametros
│   │   ├── configuration.guard.ts
│   │   ├── server-configuration.guard.ts    [alterado] modo recuperação
│   │   ├── device-settings.store.ts         [novo] coletor, nome do aparelho, deviceId
│   │   ├── ip-address.util.ts
│   │   └── settings-access.service.ts
│   ├── connectivity/connectivity.service.ts [alterado] F1-08
│   ├── feedback/
│   │   ├── notification.service.ts          [alterado] toast com ação ("Desfazer")
│   │   └── haptics.util.ts                  [novo] vibração quando suportada
│   ├── http/
│   │   ├── app-error.ts                     [alterado] code, details, kind
│   │   ├── error-messages.ts                [novo] catálogo da especificação §5
│   │   ├── http-context.ts                  [novo] REQUEST_TIMEOUT_MS, SKIP_REAUTH, SKIP_AUTH
│   │   ├── api-headers.interceptor.ts       [novo] Authorization, X-App-Version, X-Device-Id
│   │   ├── session-renewal.interceptor.ts   [novo] 401 → reautenticação → repetição
│   │   ├── pending-writes.tracker.ts        [novo] gravações em andamento
│   │   └── app-error.interceptor.ts         [alterado] códigos e timeout
│   ├── navigation/
│   │   ├── back-button.service.ts           [novo] F0-11
│   │   └── unsaved-changes.guard.ts         [novo] F0-11
│   ├── platform/
│   │   ├── capacitor-app.port.ts            [novo] token injetável para @capacitor/app
│   │   └── app-lifecycle.service.ts         [novo] retomada e segundo plano
│   ├── produtos/                            [movido] de features/product-pricing/{data-access,models}
│   │   ├── produto-api.service.ts           [alterado] por ID, busca, simulação, histórico
│   │   ├── produto.models.ts                [alterado] contratos estendidos
│   │   └── product-lookup.service.ts        [novo] resolve código: encontrado/duplicado/não encontrado
│   ├── reports/report-api.service.ts        [novo] F2-06
│   ├── scanner/                             [movido] de features/scanner/data-access
│   │   ├── barcode-scanner.port.ts          [alterado] módulo Google, QR, openSettings
│   │   ├── barcode-scanner.service.ts       [alterado] F0-12
│   │   ├── hardware-scanner.service.ts      [novo] F1-04
│   │   └── scan-flow.service.ts             [novo] câmera, coletor e manual
│   ├── storage/local-store.ts               [novo] persistência versionada
│   └── version/
│       ├── app-version.ts                   [novo] fonte única da versão
│       ├── version-api.service.ts           [novo]
│       ├── version.service.ts               [novo] compatibilidade
│       └── version.guard.ts                 [novo]
├── features/
│   ├── app-update/pages/app-update/         [novo] F0-08
│   ├── auth/pages/login/                    [alterado]
│   ├── expiry/                              [novo] F3-03 (pages, components, state)
│   ├── home/                                [alterado] pendências, recentes, seções
│   ├── inventory/                           [novo] F2-04
│   ├── labels/                              [novo] F2-01 (pages, components, data-access, state)
│   ├── pending-registrations/               [novo] F3-05
│   ├── product-pricing/                     [alterado] consulta, alteração, sucesso, histórico
│   │   ├── components/price-review-sheet/   [novo] F1-01
│   │   ├── components/price-history-sheet/  [novo] F3-06
│   │   ├── components/duplicate-products/   [novo] F0-06
│   │   └── state/recents.store.ts           [novo] F1-06
│   ├── product-search/                      [novo] F3-01
│   ├── promotions/                          [novo] F2-03
│   ├── replenishment/                       [novo] F3-04
│   ├── scanner/pages/scanner/               [alterado]
│   ├── settings/                            [alterado] ajustes e configuração do servidor
│   └── shelf-audit/                         [novo] F2-02
└── shared/
    ├── pipes/
    │   ├── brl-currency.pipe.ts
    │   ├── brl-cents.pipe.ts                [novo]
    │   └── quantity.pipe.ts                 [novo]
    ├── ui/
    │   ├── app-header/                      [alterado] faixa "sem conexão", chip de operador
    │   ├── page-state/                      [alterado] tipos 'empty' e 'offline'
    │   ├── sheet-layout/                    [novo] painel padrão
    │   ├── price-input/                     [novo] campo de preço em centavos + atalhos
    │   ├── quantity-input/                  [novo]
    │   ├── product-summary/                 [novo]
    │   ├── status-badge/                    [novo]
    │   ├── mode-banner/                     [novo] consulta × alteração
    │   ├── stat-tile/                       [novo] pendências da Home
    │   ├── scan-actions/                    [novo] Escanear · Digitar · Buscar por nome
    │   └── scan-capture/                    [novo] captura do coletor
    ├── utils/
    │   ├── money.util.ts                    [novo]
    │   ├── quantity.util.ts                 [novo]
    │   ├── date.util.ts                     [novo]
    │   ├── semver.util.ts                   [novo]
    │   └── product-code.util.ts             [alterado] mensagens por tipo de código
    └── validators/
        ├── price.validator.ts               [alterado] baseado em centavos
        └── quantity.validator.ts            [novo]
```

**Refatoração preparatória (primeiro PR do front):**

- mover `features/scanner/data-access/*` para `core/scanner/`;
- mover `features/product-pricing/data-access/produto-api.service.ts` e `models/produto-response.ts` para `core/produtos/`;
- atualizar os imports e manter os testes verdes, **sem mudança de comportamento**.

Os novos módulos dependem desses serviços, e importá-los de outra funcionalidade criaria acoplamento entre fluxos.

## 4. Mudanças transversais

### 4.1 HTTP, erros e tempos de espera (F0-03, F0-10)

**`app-error.ts`**

```ts
export type AppErrorKind = 'network' | 'timeout' | 'http';

export class AppError extends Error {
  constructor(
    readonly status: number,
    readonly userMessage: string,
    readonly kind: AppErrorKind = 'http',
    readonly code: string | null = null,
    readonly details: Readonly<Record<string, unknown>> = {}
  ) {
    super(userMessage);
    this.name = 'AppError';
  }
}
```

**`error-messages.ts`**

- Mapa `código → mensagem`, copiado do catálogo da especificação (§5).
- Fallbacks por status (0, 400, 401, 403, 404, 409, 422, 426, 429, 500 e 503).
- `messageFor(code, status, serverMessage, details)`:
  - para `DADOS_INVALIDOS`, usa `serverMessage` (a API já escreve para o usuário);
  - para `MUITAS_TENTATIVAS`, interpola os minutos a partir de `details.segundosRestantes` ou do cabeçalho `Retry-After`.

**`http-context.ts`**

```ts
export const REQUEST_TIMEOUT_MS = new HttpContextToken<number>(() => 10_000);
export const SKIP_REAUTH = new HttpContextToken<boolean>(() => false);   // login, logout, versão, recuperação
export const SKIP_AUTH = new HttpContextToken<boolean>(() => false);     // chamadas a um IP candidato
```

**`app-error.interceptor.ts`** (alterado)

1. Aplica `timeout(req.context.get(REQUEST_TIMEOUT_MS))`.
2. Converte:
   - `TimeoutError` → `AppError(0, msg, 'timeout')`;
   - `HttpErrorResponse` com status 0 → `kind 'network'`;
   - demais → lê o corpo `ApiErrorBody` (`code`, `message`, `detalhes`) e produz `AppError` com `code` e `details`.
3. Mantém o comportamento de repassar `AppError` já convertido.

Chamadas existentes com tempo próprio passam a usar o contexto, em vez de `timeout()` local:

- `ConnectivityService.check()` → 4.000 ms;
- `ConfigurationApiService.testConnection()` → 7.000 ms.

**Ordem dos interceptors** em `app.config.ts`:

```ts
provideHttpClient(withInterceptors([apiHeadersInterceptor, sessionRenewalInterceptor, appErrorInterceptor]))
```

- Requisição: `apiHeaders` (adiciona cabeçalhos) → `sessionRenewal` → `appError` (aplica timeout) → rede.
- Erro: `appError` converte em `AppError` → `sessionRenewal` decide se reautentica e repete.

**`api-headers.interceptor.ts`**

- Apenas para URLs que começam com `ApiConfigService.baseUrl()`, e não para IPs candidatos (`SKIP_AUTH`):
  - adiciona `Authorization: Bearer <token>` quando houver sessão;
  - adiciona sempre `X-App-Version: APP_VERSION` e `X-Device-Id: <deviceId>`.
- Registra gravações (`POST`, `PATCH`, `PUT` e `DELETE`) no `PendingWritesTracker` (incrementa e, em `finalize`, decrementa).

**Ajustes da tela de senha do caixa** ([settings.page.ts](../mobile/src/app/features/settings/pages/settings/settings.page.ts)): a mensagem passa a vir de `AppError.userMessage`, eliminando o "Senha do caixa incorreta." genérico.

**Testes:**

- um caso por código do catálogo;
- timeout com `fakeAsync`/`tick`;
- status 0;
- `Retry-After`;
- corpo sem `code`;
- cabeçalhos presentes só para a URL configurada;
- `SKIP_AUTH` respeitado;
- contador de gravações.

### 4.2 Sessão, reautenticação e inatividade (F0-01, F1-07)

**Modelo** (`auth-user.ts`):

```ts
export type Perfil = 1 | 2 | 3;
export type Permissao =
  | 'PRODUTO_CONSULTAR' | 'PRECO_ALTERAR' | 'PRECO_ABAIXO_CUSTO' | 'CUSTO_VISUALIZAR'
  | 'ETIQUETA_IMPRIMIR' | 'HISTORICO_VISUALIZAR' | 'RELATORIO_ENVIAR';

export interface AuthUser { codigo: number; username: string; displayName: string; perfil: Perfil; }
export interface AuthSession {
  user: AuthUser;
  token: string | null;          // null quando o servidor ainda não emite token (transição)
  expiresAt: string | null;      // ISO-8601
  permissions: readonly Permissao[];
}
```

**`AuthSessionService`** (alterado):

- `start(session, rememberUsername)`, `user()`, `token()`, `permissions()`, `can(p)` (signal computado) e `isAuthenticated()`.
- `end({ revoke })`: com `revoke = true`, chama `SessionApiService.logout()` em modo "melhor esforço" (sem bloquear a saída).
- Persiste em `sessionStorage` na chave nova `precificacao.auth.session.v2`, com a mesma validação defensiva do `restoreUser()` atual. A chave antiga é removida ao iniciar.
- Servidor sem token (transição): `token = null` e `permissions` = todas, preservando o requisito "mesmo acesso".

**`ReauthService` + `ReauthSheetComponent`:**

- `reauthenticate(): Observable<boolean>`: abre **um único** painel, mesmo que várias requisições recebam 401 ao mesmo tempo (as demais aguardam o mesmo resultado, com `shareReplay`).
- Painel (`IonModal` em modo sheet):
  - título "Sua sessão terminou";
  - nome do operador bloqueado;
  - campo de senha com "Mostrar";
  - botões "Continuar" e "Entrar com outro usuário".
- "Continuar" chama `AuthApiService.login()` com `SKIP_REAUTH`, atualiza a sessão e emite `true`.
- "Entrar com outro usuário" faz `session.end({ revoke: true })`, navega para `/login` (`navigateRoot`) e emite `false`.

**`session-renewal.interceptor.ts`:**

- Age quando o erro for `AppError` com status 401, código `SESSAO_EXPIRADA` ou `SESSAO_AUSENTE`, houver sessão e a requisição não tiver `SKIP_REAUTH`.
- Chama `reauthenticate()`. Com `true`, repete **uma vez** a requisição, clonando-a com o novo `Authorization`. Com `false`, propaga o erro original, que a tela ignora porque a navegação já ocorreu.

**`InactivityService` + `LockScreenComponent`:**

- Registra `pointerdown` e `keydown` em `document` fora da zona do Angular (`NgZone.runOutsideAngular`) e atualiza `lastInteraction`.
- Verificação a cada 15 s e na retomada do app (`AppLifecycleService.resumed$`), contando o tempo em segundo plano.
- Bloqueia quando `agora − lastInteraction ≥ minutosInatividade` (de `AppParametersService`; padrão 10), houver sessão e `PendingWritesTracker.active() === 0`.
- `locked` (signal) é exibido em `AppComponent`: `@if (inactivity.locked()) { <app-lock-screen /> }` sobre o `ion-router-outlet`.
- O bloqueio **não navega**: a tela de baixo permanece montada, preservando o estado.
- Desbloqueio: login do mesmo usuário (nova sessão). "Entrar com outro usuário": `end({ revoke: true })` + `/login`.

**Testes:**

- uma única abertura do painel para vários 401 simultâneos;
- repetição com o token novo;
- cancelamento;
- bloqueio após o tempo (`fakeAsync`);
- bloqueio adiado com gravação em andamento;
- retomada após segundo plano longo.

### 4.3 Navegação e botão voltar (F0-11)

**Dependência (D-07):** `@capacitor/app` na linha 8.x, compatível com `@capacitor/core` 8.4. Justificativa: sem ela, o botão voltar do Android não é entregue ao app (nem `MainActivity` nem `BridgeActivity` o tratam) e o Ionic não consegue fechar painéis nem navegar de volta. A mesma dependência fornece os eventos de retomada usados em F1-07 e F1-08. Após a instalação: `npx cap sync android`.

**`capacitor-app.port.ts`:** `InjectionToken` com a fatia usada do plugin (`addListener('backButton' | 'resume' | 'pause')`, `minimizeApp()`), para permitir testes sem plugin.

**`BackButtonService`** (inicializado em `app.config.ts` via `provideAppInitializer`) usa `Platform.backButton.subscribeWithPriority` do Ionic:

| Prioridade | Condição | Ação |
|---|---|---|
| 150 | `PendingWritesTracker.active() > 0` | Toast "Aguarde a conclusão" e consome o evento |
| 100 | Painel aberto | Padrão do Ionic: fecha o painel |
| 10 | Rota atual é `/home`, `/login`, `/configuracao-servidor` (primeiro acesso) ou bloqueio ativo | 1º toque: toast "Toque novamente para sair". 2º toque em até 2 s: `minimizeApp()` |
| 0 | Demais | Padrão do Ionic: volta na pilha do `ion-router-outlet` |

**`unsavedChangesGuard`:**

- `canDeactivate` em `/produto/:codigo` e nas telas com formulário.
- Pergunta "Descartar o novo preço digitado?" (`IonAlert`, botões "Continuar editando" e "Descartar") quando o componente expõe `hasUnsavedChanges()` verdadeiro.
- Vale tanto para o voltar físico quanto para a seta do cabeçalho.

**Padronização de navegação** (`NavController` do Ionic):

| Ação | Hoje | Passa a ser |
|---|---|---|
| Salvar preço → Sucesso | `navigateByUrl('/sucesso')` | `navigateForward('/sucesso', { replaceUrl: true })` |
| Sucesso → Home | `navigateByUrl('/home')` | `navigateRoot('/home')` |
| Cancelar alteração / Consultar outro | `navigateByUrl('/scanner/…')` | `navigateBack('/scanner/…')` |
| Escanear próximo (consulta/sucesso) | — | Abre a câmera; com o código, `navigateForward(destino, { replaceUrl: true })` |
| Login → Home | `navigateByUrl('/home', { replaceUrl: true })` | `navigateRoot('/home')` |

`PricingSuccessStore.clear()` passa a ser chamado em `ionViewDidLeave` da tela de Sucesso.

**Validação em aparelho** (com o `agente-android-foursys` e o `quality-assurance`): o comportamento da tabela acima nas versões de Android usadas na loja, incluindo o gesto de voltar do Android 13+ (predictive back).

### 4.4 Conectividade (F1-08)

**`ConnectivityService`** (alterado):

- `lastCheckedAt` (signal).
- `check()` com contexto de 4 s.
- `checkThrottled()`: ignora se a última verificação ocorreu há menos de 5 s.
- Gatilhos:
  - eventos `online` e `offline` de `window`;
  - `AppLifecycleService.resumed$`;
  - entrada nas telas de leitura;
  - intervalo de 60 s ativo apenas com a Home visível (`ionViewDidEnter`/`ionViewWillLeave`).

**`AppHeaderComponent`** (alterado): input `offline` (boolean) que exibe, abaixo da toolbar, a faixa "Sem conexão com o servidor da loja · Tentar novamente" e emite `retry`. As páginas passam `[offline]="connectivity.state() === 'offline'"`. O componente continua apresentacional.

### 4.5 Leitor de código e coletor (F0-12, F1-04)

**`BarcodeScannerPort`** (alterado):

```ts
export interface BarcodeScannerPort {
  isSupported(): Promise<{ supported: boolean }>;
  isGoogleBarcodeScannerModuleAvailable(): Promise<{ available: boolean }>;
  installGoogleBarcodeScannerModule(): Promise<void>;
  addListener(
    eventName: 'googleBarcodeScannerModuleInstallProgress',
    listener: (event: { state: number; progress?: number }) => void
  ): Promise<{ remove: () => Promise<void> }>;
  scan(options: { formats: BarcodeFormat[] }): Promise<{ barcodes: Array<{ rawValue?: string; displayValue?: string }> }>;
  openSettings(): Promise<void>;
}
```

> Conferir os nomes exatos dos estados do evento de instalação na documentação do plugin instalado (`node_modules/@capacitor-mlkit/barcode-scanning/README.md`) antes de implementar.

**`BarcodeScannerService`** (alterado):

- `readiness`: signal com `'unknown' | 'checking' | 'ready' | 'installing' | 'failed' | 'unsupported'`; `installProgress`: signal de 0 a 100.
- `ensureReady()`:
  1. `isSupported()` falso (navegador no `ng serve`) → `'unsupported'`, e as telas oferecem só digitação, busca e coletor;
  2. módulo disponível → `'ready'`;
  3. caso contrário, chama `installGoogleBarcodeScannerModule()` e acompanha o evento até "concluído" (`'ready'`) ou "falhou"/"cancelado"/limite de 120 s (`'failed'`).
- `scan(formats = FORMATOS_PRODUTO)`:
  - chama `ensureReady()`;
  - **não solicita permissão de câmera** (o leitor do Google não exige);
  - retorna o código ou `null` quando o usuário cancela;
  - erros do plugin viram `ScannerUnavailableError`.
- `scanQr()`: o mesmo fluxo com `BarcodeFormat.QrCode` (F3-08).
- `openSettings()`: repassa ao plugin.
- Leitura bem-sucedida chama `vibrate(60)` (`haptics.util.ts`: `navigator.vibrate?.(ms)`, sem efeito quando não suportado).

**`HardwareScannerService`** (coletor):

- `enabled`, lido de `DeviceSettingsStore`.
- O componente `scan-capture` (shared/ui) renderiza um `input` com:
  - `inputmode="none"` (não abre o teclado virtual);
  - `autocomplete="off"`;
  - `aria-label="Leitura pelo coletor"`;
  - foco automático;
  - reforço do foco após `blur` (300 ms) quando nenhum painel estiver aberto.
- No `Enter`, emite o valor sem espaços e limpa o campo. Sem foco, mostra "Toque aqui para voltar a ler".

**Permissão de câmera** (com o `agente-android-foursys`): validada a leitura sem permissão em aparelho, avaliar a remoção de `android.permission.CAMERA` do [AndroidManifest.xml](../mobile/android/app/src/main/AndroidManifest.xml) (enquanto nenhuma funcionalidade usar `startScan()`) e atualizar a orientação de permissão de câmera no [README.md](../README.md).

**`ScanFlowService`:** ponto único usado por todas as telas.

```ts
acquireByCamera(): Promise<ScanOutcome>;  // { kind: 'code', code } | { kind: 'cancelled' } | { kind: 'unavailable', reason }
validate(code: string): CodeValidation;    // 'ok' | 'not-numeric' | 'too-long' | 'empty'
```

**Componente `scan-actions`** (apresentacional):

- botões "Escanear produto", "Digitar código" e "Buscar por nome", com o estado do leitor (`readiness`, `installProgress`);
- mostra `scan-capture` quando o coletor está ativo;
- outputs: `scan`, `manual`, `search` e `code`.

### 4.6 Produtos (`core/produtos`)

- **`ProdutoApiService`:** `buscarPorCodigo`, `buscarPorId`, `buscarPorDescricao(termo, limite)`, `simularPreco(id, novoPrecoCentavos)`, `atualizarPreco(id, comando)` e `historico(id, limite)`. A conversão de centavos ↔ número da API acontece **somente aqui**:

  ```ts
  const toApi = (cents: number): number => Number((cents / 100).toFixed(2));
  const fromApi = (value: number): number => Math.round(value * 100);
  ```

- **`ProductLookupService.resolve(code)`** devolve um resultado discriminado, consumido pelas telas:

  ```ts
  type LookupResult =
    | { kind: 'found'; product: Produto }                       // valores em centavos
    | { kind: 'duplicated'; candidates: ProdutoResumo[] }       // 409 CODIGO_DUPLICADO
    | { kind: 'not-found'; code: string; looksLikeScaleLabel: boolean }
    | { kind: 'error'; error: AppError };
  ```

- O modelo interno `Produto` usa **centavos** (`precoVendaCentavos`, `precoCustoCentavos`) e quantidades em `number`. O DTO de API (`ProdutoResponse`) fica isolado em `produto.models.ts`.

### 4.7 Persistência local (`core/storage`)

**`LocalStore<T>`:**

- Envelope `{ v: number; savedAt: string; data: T }`.
- `read()` valida com a função `validate(raw): T | null` (validação defensiva, como em [pricing-success.store.ts](../mobile/src/app/features/product-pricing/state/pricing-success.store.ts)). Versão diferente → `migrate` opcional ou descarte.
- `write()` com `try/catch` (cota) e aviso em log quando o conteúdo passar de 1 MB.
- `clear()`.

**Chaves** (todas com prefixo `precificacao.`):

| Chave | Escopo | Conteúdo | Limite |
|---|---|---|---|
| `device.settings.v1` | Aparelho | `deviceId` (`crypto.randomUUID()`), nome amigável, coletor ligado | — |
| `recents.<codigoUsuario>.v1` | Operador | Últimos 8 produtos (ID, código, descrição, tipo, hora) | 8 |
| `labels.queue.v1` | Aparelho | Fila de etiquetas | 300 itens |
| `labels.sent.v1` | Aparelho | Lotes enviados (código, data, itens resumidos) | 20 lotes |
| `shelfAudit.active.v1` / `shelfAudit.history.v1` | Aparelho | Conferência ativa / resumos | 1 / 10 |
| `promotions.reminders.v1` | Aparelho | Lembretes de fim de promoção | 100 |
| `inventory.counts.v1` | Aparelho | Contagens e itens | 3.000 itens por contagem |
| `expiry.records.v1` | Aparelho | Registros de validade | 1.000 |
| `replenishment.items.v1` | Aparelho | Itens de reposição | 300 |
| `pendingRegistrations.v1` | Aparelho | Pendências de cadastro | 300 |

Ao atingir o limite, a inclusão é bloqueada com uma mensagem orientando a envio ou limpeza ("A fila chegou a 300 itens. Envie para impressão antes de adicionar mais.").

Ajustes > "Dados deste aparelho" mostra os totais e permite limpar cada lista, com confirmação.

### 4.8 Dinheiro, quantidades e datas (`shared/utils`)

| Função | Comportamento |
|---|---|
| `digitsToCents(raw)` | Extrai dígitos (até 10) → centavos (`"1050"` → `1050`) |
| `formatCentsInput(cents)` | `1050` → `"10,50"` (campo) |
| `formatBRL(cents)` | `1050` → `"R$ 10,50"` |
| `applyPercent(baseCents, pct)` | Arredondamento comercial ao centavo, em inteiros (`Math.round` sobre o produto inteiro × pct ÷ 100, com tratamento de .5 para cima) |
| `withEnding(cents, 99 \| 90)` | Mantém os reais e troca os centavos (atalhos de remarcação, F1-02) |
| `endingDown(cents, 99 \| 90)` | Maior valor terminado em ,99/,90 que não ultrapasse o valor (R$ 8,08 → R$ 7,99); devolve `null` se não houver (calculadora de promoções, F2-03) |
| `variation(from, to)` | `{ deltaCents, percent }`, com `percent` em 1 casa decimal para exibição |
| `margin(price, cost)` | `(price − cost) / price`, só para pré-visualização (a revisão usa o valor da API) |
| `formatQuantity(value, unit)` | UN/CX/PC → inteiro; KG/G/L/ML → até 3 casas, sem zeros à direita, vírgula decimal |
| `daysUntil(date, today)` | Dias de calendário no fuso local |

`price.validator.ts` passa a validar centavos: obrigatório, > 0 e ≤ 9.999.999.999 (R$ 99.999.999,99), com testes do intervalo.

### 4.9 Versão e compatibilidade (F0-08)

- **`app-version.ts`:** `export const APP_VERSION = '1.1.0';` como fonte única, igual a `package.json` (o checklist de release confere as duas).
- **`MIN_API_VERSION_FOR_FEATURE`:** mapa `funcionalidade → versão mínima da API` (ex.: `etiquetas: '1.3.0'`).
- **`VersionService`:**
  - na inicialização (`provideAppInitializer`, sem bloquear mais de 4 s) e após salvar o servidor, chama `GET /api/versao` (`SKIP_REAUTH`, `SKIP_AUTH` quando for IP candidato);
  - calcula `compatibility` (`'ok' | 'app-outdated' | 'api-outdated' | 'unknown'`) e `apiVersion` (signals), além de `featureAvailable(nome)`.
  - Servidor antigo sem `/api/versao` (404) → `'api-outdated'` com versão "anterior a 1.1".
- **`versionGuard`** em `/login` e nas rotas autenticadas: `app-outdated` → `/atualizar-app`.

### 4.10 Permissões e parâmetros (F3-07, D-03, D-08)

- `AuthSessionService.can(p)` é a única fonte para habilitar ações. Sem a lista de permissões (transição), todas são concedidas.
- **`permissionGuard(p)`:** fábrica de `CanActivateFn`. Sem permissão, não navega e mostra o toast "Seu perfil não permite esta ação. Peça a um encarregado."
- Nos templates, ações sem permissão ficam **desabilitadas com motivo** (RG-13), via `computed`.
- **`AppParametersService`:**
  - carrega `GET /api/parametros` após o login;
  - mantém valores padrão locais se a chamada falhar: variação 30%, inatividade 10 min, validade 7 dias, contagem cega ligada, sugestões de rebaixa 7:10 · 3:30 · 1:50, balança desligada, lotes de 24 h;
  - recarrega na retomada do app.

### 4.11 Componentes compartilhados e diálogos acessíveis (F1-09)

| Componente | Entradas e saídas | Observações |
|---|---|---|
| `sheet-layout` | `title`, `subtitle`; conteúdo e ações projetados | Usado dentro de `IonModal` (sheet). Título com `id` para `aria-labelledby` |
| `price-input` | `ControlValueAccessor` (centavos ou `null`); `baseCents`, `shortcuts` (boolean), `invalid` | Máscara atual de [product-price.page.ts](../mobile/src/app/features/product-pricing/pages/product-price/product-price.page.ts) migrada para centavos; atalhos de F1-02 |
| `quantity-input` | `ControlValueAccessor`; `unit` | Inteiro ou até 3 casas, conforme a unidade |
| `product-summary` | `description`, `code`, `unit`, `priceCents`, `badges` | Linha compacta usada em listas |
| `status-badge` | `tone` (`success`, `warning`, `danger`, `neutral`), `label` | Sempre com texto (não só cor) |
| `mode-banner` | `mode` (`consulta`, `alteracao`) | "MODO ALTERAÇÃO" em laranja (`--ion-color-secondary`) |
| `stat-tile` | `label`, `value`, `tone`; `selected` | Cartões de "Pendências do dia" |
| `scan-actions` / `scan-capture` | Ver §4.5 | — |
| `page-state` (alterado) | `kind` ganha `'empty'` e `'offline'` | Ícone e textos por tipo |

**Migração dos modais próprios** (digitar código, senha do caixa) para `IonModal`:

- foco no primeiro campo em `ionModalDidPresent`;
- `aria-labelledby`;
- fechamento pelo voltar (Ionic + `@capacitor/app`) e pelo gesto;
- `Enter` envia;
- o foco volta ao botão de origem ao fechar.

## 5. Rotas

Os guards existentes continuam. A coluna "Guards" lista a ordem de execução.

| Rota | Componente | Guards | Observações |
|---|---|---|---|
| `''` | → `login` | — | — |
| `configuracao-servidor` | `ServerConfigurationPage` | `serverConfigurationGuard` (alterado) | `?modo=recuperacao` permitido **sem sessão** quando há IP salvo |
| `atualizar-app` | `AppUpdatePage` | — | Bloqueante |
| `login` | `LoginPage` | `configurationGuard`, `versionGuard` | — |
| `home` | `HomePage` | `configurationGuard`, `versionGuard`, `authGuard` | — |
| `scanner` | → `scanner/consulta` | — | — |
| `scanner/:modo` | `ScannerPage` | idem home | `consulta` · `alteracao` |
| `busca-produto` | `ProductSearchPage` | idem home | `?destino=consulta\|alteracao\|etiqueta\|conferencia\|inventario\|validade\|reposicao\|promocao` |
| `consulta-estoque/:codigo` | `ProductStockPage` | idem home | `?id=` quando escolhido por ID |
| `produto/:codigo` | `ProductPricePage` | idem home + `permissionGuard('PRECO_ALTERAR')`; `canDeactivate: unsavedChangesGuard` | `?id=&origem=&preco=` (preço sugerido em centavos) |
| `sucesso` | `PricingSuccessPage` | idem home | — |
| `etiquetas` | `LabelsPage` | idem home | Abas "Pendentes" e "Enviadas" |
| `conferencia` | `ShelfAuditPage` | idem home | Inicia ou retoma |
| `conferencia/resumo/:id` | `ShelfAuditSummaryPage` | idem home | — |
| `promocoes` | `PromotionsPage` | idem home | Calculadora e lembretes |
| `inventario` | `InventoryListPage` | idem home | — |
| `inventario/:id` | `InventoryCountPage` | idem home | — |
| `inventario/:id/relatorio` | `InventoryReportPage` | idem home | — |
| `validade` | `ExpiryDashboardPage` | idem home | — |
| `validade/novo` | `ExpiryNewPage` | idem home; `canDeactivate` | — |
| `reposicao` | `ReplenishmentPage` | idem home | — |
| `pendencias-cadastro` | `PendingRegistrationsPage` | idem home | `?codigo=` para pré-preencher |
| `ajustes` | `SettingsPage` | idem home | Seções novas |
| `**` | → `login` | — | — |

Destinos aceitos pela busca e pelo "Escanear próximo":

| Destino | Navegação após escolher o produto |
|---|---|
| `consulta` | `consulta-estoque/:codigo?id=` |
| `alteracao` | `produto/:codigo?id=&origem=ALTERACAO` |
| `etiqueta` | Volta para `etiquetas`, com o painel de quantidade aberto para o produto |
| `conferencia` | Volta para o ciclo da conferência com o produto |
| `inventario` | Volta para a contagem ativa, com o painel de quantidade |
| `validade` | `validade/novo?id=` |
| `reposicao` | Volta para `reposicao`, com o painel de inclusão |
| `promocao` | Volta para `promocoes` com o produto como base |

O retorno com produto escolhido usa `ProductPickResultStore` (`state/` em `product-search`): grava `{ destino, produtoId, codigo }`, e a tela de destino consome e limpa o valor em `ionViewWillEnter`. Isso evita passar objetos pela URL.

## 6. Telas

Cada tela lista: itens, arquivos, layout, estado e serviços, interações, estados obrigatórios (RG-06), acessibilidade e testes.

### 6.1 Configuração do servidor (F0-09, F3-08)

**Arquivos:** [server-configuration.page.*](../mobile/src/app/features/settings/pages/server-configuration/server-configuration.page.ts) (alterado), [configuration-api.service.ts](../mobile/src/app/features/settings/data-access/configuration-api.service.ts) (alterado: `validateCashierPassword(password, ip?)` e `getVersion(ip)`).

**Modos:**

| Modo | Origem | Senha do caixa | Ao salvar |
|---|---|---|---|
| `primeiro-acesso` | Sem IP salvo | Não | Salva e vai ao login |
| `interno` | Ajustes, após senha do caixa | Já validada | Salva e permanece |
| `recuperacao` | Login com servidor indisponível, `?modo=recuperacao` | **Sim, validada pelo novo IP** | Salva e volta ao login |

**Layout no modo recuperação:**

```text
┌───────────────────────────────────────┐
│ ←  Alterar servidor                   │
├───────────────────────────────────────┤
│ Servidor atual: 192.168.0.10 (sem     │
│ resposta)                             │
│ Novo endereço IP                      │
│ [ 192.168.0.20            ] (x)       │
│ [ Ler QR do servidor ]                │
│ [ Testar conexão ]                    │
│ ✓ Servidor encontrado:                │
│   Mercado Central · versão 1.1.0      │
│   É o servidor da sua loja? (o) Sim   │
│ Senha do caixa                        │
│ [ ••••                 ] Mostrar      │
├───────────────────────────────────────┤
│ [ Salvar e voltar ao login ]          │
│ Cancelar (mantém 192.168.0.10)        │
└───────────────────────────────────────┘
```

**Estado (signals):** `mode`, `ipValue`, `connectionState`, `testedIp`, `serverInfo` (`VersaoResponse | null`), `confirmedServer`, `cashierPassword` (FormControl), `saving` e `errorMessage`.

**Regras de habilitação:** "Salvar" exige IP testado igual ao IP digitado e `serverInfo` carregado. Nos modos `recuperacao` e `interno`, exige também a confirmação "É o servidor da sua loja?". No modo `primeiro-acesso`, a identificação é exibida sem exigir confirmação. No modo `recuperacao`, exige ainda a senha do caixa válida (validada no clique; erro 401 mostra a mensagem do catálogo e mantém o IP antigo).

**QR (F3-08):**

- `scanQr()` lê um conteúdo no formato `precificacao://servidor?ip=…&porta=8080&loja=…`.
- `parsePairingUri()` valida o esquema, IPv4 (`normalizeIpv4`) e porta 8080.
- Conteúdo inválido: "Este QR code não é do servidor da loja."
- Válido: preenche o IP e dispara o teste.

**Aviso de loja diferente:** quando `ApiConfigService` tiver `nomeLoja` salvo e o novo for diferente, exibir o aviso da especificação (F0-09, regra 6).

**Testes:**

- os três modos;
- recuperação sem sessão permitida pelo guard;
- recuperação com sessão redirecionada a Ajustes;
- senha validada contra o IP candidato (`SKIP_AUTH`);
- cancelar não altera a URL;
- QR válido e inválido.

### 6.2 Login (F0-03, F0-08, F0-09, F0-10, F1-07)

**Arquivos:** [login.page.*](../mobile/src/app/features/auth/pages/login/login.page.ts) (alterado), [auth-api.service.ts](../mobile/src/app/features/auth/data-access/auth-api.service.ts) (alterado: resposta com token).

**Layout:**

```text
┌───────────────────────────────────────┐
│            ▌▌▌ Precificação           │
│   ● Servidor online · Mercado Central │
├───────────────────────────────────────┤
│ Entrar                                │
│ Entrar como Carlos · Não é você?      │
│ Senha  [ ••••••          ] Mostrar    │
│ [x] Lembrar usuário                   │
│ [ Entrar → ]                          │
│ Esqueci minha senha                   │
├───────────────────────────────────────┤
│ ⚠ Não conseguimos falar com o         │
│ servidor da loja                      │  ← só quando offline
│ Servidor salvo: 192.168.0.10          │
│ [Tentar novamente] [Alterar servidor] │
├───────────────────────────────────────┤
│ Versão 1.1.0                          │
└───────────────────────────────────────┘
```

**Comportamento:**

1. Ao entrar na tela, `connectivity.check()`. Status visível no topo.
2. Status `offline`, ou login com `AppError.kind` `network`/`timeout`, exibe o cartão de recuperação. "Alterar servidor" navega para `configuracao-servidor?modo=recuperacao`.
3. `MUITAS_TENTATIVAS`: mensagem com minutos; botão "Entrar" desabilitado até o fim (signal `lockedUntil` + `setInterval` de 1 s para a contagem).
4. "Entrar como {nome}" quando há nome lembrado; "Não é você?" limpa o nome, desmarca "Lembrar" e foca o campo de usuário.
5. Versão a partir de `APP_VERSION`.
6. Sucesso: `session.start(...)`, `AppParametersService.load()` e `navigateRoot('/home')`.

**Testes:** cartão por status e por erro de rede; navegação para recuperação; bloqueio com contagem; "Não é você?"; versão exibida; sessão iniciada com token e permissões; servidor antigo sem token.

### 6.3 Atualize o aplicativo (F0-08)

**Arquivos:** `features/app-update/pages/app-update/*` (novo).

**Conteúdo:**

- título "Atualize o aplicativo";
- texto "Esta versão ({APP_VERSION}) não é mais compatível com o servidor da loja (exige {versaoMinimaApp} ou superior). Peça ao responsável a versão mais recente do aplicativo.";
- botões "Tentar novamente" (refaz a verificação; se compatível, `navigateRoot('/login')`) e "Configuração do servidor" (recuperação, para o caso de IP errado).

**Testes:** textos com versões; nova verificação compatível volta ao login.

### 6.4 Home (F1-06, F1-07, F1-08, módulos F2/F3)

**Arquivos:** [home.page.*](../mobile/src/app/features/home/pages/home/home.page.ts) (alterado), [home-action-card](../mobile/src/app/features/home/components/home-action-card/home-action-card.component.ts) (adiciona `count` opcional).

**Layout:**

```text
┌───────────────────────────────────────┐
│ Precificação          [Carlos · Ger ▾]│
├───────────────────────────────────────┤
│ Olá, Carlos      ● Online · 10:42     │
│ PENDÊNCIAS DO DIA                     │
│ [ 4 etiquetas ] [ 2 vencidos ]        │
│ [ 1 promoção termina hoje → Voltar ]  │
│ PRODUTO                               │
│ [ Consulta de estoque (destaque) ]    │
│ [ Alterar preço ] [ Buscar por nome ] │
│ GÔNDOLA                               │
│ [ Conferência ] [ Etiquetas (4) ]     │
│ [ Reposição ]                         │
│ CONTROLE                              │
│ [ Validade ] [ Inventário ]           │
│ [ Promoções ]                         │
│ RECENTES                              │
│ • COCA COLA 2L · 7894900011517 · 10:31│
│ • ARROZ T1 5KG · alteração · 10:12    │
├───────────────────────────────────────┤
│ Início  Scanner  Etiquetas(4)  Ajustes│
└───────────────────────────────────────┘
```

**Comportamento:**

- **Chip do operador** (no `app-header`): menu com "Trocar operador" (`end({ revoke: true })` + `/login` com nome em branco) e "Sair" (`end({ revoke: true })` + `/login`).
- **Pendências do dia:** `stat-tile` alimentados por `computed` dos stores (fila de etiquetas, validades vencidas ou do dia, lembretes vencidos, itens de reposição pendentes). Só aparecem quando > 0. "Voltar" do lembrete abre `produto/:codigo?id=&origem=PROMOCAO_FIM&preco=`.
- Cartões desabilitados com motivo (sem permissão, servidor desatualizado ou offline).
- **Recentes** (`RecentsStore`) só aparecem com itens.
- Status com "Verificado às HH:MM" e intervalo de 60 s enquanto a tela estiver visível.
- Barra inferior: a aba "Etiquetas" é habilitada, com contador.

**Testes:** pendências exibidas e ocultas; cartões desabilitados; trocar operador; recentes; intervalo iniciado e parado com a visibilidade.

### 6.5 Scanner e busca por nome (F0-12, F1-04, F3-01)

**Scanner** — arquivos: [scanner.page.*](../mobile/src/app/features/scanner/pages/scanner/scanner.page.ts) (alterado).

- `mode-banner` com o modo (consulta ou alteração).
- `scan-actions` com "Escanear produto", "Digitar código" e "Buscar por nome" (`busca-produto?destino=<modo>`). Mostra `scan-capture` com o coletor ligado.
- Estados do leitor:
  - `installing`: barra de progresso e texto "Preparando o leitor de código… É preciso internet apenas desta vez.";
  - `failed`: mensagem da especificação com "Tentar novamente" e "Digitar código";
  - `unsupported` (navegador): só digitação, busca e coletor.
- Código lido inválido: mensagens de F0-12 (não numérico, longo demais).
- O modal "Digitar código" migra para `IonModal` (§4.11).
- Sem conexão: estado `offline` da `page-state`, mantendo "Digitar código" desabilitado com o motivo.

**Busca por nome** — arquivos: `features/product-search/pages/product-search/*`, `features/product-search/state/product-pick-result.store.ts`.

```text
┌───────────────────────────────────────┐
│ ←  Buscar produto · CONSULTA          │
│ [ coca 2l                       ] (x) │
│ Mostrando 2 resultados                │
│ COCA COLA 2L                          │
│ 7894900011517 · UN · R$ 9,99          │
│ COCA COLA ZERO 2L                     │
│ 7894900700015 · UN · R$ 9,99          │
└───────────────────────────────────────┘
```

- `FormControl` com `debounceTime(400)`, `distinctUntilChanged` e mínimo de 3 caracteres (abaixo disso: "Digite pelo menos 3 letras"). `switchMap` cancela a busca anterior.
- Termo destacado com `<mark>`, gerado por função pura que divide o texto (sem `innerHTML`).
- Resultado `truncado`: "Mostrando 20 resultados. Refine a busca…".
- Seleção: navegação conforme a tabela de destinos (§5). No destino `alteracao`, passa `origem` e o marcador `selecionadoPorNome=1`, usado pela revisão (F3-01, regra 6).
- Estados: vazio inicial ("Digite o nome do produto"), carregando, sem resultados (mensagem da especificação), erro, offline.

**Testes:** debounce; mínimo de caracteres; cancelamento; destaque sem HTML; navegação por destino; truncado.

### 6.6 Consulta de estoque (F0-06, F1-03, F1-05, F3-02, F3-06)

**Arquivos:** [product-stock.page.*](../mobile/src/app/features/product-pricing/pages/product-stock/product-stock.page.ts) (alterado), `components/duplicate-products/*` (novo), `components/price-history-sheet/*` (novo).

**Comportamento:**

- Carrega por `ProductLookupService.resolve(codigo)` ou, com `?id=`, `buscarPorId`.
- `duplicated`: o componente `duplicate-products` lista os candidatos (descrição, código, preço) com "Abrir", que navega para `consulta-estoque/:codigo?id=`. Também oferece "Registrar pendência".
- `not-found`: estado de erro com:
  - "Buscar por nome";
  - "Registrar pendência" (`pendencias-cadastro?codigo=`);
  - "Escanear outro";
  - dica de balança quando `looksLikeScaleLabel` (F3-02).
- Conteúdo adicional:
  - "Outros códigos deste produto" (`codigosSimilares`);
  - "Encontrado pelo código similar {codigoLido}" / "Encontrado como {código alternativo}";
  - faixa "Etiqueta de balança: 0,532 kg" (`etiquetaBalanca`);
  - estoque com `quantity` pipe;
  - `status-badge` de estoque zerado, negativo e sem NCM, com a orientação de F1-05;
  - rodapé "Consultado às HH:MM".
- **Ações:**
  - "Escanear próximo" (`ScanFlowService.acquireByCamera()`; código → `navigateForward('/consulta-estoque/…', { replaceUrl: true })`; cancelado → permanece);
  - "Alterar preço" (desabilitado com motivo sem `PRECO_ALTERAR`);
  - "Histórico de preço" (se `HISTORICO_VISUALIZAR` e a API suportar).
- Grava em `RecentsStore` (tipo consulta).

**Testes:** os quatro resultados do lookup; similares e indicadores; balança; "Escanear próximo" com código e cancelado; ações por permissão.

### 6.7 Alteração de preço (F0-05, F1-01, F1-02, F3-06)

**Arquivos:**

- [product-price.page.*](../mobile/src/app/features/product-pricing/pages/product-price/product-price.page.ts) (alterado);
- `components/price-review-sheet/*` (novo);
- [product-card](../mobile/src/app/features/product-pricing/components/product-card/product-card.component.ts) (alterado: custo e margem opcionais);
- `shared/ui/price-input` (novo).

**Layout:**

```text
┌───────────────────────────────────────┐
│ ←  Alterar preço                      │
│ ▌MODO ALTERAÇÃO · origem: Conferência │
├───────────────────────────────────────┤
│ COCA COLA 2L                          │
│ EAN 7894900011517                     │
│ Preço atual R$ 9,50                   │
│ Custo R$ 7,20 · Margem 24,2%          │  ← se CUSTO_VISUALIZAR
├───────────────────────────────────────┤
│ Novo preço                            │
│ R$ [ 10,50                ]           │
│ +R$ 1,00 (+10,5%) · nova margem 31,4% │
│ Base: R$ 9,50                         │
│ [−10%] [−5%] [+5%] [+10%]             │
│ [Final ,99] [Final ,90]               │
├───────────────────────────────────────┤
│ [ Revisar alteração ]                 │
│ Cancelar                              │
│ Histórico de preço ›                  │
└───────────────────────────────────────┘
```

**Painel de revisão** (`price-review-sheet` dentro de `IonModal`):

```text
┌───────────────────────────────────────┐
│ Confirme a alteração                  │
│ COCA COLA 2L                          │
│ R$ 9,50  →  R$ 95,00                  │
│ +R$ 85,50 (+900,0%) · margem 92,4%    │
│ ⚠ Variação de +900,0% em relação ao   │
│   preço atual. Confira se digitou     │
│   corretamente.                       │
│ [ ] Conferi o valor                   │
│ ⓘ Produto escolhido pela busca por    │  ← só quando aplicável
│   nome. Confira a descrição.          │
│ [ Confirmar alteração ] (habilita ao  │
│                          marcar)      │
│ Corrigir valor                        │
└───────────────────────────────────────┘
```

**Fluxo técnico:**

1. **Carga:** por código ou ID; `?preco=` (centavos) preenche o campo (promoção, lembrete, validade); `?origem=` define a origem (padrão `ALTERACAO`).
2. **Pré-visualização local** (variação e margem com `money.util`), só informativa.
3. **"Revisar alteração":** `simularPreco(id, novo)` → abre o painel com `AvaliacaoPreco` (fonte de verdade dos alertas). Durante a simulação, o botão mostra spinner. Erro → mensagem do catálogo, sem abrir o painel.
4. **"Confirmar alteração":** `atualizarPreco(id, { novoPreco, precoAnterior: precoExibido, origem, alertasConfirmados })`.
5. **Respostas:**
   - `204`: grava o resumo (`PricingSuccessStore` com `alteradoEm`, `produtoId`, `precoAnterior` e `origem`), adiciona à fila de etiquetas (`LabelsQueueStore.addFromPriceChange`), grava em recentes e navega para `/sucesso` com `replaceUrl`.
   - `409 PRECO_ALTERADO`: painel de conflito com o preço de `details.precoAtual`. "Revisar com o preço novo" recarrega o produto e mantém o valor digitado; "Cancelar" fecha.
   - `422 CONFIRMACAO_NECESSARIA`: reabre a revisão com os alertas de `details.alertas`.
   - `AppError` `timeout`/`network` (F0-05, regra 6): `buscarPorId(id)`:
     - preço = novo → Sucesso com "Confirmado após nova verificação";
     - = anterior → "A alteração não foi feita. Tente novamente.";
     - outro valor → conflito.
   - `403`: mensagem de permissão.
6. **`hasUnsavedChanges()`:** campo preenchido e diferente do preço atual, sem gravação concluída.

**Testes:**

- pré-visualização;
- atalhos (base fixa, sem acúmulo, finais ,99/,90, resultado ≤ 0);
- simulação com e sem alertas;
- botão habilitado só com as confirmações marcadas;
- `204`/`409`/`422`/`403`;
- timeout com as três situações de reconsulta;
- origem e preço sugerido pela URL;
- guard de alterações não salvas;
- custo oculto sem permissão.

### 6.8 Sucesso (F1-01, F1-03, F2-01)

**Arquivos:** [pricing-success.page.*](../mobile/src/app/features/product-pricing/pages/pricing-success/pricing-success.page.ts) (alterado), [pricing-success.store.ts](../mobile/src/app/features/product-pricing/state/pricing-success.store.ts) (alterado: `alteradoEm`, `produtoId`, `origem` e `confirmadoPorVerificacao`).

**Conteúdo:**

- "Preço alterado no sistema da loja. **Troque a etiqueta da gôndola.**" (texto ajustável após a homologação do PDV);
- resumo com hora;
- "Etiqueta adicionada à fila ({n} pendentes)" com "Ver etiquetas";
- "Desfazer alteração", com contagem regressiva de 2:00;
- "Escanear próximo";
- "Voltar para Home".

**Desfazer:**

1. Abre a mesma revisão (`price-review-sheet`) com "R$ novo → R$ anterior" e origem `DESFAZER`.
2. Ao confirmar, envia com `precoAnterior = novo`.
3. Em caso de sucesso, remove da fila de etiquetas o item criado pela alteração, se ainda pendente, e mostra "Alteração desfeita".
4. Conflito: fluxo do §6.7.
5. Após 2 minutos, o botão some.

**Testes:** contagem regressiva (`fakeAsync`); desfazer com sucesso e com conflito; remoção da fila; limpeza do resumo ao sair; "Escanear próximo".

### 6.9 Etiquetas (F2-01)

**Arquivos:**

- `features/labels/pages/labels/*`;
- `features/labels/components/label-item/*` e `send-result-sheet/*`;
- `features/labels/data-access/labels-api.service.ts`;
- `features/labels/state/labels-queue.store.ts` e `labels-sent.store.ts`.

**Layout:**

```text
┌───────────────────────────────────────┐
│ ←  Etiquetas                          │
│ [ Pendentes (4) ]  [ Enviadas ]       │
├───────────────────────────────────────┤
│ COCA COLA 2L · R$ 10,50 · Alteração   │
│   Carlos · 10:31        [-] 1 [+]     │
│ ARROZ T1 5KG · R$ 24,90 · Conferência │
│   Ana · 09:12           [-] 2 [+]     │
│ …                                     │
├───────────────────────────────────────┤
│ [ + Adicionar produto ]               │
│ [ Enviar para impressão (5 etiquetas)]│
│ Limpar fila                           │
└───────────────────────────────────────┘
```

**`LabelsQueueStore`:**

- `items` (signal), `count` e `totalLabels` (computed);
- `addFromPriceChange(produto, origem, operador)`, `addManual(produto, quantidade)`, `setQuantity(produtoId, n)` (1 a 50), `remove(produtoId)` (com `restore` para "Desfazer"), `removeIfCreatedBy(changeRef)` (desfazer de F1-01) e `clear()`;
- deduplicação por `produtoId` com o aviso "Já estava na fila: atualizado";
- limite de 300 itens.

**Adicionar produto:** `scan-actions` em painel. Busca por nome com destino `etiqueta`. Painel de quantidade.

**Enviar:**

1. Exige conexão e `ETIQUETA_IMPRIMIR`.
2. `POST /api/etiquetas/lotes`, com nome do aparelho e itens (`produtoId`, `quantidade`, `precoNaFila`).
3. A resposta abre o `send-result-sheet`:
   - código do lote em destaque (fonte grande, espaçada);
   - URL `http://{ip}:8080/etiquetas/`;
   - avisos de `precoDiferenteDaFila` e itens ignorados;
   - botão "Entendi".
4. Os itens enviados saem da fila e o lote vai para `LabelsSentStore`.

**Aba Enviadas:**

- lotes recentes, com situação atualizada por `GET /api/etiquetas/lotes` ao abrir a aba e a cada 30 s com a aba visível ("Novo" ou "Impresso às HH:MM");
- "Reenviar" cria um novo lote com os mesmos itens.

**Estados:**

- fila vazia: "Nenhuma etiqueta pendente. As alterações de preço entram aqui automaticamente.";
- offline: envio desabilitado com motivo; a fila continua editável.

**Testes:** deduplicação; limites; desfazer remoção; envio (sucesso, avisos, ignorados, 403, offline); reenvio; atualização da situação; contador da Home e da barra.

### 6.10 Conferência de gôndola (F2-02)

**Arquivos:**

- `features/shelf-audit/pages/shelf-audit/*` e `shelf-audit-summary/*`;
- `features/shelf-audit/components/audit-result-card/*`;
- `features/shelf-audit/state/shelf-audit.store.ts`.

**Layout do ciclo:**

```text
┌───────────────────────────────────────┐
│ ←  Conferência · Corredor 5           │
│ 23 conferidos · 3 diferentes · 1 sem  │
├───────────────────────────────────────┤
│ COCA COLA 2L                          │
│ 7894900011517                         │
│ Preço no sistema                      │
│            R$ 10,50                   │
│ [ ✓ Etiqueta confere ]                │
│ [ ≠ Etiqueta diferente ]              │
│ [ ∅ Sem etiqueta ]                    │
├───────────────────────────────────────┤
│ [ Escanear próximo ]  Digitar · Nome  │
│ Ver resumo ›                          │
└───────────────────────────────────────┘
```

**`ShelfAuditStore`:**

- `active` (sessão: ID, nome, iniciada em, por, itens);
- `start(nome)`, `record(produto, resultado, precoEtiquetaCentavos?)`, `alreadyAudited(produtoId)`, `finish()` (move para o histórico, máximo 10) e `discard()`;
- persistência a cada registro.

**Comportamento:**

- Sem sessão ativa: tela inicial com o nome opcional e "Iniciar conferência". Com sessão: retoma.
- "Etiqueta diferente": painel com `price-input` "Preço na etiqueta".
  - Etiqueta menor: alerta vermelho da especificação e botões "Alterar preço" (navega para `produto/:codigo?id=&origem=CONFERENCIA`; ao concluir, o Sucesso mostra "Voltar à conferência") e "Continuar".
  - Diferente e sem etiqueta: entram na fila de etiquetas com origem `CONFERENCIA`.
- Produto já conferido: painel "Produto já conferido às HH:MM (resultado). Conferir de novo?".
- **Resumo:** totais, filtros e "Encerrar conferência" (confirmação); "Enviar ao computador da loja" (§6.15) se disponível.

**Testes:** iniciar e retomar; os três resultados; regra da etiqueta menor; deduplicação; integração com a fila; encerrar; persistência após recriar o store.

### 6.11 Promoções (F2-03)

**Arquivos:**

- `features/promotions/pages/promotions/*`;
- `features/promotions/components/calculator-*/*` (um por modo);
- `features/promotions/state/promotion-reminders.store.ts`;
- `features/promotions/utils/promotion-math.ts` (funções puras sobre centavos).

**Layout:**

```text
┌───────────────────────────────────────┐
│ ←  Promoções                          │
│ Base: COCA COLA 2L · R$ 9,50 [trocar] │
│ [Desconto %][Preço alvo][Leve X pague │
│  Y][R$/kg]                            │
├───────────────────────────────────────┤
│ Desconto  [ 15 ] %                    │
│ Arredondar: (o) ,99 ( ) ,90 ( ) não   │
│ Preço final    R$ 7,99                │
│ Economia       R$ 1,51 (15,9%)        │
├───────────────────────────────────────┤
│ [ Aplicar como novo preço ]           │
│ [x] Lembrar de voltar o preço em      │
│     [ 31/10/2026 ]                    │
├───────────────────────────────────────┤
│ LEMBRETES (2)                         │
│ COCA COLA 2L · termina 31/10 · voltar │
│ para R$ 9,50                          │
└───────────────────────────────────────┘
```

**Regras de implementação:**

- `promotion-math.ts`: `discountPrice`, `discountForTarget`, `buyXPayY` e `pricePerKgOrL`, todos em centavos, com testes de borda (X ≤ Y, conteúdo zero, arredondamentos). O arredondamento usa `endingDown` (sempre para baixo); quando devolve `null`, a tela mostra "Valor baixo demais para arredondar".
- "Aplicar como novo preço":
  - disponível só nos modos 1 e 2, com produto selecionado e `PRECO_ALTERAR`;
  - navega para `produto/:codigo?id=&origem=PROMOCAO&preco=<centavos>`;
  - o lembrete só é criado **após** o Sucesso. O Sucesso consulta `PromotionDraftStore` para saber se a alteração veio de uma promoção com lembrete marcado.
- "Leve X, pague Y" e "R$/kg": sem botão de aplicar, com o texto da especificação.
- Lembretes:
  - `PromotionRemindersStore` (`add`, `due(today)`, `done(id)`);
  - "Voltar preço" navega com `origem=PROMOCAO_FIM&preco=<precoBase>`;
  - concluído o Sucesso, o lembrete é marcado como feito;
  - aviso fixo "Lembrete salvo apenas neste aparelho".

**Testes:** as quatro calculadoras; habilitação de "Aplicar"; criação do lembrete só após o Sucesso; pendência na Home no dia do fim.

### 6.12 Inventário (F2-04)

**Arquivos:**

- `features/inventory/pages/inventory-list/*`, `inventory-count/*` e `inventory-report/*`;
- `features/inventory/components/quantity-sheet/*`;
- `features/inventory/state/inventory.store.ts`.

**Layout da contagem:**

```text
┌───────────────────────────────────────┐
│ ←  Contagem · Mercearia               │
│ 148 itens · 1.284 unidades            │
├───────────────────────────────────────┤
│ [ Escanear produto ]  Digitar · Nome  │
│ Último: FEIJÃO CARIOCA 1KG · 24 UN    │
├───────────────────────────────────────┤
│ ITENS CONTADOS                        │
│ FEIJÃO CARIOCA 1KG        24 UN  ›    │
│ PICANHA BOVINA KG     12,350 kg  ›    │
│ …                                     │
├───────────────────────────────────────┤
│ [ Encerrar contagem ]                 │
└───────────────────────────────────────┘
```

**`InventoryStore`:**

- `counts` (lista);
- `create(nome, setor)`, `addQuantity(countId, produto, qtd, modo: 'somar' | 'substituir')`, `edit`, `remove` (com desfazer);
- `finish(countId, snapshot)` grava o retrato do estoque `{ produtoId, estoqueSistema, lidoEm }`, o preço e o custo (se permitido);
- limite de 3.000 itens por contagem.

**Comportamento:**

- **Painel de quantidade** (`quantity-sheet`):
  - teclado numérico;
  - decimais conforme a unidade;
  - produto já contado: "Já contado: 12" com o segmento "Somar | Substituir" (padrão Somar) e o total resultante;
  - contagem cega: nunca mostra o estoque do sistema (parâmetro `contagemCega`).
- **Encerrar:**
  1. confirmação;
  2. busca por ID de todos os itens em lotes de 10 requisições paralelas no máximo, com progresso ("Lendo estoque do sistema: 80/148");
  3. falhas pontuais marcam o item como "não lido" e permitem "Tentar novamente" só para eles;
  4. gera o relatório.
- **Relatório:**
  - filtros "Com divergência" e "Sem divergência";
  - colunas: produto, contado, sistema, diferença e diferença em R$;
  - totais;
  - aviso do horário da leitura;
  - "Enviar ao computador da loja" (§6.15);
  - rodapé "O ajuste de estoque é feito no sistema principal."

**Testes:** somar e substituir; decimais por unidade; contagem cega; encerramento com falhas parciais e nova tentativa; cálculos do relatório; limite; persistência.

### 6.13 Validade (F3-03)

**Arquivos:**

- `features/expiry/pages/expiry-dashboard/*` e `expiry-new/*`;
- `features/expiry/components/markdown-warning-sheet/*`;
- `features/expiry/state/expiry.store.ts`.

**Layout do painel:**

```text
┌───────────────────────────────────────┐
│ ←  Validade                [+ Novo]   │
│ [Vencidos 2] [Hoje 1] [7 dias 5]      │
│ [Demais 12]                           │
├───────────────────────────────────────┤
│ IOGURTE NAT 170G · venceu 27/10       │
│   6 un · Gôndola 3                    │
│   [Retirado] [Rebaixar] [•••]         │
│ PÃO DE FORMA · vence hoje             │
│ …                                     │
└───────────────────────────────────────┘
```

**Comportamento:**

- **Novo registro:**
  - produto (leitura, busca ou coletor);
  - data com atalhos "Hoje", "+3 dias", "+7 dias" e `ion-datetime` (formato `dd/mm/aaaa`);
  - quantidade (opcional, `quantity-input`) e local (opcional).
  - Data passada pede confirmação ("A data informada já passou. Registrar como vencido?").
- `ExpiryStore`: `add`, `setStatus(id, 'retirado' | 'esgotado')`, `remove` e `groups(today, diasAlerta)` (computed).
- **Rebaixar:**
  1. sugestão de desconto pelos dias restantes (`sugestoesRebaixa`);
  2. `markdown-warning-sheet` com o aviso obrigatório e a caixa "Entendi";
  3. navega para `produto/:codigo?id=&origem=VALIDADE&preco=<centavos>`.
- Aviso fixo "Registros salvos apenas neste aparelho" (v1).

**Testes:** agrupamento por data (fuso local); atalhos; data passada; sugestão por faixas; aviso obrigatório antes de navegar.

### 6.14 Reposição (F3-04)

**Arquivos:** `features/replenishment/pages/replenishment/*`, `features/replenishment/state/replenishment.store.ts`.

**Comportamento:**

- Adicionar: produto → o app mostra o estoque do sistema → quantidade a repor (opcional) e observação.
- Classificação:
  - "Ruptura: sem estoque no sistema" (`status-badge` danger, com "Avise compras") quando o estoque é ≤ 0;
  - "Repor da reserva" nos demais casos.
- Lista com filtro "Pendentes / Repostos". `ion-item-sliding` com "Reposto". Remover tem desfazer.
- "Enviar ao computador da loja" (§6.15).

**Testes:** classificação; filtros; arrastar para repor; envio.

### 6.15 Pendências de cadastro e envio de relatórios (F3-05, F2-06)

**Pendências** — arquivos: `features/pending-registrations/pages/pending-registrations/*`, `features/pending-registrations/state/pending-registrations.store.ts`.

- Formulário com:
  - código (pré-preenchido por `?codigo=`, somente leitura quando vindo de um "não encontrado");
  - "Nome na embalagem" (obrigatório, até 60 caracteres);
  - observação (opcional, até 200).
- Lista com situação "Aberta" ou "Resolvida". Deduplicação por código, com o aviso "Já registrado em dd/mm".
- Texto fixo: "O cadastro é feito no sistema principal."

**Envio de relatórios** — `core/reports/report-api.service.ts` e uma função pura por tipo (`toInventoryReport`, `toShelfAuditReport`, `toReplenishmentReport`, `toPendingRegistrationsReport`) que monta `{ tipo, titulo, colunas, linhas }` com textos e valores já formatados em pt-BR.

- Botão "Enviar ao computador da loja":
  - exige conexão e `RELATORIO_ENVIAR`;
  - fica indisponível quando a API não suporta (`featureAvailable('relatorios')`).
- Sucesso: "Relatório salvo no computador da loja, na pasta Relatórios, como {nomeArquivo}".
- A neutralização de fórmulas é responsabilidade da API. O app não precisa escapar.

**Testes:** montagem das linhas por tipo (formatação pt-BR); estados do botão; mensagens de sucesso e erro.

### 6.16 Ajustes (F0-08, F1-04, F1-07)

**Arquivos:** [settings.page.*](../mobile/src/app/features/settings/pages/settings/settings.page.ts) (alterado).

| Seção | Conteúdo |
|---|---|
| Conexão | "Configuração do servidor" (senha do caixa, como hoje, em `IonModal`) |
| Leitura | "Usar leitor físico (coletor)"; situação do leitor da câmera (pronto, preparando, indisponível) com "Preparar leitor agora" |
| Aparelho | Nome do aparelho (aparece nos lotes de etiquetas e nos relatórios) |
| Sessão | Operador atual, "Trocar operador", "Sair" |
| Dados deste aparelho | Totais por lista e "Limpar" por lista, com confirmação; "Limpar recentes" |
| Sobre | Versão do app, versão do servidor, nome da loja, IP, "Verificado às HH:MM" |

**Testes:** cada seção; mensagens de erro da senha do caixa por código (F0-10); limpeza com confirmação.

### 6.17 Reautenticação e bloqueio por inatividade (F0-01, F1-07)

Especificados no §4.2. **Requisitos de interface:**

- **Painel de reautenticação:**
  - não fecha pelo gesto nem pelo voltar sem escolha explícita (`backdropDismiss: false`, `canDismiss` controlado), para não deixar a requisição pendente sem resposta;
  - foco no campo de senha;
  - erro de senha: "Usuário ou senha incorretos.";
  - bloqueio por tentativas: mensagem de `MUITAS_TENTATIVAS`.
- **Bloqueio por inatividade:**
  - sobreposição em tela cheia, com nome e perfil;
  - campo de senha e "Desbloquear";
  - link "Entrar com outro usuário";
  - o voltar minimiza o app (§4.3);
  - leitores de tela anunciam "Sessão bloqueada por inatividade".

## 7. Contratos consumidos (TypeScript)

Espelho dos contratos do [plano de backend](04-plano-backend.md#6-contratos-de-api-consolidados). Valores monetários vêm em reais (`number` com 2 casas) e são convertidos para centavos em `core/produtos`.

```ts
export type OrigemAlteracao = 'ALTERACAO' | 'CONFERENCIA' | 'PROMOCAO' | 'PROMOCAO_FIM' | 'VALIDADE' | 'DESFAZER';
export type AlertaPreco = 'ABAIXO_DO_CUSTO' | 'VARIACAO_ALTA';

export interface LoginResponse {
  codigo: number; username: string; displayName: string; perfil: 1 | 2 | 3;
  token?: string; expiraEm?: string; permissoes?: Permissao[];   // ausentes em servidores antigos
}
export interface SessaoResponse { usuario: Omit<LoginResponse, 'token' | 'expiraEm' | 'permissoes'>; expiraEm: string; permissoes: Permissao[]; }

export interface ProdutoResponse {
  id: number; codigoBarras: string; descricao: string; unidade: string | null;
  precoVenda: number; codigoNcm: string | null; estoqueAtual: number | null;
  codigosSimilares?: string[];
  encontradoPor?: 'CODIGO_PRINCIPAL' | 'CODIGO_SIMILAR' | 'CODIGO_ALTERNATIVO' | 'ETIQUETA_BALANCA';
  codigoLido?: string;
  etiquetaBalanca?: { tipo: 'PESO' | 'PRECO'; valor: number } | null;
  precoCusto?: number | null;           // só com CUSTO_VISUALIZAR
  margemPercentual?: number | null;     // só com CUSTO_VISUALIZAR
}
export interface ProdutoResumoResponse { id: number; codigoBarras: string; descricao: string; unidade: string | null; precoVenda: number; }
export interface BuscaProdutosResponse { itens: ProdutoResumoResponse[]; truncado: boolean; }

export interface SimulacaoPrecoRequest { novoPreco: number; }
export interface AvaliacaoPrecoResponse {
  precoAtual: number; novoPreco: number; variacaoValor: number; variacaoPercentual: number;
  precoCusto: number | null; margemAtual: number | null; margemNova: number | null;
  alertas: AlertaPreco[];
}
export interface AtualizarPrecoRequest {
  novoPreco: number; precoAnterior?: number; origem?: OrigemAlteracao; alertasConfirmados?: AlertaPreco[];
}

export interface ApiErrorBody {
  timestamp: string; status: number; error: string; code: string; message: string; path: string;
  detalhes?: { precoAtual?: number; alertas?: AlertaPreco[]; produtos?: ProdutoResumoResponse[]; segundosRestantes?: number };
}

export interface VersaoResponse { versaoApi: string; versaoMinimaApp: string; nomeLoja: string | null; }
export interface ParametrosResponse {
  limiteVariacaoPercentual: number; minutosInatividade: number; diasAlertaValidade: number;
  contagemCega: boolean; sugestoesRebaixa: Array<{ diasRestantes: number; percentual: number }>;
  balancaHabilitada: boolean; etiquetasValidadeHoras: number;
}

export interface CriarLoteEtiquetasRequest {
  aparelho: string;
  itens: Array<{ produtoId: number; quantidade: number; precoNaFila: number }>;
}
export interface LoteEtiquetasResponse {
  codigo: string; criadoEm: string; aparelho: string; operador: string | null;
  situacao: 'NOVO' | 'IMPRESSO'; impressoEm: string | null;
  itens: Array<{ produtoId: number; codigoBarras: string; descricao: string; unidade: string | null;
                 preco: number; quantidade: number; precoDiferenteDaFila: boolean }>;
  ignorados: Array<{ produtoId: number; motivo: 'PRODUTO_NAO_ENCONTRADO' }>;
}
export interface LoteResumoResponse {
  codigo: string; criadoEm: string; aparelho: string; operador: string | null;
  totalEtiquetas: number; situacao: 'NOVO' | 'IMPRESSO'; impressoEm: string | null;
}

export interface HistoricoPrecoResponse {
  dataHora: string; precoAnterior: number; precoNovo: number; operador: string | null; origem: OrigemAlteracao | null;
}

export interface EnviarRelatorioRequest {
  tipo: 'INVENTARIO' | 'CONFERENCIA' | 'REPOSICAO' | 'PENDENCIAS_CADASTRO';
  aparelho: string; titulo: string; colunas: string[]; linhas: string[][];
}
export interface EnviarRelatorioResponse { nomeArquivo: string; pasta: string; }
```

## 8. Testes

| Camada | O que testar | Ferramentas |
|---|---|---|
| Utils e funções puras | `money.util`, `quantity.util`, `date.util`, `semver.util`, `promotion-math`, montagem de relatórios, `parsePairingUri` | Jasmine, tabelas de casos (inclusive bordas de arredondamento) |
| Stores | Inclusão, deduplicação, limites, persistência (recriar o store e ler de novo), migração de versão | Jasmine com `localStorage` limpo a cada teste |
| Serviços HTTP | URL, método, corpo, conversão de centavos, contexto (`SKIP_*`, timeout) | `HttpTestingController` |
| Interceptors | §4.1 e §4.2 | `HttpTestingController`, `fakeAsync` |
| Guards | `version`, `permission`, `unsavedChanges`, `serverConfiguration` (modo recuperação) | `TestBed.runInInjectionContext` |
| Serviços de plataforma | Back button, ciclo de vida, leitor (instalação do módulo, cancelamento, indisponível) e coletor | Dublês dos tokens `CAPACITOR_APP` e `BARCODE_SCANNER` |
| Páginas | Estados obrigatórios (RG-06), habilitação por permissão e conexão, navegação, mensagens | `TestBed` com serviços dublados, `ComponentFixture` |

**Meta:** toda unidade nova com testes; cobertura de linhas ≥ 90% nos arquivos novos de `core/`, `shared/utils` e `state/` (relatório do `karma-coverage`).

**Validação manual em aparelho** (entregue ao `quality-assurance` após `npx cap sync android`):

1. Botão voltar em todas as telas da tabela do §4.3, inclusive com painéis abertos e durante uma gravação.
2. Leitor: primeira leitura com e sem o módulo do Google; Wi-Fi sem internet; cancelamento; código não numérico.
3. Coletor (se houver) em scanner, conferência, inventário e etiquetas.
4. Inatividade com o app em primeiro e em segundo plano.
5. Recuperação do servidor (troca real de IP).
6. Impressão de um lote no PC da loja (Chrome/Edge, A4).
7. Fluxos completos de conferência, inventário com 100 itens e validade.
8. Leitores de tela (TalkBack) nas telas de alteração e revisão.

## 9. Engenharia e qualidade (F0-13)

1. **`npm audit`:**
   - gerar o relatório com `npm audit --omit=dev` e `npm audit`;
   - classificar por execução × desenvolvimento;
   - corrigir com atualizações compatíveis dentro das linhas atuais (Angular 21.2.x, Ionic 8.x, Capacitor 8.x);
   - registrar no PR as vulnerabilidades aceitas, com justificativa.
   - Sem `npm audit fix --force`.
2. **Vitest (D-10):**
   - em PR isolado, adotar o executor de testes unitários com Vitest suportado oficialmente pelo Angular 21;
   - manter os mesmos specs, trocando só as APIs de dublê necessárias (`jasmine.createSpy` → `vi.fn`);
   - atualizar `docs/testes.md` e os scripts `test` e `test:ci`.
   - Até a decisão, permanece o Karma.
3. **Budgets:** conferir `angular.json` e manter as telas novas como lazy (G-14).
4. **Release Android** (com o `agente-android-foursys`):
   - `npx cap sync android` após adicionar `@capacitor/app`;
   - APK assinado de release;
   - `versionCode` e `versionName` alinhados a `APP_VERSION`;
   - validação em aparelhos reais.

## 10. Ordem de implementação no frontend

Cada passo termina com `npm run lint`, `npm run test:ci` e `npm run build` verdes. A complexidade relativa está entre parênteses.

| Onda | Passo | Itens | Depende de (backend) |
|---|---|---|---|
| 1 | Refatoração preparatória (§3) e `LocalStore`, `money.util`, `quantity.util` (baixa) | — | — |
| 1 | Camada HTTP: `AppError`, catálogo, timeout, `api-headers` (média) | F0-10 | — |
| 1 | Versão e tela "Atualize o aplicativo" (baixa) | F0-08 | `/api/versao` |
| 1 | Login com status e recuperação do servidor (média) | F0-09 | `/api/versao` com `nomeLoja` |
| 1 | `@capacitor/app`, back button, guard de alterações e padronização de navegação (média) | F0-11 | — |
| 1 | Leitor robusto (média) | F0-12 | — |
| 1 | Sessão com token, reautenticação e mensagens 429 (média) | F0-01, F0-03 | Login com token, `/api/auth/*` |
| 1 | Envio de `precoAnterior` e tratamento de 409/timeout (média) | F0-05 | `PATCH` com `precoAnterior` |
| 1 | Duplicidade na consulta (baixa) | F0-06 | 409 `CODIGO_DUPLICADO` |
| 2 | Revisão com simulação, atalhos e desfazer (alta) | F1-01, F1-02 | Simulação, `PATCH` com confirmações |
| 2 | Fluxo contínuo, consulta enriquecida e recentes (média) | F1-03, F1-05, F1-06 | Similares na resposta |
| 2 | Inatividade, chip de operador e conectividade viva (média) | F1-07, F1-08 | `/api/parametros` |
| 2 | Coletor e migração dos modais (baixa) | F1-04, F1-09 | — |
| 3 | Etiquetas (alta) | F2-01 | Lotes |
| 3 | Conferência (média) | F2-02 | — |
| 3 | Promoções v1 (média) | F2-03 | — |
| 3 | Relatórios e inventário v1 (alta) | F2-06, F2-04 | `/api/relatorios` |
| 4 | Busca por nome (média) | F3-01 | Busca |
| 4 | Balança e alternativos (baixa) | F3-02 | Resolução no backend |
| 4 | Validade, reposição e pendências (média cada) | F3-03, F3-04, F3-05 | Relatórios |
| 4 | Histórico de preço (baixa) | F3-06 | Histórico |
| 4 | Permissões efetivas (média) | F3-07 | Permissões no login |
| 4 | Pareamento por QR (baixa) | F3-08 | `/pareamento/` |

## 11. Definição de pronto (frontend)

- [ ] Regras e mensagens do item conferidas com a [especificação](02-especificacao-funcional.md).
- [ ] Estados carregando, conteúdo, vazio, erro e offline implementados e testados.
- [ ] Acessibilidade: rótulos, foco, `aria-live` e `role="alert"`, alvos ≥ 48 px, contraste.
- [ ] Sem cálculo monetário fora de centavos inteiros.
- [ ] Botão voltar coerente com a tabela do §4.3.
- [ ] `npm run lint`, `npm run test:ci` e `npm run build` verdes; cobertura dos arquivos novos ≥ 90%.
- [ ] Nenhuma dependência nova sem D-07; nenhuma alteração fora da estrutura `core/`, `features/`, `shared/`.
- [ ] `docs/arquitetura.md` e `docs/testes.md` atualizados quando a organização ou os testes mudarem.
- [ ] Checklist de aparelho do §8 repassado ao `quality-assurance`.
