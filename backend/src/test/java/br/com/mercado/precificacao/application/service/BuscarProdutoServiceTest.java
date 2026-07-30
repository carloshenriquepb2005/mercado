package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.ProdutoResult;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoSimilarPort;
import br.com.mercado.precificacao.domain.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.domain.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class BuscarProdutoServiceTest {
    private BuscarProdutoPort produtoPort;
    private BuscarProdutoSimilarPort similarPort;
    private BuscarProdutoService service;

    @BeforeEach
    void setUp() {
        produtoPort = mock(BuscarProdutoPort.class);
        similarPort = mock(BuscarProdutoSimilarPort.class);
        service = new BuscarProdutoService(produtoPort, similarPort);
    }

    @Test
    void deveRetornarPrincipalSemConsultarSimilar() {
        when(produtoPort.buscarPorCodigoPrincipal("001234")).thenReturn(Optional.of(produto()));
        ProdutoResult result = service.executar(" 001234 ");
        assertThat(result.codigoBarras()).isEqualTo("001234");
        verify(similarPort, never()).buscarCodigoPrincipal("001234");
    }

    @Test
    void deveResolverCodigoSimilar() {
        when(produtoPort.buscarPorCodigoPrincipal("999")).thenReturn(Optional.empty());
        when(similarPort.buscarCodigoPrincipal("999")).thenReturn(Optional.of("001234"));
        when(produtoPort.buscarPorCodigoPrincipal("001234")).thenReturn(Optional.of(produto()));
        assertThat(service.executar("999").id()).isEqualTo(1L);
    }

    @Test
    void deveFalharQuandoCodigoOuProdutoDoSimilarNaoExistir() {
        when(produtoPort.buscarPorCodigoPrincipal("999")).thenReturn(Optional.empty());
        when(similarPort.buscarCodigoPrincipal("999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.executar("999")).isInstanceOf(ProdutoNaoEncontradoException.class);

        when(similarPort.buscarCodigoPrincipal("999")).thenReturn(Optional.of("001234"));
        when(produtoPort.buscarPorCodigoPrincipal("001234")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.executar("999")).isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "12A", "12345678901234"})
    void deveRejeitarCodigoInvalido(String codigo) {
        assertThatThrownBy(() -> service.executar(codigo)).isInstanceOf(CodigoBarrasInvalidoException.class);
    }

    private static Produto produto() {
        return new Produto(1L, "001234", "Produto", new BigDecimal("9.50"));
    }
}

