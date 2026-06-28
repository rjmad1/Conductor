---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-006-Verification.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Run verification checks (linter, unit tests, custom assertions) on output.
Generalized Purpose: Run verification checks (linter, unit tests, custom assertions) on output.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery, LOOP-004 — Planning, LOOP-005 — Implementation
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-006 — Verification

**Loop ID:** LOOP-006  
**Name:** Verification  
**Version:** 1.0  
**Status:** Active  
**Category:** Core  
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery, LOOP-004 — Planning, LOOP-005 — Implementation  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-006 independently validates the outputs produced by LOOP-005. Its sole responsibility is to determine, through objective evidence, whether the implementation satisfies the approved execution plan, the repository's quality standards, the applicable architectural constraints, and the verification criteria declared in LOOP-004. It never performs implementation. It never prescribes corrective changes. It evaluates, records evidence, and delivers exactly one of three outcomes: Accepted, Requires Rework, or Requires Human Review.

The loop is the quality gate that separates implementation from release. Its outputs are the authoritative record of what was verified, what evidence was collected, and what determination was made. That record is an engineering artefact of permanent value — it is not discarded after acceptance.

---

## Problem Statement

Implementation without independent verification produces changes that are accepted on faith rather than evidence. The implementing loop has an inherent conflict of interest: it is motivated to produce an accepted outcome and cannot objectively assess its own work. Without a structurally separate verification loop operating from the same plan but applying independent methods, defects that are invisible to the implementer — regressions in adjacent modules, documentation that diverges from code, architectural boundary violations, API compatibility breaks — escape to downstream loops and ultimately to production. The cost of a defect grows by an order of magnitude at each loop boundary it crosses.

---

## Why This Loop Exists

Verification is the engineering process that converts the implementer's claim ("I believe this is correct") into an objective finding ("the evidence shows this is or is not correct"). Its value is structural, not incidental: the separation between the agent that produces a change and the agent that evaluates it is the minimum condition for the evaluation to be trustworthy. A loop that implements and then self-verifies provides no independent assurance. Codifying verification as a separate loop, with its own inputs, evidence standards, outcome taxonomy, and human gates, makes that independence durable — it cannot be collapsed by a shortcut, a time constraint, or a misaligned incentive.

---

## Scope

**In scope:**
- Loading and interpreting the LOOP-005 verification handoff package and implementation artefacts
- Loading the LOOP-004 execution plan and verification plan as the authoritative standard against which the implementation is measured
- Executing all declared verification categories against the implementation
- Collecting objective, reproducible evidence for each verification category
- Detecting regressions in components not directly modified but potentially affected by the change
- Validating documentation consistency between code changes and recorded specifications
- Validating architectural compliance: module boundaries, ADR adherence, API contracts, event schemas
- Producing a complete, structured verification report with evidence references
- Determining exactly one outcome: Accepted, Requires Rework, or Requires Human Review
- Updating `STATUS-006.md` and `SKILL-006.md`

**Out of scope:**
- Modifying any repository source file, test file, configuration file, or documentation (this constraint is absolute; any discovered need for modification is communicated through the outcome and the rework specification)
- Prescribing implementation changes — the verification report describes findings; LOOP-005 determines how to address them
- Selecting which task to verify (LOOP-005 hands off the selected implementation)
- Making architectural decisions (findings are reported; decisions belong to LOOP-004 and human reviewers)
- Executing deployment, release, or post-release verification (those are Release loop responsibilities)
- Producing a passing outcome by adjusting expectations to match the implementation

**Maximum run duration:** 2 hours. If the loop has not reached a Stop Condition within this window, it must halt, record partial evidence, and produce a Reflection with status `stopped` and outcome `Requires Human Review` (partial evidence is insufficient for Accepted or Requires Rework determinations).

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/implementation/implementation-summary.md` | File | LOOP-005 output | Required |
| `docs/implementation/change-log.md` | File | LOOP-005 output | Required |
| `docs/implementation/modified-components.md` | File | LOOP-005 output | Required |
| `docs/implementation/implementation-metadata.md` | File | LOOP-005 output | Required |
| `docs/implementation/assumptions.md` | File | LOOP-005 output | Required |
| `docs/loops/core/STATUS-005.md` | File | LOOP-005 | Required — completion status and `execution_end_sha` |
| `docs/planning/execution-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/verification-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/rollback-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/implementation-breakdown.md` | File | LOOP-004 output | Required |
| `docs/planning/planning-metadata.md` | File | LOOP-004 output | Required |
| `docs/loops/core/STATUS-004.md` | File | LOOP-004 | Required |
| `docs/context/verification-context.md` | File | LOOP-002 output | Required |
| `docs/context/architecture-context.md` | File | LOOP-002 output | Required |
| `docs/context/dependency-context.md` | File | LOOP-002 output | Required |
| `docs/context/context-metadata.md` | File | LOOP-002 output | Required — HEAD SHA check |
| `docs/architecture/module-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/api-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/event-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/dependency-map.md` | File | LOOP-001 output | Required |
| `docs/architecture/technology-stack.md` | File | LOOP-001 output | Required |
| `docs/loops/core/SKILL-001.md` | File | LOOP-001 | Required — toolchain and test framework profile |
| `docs/loops/shared/coding-standards.md` | File | Shared | Required |
| `docs/loops/shared/verification-standards.md` | File | Shared | Required |
| `docs/tasks/task-catalog.md` | File | LOOP-003 output | Required — task classification |
| ADR directory | `docs/adr/` or configured equivalent | Repository | Required |
| Verification configuration | `.loop-006.yml` at repo root | Repository | Optional |

### Input Validation

Before Step 1 begins, the loop must verify:

- `STATUS-005.md` records `status: completed` for the selected task. If LOOP-005 did not complete, this loop cannot begin — there is no complete implementation to verify.
- The `execution_end_sha` in `implementation-metadata.md` matches the current repository HEAD SHA. If they differ, the repository has been modified since LOOP-005 completed; the implementation may not be what LOOP-005 produced. This is a GATE-1 trigger.
- `implementation-summary.md` contains a readiness statement signed by the `IMPL-CHECKER` agent. If the readiness statement is absent, the handoff package is incomplete and this loop cannot begin.
- No concurrent LOOP-006 run is active for the same task ID.
- The executing agent has no write access to repository source files. This is verified by confirming the agent's execution profile; any configuration that would allow source file modification must be rejected before the loop starts.

---

## Outputs

All outputs are written to `docs/verification/`. This loop modifies no other directory and no repository source file. The constraint is absolute and non-negotiable.

| Artefact | Path | Description |
|----------|------|-------------|
| Verification Report | `docs/verification/verification-report.md` | The primary deliverable: the complete, structured record of all verification categories executed, evidence collected, findings made, and the final outcome determination with full rationale |
| Quality Gates Record | `docs/verification/quality-gates.md` | The pass/fail record for every quality gate executed, with the criterion, the evidence collected, and the result |
| Regression Report | `docs/verification/regression-report.md` | The record of regression analysis: components assessed, regressions detected, and components cleared |
| Architecture Validation | `docs/verification/architecture-validation.md` | The record of architectural compliance assessment: module boundaries, ADR adherence, API contracts, event schemas, dependency rules |
| Verification Metadata | `docs/verification/verification-metadata.md` | Run provenance: task ID, run ID, LOOP-001 through LOOP-005 run IDs consumed, `execution_end_sha` verified, verification duration, gate counts, defect counts, outcome, confidence score |
| Verification Summary | `docs/verification/verification-summary.md` | A concise summary of the outcome, the key findings, and — if outcome is Requires Rework — a structured rework specification: finding ID, category, severity, description, and the plan step whose output it relates to |
| Loop Status | `docs/loops/core/STATUS-006.md` | Run status, outcome, and open blockers |
| Loop Skill | `docs/loops/core/SKILL-006.md` | Updated skill profile for this loop |
| Reflection | `docs/verification/reflections/REFLECTION-006-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the module catalog, API catalog, event catalog, and dependency map used as the authoritative architectural specification against which the implementation is validated.
- **LOOP-002 — Context Assembly:** Provides the verification context (test files and acceptance criteria), architecture context, and dependency context assembled for this task.
- **LOOP-003 — Task Discovery:** Provides the task classification that determines which verification categories are mandatory and which are applicable.
- **LOOP-004 — Planning:** Provides the execution plan and verification plan that define what was intended and the checkpoint criteria that must be satisfied.
- **LOOP-005 — Implementation:** Provides the implementation artefacts, change log, assumption register, and verification handoff package that are the subject of this loop's assessment.

