package com.rajajeevan.loop.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/**
 * Runtime state object for a single loop execution. Tracks current phase, gate criteria, circuit
 * breaker state, and an immutable audit log of all transitions.
 */
@Getter
public class LoopInstance {

  private final String loopId;
  private volatile LoopPhase currentPhase;
  private final List<String> gateCriteria;
  private final int maxRetries;
  private final int circuitBreakerThreshold;
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private volatile boolean circuitBreakerTripped = false;
  private final List<AuditEntry> auditLog = Collections.synchronizedList(new ArrayList<>());

  public LoopInstance(
      String loopId,
      LoopPhase initialPhase,
      List<String> gateCriteria,
      int maxRetries,
      int circuitBreakerThreshold) {
    this.loopId = loopId;
    this.currentPhase = initialPhase;
    this.gateCriteria = gateCriteria != null ? new ArrayList<>(gateCriteria) : new ArrayList<>();
    this.maxRetries = maxRetries > 0 ? maxRetries : 3;
    this.circuitBreakerThreshold = circuitBreakerThreshold > 0 ? circuitBreakerThreshold : 3;

    addAuditEntry("INITIALIZED", "Loop created at phase " + initialPhase.name());
  }

  public void transitionTo(LoopPhase newPhase) {
    LoopPhase previous = this.currentPhase;
    this.currentPhase = newPhase;
    this.consecutiveFailures.set(0);
    addAuditEntry("TRANSITION", "Phase changed from " + previous.name() + " to " + newPhase.name());
  }

  public int recordFailure() {
    int failures = consecutiveFailures.incrementAndGet();
    if (failures >= circuitBreakerThreshold) {
      circuitBreakerTripped = true;
      addAuditEntry("CIRCUIT_BREAKER_TRIPPED", "Failures reached threshold: " + failures);
    } else {
      addAuditEntry("FAILURE_RECORDED", "Consecutive failures: " + failures);
    }
    return failures;
  }

  public void resetCircuitBreaker() {
    consecutiveFailures.set(0);
    circuitBreakerTripped = false;
    addAuditEntry("CIRCUIT_BREAKER_RESET", "Circuit breaker reset to closed state");
  }

  private void addAuditEntry(String event, String detail) {
    auditLog.add(new AuditEntry(Instant.now(), event, detail));
  }

  /** Immutable audit log entry. */
  public record AuditEntry(Instant timestamp, String event, String detail) {}
}
