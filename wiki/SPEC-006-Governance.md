---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-006-Governance.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Governance, human oversight gates, and emergency stop protocol.
Generalized Purpose: Governance, human oversight gates, and emergency stop protocol.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-006 — Governance

**Version:** 1.0
**Status:** Active
**Type:** Engineering Specification
**Governs:** Ownership, review, and governance obligations for all LOOP-XXX documents and AEOS framework adopters
**Authority:** Principal AI Engineering Architect

---

## Purpose

This specification defines the governance obligations for every loop specification in the AEOS framework and for every engineering team that adopts the framework. Governance in this context means: who is responsible for a loop's correctness, how changes are reviewed and approved, what events must be recorded as governance records, and what obligations arise from operating the AEOS framework in a repository.

A loop that is `Active` but has no declared Owner or Maintainer is not governed. An ungoverned loop cannot be trusted: there is no person or role accountable for its correctness, no reviewer responsible for keeping it current, and no escalation path when the loop behaves unexpectedly. SPEC-006 closes this gap by making ownership and review obligations normative.

---

## §1 — Ownership Model

Every `Active` loop must declare three governance roles in its identity block. No role may be vacant for an Active loop.

### §1.1 — Owner

The Owner is the role or individual responsible for the loop's engineering correctness. Owner responsibilities include:

- Approving all MAJOR version increments before they are merged.
- Being the final arbiter of scope disputes about what the loop does and does not do.
- Signing off on GATE-1 escalations that cannot be resolved by the executing team.
- Ensuring that loop execution results (Reflections, gate outcomes) are reviewed periodically.

The Owner need not be a single individual. A team or function role (e.g., "Principal Engineering Function") satisfies this requirement.

### §1.2 — Maintainer

The Maintainer is the role responsible for keeping the specification current as the systems it references evolve. Maintainer responsibilities include:

- Approving MINOR version increments.
- Reviewing the loop for currency at the quarterly cadence (§3.1).
- Tracking upstream loop changes that may require a co-revision (SPEC-007).
- Flagging the loop as stale when its dependent systems have changed but the loop has not been updated (§3.2).

The Maintainer may be the same role as the Owner but must be a distinct named individual if the Owner is an individual rather than a function role.

### §1.3 — Principal Architecture

The Principal Architecture function is the framework governance authority. Principal Architecture responsibilities include:

- Approving the addition of new loops to the catalog (SPEC-010).
- Signing off on MAJOR version increments to any SPEC-NNN document.
- Maintaining the SPEC-NNN document suite.
- Being the escalation path when Owner or Maintainer roles are vacant.

The Principal Architecture function must be declared at the repository level (see §6). It is not declared per loop.

---

## §2 — Review Obligations

### §2.1 — MAJOR Version Increment

A MAJOR version increment (e.g., 1.x → 2.0) requires:

1. Owner written approval, recorded in the Version History entry.
2. Principal Architecture sign-off, recorded in the loop's Version History entry.
3. A governance event record (§5) noting the MAJOR increment, the rationale, and all known downstream consumers that must be notified.
4. A co-revision assessment under SPEC-007 to determine whether any downstream loops require simultaneous updates.

MAJOR increments must not be merged without both approvals. A PR that changes a loop from version N.x to N+1.0 without these approvals is not eligible for merge.

### §2.2 — MINOR Version Increment

A MINOR version increment (e.g., 1.0 → 1.1) requires:

1. Maintainer review and approval, recorded in the Version History entry.
2. No change to the loop's gate surface area (adding or removing gates requires a MAJOR increment).
3. No change to the loop's input/output contracts (contract changes require a MAJOR increment).

### §2.3 — Contributor Proposals

Any contributor (human or AI) may propose a revision to any loop specification. The proposal process is:

1. Contributor opens a PR or files a documented proposal.
2. The Maintainer reviews for technical correctness, scope alignment, and SPEC-001 conformance.
3. The Owner approves the change before it is applied.
4. If the change would require a MAJOR increment, the §2.1 process applies.

Reviewers must not self-approve changes to loops they own unless no other qualified reviewer is available, in which case the Principal Architecture function must co-approve.

---

## §3 — Maintenance Cadence

### §3.1 — Quarterly Review

Every `Active` loop must be reviewed for currency at least once per calendar quarter. Currency means:

- All referenced upstream loops still exist and are at compatible versions.
- The Agents roster reflects the agents currently available in the framework.
- The Verification criteria remain executable against current tooling.
- The Risks section reflects the current threat model.
- The Metrics section conforms to the current SPEC-005.

