---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/LOOP-104-Documentation.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Standard loop for generating and updating documentation.
Generalized Purpose: Standard loop for generating and updating documentation.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-104 — Documentation

**Loop ID:** LOOP-104
**Name:** Documentation
**Version:** 1.0
**Status:** Active
**Category:** Engineering
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---

## Purpose

LOOP-104 produces or updates documentation for a declared documentation gap, covering README files, API documentation, inline code documentation, ADR write-ups, architecture documents, and runbooks. The loop operates exclusively on documentation artifacts: it does not modify source code, test files, or configuration. Every documentation artifact it produces is grounded in the current codebase state as discovered by LOOP-001 and assembled by LOOP-002 — it documents what exists, not what is intended or planned.

---

## Problem Statement

Documentation drifts from implementation continuously as code evolves faster than the text describing it. Undocumented APIs force engineers to reverse-engineer contracts from source. Missing module READMEs mean every new contributor to a module must re-derive the same contextual understanding from scratch. Inaccurate architecture documents mislead more than absent ones. Without a governed documentation process, produced documentation cannot be trusted to reflect the current codebase state, and the effort invested in writing it is partially wasted.

---

## Why This Loop Exists

Documentation quality is a verifiability concern: documentation cannot be trusted unless it has been independently checked against the current code. Codifying documentation as a loop ensures: repeatability (every documentation gap is addressed through the same structured process, producing consistent artifact structure), verifiability (every documentation claim is independently verified against the codebase by a Checker before commit), auditability (documentation artifacts carry their producing run ID and the HEAD SHA at the time of production, enabling staleness detection), and safety (no documentation is committed until a human has confirmed its accuracy — the GATE-1 review is the primary accuracy gate).

---

## Scope

**In scope:**
- README files for modules, services, and repositories
- API documentation: endpoint descriptions, parameters, response schemas, error codes
- Inline code documentation: method docstrings, class-level documentation, package-level descriptions
- Architecture documents: component descriptions, integration diagrams (as textual Mermaid sources), decision summaries
- ADR summaries (for ADR-specific generation, prefer LOOP-301 which has specialised ADR governance)
- Runbooks: operational procedures for services with no existing runbook
- Updating `docs/architecture/technical-debt.md` to mark resolved documentation gaps

**Out of scope:**
- Source code modifications to add missing method signatures, docstring anchors, or any other code change (those are separate tasks for LOOP-101 or LOOP-102)
- Test file modifications
- Configuration file changes
- ADR drafting for new architectural decisions (use LOOP-301 for new ADRs)
- Marketing or promotional content
- Documentation for behaviour that does not exist in the current codebase

**Maximum run duration:** 6 hours total. If this duration is exceeded, the loop halts and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Documentation gap task record | File (`docs/tasks/task-catalog.md` entry with `primary_category: DOC`) | LOOP-003 — Task Discovery | Required |
| Documentation gap definition | Field within task record: type, target component, audience, and gap description | Task record | Required |
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 — Architecture Discovery | Required — must be no more than 7 days old |
| LOOP-002 context package | Directory (`docs/context/`) | LOOP-002 — Context Assembly | Required — produced during this run |
| Existing documentation for the target component | Repository State | Filesystem (existing README, existing docstrings, etc.) | Required — read to avoid contradiction |
| Accepted ADRs relevant to the documented component | Directory (`docs/adr/`) | Repository | Optional — include when documenting architectural decisions |
| Engineering Loop configuration | File (`.loop-104.yml` at repo root) | Repository | Optional |

### Input Validation

Before any step begins, the loop must verify:
- The task record exists, has `primary_category: DOC`, and declares the documentation type, target component, and audience.
- LOOP-001 outputs exist and are within 7 days.
- No other LOOP-104 instance is running for the same task ID.
- HEAD SHA recorded at validation time.

---

## Outputs

