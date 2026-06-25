package com.conductor.customer.domain;

import com.conductor.shared.customer.ContactType;
import com.conductor.shared.customer.PiiEncryptedConverter;
import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * A contact channel for a customer (phone, email, WhatsApp, address, social, custom).
 *
 * <p>Multiple contacts per customer are supported. Only one contact per type may be marked
 * isPrimary = true.
 *
 * <p>contact_value is encrypted at rest (Tier-1 PII). value_hash is a SHA-256 hash of the
 * normalized contact value, used for identity resolution lookups without decrypting the stored PII.
 */
@Entity
@Table(
    name = "customer_contacts",
    indexes = {
      @Index(name = "idx_contacts_customer_id", columnList = "tenant_id, customer_id"),
      @Index(
          name = "idx_contacts_type_primary",
          columnList = "tenant_id, contact_type, is_primary"),
      @Index(name = "idx_contacts_value_hash", columnList = "tenant_id, contact_type, value_hash")
    })
@Getter
@Setter
public class CustomerContact extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "contact_type", nullable = false)
  private ContactType type;

  /** Encrypted PII — actual contact value (e.g. +919876543210 or user@example.com). */
  @Convert(converter = PiiEncryptedConverter.class)
  @Column(name = "contact_value", nullable = false)
  private String value;

  /**
   * SHA-256 hash of the normalized contact value. Used for O(1) identity resolution without
   * touching the encrypted PII. Stored in plain text — hash is not reversible.
   */
  @Column(name = "value_hash", nullable = false, length = 64)
  private String valueHash;

  /** Human-readable label (e.g. "Work", "Personal", "WhatsApp"). */
  @Column(name = "label")
  private String label;

  @Column(name = "is_primary", nullable = false)
  private boolean isPrimary = false;

  @Column(name = "is_verified", nullable = false)
  private boolean isVerified = false;

  @Column(name = "verified_at")
  private Instant verifiedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
