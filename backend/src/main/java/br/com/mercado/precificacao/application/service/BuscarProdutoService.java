package br.com.mercado.precificacao.application.service;

import br.com.mercado.precificacao.application.dto.ProdutoResult;
import br.com.mercado.precificacao.application.port.in.BuscarProdutoPorCodigoUseCase;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoSimilarPort;
import br.com.mercado.precificacao.domain.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.domain.model.Produto;

public final class BuscarProdutoService implements BuscarProdutoPorCodigoUseCase {
    private final BuscarProdutoPort buscarProdutoPort;
    private final BuscarProdutoSimilarPort buscarProdutoSimilarPort;

    public BuscarProdutoService(BuscarProdutoPort buscarProdutoPort,
                                BuscarProdutoSimilarPort buscarProdutoSimilarPort) {
        this.buscarProdutoPort = buscarProdutoPort;
        this.buscarProdutoSimilarPort = buscarProdutoSimilarPort;
    }

    @Override
    public ProdutoResult executar(String codigoBarras) {
        String codigoNormalizado = normalizar(codigoBarras);

        return buscarProdutoPort.buscarPorCodigoPrincipal(codigoNormalizado)
                .or(() -> buscarProdutoSimilarPort.buscarCodigoPrincipal(codigoNormalizado)
                        .flatMap(buscarProdutoPort::buscarPorCodigoPrincipal))
                .map(BuscarProdutoService::toResult)
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

    private static ProdutoResult toResult(Produto produto) {
        return new ProdutoResult(produto.getId(), produto.getCodigoBarras(), produto.getDescricao(),
                produto.getPrecoVenda());
    }
}

