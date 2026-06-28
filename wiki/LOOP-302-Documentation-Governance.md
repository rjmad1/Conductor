---
# PROVENANCE METADATA
Original Path: docs/loops/governance/LOOP-302-Documentation-Governance.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Governance loop to enforce documentation standards.
Generalized Purpose: Governance loop to enforce documentation standards.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-007 — Reflection
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-302 — Documentation Governance

**Loop ID:** LOOP-302
**Name:** Documentation Governance
**Version:** 1.0
**Status:** Active
**Category:** Governance
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-007 — Reflection
**Human Gates:** Hard, Soft
**Owner:** Principal Architecture Function
**Maintainer:** Principal Architecture Function
**Max Run Duration:** 3 hours
**Introduced In:** 2026-06-27

---

## Purpose

LOOP-302 audits the repository's documentation for completeness, currency, accuracy, and conformance with declared documentation standards. It produces a documentation health report that identifies gaps (missing documents), staleness (documents that have not kept pace with code changes), broken references, and improvement priorities. The output is a structured, evidence-backed report that the engineering team can act on and that governance loops (LOOP-303) can incorporate into compliance assessments. Every finding is classified by severity so remediation effort can be prioritised.

---

## Problem Statement

Documentation debt accumulates silently. Modules ship without README files. ADRs sit in Proposed status for months. Architecture documents describe a system that no longer exists. Loop SKILL files fall behind their loops by dozens of runs. Without a systematic audit, no one knows the true state of documentation completeness, and the gap between documented and actual system behaviour widens with every release. By the time the gap is noticed it has compounded to the point where remediation is a multi-sprint effort rather than a single pass.

---

## Why This Loop Exists

Documentation governance is inherently cross-cutting: no single engineer owns the documentation surface of the entire repository. Codifying it as a loop makes the audit repeatable, the findings verifiable and prioritised, and the trend measurable across releases. The Maker/Checker pattern ensures that gap classifications are independently reviewed before they are published. The Hard Gate before report publication ensures that Critical findings — which may have compliance or release implications — are confirmed by a human before they become authoritative governance records. Without this loop, documentation quality is unobservable at the system level.

---

## Scope

**In scope:**
- Checking every module in the LOOP-001 module catalog for a README.md (presence and minimum section headings: purpose, usage, configuration, dependencies)
- Checking every public API in the LOOP-001 API catalog for a corresponding specification file
- Checking every ADR in `docs/adr/` for a valid status field (Proposed/Accepted/Deprecated/Superseded)
- Flagging ADRs in `Proposed` status older than 30 days without an explanatory comment
- Checking freshness of `docs/architecture/` files (flagging any not updated within 7 days of the last architecture-affecting merge)
- Checking LOOP SKILL files for currency (flagging SKILL-NNN.md files that are more than 30 runs behind their corresponding STATUS file run count)
- Verifying every Active loop has a corresponding STATUS file
- Producing a gap inventory and documentation health report with severity classifications

**Out of scope:**
- Writing or repairing any documentation (this loop audits; remediation is a separate human or loop action)
- Auditing the semantic correctness of documentation content (presence and structure only)
- Auditing test documentation or inline code comments
- Cross-repository documentation checks

**Maximum run duration:** 3 hours. If not terminated within this window, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| LOOP-001 module catalog | File (`docs/architecture/module-catalog.md`) | LOOP-001 — Architecture Discovery | Required |
| LOOP-001 API catalog | File (`docs/architecture/api-catalog.md`) | LOOP-001 — Architecture Discovery | Required |
| LOOP-001 service catalog | File (`docs/architecture/service-catalog.md`) | LOOP-001 — Architecture Discovery | Required |
| LOOP-001 run metadata | File (`docs/architecture/metadata/METADATA-001-{run-id}.md`) | LOOP-001 | Required — used for freshness check |
| ADR directory | Directory (`docs/adr/`) | Repository | Required |
| Loop status files | Directory (`docs/loops/`) | Repository | Required |
| LOOP-007 Reflection outputs | File (`docs/reflections/REFLECTION-007-{run-id}.md`) | LOOP-007 — Reflection | Optional — provides accumulated observations about documentation quality patterns |
| Prior documentation health report | File (`docs/governance/documentation/documentation-health-report-{prior-run-id}.md`) | Prior LOOP-302 run | Optional — enables trend analysis |

### Input Validation

