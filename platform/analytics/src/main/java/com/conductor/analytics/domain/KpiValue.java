package com.conductor.analytics.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Computed KPI value with threshold status evaluation result.
 * Value object — not persisted as JPA entity; stored in ClickHouse or returned via API.
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class KpiValue {

    private final UUID kpiId;
    private final String kpiName;
    private final double computedValue;
    private final KpiStatus status;
    private final Instant evaluatedAt;
    private final String tenantId;

    public enum KpiStatus {
        HEALTHY,
        WARNING,
        CRITICAL
    }
}
