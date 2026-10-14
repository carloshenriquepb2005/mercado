package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.ServidorNaoValidadoException;
import br.com.mercado.precificacao.port.input.TestarConexaoServidorInputPort;
import br.com.mercado.precificacao.port.output.VerificarUsuarioReferenciaOutputPort;

public final class TestarConexaoServidorUseCase implements TestarConexaoServidorInputPort {
    private static final String USUARIO_REFERENCIA = "bruno";
    private final VerificarUsuarioReferenciaOutputPort verificarUsuarioReferenciaOutputPort;

    public TestarConexaoServidorUseCase(
            VerificarUsuarioReferenciaOutputPort verificarUsuarioReferenciaOutputPort) {
        this.verificarUsuarioReferenciaOutputPort = verificarUsuarioReferenciaOutputPort;
    }

    @Override
    public void executar() {
        if (!verificarUsuarioReferenciaOutputPort.existePorNome(USUARIO_REFERENCIA)) {
            throw new ServidorNaoValidadoException();
        }
    }
}
