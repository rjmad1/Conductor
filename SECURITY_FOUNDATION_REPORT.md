# Security Foundation Report — Conductor Platform

**Report ID:** SEC-2026-06-Conductor  
**Status:** APPROVED WITH CONDITIONS  
**Date:** June 24, 2026  
**Auditor:** Security & Compliance Architect Agent  

---

## 1. Executive Summary

This report establishes the canonical security architecture, compliance framework, and operational guardrails for the Conductor Platform. By mapping threats, trust boundaries, multi-tenancy isolation strategies, and compliance controls, this document provides the final coordinating foundation for all subsequent feature implementation activities. No feature development may commence until the policies, standards, and gates defined herein are approved.

---

## 2. Security Architecture Overview

The Conductor Platform implements a **Defense-in-Depth** security architecture. Security boundaries are validated at each transaction tier:

```
[Public Client] ──► [Kong API Gateway] ──► [Spring Boot Monolith] ──► [PostgreSQL Database]
                       │                           │                         │
                 JWT Check & Domain          Thread-Local Context      Hibernate Filter
                 Tenant Resolution           X-Tenant-ID Header        AND tenant_id = :val
```

*   **Boundary Authentication:** The Ingress gateway validates OpenID Connect (OIDC) tokens issued by Keycloak before routing requests downstream.
*   **Logical Tenancy Propagation:** Gateway resolves user tenant scopes and injects an immutable `X-Tenant-ID` downstream context.
*   **Database Partitioning:** Data access is logical. Hibernate `@FilterDef` AOP interceptors append tenant constraints to all SQL operations.
*   **Egress Boundaries:** Integration adapters invoke third-party platforms (Zoho, Shopify) exclusively through the Squid egress forward proxy, protecting local subnets from Server-Side Request Forgery (SSRF) vulnerabilities.

---

## 3. Threat Summary (STRIDE Highlights)

A comprehensive STRIDE threat model has been compiled in [THREAT_MODEL.md](file:///c:/Users/rajaj/Projects/Conductor/THREAT_MODEL.md). The critical threat vectors and their mitigations include:

1.  **Cross-Tenant Data Exposure (Information Disclosure):** Mitigated by Spring Security scopes verification, Hibernate query filters, and Qdrant metadata vector tags search filtering.
2.  **Server-Side Request Forgery via Webhooks (Information Disclosure / DoS):** Mitigated by routing all integration traffic through Squid forward proxies, blocking local loopbacks and internal AWS IP ranges.
3.  **Audit Trail Modification (Tampering):** Mitigated by database-level triggers enforcing insert-only rules on partitioned `audit_logs` schemas.
4.  **PII Leakage in Observability Logs (Information Disclosure):** Mitigated by Logback mask filters that obscure phone numbers, email parameters, and authorization signatures.

---

## 4. Compliance Summary

The platform's controls satisfy the requirements of the following regulations and standards (mapped in detail in [COMPLIANCE_MATRIX.md](file:///c:/Users/rajaj/Projects/Conductor/COMPLIANCE_MATRIX.md)):

*   **DPDP India 2023:** Enforces local data residency within the AWS Mumbai (`ap-south-1`) region. Adheres to the 30-day compliance SLA for customer erasure requests. Logs consent history in the immutable `consent_records` ledger.
*   **GDPR:** Adheres to right-to-erasure and transmissions security guidelines using TLS 1.3.
*   **Meta WhatsApp Policy:** Mandates official Meta Cloud APIs and implements 5-second opt-out keyword processing.
*   **SOC 2 Type II:** Implements realm-level Keycloak logical access, TLS 1.3, database-trigger auditing, and automated container/dependency scanning.
*   **ISO 27001:2022:** Restricts egress via Squid proxies, isolates containers on Docker subnet networks, and rotates secrets.
*   **OWASP ASVS & Top 10:** Mitigates SQL injection via parameterized queries, implements brute-force protection, and scans for outdated components.

---

## 5. Active Security Risks & Mitigations

| Risk Identified | Risk Description | Architectural Mitigation | Residual Risk |
| :--- | :--- | :--- | :--- |
| **Vector Index Tenancy** | Multi-tenancy leaks within the shared Qdrant index context. | Metadata payload tags (`tenant_id`) enforced on all RAG queries. | Low |
| **AI Dependency Health** | LiteLLM and Dify python packages are fast-moving with a high dependency bus factor. | Pinning version dependencies using exact digests and daily Trivy container scans. | Medium |
| **SSRF Webhook Ingress** | Malicious users input internal IP subnets inside integration webhook target fields. | All egress calls are forced through the Squid proxy whitelists. | Low |
| **Audit Logs Storage** | Trigger-based logs expand excessively, consuming storage. | Partitioning table monthly and archiving CSV files to S3 Glacier Vault Locks. | Low |

---

## 6. Required Verification Controls

To maintain the security posture, the following validation checks must run in the pipeline (detailed in [SECURITY_VALIDATION_PLAN.md](file:///c:/Users/rajaj/Projects/Conductor/SECURITY_VALIDATION_PLAN.md)):

1.  **SAST Scanning:** Semgrep scans on all PRs target branches.
2.  **Container Scanning:** Trivy scans container builds, blocking pushes on CVSS score $\ge 9.0$.
3.  **Dependency Checks:** OWASP Dependency-Check scan runs weekly.
4.  **Secret Scanning:** GitLeaks pre-commit hooks verify code does not contain cleartext keys.
5.  **IaC Scanning:** Checkov scans Dockerfiles, Docker-Compose, and Helm manifests.
6.  **SBOM Generation:** Syft creates CycloneDX JSON SBOM artifacts for releases.
7.  **Automated Tenancy Tests:** Integration suite simulates header forgery and cross-tenant data requests to verify isolation rules.

---

## 7. Implementation Constraints for Future Developers

All future code implementation activities must adhere to the rules mapped in [SECURITY_GUARDRAILS.md](file:///c:/Users/rajaj/Projects/Conductor/SECURITY_GUARDRAILS.md) and [AGENT_SECURITY_RULES.md](file:///c:/Users/rajaj/Projects/Conductor/AGENT_SECURITY_RULES.md):

*   **SG-001 (No Plaintext Secrets):** Never commit raw keys.
*   **SG-003 (All APIs Authenticated):** Every REST path requires OIDC validation or signed developer API key checks.
*   **SG-005 (Mandatory Audit):** Mutating changes must trigger database trigger audit entries.
*   **SG-006 (PII Encrypted):** Customer phone numbers and email variables must use AES-256 GCM converter mapping in JPA.
*   **SG-008 (Egress Routing):** Outgoing API requests must route through the Squid forward proxy.
*   **SG-010 (No Cross-Module DB Joins):** Services must not execute joins across domain tables.

---

## 8. Approval Status & Conditions

This Security and Compliance Foundation layer is **APPROVED WITH CONDITIONS**.

### Conditions for Staging Promotion
1.  **ArchUnit Verification:** Integrate automated package import checker checks checking modular monolithic boundaries.
2.  **Trigger Audit Checks:** Confirm trigger executions function correctly under load benchmarks without increasing database write latencies.
3.  **Qdrant Filter Tests:** Run vector search validation test runs proving Tenant A cannot fetch Tenant B vector nodes.

*Report signed off and locked in governance repository.*
