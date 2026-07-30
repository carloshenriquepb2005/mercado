package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.LoginResult;
import br.com.mercado.precificacao.application.port.in.AutenticarUsuarioUseCase;
import br.com.mercado.precificacao.application.port.out.BuscarUsuarioPort;
import br.com.mercado.precificacao.domain.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.domain.model.Usuario;

public final class AutenticarUsuarioService implements AutenticarUsuarioUseCase {
    private final BuscarUsuarioPort buscarUsuarioPort;

    public AutenticarUsuarioService(BuscarUsuarioPort buscarUsuarioPort) {
        this.buscarUsuarioPort = buscarUsuarioPort;
    }

    @Override
    public LoginResult executar(String username, String password) {
        String nome = username == null ? "" : username.trim();
        if (nome.isEmpty() || password == null || password.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }
        Usuario usuario = buscarUsuarioPort.buscarPorNomeESenha(nome, password)
                .orElseThrow(CredenciaisInvalidasException::new);
        return new LoginResult(usuario.codigo(), usuario.nome(), usuario.perfil());
    }
}
