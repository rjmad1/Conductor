# Context Pack — Conductor IDE Knowledge Pack

**Purpose:** Key decisions, constraints, anti-patterns, and guard rails for AI coding agents and engineers. Read this to avoid the most costly mistakes.

---

## The 10 Non-Negotiable Rules

These constraints are ABSOLUTE. Never violate them regardless of convenience, performance, or shortcuts.

### Rule 1: EVERY query is scoped by tenant_id
```java
// CORRECT
repository.findByIdAndTenantId(id, tenantId);

// WRONG — cross-tenant data leak
repository.findById(id);
```
`tenantId` must come from the authenticated user's JWT claim, never from a request parameter.

### Rule 2: NEVER use OpenWA, Baileys, or unofficial WhatsApp libraries
The ONLY permitted WhatsApp integration is the official Meta WhatsApp Cloud API.  
Violation = existential risk (ToS termination). See ADR-003.

### Rule 3: NEVER log PII
Phone numbers, customer names, email addresses, and message content must never appear in logs.

```java
// CORRECT
log.info("Processing inbound message for customer {}", customerId);  // UUID only

// WRONG
log.info("Message from {} ({}): {}", phone, name, content);
```

### Rule 4: Consent records are APPEND-ONLY
Never UPDATE or DELETE a `consent_records` row. Only INSERT new records.  
The most recent record per `(customer_id, channel, consent_type)` is the current status.

### Rule 5: STOP = opt-out within 5 seconds
Any inbound message matching "STOP" (case-insensitive) must update `wa_opt_in_status` and publish `customer.opted_out` before returning from the webhook handler.

### Rule 6: HMAC validation uses constant-time comparison
```java
// CORRECT — timing-safe
MessageDigest.isEqual(expectedHmac.getBytes(), receivedHmac.getBytes());

// WRONG — timing attack vulnerable
expectedHmac.equals(receivedHmac);
```

### Rule 7: Marketing messages require explicit opt-in
Before any workflow action sends a marketing template, the system MUST check `customer.wa_opt_in_status == 'opted_in'`. Skipping this check is a DPDP violation.

### Rule 8: Temporal owns workflow execution — no cron jobs
All scheduled and event-triggered workflows execute through Temporal. Never use cron jobs, Spring `@Scheduled`, or direct thread sleeps to implement business automation logic.

### Rule 9: Connector credentials are encrypted
The `connector_configs.credentials` JSONB field is encrypted at rest. Never return credential data in API responses or log it.

### Rule 10: Tenant_id comes from JWT only
```java
// CORRECT
String tenantId = jwtClaims.get("tenantId");

// WRONG — never trust client-provided tenant context
String tenantId = request.getHeader("X-Tenant-Id");
```

---

## Key Architectural Decisions (Summary)

Full ADRs in `07-Governance/Decision-Records.md`.

| Decision | Choice | Why |
|---|---|---|
| Workflow engine | Temporal | Durable execution, simple SDK, better OSS. Not Camunda 8. |
| Event bus | NATS (JetStream) | Low latency, simple ops. Not Kafka for MVP. |
| WhatsApp API | Cloud API (official) | Only legal option. Not OpenWA. |
| Database | PostgreSQL 15 | JSONB for DSL, pgvector for AI. |
| IAM | Keycloak self-hosted | Data control, per-tenant realms. |
| Frontend | React 18 SPA | Not Next.js (SSR not needed). |
| Analytics | Metabase embedded | OSS, fast to ship. |
| Human inbox | Chatwoot | MIT license, REST API. |
| Strategy | OSS Assembly | 60-70% engineering savings. |

---

## Common Anti-Patterns

### Anti-Pattern: Synchronous service-to-service HTTP calls for state changes
**Wrong:** `customerService.updateOptInStatus()` called directly from `WhatsAppWebhookController`.  
**Right:** Publish `customer.opted_out` event to NATS. `customer-service` subscribes and updates.

### Anti-Pattern: Passing tenant context as a request body field
**Wrong:** `POST /api/customers { "tenantId": "...", "phone": "..." }`  
**Right:** Extract `tenantId` from validated JWT. Reject if missing.

### Anti-Pattern: Blocking Temporal workflow for I/O
**Wrong:** Making HTTP calls or DB queries inside a `@WorkflowMethod`.  
**Right:** Use `@ActivityMethod` for all I/O. Workflow methods are deterministic replays only.

