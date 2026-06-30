---
# PROVENANCE METADATA
Original Path: docs/loops/platform/LOOP-202-Integration-Validation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Platform validation loop to check third-party connector configuration.
Generalized Purpose: Platform validation loop to check third-party connector configuration.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-202 — Integration Validation

**Loop ID:** LOOP-202
**Name:** Integration Validation
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

LOOP-202 validates that integration points between platform modules conform to declared contracts, that dependencies flow in declared directions, and that no module directly references internal implementation details of another module. It produces a structured integration validation report and, when violations are found, a dependency violation report with sufficient detail for remediation. This loop is the authoritative signal that module boundary discipline is maintained. It does not modify source files; it reads the repository and writes structured findings to `docs/validation/integration/`.

---

## Problem Statement

In a multi-module platform, module boundary violations accumulate silently. A service that imports from another module's internal package creates invisible coupling; a module whose build-declared dependencies diverge from the architecture map creates a hidden integration contract; a missing circuit breaker on a synchronous cross-module call creates a latent failure mode. Without systematic validation, these violations are discovered only when they cause failures under load, data inconsistency, or deployment failures. By then, they are expensive to remediate.

---

## Why This Loop Exists

Integration contract validation is both repeatable and mechanically verifiable: build file dependency declarations, import statements, database table ownership, and synchronous call configurations are all present in the repository and can be checked against declared contracts. Codifying these checks as a loop creates an auditable record of integration health, enables trend analysis across release cycles, and ensures violations are surfaced to humans before they are merged into the main branch. Without this loop, integration discipline is enforced only through code review, which is inconsistent and does not catch issues introduced by incremental change.

---

## Scope

**In scope:**
- Verifying that module dependencies declared in build files match the module dependency map produced by LOOP-001
- Verifying that no module imports from another module's internal package (only declared public interfaces)
- Verifying that integration adapter implementations exist for all declared integration points in the architecture
- Verifying that circuit breaker, timeout, and retry configurations exist for all synchronous cross-module HTTP or RPC calls
- Verifying that each database table is owned by exactly one module (no cross-module schema writes)
- Detecting transitive dependency cycles across module boundaries

**Out of scope:**
- Executing integration tests or live integration checks
- Modifying source files or build files
- Evaluating the correctness of integration adapter logic
- Performance or latency analysis of integration points
- Third-party dependency vulnerability scanning (covered by LOOP-207)

