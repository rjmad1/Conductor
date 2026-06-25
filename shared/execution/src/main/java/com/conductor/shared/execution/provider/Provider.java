package com.conductor.shared.execution.provider;

import java.util.Map;

/** Common contract that every external provider must implement. */
public interface Provider {

  /** Returns the metadata definition of this provider. */
  ProviderDefinition getDefinition();

  /** Establishes connection/state using the provided credentials and configuration parameters. */
  void connect(ProviderCredential credential, Map<String, Object> params);

  /** Disconnects the provider and releases resources. */
  void disconnect();

  /** Executes a generic request against the provider APIs. */
  ProviderResponse execute(ProviderRequest request);

  /** Performs a health check on the provider. */
  ProviderHealth health();

  /** Validates configuration parameters. */
  boolean validateConfiguration(Map<String, Object> config);

  /** Verifies the authenticity of incoming webhook requests. */
  boolean verifyWebhook(Map<String, String> headers, String body, String secret);

  /** Refreshes dynamic credentials (like OAuth2 tokens) if supported. */
  ProviderCredential refreshCredentials(ProviderCredential credential);
}
