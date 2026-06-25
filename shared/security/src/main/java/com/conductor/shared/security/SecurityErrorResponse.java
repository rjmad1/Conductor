package com.conductor.shared.security;

import java.time.Instant;
import java.util.Map;

/** Standard RFC 7807 Problem Details representation for security failures. */
public record SecurityErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Instant timestamp,
    Map<String, Object> extensions) {}
