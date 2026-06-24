package com.conductor.analytics.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Centralized Micrometer metrics for the analytics platform.
 * Exposes counters and timers for ingestion, queries, reports, KPIs, and dashboards.
 */
@Component
public class AnalyticsMetrics {

    private final MeterRegistry registry;

    public AnalyticsMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordEventsIngested(int count) {
        Counter.builder("analytics.events.ingested")
                .description("Events successfully ingested into ClickHouse")
                .register(registry)
                .increment(count);
    }

    public void recordEventsFailed(int count) {
        Counter.builder("analytics.events.failed")
                .description("Events that failed to ingest into ClickHouse")
                .register(registry)
                .increment(count);
    }

    public void recordEventsDropped(int count) {
        Counter.builder("analytics.events.dropped")
                .description("Events dropped due to buffer overflow")
                .register(registry)
                .increment(count);
    }

    public void recordBatchInsertLatency(long durationMs) {
        Timer.builder("analytics.batch.insert.latency")
                .description("ClickHouse batch insert latency")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordQueryLatency(String queryType, long durationMs) {
        Timer.builder("analytics.query.latency")
                .tag("type", queryType)
                .description("ClickHouse query latency by type")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordReportExecution(String status) {
        Counter.builder("analytics.reports.executed")
                .tag("status", status)
                .description("Report executions")
                .register(registry)
                .increment();
    }

    public void recordKpiEvaluations(int count) {
        Counter.builder("analytics.kpi.evaluations")
                .description("KPI evaluations performed")
                .register(registry)
                .increment(count);
    }

    public void recordDashboardView() {
        Counter.builder("analytics.dashboards.views")
                .description("Dashboard view requests")
                .register(registry)
                .increment();
    }
}