---

## Trigger

A run is initiated by any of the following:

1. **LOOP-005 completion** — LOOP-005 has produced a completed implementation with a readiness statement and the verification loop is next in the engineering chain.
2. **Manual invocation** — An engineer explicitly triggers verification for a task with an existing completed implementation.
3. **Rework completion** — LOOP-005 has completed a rework cycle (implementing changes in response to a prior Requires Rework outcome) and the revised implementation is ready for re-verification.
4. **GATE-1 resolution** — A prior run was halted at GATE-1; the blocking condition has been resolved and verification resumes.

Trigger source, task ID, LOOP-005 run ID, and timestamp must be recorded in `STATUS-006.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `STATUS-005.md` records `status: completed` for the selected task | Read STATUS-005; assert status and task ID |
| PRE-2 | `execution_end_sha` in `implementation-metadata.md` matches current HEAD SHA | Compare stored SHA against `git rev-parse HEAD` |
| PRE-3 | `implementation-summary.md` contains a readiness statement signed by `IMPL-CHECKER` | Parse summary; assert readiness statement section present and non-empty |
| PRE-4 | No concurrent LOOP-006 run is active for the same task | Read `STATUS-006.md`; assert no `status: running` for this task |
| PRE-5 | The executing agent has no write access to repository source files | Verify agent execution profile; fail if source write access is detected |
| PRE-6 | All required LOOP-004 planning artefacts are readable | Verify existence and readability of all required planning files |
| PRE-7 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist |
| PRE-8 | `docs/loops/shared/verification-standards.md` is readable | File must exist |

PRE-2 failure (repository state has changed since implementation) triggers GATE-1. PRE-5 failure (source write access exists) is a hard abort — the loop cannot run in a configuration that would allow it to modify what it is meant to evaluate. All other precondition failures produce `precondition_failed` and halt.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository source files | Read | All files listed in `change-log.md` and all files in modules named in `modified-components.md` | Filesystem read permissions only — no write access | Read-only; no modification under any circumstance | N/A | Yes |
| `docs/implementation/` directory | Read | All LOOP-005 output files | Same as above; read-only | Read-only | N/A | Yes |
| `docs/planning/` directory | Read | All LOOP-004 output files | Read-only | Read-only | N/A | Yes |
| `docs/architecture/` directory | Read | All LOOP-001 output files | Read-only | Read-only | N/A | Yes |
| `docs/context/` directory | Read | All LOOP-002 output files | Read-only | Read-only | N/A | Yes |
| Build tooling | Execute (read-only execution) | Build and test runner commands as declared in `SKILL-001.md` and the verification plan | Same filesystem permissions as executing agent | Test execution is isolated to the verification environment; no deployment or publish operations | N/A | Yes — tests are repeatable |
| `docs/verification/` directory | Write | All verification output artefacts | Same as executing agent | Confined to this directory; no writes elsewhere | `git checkout docs/verification/` | Yes |
| `docs/loops/core/STATUS-006.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/STATUS-006.md` | Yes |
| `docs/loops/core/SKILL-006.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-006.md` | Yes |

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/core/LOOP-006-Verification.md` — this document
3. `docs/loops/core/STATUS-006.md` — prior run state (if it exists)
4. `docs/implementation/implementation-summary.md` — the handoff package primary document
5. `docs/implementation/assumptions.md` — implementation assumptions requiring verification
6. `docs/planning/execution-plan.md` — the authoritative statement of intended changes
7. `docs/planning/verification-plan.md` — the checkpoint-by-checkpoint criteria
8. `docs/context/verification-context.md` — test files and acceptance criteria
9. `docs/loops/shared/verification-standards.md` — evidence level requirements
10. `docs/loops/shared/coding-standards.md` — standards applied to code evaluation
11. `docs/loops/core/SKILL-001.md` — repository toolchain (test runner, linter, build commands)
12. All accepted ADRs applying to the task's affected modules
13. `.loop-006.yml` — verification configuration (if it exists)

---

## Verification Principles

These principles govern every evaluation judgement made by this loop. A finding that violates any principle is invalid and must be withdrawn and re-evaluated.

### Principle 1 — Independence

The verifying agent must not be the implementing agent for the changes under review. An agent that produced a change cannot independently evaluate it. If the same agent is presented with both roles, LOOP-006 must halt with `precondition_failed` until a different agent is assigned to verification.

### Principle 2 — Objective Evidence Over Opinion

Every finding must be supported by specific, reproducible evidence: a test output, a file diff, a lint report, a metric reading, a schema comparison, or a quoted text discrepancy. A finding unsupported by cited evidence is not a finding — it is an opinion. Opinions are not recorded as findings. They may be recorded as unresolved questions that trigger Requires Human Review.

### Principle 3 — Deterministic Validation

Given the same repository state and the same verification plan, two independent verification runs must produce the same set of findings. Any verification method whose output depends on timing, ordering, or non-deterministic test behaviour must be flagged as non-deterministic and its findings must be corroborated by a second deterministic method before they are recorded.

### Principle 4 — Reproducibility

Every verification check must be describable as a procedure that a second agent could repeat and expect to produce equivalent results. The procedure must be recorded in `quality-gates.md` with sufficient detail that reproduction is possible without access to this run's agent.

### Principle 5 — No Modification

The verification loop does not modify repository files. If a finding reveals that a file must be changed to satisfy a criterion, the finding is recorded in the verification report and the outcome is set to Requires Rework. The verifying agent does not make the change. This principle has no exceptions.

### Principle 6 — Complete Auditability

Every verification check executed, including checks that pass, is recorded with its criterion, method, evidence, result, and the agent that performed it. Passing checks are as important as failing ones: they constitute the body of positive evidence that supports an Accepted outcome.

### Principle 7 — Scope Fidelity

Verification is scoped to the changes declared in `change-log.md` and `modified-components.md`. The loop does not verify the entire repository. It does not flag pre-existing issues in unmodified code as defects in the current implementation, unless those issues are in components directly exercised by the changes and the issues constitute regressions introduced by the current change.

### Principle 8 — Outcome Neutrality

The verification loop has no preferred outcome. An Accepted determination is not better than a Requires Rework determination from a process quality perspective — both are correct outcomes if they accurately reflect the evidence. The loop is not incentivised to pass implementations or to fail them; its incentive is evidence accuracy.

---

## Verification Categories

The following twelve categories are evaluated for every run. Applicable categories are determined by task type (from `docs/tasks/task-catalog.md`) and by the contents of `modified-components.md`. Each category produces a structured category report within `quality-gates.md`.

### Category 1 — Implementation Completeness

**Purpose:** Confirm that the implementation produced exactly the changes declared in the execution plan — no more, no fewer.  
**Mandatory for:** All task types.  
**Evidence sources:** `change-log.md`, `execution-plan.md`, `implementation-metadata.md`.  
**Criteria:**
- Every plan step with `status: completed` in `STATUS-005.md` has a corresponding entry in `change-log.md`.
- Every file listed as `files_modified` in a completed step appears in `change-log.md`.
- No file appears in `change-log.md` that is not listed as `files_modified` in a completed step (plan authority).
- Steps marked as deferred are documented in `implementation-summary.md` as outstanding limitations.
- The implementation `execution_end_sha` matches the current HEAD SHA.

### Category 2 — Functional Correctness

