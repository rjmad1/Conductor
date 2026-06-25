package com.conductor.shared.middleware.tenant;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

  private static final Logger log = LoggerFactory.getLogger("CONDUCTOR_AUDIT_LOG");

  private final String serviceId;

  public AuditLogger(@Value("${spring.application.name:monolith}") String serviceId) {
    this.serviceId = serviceId;
  }

  public void logEvent(String action, String resource, String outcome, String details) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    String userId = TenantContext.getCurrentUserId();

    // Fetch Request ID from a thread-local or MDC if present
    String requestId = org.slf4j.MDC.get("requestId");
    if (requestId == null) {
      requestId = "system-execution";
    }

    String tenantIdStr = tenantId != null ? tenantId.toString() : "global";
    String userIdStr = userId != null ? userId : "anonymous";
    String timestamp = Instant.now().toString();

    // Write structured audit log message
    log.info(
        "timestamp={} tenantId={} userId={} serviceId={} requestId={} action={} resource={} outcome={} details=\"{}\"",
        timestamp,
        tenantIdStr,
        userIdStr,
        serviceId,
        requestId,
        action,
        resource,
        outcome,
        details);
  }
}
