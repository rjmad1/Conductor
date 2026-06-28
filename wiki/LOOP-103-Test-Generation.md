---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/LOOP-103-Test-Generation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Standard loop for unit and integration test generation.
Generalized Purpose: Standard loop for unit and integration test generation.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-005 — Implementation, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-103 — Test Generation

**Loop ID:** LOOP-103
**Name:** Test Generation
**Version:** 1.0
**Status:** Active
**Category:** Engineering
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-005 — Implementation, LOOP-006 — Verification
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---

## Purpose

LOOP-103 identifies test coverage gaps in declared target modules and generates targeted, executable test cases that measurably close those gaps. The loop delivers committed test code with a verified positive coverage delta: every test it produces must actually run and pass against the current unmodified codebase, testing existing behaviour rather than hypothetical behaviour. Trivial tests that pass regardless of the implementation under test do not count toward coverage targets.

---

## Problem Statement

Coverage gaps emerge continuously as features are added, code is refactored, and edge cases are discovered. Without a systematic, governed approach to closing gaps, the gaps compound — each new LOOP-101 or LOOP-102 run operates with a weaker equivalence baseline, and defect escape rates climb. Ad-hoc test writing produces tests that are structurally correct but do not actually exercise the intended behaviour, creating an illusion of coverage while providing no verification value. Without independent review of test specifications before implementation, trivial tests are merged and permanently inflate coverage numbers without providing detection capability.

---

## Why This Loop Exists

Test generation is a repeatability and verifiability concern. Repeatable: the same gap analysis applied to the same module should produce equivalent test case specifications regardless of who performs it. Verifiable: test cases must be independently reviewed before implementation to prevent trivially-passing tests from entering the suite. Auditable: the coverage delta must be measured before and after, and the gap between declared target coverage and achieved coverage must be explicit. Safety: no test code is merged until a human has confirmed the test case specifications are meaningful — the GATE-1 review of specifications before implementation is this loop's primary safety mechanism.

---

## Scope

**In scope:**
- Running coverage analysis on declared target module(s) and parsing the resulting report
- Classifying each coverage gap by the type of test needed (unit, integration, contract, edge case)
- Producing test case specifications (descriptions of what each test should verify, not code)
- Independent Checker review of each specification for triviality, redundancy, and correctness
- Implementing approved test cases via LOOP-005
- Verifying that all new tests pass, coverage delta is positive, and no existing tests regress

**Out of scope:**
- Fixing source code to make it more testable (that is LOOP-102)
- Fixing existing failing tests (that is LOOP-101)
- Writing tests for behaviour that does not yet exist (hypothetical behaviour tests are not coverage gap tests)
- Integration test infrastructure changes (new test databases, new mock servers, new test containers)
- Performance or load tests

**Maximum run duration:** 12 hours total. If this duration is exceeded, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Test generation task record | File (`docs/tasks/task-catalog.md` entry with `primary_category: TEST`) | LOOP-003 — Task Discovery | Required |
| Target module(s) declaration | Field within task record: which modules to close coverage gaps in | Task record | Required |
| Coverage target | Field within task record: minimum acceptable coverage percentage post-run | Task record | Required |
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 — Architecture Discovery | Required — must be no more than 7 days old |
| LOOP-002 context package | Directory (`docs/context/`) | LOOP-002 — Context Assembly | Required — produced during this run |
| Coverage report for target module(s) | Repository State | Coverage analysis tool via SKILL-001 commands | Required — produced in Step 1 |
| Existing test files for target module(s) | Repository State | Filesystem | Required — for gap identification and deduplication |
| Engineering Loop configuration | File (`.loop-103.yml` at repo root) | Repository | Optional |

### Input Validation

Before any step begins, the loop must verify:
- The task record exists, has `primary_category: TEST`, declares target module(s), and declares a coverage target.
- LOOP-001 outputs exist and are within 7 days.
- No other LOOP-103 instance is running for the same task ID.
- Coverage analysis tooling is available (tool command in SKILL-001).
- HEAD SHA recorded at validation time.

