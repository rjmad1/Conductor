# Event Governance Specification — Conductor Platform

This specification defines the rules, patterns, naming conventions, and lifecycle policies for all asynchronous event communications across the Conductor Platform.

---

## 1. Event Naming Convention

All events published to the NATS JetStream event bus must strictly follow the prefix structure below:

$$\text{Subject Format: } \texttt{conductor.\{tenantId\}.\{domain\}.\{entity\}.\{action\}}$$

### Segment Definitions
*   `conductor`: Shared platform prefix (constant).
*   `{tenantId}`: The unique identifier of the tenant. Helps with partition-level routing.
*   `{domain}`: The origin bounded context (e.g. `customer`, `messaging`, `workflow`).
*   `{entity}`: The primary object subject (e.g. `contact`, `message`, `execution`).
*   `{action}`: The state transition event (e.g. `created`, `updated`, `opt_out`, `failed`).

### Examples
*   `conductor.tenant-123.customer.contact.opt_out`
*   `conductor.tenant-abc.messaging.message.status_updated`

---

## 2. Event Payload Schema Wrapper

Every event payload must be wrapped in a standard JSON metadata envelope:

```json
{
  "eventId": "uuid-v4-string",
  "eventType": "conductor.customer.contact.opt_out",
  "specVersion": "1.0.0",
  "source": "/platform/customer-service",
  "tenantId": "tenant-123-uuid",
  "userId": "user-456-uuid",
  "timestamp": "2026-06-24T17:55:00.000Z",
  "correlationId": "trace-id-uuid-from-otel",
  "data": {
    "contactId": "customer-789-uuid",
    "channel": "whatsapp",
    "timestamp": "2026-06-24T17:54:55.000Z"
  }
}
```

---

## 3. Versioning & Compatibility Strategy

To ensure seamless multi-version compatibility:
1.  **SemVer Mapping:** Event payload schemas are versioned using Semantic Versioning (SemVer) mapped in the `specVersion` metadata key.
2.  **Backwards Compatibility (Mandatory):**
    *   *Additive updates only:* You may append new fields but must not delete existing fields or change their data types.
    *   *Defaulting:* All new fields in a schema update must be optional or have a default value defined.
3.  **Breaking Changes Lifecycle:** If a breaking change is required, a new event type suffix must be defined (e.g., `conductor.customer.contact.opt_out_v2`). The old consumer handlers must remain active for a 3-month transition grace period.

---

## 4. Schema Governance

1.  **Registry Store:** All schemas are written as declarative JSON Schemas in `config/schemas/` directory.
2.  **CI Validation:** Pull requests altering event emitters are scanned by CI to lint schemas against the target consumer mapping schemas.
3.  **Application Ingestion Checks:** Spring Boot event handlers validate incoming message data against registered schemas at validation gateways before routing to service logic.

---

## 5. Stream Retention Rules

NATS JetStream stream definitions enforce physical limits to prevent system memory and disk exhaustion:

| Stream Name | Subjects Scoped | Storage Type | Retention Policy | Max Age Limit |
| :--- | :--- | :--- | :--- | :--- |
| `CUSTOMER_STREAM` | `conductor.*.customer.>` | File-based | Limits-based | 30 Days |
| `MESSAGING_STREAM` | `conductor.*.messaging.>` | File-based | Limits-based | 7 Days |
| `WORKFLOW_STREAM` | `conductor.*.workflow.>` | File-based | Limits-based | 14 Days |
| `INTEGRATION_STREAM`| `conductor.*.integration.>`| File-based | Limits-based | 7 Days |

*   *Replication Factor:* Run 3-node NATS clustering in production for high availability.

---

## 6. Dead Letter Queue (DLQ) Policy

If a consumer repeatedly fails to process an event:
1.  **Retry Limit:** Consume attempts are capped at 5 retries using exponential backoff (e.g., 2s, 4s, 8s, 16s, 32s).
2.  **DLQ Route:** If all retries fail, the event is acknowledged in the main stream and pushed to the DLQ subject:
    $$\texttt{dlq.\{tenantId\}.\{stream\}.\{consumer\}}$$
3.  **DLQ Wrapper:** The payload is wrapped with execution context details, including:
    *   `x-retries`: 5
    *   `x-error-message`: Original system error stack trace.
    *   `x-dead-letter-timestamp`: Date of DLQ route.

---

## 7. Replay Rules

1.  **Consumer Offsets:** All event consumers must connect with explicit durable names. This allows NATS to track offsets across worker restarts.
2.  **History Replay:** If an integration crashes or needs to reconstruct state, a consumer can initiate a replay from:
    *   A specific sequence number.
    *   A historical timestamp.
    *   The beginning of the stream history limit.
