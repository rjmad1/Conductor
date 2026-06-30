---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/LOOP-105-Code-Review.md
Original Version: 0.1
Extraction Date: 2026-06-27
Original Purpose: Standard loop for reviewing PR code.
Generalized Purpose: Standard loop for reviewing PR code.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-105 — Code Review

**Loop ID:** LOOP-105
**Name:** Code Review
**Version:** 0.1
**Status:** Draft
**Category:** Engineering
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---


---

## Purpose

To establish a standardized procedure and workflow for Code Review within the Engineering lifecycle, ensuring consistency and reliability across the platform.
## Problem Statement

Without a dedicated Code Review loop, teams often face ad-hoc execution, leading to fragmentation, potential regressions, and operational overhead during Engineering activities.
## Why This Loop Exists

This loop abstracts the complexity of Code Review into a repeatable, automated pipeline, minimizing human error and standardizing the process across all services.
## Scope

Covers all primary operations related to Code Review. Out of scope: specialized, off-band procedures not part of the standard Engineering workflows.
## Inputs

- Initial context regarding Code Review
- Relevant source files and configurations
- Environmental constraints for Engineering
## Outputs

- Executed Code Review changes
- Validation reports
- Documentation updates
## Dependencies

- External services required for Engineering
- Prior state validation
## Trigger

Triggered manually by an engineer or automatically via a scheduled Engineering orchestration event.
## Preconditions

- System is in a stable, known state.
- Approvals and access controls for Code Review are validated.
## External State

- Version control systems
- CI/CD pipelines
- Observability and telemetry dashboards
## Required Context

- Current architecture baseline
- Task-specific constraints for Code Review
## Agents

- Principal Engineering Agent
- Specialized Engineering Agents

**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

1. **Initialization**: Gather context for Code Review.
2. **Analysis**: Evaluate current state and plan actions.
3. **Execution**: Perform the core Code Review tasks.
4. **Validation**: Verify success criteria.
5. **Finalization**: Commit changes and output reports.

**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

- Automated testing specific to Code Review.
- Manual sanity checks if required by human gates.

**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

- Agent logs the outcome of the Code Review process.
- Metrics related to Engineering efficiency are recorded.
## Human Approval Gates

- Pre-execution authorization (if destructive)
- Post-execution review of Code Review impact
## Failure Recovery

- Automatic rollback of changes.
- Alerting to the on-call engineer with context of the failure in Code Review.
## Metrics

- Time to complete Code Review
- Success/Failure rate
- Number of manual interventions
## Risks

- Unintended side-effects on interdependent Engineering systems.
- Timeouts during extensive Code Review operations.
## Stop Conditions

- Critical errors encountered during Code Review.
- Maximum retry limit reached.
## Deliverables

- A complete and validated state post-Code Review.
- Audit trails of the operation.

**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- Enhanced automation for edge cases in Code Review.
- Tighter integration with downstream Engineering tools.
## Version History

- **0.1**: Initial generation of the Code Review loop.