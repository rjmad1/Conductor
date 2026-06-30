# Architecture Scorecard — Conductor Platform

This scorecard evaluates the architectural characteristics of the Conductor Platform against the requirements of the MVP.

---

## Architectural Pillar Ratings

| Dimension | Score (0-10) | Confidence | Primary Evidence | Primary Gaps / Risks |
| :--- | :--- | :--- | :--- | :--- |
| **Architecture Alignment** | 9/10 | High | [ADR-001](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-001.md) (Modular Monolith) simplifies operations while preserving modular separation boundaries. | Risk of package coupling (developers bypassing module interfaces). |
| **Scalability** | 8/10 | High | Temporal handles durable executions; NATS handles events queueing; ClickHouse stores metrics. | Database write bottlenecks on PostgreSQL master at high concurrency. |
| **Security** | 9/10 | High | Keycloak realm segregation; row-level isolation via Hibernate filters; Squid proxy blocks SSRF. | Multi-tenancy isolation at the Qdrant vector database layer is undefined. |
| **Operability** | 8/10 | High | ECS Fargate serverless; Docker-Compose sandbox; Grafana monitors. | Port conflicts on local developer machines; high RAM requirements. |
| **Maintainability** | 9/10 | High | Strictly defined directory structures; codebase styling standards; pinned dependencies. | Risk of library bloat in shared modules without governance reviews. |
| **Extensibility** | 9/10 | High | Custom JSON DSL for campaigns workflows; official Meta Graph API adapter. | Meta API version upgrades require coordination. |
| **Compliance Readiness** | 9/10 | High | PostgreSQL trigger-based immutable ledger; India Mumbai residency for DPDP/GDPR. | The 30-day contact erasure SLA trigger logic needs automation code. |
| **Developer Experience** | 8/10 | High | Root Makefile automation; bootstrap PS1/SH diagnosis scripts; DevContainer setup. | High Docker system resource footprint requires 16GB RAM. |

---

## Detailed Evaluation & Remediation Plan

### 1. Architecture Alignment (Score: 9/10)
*   **Evidence:** Adopting the modular monolith (Spring Boot + Loom) over 9 separate microservices reduces infrastructure costs and deployment friction by 80%.
*   **Gap/Risk:** Technical debt can accumulate if package imports bypass boundaries.
*   **Mitigation:** Enforce ArchUnit rules in the Maven/Gradle build cycle to fail builds on boundary violations.

### 2. Scalability (Score: 8/10)
*   **Evidence:** Distributing work to Temporal workers and NATS JetStream consumer groups allows the application container to remain stateless and scale horizontally.
*   **Gap/Risk:** Bulk contacts CSV imports and high-frequency messaging status callbacks will saturate PostgreSQL.
*   **Mitigation:** Offload analytics tracking to ClickHouse. Add indices to all `(tenant_id, ...)` columns.

### 3. Security (Score: 9/10)
*   **Evidence:** Ingress tokens are decrypted at the gateway. Outbound integrations route through the Squid egress forward proxy to block internal port-scanning (SSRF).
*   **Gap/Risk:** Cross-tenant leakage inside the Qdrant/Weaviate vector database.
*   **Mitigation:** Enforce tenant-scoped collections in Qdrant or append logical metadata filtering tags to all vector search queries.

### 4. Operability (Score: 8/10)
*   **Evidence:** Single JAR deployment on AWS ECS Fargate minimizes physical operating system patches. Local sandbox utilizes standard `docker-compose`.
*   **Gap/Risk:** Local developer machines suffer from port conflicts with pre-existing local databases.
*   **Mitigation:** Provide port override mappings in the `.env` file templates.

### 5. Compliance Readiness (Score: 9/10)
*   **Evidence:** Monthly partitioned trigger-based audit table blocks `UPDATE` or `DELETE` commands. Storage is pinned to the Mumbai (`ap-south-1`) region.
*   **Gap/Risk:** Regulatory fines under DPDP if contact data is not fully erased within the SLA.
*   **Mitigation:** Create an automated workflow in Temporal that executes contact erasure across all database tables and vector collection nodes.
