package com.conductor.workflow.api;

import com.conductor.workflow.service.TriggerService;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for workflow trigger endpoints. Webhook trigger allows external systems to trigger
 * workflows via HTTP.
 */
@RestController
@RequestMapping("/api/v1/workflows/triggers")
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class WorkflowTriggerController {

  private final TriggerService triggerService;

  public WorkflowTriggerController(TriggerService triggerService) {
    this.triggerService = triggerService;
  }

  /**
   * POST /api/v1/workflows/triggers/webhook/{definitionId} Fires a webhook trigger for a specific
   * workflow definition.
   */
  @PostMapping("/webhook/{definitionId}")
  public ResponseEntity<Void> webhookTrigger(
      @PathVariable UUID definitionId, @RequestBody(required = false) Map<String, Object> payload) {

    triggerService.fireWebhookTrigger(definitionId, payload != null ? payload : Map.of());
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  /**
   * POST /api/v1/workflows/triggers/manual/{definitionId} Manually triggers a workflow definition.
   */
  @PostMapping("/manual/{definitionId}")
  public ResponseEntity<Void> manualTrigger(
      @PathVariable UUID definitionId, @RequestBody(required = false) Map<String, Object> input) {

    triggerService.fireManualTrigger(definitionId, input != null ? input : Map.of());
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
