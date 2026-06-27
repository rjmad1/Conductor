package com.rajajeevan.loop.engine;

import com.rajajeevan.loop.telemetry.TelemetryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core state machine logic for loop phase transitions. Defines allowed transitions, validates gate
 * criteria, manages circuit breaker thresholds, and records immutable audit entries. Pure domain
 * logic — no HTTP or framework dependency.
 */
public class LoopStateMachine {

  /** Allowed forward transitions: source → set of valid targets. */
  private static final Map<LoopPhase, Set<LoopPhase>> ALLOWED_TRANSITIONS =
      Map.of(
          LoopPhase.DISCOVERY, Set.of(LoopPhase.PLANNING),
          LoopPhase.PLANNING, Set.of(LoopPhase.IMPLEMENTATION),
          LoopPhase.IMPLEMENTATION, Set.of(LoopPhase.VERIFICATION),
          LoopPhase.VERIFICATION, Set.of(LoopPhase.REFLECTION, LoopPhase.IMPLEMENTATION),
          LoopPhase.REFLECTION, Set.of(LoopPhase.PLANNING, LoopPhase.DISCOVERY));

  /** Suggested next steps after arriving at a given phase. */
  private static final Map<LoopPhase, List<String>> PHASE_NEXT_STEPS =
      Map.of(
          LoopPhase.DISCOVERY,
              List.of("Scan repository structure", "Catalogue APIs and dependencies"),
          LoopPhase.PLANNING, List.of("Generate implementation plan", "Define target test cases"),
          LoopPhase.IMPLEMENTATION, List.of("Execute code changes", "Run compilation checks"),
          LoopPhase.VERIFICATION, List.of("Execute regression test suite", "Run static analysis"),
          LoopPhase.REFLECTION,
              List.of(
                  "Analyse failure root cause",
                  "Generate remediation recipe",
                  "Update agent context"));

  private final ConcurrentHashMap<String, LoopInstance> loops = new ConcurrentHashMap<>();

  /**
   * Registers a new loop instance. If the loop already exists, returns the existing instance.
   *
   * @param loopId Unique loop identifier
   * @param initialPhase Starting phase
   * @param gateCriteria Gate criteria strings
   * @param maxRetries Max retry count
   * @param circuitBreakerThreshold Circuit breaker failure threshold
   * @return The loop instance
   */
  public LoopInstance registerLoop(
      String loopId,
      LoopPhase initialPhase,
      List<String> gateCriteria,
      int maxRetries,
      int circuitBreakerThreshold) {
    return loops.computeIfAbsent(
        loopId,
        id ->
            new LoopInstance(id, initialPhase, gateCriteria, maxRetries, circuitBreakerThreshold));
  }

  /** Returns the loop instance for the given ID, or null if not registered. */
  public LoopInstance getLoop(String loopId) {
    return loops.get(loopId);
  }

  /**
   * Evaluates a requested state transition and returns a response indicating whether the transition
   * is allowed, along with the updated state and circuit breaker status.
   *
   * @param request The transit request
   * @return TransitResponse with the evaluation result
   */
  public TransitResponse evaluateTransition(TransitRequest request) {
    String loopId = request.getLoopId();

    LoopPhase sourcePhase = parsePhase(request.getSourcePhase());
    LoopPhase targetPhase = parsePhase(request.getTargetPhase());

    if (sourcePhase == null) {
      return errorResponse("Invalid source_phase: " + request.getSourcePhase());
    }
    if (targetPhase == null) {
      return errorResponse("Invalid target_phase: " + request.getTargetPhase());
    }

    io.opentelemetry.api.trace.Span span =
        TelemetryService.startSpan("evaluateTransition", loopId, targetPhase.name());

    try {
      LoopInstance loop =
          loops.computeIfAbsent(loopId, id -> new LoopInstance(id, sourcePhase, List.of(), 3, 3));

      // Verify source phase matches the loop's current state
      if (loop.getCurrentPhase() != sourcePhase) {
        return TransitResponse.builder()
            .transitionAllowed(false)
            .currentState(loop.getCurrentPhase().name())
            .nextSteps(PHASE_NEXT_STEPS.getOrDefault(loop.getCurrentPhase(), List.of()))
            .circuitBreaker(buildCbStatus(loop))
            .message(
                "Phase mismatch: loop is at "
                    + loop.getCurrentPhase().name()
                    + ", but source_phase is "
                    + sourcePhase.name())
            .build();
      }

      // Check circuit breaker
      if (loop.isCircuitBreakerTripped()) {
        TelemetryService.recordCircuitBreakerTransition(
            loopId, loop.getCurrentPhase().name(), "BLOCKED");
        return TransitResponse.builder()
            .transitionAllowed(false)
            .currentState(loop.getCurrentPhase().name())
            .nextSteps(
                List.of("Circuit breaker is tripped. Manual intervention or reset required."))
            .circuitBreaker(buildCbStatus(loop))
            .message("Circuit breaker is OPEN. Transition blocked.")
            .build();
      }

      // Validate the transition is structurally allowed
      Set<LoopPhase> validTargets = ALLOWED_TRANSITIONS.getOrDefault(sourcePhase, Set.of());
      if (!validTargets.contains(targetPhase)) {
        loop.recordFailure();
        return TransitResponse.builder()
            .transitionAllowed(false)
            .currentState(loop.getCurrentPhase().name())
            .nextSteps(validTargets.stream().map(LoopPhase::name).toList())
            .circuitBreaker(buildCbStatus(loop))
            .message(
                "Invalid transition: "
                    + sourcePhase.name()
                    + " → "
                    + targetPhase.name()
                    + " is not allowed. Valid targets: "
                    + validTargets)
            .build();
      }

      // Transition is valid — execute it
      loop.transitionTo(targetPhase);

      return TransitResponse.builder()
          .transitionAllowed(true)
          .currentState(targetPhase.name())
          .nextSteps(PHASE_NEXT_STEPS.getOrDefault(targetPhase, List.of()))
          .circuitBreaker(buildCbStatus(loop))
          .message("Transition accepted: " + sourcePhase.name() + " → " + targetPhase.name())
          .build();
    } catch (Throwable t) {
      TelemetryService.recordException(span, t);
      throw t;
    } finally {
      span.end();
    }
  }

  /** Builds a status response for a given loop. */
  public TransitResponse buildStatusResponse(LoopInstance loop) {
    return TransitResponse.builder()
        .transitionAllowed(!loop.isCircuitBreakerTripped())
        .currentState(loop.getCurrentPhase().name())
        .nextSteps(PHASE_NEXT_STEPS.getOrDefault(loop.getCurrentPhase(), List.of()))
        .circuitBreaker(buildCbStatus(loop))
        .message("Loop status: " + loop.getCurrentPhase().name())
        .build();
  }

  private Map<String, Object> buildCbStatus(LoopInstance loop) {
    return Map.of(
        "tripped", loop.isCircuitBreakerTripped(),
        "current_failures", loop.getConsecutiveFailures().get(),
        "threshold", loop.getCircuitBreakerThreshold());
  }

  private LoopPhase parsePhase(String phase) {
    if (phase == null) {
      return null;
    }
    try {
      return LoopPhase.valueOf(phase.toUpperCase().replace(" ", "_"));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private TransitResponse errorResponse(String message) {
    return TransitResponse.builder()
        .transitionAllowed(false)
        .currentState("UNKNOWN")
        .nextSteps(List.of())
        .circuitBreaker(Map.of("tripped", false, "current_failures", 0, "threshold", 0))
        .message(message)
        .build();
  }
}
