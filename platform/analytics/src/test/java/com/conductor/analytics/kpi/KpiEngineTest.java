package com.conductor.analytics.kpi;

import com.conductor.analytics.domain.KpiDefinition;
import com.conductor.analytics.domain.KpiValue;
import com.conductor.analytics.domain.MetricAggregation;
import com.conductor.analytics.observability.AnalyticsMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KPI evaluation engine.
 */
class KpiEngineTest {

    private KpiDefinitionRepository repository;
    private DataSource clickHouseDs;
    private AnalyticsMetrics metrics;
    private KpiEngine engine;

    @BeforeEach
    void setUp() {
        repository = mock(KpiDefinitionRepository.class);
        clickHouseDs = mock(DataSource.class);
        metrics = mock(AnalyticsMetrics.class);
        engine = new KpiEngine(repository, clickHouseDs, metrics);
    }

    @Test
    void evaluateThresholdReturnsHealthy() {
        KpiDefinition def = buildKpi(90.0, 75.0);
        assertEquals(KpiValue.KpiStatus.HEALTHY, engine.evaluateThreshold(95.0, def));
    }

    @Test
    void evaluateThresholdReturnsWarning() {
        KpiDefinition def = buildKpi(90.0, 75.0);
        assertEquals(KpiValue.KpiStatus.WARNING, engine.evaluateThreshold(85.0, def));
    }

    @Test
    void evaluateThresholdReturnsCritical() {
        KpiDefinition def = buildKpi(90.0, 75.0);
        assertEquals(KpiValue.KpiStatus.CRITICAL, engine.evaluateThreshold(70.0, def));
    }

    @Test
    void evaluateThresholdWithNullThresholdsReturnsHealthy() {
        KpiDefinition def = buildKpi(null, null);
        assertEquals(KpiValue.KpiStatus.HEALTHY, engine.evaluateThreshold(50.0, def));
    }

    @Test
    void evaluateAllWithNoDefinitionsReturnsEmpty() {
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of());
        List<KpiValue> results = engine.evaluateAll();
        assertTrue(results.isEmpty());
    }

    @Test
    void evaluateAllHandlesQueryFailureGracefully() throws Exception {
        KpiDefinition def = buildKpi(90.0, 75.0);
        def.setMetricName("workflow_success_rate");
        def.setId(UUID.randomUUID());
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(def));
        when(clickHouseDs.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        List<KpiValue> results = engine.evaluateAll();
        // Should not throw — gracefully handles error
        assertTrue(results.isEmpty());
    }

    private KpiDefinition buildKpi(Double warning, Double critical) {
        return KpiDefinition.builder()
                .id(UUID.randomUUID())
                .name("Test KPI")
                .metricName("test_metric")
                .aggregation(MetricAggregation.RATE)
                .thresholdWarning(warning)
                .thresholdCritical(critical)
                .status("ACTIVE")
                .build();
    }
}
