package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.entity.UsuarioJpaEntity;
import br.com.mercado.precificacao.adapter.output.persistence.repository.UsuarioJpaRepository;
import br.com.mercado.precificacao.core.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioPersistenceAdapterTest {
    private UsuarioJpaRepository repository;
    private UsuarioPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioJpaRepository.class);
        adapter = new UsuarioPersistenceAdapter(repository);
    }

    @Test
    void deveBuscarPorNomeESenhaSemExporSenha() {
        when(repository.findFirstByNomeAndSenha("Carlos", "123"))
                .thenReturn(Optional.of(new UsuarioJpaEntity(7L, "Carlos", "123", 3)));
        Usuario usuario = adapter.buscarPorNomeESenha("Carlos", "123").orElseThrow();
        assertEquals(7L, usuario.codigo());
        assertEquals("Carlos", usuario.nome());
        assertEquals(3, usuario.perfil());
        verify(repository).findFirstByNomeAndSenha("Carlos", "123");
    }

    @Test
    void deveRetornarVazioParaCredenciaisInexistentes() {
        when(repository.findFirstByNomeAndSenha("Carlos", "x")).thenReturn(Optional.empty());
        assertTrue(adapter.buscarPorNomeESenha("Carlos", "x").isEmpty());
    }
}
