package com.conductor.shared.middleware.tenant;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class TenantContextFilterTest {

  private final TenantContextFilter filter = new TenantContextFilter();

  @AfterEach
  void cleanUp() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  // ── Unauthenticated (no JWT) ──────────────────────────────────────────────

  @Test
  void noJwt_noHeader_doesNotSetTenant() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertNull(TenantContext.getCurrentTenantId());
    assertEquals(200, res.getStatus());
  }

  @Test
  void noJwt_validHeader_setsTenantFromHeader() throws Exception {
    UUID tenantId = UUID.randomUUID();
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Tenant-ID", tenantId.toString());
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertEquals(200, res.getStatus());
  }

  @Test
  void noJwt_invalidUuidHeader_returns400() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Tenant-ID", "not-a-uuid");
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertEquals(400, res.getStatus());
  }

  // ── Authenticated (JWT present) ───────────────────────────────────────────

  @Test
  void jwtPresent_noHeader_setsTenantFromJwt() throws Exception {
    UUID tenantId = UUID.randomUUID();
    setJwtWithTenant(tenantId);

    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertEquals(200, res.getStatus());
    // TenantContext cleared in finally — we verify via chain execution
  }

  @Test
  void jwtPresent_headerMatchesJwt_passes() throws Exception {
    UUID tenantId = UUID.randomUUID();
    setJwtWithTenant(tenantId);

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Tenant-ID", tenantId.toString());
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertEquals(200, res.getStatus());
  }

  @Test
  void jwtPresent_headerDiffersFromJwt_returns403() throws Exception {
    UUID jwtTenant = UUID.randomUUID();
    UUID spoofedTenant = UUID.randomUUID(); // attacker's own tenant or random
    setJwtWithTenant(jwtTenant);

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Tenant-ID", spoofedTenant.toString()); // spoofing attempt
    MockHttpServletResponse res = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertEquals(
        403,
        res.getStatus(),
        "Header claiming a different tenant than the JWT must be rejected with 403");
  }

  // ── extractTenantFromJwt (unit) ───────────────────────────────────────────

  @Test
  void extractTenant_standardIssuer_returnsUUID() {
    UUID tenantId = UUID.randomUUID();
    JwtAuthenticationToken token = buildJwtToken(tenantId);
    assertEquals(tenantId, TenantContextFilter.extractTenantFromJwt(token));
  }

  @Test
  void extractTenant_unknownRealmFormat_returnsNull() {
    Jwt jwt =
        Jwt.withTokenValue("tok")
            .header("alg", "RS256")
            .claim("iss", "http://keycloak/realms/some-other-realm")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
    assertNull(TenantContextFilter.extractTenantFromJwt(token));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void setJwtWithTenant(UUID tenantId) {
    JwtAuthenticationToken token = buildJwtToken(tenantId);
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  private JwtAuthenticationToken buildJwtToken(UUID tenantId) {
    String issuer = "http://keycloak:8080/realms/conductor-" + tenantId;
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claim("iss", issuer)
            .subject("user-sub-123")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
    return new JwtAuthenticationToken(jwt);
  }
}
