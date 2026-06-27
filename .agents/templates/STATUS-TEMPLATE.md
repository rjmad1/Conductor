---
# PROVENANCE METADATA
Original Path: docs/loops/templates/STATUS-TEMPLATE.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Loop specification or framework asset.
Generalized Purpose: Loop specification or framework asset.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# STATUS — LOOP-XXX Name

<!-- This file is the authoritative execution record for LOOP-XXX. It must be updated at the start and end of each workflow step (SPEC-001 §5.C1). It must contain all fields in §5.C2 in a structured, machine-readable format. Replace XXX with the loop number and Name with the loop name. -->

```yaml
loop_id: "LOOP-XXX"
current_status: "idle"
# Valid values: idle | running | completed | failed | stopped | precondition_failed | blocked_on_dependency | emergency_stopped

current_run_id: null
# Format: LOOP-XXX-YYYYMMDD-NNN (e.g., LOOP-001-20260627-001). Null when idle.

current_task_id: null
# The task ID being processed in the current run. Null when idle.

last_updated: "YYYY-MM-DDTHH:MM:SSZ"
# ISO 8601 timestamp of the last write to this file.

last_completed_run: null
# Run ID of the most recently completed run (any terminal state).

last_outcome: null
# Outcome of the most recently completed run: completed | failed | stopped | precondition_failed

open_blockers: []
# List of current blockers. Empty list if none.
# Example:
# - id: "BLK-001"
#   description: "GATE-1 awaiting approval from Principal Engineer"
#   opened_at: "2026-06-27T10:00:00Z"

gate_outcomes: {}
# Record of all gate outcomes from the current run.
# Example:
# GATE-1:
#   gate_type: "Hard"
#   trigger_condition: "Significant structural change detected"
#   reviewer: "Principal Engineer"
#   reviewer_role: "Principal Engineer or Architecture Owner"
#   decision: "approved"
#   rationale: "Drift report reviewed; findings are accurate."
#   timestamp: "2026-06-27T14:30:00Z"

emergency_stopped: false
# Set to true by a human principal to terminate the running loop at any step.
# The executing agent reads this field at the start of each step.
```

---

## Run History

| Run ID | Date | Trigger | Outcome | Steps Completed | Gate Outcomes | Notes |
|--------|------|---------|---------|-----------------|---------------|-------|
| _(none yet)_ | — | — | — | — | — | — |

---

## Open Issues

<!-- Issues requiring human resolution before the next run can succeed. -->

| ID | Description | Opened | Assigned To | Resolution |
|----|-------------|--------|-------------|------------|
| _(none)_ | — | — | — | — |

---

## Skill Observations Pending

<!-- Observations from recent runs that should be transferred to SKILL-XXX.md at the next maintenance cycle. -->

_(none pending)_

