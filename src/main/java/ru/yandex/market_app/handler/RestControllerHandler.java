package ru.yandex.market_app.handler;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.market_app.exception.ApiServiceException;

@Slf4j
@RestControllerAdvice
public class RestControllerHandler extends ResponseEntityExceptionHandler  {

    @ExceptionHandler(ApiServiceException.class)
    public final ResponseEntity<ProblemDetail> handleApiServiceException(ApiServiceException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(buildProblemDetail(ex, ex.getStatus()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        ProblemDetail body = buildProblemDetail(ex, HttpStatus.BAD_REQUEST, Map.of("validation", errors));
        return ResponseEntity
            .badRequest()
            .body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest webRequest) {
        var errorMap = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> errorMap.put(((FieldError) error).getField(), error.getDefaultMessage()));
        var body = buildProblemDetail(ex, HttpStatus.BAD_REQUEST, Map.of("validation", errorMap));
        return this.handleExceptionInternal(ex, body, headers, status, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.debug("{}: {}", ex,getClass().getName(), ex.getMessage());
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail buildProblemDetail(Exception ex, HttpStatus httpStatus) {
        return buildProblemDetail(ex, httpStatus, null);
    }

    private ProblemDetail buildProblemDetail(Exception ex, HttpStatus httpStatus, Map<String, Object> properties) {
        var uri = URI.create(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI());
        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, ex.getMessage());
        problemDetail.setInstance(uri);
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setProperties(properties);
        return problemDetail;
    }
}