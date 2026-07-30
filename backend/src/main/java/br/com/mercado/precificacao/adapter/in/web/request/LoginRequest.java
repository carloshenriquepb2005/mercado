package br.com.mercado.precificacao.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o usuário.")
        @Size(max = 80, message = "O usuário deve ter no máximo 80 caracteres.")
        String username,
        @NotBlank(message = "Informe a senha.")
        @Size(max = 120, message = "A senha deve ter no máximo 120 caracteres.")
        String password
) {
}
