package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Produto {
    private final Long id;
    private final String codigoBarras;
    private final String descricao;
    private final String unidade;
    private final BigDecimal precoVenda;
    private final String codigoNcm;
    private final BigDecimal estoqueAtual;

    public Produto(Long id, String codigoBarras, String descricao, String unidade,
                   BigDecimal precoVenda, String codigoNcm, BigDecimal estoqueAtual) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.descricao = descricao;
        this.unidade = unidade;
        this.precoVenda = precoVenda;
        this.codigoNcm = codigoNcm;
        this.estoqueAtual = estoqueAtual;
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

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public String getCodigoNcm() {
        return codigoNcm;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
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
