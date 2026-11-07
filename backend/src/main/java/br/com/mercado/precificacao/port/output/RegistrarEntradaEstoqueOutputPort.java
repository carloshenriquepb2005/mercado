package br.com.mercado.precificacao.port.output;

import br.com.mercado.precificacao.core.model.NovaEntradaEstoque;

public interface RegistrarEntradaEstoqueOutputPort {
    /**
     * Soma a quantidade ao estoque e grava custo e data de atualização de forma atômica.
     *
     * @return {@code false} quando nenhuma linha foi alterada (produto inexistente ou limite excedido)
     */
    boolean registrarEntrada(Long produtoId, NovaEntradaEstoque entrada);
}
