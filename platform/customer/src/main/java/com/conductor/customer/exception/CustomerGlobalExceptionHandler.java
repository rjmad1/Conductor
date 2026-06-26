package com.conductor.customer.exception;

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

@RestControllerAdvice(basePackages = "com.conductor.customer.api")
public class CustomerGlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(CustomerGlobalExceptionHandler.class);

  @ExceptionHandler(CustomerNotFoundException.class)
  public ProblemDetail handleNotFound(CustomerNotFoundException ex) {
    log.debug("Customer not found: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/customer-not-found"));
    return problem;
  }

  @ExceptionHandler(CustomerAlreadyExistsException.class)
  public ProblemDetail handleConflict(CustomerAlreadyExistsException ex) {
    log.warn("Customer conflict: {}", ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/customer-already-exists"));
    return problem;
  }

  @ExceptionHandler(ConsentException.class)
  public ProblemDetail handleConsent(ConsentException ex) {
    log.warn("Consent error: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/consent-error"));
    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setType(URI.create("https://conductor.io/errors/bad-request"));
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
    log.error("Unexpected error in customer API", ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setType(URI.create("https://conductor.io/errors/internal"));
    problem.setProperty("instance", request.getDescription(false));
    return problem;
  }
}
