# Technical Gaps — Conductor

**Status:** Review Board Assessment  
**Source:** Gap analysis of source documents against technical requirements  
**Last Updated:** June 2026

---

## Critical Technical Gaps

### T-C1: Architecture Conflict (Camunda vs. Temporal / Kafka vs. NATS)
**Gap:** Source documents recommend two contradictory technology stacks.  
**Resolution:** ADR-001 and ADR-002 in `07-Governance/Decision-Records.md`.  
**Decision:** Temporal + NATS for MVP.  
**Status:** RESOLVED — documented and decided

### T-C2: OpenWA Usage Recommended (ToS Violation)
**Gap:** Source documents recommend OpenWA, which violates Meta's ToS.  
**Resolution:** ADR-003 — WhatsApp Cloud API only.  
**Status:** RESOLVED — documented and enforced in coding standards

### T-C3: No Security Architecture
**Gap:** Source documents have zero security design: no authentication details, no encryption strategy, no threat model.  
**Generated solution:** `04-Architecture/Security-Architecture.md` — complete security design.  
**Risk if unresolved:** Critical — platform cannot launch without security controls.  
**Status:** Documented ⚡ — penetration testing required before launch

### T-C4: No Data Model / Schema Definitions
**Gap:** Source documents mention data entities (customers, workflows) but provide no database schema, relationships, or constraints.  
**Generated solution:** `04-Architecture/Data-Architecture.md` and `05-Engineering/Schema-Definitions.md` — complete DDL.  
**Status:** Documented ⚡ — engineering review required before implementation

### T-C5: No API Contract Specifications
**Gap:** Source documents have no API documentation, endpoint definitions, or request/response schemas.  
**Generated solution:** `05-Engineering/API-Contracts.md` — key API contracts.  
**Status:** Documented ⚡ — full OpenAPI specs still needed in repository

### T-C6: No Event Schema Definitions
**Gap:** Source documents mention an event bus but define no event types, schemas, or subjects.  
**Generated solution:** `05-Engineering/Event-Contracts.md` — complete event catalog.  
**Status:** Documented ⚡ — review required

---

## Major Technical Gaps

### T-M1: Multi-Tenancy Not Fully Specified
**Gap:** Source documents mention multi-tenancy but provide no implementation details for data isolation, tenant context propagation, or cross-tenant security.  
**Generated solution:** `04-Architecture/Solution-Architecture.md` — tenant context via JWT, tenant_id on all tables.  
**Risk:** Without explicit multi-tenancy enforcement, cross-tenant data leakage is possible.  
**Status:** Documented — must be enforced via code review checklist

### T-M2: No Temporal Worker Design
**Gap:** Source documents mention Temporal but provide no worker design: activity implementations, retry policies, signal/query patterns.  
**Generated solution:** `04-Architecture/Application-Architecture.md` — Temporal workflow and activity interface.  
**Status:** Documented ⚡ — engineering implementation required

### T-M3: WhatsApp Rate Limiting Not Addressed
**Gap:** Source documents do not address WhatsApp's rate limits (80 messages/sec per number, 1,000 conversations/day Tier 1).  
**Risk:** Campaigns sending to large segments will hit rate limits and fail silently.  
**Generated solution:** `04-Architecture/Integration-Architecture.md` — rate limiting via Redis token bucket.  
**Status:** Documented — implementation required (Redis rate limiter)

### T-M4: No Disaster Recovery Plan
**Gap:** Source documents have no backup, recovery, or business continuity strategy.  
**Generated solution:** `06-Operations/SRE.md` — RTO/RPO targets, backup schedule, DR procedure.  
**Status:** Documented ⚡ — DR runbook testing required before launch

### T-M5: Keycloak Multi-Tenant Realm Design Not Specified
**Gap:** Source documents mention Keycloak but don't specify whether tenants share a realm or get dedicated realms.  
**Recommendation:** One Keycloak realm per tenant for maximum isolation and customization.  
**Implication:** Realm creation is part of the tenant provisioning flow.  
**Status:** Documented in `04-Architecture/Solution-Architecture.md` — engineering must implement realm provisioning

### T-M6: NATS JetStream Persistence Not Addressed
**Gap:** NATS without JetStream is in-memory only — events lost on NATS restart.  
**Risk:** Service restarts during peak traffic could lose events, causing workflow misses.  
**Resolution:** JetStream must be enabled with file storage for production.  
**Status:** Documented in `04-Architecture/Solution-Architecture.md`

### T-M7: No Infrastructure as Code
**Gap:** Source documents have no IaC specification. Manual infrastructure setup is error-prone and unrepeateable.  
**Generated solution:** `04-Architecture/Infrastructure-Architecture.md` — Terraform structure defined.  
**Status:** Documented — Terraform scripts must be written in Phase 0

