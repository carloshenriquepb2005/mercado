package br.com.mercado.precificacao.core.mapper;

import br.com.mercado.precificacao.adapter.input.mapper.ProdutoInputMapper;
import br.com.mercado.precificacao.adapter.output.persistence.entity.ProdutoJpaEntity;
import br.com.mercado.precificacao.adapter.output.persistence.mapper.ProdutoPersistenceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoMapperTest {
    @Test
    void deveMapearPersistenciaCoreERespostaSemPrecoDeCusto() {
        // Arrange
        ProdutoJpaEntity entity = new ProdutoJpaEntity(1, "123", "Produto", new BigDecimal("9.50"));

        // Act
        var produto = new ProdutoPersistenceMapper().toDomain(entity);
        var result = new ProdutoMapper().toResult(produto);
        var response = new ProdutoInputMapper().toResponse(result);

        // Assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.codigoBarras()).isEqualTo("123");
        assertThat(response.precoVenda()).isEqualByComparingTo("9.50");
        assertThat(response.getClass().getDeclaredFields()).extracting("name")
                .doesNotContain("precoCusto");
    }
}
