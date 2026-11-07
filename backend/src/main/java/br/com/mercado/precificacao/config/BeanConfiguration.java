package br.com.mercado.precificacao.config;

import br.com.mercado.precificacao.adapter.input.mapper.EntradaEstoqueInputMapper;
import br.com.mercado.precificacao.adapter.input.mapper.ProdutoInputMapper;
import br.com.mercado.precificacao.adapter.output.persistence.mapper.ProdutoPersistenceMapper;
import br.com.mercado.precificacao.core.service.ProdutoService;
import br.com.mercado.precificacao.core.usecase.AtualizarPrecoProdutoUseCase;
import br.com.mercado.precificacao.core.usecase.AutenticarUsuarioUseCase;
import br.com.mercado.precificacao.core.usecase.BuscarProdutoPorCodigoUseCase;
import br.com.mercado.precificacao.core.usecase.RegistrarEntradaEstoqueUseCase;
import br.com.mercado.precificacao.core.usecase.TestarConexaoServidorUseCase;
import br.com.mercado.precificacao.core.usecase.ValidarSenhaCaixaUseCase;
import br.com.mercado.precificacao.port.input.AtualizarPrecoProdutoInputPort;
import br.com.mercado.precificacao.port.input.AutenticarUsuarioInputPort;
import br.com.mercado.precificacao.port.input.BuscarProdutoPorCodigoInputPort;
import br.com.mercado.precificacao.port.input.RegistrarEntradaEstoqueInputPort;
import br.com.mercado.precificacao.port.input.TestarConexaoServidorInputPort;
import br.com.mercado.precificacao.port.input.ValidarSenhaCaixaInputPort;
import br.com.mercado.precificacao.port.output.AtualizarPrecoProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoOutputPort;
import br.com.mercado.precificacao.port.output.BuscarProdutoSimilarOutputPort;
import br.com.mercado.precificacao.port.output.BuscarUsuarioOutputPort;
import br.com.mercado.precificacao.port.output.RegistrarEntradaEstoqueOutputPort;
import br.com.mercado.precificacao.port.output.ValidarSenhaCaixaOutputPort;
import br.com.mercado.precificacao.port.output.VerificarUsuarioReferenciaOutputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class BeanConfiguration {
    @Bean
    AutenticarUsuarioInputPort autenticarUsuarioInputPort(BuscarUsuarioOutputPort buscarUsuarioOutputPort) {
        return new AutenticarUsuarioUseCase(buscarUsuarioOutputPort);
    }

    @Bean
    BuscarProdutoPorCodigoInputPort buscarProdutoPorCodigoInputPort(
            BuscarProdutoOutputPort buscarProdutoOutputPort,
            BuscarProdutoSimilarOutputPort buscarProdutoSimilarOutputPort) {
        return new BuscarProdutoPorCodigoUseCase(buscarProdutoOutputPort, buscarProdutoSimilarOutputPort);
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
    RegistrarEntradaEstoqueInputPort registrarEntradaEstoqueInputPort(
            BuscarProdutoOutputPort buscarProdutoOutputPort,
            RegistrarEntradaEstoqueOutputPort registrarEntradaEstoqueOutputPort,
            ProdutoService produtoService,
            Clock clock) {
        return new RegistrarEntradaEstoqueUseCase(
                buscarProdutoOutputPort, registrarEntradaEstoqueOutputPort, produtoService, clock);
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    EntradaEstoqueInputMapper entradaEstoqueInputMapper() {
        return new EntradaEstoqueInputMapper();
    }

    @Bean
    ProdutoService produtoService() {
        return new ProdutoService();
    }

    @Bean
    ProdutoPersistenceMapper produtoPersistenceMapper() {
        return new ProdutoPersistenceMapper();
    }

    @Bean
    ProdutoInputMapper produtoInputMapper() {
        return new ProdutoInputMapper();
    }

    @Bean
    TestarConexaoServidorInputPort testarConexaoServidorInputPort(
            VerificarUsuarioReferenciaOutputPort verificarUsuarioReferenciaOutputPort) {
        return new TestarConexaoServidorUseCase(verificarUsuarioReferenciaOutputPort);
    }

    @Bean
    ValidarSenhaCaixaInputPort validarSenhaCaixaInputPort(
            ValidarSenhaCaixaOutputPort validarSenhaCaixaOutputPort) {
        return new ValidarSenhaCaixaUseCase(validarSenhaCaixaOutputPort);
    }
}
