---
# PROVENANCE METADATA
Original Path: docs/loops/shared/human-oversight-gates.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Human gates and approval workflows specification.
Generalized Purpose: Human gates and approval workflows specification.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Human Oversight Gates — Canonical Reference

**Version:** 1.0
**Status:** Active
**Type:** Reference Document
**Authority:** Principal AI Engineering Architect
**Applies To:** All LOOP-XXX documents in `docs/loops/`

---

## Purpose

This document is the canonical reference for all human oversight gate types used in AEOS loop specifications. Every loop's Human Approval Gates section, and LOOP-STANDARD §8, defer to this document for authoritative definitions. Loop specifications may impose additional restrictions beyond what is defined here; they may not relax the definitions in this document.

A human oversight gate is a declared point in a loop's workflow at which the loop must pause, notify a human principal, and either wait for explicit approval or proceed under defined conditions. Gates are not obstacles; they are the mechanism by which humans remain in control of consequential AI-assisted engineering actions.

---

## Gate Types

### Hard Gate (GATE-1)

A **Hard Gate** is a mandatory human halt. The loop must stop execution at the gate position and may not proceed under any circumstances until a named human principal provides explicit written approval.

**Defining characteristics:**

- No timeout. The loop waits indefinitely until approval is granted or the run is terminated.
- Approval must be written and explicit. A verbal approval, an implicit approval, or an approval by an AI agent does not satisfy a Hard Gate.
- The approver must be a human. An AI acting on behalf of a human does not satisfy a Hard Gate.
- The approval must be recorded in the STATUS file's `gate_outcomes` block before the loop advances past the gate position.
- Denial of a Hard Gate terminates the current run. A denied run is a failure (not a stop condition). The Failure Recovery section of the loop governs what happens next.
- Hard Gates have no bypass conditions. There is no circumstance under which a Hard Gate is treated as satisfied without human approval.

**When to use:**

Hard Gates must be placed before any step that makes irreversible or high-consequence changes: writing to production systems, publishing interfaces, merging to the main branch, deploying to a production environment, or modifying shared infrastructure.

**Required declaration fields in a loop's Human Approval Gates section:**

- Gate ID (e.g., GATE-1)
- Gate type: Hard
- Position: which workflow step precedes the gate
- Trigger condition: what state must be true for the gate to fire
- Approver role: which human role is authorized to approve
- Denial consequence: what the loop does if approval is denied

---

### Soft Gate (GATE-2)

A **Soft Gate** is a human notification point. The loop notifies a designated human principal and waits for a defined timeout period. If no objection is received before the timeout expires, the loop proceeds automatically.

**Defining characteristics:**

- A timeout must be declared. A Soft Gate without a declared timeout is a conformance failure.
- A notification channel must be declared. The loop must send a notification to the declared channel before starting the timeout.
- An auto-proceed action must be declared. When the timeout expires with no response, the loop takes the declared auto-proceed action and continues.
- An audit trail must be produced. The loop records the notification timestamp, the timeout period, the response received (or lack of response), and the action taken.
- A human principal may object during the timeout period. An objection before timeout expiry is treated as a Hard Gate denial: the run is terminated and the Failure Recovery section governs next steps.

**When to use:**

Soft Gates are appropriate for steps where human awareness is required but where the risk of proceeding without explicit approval is acceptable given the timeout period. Examples: sending a notification to a stakeholder, committing documentation changes to a feature branch, publishing a draft report.

**Required declaration fields:**

- Gate ID (e.g., GATE-2)
- Gate type: Soft
- Position: which workflow step precedes the gate
- Trigger condition: what state must be true for the gate to fire
- Notification channel: where the notification is sent
- Timeout duration: how long the loop waits (e.g., "4 hours", "1 business day")
- Auto-proceed action: what the loop does when the timeout expires with no response
- Objection handling: what the loop does if an objection is received before timeout

---

### Emergency Stop

