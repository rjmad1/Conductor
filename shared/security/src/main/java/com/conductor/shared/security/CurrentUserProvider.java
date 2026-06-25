package com.conductor.shared.security;

import java.util.Optional;

/** Service to retrieve the current security context's AuthenticatedPrincipal. */
public interface CurrentUserProvider {

  /**
   * Returns the current authenticated principal if one exists in the security context.
   *
   * @return optional containing the principal, or empty if unauthenticated
   */
  Optional<AuthenticatedPrincipal> getCurrentPrincipal();
}
