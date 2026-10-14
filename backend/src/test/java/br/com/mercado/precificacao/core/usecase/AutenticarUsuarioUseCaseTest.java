package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.core.model.LoginResult;
import br.com.mercado.precificacao.core.model.Usuario;
import br.com.mercado.precificacao.port.output.BuscarUsuarioOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticarUsuarioUseCaseTest {
    private BuscarUsuarioOutputPort buscarUsuarioOutputPort;
    private AutenticarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        buscarUsuarioOutputPort = mock(BuscarUsuarioOutputPort.class);
        useCase = new AutenticarUsuarioUseCase(buscarUsuarioOutputPort);
    }

    @Test
    void deveAutenticarUsuarioDaTabelaComSenhaSemHashEmQualquerPerfil() {
        for (int perfil = 1; perfil <= 3; perfil++) {
            // Arrange
            when(buscarUsuarioOutputPort.buscarPorNomeESenha("Carlos", "senha123"))
                    .thenReturn(Optional.of(new Usuario(10L, "Carlos", perfil)));

            // Act
            LoginResult result = useCase.executar(" Carlos ", "senha123");

            // Assert
            assertThat(result).isEqualTo(new LoginResult(10L, "Carlos", perfil));
        }
        verify(buscarUsuarioOutputPort, org.mockito.Mockito.times(3))
                .buscarPorNomeESenha("Carlos", "senha123");
    }

    @Test
    void deveRejeitarCredenciaisNaoEncontradas() {
        // Arrange
        when(buscarUsuarioOutputPort.buscarPorNomeESenha("Carlos", "errada"))
                .thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar("Carlos", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveRejeitarCamposAusentesSemConsultarBanco() {
        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(null, "senha"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> useCase.executar(" ", "senha"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> useCase.executar("Carlos", null))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> useCase.executar("Carlos", ""))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(buscarUsuarioOutputPort, never()).buscarPorNomeESenha("Carlos", "senha");
    }
}
