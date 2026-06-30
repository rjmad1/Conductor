---
# PROVENANCE METADATA
Original Path: docs/loops/platform/LOOP-201-Workflow-Validation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Platform validation loop to ensure workflow definition correctness.
Generalized Purpose: Platform validation loop to ensure workflow definition correctness.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-201 — Workflow Validation

**Loop ID:** LOOP-201
**Name:** Workflow Validation
**Version:** 1.0
**Status:** Active
**Category:** Platform
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-006 — Verification
**Human Gates:** Hard, Soft
**Owner:** Platform Engineering Function
**Maintainer:** Platform Engineering Function
**Max Run Duration:** 2 hours
**Introduced In:** 2026-06-27

---

## Purpose

LOOP-201 validates that workflow definitions, orchestration configurations, and execution patterns in the repository conform to the declared workflow architecture and behavioral contracts discovered by LOOP-001. It produces a structured workflow validation report that classifies all workflow artifacts as conformant, non-conformant, or unclassified, and records any violations with sufficient detail for remediation. This loop is the authoritative pre-release signal that the workflow layer is structurally sound. It does not modify source files; it reads the repository and writes structured findings to `docs/validation/workflow/`.

---

## Problem Statement

Workflow definitions are high-risk artifacts: a workflow missing a timeout can run indefinitely; a workflow without a compensation strategy can leave data in a partially committed state; an undeclared workflow dependency can create hidden coupling that breaks under load. Without systematic validation, individual workflows may appear correct in isolation while violating cross-cutting architectural contracts. As the repository grows, manual inspection of every workflow for every required attribute becomes infeasible. Gaps accumulate silently until they cause production incidents.

---

## Why This Loop Exists

Workflow contract validation is repeatable and enumerable: every requirement — declared timeout, retry policy, compensation strategy, acyclic dependencies, service-layer boundary — can be stated as a falsifiable criterion against the current codebase. Codifying these checks as a loop makes them auditable, ensures they are applied uniformly to every workflow artifact, and produces a written record that downstream Governance loops can consume. Without this loop, workflow compliance depends on reviewer memory and is not enforced systematically between release cycles.

---

## Scope

**In scope:**
- Scanning all workflow definition files in the repository (Temporal workflow classes, state machine configuration files, pipeline definition files, saga orchestrators, and scheduler configuration)
- Verifying that each workflow type discovered by LOOP-001's module catalog has a corresponding implementation
- Checking each workflow implementation for: declared timeout, retry policy, and compensation or rollback strategy
- Checking that workflow dependency declarations are acyclic
- Verifying that no workflow implementation reads from or writes to a database directly — all data access must go through the declared service layer
- Verifying that activity implementations exist for all activity types declared in workflow definitions
- Verifying that error handling paths exist for all declared failure modes

**Out of scope:**
- Executing workflows or simulating runs
- Modifying workflow source files
- Evaluating workflow business logic correctness
- Performance profiling or load testing
- Infrastructure provisioning for workflow engines

