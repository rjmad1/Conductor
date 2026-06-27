---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-010-Loop-Catalog.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Registry index and structure for all active loops.
Generalized Purpose: Registry index and structure for all active loops.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-010 — Loop Catalog

**Version:** 1.0
**Status:** Active
**Type:** Engineering Specification
**Governs:** The authoritative registry of all loop identifiers in the AEOS framework
**Authority:** Principal AI Engineering Architect

---

## Purpose

This document is the canonical registry of all loop identifiers in the AEOS framework. It is the source of truth for ID assignment, lifecycle state, ownership, and inter-loop dependency relationships. No loop is valid unless it appears in this catalog. No loop ID may be reused, even after the loop is retired or archived.

This catalog is maintained by the Principal Architecture function. It must be updated within 5 business days of any lifecycle transition (Draft → Active, Active → Deprecated, etc.).

---

## §1 — Catalog Purpose and Authority

The Principal Architecture function holds the authority to assign loop IDs and to update this catalog. The catalog assignment process is defined in SPEC-011 §Phase 2 (Proposal): a loop enters this catalog with status `Proposed` when the Proposal phase is complete and the Principal Architecture function assigns an ID. The loop remains `Proposed` until it achieves `Active` status through the full SPEC-011 lifecycle.

**No loop specification may claim an ID that is not in this catalog.** A loop specification that bears an ID not present in this catalog is invalid and must not be executed.

**ID reuse is prohibited.** Once an ID appears in this catalog, it belongs to the loop that was assigned it in perpetuity. Retired and archived loops retain their IDs in the Deprecated/Archived Registry (§4) forever.

---

## §2 — Catalog Entry Fields

Each catalog entry records the following fields:

| Field | Description |
|---|---|
| **Loop ID** | The permanent loop identifier (LOOP-NNN) |
| **Name** | Short noun phrase matching the loop's declared Name field |
| **Category** | One of: Core, Engineering, Platform, Governance, Release, AI, Research, Experimental (see ID ranges in §6) |
| **Status** | Current lifecycle state: Proposed, Active, Deprecated, Archived |
| **Version** | Current version of the loop specification (MAJOR.MINOR) |
| **Owner** | Role responsible for engineering correctness |
| **Maintainer** | Role responsible for specification currency |
| **Introduced** | Date the loop first achieved Active status (ISO 8601) |
| **Last Reviewed** | Date of most recent currency review (ISO 8601) |
| **Depends On** | Comma-separated list of Loop IDs this loop consumes outputs from |
| **Known Downstream Consumers** | Loop IDs that declare this loop as a dependency |
| **Notes** | Co-revision history, compatibility pins, or other catalog-level annotations |

---

## §3 — Active Loop Registry

All loops currently in `Active` status. Updated as of 2026-06-27.

| Loop ID | Name | Category | Status | Version | Owner | Maintainer | Introduced | Depends On |
|---|---|---|---|---|---|---|---|---|
| LOOP-001 | Architecture Discovery | Core | Active | 1.1 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | None |
| LOOP-002 | Context Assembly | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-001 |
| LOOP-003 | Task Discovery | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-001, LOOP-002 |
| LOOP-004 | Planning | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-002, LOOP-003 |
| LOOP-005 | Implementation | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-004 |
| LOOP-006 | Verification | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-005 |
| LOOP-007 | Reflection | Core | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-26 | LOOP-006 |
| LOOP-101 | Bug Fixing | Engineering | Active | 1.0 | Principal Engineering Function | Principal Engineering Function | 2026-06-27 | LOOP-002, LOOP-004, LOOP-006 |
| LOOP-102 | Refactoring | Engineering | Active | 1.0 | Principal Engineering Function | Principal Engineering Function | 2026-06-27 | LOOP-002, LOOP-004, LOOP-006 |
| LOOP-103 | Test Generation | Engineering | Active | 1.0 | Principal Engineering Function | Principal Engineering Function | 2026-06-27 | LOOP-001, LOOP-002, LOOP-005, LOOP-006 |
| LOOP-104 | Documentation | Engineering | Active | 1.0 | Principal Engineering Function | Principal Engineering Function | 2026-06-27 | LOOP-001, LOOP-002 |
| LOOP-201 | Workflow Validation | Platform | Active | 1.0 | Platform Engineering Function | Platform Engineering Function | 2026-06-27 | LOOP-006 |
| LOOP-202 | Integration Validation | Platform | Active | 1.0 | Platform Engineering Function | Platform Engineering Function | 2026-06-27 | LOOP-006 |
| LOOP-203 | Event Validation | Platform | Active | 1.0 | Platform Engineering Function | Platform Engineering Function | 2026-06-27 | LOOP-006 |
| LOOP-204 | API Contract Validation | Platform | Active | 1.0 | Platform Engineering Function | Platform Engineering Function | 2026-06-27 | LOOP-006 |
| LOOP-301 | ADR Generation | Governance | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-27 | LOOP-004 |
| LOOP-302 | Documentation Governance | Governance | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-27 | LOOP-007 |
| LOOP-304 | Release Readiness | Governance | Active | 1.0 | Principal Architecture Function | Principal Architecture Function | 2026-06-27 | LOOP-006, LOOP-007 |

