# Architecture Governance Report — Conductor Platform

**Report ID:** GOV-2026-06-Conductor  
**Status:** APPROVED WITH CONDITIONS  
**Date:** June 24, 2026  
**Auditor:** Architecture Governance Agent  

---

## 1. Executive Summary

This report establishes the canonical architecture governance layer for the Conductor Platform. It summarizes the findings from the discovery, consistency review, and validation phases. The objective is to establish strict architectural guardrails, define domain boundaries, and lock in system design choices before implementation agents begin feature development.

---

## 2. Approved Architectural Decision Records (ADR Catalog)

The platform is standardized on an **OSS Assembly Strategy** wrapping custom business logic inside a **Modular Monolith** using **Java 21 / Spring Boot 3.x with Virtual Threads**. 

The 10 core runtime decisions have been validated and approved:

| ADR ID | Decision Title | Status | Core Context & Decision |
| :--- | :--- | :--- | :--- |
| **ADR-001** | [Target Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-001.md) | **Accepted** | Modular Monolith layout using Spring Boot 3.x with Loom Virtual Threads. |
| **ADR-002** | [Multi-Tenancy Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-002.md) | **Accepted** | Shared database with row-level logical partitioning (`tenant_id` column). |
| **ADR-003** | [Workflow Runtime](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-003.md) | **Accepted** | Adopt Temporal Server for durable workflow orchestration via a custom JSON DSL. |
| **ADR-004** | [Messaging Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-004.md) | **Accepted** | Official Meta WhatsApp Cloud API exclusively (unofficial open-source scraping is banned). |
| **ADR-005** | [Event Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-005.md) | **Accepted** | Adopt NATS JetStream as the low-latency asynchronous event streaming engine. |
| **ADR-006** | [Authentication & Authorization](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-006.md) | **Accepted** | Keycloak realms (one per tenant) with Kong API Gateway token verification. |
| **ADR-007** | [Integration Framework](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-007.md) | **Accepted** | Squid egress forward proxy for outbound webhook calls to mitigate SSRF risks. |
| **ADR-008** | [Analytics Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-008.md) | **Accepted** | Metabase embedded via signed JWT iframes, querying a Postgres read-replica. |
| **ADR-009** | [Audit Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-009.md) | **Accepted** | Row-level PostgreSQL database triggers writing to an immutable, partitioned log table. |
| **ADR-010** | [Deployment Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-010.md) | **Accepted** | Local developer sandboxes via Docker-Compose; ECS Fargate cloud runs in AWS Mumbai. |

---

## 3. Open ADRs and Architectural Risks

The following implicit assumptions lack formal EDRB approvals and present active architectural risks:

1.  **ClickHouse CDC Synchronization Strategy:** While ClickHouse is designated for analytics logs, the data pipeline syncing transaction tables (PostgreSQL) to ClickHouse remains undefined.
2.  **Vector Multi-Tenancy (AI Domain):** Qdrant is adopted for RAG contexts storage, but the strategy to ensure strict tenant isolation within vector indexes must be finalized.
3.  **Local Developer Port Collisions:** Port audits indicate conflicts on standard database ports (e.g. 5432, 6379, 8080, 8123) with pre-existing services on developer workstations.

---

## 4. Governance Specifications Directory

The detailed standards governing different dimensions of the platform are published in the following catalogs:

*   **[ADR Inventory](file:///c:/Users/rajaj/Projects/Conductor/ADR_INVENTORY.md):** The discovery log mapping explicit and implicit decision records.
*   **[ADR Validation Report](file:///c:/Users/rajaj/Projects/Conductor/ADR_VALIDATION_REPORT.md):** Detail of contexts, consequences, and validation scopes for core runtime ADRs.
*   **[Domain Boundaries Catalog](file:///c:/Users/rajaj/Projects/Conductor/DOMAIN_BOUNDARIES.md):** The bounded contexts defining purposes, data ownership, APIs, events, and consumers for the 10 target domains.
*   **[Event Governance Specification](file:///c:/Users/rajaj/Projects/Conductor/EVENT_GOVERNANCE.md):** Event schemas lints, naming formats, retention schedules, and Dead Letter Queue (DLQ) policies.
*   **[API Governance Specification](file:///c:/Users/rajaj/Projects/Conductor/API_GOVERNANCE.md):** REST URI standards, OIDC realm filters, rate limiting, and idempotency guarantees.
*   **[Data Governance Specification](file:///c:/Users/rajaj/Projects/Conductor/DATA_GOVERNANCE.md):** Row-level tenant isolation, PII data tier classifications, backups, and DPDP residency mappings.
*   **[Architecture Guardrails Specification](file:///c:/Users/rajaj/Projects/Conductor/ARCHITECTURE_GUARDRAILS.md):** The list of mandatory coding and design constraints (e.g., no cross-module DB reads, mandatory proxy routes).
*   **[Agent Operating Matrix](file:///c:/Users/rajaj/Projects/Conductor/AGENT_OWNERSHIP_MATRIX.md):** Access privileges, exclusive directory scopes, and approval gates for autonomous development agents.
*   **[Architecture Scorecard](file:///c:/Users/rajaj/Projects/Conductor/ARCHITECTURE_SCORECARD.md):** Rating score reviews (0-10) for core architecture pillars, detailing gaps, risks, and mitigations.

---

## 5. Implementation Constraints for Future Agents

Autonomous coding agents must operate under the following structural constraints:
*   **No Cross-Boundary Imports:** Services in `com.conductor.workflow` cannot import classes from `com.conductor.customer` directly. Inter-service calls must use NATS event publishes or OTel-traced REST endpoints.
*   **Logical DB Isolation:** Write queries must inherit base Hibernate configuration contexts that automatically inject `AND tenant_id = :activeTenant`.
*   **Egress Control:** Egress requests to third-party endpoints (e.g. Zoho, Shopify, custom webhook callbacks) must route via the Squid egress proxy.
*   **Mumbai Residency:** All data stores and backup buckets must reside in the AWS Mumbai region (`ap-south-1`) to comply with India's DPDP Act.

---

## 6. Approval Status & Conditions

This architecture governance layer is **APPROVED WITH CONDITIONS**.

### Conditions for Beta Release
1.  **ArchUnit Setup:** Enforce modular monolith directory boundaries via automated compile-time ArchUnit tests in the root Maven/Gradle verify cycle.
2.  **Vector Isolation Verification:** Finalize metadata payload tag filters in Qdrant before loading customer vectors.
3.  **Port Overrides:** Implement environment-override settings in the `.env.local` template to resolve developer port collisions.
