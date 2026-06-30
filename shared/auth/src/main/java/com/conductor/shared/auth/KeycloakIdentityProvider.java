package com.conductor.shared.auth;

import com.conductor.shared.security.IdentityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** IdentityProvider implementation for Keycloak. */
@Component
public class KeycloakIdentityProvider implements IdentityProvider {

  private final String serverUrl;

  public KeycloakIdentityProvider(
      @Value("${keycloak.server-url:http://localhost:8080}") String serverUrl) {
    this.serverUrl = serverUrl;
  }

  @Override
  public String getName() {
    return "Keycloak";
  }

  @Override
  public String getIssuerUrl() {
    return serverUrl + "/realms/conductor";
  }
}
