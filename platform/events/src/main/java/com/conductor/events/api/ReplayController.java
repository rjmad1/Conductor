package com.conductor.events.api;

import com.conductor.events.service.ReplayService;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/replay")
public class ReplayController {

  private final ReplayService replayService;

  public ReplayController(ReplayService replayService) {
    this.replayService = replayService;
  }

  @PostMapping
  public ResponseEntity<List<String>> requestReplay(
      @RequestParam String stream,
      @RequestParam String consumer,
      @RequestParam String replayType,
      @RequestParam String startValue) {

    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      return ResponseEntity.badRequest().build();
    }

    String username = TenantContext.getCurrentUserId();
    if (username == null) {
      username = "system-api";
    }

    List<String> events =
        replayService.executeReplay(stream, consumer, replayType, startValue, username);
    return ResponseEntity.ok(events);
  }
}
