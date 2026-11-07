package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;

public record EntradaEstoque(
        Long produtoId,
        BigDecimal estoqueAnterior,
        BigDecimal estoqueAtual,
        BigDecimal precoCustoAnterior,
        BigDecimal precoCustoAtual
) {
}
