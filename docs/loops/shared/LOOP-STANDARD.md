---
# PROVENANCE METADATA
Original Path: docs/loops/shared/LOOP-STANDARD.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Define canonical engineering standard for RajaJeevanLoopEngineering execution loops.
Generalized Purpose: Define canonical engineering standard for execution loops.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-XXX, LOOP-YYY  (or "None")
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-STANDARD — Loop Engineering Specification

**Version:** 1.0  
**Status:** Active  
**Authority:** Principal AI Engineering Architect  
**Applies To:** All `LOOP-XXX` documents in `docs/loops/`

---

## 1. Purpose

This document is the canonical engineering standard for all loop specifications in this repository. It defines the interface that every `LOOP-XXX` document must implement.

A loop specification is a contract. It declares what a unit of AI-assisted engineering work does, what it requires, what it produces, how it is verified, and where humans must intervene. Any agent or engineer executing a loop must be able to derive their complete obligations from the loop document and this standard alone.

Conformance is not optional. A loop that does not satisfy this standard is not eligible for `Active` status.

---

## 2. Terminology

| Term | Definition |
|------|------------|
| **Loop** | A named, repeatable unit of AI-assisted engineering work with declared inputs, outputs, verification criteria, and human oversight gates. |
| **Loop Document** | A Markdown file in `docs/loops/` conforming to this standard. File name format: `LOOP-XXX-Meaningful-Name.md`. |
| **Maker** | The agent (AI or human) that produces the primary output of a loop step. |
| **Checker** | A separate agent (AI or human) that independently verifies the Maker's output before it is accepted. The Maker and Checker must not be the same agent for any Hard Gate. |
| **Hard Gate** | A mandatory human approval point. The loop must halt and may not proceed until explicit human approval is recorded. |
| **Soft Gate** | A human notification point. The loop may proceed after a defined timeout if no objection is received. The timeout and notification channel must be declared. |
| **Emergency Stop** | A condition under which any human principal may immediately terminate a running loop regardless of its current step. |
| **Precondition** | A condition that must be true before the loop may begin execution. |
| **Postcondition** | A condition that must be true before the loop's outputs are accepted as complete. |
| **External State** | State that exists outside the repository and that the loop reads from or writes to (databases, APIs, queues, deployment targets). |
| **Artifact** | A discrete output produced by the loop (file, PR, record, report). |
| **Run** | A single execution instance of a loop from trigger to termination. |
| **Stop Condition** | A condition that causes the loop to terminate without completing, without constituting a failure requiring incident response. |
| **Failure** | A run that terminates due to an unrecoverable error, a violated postcondition, or a rejected Hard Gate. |
| **Reflection** | A structured self-assessment of a completed or failed run, recorded as an artifact. |

---

## 3. Loop Lifecycle

Every loop document must declare which lifecycle states it supports and what transitions are valid. The canonical lifecycle is:

```
Draft → Review → Active → Deprecated → Archived
```

### State Definitions

**Draft** — The document exists but is incomplete. No agent may execute a loop in Draft status. All sections must be populated before transitioning to Review.

**Review** — The document is complete and under review (human, AI, or both). The loop may not be executed in production. A review record conforming to `templates/REVIEW-TEMPLATE.md` must be produced and attached before transition to Active.

**Active** — The document has passed review and is approved for execution. The version number at time of approval must be recorded in the Version History section.

**Deprecated** — The loop has been superseded. The document must identify its replacement. Deprecated loops must not be used in new work. Existing scheduled runs must be migrated to the replacement loop within the timeline declared in the Deprecation Notice.

**Archived** — The document has been moved to `docs/loops/archive/`. It is preserved for historical reference only. No execution is permitted.

### Transition Rules

| From | To | Required |
|------|----|----------|
| Draft | Review | All required sections populated; self-review checklist passed |
| Review | Active | Review record produced; all findings resolved; human sign-off |
| Active | Deprecated | Deprecation Notice added to document; replacement loop identified |
| Deprecated | Archived | Migration of active runs confirmed; file moved to `archive/` |
| Any | Draft | Only permitted when reverting an Active loop for major revision; version must be bumped |

---

## 4. Required Sections

Every loop document must contain the following sections in this order. No section may be omitted. No section may be left empty in a loop with status Review or Active.

### 4.1 Header Block

Immediately after the H1 title, before any section, every loop document must include a metadata block:

