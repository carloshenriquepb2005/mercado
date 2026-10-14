# Arquitetura

```text
Ionic/Angular no Android
        │ HTTP na Wi-Fi local
        ▼
adapter.input (controllers REST)
        ▼
port.input → core.usecase / core.service → core.model
        ▼
port.output → adapter.output (Spring Data JPA)
        ▼
MySQL 5.5 legado → PDV VB.NET
```

O backend utiliza arquitetura hexagonal moderada. `core` e `port` são Java puro, sem dependências de Spring, JPA, Hibernate ou HTTP. Regras de negócio ficam exclusivamente em classes com sufixo `UseCase` ou `Service`; os modelos apenas representam dados.

Os pacotes principais são:

```text
br/com/mercado/precificacao/
├── adapter/
│   ├── config/
│   ├── exception/
│   ├── input/
│   └── output/
├── config/
├── core/
│   ├── exception/
│   ├── mapper/
│   ├── model/
│   ├── service/
│   └── usecase/
├── port/
│   ├── input/
│   └── output/
└── Application.java
```

As portas usam os sufixos `InputPort` e `OutputPort`. Controllers, validação HTTP e DTOs REST ficam em `adapter.input`; cada controller possui uma interface Swagger individual em `adapter.input.swagger`. JPA e transações ficam em `adapter.output`, enquanto a criação dos casos de uso é feita por `config.BeanConfiguration`.

O frontend é organizado por funcionalidades; páginas consomem serviços e o scanner nativo fica encapsulado. Para compatibilidade com o MySQL 5.5, o backend usa Connector/J 5.1.49 e o dialeto legado da comunidade Hibernate. O schema fica integralmente sob responsabilidade do sistema existente (`ddl-auto: none`).

## Organização do aplicativo móvel

O código Angular em `mobile/src/app` é dividido por responsabilidade:

```text
app/
├── core/       # autenticação, HTTP, conectividade e infraestrutura global
├── features/   # fluxos funcionais organizados verticalmente
│   ├── auth/
│   ├── home/
│   ├── product-pricing/
│   └── scanner/
├── shared/     # UI, pipes, validadores e utilitários reutilizáveis
├── app.component.ts
├── app.config.ts
└── app.routes.ts
```

Cada funcionalidade pode conter `pages`, `components`, `data-access`, `models` e `state`. As páginas coordenam navegação, formulários e chamadas; componentes específicos permanecem na funcionalidade. Elementos visuais sem regra de negócio e usados por mais de um fluxo ficam em `shared/ui`. Integrações HTTP e nativas ficam em `data-access`, enquanto estado compartilhado entre páginas da mesma funcionalidade fica em `state`.

Testes permanecem ao lado dos arquivos testados. Estilos globais são limitados aos tokens em `theme`, ao reset em `global.scss` e aos estilos comuns de layout e ações em `styles`.
