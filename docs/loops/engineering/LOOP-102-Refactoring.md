---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/LOOP-102-Refactoring.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Standard loop for safe code refactoring.
Generalized Purpose: Standard loop for safe code refactoring.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-002 — Context Assembly, LOOP-004 — Planning, LOOP-005 — Implementation, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-102 — Refactoring

**Loop ID:** LOOP-102
**Name:** Refactoring
**Version:** 1.0
**Status:** Active
**Category:** Engineering
**Depends On:** LOOP-002 — Context Assembly, LOOP-004 — Planning, LOOP-005 — Implementation, LOOP-006 — Verification
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---

## Purpose

LOOP-102 executes a governed refactoring process that improves internal code structure, readability, or performance characteristics of a declared scope without altering externally observable behaviour. Behavioural equivalence is the governing invariant: every behaviour that was correct before the refactoring must remain correct after. The loop establishes a baseline of passing tests before any code is modified, enforces scope discipline throughout implementation, and verifies equivalence after implementation before the change set is accepted.

---

## Problem Statement

Refactoring without a prior baseline and without enforced scope discipline produces outcomes indistinguishable from uncontrolled code changes. When a refactoring inadvertently changes behaviour, there is no systematic mechanism to detect the change before it is merged. When refactoring scope is not declared and locked before implementation, the change set grows to include bug fixes, feature additions, and stylistic preferences — compounding the review burden and raising regression risk. Without a governed refactoring process, the quality of the improvement cannot be measured, and the risk of the change cannot be bounded.

---

## Why This Loop Exists

Refactoring is structurally different from bug fixing and feature development: its defining constraint — non-behaviour-change — cannot be enforced by source review alone and must be verified by a test suite against a declared baseline. Codifying refactoring as a loop makes that baseline an explicit, auditable artifact; makes the scope declaration a precondition to implementation; makes behavioural equivalence a machine-checkable postcondition; and makes the quality improvement measurable before and after. The Safety-First gate structure ensures that no refactoring proceeds when test coverage is insufficient to prove equivalence — preventing the most common failure mode of well-intentioned structural improvement.

---

## Scope

**In scope:**
- Improving internal code structure: reducing complexity, eliminating duplication, improving cohesion, clarifying naming
- Improving internal performance characteristics without changing external API behaviour
- Restructuring code to improve testability (but not writing the tests — that is LOOP-103)
- Reorganising module internals within declared module boundaries
- Applying architectural patterns already accepted by ADR to existing code that does not yet conform

**Out of scope:**
- Bug fixes (those are LOOP-101); a refactoring that reveals a bug must stop and create a new LOOP-101 task
- New functionality or feature additions
- Changes to public APIs, event schemas, or inter-module contracts
- Test file modifications beyond the minimum necessary to keep tests passing after structural changes (new tests belong in LOOP-103)
- Infrastructure or configuration changes

**Maximum run duration:** 24 hours total. If this duration is exceeded, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Refactoring task record | File (`docs/tasks/task-catalog.md` entry with `primary_category: RFCT`) | LOOP-003 — Task Discovery | Required |
| Refactoring objective | Field within task record: what structural quality metric is being improved | Task record | Required |
| Declared scope | Field within task record: files, classes, or patterns in scope | Task record | Required |
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 — Architecture Discovery | Required — must be no more than 7 days old |
| LOOP-002 context package | Directory (`docs/context/`) | LOOP-002 — Context Assembly | Required — produced during this run |
| Current test suite pass/fail state | Repository State | Test runner via SKILL-001 commands | Required — baseline established in Step 3 |
| Current quality metrics for the scope | Repository State | Analysis tooling (complexity, duplication, coverage metrics) | Required — baseline established in Step 3 |
| Prior refactoring records for the same scope | File (`docs/engineering/refactoring/`) | Prior LOOP-102 runs | Optional |
| Engineering Loop configuration | File (`.loop-102.yml` at repo root) | Repository | Optional |

### Input Validation

