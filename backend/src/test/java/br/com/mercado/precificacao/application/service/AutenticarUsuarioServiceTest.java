package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.LoginResult;
import br.com.mercado.precificacao.application.port.out.BuscarUsuarioPort;
import br.com.mercado.precificacao.domain.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticarUsuarioServiceTest {
    private BuscarUsuarioPort buscarUsuario;
    private AutenticarUsuarioService service;

    @BeforeEach
    void setUp() {
        buscarUsuario = mock(BuscarUsuarioPort.class);
        service = new AutenticarUsuarioService(buscarUsuario);
    }

    @Test
    void deveAutenticarUsuarioDaTabelaComSenhaSemHashEmQualquerPerfil() {
        for (int perfil = 1; perfil <= 3; perfil++) {
            when(buscarUsuario.buscarPorNomeESenha("Carlos", "senha123"))
                    .thenReturn(Optional.of(new Usuario(10L, "Carlos", perfil)));
            LoginResult result = service.executar(" Carlos ", "senha123");
            assertEquals(10L, result.codigo());
            assertEquals("Carlos", result.nome());
            assertEquals(perfil, result.perfil());
        }
        verify(buscarUsuario, org.mockito.Mockito.times(3))
                .buscarPorNomeESenha("Carlos", "senha123");
    }

    @Test
    void deveRejeitarCredenciaisNaoEncontradas() {
        when(buscarUsuario.buscarPorNomeESenha("Carlos", "errada")).thenReturn(Optional.empty());
        assertThrows(CredenciaisInvalidasException.class, () -> service.executar("Carlos", "errada"));
    }

    @Test
    void deveRejeitarCamposVaziosSemConsultarBanco() {
        assertThrows(CredenciaisInvalidasException.class, () -> service.executar("", "senha"));
        assertThrows(CredenciaisInvalidasException.class, () -> service.executar("Carlos", null));
    }
}
