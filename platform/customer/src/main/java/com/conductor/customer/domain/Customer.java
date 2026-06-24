package com.conductor.customer.domain;

import com.conductor.shared.customer.CustomerStatus;
import com.conductor.shared.customer.PiiEncryptedConverter;
import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Canonical customer record — the root aggregate of the Customer domain.
 *
 * PII fields (firstName, lastName) are encrypted at rest via PiiEncryptedConverter (SG-006).
 * displayName is a non-PII computed label for search/display (may contain first name only or alias).
 *
 * Extends TenantAwareEntity → tenant_id injected from TenantContext on @PrePersist.
 * Hibernate @Filter auto-scopes all queries to the active tenant.
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_customers_external_id",   columnList = "tenant_id, external_id"),
    @Index(name = "idx_customers_created_at",    columnList = "tenant_id, created_at")
})
@Getter
@Setter
public class Customer extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Optional identifier from source system (CRM, Shopify, etc.).
     * Unique per tenant + source_system combination.
     */
    @Column(name = "external_id")
    private String externalId;

    @Column(name = "source_system")
    private String sourceSystem;

    /** Encrypted PII — first name stored via AES-256-GCM. */
    @Convert(converter = PiiEncryptedConverter.class)
    @Column(name = "first_name")
    private String firstName;

    /** Encrypted PII — last name stored via AES-256-GCM. */
    @Convert(converter = PiiEncryptedConverter.class)
    @Column(name = "last_name")
    private String lastName;

    /**
     * Non-sensitive display label for search results and UI.
     * Typically "First L." or a business alias. Not encrypted.
     */
    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    /**
     * When status = MERGED, this references the surviving customer record.
     * The merged-from customer is soft-deleted (status = MERGED).
     */
    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    /**
     * Flexible JSONB bag for custom attributes not covered by first-class columns.
     * Searchable via JSONB path queries.
     */
    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
