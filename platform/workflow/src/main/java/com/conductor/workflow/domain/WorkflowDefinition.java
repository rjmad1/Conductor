package com.conductor.workflow.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
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
 * Persistent workflow definition entity. Contains the JSON DSL definition, trigger configuration,
 * and version lifecycle state. Extends TenantAwareEntity for automatic row-level tenant isolation.
 */
@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "trigger_type", nullable = false, length = 50)
  private TriggerType triggerType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "trigger_config", columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  private String triggerConfig = "{}";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  private String steps = "[]";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  private String variables = "{}";

  @Enumerated(EnumType.STRING)
  @Column(name = "version_status", nullable = false, length = 20)
  @Builder.Default
  private WorkflowVersionStatus versionStatus = WorkflowVersionStatus.DRAFT;

  @Column(nullable = false)
  @Builder.Default
  private int version = 1;

  @Column(name = "parent_definition_id")
  private UUID parentDefinitionId;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private Instant updatedAt = Instant.now();
}
