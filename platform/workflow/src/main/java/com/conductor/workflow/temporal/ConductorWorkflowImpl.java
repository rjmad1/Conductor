package com.conductor.workflow.temporal;

import com.conductor.shared.execution.ExecutionContext;
import com.conductor.shared.execution.RetryPolicy;
import com.conductor.shared.rules.Condition;
import com.conductor.shared.rules.ConditionEvaluator;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * Temporal workflow implementation. Loads the workflow definition steps from the ExecutionContext,
 * evaluates conditions via ConditionEvaluator, and dispatches each step to ConductorActivities.
 *
 * <p>Design notes: - Only uses Temporal-safe APIs (Workflow.* for time, logging, signals). - No
 * Spring beans injected — activities handle all side effects. - Compensation is triggered on step
 * failure when onFailure=COMPENSATE is set.
 */
public class ConductorWorkflowImpl implements ConductorWorkflow {

  /** Temporal-safe logger — does NOT use SLF4J directly. */
  private static final Logger log = Workflow.getLogger(ConductorWorkflowImpl.class);

  private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

  private String currentStatus = "RUNNING";
  private String receivedSignal = null;

  @Override
  public void execute(ExecutionContext context) {
    log.info(
        "Workflow execution started: definitionId={}, executionId={}, tenant={}",
        context.getWorkflowDefinitionId(),
        context.getExecutionId(),
        context.getTenantId());

    // Resolve activity stub — activity options use RetryOptions from context or defaults
    ActivityOptions options = buildActivityOptions();
    ConductorActivities activities = Workflow.newActivityStub(ConductorActivities.class, options);

    // Load workflow steps from the activities layer (reads from DB safely via activity)
    List<Map<String, Object>> steps =
        activities.loadWorkflowSteps(
            context.getWorkflowDefinitionId().toString(), context.getTenantId());

    if (steps == null || steps.isEmpty()) {
      log.warn("No steps found for definition {}", context.getWorkflowDefinitionId());
      activities.markExecutionCompleted(context.getExecutionId().toString(), "{}");
      return;
    }

    boolean compensate = false;
    String failedStep = null;

    // Execute steps sequentially
    for (Map<String, Object> step : steps) {
      String stepName = (String) step.getOrDefault("name", "unknown");

      // Evaluate step condition
      Condition condition = parseCondition(step);
      if (condition != null && !conditionEvaluator.evaluate(condition, context)) {
        log.info("Skipping step '{}': condition not met", stepName);
        continue;
      }

      // Handle WAIT step — block until signal received
      String stepType = (String) step.getOrDefault("type", "");
      if ("WAIT".equalsIgnoreCase(stepType)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) step.getOrDefault("config", Map.of());
        String signal = (String) config.getOrDefault("signal", "continue");
        log.info("Step '{}': waiting for signal '{}'", stepName, signal);
        Workflow.await(() -> receivedSignal != null && receivedSignal.equals(signal));
        receivedSignal = null;
        continue;
      }

      // Handle DELAY step
      if ("DELAY".equalsIgnoreCase(stepType)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) step.getOrDefault("config", Map.of());
        String duration = (String) config.getOrDefault("duration", "PT1M");
        log.info("Step '{}': delaying for {}", stepName, duration);
        Workflow.sleep(Duration.parse(duration));
        continue;
      }

      // Execute the activity
      try {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) step.getOrDefault("config", Map.of());
        activities.executeStep(
            context.getExecutionId().toString(), context.getTenantId(), stepName, stepType, config);

        log.info("Step '{}' completed successfully", stepName);

      } catch (Exception e) {
        log.error("Step '{}' failed: {}", stepName, e.getMessage());
        failedStep = stepName;

        // Determine failure handling
        @SuppressWarnings("unchecked")
        Map<String, Object> onFailure =
            (Map<String, Object>) step.getOrDefault("onFailure", Map.of());
        String failureAction = (String) onFailure.getOrDefault("action", "FAIL");

        if ("COMPENSATE".equalsIgnoreCase(failureAction)) {
          compensate = true;
        }
        // Stop sequential execution on failure
        break;
      }
    }

    // Handle compensation or completion
    if (failedStep != null) {
      if (compensate) {
        activities.compensateExecution(context.getExecutionId().toString(), context.getTenantId());
        activities.markExecutionCompensated(context.getExecutionId().toString());
      } else {
        activities.markExecutionFailed(
            context.getExecutionId().toString(), "Step failed: " + failedStep);
      }
    } else {
      activities.markExecutionCompleted(context.getExecutionId().toString(), "{}");
    }
  }

  @Override
  public void signal(String signalName) {
    log.info("Signal received: {}", signalName);
    this.receivedSignal = signalName;
  }

  @Override
  public String getStatus() {
    return currentStatus;
  }

  private ActivityOptions buildActivityOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setMaximumAttempts(RetryPolicy.DEFAULT_MAX_ATTEMPTS)
                .setInitialInterval(
                    Duration.ofSeconds(RetryPolicy.DEFAULT_INITIAL_INTERVAL_SECONDS))
                .setBackoffCoefficient(RetryPolicy.DEFAULT_BACKOFF_COEFFICIENT)
                .setMaximumInterval(Duration.ofSeconds(RetryPolicy.DEFAULT_MAX_INTERVAL_SECONDS))
                .build())
        .build();
  }

  /**
   * Parses a condition from the step config map. Returns null if no condition is specified (step
   * always executes).
   */
  @SuppressWarnings("unchecked")
  private Condition parseCondition(Map<String, Object> step) {
    Object conditionObj = step.get("condition");
    if (conditionObj == null) {
      return null;
    }
    if (conditionObj instanceof Map) {
      Map<String, Object> condMap = (Map<String, Object>) conditionObj;
      String field = (String) condMap.get("field");
      String operatorStr = (String) condMap.get("operator");
      Object value = condMap.get("value");
      if (field == null || operatorStr == null) {
        return null;
      }
      try {
        com.conductor.shared.rules.Operator op =
            com.conductor.shared.rules.Operator.valueOf(operatorStr);
        return Condition.builder().field(field).operator(op).value(value).build();
      } catch (IllegalArgumentException e) {
        log.warn("Unknown condition operator: {}", operatorStr);
        return null;
      }
    }
    return null;
  }
}
