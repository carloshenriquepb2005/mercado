package br.com.mercado.precificacao.core.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoTest {
    @Test
    void deveRepresentarDadosDoProdutoSemDependenciasExternas() {
        // Arrange / Act
        Produto produto = new Produto(1L, "001234", "Produto", "KG",
                new BigDecimal("9.50"), "12345678", new BigDecimal("4.250"));

        // Assert
        assertThat(produto.getId()).isEqualTo(1L);
        assertThat(produto.getCodigoBarras()).isEqualTo("001234");
        assertThat(produto.getDescricao()).isEqualTo("Produto");
        assertThat(produto.getUnidade()).isEqualTo("KG");
        assertThat(produto.getPrecoVenda()).isEqualByComparingTo("9.50");
        assertThat(produto.getCodigoNcm()).isEqualTo("12345678");
        assertThat(produto.getEstoqueAtual()).isEqualByComparingTo("4.250");
    }

    @Test
    void deveUsarIdNaIgualdade() {
        // Arrange
        Produto produto = new Produto(1L, "1", "A", "UN", BigDecimal.ONE, "1", BigDecimal.TEN);
        Produto mesmoId = new Produto(1L, "2", "B", "CX", BigDecimal.TEN, "2", BigDecimal.ZERO);
        Produto outroId = new Produto(2L, "1", "A", "UN", BigDecimal.ONE, "1", BigDecimal.TEN);

        // Assert
        assertThat(produto).isEqualTo(produto).isEqualTo(mesmoId).isNotEqualTo(outroId).isNotEqualTo(null);
        assertThat(produto.hashCode()).isEqualTo(mesmoId.hashCode());
    }
}
