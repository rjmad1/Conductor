---
# PROVENANCE METADATA
Original Path: docs/loops/governance/LOOP-303-Compliance.md
Original Version: 0.2
Extraction Date: 2026-06-27
Original Purpose: Governance loop for compliance and security checks.
Generalized Purpose: Governance loop for compliance and security checks.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-006 — Verification, LOOP-207 — Security Validation
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-303 — Compliance

**Loop ID:** LOOP-303
**Name:** Compliance
**Version:** 0.2
**Status:** Draft
**Category:** Governance
**Depends On:** LOOP-006 — Verification, LOOP-207 — Security Validation
**Human Gates:** Hard, Soft
**Owner:** Principal Architecture Function
**Maintainer:** Principal Architecture Function
**Max Run Duration:** 4 hours
**Introduced In:** 2026-06-27

---

## Purpose

LOOP-303 assesses the repository's compliance posture against declared regulatory, security, and engineering policy requirements. It produces a compliance status report with findings classified by requirement, severity, and remediation priority. The report is authoritative: it becomes the compliance record for the release cycle in which it is produced and is consumed directly by LOOP-304 (Release Readiness) as a mandatory input. Because compliance findings have legal, regulatory, and contractual implications, this loop requires mandatory human approval before any finding is published.

---

## Problem Statement

Compliance requirements span multiple dimensions — security controls, dependency licensing, data protection, access management, and engineering standards — and no single team member holds complete visibility across all of them. Without a structured, repeatable assessment, compliance gaps surface in audits, customer due diligence reviews, or production incidents rather than at the point where they can be remediated cheaply. The cost of a compliance gap discovered post-release is orders of magnitude higher than the cost of finding it pre-release. A manual compliance checklist is insufficient: it is inconsistently applied, incompletely documented, and produces no audit trail.

---

## Why This Loop Exists

Compliance assessment is methodical, cross-cutting, and audit-sensitive. Codifying it as a loop ensures every requirement is assessed on every cycle, every finding is supported by evidence, and the assessment process itself is traceable. The Hard Gate before publication is non-negotiable: compliance findings may trigger legal obligations, and a human compliance reviewer must own the decision to publish them. The Maker/Checker pattern ensures that automated assessment outputs are independently reviewed before they are presented to the human reviewer. Without this loop, compliance posture is a matter of opinion rather than evidence.

---

## Scope

**In scope:**
- Security compliance: Incorporating LOOP-207 Security Validation findings; any Critical or High security finding is an automatic compliance failure
- Dependency licensing: Checking declared dependencies against an allowlist of approved licenses; flagging GPL, AGPL, or unknown licenses as compliance findings
- Data protection: Checking that repositories handling personal data have a declared data retention policy and data classification in their documentation
- Access control: Verifying that production credentials do not appear in the repository (extends LOOP-207 secrets scan to governance records)
- Engineering standards: Verifying LOOP-STANDARD conformance for all Active loops; confirming STATUS files exist for all Active loops; flagging loops in Draft/Review status older than 90 days without evidence of active authoring

**Out of scope:**
- Making compliance decisions (this loop assesses and reports; decisions are made by humans)
- Remediating compliance gaps (remediation is a separate engineering or governance action)
- Legal advice or regulatory interpretation
- Compliance with requirements not declared in `docs/standards/Compliance.md`
- External audit execution

**Maximum run duration:** 4 hours. If not terminated within this window, the loop halts, records partial outputs, and produces a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Compliance requirements register | File (`docs/standards/Compliance.md`) | Repository | Required |
| LOOP-207 Security Validation outputs | File (`docs/security/security-validation-report-{run-id}.md`) | LOOP-207 — Security Validation | Required |
| LOOP-006 Verification outputs | File (`docs/verification/verification-report-{run-id}.md`) | LOOP-006 — Verification | Required |
| Dependency manifest files | Files (`build.gradle`, `pom.xml`, `package.json`, etc.) | Repository | Required |
| Loop status files | Directory (`docs/loops/`) | Repository | Required |
| Data classification documentation | File (`docs/data-classification.md` if present) | Repository | Optional — absence is itself a compliance finding if the system handles personal data |
| License allowlist | File (`docs/standards/License-Allowlist.md`) | Repository | Required |

