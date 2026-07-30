# Implantação local

1. Instale Java 21 no servidor Windows e gere o JAR com `mvn clean package`.
2. Crie um usuário MySQL exclusivo. Conceda apenas `SELECT` em `produtos`, `produtos_similares` e `usuarios`, além de `UPDATE (PRECO_UNITARIO)` em `produtos`. Revise os comandos com o administrador; o app não os executa.
3. Configure as variáveis de `env.example` no serviço do Windows, usando IP fixo para o servidor.
4. Libere somente a porta da API (padrão `8080`) na rede local e valide `/actuator/health`.
5. Defina `MOBILE_API_URL=http://IP-DO-SERVIDOR:8080`, instale JDK 21+ e Android SDK 35, gere o build e execute `npx cap sync android`.
6. Abra o projeto Android, gere um APK assinado, instale nos aparelhos e conceda câmera.
7. Com o aparelho na Wi-Fi da loja, valide login para usuários dos perfis 1, 2 e 3, consulta principal, similar, 404, atualização, tela de Sucesso e reflexo no PDV.

O MySQL não deve ser exposto à rede dos celulares. Mantenha `ddl-auto: none`, faça backup do legado e monitore o arquivo definido por `LOG_FILE`.
