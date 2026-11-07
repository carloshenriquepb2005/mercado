package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.EstoqueLimiteExcedidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.model.EntradaEstoque;
import br.com.mercado.precificacao.core.model.NovaEntradaEstoque;
import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.port.input.RegistrarEntradaEstoqueInputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.RegistrarEntradaEstoqueOutputPort;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

public final class RegistrarEntradaEstoqueUseCase implements RegistrarEntradaEstoqueInputPort {
    private final BuscarProdutoOutputPort buscarProdutoOutputPort;
    private final RegistrarEntradaEstoqueOutputPort registrarEntradaEstoqueOutputPort;
    private final ProdutoService produtoService;
    private final Clock clock;

    public RegistrarEntradaEstoqueUseCase(BuscarProdutoOutputPort buscarProdutoOutputPort,
                                          RegistrarEntradaEstoqueOutputPort registrarEntradaEstoqueOutputPort,
                                          ProdutoService produtoService,
                                          Clock clock) {
        this.buscarProdutoOutputPort = buscarProdutoOutputPort;
        this.registrarEntradaEstoqueOutputPort = registrarEntradaEstoqueOutputPort;
        this.produtoService = produtoService;
        this.clock = clock;
    }

    @Override
    public EntradaEstoque executar(Long produtoId, BigDecimal quantidade, BigDecimal precoCusto,
                                   LocalDate dataVencimento) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O ID do produto deve ser positivo.");
        }

        NovaEntradaEstoque entrada = new NovaEntradaEstoque(
                produtoService.validarQuantidadeEntrada(quantidade),
                produtoService.validarPrecoCusto(precoCusto),
                LocalDate.now(clock),
                dataVencimento,
                produtoService.limiteEstoque());

        Produto anterior = buscarProdutoOutputPort.buscarPorId(produtoId)
                .orElseThrow(ProdutoNaoEncontradoException::new);

        if (!registrarEntradaEstoqueOutputPort.registrarEntrada(produtoId, entrada)) {
            throw falhaSemLinhaAlterada(produtoId);
        }

        Produto atualizado = buscarProdutoOutputPort.buscarPorId(produtoId)
                .orElseThrow(ProdutoNaoEncontradoException::new);
        return new EntradaEstoque(produtoId, estoqueOuZero(anterior), estoqueOuZero(atualizado),
                anterior.getPrecoCusto(), atualizado.getPrecoCusto());
    }

    private RuntimeException falhaSemLinhaAlterada(Long produtoId) {
        return buscarProdutoOutputPort.buscarPorId(produtoId).isPresent()
                ? new EstoqueLimiteExcedidoException()
                : new ProdutoNaoEncontradoException();
    }

    private static BigDecimal estoqueOuZero(Produto produto) {
        return produto.getEstoqueAtual() == null ? BigDecimal.ZERO : produto.getEstoqueAtual();
    }
}