### Input Validation

Before Step 1 begins, the loop must verify:
- `docs/standards/Compliance.md` exists and is non-empty.
- LOOP-207 outputs exist and are from the current release candidate (verify run ID matches).
- LOOP-006 outputs exist and are from the current release candidate.
- No concurrent LOOP-303 instance is running (check STATUS-303.md for `running` status).
- HEAD SHA is consistent across all input artifacts (SPEC-001 §2.C5): any SHA mismatch is treated as a Hard Gate condition.

---

## Outputs

| Artifact | Path | Description |
|----------|------|-------------|
| Compliance Status Report | `docs/governance/compliance/compliance-status-report-{run-id}.md` | Authoritative compliance report: one row per requirement, status, finding severity, and evidence |
| Non-Compliant Findings | `docs/governance/compliance/non-compliant-findings-{run-id}.md` | Structured list of non-compliant findings with requirement ID, severity, evidence, and recommended remediation action (produced only if any non-compliant findings exist) |
| Loop Status | `docs/loops/governance/STATUS-303.md` | Run state, gate outcomes, and most recent run ID |
| Loop Skill | `docs/loops/governance/SKILL-303.md` | Accumulated calibration observations from compliance assessment runs |
| Run Metadata | `docs/governance/compliance/metadata/METADATA-303-{run-id}.md` | Provenance: run ID, HEAD SHA start/end, upstream dependency run IDs, elapsed seconds, final status |
| Reflection | `docs/governance/compliance/reflections/REFLECTION-303-{run-id}.md` | Per-run structured reflection produced before run closure |

---

## Dependencies

- **LOOP-006 — Verification:** Provides the verification report for the current release candidate. The compliance assessment incorporates whether all verification criteria passed. **Mandatory.**
- **LOOP-207 — Security Validation:** Provides the security validation report. Any Critical or High security finding is an automatic compliance failure. **Mandatory.**

---

## Trigger

A run is initiated by any of the following:

1. **Pre-release trigger** — LOOP-304 (Release Readiness) requires a current compliance report (no more than 7 days old and from the current release candidate) before it can begin.
2. **Manual invocation** — A compliance officer or engineering lead explicitly triggers the assessment.
3. **Security event** — A Critical security finding is reported from any source; a compliance assessment is triggered to determine the compliance impact.
4. **Scheduled execution** — At minimum once per release cycle; recommended quarterly for any release-independent compliance tracking.

Trigger source, timestamp, and target release candidate identifier must be recorded in STATUS-303.md at run start.

---

## Preconditions

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | `docs/standards/Compliance.md` exists and is non-empty | File existence and size check |
| PRE-2 | `docs/standards/License-Allowlist.md` exists and is non-empty | File existence and size check |
| PRE-3 | LOOP-207 outputs exist for the current release candidate | Check run ID in LOOP-207 STATUS file and verify report file exists |
| PRE-4 | LOOP-006 outputs exist for the current release candidate | Check run ID in LOOP-006 STATUS file and verify report file exists |
| PRE-5 | No concurrent LOOP-303 instance is running | Read STATUS-303.md; if `current_status: running`, halt with `skipped_concurrent` |
| PRE-6 | HEAD SHA is consistent across all input artifacts | Compare SHA fields in LOOP-207 and LOOP-006 metadata; trigger Hard Gate if mismatch |
| PRE-7 | Dependency manifest files exist (at least one of: build.gradle, pom.xml, package.json) | File existence check |

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | `docs/standards/`, `docs/loops/`, dependency manifest files | Filesystem permissions of executing agent | Scoped to this repository only | N/A (read-only) | Yes |
| `docs/governance/compliance/` | Write | Compliance status report, non-compliant findings, metadata, reflection | Same as executing agent | Confined to this directory | `git checkout docs/governance/compliance/` | Yes — re-run from identical inputs produces equivalent report |
| `docs/loops/governance/STATUS-303.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/STATUS-303.md` | Yes |
| `docs/loops/governance/SKILL-303.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/governance/SKILL-303.md` | Yes |