**Maximum run duration:** 2 hours. If the loop has not terminated within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/architecture/module-catalog.md` | File | LOOP-001 run output | Required |
| `docs/architecture/dependency-map.md` | File | LOOP-001 run output | Required |
| `docs/architecture/api-catalog.md` | File | LOOP-001 run output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 state file | Required — used for freshness check |
| `docs/loops/core/STATUS-006.md` | File | LOOP-006 state file | Required — used for dependency freshness check |
| Build files for all modules (`build.gradle`, `pom.xml`, `package.json`, equivalents) | File | Repository | Required |
| Source files for all modules | Directory | Repository | Required |
| Previous `docs/loops/platform/STATUS-202.md` | File | Prior run artifact | Optional — absence indicates first run |
| Previous integration validation report | File | `docs/validation/integration/` prior run | Optional — used for drift detection |
| `.loop-202.yml` at repository root | Configuration | Repository | Optional — declares additional scan patterns and exclusions |

### Input Validation

Before any scan step begins, the loop must verify:
- `docs/architecture/module-catalog.md` and `docs/architecture/dependency-map.md` exist, are non-empty, and their embedded HEAD SHAs match the current repository HEAD SHA. SHA mismatch is treated as a Hard Gate condition per SPEC-001 §2.C5.
- `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` and `last_updated` is no more than 7 days old. Stale outputs (older than 7 days) trigger a Soft Gate before proceeding.
- No exclusive lock on `docs/validation/integration/` exists from a concurrent run (check STATUS-202.md for `status: running`).
- The repository root is readable and all module build files are accessible.

---

## Outputs

All primary outputs are written to `docs/validation/integration/`. State files are written to `docs/loops/platform/`.

| Artifact | Path | Description |
|----------|------|-------------|
| Integration Validation Report | `docs/validation/integration/integration-validation-report-{run-id}.md` | Classified findings across all integration criterion categories; consumed by Governance loops |
| Dependency Violation Report | `docs/validation/integration/dependency-violation-report-{run-id}.md` | Produced when violations are found; each violation includes source module, target module, violation type, and file path. Produced as empty-list report when no violations found |
| Checker Validation Report | `docs/validation/integration/checker-report-{run-id}.md` | Independent assessment by INTEGRATION-CHECKER confirming or disputing scanner findings |
| Run Metadata | `docs/validation/integration/metadata/METADATA-202-{run-id}.md` | Provenance record: run ID, HEAD SHA at start and end, upstream dependency run IDs, elapsed duration, final status |
| Loop Status | `docs/loops/platform/STATUS-202.md` | Run state, metrics, gate outcomes, and open blockers for this loop |
| Loop Skill | `docs/loops/platform/SKILL-202.md` | Calibration observations accumulated across runs |
| Reflection | `docs/validation/integration/reflections/REFLECTION-202-{run-id}.md` | Per-run structured reflection produced at end of every run regardless of outcome |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Consumes `module-catalog.md` for the declared module list, `dependency-map.md` for the declared dependency graph, and `api-catalog.md` for declared public interfaces. Required; without these, no baseline exists against which to validate integration contracts.
- **LOOP-006 — Verification:** Consumes `STATUS-006.md` for recent verification context and known open issues. Optional; absence does not block execution.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop.
2. **Scheduled execution** — Recurring schedule (recommended: once per release cycle or once per week, whichever is more frequent).
3. **Repository event** — A pull request is merged to the main branch that modifies any build file (`build.gradle`, `pom.xml`, `package.json`) or any `import` statement in a module's source files.
4. **Upstream loop completion** — LOOP-001 completes a run and its dependency map has changed since the last LOOP-202 run.

Trigger source and timestamp must be recorded in `STATUS-202.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/architecture/module-catalog.md` and `docs/architecture/dependency-map.md` exist and are non-empty | File existence check; assert file size > 0 for each |
| PRE-2 | `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` | Parse STATUS-001.md; assert field value |
| PRE-3 | No other instance of LOOP-202 is currently running | Read STATUS-202.md; assert `current_status != running` |
| PRE-4 | The executing agent has read access to the repository root and all module directories | Verify readable access to root and a sample of module directories |
| PRE-5 | The executing agent has write access to `docs/validation/integration/` | Attempt temporary probe write; remove on success |
| PRE-6 | Git is available and the repository has at least one commit | `git log -1` exits successfully |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All source and build files under repository root | Filesystem permissions of executing agent | Read-only; no source file modification | N/A | Yes |
| `docs/validation/integration/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/validation/integration/` restores prior state | Yes — each file fully regenerated per run |
| `docs/loops/platform/STATUS-202.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/STATUS-202.md` | Yes |
| `docs/loops/platform/SKILL-202.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/SKILL-202.md` | Yes |
| Git history | Read | Current branch log | Filesystem permissions | Read-only; no commits made by this loop | N/A | Yes |

This loop makes no writes to any external system outside the repository. It does not call external APIs, write to databases, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/platform/LOOP-202-Integration-Validation.md` — this document
3. `docs/loops/platform/STATUS-202.md` — prior run state (if it exists)
4. `docs/architecture/module-catalog.md` — declared module list and ownership
5. `docs/architecture/dependency-map.md` — declared dependency graph
6. `docs/architecture/api-catalog.md` — declared public interfaces
7. `docs/loops/core/STATUS-001.md` — LOOP-001 freshness confirmation
8. Prior integration validation report (if it exists) — for drift comparison
9. Output of `git log --oneline -10` — recent commit context

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `INTEGRATION-SCANNER` | Maker | Steps 1–5: load state, assemble context, scan build files and source imports, classify integration findings, compute gate conditions | Filesystem read, git CLI, build file parsing, import analysis | Reports findings to GATE-1 and GATE-2 |
| `INTEGRATION-CHECKER` | Checker | Step 6: independently verifies scanner findings by re-reading source files for each violation and re-applying the relevant criterion; produces written checker report | Filesystem read, cross-reference of findings against source and build files | Independent of INTEGRATION-SCANNER; finding reviewed at GATE-1 if checker disputes scanner |
| `STATUS-WRITER` | Maker | Steps 7–9: writes all output artifacts, updates STATUS-202.md and SKILL-202.md, produces Reflection | Filesystem write | None — status writes occur after all gates are cleared |

