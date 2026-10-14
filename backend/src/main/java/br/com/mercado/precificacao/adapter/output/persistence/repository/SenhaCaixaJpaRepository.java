package br.com.mercado.precificacao.adapter.output.persistence.repository;

import br.com.mercado.precificacao.adapter.output.persistence.entity.SenhaCaixaJpaEntity;
import org.springframework.data.repository.Repository;

public interface SenhaCaixaJpaRepository extends Repository<SenhaCaixaJpaEntity, Long> {
    boolean existsBySenhaCaixa(String senhaCaixa);
}
