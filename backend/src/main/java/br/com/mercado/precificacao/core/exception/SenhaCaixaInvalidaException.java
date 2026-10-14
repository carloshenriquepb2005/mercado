package br.com.mercado.precificacao.core.exception;

public class SenhaCaixaInvalidaException extends RuntimeException {
    public SenhaCaixaInvalidaException() {
        super("Senha do caixa inválida.");
    }
}
