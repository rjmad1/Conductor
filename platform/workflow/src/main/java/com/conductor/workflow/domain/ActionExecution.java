package com.conductor.workflow.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Audit log entry for executing pluggable actions. Extends TenantAwareEntity for automatic
 * row-level security and filtering.
 */
@Entity
@Table(name = "action_executions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionExecution extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "action_type", nullable = false, length = 100)
  private String actionType;

  @Column(name = "workflow_execution_id")
  private UUID workflowExecutionId;

  @Column(name = "correlation_id")
  private String correlationId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String inputs;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String outputs;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

  @Column(name = "execution_duration_ms")
  private Long executionDurationMs;

  @Column(name = "started_at", nullable = false)
  @Builder.Default
  private Instant startedAt = Instant.now();

  @Column(name = "completed_at")
  private Instant completedAt;
}
