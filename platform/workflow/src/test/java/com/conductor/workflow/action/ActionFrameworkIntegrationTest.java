package com.conductor.workflow.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.conductor.shared.execution.action.ActionRegistry;
import com.conductor.shared.execution.action.ActionValidator;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.workflow.ActionType;
import com.conductor.workflow.BaseIntegrationTest;
import com.conductor.workflow.domain.ActionExecution;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowStepExecution;
import com.conductor.workflow.repository.ActionExecutionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowStepExecutionRepository;
import com.conductor.workflow.service.ActionExecutor;
import com.conductor.workflow.temporal.ConductorActivities;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class ActionFrameworkIntegrationTest extends BaseIntegrationTest {

  @Autowired private ActionRegistry actionRegistry;

  @Autowired private ActionValidator actionValidator;

  @Autowired private ActionExecutor actionExecutor;

  @Autowired private ActionExecutionRepository actionExecutionRepository;

  @Autowired private WorkflowExecutionRepository workflowExecutionRepository;

  @Autowired private WorkflowStepExecutionRepository workflowStepExecutionRepository;

  @Autowired private ConductorActivities conductorActivities;

  @MockBean private EventPublisher mockEventPublisher;

  @MockBean private AuditLogger mockAuditLogger;

  @org.springframework.boot.test.context.TestConfiguration
  static class TestConfig {
    @org.springframework.context.annotation.Bean("testService")
    public TestService testService() {
      return new TestService();
    }
  }

  public static class TestService {
    public String concat(String first, String second) {
      return first + "-" + second;
    }
  }

  private UUID tenantId;

  @BeforeEach
  void cleanDb() {
    actionExecutionRepository.deleteAll();
    workflowExecutionRepository.deleteAll();
    workflowStepExecutionRepository.deleteAll();
    tenantId = UUID.randomUUID();
    com.conductor.shared.middleware.tenant.TenantContext.setCurrentTenantId(tenantId);
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() {
    com.conductor.shared.middleware.tenant.TenantContext.clear();
  }

  @Test
  void testActionRegistryDiscoversHandlers() {
    assertThat(actionRegistry.getHandler("LOG")).isPresent();
    assertThat(actionRegistry.getHandler("CREATE_AUDIT_RECORD")).isPresent();
    assertThat(actionRegistry.getHandler("INVOKE_INTERNAL_SERVICE")).isPresent();
    assertThat(actionRegistry.getHandler("PUBLISH_INTERNAL_DOMAIN_EVENT")).isPresent();
    assertThat(actionRegistry.getHandler("DELAY")).isPresent();
  }

  @Test
  void testLogActionExecutionAndInterpolation() {
    Map<String, Object> config =
        Map.of("message", "User {userName} has role {roleName}", "level", "WARN");

    WorkflowExecution execution =
        WorkflowExecution.builder()
            .definitionId(UUID.randomUUID())
            .definitionVersion(1)
            .variables("{\"userName\":\"john_doe\",\"roleName\":\"admin\"}")
            .build();
    execution.setTenantId(tenantId);
    workflowExecutionRepository.save(execution);

    Map<String, Object> output =
        actionExecutor.execute(ActionType.LOG, config, tenantId.toString(), execution.getId());

    assertThat(output).containsKey("loggedMessage");
    assertThat(output.get("loggedMessage")).isEqualTo("User john_doe has role admin");

    List<ActionExecution> history =
        actionExecutionRepository.findByWorkflowExecutionId(execution.getId());
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getActionType()).isEqualTo("LOG");
    assertThat(history.get(0).getStatus()).isEqualTo("SUCCESS");
    assertThat(history.get(0).getExecutionDurationMs()).isNotNull();
  }

  @Test
  void testCreateAuditRecordAction() {
    Map<String, Object> config =
        Map.of(
            "action", "USER_LOGIN",
            "resource", "auth_server",
            "outcome", "SUCCESS",
            "details", "User login verified from IP 127.0.0.1");

    actionExecutor.execute(ActionType.CREATE_AUDIT_RECORD, config, tenantId.toString());

    verify(mockAuditLogger)
        .logEvent(
            eq("USER_LOGIN"),
            eq("auth_server"),
            eq("SUCCESS"),
            eq("User login verified from IP 127.0.0.1"));
  }

  @Test
  void testPublishInternalDomainEventAction() {
    Map<String, Object> config =
        Map.of(
            "domain", "order",
            "entity", "cart",
            "action", "checked_out",
            "payload", Map.of("cartId", "12345", "amount", 150.00));

    actionExecutor.execute(ActionType.PUBLISH_INTERNAL_DOMAIN_EVENT, config, tenantId.toString());

    verify(mockEventPublisher)
        .publish(
            eq("order"),
            eq("cart"),
            eq("checked_out"),
            eq("1.0.0"),
            eq(Map.of("cartId", "12345", "amount", 150.00)));
  }

  @Test
  void testInvokeInternalServiceAction() {
    Map<String, Object> config =
        Map.of(
            "serviceBeanName", "testService",
            "methodName", "concat",
            "arguments", List.of("hello", "world"));

    Map<String, Object> output =
        actionExecutor.execute(ActionType.INVOKE_INTERNAL_SERVICE, config, tenantId.toString());

    assertThat(output).containsKey("result");
    assertThat(output.get("result")).isEqualTo("hello-world");
  }

  @Test
  void testValidationFailureMissingConfig() {
    Map<String, Object> invalidConfig = Map.of("level", "INFO");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          actionExecutor.execute(ActionType.LOG, invalidConfig, tenantId.toString());
        });

    List<ActionExecution> history = actionExecutionRepository.findAll();
    assertThat(history).hasSize(1);
    assertThat(history.get(0).getStatus()).isEqualTo("FAILURE");
    assertThat(history.get(0).getFailureReason())
        .contains("Missing required configuration parameter: message");
  }

  @Test
  void testSanitizesSecretsInHistory() {
    Map<String, Object> config =
        Map.of(
            "domain", "vault",
            "entity", "auth",
            "action", "access",
            "payload", Map.of("password", "super_secret_password", "apiKey", "xyz123_token"));

    actionExecutor.execute(ActionType.PUBLISH_INTERNAL_DOMAIN_EVENT, config, tenantId.toString());

    List<ActionExecution> history = actionExecutionRepository.findAll();
    assertThat(history).hasSize(1);
    String inputs = history.get(0).getInputs();
    assertThat(inputs).doesNotContain("super_secret_password");
    assertThat(inputs).doesNotContain("xyz123_token");
    assertThat(inputs).contains("******");
  }

  @Test
  void testConductorActivitiesExecutesAction() {
    WorkflowExecution execution =
        WorkflowExecution.builder().definitionId(UUID.randomUUID()).definitionVersion(1).build();
    execution.setTenantId(tenantId);
    workflowExecutionRepository.save(execution);

    Map<String, Object> config = Map.of("message", "Test step logging");
    conductorActivities.executeStep(
        execution.getId().toString(), tenantId.toString(), "log-step", "LOG", config);

    List<WorkflowStepExecution> steps =
        workflowStepExecutionRepository.findByExecutionIdOrderByStartedAtAsc(execution.getId());
    assertThat(steps).hasSize(1);
    assertThat(steps.get(0).getStepName()).isEqualTo("log-step");
    assertThat(steps.get(0).getStepType()).isEqualTo(ActionType.LOG);
    assertThat(steps.get(0).getStatus())
        .isEqualTo(com.conductor.shared.workflow.WorkflowStatus.COMPLETED);
  }
}
