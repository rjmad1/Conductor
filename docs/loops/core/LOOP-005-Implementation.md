---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-005-Implementation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Execute implementation plan through sandboxed edits and code generation.
Generalized Purpose: Execute implementation plan through sandboxed edits and code generation.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery, LOOP-004 — Planning
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-005 — Implementation

**Loop ID:** LOOP-005  
**Name:** Implementation  
**Version:** 1.0  
**Status:** Active  
**Category:** Core  
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery, LOOP-004 — Planning  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-005 executes an approved engineering plan and produces the repository changes that satisfy the plan's objective. Its responsibility is singular: implementing what has been planned. It does not plan work, does not verify correctness, does not prioritise tasks, and does not make architectural decisions. Every decision about what to build and how to sequence it has been made by LOOP-004. Every decision about whether the result is correct belongs to LOOP-006. LOOP-005 is the execution engine between them.

The loop executes one atomic implementation step at a time, records changes with full traceability to the plan, and produces a verification handoff package that gives LOOP-006 everything it needs to assess correctness independently. The loop never declares implementation success; it declares implementation completeness and passes the evidence to verification.

---

## Problem Statement

Implementation performed without a disciplined execution contract produces changes that are difficult to verify, impossible to roll back reliably, and opaque to the agents that follow. Scope expands beyond the plan. Files are modified that the plan did not authorise. Undocumented assumptions accumulate silently. Documentation diverges from code. The verification loop receives a change set it was not prepared for. Codifying implementation as a loop with declared change governance, per-step recording, and a mandatory verification handoff package resolves each of these failure modes by making them detectable before verification begins.

---

## Why This Loop Exists

Implementation is the only loop in the core chain that modifies repository source files, tests, configuration, and schemas. Every other loop reads the repository or writes planning and documentation artefacts. That asymmetry makes LOOP-005 the highest-risk loop in the chain: its mistakes are the hardest to detect, the most expensive to reverse, and the most likely to compound if uncaught. Encapsulating implementation in a loop with strict change governance, plan adherence enforcement, real-time change recording, and a mandatory handoff protocol converts a high-risk freeform activity into an auditable, repeatable engineering process.

---

## Scope

**In scope:**
- Loading and validating the LOOP-004 approved execution plan before modifying any file
- Executing atomic implementation steps from the plan, one at a time, in the declared sequence
- Modifying source code, test files, configuration files, database migration scripts, build files, infrastructure definitions, API specifications, and event schema files — only those files explicitly authorised by the plan
- Creating new files authorised by the plan
- Deleting files authorised by the plan
- Synchronising documentation (README files, inline documentation, ADR status updates) where the plan declares a documentation step
- Recording all changes with full traceability to the plan step that authorised them
- Recording all assumptions made during implementation that were not resolved before execution
- Producing the complete verification handoff package in `docs/implementation/`
- Updating `STATUS-005.md` and `SKILL-005.md`

