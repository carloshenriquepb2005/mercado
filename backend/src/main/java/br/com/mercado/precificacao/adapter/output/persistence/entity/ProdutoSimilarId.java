package br.com.mercado.precificacao.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProdutoSimilarId implements Serializable {
    @Column(name = "CODIGO_SIMILAR", length = 13, nullable = false)
    private String codigoSimilar;

    @Column(name = "CODIGO_PRODUTO", length = 13, nullable = false)
    private String codigoProduto;

    protected ProdutoSimilarId() {
    }

    public ProdutoSimilarId(String codigoSimilar, String codigoProduto) {
        this.codigoSimilar = codigoSimilar;
        this.codigoProduto = codigoProduto;
    }

    public String getCodigoSimilar() { return codigoSimilar; }
    public String getCodigoProduto() { return codigoProduto; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ProdutoSimilarId other)) return false;
        return Objects.equals(codigoSimilar, other.codigoSimilar)
                && Objects.equals(codigoProduto, other.codigoProduto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoSimilar, codigoProduto);
    }
}

