---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/LOOP-101-Bug-Fixing.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Standard loop for debugging and fixing repository issues.
Generalized Purpose: Standard loop for debugging and fixing repository issues.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-002 — Context Assembly, LOOP-004 — Planning, LOOP-005 — Implementation, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-101 — Bug Fixing

**Loop ID:** LOOP-101
**Name:** Bug Fixing
**Version:** 1.0
**Status:** Active
**Category:** Engineering
**Depends On:** LOOP-002 — Context Assembly, LOOP-004 — Planning, LOOP-005 — Implementation, LOOP-006 — Verification
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---

## Purpose

LOOP-101 executes a governed, auditable bug-fix process from defect intake through verified fix delivery. It accepts a confirmed bug report as input, performs systematic root cause analysis, produces a minimal targeted fix via LOOP-005, verifies fix correctness and regression coverage via LOOP-006, and delivers a signed-off patch together with a mandatory regression test. The loop enforces that every fix is grounded in a verified root cause and that no fix is merged without human approval of both the root cause and the proposed scope.

---

## Problem Statement

Bug fixes applied without confirmed root cause analysis frequently address symptoms while leaving the underlying defect intact, leading to recurring failures. Fixes that expand beyond the minimum necessary scope introduce unintended regressions. Bug fixes without mandatory regression tests allow the same defect to re-emerge undetected after future changes. Without a governed fix process, the traceability chain from bug report to merged patch is incomplete, making post-incident review difficult and knowledge accumulation across incidents impossible.

---

## Why This Loop Exists

Bug fixing is the highest-frequency engineering activity in most repositories and the one most prone to scope creep, incomplete verification, and knowledge loss. Codifying it as a loop ensures: repeatability (every bug fix follows the same evidence-based process regardless of severity), verifiability (root cause analysis and fix scope are independently reviewed before any code is written), auditability (every fix carries a complete provenance chain from bug report to merged patch), and safety (no source code is modified until a human has approved the root cause and scope). The GATE-2 mechanism for ambiguous root causes prevents engineers and agents from proceeding with an uncertain fix — the most common source of fix-then-fix cycles.

---

## Scope

**In scope:**
- Receiving a confirmed bug report with a reproducible symptom
- Classifying the bug by severity (P0 through P3)
- Performing root cause analysis scoped to the affected module(s)
- Assembling context via LOOP-002
- Producing a minimal fix plan via LOOP-004 (fix only the defect; no opportunistic improvements)
- Implementing the fix and a mandatory regression test via LOOP-005
- Verifying fix correctness and regression coverage via LOOP-006
- Delivering a complete engineering artefact set with merged patch

**Out of scope:**
- Refactoring or cleanup in the same change set (that is LOOP-102)
- Feature additions prompted by the bug investigation
- Bugs that are not reproducible or not confirmed
- Performance or scalability issues without a functional defect (those are LOOP-160 series)
- Security vulnerabilities requiring specialised handling (those are LOOP-170 series)

**Maximum run duration:** 8 hours total (2 hours for intake and analysis phases, 6 hours for planning, implementation, and verification). If this duration is exceeded, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Bug report task record | File (`docs/tasks/task-catalog.md` entry with `primary_category: BUG`) | LOOP-003 — Task Discovery | Required |
| Bug description and reproduction steps | Field within task record | Bug reporter or LOOP-003 | Required |
| Severity classification | Field within task record (`P0` \| `P1` \| `P2` \| `P3`) | Bug reporter; inferred by INTAKE-AGENT if absent | Required |
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 — Architecture Discovery | Required — must be no more than 7 days old |
| LOOP-002 context package | Directory (`docs/context/`) | LOOP-002 — Context Assembly | Required — produced during this run |
| Repository source files for affected modules | Repository State | Filesystem | Required |
| Prior root cause records for related bugs | File (`docs/engineering/bug-fixing/`) | Prior LOOP-101 runs | Optional — used to detect recurring defect patterns |
| Engineering Loop configuration | File (`.loop-101.yml` at repo root) | Repository | Optional |

### Input Validation

Before any step begins, the loop must verify:
- The task record exists, has `primary_category: BUG`, and includes at minimum one reproduction step or a symptom description.
- LOOP-001 outputs exist at `docs/architecture/` and their recorded timestamp is within 7 days.
- No other instance of LOOP-101 is running for the same task ID (checked via `STATUS-101.md`).
- The repository is in a readable state with a valid git HEAD.
- HEAD SHA is recorded at validation time for drift detection in later steps.

