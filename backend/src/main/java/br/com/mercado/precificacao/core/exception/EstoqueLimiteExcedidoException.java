package br.com.mercado.precificacao.core.exception;

public class EstoqueLimiteExcedidoException extends RuntimeException {
    public EstoqueLimiteExcedidoException() {
        super("A entrada faria o estoque exceder o limite permitido.");
    }
}
