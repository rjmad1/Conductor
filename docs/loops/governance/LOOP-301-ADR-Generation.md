---
# PROVENANCE METADATA
Original Path: docs/loops/governance/LOOP-301-ADR-Generation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Governance loop for generating Architectural Decision Records.
Generalized Purpose: Governance loop for generating Architectural Decision Records.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-004 — Planning
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-301 — ADR Generation

**Loop ID:** LOOP-301
**Name:** ADR Generation
**Version:** 1.0
**Status:** Active
**Category:** Governance
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-004 — Planning
**Human Gates:** Hard, Soft
**Owner:** Principal Architecture Function
**Maintainer:** Principal Architecture Function
**Max Run Duration:** 4 hours
**Introduced In:** 2026-06-27

---

## Purpose

LOOP-301 generates a structured, well-reasoned Architecture Decision Record (ADR) for an architectural decision that has been made or is under active consideration. It produces a conformant ADR document in MADR format — including title, status, context, decision statement, consequences, alternatives considered, and rationale — ready for review and publication. This loop does not make architectural decisions; it documents them. The distinction is critical: the decision must already exist (or be stated by a human) before this loop begins.

---

## Problem Statement

Architectural decisions made without documentation decay into folklore. Teams lose the context for why a technology was chosen, what alternatives were considered, and what consequences were accepted. Later engineers cannot evaluate whether a decision still holds, cannot identify when circumstances have changed enough to revisit it, and cannot trace the evolution of the system's structure. Without a repeatable, auditable ADR process, knowledge is locked in the heads of individuals who may leave the organisation, and the architecture becomes impossible to reason about from the repository alone.

---

## Why This Loop Exists

ADR authoring is mentally demanding and frequently deferred. Codifying it as a loop makes the process repeatable, the output verifiable against a declared structure, and the approval traceable. The Maker/Checker pattern ensures that no single agent's interpretation of a decision is accepted without independent review. The Hard Gate before publication ensures that the human architect — who made the decision — confirms the written record is accurate before it becomes authoritative. This loop converts an ad-hoc, often-skipped practice into a governed engineering output.

---

## Scope

**In scope:**
- Generating a single ADR document for a single stated decision
- Classifying the decision type (architectural, governance, or operational)
- Populating all MADR required sections: title, status, context, decision, consequences, alternatives, rationale, and related decisions
- Updating the ADR index (`docs/adr/Decision-Records.md`)
- Notifying relevant module owners when the decision has cross-module impact (Soft Gate)
- Obtaining Principal Architect approval before publication (Hard Gate)

**Out of scope:**
- Making the architectural decision itself
- Evaluating which decision is correct (LOOP-004 — Planning covers option analysis)
- Batch generation of multiple ADRs in a single run
- Retroactive ADR generation for decisions not provided as input
- ADR deprecation or supersession workflows (those are manual governance actions)

**Maximum run duration:** 4 hours. If the loop has not terminated within this window, it halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Decision statement | Signal (human-provided text) | Human architect or LOOP-004 `planning-output.md` | Required |
| Decision type classification hint | Configuration | Human-provided or LOOP-004 outputs | Optional — loop will self-classify if absent |
| LOOP-004 planning output | File (`docs/planning/planning-output-{run-id}.md`) | LOOP-004 — Planning | Optional — required if decision originated from a planning cycle |
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 — Architecture Discovery | Required |
| Existing ADR index | File (`docs/adr/Decision-Records.md`) | Repository | Required — must exist; loop creates it only on first-ever ADR generation |
| Cross-module impact flag | Signal (human-provided boolean) | Human architect | Optional — loop will assess if absent |
| Related ADR identifiers | Configuration | Human-provided list | Optional |

### Input Validation

Before Step 1 begins, the loop must verify:
- The decision statement is non-empty and contains at least one complete sentence.
- `docs/architecture/` exists and is non-empty (confirming LOOP-001 has run).
- The LOOP-001 outputs are no more than 7 days old (checked via `METADATA-001-{run-id}.md` timestamp). If stale, treat as a Soft Gate condition: notify the human and proceed only if no objection is received within 24 hours.
- `docs/adr/` directory exists (create it if absent on first run).
- No concurrent LOOP-301 instance is running (check `STATUS-301.md` for `running` status).

