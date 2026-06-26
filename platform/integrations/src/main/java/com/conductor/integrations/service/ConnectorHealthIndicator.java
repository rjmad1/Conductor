package com.conductor.integrations.service;

import com.conductor.integrations.framework.ConnectorAdapter;
import com.conductor.integrations.framework.ConnectorHealthResult;
import com.conductor.integrations.framework.ConnectorStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("connector")
public class ConnectorHealthIndicator implements HealthIndicator {

  private final List<ConnectorAdapter> adapters;

  public ConnectorHealthIndicator(List<ConnectorAdapter> adapters) {
    this.adapters = adapters;
  }

  @Override
  public Health health() {
    Map<String, Object> details = new HashMap<>();
    boolean anyUnavailable = false;
    boolean anyDegraded = false;

    for (ConnectorAdapter adapter : adapters) {
      ConnectorHealthResult result = adapter.healthCheck(null);
      details.put(
          adapter.getConnectorType(),
          Map.of(
              "status", result.status().name(),
              "message", result.message(),
              "latencyMs", result.latencyMs()));
      if (result.status() == ConnectorStatus.UNAVAILABLE) {
        anyUnavailable = true;
      } else if (result.status() == ConnectorStatus.DEGRADED) {
        anyDegraded = true;
      }
    }

    if (anyUnavailable) {
      return Health.down().withDetails(details).build();
    }
    if (anyDegraded) {
      return Health.outOfService().withDetails(details).build();
    }
    return Health.up().withDetails(details).build();
  }
}
