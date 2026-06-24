package com.conductor.analytics.api;

import com.conductor.analytics.metrics.*;
import com.conductor.shared.middleware.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * REST API for analytics metrics across all domains.
 * All endpoints require authentication and are tenant-scoped.
 */
@RestController
@RequestMapping("/api/v1/analytics/metrics")
public class MetricsController {

    private final WorkflowMetricsService workflowMetrics;
    private final MessagingMetricsService messagingMetrics;
    private final CustomerMetricsService customerMetrics;
    private final IntegrationMetricsService integrationMetrics;
    private final TenantMetricsService tenantMetrics;

    public MetricsController(
            WorkflowMetricsService workflowMetrics,
            MessagingMetricsService messagingMetrics,
            CustomerMetricsService customerMetrics,
            IntegrationMetricsService integrationMetrics,
            TenantMetricsService tenantMetrics) {
        this.workflowMetrics = workflowMetrics;
        this.messagingMetrics = messagingMetrics;
        this.customerMetrics = customerMetrics;
        this.integrationMetrics = integrationMetrics;
        this.tenantMetrics = tenantMetrics;
    }

    @GetMapping("/workflow")
    public ResponseEntity<Map<String, Object>> getWorkflowMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String tenantId = resolveTenantId();
        return ResponseEntity.ok(workflowMetrics.getWorkflowMetrics(tenantId, resolveFrom(from), resolveTo(to)));
    }

    @GetMapping("/messaging")
    public ResponseEntity<Map<String, Object>> getMessagingMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String tenantId = resolveTenantId();
        return ResponseEntity.ok(messagingMetrics.getMessagingMetrics(tenantId, resolveFrom(from), resolveTo(to)));
    }

    @GetMapping("/customer")
    public ResponseEntity<Map<String, Object>> getCustomerMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String tenantId = resolveTenantId();
        return ResponseEntity.ok(customerMetrics.getCustomerMetrics(tenantId, resolveFrom(from), resolveTo(to)));
    }

    @GetMapping("/integration")
    public ResponseEntity<Map<String, Object>> getIntegrationMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String tenantId = resolveTenantId();
        return ResponseEntity.ok(integrationMetrics.getIntegrationMetrics(tenantId, resolveFrom(from), resolveTo(to)));
    }

    @GetMapping("/tenant")
    public ResponseEntity<Map<String, Object>> getTenantMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String tenantId = resolveTenantId();
        return ResponseEntity.ok(tenantMetrics.getTenantMetrics(tenantId, resolveFrom(from), resolveTo(to)));
    }

    private String resolveTenantId() {
        var tenantId = TenantContext.getCurrentTenantId();
        return tenantId != null ? tenantId.toString() : "system";
    }

    private Instant resolveFrom(Instant from) {
        return from != null ? from : Instant.now().minus(7, ChronoUnit.DAYS);
    }

    private Instant resolveTo(Instant to) {
        return to != null ? to : Instant.now();
    }
}
