package br.com.mercado.precificacao.application.port.out;

import br.com.mercado.precificacao.domain.model.Usuario;

import java.util.Optional;

public interface BuscarUsuarioPort {
    Optional<Usuario> buscarPorNomeESenha(String nome, String senha);
}
