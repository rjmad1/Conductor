# AI-EOS Risk Framework

## Document Metadata
* **id:** EOS-03-RISK-FW
* **title:** AI-EOS Risk Framework
* **description:** Defines the enterprise risk tiers and matches them to governance, testing, security, and agent autonomy levels.
* **owner:** Governance & Compliance Authority & DevSecOps Lead
* **domain:** Enterprise Risk Management
* **tags:** [risk, guardrails, autonomy, compliance, security]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:34:00Z
* **updated:** 2026-06-24T16:34:00Z
* **related_artifacts:** [01-constitution.md, 02-governance-architecture.md, 09-agent-trust-framework.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 4 — Critical
* **compliance_tags:** [NIST-AI-RMF-Map, ISO-27001-A.12, SOC2-CC3.1]
* **quality_score:** 1.00

---

## Purpose
The Conductor Risk Framework classifies technical, product, and operational changes into five distinct tiers. It defines explicit, mandatory guardrails for approvals, testing, agent actions, and deployment procedures proportional to the risk involved.

---

## The Risk Tiers

### Risk Tier 0 — Informational
* **Definition:** Changes with no impact on production behavior, security, or data flow (e.g., documentation typos, comments, localized code style updates).
* **Agent Autonomy:** Autonomous (Full execution, commit, and auto-merge).
* **Approval Requirements:** None.
* **Security Controls:** Standard Git signature validation.
* **Testing Requirements:** None.
* **Deployment Restrictions:** Direct commit to main or auto-merge PR allowed.
* **Monitoring Requirements:** Standard Git logs.

### Risk Tier 1 — Low
* **Definition:** Non-breaking changes inside internal tools or isolated utility methods (e.g., unit test additions, performance optimization of pure helper functions).
* **Agent Autonomy:** Operator (Autonomous execution of pre-approved commands and CI verification).
* **Approval Requirements:** 1 Peer Reviewer (Human or trusted automated review agent).
* **Security Controls:** SAST scan, dependency vulnerability check (SCA).
* **Testing Requirements:** 100% test pass, no regression in code coverage.
* **Deployment Restrictions:** Standard CI/CD pipeline deployment to development environment.
* **Monitoring Requirements:** Application logging.

### Risk Tier 2 — Moderate
* **Definition:** Standard feature updates, API additions, and minor database schema updates. Changes that modify business logic but do not affect user PII, authentication, or third-party webhooks (e.g., adding a new WhatsApp template field, expanding workflow actions).
* **Agent Autonomy:** Contributor (Suggest modifications, construct pull requests; execution must be done by humans or human-triggered tasks).
* **Approval Requirements:** 1 Human Technical Lead.
* **Security Controls:** SAST, SCA, secrets detection, input validation sanity tests.
* **Testing Requirements:** Unit + Integration tests pass. Regression testing on staging environment.
* **Deployment Restrictions:** CI/CD deployment to Staging; human-authorized canary deployment to Production (10% traffic).
* **Monitoring Requirements:** OpenTelemetry APM traces, error rate alerting.

### Risk Tier 3 — High
* **Definition:** Changes involving core platform systems, data sovereignty (PII storage, localization), integrations (WhatsApp Business API connections), or system authentication/authorization protocols (e.g., migrating Keycloak setup, modifying database residency).
* **Agent Autonomy:** Contributor (Suggest change/PR only; cannot run commands outside isolated sandboxes).
* **Approval Requirements:** 2 Humans (Owner/Lead + Platform Architect).
* **Security Controls:** High-severity SAST block, dynamic application security testing (DAST), manual security review, data privacy impact assessment (DPIA).
* **Testing Requirements:** Full Unit + Integration + E2E suite. Performance/load testing under peak targets.
* **Deployment Restrictions:** Manual deployment approval, blue-green deployment with rollback triggered automatically if error rate exceeds 0.5% in first 30 minutes.
* **Monitoring Requirements:** Real-time dashboards, audit logs, anomalies alerts, tracing.

### Risk Tier 4 — Critical
* **Definition:** Core architectural changes, constitutional modifications, primary security policy updates, major infrastructure platform changes, or modifications to high-autonomy autonomous agent runtimes (e.g., upgrading Temporal engine version, changing the EDRB approval matrix).
* **Agent Autonomy:** Observer (Read-only workspace analysis, no direct code execution, no commit permissions).
* **Approval Requirements:** Full EDRB + Compliance Lead approval.
* **Security Controls:** External penetration testing/audit verification, full threat modeling validation, isolated environment verification, human-in-the-loop validation only.
* **Testing Requirements:** Full verification suite + regression tests + manual validation + disaster recovery drill.
* **Deployment Restrictions:** Off-peak maintenance window, manual blue-green cutover, dedicated human SRE monitoring.
* **Monitoring Requirements:** 24/7 pager rotation, active security information and event management (SIEM) integration.

---

## Risk Decision Matrix

| Dimension | Tier 0 | Tier 1 | Tier 2 | Tier 3 | Tier 4 |
|---|---|---|---|---|---|
| **Agent Role** | Autonomous | Operator | Contributor | Contributor | Observer |
| **Human Approver** | None | 1 Peer | 1 Tech Lead | 2 Senior Leads | Full EDRB |
| **Pipeline Gate** | Lint | SAST, SCA | SAST, SCA, QA | SAST/DAST, DPIA | Audited Pen Test |
| **Environments** | Dev / Main | Dev / Staging | Staging -> Prod | Blue-Green / Rollback | EDRB Manual Cut |
| **Monitoring** | Git Log | Logs | Metrics/APM | Audit/Tracing | SIEM, Pager |

---

## Lifecycle Policy
* **Review Cycle:** Quarterly.
* **Revision Process:** EDRB must authorize updates to risk classifications or guardrail thresholds.

## Validation Rules
* CI/CD pipelines must dynamically parse files altered in a PR and determine the risk tier based on modified file paths (e.g., `01-constitution.md` modifications = Tier 4; `.github/` workflows = Tier 3).

## Audit Requirements
* Compliance team audits all Tier 3 and Tier 4 releases monthly to ensure complete trace logs of approvals, security sign-offs, and verification test reports.
