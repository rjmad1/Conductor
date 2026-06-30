package com.rajajeevan.loop.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for LoopStateMachine — pure domain logic, no HTTP. */
class LoopStateMachineTest {

  private LoopStateMachine machine;

  @BeforeEach
  void setUp() {
    machine = new LoopStateMachine();
  }

  @Test
  @DisplayName("Valid forward transition: DISCOVERY → PLANNING")
  void validTransitionDiscoveryToPlanning() {
    machine.registerLoop("loop-1", LoopPhase.DISCOVERY, List.of("scan"), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-1");
    req.setSourcePhase("DISCOVERY");
    req.setTargetPhase("PLANNING");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isTrue();
    assertThat(resp.getCurrentState()).isEqualTo("PLANNING");
  }

  @Test
  @DisplayName("Valid forward transition: PLANNING → IMPLEMENTATION")
  void validTransitionPlanningToImplementation() {
    machine.registerLoop("loop-2", LoopPhase.PLANNING, List.of(), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-2");
    req.setSourcePhase("PLANNING");
    req.setTargetPhase("IMPLEMENTATION");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isTrue();
    assertThat(resp.getCurrentState()).isEqualTo("IMPLEMENTATION");
  }

  @Test
  @DisplayName("Valid backward transition: VERIFICATION → IMPLEMENTATION (retry)")
  void validRetryTransition() {
    machine.registerLoop("loop-3", LoopPhase.VERIFICATION, List.of(), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-3");
    req.setSourcePhase("VERIFICATION");
    req.setTargetPhase("IMPLEMENTATION");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isTrue();
    assertThat(resp.getCurrentState()).isEqualTo("IMPLEMENTATION");
  }

  @Test
  @DisplayName("Invalid transition: DISCOVERY → VERIFICATION is rejected")
  void invalidTransitionRejected() {
    machine.registerLoop("loop-4", LoopPhase.DISCOVERY, List.of(), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-4");
    req.setSourcePhase("DISCOVERY");
    req.setTargetPhase("VERIFICATION");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isFalse();
    assertThat(resp.getMessage()).contains("not allowed");
  }

  @Test
  @DisplayName("Phase mismatch is rejected")
  void phaseMismatchRejected() {
    machine.registerLoop("loop-5", LoopPhase.DISCOVERY, List.of(), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-5");
    req.setSourcePhase("PLANNING");
    req.setTargetPhase("IMPLEMENTATION");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isFalse();
    assertThat(resp.getMessage()).contains("Phase mismatch");
  }

  @Test
  @DisplayName("Circuit breaker trips after threshold failures")
  void circuitBreakerTrips() {
    LoopInstance loop = machine.registerLoop("loop-6", LoopPhase.DISCOVERY, List.of(), 3, 2);

    // Record 2 failures to exceed threshold of 2
    loop.recordFailure();
    loop.recordFailure();

    assertThat(loop.isCircuitBreakerTripped()).isTrue();

    TransitRequest req = new TransitRequest();
    req.setLoopId("loop-6");
    req.setSourcePhase("DISCOVERY");
    req.setTargetPhase("PLANNING");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isFalse();
    assertThat(resp.getMessage()).contains("Circuit breaker");
  }

  @Test
  @DisplayName("Auto-registers loop on first transit request")
  void autoRegistersLoop() {
    TransitRequest req = new TransitRequest();
    req.setLoopId("auto-loop");
    req.setSourcePhase("DISCOVERY");
    req.setTargetPhase("PLANNING");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isTrue();
    assertThat(machine.getLoop("auto-loop")).isNotNull();
  }

  @Test
  @DisplayName("Invalid phase name returns error response")
  void invalidPhaseName() {
    TransitRequest req = new TransitRequest();
    req.setLoopId("bad-phase");
    req.setSourcePhase("INVALID_PHASE");
    req.setTargetPhase("PLANNING");

    TransitResponse resp = machine.evaluateTransition(req);
    assertThat(resp.isTransitionAllowed()).isFalse();
    assertThat(resp.getMessage()).contains("Invalid source_phase");
  }

  @Test
  @DisplayName("Status response shows correct state")
  void statusResponse() {
    LoopInstance loop =
        machine.registerLoop("status-loop", LoopPhase.IMPLEMENTATION, List.of(), 3, 3);

    TransitResponse resp = machine.buildStatusResponse(loop);
    assertThat(resp.getCurrentState()).isEqualTo("IMPLEMENTATION");
    assertThat(resp.isTransitionAllowed()).isTrue();
  }

  @Test
  @DisplayName("Audit log records transitions")
  void auditLogRecordsTransitions() {
    LoopInstance loop = machine.registerLoop("audit-loop", LoopPhase.DISCOVERY, List.of(), 3, 3);

    TransitRequest req = new TransitRequest();
    req.setLoopId("audit-loop");
    req.setSourcePhase("DISCOVERY");
    req.setTargetPhase("PLANNING");
    machine.evaluateTransition(req);

    assertThat(loop.getAuditLog()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(loop.getAuditLog().get(0).event()).isEqualTo("INITIALIZED");
    assertThat(loop.getAuditLog().get(1).event()).isEqualTo("TRANSITION");
  }

  @Test
  @DisplayName("Full lifecycle: DISCOVERY → PLANNING → IMPLEMENTATION → VERIFICATION")
  void fullLifecycle() {
    machine.registerLoop("full-loop", LoopPhase.DISCOVERY, List.of(), 3, 3);

    String[][] transitions = {
      {"DISCOVERY", "PLANNING"},
      {"PLANNING", "IMPLEMENTATION"},
      {"IMPLEMENTATION", "VERIFICATION"}
    };

    for (String[] t : transitions) {
      TransitRequest req = new TransitRequest();
      req.setLoopId("full-loop");
      req.setSourcePhase(t[0]);
      req.setTargetPhase(t[1]);
      TransitResponse resp = machine.evaluateTransition(req);
      assertThat(resp.isTransitionAllowed())
          .as("Transition %s → %s should be allowed", t[0], t[1])
          .isTrue();
    }

    assertThat(machine.getLoop("full-loop").getCurrentPhase()).isEqualTo(LoopPhase.VERIFICATION);
  }
}
