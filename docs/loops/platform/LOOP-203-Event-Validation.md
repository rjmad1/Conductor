---
# PROVENANCE METADATA
Original Path: docs/loops/platform/LOOP-203-Event-Validation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Platform validation loop to verify event schemas and message publishing.
Generalized Purpose: Platform validation loop to verify event schemas and message publishing.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-203 — Event Validation

**Loop ID:** LOOP-203
**Name:** Event Validation
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

LOOP-203 validates that event schemas, routing configurations, producer implementations, and consumer implementations conform to the declared event contracts in the event catalog produced by LOOP-001. It produces a structured event validation report and, when orphan events are found, an orphan event report identifying event types with producers but no consumers or consumers but no producers. This loop is the authoritative signal that the event-driven layer is structurally sound and that no silent message loss is possible due to unregistered consumers or unrouted producers. It does not modify source files; it reads the repository and writes structured findings to `docs/validation/events/`.

---

## Problem Statement

In an event-driven platform, orphan events — types with producers but no consumers, or consumers but no producers — are among the most dangerous architectural gaps. An orphaned producer silently discards messages that no system reads. An orphaned consumer silently does nothing. Schema drift between producer and consumer implementations causes deserialization failures that surface only at runtime. Dead letter queue absence means failed messages are lost without trace. Without systematic validation, these gaps are discovered only through production incidents or data quality audits — both expensive to remediate.

---

## Why This Loop Exists

Event contract validation has a clear enumerable structure: every event type declared in the event catalog must have a producer, a consumer, a registered schema, and a routing configuration. These declarations are present in the repository and can be cross-referenced mechanically. Codifying this as a loop provides a repeatable, auditable check that runs before every release, surfaces orphan events the moment they appear, and produces a written record that can be consumed by Governance loops. Without this loop, event contract discipline depends entirely on manual audit at code review, which does not scale and misses incremental drift.

---

## Scope

**In scope:**
- Verifying that every event type in the event catalog has at least one registered producer and at least one registered consumer
- Verifying that event payload schemas are registered and versioned in the schema registry or equivalent schema declaration files
- Verifying that producer implementations match the declared payload schema (field names and types)
- Verifying that consumer implementations handle all required payload fields declared in the schema
- Verifying that dead letter queue (DLQ) configuration exists for all Critical event types
- Verifying that event ordering guarantees in implementation match the declared contract (ordered vs. unordered)
- Verifying that schema evolution is backward/forward compatible where the event contract declares compatibility requirements

**Out of scope:**
- Executing event flows or live routing tests
- Modifying event schema files, producer code, or consumer code
- Load or throughput testing of event processing
- Infrastructure provisioning for message brokers
- Cross-system event contract validation with external producers or consumers not in this repository

**Maximum run duration:** 2 hours. If the loop has not terminated within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| `docs/architecture/event-catalog.md` | File | LOOP-001 run output | Required |
| `docs/architecture/module-catalog.md` | File | LOOP-001 run output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 state file | Required — used for freshness check |
| `docs/loops/core/STATUS-006.md` | File | LOOP-006 state file | Required — used for dependency freshness check |
| Repository source files for all modules | Directory | Repository | Required |
| Event schema declaration files (schema registry files, Avro/Protobuf/JSON Schema files, event class definitions) | File | Repository | Required |
| Messaging infrastructure configuration files (topic definitions, routing configuration, DLQ declarations) | File | Repository | Required |
| Previous `docs/loops/platform/STATUS-203.md` | File | Prior run artifact | Optional — absence indicates first run |
| Previous event validation report | File | `docs/validation/events/` prior run | Optional — used for drift detection |
| `.loop-203.yml` at repository root | Configuration | Repository | Optional — declares additional scan patterns and schema registry location |

### Input Validation

Before any scan step begins, the loop must verify:
- `docs/architecture/event-catalog.md` exists, is non-empty, and its embedded HEAD SHA matches the current repository HEAD SHA. SHA mismatch is treated as a Hard Gate condition per SPEC-001 §2.C5.
- `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` and `last_updated` is no more than 7 days old. Stale outputs trigger a Soft Gate before proceeding.
- No exclusive lock on `docs/validation/events/` exists from a concurrent run (check STATUS-203.md for `status: running`).
- At least one schema declaration file is readable (schema registry location from `.loop-203.yml` if configured, otherwise inferred from repository structure).

