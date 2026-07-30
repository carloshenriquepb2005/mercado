package br.com.mercado.precificacao.application.port.in;

import br.com.mercado.precificacao.application.dto.AtualizacaoPrecoCommand;

public interface AtualizarPrecoProdutoUseCase {
    void executar(Long produtoId, AtualizacaoPrecoCommand command);
}

