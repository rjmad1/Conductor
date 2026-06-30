# Repository Standards — Conductor Platform

This standard defines the codebase directory structure, naming standards, API formatting, event namespaces, testing specifications, and documentation formats.

---

## 1. Directory Structure

The Conductor repository is structured to separate business logic, infrastructure, governance, and operational memory:

```
/
├── ADRs/                         # Architectural Decision Records (ADR-001.md, etc.)
├── agents/                       # Codeowner-assigned agent configuration directories
├── config/                       # Keycloak, gateway configs, and environments setup
├── docs/                         # General engineering & domain design documentation
├── memory/                       # Agent Working, Organizational, and Experiment logs
│   ├── working/
│   ├── organizational/
│   └── experiment/
├── observability/                # OpenTelemetry, loki, and grafana configuration files
├── platform/                     # Modular Monolith domains code modules
│   ├── customer/                 # Customer details & consent lists database domain
│   ├── identity/                 # Keycloak interface rules
│   ├── workflow/                 # Temporal campaign workflows
│   ├── messaging/                # Meta WhatsApp Cloud API channel interface
│   ├── integration/              # Squid outbound proxy adapters
│   ├── analytics/                # Metabase analytics models
│   └── audit/                    # Trigger audit log tables
├── prompts/                      # Version-controlled agent system prompts
├── scripts/                      # Local environment make/devcontainer automation tasks
├── templates/                    # Reusable agent archetype files
└── tests/                        # Automated unit, integration, and performance benchmarks
```

---

## 2. Naming Conventions

*   **Java Monolith:**
    *   *Packages:* Standard lowercase domain naming (`com.conductor.workflow.worker`).
    *   *Classes:* PascalCase representing verbs and context (`TemporalCampaignWorker.java`).
    *   *Methods:* camelCase starting with action verb (`sendWhatsAppMessage`).
*   **Database Tables:**
    *   *Table Names:* snake_case plural (`consent_records`, `audit_logs`).
    *   *Columns:* snake_case singular. Every transactional table must contain the `tenant_id` column.
*   **Files:**
    *   *Markdown:* UPPERCASE for standards (`REPOSITORY_STANDARDS.md`), kebab-case for guides (`dev-onboarding.md`).
    *   *Scripts:* kebab-case with lowercase suffixes (`setup-devcontainer.sh`).

---

## 3. Architectural Decision Record (ADR) Standards

New ADRs must be written inside `/ADRs/` using the format:
*   *Filename:* `ADR-[ID].md` (three-digit, e.g. `ADR-011.md`).
*   *Structure:* Must contain Title, Status, Context, Decision, Consequences, and EDRB Verification Gaps.

---

## 4. API & Event Governance

*   **REST Standard:**
    *   URL Prefix: `/api/v[major_version]/[domain]/...` (e.g. `/api/v1/customer/consent`).
    *   All endpoints require authorization filters except `/healthz` and `/metrics`.
*   **Event Standards:**
    *   Namespace Format: `conductor.{tenant_id}.{domain}.{entity}.{action}`.
    *   Every published payload must wrap events in a JSON envelope containing correlation trace IDs and schema SemVer metadata.

---

## 5. Documentation Standards

*   Documentation must follow GFM standards.
*   File links must use absolute path references with the `file://` scheme (`[ADR-001](file:///c:/Users/rajaj/Projects/Conductor/ADRs/ADR-001.md)`).
*   Mermaid diagrams must be used to document architecture flows and component pathways.

---

## 6. Testing Standards

*   **Boundary Enforcement:** Unit tests must include compile-time ArchUnit rules checking domain imports isolation.
*   **Isolation Verification:** Integration tests must run simulated requests verifying that requests forged with another tenant's ID result in access rejection.
*   **Latency Testing:** Perform load benchmarks on tables running trigger-based logs.
