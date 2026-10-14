package br.com.mercado.precificacao.core.exception;

public class CodigoBarrasInvalidoException extends RuntimeException {
    public CodigoBarrasInvalidoException() {
        super("O código de barras deve conter de 1 a 13 dígitos.");
    }
}

