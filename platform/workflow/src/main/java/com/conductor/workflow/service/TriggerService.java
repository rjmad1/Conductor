package com.conductor.workflow.service;

import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Routes incoming triggers to workflow executions.
 * Supports event, API, webhook, schedule, timer, and manual triggers.
 */
@Service
public class TriggerService {

    private static final Logger log = LoggerFactory.getLogger(TriggerService.class);

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowExecutionService executionService;
    private final AuditLogger auditLogger;
    private final WorkflowMetrics metrics;

    public TriggerService(WorkflowDefinitionRepository definitionRepository,
                          WorkflowExecutionService executionService,
                          AuditLogger auditLogger,
                          WorkflowMetrics metrics) {
        this.definitionRepository = definitionRepository;
        this.executionService = executionService;
        this.auditLogger = auditLogger;
        this.metrics = metrics;
    }

    /**
     * Fires a trigger for the given tenant, trigger type, and input data.
     * Finds all published workflow definitions matching the trigger type and starts executions.
     */
    public void fireTrigger(UUID tenantId, TriggerType triggerType, Map<String, Object> triggerData) {
        List<WorkflowDefinition> definitions = definitionRepository
                .findByTenantIdAndVersionStatusOrderByCreatedAtDesc(
                        tenantId, WorkflowVersionStatus.PUBLISHED,
                        org.springframework.data.domain.PageRequest.of(0, 100));

        int triggered = 0;
        for (WorkflowDefinition definition : definitions) {
            if (definition.getTriggerType() == triggerType) {
                try {
                    executionService.startExecution(definition.getId(), triggerData);
                    triggered++;
                    log.info("Trigger fired: type={}, definition={}, tenant={}",
                            triggerType, definition.getId(), tenantId);
                } catch (Exception e) {
                    log.error("Failed to start execution for trigger: definition={}, error={}",
                            definition.getId(), e.getMessage());
                    auditLogger.logEvent("TRIGGER_FAILED", "workflow:" + definition.getId(),
                            "FAILURE", e.getMessage());
                }
            }
        }

        metrics.recordTriggerFired(triggerType.name());
        auditLogger.logEvent("TRIGGER_FIRED", "trigger:" + triggerType,
                "SUCCESS", "matched=" + triggered);
    }

    /**
     * Fires a webhook trigger for a specific definition.
     */
    public void fireWebhookTrigger(UUID definitionId, Map<String, Object> webhookPayload) {
        executionService.startExecution(definitionId, webhookPayload);
        metrics.recordTriggerFired(TriggerType.WEBHOOK.name());
    }

    /**
     * Fires a manual trigger for a specific definition.
     */
    public void fireManualTrigger(UUID definitionId, Map<String, Object> inputData) {
        executionService.startExecution(definitionId, inputData);
        metrics.recordTriggerFired(TriggerType.MANUAL.name());
    }
}
