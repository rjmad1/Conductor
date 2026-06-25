package com.conductor.analytics.ingestion;

import com.conductor.analytics.domain.AnalyticsEvent;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.messaging.EventConsumer;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Subscribes to all Conductor NATS domain event streams and ingests them into ClickHouse. Uses
 * global (system-level) subscriptions without tenant scoping to capture all events.
 *
 * <p>Consumed event domains: tenant, identity, customer, workflow, messaging, integration, audit,
 * ai
 */
@Service
public class AnalyticsEventIngestionService {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsEventIngestionService.class);
  private static final String QUEUE_GROUP = "analytics-ingestion";

  private final EventConsumer eventConsumer;
  private final ClickHouseWriter clickHouseWriter;

  /** Domain/entity/action triplets to subscribe to via wildcards. */
  private static final String[][] SUBSCRIPTIONS = {
    {"workflow", "execution", "started"},
    {"workflow", "execution", "completed"},
    {"workflow", "execution", "failed"},
    {"workflow", "definition", "created"},
    {"workflow", "definition", "updated"},
    {"workflow", "definition", "published"},
    {"messaging", "message", "status_updated"},
    {"messaging", "message", "dispatched"},
    {"messaging", "webhook", "received"},
    {"customer", "contact", "created"},
    {"customer", "contact", "updated"},
    {"customer", "contact", "opt_out"},
    {"customer", "profile", "updated"},
    {"integration", "integration", "created"},
    {"integration", "integration", "connected"},
    {"integration", "integration", "disconnected"},
    {"integration", "integration", "executed"},
    {"integration", "integration", "failed"},
    {"integration", "sync", "completed"},
    {"integration", "sync", "failed"},
    {"integration", "webhook", "received"},
    {"tenant", "profile", "created"},
    {"tenant", "profile", "updated"},
    {"identity", "user", "created"},
    {"identity", "role", "assigned"},
    {"audit", "violation", "detected"},
  };

  public AnalyticsEventIngestionService(
      EventConsumer eventConsumer, ClickHouseWriter clickHouseWriter) {
    this.eventConsumer = eventConsumer;
    this.clickHouseWriter = clickHouseWriter;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startIngestion() {
    log.info("Starting analytics event ingestion for {} event types", SUBSCRIPTIONS.length);

    for (String[] sub : SUBSCRIPTIONS) {
      String domain = sub[0];
      String entity = sub[1];
      String action = sub[2];

      try {
        eventConsumer.subscribe(
            QUEUE_GROUP,
            domain,
            entity,
            action,
            null, // Global subscription — no tenant scoping
            String.class,
            event -> handleEvent(event, domain, entity, action));
        log.info("Subscribed to analytics ingestion: {}.{}.{}", domain, entity, action);
      } catch (Exception e) {
        log.error("Failed to subscribe to {}.{}.{}", domain, entity, action, e);
      }
    }
  }

  private void handleEvent(
      ConductorEvent<String> event, String domain, String entity, String action) {
    AnalyticsEvent analyticsEvent =
        AnalyticsEvent.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .tenantId(event.getTenantId())
            .domain(domain)
            .entity(entity)
            .action(action)
            .correlationId(event.getCorrelationId())
            .source(event.getSource())
            .payload(event.getPayload())
            .createdAt(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
            .build();

    clickHouseWriter.write(analyticsEvent);
  }
}
