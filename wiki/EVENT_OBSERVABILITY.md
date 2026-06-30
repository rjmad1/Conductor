# Event Platform Observability Specification

The Conductor Event Platform exposes metrics, traces, and structured JSON logs to standard OTel Collector gateways.

---

## 1. Metrics Reference

The platform registers the following core Micrometer metrics:

### 1.1 `event.published.count`
*   **Type**: Counter
*   **Description**: Total number of events successfully published.
*   **Tags**: `domain`, `entity`, `action`, `tenantId`, `status` (`SUCCESS` / `FAILURE`).

### 1.2 `event.consumed.count`
*   **Type**: Counter
*   **Description**: Total number of events consumed.
*   **Tags**: `domain`, `entity`, `action`, `tenantId`, `status` (`SUCCESS` / `FAILURE`).

### 1.3 `event.dlq.escalation.count`
*   **Type**: Counter
*   **Description**: Number of poison messages routed to the DLQ.
*   **Tags**: `domain`, `entity`, `action`, `tenantId`, `reason`.

### 1.4 `event.publish.latency` / `event.consume.latency`
*   **Type**: Timer
*   **Description**: Execution duration of publish and consume operations.
*   **Tags**: `domain`, `entity`, `action`, `tenantId`.

---

## 2. Distributed Tracing Propagation

Every publisher extracts OpenTelemetry/MDC correlation identifiers (`requestId` / `traceparent`) and injects them into the standard metadata wrapper:
*   `correlationId`: Matches the initial request trace identifier.
*   `causationId`: Matches the immediate parent message execution span.

Subscribers automatically extract these headers on message arrival, ensuring complete trace graphs across microservices in Grafana Tempo.
