package com.conductor.workflow.api.dto;

import com.conductor.shared.workflow.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** Request payload for creating or updating a workflow definition. */
@Data
public class WorkflowDefinitionRequest {

  @NotBlank private String name;

  private String description;

  @NotNull private TriggerType triggerType;

  private Map<String, Object> triggerConfig;

  @NotNull private List<Map<String, Object>> steps;

  private Map<String, Object> variables;
}
