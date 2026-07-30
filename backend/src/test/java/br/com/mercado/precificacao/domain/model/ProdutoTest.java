package br.com.mercado.precificacao.domain.model;

import br.com.mercado.precificacao.domain.exception.PrecoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {
    @Test
    void deveCriarProdutoValido() {
        Produto produto = produto("9.50");
        assertThat(produto.getId()).isEqualTo(1L);
        assertThat(produto.getPrecoVenda()).isEqualByComparingTo("9.50");
    }

    @Test
    void deveAlterarSomenteOPreco() {
        Produto produto = produto("9.50");
        produto.alterarPreco(new BigDecimal("10.5"));
        assertThat(produto.getPrecoVenda()).isEqualByComparingTo("10.50");
        assertThat(produto.getCodigoBarras()).isEqualTo("001234");
        assertThat(produto.getDescricao()).isEqualTo("Produto");
    }

    @Test
    void deveRejeitarPrecoNuloZeroNegativoFracionadoOuAcimaDoLimite() {
        Produto produto = produto("9.50");
        for (BigDecimal valor : new BigDecimal[]{BigDecimal.ZERO, new BigDecimal("-1"),
                new BigDecimal("1.001"), new BigDecimal("100000000")}) {
            assertThatThrownBy(() -> produto.alterarPreco(valor)).isInstanceOf(PrecoInvalidoException.class);
        }
        assertThatThrownBy(() -> produto.alterarPreco(null)).isInstanceOf(PrecoInvalidoException.class);
    }

    @Test
    void deveRejeitarCamposEstruturaisInvalidos() {
        assertThatThrownBy(() -> new Produto(null, "1", "Produto", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produto(1L, " ", "Produto", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produto(1L, "1", null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Produto produto(String preco) {
        return new Produto(1L, "001234", "Produto", new BigDecimal(preco));
    }
}

