package br.com.mercado.precificacao.adapter.input.swagger;

import br.com.mercado.precificacao.adapter.input.request.SenhaCaixaRequest;
import br.com.mercado.precificacao.adapter.input.response.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface ConfiguracaoControllerSwagger {
    @Operation(summary = "Valida a comunicação da API com o banco legado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conexão validada"),
            @ApiResponse(responseCode = "503", description = "Servidor ou banco indisponível",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<Void> testarConexao();

    @Operation(summary = "Valida a senha do caixa para liberar as configurações")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha válida"),
            @ApiResponse(responseCode = "400", description = "Campo inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Senha inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<Void> validarSenhaCaixa(SenhaCaixaRequest request);
}
