package com.conductor.integrations;

import com.conductor.integrations.api.IntegrationController;
import com.conductor.integrations.domain.*;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.service.CredentialService;
import com.conductor.integrations.framework.ConnectorRegistry;
import com.conductor.shared.middleware.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.Import;

@WebMvcTest(IntegrationController.class)
@Import(com.conductor.integrations.config.IntegrationsSecurityConfig.class)
class TenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConnectorRepository connectorRepository;

    @MockBean
    private IntegrationRepository integrationRepository;

    @MockBean
    private ConnectionRepository connectionRepository;

    @MockBean
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @MockBean
    private ExecutionHistoryRepository executionHistoryRepository;

    @MockBean
    private CredentialService credentialService;

    @MockBean
    private ConnectorRegistry connectorRegistry;

    @MockBean
    private com.conductor.shared.middleware.tenant.AuditLogger auditLogger;

    @MockBean
    private com.conductor.shared.messaging.EventPublisher eventPublisher;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;
    @MockBean
    private com.conductor.shared.auth.ConductorAuthenticationEntryPoint conductorAuthenticationEntryPoint;
    @MockBean
    private com.conductor.shared.auth.ConductorAccessDeniedHandler conductorAccessDeniedHandler;

    @MockBean
    private com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;

    @MockBean
    private com.conductor.integrations.framework.OAuthStateStore oauthStateStore;

    @MockBean
    private com.conductor.integrations.framework.CredentialEncryptor credentialEncryptor;

    @MockBean
    private com.conductor.integrations.service.OAuthTokenExchangeService oauthTokenExchangeService;

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
    @WithMockUser(roles = "TENANT_ADMIN")
    void givenActiveTenant_whenGetHistory_thenOk() throws Exception {
        UUID integrationId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/integrations/" + integrationId + "/history")
                        .header("X-Tenant-ID", tenantId.toString())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void givenActiveTenant_whenGetCrossTenantHealth_thenReturn404() throws Exception {
        UUID crossTenantIntegrationId = UUID.randomUUID();

        when(integrationRepository.findByIdAndTenantId(crossTenantIntegrationId, tenantId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/integrations/" + crossTenantIntegrationId + "/health")
                        .header("X-Tenant-ID", tenantId.toString())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void givenActiveTenant_whenExecuteCrossTenant_thenReturn404() throws Exception {
        UUID crossTenantIntegrationId = UUID.randomUUID();

        when(integrationRepository.findByIdAndTenantId(crossTenantIntegrationId, tenantId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/integrations/" + crossTenantIntegrationId + "/execute")
                        .param("action", "test")
                        .header("X-Tenant-ID", tenantId.toString())
                        .with(csrf())
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
