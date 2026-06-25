package com.conductor.shared.execution.provider;

import com.conductor.shared.execution.RetryPolicy;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Encapsulates a generic provider request containing target endpoint, parameters, and
 * routing/policy directives.
 */
@Getter
@Builder
public class ProviderRequest {
  private final String path;
  private final String method; // e.g., "GET", "POST", "PUT", "DELETE"
  private final Map<String, String> headers;
  private final Map<String, String> queryParams;
  private final Object body;
  private final String idempotencyKey;
  private final String correlationId;
  private final RetryPolicy retryPolicy;
  private final RateLimitPolicy rateLimitPolicy;
}
