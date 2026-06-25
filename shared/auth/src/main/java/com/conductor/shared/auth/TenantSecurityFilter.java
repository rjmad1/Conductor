package com.conductor.shared.auth;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.security.SecurityExceptionRenderer;
import com.conductor.shared.security.TenantContextResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter executing after BearerTokenAuthenticationFilter. Binds resolved Tenant ID, User
 * ID, Roles, and Correlation ID to active thread contexts (TenantContext, MDC). Rejects
 * authenticated requests lacking tenant contexts.
 */
public class TenantSecurityFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TenantSecurityFilter.class);

  private static final String CORRELATION_MDC_KEY = "requestId";
  private static final String CORRELATION_HEADER = "X-Correlation-ID";
  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String TRACEPARENT_HEADER = "traceparent";

  private final TenantContextResolver tenantContextResolver;

  public TenantSecurityFilter(TenantContextResolver tenantContextResolver) {
    this.tenantContextResolver = tenantContextResolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 1. Resolve and set correlation ID
    String correlationId = resolveCorrelationId(request);
    MDC.put(CORRELATION_MDC_KEY, correlationId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated =
        authentication != null
            && authentication.isAuthenticated()
            && !(authentication.getClass().getSimpleName().equals("AnonymousAuthenticationToken"));

    UUID tenantId = null;
    String principalId = null;

    if (isAuthenticated) {
      // 2. Resolve Tenant context from request/token
      var resolvedTenant = tenantContextResolver.resolveTenantId(request);
      if (resolvedTenant.isPresent()) {
        tenantId = resolvedTenant.get();
        TenantContext.setCurrentTenantId(tenantId);
      }

      // 3. Resolve User context from authenticated principal
      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        principalId = jwtAuth.getToken().getSubject();
        TenantContext.setCurrentUserId(principalId);
      } else {
        principalId = authentication.getName();
        TenantContext.setCurrentUserId(principalId);
      }

      // 4. Reject authenticated requests lacking tenant context (per Task 3 requirements)
      // Global operators (e.g., Platform Admin, system:* or *:* authorities) do not require tenant
      // context
      boolean isGlobalOperator =
          authentication.getAuthorities().stream()
              .map(auth -> auth.getAuthority().toUpperCase())
              .anyMatch(
                  role ->
                      role.contains("PLATFORM_ADMIN")
                          || role.contains("SYSTEM")
                          || role.contains("SUPPORT_AGENT")
                          || role.equals("*:*"));

      if (tenantId == null && !isGlobalOperator) {
        log.warn(
            "Access denied for authenticated user '{}' due to missing tenant context", principalId);
        SecurityExceptionRenderer.renderError(
            request,
            response,
            HttpServletResponse.SC_BAD_REQUEST,
            "https://conductor.io/errors/missing-tenant",
            "Missing Tenant Context",
            "An active tenant context is required to complete this secure request",
            null);
        // Clear context before aborting
        clearContexts();
        return;
      }

      log.trace(
          "Resolved context: Tenant={}, User={}, Correlation={}",
          tenantId,
          principalId,
          correlationId);
    } else {
      // For unauthenticated requests (like permitAll endpoints), try resolving tenant if header is
      // present
      var resolvedTenant = tenantContextResolver.resolveTenantId(request);
      resolvedTenant.ifPresent(TenantContext::setCurrentTenantId);
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      clearContexts();
    }
  }

  private String resolveCorrelationId(HttpServletRequest request) {
    String corrId = request.getHeader(CORRELATION_HEADER);
    if (corrId == null || corrId.trim().isEmpty()) {
      corrId = request.getHeader(REQUEST_ID_HEADER);
    }
    if (corrId == null || corrId.trim().isEmpty()) {
      corrId = request.getHeader(TRACEPARENT_HEADER);
    }
    if (corrId == null || corrId.trim().isEmpty()) {
      corrId = UUID.randomUUID().toString();
    }
    return corrId;
  }

  private void clearContexts() {
    TenantContext.clear();
    MDC.remove(CORRELATION_MDC_KEY);
  }
}
