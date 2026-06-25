package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Built-in action to write messages to log outputs with variable interpolation. */
@Component
public class LogActionHandler implements ActionHandler {

  private static final Logger log = LoggerFactory.getLogger(LogActionHandler.class);

  @Override
  public String getActionType() {
    return "LOG";
  }

  @Override
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    String rawMessage = (String) config.get("message");
    String level = (String) config.getOrDefault("level", "INFO");

    String formattedMessage = formatMessage(rawMessage, context.getVariables());

    switch (level.toUpperCase()) {
      case "WARN" -> log.warn("[WORKFLOW LOG] {}", formattedMessage);
      case "ERROR" -> log.error("[WORKFLOW LOG] {}", formattedMessage);
      default -> log.info("[WORKFLOW LOG] {}", formattedMessage);
    }

    return ActionResult.success(Map.of("loggedMessage", formattedMessage));
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Log Message")
        .description("Outputs a message to the application log with variable interpolation.")
        .requiredConfigurationKeys(List.of("message"))
        .supportedParameters(
            Map.of("message", "The text content to log", "level", "Log level (INFO, WARN, ERROR)"))
        .build();
  }

  private String formatMessage(String message, Map<String, Object> variables) {
    if (message == null || variables == null || variables.isEmpty()) {
      return message;
    }
    String result = message;
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String placeholder = "{" + entry.getKey() + "}";
      if (result.contains(placeholder)) {
        result = result.replace(placeholder, String.valueOf(entry.getValue()));
      }
    }
    return result;
  }
}