---

## Outputs

All primary outputs are written to `docs/validation/events/`. State files are written to `docs/loops/platform/`.

| Artifact | Path | Description |
|----------|------|-------------|
| Event Validation Report | `docs/validation/events/event-validation-report-{run-id}.md` | Classified findings for all event types: conformant, non-conformant, and unclassified, with violation details |
| Orphan Event Report | `docs/validation/events/orphan-event-report-{run-id}.md` | Always produced: lists event types with missing producers or consumers, or empty-list statement if none found |
| Schema Drift Report | `docs/validation/events/schema-drift-report-{run-id}.md` | Always produced: lists event types where producer or consumer implementations diverge from the registered schema, or empty-list statement if none found |
| Checker Validation Report | `docs/validation/events/checker-report-{run-id}.md` | Independent assessment by EVENT-CHECKER |
| Run Metadata | `docs/validation/events/metadata/METADATA-203-{run-id}.md` | Provenance record: run ID, HEAD SHA at start and end, upstream dependency run IDs, elapsed duration, final status |
| Loop Status | `docs/loops/platform/STATUS-203.md` | Run state, metrics, gate outcomes, and open blockers |
| Loop Skill | `docs/loops/platform/SKILL-203.md` | Calibration observations accumulated across runs |
| Reflection | `docs/validation/events/reflections/REFLECTION-203-{run-id}.md` | Per-run structured reflection produced regardless of outcome |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Consumes `event-catalog.md` as the baseline of declared event types, producers, and consumers; consumes `module-catalog.md` for module ownership. Required.
- **LOOP-006 — Verification:** Consumes `STATUS-006.md` for recent verification context. Optional; absence does not block execution.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop.
2. **Scheduled execution** — Recurring schedule (recommended: once per release cycle or once per week, whichever is more frequent).
3. **Repository event** — A pull request is merged to the main branch that modifies any event class, schema file, producer implementation, consumer implementation, or messaging configuration file.
4. **Upstream loop completion** — LOOP-001 completes a run and its event catalog has changed since the last LOOP-203 run.

Trigger source and timestamp must be recorded in `STATUS-203.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/architecture/event-catalog.md` exists and is non-empty | File existence check; assert file size > 0 |
| PRE-2 | `docs/loops/core/STATUS-001.md` reports `last_outcome: completed` | Parse STATUS-001.md; assert field value |
| PRE-3 | No other instance of LOOP-203 is currently running | Read STATUS-203.md; assert `current_status != running` |
| PRE-4 | The executing agent has read access to the repository root | Verify readable access to repository root |
| PRE-5 | The executing agent has write access to `docs/validation/events/` | Attempt temporary probe write; remove on success |
| PRE-6 | At least one schema declaration file is readable | Assert at least one file matching schema file patterns is accessible |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All source files, schema files, and configuration files under repository root | Filesystem permissions of executing agent | Read-only; no source file modification | N/A | Yes |
| `docs/validation/events/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/validation/events/` restores prior state | Yes — each file fully regenerated per run |
| `docs/loops/platform/STATUS-203.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/STATUS-203.md` | Yes |
| `docs/loops/platform/SKILL-203.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/platform/SKILL-203.md` | Yes |
| Git history | Read | Current branch log | Filesystem permissions | Read-only; no commits made by this loop | N/A | Yes |