---

## Outputs

All primary outputs are written to `docs/adr/`. Loop state files are written to their canonical paths.

| Artifact | Path | Description |
|----------|------|-------------|
| ADR Document | `docs/adr/ADR-{NNN}-{decision-title-kebab}.md` | Complete MADR-format ADR; consumed by engineers, governance loops, and LOOP-303 Compliance |
| ADR Index Update | `docs/adr/Decision-Records.md` | Updated index entry pointing to the new ADR; consumed by LOOP-302 and LOOP-303 |
| Loop Status | `docs/loops/governance/STATUS-301.md` | Run state, gate outcomes, and most recent run ID |
| Loop Skill | `docs/loops/governance/SKILL-301.md` | Accumulated calibration observations from ADR generation runs |
| Run Metadata | `docs/governance/adr/metadata/METADATA-301-{run-id}.md` | Provenance: run ID, HEAD SHA start/end, upstream dependency run IDs, elapsed seconds, final status |
| Reflection | `docs/governance/adr/reflections/REFLECTION-301-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the authoritative module and service catalog. Consumed to identify affected components and locate related existing ADRs. **Mandatory.** Outputs must be no more than 7 days old.
- **LOOP-004 — Planning:** When the decision emerged from a planning cycle, provides the options analysis and decision context. **Optional.** If absent, the human-provided decision statement must be self-sufficient.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — A human architect or engineering lead explicitly triggers the loop with a decision statement.
2. **LOOP-004 completion** — LOOP-004 produces a planning output that includes a decided option requiring documentation.
3. **Repository event** — A pull request is merged that introduces a significant new pattern, framework, or architectural boundary without a corresponding ADR.

Trigger source, timestamp, and decision statement must be recorded in `STATUS-301.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | A non-empty decision statement is provided | Validate input field; halt with `precondition_failed` if empty |
| PRE-2 | `docs/architecture/` exists and is non-empty | Directory listing check |
| PRE-3 | `docs/adr/` exists or can be created | Directory write probe |
| PRE-4 | `docs/adr/Decision-Records.md` exists or this is a declared first-ADR run | File existence check |
| PRE-5 | No concurrent LOOP-301 instance is running | Read `STATUS-301.md`; if `current_status: running`, halt with `skipped_concurrent` |
| PRE-6 | Git is available and the repository is clean enough to stage new files | `git status` exits cleanly |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | `docs/architecture/`, `docs/adr/`, source files for context | Filesystem permissions of executing agent | Scoped to this repository only | N/A (read-only) | Yes |
| `docs/adr/` directory | Write | Single new ADR file + updated `Decision-Records.md` | Same as executing agent | Confined to `docs/adr/` | `git checkout docs/adr/` restores prior state | Yes — re-running with identical inputs produces the same ADR content |
| `docs/loops/governance/STATUS-301.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/STATUS-301.md` | Yes |
| `docs/loops/governance/SKILL-301.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/SKILL-301.md` | Yes |
| `docs/governance/adr/` | Write | Metadata and Reflection artifacts | Same as executing agent | Confined to this directory | `git checkout docs/governance/adr/` | Yes |

This loop makes no writes to external systems, APIs, databases, or deployment targets.

---

## Required Context

Before beginning, the executing agent must have loaded:
- `docs/loops/shared/LOOP-STANDARD.md` — for understanding the ADR governance structure
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — for conformance context
- `docs/architecture/module-catalog.md` — to identify affected components
- `docs/architecture/architecture-overview.md` — for architectural baseline
- `docs/adr/Decision-Records.md` — to determine the next ADR number and find related decisions
- The decision statement (human-provided or from LOOP-004 outputs)
- Any LOOP-004 planning output file if the trigger was a planning cycle

---

## Agents

