package br.com.mercado.precificacao.application.port.in;

import br.com.mercado.precificacao.application.dto.LoginResult;

public interface AutenticarUsuarioUseCase {
    LoginResult executar(String username, String password);
}
