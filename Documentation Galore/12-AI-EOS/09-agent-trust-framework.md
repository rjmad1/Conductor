# AI-EOS Agent Trust Framework

## Document Metadata
* **id:** EOS-09-AG-TRUST
* **title:** AI-EOS Agent Trust Framework
* **description:** Specifies the permissions, limitations, escalation rules, and audit patterns for agent trust tiers.
* **owner:** DevSecOps Lead & AgentOps Lead
* **domain:** AI Platform Security
* **tags:** [agent, trust, sandbox, permissions, escalation]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:46:00Z
* **updated:** 2026-06-24T16:46:00Z
* **related_artifacts:** [01-constitution.md, 03-risk-framework.md, 12-security-governance.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 4 — Critical
* **compliance_tags:** [ISO-27001-A.9, SOC2-CC6.2, NIST-AI-RMF-Gov]
* **quality_score:** 1.00

---

## Purpose
To govern the capabilities and authority of autonomous and semi-autonomous agents, this framework defines five trust levels. No agent can bypass its assigned level, and any attempt to execute unauthorized tools must trigger immediate sandbox suspension.

---

## Trust Levels Definition

### 1. Observer
* **Access Scope:** Read-only access to repository code, configuration files, and documentation.
* **Permissions:** Read workspace files, run static code analysis, execute semantic search queries.
* **Restrictions:** Absolute block on writes, commits, network calls (except semantic retrieval APIs), or tool execution.
* **Escalation Path:** Request elevation to Contributor via an ARB review.
* **Audit Requirements:** Standard API call logging.

### 2. Contributor
* **Access Scope:** Read-write access in isolated local branches. Can suggest changes.
* **Permissions:** Create feature branches, write code, run localized tests, output PR proposals.
* **Restrictions:** Cannot merge pull requests, push to protected branches, write to production databases, or trigger deployments.
* **Escalation Path:** PR review by 1 Human Technical Lead.
* **Audit Requirements:** Complete Git history tracking of all commits and PR metadata.

### 3. Operator
* **Access Scope:** Execute pre-approved operational commands and tasks under human supervision.
* **Permissions:** Run pre-approved deployment scripts, trigger staging test suites, configure test environment variables.
* **Restrictions:** Cannot modify source code, modify security policies, access live database customer PII, or change routing configurations.
* **Escalation Path:** Explicit approval from 1 Human SRE or Platform Lead.
* **Audit Requirements:** Immutable audit log of all triggered commands, inputs, and console stdout/stderr.

### 4. Autonomous
* **Access Scope:** Execute tasks autonomously within approved operational guardrails.
* **Permissions:** Auto-triage low-severity bugs, run daily automated checks, auto-generate localized unit tests, self-heal staging environment errors.
* **Restrictions:** Cannot perform Tier 3/4 tasks (such as altering compliance configurations, editing main branch code, or deploying production-wide changes).
* **Escalation Path:** Block and alert SRE when operational metrics (e.g., error rate > 1%) exceed bounds.
* **Audit Requirements:** Full trajectory execution tracking (using OpenTelemetry and evaluation logs).

### 5. Privileged
* **Access Scope:** Temporary high-level system operations (requires active Human-in-the-Loop verification).
* **Permissions:** Modify critical runtime variables, assist with database migrations, reset environment secrets.
* **Restrictions:** Only active during a verified incident response window. All actions must be actively approved by a human companion.
* **Escalation Path:** Requires 2-Factor approval from a human SRE Lead.
* **Audit Requirements:** Full visual session capture, detailed step-by-step cryptographic logging.

---

## Trust Level Mapping Matrix

| Metric / Control | Observer | Contributor | Operator | Autonomous | Privileged |
|---|---|---|---|---|---|
| **Write to Git** | Blocked | Isolated Branch | Blocked | Auto-PR | Allowed (Audited) |
| **Network Access**| Read-only (API) | Blocked | Regulated | Restricted | Full (Proxied) |
| **PII Data Access**| Blocked | Blocked | Blocked | Blocked | Masked only |
| **Max Autonomy** | 0% | 10% | 40% | 80% (Staging) | 100% (Watched) |
| **Sandboxing** | Standard VM | Isolated VM | Secure Container | Hard Sandbox | Secure Host |

---

## Lifecycle Policy
* **Review Cycle:** Quarterly.
* **Revision Process:** Modifications must be approved by the Governance & Compliance Authority.

## Validation Rules
* Agent authorization roles are declared in the root `/eos-manifest.yaml`.
* The tool executor checks the agent's token signatures before executing any action to verify it does not exceed the trust level.

## Audit Requirements
* Weekly log analysis must run to identify any attempts to execute tools outside an agent's registered trust level. Any violation is flagged as a P0 security alert.
