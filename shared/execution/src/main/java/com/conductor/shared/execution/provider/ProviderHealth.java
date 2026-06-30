package com.conductor.shared.execution.provider;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/** Represents the health status and latency of a provider integration. */
@Getter
@Builder
public class ProviderHealth {

  public enum Status {
    UP,
    DOWN,
    DEGRADED,
    UNKNOWN
  }

  private final Status status;
  private final String message;
  private final long latencyMs;
  @Builder.Default private final Instant timestamp = Instant.now();
}
