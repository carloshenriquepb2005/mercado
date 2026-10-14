package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.SenhaCaixaInvalidaException;
import br.com.mercado.precificacao.port.input.ValidarSenhaCaixaInputPort;
import br.com.mercado.precificacao.port.output.ValidarSenhaCaixaOutputPort;

public final class ValidarSenhaCaixaUseCase implements ValidarSenhaCaixaInputPort {
    private final ValidarSenhaCaixaOutputPort validarSenhaCaixaOutputPort;

    public ValidarSenhaCaixaUseCase(ValidarSenhaCaixaOutputPort validarSenhaCaixaOutputPort) {
        this.validarSenhaCaixaOutputPort = validarSenhaCaixaOutputPort;
    }

    @Override
    public void executar(String senha) {
        if (senha == null || senha.isBlank() || !validarSenhaCaixaOutputPort.existePorSenha(senha)) {
            throw new SenhaCaixaInvalidaException();
        }
    }
}