| Agent ID | Role | Artifact | Responsibilities | Tools | Gate Assignment |
|----------|------|----------|-----------------|-------|----------------|
| ADR-ASSEMBLER | Orchestrator | None | Loads all context, validates inputs, classifies decision type, routes to writer | Read, directory listing | Pre-step validation |
| CONTEXT-AGENT | Maker (context) | Context summary | Loads LOOP-001 outputs; produces a structured context summary identifying affected components and related ADRs | Read, Grep | Steps 1–2 |
| ADR-WRITER | Maker | ADR document draft | Drafts the complete MADR-format ADR including all required sections | Read, Write | Step 3 |
| ADR-CHECKER | Checker | Checker finding | Independently reviews ADR draft against VER criteria; the same agent instance that produced the draft may not serve as Checker | Read | Step 4 |
| INDEX-WRITER | Maker | Updated ADR index | Updates `Decision-Records.md` with new entry after GATE-1 approval | Read, Write | Post-GATE-1 |
| STATUS-WRITER | Maker | STATUS-301.md, SKILL-301.md, Reflection | Updates loop state files and produces Reflection | Write | Final step |

**Maker/Checker constraint:** ADR-WRITER and ADR-CHECKER must be different agent instances. The same invocation of the same model may not produce the draft and then review it in the same execution context without a stateless reset.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

**Step 1 — Context Load (ADR-ASSEMBLER)**
- Inputs: Decision statement, LOOP-004 planning output (if available), LOOP-001 architecture outputs
- Outputs: Validated decision statement, decision type classification, list of affected components, list of related ADR numbers
- Classify decision type: `architectural` (module boundary, technology choice, pattern adoption), `governance` (process, standard, policy), `operational` (deployment, configuration, monitoring)
- Record trigger and start timestamp in STATUS-301.md (`current_status: running`)
- Check for Emergency Stop signal at step start

**Step 2 — Architecture Context Load (CONTEXT-AGENT)**
- Inputs: `docs/architecture/module-catalog.md`, `docs/architecture/architecture-overview.md`, related ADR files
- Outputs: Structured context summary identifying: affected modules, current patterns in the affected area, existing related decisions with their status, technical debt items relevant to the decision
- Check for Emergency Stop signal at step start

**Step 3 — ADR Draft (ADR-WRITER — Maker)**
- Inputs: Decision statement, decision type, context summary, related ADR numbers, LOOP-004 alternatives analysis (if available)
- Outputs: Draft ADR document at `docs/adr/ADR-{NNN}-{decision-title-kebab}.md` with status `Proposed`
- Required sections in draft:
  - **Title:** "Use [X] for [concern]" format
  - **Status:** Proposed
  - **Date:** Run date (ISO 8601)
  - **Context:** Why this decision was needed; what problem it solves; what constraints existed
  - **Decision:** The single, unambiguous choice made
  - **Consequences:** Positive, negative, and neutral consequences of this decision
  - **Alternatives Considered:** At minimum two alternatives with rationale for rejection
  - **Rationale:** Why this option was selected over the alternatives
  - **Related Decisions:** Links to related ADRs
- Determine next ADR number by reading Decision-Records.md
- Write draft to `docs/adr/` (status remains `Proposed` until GATE-1 approval)
- Check for Emergency Stop signal at step start

**Step 4 — ADR Review (ADR-CHECKER — Checker)**
- Inputs: Draft ADR document, context summary, decision statement
- Outputs: Checker finding document (`docs/governance/adr/checker-finding-{run-id}.md`)
- Independently verify: decision statement is unambiguous; rationale connects to stated context and does not introduce new context; consequences are realistic and specific (not generic); each alternative was genuinely considered with specific rejection rationale (not strawmanned); all required MADR sections are populated
- Record pass/fail per VER criterion
- Check for Emergency Stop signal at step start

**Step 5 — Cross-Module Notification (SOFT GATE — GATE-2)**
- Condition: Decision affects more than one module (as identified in Step 2 context summary) OR human-provided cross-module flag is true
- Action: Notify relevant module owners via declared notification channel
- Timeout: 24 hours
- Auto-proceed if no objection received within 24 hours
- Record gate outcome in STATUS-301.md

