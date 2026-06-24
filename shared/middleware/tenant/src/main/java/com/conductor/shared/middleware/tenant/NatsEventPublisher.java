package com.conductor.shared.middleware.tenant;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class NatsEventPublisher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NatsEventPublisher.class);

    private Connection natsConnection;

    public NatsEventPublisher(@Value("${nats.server-url:nats://localhost:4222}") String natsUrl) {
        try {
            Options options = new Options.Builder()
                    .server(natsUrl)
                    .maxReconnects(-1) // reconnect infinitely
                    .build();
            this.natsConnection = Nats.connect(options);
            log.info("Successfully connected to NATS server at {}", natsUrl);
        } catch (Exception e) {
            log.error("Failed to connect to NATS server at {}", natsUrl, e);
        }
    }

    public void publishEvent(String domain, String entity, String action, String payloadJson) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        String tenantIdStr = tenantId != null ? tenantId.toString() : "system";

        // Namespace format: conductor.{tenant_id}.{domain}.{entity}.{action}
        String subject = String.format("conductor.%s.%s.%s.%s", tenantIdStr, domain, entity, action);
        
        // Wrap in JSON envelope
        String envelopeJson = String.format(
            "{\"eventId\":\"%s\",\"eventType\":\"%s\",\"tenantId\":\"%s\",\"timestamp\":\"%s\",\"version\":\"1.0.0\",\"payload\":%s}",
            UUID.randomUUID(), subject, tenantIdStr, Instant.now().toString(), payloadJson
        );

        if (natsConnection == null) {
            log.warn("NATS connection is offline. Dropping event on subject: {}", subject);
            return;
        }

        try {
            natsConnection.publish(subject, envelopeJson.getBytes(StandardCharsets.UTF_8));
            log.info("Published NATS event on subject: {}", subject);
        } catch (Exception e) {
            log.error("Failed to publish NATS event on subject: {}", subject, e);
        }
    }

    @Override
    public void close() {
        if (natsConnection != null) {
            try {
                natsConnection.close();
                log.info("NATS connection closed");
            } catch (Exception e) {
                log.error("Error closing NATS connection", e);
            }
        }
    }
}
