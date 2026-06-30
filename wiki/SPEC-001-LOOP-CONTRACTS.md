---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md
Original Version: 1.1
Extraction Date: 2026-06-27
Original Purpose: Technical interfaces and state management contract for loop automation.
Generalized Purpose: Technical interfaces and state management contract for loop automation.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-001 — Loop Contracts

**Version:** 1.1  
**Status:** Active  
**Type:** Engineering Specification  
**Governs:** All LOOP-XXX documents in the AI Engineering Operating System  
**Authority:** This document takes precedence over any individual loop specification where they conflict. Individual loop specifications may be more restrictive than this contract; they may never be less restrictive.

---

## Purpose

This specification defines the mandatory interface that every LOOP-XXX document must satisfy. It is the engineering contract that governs interoperability, composability, maintainability, version compatibility, and future extensibility across the entire loop chain. A loop that satisfies this contract can be placed in any conforming chain, upgraded independently, and reasoned about in isolation without reading every other loop's specification.

This document is not a loop. It does not define a workflow, an agent roster, or a set of outputs. It defines the structural, behavioural, and governance obligations that every loop document must declare and every loop execution must honour. Reading this document answers the question: "What guarantees does any loop in this system provide?" — regardless of which specific loop is being asked.

---

## Scope and Applicability

This contract applies to every document whose identifier follows the pattern `LOOP-NNN` and whose Status is `Active`. It applies in full to `Draft` documents during conformance review. It does not apply to shared specification documents (`SPEC-NNN`), shared guidance documents in `docs/loops/shared/`, or template files in `docs/loops/templates/`.

A loop that has not been reviewed for conformance with this contract may not be set to `Active` status. LOOP-STANDARD.md §Conformance Summary is the checklist used for that review; this document is the source of authority that the checklist implements.

---

## Document Conventions

**Shall:** A normative requirement. Non-compliance is a conformance failure.  
**Should:** A strong recommendation. Deviation requires a documented rationale in the loop's specification.  
**May:** A permitted option. Neither required nor discouraged.  
**Must not:** A normative prohibition. Violation is a conformance failure.

**Contract ID format:** `SPEC-001.§N.CN` where N is the section number and N is the clause number within that section. This format enables individual clauses to be cited in conformance reviews and loop specifications without ambiguity.

---

## §1 — Loop Identity Contract

Every loop document shall declare a loop identity block in its header, immediately following the document title. The identity block is machine-readable and human-readable. It must not be embedded in prose.

### §1.C1 — Mandatory Identity Fields

| Field | Format | Description |
|-------|--------|-------------|
| `Loop ID` | `LOOP-NNN` where NNN is a zero-padded three-digit integer | The unique, permanent identifier for this loop. Once assigned, a Loop ID is never reused, even if the loop is archived. |
| `Name` | Short noun phrase, ≤ 60 characters | The human-readable name that appears in dependency declarations and documentation references. |
| `Version` | `MAJOR.MINOR` (semantic; see §8) | The current version of this loop specification document. |
| `Status` | One of: `Draft`, `Review`, `Active`, `Deprecated`, `Archived` | The lifecycle state of this loop. Only `Active` loops may be placed in a live engineering chain. |
| `Category` | One of: `Core`, `Engineering`, `Platform`, `Governance`, `Release`, `AI`, `Research`, `Experimental` | The functional category that governs gate requirements and chain placement rules. `Platform` covers validation loops that assess platform runtime behaviour (LOOP-200 series). `Governance` covers loops that enforce engineering governance standards (LOOP-300 series). |
| `Depends On` | Comma-separated list of `LOOP-NNN — Name` pairs, or `None` | The complete list of upstream loops this loop consumes outputs from. Must be empty (`None`) only for chain-head loops. |
| `Human Gates` | Comma-separated list of gate types present in this loop, or `None` | At minimum: `Hard`, `Soft`, or `None`. Declares the gate surface area for chain integration planning. |

### §1.C2 — Optional Identity Fields

A loop specification may declare the following additional fields in the identity block:

| Field | Description |
|-------|-------------|
| `Owner` | The role or individual responsible for the loop's engineering correctness. Not a personal name unless required by governance policy. |
| `Maintainer` | The role or individual responsible for keeping this specification current. May differ from Owner. |
| `Max Run Duration` | The declared maximum elapsed time for a single run. Required for loops with time-bounded execution. |
| `Introduced In` | The date this loop was first set to `Active` status, in `YYYY-MM-DD` format. |

### §1.C3 — Identity Immutability

A loop's `Loop ID` and `Name` are immutable once set to `Active`. If a loop is substantially redesigned, a new Loop ID is assigned and the prior loop is set to `Deprecated`. Renaming an Active loop without assigning a new Loop ID is a conformance failure.

### §1.C4 — Status Transition Rules

Valid status transitions:

