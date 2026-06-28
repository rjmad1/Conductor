---
# PROVENANCE METADATA
Original Path: docs/loops/shared/verification-standards.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Output testing and verification standard.
Generalized Purpose: Output testing and verification standard.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Verification Standards — Canonical Reference

**Version:** 1.0
**Status:** Active
**Type:** Reference Document
**Authority:** Principal AI Engineering Architect
**Applies To:** All LOOP-XXX documents in `docs/loops/`

---

## Purpose

This document provides the canonical definitions of the verification levels that every AEOS loop must apply to its outputs. Every loop's Verification section, and LOOP-STANDARD §14, defer to this document for authoritative level definitions.

Verification in the AEOS framework is not optional and is not a post-hoc check. It is a structural component of every loop: the loop's correctness guarantee rests on the Verification section's criteria being independently satisfiable by a reviewer who was not involved in producing the outputs.

---

## Verification Levels

### Level 1 — Automated

Level 1 verification consists of machine-executable checks that produce a binary pass/fail result without human involvement. Level 1 checks must run to completion and produce a recorded result before any Level 2 or Level 3 check begins.

**Defining characteristics:**

- Fully automated: no human judgment is required to evaluate the criterion.
- Binary outcome: the criterion either passes or fails. Partial passes are not permitted.
- Reproducible: the check must produce the same result when run by any agent with access to the declared artifacts.
- Non-destructive: the check must not modify the artifacts it is checking.
- Documented as runnable assertions: each Level 1 criterion in a loop's Verification section must be expressible as a command, assertion, or check that can be executed by the Checker agent.

**Examples of valid Level 1 criteria:**

- File exists at the declared path and is non-empty.
- `git diff --name-only HEAD~1` shows only the files declared in the loop's Outputs section (no unauthorized modifications).
- Count of entries in the module catalog equals count of build files in the declared directory.
- No secrets-pattern matches in any output file (checked via a pattern scanner).
- All declared VER criteria have a recorded result of `passed` in the STATUS file.
- The STATUS file `status` field equals `completed`.

**What Level 1 cannot do:**

Level 1 cannot assess quality, correctness relative to requirements, architectural alignment, or adequacy of human-readable prose. These require Level 2 or Level 3.

---

### Level 2 — Checker Review

Level 2 verification is performed by a Checker agent that is separate from the Maker that produced the primary artifact. The Checker independently reviews the Level 1 results and the primary artifact, then produces a written finding.

**Defining characteristics:**

- Independent: the Checker must not be the same agent as the Maker. The Checker must not have access to the Maker's internal reasoning or scratchpad — only the declared artifacts.
- Written finding: the Checker produces a finding that states `accepted` or `rejected`, with enumerated discrepancies if rejected.
- Scope: the Checker reviews the primary artifact against the loop's declared acceptance criteria, the Level 1 results, and the loop's postconditions.
- If rejected: the Checker's finding must enumerate each discrepancy by criterion ID (VER-N) and describe the observed deviation from the expected state.

**AI Checker constraint:**

If the Checker is an AI agent, a human must review the Checker's finding before any Hard Gate is cleared. The AI Checker's finding is an input to the human's Level 3 decision, not a substitute for it. This constraint applies even when the AI Checker's finding is `accepted`.

**Written finding format:**

```
Checker Finding — LOOP-NNN Run {run-id}
Checker: {agent-id}
Date: {ISO 8601 timestamp}
Level 1 Results: All {N} criteria passed | {N} of {M} criteria passed (see below)
Primary Artifact Review: accepted | rejected
Discrepancies:
  VER-N: {criterion text} — observed: {what the checker found} — expected: {what was required}
Overall: accepted | rejected
```

---

### Level 3 — Human Approval

Level 3 verification is explicit approval by a named human or designated human role. Level 3 is required for all Hard Gates and for any output that modifies shared infrastructure, production data, or published interfaces.

**Defining characteristics:**

