package com.conductor.shared.messaging;

import com.conductor.shared.contracts.SchemaValidator;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

  private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
  private final NatsConnectionManager connectionManager;
  private final SchemaValidator schemaValidator;
  private final AuditLogger auditLogger;
  private final EventObservability observability;
  private final ObjectMapper objectMapper;
  private final java.util.concurrent.ConcurrentHashMap<Subscription, Dispatcher>
      subscriptionDispatchers = new java.util.concurrent.ConcurrentHashMap<>();

  public EventConsumer(
      NatsConnectionManager connectionManager,
      SchemaValidator schemaValidator,
      AuditLogger auditLogger,
      EventObservability observability) {
    this.connectionManager = connectionManager;
    this.schemaValidator = schemaValidator;
    this.auditLogger = auditLogger;
    this.observability = observability;
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  public <T> Subscription subscribe(
      String queueGroup,
      String domain,
      String entity,
      String action,
      String
          tenantScopeId, // Nullable. If present, restricts subscription & visibility to this tenant
      Class<T> payloadClass,
      Consumer<ConductorEvent<T>> handler) {

    Connection conn = connectionManager.getConnection();
    if (conn == null) {
      log.error("NATS connection is offline. Cannot subscribe to {}.{}.{}", domain, entity, action);
      return null;
    }

    // Determine subscription subject:
    // For tenant-scoped workers, listen only to their tenant's events.
    // For global/system workers, listen to all tenants using wildcard '*'.
    String tenantFilter = tenantScopeId != null ? tenantScopeId : "*";
    String subject = String.format("conductor.%s.%s.%s.%s", tenantFilter, domain, entity, action);
    String durableName =
        String.format(
            "%s_%s_%s_%s",
            tenantScopeId != null ? "tenant_" + tenantScopeId : "global", domain, entity, action);

    try {
      JetStream js = conn.jetStream();

      // Set up JetStream configuration
      ConsumerConfiguration cc =
          ConsumerConfiguration.builder()
              .durable(durableName)
              .deliverGroup(queueGroup) // Enforces consumer group scaling
              .ackWait(Duration.ofSeconds(30))
              .build();

      PushSubscribeOptions pso = PushSubscribeOptions.builder().configuration(cc).build();

      // Dispatcher callback
      MessageHandler messageHandler =
          (Message msg) -> {
            long startTime = System.currentTimeMillis();
            boolean success = false;
            String activeTenantId = tenantScopeId != null ? tenantScopeId : "global";
            try {
              // Parse NATS message byte payload
              String envelopeJson = new String(msg.getData(), StandardCharsets.UTF_8);

              // Deserialize to ConductorEvent
              ConductorEvent<String> rawEvent =
                  objectMapper.readValue(
                      envelopeJson, new TypeReference<ConductorEvent<String>>() {});

              activeTenantId = rawEvent.getTenantId();

              // Tenant Isolation Guard (Phase 5)
              if (tenantScopeId != null && !tenantScopeId.equals(rawEvent.getTenantId())) {
                log.error(
                    "SECURITY VIOLATION: Cross-tenant event leakage blocked. "
                        + "Consumer tenantId {} received event tenantId {} on subject {}",
                    tenantScopeId,
                    rawEvent.getTenantId(),
                    msg.getSubject());
                auditLogger.logEvent(
                    "TENANT_VIOLATION",
                    "EVENT_CONSUME:" + rawEvent.getEventId(),
                    "FAILURE",
                    String.format(
                        "Leakage blocked: consumer=%s, event=%s",
                        tenantScopeId, rawEvent.getTenantId()));
                msg.ack(); // Acknowledge to remove from stream
                return;
              }

              // Propagate correlation & causation identifiers (Phase 8)
              MDC.put("requestId", rawEvent.getCorrelationId());
              MDC.put("causationId", rawEvent.getEventId().toString());

              // Validate schema validation of payload (Phase 4 & Phase 7 Poison checking)
              boolean isValid =
                  schemaValidator.validate(
                      domain, entity, action, rawEvent.getSchemaVersion(), rawEvent.getPayload());

              if (!isValid) {
                log.warn(
                    "Poison message detected (Schema validation failed). Routing to DLQ: {}",
                    rawEvent.getEventId());
                routeToDlq(msg, rawEvent, "Schema validation failed", activeTenantId);
                msg.ack();
                return;
              }

              // Deserialize specific payload type
              T typedPayload;
              if (payloadClass == String.class) {
                @SuppressWarnings("unchecked")
                T casted = (T) rawEvent.getPayload();
                typedPayload = casted;
              } else {
                typedPayload = objectMapper.readValue(rawEvent.getPayload(), payloadClass);
              }
              ConductorEvent<T> typedEvent =
                  ConductorEvent.<T>builder()
                      .eventId(rawEvent.getEventId())
                      .eventVersion(rawEvent.getEventVersion())
                      .eventType(rawEvent.getEventType())
                      .tenantId(rawEvent.getTenantId())
                      .correlationId(rawEvent.getCorrelationId())
                      .causationId(rawEvent.getCausationId())
                      .source(rawEvent.getSource())
                      .timestamp(rawEvent.getTimestamp())
                      .producer(rawEvent.getProducer())
                      .schemaVersion(rawEvent.getSchemaVersion())
                      .payload(typedPayload)
                      .build();

              // Execute Handler
              try {
                handler.accept(typedEvent);
                msg.ack(); // Successful acknowledgment
                success = true;
              } catch (Exception handlerException) {
                log.error(
                    "Error executing handler for event: {}",
                    rawEvent.getEventId(),
                    handlerException);
                handleProcessingFailure(msg, rawEvent, handlerException, activeTenantId);
              }

            } catch (Exception e) {
              log.error("Failed to parse and process event message", e);
              // If parsing completely fails, it is a poison message, route to DLQ
              routeToDlq(msg, null, "Failed to parse: " + e.getMessage(), activeTenantId);
              msg.ack();
            } catch (Throwable t) {
              log.error("Fatal error inside consumer loop dispatcher", t);
              msg.nak();
            } finally {
              long duration = System.currentTimeMillis() - startTime;
              observability.recordConsumption(
                  domain, entity, action, activeTenantId, duration, success);
              MDC.clear();
            }
          };

      Dispatcher dispatcher = conn.createDispatcher(messageHandler);
      Subscription sub = js.subscribe(subject, dispatcher, messageHandler, false, pso);
      subscriptionDispatchers.put(sub, dispatcher);
      log.info("Subscribed to subject: {} with durable name: {}", subject, durableName);
      return sub;

    } catch (Exception e) {
      log.error("Failed to create NATS JetStream subscription on subject: {}", subject, e);
      return null;
    }
  }

  public void unsubscribe(Subscription sub) {
    if (sub == null) return;
    try {
      Dispatcher d = subscriptionDispatchers.remove(sub);
      if (d != null) {
        Connection conn = connectionManager.getConnection();
        if (conn != null) {
          conn.closeDispatcher(d);
          log.info("Successfully unsubscribed and closed dispatcher for subscription");
          return;
        }
      }
      sub.unsubscribe();
    } catch (Exception e) {
      log.error("Failed to unsubscribe subscription", e);
    }
  }

  private void handleProcessingFailure(
      Message msg, ConductorEvent<String> event, Exception ex, String tenantId) {
    long deliveries = msg.metaData().deliveredCount();
    if (deliveries >= 5) {
      log.error("Retry limit (5) exceeded for event: {}. Escalating to DLQ.", event.getEventId());
      routeToDlq(msg, event, "Retry limit exceeded. Reason: " + ex.getMessage(), tenantId);
      msg.ack();
    } else {
      // Exponential backoff nak delay calculation (Phase 7): 2s, 4s, 8s, 16s, 32s
      long backoffSeconds = (long) Math.pow(2, deliveries);
      log.warn(
          "Retrying message {} (Attempt {}). Backoff delay: {}s",
          event.getEventId(),
          deliveries,
          backoffSeconds);
      msg.nakWithDelay(Duration.ofSeconds(backoffSeconds));
    }
  }

  private void routeToDlq(
      Message msg, ConductorEvent<String> event, String reason, String tenantId) {
    try {
      Connection conn = connectionManager.getConnection();
      if (conn == null) {
        log.error("Cannot publish to DLQ: NATS connection is offline");
        return;
      }

      String stream = msg.metaData().getStream();
      String consumer = msg.metaData().getConsumer();
      String dlqSubject = String.format("dlq.%s.%s.%s", tenantId, stream, consumer);

      String originPayload =
          event != null
              ? objectMapper.writeValueAsString(event)
              : new String(msg.getData(), StandardCharsets.UTF_8);
      String dlqWrapper =
          String.format(
              "{\"deadLetteredAt\":\"%s\",\"reason\":\"%s\",\"retries\":%d,\"originalEvent\":%s}",
              java.time.Instant.now(),
              reason.replace("\"", "\\\""),
              msg.metaData().deliveredCount(),
              originPayload);

      conn.publish(dlqSubject, dlqWrapper.getBytes(StandardCharsets.UTF_8));
      log.info(
          "Successfully routed poison message {} to DLQ subject: {}",
          event != null ? event.getEventId() : "unparseable",
          dlqSubject);

      auditLogger.logEvent(
          "DLQ_ESCALATION",
          "EVENT:" + (event != null ? event.getEventId() : "unparseable"),
          "SUCCESS",
          "Event routed to DLQ. Subject: " + dlqSubject + ", Reason: " + reason);

      // Record DLQ escalation metric (Phase 11)
      String domain = "unknown";
      String entity = "unknown";
      String action = "unknown";
      if (event != null && event.getEventType() != null) {
        String[] segments = event.getEventType().split("\\.");
        if (segments.length >= 4) {
          domain = segments[1];
          entity = segments[2];
          action = segments[3];
        }
      }
      observability.recordDlqEscalation(domain, entity, action, tenantId, reason);

    } catch (Exception e) {
      log.error("Failed to route poison message to DLQ", e);
    }
  }
}
