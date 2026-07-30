package br.com.mercado.precificacao.adapter.in.web.controller;

import br.com.mercado.precificacao.adapter.in.web.mapper.ProdutoWebMapper;
import br.com.mercado.precificacao.adapter.in.web.request.AtualizarPrecoRequest;
import br.com.mercado.precificacao.adapter.in.web.response.ApiErrorResponse;
import br.com.mercado.precificacao.adapter.in.web.response.ProdutoResponse;
import br.com.mercado.precificacao.application.dto.AtualizacaoPrecoCommand;
import br.com.mercado.precificacao.application.port.in.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.application.port.in.BuscarProdutoPorCodigoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final BuscarProdutoPorCodigoUseCase buscarProduto;
    private final AtualizarPrecoProdutoUseCase atualizarPreco;
    private final ProdutoWebMapper mapper;

    public ProdutoController(BuscarProdutoPorCodigoUseCase buscarProduto,
                             AtualizarPrecoProdutoUseCase atualizarPreco,
                             ProdutoWebMapper mapper) {
        this.buscarProduto = buscarProduto;
        this.atualizarPreco = atualizarPreco;
        this.mapper = mapper;
    }

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
    @GetMapping("/{ean}")
    public ProdutoResponse buscar(@PathVariable String ean) {
        return mapper.toResponse(buscarProduto.executar(ean));
    }

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
    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable @Positive(message = "O ID do produto deve ser positivo.") Long id,
            @Valid @RequestBody AtualizarPrecoRequest request) {
        atualizarPreco.executar(id, new AtualizacaoPrecoCommand(request.novoPreco()));
        return ResponseEntity.noContent().build();
    }
}

