package br.com.mercado.precificacao.adapter.in.web.response;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
}

