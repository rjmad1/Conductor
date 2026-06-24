# System Context — Conductor

**Status:** Partially Extracted + Extended (⚡ where inferred)  
**Source:** Technical Layers document  
**Last Updated:** June 2026

---

## Purpose
Defines Conductor's external actors, system boundaries, and all integration points. This is the C4 Level 1 diagram equivalent.

---

## System Boundary

Conductor is a **multi-tenant SaaS platform**. Everything inside the boundary is built and operated by Conductor. Everything outside is either a customer system, an external API, or a third-party service.

---

## External Actors

### Human Actors

| Actor | Description | Interaction |
|---|---|---|
| **Business Owner (Tenant Admin)** | The SMB that purchases and configures Conductor | Configures workflows, views analytics, manages billing via Conductor Web App |
| **Business Agent / Staff** | Employees of the SMB who handle escalated conversations | Uses the shared inbox (Chatwoot-powered) to respond to customers |
| **End Customer** | The consumer of the SMB's services | Sends/receives WhatsApp messages via the SMB's WhatsApp number |
| **ISV Developer** | Third-party developer building connectors or capability packs | Accesses Connector SDK + Developer Portal (Phase 2) |
| **Conductor Platform Admin** | Internal Conductor operations team | Manages tenant health, billing disputes, platform monitoring |

---

### External Systems

#### Communication Channels
| System | Role | Integration Type |
|---|---|---|
| **WhatsApp Cloud API (Meta)** | Primary message delivery channel | REST API + Webhooks |
| **Twilio / Exotel (SMS)** | SMS fallback channel (Phase 2) | REST API |
| **Email SMTP (SendGrid)** | Transactional email (notifications, invoices) | REST API |

#### Business Integrations (Connectors)
| System | Role | Integration Type |
|---|---|---|
| **Shopify** | E-commerce events (orders, carts) | REST API + Webhooks |
| **Zoho CRM** | CRM lead and contact sync | REST API |
| **Razorpay** | Payment events + payment link generation | REST API + Webhooks |
| **Google Calendar** | Appointment events | REST API + Webhooks |
| **WooCommerce** | E-commerce events (Phase 2) | REST API + Webhooks |
| **Freshdesk** | Helpdesk ticket events (Phase 2) | REST API |
| **HubSpot** | CRM sync (Phase 2) | REST API |
| **Tally** | Accounting integration (Phase 2) | REST API |

#### Identity & Access
| System | Role | Integration Type |
|---|---|---|
| **Keycloak** | Authentication, SSO, RBAC, multi-tenant IAM | OpenID Connect / OAuth 2.0 |
| **Google OAuth** | Social login provider (Phase 2) | OAuth 2.0 |

#### Infrastructure & Platform Services ⚡
| System | Role | Integration Type |
|---|---|---|
| **AWS / GCP** | Cloud infrastructure (compute, storage, networking) | Cloud provider APIs |
| **Meta Business Manager** | WhatsApp Business Account (WABA) management | Meta Graph API |
| **SendGrid** | Transactional email delivery | REST API |
| **Razorpay (Billing)** | Platform subscription billing | REST API |
| **Sentry** | Error tracking and alerting | SDK |
| **Prometheus + Grafana** | Metrics and dashboards | Prometheus scrape |
| **PagerDuty** | On-call alerting (Phase 2) | Webhook |

---

## System Context Diagram (Textual C4)

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CONDUCTOR PLATFORM                          │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │  Web App     │  │  API Gateway │  │  Background Services     │  │
│  │  (React)     │  │  (Kong)      │  │  (Temporal Workers)      │  │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘  │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │  Services    │  │  Event Bus   │  │  Data Layer              │  │
│  │  (Spring     │  │  (NATS)      │  │  (PostgreSQL, Redis)     │  │
│  │   Boot)      │  │              │  │                          │  │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘  │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
         ▲                    ▲                       ▲
         │                    │                       │
   ┌─────┴──────┐      ┌──────┴──────┐        ┌──────┴──────┐
   │ Business   │      │ WhatsApp    │        │ External    │
   │ Owner /    │      │ Cloud API   │        │ Connectors  │
   │ Staff      │      │ (Meta)      │        │ Shopify,    │
   │ (Web App)  │      │             │        │ Razorpay,   │
   └────────────┘      └─────────────┘        │ Zoho, etc.  │
         ▲                    ▲                └─────────────┘
         │                    │
   ┌─────┴──────┐      ┌──────┴──────┐
   │ End        │      │ Keycloak    │
   │ Customer   │      │ (IAM)       │
   │ (WA msg)   │      │             │
   └────────────┘      └─────────────┘
```

---

## Key Integration Contracts

### WhatsApp Cloud API
- **Inbound:** Meta posts webhook to `POST /webhooks/whatsapp` when a message is received
- **Outbound:** Conductor calls `POST /v18.0/{phone-number-id}/messages` to send messages
- **Template submission:** `POST /v18.0/{waba-id}/message_templates`
- **Auth:** Meta App access token (long-lived, stored encrypted in secrets manager)
- **Rate limits:** 1,000 business-initiated conversations per day per number (tier 1 WABA)

### Keycloak
- All API requests pass a JWT issued by Keycloak
- Conductor services validate JWTs against Keycloak's JWKS endpoint
- Tenant ID is embedded in the JWT claim: `tenant_id`
- Roles are embedded as: `realm_roles` claim

### External Connectors
- Connectors authenticate via OAuth 2.0 authorization code flow (stored refresh token)
- Connector webhooks arrive at: `POST /webhooks/{connector_id}`
- Events are normalized to the Conductor event envelope before dispatch to NATS

---

## Data Flows (Summary)

### Inbound Message Flow
```
End Customer (WhatsApp) → Meta servers → Conductor webhook endpoint
→ Webhook service → NATS event bus → Conversation Engine / Workflow Engine
→ Response action → WhatsApp Cloud API → End Customer
```

### Outbound Automated Message Flow
```
External event (Shopify webhook) → Conductor webhook endpoint
→ Normalize event → NATS event bus → Workflow Engine (Temporal)
→ Evaluate conditions → Execute action: Send WhatsApp
→ WhatsApp Cloud API → End Customer
```

### Tenant Admin Flow
```
Business Owner (Browser) → React Web App → Kong API Gateway
→ Authentication (Keycloak JWT validation) → Spring Boot Services
→ PostgreSQL / Redis → Response
```

---

## Security Boundary Notes
- All external traffic enters through Kong API Gateway
- No direct access to internal services from the internet
- Webhook endpoints are verified (HMAC signature validation for each connector)
- PII data never logged in application logs
- Tenant data isolation enforced at database level (tenant_id on every row) and application level

---

## Cross-References
- `04-Architecture/Solution-Architecture.md` — Component-level design
- `04-Architecture/Integration-Architecture.md` — Connector integration details
- `04-Architecture/Security-Architecture.md` — Security controls at each boundary
