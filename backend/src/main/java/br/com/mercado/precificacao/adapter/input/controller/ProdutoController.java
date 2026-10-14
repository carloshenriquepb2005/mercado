package br.com.mercado.precificacao.adapter.input.controller;

import br.com.mercado.precificacao.adapter.input.mapper.ProdutoInputMapper;
import br.com.mercado.precificacao.adapter.input.request.AtualizarPrecoRequest;
import br.com.mercado.precificacao.adapter.input.response.ProdutoResponse;
import br.com.mercado.precificacao.adapter.input.swagger.ProdutoControllerSwagger;
import br.com.mercado.precificacao.core.model.AtualizacaoPrecoCommand;
import br.com.mercado.precificacao.port.input.AtualizarPrecoProdutoInputPort;
import br.com.mercado.precificacao.port.input.BuscarProdutoPorCodigoInputPort;
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
public class ProdutoController implements ProdutoControllerSwagger {
    private final BuscarProdutoPorCodigoInputPort buscarProdutoInputPort;
    private final AtualizarPrecoProdutoInputPort atualizarPrecoInputPort;
    private final ProdutoInputMapper produtoInputMapper;

    public ProdutoController(BuscarProdutoPorCodigoInputPort buscarProdutoInputPort,
                             AtualizarPrecoProdutoInputPort atualizarPrecoInputPort,
                             ProdutoInputMapper produtoInputMapper) {
        this.buscarProdutoInputPort = buscarProdutoInputPort;
        this.atualizarPrecoInputPort = atualizarPrecoInputPort;
        this.produtoInputMapper = produtoInputMapper;
    }

    @Override
    @GetMapping("/{ean}")
    public ProdutoResponse buscar(@PathVariable String ean) {
        return produtoInputMapper.toResponse(buscarProdutoInputPort.executar(ean));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable @Positive(message = "O ID do produto deve ser positivo.") Long id,
            @Valid @RequestBody AtualizarPrecoRequest request) {
        atualizarPrecoInputPort.executar(id, new AtualizacaoPrecoCommand(request.novoPreco()));
        return ResponseEntity.noContent().build();
    }
}