An **Emergency Stop** is the standing right of any human principal to terminate any running loop at any step, at any time, without providing a reason. Emergency Stop is not a gate in the workflow sense; it is an override mechanism available continuously throughout every loop execution.

**Defining characteristics:**

- Any human principal may invoke an Emergency Stop. No role restriction applies. No justification is required at the time of invocation.
- Emergency Stop takes effect at the boundary of the current step. The executing agent completes any atomic write it has already begun (to avoid partial writes), then halts.
- Emergency Stop cannot be bypassed, scheduled around, or timed out. It is an unconditional halt.
- The agent must check for Emergency Stop status at the start of each step by reading the STATUS-NNN.md file.
- After an Emergency Stop, the agent applies the current step's declared rollback action and writes a partial Reflection artifact before any further action.

---

## Standard Gate Positions

While gate placement is determined by each loop's specific workflow, the following positions are the recommended placement for each gate type based on the risk profile of typical engineering steps:

| Gate Type | Recommended Position | Rationale |
|---|---|---|
| Hard Gate | Before documentation write to main branch | Documentation merged to main is visible to all consumers |
| Hard Gate | Before any source modification that touches shared interfaces | Interface changes have transitive downstream impact |
| Hard Gate | Before deployment to production environment | Production changes are live and potentially irreversible |
| Hard Gate | Before post-release close (marking a release complete) | Closing a release is a permanent governance record |
| Soft Gate | Before sending external notifications or reports | Notification content should be reviewed but sends are low-risk |
| Soft Gate | Before committing to feature branches | Branch commits are reversible; awareness is appropriate |
| Soft Gate | Before publishing draft artifacts to shared workspaces | Drafts are iterative; notification without hard block is appropriate |

Individual loops may place gates at any step in their workflow. The above are recommendations, not requirements. Gates must be placed wherever the loop's risk assessment identifies that human awareness or approval is warranted.

---

## Gate Bypass Conditions

### Soft Gate Bypass

A Soft Gate is considered satisfied (bypassed) under exactly one condition: the declared timeout period expires with no objection received. When this occurs:

1. The loop records the timeout expiry as the gate outcome (auto-proceed).
2. The loop records the notification timestamp, the timeout period, and the expiry timestamp in the STATUS file's `gate_outcomes` block.
3. The loop advances to the auto-proceed action declared in the gate's specification.

No other bypass condition is valid for Soft Gates. A Soft Gate whose notification was not sent (e.g., due to a channel outage) may not be bypassed; the loop must halt and report the notification failure.

### Hard Gate Bypass

**Hard Gates have no bypass conditions.** A Hard Gate may only be satisfied by explicit written human approval. If the approver is unavailable, the run waits. If the run must be abandoned, the run is terminated as a failure.

### Emergency Stop Bypass

**Emergency Stops cannot be bypassed.** Once an Emergency Stop is set in the STATUS file, the agent must halt regardless of loop state, run criticality, or time pressure.

---

## Audit Requirements

Every gate outcome must be recorded in the STATUS file's `gate_outcomes` block. Gate records are permanent and may not be expunged, amended, or overwritten.

### Required Fields for Each Gate Record

```yaml
gate_outcomes:
  - gate_id: "GATE-1"
    gate_type: "Hard"
    trigger_condition: "Architecture diagram produced and verified by VERIF-AGENT"
    reviewer_identity: "Jane Smith"
    reviewer_role: "Principal Architecture Function"
    decision: "approved"           # approved | denied | auto-proceeded | emergency_stopped
    rationale: ""                  # Required for denials; recommended for approvals
    timestamp: "2026-06-27T14:32:00Z"
```

### Audit Fields Reference

