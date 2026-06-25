package com.conductor.shared.execution.provider;

import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe token bucket rate limiter implementation for provider requests. */
public class ProviderRateLimiter {

  private final double permitsPerSecond;
  private final long maxBurst;
  private final AtomicLong tokens = new AtomicLong();
  private volatile long lastRefillTime = System.currentTimeMillis();

  public ProviderRateLimiter(double permitsPerSecond, long maxBurst) {
    this.permitsPerSecond = permitsPerSecond;
    this.maxBurst = maxBurst;
    this.tokens.set(maxBurst);
  }

  public synchronized boolean tryAcquire() {
    refill();
    if (tokens.get() >= 1) {
      tokens.decrementAndGet();
      return true;
    }
    return false;
  }

  private void refill() {
    long now = System.currentTimeMillis();
    long delta = now - lastRefillTime;
    if (delta > 0) {
      double newTokens = delta * (permitsPerSecond / 1000.0);
      long current = tokens.get();
      long updated = Math.min(maxBurst, current + (long) newTokens);
      tokens.set(updated);
      lastRefillTime = now;
    }
  }
}
