# Decision Records Index

## A. Purpose
This index catalogs all Architectural Decision Records (ADRs) that govern the runtime environments, infrastructure stacks, coding designs, and repository guidelines of the Conductor project.

---

## B. Core Platform Runtime Decisions

These records (located under the root `/ADRs/` folder) define the runtime infrastructure choices for the Conductor MVP:

*   **[ADR-001: Target Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-001.md)** (Approved): Adopt a Modular Monolith style using Java 21 / Spring Boot 3.x with Virtual Threads (Project Loom) to maximize throughput while minimizing operational deployment costs.
*   **[ADR-002: Multi-Tenancy Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-002.md)** (Approved): logical partition isolation using a shared PostgreSQL database. Every tenant-specific table contains a `tenant_id` column, filtered automatically via Hibernate session aspects.
*   **[ADR-003: Workflow Runtime](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-003.md)** (Approved): Standardize on Temporal Server as the durable execution engine to orchestrate custom campaigns defined in JSON DSL.
*   **[ADR-004: Messaging Channel](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-004.md)** (Approved): Use the official Meta WhatsApp Cloud API exclusively. Scraping-based or unofficial automation libraries are strictly prohibited to avoid number bans.
*   **[ADR-005: Event Broker](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-005.md)** (Approved): Use NATS JetStream as the low-latency asynchronous event broker and message persistence store.
*   **[ADR-006: Auth & Auth Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-006.md)** (Approved): Adopt Keycloak for Identity IAM provider and verify signatures on JWT tokens via Kong API Gateway reverse proxies.
*   **[ADR-007: Integration Egress](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-007.md)** (Approved): Force all outbound HTTP requests made by integration connectors to route through a Squid forward proxy allowlist to block SSRF.
*   **[ADR-008: Analytics Integration](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-008.md)** (Approved): Embed Metabase dashboards in the frontend React Web App using secure, signed JWT token iframes.
*   **[ADR-009: Database Audit](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-009.md)** (Approved): Implement database trigger-based auditing, writing mutations to an immutable, partitioned `audit_logs` table.
*   **[ADR-010: Deployment Platform](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-010.md)** (Approved): Deploy local environments via Docker-Compose; deploy cloud environments via AWS ECS Fargate localized to the AWS Mumbai (`ap-south-1`) region.

---

## C. Repository Governance Decisions

These records (located under `/docs/adr/`) establish code quality pipelines and workflow validation constraints:

*   **[ADR-GOV-001: Repository Layout](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-001-Repository-Structure-Standard.md)**: Establishes folder boundaries, separating source code from deployment scripts and documentation folders.
*   **[ADR-GOV-002: Dependency Pins](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-002-Dependency-Management-Standard.md)**: Mandates pinning library dependency versions, blocking copyleft configurations, and running scan tools.
*   **[ADR-GOV-003: Module Codeowners](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-003-Module-Ownership-Standard.md)**: Maps directories to owners using standard `CODEOWNERS` configurations.
*   **[ADR-GOV-004: Configuration Variables](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-004-Configuration-Standard.md)**: Restricts developers from hardcoding credentials, mandating environment variables.
*   **[ADR-GOV-005: Testing Pyramid](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-005-Testing-Standard.md)**: Enforces unit, integration, and E2E checks with an 80% coverage minimum.
*   **[ADR-GOV-006: Log Layout](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-006-Logging-Standard.md)**: Mandates JSON format logging and MDC trace correlation tags.
*   **[ADR-GOV-007: OpenTelemetry Export](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-007-Observability-Standard.md)**: Standardizes exports of metrics, spans, and traces to the OTel Collector.
*   **[ADR-GOV-008: Security Baseline](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-008-Security-Baseline.md)**: Configures secrets blocking pre-commit checks and TLS 1.3 encryption constraints.
*   **[ADR-GOV-009: API Prefixes](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-009-API-Design-Standard.md)**: Enforces versioned base paths and standard HTTP responses.
*   **[ADR-GOV-010: PR Spec Governance](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-010-Specification-Governance-Model.md)**: Restricts merge approval until new code changes successfully map to documented specs.

---

## D. Related Pages
- [Architecture Overview](Architecture-Overview)
- [System Context](System-Context)
