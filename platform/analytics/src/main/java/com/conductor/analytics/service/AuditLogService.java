package com.conductor.analytics.service;

import com.conductor.analytics.domain.AnalyticsEvent;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

  // For MVP, since ClickHouse is not wired up yet, we will mock the retrieval.
  // In Phase 3, this will query ClickHouse or a dedicated Audit JPA table.
  public List<AnalyticsEvent> getAuditLogs(int limit) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    String tenantStr = tenantId != null ? tenantId.toString() : "global";

    List<AnalyticsEvent> logs = new ArrayList<>();

    logs.add(
        AnalyticsEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType("audit")
            .tenantId(tenantStr)
            .domain("tenant")
            .entity("tenant")
            .action("TENANT_CREATED")
            .correlationId("req-123")
            .source("tenant-service")
            .payload("{\"status\":\"SUCCESS\"}")
            .createdAt(Instant.now().minusSeconds(3600))
            .build());

    logs.add(
        AnalyticsEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType("audit")
            .tenantId(tenantStr)
            .domain("customer")
            .entity("import")
            .action("CUSTOMER_IMPORT_COMPLETED")
            .correlationId("req-456")
            .source("customer-service")
            .payload("{\"total\":10, \"failed\":0}")
            .createdAt(Instant.now().minusSeconds(1800))
            .build());

    return logs.size() > limit ? logs.subList(0, limit) : logs;
  }
}
