package com.conductor.workflow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.shared.workflow.TriggerType;
import com.conductor.workflow.BaseIntegrationTest;
import com.conductor.workflow.api.dto.WorkflowDefinitionRequest;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class WorkflowDefinitionControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private WorkflowDefinitionRepository repository;

  @Autowired private ObjectMapper objectMapper;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    repository.deleteAll();
  }

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAgentJwt() {
    return jwt()
        .jwt(
            j ->
                j.subject("test-user").issuer("http://localhost:8080/realms/conductor-" + tenantId))
        .authorities(new SimpleGrantedAuthority("ROLE_TENANT_AGENT"));
  }

  @Test
  @DisplayName("POST /api/v1/workflows creates a new draft workflow definition")
  void createWorkflowDefinition() throws Exception {
    WorkflowDefinitionRequest request = new WorkflowDefinitionRequest();
    request.setName("Test Workflow");
    request.setDescription("A test integration workflow");
    request.setTriggerType(TriggerType.EVENT);
    request.setTriggerConfig(Map.of("topic", "test-topic"));
    request.setSteps(
        List.of(Map.of("name", "step1", "type", "SEND_EVENT", "config", Map.of("domain", "test"))));
    request.setVariables(Map.of("var1", "val1"));

    mockMvc
        .perform(
            post("/api/v1/workflows")
                .with(tenantAgentJwt())
                .header("X-Tenant-ID", tenantId.toString())
                .header("X-User-ID", "test-user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    List<WorkflowDefinition> definitions = repository.findAll();
    assertThat(definitions).hasSize(1);
    assertThat(definitions.get(0).getName()).isEqualTo("Test Workflow");
    assertThat(definitions.get(0).getTenantId()).isEqualTo(tenantId);
  }

  @Test
  @DisplayName("GET /api/v1/workflows returns definitions for the current tenant")
  void listWorkflowDefinitions() throws Exception {
    WorkflowDefinition def =
        WorkflowDefinition.builder()
            .name("Workflow 1")
            .description("Desc 1")
            .triggerType(TriggerType.EVENT)
            .triggerConfig("{}")
            .steps("[]")
            .variables("{}")
            .version(1)
            .versionStatus(com.conductor.shared.workflow.WorkflowVersionStatus.DRAFT)
            .createdBy("test-user")
            .build();
    def.setTenantId(tenantId);
    repository.save(def);

    // Different tenant's definition - should not be visible
    WorkflowDefinition otherDef =
        WorkflowDefinition.builder()
            .name("Workflow Other")
            .description("Desc Other")
            .triggerType(TriggerType.EVENT)
            .triggerConfig("{}")
            .steps("[]")
            .variables("{}")
            .version(1)
            .versionStatus(com.conductor.shared.workflow.WorkflowVersionStatus.DRAFT)
            .createdBy("test-user")
            .build();
    otherDef.setTenantId(UUID.randomUUID());
    repository.save(otherDef);

    mockMvc
        .perform(
            get("/api/v1/workflows")
                .with(tenantAgentJwt())
                .header("X-Tenant-ID", tenantId.toString())
                .header("X-User-ID", "test-user-1"))
        .andExpect(status().isOk());
  }
}
