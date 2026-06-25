package com.conductor.shared.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtAuthenticationConverterTest {

  private final KeycloakJwtAuthenticationConverter converter =
      new KeycloakJwtAuthenticationConverter();

  @Test
  void givenRealmAndClientRoles_whenConverted_thenSanitizeSpacesAndNormalize() {
    Jwt jwt =
        Jwt.withTokenValue("token-val")
            .header("alg", "none")
            .claim("sub", "user-123")
            .claim("realm_access", Map.of("roles", List.of("Tenant Admin", "Campaign Editor")))
            .claim(
                "resource_access",
                Map.of(
                    "conductor-backend", Map.of("roles", List.of("API Client")),
                    "another client", Map.of("roles", List.of("service account"))))
            .build();

    AbstractAuthenticationToken token = converter.convert(jwt);
    assertNotNull(token);

    Collection<GrantedAuthority> authorities = token.getAuthorities();
    List<String> authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();

    // Check realm roles sanitization
    assertTrue(
        authorityNames.contains("ROLE_TENANT_ADMIN"),
        "Should sanitize 'Tenant Admin' to 'ROLE_TENANT_ADMIN'");
    assertTrue(
        authorityNames.contains("ROLE_CAMPAIGN_EDITOR"),
        "Should sanitize 'Campaign Editor' to 'ROLE_CAMPAIGN_EDITOR'");

    // Check client roles sanitization
    assertTrue(
        authorityNames.contains("ROLE_CONDUCTOR-BACKEND_API_CLIENT"),
        "Should map client role to ROLE_{CLIENT}_{ROLE}");
    assertTrue(
        authorityNames.contains("ROLE_ANOTHER_CLIENT_SERVICE_ACCOUNT"),
        "Should sanitize client name and role");
  }
}