**Maximum run duration:** 2 hours. If the loop has not terminated within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/architecture/module-catalog.md` | File | LOOP-001 run output | Required |
| `docs/architecture/architecture-overview.md` | File | LOOP-001 run output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 state file | Required — used for freshness check |
| `docs/loops/core/STATUS-006.md` | File | LOOP-006 state file | Required — used for dependency freshness check |
| Repository source files under all modules in module-catalog.md | Directory | Repository | Required |
| Previous `docs/loops/platform/STATUS-201.md` | File | Prior run artifact | Optional — absence indicates first run |
| Previous workflow validation report | File | `docs/validation/workflow/` prior run | Optional — used for drift detection |
| `.loop-201.yml` at repository root | Configuration | Repository | Optional — declares exclude paths and thresholds |

### Input Validation

Before any scan step begins, the loop must verify:
- `docs/architecture/module-catalog.md` exists, is non-empty, and its embedded HEAD SHA matches the current repository HEAD SHA. If the SHA does not match, treat as a Hard Gate condition per SPEC-001 §2.C5.
- `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` and its `last_updated` timestamp is no more than 7 days old. If the LOOP-001 output is stale (older than 7 days), treat as a Soft Gate condition before proceeding.
- `docs/loops/core/STATUS-006.md` reports `last_outcome: completed`. If LOOP-006 has not completed successfully, record the dependency as unavailable and proceed with reduced context; note the reduction in STATUS-201.md.
- No exclusive lock on `docs/validation/workflow/` exists from a concurrent run (check STATUS-201.md for `status: running`).
- The repository root is readable.

---

## Outputs

All primary outputs are written to `docs/validation/workflow/`. State files are written to `docs/loops/platform/`.

| Artifact | Path | Description |
|----------|------|-------------|
| Workflow Validation Report | `docs/validation/workflow/workflow-validation-report-{run-id}.md` | Classified findings: conformant, non-conformant, and unclassified workflow artifacts with violation details; consumed by Governance loops and release gate evaluations |
| Workflow Inventory | `docs/validation/workflow/workflow-inventory-{run-id}.md` | Complete enumeration of all discovered workflow artifacts with classification, owning module, and scan confidence score |
| Checker Validation Report | `docs/validation/workflow/checker-report-{run-id}.md` | Independent assessment by WORKFLOW-CHECKER confirming or disputing the scanner's classification findings |
| Run Metadata | `docs/validation/workflow/metadata/METADATA-201-{run-id}.md` | Provenance record: run ID, HEAD SHA at start and end, upstream dependency run IDs consumed, elapsed duration seconds, final status |
| Loop Status | `docs/loops/platform/STATUS-201.md` | Run state, metrics, gate outcomes, and open blockers for this loop |
| Loop Skill | `docs/loops/platform/SKILL-201.md` | Calibration observations accumulated across runs |
| Reflection | `docs/validation/workflow/reflections/REFLECTION-201-{run-id}.md` | Per-run structured reflection produced at end of every run regardless of outcome |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Consumes `docs/architecture/module-catalog.md` to enumerate declared workflow types and module ownership. Consumes `docs/architecture/architecture-overview.md` for declared architectural boundaries. Required; the loop cannot determine what workflow types are expected without this catalog.
- **LOOP-006 — Verification:** Consumes `docs/loops/core/STATUS-006.md` for context on recent verification activity and known open issues in the workflow module. Optional; absence does not block execution but reduces finding context.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop.
2. **Scheduled execution** — Recurring schedule (recommended: once per release cycle or once per week, whichever is more frequent).
3. **Repository event** — A pull request is merged to the main branch that modifies any file under `platform/workflow/` or any file matching `*Workflow*.java`, `*Activity*.java`, `*saga*`, or `*pipeline*` naming patterns.
4. **Upstream loop completion** — LOOP-001 completes a run and its module catalog has changed since the last LOOP-201 run.

Trigger source and timestamp must be recorded in `STATUS-201.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/architecture/module-catalog.md` exists and is non-empty | File existence check; assert file size > 0 |
| PRE-2 | `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` | Parse STATUS-001.md; assert field value |
| PRE-3 | No other instance of LOOP-201 is currently running | Read STATUS-201.md; assert `current_status != running` |
| PRE-4 | The executing agent has read access to the repository root | Verify readable access to repository root directory |
| PRE-5 | The executing agent has write access to `docs/validation/workflow/` | Attempt temporary probe write; remove on success |
| PRE-6 | Git is available and the repository has at least one commit | `git log -1` exits successfully |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All source files under repository root | Filesystem permissions of executing agent | Read-only; no source file modification | N/A — no writes to source | Yes |
| `docs/validation/workflow/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | Prior file content preserved in git; `git checkout docs/validation/workflow/` restores prior state | Yes — each file is fully regenerated per run |
| `docs/loops/platform/STATUS-201.md` | Read-Write | Single file | Same as executing agent | Single file; no cross-loop state | `git checkout docs/loops/platform/STATUS-201.md` | Yes |
| `docs/loops/platform/SKILL-201.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/SKILL-201.md` | Yes |
| Git history | Read | Current branch log | Filesystem permissions | Read-only; no commits made by this loop | N/A | Yes |

