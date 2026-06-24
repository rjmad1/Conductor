# Glossary — Conductor IDE Knowledge Pack

**Purpose:** Precise definitions of all domain terms. When you encounter an ambiguous term, look here first.

---

## A

**Action**
The "do Z" part of a Trigger → Condition → Action workflow. Actions are things the platform does when conditions are met: send a WhatsApp message, update a customer record, add a tag, trigger an API call. Actions are stored as JSONB in the `workflows.actions` column.

**Agent (AI)**
An AI-powered conversational component that can reason, use tools, and complete multi-turn tasks (e.g., booking an appointment). Different from a human agent. Agents are a Phase 2/3 feature; MVP uses deterministic workflow scripts.

**Agent (Human)**
A member of a tenant's customer support team who handles conversations via the shared inbox (Chatwoot). Role in RBAC: `AGENT`.

**Audit Log**
An append-only, immutable record of significant platform events (data access, exports, consent changes, role assignments). Stored in `audit_logs` table. Required for DPDP compliance.

---

## B

**Business Capability**
One of the 15 platform-level automation templates that expose business outcomes rather than technical primitives. Example: "Recover Abandoned Carts" is a business capability; "send a WhatsApp template message" is a technical primitive.

**Broadcast Campaign**
A one-time bulk WhatsApp message sent to a customer segment using an approved template. Contrast with: drip campaign.

---

## C

**Capability Pack**
A bundled set of pre-built workflows, templates, and automations tailored for a specific vertical (e.g., Healthcare Pack, Retail Pack). Tenants activate a Capability Pack to get started faster.

**Condition**
The "if Y is true" part of a Trigger → Condition → Action workflow. Conditions filter whether a workflow should execute for a given event. Example: `cart.value > 500`. All conditions must be true for the workflow to proceed.

**Conductor Platform**
The full SaaS product described in this repository. Not to be confused with Apache Kafka's conductor or any other software named Conductor.

**Consent Record**
An immutable, append-only database record capturing a customer's opt-in or opt-out event. Contains: consent text, method, timestamp. Never updated or deleted. The most recent record per customer/channel determines current status.

**Conversation**
A stateful session between a customer phone number and a tenant's WhatsApp number. Conversations have a 24-hour window in WhatsApp's model (business-initiated vs. user-initiated charges differ). Managed by `conversation-service`.

**Conversation Service**
The Spring Boot service that manages multi-turn conversation state machines. Determines whether an inbound message belongs to an active menu flow, a workflow step, or should be routed to a human agent.

---

## D

**Design Partner**
An early adopter business that participates in closed beta, provides feedback, and receives a discounted or free plan. Target: 3-5 design partners before public launch.

**Dify**
Open-source LLM workflow platform used for orchestrating AI capabilities in Phase 2/3. Self-hosted in production for data residency compliance.

**DPDP**
Digital Personal Data Protection Act (India, 2023). The primary data protection law governing Conductor's collection and processing of customer personal data. Key obligations: consent, notice, erasure rights, data localization, breach notification.

**DPBI**
Data Protection Board of India. The regulatory body under DPDP. Breach notifications must be submitted to DPBI within 72 hours.

**Drip Campaign**
A time-based sequence of WhatsApp messages sent to customers at defined intervals (e.g., Day 1, Day 3, Day 7 after signup). Contrast with: broadcast campaign.

---

## E

**Event**
A discrete occurrence at a defined moment in time. Events are facts: they are immutable once published. All platform events conform to the Event Envelope schema and are published to NATS. Example: `conductor.{tenantId}.cart.abandoned`.

**Event Envelope**
The universal wrapper for all Conductor events. Contains: `event_id`, `event_type`, `tenant_id`, `source`, `schema_version`, `occurred_at`, `ingested_at`, `payload`.

---

## F

**Flyway**
The database migration tool used to manage PostgreSQL schema changes. All schema changes must be implemented as Flyway migration files. Never modify an existing migration file.

---

## G

**Growth Plan**
The ₹4,999/month subscription tier. Target: SMBs with 5,000–20,000 customers and moderate automation needs. Key limits: 5,000 active customers, 10 workflows, 5 users.

---

## H

**HMAC**
Hash-based Message Authentication Code. Used to verify the authenticity of webhooks from WhatsApp, Shopify, and Razorpay. Always validate HMAC using `MessageDigest.isEqual()` (constant-time comparison) — never using `String.equals()` (timing attack vulnerable).

---

## I

**Integration / Connector**
A third-party system connected to Conductor that provides triggers and/or actions. MVP connectors: Shopify, Zoho CRM, Razorpay, Google Calendar.

---

## J

**JetStream**
NATS JetStream is the persistence and streaming layer of NATS. Required for production use so that events survive NATS restarts. JetStream stores events in file-based storage with configurable retention (7 days for Conductor events).

---

## K

**Keycloak**
Open-source IAM (Identity and Access Management) platform. Manages authentication, JWT issuance, and RBAC enforcement. Each Conductor tenant gets a dedicated Keycloak realm.

