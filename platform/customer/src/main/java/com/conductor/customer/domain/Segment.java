package com.conductor.customer.domain;

import com.conductor.shared.customer.SegmentType;
import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * A named customer group — static membership list or dynamically computed set.
 *
 * <p>rules (JSONB) is the rule DSL evaluated by SegmentService for DYNAMIC/RULE_BASED segments.
 * Structure: {"conditions": [{"field":"status","operator":"EQ","value":"ACTIVE"}], "logic":"AND"}
 *
 * <p>customerCount is a cached/denormalised count refreshed by SegmentService. TAG_BASED segments
 * derive membership from tag assignment (no rules JSONB needed).
 */
@Entity
@Table(
    name = "segments",
    indexes = {
      @Index(name = "idx_segments_tenant_slug", columnList = "tenant_id, slug", unique = true),
      @Index(name = "idx_segments_type", columnList = "tenant_id, segment_type")
    })
@Getter
@Setter
public class Segment extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug", nullable = false)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(name = "segment_type", nullable = false)
  private SegmentType type;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(name = "rules", columnDefinition = "jsonb")
  private String rules;

  @Column(name = "description")
  private String description;

  /** Cached count — refreshed by SegmentService.recompute(). Not real-time. */
  @Column(name = "customer_count", nullable = false)
  private long customerCount = 0;

  @Column(name = "last_computed_at")
  private Instant lastComputedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
