package br.com.mercado.precificacao.adapter.input.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record EntradaEstoqueResponse(
        @Schema(example = "150") Long produtoId,
        @Schema(example = "18.500") BigDecimal estoqueAnterior,
        @Schema(example = "31.000") BigDecimal estoqueAtual,
        @Schema(example = "6.90", nullable = true) BigDecimal precoCustoAnterior,
        @Schema(example = "7.35") BigDecimal precoCustoAtual
) {
}
