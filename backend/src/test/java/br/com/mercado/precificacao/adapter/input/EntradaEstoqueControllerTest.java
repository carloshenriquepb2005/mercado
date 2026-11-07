package br.com.mercado.precificacao.adapter.input;

import br.com.mercado.precificacao.adapter.exception.GlobalExceptionHandler;
import br.com.mercado.precificacao.adapter.input.controller.EntradaEstoqueController;
import br.com.mercado.precificacao.adapter.input.mapper.EntradaEstoqueInputMapper;
import br.com.mercado.precificacao.adapter.input.request.RegistrarEntradaEstoqueRequest;
import br.com.mercado.precificacao.core.exception.EstoqueLimiteExcedidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.model.EntradaEstoque;
import br.com.mercado.precificacao.port.input.RegistrarEntradaEstoqueInputPort;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EntradaEstoqueControllerTest {
    private static final String URL = "/api/produtos/15/entrada-estoque";

    private RegistrarEntradaEstoqueInputPort registrar;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registrar = mock(RegistrarEntradaEstoqueInputPort.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new EntradaEstoqueController(registrar, new EntradaEstoqueInputMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar200ComValoresAnterioresEAtuais() throws Exception {
        // Arrange
        when(registrar.executar(15L, new BigDecimal("12.500"), new BigDecimal("7.35"), null))
                .thenReturn(new EntradaEstoque(15L, new BigDecimal("18.500"), new BigDecimal("31.000"),
                        new BigDecimal("6.90"), new BigDecimal("7.35")));

        // Act / Assert
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":12.500,\"precoCusto\":7.35}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(15))
                .andExpect(jsonPath("$.estoqueAnterior").value(18.500))
                .andExpect(jsonPath("$.estoqueAtual").value(31.000))
                .andExpect(jsonPath("$.precoCustoAnterior").value(6.90))
                .andExpect(jsonPath("$.precoCustoAtual").value(7.35));
    }

    @Test
    void deveRepassarDataDeVencimentoInformada() throws Exception {
        // Arrange
        when(registrar.executar(any(), any(), any(), any())).thenReturn(
                new EntradaEstoque(15L, BigDecimal.ZERO, BigDecimal.ONE, null, BigDecimal.ONE));

        // Act
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1,\"precoCusto\":1,\"dataVencimento\":\"2027-03-15\"}"))
                .andExpect(status().isOk());

        // Assert
        verify(registrar).executar(15L, new BigDecimal("1"), new BigDecimal("1"), LocalDate.of(2027, 3, 15));
    }

    @Test
    void deveTratarDataDeVencimentoOmitidaNulaOuVaziaComoNaoInformada() throws Exception {
        // Arrange
        when(registrar.executar(any(), any(), any(), any())).thenReturn(
                new EntradaEstoque(15L, BigDecimal.ZERO, BigDecimal.ONE, null, BigDecimal.ONE));

        // Act
        for (String body : new String[]{
                "{\"quantidade\":1,\"precoCusto\":1}",
                "{\"quantidade\":1,\"precoCusto\":1,\"dataVencimento\":null}",
                "{\"quantidade\":1,\"precoCusto\":1,\"dataVencimento\":\"\"}"}) {
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk());
        }

        // Assert
        verify(registrar, org.mockito.Mockito.times(3))
                .executar(15L, new BigDecimal("1"), new BigDecimal("1"), null);
    }

    @Test
    void deveRejeitarDadosInvalidosCom400SemChamarOUseCase() throws Exception {
        for (String body : new String[]{
                "{}",
                "{\"precoCusto\":1}",
                "{\"quantidade\":1}",
                "{\"quantidade\":0,\"precoCusto\":1}",
                "{\"quantidade\":-1,\"precoCusto\":1}",
                "{\"quantidade\":1.0001,\"precoCusto\":1}",
                "{\"quantidade\":10000000,\"precoCusto\":1}",
                "{\"quantidade\":1,\"precoCusto\":0}",
                "{\"quantidade\":1,\"precoCusto\":1.001}",
                "{\"quantidade\":1,\"precoCusto\":100000000}",
                "{\"quantidade\":1,\"precoCusto\":1,\"dataVencimento\":\"15/03/2027\"}",
                "{\"quantidade\":1,\"precoCusto\":1,\"dataVencimento\":\"2027-02-30\"}",
                "{\"quantidade\":\"abc\",\"precoCusto\":1}",
                "nao-e-json"}) {
            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("DADOS_INVALIDOS"))
                    .andExpect(content().string(not(containsString("stackTrace"))));
        }
        verify(registrar, never()).executar(any(), any(), any(), any());
    }

    @Test
    void deveRejeitarIdNaoNumericoCom400() throws Exception {
        mockMvc.perform(post("/api/produtos/abc/entrada-estoque").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1,\"precoCusto\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404QuandoProdutoNaoExiste() throws Exception {
        // Arrange
        when(registrar.executar(any(), any(), any(), any())).thenThrow(new ProdutoNaoEncontradoException());

        // Act / Assert
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1,\"precoCusto\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void deveRetornar400QuandoLimiteDeEstoqueForExcedido() throws Exception {
        // Arrange
        when(registrar.executar(any(), any(), any(), any())).thenThrow(new EstoqueLimiteExcedidoException());

        // Act / Assert
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1,\"precoCusto\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DADOS_INVALIDOS"));
    }

    @Test
    void deveRetornar503QuandoBancoIndisponivel() throws Exception {
        // Arrange
        when(registrar.executar(any(), any(), any(), any()))
                .thenThrow(new DataAccessResourceFailureException("jdbc://segredo"));

        // Act / Assert
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1,\"precoCusto\":1}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("BANCO_INDISPONIVEL"))
                .andExpect(content().string(not(containsString("segredo"))));
    }

    @Test
    void validacaoDeMetodoDeveSerCompativelComInterfaceSwagger() throws Exception {
        // Arrange
        EntradaEstoqueController controller =
                new EntradaEstoqueController(registrar, new EntradaEstoqueInputMapper());
        Method metodo = EntradaEstoqueController.class.getMethod(
                "registrar", Long.class, RegistrarEntradaEstoqueRequest.class);

        // Act
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            var violacoes = validator.forExecutables().validateParameters(
                    controller, metodo, new Object[]{0L, new RegistrarEntradaEstoqueRequest(null, null, null)});

            // Assert: id não positivo + quantidade e custo obrigatórios
            assertThat(violacoes).hasSize(3);
        }
    }
}
