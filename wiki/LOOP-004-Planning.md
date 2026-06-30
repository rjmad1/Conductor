---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-004-Planning.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Formulate detailed step-by-step engineering implementation plan.
Generalized Purpose: Formulate detailed step-by-step engineering implementation plan.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-004 — Planning

**Loop ID:** LOOP-004  
**Name:** Planning  
**Version:** 1.0  
**Status:** Active  
**Category:** Core  
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly, LOOP-003 — Task Discovery  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-004 transforms a selected engineering task into a deterministic, verifiable, and executable implementation plan. It decides how work will be performed — not performing the work itself. Its outputs are the authoritative instructions that LOOP-005 (Implementation) executes. No implementation work may begin without a plan produced or explicitly validated by this loop.

The plan must be complete enough that an agent executing it needs no architectural inference, no undeclared assumptions, and no mid-execution decisions about sequencing or scope. Where decisions cannot be made in advance, the plan must identify them as explicit decision points with resolution criteria, not leave them implicit.

---

## Problem Statement

Implementation without a plan produces code that is locally coherent but architecturally inconsistent. Steps are executed in the wrong order, creating integration failures. Scope expands during execution as undiscovered dependencies surface. Rollback becomes impossible because the sequence of changes was never recorded. Verification is ad-hoc because checkpoints were never defined. The cost of these failures compounds with task complexity: a simple bug fix without a plan is recoverable; a cross-module refactor without one can destabilise a repository for days.

---

## Why This Loop Exists

Planning is the highest-leverage point in the engineering loop chain. A defective plan that is caught in LOOP-004 costs one loop revision. The same defect discovered during LOOP-005 (Implementation) costs a rollback, a revised plan, and a second implementation pass. The same defect discovered in LOOP-006 (Verification) costs all of that plus the time to identify root cause. Codifying planning as a loop with verification, human gates, and a rollback requirement forces plan quality to be assessed before any repository state is modified.

---

## Scope

**In scope:**
- Loading and analysing the selected task from the LOOP-003 backlog
- Determining the full set of architectural components affected by the task
- Identifying all implementation dependencies: internal module interfaces, external contracts, build order, data migration sequencing
- Decomposing the task into atomic implementation steps, each independently completable and verifiable
- Sequencing steps in valid dependency order, identifying parallelisable groups
- Defining verification checkpoints: the criteria that must pass after each atomic step before the next begins
- Defining the rollback strategy: the sequence of reversals that restores the repository to its pre-plan state if the plan is abandoned at any step
- Recording all assumptions, open questions, and architectural constraints that govern the plan
- Estimating implementation effort using observable signals
- Producing a complete, internally consistent execution plan in `docs/planning/`
- Updating `STATUS-004.md` and `SKILL-004.md`

**Out of scope:**
- Writing, modifying, or deleting source code (LOOP-005)
- Executing tests (LOOP-006)
- Modifying CI/CD pipelines or infrastructure
- Resolving open architectural unknowns (those are inputs to planning, not outputs)
- Making product or roadmap decisions
- Selecting which task to plan (LOOP-003 provides the selected task; LOOP-004 plans it)
- Producing documentation beyond the planning artefacts themselves

**Maximum run duration:** 45 minutes. If the loop has not reached a Stop Condition within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Selected task record | Single task record from `task-backlog.md` | LOOP-003 | Required |
| `docs/tasks/task-backlog.md` | File | LOOP-003 output | Required |
| `docs/tasks/dependency-graph.md` | File | LOOP-003 output | Required |
| `docs/tasks/task-catalog.md` | File | LOOP-003 output | Required |
| `docs/loops/core/STATUS-003.md` | File | LOOP-003 | Required — freshness check |
| `docs/context/context-package.md` | File | LOOP-002 output | Required |
| `docs/context/architecture-context.md` | File | LOOP-002 output | Required |
| `docs/context/dependency-context.md` | File | LOOP-002 output | Required |
| `docs/context/implementation-context.md` | File | LOOP-002 output | Required |
| `docs/context/verification-context.md` | File | LOOP-002 output | Required |
| `docs/context/context-metadata.md` | File | LOOP-002 output | Required — freshness and provenance check |
| `docs/loops/core/STATUS-002.md` | File | LOOP-002 | Required — freshness check |
| `docs/architecture/architecture-overview.md` | File | LOOP-001 output | Required |
| `docs/architecture/module-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/dependency-map.md` | File | LOOP-001 output | Required |
| `docs/architecture/api-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/technical-debt.md` | File | LOOP-001 output | Required |
| `docs/architecture/unknowns.md` | File | LOOP-001 output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 | Required — freshness check |
| `docs/loops/core/SKILL-001.md` | File | LOOP-001 | Required |
| ADR directory | `docs/adr/` or configured equivalent | Repository | Required — all accepted ADRs |
| Prior execution plan | `docs/planning/execution-plan.md` | Prior LOOP-004 run | Optional — for replanning detection |
| Planning configuration | `.loop-004.yml` at repo root | Repository | Optional |

### Input Validation

Before Step 1 begins, the loop must verify:

- The selected task record is present in `task-backlog.md` with `status: accepted` and a valid `task_id`.
- `STATUS-003.md` records a completed LOOP-003 run with a timestamp no older than 24 hours. If stale, trigger GATE-1.
- The LOOP-002 context package is present and its recorded HEAD SHA in `context-metadata.md` matches the current HEAD SHA. If the HEAD SHA has changed since the context package was assembled, trigger GATE-1.
- All required LOOP-001 output files are present and readable.
- No concurrent LOOP-004 run is active for the same task ID.

If the context package HEAD SHA has changed, the plan cannot proceed on stale context — a fresh LOOP-002 run must be triggered first. If the LOOP-003 backlog is stale, the selected task's priority rank may have changed — a human must confirm the task selection before planning proceeds.

---

## Outputs

All outputs are written to `docs/planning/`. On first run, this directory is created. On subsequent runs for the same task, each file is replaced in full; prior content is preserved in git history. On runs for a different task, prior files are archived with a run-ID suffix before being overwritten.

