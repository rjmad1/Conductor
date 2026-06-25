package com.conductor.workflow.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Acceptance test: Tenant isolation guarantees for workflow resources.
 *
 * <p>Creates resources for Tenant A and Tenant B and verifies that cross-tenant data access is
 * prevented at every layer: HTTP response, repository query, and DB row.
 */
@DisplayName("Acceptance: Workflow Tenant Isolation")
class WorkflowTenantIsolationAcceptanceTest extends BaseAcceptanceTest {

  @Autowired private WorkflowDefinitionRepository definitionRepository;
  @Autowired private WorkflowExecutionRepository executionRepository;

  private UUID tenantA;
  private UUID tenantB;

  @BeforeEach
  void setUp() {
    tenantA = UUID.randomUUID();
    tenantB = UUID.randomUUID();
  }

  @AfterEach
  void tearDown() {
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  @Test
  @DisplayName("Tenant A cannot read Tenant B's workflow definition")
  void tenantA_cannotReadTenantB_definition() throws Exception {
    // Seed definition for Tenant B directly in DB (bypasses HTTP to keep tenant context clean)
    UUID tenantBDefinitionId = seedDefinition(tenantB);

    // Tenant A requests Tenant B's definition — must receive 404 (not 403, no data leak)
    performAs(get("/api/v1/workflows/" + tenantBDefinitionId), platformAdminJwt(), tenantA)
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("List endpoint only returns definitions for the requesting tenant")
  void list_returnsOnlyCurrentTenantDefinitions() throws Exception {
    // Seed one definition each for A and B
    UUID defA = seedDefinition(tenantA);
    UUID defB = seedDefinition(tenantB);

    // List as Tenant A
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    MvcResult result =
        performAs(get("/api/v1/workflows?limit=50"), platformAdminJwt(), tenantA)
            .andExpect(status().isOk())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    // Tenant A's definition must be present
    assertThat(body).contains(defA.toString());
    // Tenant B's definition must NOT appear
    assertThat(body).doesNotContain(defB.toString());
  }

  @Test
  @DisplayName("Execution list only returns executions for the requesting tenant")
  void executionList_returnsOnlyCurrentTenantExecutions() throws Exception {
    UUID defA = seedPublishedDefinition(tenantA);
    UUID defB = seedPublishedDefinition(tenantB);

    UUID execA = seedExecution(tenantA, defA);
    UUID execB = seedExecution(tenantB, defB);

    // List as Tenant A
    MvcResult result =
        performAs(get("/api/v1/workflows/executions?limit=50"), platformAdminJwt(), tenantA)
            .andExpect(status().isOk())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    assertThat(body).contains(execA.toString());
    assertThat(body).doesNotContain(execB.toString());
  }

  @Test
  @DisplayName("Tenant A cannot trigger execution on Tenant B's workflow")
  void tenantA_cannotExecuteTenantB_workflow() throws Exception {
    UUID defB = seedPublishedDefinition(tenantB);

    // Tenant A sends execute request pointing to Tenant B's definition
    performAs(
            post("/api/v1/workflows/" + defB + "/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("phone", "+15555550999", "name", "Attacker"))),
            platformAdminJwt(),
            tenantA) // X-Tenant-ID = tenantA
        .andExpect(status().isNotFound()); // definition not found for tenant A
  }

  @Test
  @DisplayName("Tenant A cannot retrieve Tenant B's execution")
  void tenantA_cannotReadTenantB_execution() throws Exception {
    UUID defB = seedPublishedDefinition(tenantB);
    UUID execB = seedExecution(tenantB, defB);

    performAs(get("/api/v1/workflows/executions/" + execB), platformAdminJwt(), tenantA)
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Workflow definitions are strictly partitioned — DB row count verification")
  void dbPartition_definitionRows() {
    UUID def1 = seedDefinition(tenantA);
    UUID def2 = seedDefinition(tenantA);
    UUID def3 = seedDefinition(tenantB);

    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantA);
    List<WorkflowDefinition> allDefs = definitionRepository.findAll();
    // With Hibernate tenant filter active, only tenant A's records should appear
    long tenantACount = allDefs.stream().filter(d -> tenantA.equals(d.getTenantId())).count();
    long tenantBCount = allDefs.stream().filter(d -> tenantB.equals(d.getTenantId())).count();

    assertThat(tenantACount).isGreaterThanOrEqualTo(2);
    // Tenant B rows must not be visible through the filtered repository
    assertThat(tenantBCount).isZero();
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private UUID seedDefinition(UUID tId) {
    WorkflowDefinition def =
        WorkflowDefinition.builder()
            .name("Isolation Test Workflow")
            .triggerType(TriggerType.MANUAL)
            .triggerConfig("{}")
            .steps("[]")
            .variables("{}")
            .versionStatus(WorkflowVersionStatus.DRAFT)
            .version(1)
            .createdBy("acceptance-test")
            .build();
    def.setTenantId(tId);
    return definitionRepository.save(def).getId();
  }

  private UUID seedPublishedDefinition(UUID tId) {
    WorkflowDefinition def =
        WorkflowDefinition.builder()
            .name("Isolation Published Workflow")
            .triggerType(TriggerType.MANUAL)
            .triggerConfig("{}")
            .steps("[]")
            .variables("{}")
            .versionStatus(WorkflowVersionStatus.PUBLISHED)
            .version(1)
            .createdBy("acceptance-test")
            .build();
    def.setTenantId(tId);
    return definitionRepository.save(def).getId();
  }

  private UUID seedExecution(UUID tId, UUID defId) {
    WorkflowExecution exec =
        WorkflowExecution.builder()
            .definitionId(defId)
            .definitionVersion(1)
            .status(com.conductor.shared.workflow.WorkflowStatus.RUNNING)
            .input("{}")
            .variables("{}")
            .createdBy("acceptance-test")
            .build();
    exec.setTenantId(tId);
    return executionRepository.save(exec).getId();
  }
}
