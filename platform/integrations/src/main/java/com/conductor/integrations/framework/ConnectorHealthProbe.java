package com.conductor.integrations.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Shared probe logic for all connector health checks. Performs a lightweight GET against a provider
 * meta-endpoint and maps the HTTP response to a ConnectorStatus: 2xx → HEALTHY 4xx (incl. 401) →
 * HEALTHY (API is up; auth context is tenant-level) 429 → DEGRADED (provider rate-limiting) 5xx →
 * UNAVAILABLE IOException/timeout → UNAVAILABLE, retried once before giving up
 */
@SuppressWarnings("null")
public final class ConnectorHealthProbe {

  private static final Logger log = LoggerFactory.getLogger(ConnectorHealthProbe.class);

  private ConnectorHealthProbe() {}

  public static ConnectorHealthResult probe(
      String connectorType, String url, RestTemplate restTemplate) {
    long start = System.currentTimeMillis();
    try {
      ConnectorStatus status = attempt(url, restTemplate);
      long latency = System.currentTimeMillis() - start;
      log.debug("{} health probe → {} ({}ms)", connectorType, status, latency);
      return new ConnectorHealthResult(
          status, status == ConnectorStatus.HEALTHY ? "OK" : status.name(), latency);
    } catch (ResourceAccessException firstEx) {
      log.debug(
          "{} health probe transient failure, retrying: {}", connectorType, firstEx.getMessage());
      try {
        ConnectorStatus status = attempt(url, restTemplate);
        long latency = System.currentTimeMillis() - start;
        log.debug("{} health probe retry → {} ({}ms)", connectorType, status, latency);
        return new ConnectorHealthResult(
            status, status == ConnectorStatus.HEALTHY ? "OK (retry)" : status.name(), latency);
      } catch (Exception retryEx) {
        long latency = System.currentTimeMillis() - start;
        log.warn("{} health probe failed after retry: {}", connectorType, retryEx.getMessage());
        return ConnectorHealthResult.unavailable(
            "Unreachable after retry: " + retryEx.getMessage(), latency);
      }
    } catch (Exception ex) {
      long latency = System.currentTimeMillis() - start;
      log.warn("{} health probe error: {}", connectorType, ex.getMessage());
      return ConnectorHealthResult.unavailable(ex.getMessage(), latency);
    }
  }

  private static ConnectorStatus attempt(String url, RestTemplate restTemplate) {
    try {
      restTemplate.getForEntity(url, String.class);
      return ConnectorStatus.HEALTHY;
    } catch (HttpClientErrorException ex) {
      int code = ex.getStatusCode().value();
      if (code == 429) {
        return ConnectorStatus.DEGRADED;
      }
      // 401, 403, etc. → API is up; auth missing is expected on a probe
      return ConnectorStatus.HEALTHY;
    } catch (HttpServerErrorException ex) {
      return ConnectorStatus.UNAVAILABLE;
    }
    // ResourceAccessException propagates to caller for retry
  }
}
