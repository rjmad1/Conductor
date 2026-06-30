package com.conductor.workflow.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import com.conductor.shared.workflow.WorkflowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents a single execution instance of a workflow definition. Tracks Temporal execution IDs,
 * status, I/O data, and retry/compensation state.
 */
@Entity
@Table(name = "workflow_executions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "definition_id", nullable = false)
  private UUID definitionId;

  @Column(name = "definition_version", nullable = false)
  private int definitionVersion;

  @Column(name = "temporal_workflow_id")
  private String temporalWorkflowId;

  @Column(name = "temporal_run_id")
  private String temporalRunId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private WorkflowStatus status = WorkflowStatus.PENDING;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String input;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String output;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String variables;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

  @Column(name = "retry_count")
  @Builder.Default
  private int retryCount = 0;

  @Builder.Default private boolean compensated = false;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
