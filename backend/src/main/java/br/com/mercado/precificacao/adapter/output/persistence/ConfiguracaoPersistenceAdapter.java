package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.repository.SenhaCaixaJpaRepository;
import br.com.mercado.precificacao.adapter.output.persistence.repository.UsuarioJpaRepository;
import br.com.mercado.precificacao.port.output.ValidarSenhaCaixaOutputPort;
import br.com.mercado.precificacao.port.output.VerificarUsuarioReferenciaOutputPort;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracaoPersistenceAdapter implements VerificarUsuarioReferenciaOutputPort,
        ValidarSenhaCaixaOutputPort {
    private final UsuarioJpaRepository usuarioRepository;
    private final SenhaCaixaJpaRepository senhaCaixaRepository;

    public ConfiguracaoPersistenceAdapter(UsuarioJpaRepository usuarioRepository,
                                           SenhaCaixaJpaRepository senhaCaixaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.senhaCaixaRepository = senhaCaixaRepository;
    }

    @Override
    public boolean existePorNome(String nome) {
        return usuarioRepository.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existePorSenha(String senha) {
        return senhaCaixaRepository.existsBySenhaCaixa(senha);
    }
}
