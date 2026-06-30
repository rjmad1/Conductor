package com.conductor.shared.execution.provider;

import java.util.Collection;
import java.util.Optional;

/** Interface for registering and dynamically resolving provider implementations. */
public interface ProviderRegistry {

  /** Resolves the Provider instance for a given provider type. */
  Optional<Provider> getProvider(String providerType);

  /** Returns all currently registered providers. */
  Collection<Provider> getProviders();

  /** Dynamically registers a provider. */
  void register(Provider provider);
}