```
Draft    → Review      (specification complete; submitted for conformance review per SPEC-011 Phase 4)
Review   → Active      (Architecture Review and Engineering Review both approved; version set to 1.0)
Review   → Draft       (review returned specification for revision; version incremented)
Active   → Deprecated  (superseded by a new loop or declared unnecessary)
Deprecated → Archived  (no active chain references this loop; minimum 90-day deprecation window elapsed)
Draft    → Archived    (abandoned before reaching Active; no 90-day requirement)
```

No other transitions are valid. A loop may not transition from `Deprecated` back to `Active`; a new loop must be created if reactivation is needed. The `Review` state is the in-progress conformance review state; a loop in `Review` is complete but not yet approved for production execution. This state aligns with the LOOP-STANDARD.md lifecycle and the SPEC-011 Architecture Review and Engineering Review phases.

---

## §2 — Inputs Contract

### §2.C1 — Inputs Table Requirement

Every loop shall declare a complete, structured inputs table. The table must be a first-class section of the loop specification (not embedded in prose) and must contain one row per declared input. No input consumed by the loop may be absent from this table.

### §2.C2 — Required Input Row Fields

Each row in the inputs table shall contain:

| Column | Content |
|--------|---------|
| `Input` | The human-readable name of the input |
| `Type` | One of: `File`, `Directory`, `External Service`, `Repository State`, `Configuration`, `Signal` |
| `Source` | The loop or system that produces this input |
| `Required` | `Required` or `Optional`, with a qualification when relevant (e.g., "Required if task type is SEC") |

### §2.C3 — Upstream Loop Output Consumption

When a loop declares an input whose source is another loop, the input must reference the producing loop by ID and the specific output file or artefact name. A declaration of "LOOP-004 outputs" without naming the specific file is not a valid input declaration.

### §2.C4 — Input Validation Requirements

Every loop shall specify, in prose or a validation table, the checks it performs on its required inputs before beginning execution. At minimum, these checks shall cover:

- File existence for all required file inputs
- Non-emptiness for all required file inputs
- HEAD SHA consistency for all inputs that carry a SHA provenance field (the current repository HEAD SHA must match the SHA recorded in the input when the input was produced)
- Concurrent run detection for the same task (no two instances of the same loop may run for the same task simultaneously)

### §2.C5 — Precondition Gate for SHA Mismatch

When an input's recorded HEAD SHA does not match the current repository HEAD SHA, the loop shall treat this as a Hard Gate condition rather than a precondition failure. The discrepancy may indicate concurrent repository modification; human judgment is required to determine whether to re-run the producing loop or to acknowledge and proceed.

### §2.C6 — Optional Input Handling

A loop that declares optional inputs shall specify, for each optional input, the behaviour when the input is absent. Acceptable behaviours: (a) proceed with reduced scope (the scope reduction is declared), (b) skip the steps that depend on this input (the skipped steps are named), or (c) produce a `candidate`-confidence output for the affected categories. A loop may not silently discard an absent optional input without recording the absence.

---

## §3 — Outputs Contract

### §3.C1 — Outputs Table Requirement

Every loop shall declare a complete, structured outputs table. The table must be a first-class section. Every artefact that the loop writes must appear in this table.

### §3.C2 — Required Output Row Fields

Each row in the outputs table shall contain:

| Column | Content |
|--------|---------|
| `Artefact` | The human-readable name |
| `Path` | The full relative path from the repository root |
| `Description` | One sentence describing the content and its consumer |

### §3.C3 — Output Directory Isolation

Every loop shall write its primary outputs to a dedicated directory that no other loop writes to as its primary output directory. The naming convention is `docs/{loop-function}/` (e.g., `docs/planning/`, `docs/verification/`). Shared write to a common directory is permitted only for loop state files (`docs/loops/core/STATUS-NNN.md`, `docs/loops/core/SKILL-NNN.md`) and only by the loop that owns those files.

### §3.C4 — Loop State File Requirements

Every loop shall produce and maintain exactly two loop state files:

**STATUS-NNN.md** — Run state and history. At minimum, shall record:
- Current status (`idle`, `running`, `completed`, `stopped`, `failed`, `blocked_on_dependency`)
- The run ID and timestamp of the most recent run
- The outcome of the most recent run
- Open blockers (if any)
- The task ID associated with each recent run

**SKILL-NNN.md** — Calibration knowledge. At minimum, shall record:
- The observations from this loop's execution patterns that calibrate future runs
- A reference to the run ID that produced each observation
- An explicit timestamp for each observation

Both files shall be updated before the loop's run is marked closed.

### §3.C5 — Metadata Artefact Requirements

Every loop shall produce a metadata artefact (named `*-metadata.md` by convention) that records provenance for each run. The metadata artefact shall contain, at minimum:

- The loop's run ID (unique per run; format: `{LOOP-ID}-{YYYYMMDD}-{NNN}`)
- The task ID being processed
- The run ID of each upstream loop whose outputs were consumed
- The HEAD SHA at the start of the run (`run_start_sha`)
- The HEAD SHA at the end of the run (`run_end_sha`)
- The elapsed duration in seconds
- The final status

### §3.C6 — Reflection Artefact Requirement