Before Step 1 begins, the loop must verify:
- `docs/architecture/module-catalog.md` exists and is non-empty.
- `docs/architecture/api-catalog.md` exists and is non-empty.
- LOOP-001 outputs are no more than 7 days old (checked via METADATA-001 timestamp). If stale, treat as Soft Gate: notify and proceed after 24 hours if no objection.
- No concurrent LOOP-302 instance is running (check STATUS-302.md for `running` status).

---

## Outputs

| Artifact | Path | Description |
|----------|------|-------------|
| Documentation Health Report | `docs/governance/documentation/documentation-health-report-{run-id}.md` | Narrative report summarising overall health score, severity breakdown, top priorities, and trend vs. prior run |
| Gap Inventory | `docs/governance/documentation/gap-inventory-{run-id}.md` | Structured table of every identified gap with location, gap type, severity, age, and recommended remediation |
| Staleness Findings | `docs/governance/documentation/staleness-findings-{run-id}.md` | List of documents not updated in step with code changes, with last-modified date and relevant code change date |
| Loop Status | `docs/loops/governance/STATUS-302.md` | Run state, gate outcomes, and most recent run ID |
| Loop Skill | `docs/loops/governance/SKILL-302.md` | Accumulated calibration observations from documentation governance runs |
| Run Metadata | `docs/governance/documentation/metadata/METADATA-302-{run-id}.md` | Provenance: run ID, HEAD SHA start/end, upstream dependency run IDs, elapsed seconds, final status |
| Reflection | `docs/governance/documentation/reflections/REFLECTION-302-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the authoritative module catalog, API catalog, and service catalog against which documentation completeness is checked. **Mandatory.** Outputs must be no more than 7 days old.
- **LOOP-007 — Reflection:** Provides accumulated observations about documentation quality patterns from prior engineering cycles. **Optional.** If absent, the loop proceeds without trend context from the reflection chain.

---

## Trigger

A run is initiated by any of the following:

1. **Scheduled execution** — Once per sprint (recommended: weekly or at each release candidate gate).
2. **Manual invocation** — An engineer or governance process explicitly triggers an audit.
3. **Pre-release trigger** — LOOP-304 requires a current documentation health report (no more than 7 days old) as part of release readiness assessment.
4. **LOOP-001 completion** — After a LOOP-001 run that discovers significant new modules or APIs, LOOP-302 may be triggered to check whether documentation was created alongside the new components.

Trigger source and timestamp must be recorded in STATUS-302.md at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/architecture/module-catalog.md` exists and is non-empty | File existence and size check |
| PRE-2 | `docs/architecture/api-catalog.md` exists and is non-empty | File existence and size check |
| PRE-3 | `docs/adr/` directory exists | Directory existence check |
| PRE-4 | `docs/loops/` directory exists and contains at least one STATUS file | Directory listing check |
| PRE-5 | No concurrent LOOP-302 instance is running | Read STATUS-302.md; if `current_status: running`, halt with `skipped_concurrent` |
| PRE-6 | LOOP-001 outputs are no more than 7 days old | Read METADATA-001; check timestamp field |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All files under repo root (read-only documentation scan) | Filesystem permissions of executing agent | Scoped to this repository; no cross-repo reads | N/A (read-only source scan) | Yes |
| `docs/governance/documentation/` | Write | Health report, gap inventory, staleness findings, metadata, reflection | Same as executing agent | Confined to this directory | `git checkout docs/governance/documentation/` | Yes — re-run produces equivalent report from same inputs |
| `docs/loops/governance/STATUS-302.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/STATUS-302.md` | Yes |
| `docs/loops/governance/SKILL-302.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/SKILL-302.md` | Yes |

This loop makes no writes to external systems, APIs, databases, or deployment targets. It does not modify any source file, test file, or configuration file.

---

## Required Context

Before beginning, the executing agent must have loaded:
- `docs/architecture/module-catalog.md` — authoritative module list
- `docs/architecture/api-catalog.md` — authoritative API list
- `docs/architecture/service-catalog.md` — authoritative service list
- The LOOP-001 run metadata file (most recent) — for freshness check
- `docs/loops/` directory listing — to find all STATUS-NNN.md files and their run counts
- `docs/adr/Decision-Records.md` — to enumerate all ADRs and their statuses
- Prior documentation health report (if available) — for trend comparison

---

## Agents

