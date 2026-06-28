---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-005-Metrics.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Technical metrics and indicators collected during loop execution.
Generalized Purpose: Technical metrics and indicators collected during loop execution.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-005 — Metrics

**Version:** 1.0
**Status:** Active
**Type:** Engineering Specification
**Governs:** Metric naming, storage, and aggregation for all LOOP-XXX documents
**Authority:** Principal AI Engineering Architect

---

## Purpose

This specification defines the canonical metric naming conventions, required metrics per loop, storage format, aggregation responsibilities, and reporting cadence for the AEOS framework. Any loop in the `Active` lifecycle state must conform to this specification in its Metrics section. SPEC-005 is the naming authority; metric definitions (what each metric represents) are in `docs/loops/shared/metrics-definitions.md`.

---

## §1 — Naming Convention

All AEOS metrics follow the pattern:

```
{prefix}.{metric-name}
```

- **prefix** is one of the canonical prefix groups defined in §2 and §5.
- **metric-name** is snake_case, all lowercase, using underscores as separators. No hyphens, no dots within the metric name itself.
- The full key is therefore `prefix.metric_name` — exactly one dot, separating prefix from metric name.

### §1.1 — Loop-Scoped Metric Prefixes

When a metric is specific to a single loop category, the loop abbreviation is prepended:

```
{loop-abbreviation}.{prefix}.{metric-name}
```

| Loop Category | Abbreviation | Example |
|---|---|---|
| Architecture Discovery (LOOP-001) | `arch` | `arch.run.duration_seconds` |
| Context Assembly (LOOP-002) | `ctx` | `ctx.run.steps_completed` |
| Task Discovery (LOOP-003) | `task` | `task.run.status` |
| Planning (LOOP-004) | `plan` | `plan.gate.escalations` |
| Implementation (LOOP-005) | `impl` | `impl.run.duration_seconds` |
| Verification (LOOP-006) | `verif` | `verif.verification.pass_rate` |
| Reflection (LOOP-007) | `refl` | `refl.reflection.improvement_proposals` |
| Bug Fixing (LOOP-101) | `bug` | `bug.run.steps_completed` |
| Refactoring (LOOP-102) | `refac` | `refac.run.status` |
| Test Generation (LOOP-103) | `tgen` | `tgen.run.duration_seconds` |
| Documentation (LOOP-104) | `doc` | `doc.run.status` |
| Code Review (LOOP-105) | `crev` | `crev.run.steps_completed` |
| Platform loops (LOOP-201–207) | `plat` | `plat.run.status` |
| Governance loops (LOOP-301–304) | `gov` | `gov.run.status` |
| Release loops (LOOP-401–403) | `rel` | `rel.run.steps_completed` |

Loop-specific prefixes are optional for the 12 universal required metrics (§2); frameworks may elect to include the loop abbreviation to disambiguate aggregated storage.

### §1.2 — Naming Rules

1. All characters must be ASCII lowercase letters, digits, or underscores within a name component.
2. Prefix and metric name components are separated by exactly one `.` character.
3. Metric keys must be globally unique within a run's artifact set. If two metrics would produce the same key, the loop-abbreviation prefix (§1.1) must be applied to both.
4. Boolean metrics use the value strings `true` / `false`.

---

## §2 — Universal Required Metrics

Every `Active` loop must collect and record the following 12 metrics on every run. These metrics are defined in `docs/loops/shared/metrics-definitions.md`. The prefixes are as shown; no loop-abbreviation prefix is required for these in single-loop contexts.

| # | Metric Key | Prefix Group | Value Type |
|---|---|---|---|
| 1 | `run.duration_seconds` | `run.` | float |
| 2 | `run.status` | `run.` | string enum |
| 3 | `run.steps_completed` | `run.` | integer count |
| 4 | `run.steps_total` | `run.` | integer count |
| 5 | `run.trigger` | `run.` | string enum |
| 6 | `run.agent_id` | `run.` | string |
| 7 | `gate.hard.count` | `gate.` | integer count |
| 8 | `gate.soft.count` | `gate.` | integer count |
| 9 | `gate.escalations` | `gate.` | integer count |
| 10 | `verification.criteria_total` | `verification.` | integer count |
| 11 | `verification.criteria_passed` | `verification.` | integer count |
| 12 | `reflection.improvement_proposals` | `reflection.` | integer count |

