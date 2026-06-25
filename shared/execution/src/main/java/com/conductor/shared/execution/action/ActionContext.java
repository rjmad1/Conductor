package com.conductor.shared.execution.action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * Execution context provided to an action handler. Carries tenant scope, authenticated identity,
 * workflow metadata, configuration parameters, execution variables, and correlation info.
 */
@Value
@Builder
public class ActionContext {
  UUID tenantId;
  Object principal;
  UUID workflowDefinitionId;
  UUID workflowExecutionId;
  @Builder.Default Map<String, Object> configuration = new HashMap<>();
  @Builder.Default Map<String, Object> variables = new HashMap<>();
  String correlationId;
}