This loop makes no writes to external systems, APIs, databases, or deployment targets. It does not modify any source file, test file, or configuration file.

---

## Required Context

Before beginning, the executing agent must have loaded:
- `docs/standards/Compliance.md` — the compliance requirements register
- `docs/standards/License-Allowlist.md` — approved license types
- LOOP-207 security validation report for the current release candidate
- LOOP-006 verification report for the current release candidate
- Dependency manifest files (build.gradle, pom.xml, package.json as applicable)
- `docs/loops/` directory listing with all STATUS files
- `docs/data-classification.md` if present

---

## Agents

| Agent ID | Role | Artifact | Responsibilities | Tools | Gate Assignment |
|----------|------|----------|-----------------|-------|----------------|
| COMPLIANCE-LOADER | Orchestrator | None | Loads all inputs, validates PRE conditions, coordinates assessment phases | Read, directory listing | Pre-step validation |
| COMPLIANCE-ANALYST | Maker | Draft compliance assessment | Assesses each requirement in the compliance register; classifies each finding as Compliant, Non-Compliant (with severity), or Not Assessed (with reason) | Read, Write | Step 2 |
| COMPLIANCE-CHECKER | Checker | Checker finding | Independently verifies classifications are supported by evidence; confirms no requirement was assessed without evidence; checks for completeness | Read | Step 3 |
| COMPLIANCE-WRITER | Maker | Published compliance report | After GATE-1 approval, publishes the approved report and non-compliant findings file | Write | Step 5 |
| STATUS-WRITER | Maker | STATUS-303.md, SKILL-303.md, Reflection | Updates loop state files and produces Reflection | Write | Final step |

**Maker/Checker constraint:** COMPLIANCE-ANALYST and COMPLIANCE-CHECKER must be different agent instances. The same agent that produces the draft assessment may not serve as the Checker that verifies it.

---

## Workflow

**Step 1 — Input Load and Validation (COMPLIANCE-LOADER)**
- Inputs: Compliance requirements register, LOOP-207 outputs, LOOP-006 outputs, dependency manifests, loop STATUS files
- Outputs: Validated input set; list of requirements to assess; SHA consistency check result
- Record trigger and start timestamp in STATUS-303.md (`current_status: running`)
- Check for Emergency Stop signal at step start

**Step 2 — Compliance Assessment (COMPLIANCE-ANALYST — Maker)**
- Inputs: All loaded inputs from Step 1
- Outputs: Draft compliance assessment — one row per requirement:
  - **Security compliance:** Pull LOOP-207 findings; any Critical/High security finding → Non-Compliant (Critical)
  - **Dependency licensing:** Parse dependency manifests; check each dependency's license against allowlist; GPL/AGPL/unknown → Non-Compliant (High)
  - **Data protection:** Check for `docs/data-classification.md`; if system handles personal data (as declared in Compliance.md), absence of this file → Non-Compliant (High)
  - **Access control:** Check that LOOP-207 secrets scan found no credentials in repository; any credential finding → Non-Compliant (Critical)
  - **Engineering standards:** Check all Active loops for STATUS files, conformance markers; loops in Draft/Review >90 days without authoring evidence → Non-Compliant (Medium)
- Classify each finding: `Compliant`, `Non-Compliant` (with severity: Critical/High/Medium/Low), or `Not Assessed` (with specific reason)
- Write draft to `docs/governance/compliance/compliance-draft-{run-id}.md`
- Check for Emergency Stop signal at step start

