package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.AtualizacaoPrecoCommand;
import br.com.mercado.precificacao.application.port.in.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.application.port.out.AtualizarPrecoProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.domain.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.domain.model.Produto;

public final class AtualizarPrecoProdutoService implements AtualizarPrecoProdutoUseCase {
    private final BuscarProdutoPort buscarProdutoPort;
    private final AtualizarPrecoProdutoPort atualizarPrecoProdutoPort;

    public AtualizarPrecoProdutoService(BuscarProdutoPort buscarProdutoPort,
                                        AtualizarPrecoProdutoPort atualizarPrecoProdutoPort) {
        this.buscarProdutoPort = buscarProdutoPort;
        this.atualizarPrecoProdutoPort = atualizarPrecoProdutoPort;
    }

    @Override
    public void executar(Long produtoId, AtualizacaoPrecoCommand command) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O ID do produto deve ser positivo.");
        }
        if (command == null) {
            throw new PrecoInvalidoException("O novo preço é obrigatório.");
        }

        Produto produto = buscarProdutoPort.buscarPorId(produtoId)
                .orElseThrow(ProdutoNaoEncontradoException::new);
        produto.alterarPreco(command.novoPreco());
        atualizarPrecoProdutoPort.atualizarPreco(produtoId, produto.getPrecoVenda());
    }
}

