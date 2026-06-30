---
# PROVENANCE METADATA
Original Path: docs/loops/release/LOOP-403-Post-Release-Verification.md
Original Version: 0.1
Extraction Date: 2026-06-27
Original Purpose: Release loop to check post-deployment live systems.
Generalized Purpose: Release loop to check post-deployment live systems.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-402 — Deployment Validation
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-403 — Post-Release Verification

**Loop ID:** LOOP-403
**Name:** Post-Release Verification
**Version:** 0.1
**Status:** Draft
**Category:** Release
**Depends On:** LOOP-402 — Deployment Validation
**Human Gates:** Hard, Soft
**Owner:** Principal Engineering Function
**Maintainer:** Principal Engineering Function

---


---

## Purpose

To establish a standardized procedure and workflow for Post-Release Verification within the Release lifecycle, ensuring consistency and reliability across the platform.
## Problem Statement

Without a dedicated Post-Release Verification loop, teams often face ad-hoc execution, leading to fragmentation, potential regressions, and operational overhead during Release activities.
## Why This Loop Exists

This loop abstracts the complexity of Post-Release Verification into a repeatable, automated pipeline, minimizing human error and standardizing the process across all services.
## Scope

Covers all primary operations related to Post-Release Verification. Out of scope: specialized, off-band procedures not part of the standard Release workflows.
## Inputs

- Initial context regarding Post-Release Verification
- Relevant source files and configurations
- Environmental constraints for Release
## Outputs

- Executed Post-Release Verification changes
- Validation reports
- Documentation updates
## Dependencies

- External services required for Release
- Prior state validation
## Trigger

Triggered manually by an engineer or automatically via a scheduled Release orchestration event.
## Preconditions

- System is in a stable, known state.
- Approvals and access controls for Post-Release Verification are validated.
## External State

- Version control systems
- CI/CD pipelines
- Observability and telemetry dashboards
## Required Context

- Current architecture baseline
- Task-specific constraints for Post-Release Verification
## Agents

- Principal Engineering Agent
- Specialized Release Agents

**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

1. **Initialization**: Gather context for Post-Release Verification.
2. **Analysis**: Evaluate current state and plan actions.
3. **Execution**: Perform the core Post-Release Verification tasks.
4. **Validation**: Verify success criteria.
5. **Finalization**: Commit changes and output reports.

**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

- Automated testing specific to Post-Release Verification.
- Manual sanity checks if required by human gates.

**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

- Agent logs the outcome of the Post-Release Verification process.
- Metrics related to Release efficiency are recorded.
## Human Approval Gates

- Pre-execution authorization (if destructive)
- Post-execution review of Post-Release Verification impact
## Failure Recovery

- Automatic rollback of changes.
- Alerting to the on-call engineer with context of the failure in Post-Release Verification.
## Metrics

- Time to complete Post-Release Verification
- Success/Failure rate
- Number of manual interventions
## Risks

- Unintended side-effects on interdependent Release systems.
- Timeouts during extensive Post-Release Verification operations.
## Stop Conditions

- Critical errors encountered during Post-Release Verification.
- Maximum retry limit reached.
## Deliverables

- A complete and validated state post-Post-Release Verification.
- Audit trails of the operation.

**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- Enhanced automation for edge cases in Post-Release Verification.
- Tighter integration with downstream Release tools.
## Version History

- **0.1**: Initial generation of the Post-Release Verification loop.