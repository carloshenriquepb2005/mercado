package br.com.mercado.precificacao.infrastructure.exception;

import br.com.mercado.precificacao.adapter.in.web.response.ApiErrorResponse;
import br.com.mercado.precificacao.domain.exception.CodigoBarrasInvalidoException;
import br.com.mercado.precificacao.domain.exception.CredenciaisInvalidasException;
import br.com.mercado.precificacao.domain.exception.PrecoInvalidoException;
import br.com.mercado.precificacao.domain.exception.ProdutoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ApiErrorResponse> handleUnauthorized(CredenciaisInvalidasException exception,
                                                        HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", exception.getMessage(), request);
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ProdutoNaoEncontradoException exception,
                                                    HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PRODUTO_NAO_ENCONTRADO", exception.getMessage(), request);
    }

    @ExceptionHandler({PrecoInvalidoException.class, CodigoBarrasInvalidoException.class,
            IllegalArgumentException.class, ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof HttpMessageNotReadableException
                ? "O corpo da requisição é inválido."
                : exception.getMessage();
        return error(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                      HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Os dados informados são inválidos.");
        return error(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", message, request);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiErrorResponse> handleDatabase(DataAccessException exception, HttpServletRequest request) {
        log.warn("Falha de acesso ao banco legado: {}", exception.getClass().getSimpleName());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "BANCO_INDISPONIVEL",
                "Servidor ou banco de dados indisponível.", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro inesperado.", request);
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message,
                                                           HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(ZONE), status.value(), status.getReasonPhrase(), code,
                message == null || message.isBlank() ? "Os dados informados são inválidos." : message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
