package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.middleware.tenant.AuditLogger;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Built-in action to write audit events to the centralized audit logger. */
@Component
public class CreateAuditRecordActionHandler implements ActionHandler {

  private final AuditLogger auditLogger;

  public CreateAuditRecordActionHandler(AuditLogger auditLogger) {
    this.auditLogger = auditLogger;
  }

  @Override
  public String getActionType() {
    return "CREATE_AUDIT_RECORD";
  }

  @Override
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    String action = (String) config.get("action");
    String resource = (String) config.get("resource");
    String outcome = (String) config.get("outcome");
    String details = (String) config.getOrDefault("details", "");

    auditLogger.logEvent(action, resource, outcome, details);

    return ActionResult.success(
        Map.of(
            "action", action,
            "resource", resource,
            "outcome", outcome,
            "status", "audit_logged"));
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Create Audit Record")
        .description("Registers a security/operational audit event in the tenant audit logs.")
        .requiredConfigurationKeys(List.of("action", "resource", "outcome"))
        .supportedParameters(
            Map.of(
                "action", "The operation performed",
                "resource", "The target resource",
                "outcome", "Status outcome (SUCCESS, FAILURE)",
                "details", "Additional key-value pairs or text descriptions"))
        .build();
  }
}
