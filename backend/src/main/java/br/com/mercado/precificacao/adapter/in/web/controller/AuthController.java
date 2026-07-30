package br.com.mercado.precificacao.adapter.in.web.controller;

import br.com.mercado.precificacao.adapter.in.web.request.LoginRequest;
import br.com.mercado.precificacao.adapter.in.web.response.ApiErrorResponse;
import br.com.mercado.precificacao.adapter.in.web.response.LoginResponse;
import br.com.mercado.precificacao.application.dto.LoginResult;
import br.com.mercado.precificacao.application.port.in.AutenticarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AutenticarUsuarioUseCase autenticarUsuario;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuario) {
        this.autenticarUsuario = autenticarUsuario;
    }

    @Operation(summary = "Valida o acesso do atendente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = autenticarUsuario.executar(request.username(), request.password());
        return new LoginResponse(result.codigo(), result.nome(), result.nome(), result.perfil());
    }
}
