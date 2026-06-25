package com.conductor.integrations.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credentials")
@Getter
@Setter
public class Credential extends TenantAwareEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "integration_id", nullable = false)
  private Integration integration;

  @Column(name = "auth_type", nullable = false)
  private String authType;

  @Column(name = "encrypted_payload", nullable = false, columnDefinition = "text")
  private String encryptedPayload;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