| Agent ID | Role | Artifact | Responsibilities | Tools | Gate Assignment |
|----------|------|----------|-----------------|-------|----------------|
| DOC-SCANNER | Maker (inventory) | Raw inventory | Scans repository; produces raw file inventory against module/API/service catalogs; identifies missing files | Read, directory listing, Grep | Steps 1–2 |
| FRESHNESS-CHECKER | Maker (staleness) | Staleness findings | For each documentation artifact, checks last-modified vs. most recent related code change | Read, git log | Step 2 |
| COVERAGE-ANALYST | Maker (gap analysis) | Gap inventory draft | Classifies all gaps by severity; produces structured gap inventory | Read, Write | Step 3 |
| COVERAGE-CHECKER | Checker | Checker finding | Independently verifies gap classifications are not false positives; confirms each Critical/High gap has specific evidence | Read | Step 4 |
| REPORT-WRITER | Maker | Documentation health report | Synthesises findings into the narrative health report with overall score and priorities | Write | Step 5/7 |
| STATUS-WRITER | Maker | STATUS-302.md, SKILL-302.md, Reflection | Updates loop state files and produces Reflection | Write | Final step |

**Maker/Checker constraint:** COVERAGE-ANALYST and COVERAGE-CHECKER must be different agent instances. The same agent that produces the gap inventory may not serve as the Checker that verifies it.

---

## Workflow

**Step 1 — Inventory Scan (DOC-SCANNER)**
- Inputs: module-catalog.md, api-catalog.md, service-catalog.md, `docs/adr/` directory listing, `docs/loops/` directory listing
- Outputs: Raw inventory — list of modules with/without README, APIs with/without spec file, ADRs with status and creation date, Active loops with/without STATUS file
- Record step start in STATUS-302.md
- Check for Emergency Stop signal at step start

**Step 2 — Freshness Check (FRESHNESS-CHECKER)**
- Inputs: Raw inventory from Step 1; git log for `docs/architecture/` files; LOOP-001 run metadata
- Outputs: Staleness findings — for each architecture document, the last-modified date and whether it is within the 7-day freshness window; for each ADR in Proposed status, date created and whether it exceeds 30 days
- Check for Emergency Stop signal at step start

**Step 3 — Gap Analysis and Classification (COVERAGE-ANALYST — Maker)**
- Inputs: Raw inventory, staleness findings, LOOP-007 outputs (if available)
- Outputs: Gap inventory draft with severity classifications:
  - **Critical:** Module with no README.md; public API with no specification file
  - **High:** ADR stuck in Proposed for >30 days; architecture document not refreshed within 7 days of architecture-affecting merge; Active loop with no STATUS file
  - **Medium:** README.md present but missing required section headings; SKILL file >30 runs behind STATUS file
  - **Low:** Minor gaps (missing optional sections, formatting inconsistencies)
- Compute overall documentation health score: (compliant items / total items) × 100, severity-weighted
- Check for Emergency Stop signal at step start

**Step 4 — Independent Verification (COVERAGE-CHECKER — Checker)**
- Inputs: Gap inventory draft, raw inventory, repository filesystem for spot-checks
- Outputs: Checker finding document at `docs/governance/documentation/checker-finding-{run-id}.md`
- Independently verify: each Critical and High gap has a specific file path or module name as evidence; no false positives (test modules, generated code directories, explicitly excluded paths); severity classifications are consistent; health score formula applied correctly
- Record pass/fail per VER criterion in finding document
- Check for Emergency Stop signal at step start

**Step 5 — Notification if Significant Debt (SOFT GATE — GATE-2)**
- Condition: Critical or High gaps exceed 20% of total documented surface area, OR Critical gap count exceeds 5
- Action: Notify engineering team and release manager via declared notification channel
- Notification content: Health score, Critical count, High count, top 3 Critical gaps with remediation path
- Timeout: 24 hours; auto-proceed if no objection
- Record gate outcome in STATUS-302.md

**Step 6 — Human Review and Approval (HARD GATE — GATE-1)**
- Artifact under review: Gap inventory + Checker finding + health report draft
- Approver: Principal Architecture Function or Engineering Lead
- Human must confirm: severity classifications are accurate; no Critical findings are false positives; report is suitable for publication
- On approval: REPORT-WRITER publishes final report
- On denial: COVERAGE-ANALYST revises per feedback; returns to Step 4
- Record gate outcome in STATUS-302.md with reviewer identity, decision, rationale, and timestamp

