package com.conductor.workflow.temporal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conductor.shared.execution.ExecutionContext;
import io.temporal.client.WorkflowClient;
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
import org.mockito.Mockito;

/**
 * Integration tests for ConductorWorkflowImpl using Temporal's TestWorkflowEnvironment. All
 * activities are mocked to isolate workflow logic.
 */
class WorkflowExecutionIntegrationTest {

  private static final String TASK_QUEUE = "test-workflow-queue";

  private TestWorkflowEnvironment testEnv;
  private WorkflowClient workflowClient;
  private ConductorActivities mockActivities;

  @BeforeEach
  void setUp() {
    testEnv = TestWorkflowEnvironment.newInstance();
    workflowClient = testEnv.getWorkflowClient();
    mockActivities = Mockito.mock(ConductorActivities.class);

    Worker worker = testEnv.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(ConductorWorkflowImpl.class);
    worker.registerActivitiesImplementations(new TestActivities(mockActivities));
    testEnv.start();
  }

  @AfterEach
  void tearDown() {
    testEnv.close();
  }

  @Test
  @DisplayName("Workflow with two sequential steps completes successfully")
  void sequentialStepsComplete() {
    List<Map<String, Object>> steps =
        List.of(
            Map.of("name", "step-one", "type", "SEND_EVENT", "config", Map.of("domain", "test")),
            Map.of("name", "step-two", "type", "ASSIGN_USER", "config", Map.of("role", "admin")));

    when(mockActivities.loadWorkflowSteps(anyString(), anyString())).thenReturn(steps);
    when(mockActivities.executeStep(anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(Map.of("status", "ok"));
    doNothing().when(mockActivities).markExecutionCompleted(anyString(), anyString());

    ExecutionContext ctx = buildContext();
    ConductorWorkflow workflow =
        workflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    verify(mockActivities).markExecutionCompleted(ctx.getExecutionId().toString(), "{}");
  }

  @Test
  @DisplayName("Workflow marks failed when an activity throws")
  void workflowMarksFailedOnActivityError() {
    List<Map<String, Object>> steps =
        List.of(
            Map.of(
                "name",
                "failing-step",
                "type",
                "INVOKE_INTEGRATION",
                "config",
                Map.of("integrationId", "missing")));

    when(mockActivities.loadWorkflowSteps(anyString(), anyString())).thenReturn(steps);
    when(mockActivities.executeStep(anyString(), anyString(), anyString(), anyString(), any()))
        .thenThrow(new RuntimeException("integration unavailable"));
    doNothing().when(mockActivities).markExecutionFailed(anyString(), anyString());

    ExecutionContext ctx = buildContext();
    ConductorWorkflow workflow =
        workflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    verify(mockActivities).markExecutionFailed(anyString(), anyString());
  }

  @Test
  @DisplayName("Workflow skips step when condition is false")
  void conditionFalseSkipsStep() {
    List<Map<String, Object>> steps =
        List.of(
            Map.of(
                "name",
                "conditional-step",
                "type",
                "SEND_EVENT",
                "config",
                Map.of("domain", "test"),
                "condition",
                Map.of("field", "status", "operator", "EQUALS", "value", "inactive")));

    when(mockActivities.loadWorkflowSteps(anyString(), anyString())).thenReturn(steps);
    doNothing().when(mockActivities).markExecutionCompleted(anyString(), anyString());

    ExecutionContext ctx =
        ExecutionContext.builder()
            .workflowDefinitionId(UUID.randomUUID())
            .executionId(UUID.randomUUID())
            .tenantId("tenant-1")
            .variables(Map.of("status", "active")) // condition expects "inactive" -> skip
            .build();

    ConductorWorkflow workflow =
        workflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    // executeStep should NOT be called since condition is false
    verify(mockActivities, Mockito.never())
        .executeStep(anyString(), anyString(), anyString(), anyString(), any());
    verify(mockActivities).markExecutionCompleted(anyString(), anyString());
  }

  @Test
  @DisplayName("Workflow with no steps completes immediately")
  void emptyStepsCompleteImmediately() {
    when(mockActivities.loadWorkflowSteps(anyString(), anyString())).thenReturn(List.of());
    doNothing().when(mockActivities).markExecutionCompleted(anyString(), anyString());

    ExecutionContext ctx = buildContext();
    ConductorWorkflow workflow =
        workflowClient.newWorkflowStub(
            ConductorWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
    workflow.execute(ctx);

    verify(mockActivities).markExecutionCompleted(anyString(), anyString());
  }

  private ExecutionContext buildContext() {
    return ExecutionContext.builder()
        .workflowDefinitionId(UUID.randomUUID())
        .executionId(UUID.randomUUID())
        .tenantId("tenant-1")
        .variables(Map.of("status", "active"))
        .build();
  }

  private static class TestActivities implements ConductorActivities {
    private final ConductorActivities delegate;

    public TestActivities(ConductorActivities delegate) {
      this.delegate = delegate;
    }

    @Override
    public List<Map<String, Object>> loadWorkflowSteps(String definitionId, String tenantId) {
      return delegate.loadWorkflowSteps(definitionId, tenantId);
    }

    @Override
    public Map<String, Object> executeStep(
        String executionId,
        String tenantId,
        String stepName,
        String actionType,
        Map<String, Object> config) {
      return delegate.executeStep(executionId, tenantId, stepName, actionType, config);
    }

    @Override
    public void markExecutionCompleted(String executionId, String outputJson) {
      delegate.markExecutionCompleted(executionId, outputJson);
    }

    @Override
    public void markExecutionFailed(String executionId, String reason) {
      delegate.markExecutionFailed(executionId, reason);
    }

    @Override
    public void compensateExecution(String executionId, String tenantId) {
      delegate.compensateExecution(executionId, tenantId);
    }

    @Override
    public void markExecutionCompensated(String executionId) {
      delegate.markExecutionCompensated(executionId);
    }
  }
}