Before any step begins, the loop must verify:
- The task record exists, has `primary_category: RFCT`, and declares an explicit scope boundary and quality objective.
- LOOP-001 outputs exist and are within 7 days.
- No other LOOP-102 instance is running for the same task ID (checked via `STATUS-102.md`).
- The repository has a readable git HEAD and a runnable test suite (test runner command available in SKILL-001).
- HEAD SHA is recorded at validation time.

If any required input fails validation, the loop halts with `precondition_failed` and writes no output artifacts.

---

## Outputs

All primary outputs are written to `docs/engineering/refactoring/`.

| Artifact | Path | Description |
|----------|------|-------------|
| Scope Analysis | `docs/engineering/refactoring/scope-analysis.md` | Declared scope boundaries, quality metric being improved, structural problems identified, behaviour invariants |
| Equivalence Baseline | `docs/engineering/refactoring/equivalence-baseline.md` | Pre-refactoring test suite state: which tests exist for the scope, pass/fail counts, coverage metrics, equivalence sufficiency assessment |
| Equivalence Checker Report | `docs/engineering/refactoring/equivalence-checker-report.md` | EQUIV-CHECKER's independent review of the equivalence baseline and GATE-2 recommendation if coverage is insufficient |
| Refactoring Plan | `docs/planning/execution-plan.md` | Produced by LOOP-004: step-by-step refactoring sequence with intermediate checkpoints |
| Verification Report | `docs/verification/verification-report.md` | Produced by LOOP-006: baseline tests still pass; quality metric improved |
| Engineering Summary | `docs/engineering/refactoring/engineering-summary.md` | Complete run summary for human review |
| Loop Status | `docs/loops/engineering/STATUS-102.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/engineering/SKILL-102.md` | Calibration observations accumulated across runs |
| Run Metadata | `docs/engineering/refactoring/metadata/METADATA-102-{run-id}.md` | Provenance: task ID, all Core Loop run IDs, HEAD SHA at start and end, elapsed duration, outcome |
| Reflection | `docs/engineering/refactoring/reflections/REFLECTION-102-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-002 — Context Assembly:** Context package scoped to the refactoring boundary. Required — invoked during this run.
- **LOOP-004 — Planning:** Execution plan for the refactoring steps. Required — invoked during this run.
- **LOOP-005 — Implementation:** Implementation of refactoring per approved plan. Required — invoked during this run.
- **LOOP-006 — Verification:** Verification that all baseline tests still pass and quality metric improved. Required — invoked during this run.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer selects a `RFCT`-classified task from the LOOP-003 backlog and triggers LOOP-102.
2. **Automated task dispatch** — The AEOS task dispatcher selects a `RFCT`-classified task per SPEC-010 routing rules.
3. **Technical debt reduction initiative** — A governance loop or human-initiated technical debt reduction programme schedules a specific refactoring task.

The trigger source, task ID, and current HEAD SHA must be recorded in `STATUS-102.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | A `RFCT`-classified task exists with the selected task ID, declaring an explicit scope and quality objective | Read task catalog; assert entry with `primary_category: RFCT`, non-empty scope, and non-empty objective |
| PRE-2 | LOOP-001 outputs exist and are no more than 7 days old | Read `STATUS-001.md`; assert timestamp within 7 days |
| PRE-3 | No other LOOP-102 instance is running for the same task ID | Read `STATUS-102.md`; assert not `running` for this task ID |
| PRE-4 | The test suite can be executed (test runner command available) | Read `SKILL-001.md`; assert test runner command is populated |
| PRE-5 | Write access to `docs/engineering/refactoring/` | Probe write; remove on success |
| PRE-6 | The declared scope identifies at least one file or module | Read task record; assert non-empty scope field |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem (source) | Read | Files within declared refactoring scope | Filesystem permissions of executing agent | Scoped to declared boundary only | No writes in this loop's own steps | Yes |
| Test runner | Read | Execute test suite on current codebase for baseline; read results | Same as executing agent | Scoped to test suite commands from SKILL-001; no external system calls | Re-run produces equivalent result | Yes |
| `docs/engineering/refactoring/` | Write | All files listed in Outputs table | Same as executing agent | Confined to this directory | `git checkout docs/engineering/refactoring/` restores prior state | Yes |
| `docs/loops/engineering/STATUS-102.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/STATUS-102.md` | Yes |
| `docs/loops/engineering/SKILL-102.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/SKILL-102.md` | Yes |

