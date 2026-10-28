# 02 — Especificação funcional das novas modificações

> Pasta `melhorias/` · Documento 2 de 5. O diagnóstico está na [análise](01-analise.md); a implementação, nos planos de [frontend](03-plano-frontend.md) e [backend](04-plano-backend.md); a coordenação, no [plano mestre](00-master.md).

## 1. Como ler este documento

- Cada modificação tem um **ID** (`F0-01`, `F1-03`…), usado nos outros documentos.
- **Classificação de impacto:**
  - **[L]** cabe nas regras atuais;
  - **[P]** guarda dados fora do legado (aparelho ou servidor);
  - **[R]** exige mudar uma regra do `AGENTS.md`, dos requisitos ou da especificação.
- **Prioridade:**
  - **Essencial**: corrige risco ou bloqueio;
  - **Importante**: ganho claro no dia a dia;
  - **Desejável**: refinamento.
- **Decisões** (`D-xx`) e **riscos** (`R-xx`) estão detalhados no [plano mestre](00-master.md#9-decisões-pendentes).
- As **mensagens** entre aspas são o texto exato a exibir. Ajustes de redação passam pelo agente `product-owner`.
- Os **critérios de aceite** usam Gherkin em português e servem de base para os testes do agente `quality-assurance`.

## 2. Personas e contexto de uso

| Persona | Perfil legado | Rotina | O que importa para ela |
|---|---|---|---|
| Atendente / repositor | 1 — usuário | Repõe gôndolas, responde "quanto custa?" ao cliente, confere etiquetas, controla validade | Rapidez, uma mão livre, poucas digitações, não errar |
| Encarregado / gerente de loja | 2 — gerencial | Define e altera preços, faz promoções, cobra etiquetas e inventário | Segurança na alteração, visão das pendências, rastreabilidade |
| Administrador | 3 — administrador | Configura servidor e aparelhos, dá suporte | Diagnóstico simples, recuperação sem reinstalar |
| Operador do computador da loja | — (não usa o app) | Imprime etiquetas e relatórios no PC onde roda a API | Abrir uma página e imprimir, sem instalar nada |

Condições de uso que orientam as decisões de experiência:

- uso em pé, andando, muitas vezes com uma só mão e com luvas (câmara fria, açougue);
- ambiente barulhento e com iluminação variável;
- Wi-Fi com pontos fracos no fundo da loja e no depósito;
- aparelhos compartilhados entre turnos;
- interrupções frequentes (clientes, telefone, caixa chamando).

Enquanto a decisão D-03 não for tomada, **os perfis 1, 2 e 3 têm o mesmo acesso**, conforme o requisito atual.

## 3. Regras gerais

| ID | Regra |
|---|---|
| RG-01 | A única escrita no legado continua sendo `produtos.PRECO_UNITARIO`, pelo `ID`. Nenhum módulo grava estoque, custo, cadastro ou usuários. |
| RG-02 | **Toda** alteração de preço — feita na tela de alteração, na conferência, na calculadora de promoções, na validade ou ao desfazer — passa pela mesma revisão e confirmação (F1-01) e pela mesma proteção contra sobrescrita (F0-05). |
| RG-03 | Dinheiro em centavos inteiros no app e em `BigDecimal` com escala 2 na API. Arredondamento comercial (meio para cima) ao centavo. Nunca `double` ou `number` com casas decimais em cálculos. |
| RG-04 | Toda decisão usa o preço **atual do servidor**. Preços guardados em listas do aparelho servem só para exibição e trazem data e hora. |
| RG-05 | Sem cache offline de produtos. Sem conexão, as listas locais podem ser vistas e editadas, mas consultas, envios e alterações ficam indisponíveis, com explicação. |
| RG-06 | Toda tela tem os estados "carregando", "conteúdo", "vazio", "erro com ação" e "sem conexão". |
| RG-07 | Toda mensagem de erro diz **o que aconteceu** e **o que fazer**, com um botão para a ação sugerida. Nunca exibe código HTTP, SQL ou termos técnicos. |
| RG-08 | Ações de impacto (gravar preço, limpar listas, encerrar contagens) pedem confirmação. Remoções simples oferecem "Desfazer" por alguns segundos. |
| RG-09 | Toda tentativa de gravação registra operador, origem, data e hora e resultado (F0-02). |
| RG-10 | Listas e registros locais guardam "adicionado por" (operador da sessão) e "adicionado em". |
| RG-11 | Acessibilidade: alvos de toque com pelo menos 48 × 48 px, contraste WCAG AA, foco visível, resultados anunciados (`aria-live`), sem depender só de cor. |
| RG-12 | Formatos: moeda `R$ 1.234,56`; quantidades com vírgula decimal (`18,5 kg`); datas `dd/mm/aaaa`; horas `HH:MM`; textos em pt-BR. |
| RG-13 | Ações indisponíveis por perfil, conexão ou estado aparecem desabilitadas **com o motivo** ("Disponível para o perfil gerencial", "Sem conexão com o servidor"), nunca simplesmente somem sem explicação. |
| RG-14 | O modo atual é sempre visível: **consulta** (somente leitura, cor primária) e **alteração** (escrita, destaque laranja e rótulo "MODO ALTERAÇÃO"). |

## 4. Mapa de telas

| Tela | Rota | Situação | Itens |
|---|---|---|---|
| Configuração do servidor | `/configuracao-servidor` (`?modo=recuperacao`) | Alterada | F0-09, F3-08 |
| Atualize o aplicativo | `/atualizar-app` | Nova | F0-08 |
| Login | `/login` | Alterada | F0-08, F0-09, F0-10, F1-07 |
| Bloqueio por inatividade | sobreposição global | Nova | F1-07 |
| Reautenticação | painel inferior global | Nova | F0-01 |
| Home | `/home` | Alterada | F1-06, F1-07, F1-08, F2-*, F3-* |
| Scanner | `/scanner/:modo` | Alterada | F0-12, F1-04, F3-01 |
| Busca por nome | `/busca-produto?destino=…` | Nova | F3-01 |
| Consulta de estoque | `/consulta-estoque/:codigo` (`?id=`) | Alterada | F0-06, F1-03, F1-05, F3-02, F3-06 |
| Alteração de preço | `/produto/:codigo` (`?id=&origem=&preco=`) | Alterada | F0-05, F1-01, F1-02, F3-06 |
| Sucesso | `/sucesso` | Alterada | F1-01, F1-03, F2-01 |
| Etiquetas | `/etiquetas` | Nova | F2-01 |
| Conferência de gôndola | `/conferencia`, `/conferencia/resumo/:id` | Nova | F2-02 |
| Promoções | `/promocoes` | Nova | F2-03 |
| Inventário | `/inventario`, `/inventario/:id`, `/inventario/:id/relatorio` | Nova | F2-04 |
| Validade | `/validade`, `/validade/novo` | Nova | F3-03 |
| Reposição | `/reposicao` | Nova | F3-04 |
| Pendências de cadastro | `/pendencias-cadastro` | Nova | F3-05 |
| Ajustes | `/ajustes` | Alterada | F0-08, F1-04, F1-07 |
| Etiquetas (PC da loja) | `http://SERVIDOR:8080/etiquetas/` | Nova (no navegador do PC) | F2-01 |
| Pareamento (PC da loja) | `http://localhost:8080/pareamento/` | Nova (no navegador do próprio computador do servidor) | F3-08 |

## 5. Catálogo de mensagens padronizadas

O app escolhe a mensagem pelo **código** devolvido pela API e só recorre ao status HTTP quando não há código.

| Situação | Código | Mensagem | Ação oferecida |
|---|---|---|---|
| Sem rede ou servidor inacessível | — | "Não foi possível falar com o servidor da loja. Confira o Wi-Fi e se o computador do servidor está ligado." | Tentar novamente |
| Demora excessiva | — | "O servidor demorou para responder. Confira o Wi-Fi e tente novamente." | Tentar novamente |
| Banco indisponível | `BANCO_INDISPONIVEL` | "O sistema da loja não está respondendo agora. Tente novamente em instantes." | Tentar novamente |
| Credenciais inválidas | `CREDENCIAIS_INVALIDAS` | "Usuário ou senha incorretos." | — |
| Senha do caixa inválida | `SENHA_CAIXA_INVALIDA` | "Senha do caixa incorreta." | — |
| Muitas tentativas | `MUITAS_TENTATIVAS` | "Muitas tentativas seguidas. Aguarde {n} minutos e tente de novo." | — |
| Sessão ausente ou expirada | `SESSAO_AUSENTE`, `SESSAO_EXPIRADA` | "Sua sessão terminou. Digite sua senha para continuar de onde parou." | Painel de reautenticação |
| Produto não encontrado | `PRODUTO_NAO_ENCONTRADO` | "Não encontramos produto com o código {codigo}." | Buscar por nome · Registrar pendência · Escanear outro |
| Código em mais de um produto | `CODIGO_DUPLICADO` | "Este código está cadastrado em mais de um produto. Escolha o produto certo ou peça a correção no sistema principal." | Lista para escolha (consulta) · Registrar pendência |
| Preço mudou durante a edição | `PRECO_ALTERADO` | "O preço deste produto mudou para {preco} enquanto você editava." | Revisar com o preço novo · Cancelar |
| Confirmação necessária | `CONFIRMACAO_NECESSARIA` | (reabre a revisão com os alertas) | Confirmar · Corrigir |
| Dados inválidos | `DADOS_INVALIDOS` | Mensagem devolvida pela API (já escrita para o usuário) | Corrigir |
| Permissão insuficiente | `PERMISSAO_NEGADA` | "Seu perfil não permite esta ação. Peça a um encarregado." | — |
| App desatualizado | `VERSAO_APP_INCOMPATIVEL` | "Esta versão do aplicativo não é mais compatível com o servidor da loja." | Abre `/atualizar-app` |
| Conexão não validada | `CONEXAO_NAO_VALIDADA` | "O servidor respondeu, mas não conseguiu ler o sistema da loja. Avise o responsável." | Tentar novamente |
| Erro inesperado | `ERRO_INTERNO` | "Algo deu errado do nosso lado. Tente novamente; se continuar, avise o responsável." | Tentar novamente |

---

## 6. Fase 0 — Fundação

### F0-01 — Sessão autenticada para operações do app

**[L]** · Essencial · Decisões: D-02, D-08 · Corrige: A-01, BF-08, BF-09, BF-33

**Objetivo.** Somente quem fez login pode consultar e alterar dados pela API, e cada operação passa a saber **quem** a executou.

**Regras**

1. O login bem-sucedido devolve, além dos dados atuais, um **token de sessão** e sua data de expiração.
2. A sessão vale por um período máximo configurável (padrão: 10 horas, um turno — D-08). Expirada, o servidor responde `SESSAO_EXPIRADA`.
3. Exigem sessão todas as rotas `/api/**`, **exceto**: login, teste de conexão, validação da senha do caixa, versão (F0-08) e as rotas públicas da página de etiquetas no PC (F2-01).
4. "Sair" encerra a sessão também no servidor.
5. Se o servidor reiniciar, as sessões terminam e o app pede a senha novamente, **sem perder a tela e o que estava sendo feito** (painel de reautenticação).
6. **Transição** (parâmetro `APP_SEGURANCA_EXIGIR_TOKEN`):
   - **desligado** (padrão inicial): requisições **sem** token continuam aceitas para não quebrar APKs antigos e são auditadas como "operador não identificado";
   - requisições **com** token inválido ou expirado recebem `SESSAO_EXPIRADA` mesmo com o parâmetro desligado;
   - **ligado**: requisições sem token recebem `SESSAO_AUSENTE`.
7. O token nunca é registrado em log, nunca aparece na tela e não sobrevive ao fechamento completo do app.

**Fluxo de reautenticação**

1. O operador está em qualquer tela e executa uma ação, por exemplo "Confirmar alteração".
2. A API responde `SESSAO_EXPIRADA`.
3. Abre-se o painel "Sua sessão terminou", com o nome do operador preenchido e bloqueado, o campo de senha e os botões "Continuar" e "Entrar com outro usuário".
4. Com a senha correta, o app obtém uma nova sessão e **repete automaticamente** a ação interrompida, uma única vez.
5. "Entrar com outro usuário" encerra a sessão e leva ao login. Listas locais são preservadas.

**Critérios de aceite**

```gherkin
Funcionalidade: Sessão autenticada

  Cenário: Alteração sem sessão com exigência ligada
    Dado que o parâmetro de exigência de token está ligado
    Quando uma requisição de alteração de preço chega sem token
    Então a API responde 401 com o código "SESSAO_AUSENTE"
    E o preço não é alterado

  Cenário: Sessão expirada durante a alteração
    Dado que estou na revisão de alteração de preço
    E minha sessão expirou
    Quando toco em "Confirmar alteração"
    Então vejo o painel "Sua sessão terminou" com meu nome preenchido
    Quando digito minha senha corretamente
    Então a alteração é enviada novamente sem que eu redigite o preço

  Cenário: Transição com APK antigo
    Dado que o parâmetro de exigência de token está desligado
    Quando um aparelho com a versão anterior altera um preço
    Então a alteração é aceita
    E a auditoria registra "operador não identificado"

  Cenário: Sair encerra a sessão no servidor
    Dado que estou logado
    Quando toco em "Sair"
    Então o token deixa de ser aceito pela API
```

**Fora do escopo:** login único entre sistemas, hash das senhas legadas (o legado não pode ser alterado) e renovação silenciosa sem senha.

---

### F0-02 — Auditoria de alterações e acessos sensíveis

**[L]** · Essencial · Decisões: D-08 (retenção) · Corrige: A-02, BF-34

**Objetivo.** Saber quem alterou qual preço, quando, de quanto para quanto, por qual tela e com qual resultado — sem tocar no banco legado.

**Regras**

1. Eventos registrados:
   - toda tentativa de alteração de preço, com sucesso, conflito, falta de confirmação, produto inexistente, banco indisponível ou permissão negada;
   - validação da senha do caixa (aceita, negada ou bloqueada);
   - bloqueios por excesso de tentativas de login.
2. Campos de cada evento: data e hora (fuso de São Paulo), tipo, resultado, operador (código, nome e perfil, ou "não identificado"), produto (ID, código e descrição), preço anterior, preço novo, variação percentual, origem (`ALTERACAO`, `CONFERENCIA`, `PROMOCAO`, `PROMOCAO_FIM`, `VALIDADE`, `DESFAZER`), alertas confirmados, IP do aparelho e versão do app.
3. **Nunca** registrar senha, senha do caixa ou token.
4. Os eventos ficam em arquivos diários no servidor, separados do log técnico, com retenção configurável (padrão de 365 dias — D-08).
5. A auditoria cobre apenas operações feitas **pelo app**. Alterações feitas direto no sistema VB.NET não aparecem.

**Critérios de aceite**

```gherkin
Funcionalidade: Auditoria

  Cenário: Registro de alteração bem-sucedida
    Dado que o operador "Carlos" (código 7, perfil 2) está logado
    Quando ele altera o preço do produto 15 de R$ 9,50 para R$ 10,50 pela tela de alteração
    Então o arquivo de auditoria do dia contém um evento com operador 7, produto 15,
      preço anterior 9,50, preço novo 10,50, variação de 10,53% e origem "ALTERACAO"
    E o evento não contém senha nem token

  Cenário: Registro de conflito
    Quando uma alteração é recusada porque o preço mudou durante a edição
    Então a auditoria registra o resultado "CONFLITO" e o preço encontrado no banco
```

---

### F0-03 — Proteção contra tentativas repetidas

**[L]** · Essencial · Decisões: D-08, D-09 · Corrige: A-03, A-04, BF-12

**Regras**

1. **Login:**
   - após 5 falhas para o mesmo nome a partir do mesmo aparelho em 15 minutos, novas tentativas desse par ficam bloqueadas por 15 minutos;
   - limite adicional de 20 falhas por aparelho no mesmo período, para qualquer nome.
2. **Senha do caixa:** após 5 falhas do mesmo aparelho em 15 minutos, bloqueio de 15 minutos.
3. Durante o bloqueio, a API responde `MUITAS_TENTATIVAS` com o tempo restante, e o app mostra a mensagem com os minutos e desabilita o botão até lá.
4. Um sucesso zera o contador da chave correspondente.
5. Os contadores ficam em memória; reiniciar a API os zera (aceitável).
6. **D-09 (a decidir):** comparar a senha de forma exata — maiúsculas, minúsculas e espaços — na API, em vez de depender da collation do banco. Muda o comportamento para quem hoje entra com variações e por isso exige aprovação.

**Critérios de aceite**

```gherkin
  Cenário: Bloqueio após tentativas
    Dado que errei a senha 5 vezes nos últimos 15 minutos neste aparelho
    Quando tento entrar novamente
    Então vejo "Muitas tentativas seguidas. Aguarde 15 minutos e tente de novo."
    E o botão "Entrar" fica desabilitado até o fim do bloqueio
```

---

### F0-04 — Teste de conexão sem depender de um usuário específico

**[L]** · Essencial · Corrige: A-06, BF-03

**Regras**

1. O teste de conexão confirma que a API consegue **ler** as tabelas que usa (`produtos`, `produtos_similares`, `usuarios`, `senha`) com as colunas mapeadas, sem depender de nenhum registro específico.
2. Tabela vazia não é falha. Erro de leitura ou coluna inexistente é falha (`CONEXAO_NAO_VALIDADA`).
3. O contrato não muda: 204 quando validado e 503 quando não.
4. Ganho adicional: se uma coluna mapeada não existir no banco real, a falha aparece já na configuração, e não no meio do uso.

**Critério de aceite**

```gherkin
  Cenário: Usuário de referência removido
    Dado que não existe o usuário "bruno" no banco
    E as tabelas podem ser lidas
    Quando testo a conexão na configuração do servidor
    Então vejo "Conexão estabelecida com sucesso!"
```

---

### F0-05 — Proteção contra sobrescrita de preço

**[L]** · Essencial · Corrige: A-08, BF-31, BF-32

**Objetivo.** Não apagar, sem aviso, uma alteração feita no PDV, na retaguarda ou em outro aparelho entre a consulta e a confirmação.

**Regras**

1. O app envia, junto com o novo preço, o **preço que estava exibindo** (preço anterior).
2. A API altera somente se o preço atual no banco for igual ao preço anterior enviado. A gravação continua sendo só de `PRECO_UNITARIO`, pelo `ID`.
3. Se o preço atual já for **igual ao novo preço**, a operação é tratada como concluída, sem nova escrita. Isso cobre o caso em que a resposta se perdeu e o operador repetiu a ação.
4. Se for diferente dos dois, a API responde `PRECO_ALTERADO` com o preço atual.
5. Diante de um conflito, o app mostra:

   > "O preço deste produto mudou para R$ 12,49 enquanto você editava."

   com os botões "Revisar com o preço novo" (recarrega o produto e mantém o valor digitado) e "Cancelar".
6. Se a resposta da alteração não chegar (timeout), o app **verifica** o preço atual antes de mostrar erro:
   - igual ao novo: mostra Sucesso, com a observação "Confirmado após nova verificação";
   - igual ao anterior: informa que a alteração não foi feita e oferece "Tentar novamente";
   - outro valor: segue o fluxo de conflito.
7. Durante a transição, o preço anterior é opcional na API, para manter compatíveis os APKs antigos.

**Critérios de aceite**

```gherkin
Funcionalidade: Proteção contra sobrescrita

  Cenário: Preço alterado no PDV durante a edição
    Dado que abri o produto com preço R$ 9,50
    E o preço foi alterado no sistema principal para R$ 9,90
    Quando confirmo o novo preço R$ 10,50
    Então vejo "O preço deste produto mudou para R$ 9,90 enquanto você editava."
    E o preço continua R$ 9,90 no banco

  Cenário: Repetição após perda de resposta
    Dado que enviei R$ 10,50 e a resposta não chegou
    E o preço no banco já é R$ 10,50
    Quando o app verifica o produto
    Então vejo a tela de Sucesso com "Confirmado após nova verificação"
    E nenhuma nova escrita é feita
```

---

### F0-06 — Respostas corretas para dados inconsistentes do cadastro

**[L]** · Essencial · Decisão: D-06 · Corrige: A-07, A-09, BF-24

**Regras**

1. **Código principal duplicado** (mais de um produto com o mesmo `CODIGO_PRODUTO`) e **similar ambíguo** (um código similar ligado a mais de um principal) deixam de gerar "servidor indisponível" e passam a responder `CODIGO_DUPLICADO`, com a lista de produtos envolvidos (ID, código, descrição e preço).
2. **Consulta:** o app mostra a lista ("Este código está em mais de um produto") e permite abrir qualquer um pelo ID.
3. **Alteração de preço:** a proposta padrão (D-06) é **bloquear** e orientar: "Peça a correção do cadastro no sistema principal", com o atalho "Registrar pendência" (F3-05). O PDV pode escolher qualquer um dos produtos na venda, e alterar só um deles não resolve a divergência.
4. ID fora do intervalo aceito pelo banco é tratado como "produto não encontrado", em vez de erro interno.
5. Cada ocorrência de duplicidade gera um aviso no log técnico com o código (sem dados sensíveis), para o administrador corrigir no legado.

**Critérios de aceite**

```gherkin
  Cenário: Consulta de código duplicado
    Dado que o código 7891000100103 está em dois produtos
    Quando consulto esse código
    Então vejo "Este código está cadastrado em mais de um produto…"
    E vejo os dois produtos com descrição e preço para escolher

  Cenário: Alteração de código duplicado
    Dado que o código 7891000100103 está em dois produtos
    Quando tento alterar o preço por esse código
    Então a alteração não é oferecida
    E vejo a opção "Registrar pendência"
```

---

### F0-07 — Exposição mínima em produção

**[L]** · Essencial · Corrige: A-01, BF-63

**Regras**

1. No perfil de produção (`prod`), a documentação interativa (Swagger UI e `/v3/api-docs`) fica **desativada**.
2. O servidor da loja roda com o perfil `prod`, documentado em `env.example` e no guia de implantação.
3. O Actuator continua expondo só `health` e `info`, sem detalhes.
4. O README passa a dizer que o Swagger existe apenas em desenvolvimento.

---

### F0-08 — Compatibilidade entre app e servidor

**[L]** · Essencial · Corrige: A-28, BF-13

**Regras**

1. O app informa sua versão em toda requisição.
2. O servidor publica sua versão, a **versão mínima do app** aceita e, se configurado, o **nome da loja**.
3. Ao abrir o app (e após configurar o servidor):
   - **app abaixo da versão mínima:** tela bloqueante "Atualize o aplicativo" com orientação ("Peça ao responsável a versão mais recente do aplicativo da loja") e os botões "Tentar novamente" e "Configuração do servidor";
   - **servidor mais antigo do que o app espera:** faixa "O servidor da loja está desatualizado. Algumas funções ficam indisponíveis." Os módulos que dependem de recursos novos ficam desabilitados com o motivo.
4. A versão exibida no login e em Ajustes > Sobre vem de uma **fonte única** (não fica escrita no HTML).
5. Ajustes > Sobre mostra: versão do app, versão do servidor, nome da loja, IP do servidor, último teste de conexão e situação do leitor de código.

**Critério de aceite**

```gherkin
  Cenário: App abaixo da versão mínima
    Dado que o servidor exige no mínimo a versão 1.1.0
    E meu app está na versão 1.0.0
    Quando abro o app
    Então vejo a tela "Atualize o aplicativo"
    E não consigo acessar o login
```

Limitação: APKs anteriores a esta mudança não conhecem a verificação. A troca deles depende da sequência de implantação descrita no [plano mestre](00-master.md#7-ondas-de-execução).

---

### F0-09 — Recuperar o servidor a partir do login

**[L]** · Essencial · Corrige: A-10, BF-01, BF-02, BF-06

**Objetivo.** Acabar com o beco sem saída quando o IP salvo deixa de responder, mantendo a proteção pela senha do caixa.

**Regras**

1. Ao abrir o login, o app verifica o servidor salvo em segundo plano e mostra o status ("Servidor online", "Verificando…" ou "Servidor indisponível").
2. Com o servidor indisponível (na verificação ou após um login que falhou por rede), aparece o cartão:

   > **Não conseguimos falar com o servidor da loja**
   > Servidor salvo: 192.168.0.10.
   > [Tentar novamente]  [Alterar servidor]
3. "Alterar servidor" abre a configuração em **modo recuperação**:
   1. informar o novo IP (ou ler o QR do servidor — F3-08);
   2. testar a conexão;
   3. o app mostra a identificação do servidor encontrado — "Servidor encontrado: {nome da loja} · versão {x}" — e pede a confirmação "É o servidor da sua loja?";
   4. informar a **senha do caixa**, validada **pelo novo servidor**;
   5. salvar e voltar ao login com o status online.
4. No modo recuperação, nada é salvo antes de concluir os passos 2 a 4. "Cancelar" mantém o IP anterior.
5. O modo recuperação só é acessível **sem sessão ativa**. Com sessão, a alteração continua por Ajustes.
6. Se o nome da loja do novo servidor for diferente do servidor anterior (quando ambos estiverem configurados), exibir o aviso "Este servidor é de outra loja ({nome}). Confirme com o responsável antes de continuar."

**Critérios de aceite**

```gherkin
Funcionalidade: Recuperação do servidor

  Cenário: IP do servidor mudou
    Dado que o app tem o IP 192.168.0.10 salvo
    E o servidor agora responde em 192.168.0.20
    Quando abro o app
    Então vejo "Não conseguimos falar com o servidor da loja"
    Quando toco em "Alterar servidor", informo 192.168.0.20 e testo
    Então vejo "Servidor encontrado: Mercado Central · versão 1.1.0"
    Quando informo a senha do caixa correta e salvo
    Então volto ao login com o status "Servidor online"

  Cenário: Senha do caixa errada na recuperação
    Quando informo uma senha do caixa incorreta no modo recuperação
    Então vejo "Senha do caixa incorreta."
    E o IP salvo continua sendo 192.168.0.10
```

---

### F0-10 — Erros, tempos de espera e mensagens no app

**[L]** · Essencial · Corrige: A-13, A-14, BF-07, BF-28

**Regras**

1. Toda chamada à API tem tempo máximo de espera:
   - **10 s** no padrão;
   - **4 s** na verificação de status;
   - **7 s** no teste de conexão.
2. As mensagens seguem o [catálogo](#5-catálogo-de-mensagens-padronizadas), escolhidas pelo código devolvido pela API e não apenas pelo status HTTP.
3. Em Ajustes, a falha da senha do caixa só diz "Senha do caixa incorreta." quando for de fato essa a causa. Rede, bloqueio e indisponibilidade têm mensagens próprias.
4. Erros de carregamento mostram o estado de erro da tela com o botão da ação sugerida. Erros de envio mantêm o que foi digitado.

**Critério de aceite**

```gherkin
  Cenário: Senha do caixa com servidor fora do ar
    Dado que o servidor está desligado
    Quando informo a senha do caixa em Ajustes
    Então vejo "Não foi possível falar com o servidor da loja…"
    E não vejo "Senha do caixa incorreta."
```

---

### F0-11 — Botão voltar do Android e pilha de navegação

**[L]** · Essencial · Decisão: D-07 (`@capacitor/app`) · Corrige: A-11, A-21, BF-14, BF-15, BF-17, BF-40

**Comportamento esperado do botão voltar**

| Contexto | Comportamento |
|---|---|
| Painel ou modal aberto | Fecha o painel |
| Enviando dados (spinner de gravação) | Ignora e mostra "Aguarde a conclusão" |
| Home | 1º toque: "Toque novamente para sair". 2º toque em até 2 s: minimiza o app, **sem** encerrar a sessão |
| Login e primeira configuração | Minimiza o app |
| Scanner | Volta à Home |
| Consulta ou alteração | Volta ao scanner do mesmo modo ou à lista de origem |
| Alteração com preço digitado e não salvo | Pergunta "Descartar o novo preço digitado?" |
| Sucesso | Volta à Home; nunca reabre a tela de edição |
| Telas de módulo (etiquetas, conferência, inventário…) | Volta à tela anterior; sessões e listas continuam salvas |
| Bloqueio por inatividade | Minimiza o app |

**Regras adicionais**

1. Ações "Cancelar", "Consultar outro" e "Escanear próximo" substituem a tela atual, em vez de empilhar.
2. Ao sair da tela de Sucesso, o resumo é descartado.
3. A tela de Sucesso mostra a hora da alteração.

---

### F0-12 — Leitor de código robusto

**[L]** · Essencial · Corrige: A-12, BF-05, BF-18, BF-19

**Regras**

1. Antes da primeira leitura, o app verifica se o leitor do Google está disponível no aparelho.
2. Se não estiver, mostra "Preparando o leitor de código…" com progresso, iniciando a instalação. A mensagem explica que **é preciso internet uma única vez**.
3. Se a instalação falhar, mostra:

   > "Não foi possível preparar o leitor. Conecte o aparelho a uma rede com internet uma vez, ou digite o código."

   com os botões "Tentar novamente" e "Digitar código".
4. O leitor do Google **não exige permissão de câmera** no app; o pedido de permissão é removido. Se uma leitura futura exigir a permissão e ela for negada, o app oferece "Abrir configurações do aparelho".
5. Cancelar a leitura (fechar a câmera) não mostra erro.
6. Código lido com letras: "Este código não é um código de produto. Tente ler o código de barras numérico da embalagem."
7. Leitura bem-sucedida: vibração curta, quando o aparelho suportar.
8. "Digitar código" está sempre disponível, inclusive durante a preparação do leitor.

**Critérios de aceite**

```gherkin
  Cenário: Primeira leitura em aparelho sem o módulo
    Dado que o módulo do leitor do Google não está instalado
    E o aparelho tem acesso à internet
    Quando toco em "Escanear produto"
    Então vejo "Preparando o leitor de código…" com progresso
    E, ao concluir, a câmera abre

  Cenário: Leitura sem pedido de permissão
    Dado que nunca concedi permissão de câmera ao app
    Quando toco em "Escanear produto"
    Então o leitor abre sem solicitar a permissão de câmera do app
```

---

### F0-13 — Ajustes de engenharia e operação

**[L]** · Importante · Decisão: D-10 · Corrige: A-24 a A-27, BF-62

1. Launchers do `setup` passam a usar o Maven Wrapper (`mvnw.cmd`).
2. Triagem das vulnerabilidades do `npm audit`: separar dependências de execução e de desenvolvimento, atualizar dentro das linhas de versão atuais e registrar os riscos aceitos.
3. Padronizar a orientação de Java nos documentos: 17 como mínimo e 21 como recomendado.
4. Migrar os testes do mobile para Vitest, quando decidido (D-10).
5. Atualizar o guia de implantação:
   - IP fixo ou reserva DHCP para o servidor;
   - servidor configurado como serviço do Windows, sem hibernação;
   - Wi-Fi operacional separado do Wi-Fi de clientes;
   - perfil `prod`;
   - preparação dos aparelhos (módulo do leitor e permissões).

---

## 7. Fase 1 — Fluxos atuais mais rápidos e seguros

### F1-01 — Alteração de preço assistida, com revisão e "Desfazer"

**[L]** (custo e margem dependem de D-03) · Essencial · Depende de: F0-05 · Corrige: A-15, BF-29, BF-30, BF-35

**Objetivo.** Impedir erros de digitação e vendas abaixo do custo sem tornar o fluxo lento.

**Regras**

1. A tela mostra o produto e o **preço atual**. Se D-03 permitir ao perfil do operador, mostra também o **custo** e a **margem atual**. Margem = (preço de venda − custo) ÷ preço de venda.
2. Enquanto o operador digita, aparecem a variação (R$ e %) e, se permitido, a nova margem.
3. O botão principal passa a ser **"Revisar alteração"**. Ele fica desabilitado enquanto o valor for inválido ou igual ao atual. Valor igual mostra: "O novo preço é igual ao atual."
4. A revisão abre um painel com:
   - **antes → depois** em destaque;
   - variação em R$ e %;
   - nova margem (se permitido);
   - alertas;
   - botões "Confirmar alteração" e "Corrigir valor".
5. Alertas calculados pela API:

   | Alerta | Condição | Apresentação | Exigência |
   |---|---|---|---|
   | `ABAIXO_DO_CUSTO` | Novo preço < custo (custo conhecido) | Vermelho: "O novo preço está abaixo do custo (R$ X)." | Marcar "Confirmo a venda abaixo do custo" |
   | `VARIACAO_ALTA` | \|variação\| > limite (padrão 30% — D-08) | Laranja: "Variação de +250% em relação ao preço atual. Confira se digitou corretamente." | Marcar "Conferi o valor" |
6. A API **recusa** (`CONFIRMACAO_NECESSARIA`) uma alteração que tenha alertas não confirmados. A regra vale para qualquer cliente, não só para o app.
7. Toda alteração informa a **origem** (alteração, conferência, promoção, validade, desfazer) para a auditoria.
8. **Desfazer:** a tela de Sucesso oferece "Desfazer alteração" por 2 minutos. Desfazer é uma nova alteração do preço novo para o anterior, com origem `DESFAZER`, passando por F0-05; se alguém tiver alterado o preço nesse meio-tempo, segue o fluxo de conflito. Desfazer também remove da fila de etiquetas o item criado pela alteração, se ainda não tiver sido enviado.
9. A tela de alteração exibe a origem quando ela não for a tela de alteração (ex.: "Alteração a partir da conferência de gôndola").

**Fluxo principal**

1. O operador abre o produto, que mostra o preço atual R$ 9,50.
2. Digita `1050` e o campo mostra "10,50". Abaixo aparece "+R$ 1,00 (+10,5%)".
3. Toca em "Revisar alteração" e vê o painel "R$ 9,50 → R$ 10,50", sem alertas.
4. Toca em "Confirmar alteração" e chega à tela de Sucesso.

**Fluxo alternativo — erro de digitação**

1. O operador digita `10500` e o campo mostra "105,00".
2. A revisão mostra o alerta "Variação de +1.005,3%…".
3. Ele toca em "Corrigir valor" e volta ao campo, com o valor selecionado.

**Critérios de aceite**

```gherkin
Funcionalidade: Alteração assistida

  Cenário: Variação acima do limite exige confirmação
    Dado que o preço atual é R$ 9,50 e o limite de variação é 30%
    Quando digito R$ 105,00 e toco em "Revisar alteração"
    Então vejo o alerta "Variação de +1.005,3% em relação ao preço atual…"
    E o botão "Confirmar alteração" só habilita após marcar "Conferi o valor"

  Cenário: API recusa alerta não confirmado
    Quando um cliente envia uma alteração com variação de 50% sem confirmar o alerta
    Então a API responde 422 com o código "CONFIRMACAO_NECESSARIA" e o alerta "VARIACAO_ALTA"

  Cenário: Desfazer dentro do prazo
    Dado que alterei o preço de R$ 9,50 para R$ 95,00 há 30 segundos
    Quando toco em "Desfazer alteração"
    Então o preço volta a R$ 9,50
    E a auditoria registra a origem "DESFAZER"
    E o item correspondente sai da fila de etiquetas
```

---

### F1-02 — Atalhos de remarcação

**[L]** · Importante · Depende de: F1-01

**Regras**

1. Abaixo do campo de preço aparecem os botões **−10%**, **−5%**, **+5%**, **+10%**, **Final ,99** e **Final ,90**.
2. Os percentuais são aplicados **sempre sobre o preço atual** (exibido como "Base: R$ 9,50"), nunca sobre o valor já digitado, para não acumular.
3. "Final ,99" e "Final ,90" mantêm os reais do valor no campo e trocam os centavos (12,35 → 12,99 ou 12,90). Com o campo vazio, partem do preço atual.
4. Cálculo em centavos inteiros, com arredondamento comercial. Resultados ≤ R$ 0,00 não são aplicados ("O resultado precisa ser maior que zero").
5. O resultado preenche o campo, que continua editável. A revisão (F1-01) continua obrigatória.

**Critérios de aceite**

```gherkin
  Cenário: Percentual sobre o preço atual
    Dado que o preço atual é R$ 9,99
    Quando toco em "+10%"
    Então o campo mostra "10,99"

  Cenário: Percentual não acumula
    Dado que o preço atual é R$ 10,00
    Quando toco em "+10%" e depois em "+10%"
    Então o campo mostra "11,00"

  Cenário: Final ,99
    Dado que o campo mostra "12,35"
    Quando toco em "Final ,99"
    Então o campo mostra "12,99"
```

---

### F1-03 — Fluxo contínuo entre consulta, alteração e próximo produto

**[L]** · Importante · Corrige: A-17, A-22, BF-25, BF-39

**Regras**

1. **Consulta de estoque:**
   - "Escanear próximo" abre a câmera **direto**, sem passar pela tela do scanner;
   - "Alterar preço" abre a alteração do mesmo produto, sem nova leitura (respeitando D-03).
2. **Sucesso:**
   - "Escanear próximo" abre a câmera direto, no modo alteração;
   - mostra "Etiqueta adicionada à fila (1 pendente)" com o atalho "Ver etiquetas" (F2-01).
3. O texto da tela de Sucesso passa a ser "Preço alterado no sistema da loja. **Troque a etiqueta da gôndola.**" até que a homologação confirme que o PDV usa o preço sem cache. Se houver cache, acrescentar: "O caixa passa a cobrar o novo preço após {orientação validada}".
4. Cancelar a câmera em "Escanear próximo" volta à tela de onde o operador saiu.

**Critério de aceite**

```gherkin
  Cenário: Da consulta para a alteração sem reescanear
    Dado que consultei o produto 7891000100103
    Quando toco em "Alterar preço"
    Então vejo a tela de alteração desse produto com o preço atual carregado
```

---

### F1-04 — Leitura com coletor (leitor físico)

**[L]** · Importante (se a loja usar coletores) · Corrige: BF-23

**Regras**

1. Ajustes > Leitura tem o interruptor "Usar leitor físico (coletor)", gravado por aparelho.
2. Ligado, as telas de leitura mostram "Pronto para leitura pelo gatilho do coletor" e capturam o código "digitado" pelo leitor, terminado em Enter, sem abrir o teclado virtual.
3. O código passa pela mesma validação e pelo mesmo fluxo da câmera. A câmera continua disponível.
4. Se o campo de captura perder o foco (outro campo, painel aberto), a leitura fica pausada e a tela mostra "Toque aqui para voltar a ler".

**Critério de aceite**

```gherkin
  Cenário: Leitura pelo gatilho
    Dado que o modo coletor está ligado
    E estou no scanner do modo consulta
    Quando o coletor envia "7891000100103" seguido de Enter
    Então a consulta desse produto é aberta
```

---

### F1-05 — Consulta enriquecida

**[L]** · Importante · Corrige: A-20, BF-27

**Regras**

1. Mostrar "Outros códigos deste produto", com os códigos similares.
2. Se o produto foi encontrado por código similar, informar "Encontrado pelo código similar {código lido}".
3. Estoque em formato brasileiro:
   - **UN** e unidades inteiras: sem casas decimais (`18 UN`);
   - **KG** e unidades fracionadas: até 3 casas, sem zeros à direita (`18,5 kg`).
4. Indicadores:

   | Situação | Indicador | Orientação |
   |---|---|---|
   | Estoque = 0 | "Estoque zerado" (amarelo) | "Se há produto na gôndola, avise o responsável para conferir as entradas." |
   | Estoque < 0 | "Estoque negativo" (vermelho) | "Há vendas sem entrada registrada. Avise o responsável." |
   | NCM ausente | "Sem NCM" (cinza) | "Cadastro fiscal incompleto." |
5. Rodapé: "Consultado às HH:MM".

---

### F1-06 — Produtos recentes

**[L]** · Desejável

**Regras**

1. A Home mostra "Recentes": os últimos 8 produtos consultados ou alterados **pelo operador da sessão neste aparelho**, com descrição, código, ícone do tipo (consulta ou alteração) e hora.
2. Tocar em um item reabre o fluxo correspondente, **sempre buscando os dados atuais** (não é cache).
3. Os recentes não guardam preço.
4. "Limpar recentes" fica em Ajustes.

---

### F1-07 — Aparelho compartilhado

**[L]** · Essencial · Decisão: D-08 · Corrige: A-18, BF-08, BF-11

**Regras**

1. Após **10 minutos** sem interação (D-08) — incluindo o tempo com o app em segundo plano —, aparece o bloqueio:

   > **Sessão bloqueada por inatividade**
   > Carlos · Gerencial
   > [senha] [Desbloquear]
   > Entrar com outro usuário

2. O mesmo operador desbloqueia e volta exatamente à tela em que estava.
3. Outro operador entra pelo login, chega à Home, e a sessão anterior é encerrada no servidor.
4. O cabeçalho da Home mostra o operador ("Carlos · Gerencial") com o menu "Trocar operador" e "Sair".
5. No login, com nome lembrado: "Entrar como Carlos" e o link "Não é você?", que limpa o campo e desmarca "Lembrar usuário".
6. Durante uma gravação em andamento, o bloqueio aguarda a conclusão.

**Critério de aceite**

```gherkin
  Cenário: Bloqueio preserva a tela
    Dado que estou no meio de uma contagem de inventário
    E fiquei 10 minutos sem tocar no aparelho
    Então vejo "Sessão bloqueada por inatividade"
    Quando digito minha senha
    Então volto à contagem com todos os itens já contados
```

---

### F1-08 — Status do servidor sempre atual

**[L]** · Importante · Corrige: A-19

**Regras**

1. O status é verificado novamente quando:
   - o app volta ao primeiro plano;
   - a rede do aparelho volta;
   - o operador abre uma tela de leitura;
   - a cada 60 s com a Home visível.
2. Sem conexão, uma faixa discreta aparece em todas as telas: "Sem conexão com o servidor da loja · Tentar novamente". As ações que dependem do servidor ficam desabilitadas com o motivo; listas locais continuam utilizáveis.
3. A Home mostra "Verificado às HH:MM".

---

### F1-09 — Diálogos acessíveis

**[L]** · Desejável · Corrige: A-23, BF-16

**Regras**

1. Todos os painéis e modais (digitar código, senha do caixa, revisão, reautenticação, quantidades):
   - colocam o foco no primeiro campo;
   - prendem o foco enquanto abertos;
   - fecham com o botão voltar e com o gesto de arrastar;
   - anunciam o título;
   - enviam com Enter.
2. Ao fechar, o foco volta ao elemento que abriu o painel.

---

## 8. Fase 2 — Módulos já previstos na Home

### F2-01 — Etiquetas: fila e impressão

**[L]** para a fila no aparelho · **[P]** para os lotes no servidor · Essencial · Decisões: D-01, D-05 · Corrige: A-16, BF-38, BF-41 a BF-45

**Objetivo.** Garantir que **toda alteração de preço vire etiqueta trocada na gôndola**, sem planilhas nem anotações.

**Regras da fila (no aparelho)**

1. Cada item guarda:
   - produto (ID, código, descrição e unidade);
   - quantidade de etiquetas (1 a 50, padrão 1);
   - origem (`ALTERACAO_PRECO`, `CONFERENCIA`, `MANUAL`, `PROMOCAO`, `VALIDADE`);
   - quem adicionou e quando;
   - preço na inclusão, só para exibição.
2. **Inclusão automática** após toda alteração de preço bem-sucedida e após divergências na conferência (F2-02).
3. **Um item por produto:** nova inclusão do mesmo produto atualiza origem e data e mantém a quantidade, com o aviso "Já estava na fila: atualizado".
4. **Inclusão manual** por leitura de código, busca por nome ou coletor.
5. Editar a quantidade; remover com "Desfazer" (5 s); "Limpar fila" com confirmação.
6. A fila é **do aparelho**, não do operador: todos os operadores daquele aparelho veem a mesma fila.
7. Quantidade de pendentes visível no cartão da Home e na aba "Etiquetas" da barra inferior.

**Regras do envio para impressão**

1. "Enviar para impressão" exige conexão (e permissão, se D-03 restringir).
2. O servidor cria um **lote** com os **preços atuais do banco**, nunca os do aparelho, e devolve um código curto de 6 caracteres (ex.: `K7P2QX`) e a lista final.
3. Se algum preço mudou desde a inclusão, o resultado avisa: "3 etiquetas usam um preço diferente do que estava na fila (o sistema mudou depois). Confira na lista."
4. Produto não encontrado no momento do envio é retirado do lote com aviso.
5. Depois do envio, os itens vão para a aba "Enviadas", com código do lote, data, hora e situação. "Reenviar" cria um novo lote com preços atualizados.
6. O app mostra a orientação:

   > **Lote K7P2QX pronto**
   > No computador da loja, abra **http://192.168.0.10:8080/etiquetas/** e imprima o lote K7P2QX.

**Página de etiquetas no computador da loja**

1. Lista os lotes das últimas 24 horas (D-08), com código, aparelho/operador, quantidade de etiquetas, data, hora e situação ("Novo", "Impresso").
2. Abrir um lote mostra as etiquetas no layout escolhido (D-05). Padrão: A4 com 3 × 8 etiquetas, contendo descrição (até 2 linhas), preço em destaque, unidade ("/UN", "/KG"), código e data de emissão.
3. "Imprimir" usa a impressão do navegador. "Marcar como impresso" atualiza a situação, que o app mostra na aba "Enviadas".
4. Se o mesmo produto aparecer em outro lote recente ainda não impresso: "Este produto também está no lote {código}".
5. Não exige login (o conteúdo é o mesmo da gôndola) e não oferece nenhuma operação de escrita no legado.

**Fluxo principal**

1. O gerente altera o preço e vê, no Sucesso, "Etiqueta adicionada à fila (4 pendentes)".
2. Ao fim da remarcação, abre Etiquetas, confere as quantidades e toca em "Enviar para impressão".
3. Vê o lote `K7P2QX`, vai ao PC, abre a página, imprime e marca como impresso.
4. Afixa as etiquetas (a confirmação de troca na gôndola fica para a v2).

**Critérios de aceite**

```gherkin
Funcionalidade: Etiquetas

  Cenário: Inclusão automática após alteração
    Dado que alterei o preço do produto 15
    Então a fila de etiquetas contém o produto 15 com origem "ALTERACAO_PRECO"
    E o contador da aba "Etiquetas" aumenta em 1

  Cenário: Lote usa o preço atual
    Dado que o produto 15 entrou na fila com R$ 10,50
    E depois o preço foi alterado no sistema para R$ 10,90
    Quando envio a fila para impressão
    Então o lote contém o produto 15 com R$ 10,90
    E vejo o aviso de preço diferente do que estava na fila

  Cenário: Desfazer alteração remove a etiqueta pendente
    Dado que a alteração do produto 15 adicionou um item à fila
    Quando desfaço a alteração antes de enviar
    Então o item do produto 15 sai da fila
```

**v2 (fora deste escopo):** impressora térmica Bluetooth (D-05/D-07); confirmação de "etiqueta afixada" por leitura na gôndola; etiqueta "De/Por" para promoções.

---

### F2-02 — Conferência de gôndola

**[L]** · Essencial · Depende de: F2-01 · Corrige: BF-46 a BF-48

**Objetivo.** Encontrar etiquetas erradas ou ausentes antes do cliente.

**Regras**

1. "Nova conferência", com nome opcional ("Corredor 5 — Bebidas"). Há **uma conferência ativa por aparelho**, retomada automaticamente se o app fechar.
2. **Ciclo de leitura** (otimizado para o caso comum):
   1. ler o produto;
   2. o app mostra a descrição e o **preço do sistema** em destaque;
   3. três botões grandes: **"Etiqueta confere"**, **"Etiqueta diferente"** e **"Sem etiqueta"**;
   4. "Etiqueta diferente" pede o preço da etiqueta (campo com máscara) e registra a diferença.
3. Classificação e ações:

   | Resultado | Ação automática | Destaque |
   |---|---|---|
   | Confere | — | Verde |
   | Diferente, etiqueta **maior** que o sistema | Item na fila de etiquetas (origem `CONFERENCIA`) | Laranja |
   | Diferente, etiqueta **menor** que o sistema | Item na fila de etiquetas | Vermelho: "Atenção: o cliente paga o menor preço. Troque a etiqueta agora ou corrija o preço." com os botões "Alterar preço" (F1-01, origem `CONFERENCIA`) e "Continuar" |
   | Sem etiqueta | Item na fila de etiquetas | Laranja |

4. Leitura repetida na mesma conferência: "Produto já conferido às 10:32 (Confere). Conferir de novo?"
5. Código não encontrado: oferece "Buscar por nome" e "Registrar pendência" (F3-05).
6. **Resumo:**
   - totais de conferidos, conformes, diferentes e sem etiqueta;
   - lista com filtros;
   - "Encerrar conferência" pede confirmação e guarda o resumo no histórico do aparelho (últimas 10);
   - "Enviar resumo ao computador da loja" se F2-06 estiver ativo.
7. Depois de "Alterar preço", o operador volta ao ciclo da conferência.

**Critérios de aceite**

```gherkin
Funcionalidade: Conferência de gôndola

  Cenário: Etiqueta menor que o sistema
    Dado que o preço do sistema do produto 15 é R$ 10,50
    Quando informo que a etiqueta mostra R$ 9,90
    Então vejo o alerta "o cliente paga o menor preço"
    E o produto 15 entra na fila de etiquetas com origem "CONFERENCIA"

  Cenário: Retomada após fechar o app
    Dado que conferi 12 produtos e o app foi fechado
    Quando abro a conferência
    Então continuo do ponto em que parei com os 12 produtos registrados
```

---

### F2-03 — Promoções v1: calculadora de preços e lembrete de fim

**[L]** (lembretes no aparelho) · Importante · Corrige: BF-49, BF-50

**Regras da calculadora**

1. A base é um produto (por leitura, busca ou coletor; o preço atual vira a base) ou um valor digitado.
2. Modos:

   | Modo | Entrada | Resultado | Pode aplicar? |
   |---|---|---|---|
   | Desconto % | Percentual | Preço final e economia | Sim |
   | Preço alvo | Preço desejado | Desconto % equivalente | Sim |
   | Leve X, pague Y | X e Y | Preço unitário efetivo e desconto equivalente | **Não** — "Esta condição precisa ser cadastrada no sistema principal. O app só altera o preço unitário." |
   | Preço por kg/L | Conteúdo da embalagem (g, kg, ml, L) | R$/kg ou R$/L | Não (informativo) |
3. Arredondamento **sempre para baixo**, para nunca reduzir o desconto anunciado: "sem arredondar", "Terminar em ,99" (R$ 8,08 → R$ 7,99) ou "Terminar em ,90" (R$ 8,08 → R$ 7,90). Se o valor calculado for menor que o final escolhido (ex.: R$ 0,50 com ,99), o valor fica sem arredondamento e a tela informa "Valor baixo demais para arredondar". É diferente dos atalhos de remarcação (F1-02), que mantêm os reais e podem subir o preço.
4. "Aplicar como novo preço" (com produto selecionado) abre a alteração (F1-01) com o valor preenchido e origem `PROMOCAO`. A revisão e os alertas valem normalmente.

**Regras do lembrete de fim**

1. Ao aplicar uma promoção, o app oferece "Lembrar de voltar o preço", com a data de fim.
2. Lembretes vencidos aparecem no topo da Home ("Pendências do dia"): "Promoção de COCA COLA 2L termina hoje: voltar para R$ 9,50", com o botão **"Voltar preço"**, que abre a alteração com R$ 9,50 e origem `PROMOCAO_FIM`.
3. Os lembretes ficam no aparelho que aplicou a promoção (limitação explícita na tela: "Lembrete salvo apenas neste aparelho").
4. A volta do preço segue F0-05: se o preço tiver sido alterado por outra pessoa, há conflito, e o app não sobrescreve.

**Critério de aceite**

```gherkin
  Cenário: Leve 3 pague 2 é apenas informativo
    Dado que o preço do produto é R$ 6,00
    Quando calculo "Leve 3, pague 2"
    Então vejo "Preço unitário efetivo: R$ 4,00 (33,3% de desconto)"
    E não vejo o botão "Aplicar como novo preço"
```

---

### F2-04 — Inventário v1: contagem e relatório de divergências

**[P]** · Importante · Decisões: D-01, D-08 · Corrige: BF-52 a BF-55

**Objetivo.** Contar o estoque com o celular e entregar ao responsável a lista de divergências, para ajuste **no sistema principal**. O app **não grava estoque** (RG-01).

**Regras**

1. "Nova contagem", com nome e setor opcional. Várias contagens podem existir, mas só uma fica ativa por aparelho.
2. **Contagem cega** (padrão ligado — D-08): durante a contagem, o estoque do sistema **não é exibido**, para não influenciar o operador.
3. Ciclo de leitura:
   1. ler o produto;
   2. informar a quantidade (UN: inteiros; KG e similares: até 3 casas decimais);
   3. o teclado numérico abre automaticamente.
4. Produto já contado: "Já contado: 12. Somar 6 (total 18) ou substituir por 6?" O padrão é **Somar**, para contar o mesmo produto em vários locais.
5. Lista de itens contados, com edição e remoção ("Desfazer").
6. **Encerrar contagem:**
   1. confirmação;
   2. o app consulta o estoque atual de cada item e grava esse **retrato** com data e hora;
   3. gera o relatório:
      - contado × sistema, diferença em quantidade e diferença valorizada ao preço de venda (ao custo, se D-03 permitir);
      - filtros "Com divergência" e "Sem divergência";
      - totais.
7. O relatório informa: "O estoque do sistema foi lido às HH:MM. Vendas durante a contagem podem explicar parte das diferenças."
8. Entrega: "Enviar ao computador da loja" (F2-06). Sem F2-06, o relatório fica visível no aparelho.
9. Itens contados ficam salvos a cada inclusão; nada se perde se o app fechar.
10. Rodapé fixo: "O ajuste de estoque é feito no sistema principal."

**Critérios de aceite**

```gherkin
Funcionalidade: Inventário

  Cenário: Contagem em dois locais
    Dado que contei 12 unidades do produto 15
    Quando leio o produto 15 de novo e informo 6
    Então vejo "Já contado: 12" com a opção padrão "Somar"
    E o total do produto passa a 18

  Cenário: Contagem cega
    Dado que a contagem cega está ligada
    Quando leio um produto durante a contagem
    Então não vejo o estoque do sistema
```

**Limitação v1:** contagens de aparelhos diferentes não são consolidadas (v2 com D-01 = servidor).

---

### F2-05 — Promoções v2: vigência com aplicação e retorno automáticos

**[P]/[R]** · Condicional à aprovação de D-04 · Corrige: BF-51

Somente se aprovada. Regras mínimas:

1. Cadastro de promoção no servidor: produto, preço promocional, início e fim, responsável.
2. No início: aplica o preço **somente se** o preço atual for o preço base registrado; senão, marca "conflito" e notifica.
3. No fim: volta ao preço base **somente se** o preço atual ainda for o promocional; senão, marca "conflito" (alguém mudou no meio) e não sobrescreve.
4. Se o servidor estiver desligado no horário, executa ao religar e registra o atraso.
5. Todas as aplicações e retornos passam pela auditoria (origens `PROMOCAO` e `PROMOCAO_FIM`), e os produtos entram na fila de etiquetas.
6. Exige persistência própria no servidor (D-01) e o servidor ligado de forma contínua.

---

### F2-06 — Relatórios no computador da loja (habilitador)

**[P]** · Importante · Decisão: D-01 (opção servidor)

**Regras**

1. "Enviar ao computador da loja" está disponível em: resumo de conferência, relatório de inventário, lista de reposição e pendências de cadastro.
2. O servidor grava um arquivo CSV (UTF-8 com BOM, separador `;`, compatível com Excel em pt-BR) em uma pasta configurada. O nome traz tipo, data, hora, aparelho e operador (ex.: `inventario_2026-10-28_1532_carlos.csv`).
3. O app mostra: "Relatório salvo no computador da loja, na pasta Relatórios, como {nome}".
4. Textos digitados por operadores são protegidos contra fórmulas maliciosas no Excel (conteúdo iniciado por `=`, `+`, `-` ou `@` é neutralizado).
5. Não há listagem web dos relatórios: o acesso é pela pasta do PC.

---

## 9. Fase 3 — Novas rotinas do dia a dia

### F3-01 — Busca de produto por nome

**[L]** · Essencial · Corrige: BF-22, BF-26, BF-36

**Regras**

1. Disponível no scanner ("Buscar por nome"), no estado "não encontrado" e em todos os módulos que leem produtos.
2. Mínimo de 3 caracteres, pesquisa após uma pausa na digitação. Várias palavras exigem que **todas** apareçam ("coca 2l").
3. Até 20 resultados. Primeiro os que começam com o termo, depois os que o contêm, em ordem alfabética. Cada resultado mostra descrição (termo destacado), código, unidade e preço.
4. Mais de 20: "Mostrando 20 resultados. Refine a busca para encontrar mais rápido."
5. Selecionar continua o fluxo de origem (consulta, alteração, etiqueta, conferência, inventário, validade, reposição ou promoção), pelo **ID** do produto.
6. Na alteração de preço iniciada pela busca, a revisão mostra: "Produto escolhido pela busca por nome. Confira a descrição." (BF-36).
7. Sem resultados: "Nenhum produto encontrado para '{termo}'. Tente outra palavra ou leia o código de barras."

**Critério de aceite**

```gherkin
  Cenário: Busca com duas palavras
    Dado que existem "COCA COLA 2L" e "COCA COLA ZERO 350ML"
    Quando busco "coca 2l"
    Então vejo apenas "COCA COLA 2L"
```

---

### F3-02 — Códigos de balança e variações de EAN/UPC

**[L]** · Importante · Decisão: D-06 · Corrige: BF-20, BF-21, BF-37

**Regras de balança** (ativas apenas se configuradas — D-06)

1. Código de 13 dígitos com prefixo configurado (padrão "2") que **não** exista como código principal ou similar é interpretado conforme o layout configurado: posição e tamanho do código interno, posição e tamanho do valor, e tipo do valor (preço ou peso).
2. O produto é buscado pelo código interno, e a tela mostra a faixa "Etiqueta de balança: 0,532 kg" ou "Etiqueta de balança: R$ 10,63".
3. Na alteração de preço a partir de uma etiqueta de balança, a revisão avisa: "Você está alterando o preço por kg. Se a balança tiver preço próprio, atualize-a também." (**a validar**: se as balanças leem o preço do legado).
4. Sem configuração, o "não encontrado" de um código com o prefixo traz a dica: "Parece uma etiqueta de balança. Busque o produto pelo nome."

**Regras de EAN/UPC** (somente após validação no banco real — D-06)

1. Código de 12 dígitos (UPC-A) não encontrado: tentar uma vez com um zero à esquerda (EAN-13 equivalente).
2. Código de 13 dígitos iniciado por zero e não encontrado: tentar uma vez sem o zero.
3. O resultado informa "Encontrado como {código alternativo}". O código lido nunca é alterado ou gravado.

---

### F3-03 — Controle de validade

**[P]** · Importante · Decisões: D-01, D-03, D-08 · Corrige: BF-56, BF-57

**Regras**

1. **Registrar:**
   1. ler o produto;
   2. data de validade, com atalhos "Hoje", "+3 dias", "+7 dias" e calendário;
   3. quantidade (opcional);
   4. local (opcional, ex.: "Gôndola 3").
2. **Painel:**
   - grupos "Vencidos", "Vencem hoje", "Próximos 7 dias" (D-08) e "Demais", ordenados por data;
   - contadores na Home ("2 vencidos · 5 vencem em 7 dias").
3. **Ações por registro:**
   - "Retirado da gôndola";
   - "Esgotado / vendido";
   - "Rebaixar preço";
   - "Excluir registro".
4. **Rebaixar preço:**
   1. a calculadora (F2-03) sugere o desconto pelos dias restantes (padrão: ≤ 7 dias, 10%; ≤ 3 dias, 30%; ≤ 1 dia, 50% — D-08);
   2. **aviso obrigatório**, em destaque: "O sistema tem **um único preço por produto**. A redução vale para **todas** as unidades, inclusive as de validade longa. Use somente se todas as unidades estiverem perto do vencimento.";
   3. o operador marca "Entendi";
   4. segue para a alteração (F1-01), com origem `VALIDADE` (e permissão, se D-03 restringir).
5. Os registros ficam no aparelho na v1 (limitação explícita: "Registros salvos apenas neste aparelho"). Na v2, ficam no servidor e compartilhados (D-01).

**Critério de aceite**

```gherkin
  Cenário: Aviso de preço único na rebaixa
    Dado que um registro de validade vence em 2 dias
    Quando toco em "Rebaixar preço"
    Então vejo o aviso de que a redução vale para todas as unidades
    E só avanço para a alteração após marcar "Entendi"
```

---

### F3-04 — Reposição e ruptura

**[L]** (lista no aparelho) · **[P]** (lista compartilhada) · Importante · Decisões: D-01, D-07 · Corrige: BF-58

**Regras**

1. Adicionar:
   1. ler o produto;
   2. o app mostra o estoque do sistema;
   3. informar a quantidade a repor (opcional) e uma observação.
2. Classificação automática:
   - **"Ruptura: sem estoque no sistema"**, quando o estoque é ≤ 0, com a orientação "Avise compras";
   - **"Repor da reserva"**, quando há estoque.
3. Situações "Pendente" e "Reposto" (gesto de arrastar), com filtro.
4. Entrega:
   - v1: "Enviar ao computador da loja" (F2-06);
   - v2: lista compartilhada no servidor, para o estoquista ver no próprio aparelho (D-01).

---

### F3-05 — Pendências de cadastro

**[P]** · Desejável · Corrige: BF-26, BF-59

**Regras**

1. No "não encontrado" (e no código duplicado bloqueado), a opção "Registrar pendência" pede:
   - código (preenchido);
   - "Nome na embalagem" (obrigatório, até 60 caracteres);
   - observação (opcional).
2. A lista de pendências tem as situações "Aberta" e "Resolvida" e pode ser enviada ao computador da loja (F2-06).
3. O app **nunca cadastra produto** (RG-01). O texto da tela deixa isso claro: "O cadastro é feito no sistema principal."

---

### F3-06 — Histórico de preços do produto

**[P]** (leitura da auditoria no servidor) · Desejável · Depende de: F0-02

**Regras**

1. As telas de consulta e de alteração têm "Histórico de preço": as últimas 20 alterações feitas **pelo app** (data e hora, de → para, operador e origem).
2. Aviso fixo: "Mostra apenas alterações feitas pelo aplicativo. Alterações feitas no sistema principal não aparecem aqui."
3. Visibilidade por perfil conforme D-03.

---

### F3-07 — Permissões por perfil

**[R]** · Condicional a D-03 · Corrige: BF-60, BF-61

**Proposta de matriz** (a decidir; o padrão continua sendo acesso igual para todos):

| Ação | Perfil 1 | Perfil 2 | Perfil 3 |
|---|---|---|---|
| Consultar produto, estoque e similares | Sim | Sim | Sim |
| Conferência, reposição, validade (registro), inventário (contagem), pendências | Sim | Sim | Sim |
| Adicionar à fila de etiquetas | Sim | Sim | Sim |
| Enviar etiquetas para impressão | A decidir | Sim | Sim |
| Alterar preço / aplicar promoção / rebaixa | **Não** | Sim | Sim |
| Confirmar venda abaixo do custo | Não | A decidir | Sim |
| Ver custo e margem | Não | Sim | Sim |
| Ver histórico de preço | A decidir | Sim | Sim |

**Regras**

1. As permissões são calculadas e **verificadas na API**, que responde `PERMISSAO_NEGADA` quando faltam. O app só reflete o que a API informa.
2. No app, ações sem permissão aparecem desabilitadas com o motivo (RG-13).
3. A matriz é configurável no servidor, sem nova versão do app.

**v2 (fora deste escopo):** pedido de alteração de preço feito pelo perfil 1 e aprovado pelo perfil 2.

---

### F3-08 — Pareamento por QR code

**[L]** · Desejável · Decisão: D-07 (geração do QR) · Corrige: BF-02, BF-04

**Regras**

1. No computador da loja, a página `http://localhost:8080/pareamento/` mostra:
   - nome da loja e versão do servidor;
   - os IPv4 privados da máquina, com um QR code para cada um e destaque para a rede do Wi-Fi operacional.
2. Na configuração do servidor (primeiro acesso, recuperação ou interno), "Ler QR do servidor" abre o leitor (formato QR) e preenche o IP.
3. Em seguida, o app testa a conexão automaticamente. As demais regras de cada modo continuam valendo (senha do caixa na recuperação e no modo interno).
4. QR com conteúdo inválido: "Este QR code não é do servidor da loja."

---

## 10. Glossário

| Termo | Significado |
|---|---|
| Legado | Sistema VB.NET e banco MySQL 5.5 da loja |
| PDV | Frente de caixa do sistema legado |
| Gôndola | Prateleira de exposição, onde fica a etiqueta de preço |
| Lote de etiquetas | Conjunto de etiquetas enviado do app ao computador para impressão |
| Contagem cega | Contagem de estoque sem ver a quantidade registrada no sistema |
| Ruptura | Falta do produto na gôndola e no estoque |
| Coletor | Aparelho Android com leitor de código de barras físico |
| Sessão | Período autenticado do operador no app |
| Origem | Tela ou rotina de onde partiu a alteração de preço |

## 11. Rastreabilidade

| Item | Achados (01) | Brechas (00) | Frontend (03) | Backend (04) |
|---|---|---|---|---|
| F0-01 | A-01 | BF-08, BF-09, BF-10, BF-33 | §4.2, §6.17 | §5.1 |
| F0-02 | A-02 | BF-34 | — | §5.2 |
| F0-03 | A-03, A-04 | BF-12 | §4.1, §6.2 | §5.3 |
| F0-04 | A-06 | BF-03 | — | §5.4 |
| F0-05 | A-08 | BF-31, BF-32 | §6.7 | §5.5 |
| F0-06 | A-07, A-09 | BF-24 | §6.6 | §5.6 |
| F0-07 | A-01 | BF-63 | — | §5.7 |
| F0-08 | A-28 | BF-13 | §4.9, §6.3 | §5.8 |
| F0-09 | A-10 | BF-01, BF-02, BF-06 | §6.1, §6.2 | §5.8 |
| F0-10 | A-13, A-14 | BF-07, BF-28 | §4.1 | §5.6 |
| F0-11 | A-11, A-21 | BF-14, BF-15, BF-17, BF-40 | §4.3 | — |
| F0-12 | A-12 | BF-05, BF-18, BF-19 | §4.5, §6.5 | — |
| F0-13 | A-24 a A-27 | BF-62 | §9 | §9 |
| F1-01 | A-15 | BF-29, BF-30, BF-35 | §6.7, §6.8 | §5.9 |
| F1-02 | — | — | §6.7 | — |
| F1-03 | A-17, A-22 | BF-25, BF-39 | §6.6, §6.8 | — |
| F1-04 | — | BF-23 | §4.5 | — |
| F1-05 | A-20 | BF-27 | §6.6 | §5.10 |
| F1-06 | — | — | §6.4 | — |
| F1-07 | A-18 | BF-08, BF-11 | §4.2, §6.16 | §5.1 |
| F1-08 | A-19 | BF-64 | §4.4 | — |
| F1-09 | A-23 | BF-16 | §4.11 | — |
| F2-01 | A-16 | BF-38, BF-41 a BF-45 | §6.9 | §5.11 |
| F2-02 | — | BF-46 a BF-48 | §6.10 | — |
| F2-03 | — | BF-49, BF-50 | §6.11 | — |
| F2-04 | — | BF-52 a BF-55 | §6.12 | §5.14 |
| F2-05 | — | BF-51 | — | §5.15 |
| F2-06 | — | BF-58, BF-59 | §6.15 | §5.14 |
| F3-01 | A-17 | BF-22, BF-26, BF-36 | §6.5 | §5.12 |
| F3-02 | — | BF-20, BF-21, BF-37 | §6.6 | §5.13 |
| F3-03 | — | BF-56, BF-57 | §6.13 | — |
| F3-04 | — | BF-58 | §6.14 | — |
| F3-05 | — | BF-26, BF-59 | §6.15 | — |
| F3-06 | — | — | §6.6 | §5.16 |
| F3-07 | — | BF-60, BF-61 | §4.10 | §5.17 |
| F3-08 | — | BF-02, BF-04 | §6.1 | §5.18 |
