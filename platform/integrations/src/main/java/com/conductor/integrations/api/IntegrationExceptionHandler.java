package com.conductor.integrations.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.conductor.integrations.api")
public class IntegrationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IntegrationExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleNotFound(IllegalArgumentException ex, WebRequest request) {
        log.debug("Integration resource not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://conductor.io/errors/integration-not-found"));
        problem.setProperty("instance", request.getDescription(false));
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleInvalidState(IllegalStateException ex, WebRequest request) {
        log.warn("Integration invalid state: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("https://conductor.io/errors/integration-invalid-state"));
        problem.setProperty("instance", request.getDescription(false));
        return problem;
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleUnsupported(UnsupportedOperationException ex, WebRequest request) {
        log.warn("Unsupported integration action: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("https://conductor.io/errors/unsupported-action"));
        problem.setProperty("instance", request.getDescription(false));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more fields failed validation");
        problem.setType(URI.create("https://conductor.io/errors/validation"));
        problem.setTitle("Validation Failed");
        problem.setProperty("instance", request.getDescription(false));
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error in integrations API", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problem.setType(URI.create("https://conductor.io/errors/internal"));
        problem.setProperty("instance", request.getDescription(false));
        return problem;
    }
}
