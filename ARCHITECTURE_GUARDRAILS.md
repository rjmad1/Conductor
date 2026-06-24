# Architecture Guardrails — Conductor Platform

This document defines the mandatory architectural constraints, design patterns, and programming rules for the Conductor Platform. These guardrails are enforced through automated linting, compilation checks, and EDRB evaluations.

---

## 1. Core Database & Service Separation Guardrails

*   **Rule G-001 (No Shared Database Access):** No application service or module may directly access another service's database tables or write SQL joining tables owned by different domains. All cross-module data retrieval must go through REST APIs or asynchronous event handlers.
*   **Rule G-002 (Single Source of Truth):** Each service owns its schema and is the sole system of record for its domain. For example, `com.conductor.workflow` is the only service that writes workflow templates.
*   **Rule G-003 (Stateless Execution Core):** Service classes must remain strictly stateless. Stateful processes (including schedules, retry loops, and async campaigns) must be executed by Temporal workflow workers or published to NATS JetStream.

---

## 2. Multi-Tenancy & Data Isolation Guardrails

*   **Rule G-004 (Mandatory Tenant Context):** Every API request must carry a validated tenant identifier. The ingress gateway must inject this context header (`X-Tenant-ID`) to downstream services.
*   **Rule G-005 (Automated Row-Level Isolation):** All transactional tables containing tenant-scoped data must include a `tenant_id` column. Spring Boot services must implement automated query filters (e.g., Hibernate `@Filter` mapped to thread-local contexts) so that `tenant_id = :activeTenant` is automatically appended to all SELECT, UPDATE, and DELETE queries.

---

## 3. Communication and Interface Guardrails

*   **Rule G-006 (Strict API Versioning):** Every REST API endpoint must include a major version prefix in the URL path (`/api/v1/...`). API schemas are versioned and backward compatible.
*   **Rule G-007 (Strict Event Versioning):** Every NATS JetStream event subject must comply with the `conductor.{tenantId}.{domain}.{entity}.{action}` naming convention. The event metadata wrapper must contain a SemVer mapped `specVersion` key.

---

## 4. Compliance and Safety Guardrails

*   **Rule G-008 (Mandatory Squid Proxy for Egress):** No custom backend service or integration flow may execute direct HTTP requests to the public internet. All outbound integrations (Zoho CRM, Shopify, custom tenant webhook calls) must route through the outbound Squid forward proxy to protect against SSRF.
*   **Rule G-009 (Strict WhatsApp Channel Compliance):** All WhatsApp communications must go through the official Meta WhatsApp Cloud API. The use of unofficial automation or scraping tools (e.g. OpenWA, Baileys) is strictly banned due to the risk of permanent WABA suspensions.
*   **Rule G-010 (Consent Check Precedence):** The messaging engine must query the Customer Domain's consent ledger (`consent_records`) before dispatching any template message campaigns.

---

## 5. Operations & Audit Guardrails

*   **Rule G-011 (Mandatory Audit Logging):** All user settings alterations, permission upgrades, API key creations, and workflow definition updates must trigger database-level audit triggers writing logs to the immutable, partitioned `audit_logs` registry.
*   **Rule G-012 (Observability Instrumentation):** Every API handler and NATS event subscriber must propagate W3C trace context headers (`traceparent`), injecting correlation identifiers into JSON logs and publishing metrics to the central OTel Collector.
