package com.conductor.integrations.api;

import com.conductor.shared.messaging.EventPublisher;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to receive webhooks from external systems (like Razorpay, Google Calendar). Publishes
 * these as internal NATS events so the workflow engine can pick them up.
 */
@RestController
@RequestMapping("/api/v1/integrations/webhooks")
public class IntegrationWebhookController {

  private static final Logger log = LoggerFactory.getLogger(IntegrationWebhookController.class);
  private final EventPublisher eventPublisher;

  public IntegrationWebhookController(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @PostMapping("/{connectorId}")
  public ResponseEntity<Void> receiveWebhook(
      @PathVariable String connectorId, @RequestBody Map<String, Object> payload) {

    log.info("Received webhook for connector {}", connectorId);

    // Depending on the connector, we can determine the exact event name
    // For MVP, we'll just publish a generic "webhook_received" event
    String eventName = "webhook_received";

    // Some connectors pass an "event" field natively (like Razorpay does)
    if (payload.containsKey("event")) {
      eventName = payload.get("event").toString().replace('.', '_');
    }

    eventPublisher.publish("integration", connectorId, eventName, "v1", payload);
    return ResponseEntity.ok().build();
  }
}
