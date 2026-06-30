package com.conductor.shared.auth;

import com.conductor.shared.security.AuthenticatedPrincipal;
import com.conductor.shared.security.CurrentUserProvider;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Spring Security context-based implementation of CurrentUserProvider. */
@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

  private static final String CORRELATION_MDC_KEY = "requestId";

  @Override
  public Optional<AuthenticatedPrincipal> getCurrentPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      String correlationId = MDC.get(CORRELATION_MDC_KEY);
      return Optional.of(
          new KeycloakAuthenticatedPrincipal(
              jwtAuth.getToken(), jwtAuth.getAuthorities(), correlationId));
    }
    return Optional.empty();
  }
}
