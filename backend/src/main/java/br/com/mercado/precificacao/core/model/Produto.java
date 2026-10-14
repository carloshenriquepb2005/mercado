package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Produto {
    private final Long id;
    private final String codigoBarras;
    private final String descricao;
    private final BigDecimal precoVenda;

    public Produto(Long id, String codigoBarras, String descricao, BigDecimal precoVenda) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Produto produto && Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
