# API Contracts — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Product Requirements, Application Architecture  
**Last Updated:** June 2026

---

## API Design Principles

- **RESTful:** Resource-oriented URL design with standard HTTP methods
- **Versioned:** All public APIs prefixed with `/api/v1/` — breaking changes require new version
- **Tenant-scoped:** Tenant context from JWT claim, never from URL or request body
- **JSON:** All request/response bodies in JSON; `Content-Type: application/json`
- **Pagination:** All list endpoints support `?page={n}&size={n}&sort={field}&order={asc|desc}`
- **Errors:** Uniform error response format (see below)

### Request Headers (Required on all authenticated endpoints)
```http
Authorization: Bearer {jwt_token}
Content-Type: application/json
X-Request-ID: {client-generated-uuid}    # For idempotency and tracing
```

### Standard Error Response
```json
{
  "error_code": "WORKFLOW_NOT_FOUND",
  "message": "Workflow wf-001 not found",
  "timestamp": "2026-06-15T10:00:00Z",
  "request_id": "req-uuid"
}
```

### Standard List Response
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "size": 20,
    "total_elements": 145,
    "total_pages": 8
  }
}
```

---

## Tenant Service API

### Create Tenant
```http
POST /api/v1/tenants
Authorization: (Keycloak registration token)

Request:
{
  "name": "Dr. Sunita's Clinic",
  "email": "admin@drsuniita.in",
  "industry": "healthcare",
  "timezone": "Asia/Kolkata"
}

Response: 201 Created
{
  "id": "t-uuid",
  "name": "Dr. Sunita's Clinic",
  "plan": "trial",
  "status": "active",
  "trial_ends_at": "2026-07-08T00:00:00Z",
  "created_at": "2026-06-24T10:00:00Z"
}
```

### Get Tenant Profile
```http
GET /api/v1/tenants/{id}

Response: 200 OK
{
  "id": "t-uuid",
  "name": "Dr. Sunita's Clinic",
  "industry": "healthcare",
  "plan": "growth",
  "status": "active",
  "timezone": "Asia/Kolkata",
  "wa_numbers": ["+91XXXXXXXXXX"],
  "feature_flags": {
    "ai_intent": false,
    "campaigns": true,
    "max_workflows": 20
  }
}
```

---

## Customer Service API

### Create Customer
```http
POST /api/v1/customers

Request:
{
  "phone": "+919999999999",
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "tags": ["vip", "patient"],
  "custom_attributes": {
    "patient_id": "P-1234",
    "doctor": "Dr. Sunita"
  },
  "wa_opt_in": true
}

Response: 201 Created
{
  "id": "c-uuid",
  "phone": "+919999999999",
  "name": "Priya Sharma",
  "wa_opt_in_status": "opted_in",
  "created_at": "2026-06-24T10:00:00Z"
}
```

### List Customers
```http
GET /api/v1/customers?page=1&size=20&tag=vip&opt_in=true

Response: 200 OK
{
  "data": [
    {
      "id": "c-uuid",
      "phone": "+919999999999",
      "name": "Priya Sharma",
      "tags": ["vip", "patient"],
      "wa_opt_in_status": "opted_in",
      "last_interaction_at": "2026-06-01T14:30:00Z"
    }
  ],
  "pagination": { "page": 1, "size": 20, "total_elements": 450, "total_pages": 23 }
}
```

### Import Customers (CSV)
```http
POST /api/v1/customers/import
Content-Type: multipart/form-data

Form fields:
  file: {csv_file}
  opt_in_all: true|false     # Whether to opt-in all imported contacts

Response: 202 Accepted (async processing)
{
  "import_id": "import-uuid",
  "status": "processing",
  "status_url": "/api/v1/customers/imports/import-uuid"
}
```

### Update Customer Consent
```http
PUT /api/v1/customers/{id}/consent

Request:
{
  "channel": "whatsapp",
  "status": "opted_out",
  "collected_via": "customer_reply",
  "consent_text": "Customer replied STOP"
}

Response: 200 OK
{
  "customer_id": "c-uuid",
  "channel": "whatsapp",
  "status": "opted_out",
  "updated_at": "2026-06-24T10:05:00Z"
}
```

---

## Workflow Service API

### Create Workflow
```http
POST /api/v1/workflows

