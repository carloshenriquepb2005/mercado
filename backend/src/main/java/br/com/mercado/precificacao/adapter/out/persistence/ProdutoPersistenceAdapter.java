package br.com.mercado.precificacao.adapter.out.persistence;

import br.com.mercado.precificacao.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.adapter.out.persistence.repository.ProdutoJpaRepository;
import br.com.mercado.precificacao.adapter.out.persistence.repository.ProdutoSimilarJpaRepository;
import br.com.mercado.precificacao.application.port.out.AtualizarPrecoProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoSimilarPort;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import br.com.mercado.precificacao.domain.model.Produto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ProdutoPersistenceAdapter
        implements BuscarProdutoPort, BuscarProdutoSimilarPort, AtualizarPrecoProdutoPort {
    private static final Logger log = LoggerFactory.getLogger(ProdutoPersistenceAdapter.class);

    private final ProdutoJpaRepository produtoRepository;
    private final ProdutoSimilarJpaRepository similarRepository;
    private final ProdutoPersistenceMapper mapper;

    public ProdutoPersistenceAdapter(ProdutoJpaRepository produtoRepository,
                                     ProdutoSimilarJpaRepository similarRepository,
                                     ProdutoPersistenceMapper mapper) {
        this.produtoRepository = produtoRepository;
        this.similarRepository = similarRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorCodigoPrincipal(String codigoBarras) {
        return produtoRepository.findByCodigoProduto(codigoBarras).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(Math.toIntExact(id)).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> buscarCodigoPrincipal(String codigoSimilar) {
        return similarRepository.buscarCodigoPrincipal(codigoSimilar);
    }

    @Override
    @Transactional
    public void atualizarPreco(Long produtoId, BigDecimal novoPreco) {
        log.info("Tentativa de atualização de preço para o produto id={}", produtoId);
        int linhasAtualizadas = produtoRepository.atualizarPrecoPorId(Math.toIntExact(produtoId), novoPreco);
        if (linhasAtualizadas == 0) {
            throw new ProdutoNaoEncontradoException();
        }
        log.info("Preço atualizado para o produto id={}", produtoId);
    }
}
