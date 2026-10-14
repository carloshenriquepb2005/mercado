package br.com.mercado.precificacao.adapter.input.swagger;

import br.com.mercado.precificacao.adapter.input.request.AtualizarPrecoRequest;
import br.com.mercado.precificacao.adapter.input.response.ApiErrorResponse;
import br.com.mercado.precificacao.adapter.input.response.ProdutoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;

public interface ProdutoControllerSwagger {
    @Operation(summary = "Busca produto pelo código principal ou similar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "400", description = "Código inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Banco indisponível",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ProdutoResponse buscar(String ean);

    @Operation(summary = "Atualiza somente o preço de venda pelo ID do produto")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = "{\"novoPreco\":10.50}")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Preço atualizado"),
            @ApiResponse(responseCode = "400", description = "ID ou preço inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Banco indisponível",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<Void> atualizar(
            @Positive(message = "O ID do produto deve ser positivo.") Long id,
            @Valid AtualizarPrecoRequest request);
}
