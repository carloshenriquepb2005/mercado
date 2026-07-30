package br.com.mercado.precificacao.adapter.out.persistence;

import br.com.mercado.precificacao.adapter.in.web.mapper.ProdutoWebMapper;
import br.com.mercado.precificacao.adapter.out.persistence.entity.ProdutoJpaEntity;
import br.com.mercado.precificacao.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.application.dto.ProdutoResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoMapperTest {
    @Test
    void deveMapearPersistenciaEDtoSemPrecoDeCusto() {
        var domain = new ProdutoPersistenceMapper().toDomain(
                new ProdutoJpaEntity(1, "123", "Produto", new BigDecimal("9.50")));
        var response = new ProdutoWebMapper().toResponse(new ProdutoResult(domain.getId(),
                domain.getCodigoBarras(), domain.getDescricao(), domain.getPrecoVenda()));
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.codigoBarras()).isEqualTo("123");
        assertThat(response.precoVenda()).isEqualByComparingTo("9.50");
        assertThat(response.getClass().getDeclaredFields()).extracting("name")
                .doesNotContain("precoCusto");
    }
}
