package com.conductor.workflow.service;

import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.domain.WorkflowHistory;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.conductor.workflow.repository.WorkflowHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Manages workflow execution state transitions with validation.
 * Enforces the state machine rules and records history for auditability.
 */
@Service
public class WorkflowStateService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStateService.class);

    /** Valid state transitions. Key = current state, Value = set of allowed next states. */
    private static final Map<WorkflowStatus, Set<WorkflowStatus>> VALID_TRANSITIONS = Map.of(
            WorkflowStatus.PENDING, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.CANCELLED),
            WorkflowStatus.RUNNING, Set.of(WorkflowStatus.WAITING, WorkflowStatus.PAUSED,
                    WorkflowStatus.COMPLETED, WorkflowStatus.FAILED, WorkflowStatus.CANCELLED),
            WorkflowStatus.WAITING, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.CANCELLED,
                    WorkflowStatus.FAILED),
            WorkflowStatus.PAUSED, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.CANCELLED),
            WorkflowStatus.FAILED, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.COMPENSATED),
            WorkflowStatus.COMPLETED, Set.of(),
            WorkflowStatus.CANCELLED, Set.of(),
            WorkflowStatus.COMPENSATED, Set.of()
    );

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowHistoryRepository historyRepository;

    public WorkflowStateService(WorkflowExecutionRepository executionRepository,
                                WorkflowHistoryRepository historyRepository) {
        this.executionRepository = executionRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Transitions a workflow execution to a new state.
     *
     * @param execution the execution to transition
     * @param newStatus the target status
     * @param reason    optional reason for the transition
     * @param actorId   the user or system actor performing the transition
     * @return the updated execution
     * @throws IllegalStateException if the transition is not valid
     */
    @Transactional
    public WorkflowExecution transition(WorkflowExecution execution, WorkflowStatus newStatus,
                                        String reason, String actorId) {
        WorkflowStatus currentStatus = execution.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s for execution %s",
                            currentStatus, newStatus, execution.getId()));
        }

        WorkflowStatus previousStatus = execution.getStatus();
        execution.setStatus(newStatus);

        if (newStatus == WorkflowStatus.RUNNING && execution.getStartedAt() == null) {
            execution.setStartedAt(Instant.now());
        }
        if (newStatus == WorkflowStatus.COMPLETED || newStatus == WorkflowStatus.FAILED
                || newStatus == WorkflowStatus.CANCELLED || newStatus == WorkflowStatus.COMPENSATED) {
            execution.setCompletedAt(Instant.now());
        }
        if (newStatus == WorkflowStatus.FAILED && reason != null) {
            execution.setFailureReason(reason);
        }
        if (newStatus == WorkflowStatus.COMPENSATED) {
            execution.setCompensated(true);
        }

        executionRepository.save(execution);

        // Record history entry for auditability
        WorkflowHistory history = WorkflowHistory.builder()
                .executionId(execution.getId())
                .eventType("STATE_TRANSITION")
                .details(String.format("{\"from\":\"%s\",\"to\":\"%s\",\"reason\":\"%s\"}",
                        previousStatus, newStatus, reason != null ? reason : ""))
                .actorId(actorId)
                .build();
        historyRepository.save(history);

        log.info("Workflow execution {} transitioned from {} to {} (actor={})",
                execution.getId(), previousStatus, newStatus, actorId);

        return execution;
    }

    /**
     * Checks whether a state transition is valid.
     */
    public boolean isValidTransition(WorkflowStatus from, WorkflowStatus to) {
        Set<WorkflowStatus> allowedTargets = VALID_TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }
}
