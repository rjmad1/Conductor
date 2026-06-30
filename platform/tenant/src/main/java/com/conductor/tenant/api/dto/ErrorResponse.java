package com.conductor.tenant.api.dto;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;
  private Instant timestamp;
  private Map<String, Object> extensions;
}
