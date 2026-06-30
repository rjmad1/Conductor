# Dead Letter Queue (DLQ) Operational Guide

This document describes poison message handling, retries, escalations, and recovery operations in the Conductor Event Platform.

---

## 1. Poison Message & Retry Policies

When a consumer handler throws an exception during event execution:
1.  **Exponential Backoff**: The message is NAK'ed with an increasing delay:
    *   Attempt 1: 2 seconds
    *   Attempt 2: 4 seconds
    *   Attempt 3: 8 seconds
    *   Attempt 4: 16 seconds
    *   Attempt 5: 32 seconds
2.  **DLQ Route**: If the message fails 5 times, it is acknowledged on the main stream (preventing blocking) and routed to the DLQ subject:
    `dlq.{tenantId}.{stream}.{consumer}`
3.  **Audit Recording**: The event payload is wrapped in a DLQ envelope containing original event metadata and system error stack traces, and is saved in the database `dlq_records` table.

---

## 2. Inspecting DLQ Records

Retrieve all pending dead-lettered messages for the active tenant:

`GET /api/v1/events/dlq`

---

## 3. Resolving and Replaying DLQ Events

### Replay and Recover

Once the downstream consumer bug is patched, request recovery to re-deliver the event payload to the consumer stream:

`POST /api/v1/events/dlq/{id}/replay`

### Discard

If the message is an invalid duplicate that should be ignored:

`POST /api/v1/events/dlq/{id}/discard`