If any required input fails validation, the loop halts with `precondition_failed` and writes no output artifacts.

---

## Outputs

All primary outputs are written to `docs/engineering/bug-fixing/`. Core Loop outputs (from LOOP-004, LOOP-005, LOOP-006) are written to their own directories per those loops' specifications.

| Artifact | Path | Description |
|----------|------|-------------|
| Bug Classification | `docs/engineering/bug-fixing/bug-classification.md` | Severity, affected modules, reproduction steps, and initial impact scope; consumed by ANALYSIS-AGENT |
| Root Cause Analysis | `docs/engineering/bug-fixing/root-cause-analysis.md` | Root cause statement, mechanism, evidence, defect location, scope, and regression risk; reviewed at GATE-1 |
| Checker Report | `docs/engineering/bug-fixing/checker-report.md` | ANALYSIS-CHECKER's independent verification of the root cause analysis |
| Fix Plan | `docs/planning/execution-plan.md` | Produced by LOOP-004: the minimal set of changes addressing the root cause |
| Verification Report | `docs/verification/verification-report.md` | Produced by LOOP-006: confirmation that bug is no longer reproducible and regression test passes |
| Engineering Summary | `docs/engineering/bug-fixing/engineering-summary.md` | Complete run summary: task, orchestration sequence, outcome, top lessons, open items |
| Loop Status | `docs/loops/engineering/STATUS-101.md` | Run status, metrics, and open blockers for this loop |
| Loop Skill | `docs/loops/engineering/SKILL-101.md` | Calibration observations accumulated across runs |
| Run Metadata | `docs/engineering/bug-fixing/metadata/METADATA-101-{run-id}.md` | Provenance: task ID, all Core Loop run IDs, HEAD SHA at start and end, elapsed duration, outcome |
| Reflection | `docs/engineering/bug-fixing/reflections/REFLECTION-101-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-002 — Context Assembly:** Context package (source files, test files, architecture context) scoped to affected modules. Required — invoked during this run.
- **LOOP-004 — Planning:** Execution plan for the minimal fix and regression test. Required — invoked during this run.
- **LOOP-005 — Implementation:** Implementation of the fix and regression test per approved plan. Required — invoked during this run.
- **LOOP-006 — Verification:** Verification that the bug is no longer reproducible, regression test passes, and no existing tests regress. Required — invoked during this run.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer explicitly selects a `BUG`-classified task from the LOOP-003 backlog and triggers LOOP-101.
2. **P0/P1 escalation signal** — An incident management system emits a P0 or P1 signal that has been mapped to a task in the LOOP-003 catalog.
3. **Automated task dispatch** — The AEOS task dispatcher selects a `BUG`-classified task with priority rank within the configured dispatch window and routes it to LOOP-101 per SPEC-010 routing rules.

The trigger source, task ID, and current HEAD SHA must be recorded in `STATUS-101.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | A `BUG`-classified task exists in the LOOP-003 task catalog with the selected task ID | Read `docs/tasks/task-catalog.md`; assert entry with matching task ID and `primary_category: BUG` |
| PRE-2 | LOOP-001 outputs exist and are no more than 7 days old | Read `docs/loops/core/STATUS-001.md`; assert `last_completed_run` timestamp within 7 days |
| PRE-3 | No other LOOP-101 instance is running for the same task ID | Read `docs/loops/engineering/STATUS-101.md`; assert `current_status` is not `running` for this task ID |
| PRE-4 | The repository has at least one commit and a readable HEAD | `git log -1` exits successfully |
| PRE-5 | Write access to `docs/engineering/bug-fixing/` | Probe write to directory; remove on success |
| PRE-6 | The bug report contains at minimum a symptom description or one reproduction step | Read task record; assert non-empty description field |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem (source) | Read | Affected module files identified by root cause analysis | Filesystem permissions of executing agent | Scoped to declared affected modules only | No writes to source in this loop's own steps; LOOP-005 manages rollback for its writes | Yes |
| `docs/engineering/bug-fixing/` | Write | All files listed in Outputs table | Same as executing agent | Confined to this directory | `git checkout docs/engineering/bug-fixing/` restores prior state | Yes — re-running produces equivalent analysis given same inputs |
| `docs/loops/engineering/STATUS-101.md` | Read-Write | Single file | Same as executing agent | Single file; no cross-loop state | `git checkout docs/loops/engineering/STATUS-101.md` | Yes |
| `docs/loops/engineering/SKILL-101.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/SKILL-101.md` | Yes |
| LOOP-002, LOOP-004, LOOP-005, LOOP-006 STATUS files | Read | STATUS files for each invoked Core Loop | Same as executing agent | Read-only monitoring only | N/A | Yes |
| Git history | Read | Current branch log | Filesystem permissions | Read-only; no commits made by this loop directly | N/A | Yes |

