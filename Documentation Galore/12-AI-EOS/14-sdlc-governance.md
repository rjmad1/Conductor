# AI-EOS SDLC Governance

## Document Metadata
* **id:** EOS-14-SDLC-GOV
* **title:** AI-EOS SDLC Governance
* **description:** Defines the phased engineering gates, specification-first workflow, and testing requirements for Conductor development.
* **owner:** Platform Engineering Lead & Product Architect
* **domain:** Platform Operations
* **tags:** [sdlc, gates, testing, specification, workflow]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:56:00Z
* **updated:** 2026-06-24T16:56:00Z
* **related_artifacts:** [01-constitution.md, 02-governance-architecture.md, 15-cicd-governance.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [ISO-27001-A.14, SOC2-CC8.1]
* **quality_score:** 1.00

---

## Purpose
This document enforces a structured, specification-driven Software Development Life Cycle (SDLC). It maps execution steps to risk tiers and specifies validation gates required to transition code from conception to production.

---

## The Spec-to-Code Pipeline

All human developers and coding agents must follow this multi-phase gate sequence:

```
[Phase 1: RFC/Spec] ──(Approval)──> [Phase 2: Contract] ──(Verification)──> [Phase 3: Code/Test]
                                                                                  │
                                                                               (Merge)
                                                                                  │
                                                                                  ▼
[Phase 6: Release] <──(Canary Validation) <── [Phase 5: Deploy] <── [Phase 4: Build/Scan]
```

### Phase 1: Specification & RFC
* No development can begin without an approved RFC or Architecture Decision Record (ADR) detailing the business value, engineering economics, and complexity class.
* **Gate:** Approved by the Architecture Review Board (ARB).

### Phase 2: Interface Contract
* API schemas (OpenAPI), event payload definitions (AsyncAPI), and database definitions must be written and checked into the repository under `/src/contracts/`.
* **Gate:** Automated schema linters pass; downstream consumer teams verify compatibility.

### Phase 3: Implementation & Local Test
* Code is written to satisfy the contract.
* Mandatory unit and integration test suites are written alongside the code.
* **Gate:** 100% of unit tests pass, and total test coverage meets minimum targets (80% lines, 90% branches).

### Phase 4: CI Build & DevSecOps Scan
* Automated validation run checks SAST, SCA, license compliance, secrets detection, and signed commits.
* **Gate:** Zero "High" or "Critical" vulnerabilities detected.

### Phase 5: Verification & Deploy (Staging)
* Changes are deployed to the Staging environment.
* Dynamic validation and automated E2E integration suites run (simulating customer interactions and webhook failures).
* **Gate:** Staging verification passes; release authorized by SRE and Product owners (proportional to Risk Tier).

### Phase 6: Canary Release (Production)
* Deploy to 10% of users, shifting traffic over a 4-hour window.
* Automated rollback triggers if system errors exceed baseline thresholds.
* **Gate:** Complete rollout with zero incident alarms triggered.

---

## Testing Matrix Requirements

Testing requirements are strictly defined by the change's Risk Tier:

| Test Class | Tier 0 | Tier 1 | Tier 2 | Tier 3 | Tier 4 |
|---|---|---|---|---|---|
| **Unit Testing** | None | Required (>80%) | Required (>85%) | Required (>90%) | Required (>95%) |
| **Integration Testing** | None | None | Required | Required | Required |
| **Security SAST/SCA** | None | Required | Required | Required | Required |
| **Load / Performance** | None | None | None | Required | Required |
| **Disaster Recovery** | None | None | None | None | Required |

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Modifications approved by the Platform Engineering Lead.

## Validation Rules
* CI build rules parse test outputs and block branch merges if coverage drops below the required tier threshold.

## Audit Requirements
* Weekly releases are audited against their originating ticket references, verifying that every change has an associated specification, test run log, and code review approval list.
