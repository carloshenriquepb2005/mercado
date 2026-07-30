package br.com.mercado.precificacao.adapter.out.persistence.repository;

import br.com.mercado.precificacao.adapter.out.persistence.entity.ProdutoSimilarId;
import br.com.mercado.precificacao.adapter.out.persistence.entity.ProdutoSimilarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProdutoSimilarJpaRepository
        extends JpaRepository<ProdutoSimilarJpaEntity, ProdutoSimilarId> {
    @Query("""
            select ps.id.codigoProduto
              from ProdutoSimilarJpaEntity ps
             where ps.id.codigoSimilar = :codigoSimilar
            """)
    Optional<String> buscarCodigoPrincipal(@Param("codigoSimilar") String codigoSimilar);
}

