package br.com.mercado.precificacao.application.dto;

import java.math.BigDecimal;

public record ProdutoResult(Long id, String codigoBarras, String descricao, BigDecimal precoVenda) {
}