This loop does not write to any external system outside the repository. Source file modifications are delegated exclusively to LOOP-005.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/engineering/LOOP-101-Bug-Fixing.md` — this document
3. `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared Engineering Loop context
4. `docs/loops/engineering/STATUS-101.md` — prior run state (if it exists)
5. `docs/loops/core/SKILL-001.md` — repository profile: test commands, build commands, module layout
6. `docs/architecture/module-catalog.md` — module boundaries
7. `docs/architecture/technology-stack.md` — technology context
8. The selected task record from `docs/tasks/task-catalog.md`
9. Output of `git log --oneline -10` — recent commit context

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `INTAKE-AGENT` | Maker | Step 1: loads and classifies the bug report; confirms severity; verifies reproducibility preconditions | Filesystem read, task catalog read | Reports to none; output consumed by ANALYSIS-AGENT |
| `CONTEXT-AGENT` | Maker | Step 2: invokes LOOP-002 scoped to affected modules; validates context package HEAD SHA | LOOP-002 invocation, STATUS file read | None |
| `ANALYSIS-AGENT` | Maker | Step 3: performs root cause analysis; produces Root Cause Analysis; triggers GATE-2 if multiple candidate causes exist | Filesystem read, source file analysis | Reports to GATE-2; results reviewed at GATE-1 |
| `ANALYSIS-CHECKER` | Checker | Step 4: independently verifies root cause by confirming evidence and attempting reproduction using only the Root Cause Analysis; produces Checker Report | Filesystem read, independent evidence check | Independent of ANALYSIS-AGENT; finding reviewed at GATE-1 |
| `PLAN-AGENT` | Maker | Step 6: invokes LOOP-004 with minimal-fix constraints and scope bound from GATE-1 approval | LOOP-004 invocation | None — plan scope approved at GATE-1 before this step |
| `IMPL-AGENT` | Maker | Step 7: invokes LOOP-005 to implement fix and regression test | LOOP-005 invocation | Governed by LOOP-005's own gates |
| `VERIF-AGENT` | Maker | Step 8: invokes LOOP-006 with capability-specific verification profile | LOOP-006 invocation | Governed by LOOP-006's own verification |
| `STATUS-WRITER` | Maker | Steps 9–10: updates STATUS-101.md, SKILL-101.md; produces Engineering Summary, Metadata, and Reflection | Filesystem write | None — executes after all gates cleared |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1 and GATE-2: reviews root cause analysis and approves fix scope before any source modification | Human judgment | Sole authority for gate decisions |

`ANALYSIS-AGENT` and `ANALYSIS-CHECKER` must be separate agent instances. No agent may act as both Maker and Checker for the same artifact.

---

## Workflow

### Step 1 — Intake and Bug Classification

**Agent:** `INTAKE-AGENT`
**Inputs:** Task record from LOOP-003 task catalog
**Outputs:** `docs/engineering/bug-fixing/bug-classification.md`

Load the bug report from the task catalog. Extract: symptom description, reported reproduction steps, affected modules (if declared by reporter), severity (P0=data loss or security breach, P1=service down or unavailable, P2=functionality degraded, P3=cosmetic or minor). If severity is absent from the task record, infer from symptom description and record the inferred severity with rationale. Verify the bug is described with enough specificity to attempt reproduction. If the symptom is described only at an entirely generic level with no actionable context, halt with `precondition_failed`.

Write `bug-classification.md` containing: task ID, severity, symptom statement, reported reproduction steps, initially suspected affected modules, and a note on whether reproduction can be attempted from the task record alone.

Update `STATUS-101.md` with `current_status: running`, current task ID, and step 1 in progress.

---

### Step 2 — Context Assembly

**Agent:** `CONTEXT-AGENT`
**Inputs:** Bug classification from Step 1, LOOP-001 architecture outputs
**Outputs:** Context package at `docs/context/` (produced by LOOP-002)