**Purpose:** Confirm that the implementation satisfies the acceptance criteria declared in the verification plan and the test files in the verification context.  
**Mandatory for:** All task types except `DOCS`.  
**Evidence sources:** `docs/context/verification-context.md`, `docs/planning/verification-plan.md`, test execution outputs.  
**Criteria:**
- All unit tests in the verification context that cover the modified components pass.
- All integration tests for the modified module boundaries pass.
- All contract tests for modified APIs and events pass.
- All acceptance criteria declared in the task record are demonstrably satisfied by the implementation.
- Test coverage for modified source files meets or exceeds the threshold declared in `.loop-006.yml` (default: existing coverage must not decrease; new source files must have at least one test).

### Category 3 — Architectural Compliance

**Purpose:** Confirm that the implementation respects the module boundaries, dependency rules, and structural constraints defined in the LOOP-001 architecture knowledge base and the applicable ADRs.  
**Mandatory for:** All task types.  
**Evidence sources:** `docs/architecture/module-catalog.md`, `docs/architecture/dependency-map.md`, `docs/adr/`, `change-log.md`.  
**Criteria:**
- No modified file crosses a module boundary it is not authorised to cross (as declared in the module catalog).
- No new dependency is introduced between modules not declared in the dependency map, unless the plan explicitly authorised the new dependency edge and GATE-1 was cleared.
- All accepted ADRs applicable to the affected modules are respected by the implementation.
- No ADR was overridden without a recorded override rationale in `implementation-summary.md`.

### Category 4 — Code Quality

**Purpose:** Confirm that all modified source files comply with the coding standards and quality rules applicable to the repository.  
**Mandatory for:** Task types that modify source files: `FEAT`, `BUG`, `RFCT`, `ARCH`, `SEC`, `PERF`, `DEP`.  
**Evidence sources:** `docs/loops/shared/coding-standards.md`, `SKILL-001.md`, linting and formatting tool outputs.  
**Criteria:**
- All modified source files pass the linting rules declared in `SKILL-001.md` with no suppressed violations that are not declared in the plan.
- All modified source files conform to the formatting rules applicable to their language.
- No modified file contains dead code paths that are neither tested nor documented as intentional placeholders.
- No inline linting suppression comment was introduced unless declared in the plan with a documented reason.

### Category 5 — Documentation Consistency

**Purpose:** Confirm that all documentation changes are consistent with the code changes and that all required documentation updates have been made.  
**Mandatory for:** All task types.  
**Evidence sources:** `change-log.md`, `docs/architecture/api-catalog.md`, `docs/architecture/event-catalog.md`, `modified-components.md`, `implementation-summary.md`.  
**Criteria:**
- Every API modified in `modified-components.md` has a corresponding update in the API catalog or a documented gap in `implementation-summary.md`.
- Every event schema modified has a corresponding update in the event catalog or a documented gap.
- Every module whose README was required to be updated by the plan has a corresponding change in `change-log.md`.
- No documentation file that was not in the plan's scope has been modified.
- All `[synthesised]` annotations in LOOP-002 context that were carried forward into documentation are accurate with respect to the implementation.

### Category 6 — Configuration Validation

**Purpose:** Confirm that configuration changes are consistent, do not introduce secret values into tracked files, and apply uniformly across all declared environment targets.  
**Mandatory for:** Task types that modify configuration: `FEAT`, `BUG`, `INFRA`, `SEC`, `DEP`, `CONF`.  
**Evidence sources:** `change-log.md`, modified configuration files.  
**Criteria:**
- No modified configuration file contains literal secret values (API keys, passwords, certificates, tokens).
- Configuration changes that affect multiple environment targets are applied consistently across all declared targets.
- No feature flag introduced in this implementation lacks a corresponding removal task or a documented lifetime.
- All configuration keys introduced follow the naming conventions declared in `coding-standards.md`.

### Category 7 — Build Validation

**Purpose:** Confirm that the repository builds successfully with the changes applied.  
**Mandatory for:** All task types that modify source files, build files, or dependency declarations.  
**Evidence sources:** Build tool output (as declared in `SKILL-001.md`).  
**Criteria:**
- The repository builds successfully from a clean state with no errors.
- All compilation warnings introduced by the implementation are documented in `verification-report.md`. Compilation warnings are not automatically findings, but undocumented warnings are.
- No previously passing build target fails after the changes.
- New build targets introduced by the plan exist and build successfully.

### Category 8 — Test Validation

**Purpose:** Confirm that the test suite is internally consistent: new tests are valid (they fail before the fix, pass after it), and no tests have been incorrectly modified to produce passing results.  
**Mandatory for:** All task types that include test-writing steps.  
**Evidence sources:** `change-log.md`, modified test files, test execution outputs.  
**Criteria:**
- Every test introduced by the plan targets the capability declared in the plan step that introduced it.
- For `BUG` task type: the new regression test fails against the pre-implementation state (verified by applying the step rollback and running the test) and passes against the implementation.
- No existing test has been modified to reduce its coverage of the affected functionality.
- No existing test has been marked as skipped, ignored, or pending unless the plan explicitly declared this and the reason is documented.

### Category 9 — Dependency Validation

**Purpose:** Confirm that dependency changes are safe, consistent, and do not introduce licence, security, or compatibility issues.  
**Mandatory for:** Task types `DEP` and any task that introduces new external dependencies.  
**Evidence sources:** `change-log.md`, build files, dependency map, `docs/architecture/dependency-map.md`.  
**Criteria:**
- All new dependencies are declared in the updated dependency map.
- No module pins a version that conflicts with another module's version requirement for the same dependency.
- All new dependencies have a recorded licence classification in `change-log.md`.
- No dependency update removes a version pin that was declared as required in the dependency map.
- For security-motivated updates: the updated version resolves the declared vulnerability.

### Category 10 — API Compatibility

**Purpose:** Confirm that API changes preserve backward compatibility where required and that breaking changes were explicitly approved.  
**Mandatory for:** Task types `FEAT`, `BUG`, `API-CHANGE`, and any task that modifies files in `api-catalog.md` entries.  
**Evidence sources:** `docs/architecture/api-catalog.md`, `docs/context/dependency-context.md`, modified API definition files, contract test outputs.  
**Criteria:**
- All public API endpoints that existed before the implementation still exist and accept their prior request signatures.
- All additive changes (new optional fields, new endpoints) are backward-compatible.
- Breaking changes are present only where the plan declared them, and the corresponding GATE-1 approval record exists in `STATUS-005.md`.
- All consumer modules that depend on modified APIs have been updated or have a documented migration step in the plan.
- Contract tests for all modified APIs pass.

### Category 11 — Security Validation

**Purpose:** Confirm that security-sensitive changes do not weaken existing controls, introduce new vulnerabilities, or violate tenant isolation.  
**Mandatory for:** Task types `SEC`, `ARCH`, and any task that modifies authentication, authorisation, tenant isolation, cryptographic, or access control code.  
**Evidence sources:** Modified security files, `docs/architecture/module-catalog.md` (security boundary declarations), security test outputs.  
**Criteria:**
- All authentication paths that existed before the implementation still reject unauthenticated requests.
- All authorisation checks that existed before the implementation still enforce their declared permissions.
- Tenant isolation is preserved: no data path exists through which one tenant can access another tenant's data as a result of the implementation.
- No cryptographic primitive has been replaced with a weaker one without an explicit plan step and GATE-1 approval.
- All security controls introduced by the plan are present in the implementation and are tested.
- No new input path exists that is not validated against declared input constraints.

### Category 12 — Performance Validation