This loop does not write to any external system outside the repository. Source modifications are delegated exclusively to LOOP-005.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/engineering/LOOP-102-Refactoring.md` — this document
3. `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared context
4. `docs/loops/engineering/STATUS-102.md` — prior run state
5. `docs/loops/core/SKILL-001.md` — test commands, lint commands, complexity analysis tools
6. `docs/architecture/module-catalog.md` — module boundaries
7. `docs/architecture/dependency-map.md` — inter-module dependencies
8. The selected task record from `docs/tasks/task-catalog.md`

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `SCOPE-AGENT` | Maker | Step 1: loads refactoring objective; defines scope boundaries; identifies quality metric being improved | Filesystem read, task record read | None |
| `CONTEXT-AGENT` | Maker | Step 2: invokes LOOP-002 scoped to refactoring boundary; validates HEAD SHA | LOOP-002 invocation | None |
| `EQUIV-AGENT` | Maker | Step 3: establishes equivalence baseline; runs test suite; assesses coverage sufficiency | Test runner, coverage analysis tools | Reports to GATE-2 if coverage insufficient |
| `EQUIV-CHECKER` | Checker | Step 4: independently reviews equivalence baseline; flags insufficient coverage | Filesystem read, independent coverage assessment | Independent of EQUIV-AGENT; recommendation reviewed at GATE-2 and GATE-1 |
| `PLAN-AGENT` | Maker | Step 7: invokes LOOP-004 with refactoring-specific constraints | LOOP-004 invocation | None — scope approved at GATE-1 |
| `IMPL-AGENT` | Maker | Step 8: invokes LOOP-005 to implement refactoring | LOOP-005 invocation | Governed by LOOP-005's own gates |
| `VERIF-AGENT` | Maker | Step 9: invokes LOOP-006 to verify equivalence and quality improvement | LOOP-006 invocation | Governed by LOOP-006's own verification |
| `STATUS-WRITER` | Maker | Final steps: updates STATUS-102.md, SKILL-102.md; produces Engineering Summary, Metadata, Reflection | Filesystem write | None |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1 and GATE-2: reviews scope, equivalence strategy, and coverage assessment | Human judgment | Sole authority for gate decisions |

`EQUIV-AGENT` and `EQUIV-CHECKER` must be separate agent instances.

---

## Workflow

### Step 1 — Scope Definition

**Agent:** `SCOPE-AGENT`
**Inputs:** Task record from LOOP-003
**Outputs:** `docs/engineering/refactoring/scope-analysis.md`

Load the refactoring task. Extract and formalize:
- **Scope boundary:** The precise set of files, classes, modules, or patterns in scope. Any file not in this set must not be modified.
- **Quality metric being improved:** One of — cyclomatic complexity, code duplication percentage, class cohesion score, method length distribution, test coupling, or naming consistency. The metric must be measurable before and after.
- **Structural problems identified:** The specific instances of the quality problem in the scope (e.g., "method `processOrder` in `OrderService.java` has cyclomatic complexity of 24; target is below 10").
- **Behaviour invariants:** The externally observable behaviours that must remain identical after refactoring (API contracts, event schemas, return values for all existing callers).
- **Architectural constraints:** Accepted ADRs and module boundaries that must be respected throughout the refactoring.

Write `scope-analysis.md`. Update `STATUS-102.md` to `running`.

---

### Step 2 — Context Assembly

**Agent:** `CONTEXT-AGENT`
**Inputs:** Scope analysis from Step 1, LOOP-001 architecture outputs
**Outputs:** Context package at `docs/context/` (produced by LOOP-002)

Invoke LOOP-002 parameterised with:
- The scope boundary from Step 1
- Tier 1 Required: source files within scope; test files covering the scope
- Tier 2 Required: inter-module dependency context for the scope boundary
- Tier 3 Optional: callers of any public interfaces within the scope

Validate context package HEAD SHA against current repository HEAD SHA. If mismatch, trigger GATE-1.

---

### Step 3 — Equivalence Baseline

