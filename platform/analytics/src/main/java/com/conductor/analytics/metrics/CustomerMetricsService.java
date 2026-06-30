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
 * Queries ClickHouse materialized views for customer analytics metrics. Provides growth, activity,
 * consent status, and lifecycle insights.
 */
@Service
public class CustomerMetricsService {

  private static final Logger log = LoggerFactory.getLogger(CustomerMetricsService.class);
  private final DataSource clickHouseDataSource;

  public CustomerMetricsService(
      @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
    this.clickHouseDataSource = clickHouseDataSource;
  }

  public Map<String, Object> getCustomerMetrics(String tenantId, Instant from, Instant to) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("customerActivity", getActivityByAction(tenantId, from, to));
    result.put("growthTimeline", getGrowthTimeline(tenantId, from, to));
    result.put("consentMetrics", getConsentMetrics(tenantId, from, to));
    return result;
  }

  private Map<String, Long> getActivityByAction(String tenantId, Instant from, Instant to) {
    String sql =
        "SELECT action, sum(customer_count) as cnt FROM customer_metrics_daily "
            + "WHERE tenant_id = ? AND day >= toDate(?) AND day <= toDate(?) GROUP BY action";
    Map<String, Long> counts = new LinkedHashMap<>();
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          counts.put(rs.getString("action"), rs.getLong("cnt"));
        }
      }
    } catch (Exception e) {
      log.error("Failed to query customer activity for tenant {}", tenantId, e);
    }
    return counts;
  }

  private List<Map<String, Object>> getGrowthTimeline(String tenantId, Instant from, Instant to) {
    String sql =
        "SELECT day, sum(customer_count) as cnt FROM customer_metrics_daily "
            + "WHERE tenant_id = ? AND action = 'created' AND day >= toDate(?) AND day <= toDate(?) "
            + "GROUP BY day ORDER BY day";
    List<Map<String, Object>> timeline = new ArrayList<>();
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> point = new LinkedHashMap<>();
          point.put("date", rs.getDate("day").toString());
          point.put("count", rs.getLong("cnt"));
          timeline.add(point);
        }
      }
    } catch (Exception e) {
      log.error("Failed to query customer growth timeline for tenant {}", tenantId, e);
    }
    return timeline;
  }

  private Map<String, Long> getConsentMetrics(String tenantId, Instant from, Instant to) {
    Map<String, Long> consent = new LinkedHashMap<>();
    String sql =
        "SELECT action, sum(customer_count) as cnt FROM customer_metrics_daily "
            + "WHERE tenant_id = ? AND action IN ('opt_out', 'opt_in', 'consent_granted', 'consent_revoked') "
            + "AND day >= toDate(?) AND day <= toDate(?) GROUP BY action";
    try (Connection conn = clickHouseDataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tenantId);
      ps.setObject(2, from);
      ps.setObject(3, to);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          consent.put(rs.getString("action"), rs.getLong("cnt"));
        }
      }
    } catch (Exception e) {
      log.error("Failed to query consent metrics for tenant {}", tenantId, e);
    }
    return consent;
  }
}
