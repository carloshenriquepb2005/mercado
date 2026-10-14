package br.com.mercado.precificacao.core.mapper;

import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.core.model.ProdutoResult;

public final class ProdutoMapper {
    public ProdutoResult toResult(Produto produto) {
        return new ProdutoResult(produto.getId(), produto.getCodigoBarras(), produto.getDescricao(),
                produto.getPrecoVenda());
    }
}
