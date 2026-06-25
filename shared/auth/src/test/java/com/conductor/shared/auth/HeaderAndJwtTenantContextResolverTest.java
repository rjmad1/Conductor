package com.conductor.shared.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class HeaderAndJwtTenantContextResolverTest {

  private HeaderAndJwtTenantContextResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new HeaderAndJwtTenantContextResolver();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenTenantHeader_whenResolved_thenReturnTenantId() {
    UUID tenantId = UUID.randomUUID();
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Tenant-ID")).thenReturn(tenantId.toString());

    Optional<UUID> resolved = resolver.resolveTenantId(request);
    assertTrue(resolved.isPresent());
    assertEquals(tenantId, resolved.get());
  }

  @Test
  void givenSecurityContextJwt_whenResolved_thenReturnTenantId() {
    UUID tenantId = UUID.randomUUID();
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-123")
            .claim("tenant_id", tenantId.toString())
            .build();

    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

    Optional<UUID> resolved = resolver.resolveTenantId(request);
    assertTrue(resolved.isPresent());
    assertEquals(tenantId, resolved.get());
  }

  @Test
  void givenBearerTokenHeader_whenResolved_thenReturnTenantId() {
    UUID tenantId = UUID.randomUUID();
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    // Construct mock Bearer token header containing the tenant_id claim in plaintext payload
    // Payload: {"tenant_id":"<tenantId>"}
    // Base64 encoded payload: eyd0ZW5hbnRfaWQiOiI8dGVuYW50SWQ+In0=
    String payload = "{\"tenant_id\":\"" + tenantId + "\"}";
    String encodedPayload =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());
    String headerToken = "Bearer eyJhbGciOiJSUzI1NiJ9." + encodedPayload + ".signature";

    when(request.getHeader("Authorization")).thenReturn(headerToken);

    Optional<UUID> resolved = resolver.resolveTenantId(request);
    assertTrue(resolved.isPresent());
    assertEquals(tenantId, resolved.get());
  }
}