**Step 7 — Report Publication and Closure (REPORT-WRITER + STATUS-WRITER)**
- Inputs: Approved gap inventory, staleness findings, Checker finding
- Outputs: Final documentation-health-report-{run-id}.md; updated STATUS-302.md; updated SKILL-302.md; METADATA-302-{run-id}.md; REFLECTION-302-{run-id}.md
- Check for Emergency Stop signal at step start

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Documentation health report exists at declared path and is non-empty | File existence and size check |
| VER-2 | Gap inventory exists and contains at least one row (even if "no gaps found") | File existence; parse table structure |
| VER-3 | Every module listed in module-catalog.md has a corresponding entry in the gap inventory | Cross-reference module count in catalog vs. inventory rows |
| VER-4 | Every Critical and High gap has a specific file path or module name as evidence | Read gap inventory; confirm evidence column non-empty for all Critical/High rows |
| VER-5 | No source files, test files, or configuration files were modified by this loop | `git diff --name-only` confirms only `docs/governance/documentation/` and `docs/loops/governance/` files changed |
| VER-6 | STATUS-302.md updated with this run's ID and a timestamp within the last 10 minutes | Read STATUS-302.md; check `current_run_id` and `last_updated` |
| VER-7 | Reflection artifact exists at declared path and is non-empty | File existence and size check |
| VER-8 | Checker finding document exists and records a verdict | Read checker finding; confirm verdict field present |
| VER-9 | GATE-1 approval is recorded in STATUS-302.md with reviewer identity, decision, and timestamp | Read `gate_outcomes` field |
| VER-10 | Documentation health score is a number between 0 and 100 present in the health report | Parse health report; confirm score field |

---

## Reflection

Every run must produce a Reflection artifact at `docs/governance/documentation/reflections/REFLECTION-302-{run-id}.md` before the run is marked closed. The Reflection is produced by STATUS-WRITER and must contain all ten required sections from LOOP-STANDARD.md §10: Run Summary, What Was Attempted, What Happened, Verification Results, Gate Outcomes, Failures and Anomalies, Risk Observations, Metrics, Improvement Candidates, and Decision Log. A run may not be marked `completed` without a Reflection.

---

## Human Approval Gates

### GATE-1 — Documentation Health Report Approval (Hard Gate)

- **Gate type:** Hard
- **Position in Workflow:** After Step 4 (Checker review) and Step 5 (notification), before report is published
- **Artifact under review:** Gap inventory draft + Checker finding + health report draft
- **Approver:** Principal Architecture Function or Engineering Lead
- **What the approver must confirm:** Severity classifications are accurate and consistent; Critical findings are not false positives (test-only modules and generated-code directories are not documentation gaps); report is suitable for publication and distribution
- **If denied:** COVERAGE-ANALYST revises severity classifications per feedback; returns to Step 4 for re-check; GATE-1 re-opened
- **No timeout:** Hard Gate has no timeout. Loop remains blocked until explicit approval or denial.
- **Audit trail:** Written to STATUS-302.md `gate_outcomes` with reviewer identity, role, decision, rationale, and ISO 8601 timestamp.

### GATE-2 — Significant Debt Notification (Soft Gate)

- **Gate type:** Soft
- **Position in Workflow:** Step 5, after Checker review, before GATE-1
- **Trigger condition:** Critical or High gaps exceed 20% of documented surface area, OR Critical gap count exceeds 5
- **Notification channel:** Engineering team channel and release manager (from `.loop-config.yml` key `notification_channel`)
- **Notification content:** Documentation health score, Critical gap count, High gap count, top 3 Critical gaps with module name and recommended remediation action
- **Timeout:** 24 hours from notification timestamp
- **Auto-proceed action:** Loop proceeds to GATE-1 automatically after 24 hours with no objection; auto-proceed recorded in STATUS-302.md
- **If objection received:** Loop halts; human decides whether to widen the review or revise classifications before proceeding to GATE-1
- **Audit trail:** Gate outcome (notified timestamp, auto-proceed timestamp or objection received) written to STATUS-302.md `gate_outcomes`

---

## Failure Recovery

### FR-1 — LOOP-001 Outputs Stale or Missing
- **Detection:** PRE-1, PRE-2, or PRE-6 fails
- **Immediate action:** Halt at precondition check; record `precondition_failed` in STATUS-302.md
- **Recovery path:** Trigger LOOP-001; re-run LOOP-302 once fresh outputs are available
- **Rollback scope:** No outputs written; no rollback needed

