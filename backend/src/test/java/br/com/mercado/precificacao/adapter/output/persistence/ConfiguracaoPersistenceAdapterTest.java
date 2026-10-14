package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.repository.SenhaCaixaJpaRepository;
import br.com.mercado.precificacao.adapter.output.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfiguracaoPersistenceAdapterTest {
    private UsuarioJpaRepository usuarioRepository;
    private SenhaCaixaJpaRepository senhaCaixaRepository;
    private ConfiguracaoPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        senhaCaixaRepository = mock(SenhaCaixaJpaRepository.class);
        adapter = new ConfiguracaoPersistenceAdapter(usuarioRepository, senhaCaixaRepository);
    }

    @Test
    void deveConsultarUsuarioDeReferenciaSemDiferenciarMaiusculas() {
        when(usuarioRepository.existsByNomeIgnoreCase("bruno")).thenReturn(true);
        assertTrue(adapter.existePorNome("bruno"));
        verify(usuarioRepository).existsByNomeIgnoreCase("bruno");
    }

    @Test
    void deveValidarSenhaSemAlterarTabelaLegada() {
        when(senhaCaixaRepository.existsBySenhaCaixa("1234")).thenReturn(false);
        assertFalse(adapter.existePorSenha("1234"));
        verify(senhaCaixaRepository).existsBySenhaCaixa("1234");
    }
}
