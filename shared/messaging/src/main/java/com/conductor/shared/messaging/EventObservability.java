package com.conductor.shared.messaging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EventObservability {

    private final MeterRegistry meterRegistry;

    public EventObservability(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPublication(String domain, String entity, String action, String tenantId, long durationMs, boolean success) {
        Counter.builder("event.published.count")
                .tag("domain", domain)
                .tag("entity", entity)
                .tag("action", action)
                .tag("tenantId", tenantId)
                .tag("status", success ? "SUCCESS" : "FAILURE")
                .description("Number of events published to NATS JetStream")
                .register(meterRegistry)
                .increment();

        Timer.builder("event.publish.latency")
                .tag("domain", domain)
                .tag("entity", entity)
                .tag("action", action)
                .tag("tenantId", tenantId)
                .description("Publish latency in milliseconds")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordConsumption(String domain, String entity, String action, String tenantId, long durationMs, boolean success) {
        Counter.builder("event.consumed.count")
                .tag("domain", domain)
                .tag("entity", entity)
                .tag("action", action)
                .tag("tenantId", tenantId)
                .tag("status", success ? "SUCCESS" : "FAILURE")
                .description("Number of events consumed from NATS JetStream")
                .register(meterRegistry)
                .increment();

        Timer.builder("event.consume.latency")
                .tag("domain", domain)
                .tag("entity", entity)
                .tag("action", action)
                .tag("tenantId", tenantId)
                .description("Consume latency in milliseconds")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordDlqEscalation(String domain, String entity, String action, String tenantId, String reason) {
        Counter.builder("event.dlq.escalation.count")
                .tag("domain", domain)
                .tag("entity", entity)
                .tag("action", action)
                .tag("tenantId", tenantId)
                .tag("reason", reason)
                .description("Number of poison events escalated to DLQ")
                .register(meterRegistry)
                .increment();
    }
}
