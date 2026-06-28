# Conductor Event Model Specification

Every event published to the Conductor Event Platform must comply with the canonical event metadata envelope wrapper (`ConductorEvent`). This model enforces immutability, versioning, tenant scoping, and traceability.

---

## 1. Canonical Event Metadata Schema

Every event is wrapped in the following JSON structure:

```json
{
  "eventId": "uuid-v4-string",
  "eventVersion": "1.0.0",
  "eventType": "conductor.tenant.profile.created",
  "tenantId": "tenant-uuid-string",
  "correlationId": "traceparent-correlation-id",
  "causationId": "parent-event-id",
  "source": "/platform/tenant-service",
  "timestamp": "2026-06-24T18:00:00.000Z",
  "producer": "tenant-service-instance-1",
  "schemaVersion": "v1",
  "payload": {
    "id": "tenant-uuid-string",
    "name": "Acme Corp",
    "domain": "acme.com"
  }
}
```

---

## 2. Envelope Fields Reference

*   `eventId` (UUID): A unique identifier generated per event for deduplication and correlation.
*   `eventVersion` (String): The semantic version of the event structure (default `1.0.0`).
*   `eventType` (String): The qualified dot-separated subject path: `conductor.{domain}.{entity}.{action}`.
*   `tenantId` (String): The tenant identifier. Crucial for Logical Isolation and Routing.
*   `correlationId` (String): Tracks origin request headers (`requestId` / `traceparent`) propagated through W3C standards.
*   `causationId` (String): Identifies the parent event ID that triggered this downstream action.
*   `source` (String): The service boundary path originating the event.
*   `timestamp` (Instant): ISO-8601 timestamp of publication.
*   `producer` (String): System runtime host/instance name.
*   `schemaVersion` (String): e.g. `v1`, `v2` representing the JSON Schema version.
*   `payload` (JSON): Bounded context event payload object containing fields validated by the schema registry.
