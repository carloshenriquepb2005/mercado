package br.com.mercado.precificacao.adapter.out.persistence.repository;

import br.com.mercado.precificacao.adapter.out.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findFirstByNomeAndSenha(String nome, String senha);
}