Every loop shall produce, at the conclusion of every run regardless of outcome, a Reflection artefact at `docs/{loop-function}/reflections/REFLECTION-{LOOP-ID}-{run-id}.md`. The Reflection must contain all sections required by LOOP-STANDARD.md §10. A run for which no Reflection is produced may not be marked `completed` or `stopped` — it is `failed`.

### §3.C7 — Output Immutability After Handoff

Once a loop's outputs have been consumed by a downstream loop (evidenced by the downstream loop's metadata artefact referencing the producing loop's run ID), the producing loop's outputs for that run are immutable. A subsequent run of the producing loop produces a new set of outputs under a new run ID; it does not overwrite the prior run's outputs.

### §3.C8 — No Source File Modification by Non-Implementation Loops

Loops in the categories `Core` (except LOOP-005), `Quality`, `Release` (except designated deploy steps), `Operations`, and `Research` shall not modify repository source files, test files, or configuration files. Only LOOP-005 (Implementation) and loops in the Release category that explicitly declare deploy operations may modify repository contents. Violation of this constraint is a critical conformance failure.

---

## §4 — Execution Contract

### §4.C1 — Preconditions Declaration

Every loop shall declare a structured preconditions table. Each precondition row shall contain: a precondition ID (`PRE-N`), the condition statement, and the check method. Preconditions are checked before any step is executed and before any output file is created.

### §4.C2 — Precondition Failure Handling

A precondition failure (other than HEAD SHA mismatch, which is covered by §2.C5) shall produce `status: precondition_failed` in the STATUS file and halt the loop without modifying any output file. A loop may not proceed past a failed precondition.

### §4.C3 — Trigger Declaration

Every loop shall declare, in a dedicated Trigger section, the complete set of conditions that may initiate a run. Triggers must be enumerated exhaustively. A run initiated by an undeclared trigger is an ungoverned execution and shall be treated as a precondition failure.

### §4.C4 — Workflow Step Requirements

Every loop shall declare its workflow as an ordered sequence of numbered steps. Each step shall declare:

- A step number
- A title
- The responsible agent (by role ID)
- The inputs consumed by this step
- The outputs produced by this step

Steps that modify outputs or repository state shall additionally declare:
- The scope of the modification (files, directories, or state fields affected)
- The rollback action that restores the prior state if the step must be reversed

### §4.C5 — Emergency Stop

Every loop shall check for an Emergency Stop signal at the start of each workflow step (for loops with per-step atomic execution) or at a minimum at the start of each major phase. The check mechanism is: read the loop's STATUS file; if `status: emergency_stopped`, halt immediately. On halt: apply the current step's rollback action if the step was in progress; write the step at which the stop was received and the current HEAD SHA to the STATUS file; produce a partial Reflection.

Emergency Stop must never be ignored or deferred.

### §4.C6 — Maximum Run Duration

Every loop shall declare a maximum run duration. If the loop has not reached a Stop Condition within this duration, it shall halt with an appropriate `stopped` reason (e.g., `step_timeout`, `plan_timeout`, `run_timeout`). A loop with no declared maximum run duration is a conformance failure — unbounded execution is not permitted in a governed engineering chain.

### §4.C7 — Atomic Step Integrity

For loops that execute atomic steps (primarily LOOP-005), each step shall be either fully applied or fully unapplied. Partial application is a recovery event; the loop shall apply the step's declared rollback action and record the partial application before halting.

### §4.C8 — Idempotency Requirement

Every loop shall be designed so that re-executing it with identical inputs produces equivalent outputs. For loops that modify repository state (LOOP-005), idempotency applies to file modifications: re-executing a step that creates or updates a file must produce the same file content. Delete steps are exempt from the idempotency requirement; their idempotency behaviour must be explicitly declared.

---

## §5 — State Contract

### §5.C1 — STATUS File as Execution Record

The STATUS file is the authoritative record of a loop's execution state. Any agent, engineer, or downstream loop may read the STATUS file to determine the current state of a loop without reading any other artefact. The STATUS file must be kept current: updated at the start of each step (recording the step as `in_progress`) and at the end of each step (recording the step as `completed`, `failed`, or `rolled_back`).

### §5.C2 — STATUS File Required Fields

A STATUS file shall contain, at minimum, the following fields in a structured, machine-readable format:

| Field | Description |
|-------|-------------|
| `loop_id` | The Loop ID |
| `current_status` | Current lifecycle state of the loop |
| `current_run_id` | Run ID of the active run, or `null` |
| `current_task_id` | Task ID being processed, or `null` |
| `last_updated` | ISO 8601 timestamp of the last STATUS file write |
| `last_completed_run` | Run ID of the most recently completed run |
| `last_outcome` | Outcome of the most recently completed run |
| `open_blockers` | List of current blockers (empty list if none) |
| `gate_outcomes` | Record of all gate outcomes from the current run |
| `emergency_stopped` | Boolean — whether the current run received an Emergency Stop |

### §5.C3 — SKILL File as Calibration Record

