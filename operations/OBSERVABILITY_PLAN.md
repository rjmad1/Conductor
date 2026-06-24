# Observability Plan — Conductor

This document details the observability architecture, telemetry schemas, dashboards, and alerting rules for the Conductor platform MVP.

---

## 1. Structured Logging

To ensure quick debugging and trace extraction, all services must output structured JSON logs to standard output. 

### Log JSON Schema:
```json
{
  "timestamp": "2026-06-24T12:00:00.000Z",
  "level": "INFO",
  "service": "conductor-monolith",
  "trace_id": "8a3d4f826c91e0a2",
  "span_id": "01af34e9bc20718d",
  "tenant_id": "3a033f27-c10a-4712-ba26-47b2c554af68",
  "user_id": "4e7a2b91-49b2-4d2c-80a1-77d612e5f31a",
  "class": "com.conductor.messaging.OutboundDispatcher",
  "message": "Successfully dispatched template message to Meta Graph API",
  "payload": {
    "message_id": "msg_90123efd",
    "recipient_phone": "+919999999999",
    "template_name": "payment_reminder"
  }
}
```

### Logging Guidelines:
*   **Trace Context Injection:** The log appender must extract the active OpenTelemetry Trace ID and inject it into the `trace_id` field.
*   **PII Masking:** Logs must pass through a regex masking filter that strips values for keys like `phone`, `email`, and `name` to maintain compliance.

---

## 2. Core Metrics Inventory (Prometheus Format)

### Platform Metrics:
*   `conductor_platform_http_requests_total{method, path, status}`: Total HTTP requests.
*   `conductor_platform_jvm_memory_used_bytes`: JVM memory usage.
*   `conductor_platform_db_connections_active`: Active PostgreSQL database connections.

### Workflow Metrics:
*   `conductor_workflow_executions_total{workflow_id, tenant_id, status}`: Total runs.
*   `conductor_workflow_execution_duration_seconds{workflow_id}`: Time to complete a workflow.
*   `conductor_workflow_step_failures_total{step_id, error_type}`: Total failures per step.

### Messaging Metrics:
*   `conductor_messages_dispatched_total{tenant_id, direction, status}`: Message counter.
*   `conductor_messages_delivery_latency_seconds`: Time elapsed from NATS dispatch to Meta callback arrival.
*   `conductor_whatsapp_opt_outs_total{tenant_id}`: Counter for "STOP" keyword opt-outs.

### Integration Metrics:
*   `conductor_integrations_webhook_ingress_total{source, status}`: Inbound webhooks.
*   `conductor_integrations_egress_requests_total{destination, status}`: Outbound API calls.
*   `conductor_integrations_squid_blocked_requests_total`: Counts of blocked SSRF requests.

---

## 3. Distributed Tracing (OpenTelemetry)

Distributed tracing tracks requests across the entire execution flow:

```
[Client Webhook / App Dashboard]
            |
            v
   [Kong API Gateway] (Generates traceparent header W3C format)
            |
            v
[Node.js WhatsApp Adapter / Spring Monolith] (Extracts trace context and creates child spans)
            |
            +--> [NATS JetStream] (Propagates trace headers inside message metadata)
            |
            +--> [Temporal Worker] (Injects trace token to activities)
```

To ensure trace integrity, every NATS publisher must append the active OTel context to header metadata, and every consumer must extract it before starting downstream operations.

---

## 4. Dashboards Layout

We define 5 core Grafana dashboards to monitor system health:

### 1. Platform Health Dashboard
*   **Widgets:**
    *   CPU & Memory Utilization gauges for Spring Monolith and Node.js containers.
    *   PostgreSQL active/idle connection pool graphs.
    *   NATS JetStream disk and RAM storage usage metrics.
    *   Kong API Gateway average latency and error rates (5xx vs 4xx).

### 2. Workflow Health Dashboard
*   **Widgets:**
    *   Temporal active workflows counter.
    *   Workflow completion vs. failure ratios.
    *   Temporal Task Queue latency (scheduler delays).
    *   Step execution duration heatmaps.

### 3. Messaging Health Dashboard
*   **Widgets:**
    *   Outbound messages throughput (messages per second).
    *   Deliverability Funnel: Sent -> Delivered -> Read -> Failed.
    *   WhatsApp webhook processing queue depths.
    *   Failure reasons classification table (e.g. "invalid number", "template mismatch").

### 4. Integration Health Dashboard
*   **Widgets:**
    *   Shopify, Zoho CRM, and Razorpay webhook ingress rates.
    *   Squid proxy egress volume and blocked request logs.
    *   Outbound request latency to external APIs.
    *   Webhooks failure and retry queue stats.

### 5. Tenant Health Dashboard
*   **Widgets:**
    *   Active tenant count.
    *   Usage quota meters (messages sent vs plan limits).
    *   Billing subscription trends (active vs past due).
    *   Opt-out rate tracking per tenant workspace.

---

## 5. Alerts Specifications

We configure Prometheus Alertmanager rules to notify channels when criteria are met:

| Alert Name | Condition | Severity | Channel | Recovery Action |
| :--- | :--- | :--- | :--- | :--- |
| `WhatsAppWebhookLagHigh` | `sum(nats_consumer_lag{subject="whatsapp.inbound.*"}) > 5000` for 2m | CRITICAL | Slack / PagerDuty | Scale Node.js adapter replica count. |
| `TemporalTaskQueueDelay` | `temporal_task_schedule_to_start_latency > 10s` for 5m | WARNING | Slack | Inspect Temporal worker health. |
| `EgressProxyFailure` | `rate(conductor_integrations_squid_blocked_requests_total[5m]) > 20` | WARNING | Security Slack | Inspect logs for potential SSRF probes. |
| `TenantQuotaExceeded` | `usage_records_metric_count >= plan_metric_limit` | INFO | Billing System | Automatically send plan upgrade notification. |
| `DatabaseWriteLatencyHigh`| `postgresql_transaction_duration_seconds{qt="write"} > 2.5s` for 3m | CRITICAL | PagerDuty | Inspect PostgreSQL query locks and CPU load. |
