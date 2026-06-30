package com.conductor.shared.execution.action;

import lombok.Builder;
import lombok.Value;

/** Wrapper representing a registered action definition in the execution engine. */
@Value
@Builder
public class ActionDefinition {
  String actionType;
  String name;
  String description;
  ActionMetadata metadata;
}
