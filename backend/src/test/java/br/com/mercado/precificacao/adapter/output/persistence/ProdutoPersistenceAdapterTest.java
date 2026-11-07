package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.entity.ProdutoJpaEntity;
import br.com.mercado.precificacao.adapter.output.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.adapter.output.persistence.repository.ProdutoJpaRepository;
import br.com.mercado.precificacao.adapter.output.persistence.repository.ProdutoSimilarJpaRepository;
import br.com.mercado.precificacao.core.model.NovaEntradaEstoque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoPersistenceAdapterTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 5);
    private static final BigDecimal LIMITE = new BigDecimal("9999999.999");

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
    void deveInformarSeAlgumaLinhaFoiAtualizada() {
        when(produtoRepository.atualizarPrecoPorId(1, new BigDecimal("10.00"))).thenReturn(1);
        assertThat(adapter.atualizarPreco(1L, new BigDecimal("10.00"))).isTrue();

        when(produtoRepository.atualizarPrecoPorId(2, new BigDecimal("10.00"))).thenReturn(0);
        assertThat(adapter.atualizarPreco(2L, new BigDecimal("10.00"))).isFalse();
    }

    @Test
    void deveMapearPrecoDeCustoIncluindoNulo() {
        ProdutoJpaEntity comCusto = entity();
        ProdutoJpaEntity semCusto = new ProdutoJpaEntity(2, "456", "Outro", null, null, null, null, null);
        when(produtoRepository.findById(1)).thenReturn(Optional.of(comCusto));
        when(produtoRepository.findById(2)).thenReturn(Optional.of(semCusto));

        assertThat(adapter.buscarPorId(1L)).get().extracting("precoCusto").isEqualTo(new BigDecimal("6.90"));
        assertThat(adapter.buscarPorId(2L)).get().extracting("precoCusto").isNull();
    }

    @Test
    void deveUsarQuerySemVencimentoQuandoDataNaoInformada() {
        // Arrange
        NovaEntradaEstoque entrada = entrada(null);
        when(produtoRepository.registrarEntradaEstoque(1, new BigDecimal("2.500"), new BigDecimal("7.35"),
                HOJE, LIMITE)).thenReturn(1);

        // Act
        boolean registrada = adapter.registrarEntrada(1L, entrada);

        // Assert
        assertThat(registrada).isTrue();
        verify(produtoRepository).registrarEntradaEstoque(1, new BigDecimal("2.500"), new BigDecimal("7.35"),
                HOJE, LIMITE);
        verify(produtoRepository, never()).registrarEntradaEstoqueComVencimento(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveUsarQueryComVencimentoQuandoDataInformada() {
        // Arrange
        LocalDate vencimento = LocalDate.of(2027, 3, 15);
        when(produtoRepository.registrarEntradaEstoqueComVencimento(1, new BigDecimal("2.500"),
                new BigDecimal("7.35"), HOJE, vencimento, LIMITE)).thenReturn(1);

        // Act
        boolean registrada = adapter.registrarEntrada(1L, entrada(vencimento));

        // Assert
        assertThat(registrada).isTrue();
        verify(produtoRepository, never()).registrarEntradaEstoque(any(), any(), any(), any(), any());
    }

    @Test
    void deveInformarFalsoQuandoNenhumaLinhaForAlteradaNaEntrada() {
        when(produtoRepository.registrarEntradaEstoque(any(), any(), any(), any(), any())).thenReturn(0);
        when(produtoRepository.registrarEntradaEstoqueComVencimento(any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        assertThat(adapter.registrarEntrada(1L, entrada(null))).isFalse();
        assertThat(adapter.registrarEntrada(1L, entrada(LocalDate.of(2027, 3, 15)))).isFalse();
    }

    private static NovaEntradaEstoque entrada(LocalDate vencimento) {
        return new NovaEntradaEstoque(new BigDecimal("2.500"), new BigDecimal("7.35"), HOJE, vencimento, LIMITE);
    }

    private static ProdutoJpaEntity entity() {
        return new ProdutoJpaEntity(1, "123", "Produto", "UN",
                new BigDecimal("9.50"), new BigDecimal("6.90"), "12345678", new BigDecimal("7.000"));
    }
}

