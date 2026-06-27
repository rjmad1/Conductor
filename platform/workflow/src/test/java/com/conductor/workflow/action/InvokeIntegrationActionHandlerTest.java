package com.conductor.workflow.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.execution.provider.Provider;
import com.conductor.shared.execution.provider.ProviderCredential;
import com.conductor.shared.execution.provider.ProviderDefinition;
import com.conductor.shared.execution.provider.ProviderRegistry;
import com.conductor.shared.execution.provider.ProviderRequest;
import com.conductor.shared.execution.provider.ProviderResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class InvokeIntegrationActionHandlerTest {

  private ProviderRegistry providerRegistry;
  private Environment environment;
  private InvokeIntegrationActionHandler handler;
  private Provider mockProvider;

  @BeforeEach
  void setUp() {
    providerRegistry = mock(ProviderRegistry.class);
    environment = mock(Environment.class);
    mockProvider = mock(Provider.class);
    handler = new InvokeIntegrationActionHandler(providerRegistry, environment);
  }

  @Test
  void testGetActionType() {
    assertEquals("INVOKE_INTEGRATION", handler.getActionType());
  }

  @Test
  void testExecuteSuccess() {
    String providerType = "Shopify";

    ProviderDefinition def =
        ProviderDefinition.builder()
            .type(providerType.toUpperCase())
            .name(providerType)
            .version("1.0")
            .supportedActions(List.of("TEST"))
            .build();

    when(mockProvider.getDefinition()).thenReturn(def);
    when(providerRegistry.getProvider(providerType)).thenReturn(Optional.of(mockProvider));

    ProviderResponse mockResponse =
        ProviderResponse.builder()
            .statusCode(200)
            .body("{\"orderId\":\"123\"}")
            .success(true)
            .latencyMs(100)
            .build();
    when(mockProvider.execute(any(ProviderRequest.class))).thenReturn(mockResponse);

    Map<String, Object> config = new HashMap<>();
    config.put("providerType", providerType);
    config.put("path", "/orders");
    config.put("method", "POST");
    config.put("body", Map.of("itemId", "abc"));

    Map<String, Object> credentialConfig = new HashMap<>();
    credentialConfig.put("authType", "API_KEY");
    credentialConfig.put("apiKey", "shop-key-999");
    config.put("credential", credentialConfig);

    ActionContext context =
        ActionContext.builder()
            .tenantId(UUID.randomUUID())
            .configuration(config)
            .correlationId("corr-123")
            .build();

    ActionResult result = handler.execute(context);
    assertTrue(result.isSuccess());
    assertNull(result.getErrorMessage());

    Map<String, Object> outputs = result.getOutputVariables();
    assertNotNull(outputs);
    assertEquals(200, outputs.get("statusCode"));
    assertEquals("{\"orderId\":\"123\"}", outputs.get("body"));
    assertEquals(true, outputs.get("success"));
  }

  @Test
  void testExecuteProviderNotFound() {
    when(providerRegistry.getProvider("Invalid")).thenReturn(Optional.empty());

    Map<String, Object> config = Map.of("providerType", "Invalid");
    ActionContext context =
        ActionContext.builder().tenantId(UUID.randomUUID()).configuration(config).build();

    ActionResult result = handler.execute(context);
    assertFalse(result.isSuccess());
    assertTrue(result.getErrorMessage().contains("Provider not registered"));
  }

  @Test
  void testCredentialResolutionFromEnvironment() {
    String providerType = "Shopify";
    when(providerRegistry.getProvider(providerType)).thenReturn(Optional.of(mockProvider));

    when(environment.getProperty("conductor.providers.shopify.api-key"))
        .thenReturn("env-secret-key-100");
    when(environment.getProperty("conductor.providers.shopify.header-name", "X-API-Key"))
        .thenReturn("X-Shop-Key");

    ProviderResponse mockResponse =
        ProviderResponse.builder().statusCode(200).body("ok").success(true).latencyMs(10).build();

    doAnswer(
            invocation -> {
              ProviderCredential cred = invocation.getArgument(0);
              assertNotNull(cred);
              assertEquals(ProviderCredential.AuthType.API_KEY, cred.getAuthType());
              assertEquals("env-secret-key-100", cred.getApiKey());
              assertEquals("X-Shop-Key", cred.getHeaderName());
              return null;
            })
        .when(mockProvider)
        .connect(any(ProviderCredential.class), anyMap());

    when(mockProvider.execute(any(ProviderRequest.class))).thenReturn(mockResponse);

    Map<String, Object> config = Map.of("providerType", providerType, "path", "/test");
    ActionContext context =
        ActionContext.builder().tenantId(UUID.randomUUID()).configuration(config).build();

    ActionResult result = handler.execute(context);
    assertTrue(result.isSuccess());
    verify(mockProvider, times(1)).connect(any(ProviderCredential.class), anyMap());
  }
}