**Purpose:** Confirm that the implementation does not introduce measurable performance regressions in the affected modules.  
**Mandatory for:** Task type `PERF`. Applicable (not mandatory) for task types that modify data-path code or database queries.  
**Evidence sources:** Performance test outputs, query analysis outputs, as declared in `.loop-006.yml`.  
**Criteria (when applicable):**
- Response time for affected endpoints has not increased by more than the threshold declared in `.loop-006.yml` (default: 10% above pre-implementation baseline).
- Database query plans for modified queries have not regressed (no full-table scan introduced where an indexed scan existed).
- Memory allocations in modified hot paths have not increased by more than the declared threshold.
- All criteria are measured against the baseline declared in the verification plan.
- If no performance baseline exists, this category is recorded as `not_established` rather than passed or failed; this is not a defect but a finding.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `VERIF-AGENT` | Checker | Steps 1–9: loads all inputs, executes all verification categories, collects evidence, detects regressions, validates documentation and architecture, produces the verification report | Filesystem read, test runner execution (read-only), build tooling (read-only), linting tooling (read-only) | Reports to GATE-1 and GATE-2; must not have been IMPL-EXECUTOR in the current task |
| `OUTCOME-AGENT` | Checker | Step 10: independently reviews the verification report and determines the outcome | Filesystem read, cross-reference of findings against criteria | Independent of VERIF-AGENT; must not have been IMPL-EXECUTOR or VERIF-AGENT in the current task |
| `STATUS-WRITER` | Maker | Steps 11–12: updates STATUS-006.md and SKILL-006.md | Filesystem write to `docs/loops/core/` only | None |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews security findings, architectural ambiguities, incomplete evidence, and uncertain outcomes | Human judgment | Sole authority to approve or deny GATE-1 and to determine the outcome when Requires Human Review is declared |

`VERIF-AGENT`, `OUTCOME-AGENT`, and `IMPL-EXECUTOR` (from LOOP-005) must all be distinct agents. The Maker/Checker pattern applies: the agent that implemented the changes cannot verify them, and the agent that collected the evidence cannot determine the outcome from that evidence without an independent review step.

---

## Workflow

### Step 1 — Load Implementation Outputs

**Agent:** `VERIF-AGENT`  
**Inputs:** All LOOP-005 output files, `STATUS-005.md`  
**Outputs:** In-memory implementation evidence base  

Load all LOOP-005 output files. Verify input completeness: every file listed in the Inputs table must be present, readable, and non-empty. Record any missing or unreadable input file immediately as an evidence gap in `STATUS-006.md`. A missing required input that cannot be recovered triggers GATE-1.

Index the implementation evidence base by: step ID, file path, module, component type (API, event, schema, source, test, config, documentation, build, infrastructure). This index is the foundation for scoping all subsequent verification checks.

Record the `execution_end_sha` from `implementation-metadata.md`. Confirm it matches the current HEAD SHA. If it does not match, flag `repository_state_diverged = true` and trigger GATE-1 before proceeding.

Load the rework specification from any prior verification run for this task (if this is a re-verification after a Requires Rework outcome). Index the prior findings by finding ID; each prior finding must be either resolved or explicitly carried forward in this run.

---

### Step 2 — Load Approved Execution Plan

**Agent:** `VERIF-AGENT`  
**Inputs:** All LOOP-004 planning artefacts  
**Outputs:** In-memory verification standard  

Load the execution plan and verification plan. The verification plan is the primary standard document: it declares, for each checkpoint, the criterion, the method, the level (automated/checker/human), the blocking flag, and the rollback trigger. Every checkpoint in the verification plan must be evaluated in this run; no checkpoint may be skipped.

Reconcile the verification plan checkpoints against the implementation's completed steps. For each completed step, locate its corresponding checkpoint. For each deferred step, confirm that its checkpoint is recorded as `not_reached` rather than `passed` or `failed`. Mismatches between the step completion record and the checkpoint coverage are completeness findings (Category 1).

Load `implementation-summary.md` and read the "Do not verify" section. Every item listed there must be excluded from defect reporting. The exclusion list and its rationale must be recorded in `verification-report.md`.

---

### Step 3 — Validate Implementation Completeness

**Agent:** `VERIF-AGENT`  
**Inputs:** `change-log.md`, `execution-plan.md`, `implementation-metadata.md`, `STATUS-005.md`  
**Outputs:** Category 1 report (Implementation Completeness)  

Execute all Category 1 criteria. For each criterion:

1. State the criterion verbatim.
2. Execute the check.
3. Record the evidence: the specific file path, field value, count comparison, or diff excerpt that supports the result.
4. Record the result: `passed`, `failed`, or `not_applicable`.
5. If `failed`: assign a finding ID (`FIND-NNN`), record severity (`Critical`, `High`, `Medium`, `Low`), describe the discrepancy with specific evidence, and identify the plan step whose output the finding relates to.

Completeness failures in Category 1 are always `High` or `Critical` severity because they indicate either that implementation work was not done or that unauthorised work was done. Both conditions must be resolved before an Accepted outcome is possible.

---

### Step 4 — Execute Verification Categories

**Agent:** `VERIF-AGENT`  
**Inputs:** All evidence sources declared for each applicable category  
**Outputs:** Category reports for Categories 2–12 within `quality-gates.md`  

Execute each applicable verification category in sequence. For each category:

**Determine applicability:** Cross-reference the task type from `task-catalog.md` and the components from `modified-components.md` against the category's mandatory and applicable conditions. Record the applicability determination with its rationale. A category determined non-applicable must be recorded as `not_applicable` with rationale — it may not be silently omitted.

**Execute checks:** For each criterion within the category, follow the same five-step pattern as Step 3 (state criterion, execute, record evidence, record result, assign finding if failed).

**Test execution (Categories 2, 7, 8):** When a criterion requires test execution, execute the tests using the commands declared in `SKILL-001.md` and the verification plan. Record: the command executed, the test runner version, the execution timestamp, the total test count, the passed count, the failed count, the skipped count, and the full list of failing test names with their failure messages. Test execution is read-only; the agent does not modify test files to make tests pass.

**Non-deterministic check handling:** If a check produces inconsistent results across two executions (different result for the same criterion without any repository change), record the check as `non_deterministic`. Non-deterministic results cannot support either a passed or failed finding — they generate an `uncertain` evidence flag and are recorded as contributing to a Requires Human Review outcome.

**Severity taxonomy for findings:**

| Severity | Definition |
|----------|------------|
| `Critical` | The implementation breaks a declared contract, violates a security boundary, or causes data loss. Requires Rework is mandatory. |
| `High` | The implementation does not satisfy a mandatory criterion or introduces a regression. Requires Rework is the expected outcome. |
| `Medium` | The implementation partially satisfies a criterion or introduces a documentation gap. Requires Rework or Requires Human Review depending on nature. |
| `Low` | The implementation has a minor deviation from a non-mandatory criterion. May be accepted with documented limitation or may require Rework. Human Review recommended. |
| `Informational` | An observation that does not constitute a finding but is relevant for LOOP-007 (Reflection). Not a defect. |

---

### Step 5 — Record Evidence

**Agent:** `VERIF-AGENT`  
**Inputs:** All check results from Steps 3–4  
**Outputs:** Populated `quality-gates.md`; populated `verification-report.md` (findings section)  

For every check executed across all categories, write a quality gate record to `quality-gates.md`. The record must be complete enough to be reproduced by a second agent without access to this run's agent instance.

**Quality gate record format:**

| Field | Content |
|-------|---------|
| `gate_id` | `GATE-CAT-NNN` (category code + sequential number) |
| `category` | Verification category number and name |
| `criterion` | The full text of the criterion being evaluated |
| `method` | The specific procedure used to evaluate it |
| `evidence` | The specific artefact, output excerpt, or observation supporting the result |
| `result` | `passed` \| `failed` \| `not_applicable` \| `not_reached` \| `non_deterministic` |
| `finding_id` | `FIND-NNN` if `failed`; blank otherwise |
| `finding_severity` | `Critical` / `High` / `Medium` / `Low` / `Informational` if applicable |

The finding register in `verification-report.md` contains one entry per `FIND-NNN`, with full detail: gate ID that produced the finding, plan step the finding relates to, severity, description with evidence citation, and the remediation scope (what must change to resolve the finding — described as what, not how).

Findings are not remediations. The verification report describes the gap between what the plan declared and what the implementation produced. It does not prescribe implementation solutions. The phrase "to fix this, change X to Y" must never appear in a verification finding.

---