**Step 3 — Independent Verification (COMPLIANCE-CHECKER — Checker)**
- Inputs: Draft compliance assessment, all input artifacts
- Outputs: Checker finding document at `docs/governance/compliance/checker-finding-{run-id}.md`
- Independently verify:
  - Every Non-Compliant finding cites specific evidence (file path, line number, dependency name, requirement ID)
  - Every `Not Assessed` entry has a specific reason (not a generic "not applicable")
  - No requirement from Compliance.md is missing from the assessment
  - No requirement was marked Compliant without evidence of the compliance basis
- Record pass/fail per VER criterion
- Check for Emergency Stop signal at step start

**Step 4 — Mandatory Human Review (HARD GATE — GATE-1)**
- Artifact under review: Draft compliance assessment + Checker finding
- Approver: Designated compliance reviewer (Principal Architecture Function or nominated compliance officer)
- No auto-proceed is permitted for compliance reports. This is a mandatory governance approval.
- Human must confirm: all Non-Compliant findings are accurate; severity classifications are correct; the report is suitable for publication as the authoritative compliance record
- On approval: COMPLIANCE-WRITER publishes the report; STATUS-303.md records approval
- On denial: COMPLIANCE-ANALYST revises per feedback; Checker re-reviews; GATE-1 re-opened
- Record gate outcome in STATUS-303.md with reviewer identity, role, decision, rationale, and ISO 8601 timestamp

**Step 5 — Report Publication (COMPLIANCE-WRITER)**
- Inputs: Approved draft compliance assessment
- Outputs: Published `compliance-status-report-{run-id}.md`; if non-compliant findings exist, `non-compliant-findings-{run-id}.md`
- Check for Emergency Stop signal at step start

**Step 6 — Closure (STATUS-WRITER)**
- Inputs: Published reports, gate outcomes
- Outputs: Updated STATUS-303.md; updated SKILL-303.md; METADATA-303-{run-id}.md; REFLECTION-303-{run-id}.md
- Check for Emergency Stop signal at step start

Note: GATE-2 (Soft Gate) is declared in the Human Approval Gates section. It fires during Step 2 if the aggregate finding count exceeds the declared threshold, prior to GATE-1, as a notification mechanism only.

---

## Verification

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Compliance status report exists at declared path and is non-empty | File existence and size check |
| VER-2 | Every requirement in Compliance.md has a corresponding row in the compliance status report | Count requirements in register; count rows in report; counts must match |
| VER-3 | Every Non-Compliant finding cites specific evidence (file path, dependency name, or requirement reference) | Read report; confirm evidence column non-empty for all Non-Compliant rows |
| VER-4 | No source files, test files, or configuration files were modified by this loop | `git diff --name-only` confirms only `docs/governance/compliance/` and `docs/loops/governance/` files changed |
| VER-5 | STATUS-303.md updated with this run's ID and a timestamp within the last 10 minutes | Read STATUS-303.md; check `current_run_id` and `last_updated` |
| VER-6 | Reflection artifact exists at declared path and is non-empty | File existence and size check |
| VER-7 | Checker finding document exists and records a verdict | Read checker finding; confirm verdict field present |
| VER-8 | GATE-1 approval is recorded in STATUS-303.md with reviewer identity, decision, and timestamp | Read `gate_outcomes` field |
| VER-9 | LOOP-207 Critical and High findings are reflected in the compliance report as Non-Compliant entries | Cross-reference LOOP-207 report critical/high count vs. Non-Compliant (Critical) count in compliance report |
| VER-10 | If any dependency has GPL, AGPL, or unknown license, a Non-Compliant finding exists for that dependency | Cross-reference license check results vs. compliance report |

---

## Reflection

Every run must produce a Reflection artifact at `docs/governance/compliance/reflections/REFLECTION-303-{run-id}.md` before the run is marked closed. The Reflection must contain all ten required sections from LOOP-STANDARD.md §10. A run may not be marked `completed` without a Reflection. Because compliance findings have governance implications, the Reflection for any run that produced Non-Compliant findings must include a Risk Observations section that explicitly notes whether any finding represents a new risk compared to the prior run.

---

## Human Approval Gates

### GATE-1 — Compliance Report Approval (Hard Gate)

