package com.conductor.analytics.dashboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for MetabaseEmbedService JWT token generation. */
class MetabaseEmbedServiceTest {

  private MetabaseEmbedService service;

  @BeforeEach
  void setUp() {
    // Minimum 256-bit secret (32 bytes) for HMAC-SHA256
    String secret = "test-secret-key-for-metabase-jwt-signing-minimum-256-bits";
    service = new MetabaseEmbedService("http://localhost:3000", secret);
  }

  @Test
  void generateTokenProducesNonEmptyJwt() {
    String token = service.generateToken(1, "tenant-123");
    assertNotNull(token);
    assertFalse(token.isBlank());
    // JWT has 3 parts separated by dots
    String[] parts = token.split("\\.");
    assertEquals(3, parts.length);
  }

  @Test
  void generateTokenProducesUniqueTokensPerCall() {
    String token1 = service.generateToken(1, "tenant-123");
    String token2 = service.generateToken(1, "tenant-123");
    // Tokens should differ due to different iat timestamps
    assertNotEquals(token1, token2);
  }

  @Test
  void generateTokenForDifferentTenantsProducesDifferentTokens() {
    String tokenA = service.generateToken(1, "tenant-A");
    String tokenB = service.generateToken(1, "tenant-B");
    assertNotEquals(tokenA, tokenB);
  }

  @Test
  void generateEmbedUrlThrowsWithoutSecret() {
    MetabaseEmbedService noSecret = new MetabaseEmbedService("http://localhost:3000", "");
    assertThrows(IllegalStateException.class, () -> noSecret.generateEmbedUrl(1));
  }
}
