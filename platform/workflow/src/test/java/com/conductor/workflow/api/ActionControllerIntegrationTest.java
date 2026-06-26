package com.conductor.workflow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.workflow.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Controller-level integration tests for pluggable actions API. */
@AutoConfigureMockMvc
class ActionControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
  }

  @Test
  @DisplayName("GET /api/v1/actions lists all pluggable actions")
  void testListActions() throws Exception {
    String json =
        mockMvc
            .perform(
                get("/api/v1/actions")
                    .with(
                        jwt()
                            .jwt(
                                j ->
                                    j.subject("test-user")
                                        .issuer(
                                            "http://localhost:8080/realms/conductor-" + tenantId)))
                    .header("X-Tenant-ID", tenantId.toString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(json).contains("LOG");
    assertThat(json).contains("CREATE_AUDIT_RECORD");
    assertThat(json).contains("INVOKE_INTERNAL_SERVICE");
  }

  @Test
  @DisplayName("GET /api/v1/actions/LOG/metadata retrieves log action schema metadata")
  void testGetMetadata() throws Exception {
    String json =
        mockMvc
            .perform(
                get("/api/v1/actions/LOG/metadata")
                    .with(
                        jwt()
                            .jwt(
                                j ->
                                    j.subject("test-user")
                                        .issuer(
                                            "http://localhost:8080/realms/conductor-" + tenantId)))
                    .header("X-Tenant-ID", tenantId.toString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(json).contains("message");
    assertThat(json).contains("Log Message");
  }

  @Test
  @DisplayName("POST /api/v1/actions/LOG/validate validates valid and invalid configs")
  void testValidateConfig() throws Exception {
    // Valid config
    mockMvc
        .perform(
            post("/api/v1/actions/LOG/validate")
                .with(
                    jwt()
                        .jwt(
                            j ->
                                j.subject("test-user")
                                    .issuer("http://localhost:8080/realms/conductor-" + tenantId)))
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "valid log text"))))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("\"valid\":true");
            });

    // Invalid config (missing required 'message')
    mockMvc
        .perform(
            post("/api/v1/actions/LOG/validate")
                .with(
                    jwt()
                        .jwt(
                            j ->
                                j.subject("test-user")
                                    .issuer("http://localhost:8080/realms/conductor-" + tenantId)))
                .header("X-Tenant-ID", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("level", "INFO"))))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              assertThat(body).contains("\"valid\":false");
              assertThat(body).contains("Missing required configuration parameter: message");
            });
  }
}