Documentation artifacts are written to their natural repository locations (module README files to their module directory, API docs to declared API doc locations, etc.). Metadata and loop-management artifacts are written to `docs/engineering/documentation/`. The loop also updates `docs/architecture/technical-debt.md` to mark the resolved documentation gap.

| Artifact | Path | Description |
|----------|------|-------------|
| Documentation Artifact(s) | Natural repository location for the type (e.g., `platform/customer/README.md`, `docs/api/customer-api.md`, inline in source file) | The produced documentation; verified against current codebase before commit |
| Gap Classification | `docs/engineering/documentation/gap-classification.md` | Type, audience, affected component, and gap description formalised from the task record |
| Documentation Checker Report | `docs/engineering/documentation/doc-checker-report.md` | DOC-CHECKER's independent verification that documentation matches actual code behaviour |
| Technical Debt Update | `docs/architecture/technical-debt.md` | Updated to mark this documentation gap as resolved; produced after GATE-1 approval |
| Engineering Summary | `docs/engineering/documentation/engineering-summary.md` | Complete run summary for human review |
| Loop Status | `docs/loops/engineering/STATUS-104.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/engineering/SKILL-104.md` | Calibration observations accumulated across runs |
| Run Metadata | `docs/engineering/documentation/metadata/METADATA-104-{run-id}.md` | Provenance: task ID, LOOP-001 and LOOP-002 run IDs, HEAD SHA at start and end, elapsed duration, outcome |
| Reflection | `docs/engineering/documentation/reflections/REFLECTION-104-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Module boundaries, component descriptions, and existing architecture knowledge base. Required — outputs must exist and be no more than 7 days old. This loop does not invoke LOOP-001 mid-run; it consumes its most recent completed outputs.
- **LOOP-002 — Context Assembly:** Context package for the documented component (source files, existing documentation, related ADRs). Required — invoked during this run.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer selects a `DOC`-classified task from the LOOP-003 backlog and triggers LOOP-104.
2. **Automated task dispatch** — AEOS task dispatcher routes a `DOC`-classified task per SPEC-010 routing rules.
3. **Governance signal** — A periodic documentation audit identifies modules with missing or stale documentation and creates DOC tasks for each.
4. **Post-implementation trigger** — After a LOOP-101 or LOOP-102 run completes, the affected module's documentation is flagged as potentially stale and a DOC task is created.

The trigger source, task ID, and current HEAD SHA must be recorded in `STATUS-104.md` at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | A `DOC`-classified task exists with declared documentation type, target component, and audience | Read task catalog; assert `primary_category: DOC` with non-empty type, target, and audience fields |
| PRE-2 | LOOP-001 outputs exist and are no more than 7 days old | Read `STATUS-001.md`; assert timestamp within 7 days |
| PRE-3 | No other LOOP-104 instance is running for the same task ID | Read `STATUS-104.md`; assert not `running` for this task ID |
| PRE-4 | Write access to the target documentation location | Probe write; remove on success |
| PRE-5 | Write access to `docs/engineering/documentation/` | Probe write; remove on success |
| PRE-6 | The target component exists in the repository (cannot document what does not exist) | Assert at least one source file exists for the declared target component |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem (source) | Read | Source files for the documented component | Filesystem permissions | Scoped to declared component only; no writes | N/A (read only) | Yes |
| Target documentation location | Write | The specific documentation file(s) declared in the task record | Same as executing agent | Scoped to the declared documentation paths only | `git checkout <file path>` restores prior state; prior content preserved in git history | Yes — re-producing documentation from the same codebase state produces equivalent content |
| `docs/architecture/technical-debt.md` | Write | Single file — only the entry corresponding to the resolved gap is modified | Same as executing agent | Single file, single entry | `git checkout docs/architecture/technical-debt.md` | Yes |
| `docs/engineering/documentation/` | Write | All loop metadata files | Same as executing agent | Confined to this directory | `git checkout docs/engineering/documentation/` | Yes |
| `docs/loops/engineering/STATUS-104.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/STATUS-104.md` | Yes |
| `docs/loops/engineering/SKILL-104.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/engineering/SKILL-104.md` | Yes |

This loop does not write to any external system outside the repository. It does not modify source code, test files, or configuration files.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/engineering/LOOP-104-Documentation.md` — this document
3. `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared context
4. `docs/loops/engineering/STATUS-104.md` — prior run state
5. `docs/loops/core/SKILL-001.md` — repository conventions, documentation format preferences
6. `docs/architecture/module-catalog.md` — module boundaries and existing documentation references
7. `docs/architecture/technical-debt.md` — existing debt entries including documentation gaps
8. The selected task record from `docs/tasks/task-catalog.md`
9. The current content of the target documentation location (if a file exists there)

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `GAP-AGENT` | Maker | Step 1: loads and formalises the documentation gap definition; classifies type and audience | Filesystem read, task record read | None |
| `CONTEXT-AGENT` | Maker | Step 2: invokes LOOP-002 scoped to the documented component | LOOP-002 invocation | None |
| `DOC-WRITER` | Maker | Step 3: produces draft documentation grounded in the current codebase; applies type-specific structure | Filesystem read (source), documentation writing | Reports to GATE-1 |
| `DOC-CHECKER` | Checker | Step 4: independently verifies documentation accuracy against actual source code; confirms no source code was modified | Filesystem read, independent source verification | Independent of DOC-WRITER; finding reviewed at GATE-1 |
| `DOC-WRITER` | Maker | Step 6: commits approved documentation to the repository | Filesystem write (documentation files only) | None — post GATE-1 |
| `STATUS-WRITER` | Maker | Steps 7–8: updates STATUS-104.md, SKILL-104.md, technical-debt.md; produces Engineering Summary, Metadata, Reflection | Filesystem write | None |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: confirms documentation accuracy and approves for commit | Human judgment | Sole authority for gate decisions |

`DOC-WRITER` and `DOC-CHECKER` must be separate agent instances. Note that `DOC-WRITER` appears in two non-overlapping steps (draft production in Step 3, commit in Step 6) — the Maker/Checker separation applies to the draft artifact reviewed at GATE-1.

---

## Workflow

### Step 1 — Gap Classification

**Agent:** `GAP-AGENT`
**Inputs:** Task record from LOOP-003
**Outputs:** `docs/engineering/documentation/gap-classification.md`

Load and formalise the documentation gap:
- **Documentation type:** One of — `README` (module or service overview), `API-doc` (endpoint or interface documentation), `inline-doc` (method or class docstrings), `architecture-doc` (component or system level), `ADR-summary` (summary of an existing ADR), `runbook` (operational procedure).
- **Target component:** The specific module, service, class, method, or ADR to be documented.
- **Target audience:** One of — `developer` (engineers working in the module), `operator` (engineers running the service), `architect` (engineers designing across the system), `end-user` (consumers of a public API).
- **Gap description:** What is currently missing or inaccurate.
- **Natural documentation location:** The file path where the documentation will be placed in the repository.

If the gap type is `ADR-summary` for a new architectural decision (not an existing ADR), flag for GATE-2 and recommend LOOP-301 instead — this loop documents existing ADRs, not new ones.

Write `gap-classification.md`. Update `STATUS-104.md` to `running`.

---

### Step 2 — Context Assembly

**Agent:** `CONTEXT-AGENT`
**Inputs:** Gap classification from Step 1, LOOP-001 architecture outputs
**Outputs:** Context package at `docs/context/` (produced by LOOP-002)

Invoke LOOP-002 parameterised with:
- The target component from Step 1
- Tier 1 Required: source files for the target component; existing documentation for the component (including any partial README, docstrings, or architecture notes)
- Tier 2 Required: dependency context (what the component uses and what uses it)
- Tier 3 Optional: ADRs relevant to the component; module-level architecture documents referencing this component

For `API-doc` type: additionally include any OpenAPI specs, interface definitions, or contract test files.
For `runbook` type: additionally include deployment configuration, health check endpoints, and alert definitions.

Validate context package HEAD SHA against current repository HEAD SHA. If mismatch, trigger GATE-1.

---

### Step 3 — Documentation Drafting

**Agent:** `DOC-WRITER` (Maker)
**Inputs:** Gap classification from Step 1, context package from Step 2
**Outputs:** Draft documentation at the declared natural documentation location

Produce draft documentation grounded in the current codebase. Apply the structure appropriate for the declared type:

**README** structure: (1) Module/Service purpose — one paragraph, what it does and why it exists. (2) Key responsibilities — bulleted list of what this module owns. (3) Usage — how another engineer uses this module, including import/dependency declaration. (4) Configuration — required and optional configuration keys with their sources. (5) Dependencies — what this module depends on and what depends on it. (6) Testing — how to run the tests for this module. (7) Known limitations — significant constraints or known gaps.

**API-doc** structure: (1) Endpoint/Method signature. (2) Purpose — what the endpoint does. (3) Parameters — each parameter with type, required/optional, and description. (4) Response — response schema, content type, and examples for success and error cases. (5) Error codes — each possible error code with its meaning and common causes. (6) Authentication requirements.

**Inline-doc** structure: (1) Method purpose — one sentence. (2) Parameters — `@param` or equivalent per parameter. (3) Return value — what is returned and its type. (4) Side effects — any mutation of external state or notable exception paths. (5) Example usage — one example where helpful.

**Architecture-doc** structure: (1) Component purpose. (2) Component interfaces — what it exposes and what it consumes. (3) Key design decisions — with reference to accepted ADRs where applicable. (4) Mermaid diagram source for the component's relationships where applicable.

**Runbook** structure: (1) Service overview. (2) Start/stop procedures. (3) Health check verification. (4) Common failure scenarios with detection and remediation steps. (5) Escalation path.

Every documentation artifact must include a header noting the HEAD SHA at which it was produced and the run ID that produced it.

---

### Step 4 — Documentation Accuracy Review (Checker)

**Agent:** `DOC-CHECKER` (Checker)
**Inputs:** Draft documentation from Step 3, context package (source files)
**Outputs:** `docs/engineering/documentation/doc-checker-report.md`

`DOC-CHECKER` independently verifies the draft documentation against the actual source code:

1. **Accuracy:** Every factual claim in the documentation is consistent with the source code. For each claim, the Checker must cite the specific file path and line range that supports it. Claims that cannot be supported by specific evidence are flagged `UNSUPPORTED`.
2. **Completeness:** For API docs — all parameters declared in the source are documented; no documented parameter is absent from the source. For inline docs — the method signature described matches the actual method signature.
3. **No invented APIs:** The documentation does not describe methods, endpoints, or configuration keys that do not exist in the current codebase.
4. **Terminology consistency:** Terms used are consistent with LOOP-001 architecture artifacts (module names, service names, event names match the architecture catalogs).
5. **No source code modifications:** The Checker verifies that no source file was modified as part of the drafting process (`git diff` contains only documentation file changes).
6. **Audience appropriateness:** The depth and vocabulary are appropriate for the declared audience.

Write `doc-checker-report.md` with: per-criterion findings, unsupported claim list (if any), and overall finding (`accepted` | `rejected`). If `rejected`, enumerate every issue. DOC-WRITER performs one retry with corrections on `rejected`; if second attempt also produces `rejected`, GATE-1 fires unconditionally.

---

### [GATE-2 — Soft Gate: New ADR or Multiple Documentation Type Options]

Fires when: (a) a gap classified as `ADR-summary` is for a new architectural decision rather than an existing ADR, or (b) the documentation type is ambiguous (e.g., the gap could be addressed by a README update or an inline-doc pass, and the choice affects scope significantly). The loop notifies the reviewer and awaits clarification or auto-proceeds with the default recommendation. See `## Human Approval Gates` — GATE-2.