---

## Moderate Technical Gaps

### T-Mo1: No Caching Strategy
**Gap:** Source documents mention Redis but don't specify what is cached, TTLs, or cache invalidation strategy.  
**Generated solution:** `04-Architecture/Data-Architecture.md` — Redis key patterns and TTLs.  
**Status:** Documented

### T-Mo2: Database Migration Strategy Incomplete
**Gap:** Flyway mentioned in principle but no migration versioning or naming conventions.  
**Generated solution:** `05-Engineering/Schema-Definitions.md` and `05-Engineering/Coding-Standards.md` — Flyway conventions.  
**Status:** Documented

### T-Mo3: Message Partitioning Not Planned
**Gap:** The `messages` table will grow rapidly (millions of rows per active tenant). No partitioning strategy documented.  
**Generated solution:** `05-Engineering/Schema-Definitions.md` — monthly table partitioning for `messages` and `audit_logs`.  
**Status:** Documented

### T-Mo4: Search Capability Not Specified
**Gap:** Tenants need to search customers by name/phone and search conversations by keyword. No search architecture specified.  
**Generated context:** `04-Architecture/Solution-Architecture.md` — PostgreSQL ILIKE for MVP, OpenSearch for Phase 2.  
**Status:** Partially addressed — MVP approach documented

### T-Mo5: No Performance Benchmarks
**Gap:** No load testing targets or throughput requirements specified in source documents.  
**Generated solution:** `03-Product/Product-Requirements.md` — NFR requirements including latency targets.  
**Status:** Documented — load testing must be performed before launch

---

## Minor Technical Gaps

### T-Mi1: No Dependency Version Pinning Policy
**Gap:** No policy for how dependency versions are managed (security patching, major upgrades).  
**Recommendation:** Pin all dependencies to patch version; monthly automated Dependabot PRs; major upgrades quarterly.  
**Status:** Not documented

### T-Mi2: No Code Generation for API Clients
**Gap:** OpenAPI specs could be used to generate TypeScript client for frontend. Not specified.  
**Status:** Nice-to-have — Phase 2

### T-Mi3: No GraphQL Consideration
**Gap:** Source documents assume REST only. No evaluation of whether GraphQL would serve the frontend better.  
**Recommendation:** REST is correct choice for this use case. No action needed.  
**Status:** Closed (REST chosen)

---

## Technical Gap Summary

| Gap | Severity | Status |
|---|---|---|
| Architecture conflict | T-C1 | Resolved |
| OpenWA ToS violation | T-C2 | Resolved |
| No security architecture | T-C3 | Documented ⚡ |
| No data model | T-C4 | Documented ⚡ |
| No API contracts | T-C5 | Documented ⚡ |
| No event schemas | T-C6 | Documented ⚡ |
| Multi-tenancy not specified | T-M1 | Documented |
| No Temporal worker design | T-M2 | Documented ⚡ |
| WA rate limiting not addressed | T-M3 | Documented |
| No DR plan | T-M4 | Documented ⚡ |
| Keycloak realm design | T-M5 | Documented |
| NATS persistence | T-M6 | Documented |
| No IaC | T-M7 | Documented |
| No caching strategy | T-Mo1 | Documented |
| No migration conventions | T-Mo2 | Documented |
| No message partitioning | T-Mo3 | Documented |
| Search capability | T-Mo4 | Partially |
| No performance benchmarks | T-Mo5 | Documented |

---

## Cross-References
- `10-Gap-Analysis/Infrastructure-Gaps.md` — Infrastructure-specific gaps
- `10-Gap-Analysis/Compliance-Gaps.md` — Compliance gaps
- `07-Governance/Decision-Records.md` — ADRs resolving key technical conflicts

---

## LOOP-502 Identified Technical Debt

### Architectural Risks
- **Package Coupling:** Risk of bypasses in the modular monolith boundaries leading to spaghetti code.
- **Complexity Overhead:** Maintaining ClickHouse, Redis, PostgreSQL, NATS, Kafka, and multiple Orchestrators increases the operational surface area.

### Scalability Risks
- **PostgreSQL Bottleneck:** Bulk contact imports and high-frequency webhook statuses risk saturating the PostgreSQL master instance.

### Security & Compliance Risks
- **Vector DB Leakage:** Multi-tenancy isolation at the Qdrant vector database layer is undefined.
- **SLA Automation:** 30-day contact erasure SLA trigger logic for DPDP needs automated workflow (Temporal).

### Developer Experience
- **Resource Constraints:** High Docker system resource footprint requires minimum 12-16GB RAM.
- **Port Conflicts:** Local sandbox ports easily conflict with existing developer setups.
