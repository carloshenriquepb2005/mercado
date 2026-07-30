package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.AtualizacaoPrecoCommand;
import br.com.mercado.precificacao.application.port.out.AtualizarPrecoProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.domain.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.domain.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarPrecoProdutoServiceTest {
    private BuscarProdutoPort buscarPort;
    private AtualizarPrecoProdutoPort atualizarPort;
    private AtualizarPrecoProdutoService service;

    @BeforeEach
    void setUp() {
        buscarPort = mock(BuscarProdutoPort.class);
        atualizarPort = mock(AtualizarPrecoProdutoPort.class);
        service = new AtualizarPrecoProdutoService(buscarPort, atualizarPort);
    }

    @Test
    void deveAtualizarComIdEPrecoCorretos() {
        when(buscarPort.buscarPorId(15L)).thenReturn(Optional.of(produto()));
        service.executar(15L, new AtualizacaoPrecoCommand(new BigDecimal("10.5")));
        verify(atualizarPort).atualizarPreco(15L, new BigDecimal("10.50"));
    }

    @Test
    void naoDeveAtualizarProdutoInexistente() {
        when(buscarPort.buscarPorId(15L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.executar(15L, new AtualizacaoPrecoCommand(BigDecimal.ONE)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        verify(atualizarPort, never()).atualizarPreco(15L, BigDecimal.ONE);
    }

    @Test
    void deveRejeitarIdCommandOuPrecoInvalido() {
        assertThatThrownBy(() -> service.executar(0L, new AtualizacaoPrecoCommand(BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.executar(1L, null)).isInstanceOf(PrecoInvalidoException.class);
        when(buscarPort.buscarPorId(1L)).thenReturn(Optional.of(produto()));
        assertThatThrownBy(() -> service.executar(1L, new AtualizacaoPrecoCommand(null)))
                .isInstanceOf(PrecoInvalidoException.class);
        assertThatThrownBy(() -> service.executar(1L, new AtualizacaoPrecoCommand(BigDecimal.ZERO)))
                .isInstanceOf(PrecoInvalidoException.class);
        verify(atualizarPort, never()).atualizarPreco(1L, BigDecimal.ZERO);
    }

    private static Produto produto() {
        return new Produto(15L, "123", "Produto", new BigDecimal("9.50"));
    }
}

