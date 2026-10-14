package br.com.mercado.precificacao.adapter.output.persistence.repository;

import br.com.mercado.precificacao.adapter.output.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findFirstByNomeAndSenha(String nome, String senha);

    boolean existsByNomeIgnoreCase(String nome);
}
