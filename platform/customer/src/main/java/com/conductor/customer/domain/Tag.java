package com.conductor.customer.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * A label that can be applied to customers. Tags are tenant-scoped and categorised. slug is a
 * URL-safe unique identifier within the tenant (e.g. "vip", "churn-risk").
 */
@Entity
@Table(
    name = "tags",
    indexes = {
      @Index(name = "idx_tags_tenant_slug", columnList = "tenant_id, slug", unique = true),
      @Index(name = "idx_tags_category", columnList = "tenant_id, category")
    })
@Getter
@Setter
public class Tag extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug", nullable = false)
  private String slug;

  @Column(name = "category")
  private String category;

  @Column(name = "color", length = 7)
  private String color;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
