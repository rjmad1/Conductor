package com.conductor.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/** Interface to resolve active tenant parameters from incoming request contexts. */
public interface TenantContextResolver {

  /**
   * Resolves the Tenant UUID from HTTP headers, parameters or token claims.
   *
   * @param request the HTTP servlet request
   * @return optional containing the resolved UUID
   */
  Optional<UUID> resolveTenantId(HttpServletRequest request);
}
