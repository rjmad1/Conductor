package com.conductor.workflow.service;

import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.workflow.ActionType;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Dispatches workflow step actions by type. Each action type has a dedicated handler method. All
 * actions are audited and publish metrics.
 */
@Service
public class ActionExecutor {

  private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);

  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final WorkflowMetrics metrics;

  public ActionExecutor(
      EventPublisher eventPublisher, AuditLogger auditLogger, WorkflowMetrics metrics) {
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
    this.metrics = metrics;
  }

  /**
   * Executes an action step with the given configuration.
   *
   * @param actionType the type of action to execute
   * @param config action-specific configuration
   * @param tenantId the tenant context
   * @return the action result as a map
   */
  public Map<String, Object> execute(
      ActionType actionType, Map<String, Object> config, String tenantId) {
    log.info("Executing action: type={}, tenant={}", actionType, tenantId);
    boolean success = false;

    try {
      Map<String, Object> result =
          switch (actionType) {
            case SEND_EVENT -> executeSendEvent(config, tenantId);
            case INVOKE_INTEGRATION -> executeInvokeIntegration(config, tenantId);
            case CREATE_RECORD -> executeCreateRecord(config, tenantId);
            case UPDATE_RECORD -> executeUpdateRecord(config, tenantId);
            case ASSIGN_USER -> executeAssignUser(config, tenantId);
            case GENERATE_NOTIFICATION -> executeGenerateNotification(config, tenantId);
            case INVOKE_WORKFLOW -> executeInvokeWorkflow(config, tenantId);
            case DELAY -> executeDelay(config);
            case WAIT -> executeWait(config);
            case TERMINATE -> executeTerminate(config, tenantId);
          };
      success = true;
      return result;
    } finally {
      metrics.recordActionExecuted(actionType.name(), success);
      auditLogger.logEvent(
          "ACTION_EXECUTED",
          "action:" + actionType,
          success ? "SUCCESS" : "FAILURE",
          "tenant=" + tenantId);
    }
  }

  private Map<String, Object> executeSendEvent(Map<String, Object> config, String tenantId) {
    String domain = (String) config.getOrDefault("domain", "workflow");
    String entity = (String) config.getOrDefault("entity", "action");
    String action = (String) config.getOrDefault("action", "executed");
    Object payload = config.get("payload");

    eventPublisher.publish(domain, entity, action, "1.0.0", payload);
    log.info("Sent event: {}.{}.{} for tenant {}", domain, entity, action, tenantId);
    return Map.of("status", "sent", "domain", domain, "entity", entity, "action", action);
  }

  private Map<String, Object> executeInvokeIntegration(
      Map<String, Object> config, String tenantId) {
    String integrationId = (String) config.getOrDefault("integrationId", "");
    log.info(
        "Invoking integration {} for tenant {} (delegated to integration domain via event)",
        integrationId,
        tenantId);

    eventPublisher.publish(
        "integration",
        "invocation",
        "requested",
        "1.0.0",
        Map.of("integrationId", integrationId, "config", config));
    return Map.of("status", "invoked", "integrationId", integrationId);
  }

  private Map<String, Object> executeCreateRecord(Map<String, Object> config, String tenantId) {
    String entity = (String) config.getOrDefault("entity", "record");
    log.info("Creating record: entity={}, tenant={} (delegated via event)", entity, tenantId);

    eventPublisher.publish("workflow", "record", "create_requested", "1.0.0", config);
    return Map.of("status", "created", "entity", entity);
  }

  private Map<String, Object> executeUpdateRecord(Map<String, Object> config, String tenantId) {
    String entity = (String) config.getOrDefault("entity", "record");
    String recordId = (String) config.getOrDefault("recordId", "");
    log.info(
        "Updating record: entity={}, id={}, tenant={} (delegated via event)",
        entity,
        recordId,
        tenantId);

    eventPublisher.publish("workflow", "record", "update_requested", "1.0.0", config);
    return Map.of("status", "updated", "entity", entity, "recordId", recordId);
  }

  private Map<String, Object> executeAssignUser(Map<String, Object> config, String tenantId) {
    String role = (String) config.getOrDefault("role", "");
    log.info("Assigning user with role: {} for tenant {}", role, tenantId);
    return Map.of("status", "assigned", "role", role);
  }

  private Map<String, Object> executeGenerateNotification(
      Map<String, Object> config, String tenantId) {
    String channel = (String) config.getOrDefault("channel", "whatsapp");
    String templateName = (String) config.getOrDefault("templateName", "");
    String recipient = (String) config.getOrDefault("recipient", "");
    log.info(
        "Generating notification: channel={}, template={}, recipient={}, tenant={}",
        channel,
        templateName,
        recipient,
        tenantId);

    eventPublisher.publish(
        "messaging",
        "notification",
        "requested",
        "1.0.0",
        Map.of("channel", channel, "templateName", templateName, "recipient", recipient));
    return Map.of("status", "notification_sent", "channel", channel, "template", templateName);
  }

  private Map<String, Object> executeInvokeWorkflow(Map<String, Object> config, String tenantId) {
    String workflowId = (String) config.getOrDefault("workflowId", "");
    log.info("Invoking child workflow: {} for tenant {}", workflowId, tenantId);
    return Map.of("status", "child_workflow_started", "workflowId", workflowId);
  }

  private Map<String, Object> executeDelay(Map<String, Object> config) {
    String duration = (String) config.getOrDefault("duration", "PT1M");
    log.info("Delay step: duration={}", duration);
    return Map.of("status", "delayed", "duration", duration);
  }

  private Map<String, Object> executeWait(Map<String, Object> config) {
    String signal = (String) config.getOrDefault("signal", "continue");
    log.info("Wait step: waiting for signal={}", signal);
    return Map.of("status", "waiting", "signal", signal);
  }

  private Map<String, Object> executeTerminate(Map<String, Object> config, String tenantId) {
    String reason = (String) config.getOrDefault("reason", "Workflow terminated by action");
    log.info("Terminate step: reason={}, tenant={}", reason, tenantId);
    return Map.of("status", "terminated", "reason", reason);
  }
}
