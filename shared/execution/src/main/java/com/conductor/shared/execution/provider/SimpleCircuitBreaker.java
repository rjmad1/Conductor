package com.conductor.shared.execution.provider;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe default implementation of a Circuit Breaker. Transitions CLOSED -> OPEN on
 * consecutive failures, resets to HALF_OPEN after a timeout.
 */
public class SimpleCircuitBreaker implements CircuitBreaker {

  public enum State {
    CLOSED,
    OPEN,
    HALF_OPEN
  }

  private final int failureThreshold;
  private final long resetTimeoutMs;

  private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private final AtomicLong lastStateChangeTime = new AtomicLong(System.currentTimeMillis());

  public SimpleCircuitBreaker(int failureThreshold, long resetTimeoutMs) {
    this.failureThreshold = failureThreshold;
    this.resetTimeoutMs = resetTimeoutMs;
  }

  @Override
  public boolean allowRequest() {
    State currentState = state.get();
    if (currentState == State.OPEN) {
      long elapsed = System.currentTimeMillis() - lastStateChangeTime.get();
      if (elapsed >= resetTimeoutMs) {
        if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
          lastStateChangeTime.set(System.currentTimeMillis());
          return true;
        }
      }
      return false;
    }
    return true;
  }

  @Override
  public void recordSuccess() {
    consecutiveFailures.set(0);
    state.set(State.CLOSED);
  }

  @Override
  public void recordFailure(Throwable throwable) {
    consecutiveFailures.incrementAndGet();
    if (state.get() == State.HALF_OPEN || consecutiveFailures.get() >= failureThreshold) {
      if (state.compareAndSet(State.CLOSED, State.OPEN)
          || state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
        lastStateChangeTime.set(System.currentTimeMillis());
      }
    }
  }

  @Override
  public String getState() {
    return state.get().name();
  }
}
