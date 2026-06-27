---
# PROVENANCE METADATA
Original Path: docs/loops/platform/LOOP-204-API-Contract-Validation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Platform validation loop to verify REST/gRPC API contract compliance.
Generalized Purpose: Platform validation loop to verify REST/gRPC API contract compliance.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-204 — API Contract Validation

**Loop ID:** LOOP-204
**Name:** API Contract Validation
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

LOOP-204 validates that API implementations conform to their declared contracts (OpenAPI specifications, gRPC proto files, or interface declarations), that deprecated endpoints are not in use by callers within the monorepo, and that all publicly documented APIs have corresponding specification files. It produces a structured API validation report and, when violations are found, an unspecified API report and a breaking change report. This loop is the authoritative pre-release signal that the API layer is contractually sound and that no breaking changes have been introduced without a version increment. It does not modify source files; it reads the repository and writes structured findings to `docs/validation/api/`.

---

## Problem Statement

API contracts are implicit commitments to callers. A REST controller that drifts from its OpenAPI specification causes runtime failures in any client that generated code from that spec. A deprecated endpoint called by a monorepo peer creates a release ordering dependency that teams discover only when the deprecated endpoint is removed. An unversioned breaking change — a renamed field, a removed parameter, a changed HTTP method — silently breaks callers at the next deployment. Without systematic validation, API contract integrity depends on developer memory and spot-check code review, neither of which scales to a growing platform.

---

## Why This Loop Exists

API contract validation is both mechanically tractable and high-impact: every REST endpoint annotation, every gRPC service method signature, and every interface declaration in the repository is a precise, machine-readable contract that can be compared against its specification. Codifying this comparison as a loop makes it systematic, repeatable, and auditable. It catches breaking changes the moment they appear in source, before they reach a deployed environment where rollback is expensive.

---

## Scope

**In scope:**
- Verifying that every REST controller endpoint or gRPC service method has a corresponding specification entry in the API catalog produced by LOOP-001
- Verifying that implementation annotations match specification declarations (HTTP method, path, request body type, response type)
- Verifying that deprecated endpoints are annotated and their sunset date is declared
- Verifying that no caller within the monorepo calls a deprecated endpoint
- Verifying that API versioning strategy is applied consistently (all v1 endpoints under `/v1/`, all v2 endpoints under `/v2/`, etc.)
- Verifying that error response shapes match the declared error schema
- Verifying that authentication and authorization annotations are present on all non-public endpoints
- Detecting breaking changes between the current implementation and the prior run's implementation snapshot (field removal, type change, required parameter addition, HTTP method change)

**Out of scope:**
- Executing API calls or live contract testing
- Modifying API specification files or implementation code
- Performance or latency analysis
- External API client validation (only callers within the monorepo)
- Infrastructure provisioning or deployment