This loop makes no writes to any external system outside the repository. It does not call external APIs, write to databases, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/platform/LOOP-201-Workflow-Validation.md` — this document
3. `docs/loops/platform/STATUS-201.md` — prior run state (if it exists)
4. `docs/architecture/module-catalog.md` — workflow type declarations
5. `docs/architecture/architecture-overview.md` — declared architectural boundaries
6. `docs/loops/core/STATUS-001.md` — LOOP-001 freshness confirmation
7. Prior workflow validation report (if it exists) — for drift comparison
8. Output of `git log --oneline -10` — recent commit context

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `WORKFLOW-SCANNER` | Maker | Steps 1–5: load state, assemble context, scan all workflow artifacts, classify findings, compute gate conditions | Filesystem read, git CLI, source file parsing, annotation extraction | Reports findings to GATE-1 and GATE-2 |
| `WORKFLOW-CHECKER` | Checker | Step 6: independently verifies scanner classification by re-reading source files and re-applying each criterion; produces written checker report | Filesystem read, cross-reference of scanner findings against source | Independent of WORKFLOW-SCANNER; finding reviewed at GATE-1 if checker disputes scanner |
| `STATUS-WRITER` | Maker | Steps 7–9: writes all output artifacts, updates STATUS-201.md and SKILL-201.md, produces Reflection | Filesystem write | None — status writes occur after all gates are cleared |

`WORKFLOW-SCANNER` and `WORKFLOW-CHECKER` must be separate agent instances. `WORKFLOW-CHECKER` must not have produced any of the content it reviews. No single agent may act as both Maker and Checker for the same artifact.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

### Step 1 — Load Previous State

**Agent:** `WORKFLOW-SCANNER`
**Inputs:** `STATUS-201.md` (if present), prior workflow validation report (if present)
**Outputs:** In-memory prior state snapshot

Read `STATUS-201.md`. Extract: last run ID, last run date, last known workflow count, last known violation count. If no prior state exists, record `first_run = true`. Read the most recent workflow validation report and construct a prior-state index of conformant workflows, non-conformant workflows, and known violations. Record the git HEAD SHA at this moment for use in Step 5 to detect concurrent repository changes.

Check `STATUS-201.md` for `status: emergency_stopped` before proceeding. If present, halt immediately and produce a partial Reflection.

---

### Step 2 — Assemble Context from Dependencies

**Agent:** `WORKFLOW-SCANNER`
**Inputs:** `docs/architecture/module-catalog.md`, `docs/architecture/architecture-overview.md`, `docs/loops/core/STATUS-001.md`
**Outputs:** Context assembly record (in-memory)

Parse `module-catalog.md` to extract all modules with type `workflow`, `saga`, `pipeline`, or `orchestrator`, all declared workflow activity types, and module ownership assignments. Parse `architecture-overview.md` to extract declared workflow engine(s), service-layer boundary rules, and workflow-specific architectural decisions.

Validate LOOP-001 freshness: if `last_updated` in STATUS-001.md is older than 7 days, record `dependency_stale = true`. If the embedded HEAD SHA in `module-catalog.md` does not match current HEAD SHA, treat as Hard Gate per SPEC-001 §2.C5.

---

### Step 3 — Scan Workflow Artifacts

**Agent:** `WORKFLOW-SCANNER`
**Inputs:** Repository filesystem, context from Step 2
**Outputs:** Raw workflow artifact inventory

Traverse the repository and collect all workflow artifacts: classes implementing `@WorkflowInterface` (Temporal), `Workflow` base types, `StateMachine` or `Pipeline` contracts, saga orchestrators, and activity implementation classes. Also collect configuration files declaring workflow steps, timeouts, or retry policies. For each artifact, record: file path, artifact type, owning module, framework detected, and raw annotation content.

Apply the exclusion list from `.loop-201.yml` if present. Flag any modules declared as workflow owners in the module catalog that produced zero workflow artifacts as `expected_workflow_missing`.

---

### Step 4 — Classify Workflow Artifacts

**Agent:** `WORKFLOW-SCANNER`
**Inputs:** Raw workflow artifact inventory from Step 3, context from Step 2
**Outputs:** Classified workflow finding set

For each workflow artifact, evaluate the following criteria and record a classification:

- **W-1 — Timeout Declared:** The workflow definition declares a maximum execution timeout at the workflow level. Absence is a `Critical` violation.
- **W-2 — Retry Policy Declared:** The workflow or its activities declare a retry policy including maximum attempts and backoff interval. Absence is a `High` violation.
- **W-3 — Compensation Strategy Declared:** The workflow has a declared compensation or rollback path for each non-idempotent activity, or an explicit `no-compensation-required` annotation with rationale. Absence is a `High` violation.
- **W-4 — Dependency Acyclicity:** The directed graph of workflow-to-workflow invocations must be acyclic. Any cycle is a `Critical` violation.
- **W-5 — No Direct Data Layer Access:** Workflow classes must not import or instantiate repository, DAO, or ORM classes directly; all data access must go through the declared service layer. Violation is a `Critical` architectural boundary violation.
- **W-6 — Activity Implementations Present:** Every activity type declared in workflow definitions has a corresponding activity implementation class. A missing implementation is a `Critical` violation.
- **W-7 — Error Handling Paths Present:** Each declared failure mode has a corresponding `catch` block or error handler. Absence is a `High` violation.
- **W-8 — Workflow Registered:** Each workflow type is registered with the workflow engine in application bootstrap or configuration. An unregistered workflow is a `High` violation.

Classify each artifact as `conformant` (all criteria pass), `non-conformant` (one or more criteria fail, with criterion ID and evidence recorded), or `unclassified` (insufficient evidence to evaluate one or more criteria, with reason recorded). Compute a scan confidence score (0–100) per artifact.

---

### Step 5 — Evaluate Gate Conditions

**Agent:** `WORKFLOW-SCANNER`
**Inputs:** Classified finding set from Step 4, current git HEAD SHA
**Outputs:** Gate decision

Verify the current git HEAD SHA matches the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1 unconditionally.

Otherwise evaluate in priority order:
1. Any `Critical` violations (W-1, W-4, W-5, or W-6 failures) → GATE-1
2. Any `expected_workflow_missing` entries → GATE-1
3. Scan confidence below 70 for any artifact → GATE-1
4. `dependency_stale = true` → GATE-2
5. Any `High` violations and no Critical violations → GATE-2
6. Violation count increased by more than 5 since prior run → GATE-2
7. None of the above → proceed to Step 6

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

### Step 6 — Independent Checker Validation

**Agent:** `WORKFLOW-CHECKER`
**Inputs:** Classified finding set from Step 4, raw inventory from Step 3, repository source files
**Outputs:** Checker validation report

`WORKFLOW-CHECKER` independently verifies by re-reading each source file for each non-conformant finding and re-applying the relevant criterion. For each `Critical` violation: re-read the source file and confirm the criterion is actually violated, citing file path and line number. For conformant artifacts: spot-check a minimum of 20% (selected independently by the Checker) to verify no missed violations. For unclassified artifacts: determine whether additional source evidence exists to resolve classification.

The Checker produces a written validation report with pass/fail per verification sample, confirmed/disputed findings, and an overall determination of `accepted` or `rejected` with evidence. If `rejected`, enumerate every dispute. If Checker returns `rejected`, WORKFLOW-SCANNER performs a single retry of Step 4 for disputed items only. If retry does not resolve all disputes, trigger GATE-1.

---

**[GATE-1 — Hard Gate: Critical Violations Found or Low Confidence]**

The loop halts. `STATUS-201.md` is updated to `status: awaiting_approval`. No validation report artifact is written as authoritative until human approval is received and recorded. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: High Violations Only or Stale Dependency]**

The loop notifies and sets a 24-hour timer. If no objection is received, it proceeds to Step 7. See `## Human Approval Gates` — GATE-2.

