package com.conductor.workflow.api.dto;

import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Request payload for creating or updating a workflow definition.
 */
@Data
public class WorkflowDefinitionRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private TriggerType triggerType;

    private Map<String, Object> triggerConfig;

    @NotNull
    private List<Map<String, Object>> steps;

    private Map<String, Object> variables;
}