`INTEGRATION-SCANNER` and `INTEGRATION-CHECKER` must be separate agent instances. No single agent may act as both Maker and Checker for the same artifact.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

### Step 1 — Load Previous State

**Agent:** `INTEGRATION-SCANNER`
**Inputs:** `STATUS-202.md` (if present), prior integration validation report (if present)
**Outputs:** In-memory prior state snapshot

Read `STATUS-202.md`. Extract: last run ID, last run date, last known violation count, last known module count. If no prior state exists, record `first_run = true`. Construct a prior-state index of known violations and conformant integration points. Record the git HEAD SHA at this moment for use in Step 5 to detect concurrent repository changes.

Check `STATUS-202.md` for `status: emergency_stopped` before proceeding. If present, halt immediately and produce a partial Reflection.

---

### Step 2 — Assemble Context from Dependencies

**Agent:** `INTEGRATION-SCANNER`
**Inputs:** `docs/architecture/module-catalog.md`, `docs/architecture/dependency-map.md`, `docs/architecture/api-catalog.md`, `docs/loops/core/STATUS-001.md`
**Outputs:** Context assembly record (in-memory)

Parse `module-catalog.md` to extract: all declared modules with their types and ownership. Parse `dependency-map.md` to extract: the declared directed dependency graph between modules (the allowed dependency edges). Parse `api-catalog.md` to extract: all public interfaces and their owning modules.

Validate LOOP-001 freshness: if `last_updated` in STATUS-001.md is older than 7 days, record `dependency_stale = true`. If embedded HEAD SHAs in catalog files do not match current HEAD SHA, treat as Hard Gate per SPEC-001 §2.C5.

---

### Step 3 — Scan Build Files and Compute Actual Dependency Graph

**Agent:** `INTEGRATION-SCANNER`
**Inputs:** Repository build files, context from Step 2
**Outputs:** Actual dependency graph derived from build files

Parse every build file in the repository. For each module, extract its declared dependencies on other modules in the repository. Build an actual dependency graph from these declarations and compare it against the declared dependency map from LOOP-001.

Identify:
- **Undeclared dependencies:** edges present in actual build graph but absent from declared dependency map
- **Excess declarations:** edges present in declared dependency map but absent from actual build graph (informational only; not a violation)
- **Transitive dependency cycles:** apply cycle detection algorithm to the actual dependency graph

---

### Step 4 — Scan Source Imports and Integration Configurations

**Agent:** `INTEGRATION-SCANNER`
**Inputs:** Repository source files, actual dependency graph from Step 3, context from Step 2
**Outputs:** Import analysis and integration configuration findings

For each module pair where module A depends on module B (per the actual dependency graph), scan module A's source files to determine whether imports target only declared public interfaces of module B or whether they reach into internal packages.

An import is a boundary violation if: the imported type's package path contains `internal`, `impl`, `core.internal`, or is located in a sub-package not listed as a public interface in the api-catalog for module B.

Additionally, scan for:
- **Synchronous cross-module calls:** identify HTTP client calls or RPC stubs that cross module boundaries. For each, check whether a circuit breaker, timeout, and retry configuration are present in the same class or its configuration.
- **Database schema ownership:** parse schema migration files and ORM entity definitions. For each database table, identify which module's code writes to it. Flag any table written by more than one module.

