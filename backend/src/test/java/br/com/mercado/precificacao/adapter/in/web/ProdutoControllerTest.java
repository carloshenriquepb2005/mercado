package br.com.mercado.precificacao.adapter.in.web;

import br.com.mercado.precificacao.adapter.in.web.controller.ProdutoController;
import br.com.mercado.precificacao.adapter.in.web.mapper.ProdutoWebMapper;
import br.com.mercado.precificacao.application.dto.AtualizacaoPrecoCommand;
import br.com.mercado.precificacao.application.dto.ProdutoResult;
import br.com.mercado.precificacao.application.port.in.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.application.port.in.BuscarProdutoPorCodigoUseCase;
import br.com.mercado.precificacao.domain.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerTest {
    private BuscarProdutoPorCodigoUseCase buscar;
    private AtualizarPrecoProdutoUseCase atualizar;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        buscar = mock(BuscarProdutoPorCodigoUseCase.class);
        atualizar = mock(AtualizarPrecoProdutoUseCase.class);
        ProdutoController controller = new ProdutoController(buscar, atualizar, new ProdutoWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDeveRetornarProduto() throws Exception {
        when(buscar.executar("00123")).thenReturn(new ProdutoResult(1L, "00123", "Produto",
                new BigDecimal("9.50")));
        mockMvc.perform(get("/api/produtos/00123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigoBarras").value("00123"))
                .andExpect(jsonPath("$.precoVenda").value(9.50))
                .andExpect(content().string(not(containsString("precoCusto"))));
    }

    @Test
    void getDevePadronizar400E404SemDetalhesInternos() throws Exception {
        when(buscar.executar("ABC")).thenThrow(new CodigoBarrasInvalidoException());
        mockMvc.perform(get("/api/produtos/ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.path").value("/api/produtos/ABC"))
                .andExpect(content().string(not(containsString("stackTrace"))))
                .andExpect(content().string(not(containsString("SELECT"))));

        when(buscar.executar("999")).thenThrow(new ProdutoNaoEncontradoException());
        mockMvc.perform(get("/api/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void patchDeveRetornar204EUsarId() throws Exception {
        mockMvc.perform(patch("/api/produtos/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPreco\":10.50}"))
                .andExpect(status().isNoContent());
        verify(atualizar).executar(15L, new AtualizacaoPrecoCommand(new BigDecimal("10.50")));
    }

    @Test
    void patchDeveRejeitarPrecosInvalidos() throws Exception {
        for (String body : new String[]{"{}", "{\"novoPreco\":0}", "{\"novoPreco\":-1}",
                "{\"novoPreco\":1.001}", "{\"novoPreco\":100000000}"}) {
            mockMvc.perform(patch("/api/produtos/15")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("DADOS_INVALIDOS"));
        }
    }

    @Test
    void patchDeveRetornar404QuandoCasoDeUsoNaoEncontrar() throws Exception {
        doThrow(new ProdutoNaoEncontradoException()).when(atualizar).executar(any(), any());
        mockMvc.perform(patch("/api/produtos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPreco\":10.50}"))
                .andExpect(status().isNotFound());
    }
}