---

## Outputs

All primary outputs are written to `docs/engineering/test-generation/`.

| Artifact | Path | Description |
|----------|------|-------------|
| Coverage Gap Report | `docs/engineering/test-generation/coverage-gap-report.md` | Current coverage metrics, identified gaps classified by type, and prioritised gap list |
| Test Case Specifications | `docs/engineering/test-generation/test-case-specifications.md` | Descriptions of each test case: what behaviour it verifies, the input conditions, the expected output, and the type (unit/integration/contract/edge case) |
| Gap Checker Report | `docs/engineering/test-generation/gap-checker-report.md` | GAP-CHECKER's independent review of each test case specification for triviality, redundancy, and correctness |
| Verification Report | `docs/verification/verification-report.md` | Produced by LOOP-006: all new tests pass; coverage delta is positive; no existing tests regress |
| Engineering Summary | `docs/engineering/test-generation/engineering-summary.md` | Complete run summary for human review |
| Loop Status | `docs/loops/engineering/STATUS-103.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/engineering/SKILL-103.md` | Calibration observations accumulated across runs |
| Run Metadata | `docs/engineering/test-generation/metadata/METADATA-103-{run-id}.md` | Provenance: task ID, Core Loop run IDs, HEAD SHA at start and end, elapsed duration, outcome |
| Reflection | `docs/engineering/test-generation/reflections/REFLECTION-103-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Module boundaries, technology stack, and test framework information. Required — outputs must exist and be no more than 7 days old.
- **LOOP-002 — Context Assembly:** Context package for target module(s) including existing test files. Required — invoked during this run.
- **LOOP-005 — Implementation:** Implementation of approved test cases. Required — invoked during this run.
- **LOOP-006 — Verification:** Verification that all new tests pass, coverage delta is positive, no existing tests regress. Required — invoked during this run.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer selects a `TEST`-classified task and triggers LOOP-103.
2. **Automated task dispatch** — AEOS task dispatcher routes a `TEST`-classified task per SPEC-010 rules.
3. **Pre-refactoring prerequisite** — LOOP-102 GATE-2 fires for insufficient coverage, triggering an automated LOOP-103 recommendation for the same module scope.
4. **Scheduled coverage audit** — A periodic governance loop identifies modules below the minimum coverage threshold and creates TEST tasks for each.

The trigger source, task ID, and current HEAD SHA must be recorded in `STATUS-103.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | A `TEST`-classified task exists with declared target module(s) and coverage target | Read task catalog; assert `primary_category: TEST` with non-empty target and coverage target fields |
| PRE-2 | LOOP-001 outputs exist and are within 7 days | Read `STATUS-001.md`; assert timestamp within 7 days |
| PRE-3 | No other LOOP-103 instance is running for the same task ID | Read `STATUS-103.md`; assert not `running` for this task ID |
| PRE-4 | Coverage analysis tooling is configured and runnable | Read `SKILL-001.md`; assert coverage tool command is populated |
| PRE-5 | The test runner is configured and runnable | Read `SKILL-001.md`; assert test runner command is populated |
| PRE-6 | Write access to `docs/engineering/test-generation/` | Probe write; remove on success |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Coverage analysis tool | Read | Execute coverage analysis on target module(s) only; parse report | Same as executing agent; tool invoked via SKILL-001 command | Scoped to target module(s) only | Read-only; no side effects | Yes |
| Test runner | Read | Execute existing test suite to confirm no existing tests fail | Same as executing agent | Full suite execution per SKILL-001 command | Read-only | Yes |
| Repository filesystem (source and test) | Read | Source files and test files for target module(s) | Filesystem permissions | Scoped to target module(s) | No writes in this loop's own steps | Yes |
| `docs/engineering/test-generation/` | Write | All files listed in Outputs table | Same as executing agent | Confined to this directory | `git checkout docs/engineering/test-generation/` | Yes |
| `docs/loops/engineering/STATUS-103.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/STATUS-103.md` | Yes |
| `docs/loops/engineering/SKILL-103.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/SKILL-103.md` | Yes |