| Artefact | Path | Description |
|----------|------|-------------|
| Execution Plan | `docs/planning/execution-plan.md` | The primary deliverable: the complete, ordered list of atomic implementation steps with their inputs, outputs, verification checkpoints, and rollback actions |
| Implementation Breakdown | `docs/planning/implementation-breakdown.md` | The decomposition record: how the task was divided into atomic steps, the rationale for each boundary, and the parallelisation map |
| Dependency Plan | `docs/planning/dependency-plan.md` | All implementation dependencies: module interfaces, external contracts, build order constraints, data migration sequencing, and task-level predecessors |
| Verification Plan | `docs/planning/verification-plan.md` | The checkpoint-by-checkpoint verification specification: the criteria, the method, and the responsible agent for each checkpoint in the execution plan |
| Rollback Plan | `docs/planning/rollback-plan.md` | The step-by-step rollback sequence that restores the repository to its pre-plan state if the plan is abandoned at any point during execution |
| Planning Metadata | `docs/planning/planning-metadata.md` | Run provenance: task ID, plan version, run ID, LOOP-001/002/003 run IDs consumed, atomic step count, dependency count, effort estimate, confidence score, assumption count, risk count |
| Loop Status | `docs/loops/core/STATUS-004.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/core/SKILL-004.md` | Updated skill profile for this loop |
| Reflection | `docs/planning/reflections/REFLECTION-004-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the authoritative architecture knowledge base. The module catalog, dependency map, API catalog, and unknowns register are required inputs for Steps 2–4. LOOP-001 must be Active and its outputs within the freshness threshold.
- **LOOP-002 — Context Assembly:** Provides the task-scoped context package. The implementation context, dependency context, architecture context, and verification context are required inputs for Steps 2–7. LOOP-002 must be Active and its context package must have been assembled for the selected task with a HEAD SHA matching the current repository state.
- **LOOP-003 — Task Discovery:** Provides the selected task record and the validated task dependency graph. LOOP-003 must be Active and its backlog must be current (within 24 hours).

---

## Trigger

A run is initiated by any of the following:

1. **LOOP-005 prerequisite** — The Implementation loop is about to begin work and requires a current execution plan for the selected task.
2. **Manual invocation** — An engineer or agent explicitly triggers planning for a specific task from the backlog.
3. **Plan invalidation** — A prior execution plan exists but is invalidated because the HEAD SHA has changed since the plan was produced, the selected task's context package is stale, or a dependency task has been completed and the plan must be revised.
4. **GATE-1 resolution** — A prior run was halted at GATE-1; the blocking condition has been resolved and planning resumes.

Trigger source, task ID, and timestamp must be recorded in `STATUS-004.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | The selected task is present in `task-backlog.md` with `status: accepted` | Query task catalog by task ID; assert status is `accepted` |
| PRE-2 | The LOOP-002 context package was assembled for the selected task | Read `context-metadata.md`; assert task ID matches the selected task |
| PRE-3 | The context package HEAD SHA matches the current repository HEAD SHA | Compare `context-metadata.md` recorded SHA against current `git rev-parse HEAD` |
| PRE-4 | LOOP-003 backlog is no older than 24 hours | Read `STATUS-003.md` last-run timestamp; compute age |
| PRE-5 | No concurrent LOOP-004 run is active for the same task | Read `STATUS-004.md`; assert no `status: running` entry for this task ID |
| PRE-6 | All required LOOP-001 output files are present and readable | Verify existence and readability of each required file |
| PRE-7 | The executing agent has write access to `docs/planning/` | Probe write; remove probe on success |
| PRE-8 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist at declared path |

PRE-3 failure triggers GATE-1 rather than `precondition_failed` — the human must decide whether to re-run LOOP-002 or proceed with acknowledged staleness. PRE-4 failure triggers GATE-1. All other precondition failures produce `precondition_failed` and halt without modifying any artefact.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| `docs/architecture/` directory | Read | LOOP-001 output files | Filesystem permissions of executing agent | Read-only; no modification to LOOP-001 outputs | N/A | Yes |
| `docs/context/` directory | Read | LOOP-002 context package files | Same as executing agent | Read-only | N/A | Yes |
| `docs/tasks/` directory | Read | LOOP-003 task backlog, dependency graph, task catalog | Same as executing agent | Read-only | N/A | Yes |
| Repository source files | Read | Source files referenced in LOOP-002 implementation context | Same as executing agent | Read-only; scoped to files already selected by LOOP-002 | N/A | Yes |
| ADR directory | Read | All ADR files | Same as executing agent | Read-only | N/A | Yes |
| `docs/planning/` directory | Write | All files listed in the Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/planning/` restores prior state | Yes — re-planning the same task from identical inputs produces an equivalent plan |
| `docs/loops/core/STATUS-004.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/STATUS-004.md` | Yes |
| `docs/loops/core/SKILL-004.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-004.md` | Yes |

This loop makes no writes to source code, tests, configuration, CI/CD, or any system outside the repository. It is a pure planning loop: it reads and writes planning documents only.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/core/LOOP-004-Planning.md` — this document
3. `docs/loops/core/STATUS-004.md` — prior run state (if it exists)
4. `docs/context/context-package.md` — the complete task-scoped context package
5. `docs/context/architecture-context.md` — architectural constraints for this task
6. `docs/context/dependency-context.md` — dependency contracts and graph
7. `docs/context/implementation-context.md` — selected source files
8. `docs/context/verification-context.md` — test files and acceptance criteria
9. `docs/tasks/task-backlog.md` — to confirm task selection and read `priority_factors`
10. `docs/tasks/dependency-graph.md` — task-level dependencies
11. `docs/architecture/unknowns.md` — open unknowns affecting this task
12. `docs/loops/core/SKILL-001.md` — repository technology profile
13. `.loop-004.yml` — planning configuration (if it exists)
14. Prior `docs/planning/execution-plan.md` — if replanning, to understand what has changed

The agent must not begin analysis until all available required context files have been read in full.

---

## Atomicity Standard

Every implementation step produced by this loop must satisfy the atomicity standard. A step that fails the atomicity standard must be decomposed further before the plan is considered valid.

**A step is atomic if and only if all of the following hold:**

1. **Single concern:** The step modifies or creates artefacts in service of exactly one declared purpose. A step that both changes a data model and updates the API layer violates this rule; it must be split into two steps.
2. **Completable in isolation:** The step can be completed without beginning the next step. The repository is in a coherent (though not necessarily final) state when the step ends.
3. **Independently verifiable:** A verification checkpoint exists that can confirm the step's output is correct without completing subsequent steps. If a step's correctness can only be confirmed after two more steps have run, the boundary is wrong.
4. **Bounded scope:** The step modifies files within a single module boundary, or crosses exactly one module boundary for a declared interface change. Steps that require simultaneous modification of three or more modules must be decomposed.
5. **Reversible:** The step can be individually rolled back without affecting prior completed steps. If reversing step N requires also reversing steps N-1 and N-2, they should be a single step.
6. **Duration-bounded:** A single step should be completable by an implementation agent in one uninterrupted session. Steps estimated at more than 4 hours of implementation work must be decomposed.

Steps that cannot satisfy criterion 5 (reversibility) due to the nature of the work (e.g., a data migration that cannot be partially reversed) must be explicitly marked `non-reversible` with a documented compensating action in the rollback plan.

---

## Dependency Ordering Rules

The execution sequence produced by Step 6 must satisfy the following ordering rules. Any plan that violates these rules fails VER-4.

**Rule DO-1 — Interface before implementation:** A step that modifies an interface (API contract, event schema, module boundary declaration) must precede all steps that modify implementations that depend on that interface.

**Rule DO-2 — Schema before code:** A step that modifies a database schema or event schema must precede all steps that modify code reading or writing that schema.

**Rule DO-3 — Test before red:** For task types `BUG` and `TEST`, the step that writes the failing test must precede the step that writes the fix. This enforces test-first practice and makes verification of the fix machine-checkable.

**Rule DO-4 — Shared kernel last:** Steps that modify a shared kernel or cross-cutting component must be sequenced after all steps that modify the components that depend on the shared kernel, unless the shared kernel change is the prerequisite for the other changes (in which case DO-1 applies).

**Rule DO-5 — Build before deploy:** Steps that modify build configuration must precede steps that depend on the new build output.