**Kong**
Open-source API Gateway. Validates JWTs, enforces rate limits, and routes requests to backend services. The entry point for all external traffic.

---

## M

**Message**
A single WhatsApp message sent or received. Stored in the partitioned `messages` table. Each message has a `wa_message_id` (from Meta), `direction`, `status`, and `content`.

**Message Quality Rating**
Meta's rating of how customers respond to messages from a WhatsApp Business Account. Red rating = high block rate → risk of WABA suspension. Must be monitored continuously.

**Meta**
Facebook/Meta Platforms, Inc. Conductor's most critical external dependency (WhatsApp Cloud API).

**Multi-tenancy**
The architectural pattern where a single deployment of Conductor serves multiple business customers (tenants), with complete data isolation between them. All tenant data is scoped by `tenant_id`.

---

## N

**NATS**
A lightweight, high-performance messaging system used as Conductor's event bus. NATS JetStream provides persistence. Contrast with: Kafka (considered but not used for MVP — see ADR-002).

---

## O

**OpenWA / Baileys**
PROHIBITED. Unofficial/reverse-engineered WhatsApp libraries. Using these violates Meta's ToS and could result in permanent WABA suspension. Conductor uses the official WhatsApp Cloud API only. See ADR-003.

**Opt-in**
Explicit customer consent to receive messages. Required before sending marketing messages. Stored as a Consent Record with `status=opted_in`.

**Opt-out**
Customer revocation of consent, usually by sending "STOP". Must be processed within 5 seconds. Stored as a Consent Record with `status=opted_out`. After opt-out, no marketing messages may be sent.

---

## P

**pgvector**
A PostgreSQL extension for vector similarity search. Used in Phase 2 for RAG (retrieval-augmented generation) FAQ bots.

**PII**
Personally Identifiable Information. Includes: phone numbers, names, email addresses, message content. Must NOT appear in logs. Must be encrypted at rest for sensitive fields. Must be localized to India (AWS ap-south-1).

**Plan**
A Conductor subscription tier (Starter ₹1,499, Growth ₹4,999, Business ₹12,999, Enterprise custom). Plans define limits on active customers, workflows, users, messages/month, and feature access.

---

## R

**RAG**
Retrieval-Augmented Generation. A technique where relevant documents are retrieved from a knowledge base and included in an LLM prompt, enabling accurate, grounded responses. Used in Phase 2 for FAQ bots.

**Razorpay**
Indian payment gateway used for subscription billing and as a payment link connector. Webhooks are validated with HMAC-SHA256.

**RBAC**
Role-Based Access Control. Five roles in Conductor: OWNER, ADMIN, MANAGER, AGENT, ANALYST. Enforced via Keycloak realm roles and Kong JWT validation.

---

## S

**Segment**
A filtered subset of a tenant's customer list. Used as the target for campaigns. Segments are dynamic (re-evaluated at send time) or static (snapshot at creation time).

**SMB**
Small and Medium Business. Conductor's target customer: 2-50 employees, using WhatsApp as a primary customer communication channel.

**STOP**
The keyword customers send to opt out of WhatsApp messages. Conductor must detect "STOP" (case-insensitive) and process the opt-out within 5 seconds. This is a hard requirement under WhatsApp Business Policy.

---

## T

**Template (WhatsApp)**
A pre-approved message format submitted to Meta. Required for all business-initiated WhatsApp messages (marketing, notifications). Approval takes 1-3 business days. Categories: MARKETING, UTILITY, AUTHENTICATION.

**Temporal**
Open-source durable workflow execution engine. Used to execute Conductor's automation workflows reliably, with retries, timeouts, and activity isolation. Chosen over Camunda 8 (see ADR-001).

**Tenant**
A business customer of Conductor. One company = one tenant. Each tenant has isolated data, a dedicated Keycloak realm, and their own WhatsApp Business Account.

**Trigger**
The "when X happens" part of a Trigger → Condition → Action workflow. Triggers are events that initiate workflow evaluation. Types: event-based (cart.abandoned), schedule-based (cron), webhook, or manual.

---

## V

**WABA**
WhatsApp Business Account. The Meta account associated with a phone number that can send WhatsApp messages via the Cloud API. Each tenant registers their own WABA. WABA suspension is an existential risk (see Risk Register R-001).

**Workflow**
A Conductor automation rule expressed as Trigger → Conditions → Actions. Stored as JSON (DSL) in PostgreSQL. Executed by Temporal workers.

**Workflow DSL**
The JSON format used to define Conductor workflows. Contains: `trigger` (object), `conditions` (array), `actions` (array). Stored in `workflows.trigger_config`, `workflows.conditions`, and `workflows.actions` columns.

**Workflow Engine**
The subsystem that evaluates whether incoming events match active workflows and dispatches matching workflows to Temporal for execution. Implemented in `workflow-service`.

---

## Z

**Zoho CRM**
A CRM platform used by many Indian SMBs. Supported as an MVP connector — enables customer sync and CRM-triggered automations.
