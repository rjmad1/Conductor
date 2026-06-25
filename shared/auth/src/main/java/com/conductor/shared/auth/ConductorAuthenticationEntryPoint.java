package com.conductor.shared.auth;

import com.conductor.shared.security.SecurityExceptionRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationEntryPoint rendering RFC 7807 problem details for unauthenticated requests.
 */
@Component
public class ConductorAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    SecurityExceptionRenderer.renderError(
        request,
        response,
        HttpServletResponse.SC_UNAUTHORIZED,
        "https://conductor.io/errors/unauthorized",
        "Unauthorized",
        authException.getMessage() != null
            ? authException.getMessage()
            : "Authentication credentials are required or invalid",
        null);
  }
}
