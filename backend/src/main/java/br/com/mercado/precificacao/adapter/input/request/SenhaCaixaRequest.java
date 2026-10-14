package br.com.mercado.precificacao.adapter.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SenhaCaixaRequest(
        @NotBlank(message = "A senha do caixa é obrigatória.")
        @Size(max = 10, message = "A senha do caixa deve possuir no máximo 10 caracteres.")
        String senha) {
}
