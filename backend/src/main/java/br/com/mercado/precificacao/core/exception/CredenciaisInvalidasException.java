package br.com.mercado.precificacao.core.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos.");
    }
}