Invoke LOOP-002, parameterised with:
- The affected modules from Step 1's classification
- Context tier requirements: Tier 1 Required — source files for affected modules; Tier 1 Required — test files for affected modules; Tier 2 Required — inter-module dependency context; Tier 3 Optional — adjacent modules that share a boundary with affected modules

Wait for LOOP-002 to complete. Validate: the context package HEAD SHA matches the current repository HEAD SHA. If they differ, trigger GATE-1 — the repository changed during context assembly.

---

### Step 3 — Root Cause Analysis

**Agent:** `ANALYSIS-AGENT` (Maker)
**Inputs:** Context package from Step 2, bug classification from Step 1
**Outputs:** `docs/engineering/bug-fixing/root-cause-analysis.md`

Examine the source files in the context package to identify the specific code path that produces the reported symptom. The analysis must produce:

- **Root cause statement:** The specific, falsifiable condition in the code that causes the bug.
- **Mechanism description:** The causal path from the root cause to the observable symptom.
- **Evidence:** The specific code location (file path, line range) and the specific condition that constitutes the defect.
- **Defect location:** The precise file(s) and line range(s) that must be modified to correct the root cause.
- **Scope of impact:** Which modules and behaviours are affected by this defect.
- **Regression risk:** Adjacent code paths or behaviours that could be affected by a fix.

If the analysis identifies multiple candidate root causes — conditions that each independently could explain the symptom — the ANALYSIS-AGENT must record all candidates with supporting evidence and trigger GATE-2 before any single cause is selected for a fix.

Write `root-cause-analysis.md` with all required fields. Mark confidence level: High (single clear root cause with direct evidence), Medium (root cause identified with indirect evidence), Low (root cause is a candidate from multiple possibilities).

---

### Step 4 — Root Cause Verification (Checker)

**Agent:** `ANALYSIS-CHECKER` (Checker)
**Inputs:** `docs/engineering/bug-fixing/root-cause-analysis.md`, context package from Step 2
**Outputs:** `docs/engineering/bug-fixing/checker-report.md`

`ANALYSIS-CHECKER` independently verifies:
1. The cited evidence (file path, line range) exists and contains the described condition.
2. The mechanism description is logically consistent with the cited evidence.
3. The defect location is consistent with the root cause statement.
4. If multiple candidate causes are listed, each has independent evidence cited.
5. The scope of impact is consistent with module boundaries in the architecture context.

The Checker produces `checker-report.md` with: pass/fail per criterion, discrepancy list, and an overall finding of `accepted` or `rejected`. If `rejected`, ANALYSIS-AGENT performs one retry of Step 3. If the second attempt also produces `rejected`, GATE-1 fires unconditionally.

---

### [GATE-2 — Soft Gate: Ambiguous Root Cause]

Fires when ANALYSIS-AGENT identifies multiple candidate root causes in Step 3. The loop notifies the reviewer and awaits human selection of the candidate to fix. See `## Human Approval Gates` — GATE-2.

---

### Step 5 — [GATE-1 — Hard Gate: Root Cause Approval Before Source Modification]

The loop halts. No source code modification occurs until human approval is recorded. The human reviewer examines the Root Cause Analysis and the Checker Report. See `## Human Approval Gates` — GATE-1.

---

### Step 6 — Fix Planning

**Agent:** `PLAN-AGENT`
**Inputs:** Approved root cause analysis, GATE-1 approval record, context package
**Outputs:** Execution plan at `docs/planning/execution-plan.md` (produced by LOOP-004)

Invoke LOOP-004 with the following capability-specific constraints:
- **Mandatory:** A regression test step must be included covering the exact bug scenario.
- **Mandatory:** The regression test step must appear in the plan before the fix implementation step.
- **Prohibited:** Any refactoring or cleanup step not directly required to implement the fix.
- **Prohibited:** Changes to files not cited in the root cause analysis defect location, unless the GATE-1 approval explicitly expanded the scope.
- **Scope bound:** The fix addresses only the defect in the approved root cause; any additional defects discovered during implementation are reported as new task records, not fixed in this run.

If LOOP-004 cannot produce a plan satisfying these constraints, re-escalate to GATE-1.

---

### Step 7 — Implementation

**Agent:** `IMPL-AGENT`
**Inputs:** Approved execution plan from Step 6, context package
**Outputs:** Fix implementation and regression test (produced by LOOP-005)

Invoke LOOP-005 with the approved execution plan. Monitor LOOP-005 execution by reading `STATUS-005.md`. If LOOP-005 triggers Emergency Stop or produces `failed` status, propagate the failure to this loop and halt.

