package br.com.mercado.precificacao.adapter.input.controller;

import br.com.mercado.precificacao.adapter.input.request.LoginRequest;
import br.com.mercado.precificacao.adapter.input.response.LoginResponse;
import br.com.mercado.precificacao.adapter.input.swagger.AuthControllerSwagger;
import br.com.mercado.precificacao.core.model.Usuario;
import br.com.mercado.precificacao.port.input.AutenticarUsuarioInputPort;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerSwagger {
    private final AutenticarUsuarioInputPort autenticarUsuarioInputPort;

    public AuthController(AutenticarUsuarioInputPort autenticarUsuarioInputPort) {
        this.autenticarUsuarioInputPort = autenticarUsuarioInputPort;
    }

    @Override
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = autenticarUsuarioInputPort.executar(request.username(), request.password());
        return new LoginResponse(usuario.codigo(), usuario.nome(), usuario.nome(), usuario.perfil());
    }
}