---

### Step 5 — [GATE-1 — Hard Gate: Documentation Accuracy Approval Before Commit]

The loop halts. The draft documentation is not committed until human approval is recorded. The human reviewer confirms accuracy and completeness. See `## Human Approval Gates` — GATE-1.

---

### Step 6 — Documentation Commit

**Agent:** `DOC-WRITER`
**Inputs:** Approved draft documentation, GATE-1 approval record
**Outputs:** Committed documentation file(s) at natural repository location

Write the approved documentation to the declared natural documentation location. For inline documentation, this means writing into the source file's docstring fields only — no functional code changes. This is the only step in LOOP-104 that writes to the repository. Confirm that `git diff` shows only documentation changes before proceeding.

---

### Step 7 — Technical Debt Update

**Agent:** `STATUS-WRITER`
**Inputs:** Gap classification, GATE-1 approval, committed documentation path
**Outputs:** Updated `docs/architecture/technical-debt.md`

Find the documentation gap entry in `technical-debt.md` corresponding to this task. Update its status to `resolved`, add the resolving run ID and date. If no matching entry exists, add a new `resolved` entry so future runs can identify that this gap was addressed.

---

### Step 8 — Status, Skill, and Reflection

**Agent:** `STATUS-WRITER`
**Inputs:** All run artifacts, all gate outcomes
**Outputs:** Updated `STATUS-104.md`, `SKILL-104.md`, `engineering-summary.md`, `METADATA-104-{run-id}.md`, `REFLECTION-104-{run-id}.md`

