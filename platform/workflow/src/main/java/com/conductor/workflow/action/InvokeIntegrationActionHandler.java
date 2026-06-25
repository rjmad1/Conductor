package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.execution.provider.Provider;
import com.conductor.shared.execution.provider.ProviderCredential;
import com.conductor.shared.execution.provider.ProviderRegistry;
import com.conductor.shared.execution.provider.ProviderRequest;
import com.conductor.shared.execution.provider.ProviderResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Reusable workflow step action handler for executing integration calls through external providers.
 */
@Component
public class InvokeIntegrationActionHandler implements ActionHandler {

  private static final Logger log = LoggerFactory.getLogger(InvokeIntegrationActionHandler.class);

  private final ProviderRegistry providerRegistry;
  private final Environment environment;

  public InvokeIntegrationActionHandler(
      ProviderRegistry providerRegistry, Environment environment) {
    this.providerRegistry = providerRegistry;
    this.environment = environment;
  }

  @Override
  public String getActionType() {
    return "INVOKE_INTEGRATION";
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    if (config == null) {
      return ActionResult.failure("CONFIGURATION_MISSING", "Configuration is missing");
    }

    String providerType = (String) config.get("providerType");
    if (providerType == null) {
      providerType = (String) config.get("integrationType"); // fallback
    }
    if (providerType == null || providerType.trim().isEmpty()) {
      return ActionResult.failure(
          "MISSING_PROVIDER_TYPE", "Missing required parameter: providerType");
    }

    Optional<Provider> providerOpt = providerRegistry.getProvider(providerType);
    if (providerOpt.isEmpty()) {
      log.error("Provider of type {} not registered", providerType);
      return ActionResult.failure(
          "PROVIDER_NOT_REGISTERED", "Provider not registered: " + providerType);
    }
    Provider provider = providerOpt.get();

    try {
      // 1. Resolve Credentials
      ProviderCredential credential = resolveCredentials(providerType, config);

      // 2. Build Provider Request
      String path = (String) config.getOrDefault("path", "");
      String method = (String) config.getOrDefault("method", "POST");
      Map<String, String> headers = (Map<String, String>) config.getOrDefault("headers", Map.of());
      Map<String, String> queryParams =
          (Map<String, String>) config.getOrDefault("queryParams", Map.of());
      Object body = config.get("body");
      String idempotencyKey = (String) config.get("idempotencyKey");

      ProviderRequest request =
          ProviderRequest.builder()
              .path(path)
              .method(method)
              .headers(headers)
              .queryParams(queryParams)
              .body(body)
              .idempotencyKey(idempotencyKey)
              .correlationId(context.getCorrelationId())
              .build();

      // 3. Connect (if provider requires session/handshake)
      provider.connect(credential, config);

      // 4. Execute Provider Request
      log.info(
          "Executing integration action via provider {}: method={}, path={}",
          providerType,
          method,
          path);
      ProviderResponse response = provider.execute(request);

      // 5. Disconnect
      provider.disconnect();

      // 6. Return response values
      Map<String, Object> outputs = new HashMap<>();
      outputs.put("statusCode", response.getStatusCode());
      outputs.put("body", response.getBody());
      outputs.put("success", response.isSuccess());
      outputs.put("latencyMs", response.getLatencyMs());

      if (response.isSuccess()) {
        return ActionResult.success(outputs);
      } else {
        return ActionResult.builder()
            .success(false)
            .errorCode("PROVIDER_ERROR")
            .errorMessage("Provider returned status code: " + response.getStatusCode())
            .outputVariables(outputs)
            .build();
      }

    } catch (Exception ex) {
      log.error("Error executing integration provider action", ex);
      return ActionResult.failure("EXECUTION_ERROR", "Execution error: " + ex.getMessage());
    }
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Invoke Integration")
        .description("Invokes a registered integration provider API dynamically.")
        .requiredConfigurationKeys(List.of("providerType"))
        .supportedParameters(
            Map.of(
                "providerType", "Type of the provider (e.g. SHOPIFY, ZOHO)",
                "path", "Endpoint sub-path or absolute URL",
                "method", "HTTP method (GET, POST, etc.)",
                "body", "Payload for the request body"))
        .build();
  }

  @SuppressWarnings("unchecked")
  private ProviderCredential resolveCredentials(String providerType, Map<String, Object> config) {
    // 1. Check if credential object is provided directly in configuration
    Map<String, Object> credConfig = (Map<String, Object>) config.get("credential");
    if (credConfig != null) {
      String typeStr = (String) credConfig.get("authType");
      if (typeStr != null) {
        ProviderCredential.AuthType authType =
            ProviderCredential.AuthType.valueOf(typeStr.toUpperCase());
        return ProviderCredential.builder()
            .authType(authType)
            .apiKey((String) credConfig.get("apiKey"))
            .headerName((String) credConfig.get("headerName"))
            .queryParamName((String) credConfig.get("queryParamName"))
            .token((String) credConfig.get("token"))
            .accessToken((String) credConfig.get("accessToken"))
            .refreshToken((String) credConfig.get("refreshToken"))
            .scope((String) credConfig.get("scope"))
            .webhookSecret((String) credConfig.get("webhookSecret"))
            .build();
      }
    }

    // 2. Otherwise resolve from standard application configuration environment variables
    String prefix = "conductor.providers." + providerType.toLowerCase();
    String apiKey = environment.getProperty(prefix + ".api-key");
    if (apiKey != null) {
      String headerName = environment.getProperty(prefix + ".header-name", "X-API-Key");
      return ProviderCredential.builder()
          .authType(ProviderCredential.AuthType.API_KEY)
          .apiKey(apiKey)
          .headerName(headerName)
          .build();
    }

    String token = environment.getProperty(prefix + ".token");
    if (token != null) {
      return ProviderCredential.builder()
          .authType(ProviderCredential.AuthType.BEARER_TOKEN)
          .token(token)
          .build();
    }

    String oauthToken = environment.getProperty(prefix + ".oauth-token");
    if (oauthToken != null) {
      return ProviderCredential.builder()
          .authType(ProviderCredential.AuthType.OAUTH2)
          .accessToken(oauthToken)
          .build();
    }

    return null;
  }
}