**Step 6 — Principal Architect Approval (HARD GATE — GATE-1)**
- Artifact under review: Draft ADR document + Checker finding
- Approver: Principal Architect or Architecture Owner
- Human must confirm: decision statement accurately represents the decision made; rationale is correct; consequences are accepted
- On approval: ADR status changes from `Proposed` to `Accepted`; INDEX-WRITER updates Decision-Records.md
- On denial: ADR-WRITER revises per feedback and returns to Step 4; STATUS-301.md updated with denial reason
- Record gate outcome in STATUS-301.md with reviewer identity, decision, rationale, and timestamp

**Step 7 — Index Update and Closure (INDEX-WRITER + STATUS-WRITER)**
- Inputs: Approved ADR, current Decision-Records.md
- Outputs: Updated Decision-Records.md with new entry; STATUS-301.md updated to `completed`; SKILL-301.md updated; METADATA-301-{run-id}.md written; REFLECTION-301-{run-id}.md produced
- Check for Emergency Stop signal at step start

---


**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | ADR document exists at declared path and is non-empty | File existence and size check |
| VER-2 | ADR document contains all required MADR sections: Title, Status, Date, Context, Decision, Consequences, Alternatives Considered, Rationale, Related Decisions | Parse Markdown headings; confirm each required heading is present |
| VER-3 | ADR status is `Accepted` (not `Proposed`) after GATE-1 approval | Read Status field from ADR frontmatter or heading |
| VER-4 | Decision-Records.md contains an entry for this ADR with the correct number, title, and link | Read index file; confirm new row present |
| VER-5 | No repository source files were modified by this loop | `git diff --name-only` confirms only `docs/adr/` and `docs/loops/governance/` and `docs/governance/adr/` files changed |
| VER-6 | STATUS-301.md was updated with this run's ID and a timestamp within the last 10 minutes | Read STATUS-301.md; check `current_run_id` and `last_updated` fields |
| VER-7 | Reflection artifact exists at declared path and is non-empty | File existence and size check |
| VER-8 | Checker finding document exists and records a pass result (or a pass-with-notes result) | Read checker finding; confirm final verdict field |
| VER-9 | GATE-1 approval is recorded in STATUS-301.md with reviewer identity, decision, and timestamp | Read `gate_outcomes` field in STATUS-301.md |
| VER-10 | ADR number is sequential with no gaps in the existing index | Read Decision-Records.md; confirm ADR-NNN follows the highest existing number |

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

Every run — including failed and stopped runs — must produce a Reflection artifact at `docs/governance/adr/reflections/REFLECTION-301-{run-id}.md` before the run is marked closed. The Reflection is produced by STATUS-WRITER.

**Required sections:**
1. Run Summary (Loop ID, Run ID, date, trigger, final status)
2. What Was Attempted (decision being documented)
3. What Happened (actual execution including deviations)
4. Verification Results (outcome for each VER criterion)
5. Gate Outcomes (GATE-1 and GATE-2: approved/denied/not reached/auto-proceeded)
6. Failures and Anomalies (any step that did not complete as specified)
7. Risk Observations (any declared risk that materialized; any new risk observed)
8. Metrics (values for all metrics in the Metrics section)
9. Improvement Candidates (observations that would reduce risk or improve ADR quality)
10. Decision Log (significant decisions made during the run not fully specified by the Workflow)

A run may not be marked `completed` without a Reflection. A failed run must include root cause analysis in the Failures and Anomalies section.

---

## Human Approval Gates

### GATE-1 — Principal Architect Approval (Hard Gate)

- **Gate type:** Hard
- **Position in Workflow:** After Step 4 (ADR-CHECKER review) and Step 5 (cross-module notification), before ADR is published
- **Artifact under review:** Draft ADR document + ADR-CHECKER finding document
- **Approver:** Principal Architect or Architecture Owner
- **What the approver must confirm:**
  - The decision statement accurately represents the decision that was made
  - The rationale is correct and complete
  - The consequences are accepted (positive, negative, and neutral)
  - The alternatives were genuinely considered
