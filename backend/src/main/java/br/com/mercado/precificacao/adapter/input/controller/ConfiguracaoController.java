package br.com.mercado.precificacao.adapter.input.controller;

import br.com.mercado.precificacao.adapter.input.request.SenhaCaixaRequest;
import br.com.mercado.precificacao.adapter.input.swagger.ConfiguracaoControllerSwagger;
import br.com.mercado.precificacao.port.input.TestarConexaoServidorInputPort;
import br.com.mercado.precificacao.port.input.ValidarSenhaCaixaInputPort;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracaoController implements ConfiguracaoControllerSwagger {
    private final TestarConexaoServidorInputPort testarConexaoServidorInputPort;
    private final ValidarSenhaCaixaInputPort validarSenhaCaixaInputPort;

    public ConfiguracaoController(TestarConexaoServidorInputPort testarConexaoServidorInputPort,
                                  ValidarSenhaCaixaInputPort validarSenhaCaixaInputPort) {
        this.testarConexaoServidorInputPort = testarConexaoServidorInputPort;
        this.validarSenhaCaixaInputPort = validarSenhaCaixaInputPort;
    }

    @Override
    @GetMapping("/conexao")
    public ResponseEntity<Void> testarConexao() {
        testarConexaoServidorInputPort.executar();
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @Override
    @PostMapping("/validar-senha-caixa")
    public ResponseEntity<Void> validarSenhaCaixa(@Valid @RequestBody SenhaCaixaRequest request) {
        validarSenhaCaixaInputPort.executar(request.senha());
        return ResponseEntity.noContent().build();
    }
}
