package com.conductor.shared.execution.provider;

/** Interface representing a circuit breaker extension point for provider requests. */
public interface CircuitBreaker {

  /** Checks if the request is permitted to proceed. */
  boolean allowRequest();

  /** Records a successful execution. */
  void recordSuccess();

  /** Records a failed execution. */
  void recordFailure(Throwable throwable);

  /** Returns the current state of the circuit breaker. */
  String getState();
}
