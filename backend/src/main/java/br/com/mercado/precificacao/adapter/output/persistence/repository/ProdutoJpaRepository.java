package br.com.mercado.precificacao.adapter.output.persistence.repository;

import br.com.mercado.precificacao.adapter.output.persistence.entity.ProdutoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
}

