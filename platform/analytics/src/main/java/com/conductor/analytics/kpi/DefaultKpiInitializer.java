package com.conductor.analytics.kpi;

import com.conductor.analytics.domain.KpiDefinition;
import com.conductor.analytics.domain.MetricAggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds default KPI definitions on first startup if none exist.
 * These represent the 7 core platform KPIs specified in the analytics requirements.
 */
@Component
public class DefaultKpiInitializer {

    private static final Logger log = LoggerFactory.getLogger(DefaultKpiInitializer.class);
    private final KpiDefinitionRepository repository;

    public DefaultKpiInitializer(KpiDefinitionRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaults() {
        if (repository.count() > 0) {
            log.info("KPI definitions already exist, skipping seed");
            return;
        }

        log.info("Seeding default KPI definitions");

        createKpi("Workflow Success Rate", "workflow_success_rate",
                MetricAggregation.RATE, 90.0, 75.0, "platform");
        createKpi("Message Delivery Rate", "message_delivery_rate",
                MetricAggregation.RATE, 95.0, 85.0, "platform");
        createKpi("Integration Success Rate", "integration_success_rate",
                MetricAggregation.RATE, 95.0, 80.0, "platform");
        createKpi("Active Tenants", "active_tenants",
                MetricAggregation.COUNT, 1.0, 0.0, "platform");
        createKpi("Customer Growth", "customer_growth",
                MetricAggregation.COUNT, null, null, "platform");
        createKpi("Active Customers", "active_customers",
                MetricAggregation.COUNT, null, null, "platform");
        createKpi("Response Rate", "response_rate",
                MetricAggregation.RATE, 50.0, 25.0, "platform");

        log.info("Seeded {} default KPI definitions", 7);
    }

    private void createKpi(String name, String metricName, MetricAggregation aggregation,
                           Double warningThreshold, Double criticalThreshold, String owner) {
        KpiDefinition kpi = KpiDefinition.builder()
                .name(name)
                .description("Platform KPI: " + name)
                .metricName(metricName)
                .aggregation(aggregation)
                .thresholdWarning(warningThreshold)
                .thresholdCritical(criticalThreshold)
                .owner(owner)
                .status("ACTIVE")
                .build();
        repository.save(kpi);
    }
}