This loop makes no writes to any external system outside the repository. It does not call external APIs, write to databases, message brokers, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/platform/LOOP-203-Event-Validation.md` — this document
3. `docs/loops/platform/STATUS-203.md` — prior run state (if it exists)
4. `docs/architecture/event-catalog.md` — declared event types, producers, consumers
5. `docs/architecture/module-catalog.md` — module ownership
6. `docs/loops/core/STATUS-001.md` — LOOP-001 freshness confirmation
7. Prior event validation report (if it exists) — for drift comparison
8. Output of `git log --oneline -10` — recent commit context

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `EVENT-SCANNER` | Maker | Steps 1–5: load state, assemble context, scan schema and implementation files, classify event contract findings, compute gate conditions | Filesystem read, git CLI, schema file parsing, annotation and interface analysis | Reports findings to GATE-1 and GATE-2 |
| `EVENT-CHECKER` | Checker | Step 6: independently verifies scanner findings by re-reading schema files and implementation classes; produces written checker report | Filesystem read, cross-reference of findings against schema and source files | Independent of EVENT-SCANNER; finding reviewed at GATE-1 if checker disputes scanner |
| `STATUS-WRITER` | Maker | Steps 7–9: writes all output artifacts, updates STATUS-203.md and SKILL-203.md, produces Reflection | Filesystem write | None — status writes occur after all gates are cleared |

`EVENT-SCANNER` and `EVENT-CHECKER` must be separate agent instances. No single agent may act as both Maker and Checker for the same artifact.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

### Step 1 — Load Previous State

**Agent:** `EVENT-SCANNER`
**Inputs:** `STATUS-203.md` (if present), prior event validation report (if present)
**Outputs:** In-memory prior state snapshot

Read `STATUS-203.md`. Extract: last run ID, last run date, last known event type count, last known orphan count, last known schema drift count. If no prior state exists, record `first_run = true`. Construct a prior-state index of conformant event types, orphan events, and schema drift events. Record the git HEAD SHA at this moment.

Check `STATUS-203.md` for `status: emergency_stopped`. If present, halt immediately and produce a partial Reflection.

---

### Step 2 — Assemble Context from Dependencies

**Agent:** `EVENT-SCANNER`
**Inputs:** `docs/architecture/event-catalog.md`, `docs/architecture/module-catalog.md`, `docs/loops/core/STATUS-001.md`
**Outputs:** Context assembly record (in-memory)

Parse `event-catalog.md` to extract: all declared event types, their declared producers (module or class), declared consumers (module or class), transport mechanism (topic name, exchange, in-process bus), schema reference, ordering contract (ordered/unordered), and any declared DLQ configuration.

Parse `module-catalog.md` to extract module ownership for cross-referencing.

Validate LOOP-001 freshness: if `last_updated` in STATUS-001.md is older than 7 days, record `dependency_stale = true`. If embedded HEAD SHA in event-catalog.md does not match current HEAD SHA, treat as Hard Gate per SPEC-001 §2.C5.

---

### Step 3 — Scan Schema Registry and Implementation Files

**Agent:** `EVENT-SCANNER`
**Inputs:** Repository filesystem, context from Step 2
**Outputs:** Schema inventory, producer inventory, consumer inventory

Scan for all schema declaration files: Avro `.avsc` files, Protobuf `.proto` files, JSON Schema `.json` files in schema directories, and Java/Kotlin/Python event class definitions. For each schema, record: event type name, version, field declarations (name and type), and compatibility mode if declared.

Scan for all producer implementations: classes or functions that publish or emit events. For each, record: event type emitted (inferred from the type argument of the publish/emit call or from constructor), class path, and owning module.

Scan for all consumer implementations: classes or functions that subscribe to or handle events. For each, record: event type handled (inferred from the parameter type of the handler method or from subscription registration), class path, and owning module.

Scan for messaging infrastructure configuration: topic definitions, exchange declarations, routing keys, DLQ declarations, and consumer group registrations.

---

### Step 4 — Classify Event Types Against Declared Contracts

**Agent:** `EVENT-SCANNER`
**Inputs:** Schema inventory, producer inventory, consumer inventory from Step 3; event catalog from Step 2
**Outputs:** Classified event type finding set

For each event type declared in the event catalog, evaluate:

- **E-1 — Producer Present:** At least one producer implementation is found for this event type. Absence is a `Critical` violation (orphan producer-side: consumers listening for events that are never published).
- **E-2 — Consumer Present:** At least one consumer implementation is found for this event type. Absence is a `Critical` violation (orphan consumer-side: producers publishing events that no system reads).
- **E-3 — Schema Registered:** A schema declaration file exists for this event type with a valid version. Absence is a `High` violation.
- **E-4 — Producer Schema Match:** The producer implementation constructs the event with all required fields declared in the registered schema. Missing required fields are a `High` violation; extra undeclared fields are a `Medium` warning.
- **E-5 — Consumer Schema Match:** The consumer implementation reads all required fields declared in the registered schema. Missing required field accesses are a `High` violation.
- **E-6 — DLQ Configuration Present (Critical event types):** For event types declared as `criticality: critical` in the event catalog, a DLQ configuration must exist for the consumer. Absence is a `High` violation.
- **E-7 — Ordering Guarantee Match:** If the event catalog declares `ordering: ordered`, the consumer implementation and topic/queue configuration must use a partitioned or ordered delivery mechanism. Mismatch is a `High` violation.
- **E-8 — Schema Compatibility Rules Not Violated:** If the event type declares a compatibility mode (backward, forward, or full), compare the current schema version against the prior version recorded in SKILL-203.md. Flag incompatible changes as `Critical` schema evolution violations.

Also check for:
- **Undeclared event types:** producers or consumers found in the codebase for event types not present in the event catalog. Record as `undeclared_event` (informational finding; triggers GATE-2).

Classify each event type as `conformant`, `non-conformant` (with criteria IDs and evidence), or `unclassified` (with reason).

---

### Step 5 — Evaluate Gate Conditions

**Agent:** `EVENT-SCANNER`
**Inputs:** Classified finding set from Step 4, current git HEAD SHA
**Outputs:** Gate decision

Verify current git HEAD SHA matches the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1.

Otherwise evaluate in priority order:
1. Any `Critical` violations (E-1, E-2, E-8) — orphan event types or schema evolution violations → GATE-1
2. `dependency_stale = true` (LOOP-001 older than 7 days) → GATE-2
3. Any `High` violations (E-3, E-4, E-5, E-6, E-7) without Critical violations → GATE-2
4. Any `undeclared_event` findings → GATE-2
5. Schema drift count increased by more than 2 since prior run → GATE-2
6. None of the above → proceed to Step 6

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

### Step 6 — Independent Checker Validation

**Agent:** `EVENT-CHECKER`
**Inputs:** Classified finding set from Step 4, repository schema and source files
**Outputs:** Checker validation report

`EVENT-CHECKER` independently verifies by re-reading schema files and implementation classes for each non-conformant finding. For each orphan event (E-1 or E-2 violation): re-search the entire repository for any producer or consumer matching the event type, including non-obvious registration patterns (configuration-based subscription, dynamic event routing). For each schema drift finding (E-4 or E-5): re-read the schema and the implementation class, confirm field names and types match. Spot-check 20% of conformant event types for missed violations.

The Checker produces a written validation report with determination `accepted` or `rejected` with evidence. If `rejected`, EVENT-SCANNER performs a single retry of Step 4 for disputed items. Unresolved disputes trigger GATE-1.

---

**[GATE-1 — Hard Gate: Orphan Events or Schema Evolution Violations]**

The loop halts. `STATUS-203.md` is updated to `status: awaiting_approval`. No validation report artifact is written as authoritative until human approval is received and recorded. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: High Violations or Schema Drift Warnings]**

The loop notifies and sets a 24-hour timer. If no objection is received, it proceeds to Step 7. See `## Human Approval Gates` — GATE-2.