This loop does not write to any external system outside the repository. Test file creation is delegated exclusively to LOOP-005.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/engineering/LOOP-103-Test-Generation.md` — this document
3. `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared Engineering Loop context
4. `docs/loops/engineering/STATUS-103.md` — prior run state
5. `docs/loops/core/SKILL-001.md` — coverage tool command, test runner command, test file naming conventions
6. `docs/architecture/module-catalog.md` — module boundaries and ownership
7. `docs/architecture/technology-stack.md` — language and test framework details
8. The selected task record from `docs/tasks/task-catalog.md`

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `COVERAGE-AGENT` | Maker | Step 1: runs coverage tool; parses report; identifies uncovered branches, methods, and edge cases | Coverage tool, filesystem read | None |
| `CONTEXT-AGENT` | Maker | Step 2: invokes LOOP-002 for target module context | LOOP-002 invocation | None |
| `GAP-ANALYST` | Maker | Step 3: classifies each coverage gap by test type; produces test case specifications | Filesystem read, gap analysis | Reports to GATE-2 if significant integration testing would be required |
| `GAP-CHECKER` | Checker | Step 4: independently reviews each test case specification for triviality, redundancy, and scope creep; produces Gap Checker Report | Filesystem read, specification review | Independent of GAP-ANALYST; finding reviewed at GATE-1 |
| `IMPL-AGENT` | Maker | Step 7: invokes LOOP-005 to implement approved test cases | LOOP-005 invocation | Governed by LOOP-005's own gates |
| `VERIF-AGENT` | Maker | Step 8: invokes LOOP-006 to verify tests pass and coverage delta is positive | LOOP-006 invocation | Governed by LOOP-006's own verification |
| `STATUS-WRITER` | Maker | Final steps: updates STATUS-103.md, SKILL-103.md; produces Engineering Summary, Metadata, Reflection | Filesystem write | None |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1 and GATE-2: reviews test case specifications and coverage strategy | Human judgment | Sole authority for gate decisions |

`GAP-ANALYST` and `GAP-CHECKER` must be separate agent instances.

---

## Workflow

### Step 1 — Coverage Analysis

**Agent:** `COVERAGE-AGENT`
**Inputs:** Target module(s) declaration from task record, SKILL-001.md (coverage tool command)
**Outputs:** `docs/engineering/test-generation/coverage-gap-report.md`

Execute the coverage analysis tool (command from SKILL-001) against the target module(s). Parse the resulting report. Extract:
- Current branch coverage percentage for each target module
- List of uncovered branches with file path and line range
- List of uncovered methods with file path and signature
- List of uncovered edge cases (branches that are covered by at least one test but whose boundary conditions are not tested — e.g., a null check that is covered by the happy path but never exercised with a null input)
- Coverage delta from the declared target: how many percentage points are needed to reach the target

Sort gaps by value: methods with zero coverage above branches with partial coverage above edge cases. Write `coverage-gap-report.md` with the prioritised gap list.

Update `STATUS-103.md` to `running`.

---

### Step 2 — Context Assembly

**Agent:** `CONTEXT-AGENT`
**Inputs:** Target module(s) from task record, LOOP-001 architecture outputs
**Outputs:** Context package at `docs/context/` (produced by LOOP-002)

Invoke LOOP-002 parameterised with:
- Target module(s) as the scope
- Tier 1 Required: all source files for the target module(s); all existing test files for the target module(s)
- Tier 2 Required: inter-module dependency context (for understanding caller context when writing tests)
- Tier 3 Optional: test helper utilities and test infrastructure files declared in SKILL-001

Validate context package HEAD SHA against current repository HEAD SHA. If mismatch, trigger GATE-1.

---

### Step 3 — Gap Analysis and Test Case Specification

