package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.SenhaCaixaInvalidaException;
import br.com.mercado.precificacao.port.output.ValidarSenhaCaixaOutputPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidarSenhaCaixaUseCaseTest {
    private final ValidarSenhaCaixaOutputPort outputPort = mock(ValidarSenhaCaixaOutputPort.class);
    private final ValidarSenhaCaixaUseCase useCase = new ValidarSenhaCaixaUseCase(outputPort);

    @Test
    void deveAceitarSenhaEncontrada() {
        when(outputPort.existePorSenha("1234")).thenReturn(true);
        assertDoesNotThrow(() -> useCase.executar("1234"));
        verify(outputPort).existePorSenha("1234");
    }

    @Test
    void deveRejeitarSenhaAusenteOuNaoEncontrada() {
        assertThrows(SenhaCaixaInvalidaException.class, () -> useCase.executar(null));
        assertThrows(SenhaCaixaInvalidaException.class, () -> useCase.executar(" "));
        verify(outputPort, never()).existePorSenha(null);

        when(outputPort.existePorSenha("9999")).thenReturn(false);
        assertThrows(SenhaCaixaInvalidaException.class, () -> useCase.executar("9999"));
    }
}
