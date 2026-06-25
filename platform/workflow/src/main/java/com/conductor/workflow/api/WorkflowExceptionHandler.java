package com.conductor.workflow.api;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler producing RFC 7807 Problem Details responses. Covers validation errors,
 * resource-not-found, illegal state, and unexpected errors.
 */
@RestControllerAdvice(basePackages = "com.conductor.workflow.api")
public class WorkflowExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(WorkflowExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleNotFound(IllegalArgumentException ex, WebRequest request) {
    log.debug("Workflow resource not found: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/not-found"));
    problem.setTitle("Resource Not Found");
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleInvalidState(IllegalStateException ex, WebRequest request) {
    log.warn("Workflow invalid state: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/invalid-state"));
    problem.setTitle("Invalid State Transition");
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleMalformedJson(
      HttpMessageNotReadableException ex, WebRequest request) {
    log.warn("Malformed request body: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body could not be parsed");
    problem.setType(URI.create("https://conductor.io/errors/malformed-request"));
    problem.setTitle("Malformed Request Body");
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, Object> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                    (a, b) -> a));
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields failed validation");
    problem.setType(URI.create("https://conductor.io/errors/validation"));
    problem.setTitle("Validation Failed");
    problem.setProperty("instance", request.getDescription(false));
    problem.setProperty("errors", fieldErrors);
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
    log.error("Unexpected error in workflow API", ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setType(URI.create("https://conductor.io/errors/internal"));
    problem.setTitle("Internal Server Error");
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }
}