**Agent:** `EQUIV-AGENT` (Maker)
**Inputs:** Context package from Step 2, scope analysis from Step 1
**Outputs:** `docs/engineering/refactoring/equivalence-baseline.md`

Execute the test suite using the test runner command from `SKILL-001.md`. Record:
- Which tests exist that exercise code within the refactoring scope (by coverage analysis or manual identification from context package)
- Current pass count and fail count for the full test suite
- Coverage percentage for the refactoring scope (lines, branches, or methods — whichever is available)
- Assessment of whether existing tests are sufficient to prove behavioural equivalence: sufficient if coverage of the scope is >= 80% branch coverage AND all known public behaviour has at least one test

Measure the current quality metric value (e.g., run complexity analysis tool; record the current metric baseline value).

Write `equivalence-baseline.md` with: test inventory, pass/fail counts, coverage metrics, quality metric baseline, and a sufficiency assessment (`sufficient` | `insufficient — coverage below threshold` | `insufficient — uncovered public behaviour`).

---

### Step 4 — Equivalence Baseline Review (Checker)

**Agent:** `EQUIV-CHECKER` (Checker)
**Inputs:** `docs/engineering/refactoring/equivalence-baseline.md`, context package
**Outputs:** `docs/engineering/refactoring/equivalence-checker-report.md`

`EQUIV-CHECKER` independently reviews the equivalence baseline:
1. Confirms that the test inventory covers the declared scope (spot-checks at least 20% of scope files).
2. Confirms that pass/fail counts are consistent with an independently readable test suite.
3. Confirms the coverage metric calculation is consistent with the coverage data cited.
4. Confirms the sufficiency assessment is correct: if coverage is below 80% of the scope or any declared public behaviour is uncovered, produces a GATE-2 recommendation.
5. Confirms the quality metric baseline is accurate and measurable.

Write `equivalence-checker-report.md` with: pass/fail per criterion, coverage confirmation, sufficiency confirmation or GATE-2 recommendation, and overall finding (`accepted` or `rejected`).

If `rejected`, EQUIV-AGENT performs one retry of Step 3. If second attempt also produces `rejected`, GATE-1 fires unconditionally.

---

### [GATE-2 — Soft Gate: Insufficient Equivalence Coverage]

Fires when EQUIV-CHECKER determines that existing test coverage is insufficient to prove behavioural equivalence after refactoring. The loop notifies the reviewer that equivalence cannot be fully proven and that the refactoring may proceed with a reduced scope (excluding the areas with insufficient coverage) or may be paused until LOOP-103 improves coverage. See `## Human Approval Gates` — GATE-2.

---

### Step 5 — [GATE-1 — Hard Gate: Scope and Equivalence Strategy Approval]

The loop halts. No source code modification occurs until human approval is recorded. The human reviewer examines the scope analysis, equivalence baseline, and checker report. See `## Human Approval Gates` — GATE-1.

---

### Step 6 — Architecture Freshness Validation

**Agent:** `SCOPE-AGENT`
**Inputs:** GATE-1 approval record, LOOP-001 outputs
**Outputs:** Validation record in `STATUS-102.md`

Confirm LOOP-001 outputs are still within the 7-day freshness threshold after GATE-1 wait time. If GATE-1 wait exceeded 7 days since the last LOOP-001 run, this is a Soft Gate condition — record the staleness in STATUS and proceed with reduced confidence.

---

### Step 7 — Refactoring Plan

**Agent:** `PLAN-AGENT`
**Inputs:** Approved scope analysis, GATE-1 approval record, context package
**Outputs:** Execution plan at `docs/planning/execution-plan.md` (produced by LOOP-004)

Invoke LOOP-004 with the following capability-specific constraints:
- **Mandatory:** Each plan step must be independently verifiable (the test suite must pass after each step, not only after the final step).
- **Prohibited:** Any step that changes externally observable behaviour (API return values, event payloads, exception types thrown to callers).
- **Prohibited:** Any step that modifies files outside the declared scope boundary.
- **Mandatory:** The plan must declare the quality metric target and how it will be measured at plan completion.
- **Ordering rule:** Scope-narrowing refactoring (extract, split, rename within the scope) before scope-combining refactoring (merge, inline) to reduce conflict risk.

