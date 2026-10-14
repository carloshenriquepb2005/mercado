package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.entity.UsuarioJpaEntity;
import br.com.mercado.precificacao.adapter.output.persistence.repository.UsuarioJpaRepository;
import br.com.mercado.precificacao.core.model.Usuario;
import br.com.mercado.precificacao.port.output.BuscarUsuarioOutputPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements BuscarUsuarioOutputPort {
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