---

### Step 7 — Produce Validation Report and Inventory

**Agent:** `STATUS-WRITER`
**Inputs:** Classified finding set (confirmed by Checker), gate clearance
**Outputs:** Workflow validation report, workflow inventory, run metadata

Write `workflow-validation-report-{run-id}.md` containing: run summary, Critical violations section (each with artifact path, criterion ID, evidence, and remediation guidance), High severity violations section, conformant artifacts summary, unclassified artifacts with reason, and comparison against prior run (new violations, resolved violations, persisting violations).

Write `workflow-inventory-{run-id}.md` with complete enumeration of all discovered workflow artifacts, classification, and scan confidence scores.

Write `METADATA-201-{run-id}.md` with provenance fields: run ID, task ID, upstream loop run IDs consumed, HEAD SHA at start and end, elapsed duration, final status.

---

### Step 8 — Update STATUS-201.md and SKILL-201.md

**Agent:** `STATUS-WRITER`
**Inputs:** All run metrics, gate outcomes, Checker report, final output file list
**Outputs:** Updated `docs/loops/platform/STATUS-201.md`, updated `docs/loops/platform/SKILL-201.md`

Record all metrics in `## Metrics`. Record gate outcomes with reviewer identity, decision, rationale, and timestamp. Record run status. Update SKILL-201.md with calibration observations from this run (common violation patterns, scan confidence characteristics, module areas warranting closer inspection).