**Agent:** `GAP-ANALYST` (Maker)
**Inputs:** Coverage gap report from Step 1, context package from Step 2
**Outputs:** `docs/engineering/test-generation/test-case-specifications.md`

For each identified coverage gap, produce a test case specification. Each specification must contain:
- **Gap ID:** Reference to the gap in the coverage gap report
- **Test type:** Unit (isolated class behaviour, dependencies mocked), Integration (cross-module interaction, real dependencies), Contract (API or event schema behaviour), Edge case (boundary condition on existing covered code)
- **Behaviour being tested:** A single, specific, falsifiable statement of what the test verifies (e.g., "When `processOrder` is called with a null `customerId`, it throws `IllegalArgumentException` with message containing 'customerId must not be null'")
- **Input conditions:** The specific inputs required to exercise this gap
- **Expected output:** The specific expected return value, exception, state change, or event emission
- **Setup required:** Any mock configuration, test fixture, or database state required
- **Why this test is non-trivial:** A statement of what implementation defect this test would detect if the behaviour were wrong

Flag any gap where a meaningful test would require: creating new test infrastructure (new database, new mock server), significant integration with external systems, or changes to source code to make it testable. These flags trigger GATE-2 consideration.

Write `test-case-specifications.md` with all specifications, sorted by gap type (unit first, then contract, then integration, then edge case).

---

### Step 4 — Specification Review (Checker)

**Agent:** `GAP-CHECKER` (Checker)
**Inputs:** `docs/engineering/test-generation/test-case-specifications.md`, context package, coverage gap report
**Outputs:** `docs/engineering/test-generation/gap-checker-report.md`

`GAP-CHECKER` independently reviews each test case specification:

