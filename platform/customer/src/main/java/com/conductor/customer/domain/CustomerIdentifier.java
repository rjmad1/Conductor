package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer identifier for identity resolution.
 *
 * <p>Stores hashed versions of external identifiers (email, phone, external CRM ID) for fast O(1)
 * duplicate detection and merge candidate lookup.
 *
 * <p>The identifier_value is the SHA-256 hash of the normalized raw value. Raw values are never
 * stored here — they live encrypted in CustomerContact.
 *
 * <p>Source system mapping enables tracking which external system contributed the identifier.
 */
@Entity
@Table(
    name = "customer_identifiers",
    indexes = {
      @Index(
          name = "idx_identifiers_lookup",
          columnList = "tenant_id, identifier_type, identifier_hash",
          unique = true),
      @Index(name = "idx_identifiers_customer_id", columnList = "tenant_id, customer_id")
    })
@Getter
@Setter
public class CustomerIdentifier extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  /** Type of identifier: EMAIL, PHONE, EXTERNAL_ID, SOURCE_ID. */
  @Column(name = "identifier_type", nullable = false)
  private String identifierType;

  /**
   * SHA-256 hash of normalized identifier value. For email: lower-cased. For phone: E.164
   * normalised.
   */
  @Column(name = "identifier_hash", nullable = false, length = 64)
  private String identifierHash;

  /** Source system that provided this identifier (e.g. ZOHO, SHOPIFY, MANUAL). */
  @Column(name = "source_system")
  private String sourceSystem;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
