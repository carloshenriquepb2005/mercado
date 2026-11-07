package br.com.mercado.precificacao.adapter.output.persistence.repository;

import br.com.mercado.precificacao.adapter.output.persistence.entity.ProdutoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoJpaEntity, Integer> {
    Optional<ProdutoJpaEntity> findByCodigoProduto(String codigoProduto);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ProdutoJpaEntity p
               set p.precoUnitario = :novoPreco
             where p.id = :id
            """)
    int atualizarPrecoPorId(@Param("id") Integer id, @Param("novoPreco") BigDecimal novoPreco);

    /**
     * Entrada no estoque sem data de vencimento: a coluna DATA_VENCIMENTO não participa do UPDATE.
     * Consultas nativas evitam mapear DATA_ATUALIZACAO/DATA_VENCIMENTO (somente escrita) na entidade.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE produtos
               SET ESTOQUE_ATUAL = COALESCE(ESTOQUE_ATUAL, 0) + :quantidade,
                   PRECO_CUSTO = :precoCusto,
                   DATA_ATUALIZACAO = :dataAtualizacao
             WHERE ID = :id
               AND COALESCE(ESTOQUE_ATUAL, 0) + :quantidade <= :limiteEstoque
            """, nativeQuery = true)
    int registrarEntradaEstoque(@Param("id") Integer id,
                                @Param("quantidade") BigDecimal quantidade,
                                @Param("precoCusto") BigDecimal precoCusto,
                                @Param("dataAtualizacao") LocalDate dataAtualizacao,
                                @Param("limiteEstoque") BigDecimal limiteEstoque);

    /** Entrada no estoque com data de vencimento informada pelo usuário. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE produtos
               SET ESTOQUE_ATUAL = COALESCE(ESTOQUE_ATUAL, 0) + :quantidade,
                   PRECO_CUSTO = :precoCusto,
                   DATA_ATUALIZACAO = :dataAtualizacao,
                   DATA_VENCIMENTO = :dataVencimento
             WHERE ID = :id
               AND COALESCE(ESTOQUE_ATUAL, 0) + :quantidade <= :limiteEstoque
            """, nativeQuery = true)
    int registrarEntradaEstoqueComVencimento(@Param("id") Integer id,
                                             @Param("quantidade") BigDecimal quantidade,
                                             @Param("precoCusto") BigDecimal precoCusto,
                                             @Param("dataAtualizacao") LocalDate dataAtualizacao,
                                             @Param("dataVencimento") LocalDate dataVencimento,
                                             @Param("limiteEstoque") BigDecimal limiteEstoque);
}
