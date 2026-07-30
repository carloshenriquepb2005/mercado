package br.com.mercado.precificacao.adapter.in.web;

import br.com.mercado.precificacao.adapter.in.web.controller.AuthController;
import br.com.mercado.precificacao.application.dto.LoginResult;
import br.com.mercado.precificacao.application.port.in.AutenticarUsuarioUseCase;
import br.com.mercado.precificacao.domain.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    private AutenticarUsuarioUseCase autenticar;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        autenticar = mock(AutenticarUsuarioUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(autenticar))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornarUsuarioAutenticado() throws Exception {
        when(autenticar.executar("carlos", "123"))
                .thenReturn(new LoginResult(7L, "Carlos", 2));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carlos\",\"password\":\"123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(7))
                .andExpect(jsonPath("$.username").value("Carlos"))
                .andExpect(jsonPath("$.displayName").value("Carlos"))
                .andExpect(jsonPath("$.perfil").value(2));
    }

    @Test
    void deveRetornar400ParaCamposVaziosE401ParaCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        when(autenticar.executar("carlos", "errada")).thenThrow(new CredenciaisInvalidasException());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carlos\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }
}
