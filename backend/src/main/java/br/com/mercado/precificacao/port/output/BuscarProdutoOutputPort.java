package br.com.mercado.precificacao.port.output;

import br.com.mercado.precificacao.core.model.Produto;

import java.util.Optional;

public interface BuscarProdutoOutputPort {
    Optional<Produto> buscarPorCodigoPrincipal(String codigoBarras);
    Optional<Produto> buscarPorId(Long id);
}

