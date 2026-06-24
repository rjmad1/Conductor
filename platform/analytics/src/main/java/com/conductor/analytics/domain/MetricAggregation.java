package com.conductor.analytics.domain;

/**
 * Supported aggregation types for metric computations.
 */
public enum MetricAggregation {
    SUM,
    COUNT,
    AVG,
    MIN,
    MAX,
    P95,
    P99,
    RATE
}
