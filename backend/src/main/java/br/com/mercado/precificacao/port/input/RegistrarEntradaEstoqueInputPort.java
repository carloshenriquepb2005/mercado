package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.EntradaEstoque;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegistrarEntradaEstoqueInputPort {
    EntradaEstoque executar(Long produtoId, BigDecimal quantidade, BigDecimal precoCusto, LocalDate dataVencimento);
}
