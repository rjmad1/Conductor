package com.conductor.shared.workflow;

/**
 * Represents the lifecycle status of a workflow definition version. Controls whether a definition
 * can be executed or modified.
 */
public enum WorkflowVersionStatus {
  DRAFT,
  PUBLISHED,
  DEPRECATED,
  ARCHIVED
}
