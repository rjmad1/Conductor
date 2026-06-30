package com.conductor.workflow.domain;

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
 * Immutable audit history entry for workflow execution events. Records state transitions, step
 * completions, failures, retries, and compensations.
 */
@Entity
@Table(name = "workflow_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "execution_id", nullable = false)
  private UUID executionId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String details;

  @Column(nullable = false)
  @Builder.Default
  private Instant timestamp = Instant.now();

  @Column(name = "actor_id")
  private String actorId;
}