---

### Step 7 — Produce Validation Reports

**Agent:** `STATUS-WRITER`
**Inputs:** Classified finding set (confirmed by Checker), gate clearance
**Outputs:** Event validation report, orphan event report, schema drift report, run metadata

Write `event-validation-report-{run-id}.md` with run summary, Critical violations, High violations, conformant event types, unclassified event types, undeclared event findings, and comparison against prior run.

Write `orphan-event-report-{run-id}.md` listing each orphan event type with: event type name, which side is missing (producer or consumer), evidence file paths, and recommended remediation. Produce as empty-list document if no orphans found.

Write `schema-drift-report-{run-id}.md` listing each schema drift finding with: event type name, schema version, field that diverges, divergence type (missing in implementation, type mismatch), and compatibility assessment. Produce as empty-list document if no drift found.

Write `METADATA-203-{run-id}.md` with all required provenance fields.

---

### Step 8 — Update STATUS-203.md and SKILL-203.md

**Agent:** `STATUS-WRITER`
**Inputs:** All run metrics, gate outcomes, Checker report
**Outputs:** Updated `docs/loops/platform/STATUS-203.md`, updated `docs/loops/platform/SKILL-203.md`

Record all metrics. Record gate outcomes with reviewer identity, decision, rationale, and timestamp. Update SKILL-203.md with calibration observations: event types that recurrently produce drift findings, schema versioning patterns observed, and transport configuration patterns.

