package com.conductor.customer.service;

import com.conductor.customer.exception.ConsentException;
import com.conductor.shared.customer.ConsentType;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.messaging.EventConsumer;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * StopUnsubscribeListener — Opt-Out keyword handler (WA-C1). Subscribes to NATS JetStream
 * `message.inbound` events. If keyword is "STOP", revokes marketing consent in under 5 seconds.
 */
@Component
public class StopUnsubscribeListener implements SmartInitializingSingleton {

  private static final Logger log = LoggerFactory.getLogger(StopUnsubscribeListener.class);

  private final EventConsumer eventConsumer;
  private final IdentityResolutionService identityResolutionService;
  private final ConsentService consentService;

  public StopUnsubscribeListener(
      EventConsumer eventConsumer,
      IdentityResolutionService identityResolutionService,
      ConsentService consentService) {
    this.eventConsumer = eventConsumer;
    this.identityResolutionService = identityResolutionService;
    this.consentService = consentService;
  }

  @Override
  public void afterSingletonsInstantiated() {
    log.info("Registering NATS JetStream StopUnsubscribeListener for inbound messages...");
    eventConsumer.subscribe(
        "customer_consent_stop_group",
        "messaging",
        "message",
        "inbound",
        null, // Global wildcard tenant subscription
        InboundMessagePayload.class,
        this::handleInboundMessage);
  }

  private void handleInboundMessage(ConductorEvent<InboundMessagePayload> event) {
    InboundMessagePayload payload = event.getPayload();
    if (payload == null || payload.getContent() == null) {
      return;
    }

    String text = payload.getContent().getText();
    if (text == null || !text.trim().equalsIgnoreCase("STOP")) {
      return;
    }

    String eventTenantId = event.getTenantId();
    if (eventTenantId == null) {
      log.warn("Received message.inbound event without tenant ID. Cannot process opt-out.");
      return;
    }

    log.info("Intercepted opt-out keyword 'STOP' for tenant ID: {}", eventTenantId);

    try {
      // Scope db context to event's tenant ID for isolation safety
      TenantContext.setCurrentTenantId(UUID.fromString(eventTenantId));

      // Resolve customer ID
      UUID customerId = null;
      if (payload.getCustomerId() != null && !payload.getCustomerId().isBlank()) {
        try {
          customerId = UUID.fromString(payload.getCustomerId());
        } catch (IllegalArgumentException e) {
          // Ignore and fallback to phone number lookup
        }
      }

      if (customerId == null && payload.getFromPhone() != null) {
        customerId = identityResolutionService.resolveByPhone(payload.getFromPhone()).orElse(null);
      }

      if (customerId == null) {
        log.warn(
            "Could not resolve customer for phone: {}. Cannot record opt-out.",
            payload.getFromPhone());
        return;
      }

      // Revoke marketing consent
      try {
        consentService.revokeConsent(
            customerId,
            ConsentType.MARKETING,
            "WHATSAPP",
            "v1",
            "0.0.0.0",
            "SYSTEM_STOP_INTERCEPTOR",
            "{\"trigger\":\"stop_keyword\",\"message_received\":\"STOP\"}");
        log.info("Successfully processed opt-out for customer ID: {}", customerId);
      } catch (ConsentException e) {
        // Customer might already be opted out, which is acceptable
        log.info(
            "Opt-out already recorded or inactive for customer ID: {}. Detail: {}",
            customerId,
            e.getMessage());
      }

    } catch (Exception e) {
      log.error("Error processing STOP opt-out event", e);
    } finally {
      TenantContext.clear();
    }
  }

  // ── Payload Mapping DTOs ──────────────────────────────────────────────────

  public static class InboundMessagePayload {
    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("from_phone")
    private String fromPhone;

    @JsonProperty("content")
    private InboundMessageContent content;

    @JsonProperty("customer_id")
    private String customerId;

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public String getFromPhone() {
      return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
      this.fromPhone = fromPhone;
    }

    public InboundMessageContent getContent() {
      return content;
    }

    public void setContent(InboundMessageContent content) {
      this.content = content;
    }

    public String getCustomerId() {
      return customerId;
    }

    public void setCustomerId(String customerId) {
      this.customerId = customerId;
    }
  }

  public static class InboundMessageContent {
    @JsonProperty("text")
    private String text;

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }
}