- **Gate type:** Hard
- **Position in Workflow:** After Step 3 (Checker verification), before report is published
- **Artifact under review:** Draft compliance assessment + Checker finding
- **Approver:** Principal Architecture Function or nominated compliance officer (designated in `docs/standards/Compliance.md`)
- **What the approver must confirm:** All Non-Compliant findings are accurate and supported by evidence; severity classifications are correct; the report is suitable for publication as the authoritative compliance record for this release cycle
- **No auto-proceed:** Compliance reports require explicit human approval. There is no timeout. The loop remains blocked until explicit approval or denial is recorded.
- **If denied:** COMPLIANCE-ANALYST revises per feedback; Checker re-reviews; GATE-1 re-opened. Denial reason is recorded with specificity in STATUS-303.md.
- **Audit trail:** Written to STATUS-303.md `gate_outcomes` with reviewer identity, role, decision, rationale, and ISO 8601 timestamp. Gate outcomes are permanent records and may not be expunged.

### GATE-2 — Critical Findings Notification (Soft Gate)

- **Gate type:** Soft
- **Position in Workflow:** After Step 2 (compliance assessment draft), before Checker review
- **Trigger condition:** Any Critical Non-Compliant finding is identified, OR total Non-Compliant finding count exceeds 10
- **Notification channel:** Engineering leadership and security officer (from `.loop-config.yml` key `compliance_notification_channel`)
- **Notification content:** Summary of Critical findings by category, total finding count by severity, estimated release impact
- **Timeout:** 24 hours
- **Auto-proceed action:** Loop proceeds to Step 3 (Checker) automatically after 24 hours; auto-proceed recorded in STATUS-303.md. Note: GATE-1 (Hard Gate) still requires explicit approval before publication — GATE-2 auto-proceed does not bypass GATE-1.
- **If objection received:** Loop halts; human decision on whether to halt the release pipeline pending remediation
- **Audit trail:** Gate outcome written to STATUS-303.md `gate_outcomes`

---

## Failure Recovery

### FR-1 — Compliance Requirements Register Missing or Malformed
- **Detection:** PRE-1 fails or Compliance.md cannot be parsed
- **Immediate action:** Halt with `precondition_failed`; record in STATUS-303.md
- **Recovery path:** Human updates Compliance.md to a valid format; loop re-runs
- **Rollback scope:** No outputs written; no rollback needed

### FR-2 — LOOP-207 or LOOP-006 Outputs Missing or From Wrong Release Candidate
- **Detection:** PRE-3 or PRE-4 fails; SHA mismatch detected (PRE-6)
- **Immediate action:** Halt with `precondition_failed` or trigger Hard Gate for SHA mismatch per SPEC-001 §2.C5
- **Recovery path:** Re-run LOOP-207 and/or LOOP-006 for the current release candidate; loop re-runs after fresh outputs are available
- **Rollback scope:** No outputs written if halted at precondition

### FR-3 — GATE-1 Denied: Findings Disputed
- **Detection:** Compliance reviewer records denial with disputed finding IDs
- **Immediate action:** Draft remains unpublished; denial recorded in STATUS-303.md
- **Recovery path:** COMPLIANCE-ANALYST revises disputed findings with additional evidence or corrected classification; COMPLIANCE-CHECKER re-reviews; GATE-1 re-opened
- **Rollback scope:** Draft file remains in place for revision; no published report exists until approval

### FR-4 — Emergency Stop Received
- **Detection:** STATUS-303.md `emergency_stopped` set to `true`
- **Immediate action:** Current step terminates; rollback action applied; partial Reflection produced
- **Rollback scope:** `git checkout docs/governance/compliance/` removes partially-written outputs. Any published report must be clearly marked `RETRACTED` before rollback if it was published before the stop.
- **Escalation:** Partial Reflection flagged for compliance officer review before re-run

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
| `compliance.requirements_assessed` | Total requirements in Compliance.md assessed |
| `compliance.compliant_count` | Count of Compliant findings |
| `compliance.non_compliant_critical` | Count of Non-Compliant (Critical) findings |
| `compliance.non_compliant_high` | Count of Non-Compliant (High) findings |
| `compliance.non_compliant_medium` | Count of Non-Compliant (Medium) findings |
| `compliance.not_assessed_count` | Count of Not Assessed findings |
| `compliance.security_findings_incorporated` | Boolean — were LOOP-207 findings incorporated |
| `compliance.gate1_denial_count` | Number of times GATE-1 was denied before final approval |

