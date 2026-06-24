# API Governance Specification — Conductor Platform

This specification governs the design, lifecycle, authentication, security, and integration contracts for all RESTful APIs exposed by the Conductor Platform.

---

## 1. URI Path and Versioning Policy

1.  **Prefix Pattern:** All tenant and developer-facing APIs must include an explicit major version prefix in the URL path:
    $$\text{URL Prefix: } \texttt{/api/v1/...}$$
2.  **Resource Naming:** REST resource paths must utilize lowercase plural nouns in kebab-case:
    *   *Correct:* `/api/v1/consent-records`
    *   *Incorrect:* `/api/v1/consentRecord` or `/api/v1/getConsent`
3.  **HTTP Method Mapping:**
    *   `GET`: Retrieve records (safe, idempotent).
    *   `POST`: Create records or execute operations (non-idempotent without key).
    *   `PUT`: Replace records entirely (idempotent).
    *   `PATCH`: Apply partial updates (non-idempotent).
    *   `DELETE`: Prune records (idempotent).

---

## 2. Deprecation & Lifecycle Policy

1.  **API Sunset Headers:** When deprecating an API endpoint, the response headers must include:
    *   `Deprecation: true` (or the date of deprecation: `Deprecation: @1751270400`)
    *   `Sunset: YYYY-MM-DD` (the target date of absolute removal).
    *   `Link: <url-to-migration-guide>; rel="deprecation"`
2.  **Grace Period:** A version version must remain active for a minimum of 90 days following formal deprecation before absolute removal (Sunset).

---

## 3. Security (Authentication & Authorization)

### Tenant API Access (OAuth2 / OIDC)
*   **Authentication:** Ingress gateway validates the OIDC JWT token issued by Keycloak.
*   **Context Propagation:** Gateway appends identity contexts to downstream microservice parameters:
    *   `X-User-ID`: The Keycloak authenticated user identifier.
    *   `X-Tenant-ID`: The tenant's identifier resolved from the realm name.
*   **Authorization (Scopes):** Permissions are validated at the Spring Security layer using OAuth2 scope tags:
    *   `workflows:read` / `workflows:write`
    *   `contacts:read` / `contacts:write`

### Developer API Access (API Keys)
*   **Prefix Pattern:** Keys must use standard prefix schemas:
    *   `cond_live_` (Production operations)
    *   `cond_test_` (Sandbox/local sandbox testing)
*   **Validation:** Gateway checks the SHA-256 signature hash of the API key against the database store before forwarding down to services.

---

## 4. Rate Limiting Policy

1.  **Algorithm:** Token Bucket algorithm implemented in Redis.
2.  **Scopes:** Limit mappings apply per tenant per endpoint category:
    *   *Standard Write APIs:* 50 requests/sec.
    *   *Inbound Webhook Receiver APIs:* 500 requests/sec.
3.  **HTTP Response Headers:**
    *   `X-RateLimit-Limit`: Maximum requests permitted per window.
    *   `X-RateLimit-Remaining`: Tokens remaining in current bucket.
    *   `X-RateLimit-Reset`: UTC epoch timestamp indicating reset.
4.  **Exceeded Status:** Exceeding limits returns a `429 Too Many Requests` status.

---

## 5. Standard Error Format (RFC 7807)

All API error responses must adhere to the **RFC 7807 Problem Details** JSON specification:

```json
{
  "type": "https://api.conductor.com/errors/quota-exceeded",
  "title": "Tenant Quota Exceeded",
  "status": 429,
  "detail": "Tenant tenant-123 has consumed all messaging limits for the active billing cycle.",
  "instance": "/api/v1/messages/send",
  "timestamp": "2026-06-24T17:56:00.000Z",
  "invalidParams": [
    {
      "name": "recipient",
      "reason": "Request throttled due to rate limits"
    }
  ]
}
```

---

## 6. Idempotency Implementation

1.  **Idempotency Header:** All transactional mutating operations (`POST` actions like dispatching message campaigns) must support the `Idempotency-Key` header.
2.  **Storage:** The Spring Boot API interceptor caches the first request payload and response code in Redis using a key formed by:
    $$\text{Redis Key: } \texttt{idempotency:\{tenantId\}:\{idempotencyKey\}}$$
3.  **TTL:** The idempotency cache is configured with a 24-hour Time-to-Live (TTL). Repeated requests within this window return the cached response without calling downstream Temporal workflows.

---

## 7. Pagination and Tracing Standards

### Cursor-Based Pagination
For listings, offset pagination is blocked to optimize PostgreSQL performance. Cursor-based parameters are mandated:
*   `limit`: Number of rows to return (default 20, max 100).
*   `starting_after`: Base64 cursor token mapping to the primary key.
*   *Response metadata:* Return a `hasMore` boolean and an `ending_before` / `starting_after` next-page cursor.

### Trace Context Propagation
*   **Headers:** Ingress APIs must propagate standard W3C Trace Context headers:
    *   `traceparent`: Identifies trace ID and span parameters.
    *   `tracestate`: System-specific tracing states.
*   **Correlation:** Downstream logging frames (Logback/SLF4J) automatically inject this trace context (`trace_id`) into log tags.
