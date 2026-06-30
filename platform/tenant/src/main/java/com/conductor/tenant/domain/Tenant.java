package com.conductor.tenant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
public class Tenant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "tenant_key", nullable = false, unique = true, updatable = false)
  private String tenantKey;

  @Column(name = "name", nullable = false)
  private String displayName;

  @Column(name = "legal_name")
  private String legalName;

  @Column(name = "domain", nullable = false, unique = true)
  private String domain;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TenantStatus status = TenantStatus.ACTIVE;

  @Column(name = "timezone", nullable = false)
  private String timezone = "UTC";

  @Column(name = "locale", nullable = false)
  private String locale = "en_US";

  @Column(name = "default_currency", nullable = false)
  private String defaultCurrency = "USD";

  @Column(name = "subscription_status", nullable = false)
  private String subscriptionStatus = "ACTIVE";

  @Column(name = "subscription_tier", nullable = false)
  private String subscriptionTier = "STANDARD";

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(name = "settings", columnDefinition = "jsonb")
  private String settings;

  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at")
  private Instant deletedAt;
}