**Rule DO-6 — Migration before cutover:** For tasks involving data migration, the migration step must precede any step that removes the old data path. Dual-read periods must be explicitly planned as separate steps if the migration is not instantaneous.

**Rule DO-7 — Dependency task completion before dependent step:** If the task depends on another task in the LOOP-003 dependency graph that has not yet been completed, the execution plan must begin with a precondition check step that verifies the dependency task is complete. If it is not, the plan halts at that step until the dependency is resolved.

---

## Parallelisation Rules

Steps that have no dependency relationship between them are candidates for parallel execution by separate agent instances. LOOP-004 must identify parallelisable groups in `implementation-breakdown.md`. The following constraints govern parallelisation:

- Two steps may be marked as parallelisable only if they do not modify the same file.
- Two steps may be marked as parallelisable only if the output of one is not an input of the other.
- Steps in different modules with no shared interface dependency are parallelisation candidates by default.
- Steps involving schema changes are never parallelisable with steps that read or write the affected schema.
- No more than three parallel execution groups may be active simultaneously (a configurable limit in `.loop-004.yml`).
- Parallelisable steps must have independent rollback actions; if rolling back one parallel step requires rolling back another, they are not truly parallel and must be sequenced.

Each parallelisable group is recorded in `implementation-breakdown.md` as a named group with its member steps and the condition that must be satisfied for the group to begin.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `PLAN-ARCHITECT` | Maker | Steps 1–9: loads task and context, analyses architecture impact, discovers dependencies, decomposes work, sequences steps, defines checkpoints, defines rollback, records assumptions | Filesystem read, dependency graph traversal, ADR parsing, module boundary analysis | Reports to GATE-1 and GATE-2 |
| `PLAN-CHECKER` | Checker | Step 10: independently validates the plan against all verification criteria without relying on the Architect's analysis | Filesystem read, cross-reference of plan steps against module catalog, dependency map, and ADRs | Independent of PLAN-ARCHITECT; must not have participated in any planning step |
| `PLAN-WRITER` | Maker | Step 11: writes all output artefacts to `docs/planning/` | Filesystem write | Must not proceed until gate conditions from Step 10 are cleared |
| `STATUS-WRITER` | Maker | Steps 12–13: updates STATUS-004.md and SKILL-004.md | Filesystem write | None — status writes occur after all gates are cleared |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews plans involving high architectural risk, cross-cutting changes, complex rollback, or external system impact | Human judgment | Sole authority to approve or deny GATE-1 |

`PLAN-ARCHITECT` and `PLAN-CHECKER` must be separate agent instances. `PLAN-CHECKER` must not have participated in producing any section of the plan it reviews. No agent may act as both Maker and Checker for the same artefact.

---

## Workflow

### Step 1 — Load Selected Task

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** `task-backlog.md`, `task-catalog.md`, `STATUS-003.md`  
**Outputs:** Normalised task record in working memory; replanning flag  

Read the selected task record from `task-backlog.md`. Verify its `task_id`, `primary_category`, `affected_modules`, `complexity_signals`, `risk_signals`, `priority_factors`, `architectural_constraints`, and `dependencies` fields are all populated. If any required field is absent, record the gap and trigger GATE-1.

Check whether a prior execution plan exists in `docs/planning/execution-plan.md`. If a prior plan exists and its recorded task ID matches the selected task, record `replanning = true` and load the prior plan summary. Replanning occurs when the task has not changed but the repository state has (HEAD SHA changed, a dependency task completed, an architectural constraint was updated). When `replanning = true`, the Architect must identify specifically what has changed and limit re-analysis to the affected steps rather than rebuilding the plan from scratch.

Record the git HEAD SHA at this moment.

---

### Step 2 — Load Architecture Context

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** LOOP-002 context package, LOOP-001 architecture outputs  
**Outputs:** In-memory architecture context for this task  

Load the full context package from `docs/context/`. Verify that `context-metadata.md` records the same task ID as the selected task. Index the architecture context by: module name, API name, event name, schema name, ADR number, and coding convention rule.

Read `docs/architecture/unknowns.md` and identify all open unknowns with `status: open` that reference any affected module or technology named in the task. These are `planning_blockers` if their uncertainty would prevent a step from being planned deterministically. Planning blockers that cannot be resolved using available information must be declared as `unresolved_assumptions` in Step 9; if any planning blocker is of category `SEC` or `COMP`, it triggers GATE-1.

Read all accepted ADRs that apply to the task's affected modules (as listed in `architecture-context.md`). For each ADR: record the decision, the constraints it imposes on implementation, and whether any planned approach would require an ADR override. An ADR override is a GATE-1 trigger.

---

### Step 3 — Analyse Impacted Modules

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Task record, architecture context, module catalog, dependency map  
**Outputs:** Impact analysis record  

For each module listed in the task's `affected_modules`:

**Direct impact assessment:** Determine which capabilities, interfaces, and files within the module are affected. Distinguish between: files that will be modified (implementation files), files that must be read but not modified (contract files), and files that will be created (new implementation files). Record this as the module impact profile.

**Interface change assessment:** Determine whether the task changes any interface exposed by the affected module — a public API, an event schema, a shared library interface, or a database table schema. Any interface change triggers an inbound impact assessment.

**Inbound impact assessment:** For each module that is a declared consumer of the affected module (from `dependency-map.md`), assess whether the task's changes are backward-compatible. Backward compatibility is preserved if: (a) no existing method signature, event schema, or API contract is altered, or (b) all alterations are additive only (new fields with defaults, new optional endpoints, new event subtypes). If backward compatibility cannot be preserved, the affected consumer modules must be added to the implementation scope and their modifications must be planned as steps.

**Shared state assessment:** Identify any database tables, message queues, caches, or configuration values shared between affected modules and other modules not yet in scope. Shared state modifications that affect out-of-scope modules must be flagged as `shared_state_impact` and trigger GATE-2.

Record: the complete list of in-scope modules (original affected modules plus any added by inbound impact), the interface change flag, the backward compatibility assessment for each changed interface, and the shared state impact list.

---

### Step 4 — Discover Implementation Dependencies

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Impact analysis from Step 3, dependency context from LOOP-002, task dependency graph from LOOP-003  
**Outputs:** Implementation dependency record  

Identify all dependencies that govern the sequencing and safety of the implementation.

**Module-level dependencies:** For each in-scope module, list all other modules whose current interfaces must remain stable for the implementation to proceed. If any of those interfaces are themselves being changed by a concurrent task (visible in the LOOP-003 task catalog as another `status: accepted` task), record the concurrent modification risk and trigger GATE-2.

**Build-order dependencies:** Identify the build order for all affected modules as declared in the build system. Multi-module builds must respect declared compilation order. Any build step that assumes an output from a prior build step is a build-order dependency.

**Data dependencies:** For each schema modification in scope, identify: the migration sequencing (forward migration before application code that reads the new schema; application code that writes the new schema before deprecating old paths), the rollback migration, and the dual-read window if applicable.

**Test dependencies:** Identify which test files in `verification-context.md` must pass before each implementation step is considered complete. Tests that depend on multiple steps being completed are integration checkpoints, not step-level checkpoints.

