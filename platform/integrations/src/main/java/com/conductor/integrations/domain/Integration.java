package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "integrations")
@Getter
@Setter
public class Integration extends TenantAwareEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "connector_id", nullable = false)
  private Connector connector;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Column(name = "config", columnDefinition = "text")
  private String config;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