The implementation must produce: the minimal fix at the identified defect location, and a regression test that (a) would fail on the unfixed codebase, (b) passes on the fixed codebase, and (c) follows repository test naming conventions from `SKILL-001.md`.

---

### Step 8 — Verification

**Agent:** `VERIF-AGENT`
**Inputs:** LOOP-005 implementation outputs, root cause analysis, fix plan
**Outputs:** Verification report at `docs/verification/verification-report.md` (produced by LOOP-006)

Invoke LOOP-006 with the capability-specific verification profile:
- **Mandatory:** The reported bug scenario is no longer reproducible.
- **Mandatory:** The regression test passes.
- **Mandatory:** All tests that passed before the fix still pass (no regressions introduced).
- **Mandatory:** The fix is confined to the files declared in the execution plan.
- **Required category:** Test Coverage — regression test covers the code path cited in root cause analysis.

If LOOP-006 produces Requires Rework, determine whether re-planning (LOOP-004) or direct rework (LOOP-005) is needed. Maximum two rework cycles.

---

### Step 9 — Status and Skill Update

**Agent:** `STATUS-WRITER`
**Inputs:** All run artifacts, all gate outcomes, LOOP-006 verification report
**Outputs:** Updated `STATUS-101.md`, updated `SKILL-101.md`, `engineering-summary.md`, `METADATA-101-{run-id}.md`

Record all metrics in `STATUS-101.md`. Record gate outcomes. Update `SKILL-101.md` with calibration observations. Produce Engineering Summary and Engineering Metadata.

---

### Step 10 — Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** Complete run artifact set
**Outputs:** `docs/engineering/bug-fixing/reflections/REFLECTION-101-{run-id}.md`

Produce the Reflection artifact before marking the run closed. Update `STATUS-101.md` to final status.

---

## Verification

All postconditions must be true before the run is marked `completed`.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `root-cause-analysis.md` exists, is non-empty, and contains all required fields (root cause statement, mechanism, evidence, defect location, scope, regression risk) | Read file; assert presence of each required field with non-empty content |
| VER-2 | `checker-report.md` exists and records an overall finding of `accepted` | Read file; assert `overall_finding: accepted` |
| VER-3 | No source files were modified by this loop's own steps (all source modifications delegated to LOOP-005) | Confirm no source file writes occurred outside of LOOP-005's declared change log |
| VER-4 | `STATUS-101.md` has been updated with the current run ID, final status, and a timestamp within 5 minutes of run end | Read STATUS file; assert run ID, status, and timestamp within tolerance |
| VER-5 | The Reflection artifact exists at the declared path and is non-empty | File existence and non-emptiness check |
| VER-6 | LOOP-006 verification report records: bug scenario no longer reproducible, regression test passes, no pre-existing tests now fail | Read LOOP-006 verification report; assert all three outcomes present with `pass` status |
| VER-7 | GATE-1 approval is recorded in `STATUS-101.md` with approver identity, timestamp, and decision | Read STATUS file; assert `gate_outcomes.GATE-1` populated with all required fields |
| VER-8 | The fix is confined to the files declared in the LOOP-004 execution plan (no undeclared files modified) | Compare changed files in LOOP-005 change log against execution plan scope; assert no additions |
| VER-9 | The regression test is present in the repository, named following SKILL-001 conventions, and referenced in the LOOP-005 implementation summary | Verify test file exists; verify naming convention; cross-reference implementation summary |
| VER-10 | Engineering Metadata artifact exists and contains all required provenance fields including HEAD SHA at start and end | Read metadata file; assert all required fields non-empty |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/engineering/bug-fixing/reflections/REFLECTION-101-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Root Cause Summary:** The root cause statement and confidence level for this run; whether the Checker accepted on first attempt or required retry.
- **Severity and Cycle Time:** Bug severity and elapsed time from trigger to LOOP-006 Accepted outcome.
- **Regression Coverage:** Whether a regression test was produced; the specific code path it covers.
- **Scope Discipline:** Whether the fix was confined to the declared defect location or scope expanded during implementation.
- **Pattern Observation:** Whether this defect shares a root cause pattern with prior bugs in the same module.

---

## Human Approval Gates

