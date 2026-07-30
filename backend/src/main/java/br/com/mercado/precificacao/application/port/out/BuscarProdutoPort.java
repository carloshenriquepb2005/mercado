package br.com.mercado.precificacao.application.port.out;

import br.com.mercado.precificacao.domain.model.Produto;

import java.util.Optional;

public interface BuscarProdutoPort {
    Optional<Produto> buscarPorCodigoPrincipal(String codigoBarras);
    Optional<Produto> buscarPorId(Long id);
}

