package br.com.mercado.precificacao.adapter.in.web.mapper;

import br.com.mercado.precificacao.adapter.in.web.response.ProdutoResponse;
import br.com.mercado.precificacao.application.dto.ProdutoResult;

public final class ProdutoWebMapper {
    public ProdutoResponse toResponse(ProdutoResult result) {
        return new ProdutoResponse(result.id(), result.codigoBarras(), result.descricao(), result.precoVenda());
    }
}

