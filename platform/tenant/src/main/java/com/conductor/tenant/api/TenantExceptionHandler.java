package com.conductor.tenant.api;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice(basePackages = "com.conductor.tenant.api")
@SuppressWarnings("null")
public class TenantExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(TenantExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleNotFound(IllegalArgumentException ex, WebRequest request) {
    log.debug("Tenant resource not found: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/tenant-not-found"));
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleConflict(IllegalStateException ex, WebRequest request) {
    log.warn("Tenant conflict or invalid state: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/tenant-conflict"));
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
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "One or more fields failed validation");
    problem.setType(URI.create("https://conductor.io/errors/validation"));
    problem.setTitle("Validation Failed");
    problem.setProperty("instance", request.getDescription(false));
    problem.setProperty("errors", fieldErrors);
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
    log.error("Unexpected error in tenant API", ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setType(URI.create("https://conductor.io/errors/internal"));
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }
}
