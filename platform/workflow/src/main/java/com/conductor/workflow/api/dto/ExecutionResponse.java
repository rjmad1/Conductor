package com.conductor.workflow.api.dto;

import com.conductor.shared.workflow.WorkflowStatus;
import com.conductor.workflow.domain.WorkflowExecution;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/** Response payload for a workflow execution instance. */
@Data
@Builder
public class ExecutionResponse {

  private UUID id;
  private UUID tenantId;
  private UUID definitionId;
  private int definitionVersion;
  private String temporalWorkflowId;
  private WorkflowStatus status;
  private Object input;
  private Object output;
  private Instant startedAt;
  private Instant completedAt;
  private String failureReason;
  private int retryCount;
  private boolean compensated;
  private String createdBy;
  private Instant createdAt;

  public static ExecutionResponse from(WorkflowExecution e) {
    return ExecutionResponse.builder()
        .id(e.getId())
        .tenantId(e.getTenantId())
        .definitionId(e.getDefinitionId())
        .definitionVersion(e.getDefinitionVersion())
        .temporalWorkflowId(e.getTemporalWorkflowId())
        .status(e.getStatus())
        .input(e.getInput())
        .output(e.getOutput())
        .startedAt(e.getStartedAt())
        .completedAt(e.getCompletedAt())
        .failureReason(e.getFailureReason())
        .retryCount(e.getRetryCount())
        .compensated(e.isCompensated())
        .createdBy(e.getCreatedBy())
        .createdAt(e.getCreatedAt())
        .build();
  }
}
