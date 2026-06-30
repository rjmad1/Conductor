# Event Replay Guide

This guide details how to request a historical replay of events in the Conductor Event Platform.

---

## 1. Security & Scoping Constraints

*   **Tenant Isolation**: Replay operations are strictly tenant-scoped. The NATS subscription filter is mapped dynamically to `conductor.{tenantId}.>` to guarantee that a tenant cannot fetch or view events belonging to other tenants.
*   **Auditing**: Every replay request is logged to the `replay_audit_logs` table in the database, tracking the requesting username, target stream, durable consumer name, offset starting values, and execution timestamp.

---

## 2. Requesting a Replay

Replays are triggered using the REST API:

`POST /api/v1/events/replay`

### Request Parameters:

*   `stream`: The target JetStream stream name (e.g. `CUSTOMER_STREAM`).
*   `consumer`: The target durable consumer name.
*   `replayType`:
    *   `SEQUENCE`: Starts replay from a NATS sequence number.
    *   `TIMESTAMP`: Starts replay from an ISO-8601 timestamp.
    *   `ALL`: Streams the entire history available in the retention limit.
*   `startValue`: The sequence integer (e.g., `45`) or timestamp (e.g., `2026-06-24T18:00:00.000Z`).

### Example Curl:

```bash
curl -X POST "http://localhost:8000/api/v1/events/replay" \
     -H "X-Tenant-ID: tenant-123-uuid" \
     -H "X-User-ID: admin-user-uuid" \
     --data-urlencode "stream=CUSTOMER_STREAM" \
     --data-urlencode "consumer=tenant_123_customer_contact_opt_out" \
     --data-urlencode "replayType=SEQUENCE" \
     --data-urlencode "startValue=1"
```

The endpoint will return the array of replayed event envelopes.
