package com.conductor.integrations.webhooks;

import com.conductor.integrations.domain.WebhookSubscription;
import com.conductor.integrations.framework.CredentialEncryptor;
import com.conductor.integrations.repository.WebhookSubscriptionRepository;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
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
@RequestMapping("/api/v1/integrations/webhooks")
public class WebhookIngressController {

  private static final Logger log = LoggerFactory.getLogger(WebhookIngressController.class);
  private final WebhookSubscriptionRepository subscriptionRepository;
  private final WebhookSignatureValidator signatureValidator;
  private final WebhookReplayProtector replayProtector;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final CredentialEncryptor credentialEncryptor;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public WebhookIngressController(
      WebhookSubscriptionRepository subscriptionRepository,
      WebhookSignatureValidator signatureValidator,
      WebhookReplayProtector replayProtector,
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      CredentialEncryptor credentialEncryptor) {
    this.subscriptionRepository = subscriptionRepository;
    this.signatureValidator = signatureValidator;
    this.replayProtector = replayProtector;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
    this.credentialEncryptor = credentialEncryptor;
  }

  @PostMapping("/ingress/{connectorType}/{tenantId}")
  public ResponseEntity<Void> handleIngress(
      @PathVariable String connectorType,
      @PathVariable UUID tenantId,
      @RequestBody byte[] body,
      @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String shopifySignature,
      @RequestHeader(value = "X-Shopify-Topic", required = false) String shopifyTopic,
      @RequestHeader(value = "X-Shopify-Webhook-Id", required = false) String shopifyMsgId,
      @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySignature,
      @RequestHeader(value = "X-Zoho-Signature", required = false) String zohoSignature,
      @RequestHeader(value = "X-Webhook-Event-Name", required = false) String genericEventName,
      @RequestHeader(value = "X-Webhook-Message-Id", required = false) String genericMsgId) {

    TenantContext.setCurrentTenantId(tenantId);
    log.info("Received webhook ingress for connector: {}, tenant: {}", connectorType, tenantId);

    try {
      String eventName = resolveEventName(connectorType, shopifyTopic, genericEventName);
      String messageId = resolveMessageId(shopifyMsgId, genericMsgId, body);

      if (replayProtector.isDuplicate(messageId)) {
        log.warn("Duplicate webhook message detected: {}. Skipping execution.", messageId);
        return ResponseEntity.ok().build();
      }

      Optional<WebhookSubscription> subOpt =
          subscriptionRepository.findByIntegrationConnectorTypeAndEventNameAndTenantId(
              connectorType.toLowerCase(), eventName, tenantId);

      if (subOpt.isEmpty()) {
        log.warn(
            "No active subscription found for connector: {}, event: {}, tenant: {}",
            connectorType,
            eventName,
            tenantId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      WebhookSubscription subscription = subOpt.get();
      // Decrypt the stored secret; secrets are encrypted at rest with AES-256-GCM.
      String secret = credentialEncryptor.decrypt(subscription.getSecret());

      boolean isValid = false;
      if ("shopify".equalsIgnoreCase(connectorType)) {
        isValid = signatureValidator.validateShopify(body, shopifySignature, secret);
      } else if ("razorpay".equalsIgnoreCase(connectorType)) {
        isValid = signatureValidator.validateRazorpay(body, razorpaySignature, secret);
      } else if ("zoho".equalsIgnoreCase(connectorType)) {
        isValid = signatureValidator.validateZoho(body, zohoSignature, secret);
      } else {
        isValid = true;
      }

      if (!isValid) {
        log.warn("Cryptographic signature check failed for webhook connection");
        auditLogger.logEvent(
            "WEBHOOK_REJECTED", "webhook:" + connectorType, "FAILURE", "Signature invalid");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String payloadJson = new String(body, StandardCharsets.UTF_8);
      Map<?, ?> payloadMap = objectMapper.readValue(payloadJson, Map.class);

      String action = getActionName(connectorType, eventName);
      boolean published =
          eventPublisher.publish(
              "integration", connectorType.toLowerCase(), action, "v1", payloadMap);

      if (published) {
        auditLogger.logEvent(
            "WEBHOOK_RECEIVED",
            "webhook:" + connectorType,
            "SUCCESS",
            "Webhook event processed: " + eventName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
      } else {
        log.error("Failed to publish webhook payload to NATS");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      log.error("Error processing incoming webhook", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } finally {
      TenantContext.clear();
    }
  }

  private String resolveEventName(String connector, String shopifyTopic, String genericEvent) {
    if ("shopify".equalsIgnoreCase(connector) && shopifyTopic != null) {
      return shopifyTopic;
    }
    if (genericEvent != null) {
      return genericEvent;
    }
    return "generic.event";
  }

  private String resolveMessageId(String shopifyId, String genericId, byte[] body) {
    if (shopifyId != null) {
      return shopifyId;
    }
    if (genericId != null) {
      return genericId;
    }
    return "hash-" + java.util.Arrays.hashCode(body);
  }

  private String getActionName(String connector, String eventName) {
    if ("shopify".equalsIgnoreCase(connector)) {
      if ("orders/create".equalsIgnoreCase(eventName)) return "order_created";
      if ("orders/update".equalsIgnoreCase(eventName)) return "order_updated";
      if ("customers/create".equalsIgnoreCase(eventName)) return "customer_created";
      if ("customers/update".equalsIgnoreCase(eventName)) return "customer_updated";
    }
    if ("zoho".equalsIgnoreCase(connector)) {
      if ("lead.create".equalsIgnoreCase(eventName)) return "lead_created";
      if ("lead.update".equalsIgnoreCase(eventName)) return "lead_updated";
      if ("contact.create".equalsIgnoreCase(eventName)) return "contact_created";
      if ("contact.update".equalsIgnoreCase(eventName)) return "contact_updated";
    }
    if ("razorpay".equalsIgnoreCase(connector)) {
      if ("payment.created".equalsIgnoreCase(eventName)) return "payment_created";
      if ("payment.captured".equalsIgnoreCase(eventName)
          || "payment.completed".equalsIgnoreCase(eventName)) return "payment_completed";
      if ("payment.failed".equalsIgnoreCase(eventName)) return "payment_failed";
      if ("refund.created".equalsIgnoreCase(eventName)) return "refund_created";
      if ("refund.processed".equalsIgnoreCase(eventName)
          || "refund.completed".equalsIgnoreCase(eventName)) return "refund_completed";
    }
    return eventName.replace('.', '_').replace('/', '_');
  }
}