Classify each integration point as:
- `conformant` — all applicable criteria pass
- `non-conformant` — one or more criteria fail, with criterion ID, file path, and evidence recorded
- `unclassified` — insufficient evidence to evaluate one or more criteria

---

### Step 5 — Evaluate Gate Conditions

**Agent:** `INTEGRATION-SCANNER`
**Inputs:** Classified finding set from Steps 3–4, current git HEAD SHA
**Outputs:** Gate decision

Verify current git HEAD SHA matches the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1.

Otherwise evaluate in priority order:
1. Any architectural boundary violation (undeclared cross-module import from internal package) → GATE-1
2. Any transitive dependency cycle → GATE-1
3. Any table with multi-module write access → GATE-1
4. Any undeclared build dependency (not in declared dependency map) → GATE-1
5. `dependency_stale = true` → GATE-2
6. Any missing circuit breaker, timeout, or retry configuration on synchronous calls (without Critical violations) → GATE-2
7. Violation count increased by more than 3 since prior run → GATE-2
8. None of the above → proceed to Step 6

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

### Step 6 — Independent Checker Validation

**Agent:** `INTEGRATION-CHECKER`
**Inputs:** Classified finding set from Steps 3–4, repository source files and build files
**Outputs:** Checker validation report

`INTEGRATION-CHECKER` independently verifies by re-reading source files for each non-conformant finding and re-applying the relevant criterion. For each architectural boundary violation: re-read the offending import statement and confirm the imported type's package is genuinely internal (not a false positive from package naming). For each cycle: independently traverse the dependency graph from the alleged cycle entry point to confirm the cycle exists. For conformant integration points: spot-check 20% of conformant findings (selected independently by the Checker).

The Checker produces a written validation report with pass/fail per sample, confirmed/disputed findings, and an overall determination of `accepted` or `rejected` with evidence. If `rejected`, INTEGRATION-SCANNER performs a single retry of Steps 3–4 for disputed items. If disputes remain, trigger GATE-1.

---

**[GATE-1 — Hard Gate: Architectural Boundary Violation Found]**

The loop halts. `STATUS-202.md` is updated to `status: awaiting_approval`. No validation report artifact is written as authoritative until human approval is received and recorded. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: Non-Critical Integration Gaps or Stale Dependency]**

The loop notifies and sets a 24-hour timer. If no objection is received, it proceeds to Step 7. See `## Human Approval Gates` — GATE-2.

---

### Step 7 — Produce Validation Reports

**Agent:** `STATUS-WRITER`
**Inputs:** Classified finding set (confirmed by Checker), gate clearance
**Outputs:** Integration validation report, dependency violation report, run metadata

Write `integration-validation-report-{run-id}.md` containing: run summary, architectural boundary violations section, dependency graph violations section, synchronous call configuration gaps section, schema ownership violations section, conformant integration points summary, comparison against prior run.

Write `dependency-violation-report-{run-id}.md` with each violation listed with: source module, target module, violation type (import boundary, undeclared build dependency, dependency cycle, schema ownership conflict), file path, and recommended remediation. Produce this report as an empty-list document when no violations are found.

Write `METADATA-202-{run-id}.md` with all required provenance fields.

---

### Step 8 — Update STATUS-202.md and SKILL-202.md

**Agent:** `STATUS-WRITER`
**Inputs:** All run metrics, gate outcomes, Checker report
**Outputs:** Updated `docs/loops/platform/STATUS-202.md`, updated `docs/loops/platform/SKILL-202.md`

Record all metrics. Record gate outcomes with reviewer identity, decision, rationale, and timestamp. Update SKILL-202.md with calibration observations: common violation types, modules that recurrently produce violations, and patterns warranting closer attention in future runs.

---

### Step 9 — Produce Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All outputs from Steps 7–8
**Outputs:** `docs/validation/integration/reflections/REFLECTION-202-{run-id}.md`

Produce the Reflection artifact containing all ten required LOOP-STANDARD sections plus four loop-specific sections. Produce this artifact even on failed or stopped runs.

---


