package br.com.mercado.precificacao.domain.model;

import br.com.mercado.precificacao.domain.exception.PrecoInvalidoException;

import java.math.BigDecimal;
import java.util.Objects;

public final class Produto {
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");

    private final Long id;
    private final String codigoBarras;
    private final String descricao;
    private BigDecimal precoVenda;

    public Produto(Long id, String codigoBarras, String descricao, BigDecimal precoVenda) {
        if (id == null || id <= 0 || codigoBarras == null || codigoBarras.isBlank()
                || descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Dados obrigatórios do produto são inválidos.");
        }
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.descricao = descricao;
        this.precoVenda = validarPreco(precoVenda);
    }

    public void alterarPreco(BigDecimal novoPreco) {
        this.precoVenda = validarPreco(novoPreco);
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new PrecoInvalidoException("O novo preço é obrigatório.");
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecoInvalidoException("O novo preço deve ser maior que zero.");
        }
        if (preco.stripTrailingZeros().scale() > 2) {
            throw new PrecoInvalidoException("O novo preço deve ter no máximo duas casas decimais.");
        }
        if (preco.compareTo(PRECO_MAXIMO) > 0) {
            throw new PrecoInvalidoException("O novo preço excede o limite permitido.");
        }
        return preco.setScale(2);
    }

    public Long getId() { return id; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoVenda() { return precoVenda; }

    @Override
    public boolean equals(Object object) {
        return object instanceof Produto produto && Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

