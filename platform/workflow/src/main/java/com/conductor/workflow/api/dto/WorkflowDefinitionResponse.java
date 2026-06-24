package com.conductor.workflow.api.dto;

import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for a workflow definition.
 */
@Data
@Builder
public class WorkflowDefinitionResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private TriggerType triggerType;
    private Object triggerConfig;
    private Object steps;
    private Object variables;
    private WorkflowVersionStatus versionStatus;
    private int version;
    private UUID parentDefinitionId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public static WorkflowDefinitionResponse from(WorkflowDefinition d) {
        return WorkflowDefinitionResponse.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .name(d.getName())
                .description(d.getDescription())
                .triggerType(d.getTriggerType())
                .triggerConfig(d.getTriggerConfig())
                .steps(d.getSteps())
                .variables(d.getVariables())
                .versionStatus(d.getVersionStatus())
                .version(d.getVersion())
                .parentDefinitionId(d.getParentDefinitionId())
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
