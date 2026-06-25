package com.conductor.workflow.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.conductor.shared.execution.ExecutionContext;
import com.conductor.shared.execution.action.ActionRegistry;
import com.conductor.shared.execution.provider.Provider;
import com.conductor.shared.execution.provider.ProviderRegistry;
import com.conductor.shared.execution.provider.ProviderResponse;
import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.BaseIntegrationTest;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowStepExecution;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowStepExecutionRepository;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EndToEndWelcomeWorkflowIntegrationTest extends BaseIntegrationTest {

  private static final String TASK_QUEUE = "test-e2e-welcome-queue";

  private TestWorkflowEnvironment testEnv;
  private io.temporal.client.WorkflowClient testWorkflowClient;

  @Autowired private ConductorActivitiesImpl conductorActivities;
  @Autowired private WorkflowDefinitionRepository definitionRepository;
  @Autowired private WorkflowExecutionRepository executionRepository;
  @Autowired private WorkflowStepExecutionRepository stepRepository;
  @Autowired private ProviderRegistry providerRegistry;
  @Autowired private ActionRegistry actionRegistry;

  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);

    testEnv = TestWorkflowEnvironment.newInstance();
    testWorkflowClient = testEnv.getWorkflowClient();

    Worker worker = testEnv.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(ConductorWorkflowImpl.class);
    worker.registerActivitiesImplementations(conductorActivities);
    testEnv.start();
  }

  @AfterEach
  void tearDown() {
    testEnv.close();
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  @Test
  void testWelcomeWorkflowExecution() {
    assertThat(actionRegistry.getHandler("WELCOME_MESSAGE")).isPresent();

    Provider mockWhatsAppProvider = mock(Provider.class);
    when(mockWhatsAppProvider.getDefinition())
        .thenReturn(
            com.conductor.shared.execution.provider.ProviderDefinition.builder()
                .type("WHATSAPP")
                .name("WhatsApp Provider Mock")
                .version("1.0")
                .build());
    when(mockWhatsAppProvider.execute(any()))
        .thenReturn(
            ProviderResponse.builder()
                .statusCode(200)
                .success(true)
                .body(
                    "{\"messaging_product\":\"whatsapp\",\"messages\":[{\"id\":\"msg_e2e_123\"}]}")
                .build());
    providerRegistry.register(mockWhatsAppProvider);

    WorkflowDefinition definition =
        WorkflowDefinition.builder()
            .name("Welcome Workflow")
            .description("E2E Welcome Workflow Test")
            .triggerType(TriggerType.EVENT)
            .triggerConfig("{}")
            .steps(
                "["
                    + "  {"
                    + "    \"name\": \"send-welcome-whatsapp\","
                    + "    \"type\": \"WELCOME_MESSAGE\","
                    + "    \"config\": {"
                    + "      \"recipient\": \"{phone}\","
                    + "      \"message\": \"Welcome to Conductor, {name}!\""
                    + "    }"
                    + "  }"
                    + "]")
            .variables("{\"name\":\"\",\"phone\":\"\"}")
            .versionStatus(WorkflowVersionStatus.PUBLISHED)
            .version(1)
            .build();
    definition.setTenantId(tenantId);
    WorkflowDefinition savedDef = definitionRepository.save(definition);

    WorkflowExecution execution =
        WorkflowExecution.builder()
            .definitionId(savedDef.getId())
            .definitionVersion(1)
            .status(WorkflowStatus.RUNNING)
            .input("{\"phone\":\"+1234567890\",\"name\":\"Alice\"}")
            .variables("{\"phone\":\"+1234567890\",\"name\":\"Alice\"}")
            .build();
    execution.setTenantId(tenantId);
    WorkflowExecution savedExec = executionRepository.save(execution);

    ExecutionContext ctx =
        ExecutionContext.builder()
            .workflowDefinitionId(savedDef.getId())
            .executionId(savedExec.getId())
            .tenantId(tenantId.toString())
            .variables(Map.of("phone", "+1234567890", "name", "Alice"))
            .build();

    ConductorWorkflow workflow =
        testWorkflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    WorkflowExecution completedExec = executionRepository.findById(savedExec.getId()).orElseThrow();
    assertThat(completedExec.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);

    List<WorkflowStepExecution> steps =
        stepRepository.findByExecutionIdOrderByStartedAtAsc(savedExec.getId());
    assertThat(steps).hasSize(1);
    assertThat(steps.get(0).getStepName()).isEqualTo("send-welcome-whatsapp");
    assertThat(steps.get(0).getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
    assertThat(steps.get(0).getOutput()).contains("msg_e2e_123");
  }
}