**Task-level dependencies:** Read the LOOP-003 `dependency-graph.md`. For each task that the selected task depends on: verify whether that dependency task is complete (check LOOP-005/006 completion records if available). If a dependency task is incomplete, record it as a `blocking_dependency` and insert a precondition-check step at the start of the execution plan.

Detect circular dependencies within the implementation dependency graph using topological sort. Any cycle is a planning failure: a dependency cycle cannot be executed. Record the cycle and trigger GATE-1.

---

### Step 5 — Decompose Work into Atomic Steps

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Impact analysis from Step 3, implementation dependency record from Step 4, implementation context from LOOP-002  
**Outputs:** Unordered atomic step list  

Decompose the full implementation scope into atomic steps. Apply the Atomicity Standard to every proposed step. Continue decomposing until all steps satisfy all six atomicity criteria.

Each atomic step record must contain:

| Field | Description |
|-------|-------------|
| `step_id` | Sequential identifier within this plan: `STEP-NNN` |
| `title` | One imperative sentence naming the change |
| `module` | The module or modules (maximum two) modified by this step |
| `change_type` | `create` \| `modify` \| `delete` \| `migrate` \| `configure` \| `verify` \| `precondition-check` |
| `files_modified` | Explicit list of file paths that this step creates, modifies, or deletes |
| `files_read` | Explicit list of file paths read but not modified (contracts, schemas, ADRs) |
| `inputs` | The artefacts or states that must exist before this step can begin |
| `outputs` | The artefacts or states produced by this step |
| `verification_checkpoint` | The criterion that must pass before the next step begins (see Step 7) |
| `rollback_action` | The action required to undo this step if the plan is abandoned (see Step 8) |
| `reversible` | Boolean — true if the step can be independently reversed; false if `non-reversible` with compensating action |
| `parallelisable_with` | List of `step_id` values this step may execute concurrently with, or empty |
| `atomicity_evidence` | A one-sentence justification for each of the six atomicity criteria |

**Decomposition patterns by task category:**

`BUG` tasks decompose as: (1) failing test step, (2) minimal fix step, (3) regression guard step (additional tests), (4) documentation update step if the bug reveals a documentation gap.

`FEAT` tasks decompose as: (1) interface/contract definition step, (2) test definition step (tests that will fail until implementation is complete), (3) implementation step(s) per module, (4) integration checkpoint step, (5) documentation step.

`REFACTOR` tasks decompose as: (1) characterisation test step (tests that capture current behaviour before any change), (2) incremental structural change steps (one module boundary per step), (3) verification step (all characterisation tests still pass), (4) cleanup step (remove deprecated paths).

`ARCH` tasks decompose as: (1) ADR update or creation step (the decision is recorded before any code changes), (2) interface migration step, (3) consumer migration steps (one per affected consumer module), (4) integration verification step, (5) deprecation step (old paths removed or marked deprecated with a removal date).

`SEC` tasks decompose as: (1) threat model documentation step, (2) security control implementation step(s), (3) security test step, (4) penetration or boundary test step, (5) documentation and runbook step.

`SCHEMA` tasks decompose as: (1) forward migration script step, (2) rollback migration script step (written before forward migration is applied), (3) application code update step, (4) dual-read verification step (if applicable), (5) old path removal step.

For task categories not listed above, apply the general decomposition pattern: (1) precondition check, (2) preparatory steps (interface or contract changes), (3) implementation steps (one per affected module), (4) verification step, (5) cleanup step.

---

### Step 6 — Order Execution by Dependency

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Unordered atomic step list from Step 5, implementation dependency record from Step 4  
**Outputs:** Ordered execution sequence with parallelisation groups  

Apply topological sort to the atomic step list using the dependency relationships established in Step 4. Apply the seven Dependency Ordering Rules. The output is a total order of steps, with parallelisable groups identified as named execution groups.

**Execution sequence format:**

```
Phase N — [Phase Name]
  Group N.A — [Sequential steps, run in order]
    STEP-NNN — [title]
    STEP-NNN — [title]
  Group N.B — [Parallel group, run concurrently with Group N.A if applicable]
    STEP-NNN — [title]
[Gate or checkpoint between phases if applicable]
```

A **Phase** is a collection of steps that share a common purpose and after which a meaningful verification checkpoint can be placed. Phases correspond to natural seams in the implementation: "Define interfaces," "Implement consumers," "Migrate data," "Remove deprecated paths."

Each phase boundary is a candidate for a verification checkpoint. Integration checkpoints (tests that span multiple modules) must occur at phase boundaries, not within phases.

Verify the ordered sequence is acyclic (a second topological sort confirms no cycles were introduced by the ordering decisions). If a cycle is detected in the step dependency graph, it is a decomposition error from Step 5: return to Step 5 and revise the affected steps.

---

### Step 7 — Define Verification Checkpoints

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Ordered execution sequence from Step 6, verification context from LOOP-002  
**Outputs:** Verification plan draft  

Define a verification checkpoint for every atomic step and every phase boundary. Verification checkpoints are not optional; a step without a checkpoint fails VER-5.

**Step-level checkpoints:** Verify that the step's declared outputs exist and satisfy the step's completion criterion. Step-level checkpoints are always automated (Level 1 in LOOP-STANDARD §7).

**Phase-level checkpoints:** Verify that all steps in the phase collectively achieve the phase's purpose. Phase-level checkpoints may be automated (run the relevant test suite subset), Checker review (a separate agent reads the changed files and confirms correctness), or human review (for phases involving security controls, architectural decisions, or external interface changes).

For each checkpoint, specify:

| Field | Description |
|-------|-------------|
| `checkpoint_id` | `CHK-NNN`, sequential |
| `position` | After `STEP-NNN` or after `Phase N` |
| `level` | `1-automated` \| `2-checker` \| `3-human` |
| `criterion` | Falsifiable condition that must be true (not aspirational) |
| `method` | How the criterion is checked: specific command, file existence assertion, test suite name, or human review scope |
| `agent` | Which agent or role performs the check |
| `blocking` | Boolean — true if failure halts execution; false if failure is recorded and may proceed at reviewer discretion |
| `rollback_trigger` | Whether checkpoint failure triggers the rollback plan |

**Mandatory checkpoints for specific conditions:**

- Any step that modifies an API contract: a Level 2 Checker review confirming no consumer module's declared expectations are violated.
- Any step that modifies a security control: a Level 3 human review.
- Any step that modifies a database schema: a Level 1 automated check confirming the migration script is syntactically valid and the rollback script reverses the forward migration exactly.
- The final step of the plan: a Level 1 full test suite run covering all modules in scope, followed by a Level 2 Checker review of the complete diff against the plan's declared outputs.

---

### Step 8 — Define Rollback Strategy

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** Ordered execution sequence from Step 6, implementation dependency record from Step 4  
**Outputs:** Rollback plan draft  

Define the rollback strategy for each of the following scenarios:

**Per-step rollback:** For each step in the execution sequence, define the rollback action that reverses that step's changes without affecting prior completed steps. Record the rollback action in the step record (`rollback_action` field). Steps marked `reversible: false` must have a compensating action instead of a reversal.

**Phase rollback:** For each phase, define the sequence of step rollbacks (in reverse step order) that returns the repository to the state at the start of the phase. Phase rollback is the standard recovery path when a phase-level checkpoint fails.

