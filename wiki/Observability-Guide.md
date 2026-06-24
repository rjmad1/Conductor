# Observability Guide

## A. Purpose
This page documents the metrics instrumentation, tracing configurations, and log exporters that provide operational observability for the Conductor platform.

---

## B. Platform Metrics (Spring Actuator & Micrometer)

Spring Boot services export system-wide performance indicators (JVM memory, GC pauses, database connection pools) on route `/actuator/prometheus` (port `8090` or `8085` depending on service bindings).

### Custom Business Metrics:
The platform registers custom metrics to monitor business processes in real time:

- **`workflow_executions_total`**:
  - *Type*: Counter
  - *Tags*: `tenant_id`, `definition_name`, `status` (`SUCCESS`/`FAILED`)
  - *Description*: Tracks total workflows processed by the Temporal engine.
- **`workflow_execution_duration_seconds`**:
  - *Type*: Timer (Histogram)
  - *Tags*: `tenant_id`, `definition_name`
  - *Description*: Measures total latency duration of workflow steps.
- **`nats_publisher_failures_total`**:
  - *Type*: Counter
  - *Tags*: `stream`, `subject`
  - *Description*: Captures NATS JetStream publish failures.
- **`whatsapp_opt_outs_total`**:
  - *Type*: Counter
  - *Tags*: `tenant_id`
  - *Description*: Measures STOP keyword updates in the consent ledger.

---

## C. Distributed Tracing (OpenTelemetry)
All microservice transactions are instrumented with OpenTelemetry tracing headers.

- **Trace Context Propagation**:
  - API Gateway (Kong) initializes the W3C trace context header `traceparent`.
  - Monolith services parse `traceparent` and export child traces to the OTel Collector container via gRPC (port `4317`).
  - Traces are stored in **Grafana Tempo** for visualization.

---

## D. Log Aggregation (Loki)
Application service log levels standard is JSON (governed by `ADR-GOV-006`).

- **JSON Format Structure**:
  Logs include correlation tags (`trace_id`, `span_id`, `tenant_id`) alongside standard severity keys (`level`, `message`, `timestamp`).
- **Loki Exporter**: Promtail / Loki tail log containers in Kubernetes and stream outputs to **Grafana Loki** on port `3100`.

---

## E. Related Pages
- [Operations Guide](Operations-Guide)
- [Troubleshooting Guide](Troubleshooting-Guide)
