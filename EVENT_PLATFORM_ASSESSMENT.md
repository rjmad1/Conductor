# Conductor Event Platform Assessment & Architectural Review

This document provides a comprehensive review of the current event, queue, and messaging infrastructure of the Conductor Platform. It outlines what to reuse, extend, wrap, refactor, and build to establish a secure, tenant-isolated, high-performance event backbone.

---

## 1. Assessment Matrix

| Target System / Component | Strategy | Description |
| :--- | :--- | :--- |
| **Tenant Context** | **Reuse** | Reuse `TenantContext` (thread-local tenant/user context propagation). |
| **Audit Logging** | **Reuse** | Reuse `AuditLogger` (centralized structured logging via `CONDUCTOR_AUDIT_LOG`). |
| **Event Dispatches** | **Refactor & Replace** | Refactor/Replace the naive `NatsEventPublisher` with a robust publishing framework. |
| **NATS Connectivity** | **Wrap & Extend** | Wrap NATS connection management with automatic reconnection and failover. |
| **Schema Governance** | **Build** | Build a Schema Registry, validator gateway, and contract testing framework. |
| **Tenant Isolation** | **Build** | Enforce tenant-isolated subjects, routing, filters, and replay limits. |
| **DLQ (Dead Letter)** | **Build** | Build poison-message handling, exponential backoff retries, and DLQ routing. |
| **Replay Framework** | **Build** | Build a tenant-scoped, auditable, and version-aware replay mechanism. |
| **Observability** | **Build** | Expose metrics, logs, traces, dashboards, and alerts via OTel. |

---

## 2. Technical Strategy

### 2.1 Reuse
* **Context Propagation**: We will reuse `TenantContext` to fetch current tenant and user IDs for embedding inside event envelopes.
* **Audit Tracing**: We will reuse `AuditLogger` to record schema breaches, replay attempts, and DLQ escalations.
* **Libraries**: Standard Spring Boot, JPA, Jackson, and `io.nats:jnats` library dependencies will be reused.

### 2.2 Extend
* **NATS client wrapper**: We will extend the standard NATS client configurations to configure JetStream streams, subjects, durable consumers, and consumer groups dynamically.
* **Flyway Migrations**: We will extend the database schema with tables for the Schema Registry, replay audit log, and DLQ audit log.

### 2.3 Wrap
* **NATS Connection Management**: We will wrap NATS connection bootstrapping with health metrics and state management (online/offline handling).
* **OpenTelemetry Tracing**: We will wrap event publication and ingestion with trace headers (`traceparent`) to automatically link causation and correlation IDs across services.

### 2.4 Refactor
* **Naive Publishing**: Currently, `NatsEventPublisher` publishes raw strings directly. We will refactor this to:
  1. Wrap payloads in `ConductorEvent` envelopes.
  2. Perform validation against the schema registry.
  3. Ensure at-least-once delivery semantics via publisher confirmations.
  4. Trace correlation/causation metadata.

### 2.5 Build
* **`shared/events-model`**: Bounded context event models, canonical metadata envelope (`ConductorEvent`), and core event definitions.
* **`shared/contracts`**: JSON schema compilation, validation logic, and CI schema testing tasks.
* **`shared/messaging`**: At-least-once consumer loop, backoff retry policy, DLQ route publisher, and thread-safe JetStream operations.
* **`platform/events`**: The core events administration service implementing Schema Registry APIs, Replay execution controllers, DLQ management dashboard, and event archival retrieval.

---

## 3. Core Recommendations

1. **Subproject Layout**: Register `shared:events`, `shared:contracts`, `shared:messaging`, `platform:events`, and `platform:messaging-core` as modular Gradle projects to maintain strict domain segregation.
2. **Dynamic Stream Provisioning**: Use an initializer bean to bootstrap and declare JetStream streams (`CUSTOMER_STREAM`, `WORKFLOW_STREAM`, etc.) with their respective subjects and retention policies on startup if they don't already exist.
3. **Double-Layer Tenant Validation**: Validate tenant scope both at the gateway (HTTP ingress) and at the subscriber level (matching payload `tenantId` with active subscription parameters).
4. **Local Testing with Testcontainers**: Add NATS Testcontainers in integration tests to validate stream creation, consumer groups, replay, and DLQ operations under realistic conditions.