---

### Step 9 — Produce Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All outputs from Steps 7–8
**Outputs:** `docs/validation/events/reflections/REFLECTION-203-{run-id}.md`

Produce the Reflection artifact containing all ten required LOOP-STANDARD sections plus four loop-specific sections. Produce this artifact even on failed or stopped runs.

---


**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `event-validation-report-{run-id}.md` exists and is non-empty | File existence; assert file size > 0 |
| VER-2 | `orphan-event-report-{run-id}.md` exists (either with orphan listings or empty-list statement) | File existence; assert file contains populated list or explicit empty declaration |
| VER-3 | `schema-drift-report-{run-id}.md` exists (either with drift listings or empty-list statement) | File existence; assert file contains populated list or explicit empty declaration |
| VER-4 | Every event type declared in the event catalog appears in the validation report with a classification | Count event-catalog entries; assert all appear in report |
| VER-5 | Every orphan event finding cites the event type name and which side (producer/consumer) is missing | Parse orphan report; assert each entry has non-empty `event_type` and `missing_side` fields |
| VER-6 | No source file under any module has been modified by this run | `git diff --name-only HEAD` shows only `docs/validation/` and `docs/loops/platform/` paths |
| VER-7 | `STATUS-203.md` has been updated with the current run ID and a timestamp within 5 minutes of current time | Parse STATUS file; assert run ID and timestamp within tolerance |
| VER-8 | The Checker validation report exists and records a determination with evidence | File existence; parse determination and evidence fields |
| VER-9 | `REFLECTION-203-{run-id}.md` exists and contains all ten LOOP-STANDARD required sections | File existence; assert all ten section headings present |
| VER-10 | `SKILL-203.md` has been updated with an entry referencing the current run ID | Parse SKILL file; assert entry with current run ID present |

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

At the end of every run, `STATUS-WRITER` produces a Reflection at `docs/validation/events/reflections/REFLECTION-203-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus:

- **Orphan Summary:** count of orphan events (producer-side and consumer-side); comparison to prior run
- **Schema Drift Summary:** count of schema drift findings; affected event types; compatibility assessment
- **Coverage Summary:** event types covered in the scan; any event types in the catalog that could not be traced to implementations
- **Undeclared Events:** event types found in implementation but absent from the catalog — candidate for catalog update

---

## Human Approval Gates

### GATE-1 — Hard Gate: Orphan Events or Critical Schema Evolution Violations

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set (in-memory), Checker validation report |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with `status: stopped`; partial findings written to STATUS-203.md only; Reflection produced |
| Audit Trail | Approval record written to `STATUS-203.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, decision, and rationale recorded |

**Fires when:** Any orphan event type (E-1 or E-2 violation — producer or consumer absent); any Critical schema evolution violation (E-8 — incompatible schema change); `concurrent_change_detected = true`; Checker rejects findings after retry.

**Reviewer guidance:** Orphan events represent silent message loss or silent non-execution. Each must be triaged: either a producer/consumer is missing and must be added, or the event type must be removed from the catalog. Schema evolution violations require explicit migration planning. Approve to record findings as authoritative; deny if findings are false positives.

---

### GATE-2 — Soft Gate: High Violations or Schema Drift Warnings

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 5, before Step 7 |
| Artifact Under Review | Classified finding set with High or informational findings only |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-203.yml`; defaults to creating a draft PR with findings summary |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 7; `soft_gate_auto_proceeded: true` recorded in STATUS-203.md |
| Audit Trail | Notification timestamp, outcome, recorded under `gate_outcomes.GATE-2` |

**Fires when:** High violations (E-3 through E-7) without Critical violations; undeclared event findings; stale LOOP-001 dependency; schema drift count increased by more than 2 since prior run.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-203.md`. The executing agent checks this at the start of each step and halts immediately if present. A partial Reflection is produced recording the step at which the stop was received.

---

## Failure Recovery

### FR-1 — Incomplete Event Type Coverage

