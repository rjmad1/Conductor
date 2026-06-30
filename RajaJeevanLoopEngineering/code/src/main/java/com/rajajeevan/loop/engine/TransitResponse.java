package com.rajajeevan.loop.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Outbound payload returned by the transit and status endpoints. */
@Data
@Builder
public class TransitResponse {

  @JsonProperty("transition_allowed")
  private boolean transitionAllowed;

  @JsonProperty("current_state")
  private String currentState;

  @JsonProperty("next_steps")
  private List<String> nextSteps;

  @JsonProperty("circuit_breaker")
  private Map<String, Object> circuitBreaker;

  private String message;
}
