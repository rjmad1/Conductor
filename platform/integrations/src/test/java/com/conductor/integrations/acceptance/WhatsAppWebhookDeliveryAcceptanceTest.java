package com.conductor.integrations.acceptance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.integrations.domain.Connector;
import com.conductor.integrations.domain.Integration;
import com.conductor.integrations.domain.WebhookSubscription;
import com.conductor.integrations.repository.ConnectorRepository;
import com.conductor.integrations.repository.IntegrationRepository;
import com.conductor.integrations.repository.WebhookSubscriptionRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * Acceptance test: WhatsApp webhook delivery — integrations module.
 *
 * <p>Validates the full webhook ingress pipeline:
 *
 * <ol>
 *   <li>Webhook verification (GET) challenge/response with Meta
 *   <li>Delivery (POST) with valid HMAC-SHA256 signature → 202 Accepted
 *   <li>Delivery with invalid/missing signature → 401 Unauthorized
 *   <li>Delivery without active subscription → 404 Not Found
 *   <li>Event published to event bus after successful delivery
 *   <li>NATS failure returns 500
 *   <li>Latency within 500ms advisory SLA
 * </ol>
 */
@DisplayName("Acceptance: WhatsApp Webhook Delivery")
@SuppressWarnings("null")
class WhatsAppWebhookDeliveryAcceptanceTest extends BaseIntegrationsAcceptanceTest {

  private static final String WEBHOOK_SECRET = "acceptance-whatsapp-secret-2024";
  private static final String STATUS_UPDATE_EVENT = "status_updated";
  private static final String CONNECTOR_TYPE = "whatsapp";

  @Autowired private WebhookSubscriptionRepository subscriptionRepository;
  @Autowired private IntegrationRepository integrationRepository;
  @Autowired private ConnectorRepository connectorRepository;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);

    // Seed connector catalog entry (global, not tenant-scoped)
    Connector connector = new Connector();
    connector.setType(CONNECTOR_TYPE);
    connector.setName("WhatsApp Cloud API");
    connector.setVersion("1.0");
    connector.setEnabled(true);
    connector = connectorRepository.save(connector);

    // Seed integration instance for this tenant
    Integration integration = new Integration();
    integration.setTenantId(tenantId);
    integration.setConnector(connector);
    integration.setName("WhatsApp Acceptance Integration");
    integration.setEnabled(true);
    integration = integrationRepository.save(integration);

    // Seed active webhook subscription
    WebhookSubscription subscription = new WebhookSubscription();
    subscription.setTenantId(tenantId);
    subscription.setIntegration(integration);
    subscription.setEventName(STATUS_UPDATE_EVENT);
    subscription.setSecret(WEBHOOK_SECRET);
    subscription.setTargetUrl("https://platform.conductor.test/webhooks/whatsapp");
    subscription.setStatus("ACTIVE");
    subscriptionRepository.save(subscription);

    // NATS event publisher is mocked — configure default success path
    when(eventPublisher.publish(anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(true);
  }

  @AfterEach
  void tearDown() {
    subscriptionRepository.deleteAll();
    integrationRepository.deleteAll();
    connectorRepository.deleteAll();
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  // ── Step 13: Webhook verification ─────────────────────────────────────────

  @Test
  @DisplayName("GET webhook verify — challenge echoed when verify_token matches")
  void webhookVerification_challengeEchoed() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .param("hub.mode", "subscribe")
                .param("hub.verify_token", WEBHOOK_SECRET)
                .param("hub.challenge", "challenge_abc_acceptance_123"))
        .andExpect(status().isOk())
        .andExpect(content().string("challenge_abc_acceptance_123"));
  }

  @Test
  @DisplayName("GET webhook verify — wrong token returns 403 Forbidden")
  void webhookVerification_wrongToken_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .param("hub.mode", "subscribe")
                .param("hub.verify_token", "wrong-token")
                .param("hub.challenge", "some_challenge"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET webhook verify — invalid mode returns 400 Bad Request")
  void webhookVerification_invalidMode_returns400() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .param("hub.mode", "unsub")
                .param("hub.verify_token", WEBHOOK_SECRET)
                .param("hub.challenge", "x"))
        .andExpect(status().isBadRequest());
  }

  // ── Step 14: WhatsApp delivery webhook ────────────────────────────────────

  @Test
  @DisplayName("POST webhook delivery — valid HMAC signature → 202 Accepted, event published")
  void webhookDelivery_validSignature_accepted() throws Exception {
    String payload =
        "{\"object\":\"whatsapp_business_account\","
            + "\"entry\":[{\"id\":\"12345\","
            + "\"changes\":[{\"value\":{\"statuses\":[{\"id\":\"wam_001\","
            + "\"status\":\"delivered\",\"recipient_id\":\"+15555550600\"}]},"
            + "\"field\":\"messages\"}]}]}";

    String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .header("X-Hub-Signature-256", "sha256=" + signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isAccepted());

    // Verify event was published to NATS (mocked)
    verify(eventPublisher, times(1))
        .publish(eq("integration"), eq("whatsapp"), eq(STATUS_UPDATE_EVENT), eq("v1"), any());
  }

  // ── Step 15: Webhook processed ─────────────────────────────────────────────

  @Test
  @DisplayName("POST webhook delivery — missing signature → 401 Unauthorized")
  void webhookDelivery_missingSignature_returns401() throws Exception {
    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST webhook delivery — invalid HMAC signature → 401 Unauthorized")
  void webhookDelivery_invalidSignature_returns401() throws Exception {
    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .header("X-Hub-Signature-256", "sha256=invalid_signature_hex")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST webhook delivery — signature computed with wrong secret → 401")
  void webhookDelivery_wrongSecretSignature_returns401() throws Exception {
    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";
    String wrongSignature = computeHmacSha256(payload, "completely-different-secret");

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .header("X-Hub-Signature-256", "sha256=" + wrongSignature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST webhook delivery — tenant has no active subscription → 404 Not Found")
  void webhookDelivery_noSubscription_returns404() throws Exception {
    UUID unknownTenantId = UUID.randomUUID();
    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";
    String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + unknownTenantId)
                .header("X-Hub-Signature-256", "sha256=" + signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST webhook delivery — NATS publish failure returns 500")
  void webhookDelivery_natsFailure_returns500() throws Exception {
    when(eventPublisher.publish(anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(false);

    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"err\"}]}";
    String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .header("X-Hub-Signature-256", "sha256=" + signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("Webhook processing latency within 500ms advisory SLA")
  void webhookDelivery_latencyWithinSla() throws Exception {
    String payload = "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"lat_test\"}]}";
    String signature = computeHmacSha256(payload, WEBHOOK_SECRET);

    long start = System.currentTimeMillis();

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .header("X-Hub-Signature-256", "sha256=" + signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isAccepted());

    long elapsed = System.currentTimeMillis() - start;
    if (elapsed > 500) {
      System.out.printf("[PERF] Webhook processing took %dms (SLA: 500ms)%n", elapsed);
    }
    Assertions.assertThat(elapsed)
        .as("Webhook processing must complete within 5000ms")
        .isLessThan(5000L);
  }

  // ── Helper ─────────────────────────────────────────────────────────────────

  private static String computeHmacSha256(String payload, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    for (byte b : digest) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }
}