---

### Step 9 — Produce Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All outputs from Steps 7–8
**Outputs:** `docs/validation/workflow/reflections/REFLECTION-201-{run-id}.md`

Produce the Reflection artifact containing all ten required LOOP-STANDARD sections plus the four loop-specific sections declared in `## Reflection`. The Reflection must be produced before the run is marked closed. This step executes even on failed or stopped runs.

---


**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `WORKFLOW-CHECKER` without relying on `WORKFLOW-SCANNER`'s self-report.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `workflow-validation-report-{run-id}.md` exists, is non-empty, and contains conformant, non-conformant, and unclassified sections | File existence; parse sections; assert all three headings present |
| VER-2 | `workflow-inventory-{run-id}.md` exists and lists at least as many artifacts as the count of modules declared as workflow owners in `module-catalog.md` | Count module-catalog workflow-owner entries; count inventory entries; assert inventory >= expected count |
| VER-3 | Every `Critical` violation in the report references a specific file path and criterion ID | Parse violations; assert each has non-empty `file_path` and `criterion_id` fields |
| VER-4 | No source file under any module has been modified by this run | `git diff --name-only HEAD` shows only `docs/validation/` and `docs/loops/platform/` paths |
| VER-5 | `STATUS-201.md` has been updated with the current run ID and a timestamp within 5 minutes of current time | Parse STATUS file; assert run ID and timestamp within tolerance |
| VER-6 | The Checker validation report exists and records a determination of `accepted` or `rejected` with evidence | File existence; parse determination field; assert non-empty evidence |
| VER-7 | `REFLECTION-201-{run-id}.md` exists and contains all ten LOOP-STANDARD required sections | File existence; assert presence of all ten section headings |
| VER-8 | The scan covered all modules declared as workflow owners in `module-catalog.md`, or explicitly documented a scan gap for any module it could not reach | Cross-reference module-catalog workflow-owner list against inventory coverage; assert full coverage or documented gap |
| VER-9 | No secrets values appear in any output artifact | Scan all output files against secrets pattern list: API key patterns, base64 strings > 40 chars, `password=` or `secret=` with non-placeholder values |
| VER-10 | `SKILL-201.md` has been updated with an entry referencing the current run ID | Parse SKILL file; assert entry with current run ID present |

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