1. **Triviality check:** Would the test pass regardless of the implementation? A test that always passes (e.g., asserts `true`, asserts that a method doesn't throw for the happy path when no business logic is present) is trivial. Flag `TRIVIAL`.
2. **Redundancy check:** Does a materially equivalent test already exist in the test suite? If yes, flag `REDUNDANT` with the path to the existing test.
3. **Scope check:** Would implementing this test require modifying source code, adding test infrastructure, or writing integration tests requiring external systems? Flag `REQUIRES-INFRASTRUCTURE` or `REQUIRES-SOURCE-CHANGE`.
4. **Accuracy check:** Is the expected output consistent with the source code behaviour? If the specification asserts an output that the current implementation cannot produce, flag `INCONSISTENT-WITH-IMPLEMENTATION` — this indicates a bug (create a LOOP-101 task) rather than a coverage gap.
5. **Non-triviality statement check:** Does each specification include a falsifiable "why this test is non-trivial" statement?

Write `gap-checker-report.md` with: per-specification findings, flag counts, and overall finding (`accepted` | `accepted-with-exclusions` | `rejected`). Excluded specifications (flagged TRIVIAL or REDUNDANT) are removed from the approved set. Specifications flagged REQUIRES-INFRASTRUCTURE are forwarded to GATE-2. Specifications flagged INCONSISTENT-WITH-IMPLEMENTATION become new LOOP-101 task records.

---

### [GATE-2 — Soft Gate: Integration Test Infrastructure Required]

Fires when GAP-CHECKER identifies specifications requiring significant integration test infrastructure. The loop notifies the reviewer that closing these specific gaps would require infrastructure work beyond this loop's scope. Auto-proceeds by excluding the flagged specifications from the implementation scope. See `## Human Approval Gates` — GATE-2.

---

### Step 5 — [GATE-1 — Hard Gate: Test Case Specification Approval Before Test Code Merge]

The loop halts. No test code is written until human approval is recorded. The human reviewer examines the approved test case specifications (post-checker exclusions) and confirms they are meaningful and non-trivial. See `## Human Approval Gates` — GATE-1.

---

### Step 6 — Architecture Freshness Re-validation

**Agent:** `COVERAGE-AGENT`
**Inputs:** GATE-1 approval record, STATUS-001.md
**Outputs:** Freshness note in STATUS-103.md

Confirm LOOP-001 outputs are still within the 7-day freshness threshold after GATE-1 wait. If stale, record in STATUS-103.md and reduce confidence score accordingly.

---

### Step 7 — Test Case Implementation

**Agent:** `IMPL-AGENT`
**Inputs:** Approved test case specifications (post GATE-1), context package
**Outputs:** New test files (produced by LOOP-005)

Invoke LOOP-005 with the approved test case specifications as the execution plan. LOOP-005 implements each test case following the naming conventions, file locations, and test structure patterns from SKILL-001 and the context package's existing test files.

LOOP-005 must not modify any source file. If a specification requires a source code change to implement, LOOP-005 must halt and report the issue; this loop creates a new LOOP-102 task (if the change is structural) or LOOP-101 task (if the change is a bug fix) rather than proceeding.

---

### Step 8 — Verification

**Agent:** `VERIF-AGENT`
**Inputs:** LOOP-005 outputs, coverage gap report (baseline), task record (coverage target)
**Outputs:** Verification report at `docs/verification/verification-report.md` (produced by LOOP-006)

Invoke LOOP-006 with the capability-specific verification profile:
- **Mandatory:** All new test cases pass on the unmodified codebase.
- **Mandatory:** Coverage percentage for the target module(s) has increased (positive delta).
- **Mandatory:** All pre-existing tests still pass (no regressions).
- **Mandatory:** No source files were modified (only test files added or modified).
- **Advisory:** Coverage target declared in the task record — whether the target was reached or the delta toward it.

If any new test fails, LOOP-006 produces Requires Rework. The failing test specification must be reviewed: if the test was correctly specified but the implementation is wrong, the test implementation is corrected (LOOP-005 rework). If the test specification was incorrect (inconsistent with actual implementation behaviour), the specification is a LOOP-101 candidate — it revealed a bug.

---

### Step 9 — Status, Skill, and Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All run artifacts, all gate outcomes, LOOP-006 verification report
**Outputs:** Updated `STATUS-103.md`, `SKILL-103.md`, `engineering-summary.md`, `METADATA-103-{run-id}.md`, `REFLECTION-103-{run-id}.md`

Record all metrics. Update SKILL with: effective test types for this module and technology stack; gap patterns that are structurally untestable without infrastructure; test specifications that revealed bugs. Produce Reflection before marking run closed.

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `coverage-gap-report.md` exists and contains a prioritised list of gaps with file paths, line ranges, and gap types | Read file; assert non-empty with at least one gap entry |
| VER-2 | `test-case-specifications.md` exists and each specification contains all required fields (behaviour, inputs, expected output, setup, non-triviality statement) | Read file; assert each specification has all required fields |
| VER-3 | `gap-checker-report.md` exists with overall finding `accepted` or `accepted-with-exclusions` | Read file; assert overall finding is not `rejected` |
| VER-4 | No source files were modified by this loop or by LOOP-005 (only test files added or modified) | Compare LOOP-005 change log against source file list; assert no source file modifications |
| VER-5 | All new test cases pass on the unmodified codebase | Read LOOP-006 verification report; assert all new tests `pass` |
| VER-6 | Coverage percentage for target module(s) has increased | Read LOOP-006 verification report; assert coverage delta is positive |
| VER-7 | All pre-existing tests still pass | Read LOOP-006 verification report; assert pre-existing test pass count is unchanged |
| VER-8 | `STATUS-103.md` updated with current run ID and final status within 5 minutes of run end | Read STATUS file; assert run ID, status, and timestamp within tolerance |
| VER-9 | The Reflection artifact exists and is non-empty | File existence and non-emptiness check |
| VER-10 | GATE-1 approval recorded in `STATUS-103.md` with approver identity, timestamp, and decision | Read STATUS file; assert `gate_outcomes.GATE-1` populated with all required fields |

---

## Reflection

At the end of every run, produce a Reflection at `docs/engineering/test-generation/reflections/REFLECTION-103-{run-id}.md`.

The Reflection must contain all ten LOOP-STANDARD sections plus:

- **Coverage Delta:** Baseline coverage percentage versus final coverage percentage for each target module; gap toward declared target.
- **Specification Quality:** Count of specifications approved, excluded (trivial/redundant), forwarded to GATE-2 (infrastructure required), and converted to LOOP-101 tasks (inconsistent-with-implementation).
- **Test Type Distribution:** Count of unit, integration, contract, and edge case tests produced; whether the distribution matches the gap analysis.
- **Non-Triviality Confidence:** Assessment of whether the approved tests would detect the behaviours they claim to detect, based on Checker review outcomes.
- **Structural Testability Observations:** Any source code patterns that made gap closure difficult (e.g., hard-coded dependencies, private methods with no observable side effect, tightly coupled classes) — feeds future LOOP-102 recommendations.

---

## Human Approval Gates

### GATE-1 — Hard Gate: Test Case Specification Approval Before Test Code Merge

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 4 (Gap Checker Review), before Step 7 (Test Case Implementation) |
| Artifact Under Review | Approved test case specifications (post-checker exclusions) |
| Approver | Principal Engineer or Engineering Lead for the target module |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; no test code written; all analysis artifacts preserved |
| Audit Trail | Written to `STATUS-103.md` under `gate_outcomes.GATE-1`; approver identity, timestamp, decision |

**Fires when:**
- Normal workflow completion of Step 4 (every run reaches GATE-1 before test code is written)
- Checker returns `rejected` on both initial and retry attempts

**Reviewer guidance:** Review each approved specification. Confirm the "why this test is non-trivial" statement is convincing. Confirm the expected output is consistent with the module's declared contract. Reject any specification that would measure coverage without providing meaningful detection capability. It is acceptable to approve a partial set (some specifications) if others require revision.

---

### GATE-2 — Soft Gate: Integration Test Infrastructure Required

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 4 (Gap Checker Review), before GATE-1 |
| Artifact Under Review | Test case specifications flagged `REQUIRES-INFRASTRUCTURE` |
| Approver | Engineering Lead or Infrastructure Owner |
| Notification Channel | Declared in `.loop-103.yml`; defaults to draft comment on task record |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop excludes infrastructure-requiring specifications from this run; they are converted to new task records for future planning; `soft_gate_auto_proceeded: true` recorded |
| Audit Trail | Notification timestamp, flagged specifications, exclusion list, outcome recorded under `gate_outcomes.GATE-2` |

**Fires when:** GAP-CHECKER identifies specifications requiring significant integration test infrastructure beyond this loop's scope.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-103.md`. The executing agent reads this at each step boundary and halts immediately. A partial Reflection is produced.

---

## Failure Recovery

### FR-1 — Coverage Tool Cannot Execute

**Detection:** Coverage tool command from SKILL-001 exits with error.
**Immediate Action:** Record the error. Flag `coverage_tool_failed = true`.
**Recovery:** Halt with `precondition_failed` if this is Step 1. If the tool fails partway through a run, write partial coverage gap report with available data and trigger GATE-1 for human decision.
**Rollback:** No outputs written before tool failure.

### FR-2 — Gap Checker Returns Rejected (Both Attempts)

**Detection:** GAP-CHECKER returns `overall_finding: rejected` on both initial and retry.
**Immediate Action:** Record both checker reports. Flag `checker_rejected_max_retries = true`.
**Recovery:** Trigger GATE-1 unconditionally with both reports for human review.
**Rollback:** No test code written.

### FR-3 — New Test Fails at LOOP-006 Verification

**Detection:** LOOP-006 reports that a new test case fails.
**Immediate Action:** Read the failure details. Determine cause: (a) test implementation error (wrong mock, wrong assertion), or (b) test specification error (expected output inconsistent with actual behaviour).
**Recovery:** (a) LOOP-005 rework for implementation error. (b) If specification error: the failing test reveals that the current implementation does not match the expected output — create a LOOP-101 task for the inconsistency; exclude the failing test from this run's deliverables; re-verify remaining tests.
**Rollback:** LOOP-005 rolls back the failing test implementation.

### FR-4 — LOOP-005 Attempts to Modify Source Files

**Detection:** LOOP-005 requests permission to modify a source (non-test) file.
**Immediate Action:** LOOP-005 halts that step. This loop receives the notification via STATUS-005.
**Recovery:** Create a new LOOP-102 task (if structural) or LOOP-101 task (if bug) for the source change. Proceed with test case implementations that do not require source changes.
**Rollback:** No source modifications occurred.

### FR-5 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time exceeds 12 hours.
**Immediate Action:** Complete current atomic step; do not begin next.
**Recovery:** Write all output artifacts produced so far. `STATUS-103.md` set to `stopped`, `reason: max_duration_exceeded`. Produce partial Reflection.
**Rollback:** Not required; partial outputs are valid for resumption.

---

## Metrics

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 9) |
| `run.steps_total` | 9 |
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
| `test_gen.target_modules` | Count of target modules declared |
| `test_gen.gaps_identified` | Count of coverage gaps identified in Step 1 |
| `test_gen.specifications_produced` | Count of test case specifications produced by GAP-ANALYST |
| `test_gen.specifications_approved` | Count approved after Checker review and GATE-1 |
| `test_gen.specifications_excluded_trivial` | Count excluded as trivial |
| `test_gen.specifications_excluded_redundant` | Count excluded as redundant |
| `test_gen.specifications_forwarded_infrastructure` | Count forwarded to GATE-2 as infrastructure-requiring |
| `test_gen.specifications_converted_loop101` | Count converted to LOOP-101 tasks |
| `test_gen.tests_implemented` | Count of test cases implemented by LOOP-005 |
| `test_gen.coverage_pct_baseline` | Coverage percentage before this run |
| `test_gen.coverage_pct_final` | Coverage percentage after this run |
| `test_gen.coverage_delta_pct` | Percentage point improvement |
| `test_gen.coverage_target_reached` | Boolean — was declared target reached |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** Test cases expand beyond the declared target modules, or test implementations require and then make source code changes.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Some coverage gaps can only be closed by testing private behaviour that is not accessible from the public interface, leading to pressure to expose it.
- **Control:** VER-4 verifies no source files were modified. LOOP-005 halts and reports if a source modification is needed.
- **Detection:** VER-4 failure.
- **Response:** FR-4 procedure; source changes become separate task records.

