package br.com.mercado.precificacao.adapter.input.mapper;

import br.com.mercado.precificacao.adapter.input.response.ProdutoResponse;
import br.com.mercado.precificacao.core.model.ProdutoResult;

public final class ProdutoInputMapper {
    public ProdutoResponse toResponse(ProdutoResult result) {
        return new ProdutoResponse(result.id(), result.codigoBarras(), result.descricao(), result.precoVenda());
    }
}

