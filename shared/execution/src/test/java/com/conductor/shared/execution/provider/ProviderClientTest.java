package com.conductor.shared.execution.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.conductor.shared.execution.RetryPolicy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProviderClientTest {

  private static HttpServer server;
  private static int port;
  private static final AtomicInteger requestCount = new AtomicInteger(0);
  private static final AtomicInteger retryCount = new AtomicInteger(0);

  @BeforeAll
  static void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    port = server.getAddress().getPort();

    server.createContext(
        "/ok",
        new HttpHandler() {
          @Override
          public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            // Verify headers
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            assertNotNull(exchange.getRequestHeaders().getFirst("Idempotency-Key"));

            byte[] response = "{\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
              os.write(response);
            }
          }
        });

    server.createContext(
        "/retry",
        new HttpHandler() {
          @Override
          public void handle(HttpExchange exchange) throws IOException {
            int attempt = retryCount.incrementAndGet();
            if (attempt < 3) {
              exchange.sendResponseHeaders(500, 0);
              exchange.close();
            } else {
              byte[] response = "{\"status\":\"recovered\"}".getBytes(StandardCharsets.UTF_8);
              exchange.sendResponseHeaders(200, response.length);
              try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
              }
            }
          }
        });

    server.start();
  }

  @AfterAll
  static void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void testSuccessfulExecution() {
    ProviderClient client = new ProviderClient(null);
    ProviderRequest request =
        ProviderRequest.builder()
            .path("http://localhost:" + port + "/ok")
            .method("GET")
            .idempotencyKey("idem-123")
            .correlationId("corr-123")
            .build();

    ProviderResponse response = client.execute("TestProvider", request, null);
    assertTrue(response.isSuccess());
    assertEquals(200, response.getStatusCode());
    assertEquals("{\"status\":\"success\"}", response.getBody());
  }

  @Test
  void testRetryFlow() {
    ProviderClient client = new ProviderClient(null);

    RetryPolicy policy =
        RetryPolicy.builder()
            .maxAttempts(3)
            .initialIntervalSeconds(1)
            .backoffCoefficient(1.5)
            .maxIntervalSeconds(5)
            .build();

    ProviderRequest request =
        ProviderRequest.builder()
            .path("http://localhost:" + port + "/retry")
            .method("POST")
            .idempotencyKey("idem-retry")
            .correlationId("corr-retry")
            .retryPolicy(policy)
            .build();

    retryCount.set(0);
    ProviderResponse response = client.execute("TestProviderRetry", request, null);
    assertTrue(response.isSuccess());
    assertEquals(200, response.getStatusCode());
    assertEquals(3, retryCount.get()); // two failures, 3rd succeeded
  }

  @Test
  void testRateLimiting() {
    ProviderClient client = new ProviderClient(null);

    RateLimitPolicy policy =
        RateLimitPolicy.builder().enabled(true).requestsPerSecond(1.0).burstLimit(1).build();

    ProviderRequest request =
        ProviderRequest.builder()
            .path("http://localhost:" + port + "/ok")
            .method("GET")
            .idempotencyKey("idem-limit")
            .rateLimitPolicy(policy)
            .build();

    ProviderResponse response1 = client.execute("LimitedProvider", request, null);
    assertTrue(response1.isSuccess());

    assertThrows(
        IllegalStateException.class,
        () -> {
          client.execute("LimitedProvider", request, null);
        });
  }

  @Test
  void testCircuitBreakerTripping() {
    ProviderClient client = new ProviderClient(null);

    ProviderRequest request =
        ProviderRequest.builder()
            .path("http://localhost:" + port + "/invalid-endpoint-non-existent")
            .method("GET")
            .idempotencyKey("idem-cb")
            .build();

    for (int i = 0; i < 5; i++) {
      try {
        client.execute("BrokenProvider", request, null);
      } catch (Exception ignored) {
      }
    }

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> {
              client.execute("BrokenProvider", request, null);
            });
    assertTrue(ex.getMessage().contains("Circuit breaker is OPEN"));
  }
}