### Step 6 — Detect Regressions

**Agent:** `VERIF-AGENT`  
**Inputs:** Test execution outputs from Category 2 and 8, `modified-components.md`, `docs/architecture/dependency-map.md`  
**Outputs:** `regression-report.md`  

Regression detection extends beyond the directly modified components to their declared consumers and dependencies. A regression is a degradation in a component that was passing before the implementation and is failing after it, caused by the implementation rather than by a pre-existing defect.

**Scope determination:** From `dependency-map.md`, identify all modules that declare a dependency on any modified module. These are the regression candidates. From the verification context, identify which test files cover these candidate modules. Execute the relevant test subsets.

**Regression attribution:** A failed test in a candidate module is a regression only if:
1. The test was passing before the implementation (verified by checking the test against the `execution_start_sha` state — a read-only checkout of the prior state).
2. The failure is caused by a change in the modified module's behaviour or interface.

A failed test that was already failing before the implementation is a pre-existing defect, not a regression. It is recorded as `Informational` in the regression report.

**Regression report structure:** For each candidate module: the module ID, the count of tests executed, the count that passed, the count that failed, the count that were pre-existing failures, and the count of attributed regressions. For each attributed regression: the test name, the failure message, and the evidence that attributes it to the current implementation.

---

### Step 7 — Validate Documentation

**Agent:** `VERIF-AGENT`  
**Inputs:** `change-log.md`, `modified-components.md`, `docs/architecture/api-catalog.md`, `docs/architecture/event-catalog.md`, ADR directory  
**Outputs:** Category 5 report (Documentation Consistency); contributions to `architecture-validation.md`  

Execute all Category 5 criteria. Additionally, perform a direct consistency check between the implementation and the specification documents:

**API catalog consistency:** For each API listed in `modified-components.md`, read the current API catalog entry. Read the corresponding API definition file as modified by the implementation. Assert that the catalog entry accurately describes the current definition. Any discrepancy is a documentation finding.

**Event catalog consistency:** Same procedure for event schemas.

**ADR status consistency:** For any ADR whose status was changed by the implementation (e.g., a `proposed` ADR accepted as part of the plan), verify the ADR file reflects the status change and the acceptance decision is recorded.

**Inline documentation drift:** For modified source files that contain doc comments, annotations, or interface documentation, verify that the documentation accurately describes the modified interface or behaviour. Documentation that describes prior behaviour is a documentation finding.

---

### Step 8 — Validate Architecture

**Agent:** `VERIF-AGENT`  
**Inputs:** `change-log.md`, `modified-components.md`, `docs/architecture/module-catalog.md`, `docs/architecture/dependency-map.md`, ADR directory  
**Outputs:** `architecture-validation.md`  

Execute all Category 3 criteria. Produce `architecture-validation.md` with:

**Module boundary assessment:** For each modified file, record: the module it belongs to (from the module catalog), whether any other module's boundary was crossed, and whether any such crossing was authorised by the plan. Unauthorised boundary crossings are Critical findings.

**Dependency rule assessment:** For each new dependency relationship introduced (a modified file now imports from a module it did not import from before), verify the dependency is declared in the dependency map or was authorised by the plan. Undeclared new dependencies are High findings.

**ADR compliance matrix:** For each accepted ADR applicable to the affected modules, record: the ADR number and title, the constraint it imposes, whether the implementation complies, and the evidence. Non-compliance without a documented override is a High finding. Non-compliance with a documented override is an Informational finding (the override itself was reviewed at GATE-1 in LOOP-005).

**Interface stability assessment:** For each module that other modules depend on, confirm that its declared interfaces (the files representing its public boundary) have not changed in a way that violates the backward compatibility requirements declared in the plan.

---

### Step 9 — Produce Verification Report

**Agent:** `VERIF-AGENT`  
**Inputs:** All category reports, regression report, documentation validation, architecture validation  
**Outputs:** Draft `verification-report.md` and draft `verification-summary.md`  

Compile all evidence into the verification report. The report structure:

**Section 1 — Verification Scope:** Task ID, plan version, LOOP-005 run ID, `execution_end_sha`, verification categories executed and their applicability determinations.

**Section 2 — Checklist Coverage:** Every checkpoint from the verification plan, its gate ID, and its result. No checkpoint may be absent.

**Section 3 — Finding Register:** All `FIND-NNN` entries in severity order (Critical first). Each entry includes: finding ID, severity, category, gate ID, evidence citation, affected plan step, and remediation scope (what, not how).

**Section 4 — Regression Register:** All regressions attributed to the implementation, with evidence. Pre-existing failures listed separately.

**Section 5 — Assumption Resolution:** Status of each assumption from `implementation-summary.md`. Assumptions marked `IMPL-ASSM-NNN` are evaluated: either the verification evidence confirms the assumption was correct, or the evidence contradicts it (which is a finding in the relevant category).

**Section 6 — Exclusion Record:** All items from the "Do not verify" list in `implementation-summary.md`, confirmed as excluded, with the rationale.

**Section 7 — Evidence Index:** A cross-reference of every piece of evidence cited in the report to its source file, command output, or artefact location. This index enables independent reproduction.

**Verification confidence score (0–100):** A numeric measure of the completeness of the verification evidence. Deducted for: non-deterministic results (−10 per occurrence), not-reached checkpoints (−5 per), missing evidence for a passed criterion (−8 per), and categories recorded as not_applicable where applicability is uncertain (−3 per). The score is recorded in `verification-metadata.md`. A confidence score below 70 is a Requires Human Review trigger.

---

### Step 10 — Determine Outcome

**Agent:** `OUTCOME-AGENT`  
**Inputs:** Draft verification report from Step 9, quality gates record, confidence score  
**Outputs:** Final outcome determination; completed `verification-summary.md`  

`OUTCOME-AGENT` independently reviews the verification report and applies the outcome decision rules. This agent has not participated in collecting the evidence and assesses the report as a reader, not as the collector.

#### The Three Outcomes

**Accepted**  
All of the following must be true:
- No `Critical` findings exist.
- No `High` findings exist (or all `High` findings are for criteria declared non-blocking in the verification plan, and the non-blocking rationale is documented).
- No attributed regressions exist.
- All mandatory verification categories have been executed and are not in `not_reached` state.
- Confidence score is 70 or above.
- No gate conditions for Requires Human Review are present.

An Accepted outcome is not a declaration that the implementation is perfect. It is a declaration that the implementation satisfies all declared criteria and that the verification evidence supports that determination.

**Requires Rework**  
Any of the following is true:
- One or more `Critical` findings exist.
- One or more `High` findings exist that are declared blocking in the verification plan.
- One or more attributed regressions exist.
- Category 1 (Implementation Completeness) is not fully passed.
- Category 11 (Security Validation) has any `Critical` or `High` finding.

When the outcome is Requires Rework, `OUTCOME-AGENT` writes a structured rework specification in `verification-summary.md`:

For each finding that must be resolved:
- Finding ID and severity
- Category and gate ID
- Description of the gap (what the criterion required versus what the evidence showed)
- The plan step whose output the finding relates to
- The verification checkpoint that must pass after rework

The rework specification is the input to the next LOOP-005 run. It describes the gap; it does not prescribe the solution.

**Requires Human Review**  
Any of the following is true (and Requires Rework conditions are not present):
- Confidence score is below 70.
- One or more `non_deterministic` results were recorded in mandatory categories.
- One or more `Medium` or `Low` findings exist where the OUTCOME-AGENT cannot determine with confidence whether they constitute a rework requirement.
- A GATE-1 trigger condition is present (see gate triggers).
- A security finding exists that the verification loop cannot evaluate autonomously (the finding requires knowledge of the threat model or the deployment context that is not available in the verification inputs).
- The prior rework specification exists and a prior finding has neither been resolved nor explicitly carried forward.

When the outcome is Requires Human Review, `OUTCOME-AGENT` records in `verification-summary.md`: the specific questions the human reviewer must resolve, the evidence that the loop collected, and the options the human must choose between. The outcome determination is suspended; the human's decision is the outcome.

