# License Allowlist — Conductor Platform

**Version:** 1.0  
**Status:** Active  
**Last Updated:** June 2026  
**Governs:** All third-party open-source software (OSS) and dependency libraries integrated into the Conductor Platform repository and microservices.

---

## 1. Purpose

This document defines the canonical Open-Source Licensing Policy and approved list of OSS licenses for the Conductor Platform. It is a critical compliance control to mitigate legal and security risks, specifically copyleft contamination (which could compromise the platform's proprietary code) and usage license violations.

Every third-party dependency added to the repository must match one of the approved categories. Any dependency not explicitly covered by this allowlist is blocked and requires legal review.

---

## 2. Approved Permissive Licenses (Green Category)

These licenses are fully approved for direct use, static/dynamic linkage, and embedding within Conductor core microservices. They carry zero copyleft risks.

| License | Description | Commercial Usability |
| :--- | :--- | :--- |
| **MIT** | Permissive, no restriction on sublicensing or modifications. | High |
| **Apache 2.0** | Permissive, requires attribution, includes explicit patent grant. | High |
| **BSD 3-Clause** | Permissive, requires copyright attribution. | High |
| **BSD 2-Clause** | Permissive, identical to BSD 3-Clause but omits advertising clause. | High |
| **PostgreSQL License**| Permissive, MIT-like license used by PostgreSQL. | High |
| **ISC** | Permissive, functionally equivalent to MIT/2-clause BSD. | High |

---

## 3. Copyleft & Conditional Licenses (Yellow Category)

These licenses represent a copyleft risk. They are permitted **only under strict structural containment conditions**. Under no circumstances may code from these components be statically compiled, dynamically linked, or copy-pasted into Conductor microservices.

### Approved Conditional Components & Isolation Mandates

| Component | License Type | Mandate / Mitigation |
| :--- | :--- | :--- |
| **Metabase** | AGPL v3 | **Logical Isolation:** Must run in separate OCI containers. Dashboards are embedded in Conductor client via signed JWT iframes only (no JS package imports). |
| **Grafana** | AGPL v3 | **API-Only Integration:** Isolated namespace deployment. Interaction limited to standardized HTTP APIs and query engines. |
| **Loki** | AGPL v3 | **Log Shipper Boundary:** Run as an independent daemon. No direct libraries imported into Conductor code. |
| **Tempo** | AGPL v3 | **Trace Agent Boundary:** Accessed purely via open telemetry standards (OTLP client). |
| **Windmill** | AGPL v3 | **Process Separation:** Deployed in separate namespace. Executions coordinated over REST API/gRPC. |
| **Twenty CRM** | AGPL v3 | **Integration Isolation:** Accessed exclusively via external gRPC/HTTP endpoints. |

### Architectural Containment Rules
1. **OCI Container Isolation:** Every component under AGPL v3 or GPL must run as an independent, isolated process in its own OCI container.
2. **API-Only Boundary:** Inter-process communication with conditional components must happen strictly over standard network protocols (REST APIs, gRPC, NATS JetStream messages).
3. **No Code Linkage:** Any compile-time import, static library linking, or dynamic binding with AGPL v3 code is strictly prohibited.

---

## 4. Source-Available & Restricted Licenses (Orange Category)

These licenses contain proprietary restrictions, particularly around commercial resale or SaaS hosting. They are approved for internal use only.

| License Type / Component | Usage Restrictions | Compliance Verification |
| :--- | :--- | :--- |
| **Redis (RSALv2 / SSPLv1)** | Cannot host as a commercial managed Redis service. | **Compliant:** Used strictly as an internal transaction cache/queue. No API exposure. |
| **Redpanda (BSL 1.1)** | Cannot resell Redpanda as a managed streaming service. | **Compliant:** Used purely as an internal streaming database. |
| **n8n (Sustainable Use)** | Cannot resell n8n as a workflow SaaS. | **Compliant:** Used purely as an internal automation runtime. |
| **Dify (LangGenius License)**| Cannot host a competitive AI builder SaaS. | **Compliant:** Accessed via backend REST APIs only; no direct UI access. |
| **Activepieces (Community)** | Cannot host competitive workflow automation SaaS. | **Compliant:** Connectors are MIT-licensed; core runtime isolated internally. |

---

## 5. Prohibited Licenses (Red Category)

These licenses are strictly prohibited. Dependencies under these licenses must **never** be added to the Conductor repository.

- **GPL v1 / v2 / v3** (unless isolated via the Container Isolation Mandate and approved by Architecture Lead)
- **AGPL v1 / v2 / v3** (except when used as an independent external server/service under Section 3 guidelines)
- **SSPL** (unless verified as internal-only infrastructure under Section 4 guidelines)
- **Unknown / No License** (dependencies without clear license terms are treated as proprietary and blocked)

---

## 6. Verification and Compliance Governance

1. **Dependency Scans:** The compliance checker scans `build.gradle`, `pom.xml`, and dependency trees to ensure all licenses map to this allowlist.
2. **Pull Request Gate:** Adding any dependency with a license outside the Green category triggers a Hard Gate review before merge.
3. **Audit Frequency:** Dependency licensing compliance is assessed on every release candidate build as part of `LOOP-303` (Compliance).

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial version establishing the open-source license allowlist for Conductor MVP.
