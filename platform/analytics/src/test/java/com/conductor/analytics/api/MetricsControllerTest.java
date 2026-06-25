package com.conductor.analytics.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.analytics.config.AnalyticsSecurityConfig;
import com.conductor.analytics.metrics.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Web MVC tests for MetricsController authentication and response behavior. */
@WebMvcTest(MetricsController.class)
@Import(AnalyticsSecurityConfig.class)
@AutoConfigureMockMvc
class MetricsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

  @MockBean private WorkflowMetricsService workflowMetrics;
  @MockBean private MessagingMetricsService messagingMetrics;
  @MockBean private CustomerMetricsService customerMetrics;
  @MockBean private IntegrationMetricsService integrationMetrics;
  @MockBean private TenantMetricsService tenantMetrics;

  @Test
  void unauthenticatedRequestReturns401() throws Exception {
    mockMvc.perform(get("/api/v1/analytics/metrics/workflow")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void authenticatedWorkflowMetricsReturns200() throws Exception {
    when(workflowMetrics.getWorkflowMetrics(anyString(), any(), any()))
        .thenReturn(Map.of("executions", 100));

    mockMvc.perform(get("/api/v1/analytics/metrics/workflow")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void authenticatedMessagingMetricsReturns200() throws Exception {
    when(messagingMetrics.getMessagingMetrics(anyString(), any(), any()))
        .thenReturn(Map.of("messageCounts", Map.of()));

    mockMvc.perform(get("/api/v1/analytics/metrics/messaging")).andExpect(status().isOk());
  }
}
