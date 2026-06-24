# Critical ADR Validation Report — Conductor Platform

This report validates the 10 core runtime architectural decision records (ADRs) required for the Conductor platform MVP. It determines their statuses, details their consequences, and outlines any remaining verification gaps.

---

## ADR Validation Summary

All 10 core runtime ADRs have been reviewed and validated. There are no critical blockers; however, specific implementation constraints and verification gates are highlighted below.

| ADR ID | Decision Title | Status | Primary Consequence | Verification Gaps / Actions |
| :--- | :--- | :--- | :--- | :--- |
| **ADR-001** | Target Architecture | **Accepted** | Modular Monolith (Spring Boot 3.x + Virtual Threads) | ArchUnit rules must be enforced in CI to block illegal package imports. |
| **ADR-002** | Multi-Tenancy | **Accepted** | Shared DB Row-Level isolation + Keycloak Realms | Verify indexing on all `tenant_id` columns to prevent query performance degradation. |
| **ADR-003** | Workflow Runtime | **Accepted** | Temporal Server with JSON DSL | Establish a JSON schema validation test suite for the custom DSL. |
| **ADR-004** | Messaging Architecture | **Accepted** | Official Meta WhatsApp Cloud API | Implement rate-limiting on inbound webhooks to survive spikes. |
| **ADR-005** | Event Strategy | **Accepted** | NATS JetStream event broker | Configure file persistence limits and replication policies in staging/prod. |
| **ADR-006** | Auth & Auth | **Accepted** | Keycloak realms + Kong API Gateway | Set up integration tests verifying token verification in Kong. |
| **ADR-007** | Integration Framework | **Accepted** | Squid forward proxy for secure egress | Implement strict domain allowlists in the Squid proxy settings. |
| **ADR-008** | Analytics Architecture | **Accepted** | Metabase embedded via JWT | Restrict the read-replica database credentials to read-only roles. |
| **ADR-009** | Audit Architecture | **Accepted** | PostgreSQL trigger-based immutable ledger | Run write-latency benchmarks to verify trigger execution under load. |
| **ADR-010** | Deployment Strategy | **Accepted** | ECS Fargate in AWS Mumbai (`ap-south-1`) | Validate IaC (Terraform) scripts for Mumbai VPC infrastructure. |

---

## Detailed ADR Valdiations

### ADR-001: Target Architecture
*   **Context:** Balances scalability with seed-stage team constraints (3-4 engineers). Nine microservices are too expensive and operationally complex.
*   **Decision:** Build a **Modular Monolith** using **Java 21 / Spring Boot 3.x with Virtual Threads (Loom)**. Clean decoupled package structure. Out-of-the-box infrastructure assemblies (Keycloak, Temporal, Metabase, NATS).
*   **Consequences:**
    *   *Operational Simplicity:* Single container deploy, single CI/CD pipeline.
    *   *Virtual Threads:* High throughput without reactive programming complexity.
    *   *Modular Separation:* Clean boundaries permit future service extraction.
*   **Governance Check:** **Approved**. Ensure ArchUnit tests run as part of the Maven/Gradle verify phase to check that `com.conductor.workflow` does not import `com.conductor.customer` classes directly.

### ADR-002: Multi-Tenancy
*   **Context:** Strict B2B data isolation needed while maintaining low operational database overhead.
*   **Decision:** Implement a **Shared Database with Logical Partitioning (Row-Level Isolation)**. Every tenant table contains `tenant_id`. Enforce filtering using Hibernate `@FilterDef` + Spring Context Interceptors. keycloak realms handle IAM separation.
*   **Consequences:**
    *   *Zero Accidental Leaks:* Filter is automatically appended to queries via Hibernate hook.
    *   *Cost Efficient:* No idle database capacity compared to DB-per-tenant.
*   **Governance Check:** **Approved**. 
    *   *Constraint:* Multi-tenant database indexes on `(tenant_id, id)` must be enforced for all lookup keys.
    *   *Constraint:* Cross-tenant queries are blocked except for system-wide reporting.

### ADR-003: Workflow Runtime
*   **Context:** Durable, multi-day, stateful campaign flows are required (e.g. wait 2 days before sending WhatsApp).
*   **Decision:** Adopt **Temporal Server** to run workflows. Spring Boot application hosts workers polling via gRPC. User workflow configurations defined in JSON DSL are mapped to Temporal.
*   **Consequences:**
    *   *Reliability:* Automatic retries, sleep/delays run without local memory allocation.
    *   *Complexity:* Operational overhead of managing a Temporal cluster.
