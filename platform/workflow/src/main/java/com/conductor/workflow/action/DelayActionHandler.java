package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback handler for DELAY actions. The primary workflow execution of delay steps is intercepted
 * and managed by the Temporal workflow engine via Workflow.sleep.
 */
@Component
public class DelayActionHandler implements ActionHandler {

  private static final Logger log = LoggerFactory.getLogger(DelayActionHandler.class);

  @Override
  public String getActionType() {
    return "DELAY";
  }

  @Override
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    String durationStr = (String) config.getOrDefault("duration", "PT1M");

    log.info("Delay action handler executed: duration={}", durationStr);
    try {
      Duration.parse(durationStr);
      return ActionResult.success(Map.of("status", "delayed", "duration", durationStr));
    } catch (Exception e) {
      return ActionResult.failure(
          "INVALID_DURATION", "Failed to parse ISO-8601 duration: " + durationStr);
    }
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Delay Workflow")
        .description("Suspends execution for a specified ISO-8601 duration.")
        .supportedParameters(Map.of("duration", "ISO-8601 duration string (e.g. PT1M, PT5S)"))
        .build();
  }
}