**Detection:** The scan produces classifications for fewer event types than declared in the event catalog, and no exclusion configuration explains the gap.
**Immediate Action:** Record `scan_incomplete = true`. Flag uncovered event types.
**Recovery:** If gap exceeds 10% of event catalog entries, trigger GATE-1. Otherwise continue with uncovered types recorded as `unverified_this_run`.
**Rollback:** No report written for gap areas; prior entries not removed.

### FR-2 — Schema File Parse Failure

**Detection:** A schema file (Avro, Protobuf, JSON Schema, or event class) cannot be parsed.
**Immediate Action:** Record the event type as `schema_parse_failed`. Classify criteria E-3, E-4, and E-5 as `unclassified` for this event type.
**Recovery:** If the failing schema belongs to a Critical event type, trigger GATE-1. Otherwise trigger GATE-2 with parse failure noted.
**Rollback:** No rollback needed; parsing is read-only.

### FR-3 — Producer or Consumer Registration Pattern Not Recognized

**Detection:** A module declared as a producer or consumer in the event catalog has source files that match no known producer/consumer annotation or registration pattern.
**Immediate Action:** Record the registration as `pattern_not_recognized`. Classify E-1 or E-2 as `unclassified`.
**Recovery:** Record the pattern in SKILL-203.md for future recognition. Trigger GATE-2. Update `.loop-203.yml` with additional patterns if the pattern is confirmed valid.
**Rollback:** No rollback needed.

### FR-4 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 2 hours.
**Immediate Action:** Complete the current atomic step; do not begin the next step.
**Recovery:** Write all output artifacts produced so far. Write STATUS-203.md with `status: stopped`, `reason: max_duration_exceeded`. Produce a partial Reflection.
**Rollback:** Not required; partial outputs documented as such.

### FR-5 — Upstream Dependency Unavailable

**Detection:** `docs/architecture/event-catalog.md` does not exist or is empty.
**Immediate Action:** Halt with `status: precondition_failed`. Do not write any output artifact.
**Recovery:** Re-trigger LOOP-001. Re-trigger LOOP-203 after LOOP-001 completes.
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
| `scan.event_types_analyzed` | Total event types classified |
| `scan.event_types_expected` | Total event types declared in event catalog |
| `findings.orphan_producer_side` | Event types with consumers but no producers |
| `findings.orphan_consumer_side` | Event types with producers but no consumers |
| `findings.schema_drift_count` | Event types with schema drift findings |
| `findings.schema_evolution_violations` | Critical schema evolution violations (incompatible changes) |
| `findings.conformant_count` | Event types fully conformant with declared contracts |
| `findings.non_conformant_count` | Event types with at least one non-conformant criterion |
| `findings.unclassified_count` | Event types with at least one unclassified criterion |
| `findings.undeclared_events` | Event types found in implementation but absent from catalog |
| `findings.new_since_prior_run` | Violations present in this run but not prior run |
| `findings.resolved_since_prior_run` | Violations in prior run not present in this run |
| `checker.disputes_raised` | Count of findings disputed by EVENT-CHECKER |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** The scanner classifies event-like classes (e.g., domain model classes that happen to implement a serializable interface) as event producers or consumers, producing false positives.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Repository uses naming or annotation patterns that overlap with event registration patterns.
- **Control:** Classification criteria require both a framework-specific annotation and a publish/subscribe method call pattern. Checker's spot-check catches misclassifications.
- **Detection:** Checker disputes referencing misclassified classes.
- **Response:** Update classification criteria in Step 4; update `.loop-203.yml` exclusion list.

### RISK-2 — Architectural Drift

- **Description:** The event catalog is outdated, causing the scan to miss event types added since the last LOOP-001 run.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** New event types introduced without re-running LOOP-001.
- **Control:** LOOP-001 freshness check in Step 2; Soft Gate fires if outputs older than 7 days.
- **Detection:** `dependency_stale = true` flag; undeclared event findings.
- **Response:** Re-trigger LOOP-001; re-trigger LOOP-203.

### RISK-3 — Hidden Dependencies

