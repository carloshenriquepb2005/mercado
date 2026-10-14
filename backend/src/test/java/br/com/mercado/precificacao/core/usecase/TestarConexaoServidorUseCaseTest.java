package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.ServidorNaoValidadoException;
import br.com.mercado.precificacao.port.output.VerificarUsuarioReferenciaOutputPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestarConexaoServidorUseCaseTest {
    private final VerificarUsuarioReferenciaOutputPort outputPort =
            mock(VerificarUsuarioReferenciaOutputPort.class);
    private final TestarConexaoServidorUseCase useCase = new TestarConexaoServidorUseCase(outputPort);

    @Test
    void deveValidarQuandoUsuarioDeReferenciaExiste() {
        when(outputPort.existePorNome("bruno")).thenReturn(true);
        assertDoesNotThrow(useCase::executar);
        verify(outputPort).existePorNome("bruno");
    }

    @Test
    void deveFalharSemUsuarioDeReferencia() {
        when(outputPort.existePorNome("bruno")).thenReturn(false);
        assertThrows(ServidorNaoValidadoException.class, useCase::executar);
    }
}
