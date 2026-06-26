package com.conductor.shared.middleware.tenant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Resolves the active tenant context for each request.
 *
 * <p>For authenticated requests (JWT present): - Derives tenant ID from the JWT issuer claim
 * (Keycloak realm = conductor-{tenantUUID}). - If an X-Tenant-ID header is also present, it MUST
 * match the JWT-derived tenant. Mismatches are rejected with 403 to prevent cross-tenant header
 * spoofing. - The JWT-derived value is always authoritative; the header is informational only.
 *
 * <p>For unauthenticated requests (webhook ingress, health checks): - Falls back to the X-Tenant-ID
 * header value if present. - Webhook endpoints set TenantContext directly from the URL path
 * variable.
 *
 * <p>This filter runs after Spring Security's FilterChainProxy (order -100), so the SecurityContext
 * is fully populated when this filter executes.
 */
@Component
public class TenantContextFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String USER_HEADER = "X-User-ID";

  /** Keycloak realm naming convention: conductor-{tenantUUID} */
  private static final String REALM_PREFIX = "conductor-";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest httpRequest)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletResponse httpResponse = (HttpServletResponse) response;

    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        // Authenticated request — derive tenant from the verified JWT
        UUID jwtTenantId = extractTenantFromJwt(jwtAuth);

        if (jwtTenantId == null) {
          // No parseable tenant from issuer.
          // Platform admins operate cross-tenant and do not belong to any specific realm;
          // allow them through without a tenant context so global endpoints remain accessible.
          boolean isPlatformAdmin =
              jwtAuth.getAuthorities().stream()
                  .anyMatch(a -> "ROLE_PLATFORM_ADMIN".equals(a.getAuthority()));
          if (isPlatformAdmin) {
            log.debug(
                "Platform admin JWT without realm issuer — proceeding without tenant context");
            chain.doFilter(request, response);
            return;
          }
          log.warn("JWT present but tenant could not be derived from issuer; rejecting request");
          httpResponse.sendError(
              HttpServletResponse.SC_FORBIDDEN,
              "Unable to determine tenant from authentication token");
          return;
        }

        // Validate header if present — reject mismatches (spoofing attempt)
        String headerValue = httpRequest.getHeader(TENANT_HEADER);
        if (headerValue != null && !headerValue.trim().isEmpty()) {
          UUID headerTenantId;
          try {
            headerTenantId = UUID.fromString(headerValue.trim());
          } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID in X-Tenant-ID header: '{}'", headerValue);
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Tenant ID format");
            return;
          }
          if (!headerTenantId.equals(jwtTenantId)) {
            log.warn(
                "Tenant ID mismatch: X-Tenant-ID header={} jwt-derived={}. "
                    + "Possible spoofing attempt.",
                headerTenantId,
                jwtTenantId);
            httpResponse.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "X-Tenant-ID header does not match authenticated tenant");
            return;
          }
        }

        TenantContext.setCurrentTenantId(jwtTenantId);
        // Prefer sub claim for user ID; fall back to X-User-ID header
        String userId = jwtAuth.getToken().getSubject();
        if (userId == null) {
          userId = httpRequest.getHeader(USER_HEADER);
        }
        TenantContext.setCurrentUserId(userId);
        log.debug("Tenant resolved from JWT: tenantId={}, userId={}", jwtTenantId, userId);

      } else {
        // Unauthenticated path (webhook ingress, health checks, etc.)
        // Fall back to header-based resolution; webhook controllers override this
        // directly from the URL path variable before processing.
        String tenantIdStr = httpRequest.getHeader(TENANT_HEADER);
        String userId = httpRequest.getHeader(USER_HEADER);

        if (tenantIdStr != null && !tenantIdStr.trim().isEmpty()) {
          try {
            UUID tenantId = UUID.fromString(tenantIdStr.trim());
            TenantContext.setCurrentTenantId(tenantId);
            TenantContext.setCurrentUserId(userId);
            log.debug("Tenant resolved from header (unauthenticated path): {}", tenantId);
          } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format in X-Tenant-ID header: '{}'", tenantIdStr);
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Tenant ID format");
            return;
          }
        } else {
          log.debug("No X-Tenant-ID header found in request: {}", httpRequest.getRequestURI());
        }
      }

      chain.doFilter(request, response);

    } finally {
      TenantContext.clear();
    }
  }

  /**
   * Parses the tenant UUID from the Keycloak issuer string. Expected format:
   * {scheme}://{host}/realms/conductor-{tenantUUID} Returns null if the issuer does not follow the
   * expected pattern.
   */
  static UUID extractTenantFromJwt(JwtAuthenticationToken jwtAuth) {
    try {
      String issuerStr = jwtAuth.getToken().getClaimAsString("iss");
      if (issuerStr == null) {
        return null;
      }
      // Find last path segment: ".../realms/conductor-{uuid}"
      int lastSlash = issuerStr.lastIndexOf('/');
      if (lastSlash < 0) {
        return null;
      }
      String realm = issuerStr.substring(lastSlash + 1);
      if (!realm.startsWith(REALM_PREFIX)) {
        return null;
      }
      String uuidStr = realm.substring(REALM_PREFIX.length());
      return UUID.fromString(uuidStr);
    } catch (Exception e) {
      return null;
    }
  }
}
