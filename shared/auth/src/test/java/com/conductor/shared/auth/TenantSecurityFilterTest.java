package com.conductor.shared.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.conductor.shared.middleware.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class TenantSecurityFilterTest {

  private TenantSecurityFilter filter;
  private HeaderAndJwtTenantContextResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = mock(HeaderAndJwtTenantContextResolver.class);
    filter = new TenantSecurityFilter(resolver);
    SecurityContextHolder.clearContext();
    TenantContext.clear();
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
    MDC.clear();
  }

  @Test
  void givenAuthenticatedRequestWithTenant_whenFiltered_thenPopulateContexts() throws Exception {
    UUID tenantId = UUID.randomUUID();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user-456").build();

    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
    when(resolver.resolveTenantId(request)).thenReturn(Optional.of(tenantId));
    when(request.getHeader(anyString())).thenReturn(null);

    filter.doFilter(request, response, chain);

    // Verify filter chain execution
    verify(chain).doFilter(request, response);
    // Contexts should be cleared after execution
    assertNull(TenantContext.getCurrentTenantId());
    assertNull(TenantContext.getCurrentUserId());
    assertNull(MDC.get("requestId"));
  }

  @Test
  void givenAuthenticatedRequestWithoutTenant_whenFiltered_thenRejectWithBadRequest()
      throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    StringWriter sw = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(sw));

    Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user-456").build();

    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
    when(resolver.resolveTenantId(request)).thenReturn(Optional.empty()); // Missing tenant context

    filter.doFilter(request, response, chain);

    // Verify rejection
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(chain, never()).doFilter(request, response);
    assertTrue(sw.toString().contains("Missing Tenant Context"));
  }
}