---

## Risks

### RISK-1 — Scope Creep
- **Description:** COMPLIANCE-ANALYST attempts to assess requirements beyond those declared in Compliance.md, or makes compliance determinations that require legal expertise.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Ambiguous requirement; agent over-interprets its mandate.
- **Control:** Assessment is strictly bounded to requirements in Compliance.md. Any requirement not in the register is out of scope. COMPLIANCE-CHECKER verifies no out-of-register findings appear.
- **Detection:** GATE-1 review catches out-of-scope findings.
- **Response:** Out-of-scope findings are removed; loop proceeds with in-scope findings only.

### RISK-2 — Architectural Drift
- **Description:** LOOP-207 or LOOP-006 inputs are from a prior release candidate, causing the compliance report to reflect an outdated state.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Rapid iteration; LOOP-207 or LOOP-006 not re-run for the current candidate.
- **Control:** PRE-6 enforces SHA consistency across all input artifacts. COMPLIANCE-LOADER verifies run IDs match the current release candidate identifier.
- **Detection:** SHA mismatch at PRE-6 triggers Hard Gate.
- **Response:** Upstream loops are re-run; LOOP-303 re-runs with fresh inputs.

### RISK-3 — Hidden Dependencies
- **Description:** The license check does not detect transitive dependencies (dependencies of dependencies) that have non-compliant licenses.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Direct dependency has a compliant license but pulls in a GPL transitive dependency.
- **Control:** COMPLIANCE-ANALYST checks both direct and first-level transitive dependencies. For dependency trees deeper than two levels, this is flagged as `Not Assessed (transitive depth exceeded)`.
- **Detection:** COMPLIANCE-CHECKER verifies that the assessment explicitly addresses transitive dependency depth.
- **Response:** Human follow-up required for any `Not Assessed (transitive depth exceeded)` entry.

### RISK-4 — Tenant Isolation Breach
- **Description:** Low. The compliance assessment reviews tenant isolation controls as part of the security compliance dimension (LOOP-207 findings). The assessment itself does not read or write tenant-scoped runtime data, but it evaluates the controls that protect tenant isolation.
- **Likelihood:** Low
- **Impact:** Low
- **Control:** The assessment reads LOOP-207 findings (which are documentation artifacts); it does not interact with tenant data stores.
- **Detection:** COMPLIANCE-CHECKER verifies that the tenant isolation assessment row is present and references LOOP-207 evidence.
- **Response:** If LOOP-207 found tenant isolation failures, they are reflected as Critical Non-Compliant findings in this report.

### RISK-5 — Data Loss or Corruption
- **Description:** The compliance status report is overwritten between the time of approval and the time it is consumed by LOOP-304.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Two LOOP-303 runs producing reports with overlapping run IDs; erroneous overwrite.
- **Control:** Run IDs include date and sequence number; collision is structurally prevented. All outputs are git-tracked.
- **Detection:** LOOP-304 verifies report run ID matches the expected release candidate.
- **Response:** `git checkout` restores the approved report; LOOP-304 re-runs after verification.

### RISK-6 — Non-Idempotent External Write
- **Description:** Not Applicable. All writes are to git-tracked files within the repository. No external systems are written to.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Security Boundary Violation
- **Description:** The compliance assessment report inadvertently includes credential excerpts from the secrets scan evidence.
- **Likelihood:** Low
- **Impact:** Critical
- **Trigger Condition:** COMPLIANCE-ANALYST quotes credential values from LOOP-207 findings in the non-compliant-findings report.
- **Control:** Credentials must be referenced by location (file path, line number) and type, never by value. COMPLIANCE-CHECKER explicitly verifies no credential values appear in output artifacts.
- **Detection:** COMPLIANCE-CHECKER credential pattern scan on output artifacts.
- **Response:** Output artifact is redacted immediately; incident recorded in Reflection and escalated to security officer.

