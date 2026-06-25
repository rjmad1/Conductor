package com.conductor.shared.messaging;

import com.conductor.shared.contracts.SchemaValidator;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.api.PublishAck;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

  private final NatsConnectionManager connectionManager;
  private final SchemaValidator schemaValidator;
  private final AuditLogger auditLogger;
  private final EventObservability observability;
  private final ObjectMapper objectMapper;
  private final String producerName;

  public EventPublisher(
      NatsConnectionManager connectionManager,
      SchemaValidator schemaValidator,
      AuditLogger auditLogger,
      EventObservability observability,
      @Value("${spring.application.name:monolith}") String producerName) {
    this.connectionManager = connectionManager;
    this.schemaValidator = schemaValidator;
    this.auditLogger = auditLogger;
    this.observability = observability;
    this.producerName = producerName;
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  public <T> boolean publish(
      String domain, String entity, String action, String schemaVersion, T payload) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    String tenantIdStr = tenantId != null ? tenantId.toString() : "system";

    // Construct NATS subject: conductor.{tenantId}.{domain}.{entity}.{action}
    String subject = String.format("conductor.%s.%s.%s.%s", tenantIdStr, domain, entity, action);
    long startTime = System.currentTimeMillis();
    boolean success = false;

    try {
      // 1. Serialize payload
      String payloadJson;
      if (payload instanceof String) {
        payloadJson = (String) payload;
      } else {
        payloadJson = objectMapper.writeValueAsString(payload);
      }

      // 2. Validate Schema (Phase 4 & Phase 5)
      boolean isValid =
          schemaValidator.validate(domain, entity, action, schemaVersion, payloadJson);
      if (!isValid) {
        log.error(
            "Schema validation failed for event publication on subject {}. Blocking publication.",
            subject);
        auditLogger.logEvent(
            "PUBLISH_REJECTED",
            "SCHEMA:" + domain + "." + entity + "." + action,
            "FAILURE",
            "Schema validation failed for payload: " + payloadJson);
        return false;
      }

      // 3. Extract Correlation & Causation IDs (Phase 8)
      String correlationId = MDC.get("requestId");
      if (correlationId == null) {
        correlationId = UUID.randomUUID().toString();
      }
      String causationId = MDC.get("causationId");
      if (causationId == null) {
        causationId = correlationId;
      }

      // 4. Construct Event Envelope (Phase 2)
      ConductorEvent<String> event =
          ConductorEvent.<String>builder()
              .eventId(UUID.randomUUID())
              .eventVersion("1.0.0")
              .eventType(String.format("conductor.%s.%s.%s", domain, entity, action))
              .tenantId(tenantIdStr)
              .correlationId(correlationId)
              .causationId(causationId)
              .source("/platform/" + producerName)
              .timestamp(Instant.now())
              .producer(producerName)
              .schemaVersion(schemaVersion)
              .payload(payloadJson)
              .build();

      String envelopeJson = objectMapper.writeValueAsString(event);

      // 5. Connect and Publish with Confirmations (Phase 6 At-least-once)
      Connection conn = connectionManager.getConnection();
      if (conn == null) {
        log.error("NATS connection is offline. Failed to publish event: {}", subject);
        return false;
      }

      JetStream js = conn.jetStream();
      PublishAck ack = js.publish(subject, envelopeJson.getBytes(StandardCharsets.UTF_8));

      log.info(
          "Successfully published event to subject {} (Stream: {}, Seq: {})",
          subject,
          ack.getStream(),
          ack.getSeqno());
      success = true;
      return true;
    } catch (Exception e) {
      log.error("Failed to publish event to subject: {}", subject, e);
      auditLogger.logEvent("PUBLISH_FAILED", "EVENT:" + subject, "FAILURE", e.getMessage());
      return false;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      observability.recordPublication(domain, entity, action, tenantIdStr, duration, success);
    }
  }
}
