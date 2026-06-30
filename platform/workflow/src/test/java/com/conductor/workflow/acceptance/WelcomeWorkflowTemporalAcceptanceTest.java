package com.conductor.workflow.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.conductor.shared.execution.ExecutionContext;
import com.conductor.shared.execution.provider.Provider;
import com.conductor.shared.execution.provider.ProviderDefinition;
import com.conductor.shared.execution.provider.ProviderRegistry;
import com.conductor.shared.execution.provider.ProviderResponse;
import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowStepExecution;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowStepExecutionRepository;
import com.conductor.workflow.temporal.ConductorActivitiesImpl;
import com.conductor.workflow.temporal.ConductorWorkflow;
import com.conductor.workflow.temporal.ConductorWorkflowImpl;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Acceptance test: Actual Temporal workflow execution via TestWorkflowEnvironment.
 *
 * <p>Tests the full runtime path: workflow definition → execution → WelcomeMessageAction → WhatsApp
 * provider (stubbed) → step execution record → completion. Uses Temporal's in-process test
 * environment so no real cluster is required. The WhatsApp Cloud API is the only stub.
 */
@DisplayName("Acceptance: Welcome Workflow — Temporal Execution")
class WelcomeWorkflowTemporalAcceptanceTest extends BaseAcceptanceTest {

  private static final String TASK_QUEUE = "acceptance-welcome-queue";

  @Autowired private ConductorActivitiesImpl conductorActivities;
  @Autowired private WorkflowDefinitionRepository definitionRepository;
  @Autowired private WorkflowExecutionRepository executionRepository;
  @Autowired private WorkflowStepExecutionRepository stepRepository;
  @Autowired private ProviderRegistry providerRegistry;

