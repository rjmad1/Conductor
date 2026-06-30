package com.conductor.customer.service;

import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.messaging.EventConsumer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class CustomerEventListener {

  private static final Logger log = LoggerFactory.getLogger(CustomerEventListener.class);

  private final EventConsumer eventConsumer;
  private final CustomerTimelineService timelineService;
  private final IdentityResolutionService identityResolutionService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public CustomerEventListener(
      EventConsumer eventConsumer,
      CustomerTimelineService timelineService,
      IdentityResolutionService identityResolutionService) {
    this.eventConsumer = eventConsumer;
    this.timelineService = timelineService;
    this.identityResolutionService = identityResolutionService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startSubscriptions() {
    log.info("Starting NATS subscriptions for customer domain events...");

    eventConsumer.subscribe(
        "customer-message-sent-group",
        "messaging",
        "message",
        "sent",
        null,
        String.class,
        event -> {
          try {
            log.info("Received messaging.message.sent event: {}", event.getEventId());
            Map<String, Object> payload =
                objectMapper.readValue(
                    event.getPayload(), new TypeReference<Map<String, Object>>() {});
            String customerIdStr = (String) payload.get("customerId");
            if (customerIdStr != null) {
              UUID customerId = UUID.fromString(customerIdStr);
              String channel = (String) payload.get("channel");

              com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(
                  UUID.fromString(event.getTenantId()));
              try {
                timelineService.record(
                    customerId,
                    TimelineEventType.MESSAGE_SENT,
                    "messaging-service",
                    String.format(
                        "%s welcome message sent", channel != null ? channel : "WhatsApp"),
                    event.getPayload());
                log.info("Recorded MESSAGE_SENT on timeline for customer: {}", customerId);
              } finally {
                com.conductor.shared.middleware.tenant.TenantContext.clear();
              }
            }
          } catch (Exception e) {
            log.error("Failed to process messaging.message.sent event", e);
          }
        });

    eventConsumer.subscribe(
        "customer-whatsapp-status-group",
        "integration",
        "whatsapp",
        "status_updated",
        null,
        String.class,
        event -> {
          try {
            log.info("Received integration.whatsapp.status_updated event: {}", event.getEventId());
            Map<String, Object> payload =
                objectMapper.readValue(
                    event.getPayload(), new TypeReference<Map<String, Object>>() {});
            java.util.List<Map<String, Object>> entry =
                (java.util.List<Map<String, Object>>) payload.get("entry");
            if (entry != null && !entry.isEmpty()) {
              for (Map<String, Object> entryItem : entry) {
                java.util.List<Map<String, Object>> changes =
                    (java.util.List<Map<String, Object>>) entryItem.get("changes");
                if (changes != null) {
                  for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    if (value != null) {
                      java.util.List<Map<String, Object>> statuses =
                          (java.util.List<Map<String, Object>>) value.get("statuses");
                      if (statuses != null) {
                        for (Map<String, Object> statusItem : statuses) {
                          String recipientPhone = (String) statusItem.get("recipient_id");
                          String status = (String) statusItem.get("status");

                          if (recipientPhone != null) {
                            com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(
                                UUID.fromString(event.getTenantId()));
                            try {
                              Optional<UUID> customerIdOpt =
                                  identityResolutionService.resolveByPhone(recipientPhone);
                              if (customerIdOpt.isPresent()) {
                                UUID customerId = customerIdOpt.get();
                                timelineService.record(
                                    customerId,
                                    TimelineEventType.MESSAGE_RECEIVED,
                                    "whatsapp-provider",
                                    String.format(
                                        "WhatsApp message status update: %s", status.toUpperCase()),
                                    objectMapper.writeValueAsString(statusItem));
                                log.info(
                                    "Recorded MESSAGE_RECEIVED status update ({}) on timeline for customer: {}",
                                    status,
                                    customerId);
                              } else {
                                log.warn(
                                    "Could not find customer with phone: {} for status update",
                                    recipientPhone);
                              }
                            } finally {
                              com.conductor.shared.middleware.tenant.TenantContext.clear();
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          } catch (Exception e) {
            log.error("Failed to process integration.whatsapp.status_updated event", e);
          }
        });
  }
}
