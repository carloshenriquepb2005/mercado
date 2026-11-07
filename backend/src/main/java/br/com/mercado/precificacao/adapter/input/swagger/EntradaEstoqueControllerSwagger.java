package br.com.mercado.precificacao.adapter.input.swagger;

import br.com.mercado.precificacao.adapter.input.request.RegistrarEntradaEstoqueRequest;
import br.com.mercado.precificacao.adapter.input.response.ApiErrorResponse;
import br.com.mercado.precificacao.adapter.input.response.EntradaEstoqueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

public interface EntradaEstoqueControllerSwagger {
    @Operation(summary = "Registra a entrada no estoque de um produto pelo ID",
            description = "Soma a quantidade ao ESTOQUE_ATUAL e grava PRECO_CUSTO e DATA_ATUALIZACAO "
                    + "em uma única instrução. DATA_VENCIMENTO só é gravada quando informada.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(
                    value = "{\"quantidade\":12.500,\"precoCusto\":7.35,\"dataVencimento\":\"2027-03-15\"}")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "ID, quantidade, custo ou data inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Banco indisponível",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    EntradaEstoqueResponse registrar(
            @Positive(message = "O ID do produto deve ser positivo.") Long id,
            @Valid RegistrarEntradaEstoqueRequest request);
}
