package com.conductor.workflow.temporal;

import com.conductor.shared.execution.ExecutionContext;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Temporal workflow interface for the universal Conductor workflow. All workflow definitions
 * execute via this single interface — the JSON DSL definition is resolved at runtime from the
 * ExecutionContext.
 */
@WorkflowInterface
public interface ConductorWorkflow {

  /**
   * Main workflow entry point. Executes the workflow definition referenced in the execution
   * context, step by step.
   *
   * @param context runtime context carrying tenant, definition ID, and variables
   */
  @WorkflowMethod
  void execute(ExecutionContext context);

  /**
   * Signal method to resume a WAIT step or a paused workflow.
   *
   * @param signalName the name of the signal to send
   */
  @SignalMethod
  void signal(String signalName);

  /**
   * Query method to read the current workflow status without interrupting execution.
   *
   * @return the current status string
   */
  @QueryMethod
  String getStatus();
}
