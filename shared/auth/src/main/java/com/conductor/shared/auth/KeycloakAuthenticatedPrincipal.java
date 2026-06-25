package com.conductor.shared.auth;

import com.conductor.shared.security.AuthenticatedPrincipal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Keycloak-specific OIDC token implementation of AuthenticatedPrincipal. */
public class KeycloakAuthenticatedPrincipal implements AuthenticatedPrincipal {

  private final Jwt jwt;
  private final Set<String> roles;
  private final String correlationId;

  public KeycloakAuthenticatedPrincipal(
      Jwt jwt, Collection<? extends GrantedAuthority> authorities, String correlationId) {
    this.jwt = jwt;
    this.roles =
        authorities != null
            ? authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
            : Collections.emptySet();
    this.correlationId = correlationId;
  }

  @Override
  public UUID getTenantId() {
    // Check custom 'tenant_id' or 'tenant' claim inside JWT
    Object tenantIdVal = jwt.getClaim("tenant_id");
    if (tenantIdVal == null) {
      tenantIdVal = jwt.getClaim("tenant");
    }
    if (tenantIdVal == null) {
      return null;
    }
    try {
      return UUID.fromString(tenantIdVal.toString());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public String getPrincipalId() {
    return jwt.getSubject();
  }

  @Override
  public Set<String> getRoles() {
    return roles;
  }

  @Override
  public String getCorrelationId() {
    return correlationId;
  }

  /**
   * Return the underlying JWT token object.
   *
   * @return the OIDC Jwt
   */
  public Jwt getJwt() {
    return jwt;
  }
}