#### Outcome Gate Evaluation

After `OUTCOME-AGENT` determines the draft outcome, evaluate gate conditions:

- If outcome is Requires Human Review: trigger GATE-1.
- If a `Critical` finding exists in Category 11 (Security) or Category 3 (Architectural Compliance): trigger GATE-1 regardless of outcome.
- If outcome is Accepted but confidence score is between 70 and 79: trigger GATE-2.
- If outcome is Accepted but one or more `Low` findings exist: trigger GATE-2.

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

**[GATE-1 — Hard Gate: Requires Human Review, Security Finding, or Architectural Ambiguity]**

The outcome is held as provisional. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: Accepted with Reduced Confidence or Low Findings]**

The outcome is provisionally Accepted. See `## Human Approval Gates` — GATE-2.

---

### Step 11 — Update STATUS-006.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, gate outcomes, final outcome determination  
**Outputs:** Updated `docs/loops/core/STATUS-006.md`  

Record all metrics. Record gate outcomes. Record the final outcome: `accepted`, `requires_rework`, or `requires_human_review`. Record the verification confidence score. Record the finding counts by severity. Record open blockers: unresolved gate conditions, questions pending human review.

---

### Step 12 — Update SKILL-006.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Verification metrics, finding distribution, category outcomes  
**Outputs:** Updated `docs/loops/core/SKILL-006.md`  

Update the skill profile:

- Finding severity distribution by category (identifies which categories most frequently produce High/Critical findings in this repository)
- Category applicability rates by task type (identifies which categories are consistently non-applicable, candidates for configuration exclusion)
- Confidence score distribution across runs (consistently low confidence signals evidence collection gaps)
- Rework cycle rate: proportion of runs ending in Requires Rework, by task type (signals planning or implementation quality gaps for those task types)
- Non-deterministic check rate by category (signals tooling stability issues)
- Human review rate by trigger condition (signals which conditions are most frequently encountered)
- Regression detection rate: proportion of runs where attributed regressions were found (signals dependency coupling risk in the most frequently modified modules)

---

## Verification

All postconditions must be true before the run is marked `completed`. These criteria are assessed by `OUTCOME-AGENT` as part of Step 10.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Every checkpoint in the verification plan has a result (`passed`, `failed`, `not_applicable`, `not_reached`, or `non_deterministic`) in `quality-gates.md` | Count verification plan checkpoints; count gate records; assert equal |
| VER-2 | Every `FIND-NNN` entry in the finding register has a severity, a category, a gate ID, evidence citation, and a remediation scope | Inspect each finding entry; assert all fields populated |
| VER-3 | No repository source file has been modified by this loop | Compare current HEAD SHA against `execution_end_sha` from `implementation-metadata.md`; they must be equal (this loop adds no commits) |
| VER-4 | Every assumption from `implementation-summary.md` has a resolution status in the verification report | Cross-reference assumption IDs against Section 5 of verification report; assert one entry per assumption |
| VER-5 | `regression-report.md` covers all modules declared as regression candidates in Step 6 | Cross-reference candidate modules against regression report entries; assert coverage |
| VER-6 | `architecture-validation.md` contains one ADR compliance entry for every accepted ADR applicable to the affected modules | Cross-reference applicable ADRs against validation matrix; assert coverage |
| VER-7 | `verification-metadata.md` records the confidence score, outcome, and all provenance fields | Read metadata; assert all fields present |
| VER-8 | `verification-summary.md` contains a rework specification if the outcome is Requires Rework, or a human review brief if the outcome is Requires Human Review, or an acceptance statement if the outcome is Accepted | Parse summary; assert correct section for the declared outcome |
| VER-9 | `STATUS-006.md` has been updated with the current run ID, outcome, and a timestamp within 5 minutes of the current time | Read STATUS file; assert all three present |
| VER-10 | The confidence score in `verification-metadata.md` is consistent with the deduction rules declared in Step 9 | Recompute confidence from the gate record; assert within ±2 of the stored score |
| VER-11 | No finding is recorded for an item in the "Do not verify" exclusion list | Cross-reference finding register against exclusion list; assert no overlap |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/verification/reflections/REFLECTION-006-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Outcome Summary:** the final outcome, confidence score, finding count by severity, regression count, and category applicability summary
- **Finding Analysis:** for each `Critical` and `High` finding, a characterisation of the gap type (planning gap, implementation gap, specification gap, or tooling gap) — this is not attribution of blame but classification for SKILL-006 calibration
- **Evidence Quality Assessment:** which categories produced the strongest evidence (test outputs, direct diffs) versus which produced uncertain evidence (inferred from structure, extrapolated from partial data) and why
- **Non-Determinism Record:** all `non_deterministic` results, the checks involved, and whether the cause was identified
- **Gate Narrative:** for each gate that fired, the trigger, the evidence presented, and the human decision or auto-proceed outcome

---

## Human Approval Gates

### GATE-1 — Hard Gate: Requires Human Review, Critical Security/Architecture Finding, or Repository State Divergence

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 10 (outcome determination), or immediately at Step 1 if `repository_state_diverged = true` |
| Artefact Under Review | Verification report, verification summary, quality gates record, specific finding or uncertainty that triggered the gate |
| Approver | Principal Engineer or Architecture Owner; Security Lead for Category 11 triggers; Quality Lead for outcome uncertainty triggers |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | The provisional outcome stands as `requires_human_review`; the loop produces its full output artefacts with the human decision field left open; the engineering chain is paused until the human decision is recorded |
| Audit Trail | Human decision recorded in `STATUS-006.md` under `gate_outcomes.GATE-1`; reviewer name, role, timestamp, the specific question resolved, and the decision recorded |

**Fires when:**
- Outcome is Requires Human Review
- A `Critical` finding exists in Category 11 (Security) or Category 3 (Architectural Compliance)
- `repository_state_diverged = true` (HEAD SHA does not match `execution_end_sha`)
- A required input file is missing or unreadable and cannot be recovered
- `OUTCOME-AGENT` cannot determine whether a finding constitutes a rework requirement (ambiguous criterion)
- The confidence score is below 50 (severely incomplete evidence)
- One or more `non_deterministic` results in mandatory categories and no corroborating deterministic evidence exists
- `IMPL-CHECKER` readiness statement is absent from `implementation-summary.md`

**Reviewer guidance:** The GATE-1 brief in `verification-summary.md` presents the specific question or finding requiring resolution. The reviewer must provide one of the following responses, recorded verbatim in `STATUS-006.md`:
- "I confirm the outcome is Accepted" — with rationale addressing each open question
- "I confirm the outcome is Requires Rework" — with specification of which findings require resolution
- "I confirm the finding [FIND-NNN] is a known limitation and is accepted" — with documented rationale
- "I require further investigation: [specific investigation request]"

---

### GATE-2 — Soft Gate: Accepted with Reduced Confidence or Low Findings

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 10, before `STATUS-006.md` is finalised |
| Artefact Under Review | Verification report, quality gates record with Low findings, confidence score |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-006.yml`; defaults to writing a summary to `STATUS-006.md` |
| Timeout | 4 hours from notification timestamp |
| Auto-Proceed Action | Outcome is finalised as Accepted with the Low findings and confidence score recorded as caveats; `soft_gate_auto_proceeded: true` in `STATUS-006.md` |
| Audit Trail | Notification timestamp and outcome recorded under `gate_outcomes.GATE-2` |

**Fires when (and GATE-1 did not also fire):**
- Outcome is Accepted and confidence score is between 70 and 79
- Outcome is Accepted and one or more `Low` findings exist (findings that do not independently require rework but warrant notification)

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-006.md`. The executing agent checks `STATUS-006.md` at the start of each step. On emergency stop: test execution is not started for any category not yet reached; a partial verification report is produced with all evidence collected up to the point of stop; the outcome is set to `Requires Human Review` with the reason `emergency_stop`; a partial Reflection is produced.

---

## Failure Recovery

### FR-1 — Missing Verification Evidence