**Full plan rollback:** Define the complete sequence of phase rollbacks (in reverse phase order) that returns the repository to its pre-plan state. The full rollback sequence must be executable independently of any partially completed implementation.

**Non-reversible step handling:** For each step marked `reversible: false`: (a) define the compensating action (e.g., data migration that cannot be reversed must have a forward-correction migration), (b) record the impact of the non-reversibility (what state is left behind and who must be notified), and (c) require explicit human approval before the step is executed (the verification checkpoint before this step must be Level 3 — human).

**External impact rollback:** If any step affects a component that is also used by external systems or other tenants (a shared database schema, a shared event topic, a publicly accessible API), document the rollback notification plan: which external parties must be notified, in what order, and how state is coordinated during the rollback.

The rollback plan must be written such that it can be executed by an agent that has not seen the original implementation. It must not rely on the implementation agent's knowledge of intermediate states.

---

### Step 9 — Record Assumptions and Risks

**Agent:** `PLAN-ARCHITECT`  
**Inputs:** All prior step outputs, open unknowns from LOOP-001, planning blockers from Step 2  
**Outputs:** Assumption and risk register for this plan  

**Assumptions:** An assumption is a statement that the plan treats as true but that is not directly verified by the available context. Every assumption must be recorded. The plan is not invalidated by the presence of assumptions, but each assumption must have a validation method: how would a false assumption be detected, and at what step would it surface?

Each assumption record must contain:
- Assumption ID (`ASSM-NNN`)
- Statement (declarative sentence of what is assumed to be true)
- Basis (why this is treated as an assumption: missing documentation, architectural unknown, inferred from code patterns)
- Validation method (the step or checkpoint at which a false assumption would be detected)
- Impact if false (which steps would need to be revised and what the revised plan would look like)

**Risks:** A risk is a condition that, if it materialises, would cause one or more plan steps to fail or to produce an incorrect output. Risks are distinct from assumptions: an assumption is uncertain input; a risk is a potential future event.

Each risk record must contain:
- Risk ID (`PLAN-RISK-NNN`)
- Description
- Likelihood (`High` / `Medium` / `Low`) — based on observable signals, not intuition
- Impact (`High` / `Medium` / `Low`) — which steps would be affected and how severely
- Trigger condition — the specific event or observation that would confirm the risk has materialised
- Mitigation — the planning or implementation action that reduces likelihood or impact
- Contingency — the revised plan action if the risk materialises during execution

**Mandatory risk categories (all must be assessed and either declared or explicitly marked not applicable):**

- Data loss or corruption risk
- Tenant isolation breach risk
- Security boundary violation risk
- Non-idempotent external write risk
- Runaway execution risk (a step that could loop or run indefinitely)
- Backward compatibility violation risk
- Concurrent modification conflict risk (another task modifying the same files)

Risks with `High` likelihood and `High` impact in categories `SEC`, `COMP`, or data loss trigger GATE-1. Risks with `High` impact in any other category, or more than five `High` likelihood risks in any category, trigger GATE-2.

---

**[GATE-1 — Hard Gate: High-Risk, ADR Override, Complex Rollback, or Cross-Cutting Impact]**

The loop halts. `STATUS-004.md` is updated to `status: awaiting_approval`. No planning artefacts beyond the metadata file are written until human approval is received. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: Shared State Impact, Concurrent Modifications, or Elevated Risk]**

The loop notifies and waits 8 hours. If no objection is received, it proceeds to Step 10. See `## Human Approval Gates` — GATE-2.

---

### Step 10 — Validate the Plan

**Agent:** `PLAN-CHECKER`  
**Inputs:** Complete plan from Steps 1–9, architecture knowledge base, gate clearance  
**Outputs:** Checker validation report  

`PLAN-CHECKER` independently validates the plan against all verification criteria. The Checker must assess the plan directly against the source artefacts (module catalog, ADRs, dependency map) — not against the Architect's analysis summaries.

The Checker produces a validation report with: pass/fail per criterion, a list of steps failing any criterion, a list of detected atomicity violations, a list of any ADR constraints violated by the plan, and an overall finding of `accepted` or `rejected`.

If `rejected`, the report enumerates every unresolved violation. Control returns to `PLAN-ARCHITECT` for one re-attempt at the affected steps (Steps 5–9 as required). If the second attempt also produces a Checker `rejected` finding, the loop triggers GATE-1.

After the Checker's accepted finding, verify the git HEAD SHA against the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1 — the repository has been modified during planning and the plan may be based on stale file content.

---

### Step 11 — Publish Execution Plan

**Agent:** `PLAN-WRITER`  
**Inputs:** Validated plan, verification plan draft, rollback plan draft, assumption and risk register, gate clearance  
**Outputs:** All files listed in the Outputs table  

Write each output artefact in full. No partial writes. Write in dependency order:

1. `dependency-plan.md` — all implementation dependencies (module-level, build-order, data, test, task-level), including blocking dependencies and concurrent modification risks
2. `rollback-plan.md` — the complete rollback strategy: per-step actions, phase rollback sequences, full plan rollback sequence, non-reversible step compensating actions, and external impact notification plan
3. `verification-plan.md` — all checkpoints with `checkpoint_id`, position, level, criterion, method, agent, blocking flag, and rollback trigger
4. `implementation-breakdown.md` — the decomposition record: all atomic steps with full step records, parallelisation map, phase definitions, assumption register, and risk register
5. `execution-plan.md` — the primary deliverable: the complete ordered execution sequence in phase/group/step format, with each step's title, change type, modules, files, inputs, outputs, and a reference to its verification checkpoint and rollback action; the plan must be self-contained — an agent reading only this file must be able to begin execution
6. `planning-metadata.md` — run provenance: task ID, plan version (incremented on replanning), run ID, LOOP-001/002/003 run IDs consumed, HEAD SHA at planning time, atomic step count, phase count, dependency edge count, parallelisation group count, assumption count, risk count, High-likelihood risk count, verification checkpoint count, effort estimate, confidence score, Checker report reference

Each file must include at its header: task ID, plan version, generation date, run ID, loop version, and a note that it is a generated artefact maintained by LOOP-004.

**Effort estimate:** Record in `planning-metadata.md` an effort estimate derived from the atomic step count, complexity signals, and risk signals. The estimate is expressed as a range (e.g., `Low: 2–4 hours`, `Medium: 4–12 hours`, `High: 12–40 hours`) based on the number of steps, the step complexity signals, and the count of High-risk items. This is an input for LOOP-003 replanning decisions; it is not a commitment.

---

### Step 12 — Update STATUS-004.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, gate outcomes, Checker report, final output file list  
**Outputs:** Updated `docs/loops/core/STATUS-004.md`  

Record all metrics declared in `## Metrics`. Record gate outcomes. Record final status: `completed`, `failed`, or `stopped`. Record the plan version and task ID so that LOOP-005 can reference the specific plan it is executing. Record open blockers: unresolved assumptions, blocking dependency tasks, non-reversible steps requiring pre-execution human approval.

---

### Step 13 — Update SKILL-004.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Planning metrics, decomposition outcomes, assumption and risk register statistics  
**Outputs:** Updated `docs/loops/core/SKILL-004.md`  

Update the skill profile with observations that improve future planning:

- Average atomic step count by task category (calibrates future decomposition depth expectations)
- Average assumption count by task category (high assumption counts signal insufficient LOOP-001 or LOOP-002 coverage for that category)
- Most frequent risk categories (drives LOOP-001 discovery focus for risk-prone modules)
- Replanning frequency (high replanning rate signals context package freshness issues in LOOP-002)
- Parallelisation rate (percentage of steps that were placed in parallel groups — a planning efficiency signal)
- Checker rejection rate on first attempt (high rate signals decomposition or dependency analysis gaps)
- Modules most frequently appearing in plans (candidates for architectural hardening via LOOP-001 unknowns resolution)

---

## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `PLAN-CHECKER` without relying on `PLAN-ARCHITECT`'s self-report.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Every atomic step satisfies all six atomicity criteria | For each step, verify all six criteria are met; assert `atomicity_evidence` field is populated for each criterion |
| VER-2 | Every step modifies files within at most two module boundaries | Count distinct module boundaries for files in each step's `files_modified` list; assert ≤ 2 per step |
| VER-3 | Every implementation dependency identified in Step 4 is represented as an ordering constraint in the execution sequence | Cross-reference the dependency record against the step ordering; assert each dependency produces an ordering edge |
| VER-4 | The execution sequence satisfies all seven Dependency Ordering Rules | Apply each rule mechanically to the step sequence; assert no rule is violated |
| VER-5 | Every atomic step has exactly one verification checkpoint | Count checkpoints per step; assert one-to-one correspondence |
| VER-6 | Every phase boundary has a phase-level verification checkpoint | Count phase boundaries; count phase-level checkpoints; assert equal |
| VER-7 | Every step has a declared rollback action or a documented non-reversible compensating action | Inspect `rollback_action` field for each step; assert non-null |
| VER-8 | No planned step would violate any accepted ADR applicable to the affected modules | Cross-reference each step's change type and files against the constraint list extracted from accepted ADRs; assert no constraint is violated |
| VER-9 | All five mandatory risk categories are assessed in the risk register | Inspect risk register; assert one entry (or explicit `not_applicable`) for each mandatory category |
| VER-10 | The plan contains at least one Level 3 (human) checkpoint for any step that modifies a security control, an external-facing API, or is marked `non-reversible` | Identify steps meeting these criteria; assert each has a Level 3 checkpoint |
| VER-11 | The execution sequence graph is acyclic | Apply topological sort to the step dependency graph; assert success (no cycle detected) |
| VER-12 | `planning-metadata.md` records the HEAD SHA at planning time, the task ID, and the LOOP-001/002/003 run IDs consumed | Read metadata file; assert all four fields are present and non-empty |
| VER-13 | `execution-plan.md` is self-contained: every step references its checkpoint ID and rollback action ID explicitly, with no unresolved cross-references | Parse execution plan; assert all checkpoint and rollback references resolve within the plan documents |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/planning/reflections/REFLECTION-004-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Decomposition Summary:** total steps produced, steps per phase, parallelisation group count, percentage of steps in parallel groups, and the decomposition patterns applied by task category
- **Dependency Summary:** total dependency edges, count of blocking dependencies, count of concurrent modification conflicts detected, count of circular dependencies detected
- **Assumption Register Summary:** total assumptions recorded, count per basis type (missing documentation, architectural unknown, inferred from code), and the highest-impact assumption
- **Risk Summary:** total risks recorded, count per likelihood/impact combination, count of GATE-1 triggers from risk evaluation, and the highest-combined-severity risk
- **Replanning Assessment:** whether `replanning = true`, what changed since the prior plan, and which steps were revised versus carried forward unchanged

---

## Human Approval Gates

### GATE-1 — Hard Gate: High-Risk, ADR Override, Circular Dependency, or External System Impact

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 9, before Step 10 (risk evaluation); after Step 10 if Checker rejects twice; after SHA check in Step 10 if concurrent change detected |
| Artefact Under Review | Draft execution plan, implementation breakdown, rollback plan, risk register |
| Approver | Principal Engineer or Architecture Owner; Security/Compliance Lead if trigger is security or compliance risk |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; `planning-metadata.md` written with partial statistics and denial reason; Reflection produced; human must resolve the blocking condition and re-trigger |
| Audit Trail | Approval record written to `STATUS-004.md` under `gate_outcomes.GATE-1`; reviewer name, role, timestamp, decision, and resolution notes recorded |

**Fires when:**
- Any planned step would violate an accepted ADR (requires ADR update or explicit override with documented rationale)
- A circular dependency is detected in the implementation dependency graph (Step 4) or the step dependency graph (Step 6)
- A `High` likelihood, `High` impact risk exists in the categories `SEC`, `COMP`, or data loss
- Any step is marked `non-reversible` and the rollback plan has no viable compensating action
- A planning blocker from Step 2 is of category `SEC` or `COMP` and remains unresolved
- The context package HEAD SHA has changed (concurrent repository modification during planning)
- A required task-level dependency (from LOOP-003 dependency graph) is incomplete and blocking execution
- LOOP-003 backlog is older than 24 hours and the selected task's priority or scope may have changed
- Checker validation rejected on both initial and retry attempts

**Reviewer guidance:** Inspect the specific trigger. For ADR conflicts: the reviewer must either approve an ADR override (with a rationale that becomes a Reflection entry) or require the plan to be revised to comply. For circular dependencies: the reviewer must break the cycle by defining a new sequencing task. For security risks: the reviewer must confirm the plan's security controls are adequate or require additional steps. Record all decisions and their rationale in `STATUS-004.md` as correction notes for the next run.

---

### GATE-2 — Soft Gate: Shared State Impact, Elevated Risk, or Concurrent Modification Risk

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 9, before Step 10 |
| Artefact Under Review | Draft execution plan, shared state impact list, risk register, concurrent modification list |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-004.yml`; defaults to writing a summary to `STATUS-004.md` and creating a draft plan summary document |
| Timeout | 8 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 10; `soft_gate_auto_proceeded: true` recorded in `STATUS-004.md` |
| Audit Trail | Notification timestamp and outcome recorded under `gate_outcomes.GATE-2` |

**Fires when (and GATE-1 did not also fire):**
- A `shared_state_impact` was identified in Step 3 (changes affect shared infrastructure)
- A concurrent modification conflict was identified in Step 4 (another accepted task modifies the same files)
- More than five `High` likelihood risks are recorded in any category
- A `High` impact risk exists in categories other than `SEC`, `COMP`, or data loss
- More than three `ASSM-NNN` entries have `Impact if false: High`

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-004.md`. The executing agent must check `STATUS-004.md` at the start of each step and halt immediately if this value is present. On emergency stop: no further writes to `docs/planning/` are made; a partial Reflection is produced noting the step at which the stop was received and what partial plan artefacts were written.

---

## Failure Recovery

### FR-1 — Incomplete Planning Information

**Detection:** A required input field in the task record, context package, or architecture knowledge base is absent or marked `unknown`, preventing a planning decision that must be made in Step 5 or later.  
**Immediate Action:** Record the gap as a planning blocker. Do not omit the affected step from the plan; instead, insert a `precondition-check` step that validates the missing information before the dependent implementation step executes.  
**Recovery:** If the blocker is resolvable by inspecting source files in the implementation context: attempt resolution. If not resolvable: record as `ASSM-NNN` with a high-severity impact rating and trigger GATE-2 (or GATE-1 if the category is `SEC` or `COMP`).  
**Rollback:** No plan artefacts are written until the blocker is resolved or the gate is cleared.

