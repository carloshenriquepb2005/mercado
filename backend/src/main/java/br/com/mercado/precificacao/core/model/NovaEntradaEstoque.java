package br.com.mercado.precificacao.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados já validados de uma entrada no estoque, prontos para gravação.
 *
 * @param dataVencimento nula quando o usuário não informou a data (a coluna não deve ser alterada)
 */
public record NovaEntradaEstoque(
        BigDecimal quantidade,
        BigDecimal precoCusto,
        LocalDate dataAtualizacao,
        LocalDate dataVencimento,
        BigDecimal limiteEstoque
) {
}
