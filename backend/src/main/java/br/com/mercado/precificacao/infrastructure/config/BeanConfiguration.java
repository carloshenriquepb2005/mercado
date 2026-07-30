package br.com.mercado.precificacao.infrastructure.config;

import br.com.mercado.precificacao.adapter.in.web.mapper.ProdutoWebMapper;
import br.com.mercado.precificacao.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.application.port.in.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.application.port.in.AutenticarUsuarioUseCase;
import br.com.mercado.precificacao.application.port.in.BuscarProdutoPorCodigoUseCase;
import br.com.mercado.precificacao.application.port.out.AtualizarPrecoProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarUsuarioPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoPort;
import br.com.mercado.precificacao.application.port.out.BuscarProdutoSimilarPort;
import br.com.mercado.precificacao.application.service.AtualizarPrecoProdutoService;
import br.com.mercado.precificacao.application.service.AutenticarUsuarioService;
import br.com.mercado.precificacao.application.service.BuscarProdutoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(BuscarUsuarioPort buscarUsuarioPort) {
        return new AutenticarUsuarioService(buscarUsuarioPort);
    }

    @Bean
    BuscarProdutoPorCodigoUseCase buscarProdutoUseCase(BuscarProdutoPort produtoPort,
                                                       BuscarProdutoSimilarPort similarPort) {
        return new BuscarProdutoService(produtoPort, similarPort);
    }

    @Bean
    AtualizarPrecoProdutoUseCase atualizarPrecoUseCase(BuscarProdutoPort buscarProdutoPort,
                                                       AtualizarPrecoProdutoPort atualizarPrecoPort) {
        return new AtualizarPrecoProdutoService(buscarProdutoPort, atualizarPrecoPort);
    }

    @Bean
    ProdutoPersistenceMapper produtoPersistenceMapper() {
        return new ProdutoPersistenceMapper();
    }

    @Bean
    ProdutoWebMapper produtoWebMapper() {
        return new ProdutoWebMapper();
    }
}
