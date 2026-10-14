# Requisitos

- O atendente deve conseguir entrar com `nome` e `senha` da tabela `usuarios`, visualizar a Home e a disponibilidade da API, escanear ou digitar um código numérico de até 13 caracteres, visualizar descrição/código/preço, informar preço positivo com até duas casas decimais, confirmar a alteração e consultar seu resumo na tela de Sucesso. Os perfis 1, 2 e 3 possuem o mesmo acesso ao app.

- O sistema deve preservar zeros à esquerda, buscar códigos principais antes dos similares, retornar mensagens amigáveis e impedir múltiplos envios. Não há acesso direto do aplicativo ao MySQL.

- Na primeira abertura, o aplicativo deve exigir um IPv4 de servidor validado antes de liberar o login. A URL fica persistida somente no aparelho e pode ser alterada em Ajustes após validação de `senha.SENHA_CAIXA`. A senha do caixa não é persistida e a autorização é consumida ao abrir a tela protegida. No acesso interno, a conexão salva é verificada automaticamente, sem exibir o redirecionamento para o login, e qualquer teste expira depois de sete segundos.

- O login simples consulta a tabela legada `usuarios` por `nome` e `senha`, sem hash, e aceita igualmente os perfis 1, 2 e 3. O aplicativo guarda somente código, nome e perfil na sessão local; a senha não é persistida. Esse acesso funciona como controle de entrada da interface e não adiciona token aos endpoints de produto. A tabela de usuários é somente leitura.

- A API resolve primeiro `produtos.CODIGO_PRODUTO`; caso não encontre, resolve `produtos_similares.CODIGO_SIMILAR` para o código principal. A única escrita é uma transação que altera `produtos.PRECO_UNITARIO` pelo `ID`; as novas consultas a `usuarios` e `senha` são somente leitura.
