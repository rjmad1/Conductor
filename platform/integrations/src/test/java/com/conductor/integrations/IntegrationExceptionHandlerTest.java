package com.conductor.integrations;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.conductor.integrations.api.IntegrationController;
import com.conductor.integrations.api.IntegrationExceptionHandler;
import com.conductor.integrations.framework.ConnectorRegistry;
import com.conductor.integrations.framework.OAuthStateStore;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.service.CredentialService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({IntegrationController.class, IntegrationExceptionHandler.class})
@Import(com.conductor.integrations.config.IntegrationsSecurityConfig.class)
@SuppressWarnings("null")
class IntegrationExceptionHandlerTest {

  @Autowired MockMvc mockMvc;

  @MockBean ConnectorRepository connectorRepository;
  @MockBean IntegrationRepository integrationRepository;
  @MockBean ConnectionRepository connectionRepository;
  @MockBean WebhookSubscriptionRepository webhookSubscriptionRepository;
  @MockBean ExecutionHistoryRepository executionHistoryRepository;
  @MockBean CredentialService credentialService;
  @MockBean ConnectorRegistry connectorRegistry;
  @MockBean com.conductor.shared.middleware.tenant.AuditLogger auditLogger;
  @MockBean com.conductor.shared.messaging.EventPublisher eventPublisher;
  @MockBean OAuthStateStore oauthStateStore;

  @MockBean
  com.conductor.shared.auth.ConductorAuthenticationEntryPoint conductorAuthenticationEntryPoint;

  @MockBean com.conductor.shared.auth.ConductorAccessDeniedHandler conductorAccessDeniedHandler;
  @MockBean org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;
  @MockBean com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;
  @MockBean com.conductor.integrations.service.OAuthTokenExchangeService oauthTokenExchangeService;
  @MockBean com.conductor.integrations.framework.CredentialEncryptor credentialEncryptor;

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void unknownIntegration_returns404WithProblemDetail() throws Exception {
    UUID id = UUID.randomUUID();
    when(integrationRepository.findByIdAndTenantId(eq(id), any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/v1/integrations/" + id + "/health")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.type").value("https://conductor.io/errors/integration-not-found"));
  }

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void executeWithUnknownAction_returns400WithProblemDetail() throws Exception {
    UUID id = UUID.randomUUID();
    com.conductor.integrations.domain.Connector connector =
        new com.conductor.integrations.domain.Connector();
    connector.setType("shopify");
    com.conductor.integrations.domain.Integration integration =
        new com.conductor.integrations.domain.Integration();
    integration.setConnector(connector);

    when(integrationRepository.findByIdAndTenantId(eq(id), any()))
        .thenReturn(Optional.of(integration));

    org.mockito.stubbing.Answer<?> throwUnsupported =
        inv -> {
          throw new UnsupportedOperationException("Unknown Shopify action: bad-action");
        };
    com.conductor.integrations.framework.ConnectorAdapter adapter =
        mock(com.conductor.integrations.framework.ConnectorAdapter.class);
    when(adapter.execute(any(), eq("bad-action"), any())).thenAnswer(throwUnsupported);
    when(connectorRegistry.getAdapter("shopify")).thenReturn(Optional.of(adapter));

    mockMvc
        .perform(
            post("/api/v1/integrations/" + id + "/execute")
                .param("action", "bad-action")
                .with(csrf())
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.type").value("https://conductor.io/errors/unsupported-action"));
  }
}
