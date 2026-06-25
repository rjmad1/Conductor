package com.conductor.integrations.framework;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ConnectorRegistry {

  private final Map<String, ConnectorAdapter> registry = new ConcurrentHashMap<>();

  public ConnectorRegistry(List<ConnectorAdapter> adapters) {
    for (ConnectorAdapter adapter : adapters) {
      registry.put(adapter.getConnectorType().toLowerCase(), adapter);
    }
  }

  public Optional<ConnectorAdapter> getAdapter(String type) {
    if (type == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(registry.get(type.toLowerCase()));
  }
}
