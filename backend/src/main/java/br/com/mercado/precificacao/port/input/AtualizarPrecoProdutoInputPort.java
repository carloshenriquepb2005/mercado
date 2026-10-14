package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.AtualizacaoPrecoCommand;

public interface AtualizarPrecoProdutoInputPort {
    void executar(Long produtoId, AtualizacaoPrecoCommand command);
}

