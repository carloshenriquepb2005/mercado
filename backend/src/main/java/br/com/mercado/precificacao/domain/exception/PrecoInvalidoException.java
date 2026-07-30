package br.com.mercado.precificacao.domain.exception;

public class PrecoInvalidoException extends RuntimeException {
    public PrecoInvalidoException(String message) {
        super(message);
    }
}