**Maximum run duration:** 2 hours. If the loop has not terminated within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/architecture/api-catalog.md` | File | LOOP-001 run output | Required |
| `docs/architecture/module-catalog.md` | File | LOOP-001 run output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 state file | Required — used for freshness check |
| `docs/loops/core/STATUS-006.md` | File | LOOP-006 state file | Required — used for dependency freshness check |
| OpenAPI specification files (`*.yaml`, `*.yml`, `*.json` in `api/`, `docs/api/`, or declared spec directories) | File | Repository | Required |
| gRPC proto files (`*.proto`) or equivalent interface declaration files | File | Repository | Required if gRPC services are declared in the api-catalog |
| Repository source files for all modules (controller and client classes) | Directory | Repository | Required |
| Previous `docs/loops/platform/STATUS-204.md` | File | Prior run artifact | Optional — absence indicates first run |
| Previous API validation report | File | `docs/validation/api/` prior run | Optional — used for breaking change comparison |
| `.loop-204.yml` at repository root | Configuration | Repository | Optional — declares spec directory locations and versioning patterns |

### Input Validation

Before any scan step begins, the loop must verify:
- `docs/architecture/api-catalog.md` exists, is non-empty, and its embedded HEAD SHA matches the current repository HEAD SHA. SHA mismatch is treated as a Hard Gate condition per SPEC-001 §2.C5.
- `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` and `last_updated` is no more than 7 days old. Stale outputs trigger a Soft Gate before proceeding.
- At least one OpenAPI specification file or proto file is readable (inferred from repository structure or `.loop-204.yml`).
- No exclusive lock on `docs/validation/api/` exists from a concurrent run (check STATUS-204.md for `status: running`).

---

## Outputs

All primary outputs are written to `docs/validation/api/`. State files are written to `docs/loops/platform/`.

| Artifact | Path | Description |
|----------|------|-------------|
| API Validation Report | `docs/validation/api/api-validation-report-{run-id}.md` | Classified findings for all API endpoints: conformant, non-conformant, unclassified, with violation details |
| Unspecified API Report | `docs/validation/api/unspecified-api-report-{run-id}.md` | Always produced: lists endpoints found in implementation but absent from any specification file, or empty-list statement if none found |
| Breaking Change Report | `docs/validation/api/breaking-change-report-{run-id}.md` | Always produced: lists breaking changes detected between current implementation and prior run snapshot, or empty-list statement if none found |
| Checker Validation Report | `docs/validation/api/checker-report-{run-id}.md` | Independent assessment by API-CHECKER |
| Run Metadata | `docs/validation/api/metadata/METADATA-204-{run-id}.md` | Provenance record: run ID, HEAD SHA at start and end, upstream dependency run IDs, elapsed duration, final status |
| Loop Status | `docs/loops/platform/STATUS-204.md` | Run state, metrics, gate outcomes, and open blockers |
| Loop Skill | `docs/loops/platform/SKILL-204.md` | Calibration observations accumulated across runs |
| Reflection | `docs/validation/api/reflections/REFLECTION-204-{run-id}.md` | Per-run structured reflection produced regardless of outcome |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Consumes `api-catalog.md` as the baseline of declared APIs and their contracts; consumes `module-catalog.md` for module ownership. Required.
- **LOOP-006 — Verification:** Consumes `STATUS-006.md` for recent verification context. Optional; absence does not block execution.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop.
2. **Scheduled execution** — Recurring schedule (recommended: once per release cycle or once per week, whichever is more frequent).
3. **Repository event** — A pull request is merged to the main branch that modifies any OpenAPI specification file, proto file, REST controller class, or gRPC service implementation.
4. **Upstream loop completion** — LOOP-001 completes a run and its API catalog has changed since the last LOOP-204 run.

Trigger source and timestamp must be recorded in `STATUS-204.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/architecture/api-catalog.md` exists and is non-empty | File existence check; assert file size > 0 |
| PRE-2 | `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` | Parse STATUS-001.md; assert field value |
| PRE-3 | No other instance of LOOP-204 is currently running | Read STATUS-204.md; assert `current_status != running` |
| PRE-4 | The executing agent has read access to the repository root | Verify readable access |
| PRE-5 | The executing agent has write access to `docs/validation/api/` | Attempt temporary probe write; remove on success |
| PRE-6 | At least one specification file (OpenAPI YAML or proto) is readable | Assert at least one matching file is accessible |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All source and specification files under repository root | Filesystem permissions of executing agent | Read-only; no source file modification | N/A | Yes |
| `docs/validation/api/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/validation/api/` restores prior state | Yes — each file fully regenerated per run |
| `docs/loops/platform/STATUS-204.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/STATUS-204.md` | Yes |
| `docs/loops/platform/SKILL-204.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/SKILL-204.md` | Yes |
| Git history | Read | Current branch log | Filesystem permissions | Read-only; no commits made by this loop | N/A | Yes |

This loop makes no writes to any external system outside the repository. It does not call external APIs, write to databases, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/platform/LOOP-204-API-Contract-Validation.md` — this document
3. `docs/loops/platform/STATUS-204.md` — prior run state (if it exists)
4. `docs/architecture/api-catalog.md` — declared API contracts
5. `docs/architecture/module-catalog.md` — module ownership
6. `docs/loops/core/STATUS-001.md` — LOOP-001 freshness confirmation
7. Prior API validation report (if it exists) — for breaking change comparison
8. Output of `git log --oneline -10` — recent commit context

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `API-SCANNER` | Maker | Steps 1–5: load state, assemble context, scan specification files and implementation, classify API contract findings, detect breaking changes, compute gate conditions | Filesystem read, git CLI, OpenAPI/proto file parsing, annotation extraction, diff computation | Reports findings to GATE-1 and GATE-2 |
| `API-CHECKER` | Checker | Step 6: independently verifies scanner findings by re-reading specification files and implementation classes for each violation; produces written checker report | Filesystem read, cross-reference of findings against spec and source files | Independent of API-SCANNER; finding reviewed at GATE-1 if checker disputes scanner |
| `STATUS-WRITER` | Maker | Steps 7–9: writes all output artifacts, updates STATUS-204.md and SKILL-204.md, produces Reflection | Filesystem write | None — status writes occur after all gates are cleared |