**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `integration-validation-report-{run-id}.md` exists and is non-empty | File existence; assert file size > 0 |
| VER-2 | `dependency-violation-report-{run-id}.md` exists (either with violations or empty-list declaration) | File existence; assert the file contains either a populated violations list or an explicit empty-list statement |
| VER-3 | Every violation in the dependency violation report references a specific source module, target module, and file path | Parse violations; assert each has non-empty `source_module`, `target_module`, and `file_path` fields |
| VER-4 | The actual dependency graph covers all modules declared in `module-catalog.md` | Count module-catalog entries; assert all modules appear in the dependency graph analysis |
| VER-5 | No source file under any module has been modified by this run | `git diff --name-only HEAD` shows only `docs/validation/` and `docs/loops/platform/` paths |
| VER-6 | `STATUS-202.md` has been updated with the current run ID and a timestamp within 5 minutes of current time | Parse STATUS file; assert run ID and timestamp within tolerance |
| VER-7 | The Checker validation report exists and records a determination with evidence | File existence; parse determination field; assert non-empty evidence |
| VER-8 | `REFLECTION-202-{run-id}.md` exists and contains all ten LOOP-STANDARD required sections | File existence; assert all ten section headings present |
| VER-9 | No secrets values appear in any output artifact | Scan all output files against secrets patterns: API key patterns, base64 strings > 40 chars, `password=` or `secret=` with non-placeholder values |
| VER-10 | `SKILL-202.md` has been updated with an entry referencing the current run ID | Parse SKILL file; assert entry with current run ID present |

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

At the end of every run, `STATUS-WRITER` produces a Reflection at `docs/validation/integration/reflections/REFLECTION-202-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus:

- **Violation Summary:** count of each violation type (boundary violations, dependency cycle, schema conflicts, call configuration gaps); comparison to prior run
- **Coverage Summary:** modules covered in the scan; any modules skipped with reason
- **Checker Dispute Summary:** disputes raised by INTEGRATION-CHECKER and their resolution
- **Integration Health Trend:** whether the total violation count is increasing, decreasing, or stable compared to the prior three runs (if SKILL data is available)

---

## Human Approval Gates

### GATE-1 — Hard Gate: Architectural Boundary Violation Found

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set, Checker validation report |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with `status: stopped`; partial findings written to STATUS-202.md only; Reflection produced |
| Audit Trail | Approval record written to `STATUS-202.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, decision, and rationale recorded |

**Fires when:** Any architectural boundary violation (internal package import or undeclared build dependency); any transitive dependency cycle; any table with multi-module write access; `concurrent_change_detected = true`; Checker rejects findings after retry.

**Reviewer guidance:** Confirm violations are genuine. Architectural boundary violations and dependency cycles require remediation before the next release. Approve to record findings as authoritative; deny if findings contain false positives (record correction notes in STATUS-202.md).

---

### GATE-2 — Soft Gate: Non-Critical Integration Gaps or Stale Dependency

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set with non-critical gaps only |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-202.yml`; defaults to creating a draft PR with findings summary |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 7; `soft_gate_auto_proceeded: true` recorded in STATUS-202.md |
| Audit Trail | Notification timestamp, outcome, recorded under `gate_outcomes.GATE-2` |

**Fires when:** Missing circuit breaker, timeout, or retry configurations on synchronous calls (with no Critical violations); stale LOOP-001 dependency; violation count increased by more than 3 since prior run without Critical violations.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-202.md`. The executing agent checks this at the start of each step and halts immediately if present. A partial Reflection is produced recording the step at which the stop was received.

---

## Failure Recovery

### FR-1 — Incomplete Module Scan

**Detection:** The actual dependency graph covers fewer modules than declared in module-catalog.md, and no exclusion explains the gap.
**Immediate Action:** Record `scan_incomplete = true`. Flag uncovered modules.
**Recovery:** If gap exceeds 20% of declared modules, trigger GATE-1. Otherwise continue with uncovered modules recorded as `unverified_this_run`.
**Rollback:** No report written for gap areas; prior entries not removed.

