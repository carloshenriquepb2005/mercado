package br.com.mercado.precificacao.adapter.out.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "produtos_similares")
public class ProdutoSimilarJpaEntity {
    @EmbeddedId
    private ProdutoSimilarId id;

    protected ProdutoSimilarJpaEntity() {
    }

    public ProdutoSimilarJpaEntity(ProdutoSimilarId id) {
        this.id = id;
    }

    public ProdutoSimilarId getId() { return id; }
}