### FR-2 — Conflicting Architectural Constraints

**Detection:** Two or more accepted ADRs impose contradictory constraints on the implementation (e.g., one requires synchronous API calls; another requires all inter-module communication to be asynchronous).  
**Immediate Action:** Record the conflict with both ADR references. Do not resolve the conflict autonomously.  
**Recovery:** Trigger GATE-1. The human reviewer must either: (a) update one ADR to resolve the conflict, (b) issue a new ADR that supersedes one of the conflicting ones, or (c) approve an explicit exception for this task with a documented rationale.  
**Rollback:** No plan is published while the conflict is unresolved.

### FR-3 — Circular Step Dependencies

**Detection:** Step 6 topological sort detects a cycle in the atomic step dependency graph.  
**Immediate Action:** Record the cycle. Return to Step 5.  
**Recovery:** Revise the decomposition to break the cycle. Common resolutions: introduce an intermediate interface that both steps depend on (making both depend on the interface step instead of each other), or recognise that one step subsumes the other (merge them, then re-assess atomicity). If the cycle cannot be broken by decomposition revision, trigger GATE-1.  
**Rollback:** No plan is published while a step cycle is unresolved.

### FR-4 — Unresolved Assumptions Blocking the Plan

**Detection:** Step 9 produces one or more assumptions that, if false, would require the plan to be substantially revised (more than 30% of steps affected).  
**Immediate Action:** Record all assumptions in the register. Flag the plan as `high_assumption_risk`.  
**Recovery:** Trigger GATE-2 (or GATE-1 if a `SEC`/`COMP` assumption is unresolved). The human reviewer must either confirm the assumptions are safe to proceed with, or require a `RSRCH` task to resolve the assumptions before this plan is executed.  
**Rollback:** The plan may be published with the assumption register included, but `execution-plan.md` must carry a prominent `[HIGH ASSUMPTION RISK]` header that LOOP-005 must not ignore.

### FR-5 — Missing Verification Strategy

**Detection:** Step 7 cannot define a falsifiable verification criterion for one or more steps because no test files, API contracts, or observable outputs exist for the affected capability.  
**Immediate Action:** Record the missing verification coverage. Flag the affected steps as `unverifiable`.  
**Recovery:** Insert a `TEST` subtask into the plan as a preceding step: a step that creates the verification infrastructure before the implementation step that uses it. If this is not possible within the plan scope, trigger GATE-2 and record the gap as a `TEST` candidate task for the LOOP-003 backlog.  
**Rollback:** No step may be marked `blocking: false` at its checkpoint simply because verification infrastructure is missing — the plan must create the infrastructure or defer the step.

### FR-6 — Missing or Inadequate Rollback Plan

