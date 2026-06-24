# Engineering Enablement Report — Conductor Platform

This report coordinates the developer experience and AI-agent operating ecosystem for the Conductor Platform. It details the operational frameworks, memory architectures, handoff protocols, and constraints established across the 14 project governance standards.

---

## 1. Engineering Operating Model

The Conductor engineering model enforces a specification-driven, modular monlith strategy based on Java 21 / Spring Boot 3.x with Virtual Threads:
*   **Modular Boundary Enforcement:** Core directories in `/platform/` map to exclusive domain packages. Cross-module imports are blocked at compile time via ArchUnit verification checks.
*   **Multi-Tenancy Isolation:** Standard database transactions use row-levellogical partitioning (`tenant_id` filters).
*   **Quality Metrics Control:** Development cycles require linter checks, secret scanning, and automated unit testing to obtain code promotion.

---

## 2. Agent Operating Model

AI agents (including Antigravity, Claude Code, and Cursor) operate under strict privilege boundaries:
*   **Agent Classification Directory:** Access limits follow the Agent Ownership Matrix, granting exclusive, shared, or read-only directories scopes.
*   **Autonomy Tiers:** Agent action boundaries map directly to task risk tiers. High-risk configuration modifications are restricted and require multi-human sign-off.
*   **Prompt Governance:** Prompt files are version-controlled in the `/prompts/` directory and manage agent instructions as core code assets.

---

## 3. Loop Engineering Framework

Automated processes progress through five distinct lifecycle steps:

```
[ Generate ] ──► [ Evaluate ] ──► [ Decide ] ──► [ Learn ] ──► [ Scale ]
```

*   **Generate & Evaluate Separation:** Test suites and evaluation checkers run independently from code generation models.
*   **Memory Tier Isolation:** ephemerally stored variables run in Working Memory and are deleted on task completion, whereas verified decisions promote to Organizational Memory via human review gates.
*   **Escalation Protocol:** Drift events, directory collisions, or uncertainty triggers halt execution loops and escalate status reporting to human operators.

---

## 4. Knowledge Framework

Knowledge is managed as a structured, versioned asset to prevent architectural drift:
*   **ADR Log Integration:** Architectural shifts require registering a new ADR in `/ADRs/` using the canonical template.
*   **Incident Post-Mortems:** System failures and latency breaches record in standard retro logs.
*   **Knowledge Reuse Strategy:** Agents must search the shared knowledge registry before writing helper scripts or custom adapters.

---

## 5. Developer Experience (DX) Framework

Developer onboarding and environment verification are automated to take less than 30 minutes:
*   **One-Command Bootstrap:** Setting up a local sandbox uses `make bootstrap` checking memory footprints, port overrides, and compose blocks.
*   **Dynamic Overrides:** Port conflicts resolve via local `.env.local` templates.
*   **Tracing UI Integration:** Jaeger dashboard configurations provide visual audits of OTEL transaction traces.

---

## 6. Implementation Constraints

Coding agents must operate under the following structural constraints:
*   **Outbound Egress Routing:** Webhook adapter connections must route outbound calls through the Squid forward proxy to mitigate SSRF vulnerabilities.
*   **WhatsApp Compliance:** WhatsApp campaign flows must utilize the official Meta WhatsApp Cloud API exclusively. Scraping libraries are banned.
*   **Consent Checks:** Messaging flows must verify opt-in status against the customer consent ledger.
*   **Data Localization:** Data tables and S3 buckets must reside in AWS Mumbai region (`ap-south-1`) to satisfy DPDP India 2023.

---

## 7. Approval Status

The Agent Operating System and Developer Experience ecosystem is **APPROVED FOR BETA DEPLOYMENT**.

### Outstanding Pre-Staging Actions
1.  **ArchUnit Rule Check-in:** Integrate the modular package verification check class into the Maven verify plugin configuration.
2.  **Port Collisions Test:** Confirm local override configurations work cleanly across developer workstations.
3.  **Audit Latency Benchmarks:** Execute pgbench trigger tests to ensure audit triggers do not degrade database write speeds.
