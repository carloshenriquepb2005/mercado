package br.com.mercado.precificacao.port.output;

import java.math.BigDecimal;

public interface AtualizarPrecoProdutoOutputPort {
    boolean atualizarPreco(Long produtoId, BigDecimal novoPreco);
}
