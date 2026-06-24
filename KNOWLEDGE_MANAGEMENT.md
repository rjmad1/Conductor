# Knowledge Management Standard — Conductor Platform

This standard defines the capture guidelines, classification tiers, and reuse strategies for architectural, operational, security, and prompt engineering knowledge on the Conductor platform.

---

## 1. Knowledge Tiers & Capture Rules

Knowledge is grouped into five functional categories. Every capture event must record the metadata context (Author, Timestamp, Rationale, and Evidence):

### 1.1 Decision Capture
*   **Definition:** Major architectural shifts, dependency additions, or framework changes.
*   **Enforcement:** Must be written as an ADR inside `/ADRs/`. Temporary decisions or updates without a corresponding ADR are prohibited.

### 1.2 Lessons Learned
*   **Definition:** Post-mortem analyses, bug resolutions, performance optimization findings, or setup adjustments.
*   **Enforcement:** Incidents resulting in build failures, production downtime, or database trigger locks must generate a retro report in `/docs/retrospectives/`.

### 1.3 Architecture Knowledge
*   **Definition:** Domain mapping specifications, entity bounds, event definitions, and database schemas.
*   **Enforcement:** Must be updated in standard domain definition files (e.g. `/docs/domain-boundaries.md` or `REPOSITORY_STANDARDS.md`).

### 1.4 Operational & Security Knowledge
*   **Definition:** Troubleshooting guides, deployment credentials configs, network subnet whitelists, and threat mitigations.
*   **Enforcement:** Documented in standard troubleshooting indices (`/docs/troubleshooting.md`) and security matrices (`/docs/security/`).

### 1.5 Prompt Knowledge
*   **Definition:** Prompt templates, variables configurations, parser constraints, and LLM optimization parameters.
*   **Enforcement:** Must be version-controlled in the `/prompts` folder with matching lifecycle entries.

---

## 2. Knowledge Reuse Strategy

To prevent redundancy and avoid code duplication:
*   **Read-Before-Write Auditing:** Prior to writing code, developers and AI agents must query the knowledge repository (using tools like `grep_search` or CodeGraph explore) to check if a helper class, service configuration, or adapter already exists.
*   **Shared Modules Usage:** Core helpers (encryption converters, auth checking filters, OpenTelemetry trace wrappers) must be imported from `/platform/common/` rather than re-implemented within individual domain packages.
*   **Memory Ingestion:** Learnings captured during execution runs must be ingested into the agent's working memory context, ensuring subsequent tasks benefit from previous corrections.
