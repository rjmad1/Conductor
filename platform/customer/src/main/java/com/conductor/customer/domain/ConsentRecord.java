package com.conductor.customer.domain;

import com.conductor.shared.customer.ConsentAction;
import com.conductor.shared.customer.ConsentType;
import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only consent ledger — DPDP Act §6 and GDPR compliance record.
 *
 * Each record represents a single consent event (GRANTED or REVOKED).
 * Records are NEVER updated or deleted. Immutability is enforced at two levels:
 *   1. Service layer: ConsentService only ever calls repository.save(newRecord)
 *   2. Database layer: PostgreSQL trigger blocks UPDATE/DELETE on consent_records table
 *
 * The "current" consent status is derived by reading the latest record per
 * (tenant_id, customer_id, consent_type) ordered by created_at DESC.
 *
 * consentVersion allows versioned policy updates (e.g. "privacy-policy-v2").
 */
@Entity
@Table(name = "consent_records", indexes = {
    @Index(name = "idx_consent_customer_type",    columnList = "tenant_id, customer_id, consent_type"),
    @Index(name = "idx_consent_customer_created", columnList = "tenant_id, customer_id, created_at")
})
@Getter
@Setter
public class ConsentRecord extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false)
    private ConsentType consentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ConsentAction action;

    /** Communication channel that consent applies to (e.g. WHATSAPP, EMAIL). */
    @Column(name = "channel")
    private String channel;

    /** Legal basis for consent: EXPLICIT, LEGITIMATE_INTEREST, CONTRACT. */
    @Column(name = "legal_basis")
    private String legalBasis;

    /** Version of the privacy policy / consent text (e.g. "v1", "privacy-policy-v2"). */
    @Column(name = "consent_version")
    private String consentVersion;

    /** IP address of the user who granted/revoked consent. Masked in logs. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    /** Additional context: campaign ID, workflow ID, form ID, etc. */
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