```
**Version:** X.Y
**Status:** Draft | Review | Active | Deprecated | Archived
**Category:** Core | Engineering | Platform | Governance | Release | AI | Research | Experimental
**Depends On:** LOOP-XXX, LOOP-YYY  (or "None")
**Human Gates:** Hard | Soft | None
```

### 4.2 Required Section Order

1. `## Purpose` — One-paragraph statement of what the loop does and why it exists. Must be self-contained; a reader must understand the loop's value without reading any other section.

2. `## Problem Statement` — The specific engineering problem this loop is designed to solve. Distinct from Purpose: Purpose is what the loop does; Problem Statement is what would go wrong without it.

3. `## Why This Loop Exists` — The justification for codifying this work as a loop rather than ad-hoc execution. Must reference at least one of: repeatability, verifiability, auditability, safety, or scale.

4. `## Scope` — The explicit boundaries of the loop. What it does and what it explicitly does not do. Scope must be narrow enough that a single run can complete in a bounded time window.

5. `## Inputs` — All inputs the loop requires, with type, source, and whether the input is required or optional. Inputs must be enumerable before the loop begins.

6. `## Outputs` — All artifacts the loop produces, with type, destination, and acceptance criteria. Every output must be verifiable.

7. `## Dependencies` — Other loops that must be Active and whose outputs this loop consumes. Format: `LOOP-XXX — Name — what is consumed`.

8. `## Trigger` — The condition or event that initiates a run. Must be deterministic. Acceptable triggers: manual invocation, scheduled cron, upstream loop completion, repository event, external webhook.

9. `## Preconditions` — The conditions that must be true before execution begins. The loop must verify all preconditions at startup and halt if any are not met. Each precondition must be machine-checkable.

10. `## External State` — All state outside the repository that the loop reads from or writes to. For each: the system, the operation (read/write), the scope of access, and the rollback strategy if the loop fails mid-execution.

11. `## Required Context` — The information the executing agent must have loaded before beginning. Includes files to read, configurations to resolve, secrets to access, and environment requirements.

12. `## Agents` — The agents involved in the run. Each entry must specify: Agent ID, Role (Maker or Checker), Responsibilities, Tools available, and Human Oversight Gate assignment. Must conform to `templates/AGENTS-TEMPLATE.md`.

13. `## Workflow` — The step-by-step execution sequence. Each step must declare: step number, name, responsible agent, inputs consumed, outputs produced, and the gate (if any) that follows it. Steps must be ordered and non-ambiguous.

14. `## Verification` — The postconditions that must be true for the run to be considered successful. Each verification criterion must be independently checkable by the Checker agent without relying on the Maker's self-report.

15. `## Reflection` — The structured self-assessment that must be produced at the end of every run (including failed runs). See Section 10.

16. `## Human Approval Gates` — All Hard and Soft gates declared in the Workflow, with full specification. See Section 8.

17. `## Failure Recovery` — For each failure mode: the detection condition, the immediate action, the rollback procedure, and the escalation path.

18. `## Metrics` — The metrics collected during each run. See Section 11.

19. `## Risks` — The risks associated with executing this loop. Each risk must have: description, likelihood (High/Medium/Low), impact (High/Medium/Low), and mitigation.

20. `## Stop Conditions` — Conditions under which the loop terminates without failure and without completing. Each stop condition must specify what cleanup is performed and what state is left behind.

21. `## Deliverables` — The complete list of artifacts that a successful run must have produced before it is considered closed. Checklist format. Matches `templates/CHECKLIST-TEMPLATE.md`.

22. `## Future Improvements` — Known limitations and candidate improvements for future versions. Not a backlog; only items that would require a version bump to implement.

23. `## Version History` — See Section 6.

---

## 5. Naming Convention

### File Names

```
LOOP-XXX-Meaningful-Name.md
```

- `XXX` is a zero-padded three-digit integer within the category range (see below).
- The name is Title-Case-With-Hyphens.
- No abbreviations unless the abbreviation is the canonical name (e.g., `ADR`).
- The name must be meaningful without reading the file. `LOOP-005-Do-Things.md` is not acceptable.

### Category Ranges

| Range | Category |
|-------|----------|
| 001–099 | Core |
| 101–199 | Engineering |
| 201–299 | Platform |
| 301–399 | Governance |
| 401–499 | Release |

### Numbers

Numbers are assigned sequentially within a category. Gaps are permitted only when a loop is archived; its number is never reused.

### H1 Title Format

The H1 title inside the file must match the file name:

```markdown
# LOOP-XXX — Meaningful Name
```

The separator between the number and the name is ` — ` (space, em-dash, space).

---

## 6. Versioning

### Format

