package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.LoginResult;

public interface AutenticarUsuarioInputPort {
    LoginResult executar(String username, String password);
}