**Detection:** Step 8 cannot define a rollback action for a step because the change is irreversible and no compensating action exists (e.g., a schema migration that removes columns used by other services with no backward-compatible migration path).  
**Immediate Action:** Mark the step `reversible: false` with `compensating_action: none_available`.  
**Recovery:** Trigger GATE-1. The human reviewer must either: (a) redesign the step to be reversible (e.g., add a backward-compatible migration step), (b) accept the non-reversibility with an explicit documented risk, or (c) require a phased implementation that allows a grace period before the irreversible change takes effect.  
**Rollback:** No step with `compensating_action: none_available` may be included in a published plan without explicit GATE-1 approval.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-004.md` at Step 12.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of loop steps completed (of 13) |
| `run.steps_total` | 13 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-13 criteria passed |
| `verification.level1.fail` | Count of VER-1 through VER-13 criteria failed |
| `reflection.produced` | Boolean |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `plan.task_id` | The task ID being planned |
| `plan.version` | Plan version number (incremented on replanning) |
| `plan.replanning` | Boolean — was this a replan of an existing plan |
| `plan.atomic_steps_total` | Total atomic steps in the published plan |
| `plan.phases_total` | Total phases in the execution sequence |
| `plan.parallel_groups` | Count of parallelisation groups |
| `plan.parallelisable_steps` | Count of steps in parallel groups |
| `plan.sequential_steps` | Count of steps that must be sequential |
| `plan.non_reversible_steps` | Count of steps marked `reversible: false` |
| `plan.dependency_edges` | Total ordering constraints in the step dependency graph |
| `plan.blocking_dependencies` | Count of incomplete task-level dependencies |
| `plan.adr_overrides` | Count of ADR constraints that required an override decision |
| `plan.assumptions_total` | Total assumptions recorded |
| `plan.assumptions_high_impact` | Count of assumptions with `Impact if false: High` |
| `plan.risks_total` | Total risks recorded |
| `plan.risks_high_likelihood` | Count of risks with `Likelihood: High` |
| `plan.risks_high_impact` | Count of risks with `Impact: High` |
| `plan.checkpoints_total` | Total verification checkpoints |
| `plan.checkpoints_level1` | Automated checkpoints |
| `plan.checkpoints_level2` | Checker review checkpoints |
| `plan.checkpoints_level3` | Human review checkpoints |
| `plan.effort_estimate` | Effort range: `Low`, `Medium`, or `High` |
| `plan.confidence_score` | Overall plan confidence score (0–100) |
| `checker.rejected_first_attempt` | Boolean — did the Checker reject the initial plan |

---

## Risks

### RISK-1 — Overly Large Implementation Steps

- **Description:** Steps that are not truly atomic cause mid-step failures that leave the repository in an inconsistent intermediate state, requiring manual intervention to recover.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Decomposition pressure (time, complexity) leads to insufficiently granular steps.
- **Control:** Atomicity Standard is applied in Step 5 with six independently verified criteria. VER-1 and VER-2 enforce atomicity at validation time. `PLAN-CHECKER` independently assesses each step.
- **Detection:** VER-1 or VER-2 failure; LOOP-005 Reflection reporting mid-step failure.
- **Response:** Plan is revised to decompose the failing step; LOOP-004 is re-run with `replanning = true`.

### RISK-2 — Hidden Implementation Dependencies

- **Description:** A dependency between two steps is not identified in Step 4, causing them to be sequenced incorrectly. The dependent step produces incorrect output or fails when executed before its prerequisite.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Implicit module coupling (shared global state, environment variables, framework magic) not captured in the declared dependency map.
- **Control:** Step 3 shared state assessment; Step 4 explicit enumeration of build-order, data, and test dependencies; context enrichment from LOOP-002 improves coupling detection.
- **Detection:** VER-3 or VER-4 failure; LOOP-005 Reflection reporting unexpected dependency error.
- **Response:** Missing dependency edge is added; plan is re-sequenced and re-validated.

### RISK-3 — Architectural Drift During Execution

- **Description:** The repository changes between the time the plan is produced and the time LOOP-005 executes it, invalidating one or more plan steps.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Concurrent work by other engineers or agents modifies files covered by the plan between LOOP-004 completion and LOOP-005 commencement.
- **Control:** HEAD SHA recorded in `planning-metadata.md`; LOOP-005 must verify SHA before beginning execution. If SHA has changed, LOOP-004 must be re-run.
- **Detection:** LOOP-005 precondition check (HEAD SHA comparison).
- **Response:** LOOP-004 re-run with `replanning = true`; LOOP-002 re-run if context package is also stale.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop makes no writes to production systems and accesses no tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Inadequate Rollback Preparation

- **Description:** A non-reversible step is executed and the compensating action fails or is inadequate, leaving the system in a degraded state that cannot be recovered automatically.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** A schema migration removes a column before all consumers have been updated; a deployed binary is incompatible with the existing database state.
- **Control:** FR-6 procedure: GATE-1 fires for any step with `compensating_action: none_available`. Level 3 human checkpoint required before any non-reversible step executes.
- **Detection:** LOOP-005 or LOOP-006 Reflection reporting rollback failure.
- **Response:** Human-led incident recovery; post-mortem feeds into LOOP-004 `SKILL-004.md` rollback pattern improvements.

### RISK-6 — Excessive Plan Complexity

- **Description:** A plan with more than 20 atomic steps, multiple non-reversible steps, and high assumption density is unlikely to be executed without failure, and if it fails mid-execution the recovery cost is high.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Task scope was underestimated by LOOP-003; the implementation reveals significantly more complexity than the complexity signals suggested.
- **Control:** Effort estimate in `planning-metadata.md` signals complexity to LOOP-005. Plans with more than 20 steps trigger GATE-2 automatically to allow scope review before execution.
- **Detection:** Step count exceeds 20 after Step 5 decomposition.
- **Response:** Human reviewer uses GATE-2 to either approve the plan as-is or to split the task into two tasks in the LOOP-003 backlog.

### RISK-7 — Non-Idempotent External Write

- **Description:** Not applicable. This loop makes no writes outside the repository.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-8 — Unrealistic Effort Estimate

- **Description:** The effort estimate recorded in `planning-metadata.md` is significantly lower than actual implementation effort, causing LOOP-003 priority decisions based on relative effort to be incorrect for future runs.
- **Likelihood:** Medium
- **Impact:** Low
- **Trigger Condition:** Complexity signals available at planning time are insufficient to capture implementation effort accurately (e.g., hidden coupling surfaces only during implementation).
- **Control:** Effort estimates are expressed as ranges, not point estimates. SKILL-004.md tracks the relationship between plan-time estimates and LOOP-005/006 actual durations, enabling calibration.
- **Detection:** LOOP-005 and LOOP-007 Reflection duration metrics consistently exceed planning estimate ranges.
- **Response:** SKILL-004.md estimate calibration updated; LOOP-003 complexity signal table reviewed.

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | The selected task has been fully decomposed: all atomic steps satisfy the Atomicity Standard |
| SC-2 | The execution sequence satisfies all seven Dependency Ordering Rules and the step dependency graph is acyclic |
| SC-3 | All verification criteria VER-1 through VER-13 have passed |
| SC-4 | All six output artefacts listed in the Outputs table have been written |
| SC-5 | Every step has a verification checkpoint and a rollback action |
| SC-6 | All five mandatory risk categories have been assessed |
| SC-7 | `STATUS-004.md` has been updated with run metrics, plan version, and final status |
| SC-8 | `SKILL-004.md` has been updated |
| SC-9 | The Reflection artefact has been written to `docs/planning/reflections/` |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-10 | Maximum run duration (45 minutes) reached before SC-1 through SC-9 are met |
| SC-11 | GATE-1 is denied by the human reviewer |
| SC-12 | PRE-5 detects a concurrent run; this instance exits without modifying any artefact |
| SC-13 | An Emergency Stop signal is received in `STATUS-004.md` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Planning Artefacts:**
- [ ] `docs/planning/execution-plan.md` written, self-contained, and ordered by phase/group/step
- [ ] `docs/planning/implementation-breakdown.md` written with all atomic step records, parallelisation map, phase definitions, assumption register, and risk register
- [ ] `docs/planning/dependency-plan.md` written with all dependency types, blocking dependencies, and concurrent modification risks
- [ ] `docs/planning/verification-plan.md` written with one checkpoint per step and one per phase boundary, all with falsifiable criteria
- [ ] `docs/planning/rollback-plan.md` written with per-step actions, phase sequences, full plan rollback, and compensating actions for non-reversible steps
- [ ] `docs/planning/planning-metadata.md` written with task ID, plan version, HEAD SHA, LOOP run IDs consumed, and all planning statistics

**Verification:**
- [ ] All VER-1 through VER-13 criteria assessed and outcomes recorded in Reflection
- [ ] Checker validation report produced by `PLAN-CHECKER`
- [ ] No ADR constraint violated by any planned step (VER-8 passed)
- [ ] All mandatory risk categories assessed (VER-9 passed)
- [ ] Level 3 checkpoints present for all non-reversible and security-modifying steps (VER-10 passed)

**Gates:**
- [ ] Gate outcome recorded in `STATUS-004.md` for every gate that fired

**State:**
- [ ] `docs/loops/core/STATUS-004.md` updated with all required metrics, plan version, task ID, and final status
- [ ] `docs/loops/core/SKILL-004.md` updated with current planning profile

**Reflection:**
- [ ] `docs/planning/reflections/REFLECTION-004-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---

## Future Improvements

- **Plan diffing on replanning:** When `replanning = true`, produce a structured diff between the prior plan and the revised plan, making it explicit which steps were added, removed, or reordered and why. This accelerates human review of replanned work.
- **Automated atomicity scoring:** Develop a heuristic scoring function for the Atomicity Standard that evaluates each criterion from observable file metadata (line counts, import graphs, test coupling), producing a quantitative atomicity score to complement the qualitative six-criteria assessment.
- **Cross-plan dependency awareness:** Extend Step 4 to query the LOOP-003 dependency graph for tasks currently in LOOP-005 execution, detecting live concurrent modifications rather than only planned concurrent tasks.
- **Estimate calibration feedback loop:** After each LOOP-007 (Reflection) run, compare the plan's effort estimate range against actual implementation duration and update `SKILL-004.md` with a calibration factor per task category.
- **Parallelisation safety analysis:** Augment the parallelisation rules with a static analysis of shared mutable state (database tables, caches, environment variables) to identify parallelisation conflicts that are not visible from file-level analysis alone.
- **Plan template library:** Maintain a library of validated decomposition patterns (per task category, per architectural pattern) in `docs/loops/templates/` so that common task types produce consistent step structures across runs, reducing Checker rejection rates.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces the architecture knowledge base consumed in Steps 2–4
- `docs/loops/core/LOOP-002-Context-Assembly.md` — produces the task-scoped context package consumed in Steps 1–7
- `docs/loops/core/LOOP-003-Task-Discovery.md` — produces the selected task record and task dependency graph consumed in Steps 1 and 4
- `docs/loops/shared/verification-standards.md` — verification level definitions (Level 1/2/3)
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure
- `docs/loops/templates/REVIEW-TEMPLATE.md` — review record structure

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-004 as the planning loop for the AI Engineering Operating System, consuming LOOP-001, LOOP-002, and LOOP-003 outputs and producing the verified execution plan consumed by LOOP-005.