### FR-2 — Build File Parse Failure

**Detection:** A module's build file cannot be parsed (malformed XML, YAML, or Gradle syntax).
**Immediate Action:** Record the module as `parse_failed`. Flag it as `unclassified` in the dependency graph.
**Recovery:** If the failing module is a core platform module, trigger GATE-1. For peripheral modules, trigger GATE-2 with parse failure noted.
**Rollback:** No rollback needed; parsing is read-only.

### FR-3 — Cycle Detection Timeout

**Detection:** The dependency cycle detection algorithm has not terminated within 10 minutes of starting.
**Immediate Action:** Record `cycle_detection_timeout = true`. Halt cycle detection.
**Recovery:** Record all modules involved in the partially explored graph. Trigger GATE-1 with a note that cycle detection was incomplete.
**Rollback:** No rollback needed.

### FR-4 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 2 hours.
**Immediate Action:** Complete the current atomic step; do not begin the next step.
**Recovery:** Write all output artifacts produced so far. Write STATUS-202.md with `status: stopped`, `reason: max_duration_exceeded`. Produce a partial Reflection.
**Rollback:** Not required; partial outputs are documented as such.

### FR-5 — Upstream Dependency Unavailable

**Detection:** `docs/architecture/module-catalog.md` or `docs/architecture/dependency-map.md` does not exist or is empty.
**Immediate Action:** Halt with `status: precondition_failed`. Do not write any output artifact.
**Recovery:** Re-trigger LOOP-001. Re-trigger LOOP-202 after LOOP-001 completes.
**Rollback:** No output written; no rollback required.

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
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-10 criteria that passed |
| `verification.level1.fail` | Count of VER-1 through VER-10 criteria that failed |
| `reflection.produced` | Boolean — was the Reflection artifact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `scan.modules_analyzed` | Total modules included in the integration scan |
| `scan.build_files_parsed` | Total build files successfully parsed |
| `findings.boundary_violations` | Count of internal package import violations |
| `findings.undeclared_dependencies` | Count of build dependencies not in declared dependency map |
| `findings.dependency_cycles` | Count of cycles detected in the dependency graph |
| `findings.schema_ownership_conflicts` | Count of tables with multi-module write access |
| `findings.missing_call_configs` | Count of synchronous cross-module calls missing circuit breaker, timeout, or retry config |
| `findings.conformant_count` | Count of conformant integration points |
| `findings.new_since_prior_run` | Violations present in this run but not prior run |
| `findings.resolved_since_prior_run` | Violations in prior run not present in this run |
| `checker.disputes_raised` | Count of findings disputed by INTEGRATION-CHECKER |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The scanner flags test-scope or dev-scope imports as production boundary violations, producing false positives.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Test classes import internal packages of other modules for test fixture access.
- **Control:** Classification criteria explicitly exclude test source directories from the boundary violation check. The Checker's spot-check catches misclassifications.
- **Detection:** Checker disputes referencing test-scope imports.
- **Response:** FR-2 procedure for build file scope clarification; update `.loop-202.yml` exclusion list.

### RISK-2 — Architectural Drift

- **Description:** The declared dependency map in LOOP-001 outputs is outdated, causing false positives or missed violations.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 has not run since the dependency architecture changed.
- **Control:** Freshness check in Step 2; Soft Gate fires if outputs older than 7 days.
- **Detection:** `dependency_stale = true` flag.
- **Response:** Re-trigger LOOP-001; re-trigger LOOP-202.

### RISK-3 — Hidden Dependencies

