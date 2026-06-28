---
# PROVENANCE METADATA
Original Path: docs/loops/governance/LOOP-304-Release-Readiness.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Governance loop to evaluate release readiness criteria.
Generalized Purpose: Governance loop to evaluate release readiness criteria.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-006 — Verification, LOOP-007 — Reflection
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-304 — Release Readiness

**Loop ID:** LOOP-304
**Name:** Release Readiness
**Version:** 1.0
**Status:** Active
**Category:** Governance
**Depends On:** LOOP-006 — Verification, LOOP-007 — Reflection
**Human Gates:** Hard, Soft
**Owner:** Principal Architecture Function
**Maintainer:** Principal Architecture Function
**Max Run Duration:** 3 hours
**Introduced In:** 2026-06-27

---

## Purpose

LOOP-304 produces a structured release readiness assessment that gates the entry into the release pipeline. It synthesises outputs from verification, governance, and compliance loops to determine whether the system is ready to begin the release process. This loop's GATE-1 approval is the mandatory prerequisite for LOOP-401 (Release Checklist) to begin: no release may proceed without an approved readiness assessment from this loop. The loop does not perform verification or compliance assessment; it aggregates and synthesises results from the loops that do.

---

## Problem Statement

Without a structured release readiness gate, releases enter the deployment pipeline in an unknown state. Verification gaps, unresolved compliance findings, undocumented architectural decisions, and incomplete work items are discovered during deployment rather than before it — at the point of maximum disruption and cost. Engineering teams under schedule pressure skip informal readiness checks, creating an observable pattern where "ready to release" is determined by calendar date rather than evidence. A formalised, evidence-backed readiness assessment with a mandatory dual-approval gate prevents this failure mode.

---

## Why This Loop Exists

Release readiness is a synthesis problem: the evidence is spread across multiple loops and must be aggregated into a single, signed-off record. Codifying this as a loop makes the synthesis repeatable, the evidence traceable (each claim is backed by a specific upstream loop output), and the approval auditable. The dual-approval requirement at GATE-1 (Principal Engineer plus Architecture Owner) ensures that no single individual can unilaterally authorise a release. Without this loop, the release pipeline has no mandatory entry gate; LOOP-401 cannot begin without GATE-1 approval from this loop.

---

## Scope

**In scope:**
- Verifying that all planned work items for the release are in Completed status
- Confirming LOOP-006 Verification has been run on all implementation artifacts and all criteria passed
- Confirming LOOP-207 Security Validation shows no Critical or High findings
- Confirming LOOP-205 Multi-Tenant Validation shows no isolation gaps
- Confirming LOOP-303 Compliance shows Compliant status on all mandatory requirements
- Confirming all ADRs generated for this release are in Accepted status
- Confirming release notes are drafted and accurate
- Confirming rollback plan is documented and has been reviewed
- Computing a numeric readiness score and classifying the release as Ready, Conditional, or Not Ready
- Producing the signed-off readiness assessment that LOOP-401 requires as its mandatory input