At the end of every run — completed, failed, or stopped — `STATUS-WRITER` produces a Reflection at `docs/validation/workflow/reflections/REFLECTION-201-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus:

- **Violation Summary:** total Critical, High, and unclassified findings; comparison to prior run (new, resolved, persisting)
- **Coverage Summary:** count of workflow artifacts scanned per module; scan confidence scores
- **False Positive Assessment:** any Checker disputes resolved, their outcomes, and root cause if the scanner erred
- **Remediation Guidance Notes:** recurring patterns across multiple violations that feed future improvements

---

## Human Approval Gates

### GATE-1 — Hard Gate: Critical Violations Found or Low Confidence

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set (in-memory), Checker validation report |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with `status: stopped`; partial findings written to STATUS-201.md only; Reflection produced; human must schedule a new run after violations are addressed |
| Audit Trail | Approval record written to `STATUS-201.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, decision, and rationale recorded |

**Fires when:** Any Critical violation (W-1, W-4, W-5, or W-6 failure) is found; any `expected_workflow_missing` entry exists; scan confidence below 70 for any artifact; `concurrent_change_detected = true`; Checker rejects findings after retry.

**Reviewer guidance:** Confirm that Critical violations are genuine (not false positives from annotation parsing). Review the Checker report for any disputes. If findings are accurate, approve in writing and initiate remediation. If findings contain errors, deny and record correction notes in STATUS-201.md.

---

### GATE-2 — Soft Gate: High Violations Only or Stale Dependency

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set with High-severity violations only |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-201.yml`; defaults to creating a draft PR with the findings summary |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 7; `soft_gate_auto_proceeded: true` recorded in STATUS-201.md |
| Audit Trail | Notification timestamp, outcome (auto-proceeded or approved/objected), recorded under `gate_outcomes.GATE-2` |

**Fires when:** High-severity violations exist and no Critical violations exist; LOOP-001 outputs older than 7 days; violation count increased by more than 5 since prior run without Critical violations.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-201.md`. The executing agent must read `STATUS-201.md` at the start of each step and halt immediately if this value is present. On emergency stop: no further writes to `docs/validation/workflow/` are made; a partial Reflection is produced recording the step at which the stop was received and the state of outputs at that moment.

---

## Failure Recovery

### FR-1 — Incomplete Workflow Scan

**Detection:** The inventory covers fewer workflow-owner modules than declared in the module catalog, and no exclusion configuration explains the gap.
**Immediate Action:** Record `scan_incomplete = true` in STATUS-201.md. Flag uncovered modules.
**Recovery:** If gap affects more than 20% of declared workflow-owner modules, trigger GATE-1. Otherwise continue with uncovered modules recorded as `unverified_this_run` and prior run entries preserved.
**Rollback:** No report written for gap areas; prior entries not removed.

### FR-2 — Conflicting Classification Evidence

**Detection:** The same workflow artifact produces conflicting evidence for a single criterion (e.g., timeout declared at workflow level but overridden to zero in configuration).
**Immediate Action:** Classify the artifact as `unclassified` for the conflicting criterion. Record both sources of evidence.
**Recovery:** If the conflict affects a Critical criterion, trigger GATE-1. For High or lower criteria, record in the report and proceed.
**Rollback:** The conflicting criterion is excluded from conformant/non-conformant counts until resolved by a human.

### FR-3 — Scan Ceiling Reached

**Detection:** Total file count during Step 3 exceeds 500,000 or directory depth exceeds 20 levels.
**Immediate Action:** Record `scan_ceiling_reached = true`. Halt traversal of the branch that hit the limit.
**Recovery:** Record affected directories as `scan_gap`. Continue scanning remaining directories. Trigger GATE-2 with a note on the ceiling condition.
**Rollback:** No rollback needed; partial scans are valid with documented gaps.

