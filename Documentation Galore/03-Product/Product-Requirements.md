# Product Requirements — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** MVP Scope, Technical Layers, Business Needs  
**Last Updated:** June 2026

---

## Purpose
Defines all functional and non-functional requirements for MVP V1 and Phase 2 of the Conductor platform.

---

## Requirement Classification

- **Priority:** P0 (MVP blocking), P1 (MVP required), P2 (Phase 2), P3 (Phase 3 / Backlog)
- **Type:** Functional (FR), Non-Functional (NFR), Constraint (CON)
- **Status:** Draft | Review | Approved | Implemented

---

## Module 1: Tenant Management

| ID | Requirement | Priority | Type |
|---|---|---|---|
| TM-001 | System SHALL allow a new business to self-register with name, email, password, and industry vertical | P0 | FR |
| TM-002 | System SHALL create an isolated data namespace (tenant_id) for each registered business | P0 | FR |
| TM-003 | System SHALL assign a default plan (Free Trial) to a new tenant at registration | P0 | FR |
| TM-004 | System SHALL allow a tenant to update their business profile (name, logo, timezone, currency) | P0 | FR |
| TM-005 | System SHALL enforce plan-based feature gates (workflow limits, message limits, user limits) per tenant | P0 | FR |
| TM-006 | System SHALL support tenant suspension (all outbound messages paused, data retained) | P1 | FR |
| TM-007 | System SHALL support tenant deletion with DPDP-compliant 30-day data retention before purge | P1 | FR |
| TM-008 | System SHALL ensure no tenant can access another tenant's data under any circumstances | P0 | NFR |
| TM-009 | System SHALL support up to 10,000 tenants on shared infrastructure | P2 | NFR |

---

## Module 2: User & Access Management (IAM)

| ID | Requirement | Priority | Type |
|---|---|---|---|
| IAM-001 | System SHALL support email/password authentication for all users | P0 | FR |
| IAM-002 | System SHALL support OTP (SMS or email) as 2FA for all users | P1 | FR |
| IAM-003 | System SHALL define 5 roles: OWNER, ADMIN, MANAGER, AGENT, ANALYST | P0 | FR |
| IAM-004 | System SHALL enforce RBAC permissions — AGENT role cannot access workflow configuration | P0 | FR |
| IAM-005 | System SHALL allow OWNER to invite additional users to their tenant | P0 | FR |
| IAM-006 | System SHALL allow OWNER to deactivate or remove team members | P0 | FR |
| IAM-007 | System SHALL invalidate sessions on logout (token blacklisting or short-lived JWTs) | P0 | FR |
| IAM-008 | System SHALL expire sessions after 24 hours of inactivity | P0 | NFR |
| IAM-009 | System SHALL log all authentication events (login, logout, failed attempts) | P0 | FR |
| IAM-010 | System SHALL support Google SSO for login | P2 | FR |
| IAM-011 | System SHALL support API key generation and revocation for service-to-service integration | P1 | FR |
| IAM-012 | System SHALL implement Keycloak as the IAM provider | P0 | CON |

---

## Module 3: Customer Registry

| ID | Requirement | Priority | Type |
|---|---|---|---|
| CR-001 | System SHALL store customer records with: phone, name, email, tags, segments, consent status | P0 | FR |
| CR-002 | System SHALL support bulk contact import via CSV (up to 10,000 records per batch) | P0 | FR |
| CR-003 | System SHALL deduplicate contacts by phone number within a tenant | P0 | FR |
| CR-004 | System SHALL allow manual addition of customer records from the UI | P0 | FR |
| CR-005 | System SHALL support custom attributes on customer records (key-value, per-tenant schema) | P0 | FR |
| CR-006 | System SHALL display conversation history for each customer | P0 | FR |
| CR-007 | System SHALL display workflow execution history for each customer | P1 | FR |
| CR-008 | System SHALL support tag-based customer segmentation | P0 | FR |
| CR-009 | System SHALL support dynamic segments based on attribute conditions (Phase 2) | P2 | FR |
| CR-010 | System SHALL NOT expose one tenant's customer records to any other tenant | P0 | NFR |
| CR-011 | System SHALL support API-based contact import (REST endpoint) | P1 | FR |

---

## Module 4: Workflow Engine

