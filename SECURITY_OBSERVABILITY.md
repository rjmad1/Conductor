# Security Observability Specification — Conductor Platform

This specification defines the logs, metrics, alerts, dashboards, and incident response signals required to establish real-time security observability for the Conductor Platform.

---

## 1. Security Observability Architecture

The platform uses the **OpenTelemetry (OTel) Collector**, **Grafana Loki** (logs), **Prometheus** (metrics), and **Tempo** (tracing) to capture and correlate security signals:

```
[Service Containers] ──► [OTel Collector] ──► [Prometheus] (Metrics)
    (Inject Trace IDs)            │           ──► [Loki]       (Logs)
                                  └──────────► [Tempo]      (Traces) ──► [Grafana Alert Engine]
```

### Trace Context Propagation
Every log entry, trace span, and metric payload must carry the W3C trace header values (`traceparent`) generated at the Kong API Gateway to correlate API calls to database queries.

---

## 2. Security Logging Standards

Security log events must be structured as single-line JSON records. They are written to standard console streams (`stdout`/`stderr`) and forwarded by the OTel collector to Loki.

### Standard Security Log JSON Schema
```json
{
  "timestamp": "2026-06-24T17:57:00.000Z",
  "level": "WARN",
  "logger": "com.conductor.security.SecurityInterceptor",
  "trace_id": "8a3f890e720891d4e08210bfca09efb4",
  "span_id": "408cfb287ac8e90a",
  "tenant_id": "tenant-123-uuid",
  "user_id": "user-456-uuid",
  "event_type": "SECURITY_AUTHORIZATION_FAILURE",
  "message": "User attempted to access restricted resources belonging to another tenant.",
  "request_path": "/api/v1/workflows/executions",
  "client_ip": "198.51.100.12",
  "user_agent": "Mozilla/5.0...",
  "context": {
    "requested_entity_id": "workflow-789-uuid",
    "requested_entity_tenant_id": "tenant-999-uuid",
    "required_scope": "workflows:read",
    "action": "READ"
  }
}
```

---

## 3. Security Metrics Registry (Prometheus)

All modules must export the following counter and gauge metrics at the `/metrics` path:

| Metric Name | Type | Labels | Description |
| :--- | :--- | :--- | :--- |
| `security_auth_failures_total` | Counter | `realm`, `reason` | Total failed login attempts (e.g., bad password, MFA failure). |
| `security_jwt_invalid_signatures_total` | Counter | `gateway_ip` | Total requests presenting forged or expired JWT signatures. |
| `security_tenant_isolation_failures_total` | Counter | `tenant_id`, `requested_tenant_id` | **Critical:** Requests where token tenant does not match resource tenant. |
| `security_rate_limit_exceeded_total` | Counter | `tenant_id`, `endpoint` | Requests throttled with a `429 Too Many Requests` code. |
| `security_egress_proxy_blocks_total` | Counter | `target_domain` | Outbound requests blocked by the Squid egress proxy. |
| `security_api_key_failures_total` | Counter | `client_ip` | Invalid, expired, or missing developer API key validations. |
| `security_session_hijacks_detected` | Counter | `user_id` | Session tokens blocked due to refresh token reuse (replay attack). |

---

## 4. Real-Time Security Alert Rules

Alerts are configured in Grafana and routed to security teams via **Slack**, **PagerDuty**, or **Webhook channels**:

### 4.1 P0 Critical Alert: Tenant Isolation Breach
*   **Condition:** `rate(security_tenant_isolation_failures_total[1m]) > 0`
*   **Description:** Triggered instantly on any attempt to execute cross-tenant queries. Indicates active exploitation.
*   **Action:** PagerDuty wake-up; immediate automated session revocation for the offending actor.

### 4.2 P1 High Alert: Webhook Egress Blocked
*   **Condition:** `rate(security_egress_proxy_blocks_total[5m]) > 5`
*   **Description:** Integration service is trying to access a blocked address (potential SSRF scan).
*   **Action:** Slack channel alert; temporary quarantine of the integration job.

### 4.3 P2 Medium Alert: Keycloak Brute Force Lockout
*   **Condition:** `rate(security_auth_failures_total[5m]) > 10`
*   **Description:** Single user account has triggered brute force lockout limits.
*   **Action:** Email alert to security admins; user receives password reset guidelines.

---

## 5. Security Dashboards

The Grafana Security Dashboard displays the following panels:

1.  **Auth Status Panel:** Real-time graph comparing successful vs failed authentications.
2.  **Tenant Safety Gauge:** Real-time counter of tenant isolation violations (must be 0).
3.  **Active Session Map:** Geo-IP density panel tracking connection sources.
4.  **Throttling Panel:** Graph of rate-limit violations by tenant ID.
5.  **Egress Traffic Logs:** Loki console logs display listing Squid proxy denials.

---

## 6. Incident Response Automation (Security Playbooks)

When a P0 critical metric fires:
1.  **Session Terminate:** A Spring Security aspect catches the tenant validation error, publishes a `conductor.system.security.compromise` event, and instructs Keycloak to terminate the user's active session.
2.  **API Key Disable:** If the violation occurred via an API Key context, the Gateway flags the key as disabled (`is_active = false`), blocking further requests.
3.  **Circuit Breaker Quarantine:** If an integration endpoint exhibits SSRF scanning patterns, the integration gateway trips, moving all subsequent webhook jobs for that tenant to the DLQ stream.

This specification governs security operations monitoring.
