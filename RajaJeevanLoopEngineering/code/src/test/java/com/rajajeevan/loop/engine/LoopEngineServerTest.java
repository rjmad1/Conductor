package com.rajajeevan.loop.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Integration tests for LoopEngineServer HTTP endpoints using Javalin test tools. */
class LoopEngineServerTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private Javalin createTestApp() {
    LoopEngineServer server = new LoopEngineServer();
    return server.getApp();
  }

  @Test
  @DisplayName("GET /health returns UP")
  void healthCheck() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          var resp = client.get("/health");
          assertThat(resp.code()).isEqualTo(200);
          assertThat(resp.body().string()).contains("UP");
        });
  }

  @Test
  @DisplayName("POST /api/v1/loops/transit allows valid transition")
  void validTransit() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          Map<String, Object> payload =
              Map.of(
                  "loop_id", "test-loop",
                  "source_phase", "DISCOVERY",
                  "target_phase", "PLANNING",
                  "execution_logs", "Scan completed successfully.");

          var resp = client.post("/api/v1/loops/transit", payload);

          assertThat(resp.code()).isEqualTo(200);
          String body = resp.body().string();
          assertThat(body).contains("\"transition_allowed\":true");
          assertThat(body).contains("\"current_state\":\"PLANNING\"");
        });
  }

  @Test
  @DisplayName("POST /api/v1/loops/transit rejects invalid transition")
  void invalidTransit() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          Map<String, Object> payload =
              Map.of(
                  "loop_id", "test-loop-2",
                  "source_phase", "DISCOVERY",
                  "target_phase", "VERIFICATION");

          var resp = client.post("/api/v1/loops/transit", payload);

          assertThat(resp.code()).isEqualTo(409);
          String body = resp.body().string();
          assertThat(body).contains("\"transition_allowed\":false");
          assertThat(body).contains("not allowed");
        });
  }

  @Test
  @DisplayName("POST /api/v1/loops/transit returns 400 for missing fields")
  void missingFields() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          Map<String, Object> payload = Map.of("loop_id", "incomplete");

          var resp = client.post("/api/v1/loops/transit", payload);

          assertThat(resp.code()).isEqualTo(400);
        });
  }

  @Test
  @DisplayName("GET /api/v1/loops/{loopId}/status returns 404 for unknown loop")
  void statusNotFound() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          var resp = client.get("/api/v1/loops/nonexistent/status");
          assertThat(resp.code()).isEqualTo(404);
        });
  }

  @Test
  @DisplayName("GET /api/v1/loops/{loopId}/status returns status after transit")
  void statusAfterTransit() {
    JavalinTest.test(
        createTestApp(),
        (server, client) -> {
          // First, create the loop via transit
          Map<String, Object> payload =
              Map.of(
                  "loop_id", "status-test",
                  "source_phase", "DISCOVERY",
                  "target_phase", "PLANNING");

          client.post("/api/v1/loops/transit", payload);

          // Now check status
          var resp = client.get("/api/v1/loops/status-test/status");
          assertThat(resp.code()).isEqualTo(200);
          String body = resp.body().string();
          assertThat(body).contains("\"current_state\":\"PLANNING\"");
        });
  }
}
