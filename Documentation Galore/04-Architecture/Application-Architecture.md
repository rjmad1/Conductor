# Application Architecture — Conductor

**Status:** Partially Extracted + Extended (⚡ where inferred)  
**Source:** Technical Layers document  
**Last Updated:** June 2026

---

## Purpose
Defines the internal design of each application service: responsibilities, APIs exposed, events consumed/published, and dependencies.

---

## Service Design Principles

1. **Single Responsibility:** Each service owns one capability domain
2. **API-First:** Every capability is accessible via REST API (not just UI)
3. **Event-Driven:** State changes emit events to NATS; services consume rather than poll
4. **Stateless Services:** Services are stateless; state lives in PostgreSQL/Redis
5. **Tenant-Scoped:** Every data operation is scoped to a tenant_id

---

## Service: tenant-service

**Owns:** Tenant lifecycle, business profile, plan assignment, feature flags

**REST APIs:**
```
POST   /api/v1/tenants                     — Create tenant (registration)
GET    /api/v1/tenants/{id}                — Get tenant profile
PATCH  /api/v1/tenants/{id}               — Update business profile
GET    /api/v1/tenants/{id}/features       — Get active feature flags for plan
PUT    /api/v1/tenants/{id}/plan           — Assign/change plan
POST   /api/v1/tenants/{id}/suspend        — Suspend tenant
DELETE /api/v1/tenants/{id}               — Delete tenant (DPDP-compliant)
```

**Events Published:**
- `tenant.created`
- `tenant.plan.changed`
- `tenant.suspended`

**Dependencies:** Keycloak (realm creation), billing-service (plan assignment)

---

## Service: customer-service

**Owns:** Customer registry, contact management, segmentation, consent

**REST APIs:**
```
POST   /api/v1/customers                   — Create customer
GET    /api/v1/customers/{id}              — Get customer
PUT    /api/v1/customers/{id}              — Update customer
DELETE /api/v1/customers/{id}             — Delete customer (DPDP right to erasure)
POST   /api/v1/customers/import            — Bulk import via CSV
GET    /api/v1/customers?tag={tag}         — Filter by tag
POST   /api/v1/customers/{id}/tags         — Add tag
DELETE /api/v1/customers/{id}/tags/{tag}   — Remove tag
PUT    /api/v1/customers/{id}/consent      — Update consent status
GET    /api/v1/segments                    — List segments
POST   /api/v1/segments                    — Create segment definition
GET    /api/v1/segments/{id}/members       — Get segment members
```

**Events Consumed:**
- `message.inbound` (extract customer, update last_interaction)
- `message.delivered` (update delivery status in customer record)

**Events Published:**
- `customer.created`
- `customer.updated`
- `customer.opted_out`
- `customer.opted_in`

---

## Service: workflow-service

**Owns:** Workflow CRUD, activation, execution orchestration via Temporal

**REST APIs:**
```
POST   /api/v1/workflows                   — Create workflow
GET    /api/v1/workflows                   — List workflows
GET    /api/v1/workflows/{id}              — Get workflow detail
PUT    /api/v1/workflows/{id}              — Update workflow
DELETE /api/v1/workflows/{id}             — Delete workflow
PUT    /api/v1/workflows/{id}/activate     — Activate workflow
PUT    /api/v1/workflows/{id}/deactivate   — Deactivate workflow
POST   /api/v1/workflows/{id}/test         — Test workflow with customer record
GET    /api/v1/workflows/{id}/executions   — Execution history
GET    /api/v1/executions/{id}             — Execution detail with step trace
```

**Events Consumed:**
- ALL events on `conductor.{tenantId}.*` — triggers workflow evaluation

**Events Published:**
- `workflow.triggered`
- `workflow.completed`
- `workflow.failed`

**Temporal Integration:**
- `workflow-service` acts as the **Temporal client** — starts workflow executions
- Temporal Workers execute the actual workflow logic (evaluate conditions, dispatch actions)

**Internal Temporal Workflows:**
```java
@WorkflowInterface
interface AutomationWorkflow {
    @WorkflowMethod
    WorkflowResult execute(WorkflowContext context);
}

// Activities (units of work in a workflow):
interface WorkflowActivities {
    boolean evaluateConditions(List<Condition> conditions, Map<String, Object> data);
    void sendWhatsAppTemplate(String to, String templateId, Map<String, String> vars);
    void updateCustomerAttribute(String customerId, String key, Object value);
    void triggerExternalApi(String url, Map<String, Object> payload);
    void delayExecution(int minutes); // implemented via Temporal sleep
}
```

---

## Service: whatsapp-adapter

**Owns:** All WhatsApp Cloud API interactions, inbound webhook processing

**REST APIs (Internal):**
```
POST   /internal/whatsapp/send/template    — Send template message
POST   /internal/whatsapp/send/text        — Send text message
POST   /internal/whatsapp/send/media       — Send media message
POST   /internal/whatsapp/send/interactive — Send interactive buttons/list
```

**Webhook Endpoint (External — registered with Meta):**
```
GET    /webhooks/whatsapp                  — Webhook verification (challenge-response)
POST   /webhooks/whatsapp                  — Inbound messages + status updates
```

**Inbound Message Processing:**
```
1. Verify HMAC signature (X-Hub-Signature-256 header)
2. Parse Meta webhook payload
3. Normalize to Conductor event envelope
4. Publish to NATS: conductor.{tenantId}.message.inbound
5. Return HTTP 200 to Meta (within 20 seconds — Meta SLA requirement)
```

