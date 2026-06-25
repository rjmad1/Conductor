package com.conductor.shared.auth;

import com.conductor.shared.security.TenantContextResolver;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * TenantContextResolver resolving Tenant UUID from request headers (X-Tenant-ID) or dynamically
 * from an OIDC JWT bearer token.
 */
@Component
public class HeaderAndJwtTenantContextResolver implements TenantContextResolver {

  private static final Logger log =
      LoggerFactory.getLogger(HeaderAndJwtTenantContextResolver.class);
  private static final String TENANT_HEADER = "X-Tenant-ID";

  @Override
  public Optional<UUID> resolveTenantId(HttpServletRequest request) {
    // 1. Try X-Tenant-ID HTTP header first (Kong Ingress Gateway propagation)
    String tenantHeader = request.getHeader(TENANT_HEADER);
    if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
      try {
        return Optional.of(UUID.fromString(tenantHeader.trim()));
      } catch (IllegalArgumentException e) {
        log.warn("Invalid UUID format in X-Tenant-ID header: '{}'", tenantHeader);
      }
    }

    // 2. Fall back to extracting tenant_id from active Spring Security context
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Object tenantIdVal = jwtAuth.getToken().getClaim("tenant_id");
      if (tenantIdVal == null) {
        tenantIdVal = jwtAuth.getToken().getClaim("tenant");
      }
      if (tenantIdVal != null) {
        try {
          return Optional.of(UUID.fromString(tenantIdVal.toString()));
        } catch (IllegalArgumentException e) {
          log.warn("Invalid UUID format in JWT tenant claim: '{}'", tenantIdVal);
        }
      }
    }

    // 3. Last fallback: parse Bearer token directly from Authorization header (if filter order is
    // shifted)
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.trim().startsWith("Bearer ")) {
      try {
        String token = authHeader.substring(7).trim();
        var claims = JWTParser.parse(token).getJWTClaimsSet();
        Object tenantIdVal = claims.getClaim("tenant_id");
        if (tenantIdVal == null) {
          tenantIdVal = claims.getClaim("tenant");
        }
        if (tenantIdVal != null) {
          return Optional.of(UUID.fromString(tenantIdVal.toString()));
        }
      } catch (Exception e) {
        log.debug("Failed to extract tenant from Authorization header JWT: {}", e.getMessage());
      }
    }

    return Optional.empty();
  }
}
