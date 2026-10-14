package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;

public record ProdutoResult(Long id, String codigoBarras, String descricao, BigDecimal precoVenda) {
}

