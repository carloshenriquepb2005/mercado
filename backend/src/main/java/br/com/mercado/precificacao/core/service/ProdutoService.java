package br.com.mercado.precificacao.core.service;

import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;

import java.math.BigDecimal;

public final class ProdutoService {
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");

    public BigDecimal validarNovoPreco(BigDecimal preco) {
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
}
