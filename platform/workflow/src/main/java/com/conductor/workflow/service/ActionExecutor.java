package com.conductor.workflow.service;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionRegistry;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.execution.action.ActionValidator;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.workflow.ActionType;
import com.conductor.workflow.domain.ActionExecution;
import com.conductor.workflow.domain.WorkflowExecution;
import com.conductor.workflow.repository.ActionExecutionRepository;
import com.conductor.workflow.repository.WorkflowExecutionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Dispatches workflow step actions by type. Resolves pluggable actions via {@link ActionRegistry},
 * runs validations via {@link ActionValidator}, and persists execution audits to {@link
 * ActionExecutionRepository}. Falls back to legacy step implementations for backward compatibility.
 */
@Service
public class ActionExecutor {

  private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);

  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final WorkflowMetrics metrics;
  private final ActionRegistry actionRegistry;
  private final ActionValidator actionValidator;
  private final ActionExecutionRepository actionExecutionRepository;
  private final WorkflowExecutionRepository executionRepository;
  private final ObjectMapper objectMapper;

  public ActionExecutor(
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      WorkflowMetrics metrics,
      ActionRegistry actionRegistry,
      ActionValidator actionValidator,
      ActionExecutionRepository actionExecutionRepository,
      WorkflowExecutionRepository executionRepository) {
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
    this.metrics = metrics;
    this.actionRegistry = actionRegistry;
    this.actionValidator = actionValidator;
    this.actionExecutionRepository = actionExecutionRepository;
    this.executionRepository = executionRepository;
    this.objectMapper = new ObjectMapper();
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
    return execute(actionType, config, tenantId, null);
  }

  /**
   * Executes an action step with the given configuration and workflow execution context tracking.
   */
  public Map<String, Object> execute(
      ActionType actionType, Map<String, Object> config, String tenantId, UUID executionId) {
    log.info(
        "Executing action: type={}, tenant={}, execution={}", actionType, tenantId, executionId);

    UUID tenantUuid = tenantId != null ? UUID.fromString(tenantId) : null;
    UUID oldTenant = TenantContext.getCurrentTenantId();
    String oldUser = TenantContext.getCurrentUserId();
    if (tenantUuid != null) {
      TenantContext.setCurrentTenantId(tenantUuid);
    }

    try {
      Optional<ActionHandler> handlerOpt = actionRegistry.getHandler(actionType.name());
      if (handlerOpt.isPresent()) {
        ActionHandler handler = handlerOpt.get();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        ActionResult result = null;
        Exception executionException = null;

        UUID definitionId = null;
        Map<String, Object> variables = Map.of();
        String correlationId = null;

        if (executionId != null) {
          try {
            Optional<WorkflowExecution> execOpt = executionRepository.findById(executionId);
            if (execOpt.isPresent()) {
              WorkflowExecution exec = execOpt.get();
              definitionId = exec.getDefinitionId();
              correlationId = exec.getTemporalWorkflowId();
              if (exec.getVariables() != null) {
                variables = parseVariables(exec.getVariables());
              }
            }
          } catch (Exception e) {
            log.warn("Failed to load workflow execution details for ActionContext", e);
          }
        }

        ActionContext context =
            ActionContext.builder()
                .tenantId(tenantUuid)
                .workflowDefinitionId(definitionId)
                .workflowExecutionId(executionId)
                .configuration(config)
                .variables(variables)
                .correlationId(correlationId)
                .build();

        try {
          actionValidator.validate(context, handler.getMetadata());
          result = handler.execute(context);
          success = result.isSuccess();
          if (!success) {
            throw new RuntimeException(
                result.getErrorMessage() != null
                    ? result.getErrorMessage()
                    : "Action execution failed");
          }
          return result.getOutputVariables() != null ? result.getOutputVariables() : Map.of();

        } catch (Exception e) {
          executionException = e;
          throw e;
        } finally {
          long duration = System.currentTimeMillis() - startTime;
          metrics.recordActionExecuted(actionType.name(), success);
          auditLogger.logEvent(
              "ACTION_EXECUTED",
              "action:" + actionType,
              success ? "SUCCESS" : "FAILURE",
              "tenant=" + tenantId + ", duration=" + duration + "ms");

          try {
            ActionExecution actionExecution =
                ActionExecution.builder()
                    .actionType(actionType.name())
                    .workflowExecutionId(executionId)
                    .correlationId(correlationId)
                    .inputs(sanitizeJson(config))
                    .outputs(
                        result != null && result.getOutputVariables() != null
                            ? sanitizeJson(result.getOutputVariables())
                            : "{}")
                    .status(success ? "SUCCESS" : "FAILURE")
                    .failureReason(
                        executionException != null
                            ? executionException.getMessage()
                            : (result != null ? result.getErrorMessage() : null))
                    .executionDurationMs(duration)
                    .startedAt(Instant.ofEpochMilli(startTime))
                    .completedAt(Instant.now())
                    .build();
            actionExecution.setTenantId(tenantUuid);
            actionExecutionRepository.save(actionExecution);
          } catch (Exception ex) {
            log.error("Failed to persist ActionExecution history", ex);
          }
        }
      }

      log.info("ActionHandler not found for type {}, falling back to legacy execution", actionType);
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
              default -> throw new IllegalArgumentException(
                  "Unsupported action type: " + actionType);
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
    } finally {
      TenantContext.setCurrentTenantId(oldTenant);
      TenantContext.setCurrentUserId(oldUser);
    }
  }

  @SuppressWarnings("unchecked")
  private String sanitizeJson(Map<String, Object> map) {
    if (map == null) return "{}";
    Map<String, Object> sanitized = new java.util.HashMap<>(map);
    for (String key : map.keySet()) {
      if (isSensitiveKey(key)) {
        sanitized.put(key, "******");
      } else if (map.get(key) instanceof Map) {
        try {
          sanitized.put(
              key,
              objectMapper.readValue(
                  sanitizeJson((Map<String, Object>) map.get(key)),
                  new TypeReference<Map<String, Object>>() {}));
        } catch (Exception ignored) {
        }
      }
    }
    try {
      return objectMapper.writeValueAsString(sanitized);
    } catch (Exception e) {
      return "{}";
    }
  }

  private boolean isSensitiveKey(String key) {
    String lower = key.toLowerCase();
    return lower.contains("password")
        || lower.contains("secret")
        || lower.contains("token")
        || lower.contains("key")
        || lower.contains("apikey")
        || lower.contains("credential");
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

  private Map<String, Object> parseVariables(String variablesStr) {
    if (variablesStr == null || variablesStr.trim().isEmpty()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(variablesStr, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      try {
        String unescaped = objectMapper.readValue(variablesStr, String.class);
        return objectMapper.readValue(unescaped, new TypeReference<Map<String, Object>>() {});
      } catch (Exception ex) {
        log.warn("Failed to parse variables", ex);
        return Map.of();
      }
    }
  }
}
