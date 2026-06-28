---
# PROVENANCE METADATA
Original Path: docs/loops/shared/risk-controls.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Risk assessment matrix and safety control protocols.
Generalized Purpose: Risk assessment matrix and safety control protocols.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Risk Controls — Canonical Reference

**Version:** 1.0
**Status:** Active
**Type:** Reference Document
**Authority:** Principal AI Engineering Architect
**Applies To:** All LOOP-XXX documents in `docs/loops/`

---

## Purpose

This document is the mandatory risk assessment framework that every AEOS loop must apply. Each of the 8 risk categories defined here must be either assessed with a declared likelihood, impact, controls, detection signals, and response — or explicitly declared Not Applicable with a written rationale. A loop's Risks section that omits a category without a Not Applicable rationale is a conformance failure.

The goal of this framework is not to prevent all risk. It is to ensure that every loop author has consciously evaluated each risk category and has declared how the loop detects, controls, and responds to the risks that apply.

---

## Risk Severity Scale

Before the risk categories, this section defines the severity scale used in all AEOS risk assessments. The scale is defined in SPEC-001 §10.C2.

| Severity | Threshold | Examples |
|---|---|---|
| **Critical** | Any occurrence produces: loss of data, a security breach, or a production incident | Tenant data exposed to wrong tenant; production deployment of corrupt artifact; secrets written to public repository |
| **High** | Occurs in >20% of runs; produces: rework, significant schedule delay, or stakeholder escalation | Architecture discovery misses a key module in 1 of 5 runs; implementation overwrites files not in scope |
| **Medium** | Occurs in 5–20% of runs; produces: detectable deviation from expected output, gate trigger | Scope expands slightly beyond declared boundaries; verification criterion requires manual follow-up |
| **Low** | Occurs in <5% of runs; produces: minor deviation, documentation gap, or negligible delay | STATUS file missing one optional field; minor naming inconsistency in an output file |

---

## The 8 Mandatory Risk Categories

### RISK-1 — Scope Creep

**Definition:** The loop performs work beyond its declared Scope section, either by examining files it was not asked to examine, modifying artifacts it was not asked to modify, or producing outputs not declared in its Outputs section.

**Typical trigger conditions:**
- An upstream artifact contains more content than expected, causing the agent to recursively expand its analysis.
- The agent interprets an ambiguous input as permission to address related but out-of-scope problems.
- A step's output is larger than anticipated, causing the agent to add additional steps to "complete" the work.

**Standard control mechanisms:**
- The loop's Scope section explicitly lists what the loop does NOT do.
- Each workflow step declares its exact output artifacts; any file not in the declaration is unauthorized.
- VER criteria include a `git diff` check confirming only declared files were modified.

**Detection signals:**
- `git diff --name-only` shows files not in the loop's declared Outputs.
- Run duration significantly exceeds the expected range.
- Step count exceeds the declared `run.steps_total` metric.

**Standard response:**
- Halt the current step.
- Roll back any unauthorized modifications.
- Invoke GATE-1 (see Escalation Thresholds below).
- Record the scope creep as a governance event (SPEC-006 GE-05).

---

### RISK-2 — Architectural Drift

**Definition:** The loop's outputs are technically correct but architecturally inconsistent with the existing system — introducing patterns, dependencies, or structures that conflict with decisions recorded in ADRs or the module catalog.

**Typical trigger conditions:**
- The agent selects an implementation approach without consulting existing architectural records.
- A new dependency is introduced that duplicates or conflicts with an existing approved dependency.
- A module boundary is violated because the agent resolved an ambiguity without escalating.

**Standard control mechanisms:**
- LOOP-001 (Architecture Discovery) must be upstream of any loop that produces source artifacts.
- The loop's Required Context section must include the ADR index and module catalog.
- Verification criteria include a check that no new external dependencies were introduced without approval.

**Detection signals:**
- New `import` or dependency declaration in an output file that is not in the approved dependency list.
- A module in a declared output that crosses a boundary defined in the module catalog.
- A Checker agent finding that references an architectural inconsistency.

**Standard response:**
- Flag the inconsistency in the Reflection artifact.
- Do not merge or publish the affected artifact.
- Escalate to the Owner for architectural decision before proceeding.

---

### RISK-3 — Hidden Dependencies

**Definition:** The loop's outputs depend on runtime behavior, configuration, or external state that was not declared in the loop's Dependencies, External State, or Required Context sections.

