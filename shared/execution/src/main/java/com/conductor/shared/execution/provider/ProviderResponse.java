package com.conductor.shared.execution.provider;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** Encapsulates the response from a provider call, including status codes and payload. */
@Getter
@Builder
public class ProviderResponse {
  private final int statusCode;
  private final Map<String, String> headers;
  private final String body;
  private final boolean success;
  private final long latencyMs;
}
