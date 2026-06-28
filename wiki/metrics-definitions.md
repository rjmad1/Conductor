---
# PROVENANCE METADATA
Original Path: docs/loops/shared/metrics-definitions.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Detailed definitions of all standard loop metrics.
Generalized Purpose: Detailed definitions of all standard loop metrics.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Metrics Definitions — Canonical Reference

**Version:** 1.0
**Status:** Active
**Type:** Reference Document
**Authority:** Principal AI Engineering Architect
**Applies To:** All LOOP-XXX documents in `docs/loops/`

---

## Purpose

This document provides the definitions for all standard metrics collected by AEOS loops. Metric naming conventions (prefix structure, snake_case rules, loop abbreviation prefixes) are defined in `SPEC-005-Metrics.md`. This document defines what each metric represents, the unit and value type, where in the loop workflow the metric is collected, and the edge cases that affect interpretation.

---

## Loop Execution Metrics

These 12 metrics are required in every Active loop's Metrics section. They correspond to the `run.`, `gate.`, `verification.`, and `reflection.` prefix groups defined in SPEC-005 §2.

| # | Metric Key | Definition | Unit | Value Type | Collection Point | Edge Cases |
|---|---|---|---|---|---|---|
| 1 | `run.duration_seconds` | Wall-clock time from the first step's start to the final step's completion (or to the halt point for failed/stopped runs). Does not include time spent waiting at gates. | seconds | float | End of run (or halt) | For runs that are Emergency Stopped, record duration up to the halt point. Gate wait time is excluded; record as 0.0 if the run halts before the first step begins. |
| 2 | `run.status` | The terminal state of the run. | enum string | string | End of run | Valid values: `completed` (all steps and postconditions satisfied), `failed` (unrecoverable error or denied Hard Gate), `stopped` (clean stop condition triggered), `emergency_stopped` (Emergency Stop invoked). No other values are valid. |
| 3 | `run.steps_completed` | The number of workflow steps that were fully executed (including all sub-actions) before the run terminated. | count | integer | End of run | A step that was started but not completed because of a halt counts as 0 (not completed). A step that was skipped due to a stop condition counts as 0. |
| 4 | `run.steps_total` | The total number of workflow steps declared in the loop specification for a normal (non-stopped) run. | count | integer | Start of run (fixed per specification) | This is a constant derived from the specification, not a measured value. It reflects the expected full-run step count. |
| 5 | `run.trigger` | The event or condition that initiated this run. | enum string | string | Step 1 (immediately) | Valid values: `manual` (invoked by a human directly), `scheduled` (invoked by a cron or timer), `upstream_loop` (invoked upon completion of an upstream loop), `repository_event` (invoked by a git push, PR event, etc.), `webhook` (invoked by an external webhook). |
| 6 | `run.agent_id` | The Agent ID of the primary Maker agent that executed the run. | string | string | Step 1 (immediately) | For runs with multiple Maker agents, record the Agent ID of the first Maker agent. If no Agent ID is declared, record the agent's model identifier. |
| 7 | `gate.hard.count` | The number of Hard Gates (GATE-1) that were evaluated during this run, regardless of outcome. | count | integer | Each gate evaluation | For loops with no Hard Gates, this value is 0. A gate that fires but is then Emergency Stopped before resolution counts as 1. |
| 8 | `gate.soft.count` | The number of Soft Gates (GATE-2) that were evaluated during this run, regardless of outcome. | count | integer | Each gate evaluation | Same rules as `gate.hard.count`. |
| 9 | `gate.escalations` | The number of gate evaluations (Hard or Soft) that resulted in a denial or escalation (i.e., did not result in approval or auto-proceed). | count | integer | Each gate resolution | A denied Hard Gate counts as 1. A Soft Gate objection counts as 1. An auto-proceeded Soft Gate counts as 0. An Emergency Stop at a gate counts as 1. |
| 10 | `verification.criteria_total` | The total number of VER-N criteria declared in the loop specification's Verification section. | count | integer | Start of verification phase | This is a constant derived from the specification. It should equal the count of VER- entries in the Verification section. |
| 11 | `verification.criteria_passed` | The number of VER-N criteria that passed (Level 1 automated check or Checker review confirmed passing). | count | integer | End of verification phase | Criteria that were not evaluated (because the run halted before verification) count as 0 (not passed). A criterion with no evidence record is treated as 0. |
| 12 | `reflection.improvement_proposals` | The count of distinct improvement candidates recorded in the Reflection artifact's Future Improvements section. | count | integer | End of Reflection writing | Each numbered improvement candidate counts as 1. Candidates that are duplicates of previously recorded candidates (from prior runs) still count as 1 each in this run's metric. |

---

## Quality Metrics

Quality metrics measure output quality patterns across runs. They are not collected per-step; they are derived from the 12 required metrics above by the aggregation loops (LOOP-302, LOOP-303).

