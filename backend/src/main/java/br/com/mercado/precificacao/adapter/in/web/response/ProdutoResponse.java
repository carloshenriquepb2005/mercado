package br.com.mercado.precificacao.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProdutoResponse(
        @Schema(example = "150") Long id,
        @Schema(example = "7891010101010") String codigoBarras,
        @Schema(example = "COCA COLA 2L") String descricao,
        @Schema(example = "9.50") BigDecimal precoVenda
) {
}

