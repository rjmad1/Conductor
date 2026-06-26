package com.conductor.integrations.service;

import com.conductor.integrations.domain.OAuthConnection;
import com.conductor.integrations.framework.CredentialEncryptor;
import com.conductor.integrations.framework.ProxyHttpClient;
import com.conductor.integrations.repository.OAuthConnectionRepository;
import com.conductor.shared.middleware.tenant.AuditLogger;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Exchanges an OAuth authorization code for real access/refresh tokens by calling the provider's
 * token endpoint. Replaces the prior placeholder that stored fake mock tokens in the production
 * callback path.
 *
 * <p>Requires the integration's OAuthConnection to have tokenUrl, clientId, and clientSecret
 * pre-configured (stored via /credentials before OAuth initiation).
 */
@Service
public class OAuthTokenExchangeService {

  private static final Logger log = LoggerFactory.getLogger(OAuthTokenExchangeService.class);

  private final OAuthConnectionRepository oauthConnectionRepository;
  private final CredentialEncryptor encryptor;
  private final ProxyHttpClient proxyHttpClient;
  private final AuditLogger auditLogger;

  public OAuthTokenExchangeService(
      OAuthConnectionRepository oauthConnectionRepository,
      CredentialEncryptor encryptor,
      ProxyHttpClient proxyHttpClient,
      AuditLogger auditLogger) {
    this.oauthConnectionRepository = oauthConnectionRepository;
    this.encryptor = encryptor;
    this.proxyHttpClient = proxyHttpClient;
    this.auditLogger = auditLogger;
  }

  /**
   * Exchanges the authorization code for tokens and persists them (encrypted) on the existing
   * OAuthConnection record. The record must already exist with tokenUrl, clientId, and clientSecret
   * configured; if not, an IllegalStateException is thrown.
   *
   * @param integrationId the integration whose stored OAuth config is used
   * @param tenantId the owning tenant
   * @param code the authorization code received from the provider callback
   * @param redirectUri the redirect URI used in the original authorization request
   * @return the updated OAuthConnection with real (encrypted) tokens
   */
  public OAuthConnection exchange(
      UUID integrationId, UUID tenantId, String code, String redirectUri) {
    OAuthConnection conn =
        oauthConnectionRepository
            .findByIntegrationIdAndTenantId(integrationId, tenantId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No OAuth connection record found for integration "
                            + integrationId
                            + ". Call POST /credentials with OAuth config before initiating authorization."));

    String tokenUrl = conn.getTokenUrl();
    String clientId = conn.getClientId();
    String encryptedSecret = conn.getClientSecret();

    if (tokenUrl == null || tokenUrl.isBlank()) {
      throw new IllegalStateException(
          "tokenUrl is not configured on integration "
              + integrationId
              + ". Store OAuth config via POST /credentials before initiating authorization.");
    }
    if (clientId == null
        || clientId.isBlank()
        || encryptedSecret == null
        || encryptedSecret.isBlank()) {
      throw new IllegalStateException(
          "clientId or clientSecret is not configured on integration " + integrationId + ".");
    }

    String clientSecret = encryptor.decrypt(encryptedSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("code", code);
    body.add("redirect_uri", redirectUri);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    RestTemplate restTemplate = proxyHttpClient.getRestTemplate();
    ResponseEntity<Map> response;
    try {
      response = restTemplate.postForEntity(tokenUrl, request, Map.class);
    } catch (Exception e) {
      auditLogger.logEvent(
          "OAUTH_TOKEN_EXCHANGE_FAILED",
          "integration:" + integrationId,
          "FAILURE",
          "Token exchange HTTP error: " + e.getMessage());
      throw new IllegalStateException("OAuth token exchange failed: " + e.getMessage(), e);
    }

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      auditLogger.logEvent(
          "OAUTH_TOKEN_EXCHANGE_FAILED",
          "integration:" + integrationId,
          "FAILURE",
          "Provider returned non-2xx: " + response.getStatusCode());
      throw new IllegalStateException(
          "OAuth token exchange failed: provider returned " + response.getStatusCode());
    }

    Map<?, ?> tokenResponse = response.getBody();
    String accessToken =
        Optional.ofNullable(tokenResponse.get("access_token"))
            .map(Object::toString)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OAuth provider response missing access_token for integration "
                            + integrationId));
    String refreshToken =
        Optional.ofNullable(tokenResponse.get("refresh_token")).map(Object::toString).orElse(null);
    long expiresIn =
        Optional.ofNullable(tokenResponse.get("expires_in"))
            .map(v -> Long.parseLong(v.toString()))
            .orElse(3600L);
    String scope =
        Optional.ofNullable(tokenResponse.get("scope")).map(Object::toString).orElse(null);

    conn.setAccessToken(encryptor.encrypt(accessToken));
    conn.setRefreshToken(refreshToken != null ? encryptor.encrypt(refreshToken) : null);
    conn.setExpiresAt(Instant.now().plusSeconds(expiresIn));
    conn.setScope(scope);
    conn.setUpdatedAt(Instant.now());

    OAuthConnection saved = oauthConnectionRepository.save(conn);
    auditLogger.logEvent(
        "OAUTH_TOKEN_EXCHANGE_SUCCESS",
        "integration:" + integrationId,
        "SUCCESS",
        "Real OAuth tokens stored for integration");
    log.info("OAuth token exchange completed for integration={}", integrationId);
    return saved;
  }
}
