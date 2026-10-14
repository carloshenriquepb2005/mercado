package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.port.input.AtualizarPrecoProdutoInputPort;
import br.com.mercado.precificacao.port.output.AtualizarPrecoProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;

import java.math.BigDecimal;

public final class AtualizarPrecoProdutoUseCase implements AtualizarPrecoProdutoInputPort {
    private final BuscarProdutoOutputPort buscarProdutoOutputPort;
    private final AtualizarPrecoProdutoOutputPort atualizarPrecoProdutoOutputPort;
    private final ProdutoService produtoService;

    public AtualizarPrecoProdutoUseCase(BuscarProdutoOutputPort buscarProdutoOutputPort,
                                        AtualizarPrecoProdutoOutputPort atualizarPrecoProdutoOutputPort,
                                        ProdutoService produtoService) {
        this.buscarProdutoOutputPort = buscarProdutoOutputPort;
        this.atualizarPrecoProdutoOutputPort = atualizarPrecoProdutoOutputPort;
        this.produtoService = produtoService;
    }

    @Override
    public void executar(Long produtoId, BigDecimal novoPreco) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O ID do produto deve ser positivo.");
        }

        BigDecimal precoNormalizado = produtoService.validarNovoPreco(novoPreco);
        buscarProdutoOutputPort.buscarPorId(produtoId)
                .orElseThrow(ProdutoNaoEncontradoException::new);
        boolean atualizado = atualizarPrecoProdutoOutputPort.atualizarPreco(produtoId, precoNormalizado);
        if (!atualizado) {
            throw new ProdutoNaoEncontradoException();
        }
    }
}