Request:
{
  "name": "Appointment Reminder — 24h",
  "description": "Send reminder 24 hours before appointment",
  "trigger": {
    "type": "event",
    "event_name": "appointment.created",
    "source": "google_calendar",
    "delay_minutes": -1440
  },
  "conditions": [
    {
      "field": "customer.wa_opt_in_status",
      "operator": "eq",
      "value": "opted_in"
    }
  ],
  "actions": [
    {
      "type": "send_whatsapp_template",
      "template_id": "appt_reminder_24h",
      "variables": {
        "patient_name": "{{customer.name}}",
        "appointment_time": "{{event.start_time}}",
        "doctor_name": "{{event.attendees[0].name}}"
      }
    }
  ],
  "execution_limits": {
    "max_per_customer_per_day": 1
  }
}

Response: 201 Created
{
  "id": "wf-uuid",
  "name": "Appointment Reminder — 24h",
  "status": "inactive",
  "created_at": "2026-06-24T10:00:00Z"
}
```

### Activate Workflow
```http
PUT /api/v1/workflows/{id}/activate

Response: 200 OK
{
  "id": "wf-uuid",
  "status": "active",
  "activated_at": "2026-06-24T10:10:00Z"
}
```

### Get Execution History
```http
GET /api/v1/workflows/{id}/executions?page=1&size=20

Response: 200 OK
{
  "data": [
    {
      "id": "exec-uuid",
      "customer_id": "c-uuid",
      "customer_name": "Priya Sharma",
      "status": "completed",
      "started_at": "2026-06-24T09:00:00Z",
      "duration_ms": 1240,
      "steps_count": 3
    }
  ],
  "pagination": {...}
}
```

---

## WhatsApp Adapter API (Internal)

### Send Template Message
```http
POST /internal/whatsapp/send/template
X-Service-Auth: {internal-service-token}

Request:
{
  "to": "+919999999999",
  "template_id": "appt_reminder_24h",
  "wa_number_id": "wa-uuid",
  "variables": {
    "patient_name": "Priya Sharma",
    "appointment_time": "Wednesday 3:00 PM"
  },
  "idempotency_key": "exec-uuid-action-0"
}

Response: 202 Accepted
{
  "message_id": "msg-uuid",
  "wa_message_id": "wamid.XXXX",
  "status": "sent"
}
```

---

## Campaign Service API

### Create Campaign
```http
POST /api/v1/campaigns

Request:
{
  "name": "Diwali Promotion 2026",
  "type": "broadcast",
  "audience": {
    "segment_id": "seg-uuid"
  },
  "message": {
    "template_id": "diwali_promo_v1"
  },
  "schedule": {
    "type": "scheduled",
    "send_at": "2026-10-20T09:00:00+05:30"
  },
  "frequency_cap": {
    "max_per_customer_per_day": 1
  }
}

Response: 201 Created
{
  "id": "camp-uuid",
  "name": "Diwali Promotion 2026",
  "estimated_audience": 1240,
  "status": "scheduled",
  "send_at": "2026-10-20T09:00:00+05:30"
}
```

---

## Webhook Endpoints (External)

### WhatsApp Cloud API Webhook
```http
GET  /webhooks/whatsapp?hub.mode=subscribe&hub.challenge={challenge}&hub.verify_token={token}
→ Returns hub.challenge value (Meta verification)

POST /webhooks/whatsapp
X-Hub-Signature-256: sha256={hmac}
→ Processes inbound messages and status updates
→ Must return 200 within 20 seconds
```

### Generic Connector Webhook
```http
POST /webhooks/{connector_id}
X-Conductor-Tenant: {tenant_id}
{connector-specific-signature-header}: {hmac}

→ Normalized and published to NATS
→ Returns 200 immediately (async processing)
```

---

## API Versioning Policy

- Current version: `v1`
- Breaking changes: new version (`v2`) introduced with minimum 6-month overlap period
- Breaking changes definition: removing/renaming fields, changing field types, removing endpoints
- Non-breaking changes (no version bump): adding new optional fields, new endpoints

---

## Rate Limits

| Plan | Requests/minute | Burst |
|---|---|---|
| Starter | 60 | 100 |
| Growth | 300 | 500 |
| Business | 1,000 | 2,000 |
| Enterprise | Custom | Custom |

Rate limit headers included in all responses:
```http
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 245
X-RateLimit-Reset: 1718964060
```

---

## OpenAPI Specification Location

Full OpenAPI 3.0 YAML specs are in the repository at:
```
conductor-platform/shared/api-contracts/
├── tenant-api.yaml
├── customer-api.yaml
├── workflow-api.yaml
├── campaign-api.yaml
├── template-api.yaml
├── analytics-api.yaml
├── billing-api.yaml
└── webhooks-api.yaml
```

---

## Cross-References
- `05-Engineering/Event-Contracts.md` — NATS event schema definitions
- `05-Engineering/Schema-Definitions.md` — Database schema definitions
- `04-Architecture/Application-Architecture.md` — Service-level design