### Anti-Pattern: Cron-based workflow scheduling
**Wrong:** `@Scheduled(cron = "0 9 * * 1-5") void sendMorningDigest()`  
**Right:** Temporal schedule workflows for all time-based automation.

### Anti-Pattern: Template variable injection without sanitization
**Wrong:** Directly interpolating user-supplied `custom_attributes` into template variables.  
**Right:** Validate and escape all variable values before substitution.

### Anti-Pattern: Returning all tenants' data
**Wrong:** `SELECT * FROM customers` (missing WHERE tenant_id = ?)  
**Right:** Always include `WHERE tenant_id = :tenantId` — enforce at repository layer.

---

## Service Communication Patterns

### Use REST (synchronous) for:
- CRUD operations (create, read, update, delete)
- Real-time queries from the frontend
- Actions where the caller needs an immediate result

### Use NATS events (asynchronous) for:
- State change notifications between services
- Triggering workflow evaluations
- Fanout notifications (multiple subscribers)
- Operations that can tolerate eventual consistency

### Use Temporal (durable async) for:
- Multi-step workflows that span minutes, hours, or days
- Operations that need guaranteed completion with retries
- Anything with a human wait step or timer

---

## Data Mutation Rules

| Entity | CREATE | READ | UPDATE | DELETE |
|---|---|---|---|---|
| Tenant | ✅ | ✅ | ✅ (status) | Soft (`status=deleted`) |
| Customer | ✅ | ✅ | ✅ | Soft (`deleted_at`) |
| Consent Record | ✅ | ✅ | ❌ NEVER | ❌ NEVER |
| Workflow | ✅ | ✅ | ✅ (+version) | Soft (`status=archived`) |
| Message | ✅ (append only) | ✅ | Status updates only | ❌ NEVER |
| Audit Log | ✅ (append only) | ✅ | ❌ NEVER | ❌ NEVER |
| Template | ✅ | ✅ | Draft/Pending only | Soft |

---

## WhatsApp Cloud API Quick Reference

**Base URL:** `https://graph.facebook.com/v18.0/`  
**Send message:** `POST /{phone-number-id}/messages`  
**Get templates:** `GET /{waba-id}/message_templates`  
**Webhook verify:** `GET /webhooks/whatsapp` (challenge-response)  
**Webhook inbound:** `POST /webhooks/whatsapp` (HMAC validate first)  

**Rate limits:**
- 80 messages/second per phone number
- 1,000 conversations/day (Tier 1, increases with WABA history)

**24-hour window:**
- User-initiated: Free-form messages allowed for 24 hours after customer's last message
- Business-initiated: MUST use approved templates

---

## JWT Token Structure

```json
{
  "sub": "user-uuid",
  "email": "user@business.com",
  "tenantId": "tenant-uuid",
  "realm": "tenant-keycloak-realm-id",
  "roles": ["ROLE_ADMIN"],
  "iss": "https://auth.conductor.io/realms/{realm}",
  "exp": 1735689600
}
```

Access token TTL: 15 minutes  
Refresh token TTL: 30 days

---

## Error Response Format

All REST APIs return errors in this structure:

```json
{
  "error": {
    "code": "WORKFLOW_NOT_FOUND",
    "message": "Workflow abc123 does not exist or you do not have access",
    "timestamp": "2026-06-24T10:00:00Z",
    "request_id": "req_xyz"
  }
}
```

Status codes: 400 (validation), 401 (auth), 403 (permission), 404 (not found), 409 (conflict), 422 (business rule), 429 (rate limit), 500 (server error).

---

## Development vs. Production Differences

| Concern | Development | Production |
|---|---|---|
| WhatsApp | WhatsApp Business API Sandbox | Live WABA (Meta approved) |
| Database | Local PostgreSQL | RDS Multi-AZ PostgreSQL 15 |
| Events | Local NATS (single node) | NATS JetStream 3-node cluster |
| Workflows | Local Temporal | Temporal Cloud or self-hosted cluster |
| Auth | Keycloak (local Docker) | Keycloak (production cluster) |
| Secrets | .env files (gitignored) | AWS Secrets Manager |
| Billing | Razorpay Test mode | Razorpay Live mode |

---

## Phase Boundaries

| Phase | Status | Key Constraint |
|---|---|---|
| MVP V1 (Phase 0-1) | Build now | No AI; deterministic workflows only |
| Growth (Phase 2) | After 100 tenants | AI features enabled |
| Platform (Phase 3) | After Series A | Public API, marketplace, agents |

Do not implement Phase 2/3 features during MVP. Stub interfaces where future expansion is likely, but do not implement.
