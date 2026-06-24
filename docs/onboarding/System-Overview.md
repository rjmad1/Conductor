# System Overview — Conductor IDE Knowledge Pack

**Purpose:** Quick reference for AI coding agents, IDE assistants, and new engineers joining the project.

---

## What Is Conductor?

Conductor is a **multi-tenant SaaS platform** that enables small and medium businesses (SMBs) to automate their customer communications, primarily via WhatsApp.

**Core concept:** `Trigger → Condition → Action`
Any business process can be expressed as: "When X happens, if Y is true, do Z."

**What makes it different:** The **Business Capability Layer** — pre-built, outcome-oriented workflow templates (e.g., "Recover Abandoned Carts") that abstract the underlying technical primitives.

---

## Quick Facts

| Property | Value |
|---|---|
| Type | B2B SaaS, multi-tenant |
| Primary channel | WhatsApp (via Meta Cloud API) |
| Primary market | India (SMBs) |
| Tech stack | Java 21, Spring Boot 3, React 18, PostgreSQL 15, Redis 7, NATS, Temporal |
| Architecture style | Modular monolith with event-driven async processing |
| Target customers | Healthcare clinics, retail stores, professional services firms |
| Pricing | ₹1,499 – ₹12,999/month (4 tiers) |

---

## Platform Layers

```
Layer 5: Presentation     — React Web App (SPA)
Layer 4: API Gateway      — Kong (auth, rate limiting, routing)
Layer 3: App Services     — Spring Boot microservices (12 services)
Layer 2: Event/Workflow   — NATS Event Bus + Temporal Workflow Engine
Layer 1: Data             — PostgreSQL + Redis + S3
```

---

## Core Services (Spring Boot)

| Service | Port | What It Does |
|---|---|---|
| `tenant-service` | 8080 | Tenant lifecycle, plan management |
| `customer-service` | 8082 | Customer registry, consent, segments |
| `workflow-service` | 8083 | Workflow CRUD, activation, execution |
| `whatsapp-adapter` | 8088 | WhatsApp Cloud API integration |
| `conversation-service` | 8084 | Multi-turn conversation state machine |
| `campaign-service` | 8085 | Broadcast/drip campaigns |
| `template-service` | 8086 | WhatsApp template lifecycle |
| `connector-service` | 8087 | External integrations (Shopify, Razorpay, etc.) |
| `analytics-service` | 8089 | Dashboard metrics |
| `billing-service` | 8090 | Subscriptions, usage, invoices |

---

## Critical Platform Rules

1. **Every table has `tenant_id`** — no query without tenant scope
2. **`tenant_id` comes from JWT claim only** — never from user-supplied input
3. **WhatsApp Cloud API only** — OpenWA/unofficial libraries are prohibited
4. **No PII in logs** — phone numbers, customer names, message content are never logged
5. **Consent before marketing** — never send marketing to opted-out customers
6. **STOP = immediate opt-out** — must process within 5 seconds
7. **Temporal handles workflows** — never use cron jobs for workflow execution
8. **Events via NATS** — services communicate state changes via events, not direct HTTP calls

---

## Data Flow Patterns

### Outbound Automation (Trigger → Message)
```
External event (e.g., Shopify cart abandoned)
→ Connector webhook endpoint
→ Normalize to Conductor event envelope
→ Publish to NATS
→ Workflow Engine evaluates matching workflows
→ Temporal executes workflow (evaluate conditions → dispatch actions)
→ WhatsApp Adapter calls Meta Cloud API
→ Message delivered to customer
```

### Inbound Customer Message
```
Customer sends WhatsApp message
→ Meta posts webhook to /webhooks/whatsapp
→ HMAC signature validated
→ Normalize to message.inbound event
→ Publish to NATS
→ Conversation Engine processes (menu flow or agent)
→ Response dispatched via WhatsApp Adapter
```

---

## Key External Dependencies

| Service | Use | Auth |
|---|---|---|
| Meta WhatsApp Cloud API | Send/receive WhatsApp messages | System user access token |
| Keycloak | Authentication, RBAC, multi-tenant IAM | OpenID Connect |
| Temporal | Durable workflow execution | Internal API |
| NATS | Event bus for all async communication | Internal |
| Razorpay | Billing + payment links | API Key/Secret |
| Shopify | E-commerce events | OAuth 2.0 |
| Google Calendar | Appointment events | OAuth 2.0 |

---

## Workflow DSL (The Core Abstraction)

Everything in Conductor ultimately maps to this JSON structure:

```json
{
  "trigger": {
    "type": "event",
    "event_name": "cart.abandoned",
    "source": "shopify",
    "delay_minutes": 30
  },
  "conditions": [
    { "field": "cart.value", "operator": "gt", "value": 500 },
    { "field": "customer.wa_opt_in_status", "operator": "eq", "value": "opted_in" }
  ],
  "actions": [
    {
      "type": "send_whatsapp_template",
      "template_id": "cart_recovery_v1",
      "variables": { "name": "{{customer.name}}", "cart_url": "{{cart.url}}" }
    }
  ]
}
```

---

## Directory Map (Repository)

```
conductor-platform/
├── apps/web/              # React frontend
├── services/              # Spring Boot services
│   ├── tenant-service/
│   ├── workflow-service/
│   └── ...
├── workers/
│   └── workflow-worker/   # Temporal worker
├── shared/
│   ├── common-lib/        # Shared Java library
│   └── api-contracts/     # OpenAPI specs
└── infrastructure/
    └── terraform/         # IaC
```

---

## Related Knowledge Pack Files

| File | Content |
|---|---|
| `Domain-Model.md` | All entities, relationships, and field definitions |
| `Glossary.md` | All domain terms defined precisely |
| `Context-Pack.md` | Key decisions, constraints, anti-patterns |
| `Developer-Onboarding.md` | How to set up locally and contribute |
