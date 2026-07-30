package br.com.mercado.precificacao.adapter.out.persistence;

import br.com.mercado.precificacao.adapter.out.persistence.entity.UsuarioJpaEntity;
import br.com.mercado.precificacao.adapter.out.persistence.repository.UsuarioJpaRepository;
import br.com.mercado.precificacao.application.port.out.BuscarUsuarioPort;
import br.com.mercado.precificacao.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements BuscarUsuarioPort {
    private final UsuarioJpaRepository repository;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Usuario> buscarPorNomeESenha(String nome, String senha) {
        return repository.findFirstByNomeAndSenha(nome, senha).map(UsuarioPersistenceAdapter::toDomain);
    }

    private static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(entity.getCodigo(), entity.getNome(), entity.getPerfil());
    }
}