Versions follow `MAJOR.MINOR`:

```
1.0   Initial Active version
1.1   Non-breaking clarification or addition
2.0   Breaking change to inputs, outputs, agent contract, or gate structure
```

### Rules

- A new loop document begins at version `0.1` (Draft).
- The version at first Active approval is `1.0`.
- A `MINOR` bump requires a new entry in Version History and a review by at least one human or Checker agent.
- A `MAJOR` bump requires the full Review → Active transition: the loop returns to Draft, a new review is completed, and a new approval is recorded.
- The version in the header block must always match the latest entry in Version History.
- Deprecated loops retain their last Active version number. The Deprecation Notice is not a version bump.

### Version History Entry Format

Each entry in `## Version History` must follow:

```
- **X.Y** — YYYY-MM-DD — Author — Summary of change
```

---

## 7. Verification Requirements

### Principle

Every output produced by a loop must be independently verifiable. Verification is not self-attestation. A Maker agent asserting that its output is correct does not constitute verification.

### Levels

**Level 1 — Automated:** Machine-executable checks that produce a binary pass/fail result. Must be defined in the `## Verification` section as runnable assertions (test commands, lint rules, schema validators, API probes). These run without human involvement.

**Level 2 — Checker Review:** A designated Checker agent (separate from the Maker) reviews Level 1 results and the primary output. The Checker produces a written finding. If the Checker is an AI agent, a human must review the Checker's finding before any Hard Gate is cleared.

**Level 3 — Human Approval:** A named human or role explicitly approves the output. Required for all Hard Gates and for any output that modifies shared infrastructure, production data, or published interfaces.

### Requirements

- Every loop must define at least one Level 1 verification criterion.
- Any loop that writes to External State must define at least one Level 2 verification.
- Any loop with a Hard Gate must define Level 3 verification for the artifact gated by that Hard Gate.
- Verification criteria must be stated as falsifiable conditions, not aspirational statements. "The PR is correct" is not a criterion. "All CI checks pass and no test with `@TenantIsolation` annotation fails" is a criterion.

---

## 8. Human Approval Gates

### Gate Types

**Hard Gate** — The loop must halt at this point. Execution may not resume until a named human or role provides explicit written approval. Approval must be recorded in the run's STATUS document. There is no timeout on a Hard Gate.

**Soft Gate** — The loop notifies a named human or role and waits for the declared timeout. If no objection is received within the timeout, execution proceeds automatically. The notification channel, the message content, and the timeout duration must be declared in the loop document.

**Emergency Stop** — Not a gate in the workflow; a standing right of any human principal. Any human may terminate a running loop at any step by invoking the Emergency Stop protocol declared in `shared/human-oversight-gates.md`. The loop must define what cleanup it performs when an Emergency Stop is received at each step.

### Declaration Requirements

Each gate declared in `## Human Approval Gates` must specify:

- Gate ID (e.g., `GATE-1`)
- Gate type (Hard or Soft)
- Position in Workflow (step number after which it fires)
- Artifact under review (what the human is approving)
- Approver (named role or individual)
- For Soft Gates: notification channel and timeout duration
- For Hard Gates: what happens if approval is denied (rollback procedure reference)
- Audit trail requirement (where the approval record is written)

### Minimum Gate Requirements

| Loop Category | Minimum Gates Required |
|---------------|----------------------|
| Core | At least one Soft Gate |
| Engineering | At least one Hard Gate before any change is merged |
| Platform | At least one Hard Gate before any write to External State |
| Governance | At least one Hard Gate before any document is published |
| Release | Hard Gate before deployment; Hard Gate before post-release close |

---

## 9. Maker / Checker Pattern

### Principle

No agent may verify its own primary output for the purposes of gate clearance. Every significant output must be produced by a Maker and independently assessed by a Checker.

### Definitions

**Maker** — The agent responsible for producing the primary artifact of a step. May be AI or human.

**Checker** — A separate agent that independently assesses the Maker's output against declared verification criteria. The Checker must not have produced or co-produced the artifact under review. May be AI or human; for Hard Gate clearance, the final Checker sign-off must be human.

### Requirements

- The `## Agents` section must explicitly designate the Maker and Checker for each step that produces a verifiable artifact.
- The Checker must assess against the verification criteria declared in `## Verification`, not against the Maker's intent.
- The Checker's finding must be recorded as a named artifact in the run's output.
- If the Checker is an AI agent, its finding must be reviewed by a human before it is used to clear a Hard Gate.
- A Checker finding of "pass" that is later proven incorrect is a governance event requiring a Reflection entry and a potential version bump.