**Out of scope:**
- Performing verification, security assessment, or compliance assessment (those are performed by LOOP-006, LOOP-207, LOOP-303 respectively)
- Making implementation changes to resolve readiness blockers
- Approving the deployment itself (that is LOOP-401's scope)
- Post-release monitoring (that is LOOP-403's scope)

**Maximum run duration:** 3 hours. If not terminated within this window, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| LOOP-006 Verification report | File (`docs/verification/verification-report-{run-id}.md`) | LOOP-006 — Verification | Required |
| LOOP-006 STATUS file | File (`docs/loops/core/STATUS-006.md`) | LOOP-006 | Required |
| LOOP-007 Reflection outputs | File (`docs/reflections/REFLECTION-007-{run-id}.md`) | LOOP-007 — Reflection | Required |
| LOOP-207 Security Validation report | File (`docs/security/security-validation-report-{run-id}.md`) | LOOP-207 | Required |
| LOOP-205 Multi-Tenant Validation report | File (`docs/platform/multi-tenant-validation-report-{run-id}.md`) | LOOP-205 | Required |
| LOOP-303 Compliance report | File (`docs/governance/compliance/compliance-status-report-{run-id}.md`) | LOOP-303 | Required |
| Release task completion register | File (`docs/release/task-register-{release-id}.md`) | Human/project management | Required |
| Release notes draft | File (`docs/release/release-notes-{release-id}.md`) | Human/release manager | Required |
| Rollback plan | File (`docs/release/rollback-plan-{release-id}.md`) | Human/engineering | Required |
| ADR index | File (`docs/adr/Decision-Records.md`) | Repository | Required |

### Input Validation

Before Step 1 begins, the loop must verify:
- All five upstream loop reports exist and reference the current release candidate (via run IDs or release ID marker).
- Release task completion register, release notes draft, and rollback plan all exist and are non-empty.
- HEAD SHA is consistent across all upstream loop reports (SPEC-001 §2.C5): SHA mismatch is treated as a Hard Gate condition.
- No concurrent LOOP-304 instance is running.

---

## Outputs

| Artifact | Path | Description |
|----------|------|-------------|
| Release Readiness Assessment | `docs/governance/release-readiness/release-readiness-assessment-{run-id}.md` | Authoritative readiness assessment: readiness score, classification (Ready/Conditional/Not Ready), evidence summary for each dimension, approved blockers list |
| Open Blockers | `docs/governance/release-readiness/open-blockers-{run-id}.md` | Structured list of blockers preventing a Ready classification (produced only if score < 100 or any dimension is non-green) |
| Loop Status | `docs/loops/governance/STATUS-304.md` | Run state, gate outcomes, and most recent run ID; LOOP-401 reads this file to confirm GATE-1 approval before beginning |
| Loop Skill | `docs/loops/governance/SKILL-304.md` | Accumulated calibration observations from release readiness assessment runs |
| Run Metadata | `docs/governance/release-readiness/metadata/METADATA-304-{run-id}.md` | Provenance: run ID, HEAD SHA start/end, upstream dependency run IDs, elapsed seconds, final status |
| Reflection | `docs/governance/release-readiness/reflections/REFLECTION-304-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-006 — Verification:** Provides verification results for all implementation artifacts in the current release. Readiness requires all verification criteria to have passed. **Mandatory.**
- **LOOP-007 — Reflection:** Provides accumulated observations from prior cycles that may surface known patterns of risk relevant to the current release. **Mandatory.** If LOOP-007 has not been run in this cycle, the readiness assessment must note this as a Medium gap.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation by release manager** — The release manager explicitly requests a readiness assessment for a specific release candidate.
2. **Release pipeline entry** — The engineering team declares that implementation work is complete for a release candidate and requests a readiness assessment as the pipeline entry gate.
3. **Scheduled pre-release** — At a declared milestone in the release schedule (e.g., release -5 days), a readiness assessment is automatically triggered.

Trigger source, timestamp, and release candidate identifier must be recorded in STATUS-304.md at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | LOOP-006 report exists for the current release candidate | File existence check; run ID cross-reference |
| PRE-2 | LOOP-207 report exists for the current release candidate | File existence check; run ID cross-reference |
| PRE-3 | LOOP-205 report exists for the current release candidate | File existence check; run ID cross-reference |
| PRE-4 | LOOP-303 report exists and GATE-1 was approved (from STATUS-303.md) | Read STATUS-303.md `gate_outcomes`; confirm GATE-1 approved |
| PRE-5 | Release task completion register exists and is non-empty | File existence and size check |
| PRE-6 | Release notes draft exists and is non-empty | File existence and size check |
| PRE-7 | Rollback plan exists and is non-empty | File existence and size check |
| PRE-8 | No concurrent LOOP-304 instance is running | Read STATUS-304.md; if `current_status: running`, halt with `skipped_concurrent` |
| PRE-9 | HEAD SHA consistent across all upstream loop reports | Cross-reference SHA fields in all METADATA files; SHA mismatch triggers Hard Gate |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | `docs/verification/`, `docs/security/`, `docs/platform/`, `docs/governance/`, `docs/release/`, `docs/adr/` | Filesystem permissions of executing agent | Scoped to this repository only | N/A (read-only) | Yes |
| `docs/governance/release-readiness/` | Write | Readiness assessment, open blockers, metadata, reflection | Same as executing agent | Confined to this directory | `git checkout docs/governance/release-readiness/` | Yes — re-run from identical inputs produces equivalent assessment |
| `docs/loops/governance/STATUS-304.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/STATUS-304.md` | Yes |
| `docs/loops/governance/SKILL-304.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/SKILL-304.md` | Yes |

This loop makes no writes to external systems, APIs, databases, or deployment targets. It does not modify any source file, test file, or configuration file. LOOP-401 reads STATUS-304.md to confirm GATE-1 approval; that read is LOOP-401's action, not this loop's.

---

## Required Context

Before beginning, the executing agent must have loaded:
- LOOP-006 verification report (most recent for this release candidate)
- LOOP-207 security validation report (most recent for this release candidate)
- LOOP-205 multi-tenant validation report (most recent for this release candidate)
- LOOP-303 compliance status report (most recent, GATE-1 approved)
- LOOP-007 reflection output (most recent)
- Release task completion register
- Release notes draft
- Rollback plan document
- `docs/adr/Decision-Records.md` — to check ADR status for release-related decisions

---

## Agents

| Agent ID | Role | Artifact | Responsibilities | Tools | Gate Assignment |
|----------|------|----------|-----------------|-------|----------------|
| READINESS-LOADER | Orchestrator | None | Loads and validates all upstream inputs; checks SHA consistency; coordinates assessment | Read, directory listing | Pre-step validation |
| READINESS-ANALYST | Maker | Draft readiness assessment | Computes readiness score; identifies open blockers; produces assessment document | Read, Write | Step 2 |
| READINESS-CHECKER | Checker | Checker finding | Independently verifies score calculation; confirms all inputs were loaded; confirms no open blocker was omitted | Read | Step 3 |
| READINESS-WRITER | Maker | Published readiness assessment | After GATE-1 approval, publishes the assessment; updates STATUS-304.md to record GATE-1 approval in the specific field that LOOP-401 will read | Write | Step 6 |
| STATUS-WRITER | Maker | STATUS-304.md, SKILL-304.md, Reflection | Updates loop state files and produces Reflection | Write | Final step |

**Maker/Checker constraint:** READINESS-ANALYST and READINESS-CHECKER must be different agent instances.

---

## Workflow

**Step 1 — Input Load and Validation (READINESS-LOADER)**
- Inputs: All declared inputs
- Outputs: Validated input set; SHA consistency result; release candidate identifier confirmed
- Record trigger, release candidate ID, and start timestamp in STATUS-304.md (`current_status: running`)
- Check for Emergency Stop signal at step start

**Step 2 — Readiness Assessment (READINESS-ANALYST — Maker)**
- Inputs: All validated inputs from Step 1
- Outputs: Draft release readiness assessment with readiness score and dimension-level status

Compute **readiness score** using the formula:
```
base_score = (completed_tasks / planned_tasks) × (passed_verification / total_verification) × 100
critical_penalty = critical_security_findings × 20 + critical_compliance_findings × 20
readiness_score = max(0, base_score - critical_penalty)
```

Assess each dimension:
- **Work completion:** All planned tasks in Completed status → Green; any Incomplete → Red
- **Verification:** All LOOP-006 criteria passed → Green; any failed → Red
- **Security:** LOOP-207 shows no Critical/High findings → Green; Critical → Red, High → Yellow
- **Tenant isolation:** LOOP-205 shows no isolation gaps → Green; any gap → Red
- **Compliance:** LOOP-303 shows all mandatory requirements Compliant → Green; any Non-Compliant (Critical/High) → Red
- **ADR completeness:** All release-related ADRs in Accepted status → Green; any in Proposed → Yellow
- **Release artifacts:** Release notes drafted and accurate, rollback plan present → Green; absent → Red

Classify overall release status:
- **Ready (score ≥ 85, no Red dimensions):** Release may proceed to LOOP-401 pending GATE-1 approval
- **Conditional (score 70–84, no Red dimensions):** Release may proceed with documented conditions; triggers GATE-2
- **Not Ready (score < 70, OR any Red dimension):** Release must not proceed to LOOP-401

Produce open blockers list for any Red dimension.
- Check for Emergency Stop signal at step start

**Step 3 — Independent Verification (READINESS-CHECKER — Checker)**
- Inputs: Draft assessment, all upstream reports
- Outputs: Checker finding at `docs/governance/release-readiness/checker-finding-{run-id}.md`
- Verify: score formula applied correctly; all five upstream reports were actually loaded (not assumed); no open blocker omitted; dimension statuses match the evidence in upstream reports
- Check for Emergency Stop signal at step start

**Step 4 — Conditional Notification (SOFT GATE — GATE-2)**
- Condition: Readiness score is 70–84 (Conditional classification)
- Action: Notify release manager with score, dimension statuses, and conditions required for approval
- Timeout: 24 hours
- Auto-proceed to GATE-1 after 24 hours if no objection
- Record gate outcome in STATUS-304.md

**Step 5 — Dual Approval (HARD GATE — GATE-1)**
- Artifact under review: Draft readiness assessment + Checker finding + open blockers list
- Approver: **Both** Principal Engineer and Architecture Owner must approve. Both approvals must be recorded before LOOP-401 may begin.
- On approval: READINESS-WRITER publishes the assessment; STATUS-304.md is updated with both approvers' identities and timestamps in the `gate_outcomes.GATE-1` field; this field is what LOOP-401 reads
- On denial: READINESS-ANALYST revises per feedback; Checker re-reviews; GATE-1 re-opened
- Record gate outcome with both approvers' identities, roles, decisions, rationales, and timestamps

**Step 6 — Publication and Closure (READINESS-WRITER + STATUS-WRITER)**
- Inputs: Approved assessment, gate outcomes
- Outputs: Published readiness assessment; open-blockers file (if applicable); STATUS-304.md updated; SKILL-304.md updated; METADATA-304-{run-id}.md; REFLECTION-304-{run-id}.md
- Check for Emergency Stop signal at step start

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Readiness assessment exists at declared path and is non-empty | File existence and size check |
| VER-2 | Readiness score is computed and present as a number between 0 and 100 | Parse assessment; confirm score field |
| VER-3 | All seven readiness dimensions have a status (Green/Yellow/Red) with evidence citation | Read assessment; confirm dimension table with evidence column |
| VER-4 | Readiness classification matches score: Ready ≥ 85 no-Red, Conditional 70-84 no-Red, Not Ready otherwise | Parse score and dimension statuses; verify classification rule applied correctly |
| VER-5 | Open blockers file exists if classification is Conditional or Not Ready | File existence check conditional on classification |
| VER-6 | No source files, test files, or configuration files were modified by this loop | `git diff --name-only` confirms only `docs/governance/release-readiness/` and `docs/loops/governance/` files changed |
| VER-7 | STATUS-304.md updated with this run's ID, GATE-1 approval with both approvers' identities, and timestamp | Read STATUS-304.md `gate_outcomes.GATE-1`; confirm both approver fields present |
| VER-8 | Reflection artifact exists at declared path and is non-empty | File existence and size check |
| VER-9 | Checker finding document exists and records a verdict | Read checker finding; confirm verdict field |
| VER-10 | All five upstream loop report IDs are recorded in METADATA-304-{run-id}.md (provenance chain) | Read metadata file; confirm upstream_run_ids field contains all five IDs |

---

## Reflection

Every run must produce a Reflection artifact at `docs/governance/release-readiness/reflections/REFLECTION-304-{run-id}.md` before the run is marked closed. For runs that produce a Not Ready classification, the Reflection must include a specific section listing each Red dimension and its root cause, to enable future runs to quickly identify whether prior blockers have been resolved.

---

## Human Approval Gates

### GATE-1 — Dual Approval for Release Entry (Hard Gate)

- **Gate type:** Hard
- **Position in Workflow:** After Step 3 (Checker verification) and Step 4 (conditional notification), before release assessment is published
- **Artifact under review:** Draft readiness assessment + Checker finding + open blockers list
- **Approver:** Principal Engineer AND Architecture Owner (both must approve; one approval alone is insufficient)
- **What approvers must confirm:** Readiness score calculation is accurate; dimension statuses reflect actual evidence; the release is ready (or conditionally ready with stated conditions) to proceed to LOOP-401
- **LOOP-401 dependency:** LOOP-401 reads STATUS-304.md `gate_outcomes.GATE-1` field and will not begin if both approvers are not recorded
- **If denied:** READINESS-ANALYST revises per feedback; Checker re-reviews; GATE-1 re-opened. Either approver may deny.
- **No timeout:** Hard Gate. Loop remains blocked until both approvals are explicitly recorded.
- **Audit trail:** Both approvals written to STATUS-304.md `gate_outcomes.GATE-1` with each approver's identity, role, decision, rationale, and ISO 8601 timestamp.

### GATE-2 — Conditional Release Notification (Soft Gate)

- **Gate type:** Soft
- **Position in Workflow:** Step 4, after Checker verification, before GATE-1; fires only if classification is Conditional (score 70–84)
- **Notification channel:** Release manager and engineering leadership (from `.loop-config.yml`)
- **Notification content:** Readiness score, classification, Yellow dimension statuses with conditions, list of conditions required for a full Ready classification
- **Timeout:** 24 hours
- **Auto-proceed action:** Loop proceeds to GATE-1 after 24 hours with no objection; conditions are recorded in the assessment as acknowledged
- **If objection received:** Loop halts; human decides whether to resolve Yellow dimensions before proceeding or to accept them as conditions
- **Audit trail:** Gate outcome written to STATUS-304.md `gate_outcomes.GATE-2`

---

## Failure Recovery

### FR-1 — Upstream Loop Reports Missing or Not From Current Release Candidate
- **Detection:** PRE-1 through PRE-4 fail; run ID mismatch
- **Immediate action:** Halt with `precondition_failed`; record specific missing input in STATUS-304.md
- **Recovery path:** The missing upstream loop is re-run for the current release candidate; LOOP-304 re-runs after all reports are available
- **Rollback scope:** No outputs written; no rollback needed

### FR-2 — SHA Mismatch Across Upstream Reports (PRE-9)
- **Detection:** READINESS-LOADER detects that upstream reports have different HEAD SHAs, indicating concurrent repository modification
- **Immediate action:** Trigger Hard Gate per SPEC-001 §2.C5; record mismatch in STATUS-304.md
- **Recovery path:** Human determines whether to re-run upstream loops on a consistent HEAD or to acknowledge the mismatch; loop re-runs after resolution
- **Rollback scope:** No outputs written

### FR-3 — GATE-1 Denied
- **Detection:** Either approver records a denial
- **Immediate action:** Assessment remains unpublished; denial reason recorded in STATUS-304.md; LOOP-401 cannot begin
- **Recovery path:** READINESS-ANALYST revises assessment per feedback; Checker re-reviews; GATE-1 re-opened with both approvers
- **Rollback scope:** Draft assessment file remains for revision; STATUS-304.md does not record GATE-1 approval until both approvers confirm

### FR-4 — Emergency Stop Received
- **Detection:** STATUS-304.md `emergency_stopped` set to `true`
- **Immediate action:** Current step terminates; rollback action applied; partial Reflection produced
- **Rollback scope:** `git checkout docs/governance/release-readiness/` removes partially-written outputs; LOOP-401 cannot begin (STATUS-304.md will not show GATE-1 approval)
- **Escalation:** Partial Reflection flagged for Principal Engineer review before re-run

---

## Metrics

**Standard metrics:**

| Metric | Definition |
|--------|-----------|
| `run.duration_seconds` | Wall-clock time from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of workflow steps completed before termination |
| `run.steps_total` | 6 |
| `gate.hard.count` | Number of Hard Gates reached |
| `gate.hard.approved` | Number of Hard Gates approved |
| `gate.hard.denied` | Number of Hard Gates denied |
| `gate.soft.count` | Number of Soft Gates reached |
| `gate.soft.auto_proceeded` | Number of Soft Gates that auto-proceeded |
| `verification.level1.pass` | Count of VER criteria that passed |
| `verification.level1.fail` | Count of VER criteria that failed |
| `reflection.produced` | Boolean |

**Loop-specific metrics:**

| Metric | Definition |
|--------|-----------|
| `readiness.score` | Numeric readiness score 0–100 |
| `readiness.classification` | `ready` \| `conditional` \| `not_ready` |
| `readiness.red_dimensions` | Count of Red-status dimensions |
| `readiness.yellow_dimensions` | Count of Yellow-status dimensions |
| `readiness.open_blockers` | Count of open blockers |
| `readiness.gate1_denial_count` | Number of GATE-1 denials before final approval |
| `readiness.days_to_release_entry` | Calendar days from release candidate declaration to GATE-1 approval |

---

## Risks

### RISK-1 — Scope Creep
- **Description:** READINESS-ANALYST attempts to perform verification or compliance assessment rather than synthesising existing results, duplicating work and potentially contradicting upstream loop findings.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Upstream loop reports contain gaps; analyst attempts to fill them independently.
- **Control:** Scope section explicitly restricts to synthesis of upstream loop outputs. Any dimension with missing upstream evidence is classified as Not Assessed (Red equivalent) rather than assessed by this loop.
- **Detection:** READINESS-CHECKER verifies that every dimension status cites an upstream loop report as its evidence source.
- **Response:** Dimension without upstream evidence is reclassified as Not Assessed; upstream loop is flagged as missing.

### RISK-2 — Architectural Drift
- **Description:** Upstream loop reports are from a prior release candidate, causing the readiness assessment to reflect a state that no longer matches the current repository.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Rapid iteration with frequent commits between upstream loop runs.
- **Control:** PRE-9 enforces SHA consistency across all upstream reports. READINESS-LOADER cross-references run IDs against the declared release candidate identifier.
- **Detection:** SHA mismatch at PRE-9 triggers Hard Gate.
- **Response:** Upstream loops re-run on consistent HEAD; LOOP-304 re-runs.

### RISK-3 — Hidden Dependencies
- **Description:** LOOP-205 and LOOP-207 are listed as indirect dependencies (not in the Depends On field) but their outputs are required. If those loops have not been run for this candidate, PRE-3 and PRE-2 catch this, but the dependency chain is not explicit in the header.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** New team members or automated orchestration systems do not run LOOP-205/LOOP-207 before LOOP-304.
- **Control:** PRE-2 and PRE-3 enforce the dependency. The Depends On field is being deliberately kept minimal (LOOP-006 and LOOP-007 as direct consumers); LOOP-205 and LOOP-207 are consumed transitively via their reports.
- **Detection:** PRE-2 and PRE-3 failure at run start.
- **Response:** Release manager ensures all upstream loops are run before triggering LOOP-304.

### RISK-4 — Tenant Isolation Breach
- **Description:** Not Applicable. This loop synthesises documentation artifacts only. It does not read or write tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption
- **Description:** The readiness assessment is published before both GATE-1 approvals are recorded, creating a window where LOOP-401 could read an incomplete approval record.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** READINESS-WRITER publishes before STATUS-304.md is updated with both approvals; race condition in multi-agent execution.
- **Control:** READINESS-WRITER writes the assessment file and then immediately writes STATUS-304.md in the same step. The STATUS-304.md write records both approvals atomically. LOOP-401 reads STATUS-304.md, not the assessment file, to confirm GATE-1.
- **Detection:** VER-7 verifies both approvers are recorded in STATUS-304.md before the run is marked completed.
- **Response:** If VER-7 fails, the run is not marked completed; GATE-1 must be re-confirmed.

### RISK-6 — Non-Idempotent External Write
- **Description:** Not Applicable. All writes are to git-tracked files within the repository. No external systems are written to.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation
- **Description:** The readiness assessment inadvertently discloses sensitive security findings from LOOP-207 in a form that exceeds the need-to-know scope.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** READINESS-ANALYST quotes full security finding details rather than referencing the LOOP-207 report.
- **Control:** The readiness assessment references LOOP-207 findings by count and severity only; it does not reproduce finding details. Full finding details remain in the LOOP-207 report, which has its own access controls.
- **Detection:** READINESS-CHECKER verifies the assessment does not contain credential patterns or detailed vulnerability descriptions.
- **Response:** Assessment is revised to reference-only; GATE-1 re-run.

### RISK-8 — Runaway Execution
- **Description:** READINESS-ANALYST enters a scoring loop that does not converge due to conflicting inputs or a formula error.
- **Likelihood:** Low
- **Impact:** Low
- **Trigger Condition:** Pathological input values (e.g., planned_tasks = 0, causing division by zero in score formula).
- **Control:** Maximum run duration of 3 hours enforced. Score formula includes guards for zero-denominator cases (if planned_tasks = 0, base_score = 0). Step timing recorded.
- **Detection:** Run duration monitoring; step completion recorded in STATUS-304.md.
- **Response:** Loop halts at 3-hour mark; formula error is recorded in Reflection; READINESS-ANALYST is corrected.

---

## Stop Conditions

| Condition | Cleanup Performed | State Left Behind |
|-----------|-------------------|-------------------|
| Maximum run duration (3 hours) exceeded | Partial Reflection produced; STATUS-304.md updated to `stopped` | Partial assessment in `docs/governance/release-readiness/`; LOOP-401 cannot proceed |
| Emergency Stop received | Current step rollback applied; partial Reflection produced | STATUS-304.md records step and HEAD SHA; LOOP-401 cannot proceed |
| Concurrent run detected (PRE-8) | No cleanup needed | STATUS-304.md records `skipped_concurrent` |
| Input validation failure (PRE-1 through PRE-9) | No cleanup needed | STATUS-304.md records `precondition_failed` with specific reason |

---

## Deliverables

- [ ] `docs/governance/release-readiness/release-readiness-assessment-{run-id}.md` exists and is non-empty
- [ ] `docs/governance/release-readiness/open-blockers-{run-id}.md` exists if classification is Conditional or Not Ready
- [ ] Readiness score and classification are present in the assessment
- [ ] All seven readiness dimensions have Green/Yellow/Red status with evidence citations
- [ ] `docs/loops/governance/STATUS-304.md` updated with this run's ID and both GATE-1 approvers' identities, decisions, and timestamps
- [ ] `docs/loops/governance/SKILL-304.md` updated with at least one new calibration observation
- [ ] `docs/governance/release-readiness/metadata/METADATA-304-{run-id}.md` produced with all five upstream loop run IDs
- [ ] `docs/governance/release-readiness/reflections/REFLECTION-304-{run-id}.md` produced with all 10 required sections
- [ ] `docs/governance/release-readiness/checker-finding-{run-id}.md` exists with a verdict
- [ ] All VER-1 through VER-10 criteria confirmed passed

---

## Future Improvements

- **Automated blocker tracking:** Add a step that cross-references open blockers with the issue tracker to determine whether remediation is already in progress. Requires MAJOR version bump (new external state).
- **Score trend:** Include historical readiness scores in the assessment to show whether readiness is improving or degrading over successive release candidates.
- **Configurable dimension weights:** Allow the readiness score formula weights to be configured per-release (e.g., a security patch release might weight the security dimension more heavily). Requires MAJOR version bump (new input: weight configuration).
- **Release type classification:** Distinguish between major, minor, patch, and emergency releases, applying different gate requirements per type.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — canonical loop engineering standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract specification
- `docs/loops/core/LOOP-006-Verification.md` — mandatory upstream dependency
- `docs/loops/core/LOOP-007-Reflection.md` — mandatory upstream dependency
- `docs/loops/release/LOOP-401-Release-Checklist.md` — the downstream loop that requires GATE-1 approval from this loop
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version.