**Typical trigger conditions:**
- An agent resolves a path or identifier by querying a live system rather than using a declared artifact.
- An output file contains hard-coded values derived from the current environment (e.g., port numbers, host names) that will differ in other environments.
- A tool call produces a result that depends on the current authenticated user's permissions, which differ from the permissions in the target environment.

**Standard control mechanisms:**
- All external reads must be declared in the External State section before the loop is set to Active.
- Verification criteria include checks that output files contain no environment-specific values unless the loop's Scope explicitly permits them.
- The Checker agent runs in a sandboxed environment to surface hidden dependencies.

**Detection signals:**
- An output artifact references a path, host, or credential that is not in the declared artifact set.
- The loop fails on re-run with identical declared inputs but different environmental state.

**Standard response:**
- Record the undeclared dependency in the Reflection artifact.
- Update the loop's External State section to declare the dependency (triggers a MINOR version increment).
- Re-run the loop with the dependency now declared before accepting the output.

---

### RISK-4 — Tenant Isolation Breach

**Definition:** The loop reads, writes, or processes data belonging to a tenant it was not invoked for, or produces outputs that co-mingle data from multiple tenants.

**Typical trigger conditions:**
- A database query is missing a tenant-scoping clause.
- A file path or artifact name includes a tenant identifier derived from user input without sanitization.
- The loop processes a shared queue and does not filter by the invoking tenant's ID.

**Standard control mechanisms:**
- The loop's Inputs section must declare the invoking tenant ID as a required input.
- Every external read and write in the External State section must declare the tenant scope.
- Verification criteria include a check that no output artifact contains data from a tenant other than the invoking tenant.

**Detection signals:**
- An output artifact contains records with a tenant ID different from the invoking tenant.
- A database query log shows rows returned from multiple tenant partitions.
- A security boundary check reports cross-tenant data access.

**Standard response:**
- **Immediate Emergency Stop.** This is the only risk category that mandates an unconditional Emergency Stop as the first response.
- Security incident notification to the Framework Owner and the affected tenants.
- Preserve all artifacts from the run as evidence.
- Do not retry the loop until the root cause is identified and the External State declarations are corrected.

---

### RISK-5 — Data Loss or Corruption

**Definition:** The loop deletes, truncates, or corrupts data that existed before the run began and that the loop was not authorized to modify.

**Typical trigger conditions:**
- A write operation to an external system uses an overwrite mode rather than an append mode, destroying pre-existing data.
- A file rename or move operation is interrupted mid-execution, leaving the file in an unrecoverable state.
- A database transaction is rolled back incorrectly, leaving the database in a state inconsistent with both the pre-run and post-run intent.

**Standard control mechanisms:**
- All external write operations must declare a rollback strategy in the External State section.
- Before any destructive write, the loop takes a checkpoint: a snapshot of the current state sufficient to restore it.
- Verification criteria include confirmation that pre-existing data not in scope is unchanged.

**Detection signals:**
- A file that existed before the run is absent or smaller after the run.
- A database record count is lower after the run than before.
- An integrity check on a written artifact fails.

**Standard response:**
- Halt immediately.
- Attempt rollback using the declared rollback strategy.
- If rollback fails or is partial, produce an incident record and escalate to the Owner.
- Record the data loss event in the governance event log.

---

### RISK-6 — Non-Idempotent External Write

**Definition:** A loop step that writes to external state produces a different result on re-run (e.g., duplicate records, double-applied patches), because the write is not idempotent.

**Typical trigger conditions:**
- An append-mode write is executed twice because the loop was interrupted and restarted from before the write step.
- A counter is incremented on each run rather than being set to a computed value.
- A queue message is published by the loop but the queue does not de-duplicate messages.

**Standard control mechanisms:**
- Every external write operation must be designed for idempotency: running it twice with the same inputs must produce the same result as running it once.
- The loop's External State section must declare the idempotency strategy for each write.
- Verification criteria include a re-run-safety check confirming that the output is the same on second invocation.

**Detection signals:**
- On re-run, an external system reports a duplicate key error, conflict, or constraint violation.
- A count of external records is higher than expected after a second run.

**Standard response:**
- Document the non-idempotency in the Reflection artifact.
- Update the loop's External State section with a de-duplication or upsert strategy.
- Remove the duplicate records from the external system if detectable.

---

### RISK-7 — Security Boundary Violation