`API-SCANNER` and `API-CHECKER` must be separate agent instances. No single agent may act as both Maker and Checker for the same artifact.

---

## Workflow

### Step 1 — Load Previous State

**Agent:** `API-SCANNER`
**Inputs:** `STATUS-204.md` (if present), prior API validation report (if present)
**Outputs:** In-memory prior state snapshot

Read `STATUS-204.md`. Extract: last run ID, last run date, last known endpoint count, last known violation count, prior implementation snapshot (endpoint list with HTTP methods, paths, and parameter signatures) for breaking change comparison. If no prior state exists, record `first_run = true`. Record the git HEAD SHA at this moment.

Check `STATUS-204.md` for `status: emergency_stopped`. If present, halt immediately and produce a partial Reflection.

---

### Step 2 — Assemble Context from Dependencies

**Agent:** `API-SCANNER`
**Inputs:** `docs/architecture/api-catalog.md`, `docs/architecture/module-catalog.md`, `docs/loops/core/STATUS-001.md`
**Outputs:** Context assembly record (in-memory)

Parse `api-catalog.md` to extract: all declared API endpoints with protocol (REST/gRPC/GraphQL), HTTP method and path (for REST), service method name (for gRPC), stability classification (public/internal/experimental), owning module, authentication requirement, and declared specification file reference.

Parse `module-catalog.md` to extract module ownership.

Validate LOOP-001 freshness: if `last_updated` in STATUS-001.md is older than 7 days, record `dependency_stale = true`. If embedded HEAD SHA in api-catalog.md does not match current HEAD SHA, treat as Hard Gate per SPEC-001 §2.C5.

---

### Step 3 — Scan Specification Files

**Agent:** `API-SCANNER`
**Inputs:** Repository filesystem, context from Step 2
**Outputs:** Specification inventory

Locate and parse all OpenAPI specification files (YAML or JSON). For each, extract: all declared paths with HTTP methods, request body schemas (field names and types), response schemas per status code, security scheme declarations, and deprecation markers. Also parse all gRPC proto files, extracting service definitions, RPC method names, request and response message types, and field definitions.

Record the specification inventory: for each specification entry, note the owning module (inferred from file location or spec file metadata), stability classification, and any declared deprecation with sunset date.

---

### Step 4 — Scan Implementation and Classify Findings

**Agent:** `API-SCANNER`
**Inputs:** Repository source files, specification inventory from Step 3, context from Step 2
**Outputs:** Classified API contract finding set, implementation snapshot for breaking change detection

Scan all REST controller classes for endpoint annotations (`@GetMapping`, `@PostMapping`, `@RequestMapping`, `@Controller`, Spring MVC equivalents, or framework-specific equivalents). For each endpoint, record: HTTP method, path, request body parameter type, response type, and security annotations present.

Scan all gRPC service implementation classes for declared RPC methods.

For each endpoint found in implementation, evaluate:

- **A-1 — Specification Present:** The endpoint has a corresponding entry in the api-catalog and a corresponding specification file. Absence is a `Critical` violation.
- **A-2 — HTTP Method Match:** The implemented HTTP method matches the specification. Mismatch is a `Critical` violation.
- **A-3 — Path Match:** The implemented URL path matches the specification. Mismatch is a `Critical` violation.
- **A-4 — Request Body Type Match:** The request body parameter type is consistent with the specification's request body schema. Type mismatch is a `High` violation.
- **A-5 — Response Type Match:** The declared response type is consistent with the specification's response schema for the 200 status code. Type mismatch is a `High` violation.
- **A-6 — Versioning Consistency:** If the API catalog declares a versioning strategy (e.g., all v1 endpoints under `/v1/`), the endpoint's path must conform. Mismatch is a `High` violation.
- **A-7 — Deprecated Annotation Present:** If the endpoint is marked deprecated in the specification, the implementation class or method must carry a `@Deprecated` annotation (or framework-equivalent) with a declared sunset date. Absence is a `High` violation.
- **A-8 — Deprecated Endpoint Not Called:** For each deprecated endpoint in the specification, scan all callers within the monorepo (HTTP client classes, Feign clients, gRPC stubs). Any caller of a deprecated endpoint is a `High` violation.
- **A-9 — Auth Annotation Present:** For every non-public endpoint (not declared as `public` in the api-catalog), the controller method or class must carry an authentication or authorization annotation (e.g., `@PreAuthorize`, `@Secured`, `@RolesAllowed`, or a security filter that covers it by convention). Absence is a `High` violation.
- **A-10 — Error Response Shape Match:** Declared error response types match the platform's declared error schema (from the api-catalog's global error schema declaration). Mismatch is a `Medium` violation.

