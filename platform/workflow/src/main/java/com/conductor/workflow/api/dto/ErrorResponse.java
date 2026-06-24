package com.conductor.workflow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * RFC 7807 Problem Details error response per API_GOVERNANCE.md.
 */
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