| Field | Required For | Description |
|---|---|---|
| `gate_id` | All outcomes | The gate identifier as declared in the loop specification |
| `gate_type` | All outcomes | Hard, Soft, or EmergencyStop |
| `trigger_condition` | All outcomes | The condition that caused the gate to fire |
| `reviewer_identity` | Hard Gate approvals/denials | The name or system ID of the reviewing human |
| `reviewer_role` | Hard Gate approvals/denials | The role that authorizes this reviewer to approve |
| `decision` | All outcomes | One of: approved, denied, auto-proceeded, emergency_stopped |
| `rationale` | Denials (required); Approvals (recommended) | The documented reason for the decision |
| `timestamp` | All outcomes | ISO 8601 timestamp of the gate decision |

### Audit Permanence

Gate records, once written, may not be deleted or modified in place. If a gate record contains an error (e.g., wrong timestamp due to clock drift), a correction record must be appended to the STATUS file referencing the original record by gate_id and timestamp. The original erroneous record is preserved alongside the correction.

---

## Emergency Stop Protocol

The Emergency Stop protocol is a step-by-step procedure that every agent in the AEOS framework must follow when an Emergency Stop is detected.

### Triggering an Emergency Stop (Human Action)

A human principal triggers an Emergency Stop by setting `emergency_stopped: true` in the loop's STATUS-NNN.md file. No other mechanism is required; the STATUS file is the control plane. The human principal may trigger an Emergency Stop by:

1. Directly editing the STATUS file.
2. Issuing a command to the executing agent (if the agent is interactive) that causes it to set `emergency_stopped: true`.

### Agent Response to Emergency Stop

The executing agent must check for Emergency Stop status at the **start of each step** by reading the STATUS-NNN.md file before beginning step execution. If `emergency_stopped: true` is found:

1. **Do not begin the step.** The agent must not execute any action associated with the pending step.
2. **Apply the current step's declared rollback action.** If the previous step produced any external state that the current step was about to build on, the rollback action for the previous step must be executed.
3. **Write a partial Reflection artifact.** The agent must write a `REFLECTION-NNN-{run-id}.md` file recording:
   - The step number at which the Emergency Stop was received.
   - The current HEAD commit SHA.
   - A summary of steps completed before the stop.
   - Any external state changes made before the stop and the rollback actions applied.
   - The metric values collected up to the point of the stop.
4. **Update the STATUS file.** Set `status: emergency_stopped` and record the Emergency Stop as a gate outcome with `gate_type: EmergencyStop` and `decision: emergency_stopped`.
5. **Halt.** Take no further action.

### Human Principal Post-Stop Responsibilities

After an Emergency Stop is confirmed (agent has halted and STATUS file reflects `emergency_stopped`), the human principal who triggered the stop must:

1. Document the reason for the Emergency Stop in the STATUS file's `open_blockers` section.
2. Assess whether any external state changes made before the stop require remediation.
3. Decide whether the run should be restarted from the beginning, restarted from a checkpoint, or abandoned permanently.
4. Record the Emergency Stop as a governance event (SPEC-006 §4, event GE-02).

---

## Gate Interaction Rules

When multiple gates would fire simultaneously at the same step boundary, the following priority rules apply:

1. **Emergency Stop supersedes all gates.** If an Emergency Stop is set in the STATUS file, it fires regardless of any gate that would otherwise be evaluated at the same step.
2. **Hard Gate (GATE-1) supersedes Soft Gate (GATE-2).** If both would fire at the same step boundary, only the Hard Gate fires. The Soft Gate notification is sent as part of the Hard Gate notification, and the Hard Gate's approval satisfies both.
3. **Multiple Soft Gates at the same step boundary** fire together. Notifications for all are sent simultaneously. The timeout for all begins at the same moment. An objection to any one of the simultaneous Soft Gates halts the step.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` §8 — Gate declaration requirements in loop specifications
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §5 — Human oversight contract
- `docs/loops/shared/SPEC-006-Governance.md` — Governance event recording for gate outcomes
- `docs/loops/shared/verification-standards.md` — Verification level requirements that interact with gate clearing

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

