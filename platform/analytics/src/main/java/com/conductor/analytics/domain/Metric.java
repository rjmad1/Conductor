package com.conductor.analytics.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a computed metric data point with dimensional metadata.
 * Used for query results and API responses — not a JPA entity.
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Metric {

    private final String name;
    private final double value;
    private final String unit;
    private final Map<String, String> dimensions;
    private final MetricAggregation aggregation;
    private final Instant timestamp;
    private final String tenantId;
}
