package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.ProdutoResult;

public interface BuscarProdutoPorCodigoInputPort {
    ProdutoResult executar(String codigoBarras);
}

