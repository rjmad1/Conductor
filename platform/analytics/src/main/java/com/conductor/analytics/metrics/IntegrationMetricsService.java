package com.conductor.analytics.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;

/**
 * Queries ClickHouse materialized views for integration analytics metrics.
 * Provides connector usage, webhook health, sync success/failure rates.
 */
@Service
public class IntegrationMetricsService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationMetricsService.class);
    private final DataSource clickHouseDataSource;

    public IntegrationMetricsService(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
        this.clickHouseDataSource = clickHouseDataSource;
    }

    public Map<String, Object> getIntegrationMetrics(String tenantId, Instant from, Instant to) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("connectorUsage", getConnectorUsage(tenantId, from, to));
        result.put("webhookHealth", getWebhookHealth(tenantId, from, to));
        result.put("syncHealth", getSyncHealth(tenantId, from, to));
        result.put("overallSuccessRate", getOverallSuccessRate(tenantId, from, to));
        return result;
    }

    private List<Map<String, Object>> getConnectorUsage(String tenantId, Instant from, Instant to) {
        String sql = "SELECT entity, action, sum(event_count) as cnt FROM integration_metrics_hourly " +
                "WHERE tenant_id = ? AND hour >= ? AND hour <= ? GROUP BY entity, action ORDER BY cnt DESC";
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("connector", rs.getString("entity"));
                    row.put("action", rs.getString("action"));
                    row.put("count", rs.getLong("cnt"));
                    results.add(row);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query connector usage for tenant {}", tenantId, e);
        }
        return results;
    }

    private Map<String, Long> getWebhookHealth(String tenantId, Instant from, Instant to) {
        String sql = "SELECT action, sum(event_count) as cnt FROM integration_metrics_hourly " +
                "WHERE tenant_id = ? AND entity = 'webhook' AND hour >= ? AND hour <= ? GROUP BY action";
        Map<String, Long> health = new LinkedHashMap<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    health.put(rs.getString("action"), rs.getLong("cnt"));
                }
            }
        } catch (Exception e) {
            log.error("Failed to query webhook health for tenant {}", tenantId, e);
        }
        return health;
    }

    private Map<String, Long> getSyncHealth(String tenantId, Instant from, Instant to) {
        String sql = "SELECT action, sum(event_count) as cnt FROM integration_metrics_hourly " +
                "WHERE tenant_id = ? AND entity = 'sync' AND hour >= ? AND hour <= ? GROUP BY action";
        Map<String, Long> health = new LinkedHashMap<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    health.put(rs.getString("action"), rs.getLong("cnt"));
                }
            }
        } catch (Exception e) {
            log.error("Failed to query sync health for tenant {}", tenantId, e);
        }
        return health;
    }

    private double getOverallSuccessRate(String tenantId, Instant from, Instant to) {
        String sql = "SELECT " +
                "sumIf(event_count, action NOT IN ('failed')) as successes, " +
                "sum(event_count) as total " +
                "FROM integration_metrics_hourly WHERE tenant_id = ? AND hour >= ? AND hour <= ?";
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long total = rs.getLong("total");
                    if (total == 0) return 0.0;
                    return (double) rs.getLong("successes") / total * 100.0;
                }
            }
        } catch (Exception e) {
            log.error("Failed to compute integration success rate for tenant {}", tenantId, e);
        }
        return 0.0;
    }
}