### FR-4 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 2 hours.
**Immediate Action:** Complete the current atomic step; do not begin the next step.
**Recovery:** Write all output artifacts produced so far. Write STATUS-201.md with `status: stopped`, `reason: max_duration_exceeded`. Produce a partial Reflection. The next run re-executes fully.
**Rollback:** Not required; partial outputs are documented as such.

### FR-5 — Upstream Dependency Unavailable

**Detection:** `docs/architecture/module-catalog.md` does not exist, is empty, or its HEAD SHA mismatch led to a denied GATE-1.
**Immediate Action:** Halt with `status: precondition_failed`. Do not write any output artifact.
**Recovery:** Re-trigger LOOP-001. Re-trigger LOOP-201 after LOOP-001 completes.
**Rollback:** No output written; no rollback required.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-201.md` at Step 8.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 9) |
| `run.steps_total` | 9 |
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
| `scan.workflow_artifacts_found` | Total workflow artifacts discovered |
| `scan.modules_covered` | Count of modules whose workflow artifacts were scanned |
| `scan.modules_expected` | Count of modules declared as workflow owners in module catalog |
| `findings.critical_count` | Count of Critical severity violations |
| `findings.high_count` | Count of High severity violations |
| `findings.conformant_count` | Count of conformant workflow artifacts |
| `findings.non_conformant_count` | Count of non-conformant workflow artifacts |
| `findings.unclassified_count` | Count of unclassified workflow artifacts |
| `findings.new_since_prior_run` | Violations present in this run but not in prior run |
| `findings.resolved_since_prior_run` | Violations in prior run not present in this run |
| `checker.disputes_raised` | Count of findings disputed by WORKFLOW-CHECKER |
| `checker.disputes_confirmed` | Count of disputes confirmed as scanner errors |
| `confidence.mean_score` | Mean scan confidence score across all artifacts |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The scanner classifies non-workflow source files as workflow artifacts, inflating violation count.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Framework-specific annotations present on classes that serve dual purposes.
- **Control:** Classification criteria in Step 4 require both a framework annotation and structural evidence (class hierarchy or method signatures). Checker's spot-check catches false positives.
- **Detection:** Checker disputes in Step 6 referencing misclassified artifacts.
- **Response:** FR-2 procedure; reclassify disputed artifacts.

### RISK-2 — Architectural Drift

- **Description:** The module catalog is outdated, causing the scan to miss newly added workflow modules.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 has not run since new workflow modules were added.
- **Control:** LOOP-001 freshness check in Step 2; Soft Gate fires if outputs older than 7 days.
- **Detection:** `dependency_stale = true` flag.
- **Response:** Re-trigger LOOP-001; re-trigger LOOP-201.

### RISK-3 — Hidden Dependencies

- **Description:** Workflow artifacts in unconventional locations or using non-standard naming conventions are missed by the scan.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository uses a custom workflow framework with non-standard patterns.
- **Control:** `.loop-201.yml` extension mechanism allows engineers to declare additional scan patterns; SKILL-201.md accumulates patterns across runs.
- **Detection:** Engineer review of workflow inventory reveals missing artifacts.
- **Response:** Update `.loop-201.yml` with additional patterns; re-run.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads repository source files and writes only to `docs/validation/workflow/` and state files. It does not access runtime data, databases, or tenant-scoped storage.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** A failed mid-run write could leave a validation report in a partially written state, causing a downstream loop to consume a corrupt report.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Filesystem interruption during Step 7.
- **Control:** Each output file is written atomically (write to temp file, rename to final path). The prior run's report is preserved in git history.
- **Detection:** Downstream loop precondition check detects empty or malformed report file.
- **Response:** `git checkout docs/validation/workflow/` restores prior state; re-run LOOP-201.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent — re-running produces equivalent output for the same HEAD SHA.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** The scan might read secrets from workflow configuration files and inadvertently include them in output artifacts.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Workflow configuration files contain inline credential values.
- **Control:** VER-9 scans all output artifacts for secrets patterns before the run closes. Configuration values are never written to outputs; only configuration key names are recorded.
- **Detection:** VER-9 failure.
- **Response:** Halt; do not write the affected artifact; trigger GATE-1; record event in STATUS-201.md without reproducing the secret value.

### RISK-8 — Runaway Execution

- **Description:** An extremely large repository or deeply nested source tree causes the scan to exceed the 2-hour limit.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository exceeds scan ceiling thresholds (depth > 20, files > 500,000).
- **Control:** Scan ceiling enforced in Step 3; maximum run duration enforced by FR-4.
- **Detection:** `scan_ceiling_reached` flag or `max_duration_exceeded` status.
- **Response:** FR-4 procedure; partial outputs preserved.

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | All modules declared as workflow owners in module-catalog.md were covered by the scan, or scan gaps documented |
| SC-2 | All VER-1 through VER-10 verification criteria assessed and outcomes recorded |
| SC-3 | All workflow artifacts classified (conformant, non-conformant, or unclassified with reason) |
| SC-4 | All output artifacts listed in the Outputs table written |
| SC-5 | `STATUS-201.md` updated with run metrics and final status |
| SC-6 | `SKILL-201.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (2 hours) reached before SC-1 through SC-7 are met |
| SC-9 | GATE-1 denied by the human reviewer |
| SC-10 | PRE-3 detects a concurrent run; this instance exits without modifying any artifact |
| SC-11 | Emergency Stop signal received in `STATUS-201.md` |
| SC-12 | A required precondition (PRE-1 through PRE-6) is not met |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Validation Artifacts:**
- [ ] `docs/validation/workflow/workflow-validation-report-{run-id}.md` written and contains all three classification sections
- [ ] `docs/validation/workflow/workflow-inventory-{run-id}.md` written with all discovered artifacts enumerated
- [ ] `docs/validation/workflow/checker-report-{run-id}.md` written by WORKFLOW-CHECKER with determination recorded

