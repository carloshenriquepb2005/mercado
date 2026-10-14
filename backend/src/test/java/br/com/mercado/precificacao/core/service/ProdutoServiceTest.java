package br.com.mercado.precificacao.core.service;

import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoServiceTest {
    private final ProdutoService service = new ProdutoService();

    @Test
    void deveNormalizarPrecoValidoParaDuasCasas() {
        // Act
        BigDecimal preco = service.validarNovoPreco(new BigDecimal("10.5"));

        // Assert
        assertThat(preco).isEqualByComparingTo("10.50");
        assertThat(preco.scale()).isEqualTo(2);
    }

    @Test
    void deveRejeitarPrecoNulo() {
        // Act / Assert
        assertThatThrownBy(() -> service.validarNovoPreco(null))
                .isInstanceOf(PrecoInvalidoException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.001", "100000000"})
    void deveRejeitarPrecoForaDasRegras(String valor) {
        // Arrange
        BigDecimal preco = new BigDecimal(valor);

        // Act / Assert
        assertThatThrownBy(() -> service.validarNovoPreco(preco))
                .isInstanceOf(PrecoInvalidoException.class);
    }
}