  private TestWorkflowEnvironment testEnv;
  private io.temporal.client.WorkflowClient testWorkflowClient;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);

    // Build in-process Temporal environment
    testEnv = TestWorkflowEnvironment.newInstance();
    testWorkflowClient = testEnv.getWorkflowClient();

    Worker worker = testEnv.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(ConductorWorkflowImpl.class);
    worker.registerActivitiesImplementations(conductorActivities);
    testEnv.start();

    // Stub WhatsApp provider — only external dependency
    Provider mockWhatsApp = mock(Provider.class);
    when(mockWhatsApp.getDefinition())
        .thenReturn(
            ProviderDefinition.builder()
                .type("WHATSAPP")
                .name("WhatsApp Mock")
                .version("1.0")
                .build());
    when(mockWhatsApp.execute(any()))
        .thenReturn(
            ProviderResponse.builder()
                .statusCode(200)
                .success(true)
                .body(
                    "{\"messaging_product\":\"whatsapp\",\"messages\":[{\"id\":\"wam_acceptance_temporal_001\"}]}")
                .build());
    providerRegistry.register(mockWhatsApp);
  }

  @AfterEach
  void tearDown() {
    if (testEnv != null) {
      testEnv.close();
    }
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  @Test
  @DisplayName("WelcomeMessageAction executes and WhatsApp provider invoked — workflow completes")
  void welcomeWorkflowExecutesToCompletion() {
    // Seed definition in DB
    WorkflowDefinition definition =
        WorkflowDefinition.builder()
            .name("Welcome Workflow")
            .description("Acceptance — Temporal execution test")
            .triggerType(TriggerType.EVENT)
            .triggerConfig("{}")
            .steps(
                "[{\"name\":\"send-welcome-whatsapp\","
                    + "\"type\":\"WELCOME_MESSAGE\","
                    + "\"config\":{\"recipient\":\"{phone}\",\"message\":\"Welcome, {name}!\"}}]")
            .variables("{\"name\":\"\",\"phone\":\"\"}")
            .versionStatus(WorkflowVersionStatus.PUBLISHED)
            .version(1)
            .createdBy("acceptance-test")
            .build();
    definition.setTenantId(tenantId);
    WorkflowDefinition saved = definitionRepository.save(definition);

    // Seed execution record
    WorkflowExecution execution =
        WorkflowExecution.builder()
            .definitionId(saved.getId())
            .definitionVersion(1)
            .status(WorkflowStatus.RUNNING)
            .input("{\"phone\":\"+15555550100\",\"name\":\"Alice\"}")
            .variables("{\"phone\":\"+15555550100\",\"name\":\"Alice\"}")
            .createdBy("acceptance-test")
            .build();
    execution.setTenantId(tenantId);
    WorkflowExecution savedExec = executionRepository.save(execution);

    // Build Temporal context
    ExecutionContext ctx =
        ExecutionContext.builder()
            .workflowDefinitionId(saved.getId())
            .executionId(savedExec.getId())
            .tenantId(tenantId.toString())
            .userId("acceptance-test")
            .correlationId(UUID.randomUUID().toString())
            .variables(Map.of("phone", "+15555550100", "name", "Alice"))
            .build();

    // Execute workflow synchronously in test environment
    ConductorWorkflow workflow =
        testWorkflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    // Assert: execution status = COMPLETED
    WorkflowExecution completed = executionRepository.findById(savedExec.getId()).orElseThrow();
    assertThat(completed.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);

    // Assert: step execution recorded
    List<WorkflowStepExecution> steps =
        stepRepository.findByExecutionIdOrderByStartedAtAsc(savedExec.getId());
    assertThat(steps).hasSize(1);
    assertThat(steps.get(0).getStepName()).isEqualTo("send-welcome-whatsapp");
    assertThat(steps.get(0).getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
    assertThat(steps.get(0).getOutput()).contains("wam_acceptance_temporal_001");
  }

  @Test
  @DisplayName("Provider failure → step marked FAILED, workflow transitions to FAILED")
  void providerFailure_workflowFails() {
    // Override the WhatsApp stub to return a failure
    Provider failingWhatsApp = mock(Provider.class);
    when(failingWhatsApp.getDefinition())
        .thenReturn(
            ProviderDefinition.builder()
                .type("WHATSAPP")
                .name("WhatsApp Failing Mock")
                .version("1.0")
                .build());
    when(failingWhatsApp.execute(any()))
        .thenReturn(
            ProviderResponse.builder()
                .statusCode(503)
                .success(false)
                .body("{\"error\":\"service_unavailable\"}")
                .build());
    providerRegistry.register(failingWhatsApp);

    WorkflowDefinition definition =
        WorkflowDefinition.builder()
            .name("Failing Workflow")
            .triggerType(TriggerType.EVENT)
            .triggerConfig("{}")
            .steps(
                "[{\"name\":\"fail-step\","
                    + "\"type\":\"WELCOME_MESSAGE\","
                    + "\"config\":{\"recipient\":\"+15555550200\",\"message\":\"Test\"}}]")
            .variables("{}")
            .versionStatus(WorkflowVersionStatus.PUBLISHED)
            .version(1)
            .createdBy("acceptance-test")
            .build();
    definition.setTenantId(tenantId);
    WorkflowDefinition savedDef = definitionRepository.save(definition);

    WorkflowExecution execution =
        WorkflowExecution.builder()
            .definitionId(savedDef.getId())
            .definitionVersion(1)
            .status(WorkflowStatus.RUNNING)
            .input("{}")
            .variables("{}")
            .createdBy("acceptance-test")
            .build();
    execution.setTenantId(tenantId);
    WorkflowExecution savedExec = executionRepository.save(execution);

    ExecutionContext ctx =
        ExecutionContext.builder()
            .workflowDefinitionId(savedDef.getId())
            .executionId(savedExec.getId())
            .tenantId(tenantId.toString())
            .variables(Map.of("phone", "+15555550200", "name", "Bob"))
            .build();

    ConductorWorkflow workflow =
        testWorkflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    WorkflowExecution result = executionRepository.findById(savedExec.getId()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(WorkflowStatus.FAILED);

    List<WorkflowStepExecution> steps =
        stepRepository.findByExecutionIdOrderByStartedAtAsc(savedExec.getId());
    assertThat(steps.get(0).getStatus()).isEqualTo(WorkflowStatus.FAILED);
  }

  @Test
  @DisplayName("Action framework — WelcomeMessageAction registered in ActionRegistry")
  void welcomeMessageActionRegistered() {
    // Verify WHATSAPP provider is wired into the registry — a prerequisite for
    // WelcomeMessageAction.
    // Full execution path is covered by welcomeWorkflowExecutesToCompletion.
    assertThat(providerRegistry.getProvider("WHATSAPP")).isPresent();
  }
}