The SKILL file is the authoritative calibration record for a loop. It accumulates observations across runs and informs future run configuration. SKILL file entries are never deleted; they may be superseded (an entry explicitly marks a prior entry obsolete by run ID reference).

### §5.C4 — SKILL File Required Fields per Entry

Each SKILL file entry shall contain: the run ID that produced the observation, the date, the observation text, the metric or evidence that supports it, and the applicability scope (task category, technology class, or `all`).

### §5.C5 — Checkpoint Requirements

For loops with long-running or multi-step execution, checkpoints shall be declared at natural pause points where the loop's state can be persisted and execution can be resumed. Each checkpoint shall be declared with: a checkpoint ID, the position in the workflow, and the state that must be persisted to enable resumption from that checkpoint.

### §5.C6 — Resumption Protocol

A loop that supports resumption (from a prior interrupted run) shall declare its resumption protocol explicitly. The protocol must specify: how prior completed steps are detected, how the resumption starting point is determined, and whether accumulated outputs from prior steps are reloaded or regenerated.

### §5.C7 — Metadata Persistence Requirements

The metadata artefact produced by each run shall be retained indefinitely. Old run metadata may not be overwritten by a new run's metadata. If the output directory grows large, archival of old metadata is permitted but not deletion.

---

## §6 — Verification Contract

### §6.C1 — Verification Criteria Table Requirement

Every loop shall declare a structured verification criteria table with one row per criterion. Each row shall contain: a criterion ID (`VER-N`), the criterion statement, and the check method. Verification criteria are the postconditions that must be satisfied before a run is marked `completed`.

### §6.C2 — Minimum Verification Criteria Count

Every loop shall declare at least eight verification criteria. The minimum set shall include:
- At least one criterion verifying that required outputs exist and are non-empty
- At least one criterion verifying that no unauthorised modifications were made (for non-implementation loops, this means VER-N confirms no source files were modified)
- At least one criterion verifying that the STATUS file was updated
- At least one criterion verifying that the Reflection artefact was produced

### §6.C3 — Checker Independence

For every artefact produced by a Maker agent, a Checker agent shall perform independent verification before the loop's run is marked `completed`. The same agent instance may not act as both Maker and Checker for the same artefact in the same run. This requirement applies regardless of whether "agents" are implemented as distinct model instances, distinct processes, or distinct invocations of the same model — the independence guarantee is structural, not technical.

### §6.C4 — Evidence Standards

Every verification criterion result shall be supported by specific, reproducible evidence. Evidence means: a file path and content excerpt, a command and its output, a metric value and its source, or a direct comparison between a declared expectation and an observed value. A criterion recorded as `passed` without cited evidence is a verification integrity failure.

### §6.C5 — Verification Scope Fidelity

A loop's verification criteria must be scoped to the loop's own outputs. A loop may not claim credit for verification performed by a downstream loop. A loop that passes its own verification criteria is verified to have produced its declared outputs correctly; it is not verified to have produced outputs that satisfy downstream requirements.

### §6.C6 — Failure Criteria Declaration

Every loop shall declare, in a dedicated Failure Recovery section, the recovery procedures for each class of failure it may encounter. Each recovery procedure shall specify: the detection mechanism, the immediate action, the recovery path, the rollback scope, and the reporting obligation. A failure mode with no declared recovery procedure is an unhandled failure — all unhandled failures produce `status: failed` and trigger GATE-1 escalation.

---

## §7 — Dependency Contract

### §7.C1 — Dependency Declaration Completeness

A loop's `Depends On` field in the identity block shall list every upstream loop whose outputs the loop consumes. The list shall be complete: a loop that consumes an output from an upstream loop not listed in `Depends On` is in violation of the dependency contract. Undeclared dependencies produce opaque failures and undermine chain integrity.

### §7.C2 — Mandatory vs. Optional Dependencies

