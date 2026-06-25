package com.conductor.workflow.service;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.workflow.WorkflowStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Workflow observability metrics using Micrometer. Publishes counters and timers to Prometheus via
 * the actuator metrics endpoint.
 */
@Component
public class WorkflowMetrics {

  private final MeterRegistry registry;

  public WorkflowMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  public void recordDefinitionCreated() {
    Counter.builder("workflow.definitions.created")
        .tag("tenant", tenantTag())
        .register(registry)
        .increment();
  }

  public void recordExecutionStarted() {
    Counter.builder("workflow.executions.started")
        .tag("tenant", tenantTag())
        .register(registry)
        .increment();
  }

  public void recordExecutionCompleted(WorkflowStatus status, Duration duration) {
    Counter.builder("workflow.executions.completed")
        .tag("tenant", tenantTag())
        .tag("status", status.name())
        .register(registry)
        .increment();

    Timer.builder("workflow.executions.duration")
        .tag("tenant", tenantTag())
        .tag("status", status.name())
        .register(registry)
        .record(duration);
  }

  public void recordRetry() {
    Counter.builder("workflow.retries.total")
        .tag("tenant", tenantTag())
        .register(registry)
        .increment();
  }

  public void recordCompensation() {
    Counter.builder("workflow.compensations.total")
        .tag("tenant", tenantTag())
        .register(registry)
        .increment();
  }

  public void recordActionExecuted(String actionType, boolean success) {
    Counter.builder("workflow.actions.executed")
        .tag("tenant", tenantTag())
        .tag("type", actionType)
        .tag("success", String.valueOf(success))
        .register(registry)
        .increment();
  }

  public void recordTriggerFired(String triggerType) {
    Counter.builder("workflow.triggers.fired")
        .tag("tenant", tenantTag())
        .tag("type", triggerType)
        .register(registry)
        .increment();
  }

  private String tenantTag() {
    UUID tenantId = TenantContext.getCurrentTenantId();
    return tenantId != null ? tenantId.toString() : "system";
  }
}
