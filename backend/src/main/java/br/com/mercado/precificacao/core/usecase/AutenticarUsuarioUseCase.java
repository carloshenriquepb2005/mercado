package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.core.model.LoginResult;
import br.com.mercado.precificacao.core.model.Usuario;
import br.com.mercado.precificacao.port.input.AutenticarUsuarioInputPort;
import br.com.mercado.precificacao.port.output.BuscarUsuarioOutputPort;

public final class AutenticarUsuarioUseCase implements AutenticarUsuarioInputPort {
    private final BuscarUsuarioOutputPort buscarUsuarioOutputPort;

    public AutenticarUsuarioUseCase(BuscarUsuarioOutputPort buscarUsuarioOutputPort) {
        this.buscarUsuarioOutputPort = buscarUsuarioOutputPort;
    }

    @Override
    public LoginResult executar(String username, String password) {
        String nome = username == null ? "" : username.trim();
        if (nome.isEmpty() || password == null || password.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }
        Usuario usuario = buscarUsuarioOutputPort.buscarPorNomeESenha(nome, password)
                .orElseThrow(CredenciaisInvalidasException::new);
        return new LoginResult(usuario.codigo(), usuario.nome(), usuario.perfil());
    }
}
