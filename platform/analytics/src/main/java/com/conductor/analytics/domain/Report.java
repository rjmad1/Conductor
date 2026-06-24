package com.conductor.analytics.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Report definition entity. Supports scheduled and ad-hoc report execution.
 * Tenant-scoped via TenantAwareEntity.
 */
@Entity
@Table(name = "analytics_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String reportType; // SCHEDULED, ADHOC

    @Column(columnDefinition = "TEXT", nullable = false)
    private String queryDefinition;

    @Column(nullable = false)
    private String outputFormat; // CSV, EXCEL, PDF, JSON

    private String scheduleCron;

    @Column(nullable = false)
    private String status; // ACTIVE, ARCHIVED

    private Instant lastRunAt;

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
