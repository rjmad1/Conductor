package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Assignment record linking a Tag to a Customer.
 * Unique per (tenant_id, customer_id, tag_id).
 */
@Entity
@Table(name = "customer_tags", indexes = {
    @Index(name = "idx_customer_tags_customer", columnList = "tenant_id, customer_id"),
    @Index(name = "idx_customer_tags_tag",      columnList = "tenant_id, tag_id"),
    @Index(name = "idx_customer_tags_unique",   columnList = "tenant_id, customer_id, tag_id", unique = true)
})
@Getter
@Setter
public class CustomerTag extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "assigned_by")
    private String assignedBy;
}
