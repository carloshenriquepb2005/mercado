package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;

public record AtualizacaoPrecoCommand(BigDecimal novoPreco) {
}