### FR-2 — Module Catalog Contains Modules That Cannot Be Located in Repository
- **Detection:** DOC-SCANNER cannot find the path declared for a module in module-catalog.md
- **Immediate action:** Record the unresolvable module as a High gap of type `module_not_found`; continue scanning other modules
- **Recovery path:** Human resolves whether the module was renamed or removed; re-run LOOP-001 to refresh catalog
- **Rollback scope:** No partial output rollback needed; gap inventory records unresolvable modules

### FR-3 — GATE-1 Denied: False Positives Identified
- **Detection:** GATE-1 approver identifies false positives; denial recorded with specific gap IDs to remove
- **Immediate action:** COVERAGE-ANALYST revises gap inventory, removing confirmed false positives
- **Recovery path:** Revised inventory goes to COVERAGE-CHECKER for re-check; GATE-1 re-opened
- **Rollback scope:** Gap inventory file updated in place; no health report published until re-approval

### FR-4 — Emergency Stop Received
- **Detection:** STATUS-302.md `emergency_stopped` field set to `true`
- **Immediate action:** Current step terminates; rollback action applied; partial Reflection produced
- **Rollback scope:** `git checkout docs/governance/documentation/` removes partially-written outputs
- **Escalation:** Partial Reflection flagged for human review before re-run

---

## Metrics

**Standard metrics:**

| Metric | Definition |
|--------|-----------|
| `run.duration_seconds` | Wall-clock time from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of workflow steps completed before termination |
| `run.steps_total` | 7 |
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
| `doc.modules_checked` | Total modules from module catalog checked |
| `doc.apis_checked` | Total APIs from API catalog checked |
| `doc.adrs_checked` | Total ADRs in `docs/adr/` checked |
| `doc.gaps_critical` | Count of Critical severity gaps |
| `doc.gaps_high` | Count of High severity gaps |
| `doc.gaps_medium` | Count of Medium severity gaps |
| `doc.gaps_low` | Count of Low severity gaps |
| `doc.health_score` | Numeric health score 0–100 |
| `doc.health_score_delta` | Change in health score vs. prior run (positive = improved) |

---

## Risks

### RISK-1 — Scope Creep
- **Description:** DOC-SCANNER attempts to assess documentation content quality rather than presence and structure, producing subjective findings that cannot be independently verified.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Ambiguous instruction; agent interprets "completeness" as "correctness."
- **Control:** Scope section explicitly restricts to presence and structure checks. COVERAGE-CHECKER verifies each finding has a specific, objective criterion (file missing, heading absent, threshold exceeded).
- **Detection:** GATE-1 review catches subjective findings.
- **Response:** COVERAGE-ANALYST revises to objective criteria only; GATE-1 re-run.

### RISK-2 — Architectural Drift
- **Description:** LOOP-302 produces findings based on a stale LOOP-001 module catalog, resulting in false-positive gaps (modules that no longer exist) or false-negative gaps (new modules not in the catalog).
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** LOOP-001 has not been run after significant repository restructuring.
- **Control:** PRE-6 enforces a 7-day freshness requirement. COVERAGE-CHECKER spot-checks that gap locations exist in the repository.
- **Detection:** COVERAGE-CHECKER flags modules listed in the catalog that cannot be found at their declared paths.
- **Response:** LOOP-001 is re-run; LOOP-302 re-runs with fresh catalog.

### RISK-3 — Hidden Dependencies
- **Description:** The freshness check depends on git log, which may not be available in all execution environments (shallow clones, detached HEAD).
- **Likelihood:** Low
- **Impact:** Low
- **Trigger Condition:** Loop executes in a shallow clone or detached HEAD state without full git history.
- **Control:** PRE-1 verifies git is available. FRESHNESS-CHECKER degrades gracefully (reports "freshness_check_unavailable") if git log is unavailable.
- **Detection:** FRESHNESS-CHECKER step records "freshness_check_unavailable" in staleness findings.
- **Response:** Human reviews staleness findings with awareness that some checks were skipped.