Also check for unspecified endpoints: endpoints found in implementation but absent from any specification file. Record as `unspecified_endpoint`.

Compute an implementation snapshot: for each endpoint, record the signature (method, path, required parameter names and types). Store in SKILL-204.md for breaking change comparison in future runs.

Compare current implementation snapshot against the prior run snapshot from Step 1:
- A required field or parameter present in the prior snapshot but absent in the current snapshot is a `Critical` breaking change.
- A type change for an existing required field is a `Critical` breaking change.
- An HTTP method change for an existing path is a `Critical` breaking change.
- An optional field becoming required is a `High` breaking change.

---

### Step 5 — Evaluate Gate Conditions

**Agent:** `API-SCANNER`
**Inputs:** Classified finding set from Step 4, current git HEAD SHA
**Outputs:** Gate decision

Verify current git HEAD SHA matches the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1.

Otherwise evaluate in priority order:
1. Any `Critical` violations (A-1, A-2, A-3) or Critical breaking changes → GATE-1
2. Any `unspecified_endpoint` finding → GATE-1
3. `dependency_stale = true` → GATE-2
4. Any `High` violations (A-4 through A-9) without Critical violations → GATE-2
5. Any High breaking changes without Critical violations → GATE-2
6. Any `Medium` violations without Higher violations → proceed (record in report, no gate)
7. None of the above → proceed to Step 6

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

### Step 6 — Independent Checker Validation

**Agent:** `API-CHECKER`
**Inputs:** Classified finding set from Step 4, repository specification and source files
**Outputs:** Checker validation report

`API-CHECKER` independently verifies by re-reading specification files and implementation classes for each non-conformant finding. For each `Critical` violation: re-read the specification entry and the controller annotation, confirm the discrepancy, cite line numbers. For each breaking change: independently diff the prior and current implementation snapshots to confirm the change is genuine. Spot-check 20% of conformant endpoints.

The Checker produces a written validation report with determination `accepted` or `rejected` with evidence. If `rejected`, API-SCANNER performs a single retry of Step 4 for disputed items. Unresolved disputes trigger GATE-1.

---

**[GATE-1 — Hard Gate: Missing Specification or Breaking Change Detected]**

The loop halts. `STATUS-204.md` is updated to `status: awaiting_approval`. No validation report artifact is written as authoritative until human approval is received and recorded. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: High Violations or Stale Dependency]**

The loop notifies and sets a 24-hour timer. If no objection is received, it proceeds to Step 7. See `## Human Approval Gates` — GATE-2.

---

### Step 7 — Produce Validation Reports

**Agent:** `STATUS-WRITER`
**Inputs:** Classified finding set (confirmed by Checker), gate clearance
**Outputs:** API validation report, unspecified API report, breaking change report, run metadata

Write `api-validation-report-{run-id}.md` with: run summary, Critical violations, High violations, Medium violations, conformant endpoints, comparison against prior run.

Write `unspecified-api-report-{run-id}.md` listing each unspecified endpoint with: controller class, HTTP method, path, and recommended action (add to specification or annotate as internal with explicit exclusion). Produce as empty-list document if no unspecified endpoints found.

Write `breaking-change-report-{run-id}.md` listing each breaking change with: endpoint identifier, change type, prior value, current value, and compatibility assessment. Produce as empty-list document if no breaking changes found.

Write `METADATA-204-{run-id}.md` with all required provenance fields.

---

### Step 8 — Update STATUS-204.md and SKILL-204.md

