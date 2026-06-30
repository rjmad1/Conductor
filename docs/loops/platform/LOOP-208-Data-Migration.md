---
# PROVENANCE METADATA
Original Path: docs/loops/platform/LOOP-208-Data-Migration.md
Original Version: 0.1
Extraction Date: 2026-06-27
Original Purpose: Validates schema mapping, data transformation, and delta sync integrity during legacy-to-cloud system transitions.
Generalized Purpose: Validates schema mapping, data transformation, and delta sync integrity during legacy-to-cloud system transitions.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-006 — Verification
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-208 — Data Migration

**Loop ID:** LOOP-208
**Name:** Data Migration
**Version:** 0.1
**Status:** Draft
**Category:** Platform
**Depends On:** LOOP-006 — Verification
**Human Gates:** Hard, Soft
**Owner:** Platform Engineering Function
**Maintainer:** Platform Engineering Function

---


---

## Purpose

To establish a standardized procedure and workflow for Data Migration within the Platform lifecycle, ensuring consistency and reliability across the platform.
## Problem Statement

Without a dedicated Data Migration loop, teams often face ad-hoc execution, leading to fragmentation, potential regressions, and operational overhead during Platform activities.
## Why This Loop Exists

This loop abstracts the complexity of Data Migration into a repeatable, automated pipeline, minimizing human error and standardizing the process across all services.
## Scope

Covers all primary operations related to Data Migration. Out of scope: specialized, off-band procedures not part of the standard Platform workflows.
## Inputs

- Initial context regarding Data Migration
- Relevant source files and configurations
- Environmental constraints for Platform
## Outputs

- Executed Data Migration changes
- Validation reports
- Documentation updates
## Dependencies

- External services required for Platform
- Prior state validation
## Trigger

Triggered manually by an engineer or automatically via a scheduled Platform orchestration event.
## Preconditions

- System is in a stable, known state.
- Approvals and access controls for Data Migration are validated.
## External State

- Version control systems
- CI/CD pipelines
- Observability and telemetry dashboards
## Required Context

- Current architecture baseline
- Task-specific constraints for Data Migration
## Agents

- Principal Engineering Agent
- Specialized Platform Agents

**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

1. **Initialization**: Gather context for Data Migration.
2. **Analysis**: Evaluate current state and plan actions.
3. **Execution**: Perform the core Data Migration tasks.
4. **Validation**: Verify success criteria.
5. **Finalization**: Commit changes and output reports.

**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

- Automated testing specific to Data Migration.
- Manual sanity checks if required by human gates.

**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

- Agent logs the outcome of the Data Migration process.
- Metrics related to Platform efficiency are recorded.
## Human Approval Gates

- Pre-execution authorization (if destructive)
- Post-execution review of Data Migration impact
## Failure Recovery

- Automatic rollback of changes.
- Alerting to the on-call engineer with context of the failure in Data Migration.
## Metrics

- Time to complete Data Migration
- Success/Failure rate
- Number of manual interventions
## Risks

- Unintended side-effects on interdependent Platform systems.
- Timeouts during extensive Data Migration operations.
## Stop Conditions

- Critical errors encountered during Data Migration.
- Maximum retry limit reached.
## Deliverables

- A complete and validated state post-Data Migration.
- Audit trails of the operation.

**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- Enhanced automation for edge cases in Data Migration.
- Tighter integration with downstream Platform tools.
## Version History

- **0.1**: Initial generation of the Data Migration loop.