**Verification:**
- [ ] All VER-1 through VER-10 criteria assessed and outcomes recorded in Reflection
- [ ] VER-4 (no source file modification) confirmed
- [ ] VER-9 (secrets scan) passed on all output artifacts

**Gates:**
- [ ] Gate outcome recorded in `STATUS-201.md` for every gate that fired

**State:**
- [ ] `docs/loops/platform/STATUS-201.md` updated with all required metrics and final status
- [ ] `docs/loops/platform/SKILL-201.md` updated with current calibration observations

**Provenance:**
- [ ] `docs/validation/workflow/metadata/METADATA-201-{run-id}.md` written with all required provenance fields

**Reflection:**
- [ ] `docs/validation/workflow/reflections/REFLECTION-201-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus four loop-specific sections

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Framework-specific rule extensions:** Support pluggable criterion sets for each workflow framework (Temporal, RajaJeevanLoopEngineering, Airflow, custom) to eliminate most `unclassified` results from non-standard annotations.
- **Dependency graph visualization:** Produce a Mermaid diagram of the workflow-to-workflow invocation graph as part of the inventory artifact, making acyclicity checks human-readable.
- **Historical trend tracking:** Maintain a rolling violation trend across the last ten runs in SKILL-201.md so Governance loops can detect improving or deteriorating workflow quality without reading all individual reports.
- **Activity coverage matrix:** Produce a cross-reference matrix of workflow types to activity implementations showing which activities are shared across workflows.
- **Runtime contract validation:** Extend scope (in a future version, as a separate loop) to validate that registered timeout values comply with declared SLA budgets from the service catalog.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard for all loop specifications
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — conformance requirements; §14 is the checklist for Active status
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces the module catalog consumed by this loop
- `docs/loops/core/LOOP-006-Verification.md` — upstream verification context
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions and Emergency Stop protocol
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version establishing LOOP-201 as the platform workflow validation loop for the AI Engineering Operating System.

