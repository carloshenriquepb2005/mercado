package br.com.mercado.precificacao.application.port.in;

import br.com.mercado.precificacao.application.dto.ProdutoResult;

public interface BuscarProdutoPorCodigoUseCase {
    ProdutoResult executar(String codigoBarras);
}

