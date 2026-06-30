package com.conductor.shared.templates;

import com.conductor.shared.workflow.TriggerType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Defines a reusable workflow template that tenants can instantiate. Templates are loaded from
 * classpath JSON resources.
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class WorkflowTemplateDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String templateId;
  private final String name;
  private final String description;
  private final String category;
  private final String version;
  private final TriggerType triggerType;
  private final Map<String, Object> triggerConfig;
  private final List<Map<String, Object>> steps;
  private final Map<String, Object> variables;
}
