package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Directed relationship between two customers in the same tenant. Examples: HOUSEHOLD_MEMBER,
 * REFERRED_BY, ACCOUNT_MANAGER_OF.
 *
 * <p>Relationships are directional (from → to) but queries must traverse both directions.
 */
@Entity
@Table(
    name = "customer_relationships",
    indexes = {
      @Index(name = "idx_relationships_from", columnList = "tenant_id, from_customer_id"),
      @Index(name = "idx_relationships_to", columnList = "tenant_id, to_customer_id")
    })
@Getter
@Setter
public class CustomerRelationship extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "from_customer_id", nullable = false)
  private UUID fromCustomerId;

  @Column(name = "to_customer_id", nullable = false)
  private UUID toCustomerId;

  /** Relationship type label: HOUSEHOLD_MEMBER, REFERRED_BY, ACCOUNT_MANAGER_OF, etc. */
  @Column(name = "relationship_type", nullable = false)
  private String relationshipType;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
