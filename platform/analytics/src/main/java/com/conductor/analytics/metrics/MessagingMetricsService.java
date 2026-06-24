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
 * Queries ClickHouse materialized views for messaging analytics metrics.
 * Provides sent/delivered/read/failed counts, response rate, channel performance.
 */
@Service
public class MessagingMetricsService {

    private static final Logger log = LoggerFactory.getLogger(MessagingMetricsService.class);
    private final DataSource clickHouseDataSource;

    public MessagingMetricsService(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
        this.clickHouseDataSource = clickHouseDataSource;
    }

    public Map<String, Object> getMessagingMetrics(String tenantId, Instant from, Instant to) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messageCounts", getMessageCountsByAction(tenantId, from, to));
        result.put("channelPerformance", getChannelPerformance(tenantId, from, to));
        result.put("deliveryRate", getDeliveryRate(tenantId, from, to));
        result.put("readRate", getReadRate(tenantId, from, to));
        return result;
    }

    private Map<String, Long> getMessageCountsByAction(String tenantId, Instant from, Instant to) {
        String sql = "SELECT action, sum(message_count) as cnt FROM messaging_metrics_hourly " +
                "WHERE tenant_id = ? AND hour >= ? AND hour <= ? GROUP BY action";
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
            log.error("Failed to query messaging counts for tenant {}", tenantId, e);
        }
        return counts;
    }

    private List<Map<String, Object>> getChannelPerformance(String tenantId, Instant from, Instant to) {
        String sql = "SELECT channel, action, sum(message_count) as cnt FROM messaging_metrics_hourly " +
                "WHERE tenant_id = ? AND hour >= ? AND hour <= ? GROUP BY channel, action ORDER BY channel";
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("channel", rs.getString("channel"));
                    row.put("action", rs.getString("action"));
                    row.put("count", rs.getLong("cnt"));
                    results.add(row);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query channel performance for tenant {}", tenantId, e);
        }
        return results;
    }

    private double getDeliveryRate(String tenantId, Instant from, Instant to) {
        return computeRate(tenantId, from, to, "delivered", "dispatched");
    }

    private double getReadRate(String tenantId, Instant from, Instant to) {
        return computeRate(tenantId, from, to, "read", "delivered");
    }

    private double computeRate(String tenantId, Instant from, Instant to, String numeratorAction, String denominatorAction) {
        String sql = "SELECT " +
                "sumIf(message_count, action = ?) as numerator, " +
                "sumIf(message_count, action = ?) as denominator " +
                "FROM messaging_metrics_hourly WHERE tenant_id = ? AND hour >= ? AND hour <= ?";
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeratorAction);
            ps.setString(2, denominatorAction);
            ps.setString(3, tenantId);
            ps.setObject(4, from);
            ps.setObject(5, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long denom = rs.getLong("denominator");
                    if (denom == 0) return 0.0;
                    return (double) rs.getLong("numerator") / denom * 100.0;
                }
            }
        } catch (Exception e) {
            log.error("Failed to compute rate for tenant {}", tenantId, e);
        }
        return 0.0;
    }
}