**Agent:** `STATUS-WRITER`
**Inputs:** All run metrics, gate outcomes, Checker report, implementation snapshot
**Outputs:** Updated `docs/loops/platform/STATUS-204.md`, updated `docs/loops/platform/SKILL-204.md`

Record all metrics. Record gate outcomes. Store the current implementation snapshot in SKILL-204.md for use as the prior-run baseline in the next run. Update SKILL-204.md with calibration observations: violation patterns, endpoints that recurrently drift from spec, and module areas requiring closer attention.

---

### Step 9 — Produce Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All outputs from Steps 7–8
**Outputs:** `docs/validation/api/reflections/REFLECTION-204-{run-id}.md`

Produce the Reflection artifact containing all ten required LOOP-STANDARD sections plus four loop-specific sections. Produce even on failed or stopped runs.

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `api-validation-report-{run-id}.md` exists and is non-empty | File existence; assert file size > 0 |
| VER-2 | `unspecified-api-report-{run-id}.md` exists (populated or empty-list) | File existence; assert file contains populated list or explicit empty declaration |
| VER-3 | `breaking-change-report-{run-id}.md` exists (populated or empty-list) | File existence; assert file contains populated list or explicit empty declaration |
| VER-4 | Every API endpoint declared in `api-catalog.md` appears in the validation report with a classification | Count api-catalog entries; assert all appear in report |
| VER-5 | Every breaking change finding in the breaking change report cites the prior value, current value, and change type | Parse breaking change report; assert each entry has non-empty `prior_value`, `current_value`, and `change_type` fields |
| VER-6 | No source file under any module has been modified by this run | `git diff --name-only HEAD` shows only `docs/validation/` and `docs/loops/platform/` paths |
| VER-7 | `STATUS-204.md` has been updated with the current run ID and a timestamp within 5 minutes of current time | Parse STATUS file; assert run ID and timestamp within tolerance |
| VER-8 | The Checker validation report exists and records a determination with evidence | File existence; parse determination and evidence fields |
| VER-9 | `REFLECTION-204-{run-id}.md` exists and contains all ten LOOP-STANDARD required sections | File existence; assert all ten section headings present |
| VER-10 | `SKILL-204.md` has been updated with the current implementation snapshot and an entry referencing the current run ID | Parse SKILL file; assert implementation snapshot and run ID entry present |

---

## Reflection

At the end of every run, `STATUS-WRITER` produces a Reflection at `docs/validation/api/reflections/REFLECTION-204-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus:

- **Breaking Change Summary:** count and types of breaking changes detected; comparison to prior run
- **Specification Coverage Summary:** percentage of discovered endpoints that have specification entries; count of unspecified endpoints
- **Checker Dispute Summary:** disputes raised by API-CHECKER and their resolution
- **Deprecation Status Summary:** count of deprecated endpoints with and without sunset dates declared; count of callers of deprecated endpoints found

---

## Human Approval Gates

### GATE-1 — Hard Gate: Missing Specification or Breaking Change Detected

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set (in-memory), Checker validation report |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with `status: stopped`; partial findings written to STATUS-204.md only; Reflection produced |
| Audit Trail | Approval record written to `STATUS-204.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, decision, and rationale recorded |

**Fires when:** Any endpoint lacks a specification file; any `Critical` breaking change (removed required field, type change, HTTP method change); any HTTP method or path mismatch; `concurrent_change_detected = true`; Checker rejects findings after retry.

**Reviewer guidance:** Breaking changes must be resolved before release: either revert the change, add API versioning, or communicate the breaking change to callers and schedule a migration. Unspecified endpoints must be added to the API specification or explicitly marked as internal. Approve to record findings as authoritative; deny if findings are false positives.

---

### GATE-2 — Soft Gate: High Violations or Stale Dependency

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set with High findings only |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-204.yml`; defaults to creating a draft PR with findings summary |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 7; `soft_gate_auto_proceeded: true` recorded in STATUS-204.md |
| Audit Trail | Notification timestamp, outcome, recorded under `gate_outcomes.GATE-2` |

**Fires when:** High violations (A-4 through A-9) without Critical violations; High breaking changes without Critical ones; stale LOOP-001 dependency.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-204.md`. The executing agent checks this at the start of each step and halts immediately if present. A partial Reflection is produced recording the step at which the stop was received.

---

## Failure Recovery

### FR-1 — Specification File Not Parseable

