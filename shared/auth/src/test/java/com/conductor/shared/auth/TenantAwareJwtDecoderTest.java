package com.conductor.shared.auth;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

class TenantAwareJwtDecoderTest {

  private TenantAwareJwtDecoder decoder;
  private SecurityMetrics securityMetrics;

  @BeforeEach
  void setUp() {
    securityMetrics = new SecurityMetrics(new SimpleMeterRegistry());
    decoder = new TenantAwareJwtDecoder("http://localhost:8080", securityMetrics);
  }

  @Test
  void givenInvalidIssuer_whenDecoded_thenThrowJwtException() {
    // Token with a disallowed issuer (SSRF prevention test)
    String malformedToken =
        "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vbWFsaWNpb3VzLmNvbS9yZWFsbXMvY29uZHVjdG9yIn0.signature";

    JwtException exception = assertThrows(JwtException.class, () -> decoder.decode(malformedToken));
    assertTrue(exception.getMessage().contains("Issuer is not authorized"));
  }

  @Test
  void givenMissingIssuer_whenDecoded_thenThrowJwtException() {
    // Token with no claims
    String tokenWithoutIssuer = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.e30.signature";

    JwtException exception =
        assertThrows(JwtException.class, () -> decoder.decode(tokenWithoutIssuer));
    assertTrue(exception.getMessage().contains("Missing issuer"));
  }

  @Test
  void givenMalformedToken_whenDecoded_thenThrowJwtException() {
    String malformed = "not-a-jwt";
    assertThrows(JwtException.class, () -> decoder.decode(malformed));
  }
}
