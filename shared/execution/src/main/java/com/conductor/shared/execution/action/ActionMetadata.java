package com.conductor.shared.execution.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Declares the schema and capabilities of a workflow action. Helps drive REST metadata discovery,
 * dynamic forms generation, and pre-execution validation checks.
 */
@Value
@Builder
public class ActionMetadata {
  String actionType;
  String name;
  String description;
  @Builder.Default List<String> requiredConfigurationKeys = new ArrayList<>();

  @Builder.Default
  Map<String, String> supportedParameters = new HashMap<>(); // paramName -> description

  @Builder.Default List<String> requiredPermissions = new ArrayList<>();
}