*   **Governance Check:** **Approved**. Confirm that workflow state persistence (history) is configured to purge after a retention period (e.g., 7-14 days) to prevent disk expansion.

### ADR-004: Messaging Architecture
*   **Context:** Sending campaign messages on WhatsApp while complying with Meta platform rules. Unofficial libraries risk permanent numbers bans.
*   **Decision:** **Official Meta WhatsApp Cloud API** exclusively. Process opt-out keywords (e.g. "STOP") asynchronously within 5 seconds.
*   **Consequences:**
    *   *Legal/SaaS Viability:* High confidence against Meta bans.
    *   *Operational:* Mandatory Facebook developer account and business verification.
*   **Governance Check:** **Approved**. Opt-out messages must trigger an immediate NATS event which writes to the Customer Consent database bypass-caching.

### ADR-005: Event Strategy
*   **Context:** Need asynchronous communication between the monolith and external adapters (like `whatsapp-adapter`).
*   **Decision:** Adopt **NATS JetStream** as low-latency message broker and stream store.
*   **Consequences:**
    *   *Footprint:* Single lightweight container (<50MB memory) compared to Apache Kafka.
    *   *Reliability:* At-least-once message delivery via durable stream subscriptions.
*   **Governance Check:** **Approved**. Define stream retention limits (size/age) so NATS storage doesn't consume all system disk volumes.

### ADR-006: Authentication & Authorization
*   **Context:** Need secure user auth, API key management, and RBAC without writing custom logic.
*   **Decision:** Deploy **Keycloak** as Identity Provider with **Kong API Gateway** checking JWT signatures. Spring Security handles endpoint permissions.
*   **Consequences:**
    *   *Security:* Standards-based (OIDC / OAuth 2.0).
    *   *SaaS Tenant Separation:* One Keycloak Realm per tenant.
*   **Governance Check:** **Approved**. Gateway must pass the validated `X-Tenant-ID` header downstream to the modular monolith.

### ADR-007: Integration Framework
*   **Context:** Workflows need to trigger Zoho, Shopify, or custom HTTP webhooks. Outbound calls can lead to SSRF vulnerabilities.
*   **Decision:** Run all egress traffic from the integrations module through a **Squid Egress Proxy** running a strict domain allowlist.
*   **Consequences:**
    *   *Security:* Blocks requests pointing to internal metadata endpoints or subnets.
*   **Governance Check:** **Approved**. Egress IP ranges must be static to allow tenants to whitelist Conductor IP addresses in their external firewall configurations.

### ADR-008: Analytics Architecture
*   **Context:** Tenants require delivery metrics and CTR charts. Frontend chart logic is expensive to build.
*   **Decision:** Adopt **Metabase** embedded via secure signed JWTs inside standard React iframes, querying a Postgres read-replica.
*   **Consequences:**
    *   *Velocity:* dashboards are visually configured, not coded.
    *   *Licensing:* Metabase Pro license costs must be incorporated into platform billing maps.
*   **Governance Check:** **Approved**. Ensure Metabase queries run against a read-only PostgreSQL replica database user role to block SQL injections.

### ADR-009: Audit Architecture
*   **Context:** SOC2 and DPDP India require auditing all sensitive settings changes. Application-layer auditing can be bypassed by manual queries.
*   **Decision:** **Database Trigger-Based Audit Ledger** in PostgreSQL. Row alterations write JSONB logs to a central `audit_logs` table. Enforce write-once immutability via DB trigger.
*   **Consequences:**
    *   *Reliability:* 100% database audit coverage.
*   **Governance Check:** **Approved**. Ensure monthly physical partitioning of the `audit_logs` table is configured to simplify data rotation.

### ADR-010: Deployment Strategy
*   **Context:** Secure, compliant, and cost-effective multi-environment cloud deployments.
*   **Decision:** Docker-Compose locally; **AWS ECS Fargate** for cloud environments, localized to AWS Mumbai (`ap-south-1`) for Indian data residency compliance.
*   **Consequences:**
    *   *Low Ops Burden:* Serverless containers avoid VM patch overhead.
    *   *Compliance:* Data residency satisfies DPDP regulations.
*   **Governance Check:** **Approved**. Verify that backups for PostgreSQL and logs are also retained within the Mumbai geographical boundary.
