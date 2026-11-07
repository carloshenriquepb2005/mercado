package br.com.mercado.precificacao.core.service;

import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.core.exception.QuantidadeInvalidaException;

import java.math.BigDecimal;

public final class ProdutoService {
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");
    private static final BigDecimal QUANTIDADE_MAXIMA = new BigDecimal("9999999.999");
    private static final int CASAS_QUANTIDADE = 3;

    public BigDecimal limiteEstoque() {
        return QUANTIDADE_MAXIMA;
    }

    public BigDecimal validarNovoPreco(BigDecimal preco) {
        return validarValorMonetario(preco, "O novo preço");
    }

    public BigDecimal validarPrecoCusto(BigDecimal precoCusto) {
        return validarValorMonetario(precoCusto, "O preço de custo");
    }

    public BigDecimal validarQuantidadeEntrada(BigDecimal quantidade) {
        if (quantidade == null) {
            throw new QuantidadeInvalidaException("A quantidade é obrigatória.");
        }
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new QuantidadeInvalidaException("A quantidade deve ser maior que zero.");
        }
        if (quantidade.stripTrailingZeros().scale() > CASAS_QUANTIDADE) {
            throw new QuantidadeInvalidaException("A quantidade deve ter no máximo três casas decimais.");
        }
        if (quantidade.compareTo(QUANTIDADE_MAXIMA) > 0) {
            throw new QuantidadeInvalidaException("A quantidade excede o limite permitido.");
        }
        return quantidade.setScale(CASAS_QUANTIDADE);
    }

    private BigDecimal validarValorMonetario(BigDecimal valor, String rotulo) {
        if (valor == null) {
            throw new PrecoInvalidoException(rotulo + " é obrigatório.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecoInvalidoException(rotulo + " deve ser maior que zero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new PrecoInvalidoException(rotulo + " deve ter no máximo duas casas decimais.");
        }
        if (valor.compareTo(PRECO_MAXIMO) > 0) {
            throw new PrecoInvalidoException(rotulo + " excede o limite permitido.");
        }
        return valor.setScale(2);
    }
}
