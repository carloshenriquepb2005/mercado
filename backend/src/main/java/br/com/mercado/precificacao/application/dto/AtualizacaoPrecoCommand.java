package br.com.mercado.precificacao.application.dto;

import java.math.BigDecimal;

public record AtualizacaoPrecoCommand(BigDecimal novoPreco) {
}

