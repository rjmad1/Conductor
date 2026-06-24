package com.conductor.workflow.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;
import java.util.Map;

/**
 * Temporal activity interface for all workflow step actions.
 * Activities are the boundary between the deterministic Temporal workflow and
 * non-deterministic side effects (DB reads, event publishing, external calls).
 */
@ActivityInterface
public interface ConductorActivities {

    /**
     * Loads workflow step definitions from the database.
     * Invoked at workflow start — safe to retry.
     */
    @ActivityMethod
    List<Map<String, Object>> loadWorkflowSteps(String definitionId, String tenantId);

    /**
     * Executes a single workflow step action via the ActionExecutor.
     *
     * @param executionId the workflow execution ID
     * @param tenantId    the tenant context
     * @param stepName    the step name for tracking
     * @param actionType  the action type string (maps to ActionType enum)
     * @param config      step-specific configuration
     */
    @ActivityMethod
    Map<String, Object> executeStep(String executionId, String tenantId, String stepName,
                                     String actionType, Map<String, Object> config);

    /**
     * Marks a workflow execution as completed and stores the output.
     */
    @ActivityMethod
    void markExecutionCompleted(String executionId, String outputJson);

    /**
     * Marks a workflow execution as failed with a reason.
     */
    @ActivityMethod
    void markExecutionFailed(String executionId, String reason);

    /**
     * Triggers compensation for a failed execution.
     */
    @ActivityMethod
    void compensateExecution(String executionId, String tenantId);

    /**
     * Marks a workflow execution as compensated after successful compensation.
     */
    @ActivityMethod
    void markExecutionCompensated(String executionId);
}
