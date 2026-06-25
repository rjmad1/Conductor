package com.conductor.shared.execution.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProviderRegistryTest {

  private static class DummyProvider implements Provider {
    private final String type;

    public DummyProvider(String type) {
      this.type = type != null ? type.toUpperCase() : null;
    }

    @Override
    public ProviderDefinition getDefinition() {
      return ProviderDefinition.builder()
          .type(type)
          .name(type + " Test")
          .version("1.0")
          .supportedActions(List.of("TEST_ACTION"))
          .build();
    }

    @Override
    public void connect(ProviderCredential credential, Map<String, Object> params) {}

    @Override
    public void disconnect() {}

    @Override
    public ProviderResponse execute(ProviderRequest request) {
      return ProviderResponse.builder().statusCode(200).success(true).body("ok").build();
    }

    @Override
    public ProviderHealth health() {
      return ProviderHealth.builder().status(ProviderHealth.Status.UP).latencyMs(1).build();
    }

    @Override
    public boolean validateConfiguration(Map<String, Object> config) {
      return true;
    }

    @Override
    public boolean verifyWebhook(Map<String, String> headers, String body, String secret) {
      return true;
    }

    @Override
    public ProviderCredential refreshCredentials(ProviderCredential credential) {
      return credential;
    }
  }

  @Test
  void testRegisterAndResolve() {
    List<Provider> providers = new ArrayList<>();
    providers.add(new DummyProvider("Shopify"));
    providers.add(new DummyProvider("Zoho"));

    SpringProviderRegistry registry = new SpringProviderRegistry(providers);

    Optional<Provider> shopify = registry.getProvider("Shopify");
    assertTrue(shopify.isPresent());
    assertEquals("SHOPIFY", shopify.get().getDefinition().getType());

    Optional<Provider> shopifyUpper = registry.getProvider("SHOPIFY");
    assertTrue(shopifyUpper.isPresent());

    Optional<Provider> shopifyLower = registry.getProvider("shopify");
    assertTrue(shopifyLower.isPresent());

    Optional<Provider> zoho = registry.getProvider("ZOHO");
    assertTrue(zoho.isPresent());

    Optional<Provider> missing = registry.getProvider("MISSING");
    assertFalse(missing.isPresent());
  }

  @Test
  void testDynamicRegistration() {
    SpringProviderRegistry registry = new SpringProviderRegistry(List.of());
    assertFalse(registry.getProvider("dynamic").isPresent());

    registry.register(new DummyProvider("Dynamic"));
    Optional<Provider> provider = registry.getProvider("dynamic");
    assertTrue(provider.isPresent());
    assertEquals("DYNAMIC", provider.get().getDefinition().getType());
  }
}
