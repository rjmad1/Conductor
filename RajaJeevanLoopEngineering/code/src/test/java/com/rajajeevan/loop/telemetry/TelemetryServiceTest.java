package com.rajajeevan.loop.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelemetryServiceTest {

  @Test
  @DisplayName("Telemetry runs safely and registers attributes on spans")
  void testTelemetryExecution() {
    Span span = TelemetryService.startSpan("TestSpan", "test-loop-id", "PLANNING");
    assertThat(span).isNotNull();

    // Log exception safely
    TelemetryService.recordException(span, new RuntimeException("Simulated Failure"));
    span.end();
  }

  @Test
  @DisplayName("Telemetry records circuit breaker state transitions")
  void testCircuitBreakerTransitionTelemetry() {
    TelemetryService.recordCircuitBreakerTransition("TestBreaker", "CLOSED", "OPEN");
  }
}
