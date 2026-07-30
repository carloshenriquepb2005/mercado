package br.com.mercado.precificacao.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI precificacaoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("App Satélite de Precificação")
                .description("Consulta de produtos e atualização exclusiva do preço de venda no legado.")
                .version("1.0.0"));
    }
}