### RISK-8 — Runaway Execution
- **Description:** The dependency license check enters a loop on a large transitive dependency tree or on circular references in a package manifest.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Pathologically large dependency tree; circular npm/gradle dependencies.
- **Control:** Maximum run duration of 4 hours enforced. License check is bounded to direct dependencies and first-level transitive dependencies; deeper levels are logged as Not Assessed.
- **Detection:** Run duration monitoring; step timing recorded in STATUS-303.md.
- **Response:** Loop halts at 4-hour mark with `stopped` status; partial assessment recorded.

---

## Stop Conditions

| Condition | Cleanup Performed | State Left Behind |
|-----------|-------------------|-------------------|
| Maximum run duration (4 hours) exceeded | Partial Reflection produced; STATUS-303.md updated to `stopped` | Partial draft assessment in `docs/governance/compliance/`; marked as partial |
| Emergency Stop received | Current step rollback applied; partial Reflection produced; any published report marked RETRACTED | STATUS-303.md records step and HEAD SHA |
| Concurrent run detected (PRE-5) | No cleanup needed | STATUS-303.md records `skipped_concurrent` |
| Input validation failure (PRE-1 through PRE-7) | No cleanup needed | STATUS-303.md records `precondition_failed` with specific reason |

---

## Deliverables

- [ ] `docs/governance/compliance/compliance-status-report-{run-id}.md` exists and is non-empty
- [ ] `docs/governance/compliance/non-compliant-findings-{run-id}.md` exists if any non-compliant findings were identified
- [ ] Every requirement in Compliance.md has a row in the compliance status report
- [ ] `docs/loops/governance/STATUS-303.md` updated with this run's ID, outcome, and gate outcomes
- [ ] `docs/loops/governance/SKILL-303.md` updated with at least one new calibration observation
- [ ] `docs/governance/compliance/metadata/METADATA-303-{run-id}.md` produced with all required provenance fields
- [ ] `docs/governance/compliance/reflections/REFLECTION-303-{run-id}.md` produced with all 10 required sections
- [ ] `docs/governance/compliance/checker-finding-{run-id}.md` exists with a verdict
- [ ] GATE-1 approval recorded in STATUS-303.md with reviewer identity, role, decision, rationale, and timestamp
- [ ] All VER-1 through VER-10 criteria confirmed passed

---

## Future Improvements

- **Automated license resolution:** Integrate with a dependency license database (e.g., SPDX) to automatically resolve dependency licenses rather than requiring the allowlist to be manually maintained.
- **Transitive dependency depth:** Extend license checking beyond first-level transitive dependencies. Requires a MINOR version bump (new capability, no contract change).
- **Requirement versioning:** Allow the compliance register to version requirements, so that a compliance report can reference which version of each requirement it was assessed against. Requires a MAJOR version bump (new input field).
- **Automated remediation tracking:** Add a step that links Non-Compliant findings to open remediation tasks in the issue tracker. Requires a MAJOR version bump (new external state: issue tracker integration).

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — canonical loop engineering standard
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — loop contract specification
- `docs/loops/core/LOOP-006-Verification.md` — mandatory upstream dependency
- `docs/loops/platform/LOOP-207-Security-Validation.md` — mandatory upstream dependency
- `docs/loops/shared/human-oversight-gates.md` — gate type definitions
- `docs/standards/Compliance.md` — the compliance requirements register this loop assesses against

---

## Version History

- **0.2** — 2026-06-27 — Principal AI Engineering Architect — Reverted to Draft status due to dependency on draft loop LOOP-207 and missing input License-Allowlist.md.
- **1.0** — 2026-06-27 — Principal AI Engineering Architect — Initial Active version.

