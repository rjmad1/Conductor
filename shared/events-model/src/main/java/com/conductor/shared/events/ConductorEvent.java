package com.conductor.shared.events;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ConductorEvent<T> {
  private final UUID eventId;
  private final String eventVersion;
  private final String eventType;
  private final String tenantId;
  private final String correlationId;
  private final String causationId;
  private final String source;
  private final Instant timestamp;
  private final String producer;
  private final String schemaVersion;
  private final T payload;
}
