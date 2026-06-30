package com.conductor.shared.execution.action;

/**
 * Pluggable handler interface for executing workflow actions. Handlers receive context containing
 * parameters, tenant details, and execution variables, and return success/failure results.
 */
public interface ActionHandler {

  /** Returns the unique identifier/type of action handled (e.g. "LOG", "CREATE_AUDIT_RECORD"). */
  String getActionType();

  /** Executes the action step with the provided contextual data. */
  ActionResult execute(ActionContext context);

  /** Returns the metadata schema describing the requirements of this action handler. */
  ActionMetadata getMetadata();
}