### RISK-4 — Tenant Isolation Breach
- **Description:** Not Applicable. This loop reads and writes governance documentation artifacts only. It does not access tenant-scoped runtime data, application databases, or multi-tenant services of any kind.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Data Loss or Corruption
- **Description:** A prior documentation health report is overwritten before its findings have been reviewed or actioned.
- **Likelihood:** Low
- **Impact:** Low
- **Trigger Condition:** Run ID collision (two runs with identical ID) or incorrect file write path.
- **Control:** Run IDs include date and sequence number; collision is structurally prevented. All outputs are git-tracked; prior versions are recoverable.
- **Detection:** VER-1 and VER-2 confirm outputs exist; git history confirms prior outputs preserved.
- **Response:** `git checkout` of prior run artifacts if needed.

### RISK-6 — Non-Idempotent External Write
- **Description:** Not Applicable. All writes are to git-tracked files within the repository. Re-running with identical inputs produces equivalent outputs. No external systems are written to.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation
- **Description:** The loop's scan inadvertently reads files containing secrets and includes excerpts in the gap inventory or health report.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Repository contains improperly committed credential files; scanner reads and quotes content.
- **Control:** DOC-SCANNER operates on documentation files (Markdown, YAML spec files) only. It does not read `.env`, `*.key`, `*.pem`, or credential files. COVERAGE-CHECKER includes a credential pattern scan on output artifacts before GATE-1.
- **Detection:** COVERAGE-CHECKER flags credential patterns in output artifacts.
- **Response:** Output artifact is redacted; scanner exclusion list updated; incident recorded in Reflection.

### RISK-8 — Runaway Execution
- **Description:** DOC-SCANNER enters a recursive scan loop on a symlinked directory structure or encounters an unusually large repository.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Symlinked directories that create cycles; repository with unusual depth.
- **Control:** Maximum run duration of 3 hours enforced. DOC-SCANNER uses depth-limited traversal (maximum 10 levels). Symlinks are followed only one level deep.
- **Detection:** Run duration monitoring; DOC-SCANNER records file count at each depth level.
- **Response:** Loop halts at 3-hour mark with `stopped` status; partial inventory recorded.

---

## Stop Conditions

| Condition | Cleanup Performed | State Left Behind |
|-----------|-------------------|-------------------|
| Maximum run duration (3 hours) exceeded | Partial Reflection produced; STATUS-302.md updated to `stopped` | Partial outputs in `docs/governance/documentation/`; marked as partial |
| Emergency Stop received | Current step rollback applied; partial Reflection produced | STATUS-302.md records step and HEAD SHA |
| Concurrent run detected (PRE-5) | No cleanup needed (loop never started) | STATUS-302.md records `skipped_concurrent` |
| LOOP-001 outputs missing or stale (PRE-1/PRE-2/PRE-6) | No cleanup needed | STATUS-302.md records `precondition_failed` with reason |

---

## Deliverables

- [ ] `docs/governance/documentation/documentation-health-report-{run-id}.md` exists and is non-empty with health score
- [ ] `docs/governance/documentation/gap-inventory-{run-id}.md` exists with at least one row
- [ ] `docs/governance/documentation/staleness-findings-{run-id}.md` exists
- [ ] `docs/loops/governance/STATUS-302.md` updated with this run's ID, outcome, and gate outcomes
- [ ] `docs/loops/governance/SKILL-302.md` updated with at least one new calibration observation
- [ ] `docs/governance/documentation/metadata/METADATA-302-{run-id}.md` produced with all required provenance fields
- [ ] `docs/governance/documentation/reflections/REFLECTION-302-{run-id}.md` produced with all 10 required sections
- [ ] `docs/governance/documentation/checker-finding-{run-id}.md` exists with a verdict
- [ ] GATE-1 approval recorded in STATUS-302.md with reviewer identity, role, decision, and timestamp
- [ ] All VER-1 through VER-10 criteria confirmed passed

---

## Future Improvements

- **Content quality scoring:** Extend the audit to assess README minimum content using a declared required-section schema. Requires MAJOR version bump (new input: required-section schema file).
- **Broken reference detection:** Add a step to scan Markdown files for links and validate that each internal link target exists in the repository. Currently out of scope.
- **Trend dashboard:** Produce a machine-readable metrics file suitable for visualisation across multiple LOOP-302 runs, enabling documentation health trend charts.
- **Auto-remediation stubs:** For each gap, generate a stub document pre-populated with required structure but empty sections, reducing the friction of starting remediation. Requires MAJOR version bump (new output artifacts).

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — canonical loop engineering standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract specification
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — mandatory upstream dependency
- `docs/loops/core/LOOP-007-Reflection.md` — optional upstream dependency
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions

---

## Version History

- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version.