For each declared dependency, the loop specification shall distinguish between mandatory dependencies (the loop cannot begin without the upstream loop's completed outputs) and optional dependencies (the loop proceeds with reduced scope when the upstream output is absent). A dependency not explicitly marked as optional is mandatory.

### §7.C3 — Dependency Freshness Requirements

Every loop shall declare the freshness requirements for each upstream dependency it consumes. The canonical freshness mechanism is HEAD SHA comparison (see §2.C5), but a loop may additionally declare time-based freshness thresholds (e.g., "LOOP-001 outputs must be no more than 7 days old"). When a freshness threshold is violated, the loop shall treat it as a Soft Gate condition unless the dependency is in the Security or Compliance category, in which case it is a Hard Gate condition.

### §7.C4 — Circular Dependency Prohibition

No loop in an Active chain shall have a dependency graph that contains a cycle. A loop that depends (directly or transitively) on a loop that depends on it is a conformance failure. This constraint is verified during conformance review; it applies both to direct dependencies and to optional dependencies.

### §7.C5 — Downstream Loop Awareness

A loop specification should document its known downstream consumers (the loops that consume its outputs) in a dedicated section or within the outputs table. This awareness is not a dependency declaration in the upstream direction — it is documentation for engineers who must understand change impact. A loop may not use knowledge of its downstream consumers to alter its own behaviour (e.g., producing different outputs based on which downstream loop will consume them).

### §7.C6 — Dependency Version Compatibility

When a loop declares a dependency on another loop, the dependency is compatible with any version of the upstream loop that maintains the same output schema. A loop shall declare, for each upstream dependency, the minimum version that satisfies its input requirements. If the upstream loop has been revised in a way that changes the output schema, both loops' versions must be updated together (a co-revision).

---

## §8 — Versioning Contract

### §8.C1 — Semantic Versioning Format

Every loop specification uses `MAJOR.MINOR` versioning. `MAJOR` and `MINOR` are non-negative integers. There is no patch component — specification documents do not distinguish between bug fixes and features at a third version level. Version `1.0` is the initial Active version; `0.x` versions are Draft.

### §8.C2 — MAJOR Version Increment Triggers

The MAJOR version shall be incremented when any of the following changes are made to the loop specification:

- A required input is added, removed, or renamed
- A required output is added, removed, or renamed
- A mandatory verification criterion is added or removed
- A workflow step is added, removed, or reordered
- A gate type changes (Hard to Soft, or Soft to Hard)
- A gate trigger condition is added or removed
- The Maker/Checker assignment for an artefact changes
- A declared external state write is added or removed

A MAJOR version increment signals to all consuming loops that their dependency declarations may need to be reviewed for compatibility.

### §8.C3 — MINOR Version Increment Triggers

The MINOR version shall be incremented when any of the following changes are made:

- Documentation is clarified without changing behaviour
- A new optional input is added
- A new optional output is added
- A new optional verification criterion is added
- An informational section (Future Improvements, References) is updated
- The Reflection or SKILL sections are augmented without changing required content

A MINOR version increment is backward-compatible and does not require downstream loop review.

### §8.C4 — Version History Requirement

Every loop specification shall maintain a Version History section as its final section. The Version History shall record, for each version, the version number, the date, the author or authority, and a concise description of what changed. The Version History entry for MAJOR increments shall identify each breaking change by type (using the categories in §8.C2).

### §8.C5 — Deprecation Policy

A loop that is being superseded or retired shall be moved to `Deprecated` status before being set to `Archived`. The minimum deprecation window is 90 days for any loop with active downstream consumers. During this window, the Deprecated loop continues to function for existing chain configurations, but new chain configurations may not declare a dependency on a Deprecated loop. After the window, the loop may be set to `Archived` (moved to `docs/loops/archive/`). The terminal state is `Archived`, not `Retired`; these terms are synonymous within this framework but `Archived` is the canonical term aligned with LOOP-STANDARD.md.

### §8.C6 — Backward Compatibility Guarantee

A loop at version `N.M` shall be compatible with all downstream loops that declared a dependency on version `N.x` (any MINOR version within the same MAJOR). A loop at version `N+1.0` is not guaranteed to be compatible with downstream loops that declared a dependency on version `N.x`. A MAJOR version bump obligates the loop's author to communicate breaking changes to all known downstream loop maintainers before the new version is set to Active.

---

## §9 — Governance Contract

### §9.C1 — Hard Gate Requirements by Category

The following minimum Hard Gate requirements apply based on a loop's declared Category:

| Category | Minimum Hard Gates |
|----------|--------------------|
| `Core` | At least one Hard Gate before any modification to shared repository state (APIs, schemas, security controls, infrastructure) |
| `Engineering` | At least one Hard Gate before merging significant changes; Human Gates section must declare specific trigger conditions |
| `Platform` | At least one Hard Gate before any write to a shared repository artifact or before a validation finding is recorded that would block a release |
| `Governance` | At least one Hard Gate before any governance document (ADR, compliance report, release readiness assessment) is published or any compliance finding is recorded as authoritative |
| `Release` | At least two Hard Gates: one before deployment begins, one before production cutover |
| `AI` | At least one Hard Gate before model deployment or when safety limits are exceeded |
| `Research` | Soft Gates only, unless a finding would trigger an immediate remediation action |
| `Experimental` | Soft Gates only, unless a write to a shared repository asset is performed |

### §9.C2 — Soft Gate Requirements

Every loop shall declare at least one Soft Gate for conditions that are concerning but recoverable without mandatory human intervention. A loop with no Soft Gates must explicitly justify the omission in its Human Approval Gates section. A loop with only Hard Gates and no Soft Gates may be appropriate for high-stakes categories; a loop with only Soft Gates and no Hard Gates is not appropriate for Core, Engineering, or Quality categories.

### §9.C3 — Gate Timeout Declaration

Every Soft Gate shall declare: the timeout duration, the notification channel, the auto-proceed action, and the audit trail requirements. A Soft Gate with no timeout is a Hard Gate and must be reclassified.

### §9.C4 — Approver Role Specification

Every Hard Gate shall declare the approver role (not a specific individual name unless required by governance policy). The approver role shall be descriptive enough to identify who holds the authority without requiring organisational knowledge: for example, "Principal Engineer or Architecture Owner" is valid; "Engineering Manager" is not valid without a qualifier.

### §9.C5 — Gate Audit Trail

Every gate outcome — approval, denial, auto-proceed — shall be recorded in the STATUS file with: the gate ID, the trigger condition, the reviewer identity and role (for Hard Gates), the decision, the rationale, and the timestamp. Gate outcomes may not be expunged from the STATUS file. They are permanent records of governance decisions.

### §9.C6 — Maker/Checker Assignment

Every loop shall declare, in its Agents section, which agents are designated as Makers (producers of artefacts) and which are designated as Checkers (independent reviewers of those artefacts). The assignment shall be per-artefact. The Maker/Checker prohibition states: the same agent instance may not act as both Maker and Checker for the same artefact in the same run. This prohibition is non-negotiable and applies to all loops in all categories.

### §9.C7 — Ownership and Maintainability

A loop's Owner is responsible for the engineering correctness of its specification. A loop's Maintainer is responsible for keeping the specification current as the repository evolves. These roles may be the same; they may not be vacant. A loop with no declared Owner and no declared Maintainer may not be set to Active. If a human engineer is not available to fill these roles, the loop must be set to Deprecated until ownership is established.

### §9.C8 — Review Expectations

A loop specification shall be reviewed by the Owner or Maintainer before each MAJOR version increment. A MINOR version increment may be made by the Maintainer without a full review. Any engineer may propose a revision; the Owner approves revisions before they are applied.

---

## §10 — Mandatory Risk Assessment Contract

### §10.C1 — Risk Section Requirement

Every loop specification shall contain a Risks section. The Risks section must assess at minimum the following five risk categories. Each risk must be either assessed with its likelihood, impact, controls, detection method, and response, or explicitly declared as Not Applicable with a stated reason. Silence is not an acceptable response — an unaddressed risk category is a conformance failure.

| Risk ID | Risk Category | Description |
|---------|--------------|-------------|
| RISK-1 | Scope Creep | The loop produces changes or artefacts beyond its declared scope |
| RISK-2 | Architectural Drift | Outputs are inconsistent with the repository's declared architecture |
| RISK-3 | Hidden Dependencies | Runtime dependencies not declared in the dependency contract emerge during execution |
| RISK-4 | Tenant Isolation Breach | The loop's actions create a path through which one tenant can access another's data |
| RISK-5 | Data Loss or Corruption | The loop's outputs overwrite or corrupt prior artefacts or repository state |
| RISK-6 | Non-Idempotent External Write | The loop writes to external state in a way that cannot be safely re-executed |
| RISK-7 | Security Boundary Violation | The loop's actions weaken a declared security control |
| RISK-8 | Runaway Execution | The loop runs without bound, consuming resources or modifying state indefinitely |

Note: RISK-4 (Tenant Isolation) and RISK-6 (Non-Idempotent External Write) are expected to be Not Applicable for loops that are read-only with respect to application data and external services. The explicit N/A declaration with rationale is the required form.

### §10.C2 — Risk Severity Scale

All risk assessments shall use the following likelihood and impact scale:

| Level | Likelihood | Impact |
|-------|-----------|--------|
| `Critical` | N/A | Loss of data, security breach, or production incident |
| `High` | Occurs in > 20% of runs | Engineering rework, significant delay, or stakeholder escalation |
| `Medium` | Occurs in 5–20% of runs | Detectable deviation, gate trigger, or additional verification round |
| `Low` | Occurs in < 5% of runs | Minor deviation, informational finding, or documentation gap |

---

## §11 — External State Contract

### §11.C1 — External State Table Requirement

Every loop specification shall contain a structured External State table that enumerates every system or resource the loop reads from or writes to. No external state interaction may be undeclared.

### §11.C2 — Required External State Row Fields

Each row in the External State table shall contain:

| Column | Content |
|--------|---------|
| `System` | The name of the external system or resource |
| `Operation` | `Read`, `Write`, or `Read-Write` |
| `Scope` | The specific files, directories, tables, or endpoints accessed |
| `Auth` | The authentication or permission mechanism required |
| `Isolation` | How the operation is isolated from unrelated state (e.g., scoped to declared files only) |
| `Rollback` | How the operation is reversed if the loop must roll back |
| `Idempotent` | `Yes`, `No`, or `Conditional` — with a condition note if Conditional |

### §11.C3 — Prohibition on Undeclared External Writes

A loop shall not write to any system, file, or resource not declared in its External State table. An undeclared write discovered during execution is a critical conformance failure and shall trigger GATE-1 regardless of the loop's category. The write must be reversed if reversible; it must be recorded as an unauthorised modification if irreversible.

### §11.C4 — Secret Values Prohibition

No loop may write a secret value (API key, password, certificate, token, or credential) to any tracked file, log, or artefact. Secret values are managed outside the loop chain, in the secret management system declared in the repository's configuration. A loop that encounters a secret value during execution must pass it by reference, not by value.

---

## §12 — Reflection Contract

### §12.C1 — Reflection Section Requirement

Every loop specification shall define a Reflection section that specifies: the required sections of the per-run Reflection artefact, the agent responsible for producing it, and any loop-specific additions beyond the ten required by LOOP-STANDARD.md §10.

### §12.C2 — Reflection Timing

The Reflection artefact shall be produced before the loop's run is marked closed. A run may not be marked `completed` or `stopped` without a Reflection. A run may be marked `failed` without a Reflection only if the failure occurred before any output was produced; in all other cases, a partial Reflection is required even for `failed` runs.

### §12.C3 — Reflection Scope

The Reflection covers the loop's own execution. It does not evaluate the correctness of the implementation it processed (that is LOOP-006's domain), the quality of the task it was given (that is LOOP-007's domain), or any other loop's behaviour. A loop's Reflection is strictly self-referential: what did this loop do, what worked, what did not, and what should change in future runs of this loop.

---

## §13 — Extensibility Contract

### §13.C1 — Extension by Addition

A future LOOP-XXX specification may add sections beyond those required by this contract and by LOOP-STANDARD.md without violating compatibility. Additional sections must not conflict with the mandatory sections. An additional section that redefines a mandatory field or overrides a normative requirement of this contract is not a conforming extension — it is a contract violation.

### §13.C2 — Extension Naming Convention

Additional sections shall be named with a prefix that identifies them as loop-specific extensions, to prevent collision with future contract sections. The recommended prefix format is `{LOOP-ID}:` (e.g., `LOOP-005: Change Governance`). If SPEC-001 is later revised to include a section that was previously a loop-specific extension, the loop-specific section shall be retired in the same MINOR revision that adopts it.

### §13.C3 — Contract Amendment Process

This contract (SPEC-001) may be amended by incrementing its version. An amendment that adds a new mandatory section (§N.CX becoming `shall`) is a MAJOR version increment and requires conformance review of all Active loops before the amendment takes effect. An amendment that clarifies existing normative language without changing obligations is a MINOR version increment and does not require re-review of conforming loops.

### §13.C4 — Backward Compatibility of Amendments

A loop that was conformant with SPEC-001 version `N.x` and has not been modified remains conformant with SPEC-001 version `N.M` (same MAJOR, higher MINOR). It is not required to be conformant with SPEC-001 version `N+1.0` until its next review cycle, providing a minimum 90-day grace period from the date the new SPEC-001 MAJOR version is declared Active.

### §13.C5 — New Loop Categories

A new Category value may be added to the §1.C1 identity field enumeration via a MINOR version increment, provided the new Category comes with a declared minimum gate requirement in §9.C1. An existing Category may not be renamed or removed without a MAJOR version increment.

### §13.C6 — Future Specification Documents

Additional `SPEC-NNN` documents may be created to define contracts for specific subsystems (e.g., SPEC-002 for Agent Protocols, SPEC-003 for Knowledge Base Schema). These documents do not supersede SPEC-001; they supplement it. A future SPEC-NNN that conflicts with SPEC-001 must explicitly declare the conflict and the resolution mechanism in its own Purpose section.

---

## §14 — Conformance Summary

A loop is conformant with SPEC-001 version 1.0 when all of the following are true. This checklist is the authoritative gate for setting a loop's status to `Active`.

### Identity and Structure

- [ ] §1.C1: All seven mandatory identity fields are present and correctly formatted in the document header
- [ ] §1.C3: Loop ID and Name are not duplicates of any existing Active or Deprecated loop
- [ ] §1.C4: The loop's current Status is a valid lifecycle state

### Inputs

- [ ] §2.C1: A structured inputs table is present and complete
- [ ] §2.C2: Every input row contains all four required columns
- [ ] §2.C4: Input validation requirements are declared in prose or a validation table
- [ ] §2.C5: HEAD SHA mismatch is treated as a Hard Gate condition, not a precondition failure

### Outputs

- [ ] §3.C1: A structured outputs table is present and complete
- [ ] §3.C3: All outputs are written to the loop's dedicated directory (except STATE files)
- [ ] §3.C4: STATUS-NNN.md and SKILL-NNN.md are declared as loop outputs
- [ ] §3.C5: A metadata artefact is declared with all required provenance fields
- [ ] §3.C6: A Reflection artefact is declared
- [ ] §3.C8: The loop does not declare source file modification unless it is LOOP-005 or a designated Release loop

### Execution

- [ ] §4.C1: A structured preconditions table is present with all required columns
- [ ] §4.C3: A Trigger section is present with an exhaustive enumeration
- [ ] §4.C4: Each workflow step declares the responsible agent, inputs, and outputs
- [ ] §4.C5: Emergency Stop is declared and handled at each step boundary
- [ ] §4.C6: A maximum run duration is declared
- [ ] §4.C8: Idempotency behaviour is declared (with exemptions noted)

### State

- [ ] §5.C1: The STATUS file is updated at each step (start and end)
- [ ] §5.C2: The STATUS file contains all required fields
- [ ] §5.C4: The SKILL file entry format includes all required fields

### Verification

- [ ] §6.C1: A structured verification criteria table is present
- [ ] §6.C2: At least eight verification criteria are declared, including the minimum required types
- [ ] §6.C3: Maker/Checker assignment is declared per artefact; the same agent does not hold both roles for any artefact
- [ ] §6.C6: A Failure Recovery section is present with a procedure for each declared failure class

### Dependencies

- [ ] §7.C1: The `Depends On` field is complete and accurate
- [ ] §7.C2: Each dependency is classified as mandatory or optional
- [ ] §7.C3: Freshness requirements are declared for each upstream dependency
- [ ] §7.C4: No circular dependency exists (direct or transitive)

### Versioning

- [ ] §8.C1: Version is in `MAJOR.MINOR` format; Active loops are at `1.0` or higher
- [ ] §8.C4: A Version History section is present as the final section

### Governance

- [ ] §9.C1: The loop meets the minimum Hard Gate requirement for its Category
- [ ] §9.C2: At least one Soft Gate is declared (or the omission is justified)
- [ ] §9.C3: Every Soft Gate declares a timeout, notification channel, auto-proceed action, and audit trail
- [ ] §9.C4: Every Hard Gate declares an approver role
- [ ] §9.C5: Gate audit trail requirements are declared
- [ ] §9.C6: Maker/Checker roles are assigned in the Agents section per artefact

### Risk

- [ ] §10.C1: All eight mandatory risk categories are assessed or explicitly declared Not Applicable with rationale

### External State

- [ ] §11.C1: An External State table is present
- [ ] §11.C2: Every row contains all seven required columns
- [ ] §11.C3: No undeclared external writes exist in the workflow description

### Reflection

- [ ] §12.C1: A Reflection section is present specifying the required artefact structure
- [ ] §12.C2: The timing of Reflection production is declared (before run closure)

**Conformance determination:** A loop satisfies all checked items → Conformant with SPEC-001 v1.0.  
Any unchecked item → Non-conformant. The loop may not be set to Active until all items are satisfied.

---

## §15 — Contract Integrity

### §15.C1 — Self-Referential Application

This document (SPEC-001) does not itself implement the Loop Contracts because it is not a loop. It is exempt from the conformance checklist in §14. However, it is subject to the versioning rules in §8 and the amendment process in §13.C3. Changes to this document follow those rules.

### §15.C2 — Conflict Resolution

If a specific loop specification conflicts with this contract on a normative requirement (a `shall` clause), this contract takes precedence. The loop specification must be revised to conform. If the conflict is intentional and justified (a loop operating in an unusual context requires a genuine exception), the exception shall be declared explicitly in the loop specification's header block as `Contract Exception: SPEC-001.§N.CN — [reason]`. A declared exception is reviewed as part of the conformance process; it may be approved or denied. An undeclared conflict is always a conformance failure.

### §15.C3 — Governing Authority

SPEC-001 is governed by the Principal Architecture function. Amendments to normative clauses require Principal Architecture approval. Amendments to informational content (references, examples, future improvements) may be made by the Maintainer without Principal Architecture review.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — the per-loop specification standard that implements this contract's requirements in a prescriptive section structure
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — reference implementation; first conformant loop
- `docs/loops/core/LOOP-002-Context-Assembly.md` — reference implementation; illustrates optional dependency handling
- `docs/loops/core/LOOP-003-Task-Discovery.md` — reference implementation; illustrates two-gate configuration
- `docs/loops/core/LOOP-004-Planning.md` — reference implementation; illustrates assumption and risk register patterns
- `docs/loops/core/LOOP-005-Implementation.md` — the sole loop authorised to modify repository source files; reference for §3.C8 exception
- `docs/loops/core/LOOP-006-Verification.md` — reference implementation; illustrates the three-outcome taxonomy and evidence standards
- `docs/loops/core/LOOP-007-Reflection.md` — reference implementation; illustrates accumulative output design and knowledge base contracts
- `docs/loops/shared/engineering-principles.md` — engineering principles that inform but do not supersede this contract
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions referenced by §9

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Defines the complete Loop Contract across fifteen sections governing identity, inputs, outputs, execution, state, verification, dependencies, versioning, governance, risk, external state, reflection, extensibility, conformance, and contract integrity. Derived from the observed patterns across LOOP-001 through LOOP-007.
- **1.1** — 2026-06-27 — Principal AI Engineering Architect — Added: `Platform` and `Governance` to §1.C1 Category enumeration with descriptions. Added: `Review` as a valid lifecycle state to §1.C4 with explicit transition rules aligning with LOOP-STANDARD.md and SPEC-011 lifecycle phases. Added: `Platform` and `Governance` rows to §9.C1 minimum Hard Gate requirements table. Changed: terminal lifecycle state from `Retired` to `Archived` in §8.C5 for consistency with LOOP-STANDARD.md and the archive directory convention. All changes are MINOR (additive, backward-compatible with loops conformant at v1.0).

