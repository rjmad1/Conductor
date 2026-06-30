package com.conductor.integrations.framework;

public record ConnectorHealthResult(ConnectorStatus status, String message, long latencyMs) {
  public static ConnectorHealthResult healthy(long latencyMs) {
    return new ConnectorHealthResult(ConnectorStatus.HEALTHY, "OK", latencyMs);
  }

  public static ConnectorHealthResult degraded(String reason, long latencyMs) {
    return new ConnectorHealthResult(ConnectorStatus.DEGRADED, reason, latencyMs);
  }

  public static ConnectorHealthResult unavailable(String reason, long latencyMs) {
    return new ConnectorHealthResult(ConnectorStatus.UNAVAILABLE, reason, latencyMs);
  }

  public static ConnectorHealthResult unknown(long latencyMs) {
    return new ConnectorHealthResult(ConnectorStatus.UNKNOWN, "Status undetermined", latencyMs);
  }
}
