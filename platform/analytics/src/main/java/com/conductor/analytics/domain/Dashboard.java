package com.conductor.analytics.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dashboard metadata entity. Stores layout configuration for embedded analytics views.
 * Tenant-scoped via TenantAwareEntity.
 */
@Entity
@Table(name = "analytics_dashboards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String layoutJson;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DashboardWidget> widgets = new ArrayList<>();

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
