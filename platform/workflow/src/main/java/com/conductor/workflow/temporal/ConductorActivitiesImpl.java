package com.conductor.workflow.temporal;

import com.conductor.shared.workflow.ActionType;
import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowStepExecution;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowStepExecutionRepository;
import com.conductor.workflow.service.ActionExecutor;
import com.conductor.workflow.service.CompensationService;
import com.conductor.workflow.service.WorkflowStateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporal activity implementations. Each method has access to Spring-managed beans and performs
 * side effects (DB reads/writes, event publishing, compensation) on behalf of the workflow.
 */
@Component
@ActivityImpl(taskQueues = WorkflowWorkerConfig.SYSTEM_TASK_QUEUE)
public class ConductorActivitiesImpl implements ConductorActivities {

  private static final Logger log = LoggerFactory.getLogger(ConductorActivitiesImpl.class);

  private final WorkflowDefinitionRepository definitionRepository;
  private final WorkflowExecutionRepository executionRepository;
  private final WorkflowStepExecutionRepository stepRepository;
  private final WorkflowStateService stateService;
  private final ActionExecutor actionExecutor;
  private final CompensationService compensationService;
  private final ObjectMapper objectMapper;

  public ConductorActivitiesImpl(
      WorkflowDefinitionRepository definitionRepository,
      WorkflowExecutionRepository executionRepository,
      WorkflowStepExecutionRepository stepRepository,
      WorkflowStateService stateService,
      ActionExecutor actionExecutor,
      CompensationService compensationService) {
    this.definitionRepository = definitionRepository;
    this.executionRepository = executionRepository;
    this.stepRepository = stepRepository;
    this.stateService = stateService;
    this.actionExecutor = actionExecutor;
    this.compensationService = compensationService;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public List<Map<String, Object>> loadWorkflowSteps(String definitionId, String tenantId) {
    try {
      UUID tenantUuid = UUID.fromString(tenantId);
      UUID defUuid = UUID.fromString(definitionId);

      return definitionRepository
          .findByIdAndTenantId(defUuid, tenantUuid)
          .map(this::parseSteps)
          .orElseGet(
              () -> {
                log.warn("Definition not found: {} for tenant {}", definitionId, tenantId);
                return Collections.emptyList();
              });
    } catch (Exception e) {
      log.error(
          "Failed to load workflow steps for definition {}: {}", definitionId, e.getMessage());
      throw Activity.wrap(e);
    }
  }

  @Override
  public Map<String, Object> executeStep(
      String executionId,
      String tenantId,
      String stepName,
      String actionType,
      Map<String, Object> config) {
    UUID executionUuid = UUID.fromString(executionId);

    // Record step start
    WorkflowStepExecution step =
        WorkflowStepExecution.builder()
            .executionId(executionUuid)
            .stepName(stepName)
            .stepType(ActionType.valueOf(actionType))
            .status(WorkflowStatus.RUNNING)
            .input(serializeJson(config))
            .startedAt(Instant.now())
            .build();
    stepRepository.save(step);

    try {
      Map<String, Object> result =
          actionExecutor.execute(ActionType.valueOf(actionType), config, tenantId, executionUuid);

      // Record step completion
      step.setStatus(WorkflowStatus.COMPLETED);
      step.setOutput(serializeJson(result));
      step.setCompletedAt(Instant.now());
      stepRepository.save(step);

      log.info("Step '{}' completed for execution {}", stepName, executionId);
      return result;

    } catch (Exception e) {
      step.setStatus(WorkflowStatus.FAILED);
      step.setFailureReason(e.getMessage());
      step.setCompletedAt(Instant.now());
      stepRepository.save(step);

      log.error("Step '{}' failed for execution {}: {}", stepName, executionId, e.getMessage());
      throw Activity.wrap(e);
    }
  }

  @Override
  public void markExecutionCompleted(String executionId, String outputJson) {
    WorkflowExecution execution = loadExecution(executionId);
    execution.setOutput(outputJson);
    stateService.transition(execution, WorkflowStatus.COMPLETED, null, "temporal");
    log.info("Execution {} marked COMPLETED", executionId);
  }

  @Override
  public void markExecutionFailed(String executionId, String reason) {
    WorkflowExecution execution = loadExecution(executionId);
    stateService.transition(execution, WorkflowStatus.FAILED, reason, "temporal");
    log.info("Execution {} marked FAILED: {}", executionId, reason);
  }

  @Override
  public void compensateExecution(String executionId, String tenantId) {
    compensationService.compensate(UUID.fromString(executionId), Map.of(), tenantId);
    log.info("Compensation executed for execution {}", executionId);
  }

  @Override
  public void markExecutionCompensated(String executionId) {
    WorkflowExecution execution = loadExecution(executionId);
    stateService.transition(execution, WorkflowStatus.COMPENSATED, null, "temporal");
    log.info("Execution {} marked COMPENSATED", executionId);
  }

  // --- Private helpers ---

  private WorkflowExecution loadExecution(String executionId) {
    return executionRepository
        .findById(UUID.fromString(executionId))
        .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));
  }

  private List<Map<String, Object>> parseSteps(WorkflowDefinition definition) {
    try {
      String stepsStr = definition.getSteps();
      if (stepsStr != null && stepsStr.trim().startsWith("\"") && stepsStr.trim().endsWith("\"")) {
        try {
          stepsStr = objectMapper.readValue(stepsStr, String.class);
        } catch (Exception e) {
          // ignore and fallback to direct parsing
        }
      }
      return objectMapper.readValue(stepsStr, new TypeReference<List<Map<String, Object>>>() {});
    } catch (JsonProcessingException e) {
      log.error("Failed to parse steps for definition {}: {}", definition.getId(), e.getMessage());
      return Collections.emptyList();
    }
  }

  private String serializeJson(Object data) {
    if (data == null) return "{}";
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
