---
# PROVENANCE METADATA
Original Path: docs/loops/templates/LOOP-TEMPLATE.md
Original Version: 0.1
Extraction Date: 2026-06-27
Original Purpose: Loop specification or framework asset.
Generalized Purpose: Loop specification or framework asset.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-XXX — Name, LOOP-YYY — Name  *(or "None" for chain-head loops)*
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-XXX — Name

<!-- Replace LOOP-XXX with the assigned Loop ID (e.g., LOOP-108). Replace Name with the loop's Title-Case-Hyphenated name. -->

**Loop ID:** LOOP-XXX
**Name:** Name
**Version:** 0.1
**Status:** Draft
**Category:** Core | Engineering | Platform | Governance | Release
**Depends On:** LOOP-XXX — Name, LOOP-YYY — Name  *(or "None" for chain-head loops)*
**Human Gates:** Hard, Soft  *(list gate types present; use "None" only if explicitly justified)*
**Owner:** <!-- Role responsible for engineering correctness of this specification -->
**Maintainer:** <!-- Role responsible for keeping this specification current; may be same as Owner -->

---

## Purpose

<!-- One to three paragraphs. State what the loop does, why it exists, and what the system would lose if this loop did not exist. Do NOT describe the workflow here — that belongs in ## Workflow. -->

---

## Problem Statement

<!-- One to two paragraphs describing the engineering problem this loop solves. Frame the problem without presupposing the solution. What goes wrong in the absence of this loop? -->

---

## Why This Loop Exists

<!-- One to two paragraphs explaining the structural reason this is a loop rather than a step in an existing loop or a human-performed activity. Reference at least one of: repeatability, verifiability, auditability, safety, or scale. -->

---

## Scope

**In scope:**
<!-- Bullet list of what this loop explicitly does. Be specific. -->

**Out of scope:**
<!-- Bullet list of what this loop explicitly does NOT do. This is as important as the In scope list — it defines the boundaries that prevent scope creep. Reference other loops by ID where appropriate. -->

**Maximum run duration:** <!-- e.g., 4 hours. Required by SPEC-001 §4.C6. -->

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| <!-- Input name --> | <!-- File \| Directory \| External Service \| Repository State \| Configuration \| Signal --> | <!-- Loop ID or system --> | <!-- Required \| Optional --> |

<!-- Add one row per input. For Optional inputs, declare fallback behaviour in a note after the table. -->

### Input Validation

<!-- Declare the checks performed on required inputs before execution begins. Must include: file existence, non-emptiness, HEAD SHA consistency for provenance-carrying inputs, concurrent-run detection. -->

---

## Outputs

All outputs are written to `docs/<!-- function name -->/`. On first run this directory is created; on subsequent runs files are updated in place.

