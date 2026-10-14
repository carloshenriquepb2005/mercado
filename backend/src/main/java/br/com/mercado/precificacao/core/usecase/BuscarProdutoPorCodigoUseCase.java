package br.com.mercado.precificacao.core.usecase;

import br.com.mercado.precificacao.core.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.core.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.core.mapper.ProdutoMapper;
import br.com.mercado.precificacao.core.model.ProdutoResult;
import br.com.mercado.precificacao.port.input.BuscarProdutoPorCodigoInputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoSimilarOutputPort;

public final class BuscarProdutoPorCodigoUseCase implements BuscarProdutoPorCodigoInputPort {
    private final BuscarProdutoOutputPort buscarProdutoOutputPort;
    private final BuscarProdutoSimilarOutputPort buscarProdutoSimilarOutputPort;
    private final ProdutoMapper produtoMapper;

    public BuscarProdutoPorCodigoUseCase(BuscarProdutoOutputPort buscarProdutoOutputPort,
                                         BuscarProdutoSimilarOutputPort buscarProdutoSimilarOutputPort,
                                         ProdutoMapper produtoMapper) {
        this.buscarProdutoOutputPort = buscarProdutoOutputPort;
        this.buscarProdutoSimilarOutputPort = buscarProdutoSimilarOutputPort;
        this.produtoMapper = produtoMapper;
    }

    @Override
    public ProdutoResult executar(String codigoBarras) {
        String codigoNormalizado = normalizar(codigoBarras);

        return buscarProdutoOutputPort.buscarPorCodigoPrincipal(codigoNormalizado)
                .or(() -> buscarProdutoSimilarOutputPort.buscarCodigoPrincipal(codigoNormalizado)
                        .flatMap(buscarProdutoOutputPort::buscarPorCodigoPrincipal))
                .map(produtoMapper::toResult)
                .orElseThrow(ProdutoNaoEncontradoException::new);
    }

    private static String normalizar(String codigoBarras) {
        if (codigoBarras == null) {
            throw new CodigoBarrasInvalidoException();
        }
        String normalizado = codigoBarras.trim();
        if (normalizado.isEmpty() || normalizado.length() > 13 || !normalizado.matches("\\d+")) {
            throw new CodigoBarrasInvalidoException();
        }
        return normalizado;
    }
}

