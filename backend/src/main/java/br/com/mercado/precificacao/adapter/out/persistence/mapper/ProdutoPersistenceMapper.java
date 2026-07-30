package br.com.mercado.precificacao.adapter.out.persistence.mapper;

import br.com.mercado.precificacao.adapter.out.persistence.entity.ProdutoJpaEntity;
import br.com.mercado.precificacao.domain.model.Produto;

public final class ProdutoPersistenceMapper {
    public Produto toDomain(ProdutoJpaEntity entity) {
        return new Produto(entity.getId().longValue(), entity.getCodigoProduto(), entity.getDescricao(),
                entity.getPrecoUnitario());
    }
}