### GATE-1 — Hard Gate: Root Cause Approval Before Source Modification

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 4 (Checker Review), before Step 6 (Fix Planning) |
| Artifact Under Review | Root Cause Analysis document and Checker Report |
| Approver | Principal Engineer or Engineering Lead with ownership of the affected module |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; no source code is modified; all analysis artifacts preserved; Reflection produced |
| Audit Trail | Approval record written to `STATUS-101.md` under `gate_outcomes.GATE-1`; approver identity, timestamp, decision, and any scope adjustments recorded |

**Fires when:**
- Normal workflow completion of Step 4 (every run reaches GATE-1 before any source modification)
- Checker validation rejected on both the initial attempt and the one permitted retry
- Multiple candidate root causes could not be resolved by GATE-2

**Reviewer guidance:** Confirm the root cause analysis identifies a specific, falsifiable condition in the code. Verify the Checker confirmed the evidence. Confirm the proposed fix scope is minimal. If scope expansion is warranted, record it explicitly in the approval record. If root cause is incorrect, deny and provide correction notes.

---

### GATE-2 — Soft Gate: Ambiguous Root Cause

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | During Step 3, before Step 4 |
| Artifact Under Review | Root Cause Analysis with multiple candidate causes |
| Approver | Principal Engineer or Engineering Lead with ownership of the affected module |
| Notification Channel | Declared in `.loop-101.yml`; defaults to creating a draft comment on the task record |
| Timeout | 48 hours from notification timestamp |
| Auto-Proceed Action | Loop selects the highest-confidence candidate cause and proceeds; `soft_gate_auto_proceeded: true` with selected candidate recorded |
| Audit Trail | Notification timestamp, candidate list, outcome (human-selected or auto-proceeded) recorded under `gate_outcomes.GATE-2` |

