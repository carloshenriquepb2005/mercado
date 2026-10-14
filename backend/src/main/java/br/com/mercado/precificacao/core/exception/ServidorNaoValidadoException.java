package br.com.mercado.precificacao.core.exception;

public class ServidorNaoValidadoException extends RuntimeException {
    public ServidorNaoValidadoException() {
        super("Não foi possível validar a conexão com o servidor.");
    }
}
