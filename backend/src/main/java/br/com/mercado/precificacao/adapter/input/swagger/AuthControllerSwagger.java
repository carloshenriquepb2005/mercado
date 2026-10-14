package br.com.mercado.precificacao.adapter.input.swagger;

import br.com.mercado.precificacao.adapter.input.request.LoginRequest;
import br.com.mercado.precificacao.adapter.input.response.ApiErrorResponse;
import br.com.mercado.precificacao.adapter.input.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface AuthControllerSwagger {
    @Operation(summary = "Valida o acesso do atendente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    LoginResponse login(LoginRequest request);
}
