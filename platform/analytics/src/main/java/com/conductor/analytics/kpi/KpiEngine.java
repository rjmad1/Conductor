package com.conductor.analytics.kpi;

import com.conductor.analytics.domain.KpiDefinition;
import com.conductor.analytics.domain.KpiValue;
import com.conductor.analytics.observability.AnalyticsMetrics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * KPI evaluation engine. Scheduled to run periodically, evaluates all active KPI definitions
 * against current ClickHouse metrics, computes threshold status, and records results.
 */
@Component
public class KpiEngine {

  private static final Logger log = LoggerFactory.getLogger(KpiEngine.class);

  private final KpiDefinitionRepository repository;
  private final DataSource clickHouseDataSource;
  private final AnalyticsMetrics metrics;

  public KpiEngine(
      KpiDefinitionRepository repository,
      @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource,
      AnalyticsMetrics metrics) {
    this.repository = repository;
    this.clickHouseDataSource = clickHouseDataSource;
    this.metrics = metrics;
  }

  /** Scheduled KPI evaluation — runs every 5 minutes. */
  @Scheduled(fixedDelayString = "${analytics.kpi.evaluation-interval-ms:300000}")
  public void scheduledEvaluation() {
    log.debug("Running scheduled KPI evaluation");
    List<KpiValue> results = evaluateAll();
    for (KpiValue kpi : results) {
      if (kpi.getStatus() != KpiValue.KpiStatus.HEALTHY) {
        log.warn(
            "KPI alert: {} = {} (status: {})",
            kpi.getKpiName(),
            kpi.getComputedValue(),
            kpi.getStatus());
      }
    }
    metrics.recordKpiEvaluations(results.size());
  }

  /** Evaluate all active KPI definitions and return computed values. */
  public List<KpiValue> evaluateAll() {
    List<KpiDefinition> definitions = repository.findByStatus("ACTIVE");
    List<KpiValue> results = new ArrayList<>();

    for (KpiDefinition def : definitions) {
      try {
        double value = computeMetricValue(def);
        KpiValue.KpiStatus status = evaluateThreshold(value, def);
        results.add(
            KpiValue.builder()
                .kpiId(def.getId())
                .kpiName(def.getName())
                .computedValue(value)
                .status(status)
                .evaluatedAt(Instant.now())
                .tenantId(def.getTenantId() != null ? def.getTenantId().toString() : "system")
                .build());
      } catch (Exception e) {
        log.error("Failed to evaluate KPI: {}", def.getName(), e);
      }
    }
    return results;
  }

  double computeMetricValue(KpiDefinition def) {
    Instant now = Instant.now();
    Instant dayAgo = now.minus(24, ChronoUnit.HOURS);
    String metricName = def.getMetricName();

    // Map metric names to ClickHouse queries
    String sql =
        buildMetricQuery(
            metricName, def.getTenantId() != null ? def.getTenantId().toString() : null);
    if (sql == null) {
      log.warn("No query mapping for metric: {}", metricName);
      return 0.0;
    }

    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setObject(1, dayAgo);
      ps.setObject(2, now);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getDouble(1);
        }
      }
    } catch (Exception e) {
      log.error("Failed to compute metric value for {}", metricName, e);
      throw new RuntimeException(e);
    }
    return 0.0;
  }

  KpiValue.KpiStatus evaluateThreshold(double value, KpiDefinition def) {
    if (def.getThresholdCritical() != null && value <= def.getThresholdCritical()) {
      return KpiValue.KpiStatus.CRITICAL;
    }
    if (def.getThresholdWarning() != null && value <= def.getThresholdWarning()) {
      return KpiValue.KpiStatus.WARNING;
    }
    return KpiValue.KpiStatus.HEALTHY;
  }

  private String buildMetricQuery(String metricName, String tenantId) {
    String tenantClause = tenantId != null ? " AND tenant_id = '" + tenantId + "'" : "";

    return switch (metricName) {
      case "workflow_success_rate" -> "SELECT sumIf(executions_count, status = 'completed') * 100.0 / greatest(sum(executions_count), 1) "
          + "FROM workflow_metrics_hourly WHERE hour >= ? AND hour <= ?"
          + tenantClause;
      case "message_delivery_rate" -> "SELECT sumIf(message_count, action = 'delivered') * 100.0 / greatest(sumIf(message_count, action = 'dispatched'), 1) "
          + "FROM messaging_metrics_hourly WHERE hour >= ? AND hour <= ?"
          + tenantClause;
      case "integration_success_rate" -> "SELECT sumIf(event_count, action != 'failed') * 100.0 / greatest(sum(event_count), 1) "
          + "FROM integration_metrics_hourly WHERE hour >= ? AND hour <= ?"
          + tenantClause;
      case "active_tenants" -> "SELECT count(DISTINCT tenant_id) FROM tenant_metrics_daily WHERE day >= toDate(?) AND day <= toDate(?)"
          + tenantClause;
      case "customer_growth" -> "SELECT sum(customer_count) FROM customer_metrics_daily WHERE action = 'created' AND day >= toDate(?) AND day <= toDate(?)"
          + tenantClause;
      case "active_customers" -> "SELECT count(DISTINCT tenant_id) FROM customer_metrics_daily WHERE day >= toDate(?) AND day <= toDate(?)"
          + tenantClause;
      case "response_rate" -> "SELECT sumIf(message_count, action = 'read') * 100.0 / greatest(sumIf(message_count, action = 'delivered'), 1) "
          + "FROM messaging_metrics_hourly WHERE hour >= ? AND hour <= ?"
          + tenantClause;
      default -> null;
    };
  }
}
