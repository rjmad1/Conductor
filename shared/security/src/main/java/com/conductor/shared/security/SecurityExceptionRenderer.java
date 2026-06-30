package com.conductor.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/** Utility class to write RFC 7807 JSON error responses to the HTTP response output stream. */
public final class SecurityExceptionRenderer {

  private static final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private SecurityExceptionRenderer() {}

  /** Writes the error response as JSON with standard headers and the correct HTTP status. */
  public static void renderError(
      HttpServletRequest request,
      HttpServletResponse response,
      int status,
      String type,
      String title,
      String detail,
      Map<String, Object> extensions)
      throws IOException {

    response.setStatus(status);
    response.setContentType("application/problem+json");
    response.setCharacterEncoding("UTF-8");

    SecurityErrorResponse errorResponse =
        new SecurityErrorResponse(
            type, title, status, detail, request.getRequestURI(), Instant.now(), extensions);

    String json = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(json);
    response.getWriter().flush();
  }
}
