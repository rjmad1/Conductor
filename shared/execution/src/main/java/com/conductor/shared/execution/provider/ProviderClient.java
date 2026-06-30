package com.conductor.shared.execution.provider;

import com.conductor.shared.execution.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Reusable HTTP execution framework for interacting with external providers. Supports timeouts,
 * retries, rate limiting, circuit breakers, idempotency keys, correlation IDs, metrics, and
 * structured logging.
 */
@Component
public class ProviderClient {

  private static final Logger log = LoggerFactory.getLogger(ProviderClient.class);

  private final RestTemplate restTemplate;
  private final MeterRegistry meterRegistry;

  private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
  private final Map<String, ProviderRateLimiter> rateLimiters = new ConcurrentHashMap<>();

  private final RetryPolicy defaultRetryPolicy =
      RetryPolicy.builder()
          .maxAttempts(3)
          .initialIntervalSeconds(2)
          .backoffCoefficient(2.0)
          .maxIntervalSeconds(30)
          .build();

  public ProviderClient(@Autowired(required = false) MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.restTemplate = new RestTemplate();

    // Configure standard timeouts on simple HTTP request factory
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(10000);
    this.restTemplate.setRequestFactory(factory);

    // Prevent RestTemplate from throwing exceptions on non-2xx codes automatically,
    // so we can capture status code and implement custom retry rules.
    this.restTemplate.setErrorHandler(
        new DefaultResponseErrorHandler() {
          @Override
          public void handleError(@NonNull ClientHttpResponse response) throws IOException {
            // Do not throw exceptions, allow the framework to capture error codes.
          }
        });
  }

  @SuppressWarnings("null")
  public ProviderResponse execute(
      String providerName, ProviderRequest request, ProviderCredential credential) {
    log.info("Executing request to provider: {} at path: {}", providerName, request.getPath());

    // 1. Resolve Circuit Breaker
    CircuitBreaker breaker =
        circuitBreakers.computeIfAbsent(
            providerName.toUpperCase(),
            k -> new SimpleCircuitBreaker(5, 10000)); // trip after 5 failures, 10s cool-off

    if (!breaker.allowRequest()) {
      recordFailureMetric(providerName, "circuit_breaker_open");
      throw new IllegalStateException("Circuit breaker is OPEN for provider: " + providerName);
    }

    // 2. Resolve Rate Limiter
    RateLimitPolicy limitPolicy =
        request.getRateLimitPolicy() != null
            ? request.getRateLimitPolicy()
            : RateLimitPolicy.builder().enabled(false).build();

    if (limitPolicy.isEnabled()) {
      ProviderRateLimiter limiter =
          rateLimiters.computeIfAbsent(
              providerName.toUpperCase(),
              k ->
                  new ProviderRateLimiter(
                      limitPolicy.getRequestsPerSecond(), limitPolicy.getBurstLimit()));

      if (!limiter.tryAcquire()) {
        recordRateLimitEvent(providerName);
        breaker.recordFailure(new RuntimeException("Rate limit exceeded"));
        throw new IllegalStateException("Rate limit exceeded for provider: " + providerName);
      }
    }

    // 3. Build URL with query params
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(request.getPath());
    if (request.getQueryParams() != null) {
      request.getQueryParams().forEach(uriBuilder::queryParam);
    }
    String finalUrl = uriBuilder.toUriString();

    // 4. Setup Headers
    HttpHeaders headers = new HttpHeaders();
    if (request.getHeaders() != null) {
      request.getHeaders().forEach(headers::add);
    }

    // Correlation ID
    String correlationId =
        request.getCorrelationId() != null
            ? request.getCorrelationId()
            : UUID.randomUUID().toString();
    headers.set("X-Correlation-Id", correlationId);

    // Idempotency Key
    if (request.getIdempotencyKey() != null) {
      headers.set("Idempotency-Key", request.getIdempotencyKey());
    }

    // Apply Credentials
    if (credential != null) {
      switch (credential.getAuthType()) {
        case API_KEY -> {
          String keyName =
              credential.getHeaderName() != null ? credential.getHeaderName() : "X-API-Key";
          headers.set(keyName, credential.getApiKey());
        }
        case BEARER_TOKEN -> {
          String tokenVal = credential.getToken();
          headers.set(
              "Authorization", tokenVal.startsWith("Bearer ") ? tokenVal : "Bearer " + tokenVal);
        }
        case OAUTH2 -> {
          headers.set("Authorization", "Bearer " + credential.getAccessToken());
        }
        default -> {
          // certificate / custom credentials handled separately
        }
      }
    }

    // 5. Execution loop with Retries
    RetryPolicy retryPolicy =
        request.getRetryPolicy() != null ? request.getRetryPolicy() : defaultRetryPolicy;
    int maxAttempts = retryPolicy.getMaxAttempts();
    long backoffMs = retryPolicy.getInitialIntervalSeconds() * 1000;
    double multiplier = retryPolicy.getBackoffCoefficient();
    long maxIntervalMs = retryPolicy.getMaxIntervalSeconds() * 1000;

    ProviderResponse response = null;
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      long startTime = System.nanoTime();
      try {
        log.debug("HTTP call attempt {}/{} to URL: {}", attempt, maxAttempts, finalUrl);

        HttpEntity<Object> entity = new HttpEntity<>(request.getBody(), headers);
        HttpMethod httpMethod =
            HttpMethod.valueOf(
                request.getMethod() != null ? request.getMethod().toUpperCase() : "POST");

        ResponseEntity<String> result =
            restTemplate.exchange(finalUrl, httpMethod, entity, String.class);

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        boolean isSuccess = result.getStatusCode().is2xxSuccessful();

        response =
            ProviderResponse.builder()
                .statusCode(result.getStatusCode().value())
                .headers(result.getHeaders().toSingleValueMap())
                .body(result.getBody())
                .success(isSuccess)
                .latencyMs(durationMs)
                .build();

        recordRequestCompletedMetric(providerName, request.getMethod(), isSuccess, durationMs);

        if (isSuccess) {
          breaker.recordSuccess();
          log.info(
              "Request successful: status={}, latency={}ms, correlationId={}",
              result.getStatusCode().value(),
              durationMs,
              correlationId);
          return response;
        } else {
          // If 5xx, we might want to retry
          if (result.getStatusCode().is5xxServerError() && attempt < maxAttempts) {
            recordRetryMetric(providerName);
            sleep(backoffMs);
            backoffMs = Math.min((long) (backoffMs * multiplier), maxIntervalMs);
            continue;
          }
          breaker.recordFailure(
              new RuntimeException("Status code: " + result.getStatusCode().value()));
          log.warn(
              "Request completed with failure: status={}, correlationId={}",
              result.getStatusCode().value(),
              correlationId);
          return response;
        }

      } catch (Exception ex) {
        lastException = ex;
        recordFailureMetric(providerName, ex.getClass().getSimpleName());

        log.warn(
            "Request failed on attempt {}/{}: {} (correlationId={})",
            attempt,
            maxAttempts,
            ex.getMessage(),
            correlationId);

        if (attempt < maxAttempts) {
          recordRetryMetric(providerName);
          sleep(backoffMs);
          backoffMs = Math.min((long) (backoffMs * multiplier), maxIntervalMs);
        }
      }
    }