### Anti-Patterns

The following patterns violate the Maker/Checker requirement and are prohibited:

- The same AI agent instance both generates and reviews code in the same run without a stateless reset between steps.
- A human who authored a change also approves it at a Hard Gate without a second human reviewer.
- A Checker that reviews only the Maker's summary of the output rather than the output itself.

---

## 10. Reflection Requirements

### Principle

Every completed or failed run must produce a Reflection. Reflection is not optional on failure; it is most important on failure.

### Reflection Artifact

The Reflection is a structured document produced at the end of every run. It must be stored in the run's output location (defined in the loop's `## Deliverables` section).

### Required Reflection Sections

Every Reflection document must contain:

1. **Run Summary** — Loop ID, Run ID, date, trigger, final status (Completed / Failed / Stopped).
2. **What Was Attempted** — The intended outcome of the run.
3. **What Happened** — What actually occurred, including deviations from the Workflow.
4. **Verification Results** — The outcome of each Level 1, 2, and 3 verification criterion.
5. **Gate Outcomes** — The outcome of each Human Approval Gate (approved, denied, timed out, not reached).
6. **Failures and Anomalies** — Any step that did not complete as specified, with root cause if known.
7. **Risk Observations** — Any risk declared in `## Risks` that materialized, and any new risk observed.
8. **Metrics** — Values for all metrics declared in `## Metrics`.
9. **Improvement Candidates** — Observations that, if acted on, would reduce risk or improve reliability. These feed into `## Future Improvements` in the next version.
10. **Decision Log** — Any significant decision made during the run that was not fully specified by the Workflow (including the rationale).

### Reflection Timing

The Reflection must be produced before the run is marked closed. A run may not be marked Completed without a Reflection. A run may not be marked Failed without a Reflection that includes root cause.

---

## 11. Metrics

### Principle

Every loop must declare the metrics it collects. Metrics exist for three purposes: detecting degradation over time, informing governance decisions, and feeding future loop improvements.

### Required Metrics (All Loops)

Every loop must collect and record the following in its Reflection:

| Metric | Definition |
|--------|------------|
| `run.duration_seconds` | Wall-clock time from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of workflow steps completed before termination |
| `run.steps_total` | Total workflow steps declared |
| `gate.hard.count` | Number of Hard Gates reached |
| `gate.hard.approved` | Number of Hard Gates approved |
| `gate.hard.denied` | Number of Hard Gates denied |
| `gate.soft.count` | Number of Soft Gates reached |
| `gate.soft.auto_proceeded` | Number of Soft Gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of Level 1 criteria that passed |
| `verification.level1.fail` | Count of Level 1 criteria that failed |
| `reflection.produced` | Boolean — was a Reflection artifact produced? |

### Additional Metrics

Each loop must declare in `## Metrics` any additional metrics relevant to its specific outputs. Examples: lines of code changed, test coverage delta, number of findings, deployment duration.

### Metric Storage

Metric values must be written to the Reflection artifact. Aggregation across runs is the responsibility of the Governance loops; individual loops are responsible only for accurate per-run recording.

---

## 12. Risk Controls

### Principle

Every loop must declare its risks and the controls that bound them. Controls are not aspirational; they must be enforceable within the loop's Workflow.

### Risk Declaration Format

Each risk in `## Risks` must follow:

```
### RISK-N — Name
- **Description:** What could go wrong.
- **Likelihood:** High | Medium | Low
- **Impact:** High | Medium | Low
- **Trigger Condition:** What causes this risk to materialize.
- **Control:** How the loop prevents or mitigates this risk.
- **Detection:** How the loop or a human would detect that this risk has materialized.
- **Response:** What happens when this risk materializes (reference Failure Recovery step).
```

### Mandatory Risk Categories

Every loop must assess and either declare a risk or explicitly note "not applicable" for each of the following categories:

- **Data loss or corruption** — Does any step write to a store that cannot be trivially rolled back?
- **Tenant isolation breach** — Could any step read or write data belonging to the wrong tenant?
- **Security boundary violation** — Could any step escalate privilege, expose credentials, or bypass an auth control?
- **Non-idempotent external write** — Does any step write to an external system in a way that cannot be safely retried?
- **Runaway execution** — Could the loop execute indefinitely if a step does not terminate?

### Runaway Execution Control

Every loop must declare a maximum run duration. If the loop has not terminated within this duration, it must stop itself and produce a Reflection with status `stopped`. The maximum run duration must be declared in the `## Scope` section.

