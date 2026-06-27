package com.conductor.integrations;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.integrations.domain.*;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.webhooks.*;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebhookIngressController.class)
@Import(com.conductor.integrations.config.IntegrationsSecurityConfig.class)
@SuppressWarnings("null")
class WebhookIngressIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private WebhookSubscriptionRepository subscriptionRepository;

  @MockBean private WebhookSignatureValidator signatureValidator;

  @MockBean private WebhookReplayProtector replayProtector;

  @MockBean private com.conductor.shared.messaging.EventPublisher eventPublisher;

  @MockBean private com.conductor.shared.middleware.tenant.AuditLogger auditLogger;

  @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

  @MockBean
  private com.conductor.shared.auth.ConductorAuthenticationEntryPoint
      conductorAuthenticationEntryPoint;

  @MockBean
  private com.conductor.shared.auth.ConductorAccessDeniedHandler conductorAccessDeniedHandler;

  @MockBean private com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;

  @MockBean private com.conductor.integrations.framework.CredentialEncryptor credentialEncryptor;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    TenantContext.setCurrentTenantId(tenantId);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void givenWebhookIngress_whenSignatureAndReplayValid_thenAcceptAndPublish() throws Exception {
    String body = "{\"event\":\"test\"}";
    String mockSignature = "sig-123";
    String secret = "secret-key";

    WebhookSubscription sub = new WebhookSubscription();
    sub.setSecret("encrypted-" + secret);
    sub.setEventName("orders/create");
    sub.setStatus("ACTIVE");

    when(subscriptionRepository.findByIntegrationConnectorTypeAndEventNameAndTenantId(
            "shopify", "orders/create", tenantId))
        .thenReturn(Optional.of(sub));

    when(credentialEncryptor.decrypt("encrypted-" + secret)).thenReturn(secret);
    when(signatureValidator.validateShopify(any(), any(), any())).thenReturn(true);
    when(replayProtector.isDuplicate(any())).thenReturn(false);
    when(eventPublisher.publish(anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(true);

    mockMvc
        .perform(
            post("/api/v1/integrations/webhooks/ingress/shopify/" + tenantId)
                .header("X-Shopify-Hmac-SHA256", mockSignature)
                .header("X-Shopify-Topic", "orders/create")
                .header("X-Shopify-Webhook-Id", "msg-001")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    verify(eventPublisher)
        .publish(eq("integration"), eq("shopify"), eq("order_created"), eq("v1"), any());
  }
}
