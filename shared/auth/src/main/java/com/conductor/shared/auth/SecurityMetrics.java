package com.conductor.shared.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Micrometer metric collector for authentication and authorization events. */
@Component
public class SecurityMetrics {

  private final MeterRegistry registry;

  public SecurityMetrics() {
    this.registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
  }

  public SecurityMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  @org.springframework.beans.factory.annotation.Autowired
  public SecurityMetrics(ObjectProvider<MeterRegistry> registryProvider) {
    this.registry = registryProvider.getIfAvailable(SimpleMeterRegistry::new);
  }

  /** Records a successful authentication event. */
  public void recordAuthenticationSuccess() {
    Counter.builder("security.authentication.success")
        .description("Number of successful authentications")
        .register(registry)
        .increment();
  }

  /** Records a failed authentication event. */
  public void recordAuthenticationFailure() {
    Counter.builder("security.authentication.failure")
        .description("Number of failed authentications")
        .register(registry)
        .increment();
  }

  /** Records a failed authorization check. */
  public void recordAuthorizationFailure() {
    Counter.builder("security.authorization.failure")
        .description("Number of authorization failures")
        .register(registry)
        .increment();
  }

  /**
   * Records the latency associated with verifying the token signature and claims.
   *
   * @param duration validation duration
   */
  public void recordTokenValidationLatency(Duration duration) {
    Timer.builder("security.token.validation.latency")
        .description("Latency of token signature verification and validation")
        .register(registry)
        .record(duration);
  }
}
