package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Flexible key-value attribute store for a customer.
 *
 * <p>Extends the Customer model without schema changes. The value field is JSONB to support
 * numbers, booleans, arrays, and nested objects. dataType is a hint for UI rendering and validation
 * (STRING, NUMBER, BOOLEAN, DATE, JSON).
 */
@Entity
@Table(
    name = "customer_attributes",
    indexes = {
      @Index(
          name = "idx_attributes_customer_key",
          columnList = "tenant_id, customer_id, attribute_key",
          unique = true),
      @Index(name = "idx_attributes_key_all", columnList = "tenant_id, attribute_key")
    })
@Getter
@Setter
public class CustomerAttribute extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "attribute_key", nullable = false)
  private String key;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(name = "attribute_value", columnDefinition = "jsonb")
  private String value;

  /** Type hint: STRING, NUMBER, BOOLEAN, DATE, JSON. */
  @Column(name = "data_type")
  private String dataType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
