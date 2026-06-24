# Observability Foundation Specification — Conductor Platform

This specification defines the logs, metrics, traces, alerting policies, and dashboard layouts required to monitor the Conductor Platform.

---

## 1. Instrumentation Framework
Observability is driven by OpenTelemetry (OTel) standards, scraping metrics using Prometheus, aggregating logs via Loki, and compiling traces via Tempo/Jaeger.

```
+--------------------------------------------------------------+
|                       Application Pod                        |
|                                                              |
|   [ OTel SDK ] ───(OTLP/gRPC)───► [ OTel Collector Pod ]      |
|                                         │                    |
+--------------------------------------------------------------+
                                          │
                  ┌───────────────────────┼───────────────────────┐
                  ▼ (Metrics)             ▼ (Logs)                ▼ (Traces)
          ┌───────────────┐       ┌───────────────┐       ┌───────────────┐
          │  Prometheus   │       │ Grafana Loki  │       │ Tempo/Jaeger  │
          └───────────────┘       └───────────────┘       └───────────────┘
```

---

## 2. Core Telemetry Signals

### Logs (Format: Structured JSON)
All services must print logs to standard output in structured JSON format with tracing context identifiers injected:
```json
{
  "timestamp": "2026-06-24T17:45:32.001Z",
  "level": "INFO",
  "service.name": "conductor-workflow-engine",
  "trace_id": "4b68e980327f311c875d71cde2f2115e",
  "span_id": "7bf311c875d71cde",
  "tenant.id": "tenant-abc-123",
  "message": "Workflow task completed successfully",
  "workflow.type": "CRM_Sync",
  "execution.time_ms": 142
}
```

### Metrics (Format: Prometheus Exposition)
*   **System Metrics:** CPU, Memory, Disk, Network I/O.
*   **Application Metrics:** HTTP request duration histogram (`http_server_duration_milliseconds`), active connection gauges, database connection pool statistics.
*   **Ecosystem Specifics:** Temporal active workflows count, NATS message publish rates, Redis hit/miss ratios.

### Traces (Format: OTLP v1)
*   Capture distributed trace spans across network calls (Kong Gateway -> Spring Boot Core -> Temporal Server -> Spring Boot Worker -> Qdrant -> PostgreSQL).
*   Enforce trace propagation using W3C Trace Context headers (`traceparent`, `tracestate`).

---

## 3. Required Monitor Dashboards

### Dashboard A: Platform Health
*   **Focus:** Global resource availability and network state.
*   **Key Panels:** CPU usage per namespace, RAM utilization limits, Kong Gateway proxy response times (p95, p99), PostgreSQL database active connections, Redis cache eviction rates.

### Dashboard B: Workflow Health
*   **Focus:** State of Temporal, Camunda, and Kestra execution engines.
*   **Key Panels:** Total active/completed/failed workflows count, workflow execution duration histogram, Temporal worker poll latency, scheduled queue delays, Camunda user tasks queue size.

### Dashboard C: Messaging Health
*   **Focus:** Throughput and backlog of NATS JetStream and Kafka/Redpanda.
*   **Key Panels:** Message ingestion rates (messages/sec), messaging bytes throughput, consumer lag gauge per consumer group, NATS JetStream file-store limits, Redpanda partition counts.

### Dashboard D: Integration Health
*   **Focus:** Success metrics of Activepieces, n8n, and Windmill integrations.
*   **Key Panels:** Executed connector requests, integration workflow success/failure rates, runner sandbox execution durations, script compiler cache hit rates, egress firewall network blockage counts.

### Dashboard E: AI Health
*   **Focus:** AI engines (Dify, LangGraph, OpenWebUI, LiteLLM) and vector indexes (Qdrant, Weaviate).
*   **Key Panels:** LLM API token consumption counts, LiteLLM request routing latency, model execution errors, vector database search latency (p95), HNSW index rebuild durations.

### Dashboard F: Tenant Health
*   **Focus:** Multi-tenant usage and isolation validation.
*   **Key Panels:** Active sessions count per tenant, API requests per second grouped by `tenant_id`, database storage utilization per tenant, vector search query allocations, resource usage quotas.

---

## 4. Alerting Policy Rules (Prometheus Alertmanager)

*   **Alert Rule 1: HostOutofMemory**
    *   *Expression:* `node_memory_Active_bytes / node_memory_MemTotal_bytes * 100 > 90`
    *   *Severity:* Critical
    *   *Description:* System RAM utilization exceeds 90% for 5 minutes.
*   **Alert Rule 2: DatabaseConnectionSaturation**
    *   *Expression:* `pg_stat_database_numbackends / pg_settings_max_connections * 100 > 85`
    *   *Severity:* Warning
    *   *Description:* PostgreSQL active connection usage exceeds 85% of pool limits.
*   **Alert Rule 3: WorkflowExecutionFailures**
    *   *Expression:* `rate(temporal_workflow_failed_total[5m]) > 2`
    *   *Severity:* Critical
    *   *Description:* More than 2 Temporal workflows failing per second.
*   **Alert Rule 4: TenantQuotaExceeded**
    *   *Expression:* `rate(conductor_tenant_request_throttled_total[1m]) > 5`
    *   *Severity:* Warning
    *   *Description:* Tenant has exceeded API rate limit constraints.

---

## 5. Verification and Recommendations Metadata
*   **Confidence Level:** High (Metrics, logs, and trace structures conform to OpenTelemetry specifications)
*   **Evidence Completeness:** 100% (All 6 requested dashboards and core alerting rules defined)
*   **Validation Gaps:** Minor (Dashboard templates require runtime JSON import and adjustments to query variables matching actual namespace naming)
*   **Assumptions:** Assumed the telemetry collection plane is running OpenTelemetry Collector and exporting to a shared Prometheus/Loki/Tempo engine.
