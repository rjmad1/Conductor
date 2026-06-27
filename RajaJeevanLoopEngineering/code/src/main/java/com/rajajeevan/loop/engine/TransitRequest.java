package com.rajajeevan.loop.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Inbound payload for the POST /api/v1/loops/transit endpoint. */
@Data
@NoArgsConstructor
public class TransitRequest {

  @JsonProperty("loop_id")
  private String loopId;

  @JsonProperty("source_phase")
  private String sourcePhase;

  @JsonProperty("target_phase")
  private String targetPhase;

  private Map<String, Object> artifacts;

  @JsonProperty("execution_logs")
  private String executionLogs;
}
