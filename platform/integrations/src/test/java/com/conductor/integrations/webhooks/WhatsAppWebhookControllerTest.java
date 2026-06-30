package com.conductor.integrations.webhooks;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.integrations.domain.WebhookSubscription;
import com.conductor.integrations.repository.WebhookSubscriptionRepository;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WhatsAppWebhookController.class)
@Import(com.conductor.integrations.config.IntegrationsSecurityConfig.class)
class WhatsAppWebhookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private WebhookSubscriptionRepository subscriptionRepository;
  @MockBean private EventPublisher eventPublisher;
  @MockBean private AuditLogger auditLogger;
  @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;
  @MockBean private com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;
  @MockBean private com.conductor.shared.auth.ConductorAuthenticationEntryPoint authEntryPoint;
  @MockBean private com.conductor.shared.auth.ConductorAccessDeniedHandler accessDeniedHandler;

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
  void testVerifyWebhook_Success() throws Exception {
    WebhookSubscription subscription = new WebhookSubscription();
    subscription.setSecret("my_verify_token");

    when(subscriptionRepository.findByIntegrationConnectorTypeAndEventNameAndTenantId(
            "whatsapp", "status_updated", tenantId))
        .thenReturn(Optional.of(subscription));

    mockMvc
        .perform(
            get("/api/v1/integrations/webhooks/whatsapp/" + tenantId)
                .param("hub.mode", "subscribe")
                .param("hub.verify_token", "my_verify_token")
                .param("hub.challenge", "challenge_123"))
        .andExpect(status().isOk())
        .andExpect(content().string("challenge_123"));
  }
}
