# Engineering Operating System (EOS) — Conductor Platform

This document defines the core governance structure for human developers and autonomous AI agents operating within the Conductor Platform repository. This is an authoritative governance layer that coordinates the architecture, security, code, documentation, release, and knowledge lifecycles.

---

## 1. Architecture Governance

The Conductor Platform is structured as a **Modular Monolith** using **Java 21 / Spring Boot 3.x with Virtual Threads (Loom)**. All components must adhere to the decisions registered in the [ADR Catalog](file:///c:/Users/rajaj/Projects/Conductor/ADR_INVENTORY.md).

### 1.1 Core Monolith Rules
*   **Modular Boundary Isolation:** Packages must be strictly organized by domain contexts (e.g., `com.conductor.workflow`, `com.conductor.customer`). Cross-boundary dependency imports are strictly prohibited.
*   **ArchUnit Enforcement:** Monolith modularity boundaries are validated via ArchUnit compile-time tests in the Maven/Gradle verify cycle. Any build containing cross-domain violations will be rejected.
*   **Stateless Core:** Application service layers must remain stateless. Distributed scheduling or campaign state flows must be deferred to the **Temporal Server** or run via **NATS JetStream** events.

### 1.2 Data Access Rules
*   **Logical Partitioning:** Multi-tenancy is logical and implemented on a shared database utilizing row-level filters (`tenant_id`). Direct cross-module SQL joins or cross-module database reads are banned.
*   **Data Access Boundaries:** Cross-domain data fetching must utilize public API endpoints or event subscriptions.

---

## 2. Security Governance

Security is designed around defense-in-depth principles as validated in the [Security Foundation Report](file:///c:/Users/rajaj/Projects/Conductor/SECURITY_FOUNDATION_REPORT.md).

### 2.1 Identity & Authorization
*   **Realm Partitioning:** Keycloak realms handle IAM separation, with one realm dedicated per tenant.
*   **Token Verification:** The Kong API Gateway verifies JWT signatures at the edge and propagates the validated `X-Tenant-ID` header downstream.

### 2.2 Network & Egress Controls
*   **Egress Forwarding:** Direct outbound web calls are blocked. Custom services or connectors invoking external endpoints (e.g. Zoho CRM, Shopify, webhooks) must route requests through the Squid egress forward proxy to prevent Server-Side Request Forgery (SSRF).
*   **Data Residency Compliance:** All transactional data, object storage buckets, and backups must reside exclusively in the AWS Mumbai (`ap-south-1`) region to satisfy DPDP India 2023 compliance.

### 2.3 Cryptography & Secrets
*   **PII Encryption:** Sensitive customer details (emails, phone numbers) must be encrypted at rest using AES-256 GCM converters mapped via JPA.
*   **Secrets Isolation:** Plaintext secrets or credentials are banned from repositories. GitLeaks pre-commit hooks and Checkov scans run in the CI/CD pipeline.

---

## 3. Agent Governance

AI agents (including Google Antigravity, Claude Code, Cursor, Windsurf, OpenHands, and Roo Code) must operate under strict, sandboxed boundaries as mapped in the [Agent Ownership Matrix](file:///c:/Users/rajaj/Projects/Conductor/AGENT_OWNERSHIP_MATRIX.md).

### 3.1 Permission Classifications
*   **Exclusive Domains:** Only the designated agent may write to a specific domain directory (e.g. only the Security Agent writes to `platform/identity/`).
*   **Shared Domains:** Multiple agents may edit shared folders (like `platform/common/` or documentation), requiring codeowner review.
*   **Restricted Paths:** Critical config files (`docker-compose.local.yml`, environments settings) are restricted. Any modifications require explicit human EDRB approval.

### 3.2 Autonomy Tiers
Agent autonomy matches the risk levels mapped in `eos-manifest.yaml`:
*   *Tier 0 (Informational):* Fully autonomous doc updates.
*   *Tier 1-2 (Low-Moderate):* Code modifications in unit tests or feature adapters with automated test passing requirements.
*   *Tier 3-4 (High-Critical):* Infrastructure changes, auth adjustments, and manifest updates require multi-human sign-off.

---

## 4. Code Governance

All code committed to the repository must meet strict standard quality metrics:
*   **Type Safety:** Absolute requirement for TypeScript in frontend projects; Java 21 type structures must not use raw, generic typings or `Object` parameters.
*   **Error Handling:** Uncaught exceptions are banned. External service API calls or disk transactions must be wrapped in try/catch structures and print contextual, structured error messages.
*   **Style Standards:** Enforced using EditorConfig linting setups. Commit hooks reject code formatting violations.

---

## 5. Documentation Governance

Documentation is a first-class engineering artifact that must reflect current system behavior.
*   **ADR Standard:** Major architectural shifts require registering a new ADR in `/ADRs` using the canonical template.
*   **Spec-Driven:** Prior to any file creation or refactoring, a `spec.md` outlining API interfaces and data models must be committed and approved.
*   **Markdown Formatting:** Documentation must follow clean GitHub Flavored Markdown (GFM) syntax, including proper file links (`[file.java](file:///absolute/path/to/file.java)`) and structured mermaid diagrams for component routing validation.

---

## 6. Release Governance

The path from local developer verification to production stages is automated and GitOps-driven:
*   **Git Branching Model:** Feature branch development with pull request merges to `main`. Force pushing is disabled.
*   **GitOps Reconciliation:** Deployment configuration changes are managed via ArgoCD. Staging and production Kubernetes/ECS manifests reconcile automatically from repo Helm templates.
*   **SBOM Generation:** Every build compile triggers automated CycloneDX SBOM generation through the Syft scanner to audit third-party dependency supplies.

---

## 7. Knowledge Governance

Knowledge capture prevents architectural drift and simplifies onboarding:
*   **Decision Log:** Decisions must capture the "what," "why," and "evidence" in the repository decision register.
*   **Incident Retrospectives:** Failures, performance bottlenecks, and security events must generate standard post-mortems in `/docs/retrospectives/`.
*   **Lessons Learned:** Reusable architectural or developer setup optimizations must be registered in the Knowledge Base to ensure continuous learning.
