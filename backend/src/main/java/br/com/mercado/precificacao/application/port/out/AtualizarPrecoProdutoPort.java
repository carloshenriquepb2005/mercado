package br.com.mercado.precificacao.application.port.out;

import java.math.BigDecimal;

public interface AtualizarPrecoProdutoPort {
    void atualizarPreco(Long produtoId, BigDecimal novoPreco);
}
