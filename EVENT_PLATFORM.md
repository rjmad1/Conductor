# Conductor Event Platform Specification

The Conductor Event Platform is a secure, high-performance, and tenant-isolated messaging backbone built on top of **NATS JetStream** and structured JSON Schema contract validation. It coordinates asynchronous messaging across all Conductor platform domains.

---

## 1. Architectural Overview

```
[ Domain Emitters ]  ──►  [ EventPublisher ]  ──►  [ Schema Validator ]
                                 │                      │
                                 ▼                      ▼
                            Inject MDC            Match JSON Schema
                         Correlation Headers      from config/schemas/
                                 │                      │
                                 ▼                      ▼
                         [ NATS JetStream ]  ◄── Enforce Tenant Subject
                                 │
                         [ EventConsumer ]  ──►  Verify Tenant Visibility
                                 │                      │
                        (Process Payload)               ▼
                         /             \            If Violation:
                        ▼               ▼        Drop Event & Log Audit
                    Success          Failure
                       │                │
                      msg.ack()    [Nak Retry] ──► [DLQ Routing (after 5)]
```

---

## 2. Platform Subprojects Layout

The event backbone is segregated into 5 specialized Gradle subprojects:
*   `shared:events-model`: Defines the canonical `ConductorEvent` envelope and metadata.
*   `shared:contracts`: Compiles and executes runtime JSON Schema validations.
*   `shared:messaging`: Encapsulates NATS connection pooling, Publisher/Subscriber middleware, retries, and isolation.
*   `platform:messaging-core`: Provides administrators with tools to provision JetStream streams and consumer groups.
*   `platform:events`: Database-backed Replay orchestrations, DLQ inspections, and registry monitoring REST APIs.

---

## 3. Streaming and Retention Strategy

All events map to logical NATS JetStream streams. The physical retention limits are enforced via Max Age limits:

| Stream Name | NATS Subjects Scoped | Storage Type | Retention Policy | Max Age Limit |
| :--- | :--- | :--- | :--- | :--- |
| `TENANT_STREAM` | `conductor.*.tenant.>` | File-based | Limits-based | 30 Days |
| `CUSTOMER_STREAM`| `conductor.*.customer.>`| File-based | Limits-based | 30 Days |
| `MESSAGING_STREAM`| `conductor.*.messaging.>`| File-based | Limits-based | 7 Days |
| `WORKFLOW_STREAM`| `conductor.*.workflow.>`| File-based | Limits-based | 14 Days |
| `INTEGRATION_STREAM`| `conductor.*.integration.>`| File-based | Limits-based | 7 Days |
| `ANALYTICS_STREAM`| `conductor.*.analytics.>`| File-based | Limits-based | 7 Days |
| `AUDIT_STREAM` | `conductor.*.audit.>` | File-based | Limits-based | 90 Days |
| `AI_STREAM` | `conductor.*.ai.>` | File-based | Limits-based | 7 Days |
| `DLQ_STREAM` | `dlq.>` | File-based | Limits-based | 30 Days |

---

## 4. Initializing Platform Streams

Streams are declared dynamically on startup via the `StreamManager` bean in `:platform:messaging-core`.
If a stream does not exist in the active NATS cluster, it will be automatically provisioned with limits-based retention and file-based storage.