The Maintainer is responsible for initiating quarterly reviews. Review completion is recorded in the STATUS file under `last_reviewed`.

### §3.2 — Stale Loop Flagging

A loop must be flagged as `Stale` (a sub-state of `Active`) when all of the following are true:

- More than 6 months have elapsed since the last recorded review (`last_reviewed` in STATUS).
- At least one system the loop depends on has shipped a change that may affect the loop's correctness.
- The loop has not been updated to reflect that change.

A Stale loop may continue to execute but must display a warning in its STATUS file. The Maintainer has 30 days from stale flagging to either update the loop or record a rationale for why no update is needed.

---

## §4 — Governance Events

The following events, when they occur during loop execution or loop lifecycle management, are governance events. Each governance event must produce a governance record (§5).

| Event ID | Event | Trigger Condition |
|---|---|---|
| GE-01 | GATE-1 Denial | A Hard Gate is evaluated and the human reviewer denies approval |
| GE-02 | Emergency Stop | An Emergency Stop is invoked on any running loop |
| GE-03 | Checker Override | A Checker agent's finding is overridden without a documented rationale from the Owner |
| GE-04 | Unresolved Reflection Finding | A Reflection artifact records an improvement candidate that has not been addressed within 30 days |
| GE-05 | Precondition Bypass | A loop is executed without verifying all declared preconditions |
| GE-06 | Tenant Isolation Breach | A loop execution touches data belonging to a tenant it was not invoked for |
| GE-07 | MAJOR Version Increment | A loop or specification document advances to a new MAJOR version |
| GE-08 | Loop Deprecation | A loop's status is set to `Deprecated` |

---

## §5 — Governance Records

### §5.1 — STATUS File Record

Every governance event must be recorded in the loop's STATUS file under the `governance_events` block:

```yaml
governance_events:
  - event_id: GE-01
    date: "2026-06-27T14:32:00Z"
    loop_id: LOOP-001
    run_id: LOOP-001-20260627-001
    description: "GATE-1 denied: architecture diagram missing tenant boundary labels"
    reviewer: "Principal Architecture Function"
    resolution: "pending"
```

### §5.2 — Governance Event Log Files

Governance events GE-01 through GE-08 must also produce a standalone governance event log file at:

```
docs/governance/events/GOVEVENT-{LOOP-ID}-{date}-{NNN}.md
```

Where:
- `{LOOP-ID}` is the loop identifier (e.g., `LOOP-001`).
- `{date}` is the ISO date of the event (e.g., `20260627`).
- `{NNN}` is a zero-padded sequence number for the day (001, 002, etc.).

The governance event log file must contain: event ID, event type, timestamp, loop ID, run ID, trigger description, reviewer identity and role, decision or outcome, and any required follow-up actions with owners and deadlines.

### §5.3 — Governance Record Permanence

Governance records are permanent. They may not be deleted or expunged. Corrections are appended as amendments, never applied in-place to the original record.

---

## §6 — Repository Adoption

A team or repository that adopts the AEOS framework must:

1. Designate a **Framework Owner**: the individual or role responsible for the loop catalog (SPEC-010), the SPEC-NNN document suite, and the Principal Architecture function within that repository.
2. Record the Framework Owner in `docs/aeos/AEOS-001-Architecture.md` or an equivalent framework-level configuration document.
3. Establish a `docs/governance/` directory for governance event log files.
4. Ensure the Framework Owner has write access to all loop specifications and SPEC documents.

Without a designated Framework Owner, the AEOS framework status must not be set to `Active` for any loop in the repository. A repository where the Framework Owner role is vacant must flag all Active loops as `Ungovernance-Warning` in their STATUS files until the role is filled.

---

## §7 — Conformance

A loop specification is conformant with SPEC-006 if and only if:

1. It declares an Owner in its identity block (not blank, not TBD).
2. It declares a Maintainer in its identity block (not blank, not TBD).
3. The repository in which it resides has a designated Framework Owner (§6).
4. Its STATUS file has a `last_reviewed` date not more than 6 months in the past (§3.2).

Non-conformance with §7.1 or §7.2 is a hard conformance failure that blocks `Active` status. Non-conformance with §7.4 triggers Stale Loop flagging (§3.2) but does not immediately block execution.

---

## References

- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — Loop contract authority
- `docs/loops/shared/SPEC-007-Versioning.md` — Co-revision protocol
- `docs/loops/shared/SPEC-010-Loop-Catalog.md` — Loop registry
- `docs/loops/shared/SPEC-011-LOOP-AUTHORING-GUIDE.md` — Authoring guide citing this specification

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

