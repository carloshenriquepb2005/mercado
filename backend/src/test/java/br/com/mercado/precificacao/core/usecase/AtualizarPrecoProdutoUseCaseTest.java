package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.port.output.AtualizarPrecoProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarPrecoProdutoUseCaseTest {
    private BuscarProdutoOutputPort buscarProdutoOutputPort;
    private AtualizarPrecoProdutoOutputPort atualizarPrecoProdutoOutputPort;
    private AtualizarPrecoProdutoUseCase useCase;

    @BeforeEach
    void setUp() {
        buscarProdutoOutputPort = mock(BuscarProdutoOutputPort.class);
        atualizarPrecoProdutoOutputPort = mock(AtualizarPrecoProdutoOutputPort.class);
        useCase = new AtualizarPrecoProdutoUseCase(
                buscarProdutoOutputPort, atualizarPrecoProdutoOutputPort, new ProdutoService());
    }

    @Test
    void deveAtualizarComIdEPrecoCorretos() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto()));
        when(atualizarPrecoProdutoOutputPort.atualizarPreco(15L, new BigDecimal("10.50")))
                .thenReturn(true);

        // Act
        useCase.executar(15L, new BigDecimal("10.5"));

        // Assert
        verify(atualizarPrecoProdutoOutputPort).atualizarPreco(15L, new BigDecimal("10.50"));
    }

    @Test
    void naoDeveAtualizarProdutoInexistente() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(15L, BigDecimal.ONE))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        verify(atualizarPrecoProdutoOutputPort, never()).atualizarPreco(15L, BigDecimal.ONE);
    }

    @Test
    void deveFalharQuandoAtualizacaoNaoAlterarNenhumaLinha() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(15L)).thenReturn(Optional.of(produto()));
        when(atualizarPrecoProdutoOutputPort.atualizarPreco(15L, BigDecimal.ONE)).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(15L, BigDecimal.ONE))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deveRejeitarIdOuPrecoInvalido() {
        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(0L, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar(1L, null))
                .isInstanceOf(PrecoInvalidoException.class);
    }

    @Test
    void deveRejeitarPrecoInvalidoSemAtualizar() {
        // Arrange
        when(buscarProdutoOutputPort.buscarPorId(1L)).thenReturn(Optional.of(produto()));

        // Act / Assert
        assertThatThrownBy(() -> useCase.executar(1L, BigDecimal.ZERO))
                .isInstanceOf(PrecoInvalidoException.class);
        verify(atualizarPrecoProdutoOutputPort, never()).atualizarPreco(1L, BigDecimal.ZERO);
    }

    private static Produto produto() {
        return new Produto(15L, "123", "Produto", "UN", new BigDecimal("9.50"), "12345678",
                new BigDecimal("10.000"));
    }
}