    // Trip the breaker if we exhausted all retries and still failed
    if (lastException != null) {
      breaker.recordFailure(lastException);
      throw new RuntimeException("Provider call exhausted retries and failed", lastException);
    }

    if (response == null) {
      throw new RuntimeException("Provider call failed with no response");
    }
    breaker.recordFailure(
        new RuntimeException("Server error response: status=" + response.getStatusCode()));
    return response;
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Retry interrupted", e);
    }
  }

  // --- Observability & Metrics Helpers ---

  private void recordRequestCompletedMetric(
      String provider, String method, boolean success, long durationMs) {
    if (meterRegistry != null) {
      Counter.builder("provider.request.count")
          .tag("provider", provider)
          .tag("method", method)
          .tag("status", success ? "success" : "failure")
          .register(meterRegistry)
          .increment();

      Timer.builder("provider.request.latency")
          .tag("provider", provider)
          .tag("method", method)
          .register(meterRegistry)
          .record(Duration.ofMillis(durationMs));
    }
  }

  private void recordFailureMetric(String provider, String reason) {
    if (meterRegistry != null) {
      Counter.builder("provider.request.failed")
          .tag("provider", provider)
          .tag("reason", reason)
          .register(meterRegistry)
          .increment();
    }
  }

  private void recordRetryMetric(String provider) {
    if (meterRegistry != null) {
      Counter.builder("provider.retry.count")
          .tag("provider", provider)
          .register(meterRegistry)
          .increment();
    }
  }

  private void recordRateLimitEvent(String provider) {
    if (meterRegistry != null) {
      Counter.builder("provider.ratelimit.events")
          .tag("provider", provider)
          .register(meterRegistry)
          .increment();
    }
  }
}
