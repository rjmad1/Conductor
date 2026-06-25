package com.conductor.customer.domain;

import com.conductor.shared.customer.TimelineEventType;
import com.conductor.shared.middleware.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Append-only chronological event log for a customer — the Customer 360 timeline.
 *
 * <p>Records all significant events in the customer lifecycle: creation, messages, consent changes,
 * tag assignments, segment memberships, workflow executions, integration activity, and profile
 * updates.
 *
 * <p>Timeline entries are NEVER modified or deleted. eventSource identifies which domain service
 * recorded the entry. metadata (JSONB) carries event-specific context (e.g. message ID, workflow
 * run ID).
 */
@Entity
@Table(
    name = "customer_timeline",
    indexes = {
      @Index(
          name = "idx_timeline_customer_occurred",
          columnList = "tenant_id, customer_id, occurred_at"),
      @Index(
          name = "idx_timeline_customer_event_type",
          columnList = "tenant_id, customer_id, event_type")
    })
@Getter
@Setter
public class CustomerTimeline extends TenantAwareEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private TimelineEventType eventType;

  /** Service or component that recorded this entry (e.g. customer-service, messaging-service). */
  @Column(name = "event_source")
  private String eventSource;

  /** Human-readable one-liner for the timeline UI. */
  @Column(name = "summary")
  private String summary;

  /** Event-specific context: IDs, amounts, references, etc. */
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt = Instant.now();
}
