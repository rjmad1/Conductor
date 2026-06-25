package com.conductor.analytics.metrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Queries ClickHouse materialized views for workflow analytics metrics. All queries are
 * tenant-scoped via mandatory tenant_id parameter.
 */
@Service
public class WorkflowMetricsService {

  private static final Logger log = LoggerFactory.getLogger(WorkflowMetricsService.class);
  private final DataSource clickHouseDataSource;

  public WorkflowMetricsService(
      @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
    this.clickHouseDataSource = clickHouseDataSource;
  }

  public Map<String, Object> getWorkflowMetrics(String tenantId, Instant from, Instant to) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("executions", getExecutionCounts(tenantId, from, to));
    result.put("successRate", getSuccessRate(tenantId, from, to));
    result.put("avgLatencyMs", getAverageLatency(tenantId, from, to));
    result.put("retries", getEventCount(tenantId, "workflow", "execution", "retried", from, to));
    result.put(
        "compensations", getEventCount(tenantId, "workflow", "execution", "compensated", from, to));
    return result;
  }

  private Map<String, Long> getExecutionCounts(String tenantId, Instant from, Instant to) {
    String sql =
        "SELECT status, sum(executions_count) as cnt FROM workflow_metrics_hourly "
            + "WHERE tenant_id = ? AND hour >= ? AND hour <= ? GROUP BY status";
    Map<String, Long> counts = new LinkedHashMap<>();
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          counts.put(rs.getString("status"), rs.getLong("cnt"));
        }
      }
    } catch (Exception e) {
      log.error("Failed to query workflow execution counts for tenant {}", tenantId, e);
    }
    return counts;
  }

  private double getSuccessRate(String tenantId, Instant from, Instant to) {
    String sql =
        "SELECT "
            + "sumIf(executions_count, status = 'completed') as completed, "
            + "sum(executions_count) as total "
            + "FROM workflow_metrics_hourly WHERE tenant_id = ? AND hour >= ? AND hour <= ?";
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          long total = rs.getLong("total");
          if (total == 0) return 0.0;
          return (double) rs.getLong("completed") / total * 100.0;
        }
      }
    } catch (Exception e) {
      log.error("Failed to query workflow success rate for tenant {}", tenantId, e);
    }
    return 0.0;
  }

  private double getAverageLatency(String tenantId, Instant from, Instant to) {
    String sql =
        "SELECT sum(total_duration_ms) / sum(executions_count) as avg_ms "
            + "FROM workflow_metrics_hourly WHERE tenant_id = ? AND hour >= ? AND hour <= ? "
            + "AND total_duration_ms > 0";
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getDouble("avg_ms");
        }
      }
    } catch (Exception e) {
      log.error("Failed to query workflow latency for tenant {}", tenantId, e);
    }
    return 0.0;
  }

  private long getEventCount(
      String tenantId, String domain, String entity, String action, Instant from, Instant to) {
    String sql =
        "SELECT count() as cnt FROM conductor_events "
            + "WHERE tenant_id = ? AND domain = ? AND entity = ? AND action = ? "
            + "AND created_at >= ? AND created_at <= ?";
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setString(2, domain);
      ps.setString(3, entity);
      ps.setString(4, action);
      ps.setObject(5, from);
      ps.setObject(6, to);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong("cnt");
      }
    } catch (Exception e) {
      log.error("Failed to query event count for tenant {}", tenantId, e);
    }
    return 0;
  }
}
