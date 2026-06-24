# ADR-GOV-006: Logging Standard

## Status
ACCEPTED

## Context
Diagnosing errors in distributed systems (like a workflow execution platform using Temporal and NATS) is extremely difficult without structured, traceable logs. Additionally, logging PII (like customer phone numbers or message content) violates DPDP and GDPR.

## Decision
We enforce the following logging policies:
1.  **Format**: All application logs must be structured as JSON in production.
2.  **PII Protection**: Logging customer phone numbers, names, email addresses, or raw message payloads is strictly prohibited. Logs must only contain system identifiers (e.g., `customerId`, `tenantId`, `workflowId`).
3.  **Correlation IDs**: Every request must carry a unique `request_id` or `trace_id` propagated across HTTP headers, NATS event envelopes, and Temporal workflows.
4.  **Log Levels**:
    *   `ERROR` for unexpected issues requiring alert notification.
    *   `WARN` for rule violations or recoverable errors.
    *   `INFO` for business events and flow transitions.

## Rationale
*   **Traceability**: Enables correlation of a single customer trigger across multiple microservices.
*   **Security & Privacy**: Avoids logging sensitive data which would lead to compliance audits.

## Consequences
*   Logs must be ingested into a centralized platform (Elasticsearch/Loki).
*   Any PR code reviews must verify that PII is not leaked into logs.
