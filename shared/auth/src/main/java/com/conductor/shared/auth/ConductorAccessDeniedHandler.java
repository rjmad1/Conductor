package com.conductor.shared.auth;

import com.conductor.shared.security.SecurityExceptionRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Custom AccessDeniedHandler rendering RFC 7807 problem details for unauthorized requests. */
@Component
public class ConductorAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityMetrics securityMetrics;

  public ConductorAccessDeniedHandler(SecurityMetrics securityMetrics) {
    this.securityMetrics = securityMetrics;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    securityMetrics.recordAuthorizationFailure();

    SecurityExceptionRenderer.renderError(
        request,
        response,
        HttpServletResponse.SC_FORBIDDEN,
        "https://conductor.io/errors/forbidden",
        "Forbidden",
        accessDeniedException.getMessage() != null
            ? accessDeniedException.getMessage()
            : "Access is denied due to insufficient permissions",
        null);
  }
}
