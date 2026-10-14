package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoSimilarOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BuscarProdutoPorCodigoUseCaseTest {
    private BuscarProdutoOutputPort buscarProdutoOutputPort;
    private BuscarProdutoSimilarOutputPort buscarProdutoSimilarOutputPort;
    private BuscarProdutoPorCodigoUseCase useCase;

    @BeforeEach
    void setUp() {
        buscarProdutoOutputPort = mock(BuscarProdutoOutputPort.class);
        buscarProdutoSimilarOutputPort = mock(BuscarProdutoSimilarOutputPort.class);
        useCase = new BuscarProdutoPorCodigoUseCase(
                buscarProdutoOutputPort, buscarProdutoSimilarOutputPort);
    }

    @Test
    void deveRetornarPrincipalSemConsultarSimilar() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorCodigoPrincipal("001234"))
                .thenReturn(Optional.of(produto()));

        // Act
        Produto result = useCase.executar(" 001234 ");

        // Assert
        assertThat(result.getCodigoBarras()).isEqualTo("001234");
        verify(buscarProdutoSimilarOutputPort, never()).buscarCodigoPrincipal("001234");
    }

    @Test
    void deveResolverCodigoSimilar() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorCodigoPrincipal("999")).thenReturn(Optional.empty());
        when(buscarProdutoSimilarOutputPort.buscarCodigoPrincipal("999"))
                .thenReturn(Optional.of("001234"));
        when(buscarProdutoOutputPort.buscarPorCodigoPrincipal("001234"))
                .thenReturn(Optional.of(produto()));

        // Act
        Produto result = useCase.executar("999");

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deveFalharQuandoCodigoOuProdutoDoSimilarNaoExistir() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorCodigoPrincipal("999")).thenReturn(Optional.empty());
        when(buscarProdutoSimilarOutputPort.buscarCodigoPrincipal("999")).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar("999"))
                .isInstanceOf(ProdutoNaoEncontradoException.class);

        // Arrange
        when(buscarProdutoSimilarOutputPort.buscarCodigoPrincipal("999"))
                .thenReturn(Optional.of("001234"));
        when(buscarProdutoOutputPort.buscarPorCodigoPrincipal("001234")).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar("999"))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "12A", "12345678901234"})
    void deveRejeitarCodigoInvalido(String codigo) {
        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(codigo))
                .isInstanceOf(CodigoBarrasInvalidoException.class);
    }

    private static Produto produto() {
        return new Produto(1L, "001234", "Produto", "UN", new BigDecimal("9.50"), "12345678",
                new BigDecimal("10.000"));
    }
}
