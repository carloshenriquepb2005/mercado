package br.com.mercado.precificacao.port.input;

import br.com.mercado.precificacao.core.model.Produto;

public interface BuscarProdutoPorCodigoInputPort {
    Produto executar(String codigoBarras);
}

