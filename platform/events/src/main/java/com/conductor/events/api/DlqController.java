package com.conductor.events.api;

import com.conductor.events.domain.DlqRecord;
import com.conductor.events.service.DlqService;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/dlq")
@PreAuthorize("hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class DlqController {

  private final DlqService dlqService;

  public DlqController(DlqService dlqService) {
    this.dlqService = dlqService;
  }

  @GetMapping
  public ResponseEntity<List<DlqRecord>> getPendingRecords() {
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(dlqService.getPendingRecords());
  }

  @PostMapping("/{id}/replay")
  public ResponseEntity<Void> replayRecord(@PathVariable UUID id) {
    boolean success = dlqService.replayRecord(id);
    if (success) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/{id}/discard")
  public ResponseEntity<Void> discardRecord(@PathVariable UUID id) {
    dlqService.discardRecord(id);
    return ResponseEntity.ok().build();
  }
}
