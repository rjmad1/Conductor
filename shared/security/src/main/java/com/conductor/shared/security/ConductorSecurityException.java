package com.conductor.shared.security;

/** Base exception class representing security foundation errors. */
public class ConductorSecurityException extends RuntimeException {

  private final int statusCode;
  private final String type;
  private final String title;

  public ConductorSecurityException(int statusCode, String type, String title, String message) {
    super(message);
    this.statusCode = statusCode;
    this.type = type;
    this.title = title;
  }

  public ConductorSecurityException(
      int statusCode, String type, String title, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.type = type;
    this.title = title;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }
}
