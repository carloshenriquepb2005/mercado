package br.com.mercado.precificacao.core.exception;

public class PrecoInvalidoException extends RuntimeException {
    public PrecoInvalidoException(String message) {
        super(message);
    }
}

