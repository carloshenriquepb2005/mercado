package br.com.mercado.precificacao.adapter.input.controller;

import br.com.mercado.precificacao.adapter.input.mapper.EntradaEstoqueInputMapper;
import br.com.mercado.precificacao.adapter.input.request.RegistrarEntradaEstoqueRequest;
import br.com.mercado.precificacao.adapter.input.response.EntradaEstoqueResponse;
import br.com.mercado.precificacao.adapter.input.swagger.EntradaEstoqueControllerSwagger;
import br.com.mercado.precificacao.port.input.RegistrarEntradaEstoqueInputPort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/produtos")
public class EntradaEstoqueController implements EntradaEstoqueControllerSwagger {
    private final RegistrarEntradaEstoqueInputPort registrarEntradaInputPort;
    private final EntradaEstoqueInputMapper entradaEstoqueInputMapper;

    public EntradaEstoqueController(RegistrarEntradaEstoqueInputPort registrarEntradaInputPort,
                                    EntradaEstoqueInputMapper entradaEstoqueInputMapper) {
        this.registrarEntradaInputPort = registrarEntradaInputPort;
        this.entradaEstoqueInputMapper = entradaEstoqueInputMapper;
    }

    @Override
    @PostMapping("/{id}/entrada-estoque")
    public EntradaEstoqueResponse registrar(
            @PathVariable Long id,
            @RequestBody RegistrarEntradaEstoqueRequest request) {
        return entradaEstoqueInputMapper.toResponse(registrarEntradaInputPort.executar(
                id, request.quantidade(), request.precoCusto(), request.dataVencimento()));
    }
}
