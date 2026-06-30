package com.conductor.shared.security;

/** Interface representing the Identity Provider configuration details. */
public interface IdentityProvider {

  /**
   * Name of the Identity Provider (e.g. Keycloak).
   *
   * @return IDP provider name
   */
  String getName();

  /**
   * Issuer URL of the IDP.
   *
   * @return provider issuer URL string
   */
  String getIssuerUrl();
}