**Out of scope:**
- Making architectural decisions (LOOP-004)
- Selecting which task to implement (LOOP-003)
- Verifying that changes are correct (LOOP-006)
- Writing tests as the primary verification mechanism — tests written by LOOP-005 are plan-prescribed test steps, not self-verification
- Modifying any file not listed in the execution plan's `files_modified` list for the current step
- Resolving architectural unknowns (those remain in LOOP-001's domain)
- Deploying changes to any environment
- Making changes to CI/CD pipeline definitions not explicitly planned
- Self-reporting implementation correctness

**Maximum run duration:** 4 hours per atomic step; 24 hours for the complete plan. If an individual step has not completed within 4 hours, the step is abandoned and the loop halts with `step_timeout`. If the complete plan has not completed within 24 hours, the loop halts with `plan_timeout`. Both produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/planning/execution-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/implementation-breakdown.md` | File | LOOP-004 output | Required |
| `docs/planning/dependency-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/verification-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/rollback-plan.md` | File | LOOP-004 output | Required |
| `docs/planning/planning-metadata.md` | File | LOOP-004 output | Required — HEAD SHA and plan version |
| `docs/loops/core/STATUS-004.md` | File | LOOP-004 | Required — plan completion status |
| `docs/context/context-package.md` | File | LOOP-002 output | Required |
| `docs/context/implementation-context.md` | File | LOOP-002 output | Required |
| `docs/context/verification-context.md` | File | LOOP-002 output | Required |
| `docs/context/context-metadata.md` | File | LOOP-002 output | Required — HEAD SHA check |
| `docs/architecture/module-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/api-catalog.md` | File | LOOP-001 output | Required |
| `docs/loops/core/SKILL-001.md` | File | LOOP-001 | Required — coding conventions and technology stack |
| `docs/loops/shared/coding-standards.md` | File | Shared | Required |
| `docs/tasks/task-catalog.md` | File | LOOP-003 output | Required — selected task record |
| Prior `docs/implementation/` artefacts | Directory | Prior run of LOOP-005 | Optional — enables resumption after interruption |
| Implementation configuration | `.loop-005.yml` at repo root | Repository | Optional |
| ADR directory | `docs/adr/` or configured equivalent | Repository | Required — all accepted ADRs |

### Input Validation

Before Step 1 begins, the loop must verify:

- `STATUS-004.md` records a completed LOOP-004 run with `status: completed` for the selected task. If LOOP-004 did not complete successfully, LOOP-005 must not begin.
- The HEAD SHA recorded in `planning-metadata.md` matches the current repository HEAD SHA. If they differ, the plan was produced against a different repository state; LOOP-004 must be re-run before LOOP-005 may proceed.
- The LOOP-002 context package HEAD SHA in `context-metadata.md` matches the current HEAD SHA. Same condition as above.
- The execution plan is syntactically complete: all mandatory step fields are populated for every step in the plan.
- No concurrent LOOP-005 run is active for the same task ID.
- The executing agent has write access to the repository and to `docs/implementation/`.

All HEAD SHA failures trigger GATE-1 rather than `precondition_failed`. The human reviewer decides whether to re-run the upstream loops or to acknowledge and proceed with the documented staleness.

---

## Outputs

All implementation artefacts are written to `docs/implementation/`. Repository source file changes are made in the repository working tree according to the plan. No output is written until at least Step 1 is complete and the first atomic step has been validated as ready to execute.

| Artefact | Path | Description |
|----------|------|-------------|
| Implementation Summary | `docs/implementation/implementation-summary.md` | The primary handoff document: a complete record of what was implemented, step by step, with planned versus actual changes, all assumptions, and the readiness statement for LOOP-006 |
| Change Log | `docs/implementation/change-log.md` | The per-step record of every file created, modified, or deleted, with the plan step ID that authorised each change and a one-sentence description of the change |
| Modified Components | `docs/implementation/modified-components.md` | The list of affected modules, services, APIs, events, schemas, and configuration areas, cross-referenced to the verification plan checkpoints that cover them |
| Implementation Metadata | `docs/implementation/implementation-metadata.md` | Run provenance: task ID, plan version, run ID, LOOP-001/002/003/004 run IDs consumed, HEAD SHA at execution start, HEAD SHA at execution end, step completion counts, assumption counts, lines added/modified/deleted, execution duration |
| Assumptions Register | `docs/implementation/assumptions.md` | All assumptions made during implementation that were not present in the LOOP-004 assumption register, with discovery step, basis, and potential impact on verification |
| Loop Status | `docs/loops/core/STATUS-005.md` | Run status, step-by-step progress, and open blockers |
| Loop Skill | `docs/loops/core/SKILL-005.md` | Updated skill profile for this loop |
| Reflection | `docs/implementation/reflections/REFLECTION-005-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the module catalog and API catalog used to validate that implementation changes respect module boundaries and do not modify undeclared APIs. LOOP-001 must be Active.
- **LOOP-002 — Context Assembly:** Provides the implementation context (selected source files) and verification context (test files and acceptance criteria) consumed during execution. Must have been assembled for the selected task with a HEAD SHA matching the current repository state.
- **LOOP-003 — Task Discovery:** Provides the selected task record and classification that governs change management expectations (e.g., API changes require GATE-1; bug fixes do not change interface contracts).
- **LOOP-004 — Planning:** Provides the execution plan that authorises every file modification. This is a hard dependency: LOOP-005 may not begin without a completed LOOP-004 plan for the selected task.

---

## Trigger

A run is initiated by any of the following:

1. **LOOP-004 completion** — LOOP-004 has produced a completed execution plan and the implementation loop is next in the engineering chain for the selected task.
2. **Manual invocation** — An engineer explicitly triggers implementation for a task with an existing approved plan.
3. **Resumption after interruption** — A prior LOOP-005 run was stopped (step timeout, plan timeout, Emergency Stop, or gate denial) and conditions have been resolved. The loop resumes from the last completed step.
4. **Post-replan execution** — LOOP-004 has produced a revised plan (replanning) and LOOP-005 must execute the delta steps.

Trigger source, task ID, plan version, and timestamp must be recorded in `STATUS-005.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | LOOP-004 `STATUS-004.md` records `status: completed` for the selected task and plan version | Read STATUS-004; assert status and task ID |
| PRE-2 | HEAD SHA in `planning-metadata.md` matches current HEAD SHA | Compare stored SHA against `git rev-parse HEAD` |
| PRE-3 | HEAD SHA in `context-metadata.md` matches current HEAD SHA | Compare stored SHA against current HEAD |
| PRE-4 | No concurrent LOOP-005 run is active for the same task ID | Read `STATUS-005.md`; assert no `status: running` entry for this task |
| PRE-5 | The executing agent has write access to repository source files | Probe write on a temporary file in the repository root; remove on success |
| PRE-6 | The executing agent has write access to `docs/implementation/` | Probe write; remove on success |
| PRE-7 | All plan step records have their mandatory fields populated | Parse execution plan; assert no empty required fields |
| PRE-8 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist |
| PRE-9 | `docs/loops/shared/coding-standards.md` is readable | File must exist; this document governs all code produced |

PRE-2 and PRE-3 failures trigger GATE-1. All other precondition failures produce `precondition_failed` status and halt without modifying any repository file.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository source files | Read-Write | Files explicitly listed in `files_modified` and `files_read` fields of authorised plan steps | Filesystem permissions of executing agent | Scoped to files declared in the plan; no file outside the plan's declared scope may be modified | `git checkout <file>` restores each file; full plan rollback via `docs/planning/rollback-plan.md` | Steps that create or update files are idempotent if re-executed against the same inputs; delete steps are not idempotent |
| `docs/implementation/` directory | Write | All implementation artefact files | Same as executing agent | Confined to this directory | `git checkout docs/implementation/` | Yes — artefacts are regenerated from step execution records |
| `docs/loops/core/STATUS-005.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/STATUS-005.md` | Yes |
| `docs/loops/core/SKILL-005.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-005.md` | Yes |

**Critical constraint:** This loop modifies repository source files. Every source file modification must be authorised by a step in the execution plan. No modification may be made to a file not listed in the current step's `files_modified` field. This constraint is enforced at Step 4 (execution) and checked at Step 5 (recording) before the next step begins.

This loop does not: call external APIs, write to databases at runtime, push to remote repositories, trigger deployments, or send notifications. Those actions belong to release and deployment loops.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/core/LOOP-005-Implementation.md` — this document
3. `docs/loops/core/STATUS-005.md` — prior run state (if it exists; enables resumption)
4. `docs/planning/execution-plan.md` — the authorised implementation sequence
5. `docs/planning/rollback-plan.md` — rollback actions for every step
6. `docs/planning/verification-plan.md` — checkpoint definitions
7. `docs/context/context-package.md` — the assembled task context
8. `docs/context/implementation-context.md` — the selected source files
9. `docs/context/verification-context.md` — test files and acceptance criteria
10. `docs/loops/shared/coding-standards.md` — the coding standards that govern all code produced
11. `docs/loops/core/SKILL-001.md` — repository technology profile (language, frameworks, conventions)
12. `docs/architecture/module-catalog.md` — module boundaries to be respected
13. All accepted ADRs that apply to the selected task's affected modules (as listed in `docs/context/architecture-context.md`)
14. `.loop-005.yml` — implementation configuration (if it exists)

The agent must not begin executing any plan step until all required context has been loaded. Loading context is a read-only act; no file is modified during this loading phase.

---

## Implementation Principles

These principles govern every modification made by this loop. A change that violates any principle is a change governance failure and must be halted, recorded, and escalated before the next step proceeds.

### Principle 1 — Plan Authority

The execution plan is the sole authority for what may be changed. A file not in the current step's `files_modified` list may not be modified for any reason, including discovered convenience, observed improvement, or apparent necessity. If a modification outside the plan's scope is discovered to be necessary, the loop halts, records the gap as an implementation blocker, and returns control to LOOP-004 for a plan revision.

### Principle 2 — Minimal Blast Radius

Each atomic step modifies the minimum set of files necessary to achieve that step's declared output. The executing agent must resist the impulse to improve adjacent code, reformat unrelated files, rename unrelated symbols, or fix visible but unplanned issues. Incidental improvements discovered during implementation are recorded as candidate tasks in `docs/implementation/assumptions.md` and fed back to LOOP-003 in the next cycle; they are not implemented in this run.

### Principle 3 — Single Responsibility per Change

Each file modification within a step serves a single, declared purpose. A file change that serves two purposes belongs to two steps. If this is discovered during execution, the loop halts and records a plan decomposition gap.

### Principle 4 — Incremental and Reviewable

Changes are made incrementally, step by step, in the planned sequence. At no point during execution is the repository left in a state where more than one step's changes are partially applied. A step is either fully applied or not applied at all. Partial step application is a recovery event (see FR-2).

### Principle 5 — Backward Compatibility Preservation

Unless the plan explicitly authorises a breaking change (and the plan's GATE-1 was approved for that change), the implementation must preserve backward compatibility. Any code path removed must be deprecated before removal with the removal scheduled in a separate future step. Any interface modified must remain callable with its prior signature unless the plan specifies the breaking change window.

### Principle 6 — Coding Standards Compliance

All code produced must comply with `docs/loops/shared/coding-standards.md` and with any repository-specific linting, formatting, or style rules identified in `SKILL-001.md`. The executing agent must not bypass a linting rule by adding an inline suppression comment unless the plan explicitly declares the suppression and records the reason.

### Principle 7 — Traceability

Every modified file carries a traceable link to the plan step that authorised the modification. This link is recorded in `change-log.md` (by step ID), not necessarily as an inline comment in the source file. The traceability requirement is satisfied by the change log record, not by source-level annotations.

### Principle 8 — Documentation Synchronisation

Any step that changes a public API, an event schema, a module's declared responsibilities, or a database schema must include a corresponding documentation update within the same step. A code change to a public API without an API catalog update is a documentation drift violation. If the plan did not include a documentation step for a change that requires documentation, the loop halts and records a plan gap (see FR-6).

### Principle 9 — Test Awareness

The executing agent is aware of the verification context: the tests that will be run by LOOP-006 against this implementation. Where the plan includes test-writing steps, those tests are written before the implementation steps they cover (in compliance with LOOP-004 Rule DO-3). Where the plan does not include test-writing steps, the agent does not write tests — that omission is visible to LOOP-006 and may cause verification to trigger a plan revision.

### Principle 10 — Assumption Declaration

When the implementation agent encounters a decision point not resolved by the plan, the architecture context, or the coding standards, it records an assumption rather than making a silent decision. Silent decisions are implementation defects. A recorded assumption is a first-class output that LOOP-006 uses to determine whether the implementation is consistent with intended behaviour.

---

## Change Management Governance

The following governance expectations apply to each change type. These are repository-agnostic engineering standards; repository-specific policies are declared in `.loop-005.yml` and take precedence where they are more restrictive.

### Source Code Changes

- Modified only within the boundaries of the affected modules declared in the plan.
- Must not introduce new external dependencies not declared in the dependency plan.
- Must comply with all accepted ADRs that apply to the affected modules.
- Must not alter the public interface of a module unless the plan explicitly authorises the interface change and GATE-1 has been cleared for it.
- Must not contain dead code paths that are neither tested nor planned for future use.

### Documentation Updates

- README files must be updated in the same step as the source change they document.
- API catalog entries must be updated in the same step as the API change they describe.
- ADR status may be updated from `proposed` to `accepted` only by a plan step explicitly designated as an ADR acceptance step.
- Inline documentation (doc comments, annotations) must be updated in the same step as the code they describe.

### Configuration Changes

- Environment-specific configuration changes must not hardcode values that belong in environment variables or secrets management.
- Configuration changes must not introduce secret values into tracked files.
- Configuration changes that affect multiple environments must be made consistently across all declared environment targets.
- Feature flags introduced by a configuration change must have a corresponding removal task recorded in `docs/implementation/assumptions.md` if no removal step exists in the current plan.

### Database Migration Changes

- Forward migration scripts must be written before application code that depends on the new schema (LOOP-004 Rule DO-2).
- Every forward migration must have a corresponding rollback migration in `docs/planning/rollback-plan.md`.
- Migrations must be idempotent where the target database engine supports it.
- Migrations that remove columns or tables used by other services must not be executed in the same step as the code change that stops using them — they require a separate step with a dual-read window.
- Migration file naming must follow the repository's declared migration file convention (from `SKILL-001.md`).

### Build Configuration Changes

- Build file changes must not alter the build outputs of modules not in the plan's scope.
- Dependency version changes must apply uniformly across all modules that declare the same dependency.
- New dependencies must be evaluated for licence compatibility and recorded in `change-log.md` with their licence classification.

### Infrastructure Definition Changes

- Changes to infrastructure definitions (Terraform, Helm, Kubernetes manifests) require GATE-1 regardless of whether they appear explicitly in the gate trigger list — infrastructure changes affect shared state outside the repository.
- Infrastructure changes must be accompanied by a corresponding rollback definition in `docs/planning/rollback-plan.md`.

### API Changes

- Any change to a public API contract (adding, modifying, or removing endpoints, request fields, or response fields) requires GATE-1.
- Additive changes to internal APIs (new optional fields, new optional endpoints) may proceed without GATE-1 if the plan's GATE-1 was cleared for the broader API change.
- Breaking changes to internal APIs require a migration step for each known consumer module in the plan.

### Event Schema Changes

- Event schema changes follow the same governance as API changes.
- Schema evolution must be backward-compatible where consumers cannot be updated simultaneously.
- New required fields on existing events require a default value or a consumer migration step before the field is made required.

### Dependency Updates

- Dependency version changes must be verified against the module catalog to confirm no other module pins a conflicting version.
- Major version updates require GATE-1 if the updated dependency is a transitive dependency of three or more modules.
- Security-motivated dependency updates carry an expedited priority but still require plan step authorisation.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `IMPL-EXECUTOR` | Maker | Steps 1–8: loads the plan, validates prerequisites, executes atomic steps one at a time, records changes, synchronises documentation, declares assumptions, prepares the verification handoff | Filesystem read-write (scoped to plan-authorised files), language tooling for syntax validation, build tooling for build verification | Reports to GATE-1 and GATE-2; Emergency Stop checked at each step boundary |
| `CHANGE-RECORDER` | Maker | Step 5 (embedded in each step): records changes to `change-log.md` immediately after each step completes | Filesystem write to `docs/implementation/` only | None — purely recording |
| `IMPL-CHECKER` | Checker | Step 8 (handoff preparation): independently reviews the change log and modified components list against the plan to confirm no unauthorised changes were made | Filesystem read, cross-reference of modified files against plan declarations | Independent of IMPL-EXECUTOR; must not have made any modification reviewed |
| `STATUS-WRITER` | Maker | Steps 9–11: updates STATUS-005.md, SKILL-005.md, and publishes the implementation summary | Filesystem write to `docs/` directories only | None |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews changes to public APIs, security controls, infrastructure definitions, and non-reversible steps before they execute or after they complete | Human judgment | Sole authority to approve or deny GATE-1 |

`IMPL-EXECUTOR` and `IMPL-CHECKER` must be separate agent instances. `IMPL-CHECKER` reviews changes after they have been made; it does not participate in making them. `CHANGE-RECORDER` is logically embedded within `IMPL-EXECUTOR`'s step cycle but is accountable as a distinct recorder — its outputs are the primary evidence for `IMPL-CHECKER`.

---

## Workflow

### Step 1 — Load Approved Execution Plan

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** All required input files listed above  
**Outputs:** In-memory plan state; resumption record if replanning  

Load the execution plan from `docs/planning/execution-plan.md`. Parse all steps into an ordered in-memory list. Verify the plan is complete: every step has a `step_id`, `title`, `change_type`, `files_modified`, `inputs`, `outputs`, `verification_checkpoint`, and `rollback_action`.

Check whether a prior LOOP-005 run exists for the same task ID and plan version by reading `STATUS-005.md`. If a prior run was stopped with one or more completed steps, record `resuming = true` and load the list of already-completed steps. Resuming skips completed steps and begins from the first incomplete step. The resumption record is written to `STATUS-005.md` immediately.

Record the git HEAD SHA at this moment in `implementation-metadata.md` as the `execution_start_sha`. This SHA is the baseline against which all authorised changes are measured.

---

### Step 2 — Validate Prerequisites

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** Plan dependency record, task dependency graph, execution plan  
**Outputs:** Prerequisite validation record  

For each `precondition-check` step at the head of the execution plan (steps of type `precondition-check` inserted by LOOP-004 Rule DO-7), verify the named prerequisite:

- If the prerequisite is a dependency task completion: check LOOP-005/006 artefacts for the dependency task ID. If not found, the dependency is not complete; record a `blocking_dependency` and halt. This is not a failure — it is an expected wait state. Write `status: blocked_on_dependency` to `STATUS-005.md`. The loop may be re-triggered when the dependency completes.
- If the prerequisite is a file existence assertion: verify the file exists and is non-empty.
- If the prerequisite is a build output existence: verify the build output is present.
- If the prerequisite is a service availability check: this loop does not make network calls; record the check as `deferred_to_loop006` and proceed.

If any prerequisite check fails (other than dependency completion, which produces a wait state rather than a failure), trigger GATE-1.

After prerequisite validation, verify that the repository has no uncommitted changes in any file listed in the plan's scope. If uncommitted changes exist in plan-scoped files and `resuming = false` (this is not a resumption), trigger GATE-1 — the repository state is inconsistent with the plan's baseline.

---

### Step 3 — Load Implementation Context

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** LOOP-002 context package, implementation context, coding standards  
**Outputs:** Indexed implementation knowledge base  

Load `docs/context/implementation-context.md` and index all selected source files by module, file type, and purpose. For each source file in the context, confirm the file exists at the declared path. Files that are in the implementation context but do not exist at their declared path are missing dependencies — record them in `STATUS-005.md` and trigger GATE-1.

Load all accepted ADRs applicable to this task's affected modules. Index each ADR's constraints for use in Steps 4 and 7.

Load `docs/loops/shared/coding-standards.md` and extract all rules that apply to the languages and frameworks in the implementation scope (from `SKILL-001.md`).

If `resuming = true`, also load the existing `change-log.md` and `assumptions.md` from the prior partial run. These are live documents that accumulate across the complete plan execution.

---

### Step 4 — Execute One Atomic Implementation Step

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** Current step record from execution plan, implementation knowledge base  
**Outputs:** Modified repository files (as declared by the step); step completion record  

This step is the core of the loop. It executes exactly one atomic step from the execution plan. Steps are executed in the declared sequence; no step may be skipped or reordered during execution.

**Before modifying any file:**

1. Read the step record in full: `step_id`, `change_type`, `files_modified`, `files_read`, `inputs`, `outputs`, `verification_checkpoint`, and `rollback_action`.
2. Verify that every file in `files_modified` is readable (or, for new files, that the parent directory is writable).
3. Check `STATUS-005.md` for an Emergency Stop signal. If present, halt immediately without modifying any file.
4. If the step is a GATE-1 trigger (see gate triggers below), notify the human reviewer and halt until approval is received. Do not modify any file before GATE-1 approval.
5. Record the step as `status: in_progress` in `STATUS-005.md`.

**During file modification:**

- Modify files strictly as declared in the step record. The change must produce the `outputs` declared in the step record. Nothing more.
- If implementing source code: apply the coding standards from Step 3. The code must be syntactically valid for the language and must not introduce suppressed linting violations unless declared in the plan.
- If implementing a test step: write the test before writing the code it tests (LOOP-004 Rule DO-3). The test must fail against the current implementation and must be designed to pass against the planned implementation.
- If implementing a migration step: follow the migration governance rules from Change Management. Write the rollback migration in the rollback plan file immediately after writing the forward migration.
- If the step is a `configure` type: apply configuration change governance. No secret values in tracked files.
- If the step is a `delete` type: verify that no other in-plan step declares the deleted file as an input before deleting.

**Deviation detection:** If, during execution, the implementation agent observes that the actual change required differs from the declared change (a file that the step declares as modified cannot be modified as described because the file's current structure differs from the plan's expectation), the agent must halt the step immediately. It records the deviation as an implementation blocker in `STATUS-005.md` and triggers GATE-1. It does not improvise a solution.

**After modifying files:**

1. Verify that all files declared in `files_modified` have been modified (for `modify`/`create` steps) or deleted (for `delete` steps).
2. Verify no files outside the `files_modified` list have been changed (compare `git diff --name-only` against the declared file list).
3. If any undeclared file has been modified, record it immediately as a change governance violation and trigger GATE-1. The undeclared modification may not stand until GATE-1 approves it.
4. Record the step as `status: completed` in `STATUS-005.md`.
5. Hand off to `CHANGE-RECORDER` (Step 5) before advancing to the next step.

Steps 4 and 5 execute as a pair for each atomic step in the plan. After Step 5 completes recording, Step 4 is re-entered for the next plan step. This continues until all steps are complete or a halt condition is reached.

---

### Step 5 — Record Changes

**Agent:** `CHANGE-RECORDER` (embedded in IMPL-EXECUTOR's step cycle)  
**Inputs:** Completed step record, list of modified files from Step 4  
**Outputs:** Updated `change-log.md`; updated `modified-components.md`  

Immediately after each atomic step completes, record:

**In `change-log.md`:**
- Step ID
- Step title
- Change type
- For each file modified: file path, change type (`created`, `modified`, `deleted`), and a one-sentence description of what changed in the file
- For each new dependency introduced: dependency name, version, and licence classification
- Timestamp of step completion
- Any deviation from the plan (if a file was changed that was not declared, or if a declared file was not changed)

**In `modified-components.md`:**
- For each module touched by a completed step: module ID, files modified within the module, and the nature of the modification (source, test, config, documentation, schema, build)
- For each API modified: API name, change type (added, modified, removed, deprecated), and the checkpoint ID from the verification plan that covers this change
- For each event schema modified: event type, change type, and the checkpoint ID that covers it
- For each database schema modified: schema object, migration file name, and the checkpoint ID that covers it

This recording must be completed before the next atomic step begins. A step whose changes are not recorded in `change-log.md` is not considered complete regardless of its `STATUS-005.md` entry.

---

### Step 6 — Synchronise Documentation

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** Completed step record, change log entry from Step 5  
**Outputs:** Updated documentation files (within the plan's scope)  

For any step where the plan declares a documentation synchronisation obligation (README update, API catalog update, ADR status update, inline comment update), execute the documentation change as part of the step before marking it complete.

Documentation synchronisation is not a separate step in the workflow; it is an obligation of Step 4 for steps that include documentation changes. This workflow step exists to enforce the obligation explicitly: the agent must confirm, after every source-modifying step, that no documentation obligation has been deferred.

**Deferred documentation detection:** After each source-modifying step, check the list of documentation obligations from the plan against the completed changes. If a documentation obligation for a completed source step has not been fulfilled (e.g., an API was modified but the API catalog was not updated), record the gap as an implementation blocker and trigger GATE-2. Documentation obligations may not be silently deferred.

If the plan did not include a documentation step for a change that, during execution, is observed to require one (e.g., a code change reveals that an undocumented API exists and is now being modified), record the gap as a plan deficiency in `docs/implementation/assumptions.md` and trigger GATE-2. The missing documentation step must be added via a LOOP-004 plan revision or approved as an explicit exception.

---

### Step 7 — Record Assumptions

**Agent:** `IMPL-EXECUTOR`  
**Inputs:** Completed steps, implementation knowledge base, encountered decision points  
**Outputs:** Updated `docs/implementation/assumptions.md`  

After each atomic step, the agent reviews whether any implementation decision was made that is not directly derivable from the plan, the context package, the architecture context, or the coding standards. Each such decision is an assumption.

Each assumption record in `docs/implementation/assumptions.md` must contain:

| Field | Description |
|-------|-------------|
| `assumption_id` | `IMPL-ASSM-NNN`, sequential, unique per run |
| `discovery_step` | The `step_id` during which the assumption was made |
| `statement` | The declarative statement of what was assumed to be true |
| `basis` | Why the plan did not resolve this decision (missing context, ambiguous specification, inferred behaviour) |
| `decision_made` | The specific implementation choice taken based on the assumption |
| `alternative` | The other plausible implementation choice that was not taken |
| `impact_if_wrong` | What the consequence would be for LOOP-006 verification if the assumption is incorrect |
| `verification_hint` | A specific test or assertion that LOOP-006 should run to validate this assumption |

Assumptions from the LOOP-004 planning register (`ASSM-NNN`) that were validated as true during implementation are recorded as `validated` in `docs/implementation/assumptions.md` with the evidence. Assumptions from the planning register that were validated as false trigger GATE-1 — a false planning assumption requires a plan revision before execution continues.

---

### Step 8 — Prepare Artefacts for Verification

**Agent:** `IMPL-CHECKER`  
**Inputs:** Completed step sequence, `change-log.md`, `modified-components.md`, `assumptions.md`, verification plan from LOOP-004  
**Outputs:** Verification handoff package (completed `implementation-summary.md`, validated `modified-components.md`)  

`IMPL-CHECKER` performs the independent handoff review. This is not self-verification; it is the preparation of evidence for LOOP-006. The Checker does not assess correctness — it assesses completeness and plan adherence.

**IMPL-CHECKER independently verifies:**

1. Every file listed as `files_modified` in every completed plan step appears in `change-log.md`. No completed step has unrecorded changes.
2. No file appears in `change-log.md` that is not listed as `files_modified` in a completed plan step. No unauthorised changes were made.
3. Every module, API, event, and schema modified in `change-log.md` appears in `modified-components.md` with the correct checkpoint reference.
4. Every documentation obligation for a completed step is either fulfilled (the documentation file appears in `change-log.md`) or recorded as a plan gap in `assumptions.md`.
5. Every assumption from the LOOP-004 planning register is resolved: either `validated` (true during implementation) or `refuted` (false, triggering GATE-1) or `unresolved` (not encountered during execution, remaining open for LOOP-006).
6. No secret values appear in any modified file.

The Checker produces a handoff readiness report: `accepted` or `rejected`. If `rejected`, the report enumerates every unresolved issue. `IMPL-EXECUTOR` addresses the issues and the Checker re-reviews (maximum one re-review). If still `rejected`, GATE-1 fires.

On Checker `accepted`, the Checker writes `implementation-summary.md`:

**`implementation-summary.md` structure:**

- **Readiness statement:** The explicit declaration that this implementation is ready for LOOP-006 verification. This statement records plan version, task ID, run ID, step completion count, and the Checker agent ID. It does not declare correctness.
- **Planned vs. actual changes:** For each plan step, the declared change versus the actual change, with any deviation documented.
- **Files modified:** Complete list cross-referenced to plan step IDs and verification checkpoint IDs.
- **Components affected:** Modules, APIs, events, schemas, and configuration areas modified.
- **Assumptions:** Complete list of all `IMPL-ASSM-NNN` entries with their verification hints.
- **Planning register resolution:** Status of each `ASSM-NNN` from LOOP-004: validated, refuted, or unresolved.
- **Outstanding limitations:** Changes that are incomplete or deferred, with the reason.
- **Known risks:** Implementation-observed risks not in the LOOP-004 risk register, including file-level observations from execution.
- **Verification hints:** Ordered list of specific checks LOOP-006 should perform, derived from the verification plan and supplemented by assumption verification hints.
- **Do not verify:** Items that were intentionally not changed despite being related to the task — to prevent LOOP-006 from flagging planned omissions as defects.

---

### Step 9 — Update STATUS-005.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, step completion records, gate outcomes, Checker handoff report  
**Outputs:** Updated `docs/loops/core/STATUS-005.md`  

Record all metrics. Record gate outcomes. Record final status: `completed`, `failed`, or `stopped`. Record the implementation HEAD SHA (`execution_end_sha`) — the SHA after all steps have been applied. Record open blockers: false planning assumptions awaiting plan revision, deferred documentation obligations, incomplete steps.

The `execution_end_sha` is a critical provenance field: LOOP-006 uses it to confirm it is verifying the same repository state that LOOP-005 produced.

---

### Step 10 — Update SKILL-005.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Implementation metrics, deviation records, assumption statistics  
**Outputs:** Updated `docs/loops/core/SKILL-005.md`  

Update the skill profile:

- Modules most frequently implemented in this repository (candidates for improved LOOP-002 pre-selection)
- Change types most frequently producing deviations (signals LOOP-004 planning gaps for those change types)
- Assumption categories most frequently recorded (signals LOOP-002 context gaps or LOOP-001 architecture gaps)
- Average steps completed per run before a halt condition (signals plan complexity calibration for LOOP-004)
- Documentation obligation gap rate (proportion of steps where documentation synchronisation required GATE-2 escalation)
- GATE-1 trigger distribution across gate trigger conditions (signals which change types most frequently require human review)

---

### Step 11 — Publish Implementation Summary

**Agent:** `STATUS-WRITER`  
**Inputs:** `implementation-summary.md` from Step 8, `implementation-metadata.md`, all change records  
**Outputs:** Updated `docs/implementation/implementation-metadata.md`; final `STATUS-005.md` confirmation  

Write `implementation-metadata.md` with: task ID, plan version, run ID, LOOP-001/002/003/004 run IDs consumed, `execution_start_sha`, `execution_end_sha`, steps planned, steps completed, steps deferred, files created, files modified, files deleted, lines added, lines modified, lines deleted, assumptions recorded, LOOP-004 assumptions validated, LOOP-004 assumptions refuted, LOOP-004 assumptions unresolved, documentation obligations fulfilled, documentation obligations deferred, execution duration in seconds, gate outcomes, and Checker report reference.

Confirm that all Stop Conditions are satisfied. Write final `status: completed` to `STATUS-005.md`. The implementation is now ready for LOOP-006.

---

## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `IMPL-CHECKER` without relying on `IMPL-EXECUTOR`'s self-report.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Every file declared as `files_modified` in every completed plan step appears in `change-log.md` | Cross-reference plan step files against change log entries; assert one-to-one coverage |
| VER-2 | No file appears in `change-log.md` that is not declared in a completed plan step's `files_modified` list | Invert the cross-reference; assert no surplus entries in change log |
| VER-3 | Every module modified in `change-log.md` appears in `modified-components.md` with the correct verification checkpoint reference | Cross-reference change log modules against modified components; assert all present |
| VER-4 | No secret values appear in any modified file | Scan all files in `change-log.md` against secrets patterns; assert zero matches |
| VER-5 | Every documentation obligation for a completed step is either fulfilled or recorded as a plan gap in `assumptions.md` | Cross-reference documentation obligations from the plan against change log entries and assumption register; assert all accounted for |
| VER-6 | Every assumption from the LOOP-004 planning register is resolved: validated, refuted, or unresolved-and-documented | Read LOOP-004 assumption register; cross-reference against `assumptions.md`; assert every planning assumption has a resolution status |
| VER-7 | `implementation-summary.md` contains a readiness statement, planned-vs-actual table, complete file list, complete component list, complete assumption list, outstanding limitations, and verification hints | Parse the summary document; assert all required sections are present and non-empty |
| VER-8 | `implementation-metadata.md` records `execution_start_sha`, `execution_end_sha`, task ID, and plan version | Read metadata; assert all four fields present and non-empty |
| VER-9 | The `execution_end_sha` in `implementation-metadata.md` matches the current HEAD SHA | Compare stored SHA against `git rev-parse HEAD`; assert equal |
| VER-10 | No step marked `non-reversible: false` in the plan has its rollback action left as `none` | Inspect rollback plan entries for reversible steps; assert all have defined actions |
| VER-11 | `STATUS-005.md` has been updated with the current run ID and a timestamp within 5 minutes of the current time | Read STATUS file; assert run ID and timestamp within tolerance |
| VER-12 | The count of completed steps in `STATUS-005.md` matches the count of step entries in `change-log.md` | Compare counts; assert equal |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/implementation/reflections/REFLECTION-005-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Plan Adherence Summary:** count of steps executed as declared versus steps where a deviation was detected; count of unauthorised file modification attempts blocked; count of GATE-1 triggers from deviation detection
- **Change Summary:** files created, modified, and deleted; lines added, modified, and deleted; change type distribution (source, test, config, documentation, schema, build)
- **Assumption Summary:** count of new assumptions recorded by category (missing context, ambiguous specification, inferred behaviour); count of LOOP-004 planning assumptions validated, refuted, and unresolved; the highest-impact refuted assumption
- **Documentation Synchronisation Summary:** documentation obligations fulfilled versus deferred; root causes of deferred obligations
- **Gate Narrative:** for each gate that fired, the trigger condition, the artefacts reviewed, the human decision or auto-proceed outcome, and the time elapsed at the gate

---

## Human Approval Gates

### GATE-1 — Hard Gate: Public Interface Change, Security Change, Infrastructure Change, or Plan Deviation

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | Before the execution of any triggering step (pre-execution gate) or after a deviation is detected (post-execution gate) |
| Artefact Under Review | The step record, the proposed file changes (pre-execution) or the detected deviation (post-execution), and the relevant section of the rollback plan |
| Approver | Principal Engineer or Architecture Owner; Security Lead if the trigger is security-related; Infrastructure Lead if the trigger is infrastructure-related |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | The triggering step is not executed (pre-execution) or is rolled back (post-execution); loop terminates with status `stopped`; Reflection produced; LOOP-004 must revise the plan before LOOP-005 resumes |
| Audit Trail | Approval record written to `STATUS-005.md` under `gate_outcomes.GATE-1`; reviewer name, role, timestamp, decision, and specific scope of approval recorded |

**Fires when:**

Pre-execution (before the step executes):
- The step modifies a public API contract (adds, modifies, or removes an endpoint, request field, or response field visible to external consumers)
- The step modifies authentication, authorisation, or tenant isolation logic
- The step modifies security configuration, cryptographic settings, or access control rules
- The step modifies an infrastructure definition (Terraform, Helm, Kubernetes manifest, Docker Compose)
- The step is marked `non-reversible: true` in the plan (a human must approve every non-reversible step before it executes)
- The step modifies files in three or more module boundaries simultaneously
- The prerequisite validation in Step 2 fails for a reason other than an incomplete dependency task

Post-execution (after a deviation is detected during Step 4):
- An unauthorised file modification is detected (a file outside the plan's `files_modified` list was modified)
- A file declared in the plan's `files_modified` list could not be modified as described (structural deviation)
- A LOOP-004 planning assumption is refuted during implementation (the assumption was false)
- The Checker's handoff review is rejected on both the initial review and one re-review

**Reviewer guidance (pre-execution):** Confirm that the proposed change is consistent with the current architectural state, that the rollback plan is adequate for the step's risk, and that the correct stakeholders are aware of the change. Record the specific scope of approval (what you are approving — not a blanket approval for all changes in the plan).

**Reviewer guidance (post-execution deviation):** Inspect the deviation. Determine whether the deviation should be accepted (the implementation as executed is correct), rolled back (the step's changes must be reversed and the plan revised), or escalated (the deviation reveals a deeper architectural issue requiring LOOP-001 re-run).

---

### GATE-2 — Soft Gate: Documentation Gap, Concurrent Modification, or Elevated Assumption Rate

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 6 (documentation synchronisation check) or after Step 7 (assumption recording) |
| Artefact Under Review | Documentation obligation gaps, assumption register, concurrent modification conflict |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-005.yml`; defaults to writing a notification to `STATUS-005.md` |
| Timeout | 4 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds with the gap recorded; `soft_gate_auto_proceeded: true` in `STATUS-005.md` |
| Audit Trail | Notification timestamp and outcome recorded under `gate_outcomes.GATE-2` |

**Fires when (and GATE-1 did not also fire):**
- A documentation obligation for a completed step is not fulfilled and no documentation step exists in the remaining plan
- A concurrent modification conflict is detected (files in the plan's scope have been modified by another agent or engineer since the `execution_start_sha`)
- More than three new `IMPL-ASSM-NNN` entries are recorded in a single step (high assumption density signals plan incompleteness for that step)
- A new external dependency is introduced that was not declared in the dependency plan

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-005.md`. The executing agent checks `STATUS-005.md` at the start of each atomic step (Step 4, check 3). On emergency stop: the current step is abandoned without completing its file modifications (if modifications are in progress, they are reverted using the step's declared rollback action); no further steps are executed; a partial Reflection is produced; `STATUS-005.md` records the step at which the stop was received and the current repository SHA.

---

## Failure Recovery

### FR-1 — Merge Conflict

**Detection:** During Step 4, a file declared in `files_modified` cannot be modified as planned because another change to the same file has been committed since the planning HEAD SHA.  
**Immediate Action:** Do not attempt to resolve the conflict autonomously. Record the conflict in `STATUS-005.md` as an implementation blocker. Trigger GATE-1.  
**Recovery:** Human reviewer decides: (a) re-run LOOP-004 to re-plan against the current HEAD SHA, or (b) manually resolve the conflict and approve a revised file modification. In either case, the plan version is incremented and LOOP-005 resumes from the conflicting step.  
**Rollback:** All steps completed before the conflict step are preserved; the conflicting step was not executed; no partial modification was applied.

### FR-2 — Partial Step Application

**Detection:** During Step 4, file modification begins but is interrupted (agent timeout, process failure, Emergency Stop) after some but not all files in the step's `files_modified` list have been modified.  
**Immediate Action:** The step is in an indeterminate state. Record `step_status: partial` in `STATUS-005.md`. Record which files were modified and which were not.  
**Recovery:** Apply the step's rollback action to all files that were partially modified, restoring them to their pre-step state. Mark the step as `rolled_back`. Resume from the step (re-execute it from scratch) on the next trigger.  
**Rollback:** Step-level rollback from `docs/planning/rollback-plan.md`; prior completed steps are not affected.

### FR-3 — Unexpected Architectural Constraint

**Detection:** During Step 4, the implementing agent observes that a file's current structure requires a change pattern that would violate an accepted ADR or a module boundary rule not visible at planning time.  
**Immediate Action:** Halt the step immediately without modifying any file. Record the constraint in `STATUS-005.md`. Trigger GATE-1.  
**Recovery:** Human reviewer assesses the constraint. Options: (a) the ADR is updated or an exception is documented, allowing the step to proceed; (b) the plan is revised by LOOP-004 to respect the constraint (adding an intermediate step or changing the approach); (c) the task is deferred and the constraint is added to the LOOP-001 unknowns register.  
**Rollback:** No files were modified; no rollback is required.

### FR-4 — Missing Dependency

**Detection:** During Step 3 or Step 4, a file declared as an input to a step does not exist in the repository, or a module interface declared in the dependency plan has changed since the plan was produced.  
**Immediate Action:** Record the missing dependency. Trigger GATE-1 if the missing dependency is in the current step's `inputs`; trigger GATE-2 if it is in a future step's `inputs` (and record it as a future blocker).  
**Recovery:** For a current-step blocker: human reviewer decides whether to (a) create the missing dependency before resuming, (b) re-run LOOP-004 to revise the plan, or (c) defer the task. For a future-step blocker: record and proceed; the blocker will be re-evaluated when the affected step is reached.  
**Rollback:** No files were modified by the failed step; no rollback required.

### FR-5 — Incomplete Implementation (Step Timeout or Plan Timeout)

**Detection:** An atomic step has been executing for more than 4 hours (`step_timeout`) or the complete plan has been executing for more than 24 hours (`plan_timeout`).  
**Immediate Action:** Abandon the current step. Apply the step's rollback action to restore files to their pre-step state. Record `status: stopped` with reason `step_timeout` or `plan_timeout`.  
**Recovery:** The loop may be re-triggered to resume from the last completed step. If the same step times out on re-entry, it signals that the step is not atomic in practice; return to LOOP-004 for decomposition revision.  
**Rollback:** Step-level rollback from `docs/planning/rollback-plan.md`.

### FR-6 — Missing Documentation Synchronisation

**Detection:** Step 6 identifies that a completed source step has a documentation obligation that is not in the plan and cannot be fulfilled by any remaining plan step.  
**Immediate Action:** Record the gap in `assumptions.md`. Trigger GATE-2.  
**Recovery:** If GATE-2 auto-proceeds, the gap is carried to LOOP-006 as an outstanding limitation in `implementation-summary.md`. LOOP-006 must decide whether to pass verification with the gap documented or to require a supplementary documentation step. If GATE-2 is acted on: a plan revision is requested from LOOP-004 to add the missing documentation step.  
**Rollback:** No rollback required; the source change is preserved; the documentation obligation is documented as outstanding.

### FR-7 — Rollback to Last Known Good State

**Detection:** Multiple consecutive steps have failed or triggered GATE-1 denials, indicating the plan is not executable against the current repository state.  
**Immediate Action:** Record `rollback_initiated: true` in `STATUS-005.md`. Begin executing the phase rollback sequence from `docs/planning/rollback-plan.md` for all completed steps, in reverse step order.  
**Recovery:** Execute rollback actions for each completed step. For non-reversible steps: execute the compensating action declared in the rollback plan. After all rollback actions complete, verify the repository matches the `execution_start_sha` (git diff should be empty for all plan-scoped files). If it does not match, record the residual changes and trigger GATE-1 for human recovery.  
**Rollback:** This procedure is the rollback. Its completion is recorded in `STATUS-005.md` with the post-rollback HEAD SHA.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-005.md` at Step 9.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of loop workflow steps completed (of 11) |
| `run.steps_total` | 11 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-12 criteria passed |
| `verification.level1.fail` | Count of VER-1 through VER-12 criteria failed |
| `reflection.produced` | Boolean |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `impl.task_id` | Task being implemented |
| `impl.plan_version` | LOOP-004 plan version consumed |
| `impl.resuming` | Boolean — was this a resumption of a prior interrupted run |
| `impl.steps_planned` | Total atomic implementation steps in the plan |
| `impl.steps_completed` | Atomic steps completed in this run |
| `impl.steps_deferred` | Steps not reached due to halt condition |
| `impl.steps_rolled_back` | Steps rolled back due to failure or GATE-1 denial |
| `impl.files_created` | Files created during this run |
| `impl.files_modified` | Files modified during this run |
| `impl.files_deleted` | Files deleted during this run |
| `impl.lines_added` | Lines of code added |
| `impl.lines_modified` | Lines of code modified |
| `impl.lines_deleted` | Lines of code deleted |
| `impl.modules_touched` | Count of distinct modules with at least one file modified |
| `impl.unauthorised_modifications_blocked` | Count of unauthorised file modification attempts detected and blocked |
| `impl.deviations_detected` | Count of steps where actual change differed from declared change |
| `impl.assumptions_new` | Count of new `IMPL-ASSM-NNN` entries recorded |
| `impl.planning_assumptions_validated` | Count of LOOP-004 `ASSM-NNN` entries confirmed true |
| `impl.planning_assumptions_refuted` | Count of LOOP-004 `ASSM-NNN` entries confirmed false |
| `impl.planning_assumptions_unresolved` | Count of LOOP-004 `ASSM-NNN` entries not encountered |
| `impl.documentation_obligations_fulfilled` | Count of documentation synchronisation steps completed |
| `impl.documentation_obligations_deferred` | Count of documentation synchronisation obligations not fulfilled |
| `impl.gate1_pre_execution_count` | Hard gates triggered before a step executed |
| `impl.gate1_post_execution_count` | Hard gates triggered after a deviation was detected |
| `impl.execution_start_sha` | HEAD SHA at run start |
| `impl.execution_end_sha` | HEAD SHA at run end |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The implementing agent modifies files beyond those declared in the plan — improving adjacent code, refactoring visible but unplanned issues, or fixing discovered bugs.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** The agent exercises discretion beyond Principle 1 (Plan Authority) and Principle 2 (Minimal Blast Radius).
- **Control:** VER-2 detects unauthorised modifications after each step. Step 4 enforces the plan file list before modification. Any detected unauthorised modification triggers GATE-1.
- **Detection:** VER-2 failure; `impl.unauthorised_modifications_blocked` metric above zero.
- **Response:** GATE-1 for human decision on whether to accept or revert the unauthorised change; scope creep is recorded in Reflection under Failures and Anomalies.

### RISK-2 — Architectural Drift

- **Description:** Implementation choices gradually deviate from the architecture defined in LOOP-001 outputs, because the implementing agent makes decisions that are locally sensible but violate architectural rules not salient at execution time.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** ADR constraints are not loaded or not applied during Step 4; module boundary rules are not enforced.
- **Control:** Step 3 explicitly loads and indexes all applicable ADRs. FR-3 procedure halts execution on constraint discovery. VER-1/VER-2 detect scope violations. IMPL-CHECKER independently validates boundary compliance.
- **Detection:** FR-3 trigger; post-run LOOP-001 re-run detecting drift from expected architecture.
- **Response:** FR-3 procedure; GATE-1; plan revision by LOOP-004.

### RISK-3 — Hidden Dependencies Discovered During Execution

- **Description:** A dependency between two implementation steps is discovered during execution that was not identified during LOOP-004 planning, requiring a resequencing that the current plan does not accommodate.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Implicit module coupling (shared environment variable, shared singleton, framework-mediated dependency) not visible at planning time.
- **Control:** FR-4 procedure records the dependency and triggers GATE-1 or GATE-2 depending on timing. LOOP-004's plan revision incorporates the newly discovered dependency.
- **Detection:** Step 4 deviation detection when a declared input is found to depend on an undeclared prerequisite.
- **Response:** FR-4 procedure.

### RISK-4 — Breaking Changes Without Approved Gate

- **Description:** A modification to a public API, event schema, or shared module interface is made without GATE-1 approval, breaking consumer compatibility.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** A step that modifies a public interface is executed without a pre-execution GATE-1 check.
- **Control:** Gate trigger list explicitly includes public API changes, event schema changes, and shared module interface modifications. Step 4, check 4 enforces the pre-execution gate before any file is modified.
- **Detection:** Post-execution LOOP-006 contract test failures.
- **Response:** Immediate rollback of the breaking step; GATE-1 for revised approach.

### RISK-5 — Tenant Isolation Breach

- **Description:** A code change modifies tenant isolation logic (authentication, authorisation, data scoping) in a way that allows cross-tenant data access.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** A step modifying authentication or authorisation code is executed without GATE-1 approval.
- **Control:** GATE-1 fires pre-execution for all steps modifying authentication, authorisation, or tenant isolation logic. Security Lead is the required approver for these triggers.
- **Detection:** LOOP-006 tenant isolation tests.
- **Response:** Immediate rollback; Security Lead-led incident review; LOOP-001 security model update.

### RISK-6 — Documentation Drift

- **Description:** Source code changes are implemented without corresponding documentation updates, creating a growing gap between code behaviour and recorded specification.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Documentation obligation enforcement in Step 6 is bypassed or produces a GATE-2 that auto-proceeds without human review.
- **Control:** Step 6 enforces documentation synchronisation obligations. GATE-2 fires for all detected gaps. VER-5 requires all obligations to be either fulfilled or explicitly documented as gaps.
- **Detection:** VER-5 failure; `impl.documentation_obligations_deferred` metric above zero.
- **Response:** FR-6 procedure; outstanding limitation carried to LOOP-006.

### RISK-7 — Configuration Inconsistency

- **Description:** Configuration changes are applied inconsistently across environment targets (e.g., a change is made to production configuration but not staging), creating environment-specific failure modes invisible in testing.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Configuration change governance rules are not enforced during Step 4.
- **Control:** Change Management Governance specifies that configuration changes affecting multiple environments must be applied consistently. IMPL-CHECKER VER-2 detects if declared files were not changed.
- **Detection:** VER-1/VER-2 failure; environment-specific test failures in LOOP-006.
- **Response:** Plan revision to add missing environment target step; GATE-2 notification.

### RISK-8 — Secrets Exposure

- **Description:** A configuration or source file modification introduces a secret value into a tracked file, exposing credentials in the repository.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** Configuration change governance rules are not enforced; the implementing agent uses a literal secret value rather than a reference.
- **Control:** VER-4 scans all modified files for secrets patterns before the run is marked complete. Change Management Governance prohibits secret values in tracked files.
- **Detection:** VER-4 failure.
- **Response:** Immediate revocation of the exposed secret by the appropriate credential owner (out of band from this loop); removal of the secret from the file; GATE-1 for the revised step; incident record in Reflection.

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | All planned atomic steps have been executed and recorded in `change-log.md` |
| SC-2 | All verification criteria VER-1 through VER-12 have passed |
| SC-3 | All documentation obligations are either fulfilled or recorded in `assumptions.md` as outstanding limitations |
| SC-4 | All five implementation artefacts listed in the Outputs table have been written |
| SC-5 | `implementation-summary.md` has been written with the Checker's readiness statement |
| SC-6 | `implementation-metadata.md` records `execution_start_sha` and `execution_end_sha` |
| SC-7 | `STATUS-005.md` has been updated with run metrics and final status |
| SC-8 | `SKILL-005.md` has been updated |
| SC-9 | The Reflection artefact has been written |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-10 | An atomic step exceeds 4 hours (`step_timeout`) |
| SC-11 | The complete plan exceeds 24 hours (`plan_timeout`) |
| SC-12 | GATE-1 is denied by the human reviewer |
| SC-13 | PRE-4 detects a concurrent run; this instance exits without modifying any file |
| SC-14 | An Emergency Stop signal is received |
| SC-15 | A blocking prerequisite dependency task is incomplete (wait state, not failure) |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Implementation Artefacts:**
- [ ] Repository source files modified as declared in the execution plan
- [ ] `docs/implementation/implementation-summary.md` written with readiness statement, planned-vs-actual, all file and component lists, all assumptions, outstanding limitations, and verification hints
- [ ] `docs/implementation/change-log.md` written with a per-step entry for every file modified
- [ ] `docs/implementation/modified-components.md` written with all affected modules, APIs, events, schemas cross-referenced to verification checkpoint IDs
- [ ] `docs/implementation/implementation-metadata.md` written with all provenance fields including `execution_start_sha` and `execution_end_sha`
- [ ] `docs/implementation/assumptions.md` written with all `IMPL-ASSM-NNN` entries and LOOP-004 assumption resolution records

**Verification:**
- [ ] All VER-1 through VER-12 criteria assessed and outcomes recorded in Reflection
- [ ] `IMPL-CHECKER` handoff review completed with `accepted` finding
- [ ] VER-4 (secrets scan) passed on all modified files
- [ ] VER-2 (no unauthorised modifications) confirmed

**Gates:**
- [ ] Gate outcome recorded in `STATUS-005.md` for every gate that fired
- [ ] All pre-execution GATE-1 approvals recorded with reviewer identity and scope of approval

**State:**
- [ ] `docs/loops/core/STATUS-005.md` updated with all metrics, `execution_end_sha`, and final status
- [ ] `docs/loops/core/SKILL-005.md` updated

**Reflection:**
- [ ] `docs/implementation/reflections/REFLECTION-005-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---

## Future Improvements

- **Step-level SHA snapshots:** Record the HEAD SHA after each atomic step completes, enabling fine-grained rollback to any step boundary rather than only to phase boundaries.
- **Syntax validation before commit:** Integrate a language-specific syntax check immediately after each source file is modified, before the step is recorded as complete. A syntactically invalid file means the step has not achieved its declared output.
- **Parallel step orchestration:** Extend the loop to support concurrent execution of steps in declared parallel groups (from `implementation-breakdown.md`), with independent `IMPL-EXECUTOR` instances and a shared `STATUS-005.md` coordinator tracking cross-instance progress.
- **Deviation pattern analysis:** Track deviation types in `SKILL-005.md` across runs to identify recurring planning gaps (e.g., steps of type `migrate` consistently require more files than planned) and feed these patterns back to LOOP-004's decomposition patterns library.
- **Documentation obligation pre-validation:** Before beginning execution (Step 3), scan the plan for steps that modify APIs, schemas, or module interfaces and verify that a documentation step exists for each. Surface missing documentation steps as plan gaps before any source file is modified, reducing GATE-2 triggers during execution.
- **Change scope visualisation:** Produce a machine-readable change scope map in `modified-components.md` (beyond the current human-readable format) that LOOP-006 can parse directly to configure its verification scope, reducing the manual interpretation burden in the handoff.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces the architecture knowledge base and module catalog enforced during implementation
- `docs/loops/core/LOOP-002-Context-Assembly.md` — produces the implementation and verification context consumed in Steps 3–8
- `docs/loops/core/LOOP-003-Task-Discovery.md` — provides the task classification that governs change management expectations
- `docs/loops/core/LOOP-004-Planning.md` — produces the execution plan, rollback plan, and verification plan that authorise and sequence all implementation work
- `docs/loops/shared/coding-standards.md` — coding standards applied to all code produced
- `docs/loops/shared/verification-standards.md` — verification level definitions used in checkpoint classification
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-005 as the implementation execution loop for the AI Engineering Operating System, consuming LOOP-001 through LOOP-004 outputs and producing the verified implementation and handoff package consumed by LOOP-006.

