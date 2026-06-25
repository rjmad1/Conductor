package com.conductor.analytics.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * KPI definition with threshold configuration for alerting. Tenant-scoped via TenantAwareEntity.
 */
@Entity
@Table(name = "analytics_kpi_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDefinition extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private String metricName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MetricAggregation aggregation;

  private Double thresholdWarning;

  private Double thresholdCritical;

  private String owner;

  @Column(nullable = false)
  private String status; // ACTIVE, DISABLED

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  private Instant updatedAt;

  @PrePersist
  void onPrePersist() {
    this.createdAt = Instant.now();
    if (this.status == null) {
      this.status = "ACTIVE";
    }
  }

  @PreUpdate
  void onPreUpdate() {
    this.updatedAt = Instant.now();
  }
}
