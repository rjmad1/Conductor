package com.conductor.integrations.service;

import com.conductor.shared.middleware.tenant.TenantContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class IntegrationMetrics {

    private final MeterRegistry registry;

    public IntegrationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordConnectorHealth(String connectorType, boolean success) {
        Counter.builder("connector.health.checks")
                .tag("connector", connectorType)
                .tag("result", success ? "healthy" : "unhealthy")
                .register(registry)
                .increment();
    }

    public void recordConnectorHealthLatency(String connectorType, long latencyMs) {
        Timer.builder("connector.health.latency")
                .tag("connector", connectorType)
                .register(registry)
                .record(Duration.ofMillis(latencyMs));
    }

    public void recordOAuthHealth(String connectorType, boolean success) {
        Counter.builder("oauth.health")
                .tag("tenant", tenantTag())
                .tag("connector", connectorType)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

    public void recordWebhookHealth(String connectorType, boolean success) {
        Counter.builder("webhook.health")
                .tag("tenant", tenantTag())
                .tag("connector", connectorType)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

    public void recordExecution(String connectorType, String action, boolean success, Duration duration) {
        Counter.builder("connector.execution.completed")
                .tag("tenant", tenantTag())
                .tag("connector", connectorType)
                .tag("action", action)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();

        Timer.builder("connector.execution.duration")
                .tag("tenant", tenantTag())
                .tag("connector", connectorType)
                .tag("action", action)
                .register(registry)
                .record(duration);
    }

    private String tenantTag() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return tenantId != null ? tenantId.toString() : "system";
    }
}
