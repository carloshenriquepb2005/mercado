package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.Usuario;

public interface AutenticarUsuarioInputPort {
    Usuario executar(String username, String password);
}
