package br.com.mercado.precificacao.adapter.input.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarEntradaEstoqueRequest(
        @NotNull(message = "A quantidade é obrigatória.")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero.")
        @DecimalMax(value = "9999999.999", message = "A quantidade excede o limite permitido.")
        @Digits(integer = 7, fraction = 3,
                message = "A quantidade deve ter até sete dígitos inteiros e três casas decimais.")
        BigDecimal quantidade,

        @NotNull(message = "O preço de custo é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero.")
        @DecimalMax(value = "99999999.99", message = "O preço de custo excede o limite permitido.")
        @Digits(integer = 8, fraction = 2,
                message = "O preço de custo deve ter até oito dígitos inteiros e duas casas decimais.")
        BigDecimal precoCusto,

        LocalDate dataVencimento
) {
}
