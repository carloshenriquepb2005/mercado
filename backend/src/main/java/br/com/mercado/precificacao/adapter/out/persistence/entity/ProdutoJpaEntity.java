package br.com.mercado.precificacao.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class ProdutoJpaEntity {
    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "CODIGO_PRODUTO", length = 13, nullable = false)
    private String codigoProduto;

    @Column(name = "DESCRICAO", length = 60, nullable = false)
    private String descricao;

    @Column(name = "PRECO_UNITARIO", precision = 10, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    protected ProdutoJpaEntity() {
    }

    public ProdutoJpaEntity(Integer id, String codigoProduto, String descricao, BigDecimal precoUnitario) {
        this.id = id;
        this.codigoProduto = codigoProduto;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
    }

    public Integer getId() { return id; }
    public String getCodigoProduto() { return codigoProduto; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}

