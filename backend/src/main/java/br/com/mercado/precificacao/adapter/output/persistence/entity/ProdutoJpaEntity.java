package br.com.mercado.precificacao.adapter.output.persistence.entity;

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

    @Column(name = "UN")
    private String unidade;

    @Column(name = "PRECO_UNITARIO", precision = 10, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @Column(name = "CODIGO_NCM")
    private String codigoNcm;

    @Column(name = "ESTOQUE_ATUAL")
    private BigDecimal estoqueAtual;

    protected ProdutoJpaEntity() {
    }

    public ProdutoJpaEntity(Integer id, String codigoProduto, String descricao, String unidade,
                            BigDecimal precoUnitario, String codigoNcm, BigDecimal estoqueAtual) {
        this.id = id;
        this.codigoProduto = codigoProduto;
        this.descricao = descricao;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.codigoNcm = codigoNcm;
        this.estoqueAtual = estoqueAtual;
    }

    public Integer getId() { return id; }
    public String getCodigoProduto() { return codigoProduto; }
    public String getDescricao() { return descricao; }
    public String getUnidade() { return unidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public String getCodigoNcm() { return codigoNcm; }
    public BigDecimal getEstoqueAtual() { return estoqueAtual; }
}

