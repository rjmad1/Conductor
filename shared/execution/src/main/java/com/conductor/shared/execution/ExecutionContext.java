package com.conductor.shared.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable execution context passed through workflow steps.
 * Carries all runtime state needed by the Temporal workflow and activities.
 */
@Getter
@Builder
@ToString
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ExecutionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID workflowDefinitionId;
    private final UUID executionId;
    private final String tenantId;
    private final String userId;
    private final String correlationId;
    private final int definitionVersion;

    @Builder.Default
    private final Map<String, Object> variables = new HashMap<>();

    @Builder.Default
    private final Map<String, Object> triggerData = new HashMap<>();
}