| ID | Requirement | Priority | Type |
|---|---|---|---|
| WE-001 | System SHALL allow users to create workflows in a visual no-code designer | P0 | FR |
| WE-002 | System SHALL support Trigger types: event-based, schedule-based (cron), manual, webhook | P0 | FR |
| WE-003 | System SHALL support Condition operators: eq, ne, gt, lt, gte, lte, contains, in_list, is_empty | P0 | FR |
| WE-004 | System SHALL support Action types: send_whatsapp_template, send_whatsapp_message, delay, branch, update_customer, add_tag, trigger_api | P0 | FR |
| WE-005 | System SHALL execute workflows durably — a crash/restart shall not lose in-flight executions | P0 | NFR |
| WE-006 | System SHALL enforce workflow execution limits: max 50 executions per customer per workflow per day | P0 | FR |
| WE-007 | System SHALL support circuit-breaker: disable workflow after 100 consecutive failures | P0 | FR |
| WE-008 | System SHALL log every workflow execution with trigger, conditions evaluated, actions taken, and outcome | P0 | FR |
| WE-009 | System SHALL support workflow enable/disable without deletion | P0 | FR |
| WE-010 | System SHALL allow workflow testing with a specific test customer record before activation | P1 | FR |
| WE-011 | System SHALL enforce plan limits on active workflow count per tenant | P0 | FR |
| WE-012 | Workflow execution SHALL complete within 30 seconds for 99% of executions | P0 | NFR |
| WE-013 | System SHALL use Temporal as the workflow execution runtime | P0 | CON |

---

## Module 5: Conversation Engine

| ID | Requirement | Priority | Type |
|---|---|---|---|
| CE-001 | System SHALL support two-way WhatsApp conversations triggered by inbound customer messages | P0 | FR |
| CE-002 | System SHALL maintain conversation context (current step, collected data) across message turns | P0 | FR |
| CE-003 | System SHALL support menu-based conversation flows (numbered options: reply 1, 2, 3) | P0 | FR |
| CE-004 | System SHALL support slot-filling (collect name, email, phone in sequence) | P1 | FR |
| CE-005 | System SHALL detect STOP, UNSUBSCRIBE keywords and immediately opt-out the customer | P0 | FR |
| CE-006 | System SHALL trigger agent handoff when a workflow reaches a "route_to_agent" action | P1 | FR |
| CE-007 | System SHALL expire conversation sessions after 24 hours of inactivity | P0 | FR |
| CE-008 | System SHALL support fallback responses when customer input is not recognized | P0 | FR |
| CE-009 | System SHALL support NLU-based intent detection (Phase 2) | P2 | FR |
| CE-010 | System SHALL integrate with Chatwoot for the agent shared inbox | P1 | FR |

---

## Module 6: WhatsApp Adapter

| ID | Requirement | Priority | Type |
|---|---|---|---|
| WA-001 | System SHALL use the official WhatsApp Cloud API (Meta Graph API) for all message delivery | P0 | CON |
| WA-002 | System SHALL support sending WhatsApp Business approved templates | P0 | FR |
| WA-003 | System SHALL support sending free-form text messages within the 24-hour customer service window | P0 | FR |
| WA-004 | System SHALL support sending media messages (images, PDFs, documents) | P1 | FR |
| WA-005 | System SHALL support interactive button messages (up to 3 buttons) | P1 | FR |
| WA-006 | System SHALL support interactive list messages (up to 10 items) | P1 | FR |
| WA-007 | System SHALL process inbound WhatsApp webhooks within 5 seconds of receipt | P0 | NFR |
| WA-008 | System SHALL retry failed message deliveries up to 3 times with exponential backoff | P0 | FR |
| WA-009 | System SHALL track delivery status (sent, delivered, read, failed) for each message | P0 | FR |
| WA-010 | System SHALL NOT use unauthorized/unofficial WhatsApp libraries (OpenWA, Baileys, etc.) | P0 | CON |
| WA-011 | System SHALL support multiple WhatsApp Business numbers per tenant (per plan limits) | P0 | FR |
| WA-012 | System SHALL surface Meta-imposed rate limits and handle gracefully (queue, retry) | P0 | FR |

---

## Module 7: Template Management

| ID | Requirement | Priority | Type |
|---|---|---|---|
| TMP-001 | System SHALL allow users to create WhatsApp message templates via the UI | P0 | FR |
| TMP-002 | System SHALL submit templates to Meta for approval via WhatsApp Cloud API | P0 | FR |
| TMP-003 | System SHALL track template approval status (pending, approved, rejected) | P0 | FR |
| TMP-004 | System SHALL notify users when a template is approved or rejected by Meta | P0 | FR |
| TMP-005 | System SHALL support variable binding ({{1}}, {{2}}) in templates | P0 | FR |
| TMP-006 | System SHALL support multi-language templates | P1 | FR |
| TMP-007 | System SHALL prevent workflows from using rejected or unapproved templates | P0 | FR |

---

## Module 8: Integration Hub

