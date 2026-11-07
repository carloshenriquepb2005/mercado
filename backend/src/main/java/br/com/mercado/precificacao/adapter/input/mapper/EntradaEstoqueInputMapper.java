package br.com.mercado.precificacao.adapter.input.mapper;

import br.com.mercado.precificacao.adapter.input.response.EntradaEstoqueResponse;
import br.com.mercado.precificacao.core.model.EntradaEstoque;

public final class EntradaEstoqueInputMapper {
    public EntradaEstoqueResponse toResponse(EntradaEstoque entrada) {
        return new EntradaEstoqueResponse(entrada.produtoId(), entrada.estoqueAnterior(),
                entrada.estoqueAtual(), entrada.precoCustoAnterior(), entrada.precoCustoAtual());
    }
}