### RISK-2 — Architectural Drift

- **Description:** New test code introduces test utilities or fixtures that create cross-module dependencies not sanctioned by the architecture.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Test infrastructure is shared across module boundaries without a declared shared test library.
- **Control:** Context package includes module boundary information; test case specifications must not reference types from other modules unless through declared public interfaces.
- **Detection:** LOOP-006 architectural compliance check.
- **Response:** LOOP-006 Requires Rework; offending test imports are corrected.

### RISK-3 — Hidden Dependencies

- **Description:** Test case specifications assume mock behaviour that does not accurately reflect the dependency's contract, producing tests that pass in isolation but mask incorrect behaviour in integration.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Unit test specifications rely on mocked dependencies with incorrectly specified behaviour.
- **Control:** GAP-CHECKER specification review includes checking that expected outputs are consistent with declared contracts of mocked dependencies.
- **Detection:** Integration test runs in CI that exercise the same paths without mocks.
- **Response:** Flagged during Checker review; specification revised before GATE-1.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop operates on repository source and test files only. It does not access tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** Not applicable. This loop only adds test files; it does not modify source code, data handling code, or configuration. The only data risk would be from test execution that modifies shared test state, but this loop verifies tests on the unmodified codebase.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. Test file creation via LOOP-005 is idempotent. This loop does not write to any external runtime system.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** Test code could inadvertently expose security-sensitive implementation details (hardcoded credentials in test fixtures, bypassed auth checks in test helpers).
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Test case specifications for authentication or authorisation code require test fixtures that store credentials or bypass checks.
- **Control:** GAP-CHECKER includes a security check for specifications that require credential fixtures or auth bypass patterns. These are flagged for Security Lead review at GATE-1.
- **Detection:** Security patterns in test specifications identified during Checker review.
- **Response:** Security Lead review required at GATE-1 for any test specification flagged for security sensitivity.

