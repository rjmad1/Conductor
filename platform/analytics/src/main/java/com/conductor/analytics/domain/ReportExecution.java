package com.conductor.analytics.domain;

import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks individual report execution runs with status, timing, and output path.
 */
@Entity
@Table(name = "analytics_report_executions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecution extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID reportId;

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Column(nullable = false)
    private String status; // RUNNING, COMPLETED, FAILED

    private String outputPath;

    private long rowCount;

    private String errorMessage;

    @PrePersist
    void onPrePersist() {
        this.startedAt = Instant.now();
        if (this.status == null) {
            this.status = "RUNNING";
        }
    }
}
