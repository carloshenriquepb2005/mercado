package br.com.mercado.precificacao.port.input;

import java.math.BigDecimal;

public interface AtualizarPrecoProdutoInputPort {
    void executar(Long produtoId, BigDecimal novoPreco);
}