Record all metrics. Update SKILL with: effective documentation patterns for this technology stack; claim types that required Checker retry; terminology divergences identified. Produce Reflection before marking run closed.

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | `gap-classification.md` exists and declares all required fields (type, target component, audience, gap description, natural location) | Read file; assert all fields non-empty |
| VER-2 | `doc-checker-report.md` exists with overall finding `accepted` | Read file; assert `overall_finding: accepted` |
| VER-3 | No source code, test file, or configuration file was modified (only documentation files written) | `git diff --name-only` against HEAD SHA at run start; assert all changed files are documentation files only |
| VER-4 | The committed documentation file exists at the declared natural documentation location | File existence check at the declared path |
| VER-5 | The committed documentation contains a header noting the HEAD SHA and run ID of production | Read committed file; assert header fields present |
| VER-6 | `docs/architecture/technical-debt.md` has been updated to mark this gap as resolved | Read technical-debt file; assert entry with matching gap description has status `resolved` |
| VER-7 | `STATUS-104.md` updated with current run ID and final status within 5 minutes of run end | Read STATUS file; assert run ID, status, and timestamp within tolerance |
| VER-8 | The Reflection artifact exists at the declared path and is non-empty | File existence and non-emptiness check |
| VER-9 | GATE-1 approval recorded in `STATUS-104.md` with approver identity, timestamp, and decision | Read STATUS file; assert `gate_outcomes.GATE-1` populated |
| VER-10 | Engineering Metadata artifact exists with all required provenance fields | Read metadata file; assert all required fields non-empty |