**Detection:** A required input file is absent, unreadable, or does not contain the expected data fields.  
**Immediate Action:** Record the gap in `STATUS-006.md`. Record a `not_reached` result for all gate records that depend on the missing evidence.  
**Recovery:** If the missing evidence is from LOOP-005 (e.g., `change-log.md` is incomplete): record a Category 1 High finding — the handoff package is incomplete. The outcome is Requires Rework. If the missing evidence is from a verification tool failure (FR-5): apply that procedure.  
**Reporting:** The evidence gap is recorded in the Evidence Index of `verification-report.md` as missing. The confidence score is reduced accordingly. GATE-1 fires if the gap affects a mandatory category.

### FR-2 — Incomplete Test Execution

**Detection:** The test runner exits with a non-zero status not attributable to test failures (infrastructure failure, timeout, out-of-memory), or fewer tests are executed than the verification context declares.  
**Immediate Action:** Record the execution failure. Do not treat it as a test failure finding — distinguish between a test failing and a test runner failing.  
**Recovery:** Attempt one re-execution. If the second execution also fails non-deterministically, record the affected checks as `non_deterministic`. Reduce confidence score. If the failure is in a mandatory category, trigger GATE-1.  
**Reporting:** The test runner failure is recorded in the Evidence Index with the command, exit code, and output excerpt.

### FR-3 — Conflicting Verification Results

