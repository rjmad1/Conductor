# Event Contracts Specification

Every asynchronous payload published to NATS JetStream is governed by a versioned JSON Schema contract. This ensures backwards compatibility and prevents breaking changes from propagating.

---

## 1. Schema Validation Gateway

Both publishing and consumption actions execute validation checks at ingestion and egress:
*   **Egress validation**: `EventPublisher` validates event payloads against the registry prior to publishing to prevent sending invalid data.
*   **Ingress validation**: `EventConsumer` validates received payloads. If an invalid or unparseable payload escapes, it is marked as a poison message and routed immediately to the Dead Letter Queue (DLQ).

---

## 2. Dynamic Schema Directory

Schemas are defined in standard Draft-07 JSON Schema templates saved inside:
`config/schemas/{domain}.{entity}.{action}.v{version}.json`

### Supported Core Event Contracts:

*   **Tenant Profile Created (`v1`)**: [tenant.profile.created.v1.json](file:///c:/Users/rajaj/Projects/Conductor/config/schemas/tenant.profile.created.v1.json)
*   **Tenant Profile Updated (`v1`)**: [tenant.profile.updated.v1.json](file:///c:/Users/rajaj/Projects/Conductor/config/schemas/tenant.profile.updated.v1.json)
*   **Identity User Created (`v1`)**: [identity.user.created.v1.json](file:///c:/Users/rajaj/Projects/Conductor/config/schemas/identity.user.created.v1.json)
*   **Identity Role Assigned (`v1`)**: [identity.role.assigned.v1.json](file:///c:/Users/rajaj/Projects/Conductor/config/schemas/identity.role.assigned.v1.json)

---

## 3. Schema Example (tenant.profile.created)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "TenantCreatedPayload",
  "type": "object",
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "name": { "type": "string", "minLength": 1 },
    "domain": { "type": "string", "minLength": 1 }
  },
  "required": ["id", "name", "domain"],
  "additionalProperties": false
}
```
