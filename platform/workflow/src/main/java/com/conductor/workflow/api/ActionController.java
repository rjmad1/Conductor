package com.conductor.workflow.api;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionRegistry;
import com.conductor.shared.execution.action.ActionValidator;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for Action Framework discovery and configuration validation. */
@RestController
@RequestMapping("/api/v1/actions")
public class ActionController {

  private final ActionRegistry actionRegistry;
  private final ActionValidator actionValidator;

  public ActionController(ActionRegistry actionRegistry, ActionValidator actionValidator) {
    this.actionRegistry = actionRegistry;
    this.actionValidator = actionValidator;
  }

  /** GET /api/v1/actions — Lists all available, pluggable actions. */
  @GetMapping
  public ResponseEntity<List<ActionInfo>> listAvailableActions() {
    List<ActionInfo> actions =
        actionRegistry.getHandlers().stream()
            .map(
                handler ->
                    new ActionInfo(
                        handler.getActionType(),
                        handler.getMetadata().getName(),
                        handler.getMetadata().getDescription()))
            .collect(Collectors.toList());
    return ResponseEntity.ok(actions);
  }

  /** GET /api/v1/actions/{actionType}/metadata — Retrieves schema requirements for an action. */
  @GetMapping("/{actionType}/metadata")
  public ResponseEntity<ActionMetadata> getActionMetadata(@PathVariable String actionType) {
    return actionRegistry
        .getHandler(actionType)
        .map(handler -> ResponseEntity.ok(handler.getMetadata()))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /** POST /api/v1/actions/{actionType}/validate — Validates input parameters before execution. */
  @PostMapping("/{actionType}/validate")
  public ResponseEntity<ValidationResult> validateActionConfig(
      @PathVariable String actionType, @RequestBody Map<String, Object> config) {

    var handlerOpt = actionRegistry.getHandler(actionType);
    if (handlerOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    ActionHandler handler = handlerOpt.get();
    UUID tenantId = TenantContext.getCurrentTenantId();
    if (tenantId == null) {
      // In sandbox/testing or external verification endpoints, default context if absent
      tenantId = UUID.randomUUID();
    }

    ActionContext context =
        ActionContext.builder().tenantId(tenantId).configuration(config).build();

    try {
      actionValidator.validate(context, handler.getMetadata());
      return ResponseEntity.ok(new ValidationResult(true, null));
    } catch (IllegalArgumentException | SecurityException e) {
      return ResponseEntity.ok(new ValidationResult(false, e.getMessage()));
    }
  }

  public record ActionInfo(String actionType, String name, String description) {}

  public record ValidationResult(boolean valid, String error) {}
}
