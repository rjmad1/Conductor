# ADR Inventory — Conductor Platform

This inventory compiles all architectural decision records (ADRs) discovered in the repository, including explicit, implicit, and undocumented architectural assumptions.

---

## 1. Explicit Architectural Decision Records

### Core Platform Runtime ADRs (Located in `/ADRs/`)

These records govern the runtime architecture, infrastructure, and technology stack choices for the Conductor MVP:

| ADR ID | Title | Status | Scope |
| :--- | :--- | :--- | :--- |
| **ADR-001** | [Target Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-001.md) | Approved | Adopt a Modular Monolith style using Java 21 / Spring Boot 3.x with Virtual Threads (Project Loom) to optimize for operational simplicity. |
| **ADR-002** | [Multi-Tenancy Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-002.md) | Approved | Shared Database with Logical Partitioning (Row-Level Isolation via Hibernate filters and a `tenant_id` column). |
| **ADR-003** | [Workflow Runtime](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-003.md) | Approved | Adopt Temporal Server as the core workflow execution engine, orchestrating custom workflows defined in JSON DSL. |
| **ADR-004** | [Messaging Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-004.md) | Approved | Standardize entirely on the official Meta WhatsApp Cloud API; strictly ban unofficial scraping wrappers. |
| **ADR-005** | [Event Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-005.md) | Approved | Adopt NATS JetStream as the low-latency asynchronous event broker and streaming engine. |
| **ADR-006** | [Authentication & Authorization](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-006.md) | Approved | Adopt Keycloak for Identity/IAM, with Kong API Gateway (or OAuth2 proxy) verifying token signatures. |
| **ADR-007** | [Integration Framework](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-007.md) | Approved | Force all outbound HTTP egress from the integration framework to route through a Squid forward proxy to block SSRF. |
| **ADR-008** | [Analytics Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-008.md) | Approved | Embed Metabase charts inside the dashboard using signed JWT iframes, querying a Postgres read replica. |
| **ADR-009** | [Audit Architecture](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-009.md) | Approved | Row-level PostgreSQL database triggers executing `AFTER` updates, writing to an immutable, partitioned log table. |
| **ADR-010** | [Deployment Strategy](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-010.md) | Approved | Docker-Compose for local developer sandboxes; AWS ECS Fargate deployed in the AWS Mumbai region (`ap-south-1`). |

### Repository Governance ADRs (Located in `/docs/adr/`)

These records govern codebase styling, structures, testing, and operations:

| ADR ID | Title | Status | Scope |
| :--- | :--- | :--- | :--- |
| **ADR-GOV-001** | [ADR-GOV-001](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-001-Repository-Structure-Standard.md) | Approved | Mandates clean top-level directories and separates source code (`/src`) from documentation (`/docs`) and configurations. |
| **ADR-GOV-002** | [ADR-GOV-002](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-002-Dependency-Management-Standard.md) | Approved | Pin library dependencies to exact versions, block copyleft licenses, and run static vulnerability scanners. |
| **ADR-GOV-003** | [ADR-GOV-003](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-003-Module-Ownership-Standard.md) | Approved | Dictates ownership rules for microservices and library components, mapping specific folders to CODEOWNERS. |
| **ADR-GOV-004** | [ADR-GOV-004](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-004-Configuration-Standard.md) | Approved | Mandates environment variable configuration injected at runtime; forbids hardcoded variables in codebase. |
| **ADR-GOV-005** | [ADR-GOV-005](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-005-Testing-Standard.md) | Approved | Enforces the Testing Pyramid (unit, integration, contract, and E2E) with a strict minimum of 80% code coverage. |
| **ADR-GOV-006** | [ADR-GOV-006](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-006-Logging-Standard.md) | Approved | Standardizes JSON format, mandatory correlation trace contexts, and standardizes debug/info/error levels. |
| **ADR-GOV-007** | [ADR-GOV-007](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-007-Observability-Standard.md) | Approved | Standardizes OpenTelemetry SDK exposures, exporting log, metrics, and trace telemetry to the central OTel Collector. |
| **ADR-GOV-008** | [ADR-GOV-008](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-008-Security-Baseline.md) | Approved | Mandates OIDC realms, TLS 1.3, DB encryption-at-rest, and pre-commit checks to verify secrets are not committed. |
| **ADR-GOV-009** | [ADR-GOV-009](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-009-API-Design-Standard.md) | Approved | Enforces REST/JSON standards, API version prefixes (`/api/v1/`), standard pagination, and error responses. |
| **ADR-GOV-010** | [ADR-GOV-010](file:///c:/Users/rajaj/Projects/Conductor/docs/adr/ADR-GOV-010-Specification-Governance-Model.md) | Approved | Mandates that code and tests must map to product and API specs in the `/docs` folder before pull request approvals. |

---

## 2. Implicit Architectural Decisions & Assumptions

Several critical components in the reference blueprints lack dedicated, approved ADRs but represent active design assumptions:

### A. The OLAP Storage Engine (ClickHouse)
*   **Context:** `REFERENCE_ARCHITECTURE.md` (Section 4) specifies ClickHouse as the columnar OLAP database engine to store system metrics, logs, and billing logs.
*   **Assumption:** ClickHouse will act as the target for CDC (Change Data Capture) pipelines originating from the core transaction database.
*   **Status:** *Implicit/Unapproved*. No explicit migration/sync mechanism has been selected or defined as an ADR.

### B. AI & Stateful Agent Engine (LiteLLM & LangGraph)
*   **Context:** `REFERENCE_ARCHITECTURE.md` (Section 5) incorporates LangGraph for stateful multi-agent graphs and LiteLLM as an API proxy.
*   **Assumption:** The backend Java modular monolith will integrate with these Node.js/Python engines via REST APIs, passing tenant scopes in standard headers.
*   **Status:** *Implicit/Unapproved*. Agent boundaries and connection parameters are undocumented.

### C. Vector Retrieval-Augmented Generation (Qdrant/Weaviate)
*   **Context:** Qdrant is introduced as the primary vector storage target for semantic search and AI RAG pipelines.
*   **Assumption:** Tenant isolation within the vector database will be logical, routing requests via LiteLLM using metadata tags.
*   **Status:** *Implicit/Unapproved*. Data isolation guarantees at the vector layer remain undefined.

### D. Ingress API Gateway (Kong Gateway vs OAuth2 Proxy)
*   **Context:** There is a mismatch between `REFERENCE_ARCHITECTURE.md` (recommends OAuth2 Proxy + central ingress) and `docs/architecture/architecture_modernization_report.md` (diagrams Kong API Gateway).
*   **Assumption:** Kong acts as the reverse proxy executing OIDC token verification before forwarding down to the monolith container.
*   **Status:** *Conflicting Assumption*. Standard gateway pattern must be frozen.

---

## 3. Recommended Actions to Freeze Decisions

1.  **Promote Ingress Gateway ADR:** Freeze Kong API Gateway as the ingress gate.
2.  **Define CDC and OLAP Strategy:** Draft a formal ADR detailing how PostgreSQL data replicates to ClickHouse (e.g., via Debezium or application event streams).
3.  **Define Vector Multi-Tenancy:** Finalize a policy for multi-tenant data storage within Qdrant (e.g., namespace isolation or payload filtering).
