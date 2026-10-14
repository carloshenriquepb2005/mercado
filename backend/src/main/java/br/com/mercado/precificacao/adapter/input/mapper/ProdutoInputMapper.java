package br.com.mercado.precificacao.adapter.input.mapper;

import br.com.mercado.precificacao.adapter.input.response.ProdutoResponse;
import br.com.mercado.precificacao.core.model.Produto;

public final class ProdutoInputMapper {
    public ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getCodigoBarras(), produto.getDescricao(),
                produto.getUnidade(), produto.getPrecoVenda(), produto.getCodigoNcm(), produto.getEstoqueAtual());
    }
}

