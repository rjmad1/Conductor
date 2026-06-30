package com.conductor.shared.security;

import java.util.Set;
import java.util.UUID;

/** Platform-agnostic abstraction representing the authenticated user/machine identity. */
public interface AuthenticatedPrincipal {

  /**
   * Resolves the UUID of the tenant scope under which the request is executing.
   *
   * @return the tenant UUID
   */
  UUID getTenantId();

  /**
   * Resolves the primary unique identifier of the security principal (e.g. keycloak user ID).
   *
   * @return the principal identifier string
   */
  String getPrincipalId();

  /**
   * Returns the set of roles/permissions associated with the principal.
   *
   * @return the set of granted roles
   */
  Set<String> getRoles();

  /**
   * Returns the correlation ID associated with the principal context.
   *
   * @return the correlation ID string
   */
  String getCorrelationId();
}
