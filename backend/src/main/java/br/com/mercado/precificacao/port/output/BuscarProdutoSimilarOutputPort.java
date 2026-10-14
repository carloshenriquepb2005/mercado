package br.com.mercado.precificacao.port.output;

import java.util.Optional;

public interface BuscarProdutoSimilarOutputPort {
    Optional<String> buscarCodigoPrincipal(String codigoSimilar);
}

