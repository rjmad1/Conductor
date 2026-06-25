package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transformations")
@Getter
@Setter
public class Transformation extends TenantAwareEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "integration_id", nullable = false)
  private Integration integration;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "version", nullable = false)
  private int version = 1;

  @Column(name = "mapping_rules", nullable = false, columnDefinition = "text")
  private String mappingRules;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