---

### Step 8 — Implementation

**Agent:** `IMPL-AGENT`
**Inputs:** Approved execution plan from Step 7, context package, equivalence baseline
**Outputs:** Refactoring implementation (produced by LOOP-005)

Invoke LOOP-005 with the approved execution plan. Monitor via `STATUS-005.md`. LOOP-005 must apply each step atomically and verify the test suite passes after each step before proceeding to the next (intermediate equivalence checkpoints).

If LOOP-005 detects a step that would require modifying a file outside the declared scope boundary, it must halt and trigger GATE-1 in this loop rather than proceeding. If LOOP-005 discovers a defect (a test fails that was passing before the refactoring, attributed to a pre-existing bug, not introduced by the refactoring), it must halt and create a new LOOP-101 task record; the refactoring continues only after that defect is recorded.

---

### Step 9 — Verification

**Agent:** `VERIF-AGENT`
**Inputs:** LOOP-005 outputs, equivalence baseline, scope analysis
**Outputs:** Verification report at `docs/verification/verification-report.md` (produced by LOOP-006)

Invoke LOOP-006 with the capability-specific verification profile:
- **Mandatory:** All tests that were passing in the equivalence baseline still pass.
- **Mandatory:** No new test failures have been introduced.
- **Mandatory:** The quality metric has improved (current value is closer to target than baseline value).
- **Mandatory:** No files outside the declared scope boundary were modified.
- **Mandatory:** No public API contract was changed (API contract comparison against baseline).
- **Required category:** Behavioural equivalence verification.

---

### Step 10 — Status, Skill, and Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All run artifacts, all gate outcomes, LOOP-006 verification report
**Outputs:** Updated `STATUS-102.md`, `SKILL-102.md`, `engineering-summary.md`, `METADATA-102-{run-id}.md`, `REFLECTION-102-{run-id}.md`

Record all metrics. Update SKILL with quality metric improvement observed, scope patterns that required GATE-2, coverage thresholds that proved difficult in this technology stack. Produce Reflection before marking run closed.

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `scope-analysis.md` exists and declares: scope boundary, quality metric, structural problems, behaviour invariants, and architectural constraints | Read file; assert all fields non-empty |
| VER-2 | `equivalence-baseline.md` exists and records: test inventory, pass/fail counts, coverage metrics, quality metric baseline, and sufficiency assessment | Read file; assert all fields non-empty |
| VER-3 | `equivalence-checker-report.md` exists with overall finding `accepted` | Read file; assert `overall_finding: accepted` |
| VER-4 | LOOP-006 verification report confirms all baseline tests still pass and no new failures introduced | Read LOOP-006 report; assert baseline test pass count maintained and new failure count is zero |
| VER-5 | LOOP-006 verification report confirms the quality metric improved | Read LOOP-006 report; assert metric delta is in the improvement direction |
| VER-6 | No files outside the declared scope boundary were modified | Compare LOOP-005 change log against declared scope; assert no out-of-scope files appear |
| VER-7 | `STATUS-102.md` updated with current run ID and final status within 5 minutes of run end | Read STATUS file; assert run ID, status, and timestamp within tolerance |
| VER-8 | The Reflection artifact exists and is non-empty | File existence and non-emptiness check |
| VER-9 | GATE-1 approval recorded in `STATUS-102.md` with approver identity, timestamp, and decision | Read STATUS file; assert `gate_outcomes.GATE-1` populated |
| VER-10 | Engineering Metadata artifact exists with all required provenance fields | Read metadata file; assert all required fields non-empty |

---

## Reflection

At the end of every run, produce a Reflection at `docs/engineering/refactoring/reflections/REFLECTION-102-{run-id}.md`.

The Reflection must contain all ten LOOP-STANDARD sections plus:

- **Equivalence Evidence Quality:** Coverage percentage achieved for the scope; whether the checker accepted on first attempt; whether GATE-2 fired.
- **Quality Metric Delta:** Baseline value versus post-refactoring value; percentage improvement; whether the target was reached.
- **Scope Discipline:** Whether any files outside the declared boundary were touched; whether any behaviour invariants were at risk during implementation.
- **Intermediate Checkpoint Outcomes:** How many LOOP-005 intermediate checkpoints passed without issue; whether any step required rollback.
- **Pattern Observation:** Recurring structural problems in the affected module; recommended priority for future LOOP-103 runs to improve equivalence coverage.

---

## Human Approval Gates

### GATE-1 — Hard Gate: Scope and Equivalence Strategy Approval

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 4 (Equivalence Baseline Checker Review), before Step 7 (Refactoring Plan) |
| Artifact Under Review | Scope Analysis, Equivalence Baseline, and Equivalence Checker Report |
| Approver | Principal Engineer or Architecture Owner for the affected module |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; no source code modified; all analysis artifacts preserved |
| Audit Trail | Written to `STATUS-102.md` under `gate_outcomes.GATE-1`; approver identity, timestamp, decision, any scope adjustments |

**Fires when:**
- Normal workflow completion of Step 4 (every run reaches GATE-1 before source modification)
- Equivalence Checker rejected on both initial and retry attempts
- Context HEAD SHA mismatch detected

**Reviewer guidance:** Confirm the declared scope is bounded correctly. Confirm the equivalence baseline is adequate — if coverage is below 80% and GATE-2 auto-proceeded, explicitly confirm whether reduced scope or full scope with acknowledged risk is acceptable. Confirm the quality metric is measurable and the improvement target is reasonable.

---

### GATE-2 — Soft Gate: Insufficient Equivalence Coverage

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 4, before GATE-1 |
| Artifact Under Review | Equivalence Checker Report with coverage insufficiency finding |
| Approver | Principal Engineer or Architecture Owner |
| Notification Channel | Declared in `.loop-102.yml`; defaults to draft comment on task record |
| Timeout | 48 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds with reduced scope (excluding areas below 80% coverage); `soft_gate_auto_proceeded: true` recorded with excluded areas noted |
| Audit Trail | Notification timestamp, coverage data, excluded areas, outcome recorded under `gate_outcomes.GATE-2` |