**Detection:** Two checks within the same category produce contradictory results (e.g., a unit test passes for a function but a contract test fails for the same function's interface).  
**Immediate Action:** Record both results. Do not resolve the conflict by choosing the more convenient result.  
**Recovery:** Apply the higher-severity finding: if one result is `passed` and the other `failed`, the category is `failed`. Record the conflict in `verification-report.md` with both evidence citations. The conflict contributes to the Requires Human Review outcome trigger if the OUTCOME-AGENT cannot determine which finding is authoritative.  
**Reporting:** Conflicts are recorded in the verification report under Section 3 with a conflict annotation.

### FR-4 — Missing Documentation

**Detection:** Category 5 check determines that a documentation obligation exists (an API was modified, a README update was required) but the corresponding documentation file does not appear in `change-log.md` and has not been updated.  
**Immediate Action:** Record a Category 5 finding of severity Medium or High (High if the missing documentation is for a public API or event schema).  
**Recovery:** Record the finding in the rework specification if outcome is Requires Rework. Do not update the documentation — that is LOOP-005's responsibility after rework.  
**Reporting:** The finding is recorded with the specific documentation file path and the API or component it should describe.

### FR-5 — Verification Tool Failure

**Detection:** A tooling dependency (linter, formatter, build system, test runner) fails to execute due to an environment issue rather than a code issue.  
**Immediate Action:** Record the tool failure. Do not record a finding against the implementation for a tool failure.  
**Recovery:** Record the affected category as `tool_failure` with the tool name, version, command, and error output. Apply one retry. If the retry fails, record all dependent checks as `not_reached`. If a mandatory category is `not_reached` due to tool failure, trigger GATE-1.  
**Reporting:** The tool failure is recorded in the Evidence Index and reduces the confidence score. A tool failure is not a finding against the implementation.

### FR-6 — Repository Inconsistency

**Detection:** During architecture validation or regression detection, a file that should exist according to the module catalog does not exist, or a module boundary in the catalog does not match the observed directory structure.  
**Immediate Action:** Record the inconsistency as an `Informational` finding against Category 3 (Architectural Compliance). It is a LOOP-001 discovery gap, not necessarily an implementation defect.  
**Recovery:** Confirm whether the inconsistency pre-dates the implementation by checking the `execution_start_sha` state. If it pre-dates the implementation: record as pre-existing and exclude from findings. If it was introduced by the implementation: escalate to a `High` finding in Category 3 and include in the rework specification.  
**Reporting:** The inconsistency is recorded with its attribution (pre-existing or introduced) and the evidence supporting that attribution.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-006.md` at Step 11.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of loop steps completed (of 12) |
| `run.steps_total` | 12 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-11 criteria passed |
| `verification.level1.fail` | Count of VER-1 through VER-11 criteria failed |
| `reflection.produced` | Boolean |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `verif.task_id` | Task being verified |
| `verif.plan_version` | LOOP-004 plan version consumed |
| `verif.outcome` | `accepted` \| `requires_rework` \| `requires_human_review` |
| `verif.confidence_score` | Verification confidence score (0–100) |
| `verif.categories_executed` | Count of categories with result other than `not_applicable` |
| `verif.categories_not_applicable` | Count of categories determined non-applicable |
| `verif.gates_total` | Total quality gate records in `quality-gates.md` |
| `verif.gates_passed` | Gates with result `passed` |
| `verif.gates_failed` | Gates with result `failed` |
| `verif.gates_not_reached` | Gates with result `not_reached` |
| `verif.gates_non_deterministic` | Gates with result `non_deterministic` |
| `verif.findings_critical` | Count of `Critical` findings |
| `verif.findings_high` | Count of `High` findings |
| `verif.findings_medium` | Count of `Medium` findings |
| `verif.findings_low` | Count of `Low` findings |
| `verif.findings_informational` | Count of `Informational` findings |
| `verif.regressions_attributed` | Count of regressions attributed to the implementation |
| `verif.regressions_pre_existing` | Count of test failures found to be pre-existing |
| `verif.assumptions_verified_correct` | Count of implementation assumptions confirmed true |
| `verif.assumptions_contradicted` | Count of implementation assumptions found false |
| `verif.assumptions_unresolvable` | Count of assumptions not encountered in evidence |
| `verif.documentation_discrepancies` | Count of documentation consistency findings |
| `verif.architectural_violations` | Count of Category 3 High or Critical findings |
| `verif.rework_cycle_number` | Number of times this task has been through the verify-rework cycle |
| `verif.execution_end_sha_confirmed` | Boolean — did the HEAD SHA match `execution_end_sha` |
| `verif.no_source_modifications_confirmed` | Boolean — VER-3 passed |

---

## Risks

### RISK-1 — False Positives

- **Description:** The loop reports a finding against a criterion that is in fact satisfied, causing unnecessary rework.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Verification method is overly broad; evidence collection is ambiguous; the "Do not verify" exclusion list is incomplete.
- **Control:** Principle 2 (Objective Evidence Over Opinion) requires every finding to cite specific evidence. Principle 7 (Scope Fidelity) prohibits findings outside the change scope. VER-11 enforces the exclusion list. `OUTCOME-AGENT` independently reviews findings before the outcome is determined.
- **Detection:** A rework cycle produces no code changes (the implementation was correct and the finding was spurious).
- **Response:** The false positive is recorded in LOOP-007 Reflection; the verification criterion or method is revised in the next LOOP-006 version bump.

### RISK-2 — False Negatives

- **Description:** A defect in the implementation is not detected, resulting in an Accepted outcome for a flawed implementation.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** Verification coverage is incomplete; a criterion is omitted; a regression escapes the candidate scope determination; a test does not cover the defective path.
- **Control:** VER-1 enforces 100% checkpoint coverage. Category 6 (regression detection) extends scope beyond directly modified components. The confidence score penalises incomplete coverage and triggers human review below 70.
- **Detection:** Post-acceptance defect discovery in LOOP-007, a Release loop, or production observation.
- **Response:** The escaped defect is recorded in LOOP-007; a new `BUG` task is created in LOOP-003; the verification criterion that should have caught it is identified and strengthened in SKILL-006.

### RISK-3 — Incomplete Verification Coverage

- **Description:** Not all verification categories are executed, leaving gaps in the evidence base.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Tool failures (FR-5), missing inputs (FR-1), or time constraint (`stopped` due to maximum run duration).
- **Control:** VER-1 requires every checkpoint to have a recorded result including `not_reached`. Confidence score is reduced for `not_reached` results. `not_reached` in mandatory categories triggers GATE-1.
- **Detection:** VER-1 failure.
- **Response:** FR-1 or FR-5 procedure; GATE-1 if mandatory category is affected.

### RISK-4 — Verification Bias

- **Description:** The verification loop is configured or influenced to favour passing outcomes, undermining its independence.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** The verifying agent is the same as the implementing agent; the verification criteria are adjusted after implementation to match the implementation; the "Do not verify" list is used to exclude legitimate verification targets.
- **Control:** PRE-5 prohibits source write access. Agent identity separation is enforced at the loop level (VERIF-AGENT ≠ IMPL-EXECUTOR). VER-11 cross-references the exclusion list against findings to detect misuse. Principle 8 (Outcome Neutrality) is declaratively part of the loop standard.
- **Detection:** Audit of `quality-gates.md` showing criteria that were modified between the verification plan and the gate record.
- **Response:** Any discovered verification bias is a governance event; it is escalated via GATE-1 and recorded in `STATUS-006.md` as a process failure.

### RISK-5 — Missing Evidence

- **Description:** A criterion is marked `passed` without adequate supporting evidence, making the passing determination unverifiable.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Evidence collection step is incomplete; the evidence field in a gate record is populated with a reference rather than an actual artefact excerpt.
- **Control:** Principle 6 (Complete Auditability) requires evidence for every check. The Evidence Index in Section 7 of `verification-report.md` cross-references all evidence citations. VER-2 requires the evidence citation field in every finding record to be populated.
- **Detection:** Post-run audit of the Evidence Index finds a citation with no corresponding artefact.
- **Response:** The gate record is invalidated; the criterion is re-evaluated in a follow-up run.

### RISK-6 — Regression Escape

- **Description:** A regression in a dependent module is not detected because the regression candidate scope was too narrow.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** The dependency map is incomplete (LOOP-001 limitation); a module is implicitly coupled to the modified module through shared infrastructure not captured in declared dependencies.
- **Control:** Step 6 uses `dependency-map.md` as the scope source; any gap in the map is a LOOP-001 finding. SKILL-006.md tracks regression detection rates by module, flagging frequently-missed regression sources.
- **Detection:** LOOP-007 or Release loop discovers a regression post-acceptance.
- **Response:** New `RSRCH` task created for the hidden dependency; LOOP-001 unknowns updated; verification scope expanded in the next LOOP-006 run for that module.

### RISK-7 — Tenant Isolation Breach (Verification Perspective)

- **Description:** Category 11 (Security Validation) fails to detect a tenant isolation regression, allowing a cross-tenant data access path introduced by the implementation to pass verification.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** Tenant isolation tests do not cover the specific data path modified; the security boundary model is incomplete.
- **Control:** Category 11 criterion explicitly includes tenant isolation. All GATE-1 approvals for security-related steps in LOOP-005 are reviewed in the verification report. `Critical` Category 11 findings trigger GATE-1 unconditionally.
- **Detection:** Post-acceptance security audit or incident.
- **Response:** Immediate escalation to Security Lead; the implementation is rolled back using LOOP-004's rollback plan; the verification gap is recorded as a LOOP-006 process failure.

### RISK-8 — Non-Idempotent External Write

- **Description:** Not applicable. This loop writes only to `docs/verification/` and the loop state files. It does not write to source files, databases, or external systems.
- **Likelihood:** N/A
- **Impact:** N/A

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | All twelve verification categories have been evaluated (with an applicability determination for each) |
| SC-2 | All checkpoints in the verification plan have a result in `quality-gates.md` |
| SC-3 | All verification criteria VER-1 through VER-11 have passed |
| SC-4 | All six output artefacts listed in the Outputs table have been written |
| SC-5 | Exactly one outcome has been determined and recorded |
| SC-6 | `STATUS-006.md` has been updated with the outcome, confidence score, and final status |
| SC-7 | `SKILL-006.md` has been updated |
| SC-8 | The Reflection artefact has been written |
| SC-9 | No repository source file has been modified (VER-3 confirmed) |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-10 | Maximum run duration (2 hours) reached; partial evidence collected; outcome is `requires_human_review` |
| SC-11 | GATE-1 is active and awaiting human decision; loop suspends after writing all available evidence |
| SC-12 | PRE-4 detects a concurrent run; this instance exits without modifying any artefact |
| SC-13 | An Emergency Stop signal is received; outcome set to `requires_human_review` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Verification Artefacts:**
- [ ] `docs/verification/verification-report.md` written with all seven sections, all findings, all evidence citations, and the confidence score
- [ ] `docs/verification/quality-gates.md` written with one record per checkpoint, including passed checkpoints
- [ ] `docs/verification/regression-report.md` written covering all regression candidate modules
- [ ] `docs/verification/architecture-validation.md` written with module boundary assessment, dependency rule assessment, and ADR compliance matrix
- [ ] `docs/verification/verification-metadata.md` written with all provenance fields, outcome, and confidence score
- [ ] `docs/verification/verification-summary.md` written with the correct outcome section (acceptance statement, rework specification, or human review brief)

**Verification:**
- [ ] VER-1 through VER-11 all assessed and outcomes recorded
- [ ] VER-3 (no source modifications) confirmed
- [ ] VER-11 (exclusion list respected) confirmed

**Outcome:**
- [ ] Exactly one outcome recorded: `accepted`, `requires_rework`, or `requires_human_review`
- [ ] Outcome is consistent with the finding register and confidence score

**Gates:**
- [ ] Gate outcome recorded in `STATUS-006.md` for every gate that fired
- [ ] GATE-1 human decision recorded if applicable

**State:**
- [ ] `docs/loops/core/STATUS-006.md` updated with outcome, confidence score, finding counts, and final status
- [ ] `docs/loops/core/SKILL-006.md` updated

**Reflection:**
- [ ] `docs/verification/reflections/REFLECTION-006-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---

## Future Improvements

- **Baseline snapshot management:** Formalise the process of capturing the pre-implementation test baseline (at `execution_start_sha`) for regression attribution, so that the regression detection procedure does not require a transient checkout of the prior state during the verification run.
- **Finding deduplication across rework cycles:** Track finding IDs across multiple rework cycles for the same task, identifying which findings are persistently recurring (signals a specification ambiguity or a systematic implementation gap) versus which are resolved in each cycle.
- **Confidence score calibration:** Correlate confidence scores with post-acceptance defect discovery rates across runs to calibrate the score's predictive accuracy; adjust deduction weights if high-confidence runs produce more false negatives than expected.
- **Category parallelisation:** For repositories with long test suites, execute independent verification categories concurrently (Categories 4, 5, and 9 share no evidence dependency) with separate `VERIF-AGENT` instances, reducing total verification duration.
- **Rework specification precision:** Extend the rework specification in `verification-summary.md` to include the specific verification checkpoint that the reworked implementation must satisfy, enabling LOOP-005's rework execution to be precisely scoped without requiring a full re-run of LOOP-004.
- **Pre-verification smoke check:** Before investing the full run duration, execute a rapid completeness check (Category 1 only) in the first 5 minutes to confirm the handoff package is valid and the HEAD SHA matches. Fail fast if Category 1 is not passed rather than running all categories against an incomplete implementation.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — provides the architecture knowledge base used in Categories 3, 5, 9, and 10
- `docs/loops/core/LOOP-002-Context-Assembly.md` — provides the verification context and architecture context consumed in Steps 1–4
- `docs/loops/core/LOOP-003-Task-Discovery.md` — provides the task classification that determines category applicability
- `docs/loops/core/LOOP-004-Planning.md` — provides the execution plan and verification plan that constitute the primary verification standard
- `docs/loops/core/LOOP-005-Implementation.md` — provides the implementation artefacts and verification handoff package that are the subject of this loop
- `docs/loops/shared/verification-standards.md` — defines verification levels and evidence requirements
- `docs/loops/shared/coding-standards.md` — defines the code quality standards evaluated in Category 4
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-006 as the independent verification loop for the AI Engineering Operating System, consuming LOOP-001 through LOOP-005 outputs and producing the authoritative verification determination consumed by LOOP-007.

