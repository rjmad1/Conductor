package com.conductor.shared.security;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthorizationEngine {

  /**
   * Checks if the active authenticated context has the requested permission. Enforces
   * deny-by-default. Wildcards (e.g., "workflows:*") are supported.
   */
  public boolean hasPermission(String requiredPermission) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    Collection<? extends org.springframework.security.core.GrantedAuthority> authorities =
        authentication.getAuthorities();
    if (authorities == null || authorities.isEmpty()) {
      return false;
    }

    // Standard mapping check
    for (org.springframework.security.core.GrantedAuthority authority : authorities) {
      String roleOrPermission = authority.getAuthority();

      // Strip spring security prefixes if present
      String cleaned = roleOrPermission;
      if (cleaned.startsWith("ROLE_")) {
        cleaned = cleaned.substring(5);
      } else if (cleaned.startsWith("SCOPE_")) {
        cleaned = cleaned.substring(6);
      }

      cleaned = cleaned.toLowerCase();
      String req = requiredPermission.toLowerCase();

      // Match full permissions or wildcard mappings
      if (cleaned.equals("*:*") || cleaned.equals(req)) {
        return true;
      }

      if (cleaned.endsWith(":*")) {
        String prefix = cleaned.substring(0, cleaned.length() - 2);
        if (req.startsWith(prefix + ":")) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks if the authenticated tenant matches the target resource owner. Used for resource-level
   * ABAC checks.
   */
  public boolean isResourceOwner(String resourceTenantId, String activeTenantId) {
    if (resourceTenantId == null || activeTenantId == null) {
      return false;
    }
    return resourceTenantId.equalsIgnoreCase(activeTenantId);
  }
}