**Definition:** The loop reads from, writes to, or invokes operations across a security boundary it was not authorized to cross — including but not limited to: accessing secrets without declared authorization, calling privileged APIs, or writing to systems outside the declared scope.

**Typical trigger conditions:**
- An agent uses a credential that grants broader access than the loop's declared External State scope.
- A tool invocation triggers an authenticated operation on a system not listed in the loop's External State section.
- An output artifact contains a secret or credential that should not be present in a file.

**Standard control mechanisms:**
- The loop's Required Context section must declare every secret and credential required; any undeclared credential use is a security boundary violation.
- Tool invocations that require authentication must be declared in the External State section with the scope of access.
- Verification criteria include a secrets-pattern scan of all output artifacts.

**Detection signals:**
- A secrets scanner finds a match in an output file.
- An authentication audit log shows a credential use not correlated with a declared External State entry.
- A downstream system reports unauthorized access from the loop's agent identity.

**Standard response:**
- **Immediate Emergency Stop.**
- Security incident notification to the Framework Owner.
- Revoke the credential if it was exposed.
- Do not retry until the root cause is identified and the loop's security declarations are corrected.

---

### RISK-8 — Runaway Execution

**Definition:** A loop run executes for longer than expected, consuming resources indefinitely or until externally terminated, without producing the expected outputs or triggering a graceful stop condition.

**Typical trigger conditions:**
- An agent enters a retry loop without a maximum retry count.
- A recursive analysis expands beyond the declared scope without a depth limit.
- A step that depends on an external system response waits indefinitely because the external system is unavailable.

**Standard control mechanisms:**
- Every loop must declare a `max_duration` in its Scope section. If a run exceeds this duration, the loop must halt and apply its Stop Condition for timeout.
- Retry logic in any step must declare a maximum retry count and a backoff strategy.
- External system dependencies must declare a timeout after which the step fails and the Failure Recovery section governs.

**Detection signals:**
- `run.duration_seconds` exceeds the loop's declared `max_duration`.
- A step has been executing for longer than its declared step timeout.
- The monitoring agent has not received a heartbeat from the executing agent within the expected interval.

**Standard response:**
- Halt execution at the end of the current atomic unit of work.
- Preserve all partial outputs produced before the halt.
- Write a partial Reflection artifact recording the step at which the halt occurred.
- Record the runaway as a Stop Condition (not a failure) in the STATUS file, unless outputs were corrupted, in which case it is a failure.

---

## Rollback Control Standards

Any loop step that writes to external state must declare a rollback action. Rollback actions are subject to the following standards:

### Atomicity Requirement

A rollback action must be **atomic**: either the rollback fully succeeds and restores the system to its pre-write state, or the rollback fails and the system is left in its pre-write state with no partial application. A rollback that partially succeeds is a partial rollback, which is prohibited.

If an atomic rollback is not achievable for a given write operation, the loop must either:

1. Redesign the write to be atomic (preferred), or
2. Declare the write as irreversible and place a Hard Gate before it, with explicit documentation that rollback is not possible.

### Partial Rollback Response

If a rollback cannot be completed atomically (partial rollback detected), the following response is mandatory:

1. Halt all further rollback attempts.
2. Record a rollback incident in the STATUS file's `open_blockers` section.
3. Escalate to the Owner immediately.
4. Produce an incident record in the governance event log.

---

## Escalation Thresholds

The following risk categories trigger mandatory escalation actions when detected:

| Risk | Detected Condition | Required Immediate Action |
|---|---|---|
| RISK-1 (Scope Creep) | Unauthorized file modification detected | Invoke GATE-1; halt current step |
| RISK-4 (Tenant Isolation Breach) | Cross-tenant data access confirmed or suspected | Immediate Emergency Stop + security incident notification |
| RISK-7 (Security Boundary Violation) | Unauthorized credential use or secret exposure | Immediate Emergency Stop + security incident notification |
| RISK-8 (Runaway Execution) | Run duration exceeds `max_duration` | Halt; preserve partial outputs; write partial Reflection |

RISK-2, RISK-3, RISK-5, and RISK-6 do not mandate an Emergency Stop but do require the loop to halt the affected step, apply rollback where applicable, and escalate to the Owner before proceeding.

---

## References

- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §10 — Risk assessment contract
- `docs/loops/shared/LOOP-STANDARD.md` §19 — Risks section requirements
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol
- `docs/loops/shared/SPEC-006-Governance.md` — Governance event recording for risk incidents

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