**Events Published:**
- `message.inbound` (customer sent us a message)
- `message.sent` (we sent a message)
- `message.delivered` (message delivered to customer device)
- `message.read` (customer read the message)
- `message.failed` (delivery failure)

**Critical Implementation Notes:**
- MUST return 200 to Meta within 20 seconds or Meta will retry indefinitely
- All sending is async: publish to NATS, Temporal worker picks up and sends
- Rate limiting: Max 80 messages/second per phone number (Meta limit)

---

## Service: conversation-service

**Owns:** Conversation state machine, inbound message routing

**REST APIs:**
```
GET    /api/v1/conversations               — List active conversations
GET    /api/v1/conversations/{id}          — Get conversation with full history
POST   /api/v1/conversations/{id}/assign   — Assign to agent
POST   /api/v1/conversations/{id}/resolve  — Mark resolved
```

**Events Consumed:**
- `message.inbound` — processes inbound messages, updates conversation state

**State Machine:**
```
IDLE → [inbound message] → ACTIVE → [workflow running] → WAITING_REPLY
WAITING_REPLY → [customer reply matches] → NEXT_STEP
WAITING_REPLY → [timeout or STOP] → CLOSED
ACTIVE → [route_to_agent action] → AGENT_ASSIGNED
AGENT_ASSIGNED → [agent resolves] → CLOSED
```

**Redis Usage:**
- Conversation sessions stored in Redis with 24h TTL
- Key: `conv:{tenantId}:{customerPhone}` → session state JSON

---

## Service: campaign-service

**Owns:** Campaign lifecycle — creation, scheduling, execution, analytics

**REST APIs:**
```
POST   /api/v1/campaigns                   — Create campaign
GET    /api/v1/campaigns                   — List campaigns
GET    /api/v1/campaigns/{id}              — Campaign detail + analytics
PUT    /api/v1/campaigns/{id}              — Update (before launch)
DELETE /api/v1/campaigns/{id}             — Delete
POST   /api/v1/campaigns/{id}/launch       — Launch now
POST   /api/v1/campaigns/{id}/cancel       — Cancel scheduled
```

**Broadcast Campaign Execution:**
```
1. Fetch segment members from customer-service
2. Filter: opted-in only, frequency cap check (1/day per customer)
3. For each eligible customer: publish send event to NATS
4. Temporal batch workflow handles the send queue (rate-limited to WhatsApp limits)
5. Track delivery status per customer
```

**Events Published:**
- `campaign.launched`
- `campaign.completed`
- `campaign.message.sent` (per customer)

---

## Service: template-service

**Owns:** WhatsApp message template lifecycle

**REST APIs:**
```
POST   /api/v1/templates                   — Create template
GET    /api/v1/templates                   — List templates
GET    /api/v1/templates/{id}              — Get template
PUT    /api/v1/templates/{id}              — Update (only PENDING templates)
DELETE /api/v1/templates/{id}             — Delete
POST   /api/v1/templates/{id}/submit       — Submit to Meta for approval
GET    /api/v1/templates/{id}/status       — Check approval status
```

**Meta API Integration:**
- Submit: `POST /v18.0/{waba_id}/message_templates`
- Status poll: `GET /v18.0/{template_id}` (webhook from Meta on status change preferred)

---

## Service: analytics-service

**Owns:** Metrics aggregation, dashboard data

**Design:** Event-driven analytics — consumes all events from NATS and writes aggregates to dedicated analytics tables (separate from operational tables for query performance).

**REST APIs:**
```
GET /api/v1/analytics/overview             — Dashboard KPIs (messages, workflows, customers)
GET /api/v1/analytics/messages             — Message metrics by date range
GET /api/v1/analytics/workflows/{id}       — Workflow performance metrics
GET /api/v1/analytics/campaigns/{id}       — Campaign metrics
GET /api/v1/analytics/customers            — Customer growth metrics
```

**Events Consumed:** All events (to compute metrics)

**Metabase Integration:** Metabase connects directly to the analytics schema in PostgreSQL. The `analytics-service` is responsible for maintaining the denormalized analytics tables that Metabase queries.

---

## Service: billing-service

**Owns:** Subscription management, usage tracking, invoicing

**REST APIs:**
```
GET    /api/v1/billing/subscription        — Current subscription
POST   /api/v1/billing/subscribe           — Start/upgrade subscription
PUT    /api/v1/billing/subscription        — Change plan
DELETE /api/v1/billing/subscription        — Cancel subscription
GET    /api/v1/billing/usage               — Current month usage
GET    /api/v1/billing/invoices            — Invoice history
GET    /api/v1/billing/invoices/{id}       — Invoice detail + download
POST   /api/v1/billing/payment-methods     — Add payment method
```

**Events Consumed:**
- `message.sent` → increments usage_records counter for tenant

**Razorpay Integration:**
- Subscription creation, modification, payment capture
- Webhook for: payment.captured, subscription.charged, subscription.halted

---

## Cross-References
- `04-Architecture/Solution-Architecture.md` — Platform layers and component inventory
- `04-Architecture/Data-Architecture.md` — Database schemas for each service
- `05-Engineering/API-Contracts.md` — Full API specifications
- `05-Engineering/Event-Contracts.md` — Event schema definitions
