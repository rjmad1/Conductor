package com.conductor.shared.execution.action;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Result returned by an action execution. Contains success status, output variables, error code and
 * message, and optional execution metadata.
 */
@Value
@Builder
public class ActionResult {
  boolean success;
  @Builder.Default Map<String, Object> outputVariables = new HashMap<>();
  String errorCode;
  String errorMessage;
  @Builder.Default Map<String, Object> metadata = new HashMap<>();

  public static ActionResult success(Map<String, Object> outputVariables) {
    return ActionResult.builder().success(true).outputVariables(outputVariables).build();
  }

  public static ActionResult success(
      Map<String, Object> outputVariables, Map<String, Object> metadata) {
    return ActionResult.builder()
        .success(true)
        .outputVariables(outputVariables)
        .metadata(metadata)
        .build();
  }

  public static ActionResult failure(String errorCode, String errorMessage) {
    return ActionResult.builder()
        .success(false)
        .errorCode(errorCode)
        .errorMessage(errorMessage)
        .build();
  }
}
