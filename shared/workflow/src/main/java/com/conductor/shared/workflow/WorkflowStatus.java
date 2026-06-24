package com.conductor.shared.workflow;

/**
 * Represents the execution state of a workflow instance.
 * State transitions are enforced by WorkflowStateService.
 */
public enum WorkflowStatus {
    PENDING,
    RUNNING,
    WAITING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    COMPENSATED
}
