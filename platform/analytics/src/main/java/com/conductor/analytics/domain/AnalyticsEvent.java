package com.conductor.analytics.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Lightweight DTO mapping a ConductorEvent into a ClickHouse-insertable row. */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class AnalyticsEvent {

  private final UUID eventId;
  private final String eventType;
  private final String tenantId;
  private final String domain;
  private final String entity;
  private final String action;
  private final String correlationId;
  private final String source;
  private final String payload;
  private final Instant createdAt;
}
