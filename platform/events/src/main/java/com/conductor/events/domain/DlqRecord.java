package com.conductor.events.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_records")
@Getter
@Setter
public class DlqRecord extends TenantAwareEntity {

    @Id
    private UUID id = UUID.randomUUID();

    private String stream;
    private String consumer;
    private String reason;
    private UUID originalEventId;
    private String payload;
    private Instant deadLetteredAt = Instant.now();
    private String status = "PENDING"; // PENDING, REPLAYED, DISCARDED
}
