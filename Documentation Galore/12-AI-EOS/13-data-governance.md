# AI-EOS Data Governance

## Document Metadata
* **id:** EOS-13-DATA-GOV
* **title:** AI-EOS Data Governance
* **description:** Defines data classifications, data sovereignty rules, encryption requirements, and lifecycle retention matrices.
* **owner:** Data Platform Lead & Compliance Officer
* **domain:** Enterprise Data
* **tags:** [data, sovereignty, residency, retention, encryption, lineage]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:54:00Z
* **updated:** 2026-06-24T16:54:00Z
* **related_artifacts:** [01-constitution.md, 11-compliance-governance.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 4 — Critical
* **compliance_tags:** [DPDP-India, GDPR, SOC2-CC6.5]
* **quality_score:** 1.00

---

## Purpose
This document establishes rules for handling, storing, and purging data within the Conductor platform. It guarantees data sovereignty (specifically under DPDP India data localization and GDPR requirements) and defines encryption and retention policies.

---

## Data Residency & Sovereignty Map

Conductor strictly isolates databases and data replication strategies based on customer geolocations:

| Customer Origin | Primary Storage Location | Disaster Recovery Location | Data Replication Limits |
|---|---|---|---|
| **India (DPDP India)** | AWS `ap-south-1` (Mumbai) | AWS `ap-south-2` (Hyderabad) | **NO** replication, caching, or transmission to servers located outside the Republic of India. |
| **European Union (GDPR)** | AWS `eu-west-1` (Ireland) | AWS `eu-central-1` (Frankfurt) | **NO** un-encrypted outbound replication outside the EU boundary. EU-standard contractual clauses (SCCs) apply. |
| **Rest of World** | AWS `us-east-1` (N. Virginia) | AWS `us-west-2` (Oregon) | Standard global sync protocols. |

---

## Data Lifecycle & Retention Matrix

Data assets are classified into four distinct levels, each carrying explicit retention thresholds:

```
┌──────────────────┐
│  Data Ingest     │
└────────┬─────────┘
         ├───────────────► Public Data (Indefinite / Git)
         ├───────────────► Internal Config (Active lifecycle)
         ├───────────────► Sensitive Operations (2 Years / DB)
         └───────────────► Restricted PII (30 Days Erasure / Encrypted)
```

| Data Classification | Type of Information | Encryption Level | Retention Policy | Disposal Method |
|---|---|---|---|---|
| **Public** | Public website, documentation. | None required | Indefinite | N/A |
| **Internal** | System logs, codebase config (no credentials), trace outputs. | AES-256 (At rest) | 90 days | Overwrite / automated delete |
| **Sensitive** | Conversation histories, NATS message payloads, agent states. | AES-256 (At rest), TLS 1.3 (Transit) | 2 years | Cryptographic erasure (delete key) |
| **Restricted (PII)** | Customer phone numbers, consent logs, metadata records. | Column-level Encryption (AES-GCM), KMS | Active lifecycle + 30 days after deletion request | Secure shredding / Database hard delete |

---

## Data Lineage & Traceability

* Every write operation to the primary database must record a metadata lineage tag indicating:
  - `origin_tenant_id`: The tenant ID owning the record.
  - `write_source_id`: The ID of the user or agent that triggered the write.
  - `data_authority`: The verified legal basis for the record (e.g., `consent_opt_in`).
* Cross-tenant queries are blocked at the database driver wrapper level by enforcing dynamic query parameter overrides (`WHERE tenant_id = ?`).

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Modifications must be approved by the Data Platform Lead and Governance Board.

## Validation Rules
* CI/CD scans Terraform deployment configurations to ensure databases are provisioned only in approved geolocations.

## Audit Requirements
* Semi-annual data integrity audits run verification queries on production databases to verify that zero cross-border replication has occurred for Indian citizen records.