- **Description:** Dynamic event subscription (e.g., consumers registered at runtime via configuration files not scanned) causes false orphan findings.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository uses configuration-based consumer registration not recognized by scan patterns.
- **Control:** FR-3 procedure; SKILL-203.md accumulates registration patterns; `.loop-203.yml` allows explicit registration declarations.
- **Detection:** Checker identifies known-good consumers flagged as orphans.
- **Response:** FR-3 procedure; update scan patterns.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads repository source files and writes only to `docs/validation/events/` and state files. It does not access runtime message brokers, databases, or tenant-scoped storage.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** A partially written orphan event report could mislead a release gate into treating a partial scan as clean.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Filesystem interruption during Step 7.
- **Control:** Output files written atomically. Prior run reports preserved in git history.
- **Detection:** Downstream loop precondition check detects empty or malformed report.
- **Response:** `git checkout docs/validation/events/` restores prior state; re-run LOOP-203.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent — re-running produces equivalent output for the same HEAD SHA.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** Event payload schemas or configuration files might contain embedded credentials that are inadvertently included in output artifacts.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Schema files contain example payloads with embedded test credentials.
- **Control:** VER-9 scans all output artifacts for secrets patterns. Schema values are never reproduced verbatim; only field names and types are recorded.
- **Detection:** VER-9 failure.
- **Response:** Halt; do not write the affected artifact; trigger GATE-1; record event without reproducing the credential value.

### RISK-8 — Runaway Execution

- **Description:** A repository with thousands of event types and deeply nested schema hierarchies exceeds the 2-hour run limit.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Event catalog with more than 500 event types or deeply nested Protobuf/Avro schemas.
- **Control:** Maximum run duration enforced by FR-4.
- **Detection:** `max_duration_exceeded` status.
- **Response:** FR-4 procedure; partial outputs preserved.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All event types in the event catalog classified (or scan gaps documented) |
| SC-2 | All VER-1 through VER-10 verification criteria assessed and outcomes recorded |
| SC-3 | Orphan event report and schema drift report produced (even if empty) |
| SC-4 | All output artifacts listed in the Outputs table written |
| SC-5 | `STATUS-203.md` updated with run metrics and final status |
| SC-6 | `SKILL-203.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (2 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | PRE-3 detects a concurrent run |
| SC-11 | Emergency Stop signal received in `STATUS-203.md` |
| SC-12 | A required precondition (PRE-1 through PRE-6) is not met |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Validation Artifacts:**
- [ ] `docs/validation/events/event-validation-report-{run-id}.md` written and non-empty
- [ ] `docs/validation/events/orphan-event-report-{run-id}.md` written (populated or empty-list)
- [ ] `docs/validation/events/schema-drift-report-{run-id}.md` written (populated or empty-list)
- [ ] `docs/validation/events/checker-report-{run-id}.md` written with determination recorded

**Verification:**
- [ ] All VER-1 through VER-10 criteria assessed and outcomes recorded in Reflection
- [ ] VER-6 (no source file modification) confirmed
- [ ] VER-9 (secrets scan) passed on all output artifacts

**Gates:**
- [ ] Gate outcome recorded in `STATUS-203.md` for every gate that fired

**State:**
- [ ] `docs/loops/platform/STATUS-203.md` updated with all required metrics and final status
- [ ] `docs/loops/platform/SKILL-203.md` updated

**Provenance:**
- [ ] `docs/validation/events/metadata/METADATA-203-{run-id}.md` written

**Reflection:**
- [ ] `docs/validation/events/reflections/REFLECTION-203-{run-id}.md` produced with all required sections

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Schema registry integration:** Extend Step 3 to query a live schema registry API (Confluent Schema Registry, AWS Glue Schema Registry) in addition to file-based schemas, eliminating a class of schema drift findings where the registry is the source of truth.
- **Consumer group coverage:** Extend E-2 to verify not only that consumers exist but that consumer group registrations cover all partitions for ordered event types.
- **Backward compatibility automated check:** Automate E-8 by running Avro or Protobuf compatibility checkers during Step 4 rather than relying on field-by-field comparison.
- **Event flow visualization:** Produce a Mermaid diagram of the producer-to-consumer flow graph for all conformant event types as part of the validation report.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — conformance requirements
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces event catalog consumed by this loop
- `docs/loops/core/LOOP-006-Verification.md` — upstream verification context
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions and Emergency Stop protocol
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version establishing LOOP-203 as the platform event validation loop for the AI Engineering Operating System.