---

## Reflection

At the end of every run, produce a Reflection at `docs/engineering/documentation/reflections/REFLECTION-104-{run-id}.md`.

The Reflection must contain all ten LOOP-STANDARD sections plus:

- **Accuracy Evidence Quality:** Count of claims in the documentation; count independently verified by Checker; count flagged as `UNSUPPORTED`; whether Checker accepted on first attempt.
- **Gap Type Distribution:** Whether the documentation type declared in the task record was the most appropriate type; whether an alternative type would have better addressed the gap.
- **Source Code Discoverability:** How easily the documented behaviours could be inferred from the source code; recommendations for improving inline documentation that would make future LOOP-104 runs faster.
- **Staleness Risk Assessment:** Based on the volatility of the documented component (measured from git commit frequency in LOOP-001 outputs), estimate when this documentation may next require a refresh.
- **Technical Debt Delta:** Count of documentation gaps resolved; whether `technical-debt.md` was successfully updated.

---

## Human Approval Gates

### GATE-1 — Hard Gate: Documentation Accuracy Approval Before Commit

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 4 (Documentation Checker Review), before Step 6 (Documentation Commit) |
| Artifact Under Review | Draft documentation and Documentation Checker Report |
| Approver | Principal Engineer, Engineering Lead, or Architecture Owner for the documented component |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; no documentation committed; all analysis and draft artifacts preserved for revision |
| Audit Trail | Written to `STATUS-104.md` under `gate_outcomes.GATE-1`; approver identity, timestamp, decision, and any correction requests recorded |

