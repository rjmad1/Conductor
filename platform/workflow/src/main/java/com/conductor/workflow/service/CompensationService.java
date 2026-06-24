package com.conductor.workflow.service;

import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.workflow.ActionType;
import com.conductor.workflow.domain.WorkflowHistory;
import com.conductor.workflow.domain.WorkflowStepExecution;
import com.conductor.workflow.repository.WorkflowHistoryRepository;
import com.conductor.workflow.repository.WorkflowStepExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Executes compensating actions in reverse order when a workflow fails.
 * Each completed step's compensation action is recorded in history for auditability.
 */
@Service
public class CompensationService {

    private static final Logger log = LoggerFactory.getLogger(CompensationService.class);

    private final WorkflowStepExecutionRepository stepRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final ActionExecutor actionExecutor;
    private final AuditLogger auditLogger;
    private final WorkflowMetrics metrics;

    public CompensationService(WorkflowStepExecutionRepository stepRepository,
                                WorkflowHistoryRepository historyRepository,
                                ActionExecutor actionExecutor,
                                AuditLogger auditLogger,
                                WorkflowMetrics metrics) {
        this.stepRepository = stepRepository;
        this.historyRepository = historyRepository;
        this.actionExecutor = actionExecutor;
        this.auditLogger = auditLogger;
        this.metrics = metrics;
    }

    /**
     * Executes compensation for a failed workflow by reversing completed steps.
     *
     * @param executionId        the failed execution
     * @param compensationConfig per-step compensation configurations (stepName -> config map)
     * @param tenantId           the tenant context
     */
    @Transactional
    public void compensate(UUID executionId, Map<String, Map<String, Object>> compensationConfig, String tenantId) {
        log.info("Starting compensation for execution {} (tenant={})", executionId, tenantId);

        List<WorkflowStepExecution> completedSteps = stepRepository.findByExecutionIdOrderByStartedAtAsc(executionId);

        // Reverse order for compensation
        Collections.reverse(completedSteps);

        int compensated = 0;
        for (WorkflowStepExecution step : completedSteps) {
            if (step.getStatus() != com.conductor.shared.workflow.WorkflowStatus.COMPLETED) {
                continue;
            }

            Map<String, Object> config = compensationConfig.getOrDefault(step.getStepName(), Map.of());
            if (config.isEmpty()) {
                log.debug("No compensation config for step {}, skipping", step.getStepName());
                continue;
            }

            try {
                String compensationAction = (String) config.getOrDefault("action", "SEND_EVENT");
                Map<String, Object> compensationParams = (Map<String, Object>) config.getOrDefault("config", Map.of());

                actionExecutor.execute(ActionType.valueOf(compensationAction), compensationParams, tenantId);

                // Record compensation history
                WorkflowHistory history = WorkflowHistory.builder()
                        .executionId(executionId)
                        .eventType("STEP_COMPENSATED")
                        .details(String.format("{\"step\":\"%s\",\"action\":\"%s\"}",
                                step.getStepName(), compensationAction))
                        .actorId("compensation-service")
                        .build();
                historyRepository.save(history);

                compensated++;
                log.info("Compensated step {} for execution {}", step.getStepName(), executionId);
            } catch (Exception e) {
                log.error("Compensation failed for step {} in execution {}: {}",
                        step.getStepName(), executionId, e.getMessage());
                auditLogger.logEvent("COMPENSATION_FAILED", "execution:" + executionId,
                        "FAILURE", "step=" + step.getStepName() + " error=" + e.getMessage());
            }
        }

        metrics.recordCompensation();
        auditLogger.logEvent("COMPENSATION_COMPLETED", "execution:" + executionId,
                "SUCCESS", "steps_compensated=" + compensated);

        log.info("Compensation complete for execution {}: {} steps compensated", executionId, compensated);
    }
}
