package br.com.mercado.precificacao.application.port.out;

import java.util.Optional;

public interface BuscarProdutoSimilarPort {
    Optional<String> buscarCodigoPrincipal(String codigoSimilar);
}

