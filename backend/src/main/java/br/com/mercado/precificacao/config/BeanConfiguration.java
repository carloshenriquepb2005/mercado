package br.com.mercado.precificacao.config;

import br.com.mercado.precificacao.adapter.input.mapper.ProdutoInputMapper;
import br.com.mercado.precificacao.adapter.output.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.core.mapper.ProdutoMapper;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.core.usecase.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.core.usecase.AutenticarUsuarioUseCase;
import br.com.mercado.precificacao.core.usecase.BuscarProdutoPorCodigoUseCase;
import br.com.mercado.precificacao.port.input.AtualizarPrecoProdutoInputPort;
import br.com.mercado.precificacao.port.input.AutenticarUsuarioInputPort;
import br.com.mercado.precificacao.port.input.BuscarProdutoPorCodigoInputPort;
import br.com.mercado.precificacao.port.output.AtualizarPrecoProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoSimilarOutputPort;
import br.com.mercado.precificacao.port.output.BuscarUsuarioOutputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    AutenticarUsuarioInputPort autenticarUsuarioInputPort(BuscarUsuarioOutputPort buscarUsuarioOutputPort) {
        return new AutenticarUsuarioUseCase(buscarUsuarioOutputPort);
    }

    @Bean
    BuscarProdutoPorCodigoInputPort buscarProdutoPorCodigoInputPort(
            BuscarProdutoOutputPort buscarProdutoOutputPort,
            BuscarProdutoSimilarOutputPort buscarProdutoSimilarOutputPort,
            ProdutoMapper produtoMapper) {
        return new BuscarProdutoPorCodigoUseCase(
                buscarProdutoOutputPort, buscarProdutoSimilarOutputPort, produtoMapper);
    }

    @Bean
    AtualizarPrecoProdutoInputPort atualizarPrecoProdutoInputPort(
            BuscarProdutoOutputPort buscarProdutoOutputPort,
            AtualizarPrecoProdutoOutputPort atualizarPrecoProdutoOutputPort,
            ProdutoService produtoService) {
        return new AtualizarPrecoProdutoUseCase(
                buscarProdutoOutputPort, atualizarPrecoProdutoOutputPort, produtoService);
    }

    @Bean
    ProdutoService produtoService() {
        return new ProdutoService();
    }

    @Bean
    ProdutoMapper produtoMapper() {
        return new ProdutoMapper();
    }

    @Bean
    ProdutoPersistenceMapper produtoPersistenceMapper() {
        return new ProdutoPersistenceMapper();
    }

    @Bean
    ProdutoInputMapper produtoInputMapper() {
        return new ProdutoInputMapper();
    }
}
