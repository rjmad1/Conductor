package com.conductor.workflow.service;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionValidator;
import com.conductor.shared.security.AuthorizationService;
import org.springframework.stereotype.Service;

/**
 * Default implementation of ActionValidator. Performs pre-execution checks on tenant context,
 * required configuration options, parameter compatibility, and caller permissions.
 */
@Service
public class DefaultActionValidator implements ActionValidator {

  private final AuthorizationService authorizationService;

  public DefaultActionValidator(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @Override
  public void validate(ActionContext context, ActionMetadata metadata)
      throws IllegalArgumentException, SecurityException {
    if (context == null) {
      throw new IllegalArgumentException("Action context is required");
    }
    if (metadata == null) {
      throw new IllegalArgumentException("Action metadata is required");
    }

    // 1. Tenant context check
    if (context.getTenantId() == null) {
      throw new IllegalArgumentException("Tenant context is required for action execution");
    }

    // 2. Required configuration keys
    if (metadata.getRequiredConfigurationKeys() != null) {
      for (String requiredKey : metadata.getRequiredConfigurationKeys()) {
        if (context.getConfiguration() == null
            || !context.getConfiguration().containsKey(requiredKey)) {
          throw new IllegalArgumentException(
              "Missing required configuration parameter: " + requiredKey);
        }
        Object value = context.getConfiguration().get(requiredKey);
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
          throw new IllegalArgumentException(
              "Required configuration parameter is empty: " + requiredKey);
        }
      }
    }

    // 3. Supported parameters check
    if (context.getConfiguration() != null && metadata.getSupportedParameters() != null) {
      for (String configKey : context.getConfiguration().keySet()) {
        if (isCommonParameter(configKey)) {
          continue;
        }
        if (!metadata.getSupportedParameters().containsKey(configKey)
            && (metadata.getRequiredConfigurationKeys() == null
                || !metadata.getRequiredConfigurationKeys().contains(configKey))) {
          throw new IllegalArgumentException("Unsupported configuration parameter: " + configKey);
        }
      }
    }

    // 4. Execution permissions check
    if (metadata.getRequiredPermissions() != null && !metadata.getRequiredPermissions().isEmpty()) {
      if (authorizationService == null) {
        throw new SecurityException("Authorization service not available to check permissions");
      }
      for (String permission : metadata.getRequiredPermissions()) {
        if (!authorizationService.hasPermission(permission)) {
          throw new SecurityException(
              "Principal lacks required permission for action execution: " + permission);
        }
      }
    }
  }

  private boolean isCommonParameter(String key) {
    return "onFailure".equals(key)
        || "name".equals(key)
        || "type".equals(key)
        || "stepName".equals(key);
  }
}
