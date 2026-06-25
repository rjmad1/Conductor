package com.conductor.tenant.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class TenantMetrics {

  private final MeterRegistry registry;

  public TenantMetrics(ObjectProvider<MeterRegistry> registryProvider) {
    this.registry = registryProvider.getIfAvailable(SimpleMeterRegistry::new);
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
