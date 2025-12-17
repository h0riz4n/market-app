package ru.yandex.market_app.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;

@Slf4j
@RestControllerAdvice
public class RestControllerHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiServiceException.class)
    public final Mono<ResponseEntity<ProblemDetail>> handleApiServiceException(ApiServiceException ex, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> {
            return ResponseEntity
                .status(ex.getStatus())
                .body(buildProblemDetail(ex, ex.getStatus(), exchange));
        });
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        var errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        var body = buildProblemDetail(ex, HttpStatus.BAD_REQUEST, Map.of("validation", errors), exchange);
        return Mono.fromSupplier(() -> ResponseEntity.badRequest().body(body));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleExceptionInternal(
		Exception ex, 
        @Nullable Object body, 
        @Nullable HttpHeaders headers, 
        HttpStatusCode status, 
        ServerWebExchange exchange
    ) {
        log.debug("{}: {}", ex,getClass().getName(), ex.getMessage());
		return super.handleExceptionInternal(ex, body, headers, status, exchange);
	}

    private ProblemDetail buildProblemDetail(Exception ex, HttpStatus httpStatus, ServerWebExchange exchange) {
        return buildProblemDetail(ex, httpStatus, null, exchange);
    }

    private ProblemDetail buildProblemDetail(Exception ex, HttpStatus httpStatus, Map<String, Object> properties, ServerWebExchange exchange) {
        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, ex.getMessage());
        problemDetail.setInstance(exchange.getRequest().getURI());
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setProperties(properties);
        return problemDetail;
    }
}