- **If denied:** ADR-WRITER revises the ADR addressing the denial reason; loop returns to Step 4; STATUS-301.md records denial with reason
- **No timeout:** A Hard Gate has no timeout. The loop remains blocked until explicit approval or denial is recorded.
- **Audit trail:** Approval is written to STATUS-301.md `gate_outcomes` field with reviewer identity, role, decision (approved/denied), rationale, and ISO 8601 timestamp.

### GATE-2 — Cross-Module Impact Notification (Soft Gate)

- **Gate type:** Soft
- **Position in Workflow:** Step 5, after ADR-CHECKER review, before GATE-1
- **Trigger condition:** Decision classified as cross-module impact (affects more than one module, or human flag is set)
- **Notification channel:** Engineering team channel (declared in repository configuration at `.loop-config.yml` key `notification_channel`)
- **Notification content:** ADR title, decision summary (one sentence), affected modules, link to draft ADR document
- **Timeout:** 24 hours from notification timestamp
- **Auto-proceed action:** If no objection received within 24 hours, loop proceeds to GATE-1 automatically; auto-proceed is recorded in STATUS-301.md
- **If objection received:** Loop halts; human decision required on whether to revise the ADR, widen the review, or proceed
- **Audit trail:** Gate outcome (notified timestamp, auto-proceed timestamp or objection received) written to STATUS-301.md `gate_outcomes`

---

## Failure Recovery

### FR-1 — Decision Statement Ambiguous or Insufficient

- **Detection:** ADR-CHECKER returns a fail finding on the Decision or Rationale sections; or ADR-WRITER cannot produce a non-empty decision statement from the input
- **Immediate action:** Halt at Step 4; write partial Reflection with status `stopped`
- **Recovery path:** Human provides a revised, more complete decision statement; loop re-runs from Step 1
- **Rollback scope:** Delete draft ADR file if written; restore Decision-Records.md to pre-run state

### FR-2 — LOOP-001 Outputs Stale or Missing

- **Detection:** PRE-2 fails (directory missing) or LOOP-001 outputs are older than 7 days
- **Immediate action:** Halt at precondition check; record `precondition_failed` in STATUS-301.md
- **Recovery path:** Trigger LOOP-001 to refresh outputs; re-run LOOP-301 once fresh outputs are available
- **Rollback scope:** No outputs written; no rollback needed

### FR-3 — GATE-1 Denied

- **Detection:** Principal Architect records a denial with revision instructions
- **Immediate action:** ADR status remains `Proposed`; denial reason is recorded in STATUS-301.md
- **Recovery path:** ADR-WRITER revises per feedback; Checker re-reviews; GATE-1 re-opened
- **Rollback scope:** Draft ADR may be kept in `Proposed` state pending revision; no index update is performed until GATE-1 is approved

### FR-4 — Emergency Stop Received

- **Detection:** STATUS-301.md `emergency_stopped` field set to `true` by any human principal
- **Immediate action:** Current step terminates immediately; rollback action for the current step is applied; partial Reflection is produced
- **Rollback scope:** If ADR file was written, it remains in `Proposed` status (not published); Decision-Records.md is restored to pre-run state
- **Escalation:** Partial Reflection is flagged for human review before any re-run

### FR-5 — ADR Number Collision

- **Detection:** Computed ADR number already exists in `docs/adr/` directory
- **Immediate action:** Halt at Step 3; record `adr_number_conflict` in STATUS-301.md
- **Recovery path:** Human resolves the collision (confirm whether existing ADR should be renumbered or new ADR should take next available number); loop re-runs from Step 3

---

## Metrics

**Standard metrics (required for all loops):**

