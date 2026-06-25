package com.conductor.workflow.domain;

import com.conductor.shared.workflow.ActionType;
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

/** Tracks the execution state of individual steps within a workflow execution. */
@Entity
@Table(name = "workflow_step_executions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepExecution {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "execution_id", nullable = false)
  private UUID executionId;

  @Column(name = "step_name", nullable = false)
  private String stepName;

  @Enumerated(EnumType.STRING)
  @Column(name = "step_type", nullable = false, length = 50)
  private ActionType stepType;

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

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

  @Column(name = "retry_count")
  @Builder.Default
  private int retryCount = 0;
}
