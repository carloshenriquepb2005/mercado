package br.com.mercado.precificacao.adapter.input;

import br.com.mercado.precificacao.adapter.exception.GlobalExceptionHandler;
import br.com.mercado.precificacao.adapter.input.controller.ConfiguracaoController;
import br.com.mercado.precificacao.core.exception.SenhaCaixaInvalidaException;
import br.com.mercado.precificacao.core.exception.ServidorNaoValidadoException;
import br.com.mercado.precificacao.port.input.TestarConexaoServidorInputPort;
import br.com.mercado.precificacao.port.input.ValidarSenhaCaixaInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfiguracaoControllerTest {
    private TestarConexaoServidorInputPort testarConexao;
    private ValidarSenhaCaixaInputPort validarSenha;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        testarConexao = mock(TestarConexaoServidorInputPort.class);
        validarSenha = mock(ValidarSenhaCaixaInputPort.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ConfiguracaoController(testarConexao, validarSenha))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveValidarConexaoSemExporDadosDoUsuarioConsultado() throws Exception {
        mockMvc.perform(get("/api/configuracoes/conexao"))
                .andExpect(status().isNoContent());
        verify(testarConexao).executar();
    }

    @Test
    void deveRetornar503QuandoConexaoNaoForValidada() throws Exception {
        doThrow(new ServidorNaoValidadoException()).when(testarConexao).executar();
        mockMvc.perform(get("/api/configuracoes/conexao"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CONEXAO_NAO_VALIDADA"));
    }

    @Test
    void deveValidarSenhaCaixa() throws Exception {
        mockMvc.perform(post("/api/configuracoes/validar-senha-caixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"1234\"}"))
                .andExpect(status().isNoContent());
        verify(validarSenha).executar("1234");
    }

    @Test
    void deveRejeitarSenhaAusenteInvalidaOuMuitoLonga() throws Exception {
        mockMvc.perform(post("/api/configuracoes/validar-senha-caixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/configuracoes/validar-senha-caixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"12345678901\"}"))
                .andExpect(status().isBadRequest());

        doThrow(new SenhaCaixaInvalidaException()).when(validarSenha).executar("9999");
        mockMvc.perform(post("/api/configuracoes/validar-senha-caixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"9999\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SENHA_CAIXA_INVALIDA"));
    }
}
