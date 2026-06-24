# Monitoring & Observability — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Observability Stack

| Layer | Tool | Purpose |
|---|---|---|
| Metrics | Prometheus | Scrape metrics from all services |
| Dashboards | Grafana | Visualization, alerting |
| Application logs | ELK or CloudWatch Logs | Structured log search |
| Distributed tracing | OpenTelemetry + Jaeger | Request trace across services |
| Error tracking | Sentry | Exception capture, error grouping |
| Uptime monitoring | Better Uptime / Pingdom | External availability monitoring |
| Alert routing | PagerDuty | On-call escalation (Phase 2) |

---

## Service Health Endpoints

Every service MUST expose:
```
GET /health           → { status: "UP", components: { db: "UP", redis: "UP" } }
GET /health/live      → 200 if process is alive
GET /health/ready     → 200 if service is ready to serve traffic
GET /metrics          → Prometheus metrics endpoint (Micrometer)
```

Kong health check: `/status` → 200

---

## Key Metrics — Definitions

### Platform-Level Metrics

| Metric | Type | Alert Threshold | Description |
|---|---|---|---|
| `conductor_messages_total{status}` | Counter | - | Total messages by status (sent/delivered/failed) |
| `conductor_message_failure_rate` | Gauge | > 5% | % of messages failing delivery |
| `conductor_workflows_executed_total` | Counter | - | Total workflow executions |
| `conductor_workflow_failure_rate` | Gauge | > 2% | % of workflow executions failing |
| `conductor_active_tenants` | Gauge | - | Tenants with at least 1 event in last 24h |
| `conductor_api_requests_total{endpoint, status}` | Counter | - | API request count by endpoint and status |
| `conductor_api_latency_p95{endpoint}` | Histogram | > 500ms | API response time 95th percentile |

### WhatsApp-Specific Metrics

| Metric | Alert Threshold | Description |
|---|---|---|
| `wa_outbound_success_rate` | < 90% → P1 | Meta API call success rate |
| `wa_api_latency_p95` | > 3s → WARN | Meta API response time |
| `wa_rate_limit_hits_total` | Spike → WARN | Rate limit encounters |
| `wa_webhook_processing_time_p95` | > 5s → P1 | Inbound webhook processing time |

### Infrastructure Metrics

| Metric | Alert Threshold | Description |
|---|---|---|
| `container_cpu_usage` | > 80% → WARN, > 95% → P1 | Per-service CPU utilization |
| `container_memory_usage` | > 85% → WARN | Per-service memory utilization |
| `db_connections_active` | > 150 → WARN, > 180 → P1 | PostgreSQL connection count |
| `db_query_latency_p99` | > 1s → WARN | Slow query indicator |
| `redis_memory_used` | > 80% → WARN | Redis memory pressure |
| `nats_pending_messages` | > 10,000 → WARN | Event bus backlog |

---

## Grafana Dashboards

### Dashboard 1: Platform Overview
**Audience:** Engineering, Product, CTO  
**Refresh:** 1 minute

Panels:
- Messages sent/delivered/failed (last 24h, time series)
- Workflow execution success rate (gauge + time series)
- Active tenants (last 24h)
- API error rate by service
- Current on-call engineer

### Dashboard 2: WhatsApp Health
**Audience:** Engineering on-call  
**Refresh:** 30 seconds

Panels:
- Meta API success rate (real-time gauge)
- Messages per minute (rate)
- Inbound webhook processing time (histogram)
- Rate limit incidents
- Failed deliveries by error code (table)

### Dashboard 3: Tenant Operations
**Audience:** Customer Success  
**Refresh:** 5 minutes

Panels:
- Top tenants by message volume
- Trial tenants approaching limit (list)
- Tenants with elevated failure rate (list, action item)
- Workflow activation trend

### Dashboard 4: Infrastructure Health
**Audience:** DevOps/SRE  
**Refresh:** 30 seconds

Panels:
- CPU/memory per service (heatmap)
- Database connections and query latency
- Redis memory and cache hit rate
- NATS message throughput
- Deployment events overlay

---

## Alert Definitions

### P0 Alerts (Page immediately — 24/7)

| Alert | Condition | Action |
|---|---|---|
| Platform Down | Any critical service health check fails for > 2 minutes | Page on-call + CTO |
| Database Unavailable | PostgreSQL health check fails for > 1 minute | Page on-call + Engineering Lead |
| WABA Suspended | wa_number status = suspended for any tenant | Page on-call — urgent commercial impact |
| Security Incident | Audit log detects cross-tenant data access | Page on-call + CTO |

### P1 Alerts (Page within 15 minutes — business hours extended)

| Alert | Condition | Action |
|---|---|---|
| High Message Failure Rate | > 10% failure rate sustained 10 minutes | Page on-call |
| API Error Spike | > 5% 5xx responses sustained 5 minutes | Page on-call |
| Workflow Failures | > 5% failure rate sustained 15 minutes | Page on-call |
| DB CPU Critical | RDS CPU > 90% sustained 5 minutes | Page on-call |
| Memory Pressure | Service OOM kill detected | Page on-call |

### P2 Alerts (Slack alert — next business hour)

| Alert | Condition | Action |
|---|---|---|
| API Latency High | p95 > 500ms sustained 10 minutes | Slack #alerts |
| Message Failure Rate Elevated | 5-10% failure rate sustained 30 minutes | Slack #alerts |
| Disk Space Warning | RDS storage > 80% | Slack #infra |
| Trial Tenant Limit | Tenant at 90% of trial message limit | Slack #customer-success |
| Connector Error | Connector webhook HMAC failures > 100/hour | Slack #alerts |

---

## Structured Logging Standards

All services emit JSON logs to stdout. Log aggregator (CloudWatch Logs or ELK) collects.

**Required fields in every log line:**
```json
{
  "timestamp": "2026-06-15T10:30:00.123Z",
  "level": "INFO",
  "service": "workflow-service",
  "trace_id": "trace-uuid",
  "span_id": "span-uuid",
  "tenant_id": "t-uuid",
  "message": "Workflow execution completed",
  "workflow_id": "wf-uuid",
  "execution_id": "exec-uuid",
  "duration_ms": 1240
}
```

**PII Policy:**
- `customer.phone`, `customer.email` NEVER in logs
- `customer.name` only at DEBUG level, disabled in production
- WhatsApp message content NOT logged (privacy + data minimization)

---

## Distributed Tracing

**OpenTelemetry auto-instrumentation** for all Spring Boot services.  
**Trace propagation:** W3C Trace Context headers (`traceparent`, `tracestate`)

Every inbound webhook and API request starts a new trace. Trace propagated through:
- HTTP calls (via OpenTelemetry HTTP instrumentation)
- NATS messages (via custom trace propagation in event envelope headers)
- Temporal workflows (Temporal has native OTel support)

**Jaeger UI** accessible at internal URL for trace exploration.

---

## SLO / SLA Targets

| SLO | Target | Measurement |
|---|---|---|
| API availability | 99.5% (MVP) | Uptime checks every 1 minute |
| API p95 latency | < 300ms | Prometheus histogram |
| Message delivery time | < 10s from trigger | Event timestamps |
| Webhook processing | < 5s | Histogram |
| Monthly uptime | 99.5% (MVP) → 99.9% (Phase 2) | Monthly calculation |

---

## Cross-References
- `06-Operations/SRE.md` — SRE practices and capacity planning
- `06-Operations/Incident-Management.md` — Alert → Incident escalation flow
- `06-Operations/Runbooks.md` — Response procedures
