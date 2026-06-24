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
 * Queries ClickHouse materialized views for tenant-level analytics.
 * Provides cross-domain usage overview per tenant.
 */
@Service
public class TenantMetricsService {

    private static final Logger log = LoggerFactory.getLogger(TenantMetricsService.class);
    private final DataSource clickHouseDataSource;

    public TenantMetricsService(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
        this.clickHouseDataSource = clickHouseDataSource;
    }

    public Map<String, Object> getTenantMetrics(String tenantId, Instant from, Instant to) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("domainUsage", getDomainUsage(tenantId, from, to));
        result.put("activityTimeline", getActivityTimeline(tenantId, from, to));
        result.put("totalEvents", getTotalEvents(tenantId, from, to));
        return result;
    }

    /**
     * Returns platform-wide tenant overview (for system admins / no tenant filter).
     */
    public List<Map<String, Object>> getPlatformTenantOverview(Instant from, Instant to) {
        String sql = "SELECT tenant_id, sum(event_count) as total_events, " +
                "count(DISTINCT domain) as active_domains " +
                "FROM tenant_metrics_daily WHERE day >= toDate(?) AND day <= toDate(?) " +
                "GROUP BY tenant_id ORDER BY total_events DESC LIMIT 100";
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, from);
            ps.setObject(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("tenantId", rs.getString("tenant_id"));
                    row.put("totalEvents", rs.getLong("total_events"));
                    row.put("activeDomains", rs.getInt("active_domains"));
                    results.add(row);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query platform tenant overview", e);
        }
        return results;
    }

    private Map<String, Long> getDomainUsage(String tenantId, Instant from, Instant to) {
        String sql = "SELECT domain, sum(event_count) as cnt FROM tenant_metrics_daily " +
                "WHERE tenant_id = ? AND day >= toDate(?) AND day <= toDate(?) GROUP BY domain";
        Map<String, Long> usage = new LinkedHashMap<>();
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usage.put(rs.getString("domain"), rs.getLong("cnt"));
                }
            }
        } catch (Exception e) {
            log.error("Failed to query domain usage for tenant {}", tenantId, e);
        }
        return usage;
    }

    private List<Map<String, Object>> getActivityTimeline(String tenantId, Instant from, Instant to) {
        String sql = "SELECT day, sum(event_count) as cnt FROM tenant_metrics_daily " +
                "WHERE tenant_id = ? AND day >= toDate(?) AND day <= toDate(?) " +
                "GROUP BY day ORDER BY day";
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
                    point.put("events", rs.getLong("cnt"));
                    timeline.add(point);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query activity timeline for tenant {}", tenantId, e);
        }
        return timeline;
    }

    private long getTotalEvents(String tenantId, Instant from, Instant to) {
        String sql = "SELECT sum(event_count) as total FROM tenant_metrics_daily " +
                "WHERE tenant_id = ? AND day >= toDate(?) AND day <= toDate(?)";
        try (Connection conn = clickHouseDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("total");
            }
        } catch (Exception e) {
            log.error("Failed to query total events for tenant {}", tenantId, e);
        }
        return 0;
    }
}
