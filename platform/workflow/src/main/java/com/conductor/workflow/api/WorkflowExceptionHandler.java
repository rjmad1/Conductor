package com.conductor.workflow.api;

import com.conductor.workflow.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler producing RFC 7807 Problem Details responses.
 * Covers validation errors, resource-not-found, illegal state, and unexpected errors.
 */
@RestControllerAdvice
public class WorkflowExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .type("https://conductor.io/errors/not-found")
                        .title("Resource Not Found")
                        .status(404)
                        .detail(ex.getMessage())
                        .instance(request.getDescription(false))
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalStateException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.builder()
                        .type("https://conductor.io/errors/invalid-state")
                        .title("Invalid State Transition")
                        .status(422)
                        .detail(ex.getMessage())
                        .instance(request.getDescription(false))
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           WebRequest request) {
        Map<String, Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .type("https://conductor.io/errors/validation")
                        .title("Validation Failed")
                        .status(400)
                        .detail("One or more fields failed validation")
                        .instance(request.getDescription(false))
                        .timestamp(Instant.now())
                        .extensions(fieldErrors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .type("https://conductor.io/errors/internal")
                        .title("Internal Server Error")
                        .status(500)
                        .detail("An unexpected error occurred")
                        .instance(request.getDescription(false))
                        .timestamp(Instant.now())
                        .build());
    }
}