**Fires when:** EQUIV-CHECKER finds coverage below 80% of the scope or uncovered declared public behaviour.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-102.md`. The executing agent reads this at each step boundary and halts immediately. On emergency stop: LOOP-005 applies its own step rollback; a partial Reflection is produced.

---

## Failure Recovery

### FR-1 — Equivalence Checker Rejects Baseline (Both Attempts)

**Detection:** `EQUIV-CHECKER` returns `overall_finding: rejected` on both attempts.
**Immediate Action:** Record both rejected checker reports. Flag `checker_rejected_max_retries = true`.
**Recovery:** Trigger GATE-1 unconditionally with both reports attached for human review.
**Rollback:** No source code modified.

### FR-2 — LOOP-005 Detects Out-of-Scope Modification Required

**Detection:** LOOP-005 determines a required implementation step would modify a file outside the declared scope boundary.
**Immediate Action:** LOOP-005 halts that step; reports to this loop via STATUS-005.
**Recovery:** This loop triggers GATE-1. Human decides: expand the scope (update scope analysis and re-approve), skip the problematic refactoring step, or abandon the run.
**Rollback:** LOOP-005 applies rollback for the current step only; prior completed steps are preserved.

### FR-3 — LOOP-005 Discovers a Pre-existing Defect

**Detection:** A test that was passing in the equivalence baseline now fails during a LOOP-005 intermediate checkpoint, and the failure is attributable to a pre-existing bug (not introduced by this refactoring step).
**Immediate Action:** LOOP-005 halts. This loop creates a new LOOP-101 task record for the discovered defect.
**Recovery:** The refactoring pauses. Human decides whether to: (a) fix the defect first (run LOOP-101) and then resume refactoring, (b) exclude the affected area from the refactoring scope, or (c) abandon this run.
**Rollback:** LOOP-005 rolls back to the last passing intermediate checkpoint.

### FR-4 — LOOP-006 Produces Requires Rework (Behaviour Changed)

**Detection:** LOOP-006 reports that at least one baseline test now fails, indicating a behaviour change was introduced.
**Immediate Action:** Record the rework specification.
**Recovery:** Determine which refactoring step introduced the behaviour change (using LOOP-005's intermediate checkpoints). Re-invoke LOOP-005 to roll back to the last equivalence-passing checkpoint. Re-plan from that point excluding the problematic step. Maximum two rework cycles.
**Rollback:** LOOP-005 rolls back to the identified checkpoint.

### FR-5 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 24 hours.
**Immediate Action:** Complete current atomic step; do not begin next step.
**Recovery:** Write all output artifacts produced so far. Write `STATUS-102.md` with `status: stopped`, `reason: max_duration_exceeded`. Produce partial Reflection.
**Rollback:** Not required; partial outputs are valid for resumption.

---

## Metrics

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 10) |
| `run.steps_total` | 10 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that auto-proceeded |
| `verification.level1.pass` | VER-1 through VER-10 criteria passed |
| `verification.level1.fail` | VER-1 through VER-10 criteria failed |
| `reflection.produced` | Boolean |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `refactoring.quality_metric_type` | The metric being improved (complexity, duplication, etc.) |
| `refactoring.quality_metric_baseline` | Measured value before refactoring |
| `refactoring.quality_metric_final` | Measured value after refactoring |
| `refactoring.quality_metric_delta_pct` | Percentage improvement |
| `refactoring.scope_files_declared` | Count of files in declared scope |
| `refactoring.scope_files_modified` | Count of files actually modified |
| `refactoring.coverage_pct_baseline` | Branch coverage percentage at baseline |
| `refactoring.gate2_fired` | Boolean — did GATE-2 fire for insufficient coverage |
| `refactoring.scope_reduced` | Boolean — was scope reduced due to GATE-2 auto-proceed |
| `refactoring.intermediate_checkpoints_passed` | Count of LOOP-005 intermediate checkpoints that passed |
| `refactoring.rework_cycles` | Count of LOOP-006 Requires Rework outcomes |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The refactoring expands beyond the declared scope boundary, incorporating adjacent code that was not approved.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** The structural improvement requires changes to callers or consumers of the refactored component.
- **Control:** Strict scope boundary declared in Step 1; LOOP-004 constraints prohibit out-of-scope changes; VER-6 detects out-of-scope modifications.
- **Detection:** VER-6 failure; LOOP-005 change log contains undeclared files.
- **Response:** FR-2 procedure; GATE-1 re-fires for scope expansion decision.

### RISK-2 — Architectural Drift

- **Description:** The refactoring reorganises code in a way that violates an accepted ADR or introduces a dependency pattern not compliant with the module architecture.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** The refactoring moves code between packages or introduces new import relationships.
- **Control:** Architectural constraints declared in scope analysis; LOOP-006 architecture compliance check.
- **Detection:** LOOP-006 architectural compliance failure.
- **Response:** LOOP-006 Requires Rework; rework specification identifies the violated constraint.

### RISK-3 — Hidden Dependencies

- **Description:** Callers not covered by the test suite depend on implementation details that change during refactoring.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Test coverage is below 100% of the scope's public surface; some callers are not tested.
- **Control:** Equivalence baseline identifies coverage gaps; GATE-2 fires for coverage below 80%; remaining gap is documented and acknowledged at GATE-1.
- **Detection:** Defect escapes detected in subsequent LOOP-101 runs traced to this refactoring.
- **Response:** SKILL-102 records the gap; future LOOP-103 runs for this module are prioritised.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop operates on repository source files only. It does not access tenant-scoped runtime data. Source modifications are evaluated for behavioural equivalence, not for tenant isolation impact (that is a concern for the subsequent Release Loop).
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** Not applicable to this loop's own operations. Refactoring changes are source-only; data access behaviour is verified by LOOP-006 equivalence checks. If a refactoring inadvertently changes data handling behaviour, LOOP-006 detects the change via test failures.
- **Likelihood:** N/A as a direct risk of this loop
- **Impact:** N/A

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. Source code modifications via LOOP-005 are idempotent. This loop does not write to any external runtime system.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** A refactoring of authentication or authorisation logic could inadvertently weaken a security control while maintaining the same test behaviour.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** The declared scope includes authentication, authorisation, tenant isolation, or cryptographic code.
- **Control:** Security-sensitive scope triggers mandatory Security Lead review at GATE-1 per ENGINEERING-LOOP-GUIDE common gate conditions. LOOP-006 verification profile includes security posture check.
- **Detection:** Security-sensitive file paths identified in scope analysis flag mandatory Security Lead review.
- **Response:** Security Lead review cannot be auto-proceeded for any refactoring touching security-sensitive scope.

### RISK-8 — Runaway Execution

- **Description:** A large refactoring scope could cause the implementation phase to exceed the 24-hour maximum run duration.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Declared scope encompasses a very large number of files or a deeply entangled component.
- **Control:** Maximum run duration enforced by FR-5. Scope analysis (Step 1) should estimate implementation size; human may split a large refactoring into multiple LOOP-102 runs at GATE-1.
- **Detection:** Wall-clock elapsed time check at each step boundary.
- **Response:** FR-5 procedure; partial outputs preserved; run marked `stopped`.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All verification criteria VER-1 through VER-10 passed |
| SC-2 | LOOP-006 produced Accepted outcome |
| SC-3 | GATE-1 approval recorded with approver identity and timestamp |
| SC-4 | All artifacts in Outputs table written |
| SC-5 | `STATUS-102.md` updated with all metrics and `completed` status |
| SC-6 | `SKILL-102.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (24 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | PRE-3 concurrent run detected |
| SC-11 | Emergency Stop received |
| SC-12 | LOOP-006 Requires Rework and maximum rework cycles (2) exceeded |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Analysis Artifacts:**
- [ ] `docs/engineering/refactoring/scope-analysis.md` written with all required fields
- [ ] `docs/engineering/refactoring/equivalence-baseline.md` written with test inventory, coverage metrics, quality metric baseline, and sufficiency assessment
- [ ] `docs/engineering/refactoring/equivalence-checker-report.md` written with overall finding `accepted`

**Implementation Artifacts (via LOOP-005):**
- [ ] Refactoring implemented within declared scope boundary
- [ ] All LOOP-005 intermediate checkpoints passed (test suite passed after each step)

**Verification Artifacts (via LOOP-006):**
- [ ] All baseline tests still pass
- [ ] Quality metric has improved
- [ ] No out-of-scope files modified

**Gates:**
- [ ] GATE-1 approval recorded with approver identity, timestamp, and decision
- [ ] GATE-2 outcome recorded if fired

**State:**
- [ ] `docs/loops/engineering/STATUS-102.md` updated with all metrics and final status
- [ ] `docs/loops/engineering/SKILL-102.md` updated

**Metadata and Reflection:**
- [ ] `docs/engineering/refactoring/metadata/METADATA-102-{run-id}.md` produced
- [ ] `docs/engineering/refactoring/engineering-summary.md` produced
- [ ] `docs/engineering/refactoring/reflections/REFLECTION-102-{run-id}.md` produced with all required sections

---

## Future Improvements

- **Incremental scope splitting:** Provide a tool that takes a large declared scope and proposes a sequence of smaller LOOP-102 sub-runs, each independently verifiable, to reduce the risk of a single large refactoring run exceeding its time budget or introducing accumulated equivalence risk.
- **Automated behaviour invariant extraction:** Derive behaviour invariants from the test suite rather than requiring the scope analysis to declare them manually — reducing the risk that an undeclared invariant is inadvertently changed.
- **Refactoring catalogue integration:** Maintain a catalogue of approved refactoring patterns (extract method, replace conditional with polymorphism, etc.) in SKILL-102, and allow the Refactoring Plan step to select from the catalogue rather than generating a new pattern each time.
- **Coverage pre-computation:** Before GATE-1, provide a coverage estimate for the scope to allow the reviewer to decide whether to proceed with LOOP-103 first or accept the current coverage level with reduced confidence.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract requirements
- `docs/loops/shared/SPEC-011-REVIEW-PROCESS.md` — review process for loop activation
- `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared Engineering Loop context
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate definitions
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage conventions

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-102 as the governed refactoring loop for the AEOS Engineering Loop series.

