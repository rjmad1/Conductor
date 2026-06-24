# AI-EOS Compliance Governance

## Document Metadata
* **id:** EOS-11-COMP-GOV
* **title:** AI-EOS Compliance Governance
* **description:** Maps system controls to international regulations and specifies evidence and audit readiness models.
* **owner:** Governance & Compliance Authority & Legal Counsel
* **domain:** Enterprise Compliance
* **tags:** [compliance, GDPR, SOC2, ISO-27001, EU-AI-Act, NIST-AI-RMF, audit]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:50:00Z
* **updated:** 2026-06-24T16:50:00Z
* **related_artifacts:** [01-constitution.md, 13-data-governance.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 4 — Critical
* **compliance_tags:** [GDPR, SOC2, ISO-27001, EU-AI-Act, NIST-AI-RMF, DPDP-India]
* **quality_score:** 1.00

---

## Purpose
This document defines how Conductor satisfies security, privacy, and artificial intelligence regulations at a Level 4 (Regulated Enterprise) maturity level. It provides a mapping of specific engineering controls to global regulatory frameworks and details audit readiness protocols.

---

## Control Mapping Matrix

| Control ID | Control Description | Mapping Frameworks | Evidence Required | Traceability Link |
|---|---|---|---|---|
| **CTRL-PRIV-01** | DPDP India consent verification; explicit opt-in captured before sending messages. | DPDP India (Sec 6), GDPR (Art 6) | Consent records in encrypted DB tables with timestamp and text. | `Documentation Galore/07-Governance/Compliance.md` |
| **CTRL-PRIV-02** | Right to Erasure; deletion of customer PII data within 30 days of request. | DPDP India (Sec 12), GDPR (Art 17) | Anonymization script execution logs; DB deletion receipts. | `Documentation Galore/06-Operations/Runbooks.md#Runbook-8` |
| **CTRL-PRIV-03** | Data Residency; all customer data of Indian citizens hosted in ap-south-1 Mumbai. | DPDP India, GDPR | AWS Terraform infrastructure manifests mapping subnets. | `src/infra/terraform/subnets.tf` |
| **CTRL-SEC-01** | Branch protection and mandatory dual human reviews for high-risk code changes. | SOC2 (CC8.1), ISO 27001 (A.12) | Git repository history showing signed commits and pull request logs. | `Documentation Galore/12-AI-EOS/05-repository-operating-model.md` |
| **CTRL-SEC-02** | Automatic SAST, SCA, and secrets scanning on commit. | SOC2 (CC7.1), ISO 27001 (A.14) | GitHub Actions workflow execution logs and scanning dashboards. | `Documentation Galore/12-AI-EOS/15-cicd-governance.md` |
| **CTRL-AI-01** | Human-in-the-loop validation of autonomous agent actions for high-risk operations. | EU AI Act (Art 14), NIST AI RMF (Gov) | Audit logs showing prompt, tool choices, and human signature token. | `Documentation Galore/12-AI-EOS/09-agent-trust-framework.md` |
| **CTRL-AI-02** | Continuous evaluation of prompt templates and LLM outputs for safety and quality metrics. | EU AI Act (Art 15), NIST AI RMF (Measure) | Quality database logs, test datasets, validation execution results. | `Documentation Galore/12-AI-EOS/18-knowledge-quality-framework.md` |

---

## Audit Readiness Model

To remain audit-ready at all times, Conductor operates under a continuous evidence-collection architecture:

```
[System Event] ──> [Automated Telemetry/Logs] ──> [Evidence Archiver] ──> [Immutable S3 Bucket]
                                                                                │
                                                                                ▼
                                                                     [Continuous Audit Engine]
```

### 1. Evidence Archiver
Every compliance-sensitive action (e.g., database backups, user deletions, code deployments, prompt promotions) publishes an audit event to a secure, append-only S3 bucket.

### 2. Automated Self-Audits
An internal compliance bot audits system configurations daily:
* Verifies that encryption-at-rest is enabled on all PostgreSQL database instances.
* Scans codebase for un-parameterized SQL queries (prevention of SQL injection).
* Audits active agent configurations in production against the allowed agent registry.

### 3. Incident Response and Breach Notifications
* If a data breach is detected, the incident classification defaults to P0.
* Notification SLA: DPBI (Data Protection Board of India) and relevant GDPR supervisory authorities must be notified within 72 hours of detection.

---

## Lifecycle Policy
* **Review Cycle:** Semi-annually.
* **Revision Process:** Modifications require approvals from the Governance & Compliance Authority and Legal Counsel.

## Validation Rules
* Any system component deployed without a mapped Control ID in `eos-manifest.yaml` fails compliance verification.

## Audit Requirements
* External auditor reviews of SOC2 Type II, ISO 27001, and EU AI Act conformity are conducted annually, using the archived S3 evidence bucket as the primary data source.
