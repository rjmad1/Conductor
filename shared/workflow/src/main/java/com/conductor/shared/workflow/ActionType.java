package com.conductor.shared.workflow;

/** Supported workflow step action types. Each type maps to a Temporal activity implementation. */
public enum ActionType {
  SEND_EVENT,
  INVOKE_INTEGRATION,
  CREATE_RECORD,
  UPDATE_RECORD,
  ASSIGN_USER,
  GENERATE_NOTIFICATION,
  INVOKE_WORKFLOW,
  DELAY,
  WAIT,
  TERMINATE
}