- **Description:** Runtime dependency injection or reflection-based instantiation creates cross-module dependencies not visible in import statements or build files.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository uses extensive runtime DI with no compile-time interface declarations.
- **Control:** SKILL-202.md accumulates observations about DI patterns; engineers can annotate known runtime integration points via `.loop-202.yml`.
- **Detection:** Integration failures at runtime that were not caught by this loop.
- **Response:** Update `.loop-202.yml` to add runtime integration declarations; improve scan patterns in next loop version.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads repository source files and writes only to `docs/validation/integration/` and state files. It does not access runtime data, databases, or tenant-scoped storage.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** A partially written validation report could mislead a downstream loop into treating incomplete findings as authoritative.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Filesystem interruption during Step 7.
- **Control:** Output files written atomically (write to temp, rename to final). Prior run's report preserved in git history.
- **Detection:** Downstream loop precondition check detects empty or malformed report.
- **Response:** `git checkout docs/validation/integration/` restores prior state; re-run LOOP-202.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent — re-running produces equivalent output for the same HEAD SHA.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** The scan might read credentials from build configuration files and inadvertently include them in output artifacts.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Build files contain embedded credential values (a governance issue, but this loop must not amplify exposure).
- **Control:** VER-9 scans all output artifacts for secrets patterns. Configuration values are never written to outputs; only key names are recorded.
- **Detection:** VER-9 failure.
- **Response:** Halt; do not write the affected artifact; trigger GATE-1; record event without reproducing the secret value.

### RISK-8 — Runaway Execution

- **Description:** Cycle detection on a very large or densely connected dependency graph runs beyond the time budget.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository with hundreds of modules and many cross-dependencies.
- **Control:** FR-3 (cycle detection timeout at 10 minutes); FR-4 (maximum run duration at 2 hours).
- **Detection:** `cycle_detection_timeout = true` or `max_duration_exceeded` status.
- **Response:** FR-3 or FR-4 procedure as applicable.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All declared modules were covered in the scan, or scan gaps documented |
| SC-2 | All VER-1 through VER-10 verification criteria assessed and outcomes recorded |
| SC-3 | All integration points classified (conformant, non-conformant, or unclassified with reason) |
| SC-4 | All output artifacts listed in the Outputs table written |
| SC-5 | `STATUS-202.md` updated with run metrics and final status |
| SC-6 | `SKILL-202.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (2 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | PRE-3 detects a concurrent run |
| SC-11 | Emergency Stop signal received in `STATUS-202.md` |
| SC-12 | A required precondition (PRE-1 through PRE-6) is not met |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Validation Artifacts:**
- [ ] `docs/validation/integration/integration-validation-report-{run-id}.md` written and non-empty
- [ ] `docs/validation/integration/dependency-violation-report-{run-id}.md` written (even as empty-list document)
- [ ] `docs/validation/integration/checker-report-{run-id}.md` written by INTEGRATION-CHECKER with determination recorded

**Verification:**
- [ ] All VER-1 through VER-10 criteria assessed and outcomes recorded in Reflection
- [ ] VER-5 (no source file modification) confirmed
- [ ] VER-9 (secrets scan) passed on all output artifacts

**Gates:**
- [ ] Gate outcome recorded in `STATUS-202.md` for every gate that fired

**State:**
- [ ] `docs/loops/platform/STATUS-202.md` updated with all required metrics and final status
- [ ] `docs/loops/platform/SKILL-202.md` updated

**Provenance:**
- [ ] `docs/validation/integration/metadata/METADATA-202-{run-id}.md` written

**Reflection:**
- [ ] `docs/validation/integration/reflections/REFLECTION-202-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus four loop-specific sections

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Incremental scanning:** When only a small set of modules changed since the last run, limit dependency graph recomputation to affected modules rather than full traversal.
- **Public interface contract generation:** Extend scope to automatically generate interface contract declarations from annotated public classes, making the api-catalog self-maintaining.
- **Visualization artifact:** Produce a Mermaid diagram of the actual dependency graph alongside the report, highlighting violations in a distinct color.
- **Runtime dependency tracing:** Extend scope to analyze dependency injection container configurations to surface runtime cross-module dependencies not visible at compile time.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard for all loop specifications
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — conformance requirements
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces module catalog and dependency map consumed by this loop
- `docs/loops/core/LOOP-006-Verification.md` — upstream verification context
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions and Emergency Stop protocol
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version establishing LOOP-202 as the platform integration validation loop for the AI Engineering Operating System.