### §3.1 — Known Downstream Consumer Index

This index is derived from the Depends On column above and is provided for upstream maintainers to identify which loops must be notified before a MAJOR increment.

| Loop ID | Known Downstream Consumers |
|---|---|
| LOOP-001 | LOOP-002, LOOP-003, LOOP-103, LOOP-104, LOOP-105 |
| LOOP-002 | LOOP-003, LOOP-004, LOOP-101, LOOP-102, LOOP-103, LOOP-104, LOOP-105 |
| LOOP-003 | LOOP-004 |
| LOOP-004 | LOOP-005, LOOP-101, LOOP-102, LOOP-301 |
| LOOP-005 | LOOP-006, LOOP-103, LOOP-206 |
| LOOP-006 | LOOP-007, LOOP-101, LOOP-102, LOOP-201, LOOP-202, LOOP-203, LOOP-204, LOOP-205, LOOP-207, LOOP-303, LOOP-304 |
| LOOP-007 | LOOP-302, LOOP-304 |
| LOOP-207 | LOOP-303 |
| LOOP-304 | LOOP-401 |
| LOOP-401 | LOOP-402 |
| LOOP-402 | LOOP-403 |

---

## §4 — Deprecated/Archived Registry

No loops have been deprecated or archived as of this version. Entries will be added here as loops are retired, with their final version, deprecation date, replacement loop ID, and reason for deprecation.

| Loop ID | Name | Final Version | Deprecated | Archived | Replaced By | Reason |
|---|---|---|---|---|---|---|
| — | — | — | — | — | — | — |

---

## §5 — Reserved/Proposed Registry

The following loop specifications are proposed and in Draft status. Content has not yet been fully authored.

| Loop ID | Name | Status | Proposed By | Proposed Date | Notes |
|---|---|---|---|---|---|
| LOOP-105 | Code Review | Proposed | Principal Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-205 | Multi-Tenant Validation | Proposed | Platform Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-206 | Observability Validation | Proposed | Platform Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-207 | Security Validation | Proposed | Platform Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-303 | Compliance | Proposed | Principal Architecture Function | 2026-06-27 | Reverted to Draft 0.2 due to draft dependency |
| LOOP-401 | Release Checklist | Proposed | Principal Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-402 | Deployment Validation | Proposed | Principal Engineering Function | 2026-06-27 | Planned draft skeleton |
| LOOP-403 | Post-Release Verification | Proposed | Principal Engineering Function | 2026-06-27 | Planned draft skeleton |

---

## §6 — ID Assignment Rules

Loop ID assignment follows the category ranges and rules defined in SPEC-011. The Principal Architecture function assigns IDs sequentially within the applicable range. Ranges are:

| Range | Category | Description |
|---|---|---|
| 001–099 | Core | Foundational loops that form the base execution chain |
| 100–199 | Engineering | Domain-specific engineering work loops |
| 200–299 | Platform | Platform validation and runtime assessment loops |
| 300–399 | Governance | Framework governance and compliance loops |
| 400–499 | Release | Release, deployment, and post-release verification loops |
| 500–599 | AI | AI-specific workflows (reserved for future use) |
| 600–699 | Research | Research and exploration loops (reserved for future use) |
| 700–799 | Experimental | Experimental loops pending promotion or retirement |
| 800–999 | Reserved | Reserved by Principal Architecture for future categories |

### §6.1 — Assignment Rules

1. IDs are assigned in ascending order within a category range. No gaps are introduced intentionally; gaps arising from retired loops are preserved, not reused.
2. The Principal Architecture function is the sole authority for ID assignment. No team may self-assign a Loop ID.
3. A Proposed loop receives its ID before the loop specification is authored, enabling the ID to appear in cross-references from other loop specifications during authoring.
4. An ID assigned to a Proposed loop that does not reach Active status within 180 days is moved to the Reserved registry with a note explaining its status.

---

## §7 — Maintenance Obligations

The Principal Architecture function must update this catalog within 5 business days of any of the following events:

- A loop transitions to any new lifecycle state (Proposed, Active, Deprecated, Archived).
- A loop's MAJOR version is incremented.
- A co-revision relationship is established between two loops.
- A new downstream consumer dependency is declared.
- A Framework Owner is designated for a repository adopting the AEOS framework.

Failure to maintain this catalog within the required window is a governance event (SPEC-006 §4) that must be recorded in the governance event log.

---

## References

- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §1 — Loop identity contract
- `docs/loops/shared/SPEC-006-Governance.md` — Governance obligations
- `docs/loops/shared/SPEC-007-Versioning.md` — Co-revision protocol and compatibility matrix
- `docs/loops/shared/SPEC-011-LOOP-AUTHORING-GUIDE.md` — Loop proposal and ID assignment process

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version; 26 loops registered |

