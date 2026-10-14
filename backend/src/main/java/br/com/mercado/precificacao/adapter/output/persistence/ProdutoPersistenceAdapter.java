package br.com.mercado.precificacao.adapter.output.persistence;

import br.com.mercado.precificacao.adapter.output.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.adapter.output.persistence.repository.ProdutoJpaRepository;
import br.com.mercado.precificacao.adapter.output.persistence.repository.ProdutoSimilarJpaRepository;
import br.com.mercado.precificacao.core.model.Produto;
import br.com.mercado.precificacao.port.output.AtualizarPrecoProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoSimilarOutputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ProdutoPersistenceAdapter
        implements BuscarProdutoOutputPort, BuscarProdutoSimilarOutputPort, AtualizarPrecoProdutoOutputPort {
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
    public boolean atualizarPreco(Long produtoId, BigDecimal novoPreco) {
        log.info("Tentativa de atualização de preço para o produto id={}", produtoId);
        int linhasAtualizadas = produtoRepository.atualizarPrecoPorId(Math.toIntExact(produtoId), novoPreco);
        if (linhasAtualizadas > 0) {
            log.info("Preço atualizado para o produto id={}", produtoId);
        }
        return linhasAtualizadas > 0;
    }
}
