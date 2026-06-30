package com.conductor.shared.execution.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Spring-backed implementation of ProviderRegistry. Automatically wires all Provider beans on
 * startup and maps them by provider type. Supports thread-safe dynamic runtime registration.
 */
@Service
public class SpringProviderRegistry implements ProviderRegistry {

  private final Map<String, Provider> providers = new ConcurrentHashMap<>();

  public SpringProviderRegistry(List<Provider> providerList) {
    if (providerList != null) {
      for (Provider provider : providerList) {
        register(provider);
      }
    }
  }

  @Override
  public Optional<Provider> getProvider(String providerType) {
    if (providerType == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(providers.get(providerType.toUpperCase()));
  }

  @Override
  public Collection<Provider> getProviders() {
    return providers.values();
  }

  @Override
  public void register(Provider provider) {
    if (provider != null
        && provider.getDefinition() != null
        && provider.getDefinition().getType() != null) {
      providers.put(provider.getDefinition().getType().toUpperCase(), provider);
    }
  }
}
