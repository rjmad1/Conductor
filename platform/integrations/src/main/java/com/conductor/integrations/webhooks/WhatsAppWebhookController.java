package com.conductor.integrations.webhooks;

import com.conductor.integrations.domain.WebhookSubscription;
import com.conductor.integrations.repository.WebhookSubscriptionRepository;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.security.HmacValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations/webhooks/whatsapp")
public class WhatsAppWebhookController {

  private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

  private final WebhookSubscriptionRepository subscriptionRepository;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public WhatsAppWebhookController(
      WebhookSubscriptionRepository subscriptionRepository,
      EventPublisher eventPublisher,
      AuditLogger auditLogger) {
    this.subscriptionRepository = subscriptionRepository;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
  }

  @GetMapping("/{tenantId}")
  public ResponseEntity<String> verifyWebhook(
      @PathVariable UUID tenantId,
      @RequestParam("hub.mode") String mode,
      @RequestParam("hub.verify_token") String verifyToken,
      @RequestParam("hub.challenge") String challenge) {

    log.info("Received WhatsApp webhook verification for tenant: {}", tenantId);

    if (!"subscribe".equalsIgnoreCase(mode)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid mode");
    }

    Optional<WebhookSubscription> subscriptionOpt =
        subscriptionRepository.findByIntegrationConnectorTypeAndEventNameAndTenantId(
            "whatsapp", "status_updated", tenantId);

    if (subscriptionOpt.isPresent()) {
      String secret = subscriptionOpt.get().getSecret();
      if (secret.equals(verifyToken)) {
        log.info("WhatsApp webhook verified successfully for tenant: {}", tenantId);
        return ResponseEntity.ok(challenge);
      }
    }

    log.warn("WhatsApp webhook verification failed (token mismatch) for tenant: {}", tenantId);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification token mismatch");
  }

  @PostMapping("/{tenantId}")
  public ResponseEntity<Void> handleWebhook(
      @PathVariable UUID tenantId,
      @RequestBody byte[] body,
      @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) {

    TenantContext.setCurrentTenantId(tenantId);
    log.info("Received WhatsApp webhook ingress for tenant: {}", tenantId);

    try {
      Optional<WebhookSubscription> subscriptionOpt =
          subscriptionRepository.findByIntegrationConnectorTypeAndEventNameAndTenantId(
              "whatsapp", "status_updated", tenantId);

      if (subscriptionOpt.isEmpty()) {
        log.warn("No active WhatsApp webhook subscription found for tenant: {}", tenantId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      String secret = subscriptionOpt.get().getSecret();

      boolean isValid = false;
      if (signatureHeader != null) {
        String signature = signatureHeader;
        if (signature.startsWith("sha256=")) {
          signature = signature.substring(7);
        }
        isValid = HmacValidator.isValidSignature(body, signature, secret);
      }

      if (!isValid) {
        log.warn("WhatsApp webhook signature validation failed");
        auditLogger.logEvent(
            "WEBHOOK_REJECTED", "webhook:whatsapp", "FAILURE", "Signature invalid");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String payloadJson = new String(body, StandardCharsets.UTF_8);
      Map<?, ?> payloadMap = objectMapper.readValue(payloadJson, Map.class);

      log.info("Publishing whatsapp status_updated event to NATS...");
      boolean published =
          eventPublisher.publish("integration", "whatsapp", "status_updated", "v1", payloadMap);

      if (published) {
        auditLogger.logEvent(
            "WEBHOOK_RECEIVED",
            "webhook:whatsapp",
            "SUCCESS",
            "WhatsApp webhook processed status update");
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
      } else {
        log.error("Failed to publish WhatsApp webhook payload to NATS");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      log.error("Error processing WhatsApp webhook", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } finally {
      TenantContext.clear();
    }
  }
}