---

## 13. External State

### Principle

External state is any state that exists outside the repository and that survives loop termination. Every access to external state introduces risk and must be explicitly declared and controlled.

### Declaration Requirements

Every system listed in `## External State` must specify:

| Field | Requirement |
|-------|-------------|
| System | Name and type of the external system |
| Operation | `read` \| `write` \| `read-write` |
| Scope | The specific resource, table, queue, or API endpoint accessed |
| Auth | How the loop authenticates to the system |
| Isolation | How the loop ensures it does not affect other tenants or environments |
| Rollback | How the effect of a write is reversed if the loop fails after the write |
| Idempotency | Whether the operation is safe to retry; if not, the guard mechanism |

### Prohibition on Undeclared External Writes

A loop must not write to any external system not declared in `## External State`. An AI agent that discovers it needs to write to an undeclared external system must halt, produce a partial Reflection, and trigger a Hard Gate for human decision.

---

## 14. Dependencies

### Principle

Dependencies between loops must be explicit. Implicit dependencies (a loop assuming another loop has run without declaring it) are prohibited.

### Declaration Format

Each dependency in `## Dependencies` must follow:

```
- **LOOP-XXX — Name:** What this loop consumes from it. Required | Optional.
```

### Rules

- A dependency may only reference a loop in `Active` status. A loop may not be approved as Active if any of its declared dependencies are in `Draft` or `Review` status.
- Circular dependencies are prohibited. The dependency graph must be a directed acyclic graph.
- If a dependency is marked Optional, the loop must declare the fallback behavior when the dependency's output is not available.
- When a depended-upon loop is deprecated, all loops that depend on it must update their dependency declaration within the timeline specified by the deprecating loop's Deprecation Notice.

---

## 15. References

### Internal References

When a loop references another document in this repository, use a relative path from the repository root:

```
docs/loops/shared/human-oversight-gates.md
docs/loops/templates/REVIEW-TEMPLATE.md
```

Do not use absolute paths. Do not use URLs for internal documents.

### External References

When a loop references an external standard, tool, or specification:

- Cite the specific version or edition referenced.
- Do not assume the external reference is stable; note if the loop's behavior depends on a specific version.
- External references must appear in a `## References` section at the end of the document, before `## Version History`.

### Self-Reference Prohibition

A loop document must not reference itself as a dependency or as a verification authority.

---

## 16. Change Log Requirements

### Principle

Every change to a loop document that affects its behavior, contract, or gate structure must be recorded. The Version History section is an audit trail, not a summary.

### Entry Requirements

Every Version History entry must include:

- Version number
- Date (ISO 8601: `YYYY-MM-DD`)
- Author (human name or agent ID)
- Nature of change: `Added` | `Changed` | `Fixed` | `Deprecated` | `Removed`
- Summary: one sentence describing what changed and why

### What Requires a Version Entry

| Change Type | Version Bump |
|-------------|-------------|
| Added a required section | MINOR |
| Changed input or output contract | MAJOR |
| Changed gate structure (add/remove/change gate type) | MAJOR |
| Changed agent assignments | MINOR |
| Corrected a factual error | MINOR |
| Changed verification criteria | MINOR if additive; MAJOR if a criterion is removed |
| Deprecated the loop | MINOR (Deprecation Notice added) |
| Changed failure recovery procedure | MINOR |
| Rewrote the document for a new major version | MAJOR |

### What Does Not Require a Version Entry

- Typographical corrections that do not change meaning
- Reformatting that does not change content
- Adding entries to `## Future Improvements`

---

## Conformance Summary

A loop document is conformant with this standard if and only if:

- [ ] The file name follows the `LOOP-XXX-Meaningful-Name.md` convention
- [ ] The H1 title matches the file name using the declared format
- [ ] The header block is present with all five fields populated
- [ ] All 23 required sections are present in the declared order
- [ ] No required section is empty (for loops in Review or Active status)
- [ ] The Maker/Checker pattern is declared in `## Agents` for every artifact-producing step
- [ ] At least one Hard Gate or Soft Gate is declared (per category minimum in Section 8)
- [ ] All five mandatory risk categories are assessed in `## Risks`
- [ ] All external state accesses are declared with all required fields in `## External State`
- [ ] All dependencies reference Active loops
- [ ] Version History contains an entry for every behavioral change
- [ ] The version in the header block matches the latest Version History entry
- [ ] A Reflection artifact requirement is declared in `## Deliverables`

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version establishing the canonical loop engineering standard.

