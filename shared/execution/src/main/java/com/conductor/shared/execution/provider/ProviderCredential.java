package com.conductor.shared.execution.provider;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/** Abstraction representing provider credentials supporting multiple authentication methods. */
@Getter
@Builder
public class ProviderCredential {

  public enum AuthType {
    API_KEY,
    BEARER_TOKEN,
    OAUTH2,
    WEBHOOK_SECRET,
    CERTIFICATE
  }

  private final AuthType authType;

  // API Key specific fields
  private final String apiKey;
  private final String headerName;
  private final String queryParamName;

  // Bearer specific fields
  private final String token;

  // OAuth2 specific fields
  private final String accessToken;
  private final String refreshToken;
  private final Instant expiresAt;
  private final String scope;

  // Webhook Secret specific fields
  private final String webhookSecret;

  // Certificate / future cert-based authentication
  private final byte[] certificateData;
  private final String certificatePassword;
}