| Metric | Definition |
|--------|-----------|
| `run.duration_seconds` | Wall-clock time from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of workflow steps completed before termination |
| `run.steps_total` | 7 (total declared steps) |
| `gate.hard.count` | Number of Hard Gates reached (maximum 1 in this loop) |
| `gate.hard.approved` | Number of Hard Gates approved |
| `gate.hard.denied` | Number of Hard Gates denied |
| `gate.soft.count` | Number of Soft Gates reached (maximum 1 in this loop) |
| `gate.soft.auto_proceeded` | Number of Soft Gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER criteria that passed |
| `verification.level1.fail` | Count of VER criteria that failed |
| `reflection.produced` | Boolean — was a Reflection artifact produced? |

**Loop-specific metrics:**

| Metric | Definition |
|--------|-----------|
| `adr.decision_type` | `architectural` \| `governance` \| `operational` |
| `adr.alternatives_count` | Number of alternatives documented in the ADR |
| `adr.modules_affected` | Number of modules identified as affected by the decision |
| `adr.checker_revision_rounds` | Number of times the draft returned to ADR-WRITER after Checker review |
| `adr.gate1_denial_count` | Number of times GATE-1 was denied before final approval |

---

## Risks

### RISK-1 — Scope Creep
- **Description:** ADR-WRITER expands scope to document multiple decisions, revise existing ADRs, or include implementation guidance that belongs in code comments or documentation.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Decision statement is compound or ambiguous; ADR-WRITER interprets it expansively.
- **Control:** Scope section explicitly limits the loop to a single decision per run. ADR-CHECKER verifies the ADR does not contain multiple distinct decisions.
- **Detection:** ADR-CHECKER finding includes a scope check; VER-2 verifies section structure.
- **Response:** ADR-WRITER is instructed to split compound decisions; loop re-runs for each distinct decision.

### RISK-2 — Architectural Drift
- **Description:** ADR documents a decision that contradicts existing accepted ADRs or the current architecture baseline without acknowledging the conflict.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** LOOP-001 outputs are stale; context summary misses a related ADR.
- **Control:** Context-AGENT explicitly loads existing ADRs and architecture baseline. ADR-WRITER must populate the Related Decisions section. ADR-CHECKER verifies related decisions are cited.
- **Detection:** GATE-1 human review catches contradictions; ADR-CHECKER checks Related Decisions section.
- **Response:** ADR is revised to acknowledge and supersede conflicting decisions, or GATE-1 is denied pending clarification.

### RISK-3 — Hidden Dependencies
- **Description:** The ADR references a component or pattern that is not present in the architecture baseline, creating a false dependency.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Decision anticipates a future state that has not yet been implemented.
- **Control:** CONTEXT-AGENT grounds the ADR in actual LOOP-001 outputs. Speculative future state must be explicitly labeled as such in the Consequences section.
- **Detection:** ADR-CHECKER verifies that all referenced components exist in the module catalog.
- **Response:** ADR-WRITER revises to separate current-state and future-state consequences.

### RISK-4 — Tenant Isolation Breach
- **Description:** Not Applicable. This loop operates on governance artifacts (ADR documents and the ADR index) only. It does not read or write tenant-scoped runtime data, databases, or application state of any kind.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption
- **Description:** The ADR file or the Decision-Records.md index is overwritten incorrectly, losing prior ADR entries.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Index-WRITER appends incorrectly or overwrites the full file with only the new entry.
- **Control:** All writes are to git-tracked files; prior state is recoverable via `git checkout`. Decision-Records.md is read before write to confirm existing entries are preserved.
- **Detection:** VER-4 verifies index contains the new entry; human review at GATE-1 checks the index state.
- **Response:** `git checkout docs/adr/Decision-Records.md` restores prior state; loop re-runs Step 7.

### RISK-6 — Non-Idempotent External Write
- **Description:** Not Applicable. All writes are to git-tracked files within the repository. Re-running with identical inputs produces equivalent file content. No writes are made to external APIs, databases, or services.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation
- **Description:** The loop inadvertently includes sensitive information (credentials, personal data, internal IP addresses) in the ADR document.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Decision statement provided by human includes sensitive context.
- **Control:** ADR-CHECKER includes an explicit check for credential patterns, PII, and internal-only references. GATE-1 human review is the final safety check before publication.
- **Detection:** ADR-CHECKER finding includes a sensitive content scan. GATE-1 approver reviews for sensitive content.
- **Response:** ADR-WRITER redacts sensitive content; loop re-runs from Step 4.

