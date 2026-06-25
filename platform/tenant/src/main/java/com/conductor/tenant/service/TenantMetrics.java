package com.conductor.tenant.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TenantMetrics {

  private final MeterRegistry registry;

  public TenantMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  public void recordTenantCreated() {
    Counter.builder("tenant.creation")
        .description("Number of tenants created")
        .register(registry)
        .increment();
  }

  public void recordTenantUpdated() {
    Counter.builder("tenant.update")
        .description("Number of tenants updated")
        .register(registry)
        .increment();
  }

  public void recordTenantRetrieval() {
    Counter.builder("tenant.retrieval")
        .description("Number of tenants retrieved")
        .register(registry)
        .increment();
  }
}