- Named approver: the approver must be identified by name or role. Anonymous approvals do not satisfy Level 3.
- Explicit: the approver must state approval in writing. Silence, absence of objection, or implicit acceptance does not satisfy Level 3 (that is a Soft Gate outcome, not Level 3 verification).
- Recorded: Level 3 approval is recorded in the STATUS file's `gate_outcomes` section (see `human-oversight-gates.md`).
- Role-bounded: the approver must hold the role declared in the gate specification. An approver outside the declared role does not satisfy Level 3, even if they are senior to the declared role.

**When Level 3 is required:**

- Any Hard Gate clearance.
- Any output that is published to an external system (production database, external API, public repository).
- Any output that modifies a shared interface (API contract, event schema, authentication boundary).
- Any output that is cited as evidence in a compliance or governance record.

---

## Evidence Standards

Every verification criterion result must be supported by specific, reproducible evidence. A criterion recorded as `passed` without cited evidence is a **verification integrity failure** — equivalent to a failed criterion for conformance purposes.

### Valid Evidence Forms

| Evidence Type | Example |
|---|---|
| File path and content excerpt | `docs/aeos/AEOS-001-Architecture.md`, lines 1-5: `# AEOS-001...` (non-empty, correct title) |
| Command and its output | `git diff --name-only HEAD~1` → `docs/loops/shared/SPEC-005-Metrics.md` (only declared file) |
| Metric value and its source | `verification.criteria_passed: 10` from `STATUS-001.md` current run record |
| Direct comparison | Expected: `status: completed`; Observed: `status: completed` in STATUS-001.md line 12 |

### Invalid Evidence Forms

The following do not constitute valid evidence:

- "Criterion passed per the Maker's self-report."
- "Verified by inspection." (without a citation)
- "No issues found." (without criteria-by-criteria results)
- A reference to a file that was not declared as an output of this loop.

---

## Verification Scope

A loop's verification criteria cover **only that loop's own outputs**. Specifically:

- A loop may not claim credit for verification performed by a downstream loop.
- A loop may not declare a criterion satisfied based on the fact that a downstream loop will check it.
- A loop may not use the successful completion of an upstream loop as evidence for its own criteria (the upstream loop's outputs are inputs, not evidence of this loop's correctness).

If a criterion cannot be verified within the loop's own run (e.g., because the artifact is not yet in its final state), the criterion must be deferred to the appropriate downstream loop and the deferral must be noted with a reference to the downstream loop's criterion ID.

---

## Minimum Criteria Count

Every loop must declare at least **8 VER-N criteria** in its Verification section (SPEC-001 §6.C2). This minimum exists because loops with fewer criteria have historically produced systematic blind spots in verification.

### Mandatory Criterion Types

Among the 8 or more criteria, the following types must be present in every loop:

| Mandatory Type | Description |
|---|---|
| Output existence/non-emptiness | At least one criterion confirming each declared output file exists and is non-empty |
| No unauthorized modifications | At least one criterion confirming `git diff` shows only declared output files |
| STATUS file updated | At least one criterion confirming the STATUS file reflects the current run's completion state |
| Reflection artifact produced | At least one criterion confirming the Reflection artifact was written |

These four mandatory types count toward the minimum of 8. A loop with exactly 8 criteria may satisfy the minimum with these 4 types plus 4 additional loop-specific criteria.

---

## Independent Executability

Every criterion must be evaluable by a reviewer with access **only to the declared artifacts** — without re-running the loop, consulting the producing agent, or observing intermediate states.

This means:

- Each criterion must name the artifact(s) it checks.
- Each criterion must describe the check in enough detail that an agent or human who was not present for the run can execute the check against the artifacts.
- A criterion that depends on internal loop state (e.g., "the agent determined that X") is not independently executable and must be rewritten to reference an artifact that records that determination.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` §14 — Verification section requirements
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §6 — Verification contract
- `docs/loops/shared/human-oversight-gates.md` — Gate types and audit requirements
- `docs/loops/shared/metrics-definitions.md` — Metric definitions for verification metrics

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

