package com.rajajeevan.loop.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Service providing OpenTelemetry instrumentation for AI Loop execution. Creates trace spans,
 * tracks parent/child contexts, and records telemetry attributes.
 */
public class TelemetryService {

  private static final String INSTRUMENTATION_NAME = "com.rajajeevan.loop.telemetry";
  private static final Tracer tracer = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME);

  /**
   * Starts a tracing span for a loop step.
   *
   * @param name Name of the loop phase or step
   * @param loopId Unique loop identifier
   * @param phase Current phase name
   * @return The started Span
   */
  public static Span startSpan(String name, String loopId, String phase) {
    Span span = tracer.spanBuilder(name).startSpan();

    if (loopId != null) {
      span.setAttribute("loop.id", loopId);
    }
    if (phase != null) {
      span.setAttribute("loop.phase", phase);
    }

    return span;
  }

  /**
   * Records a circuit breaker state transition.
   *
   * @param circuitBreakerName Name of the breaker
   * @param fromState Originating state
   * @param toState Targeted state
   */
  public static void recordCircuitBreakerTransition(
      String circuitBreakerName, String fromState, String toState) {
    Span span =
        tracer
            .spanBuilder("circuit_breaker_transition")
            .setAttribute("cb.name", circuitBreakerName)
            .setAttribute("cb.from_state", fromState)
            .setAttribute("cb.to_state", toState)
            .startSpan();

    span.addEvent("State changed from " + fromState + " to " + toState);
    span.end();
  }

  /**
   * Records an execution failure onto the current span.
   *
   * @param span Current span
   * @param throwable Exception that caused the failure
   */
  public static void recordException(Span span, Throwable throwable) {
    if (span != null && throwable != null) {
      span.setStatus(StatusCode.ERROR, throwable.getMessage());
      span.recordException(throwable);
    }
  }
}
