package com.conductor.shared.execution.action;

/** Validates the action execution context and configuration values against action metadata. */
public interface ActionValidator {

  /**
   * Validates the given action context against the metadata. Throws exception if validation fails.
   */
  void validate(ActionContext context, ActionMetadata metadata)
      throws IllegalArgumentException, SecurityException;
}