| Artifact | Path | Description |
|----------|------|-------------|
| <!-- Artifact name --> | `docs/<!-- path -->/<!-- filename -->` | <!-- One sentence: content and primary consumer --> |
| Loop Status | `docs/loops/<!-- category -->/STATUS-XXX.md` | Run state, metrics, and open blockers for this loop |
| Loop Skill | `docs/loops/<!-- category -->/SKILL-XXX.md` | Calibration knowledge accumulated across runs |
| Run Metadata | `docs/<!-- function -->/metadata/METADATA-XXX-{run-id}.md` | Provenance: run ID, HEAD SHA start/end, upstream dependency run IDs, duration, final status |
| Reflection | `docs/<!-- function -->/reflections/REFLECTION-XXX-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

<!-- For each dependency: Loop ID, name, what this loop consumes, and whether it is mandatory or optional. -->

- **LOOP-XXX — Name:** What this loop consumes from it. Mandatory | Optional.

<!-- If no dependencies: "None. This is a chain-head loop." -->

---

## Trigger

<!-- Enumerate ALL conditions that may initiate a run. Be exhaustive — a run initiated by an undeclared trigger is an ungoverned execution (SPEC-001 §4.C3). -->

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop.
2. <!-- Additional triggers... -->

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | <!-- Condition that must be true before Step 1 --> | <!-- How to verify it programmatically --> |

<!-- All preconditions are checked before any output file is created. A failed precondition halts the loop with status precondition_failed. -->

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| <!-- System name and type --> | <!-- Read \| Write \| Read-Write --> | <!-- Specific resources accessed --> | <!-- Auth mechanism --> | <!-- How cross-tenant/cross-env contamination is prevented --> | <!-- Reversal strategy if loop fails mid-write --> | <!-- Yes \| No \| Conditional --> |

---

## Required Context

<!-- List all documents and artefacts the executing agent must load before beginning Step 1. -->

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard for loop execution
2. `docs/loops/<!-- category -->/LOOP-XXX-Name.md` — this document
3. `docs/loops/<!-- category -->/STATUS-XXX.md` — prior run state (if it exists)
4. <!-- Additional context files -->

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| <!-- AGENT-ID --> | <!-- Maker \| Checker --> | <!-- Steps N–M: what this agent does --> | <!-- Tools available --> | <!-- Gate assignment --> |

<!-- Every artifact-producing step must have exactly one Maker and one Checker. The same agent may not be both Maker and Checker for any artifact. -->

---

## Workflow

<!-- Write each step as a subsection. Steps are ordered and non-ambiguous. Write in present tense. -->

### Step 1 — <!-- Step Name -->

**Agent:** `<!-- AGENT-ID -->`
**Inputs:** <!-- Consumed inputs -->
**Outputs:** <!-- Produced outputs -->

<!-- Step description: what the agent does, what decisions it makes, what it produces. -->

---

**[GATE-1 — <!-- Hard \| Soft --> Gate: <!-- Gate Name -->]**

<!-- Brief description of when this gate fires and what it does. Full gate specification is in ## Human Approval Gates. -->

---

### Step 2 — <!-- Step Name -->

**Agent:** `<!-- AGENT-ID -->`
**Inputs:** <!-- Consumed inputs -->
**Outputs:** <!-- Produced outputs -->

<!-- Step description -->

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | <!-- Falsifiable condition that must be true for run to be marked completed --> | <!-- How to evaluate it independently without relying on Maker self-report --> |
| VER-2 | All required output artifacts exist and are non-empty | Check file existence and size > 0 for each path in Outputs table |
| VER-3 | No source files were modified (unless this loop is LOOP-005 or a designated Release deploy loop) | Run `git diff --name-only` and assert no source, test, or config files are changed |
| VER-4 | `STATUS-XXX.md` has been updated with the current run ID and a timestamp within 5 minutes of the current time | Read STATUS file; assert run ID present and timestamp within tolerance |
| VER-5 | Reflection artifact has been produced at the declared path | Check file existence at `docs/<!-- function -->/reflections/REFLECTION-XXX-{run-id}.md` |
| VER-6 | <!-- At least 3 more criteria specific to this loop's outputs --> | |
| VER-7 | | |
| VER-8 | | |

<!-- Minimum 8 criteria required by SPEC-001 §6.C2. -->

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/<!-- function -->/reflections/REFLECTION-XXX-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- <!-- Loop-specific reflection section 1 -->
- <!-- Loop-specific reflection section 2 -->

---

## Human Approval Gates

### GATE-1 — <!-- Hard \| Soft --> Gate: <!-- Gate Name -->

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | <!-- Hard Gate \| Soft Gate --> |
| Position in Workflow | After Step N, before Step N+1 |
| Artifact Under Review | <!-- What the human is approving --> |
| Approver | <!-- Role — not a personal name unless required by policy --> |
| Timeout | <!-- "None — explicit written approval required" for Hard Gates; "HH hours from notification timestamp" for Soft Gates --> |
| Auto-Proceed Action | <!-- Soft Gate only: what happens if no objection within timeout --> |
| Approval Denied — Action | <!-- Hard Gate only: what happens if denied --> |
| Audit Trail | Approval record written to `STATUS-XXX.md` under `gate_outcomes.GATE-1` |

**Fires when:**
- <!-- Condition 1 -->
- <!-- Condition 2 -->

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-XXX.md`. The executing agent must read `STATUS-XXX.md` at the start of each step and halt immediately if this value is present.

---

## Failure Recovery

### FR-1 — <!-- Failure Mode Name -->

**Detection:** <!-- How this failure is detected -->
**Immediate Action:** <!-- What the loop does immediately upon detection -->
**Recovery:** <!-- Recovery path -->
**Rollback:** <!-- How to restore prior state -->

---

