package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Membership record linking a Customer to a Segment.
 * Source indicates whether the customer was added manually or by the rule engine.
 */
@Entity
@Table(name = "customer_segments", indexes = {
    @Index(name = "idx_customer_segments_customer", columnList = "tenant_id, customer_id"),
    @Index(name = "idx_customer_segments_segment",  columnList = "tenant_id, segment_id"),
    @Index(name = "idx_customer_segments_unique",   columnList = "tenant_id, customer_id, segment_id", unique = true)
})
@Getter
@Setter
public class CustomerSegment extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "segment_id", nullable = false)
    private UUID segmentId;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt = Instant.now();

    @Column(name = "added_by")
    private String addedBy;

    /** MANUAL or RULE_ENGINE. */
    @Column(name = "source", nullable = false)
    private String source;
}
