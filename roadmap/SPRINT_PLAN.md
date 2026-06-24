# Sprint Plan — Conductor

This document outlines the detailed 6-sprint developer execution plan (based on 2-week iterations) to build and deploy the Conductor MVP.

---

## Sprint 1: Foundational Setup (Weeks 1-2)

### Goals
Establish the local development compose stack, execute initial database migrations, configure the Keycloak authentication realm, and implement the gateway-to-monolith tenant injection logic.

### User Stories
*   `STORY-101`: API Gateway Tenant Injection (Tasks T-101, T-102)
*   `STORY-102`: Keycloak Identity Provider Integration (Tasks T-103, T-104)

### Dependencies
None.

### Definition of Done (DoD)
*   All project dependencies compile successfully.
*   Database migrations (`plans`, `tenants`, `subscriptions`, `users`) execute without error on startup.
*   Incoming HTTP requests with valid Keycloak JWTs resolve correctly, and the `X-Tenant-ID` header is propagated.
*   Hibernate interceptor automatically appends `tenant_id` clauses to database SELECT queries.
*   Test suite passes.

---

## Sprint 2: Customer Registry & Consent Ledger (Weeks 3-4)

### Goals
Implement the customer contact management database structure, build REST API endpoints for contact profiles, write the CSV bulk import processor, and set up the immutable consent triggers.

### User Stories
*   `STORY-201`: Customer Profile API & Bulk Import (Tasks T-201, T-202)
*   `STORY-202`: DPDP Compliance (Task T-203 - Consent triggers)

### Dependencies
*   Completion of Sprint 1 (Tenant context resolution).

### Definition of Done (DoD)
*   Customer CRUD endpoints pass functional tests with active tenant separation.
*   CSV parsing of a file containing 10,000 contacts completes asynchronously in under 10 seconds without memory leaks.
*   A database trigger is verified on the `consent_records` table, successfully rejecting any direct `UPDATE` or `DELETE` SQL statements.

---

## Sprint 3: Inbound Webhooks & Event Ingestion (Weeks 5-6)

### Goals
Establish the Node.js WhatsApp adapter, verify incoming webhook signatures, publish parsed messages to NATS JetStream, and implement the priority keyword unsubscribe handling.

### User Stories
*   `STORY-301`: Inbound Webhook Ingestion (Tasks T-301, T-302)

### Dependencies
*   Completion of Sprint 2 (Consent ledger schema).

### Definition of Done (DoD)
*   The Node.js Adapter verifies incoming Meta HMAC signatures.
*   Incoming webhook payloads are converted to system events and published to NATS within 1 second.
*   An inbound WhatsApp message containing the keyword "STOP" updates the contact's consent record in the database within 5 seconds.

---

## Sprint 4: Outbound Dispatcher & DSL Parser (Weeks 7-8)

### Goals
Create the outbound WhatsApp Meta API client, enforce pre-send consent validation gates, write the Workflow JSON DSL parsing service, and configure the local Temporal runtime workers.

### User Stories
*   `STORY-302`: Outbound Campaign Dispatcher (Tasks T-303, T-304)
*   `STORY-401`: Workflow DSL Parser (Task T-401)
*   `STORY-402`: Temporal Workers Setup (Task T-402)

### Dependencies
*   Completion of Sprint 3 (NATS and Webhook adapter).

### Definition of Done (DoD)
*   Workflow JSON definitions are validated against trigger rules and steps constraints.
*   Temporal workers successfully connect and register with the Temporal task queue.
*   Outbound template messages to opted-out contacts are blocked before network transmission.

---

## Sprint 5: Workflow Runtime & Egress Proxy (Weeks 9-10)

### Goals
Code active workflow execution activities in Temporal, configure the Squid egress proxy to prevent SSRF vulnerabilities, and implement Shopify and Zoho CRM webhook mapping controllers.

### User Stories
*   `STORY-402`: Workflow Activities Implementation (Task T-403)
*   `STORY-501`: Webhook Egress Security (Task T-501)
*   `STORY-502`: Shopify & Zoho CRM Connectors (Tasks T-502, T-503)

### Dependencies
*   Completion of Sprint 4 (Temporal worker registration).

### Definition of Done (DoD)
*   A multi-step Temporal workflow successfully runs condition evaluations, delays, and WhatsApp dispatches.
*   The Squid proxy blocks requests targeting private subnets while permitting calls to external APIs.
*   Shopify order webhooks trigger NATS events that start the campaign workflow.

---

## Sprint 6: Billing, Analytics & Compliance (Weeks 11-12)

### Goals
Implement Razorpay payment status webhook maps, configure Metabase analytics embeds using signed JWTs, establish write-once database audit logs, and build the scheduled contact anonymizer task.

### User Stories
*   `STORY-503`: Razorpay Subscriptions (Task T-504)
*   `STORY-202`: DPDP Compliance Anonymization (Task T-204)
*   Analytics Metabase Dashboard Integration

### Dependencies
*   Completion of Sprints 1 through 5.

### Definition of Done (DoD)
*   Razorpay billing webhook events adjust the tenant's workspace plan level.
*   Metabase dashboard embeds filter data by `tenant_id` using a secure token.
*   A scheduled job processes erasure requests and hashes PII contact columns within the 30-day compliance SLA.