| Metric Key | Definition | Unit | Aggregation Source | Notes |
|---|---|---|---|---|
| `run.verification.pass_rate` | Percentage of VER criteria that pass on first attempt, averaged across all runs in the reporting period. Computed as `mean(verification.criteria_passed / verification.criteria_total)` over the period. | percentage (0.0–100.0) | LOOP-302 weekly | A pass rate below 80% triggers a flag in the Documentation Governance report for manual review. |
| `run.gate.escalation_rate` | Percentage of runs in the reporting period that triggered at least one gate escalation (`gate.escalations > 0`). | percentage (0.0–100.0) | LOOP-302 weekly | Persistent escalation rates above 20% indicate systemic issues in the loop's pre-gate verification. |
| `run.reflection.improvement_proposals` | Mean count of improvement proposals per run, averaged across the reporting period. | count (float) | LOOP-302 weekly | An upward trend across reporting periods indicates accumulating unresolved technical debt. |
| `run.failure_rate` | Percentage of runs that terminated with `run.status: failed`. | percentage (0.0–100.0) | LOOP-302 weekly | Failure rates above 10% for any individual loop trigger a SPEC-006 maintenance review. |
| `run.completion_rate` | Percentage of runs that terminated with `run.status: completed`. | percentage (0.0–100.0) | LOOP-302 weekly | Complement of failure rate when stop conditions are excluded. |
| `run.mean_duration_seconds` | Mean `run.duration_seconds` across all completed runs in the reporting period. | seconds (float) | LOOP-302 weekly | Outliers (>2 standard deviations from mean) are flagged for runaway execution review (RISK-8). |

---

## Governance Metrics

Governance metrics measure the health of the AEOS framework itself. They are collected and reported by LOOP-303 (Compliance) on a monthly cadence.

| Metric Key | Definition | Unit | Aggregation Source | Notes |
|---|---|---|---|---|
| `framework.loops.active_count` | The total number of loops with `Status: Active` in the SPEC-010 Loop Catalog at the time of reporting. | count | LOOP-303 monthly | Includes all categories. Compared against prior month to track framework growth. |
| `framework.loops.pending_review_count` | The number of Active loops whose `last_reviewed` date in the STATUS file is more than 90 days before the reporting date. | count | LOOP-303 monthly | Values above 0 are flagged. Values above 3 are flagged as critical governance issues. |
| `framework.loops.stale_count` | The number of Active loops that have been flagged as Stale (per SPEC-006 §3.2). | count | LOOP-303 monthly | Should be 0 in a well-maintained framework. |
| `framework.skills.total_observations` | The cumulative count of distinct observations recorded across all Reflection artifacts in the reporting period. | count | LOOP-303 monthly | An observation is any finding recorded in the Reflection's Future Improvements section. |
| `framework.gate.hard.denial_rate` | Percentage of Hard Gate evaluations (across all loops) that resulted in denial, measured over the reporting period. | percentage (0.0–100.0) | LOOP-303 monthly | A rate above 15% indicates systemic issues with artifact quality reaching gates. |
| `framework.emergency_stops` | Count of Emergency Stop events across all loops in the reporting period. | count | LOOP-303 monthly | Each Emergency Stop event corresponds to a SPEC-006 GE-02 governance event record. |
| `framework.governance_events.open` | Count of governance events (any GE type) that are recorded as unresolved in the governance event log at the time of reporting. | count | LOOP-303 monthly | Open governance events must have a declared resolution owner and deadline. |

---

## Reporting Cadence

| Metric Group | Cadence | Produced By | Written To |
|---|---|---|---|
| Loop execution metrics (12 required) | Per run | Executing loop | Reflection artifact (primary); STATUS file current run record (secondary) |
| Quality metrics | Weekly | LOOP-302 (Documentation Governance) | LOOP-302 Reflection artifact; shared documentation governance report |
| Governance metrics | Monthly | LOOP-303 (Compliance) | LOOP-303 Reflection artifact; monthly compliance report |

---

## Storage and Access

### Per-Run Metrics (STATUS File Structure)

Per-run metrics are written to the STATUS file under the current run's metrics block:

```yaml
current_run:
  run_id: "LOOP-001-20260627-001"
  started: "2026-06-27T09:00:00Z"
  completed: "2026-06-27T09:47:18Z"
  metrics:
    run.duration_seconds: 47.3
    run.status: "completed"
    run.steps_completed: 8
    run.steps_total: 8
    run.trigger: "manual"
    run.agent_id: "ARCH-SCANNER"
    gate.hard.count: 1
    gate.soft.count: 0
    gate.escalations: 0
    verification.criteria_total: 10
    verification.criteria_passed: 10
    reflection.improvement_proposals: 2
```

### Reflection Artifact Metrics

The Reflection artifact includes the same metrics in a Metrics section as a YAML block embedded in Markdown. The Reflection is the primary and authoritative source; the STATUS file is a convenience copy of the most recent run.

### No External Backend Required

The AEOS framework does not require an external metrics backend (time-series database, observability platform, etc.). The STATUS files and Reflection artifacts are sufficient for framework operation. If an external metrics backend is configured for the repository, SPEC-012 governs the publication protocol, including how metrics are mapped to the external system's schema and what the authoritative source remains (the STATUS/Reflection files, not the external backend).

---

## References

- `docs/loops/shared/SPEC-005-Metrics.md` — Metric naming conventions and storage contract
- `docs/loops/shared/LOOP-STANDARD.md` §11 — Metrics section requirements in loop specifications
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §7 — Metrics contract

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version; all 12 required metrics defined |

