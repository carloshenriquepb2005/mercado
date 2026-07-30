# Requisitos

O atendente deve conseguir entrar com `nome` e `senha` da tabela `usuarios`, visualizar a Home e a disponibilidade da API, escanear ou digitar um código numérico de até 13 caracteres, visualizar descrição/código/preço, informar preço positivo com até duas casas decimais, confirmar a alteração e consultar seu resumo na tela de Sucesso. Os perfis 1, 2 e 3 possuem o mesmo acesso ao app.

O sistema deve preservar zeros à esquerda, buscar códigos principais antes dos similares, retornar mensagens amigáveis e impedir múltiplos envios. Não há cadastro, exclusão, edição de outros campos, sincronização offline, migrations ou acesso direto do aplicativo ao MySQL.

Promoções, etiquetas, inventário e configurações aparecem como atalhos visuais da Home, mas permanecem indisponíveis até que existam requisitos e contratos próprios.