The four prefix groups (`run.`, `gate.`, `verification.`, `reflection.`) correspond to the four major phases of a loop execution. A loop must not use these prefixes for non-standard metrics without qualifying with the loop abbreviation to avoid collision.

---

## §3 — Metric Value Types

| Type | Description | Format |
|---|---|---|
| `string enum` | A value from a declared closed set | One of the declared enum values; recorded in quotes in YAML |
| `integer count` | A non-negative whole number | Integer, no decimal point |
| `float duration` | A non-negative duration or ratio | Decimal number, up to 3 decimal places |
| `boolean` | A binary flag | Literal `true` or `false` (not `yes`/`no`, not `1`/`0`) |

Metric values must never be null or omitted. If a metric is not applicable for a particular run (e.g., `gate.hard.count` for a loop with no Hard Gates), it must be recorded as `0` for counts, `0.0` for floats, or `false` for booleans.

---

## §4 — Storage Contract

### §4.1 — Primary Storage: Reflection Artifact

The Reflection artifact (`REFLECTION-NNN-{run-id}.md`) is the authoritative source for per-run metric values. Metrics are recorded in the Reflection's Metrics section as a YAML block embedded in the Markdown:

```yaml
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

### §4.2 — Secondary Storage: STATUS File

Metric values from the most recent run are written to the loop's `STATUS-NNN.md` file under a `metrics:` YAML block in the current run record. The STATUS file retains only the most recent run's metrics; historical values are in Reflection artifacts.

### §4.3 — Aggregation Responsibility

Aggregation across runs (trend computation, pass-rate calculation, failure-rate analysis) is the exclusive responsibility of Governance loops. Specifically:

- **LOOP-302 (Documentation Governance):** aggregates per-loop run metrics on a weekly cadence.
- **LOOP-303 (Compliance):** performs cross-loop trend analysis on a monthly cadence.

Individual engineering loops (LOOP-001 through LOOP-207) must not perform cross-run aggregation. They write per-run values only.

### §4.4 — Immutability

Once written to a Reflection artifact, metric values must not be altered. If a measurement error is discovered, a correction record is appended to the Reflection artifact but the original values are preserved. Correction records must state: the field corrected, the original value, the corrected value, and the reason.

---

## §5 — Cross-Cutting Runtime Metrics

The Runtime (defined in SPEC-012) publishes the following metrics regardless of which loop is executing. These are session-level metrics that aggregate across all loops within a single agent invocation session:

| Metric Key | Value Type | Description |
|---|---|---|
| `runtime.session.duration_seconds` | float | Total wall-clock time from session start to session end |
| `runtime.agent.invocations` | integer count | Number of distinct agent invocations within the session |
| `runtime.tool.invocations` | integer count | Total tool calls made by all agents in the session |
| `runtime.gate.escalations` | integer count | Total GATE-1 escalations triggered in the session |
| `runtime.emergency_stops` | integer count | Number of Emergency Stop events in the session |

Runtime metrics are written to the session-level STATUS record and are not loop-specific. A loop's Reflection artifact may reference runtime metrics from its session context but must not record them under its own metric keys.

---

## §6 — Conformance

A loop specification is conformant with SPEC-005 if and only if all of the following are true:

1. Its Metrics section declares all 12 universal required metrics from §2 with their correct prefix groups.
2. All metric keys used in the loop specification follow the snake_case naming rules in §1.2.
3. Loop-specific metrics (beyond the 12 required) use the loop abbreviation prefix from §1.1 to avoid collision.
4. The Metrics section specifies that values are written to the Reflection artifact (primary) and STATUS file (secondary).
5. The loop does not declare aggregation responsibilities (aggregation is delegated to LOOP-302 and LOOP-303).

Non-conformance with SPEC-005 is a conformance failure that blocks transition to `Active` status. The reviewing authority is the Principal Architecture Function.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` §11 — Metrics section requirements
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — Loop contract authority
- `docs/loops/shared/metrics-definitions.md` — Metric definitions and edge cases
- SPEC-012 — Runtime specification (cross-cutting runtime metrics)

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