### RISK-8 — Runaway Execution
- **Description:** The ADR-WRITER or ADR-CHECKER enters a revision loop that does not converge, repeatedly producing and rejecting drafts without halting.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Decision statement is too ambiguous for automated resolution; Checker and Writer disagree on fundamental interpretation.
- **Control:** Maximum run duration of 4 hours enforced. After 3 revision cycles without convergence, loop halts with status `stopped` and escalates to human.
- **Detection:** `adr.checker_revision_rounds` metric; run duration monitoring.
- **Response:** Loop halts; human resolves the interpretation disagreement; loop re-runs with clarified decision statement.

---

## Stop Conditions

The loop terminates without failure (status `stopped`) under the following conditions:

| Condition | Cleanup Performed | State Left Behind |
|-----------|-------------------|-------------------|
| Maximum run duration (4 hours) exceeded | Partial Reflection produced; STATUS-301.md updated to `stopped` | Draft ADR may exist in `docs/adr/` with status `Proposed`; must be reviewed by human before re-run |
| Emergency Stop received from any human principal | Current step's rollback action applied; partial Reflection produced | STATUS-301.md records step at which stop was received and current HEAD SHA |
| Revision loop exceeds 3 cycles without convergence | Partial Reflection produced; STATUS-301.md updated to `stopped` with reason `revision_loop_exceeded` | Draft ADR exists with status `Proposed`; decision statement must be clarified before re-run |
| Concurrent run detected at PRE-5 | No cleanup needed (loop never started) | STATUS-301.md records `skipped_concurrent` entry |
| LOOP-001 outputs missing or stale (PRE-2 fails) | No cleanup needed | STATUS-301.md records `precondition_failed` with reason |

---

## Deliverables

A run is not closed as `completed` until all of the following are confirmed present:

- [ ] `docs/adr/ADR-{NNN}-{decision-title-kebab}.md` exists and status is `Accepted`
- [ ] `docs/adr/Decision-Records.md` contains a new entry for this ADR
- [ ] `docs/loops/governance/STATUS-301.md` updated with this run's ID, outcome, and gate outcomes
- [ ] `docs/loops/governance/SKILL-301.md` updated with at least one new observation from this run
- [ ] `docs/governance/adr/metadata/METADATA-301-{run-id}.md` produced with all required provenance fields
- [ ] `docs/governance/adr/reflections/REFLECTION-301-{run-id}.md` produced with all 10 required sections
- [ ] `docs/governance/adr/checker-finding-{run-id}.md` exists and records a pass verdict
- [ ] GATE-1 approval recorded in STATUS-301.md with reviewer identity, role, decision, rationale, and timestamp
- [ ] All VER-1 through VER-10 criteria confirmed passed

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Batch ADR generation:** Allow a single run to process a list of related decisions from a planning cycle, producing multiple coordinated ADRs with consistent cross-references. Requires a MAJOR version bump (change to input contract).
- **ADR supersession workflow:** Extend the loop to handle supersession of existing ADRs when a new decision replaces an old one, updating the old ADR's status to `Superseded` automatically. Currently a manual governance action.
- **Automated related-ADR discovery:** Use semantic similarity on the decision statement to automatically identify related ADRs beyond the human-provided list. Currently CONTEXT-AGENT only does keyword-based search.
- **MADR schema validation:** Add a machine-readable schema validator for MADR format so VER-2 can be Level 1 (automated) rather than requiring manual inspection. Requires declaring a MADR schema file in the repository.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — the canonical loop engineering standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — the loop contract specification
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — mandatory upstream dependency
- `docs/loops/core/LOOP-004-Planning.md` — optional upstream dependency
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions
- MADR (Markdown Any Decision Record) format: https://adr.github.io/madr/ — the ADR format this loop targets

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version.