## Metrics

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed |
| `run.steps_total` | Total workflow steps declared |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | VER criteria that passed |
| `verification.level1.fail` | VER criteria that failed |
| `reflection.produced` | Boolean — was Reflection artifact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| <!-- metric.name --> | <!-- Definition --> |

---

## Risks

### RISK-1 — Scope Creep
- **Description:** <!-- What could go wrong -->
- **Likelihood:** High | Medium | Low
- **Impact:** High | Medium | Low
- **Trigger Condition:** <!-- What causes this risk to materialize -->
- **Control:** <!-- How the loop prevents or bounds this risk -->
- **Detection:** <!-- How detection occurs -->
- **Response:** <!-- Reference to Failure Recovery procedure -->

### RISK-2 — Architectural Drift
- **Description:** Outputs are inconsistent with the repository's declared architecture.
- **Likelihood:** <!-- -->
- **Impact:** <!-- -->
- **Trigger Condition:** <!-- -->
- **Control:** <!-- -->
- **Detection:** <!-- -->
- **Response:** <!-- -->

### RISK-3 — Hidden Dependencies
- **Description:** Runtime dependencies not declared in the dependency contract emerge during execution.
- **Likelihood:** <!-- -->
- **Impact:** <!-- -->
- **Trigger Condition:** <!-- -->
- **Control:** <!-- -->
- **Detection:** <!-- -->
- **Response:** <!-- -->

### RISK-4 — Tenant Isolation Breach
- **Description:** <!-- Assess or declare N/A with rationale -->
- **Likelihood:** N/A | <!-- -->
- **Impact:** N/A | <!-- -->

### RISK-5 — Data Loss or Corruption
- **Description:** <!-- -->
- **Likelihood:** <!-- -->
- **Impact:** <!-- -->
- **Trigger Condition:** <!-- -->
- **Control:** <!-- -->
- **Detection:** <!-- -->
- **Response:** <!-- -->

### RISK-6 — Non-Idempotent External Write
- **Description:** <!-- Assess or declare N/A with rationale -->
- **Likelihood:** N/A | <!-- -->
- **Impact:** N/A | <!-- -->

### RISK-7 — Security Boundary Violation
- **Description:** <!-- -->
- **Likelihood:** <!-- -->
- **Impact:** <!-- -->
- **Trigger Condition:** <!-- -->
- **Control:** <!-- -->
- **Detection:** <!-- -->
- **Response:** <!-- -->

### RISK-8 — Runaway Execution
- **Description:** The loop runs without bound, consuming resources indefinitely.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** A workflow step does not terminate within its time budget.
- **Control:** Maximum run duration declared in Scope; FR procedure halts and records partial outputs.
- **Detection:** Wall-clock duration exceeds declared maximum.
- **Response:** FR-N (maximum run duration exceeded procedure).

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | <!-- Condition 1 --> |
| SC-2 | All verification criteria VER-1 through VER-N have passed |
| SC-3 | `STATUS-XXX.md` has been updated with run metrics and final status |
| SC-4 | `SKILL-XXX.md` has been updated |
| SC-5 | Reflection artifact has been written |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-6 | Maximum run duration reached before SC-1 through SC-5 are met |
| SC-7 | GATE-1 is denied by the approver |
| SC-8 | An Emergency Stop signal is received in `STATUS-XXX.md` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Primary Artifacts:**
- [ ] <!-- Primary artifact 1 --> written and internally consistent
- [ ] <!-- Primary artifact 2 --> written

**Verification:**
- [ ] All VER-1 through VER-N criteria assessed and outcomes recorded in Reflection
- [ ] Checker validation report produced

**Gates:**
- [ ] Gate outcome recorded in `STATUS-XXX.md` for every gate that fired

**State:**
- [ ] `docs/loops/<!-- category -->/STATUS-XXX.md` updated with all required metrics and final status
- [ ] `docs/loops/<!-- category -->/SKILL-XXX.md` updated

**Reflection:**
- [ ] `docs/<!-- function -->/reflections/REFLECTION-XXX-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus loop-specific sections

---

## Future Improvements

<!-- Three to six specific, scoped improvement ideas. Not vague ("make it better") — named improvements with a mechanism. -->

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — Loop Contracts governing every loop specification
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions

---

## Version History

- **0.1** — <!-- YYYY-MM-DD --> — <!-- Author role --> — Initial Draft. <!-- Brief description of what this version establishes. -->