**Fires when:**
- Normal workflow completion of Step 4 (every run reaches GATE-1 before documentation is committed)
- Checker validation rejected on both initial and retry attempts

**Reviewer guidance:** Read the draft documentation against the source code for the documented component. Confirm every factual claim is accurate. Confirm the Checker's verification evidence. Confirm the documentation is appropriate for the declared audience. Note any missing information or inaccuracies in your decision record; these become correction guidance for either the current run's retry or a future LOOP-104 run.

---

### GATE-2 — Soft Gate: New ADR or Ambiguous Documentation Type

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 1 (Gap Classification), before Step 2 (Context Assembly) |
| Artifact Under Review | Gap classification with ambiguity or new-ADR flag |
| Approver | Engineering Lead or Architecture Owner |
| Notification Channel | Declared in `.loop-104.yml`; defaults to draft comment on task record |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds with the default recommendation in the gap classification (existing ADR summary or primary documentation type); `soft_gate_auto_proceeded: true` recorded |
| Audit Trail | Notification timestamp, ambiguity description, outcome recorded under `gate_outcomes.GATE-2` |

**Fires when:** Gap is classified as a new ADR (redirect to LOOP-301 recommended) or documentation type is ambiguous with significant scope implications.

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-104.md`. The executing agent reads this at each step boundary and halts immediately. No further documentation changes are made. A partial Reflection is produced.

---

## Failure Recovery

### FR-1 — Documentation Checker Rejects Draft (Both Attempts)

**Detection:** DOC-CHECKER returns `overall_finding: rejected` on both initial and retry.
**Immediate Action:** Record both rejected checker reports. Flag `checker_rejected_max_retries = true`.
**Recovery:** Trigger GATE-1 unconditionally. Human reviews both drafts and both checker reports.
**Rollback:** No documentation committed; no source code modified.

### FR-2 — Source Code Modified During Drafting

**Detection:** DOC-CHECKER or VER-3 detects that a source, test, or configuration file was modified.
**Immediate Action:** Immediately halt. Record the unexpected modification.
**Recovery:** Do not commit any changes. `git checkout` the modified files to restore source state. Record the incident in the Reflection. Trigger GATE-1 with full incident record.
**Rollback:** Restore all modified source files via `git checkout`.

### FR-3 — Documentation Location Already Has Conflicting Content

**Detection:** The natural documentation location already contains a file that contradicts the draft.
**Immediate Action:** Record the conflict in the gap classification.
**Recovery:** Include both the existing content and the draft content in the GATE-1 review package, explicitly noting the conflict. Human decides: overwrite existing content (if it is inaccurate), merge (if both contain correct information), or abort.
**Rollback:** No commit made until human decision at GATE-1.

### FR-4 — Context HEAD SHA Mismatch

**Detection:** Context package HEAD SHA does not match current repository HEAD SHA.
**Immediate Action:** Record the mismatch. Do not proceed with drafting.
**Recovery:** Trigger GATE-1. Human confirms whether to re-assemble context or proceed with acknowledged drift.
**Rollback:** No documentation committed.

### FR-5 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time exceeds 6 hours.
**Immediate Action:** Complete current atomic step; do not begin next step.
**Recovery:** Write all output artifacts produced so far (including any partial draft). Set `STATUS-104.md` to `stopped`, `reason: max_duration_exceeded`. Produce partial Reflection.
**Rollback:** If documentation has been committed (Step 6 completed), the committed documentation is valid. If Step 6 was not reached, no commit was made.

---

## Metrics

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 8) |
| `run.steps_total` | 8 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that auto-proceeded |
| `verification.level1.pass` | VER-1 through VER-10 criteria passed |
| `verification.level1.fail` | VER-1 through VER-10 criteria failed |
| `reflection.produced` | Boolean |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `doc.type` | Documentation type produced (`README` \| `API-doc` \| `inline-doc` \| `architecture-doc` \| `ADR-summary` \| `runbook`) |
| `doc.target_component` | The component documented (module or service name) |
| `doc.checker_accepted_first_attempt` | Boolean — did DOC-CHECKER accept on first attempt |
| `doc.claims_total` | Count of factual claims in the produced documentation |
| `doc.claims_verified` | Count of claims independently verified by DOC-CHECKER with cited evidence |
| `doc.claims_unsupported` | Count of claims flagged as UNSUPPORTED by DOC-CHECKER |
| `doc.technical_debt_entry_resolved` | Boolean — was technical-debt.md updated |
| `doc.lines_produced` | Line count of the produced documentation artifact |

---

## Risks

### RISK-1 — Scope Creep

- **Description:** Documentation drafting expands to cover components, APIs, or behaviours not declared in the gap definition; or the DOC-WRITER modifies source code to add docstring anchors.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** The documented component is entangled with undocumented dependencies; accurate documentation of component A requires mentioning component B.
- **Control:** Gap classification strictly bounds the target. VER-3 detects any source code modification. DOC-CHECKER flags claims about undeclared components.
- **Detection:** VER-3 failure; DOC-CHECKER scope violation flag.
- **Response:** FR-2 procedure if source modified. Scope expansion requires a new gap-classification step with GATE-1 re-approval.

### RISK-2 — Architectural Drift in Documentation

- **Description:** Produced documentation introduces or reinforces incorrect terminology, incorrect module names, or incorrect dependency descriptions that diverge from LOOP-001 architecture artifacts.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** DOC-WRITER uses informal naming or reasoning from memory rather than from LOOP-001 artifacts.
- **Control:** DOC-CHECKER terminology consistency check (criterion 4) catches divergences from LOOP-001 artifacts before commit.
- **Detection:** DOC-CHECKER criterion 4 failure.
- **Response:** DOC-WRITER corrects terminology in retry; if uncorrected, GATE-1 reviewer catches it.

### RISK-3 — Hidden Dependencies

- **Description:** Documentation describes a component's behaviour as if it were standalone, omitting critical dependency-induced constraints that affect the documented behaviour.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** The component's behaviour is substantially determined by its dependencies (configuration-driven behaviour, delegated logic).
- **Control:** Context package includes dependency context (Tier 2 Required). DOC-CHECKER checks completeness for API docs.
- **Detection:** Future LOOP-104 run discovers that documentation is incomplete; or engineers report documentation inaccuracy.
- **Response:** New LOOP-104 task to update the documentation with dependency-induced constraints added.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop operates on repository source and documentation files only. It does not read or write tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption

- **Description:** Not applicable. Documentation commits to the repository are reversible via `git revert`. No runtime data is modified.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are idempotent: re-producing documentation from the same codebase state produces equivalent content.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation

- **Description:** Documentation of security-sensitive components (authentication flows, authorisation rules, tenant isolation mechanisms) could inadvertently expose internal security logic that should not be in public-facing documentation.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** The target component is security-sensitive (auth, authz, cryptography, tenant isolation) and the declared audience includes external consumers.
- **Control:** Gap classification audience field; DOC-CHECKER includes a review of whether internal security logic is inappropriately exposed in documentation intended for external audiences. GATE-1 reviewer confirms.
- **Detection:** DOC-CHECKER security exposure flag.
- **Response:** GATE-1 review explicitly includes security review for any documentation of security-sensitive components.

### RISK-8 — Runaway Execution

- **Description:** Documentation drafting for a very large API surface or complex runbook could exceed the 6-hour maximum run duration.
- **Likelihood:** Low
- **Impact:** Low
- **Trigger Condition:** Target component has an unusually large API surface or very complex operational procedures.
- **Control:** Maximum run duration of 6 hours enforced by FR-5. Task record should scope documentation to a manageable surface; large API surfaces should be split into multiple DOC tasks.
- **Detection:** Wall-clock elapsed time check at each step boundary.
- **Response:** FR-5 procedure; partial documentation artifact preserved; new DOC task covers the remaining scope.

---

## Stop Conditions

**Normal completion** (status `completed`):

| ID | Condition |
|----|-----------|
| SC-1 | All verification criteria VER-1 through VER-10 passed |
| SC-2 | Documentation committed to natural repository location |
| SC-3 | GATE-1 approval recorded with approver identity and timestamp |
| SC-4 | All artifacts in Outputs table written |
| SC-5 | `STATUS-104.md` updated with all metrics and `completed` status |
| SC-6 | `SKILL-104.md` updated |
| SC-7 | Reflection artifact written |

**Normal termination without completion** (status `stopped`):

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (6 hours) reached |
| SC-9 | GATE-1 denied |
| SC-10 | Concurrent run detected for same task ID |
| SC-11 | Emergency Stop received |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Gap Analysis Artifacts:**
- [ ] `docs/engineering/documentation/gap-classification.md` written with type, target, audience, gap description, and natural location
- [ ] `docs/engineering/documentation/doc-checker-report.md` written with overall finding `accepted`

**Documentation Artifacts:**
- [ ] Documentation committed to the declared natural repository location
- [ ] Documentation contains header with HEAD SHA and run ID

**Technical Debt:**
- [ ] `docs/architecture/technical-debt.md` updated to mark gap as `resolved`

**Gates:**
- [ ] GATE-1 approval recorded in `STATUS-104.md` with approver identity, timestamp, and decision
- [ ] GATE-2 outcome recorded if fired

**State:**
- [ ] `docs/loops/engineering/STATUS-104.md` updated with all metrics and final status
- [ ] `docs/loops/engineering/SKILL-104.md` updated

**Metadata and Reflection:**
- [ ] `docs/engineering/documentation/metadata/METADATA-104-{run-id}.md` produced with all provenance fields
- [ ] `docs/engineering/documentation/engineering-summary.md` produced
- [ ] `docs/engineering/documentation/reflections/REFLECTION-104-{run-id}.md` produced with all required sections

---

## Future Improvements

- **Documentation freshness detection:** Integrate LOOP-001 git change frequency data to automatically flag documentation for re-validation when the documented component's source changes significantly after this run.
- **Audience-adaptive generation:** Build audience profiles in SKILL-104 — what vocabulary level, what level of implementation detail, and what example density is appropriate for each audience type in this repository's culture.
- **Cross-reference validation:** After documentation is committed, verify that any cross-references to other documentation files or API specs resolve correctly.
- **Docstring coverage metric:** Integrate with existing inline documentation coverage tooling (JavaDoc coverage, Python docstring coverage) to track inline documentation completion across the module catalog over time.
- **ADR gap detection:** During gap classification, cross-reference the module's accepted ADRs against any architecture documents for that module and automatically identify ADRs that are not reflected in documentation — creating task records for LOOP-104 or LOOP-301 as appropriate.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract requirements
- `docs/loops/shared/SPEC-011-REVIEW-PROCESS.md` — review process for loop activation
- `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — shared Engineering Loop context
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate definitions
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage conventions

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-104 as the governed documentation loop for the AEOS Engineering Loop series.

