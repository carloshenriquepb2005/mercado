package br.com.mercado.precificacao.adapter.input.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AtualizarPrecoRequest(
        @NotNull(message = "O novo preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O novo preço deve ser maior que zero.")
        @DecimalMax(value = "99999999.99", message = "O novo preço excede o limite permitido.")
        @Digits(integer = 8, fraction = 2, message = "O novo preço deve ter até oito dígitos inteiros e duas casas decimais.")
        BigDecimal novoPreco
) {
}