**Fires when:** ANALYSIS-AGENT identifies more than one candidate root cause with independent supporting evidence.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-101.md`. The executing agent reads this file at the start of each step and halts immediately if this value is present. On emergency stop: no further source modifications are made; a partial Reflection is produced recording the step at which the stop was received.

---

## Failure Recovery

### FR-1 — Root Cause Analysis Cannot Identify Defect Location

**Detection:** ANALYSIS-AGENT cannot identify a specific file and line range after examining all context package files.
**Immediate Action:** Record findings with confidence level Low. Flag `root_cause_unresolved = true`.
**Recovery:** Trigger GATE-1 with partial analysis. Human may expand context scope or provide additional information.
**Rollback:** No source code modified; no rollback required.

### FR-2 — Checker Rejects Root Cause Analysis (Both Attempts)

**Detection:** `ANALYSIS-CHECKER` returns `overall_finding: rejected` on both the initial attempt and the one permitted retry.
**Immediate Action:** Record both rejected checker reports. Flag `checker_rejected_max_retries = true`.
**Recovery:** Trigger GATE-1 unconditionally. Human reviews both analysis attempts and both checker reports.
**Rollback:** No source code modified; all analysis documents preserved.

### FR-3 — LOOP-005 Implementation Fails

**Detection:** `STATUS-005.md` records `current_status: failed` or Emergency Stop received during Step 7.
**Immediate Action:** Read LOOP-005 failure record. Record failure reason in `STATUS-101.md`.
**Recovery:** If failure is due to a plan defect, return to Step 6 and re-invoke LOOP-004. If failure is unrecoverable, halt with `failed` status and produce Reflection.
**Rollback:** LOOP-005 manages its own rollback per its specification. Verify LOOP-005 completed rollback by confirming repository state matches the HEAD SHA recorded before LOOP-005 was invoked.

### FR-4 — LOOP-006 Produces Requires Rework Outcome

**Detection:** LOOP-006 verification report records outcome `requires_rework`.
**Immediate Action:** Read the rework specification from the report.
**Recovery:** If rework affects plan structure, re-invoke LOOP-004. If rework is within existing plan scope, re-invoke LOOP-005 directly. Maximum two rework cycles; if a third rework is required, halt with `failed` and produce Reflection.
**Rollback:** LOOP-005 manages rollback for its implementation steps.

### FR-5 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 8 hours.
**Immediate Action:** Complete the current atomic step; do not begin the next step.
**Recovery:** Write all output artifacts produced so far. Write `STATUS-101.md` with `status: stopped`, `reason: max_duration_exceeded`. Produce partial Reflection.
**Rollback:** Not required; partial outputs are valid for the subsequent run.

### FR-6 — Context HEAD SHA Mismatch

**Detection:** Context package HEAD SHA does not match current repository HEAD SHA.
**Immediate Action:** Record mismatch in `STATUS-101.md`. Do not proceed with analysis.
**Recovery:** Trigger GATE-1. Human confirms whether to re-assemble context or acknowledge drift and proceed. Re-invoke LOOP-002 if re-assembly is approved.
**Rollback:** No source code modified.

---

## Metrics

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 10) |
| `run.steps_total` | 10 |
| `gate.hard.count` | Hard gates reached during this run |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-10 criteria that passed |
| `verification.level1.fail` | Count of VER-1 through VER-10 criteria that failed |
| `reflection.produced` | Boolean — was the Reflection artifact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `bug.severity` | Severity of the bug addressed (`P0` \| `P1` \| `P2` \| `P3`) |
| `bug.root_cause_confidence` | Confidence level of root cause analysis (`High` \| `Medium` \| `Low`) |
| `bug.checker_accepted_first_attempt` | Boolean — did ANALYSIS-CHECKER accept on first attempt |
| `bug.rework_cycles` | Count of LOOP-006 Requires Rework outcomes before final outcome |
| `bug.regression_test_produced` | Boolean — was a regression test included in the implementation |
| `bug.fix_files_changed` | Count of source files modified by the fix |
| `bug.time_to_gate1_seconds` | Elapsed seconds from trigger to GATE-1 notification |
| `bug.gate1_wait_seconds` | Elapsed seconds spent waiting at GATE-1 |
| `bug.scope_expanded` | Boolean — was fix scope expanded beyond initial defect location at GATE-1 |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The fix expands beyond the minimum changes needed, incorporating opportunistic refactoring, cleanup, or additional bug fixes.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** ANALYSIS-AGENT identifies adjacent code quality issues during investigation; IMPL-AGENT makes "obvious" improvements while implementing.
- **Control:** LOOP-004 planning constraints prohibit non-fix changes. GATE-1 approval locks scope before implementation. VER-8 verifies only declared files were modified.
- **Detection:** VER-8 criterion failure; LOOP-005 change log contains files not in execution plan.
- **Response:** Halt LOOP-005; record scope violation in STATUS; GATE-1 re-fires for human decision.

### RISK-2 — Architectural Drift

- **Description:** The fix introduces a dependency, pattern, or interface change inconsistent with LOOP-001 architecture.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Root cause is in an architectural boundary component requiring a design decision.
- **Control:** Architecture context loaded before analysis. LOOP-004 constraints prohibit architectural boundary changes without explicit approval. LOOP-006 includes architectural compliance in its profile.
- **Detection:** LOOP-006 architectural compliance check failure.
- **Response:** LOOP-006 Requires Rework; rework specification includes the violated architectural constraint. Architectural boundary changes require additional GATE-1.

### RISK-3 — Hidden Dependencies

- **Description:** The fix corrects the root cause but breaks a caller or consumer that depended on the incorrect behaviour.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** The defective behaviour was compensated for by callers; fixing it exposes caller-side assumptions.
- **Control:** Regression risk assessment in Step 3 identifies adjacent behaviours. LOOP-006 verifies all pre-existing tests still pass.
- **Detection:** LOOP-006 detects pre-existing test failures after fix application.
- **Response:** LOOP-006 Requires Rework. Rework must address caller-side regression without reverting the root cause fix.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop operates on repository artifacts only. It does not read or write tenant-scoped runtime data. No fix is deployed by this loop; deployment is a Release Loop responsibility.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** A fix to data handling code could inadvertently introduce a data loss or corruption path not caught by the regression test.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** The defect is in data persistence, migration, or transformation code.
- **Control:** Root cause analysis must identify data handling scope. LOOP-006 verification profile requires data integrity checks for fixes touching persistence code. GATE-1 reviewer is notified when the fix scope includes data handling components.
- **Detection:** LOOP-006 data integrity verification category failure.
- **Response:** GATE-1 fires with explicit data-handling scope warning before implementation proceeds.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. Source code modifications via LOOP-005 are idempotent: re-running LOOP-005 with an identical execution plan produces equivalent output. This loop does not directly write to any external runtime system.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** A fix to authentication, authorisation, or input validation code could weaken a security control.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** The defect is in security-sensitive code (auth, authz, tenant isolation, input validation, cryptography).
- **Control:** Root cause analysis must flag security-sensitive scope. GATE-1 fires with mandatory Security Lead review per ENGINEERING-LOOP-GUIDE common gate conditions.
- **Detection:** Security-sensitive file paths identified during Step 3 trigger mandatory Security Lead gate notation in GATE-1 request.
- **Response:** Security Lead review is required and cannot be auto-proceeded before any security-sensitive fix is implemented.

### RISK-8 — Runaway Execution

- **Description:** Root cause analysis or LOOP-005 implementation could run beyond the declared maximum duration if the affected codebase is unexpectedly large.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** The affected module is very large; the bug spans many files; LOOP-005 encounters a complex implementation scenario.
- **Control:** Maximum run duration of 8 hours enforced by FR-5. Per-phase budgets: 2 hours for intake and analysis, 6 hours for remaining phases.
- **Detection:** Wall-clock elapsed time check at each step boundary.
- **Response:** FR-5 procedure; partial outputs preserved; run marked `stopped`.

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | All verification criteria VER-1 through VER-10 have passed |
| SC-2 | LOOP-006 produced an Accepted outcome |
| SC-3 | GATE-1 approval was recorded with approver identity and timestamp |
| SC-4 | All artifacts listed in the Outputs table have been written |
| SC-5 | `STATUS-101.md` updated with all metrics and final status `completed` |
| SC-6 | `SKILL-101.md` updated with calibration observations from this run |
| SC-7 | The Reflection artifact has been written |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (8 hours) reached before SC-1 through SC-7 are met |
| SC-9 | GATE-1 denied by the human reviewer |
| SC-10 | PRE-3 detects a concurrent run for the same task ID; this instance exits without modifying any artifact |
| SC-11 | Emergency Stop signal received in `STATUS-101.md` |
| SC-12 | LOOP-006 Requires Rework and maximum rework cycle count (2) is exceeded |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Analysis Artifacts:**
- [ ] `docs/engineering/bug-fixing/bug-classification.md` written with all required fields
- [ ] `docs/engineering/bug-fixing/root-cause-analysis.md` written with root cause statement, mechanism, evidence, defect location, scope, and regression risk
- [ ] `docs/engineering/bug-fixing/checker-report.md` written with overall finding `accepted`

**Implementation Artifacts (via LOOP-005):**
- [ ] Fix implemented at the defect location identified in the root cause analysis
- [ ] Regression test implemented, named per SKILL-001 conventions, covering the exact bug scenario

**Verification Artifacts (via LOOP-006):**
- [ ] Verification report confirms bug scenario no longer reproducible
- [ ] Verification report confirms regression test passes
- [ ] Verification report confirms no pre-existing tests regress

**Gates:**
- [ ] GATE-1 approval recorded in `STATUS-101.md` with approver identity, timestamp, and decision
- [ ] GATE-2 outcome recorded if GATE-2 fired (human selection or auto-proceeded with rationale)

**State:**
- [ ] `docs/loops/engineering/STATUS-101.md` updated with all required metrics and final status
- [ ] `docs/loops/engineering/SKILL-101.md` updated with calibration observations

**Metadata and Reflection:**
- [ ] `docs/engineering/bug-fixing/metadata/METADATA-101-{run-id}.md` produced with all provenance fields
- [ ] `docs/engineering/bug-fixing/engineering-summary.md` produced
- [ ] `docs/engineering/bug-fixing/reflections/REFLECTION-101-{run-id}.md` produced and contains all required sections

---

## Future Improvements

- **Automated reproduction harness:** Integrate with the repository's test framework to automatically attempt to reproduce the bug using the task record's reproduction steps, reducing time between intake and root cause confirmation.
- **Root cause pattern matching:** Cross-reference new root causes against SKILL-101 history to detect recurring defect patterns; surface the pattern to the reviewer at GATE-1 enabling structural fixes to be scheduled alongside the immediate fix.
- **Blame-based scope narrowing:** Use `git blame` on the reported symptom location to identify the commit that introduced the defect, providing additional context for regression test design.
- **P0 expedited path:** Define an expedited gate procedure for P0 bugs that reduces GATE-2 timeout from 48 hours to 2 hours and pre-authorises a reduced-scope emergency fix while the full root cause analysis continues.
- **Checker confidence calibration:** Track cases where the Checker accepted an analysis that later proved incorrect (defect escape), using SKILL-101 accumulation to calibrate minimum evidence requirements for acceptance.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract requirements
- `docs/loops/shared/SPEC-011-REVIEW-PROCESS.md` — review process for loop activation
- `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared Engineering Loop philosophy, terminology, and design principles
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-101 as the governed bug-fix loop for the AEOS Engineering Loop series.

