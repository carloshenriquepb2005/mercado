package br.com.mercado.precificacao.port.output;

import br.com.mercado.precificacao.core.model.Usuario;

import java.util.Optional;

public interface BuscarUsuarioOutputPort {
    Optional<Usuario> buscarPorNomeESenha(String nome, String senha);
}
