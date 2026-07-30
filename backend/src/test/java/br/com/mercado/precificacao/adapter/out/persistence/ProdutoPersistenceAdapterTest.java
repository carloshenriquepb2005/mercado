package br.com.mercado.precificacao.adapter.out.persistence;

import br.com.mercado.precificacao.adapter.out.persistence.entity.ProdutoJpaEntity;
import br.com.mercado.precificacao.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.adapter.out.persistence.repository.ProdutoJpaRepository;
import br.com.mercado.precificacao.adapter.out.persistence.repository.ProdutoSimilarJpaRepository;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProdutoPersistenceAdapterTest {
    private ProdutoJpaRepository produtoRepository;
    private ProdutoSimilarJpaRepository similarRepository;
    private ProdutoPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        produtoRepository = mock(ProdutoJpaRepository.class);
        similarRepository = mock(ProdutoSimilarJpaRepository.class);
        adapter = new ProdutoPersistenceAdapter(produtoRepository, similarRepository,
                new ProdutoPersistenceMapper());
    }

    @Test
    void deveBuscarPrincipalEPorId() {
        ProdutoJpaEntity entity = entity();
        when(produtoRepository.findByCodigoProduto("123")).thenReturn(Optional.of(entity));
        when(produtoRepository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(adapter.buscarPorCodigoPrincipal("123")).get().extracting("descricao").isEqualTo("Produto");
        assertThat(adapter.buscarPorId(1L)).get().extracting("id").isEqualTo(1L);
    }

    @Test
    void deveBuscarSimilar() {
        when(similarRepository.buscarCodigoPrincipal("999")).thenReturn(Optional.of("123"));
        assertThat(adapter.buscarCodigoPrincipal("999")).contains("123");
    }

    @Test
    void deveAtualizarUmaLinhaERejeitarZeroLinhas() {
        when(produtoRepository.atualizarPrecoPorId(1, new BigDecimal("10.00"))).thenReturn(1);
        adapter.atualizarPreco(1L, new BigDecimal("10.00"));

        when(produtoRepository.atualizarPrecoPorId(2, new BigDecimal("10.00"))).thenReturn(0);
        assertThatThrownBy(() -> adapter.atualizarPreco(2L, new BigDecimal("10.00")))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    private static ProdutoJpaEntity entity() {
        return new ProdutoJpaEntity(1, "123", "Produto", new BigDecimal("9.50"));
    }
}

