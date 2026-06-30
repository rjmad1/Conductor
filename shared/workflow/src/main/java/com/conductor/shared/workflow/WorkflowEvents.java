package com.conductor.shared.workflow;

/**
 * Constants for workflow domain event subject suffixes. Full subject format:
 * conductor.{tenantId}.workflow.{entity}.{action} Compliant with EVENT_GOVERNANCE.md naming
 * convention.
 */
public final class WorkflowEvents {

  private WorkflowEvents() {}

  // Domain
  public static final String DOMAIN = "workflow";

  // Definition events
  public static final String ENTITY_DEFINITION = "definition";
  public static final String ACTION_CREATED = "created";
  public static final String ACTION_UPDATED = "updated";
  public static final String ACTION_PUBLISHED = "published";

  // Execution events
  public static final String ENTITY_EXECUTION = "execution";
  public static final String ACTION_STARTED = "started";
  public static final String ACTION_COMPLETED = "completed";
  public static final String ACTION_FAILED = "failed";
  public static final String ACTION_RETRIED = "retried";
  public static final String ACTION_COMPENSATED = "compensated";
  public static final String ACTION_CANCELLED = "cancelled";

  // Schema version
  public static final String SCHEMA_VERSION = "1.0.0";
}
