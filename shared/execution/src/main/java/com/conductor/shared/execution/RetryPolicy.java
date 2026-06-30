package com.conductor.shared.execution;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Configurable retry policy for workflow steps and activities. Maps directly to Temporal
 * RetryOptions.
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class RetryPolicy implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final int DEFAULT_MAX_ATTEMPTS = 3;
  public static final long DEFAULT_INITIAL_INTERVAL_SECONDS = 2;
  public static final double DEFAULT_BACKOFF_COEFFICIENT = 2.0;
  public static final long DEFAULT_MAX_INTERVAL_SECONDS = 120;

  @Builder.Default private final int maxAttempts = DEFAULT_MAX_ATTEMPTS;

  @Builder.Default private final long initialIntervalSeconds = DEFAULT_INITIAL_INTERVAL_SECONDS;

  @Builder.Default private final double backoffCoefficient = DEFAULT_BACKOFF_COEFFICIENT;

  @Builder.Default private final long maxIntervalSeconds = DEFAULT_MAX_INTERVAL_SECONDS;

  /** Converts to a human-readable summary for audit logging. */
  public String toAuditString() {
    return String.format(
        "maxAttempts=%d, initialInterval=%ds, backoff=%.1f, maxInterval=%ds",
        maxAttempts, initialIntervalSeconds, backoffCoefficient, maxIntervalSeconds);
  }
}