| ID | Requirement | Priority | Type |
|---|---|---|---|
| IH-001 | System SHALL provide a Shopify connector with: order.created, order.shipped, cart.abandoned events | P0 | FR |
| IH-002 | System SHALL provide a Razorpay connector with: payment.completed, payment.failed events + generate_payment_link action | P0 | FR |
| IH-003 | System SHALL provide a Google Calendar connector for appointment.created, appointment.cancelled events | P0 | FR |
| IH-004 | System SHALL provide a Zoho CRM connector for lead.created, deal.updated events + create_contact action | P1 | FR |
| IH-005 | System SHALL authenticate connectors via OAuth 2.0 where available, API keys where not | P0 | FR |
| IH-006 | System SHALL allow users to connect/disconnect integrations from the UI | P0 | FR |
| IH-007 | System SHALL validate connector credentials on setup and alert if invalid | P0 | FR |
| IH-008 | System SHALL support inbound webhooks (any external system can post events to Conductor) | P1 | FR |
| IH-009 | System SHALL publish a Connector SDK for third-party connector development (Phase 2) | P2 | FR |

---

## Module 9: Campaign Engine

| ID | Requirement | Priority | Type |
|---|---|---|---|
| CAM-001 | System SHALL support broadcast campaigns (one-time message to a segment) | P0 | FR |
| CAM-002 | System SHALL require a WhatsApp approved template for all broadcast campaigns | P0 | FR |
| CAM-003 | System SHALL support campaign scheduling (send at specific date/time) | P0 | FR |
| CAM-004 | System SHALL enforce frequency capping: max 1 marketing message per customer per day | P0 | FR |
| CAM-005 | System SHALL provide campaign analytics: sent, delivered, read, reply count | P0 | FR |
| CAM-006 | System SHALL support drip campaigns (timed sequence) | P2 | FR |
| CAM-007 | System SHALL support A/B testing for campaign templates | P2 | FR |

---

## Module 10: Analytics

| ID | Requirement | Priority | Type |
|---|---|---|---|
| AN-001 | System SHALL provide a dashboard with: messages sent, delivered, read, failed today/this week/this month | P0 | FR |
| AN-002 | System SHALL track and display workflow execution counts and success/failure rates | P0 | FR |
| AN-003 | System SHALL track and display campaign performance metrics | P0 | FR |
| AN-004 | System SHALL display customer growth over time | P0 | FR |
| AN-005 | System SHALL allow analytics data export (CSV) | P1 | FR |
| AN-006 | System SHALL use Metabase as the embedded analytics engine | P0 | CON |

---

## Module 11: Consent & Compliance

| ID | Requirement | Priority | Type |
|---|---|---|---|
| CC-001 | System SHALL track WhatsApp opt-in consent for each customer (status, date, source, consent text) | P0 | FR |
| CC-002 | System SHALL block outbound marketing messages to customers without WhatsApp opt-in | P0 | FR |
| CC-003 | System SHALL immediately opt-out a customer who replies STOP, UNSUBSCRIBE, or any locale equivalent | P0 | FR |
| CC-004 | System SHALL log all consent changes with timestamp and actor | P0 | FR |
| CC-005 | System SHALL provide a consent export report for audit purposes | P1 | FR |
| CC-006 | System SHALL provide a Data Processing Agreement (DPA) template for tenants to use with their customers | P2 | FR |
| CC-007 | System SHALL support customer data deletion requests (right to erasure) within 30 days | P1 | FR |

---

## Non-Functional Requirements (Platform-Wide)

| ID | Requirement | Priority | Target |
|---|---|---|---|
| NFR-001 | Platform API availability | P0 | 99.5% uptime (MVP) → 99.9% (Phase 2) |
| NFR-002 | API response time (p95) | P0 | < 300ms for read endpoints, < 500ms for write endpoints |
| NFR-003 | Message delivery processing time | P0 | < 5 seconds from event to WhatsApp send |
| NFR-004 | Data encryption at rest | P0 | AES-256 for all PII fields |
| NFR-005 | Data encryption in transit | P0 | TLS 1.3 for all connections |
| NFR-006 | Audit log retention | P0 | 1 year minimum |
| NFR-007 | Backup and recovery | P0 | Daily backups, RTO 4h, RPO 1h (MVP) |
| NFR-008 | DPDP India compliance | P0 | Must be compliant at MVP launch |
| NFR-009 | WhatsApp Business Policy compliance | P0 | Mandatory — non-compliance risks WABA suspension |
| NFR-010 | Platform scalability | P1 | Handle 1,000 concurrent tenants |

---

## Cross-References
- `03-Product/Capabilities.md` — Capability specifications mapped to requirements
- `03-Product/User-Stories.md` — User stories derived from these requirements
- `04-Architecture/Application-Architecture.md` — Service design implementing requirements
- `05-Engineering/API-Contracts.md` — API contracts satisfying integration requirements
