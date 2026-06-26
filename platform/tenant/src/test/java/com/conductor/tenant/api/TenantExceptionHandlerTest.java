package com.conductor.tenant.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.conductor.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TenantController.class, TenantExceptionHandler.class})
class TenantExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TenantService tenantService;
  @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;
  @MockBean private com.conductor.shared.middleware.tenant.TenantFilterAspect tenantFilterAspect;

  @Test
  @WithMockUser(roles = "PLATFORM_ADMIN")
  void missingRequiredField_returns400WithValidationErrors() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"acme\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.type").value("https://conductor.io/errors/validation"))
        .andExpect(jsonPath("$.errors").exists());
  }

  @Test
  @WithMockUser(roles = "PLATFORM_ADMIN")
  void serviceThrowsIllegalArgument_returns404WithProblemDetail() throws Exception {
    when(tenantService.createTenant(any(), any(), any(), any(), any(), any(), any()))
        .thenThrow(new IllegalArgumentException("Tenant plan not found"));

    mockMvc
        .perform(
            post("/api/v1/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"tenantKey\":\"acme\",\"displayName\":\"Acme Corp\",\"domain\":\"acme.io\"}"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.type").value("https://conductor.io/errors/tenant-not-found"));
  }
}