**Detection:** An OpenAPI YAML or proto file is syntactically invalid and cannot be parsed.
**Immediate Action:** Record the file as `spec_parse_failed`. Mark all endpoints declared under that specification as `unclassified`.
**Recovery:** If the failing spec covers more than 20% of declared API endpoints, trigger GATE-1. Otherwise trigger GATE-2 with parse failure noted.
**Rollback:** No rollback needed; parsing is read-only.

### FR-2 — Incomplete Endpoint Coverage

**Detection:** The scan produces classifications for fewer endpoints than declared in the api-catalog, with no exclusion explaining the gap.
**Immediate Action:** Record `scan_incomplete = true`. Flag uncovered endpoints.
**Recovery:** If gap exceeds 10% of declared endpoints, trigger GATE-1. Otherwise continue with uncovered endpoints recorded as `unverified_this_run`.
**Rollback:** No report written for gap areas; prior entries not removed.

### FR-3 — Prior Implementation Snapshot Unavailable

**Detection:** No prior implementation snapshot exists in SKILL-204.md (first run or SKILL file was reset).
**Immediate Action:** Record `first_run = true` for breaking change analysis. Skip breaking change comparison; produce an empty-list breaking change report.
**Recovery:** Continue normally; the current run's implementation snapshot is recorded for future runs.
**Rollback:** No rollback needed.

### FR-4 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 2 hours.
**Immediate Action:** Complete the current atomic step; do not begin the next step.
**Recovery:** Write all output artifacts produced so far. Write STATUS-204.md with `status: stopped`, `reason: max_duration_exceeded`. Produce a partial Reflection.
**Rollback:** Not required; partial outputs are documented as such.

### FR-5 — Upstream Dependency Unavailable

**Detection:** `docs/architecture/api-catalog.md` does not exist or is empty.
**Immediate Action:** Halt with `status: precondition_failed`. Do not write any output artifact.
**Recovery:** Re-trigger LOOP-001. Re-trigger LOOP-204 after LOOP-001 completes.
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
| `scan.endpoints_analyzed` | Total API endpoints classified |
| `scan.endpoints_expected` | Total endpoints declared in api-catalog |
| `scan.spec_files_parsed` | Total specification files successfully parsed |
| `findings.critical_count` | Count of Critical violations |
| `findings.high_count` | Count of High violations |
| `findings.medium_count` | Count of Medium violations |
| `findings.conformant_count` | Count of conformant endpoints |
| `findings.unspecified_endpoints` | Endpoints found in implementation but absent from spec |
| `findings.breaking_changes_critical` | Count of Critical breaking changes detected |
| `findings.breaking_changes_high` | Count of High breaking changes detected |
| `findings.deprecated_without_sunset` | Deprecated endpoints missing sunset date |
| `findings.callers_of_deprecated` | Count of callers of deprecated endpoints within monorepo |
| `findings.new_since_prior_run` | Violations present in this run but not prior run |
| `findings.resolved_since_prior_run` | Violations in prior run not present in this run |
| `checker.disputes_raised` | Count of findings disputed by API-CHECKER |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The scanner classifies internal-only utility methods annotated with HTTP mapping annotations (e.g., in test controllers) as production API violations.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Test controllers or internal utility controllers use the same annotations as production controllers.
- **Control:** Scanner excludes classes in test source directories and classes annotated with test-specific markers. Checker spot-check catches misclassifications.
- **Detection:** Checker disputes referencing test-scope controllers.
- **Response:** Update `.loop-204.yml` exclusion patterns; reclassify.

### RISK-2 — Architectural Drift

- **Description:** The api-catalog is outdated, causing the scan to miss newly added endpoints.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 has not run since new API endpoints were added.
- **Control:** LOOP-001 freshness check in Step 2; Soft Gate fires if outputs older than 7 days.
- **Detection:** `dependency_stale = true` flag; unspecified endpoint findings.
- **Response:** Re-trigger LOOP-001; re-trigger LOOP-204.

### RISK-3 — Hidden Dependencies

