package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sync_jobs")
@Getter
@Setter
public class SyncJob extends TenantAwareEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "integration_id", nullable = false)
  private Integration integration;

  @Column(name = "sync_type", nullable = false)
  private String syncType;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "progress", columnDefinition = "text")
  private String progress;
}
