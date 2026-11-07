package br.com.mercado.precificacao.core.service;

import br.com.mercado.precificacao.core.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.core.exception.QuantidadeInvalidaException;
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

    @Test
    void deveNormalizarPrecoDeCustoValidoParaDuasCasas() {
        // Act
        BigDecimal custo = service.validarPrecoCusto(new BigDecimal("7.5"));

        // Assert
        assertThat(custo).isEqualByComparingTo("7.50");
        assertThat(custo.scale()).isEqualTo(2);
    }

    @Test
    void deveRejeitarPrecoDeCustoNuloComMensagemEspecifica() {
        // Act / Assert
        assertThatThrownBy(() -> service.validarPrecoCusto(null))
                .isInstanceOf(PrecoInvalidoException.class)
                .hasMessage("O preço de custo é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.001", "100000000"})
    void deveRejeitarPrecoDeCustoForaDasRegras(String valor) {
        // Arrange
        BigDecimal custo = new BigDecimal(valor);

        // Act / Assert
        assertThatThrownBy(() -> service.validarPrecoCusto(custo))
                .isInstanceOf(PrecoInvalidoException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "0.001", "12.5", "9999999.999"})
    void deveNormalizarQuantidadeValidaParaTresCasas(String valor) {
        // Act
        BigDecimal quantidade = service.validarQuantidadeEntrada(new BigDecimal(valor));

        // Assert
        assertThat(quantidade).isEqualByComparingTo(valor);
        assertThat(quantidade.scale()).isEqualTo(3);
    }

    @Test
    void deveRejeitarQuantidadeNula() {
        // Act / Assert
        assertThatThrownBy(() -> service.validarQuantidadeEntrada(null))
                .isInstanceOf(QuantidadeInvalidaException.class)
                .hasMessage("A quantidade é obrigatória.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.0001", "1.2345", "10000000", "9999999.9991"})
    void deveRejeitarQuantidadeForaDasRegras(String valor) {
        // Arrange
        BigDecimal quantidade = new BigDecimal(valor);

        // Act / Assert
        assertThatThrownBy(() -> service.validarQuantidadeEntrada(quantidade))
                .isInstanceOf(QuantidadeInvalidaException.class);
    }

    @Test
    void deveAceitarCasasDecimaisExcedentesQuandoSaoZeros() {
        // Act
        BigDecimal quantidade = service.validarQuantidadeEntrada(new BigDecimal("2.50000"));

        // Assert
        assertThat(quantidade).isEqualByComparingTo("2.500");
    }

    @Test
    void deveExporLimiteDeEstoqueDaColunaDecimal10Por3() {
        assertThat(service.limiteEstoque()).isEqualByComparingTo("9999999.999");
    }
}