### RISK-8 — Runaway Execution

- **Description:** Large coverage gap analysis on a very large module or coverage tool failure could exhaust the 12-hour time budget.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Target module(s) are very large; coverage tool is slow; many LOOP-006 rework cycles occur.
- **Control:** Maximum run duration enforced by FR-5. Task record should declare a focused scope rather than an entire service.
- **Detection:** Wall-clock elapsed time check at each step boundary.
- **Response:** FR-5 procedure; partial outputs preserved.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All verification criteria VER-1 through VER-10 passed |
| SC-2 | LOOP-006 produced Accepted outcome |
| SC-3 | GATE-1 approval recorded with approver identity and timestamp |
| SC-4 | All artifacts in Outputs table written |
| SC-5 | `STATUS-103.md` updated with all metrics and `completed` status |
| SC-6 | `SKILL-103.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (12 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | Concurrent run detected for same task ID |
| SC-11 | Emergency Stop received |
| SC-12 | LOOP-006 Requires Rework and maximum rework cycles (2) exceeded |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Analysis Artifacts:**
- [ ] `docs/engineering/test-generation/coverage-gap-report.md` written with prioritised gap list
- [ ] `docs/engineering/test-generation/test-case-specifications.md` written with all required fields per specification
- [ ] `docs/engineering/test-generation/gap-checker-report.md` written with overall finding `accepted` or `accepted-with-exclusions`

**Implementation Artifacts (via LOOP-005):**
- [ ] All approved test cases implemented as runnable test methods
- [ ] Test files named and placed following SKILL-001 conventions
- [ ] No source files modified

**Verification Artifacts (via LOOP-006):**
- [ ] All new tests pass
- [ ] Coverage delta is positive
- [ ] No pre-existing tests regress

**Gates:**
- [ ] GATE-1 approval recorded with approver identity, timestamp, and decision
- [ ] GATE-2 outcome recorded if fired (auto-proceeded with exclusion list, or human decision)

**State:**
- [ ] `docs/loops/engineering/STATUS-103.md` updated with all metrics and final status
- [ ] `docs/loops/engineering/SKILL-103.md` updated

**Metadata and Reflection:**
- [ ] `docs/engineering/test-generation/metadata/METADATA-103-{run-id}.md` produced
- [ ] `docs/engineering/test-generation/engineering-summary.md` produced
- [ ] `docs/engineering/test-generation/reflections/REFLECTION-103-{run-id}.md` produced with all required sections

---

## Future Improvements

- **Mutation testing integration:** After LOOP-006 verification, run a mutation testing tool against the target module to measure how many mutations the new tests detect; feed the mutation score back to GAP-CHECKER to calibrate non-triviality assessment.
- **Specification template library:** Build a library of test case specification templates per gap type (null input, boundary value, concurrency, error path) and per technology stack, reducing the time required to produce complete specifications.
- **LOOP-102 integration signal:** When SKILL-103 accumulates observations that a module is structurally difficult to test (low achievable coverage despite many gap closure attempts), automatically create a LOOP-102 task to improve testability before the next LOOP-103 run.
- **Coverage target calibration:** Use historical coverage delta data across LOOP-103 runs to estimate achievable coverage given a module's size and complexity, enabling more realistic target-setting in the task record.

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

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-103 as the governed test generation loop for the AEOS Engineering Loop series.

