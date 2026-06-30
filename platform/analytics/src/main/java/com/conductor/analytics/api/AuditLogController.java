package com.conductor.analytics.api;

import com.conductor.analytics.domain.AnalyticsEvent;
import com.conductor.analytics.service.AuditLogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST API for querying Audit Logs (MVP mock). */
@RestController
@RequestMapping("/api/v1/analytics/audit-logs")
@PreAuthorize("hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class AuditLogController {

  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ResponseEntity<List<AnalyticsEvent>> list(@RequestParam(defaultValue = "50") int limit) {
    return ResponseEntity.ok(auditLogService.getAuditLogs(limit));
  }
}
