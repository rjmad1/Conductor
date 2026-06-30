package com.conductor.events.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "replay_audit_logs")
@Getter
@Setter
public class ReplayAuditLog extends TenantAwareEntity {

  @Id private UUID id = UUID.randomUUID();

  private String username;
  private String stream;
  private String consumer;
  private String replayType;
  private String startValue;
  private Instant requestedAt = Instant.now();
}