- **Description:** Dynamically registered routes or runtime-generated endpoints are not visible to static analysis.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository uses programmatic route registration (e.g., `RouterFunction` in Spring WebFlux).
- **Control:** SKILL-204.md accumulates patterns across runs; `.loop-204.yml` allows explicit declarations of dynamic endpoints.
- **Detection:** Unspecified endpoint findings that do not correspond to annotated controllers.
- **Response:** Update `.loop-204.yml` with explicit declarations; improve scan patterns.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads repository source files and specification files, writing only to `docs/validation/api/` and state files. It does not access runtime data, databases, or tenant-scoped storage.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** A partially written breaking change report could mislead a release gate into treating a breaking change as absent.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Filesystem interruption during Step 7.
- **Control:** Output files written atomically. Prior run reports preserved in git history.
- **Detection:** Downstream loop precondition check detects empty or malformed report.
- **Response:** `git checkout docs/validation/api/` restores prior state; re-run LOOP-204.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent — re-running produces equivalent output for the same HEAD SHA.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** The scan might read credential values from API specification example payloads or configuration files and inadvertently include them in output artifacts.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** OpenAPI spec files contain example payloads with embedded credential values.
- **Control:** VER-9 scans all output artifacts for secrets patterns. Example values are never reproduced verbatim; only field names and types are recorded.
- **Detection:** VER-9 failure.
- **Response:** Halt; do not write the affected artifact; trigger GATE-1; record event without reproducing the credential value.

### RISK-8 — Runaway Execution

- **Description:** A repository with thousands of endpoints or extremely large OpenAPI specifications exceeds the 2-hour run limit.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository with more than 1,000 API endpoints or specification files exceeding 10MB.
- **Control:** Maximum run duration enforced by FR-4.
- **Detection:** `max_duration_exceeded` status.
- **Response:** FR-4 procedure; partial outputs preserved.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All endpoints declared in api-catalog classified (or scan gaps documented) |
| SC-2 | All VER-1 through VER-10 verification criteria assessed and outcomes recorded |
| SC-3 | All three report artifacts produced (api-validation-report, unspecified-api-report, breaking-change-report) |
| SC-4 | All output artifacts listed in the Outputs table written |
| SC-5 | `STATUS-204.md` updated with run metrics and final status |
| SC-6 | `SKILL-204.md` updated with implementation snapshot |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (2 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | PRE-3 detects a concurrent run |
| SC-11 | Emergency Stop signal received in `STATUS-204.md` |
| SC-12 | A required precondition (PRE-1 through PRE-6) is not met |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Validation Artifacts:**
- [ ] `docs/validation/api/api-validation-report-{run-id}.md` written and non-empty
- [ ] `docs/validation/api/unspecified-api-report-{run-id}.md` written (populated or empty-list)
- [ ] `docs/validation/api/breaking-change-report-{run-id}.md` written (populated or empty-list)
- [ ] `docs/validation/api/checker-report-{run-id}.md` written with determination recorded

**Verification:**
- [ ] All VER-1 through VER-10 criteria assessed and outcomes recorded in Reflection
- [ ] VER-6 (no source file modification) confirmed
- [ ] VER-9 (secrets scan) passed on all output artifacts

**Gates:**
- [ ] Gate outcome recorded in `STATUS-204.md` for every gate that fired

**State:**
- [ ] `docs/loops/platform/STATUS-204.md` updated with all required metrics and final status
- [ ] `docs/loops/platform/SKILL-204.md` updated with implementation snapshot

**Provenance:**
- [ ] `docs/validation/api/metadata/METADATA-204-{run-id}.md` written

**Reflection:**
- [ ] `docs/validation/api/reflections/REFLECTION-204-{run-id}.md` produced with all required sections

---

## Future Improvements

- **Contract testing integration:** Extend scope (in a future version) to execute Pact or Spring Cloud Contract tests as part of the validation, moving from static to live contract verification.
- **OpenAPI spec auto-generation comparison:** Where framework annotations can auto-generate an OpenAPI spec, generate it and compare against the committed spec file, catching spec drift automatically.
- **Client library validation:** Extend scope to validate that generated client libraries (Feign clients, OpenAPI-generated clients) are in sync with the current specification.
- **Sunset date tracking:** Add a SKILL-203.md-style tracker for deprecated endpoint sunset dates, proactively notifying teams when a sunset date is approaching.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — conformance requirements
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces api-catalog consumed by this loop
- `docs/loops/core/LOOP-006-Verification.md` — upstream verification context
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions and Emergency Stop protocol
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version establishing LOOP-204 as the platform API contract validation loop for the AI Engineering Operating System.

