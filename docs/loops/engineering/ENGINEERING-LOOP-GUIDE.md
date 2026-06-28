---
# PROVENANCE METADATA
Original Path: docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Loop specification or framework asset.
Generalized Purpose: Loop specification or framework asset.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Engineering Loop Guide

**Version:** 1.0  
**Status:** Active  
**Type:** Architectural Guide  
**Governs:** All LOOP-1XX Engineering Loop specifications  
**Authority:** This guide is subordinate to SPEC-001 (Loop Contracts) and LOOP-STANDARD.md. Where this guide conflicts with either, those documents prevail.

---

## Purpose

This guide defines the common standards, philosophy, shared terminology, integration model, and design principles that govern every Engineering Loop in the AI Engineering Operating System (AEOS). Engineering Loops form the LOOP-100 series (LOOP-101 through LOOP-199), each implementing a distinct engineering capability: bug fixing, refactoring, test generation, documentation, code review, dependency management, performance optimisation, security remediation, technical debt management, code modernisation, and capabilities not yet defined.

This guide exists so that each Engineering Loop specification does not independently rediscover, restate, or inadvertently contradict the shared principles that govern all of them. It is the single authoritative source for what it means to be an Engineering Loop. A reader who thoroughly understands this guide and then reads any LOOP-1XX specification should find nothing in that specification that contradicts or is unexplained by this guide.

---

## Audience

This document is written for the following readers, in order of primary use:

**Software Engineers and Technical Leads** — working within a repository that has adopted the AEOS framework, determining which Engineering Loop applies to a given engineering task and how to initiate it.

**Architects** — designing new Engineering Loop specifications (LOOP-1XX) or revising existing ones, needing a governing reference that ensures consistency across the category.

**AI Coding Agents and Autonomous Engineering Systems** — executing Engineering Loops, needing a stable reference for shared behaviours, terminology, and integration expectations that apply regardless of which specific capability loop is running.

**Repository Maintainers** — adopting, configuring, or extending the Engineering Loop framework in a specific repository, needing to understand what is repository-agnostic (governed here) versus what is repository-specific (configured in `.loop-1XX.yml` files).

---

## Engineering Loop Philosophy

### Engineering Loops Extend the Core Loop Framework

The Core Loop Framework (LOOP-001 through LOOP-007) defines the universal engineering process: discovering architecture, assembling context, finding tasks, planning, implementing, verifying, and reflecting. This process applies to every engineering activity regardless of its nature. Engineering Loops do not replace this process. They extend it by providing capability-specific orchestration that determines which Core Loops are invoked, in what order, with what parameters, and with what capability-specific analysis layered between them.

An Engineering Loop without the Core Loops is an incomplete loop. An Engineering Loop that redefines the behaviour of a Core Loop is a malformed loop. Every Engineering Loop is, at its essence, a structured composition of Core Loop invocations with a declared engineering capability applied at the capability-specific decision points.

### Engineering Loops Orchestrate; They Do Not Duplicate

Implementation belongs to LOOP-005. Verification belongs to LOOP-006. Reflection belongs to LOOP-007. An Engineering Loop that re-implements these responsibilities — even incompletely, even "just for this capability" — creates two competing definitions of the same activity. The Engineering Loop provides the capability-specific inputs, parameters, and analysis that specialise the Core Loop's behaviour; it does not provide an alternative Core Loop.

A bug-fixing loop knows what constitutes a bug and how to identify root causes. It does not know how to implement the fix — that is LOOP-005's knowledge. It knows what evidence must be produced to confirm the fix — but it does not perform that verification; it configures LOOP-006 with the relevant criteria and lets LOOP-006 verify. The Engineering Loop is the director; the Core Loops are the specialists.

### Engineering Loops Are Repository-Agnostic

An Engineering Loop specification defines the capability in terms of universal engineering concepts: what a bug is, how root cause analysis proceeds, what a regression means, what the verification criteria for a fix must include. It does not define repository-specific commands, file paths, technology choices, or build configurations. Those details live in the repository's `.loop-1XX.yml` configuration file and in the LOOP-001 architecture discovery outputs that the Engineering Loop consumes.

A LOOP-101 specification that works for a Java monolith must work equally for a TypeScript microservices system. The capability — finding, planning, implementing, and verifying a bug fix — is the same. The technology details are supplied by LOOP-001 and LOOP-002 at runtime.

### Engineering Loops Produce Verifiable Engineering Artefacts

The output of every Engineering Loop run is not a code change — it is a complete engineering artefact set: an analysis, a plan, an implementation, verification evidence, and a reflection. The code change is the implementation artefact produced by LOOP-005 as part of the orchestration. But the Engineering Loop is responsible for the completeness of the artefact set as a whole. An Engineering Loop run that produces only a code change, without the analysis, plan, verification evidence, and reflection, is an incomplete run regardless of whether the code change is correct.

### Engineering Loops Are Deterministic Where Practical

Given identical inputs — the same repository state, the same task definition, the same configuration — an Engineering Loop run should produce equivalent outputs. Not byte-for-byte identical (the analysis and reflection contain natural language), but structurally equivalent: the same artefacts, the same verification outcomes, the same gate trigger conditions. Where non-determinism is unavoidable (e.g., an LLM-generated analysis), the Engineering Loop declares it explicitly and provides verification criteria that are deterministic.

---

## Relationship to the Core Loops

Every Engineering Loop consumes outputs from and invokes behaviours of the Core Loops. The table below defines the relationship between each Core Loop and the Engineering Loop category.

| Core Loop | Engineering Loop Relationship |
|-----------|------------------------------|
| **LOOP-001 — Architecture Discovery** | Consumed by every Engineering Loop. The architecture knowledge base is the Engineering Loop's primary reference for module boundaries, API contracts, dependency rules, and technology profile. An Engineering Loop shall not proceed if LOOP-001 outputs are absent or stale beyond the declared freshness threshold. Engineering Loops do not invoke LOOP-001 mid-run; they consume its most recent completed outputs at run start. |
| **LOOP-002 — Context Assembly** | Invoked by every Engineering Loop as the first active step, parameterised with the selected task and the Engineering Loop's capability-specific context requirements. LOOP-002 produces the context package that scopes all subsequent steps. The Engineering Loop declares, in its specification, which Tiers of context it requires and which context types are Tier 4 (excluded) for its capability. |
| **LOOP-003 — Task Discovery** | The source of the Engineering Loop's input task. Engineering Loops consume a selected task record from LOOP-003's backlog; they do not select tasks independently. The task's classification (`BUG`, `RFCT`, `TEST`, etc.) is the primary routing signal that determines which Engineering Loop is appropriate. |
| **LOOP-004 — Planning** | Invoked by every Engineering Loop after context assembly, parameterised with the capability-specific atomicity expectations, ordering rules, and non-negotiable constraints. The Engineering Loop provides the planning constraints; LOOP-004 produces the execution plan. An Engineering Loop must not produce its own execution plan independently of LOOP-004. |
| **LOOP-005 — Implementation** | Invoked by every Engineering Loop that results in repository modifications. The Engineering Loop configures LOOP-005 with the approved execution plan and the capability-specific implementation governance (e.g., a bug-fixing loop requires regression test creation before the fix; a refactoring loop prohibits behaviour changes). LOOP-005 executes; the Engineering Loop monitors. |
| **LOOP-006 — Verification** | Invoked by every Engineering Loop after implementation, parameterised with capability-specific verification criteria and the mandatory quality gate profile for the capability type. A bug-fixing loop requires regression confirmation; a documentation loop requires consistency checks. The Engineering Loop declares the verification expectations; LOOP-006 executes them independently. |
| **LOOP-007 — Reflection** | Invoked by every Engineering Loop at the conclusion of a run, regardless of outcome. LOOP-007 receives the Engineering Loop's complete artefact set and produces the lessons learned and engineering patterns specific to this capability and this cycle. The Engineering Loop does not self-reflect; it hands off to LOOP-007. |

### The Orchestration Model

The Engineering Loop's primary workflow is orchestration: it initiates Core Loop invocations, provides capability-specific parameters, monitors outcomes, and determines the next step based on those outcomes. An Engineering Loop run produces the following sequence of Core Loop activations:

```
Engineering Loop (Orchestrator)
  ↓ consumes
LOOP-001 outputs (pre-existing; freshness-checked)
  ↓ invokes
LOOP-002 → context package (parameterised for this capability and task)
  ↓ invokes
LOOP-003 backlog (pre-existing; selected task consumed)
  ↓ invokes
LOOP-004 → execution plan (parameterised with capability constraints)
  ↓ invokes (if implementation is required)
LOOP-005 → repository changes + implementation artefacts
  ↓ invokes
LOOP-006 → verification report + outcome
  ↓ invokes (always)
LOOP-007 → reflection artefacts + knowledge base updates
  ↓ produces
Engineering Loop artefact set (analysis, summary, metadata)
```

The Engineering Loop does not redefine or override any step within a Core Loop. It provides inputs and reads outputs. The internal behaviour of each Core Loop is governed by that loop's own specification.

### Capability-Specific Parameters

Engineering Loops supply the following parameter types to Core Loops:

**To LOOP-002:** The task record and the declared context tier requirements for the capability. For example, a security remediation loop may require security architecture context as Tier 1 Required, whereas a documentation loop would classify security architecture as Tier 3 Optional.

**To LOOP-004:** The capability-specific atomicity constraints, mandatory ordering rules, and non-negotiable step types. A bug-fixing loop mandates that a regression test step precedes the fix implementation step (test-before-red, Rule DO-3). A refactoring loop prohibits any step that changes externally observable behaviour.

**To LOOP-005:** The capability-specific implementation governance — the constraints that apply to all code produced in this capability context, beyond the universal coding standards.

**To LOOP-006:** The capability-specific verification profile — which verification categories are mandatory, which are applicable, and what additional criteria are required beyond the universal quality gates. A security remediation loop requires Category 11 (Security Validation) as mandatory regardless of task classification.

---

## Scope of Engineering Capabilities

The LOOP-100 series encompasses the following engineering capabilities. Each capability is implemented as a separate Engineering Loop. New capabilities are added by creating new LOOP-1XX specifications; no existing loop is modified to accommodate a new capability.

| Range | Capability Domain |
|-------|------------------|
| LOOP-101–109 | Defect Management (bug fixing, incident response, regression triage) |
| LOOP-110–119 | Structural Quality (refactoring, code modernisation, technical debt reduction) |
| LOOP-120–129 | Test Engineering (test generation, coverage improvement, test suite maintenance) |
| LOOP-130–139 | Documentation (API documentation, architecture documentation, code documentation) |
| LOOP-140–149 | Code Intelligence (code review, static analysis integration, quality scoring) |
| LOOP-150–159 | Dependency Management (upgrades, vulnerability remediation, licence compliance) |
| LOOP-160–169 | Performance Engineering (profiling, optimisation, regression prevention) |
| LOOP-170–179 | Security Engineering (vulnerability remediation, security posture assessment) |
| LOOP-180–189 | Infrastructure and Configuration (configuration management, infrastructure-as-code review) |
| LOOP-190–199 | Reserved for future Engineering Loop categories |

Adding a new capability requires: assigning the next available Loop ID in the appropriate range, creating the LOOP-NNN specification conformant with SPEC-001 and this guide, and registering the loop in SPEC-010 (Loop Catalog). No other existing document is modified to add a new capability.

---

## Shared Terminology

The following definitions are canonical across all Engineering Loops and the Core Loops that support them. These definitions are repository-agnostic and suitable for use in specifications, analysis artefacts, verification criteria, and reflection records. A term used in any Engineering Loop or Core Loop specification with a meaning different from this definition must be explicitly redefined in that specification's Scope section.

**Assumption**  
A decision made during an engineering activity that is not directly derivable from the plan, the context package, the architecture documentation, or the coding standards. Every assumption must be recorded with its basis, the decision made, the alternative not taken, and the potential impact if the assumption is wrong. Assumptions are inputs to verification and to LOOP-007 reflection; they are not defects.

**Bug**  
A defect in the implementation of software that causes the software to behave contrary to its declared specification or to a reasonable expectation of correct behaviour in the absence of a specification. A bug has an observable symptom, a root cause, and a scope of impact. A bug is distinct from a missing feature (the specification does not declare the behaviour) and from a design limitation (the specification intentionally declares a constrained behaviour).

**Capability**  
A distinct engineering activity implemented as an Engineering Loop. A capability has a defined purpose, a defined input set, a defined output set, and a defined set of Core Loop invocations. Two Engineering Loops may address overlapping concerns (e.g., bug fixing and regression triage) but must not duplicate each other's capability boundaries.

**Change Set**  
The complete set of modifications to repository files produced by a single Engineering Loop run, as recorded in LOOP-005's `change-log.md`. A change set is bounded by the execution plan produced by LOOP-004 for the selected task. Changes outside the execution plan are not part of the change set; they are scope violations.

**Constraint**  
A boundary on the Engineering Loop's behaviour that is imposed externally and cannot be changed by the loop or its agents. Constraints include: architectural boundaries declared in LOOP-001 outputs, accepted ADRs, security policies, compliance requirements, and human-approved gate decisions. Constraints are distinct from assumptions (which can be wrong) — constraints are treated as true and inviolable.

**Defect**  
The root cause condition in the code, configuration, or specification that produces a bug. A bug is the observable symptom; a defect is the underlying cause. Fixing a bug requires identifying and correcting the defect. A fix that addresses the symptom without correcting the defect is incomplete.

**Engineering Artefact**  
A structured document or data file produced by an Engineering Loop run as a declared output. Engineering artefacts are traceable (each carries its producing run ID and task ID), immutable after handoff (once consumed by a downstream loop, they are not overwritten), and verifiable (their content satisfies declared criteria that can be checked independently). Code changes are implementation artefacts, a subset of engineering artefacts.

**Incident**  
A bug or system failure that has manifested in a production or production-equivalent environment and is causing observable harm to users or systems. An incident is a bug with an active impact; all incidents are bugs, but not all bugs are incidents. Incident response Engineering Loops operate under elevated urgency constraints and may invoke expedited gate procedures.

**Refactor**  
A modification to source code that improves its internal structure — readability, maintainability, testability, or conformance to architectural standards — without changing its externally observable behaviour. A refactor that changes behaviour (intentionally or unintentionally) is not a refactor; it is a feature change or a bug fix. The non-behaviour-change constraint is the defining property of a refactor and must be verified by LOOP-006.

**Regression**  
A defect introduced by a change that causes previously correct behaviour to become incorrect. A regression is a defect whose cause is traceable to a specific change in the repository. A pre-existing defect that was always present is not a regression. LOOP-006's regression detection step (Step 6) is the authoritative mechanism for attributing regressions; Engineering Loops do not perform independent regression attribution.

**Risk**  
A condition that, if it occurs, would negatively impact the engineering outcome, the system's quality, or the repository's integrity. Every Engineering Loop assesses its mandatory risk categories (SPEC-001 §10) and declares capability-specific risks. A risk is characterised by likelihood, impact, trigger condition, mitigation, and contingency response.

**Root Cause**  
The earliest condition in the causal chain that, if it had been different, would have prevented the defect from occurring. Root cause analysis produces a root cause statement and a mechanism description: the path from the root cause to the observable symptom. Engineering Loops that address defects (Defect Management domain) are required to produce a root cause record before an execution plan is produced.

**Technical Debt**  
The accumulated cost of prior engineering decisions that were expedient at the time but have reduced the codebase's maintainability, testability, or architectural clarity. Technical debt is not a defect — the code may function correctly — but it imposes increasing implementation and verification cost on future Engineering Loop runs. Technical debt is recorded as `RSRCH` or `RFCT` task types in LOOP-003's backlog.

**Validation**  
The process of confirming that the right thing was built — that the implementation satisfies the intent of the original requirement. Validation is the domain of business logic testing, acceptance testing, and stakeholder review. It is distinct from verification.

**Verification**  
The process of confirming that the thing was built correctly — that the implementation satisfies its declared specification. Verification is LOOP-006's domain: it checks correctness against the execution plan, architectural constraints, and quality criteria. Verification does not assess whether the right thing was built, only whether the thing was built correctly.

**Verification Confidence Score**  
A numeric measure (0–100) of the completeness and reliability of the verification evidence collected by LOOP-006 for a given run. A high score indicates comprehensive evidence; a low score indicates evidence gaps that may allow defects to escape undetected. Engineering Loops declare a minimum acceptable confidence score threshold in their verification profile; a score below this threshold triggers a Hard Gate.

**Validation vs. Verification**  
To be unambiguous: within the AEOS framework, *verification* is used exclusively in the sense of "built correctly per specification" (LOOP-006's responsibility). *Validation* is used in the sense of "the right thing was built per intent" (a human responsibility, triggered through GATE-1 when required). Engineering Loop specifications must use these terms with their declared meanings.

---

## Standard Inputs

All Engineering Loops consume from a common set of input categories. Each Engineering Loop specification declares which of these inputs are Required, Optional, or Not Applicable for its capability.

### Architecture Inputs (from LOOP-001)

- `docs/architecture/module-catalog.md` — the authoritative module boundaries, public interfaces, and module ownership
- `docs/architecture/api-catalog.md` — all declared API contracts, versioning status, and consumer registry
- `docs/architecture/event-catalog.md` — all declared event schemas, producers, and consumers
- `docs/architecture/dependency-map.md` — inter-module dependencies and transitive dependency graph
- `docs/architecture/technology-stack.md` — languages, frameworks, toolchain versions, and configuration conventions
- `docs/loops/core/SKILL-001.md` — repository-specific calibrations: test commands, lint commands, build commands, file naming conventions

The architecture inputs collectively define the repository's engineering contract. An Engineering Loop that operates without architecture inputs may produce correct code but cannot ensure architectural compliance.

### Context Inputs (from LOOP-002)

- `docs/context/context-package.md` — the assembled context index for the selected task
- `docs/context/implementation-context.md` — the selected source files relevant to the task
- `docs/context/verification-context.md` — the test files and acceptance criteria relevant to the task
- `docs/context/architecture-context.md` — the architectural documents scoped to the task's affected modules
- `docs/context/dependency-context.md` — the dependency information scoped to the task
- `docs/context/context-metadata.md` — the HEAD SHA at context assembly time (required for freshness validation)

Context inputs are task-scoped. They do not represent the entire repository; they represent the minimum sufficient set for the task. Engineering Loops that require context beyond what LOOP-002 assembles by default must declare the additional context requirements as LOOP-002 parameters.

### Task Inputs (from LOOP-003)

- A selected task record from `docs/tasks/task-catalog.md`, with fields: `task_id`, `primary_category`, `source`, `description`, `affected_modules`, `dependencies`, `complexity_signals`, `risk_signals`, `priority_rank`, `confidence`
- The task's classification code (e.g., `BUG`, `RFCT`, `TEST`) which routes to the appropriate Engineering Loop
- The task's dependency graph (other tasks that must be completed before this task)

### Planning Inputs (from LOOP-004, when resuming or re-planning)

- `docs/planning/execution-plan.md` — the approved step sequence
- `docs/planning/verification-plan.md` — the checkpoint definitions
- `docs/planning/rollback-plan.md` — the rollback actions
- `docs/planning/planning-metadata.md` — plan provenance, including HEAD SHA and plan version

Planning inputs are consumed when an Engineering Loop is resuming after interruption or when LOOP-006 has issued a Requires Rework outcome and the loop is determining whether re-planning is necessary.

### Implementation Inputs (from LOOP-005, when verifying or reflecting)

- `docs/implementation/implementation-summary.md`
- `docs/implementation/change-log.md`
- `docs/implementation/assumptions.md`
- `docs/implementation/implementation-metadata.md`

### Verification Inputs (from LOOP-006, when reflecting)

- `docs/verification/verification-report.md`
- `docs/verification/verification-summary.md`
- `docs/verification/verification-metadata.md`

### Repository State Inputs

- Current HEAD SHA (from `git rev-parse HEAD` or equivalent)
- Repository build configuration (build files, dependency manifests)
- Test suite structure (test directories, test runner configuration from `SKILL-001.md`)
- CI/CD pipeline definitions (for understanding what automation already covers)
- ADR directory (for understanding the repository's recorded design decisions)

### Engineering Loop Configuration

- `.loop-1XX.yml` at the repository root — capability-specific configuration that adapts the Engineering Loop to this repository without modifying the loop specification itself. Common configuration keys include: minimum verification confidence score, maximum run duration overrides, notification channels, and repository-specific tool commands.

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Standard Outputs

Every Engineering Loop produces a standard artefact set. The table below defines which artefacts are mandatory (produced by every Engineering Loop run regardless of outcome) and which are conditional (produced only when certain conditions are met).

### Mandatory Artefacts

| Artefact | Content | Produces When |
|----------|---------|---------------|
| Engineering Analysis | The capability-specific analysis performed before planning. For bug fixing: root cause analysis. For refactoring: structural assessment. For test generation: coverage gap analysis. | Always — this is the capability-specific contribution of the Engineering Loop beyond what Core Loops provide. |
| Engineering Summary | A concise summary of the complete Engineering Loop run: the task, the orchestration sequence, the outcome, and the most significant findings. | Always. |
| Engineering Metadata | Run provenance: task ID, Engineering Loop ID and version, all Core Loop run IDs consumed, total elapsed duration, outcome, HEAD SHA at start and end. | Always. |
| Reflection Artefact | The LOOP-007 reflection for this run (produced by LOOP-007, triggered by this Engineering Loop). | Always — even for stopped or failed runs. |
| STATUS update | The Engineering Loop's STATUS file, recording the run outcome and current state. | Always. |

### Conditional Artefacts

| Artefact | Content | Produces When |
|----------|---------|---------------|
| Execution Plan | The LOOP-004 execution plan (produced by LOOP-004, triggered by this Engineering Loop). | When the Engineering Loop reaches the planning phase (not produced for runs that halt before planning). |
| Implementation Summary | The LOOP-005 implementation summary (produced by LOOP-005, triggered by this Engineering Loop). | When implementation is executed. |
| Verification Report | The LOOP-006 verification report (produced by LOOP-006, triggered by this Engineering Loop). | When verification is executed. |
| Rework Specification | The structured list of findings from LOOP-006 that must be resolved before Accepted outcome. | When LOOP-006 outcome is Requires Rework. |
| Root Cause Record | For Defect Management loops: the root cause analysis document, produced before planning. | For LOOP-101 through LOOP-109 capability types. |
| Capability-Specific Analysis | Additional analysis specific to the Engineering Loop's capability that is not covered by the Core Loop outputs. | As declared in the individual Engineering Loop specification. |

### Artefact Directory Convention

Engineering Loop artefacts are written to `docs/engineering/{capability-name}/` (e.g., `docs/engineering/bug-fixing/`). Each Engineering Loop specification declares its primary output directory. This directory is distinct from the Core Loop output directories (`docs/planning/`, `docs/implementation/`, `docs/verification/`) which are produced by the Core Loops themselves.

---

## Shared Workflow Pattern

Every Engineering Loop follows the same canonical workflow. Each step in this pattern is an obligation for the Engineering Loop; the implementation of each step is either the Engineering Loop's own analysis or an invocation of a Core Loop.

### Step 1 — Receive Engineering Request

The Engineering Loop receives a task record selected from the LOOP-003 backlog. It validates that the task's classification matches this Engineering Loop's declared capability scope. It validates that the task's dependencies are complete (prior tasks that must be done first are in the `completed` state in the task catalog). It records the task ID, the current HEAD SHA, and the trigger source in its STATUS file.

### Step 2 — Validate Architecture Freshness

Before assembling context, the Engineering Loop validates that LOOP-001 outputs meet the declared freshness requirement. If LOOP-001 outputs are stale beyond the threshold, the Engineering Loop triggers a Soft Gate (GATE-2) — it may proceed with stale architecture knowledge but must record the staleness and reduce the confidence score accordingly. If LOOP-001 outputs are absent, the Engineering Loop triggers a Hard Gate (GATE-1) — it cannot proceed without architecture knowledge.

### Step 3 — Invoke LOOP-002: Context Assembly

The Engineering Loop invokes LOOP-002, providing the task record and the capability-specific context tier requirements. It waits for LOOP-002 to complete and validates the context package HEAD SHA against the current repository HEAD SHA. If they differ, GATE-1 fires — the context was assembled against a different repository state.

### Step 4 — Execute Capability-Specific Analysis

This step is the Engineering Loop's primary proprietary contribution. The analysis is capability-specific and is not delegated to a Core Loop:

- **Defect Management loops:** root cause analysis, reproduction verification, scope of impact assessment
- **Structural Quality loops:** structural assessment, change risk profiling, behaviour invariant identification
- **Test Engineering loops:** coverage gap analysis, test design classification, test dependency mapping
- **Documentation loops:** documentation completeness assessment, accuracy verification, audience analysis
- **Code Intelligence loops:** quality scoring, pattern identification, finding classification
- **Dependency Management loops:** compatibility matrix analysis, transitive impact assessment, licence compliance review
- **Performance Engineering loops:** baseline measurement, bottleneck identification, regression risk profiling
- **Security Engineering loops:** vulnerability classification, attack surface mapping, remediation priority assessment

The analysis produces a structured Analysis Artefact (see Engineering Artefacts section). The analysis is reviewed by a Checker agent before planning begins.

### Step 5 — Invoke LOOP-004: Planning

The Engineering Loop invokes LOOP-004, providing the analysis artefact and the capability-specific planning constraints (mandatory step types, ordering rules, and prohibited step types). It waits for LOOP-004 to produce an approved execution plan.

If LOOP-004 cannot produce a plan that satisfies the capability-specific constraints (e.g., a bug-fixing loop cannot produce a plan where the regression test step follows rather than precedes the fix step), the Engineering Loop triggers GATE-1. The plan is not relaxed; the constraint is non-negotiable.

### Step 6 — Invoke LOOP-005: Implementation (when applicable)

For Engineering Loops that produce repository modifications, the Engineering Loop invokes LOOP-005 with the approved execution plan. It monitors the implementation by reading STATUS-005.md for gate triggers, Emergency Stop signals, and step completions.

For Engineering Loops that do not produce repository modifications (e.g., code review, documentation quality assessment), this step is skipped. The Engineering Loop specification must declare explicitly whether LOOP-005 is invoked.

### Step 7 — Invoke LOOP-006: Verification

The Engineering Loop invokes LOOP-006 with the capability-specific verification profile. This profile specifies which verification categories are mandatory for this capability, the minimum acceptable confidence score, and any additional verification criteria beyond LOOP-006's universal set.

If LOOP-006 produces a Requires Rework outcome, the Engineering Loop determines whether re-planning is required (if the rework affects the execution plan structure) or whether LOOP-005 can proceed directly with the rework specification. The Engineering Loop does not modify the rework specification — it determines the path and invokes the appropriate loop.

### Step 8 — Invoke LOOP-007: Reflection

The Engineering Loop invokes LOOP-007 upon conclusion of the run, regardless of outcome. It provides LOOP-007 with the complete artefact set: the engineering analysis, the implementation summary (if applicable), the verification report (if applicable), and the Engineering Loop's own STATUS and metadata.

### Step 9 — Publish Engineering Summary

The Engineering Loop produces its Engineering Summary and Engineering Metadata artefacts, updates its STATUS file to `completed` (or `stopped` or `failed`), and updates its SKILL file with calibration observations from this run.

---

## Common Human Approval Gates

The following gate conditions are shared across Engineering Loops. Every Engineering Loop specification shall declare these conditions as Hard Gate triggers (GATE-1) unless the Engineering Loop's category makes a condition structurally inapplicable, in which case the inapplicability must be explicitly declared.

Engineering Loop specifications may add capability-specific gate conditions beyond these; they may not remove or downgrade these conditions without a SPEC-001 Contract Exception declaration.

### Security-Sensitive Changes

Any modification to authentication logic, authorisation checks, tenant isolation boundaries, cryptographic implementations, session management, input validation, output encoding, or access control rules. Requires Security Lead review before the implementation step executes. Cannot be auto-proceeded.

### Public API Changes

Any modification that adds, removes, or changes the signature of an endpoint, message type, or interface contract that is consumed by parties outside the immediate module. Requires Principal Engineer review before the implementation step executes. Includes: REST endpoint changes, GraphQL schema changes, event schema changes, SDK method changes, and RPC definition changes.

### Database Schema Changes

Any migration that adds, modifies, or drops database objects (tables, columns, indexes, constraints, triggers, views, stored procedures). Requires data architect or principal engineer review. Applies to all database engines and all migration frameworks. The dual-write/dual-read migration pattern for column removal requires an additional Human Review gate before the removal migration executes.

### Infrastructure Modifications

Any change to infrastructure-as-code definitions, Kubernetes manifests, Helm charts, Terraform configurations, Docker Compose files, or cloud provider resource definitions. Requires Infrastructure Lead review. Infrastructure changes affect shared state outside the repository and cannot be rolled back as cleanly as code changes.

### Architectural Boundary Changes

Any modification that introduces a new dependency between modules, crosses a declared module boundary, violates an accepted ADR, or establishes a new inter-service communication path. Requires Architecture Owner review. These changes alter the architecture knowledge base maintained by LOOP-001 and must be reflected in a LOOP-001 re-run after implementation.

### Regulatory or Compliance-Sensitive Changes

Any modification affecting data retention, data handling, audit logging, consent management, privacy controls, or any component subject to a declared regulatory requirement. Requires Compliance Owner review. A compliance impact assessment must be attached to the gate review request.

### High-Risk Production Changes

Any implementation step designated non-reversible in the execution plan, or any change that affects systems or data with no staging environment equivalent. Requires Principal Engineer review. The rollback plan for the step must be reviewed and approved as part of the gate; a gate approval without rollback plan review is incomplete.

### Verification Confidence Below Threshold

When LOOP-006 reports a verification confidence score below the minimum declared in the Engineering Loop's verification profile. The specific threshold is declared per capability; a single universal threshold is not appropriate because some capabilities inherently produce lower-evidence verification than others. When the score falls below threshold, the outcome is at minimum Requires Human Review.

---

## Cross-Cutting Metrics

The following metrics are defined for consistent measurement across all Engineering Loops. Individual Engineering Loop specifications may add capability-specific metrics; they may not redefine these shared metrics. Thresholds for these metrics are not defined here — threshold selection is repository-specific and is configured in `.loop-1XX.yml`.

### Cycle Time

The elapsed wall-clock duration from Engineering Loop trigger to Engineering Loop run closure (STATUS file records `completed`, `stopped`, or `failed`). Measured in seconds. Captured in the Engineering Metadata artefact. Cycle time includes all waiting time at Human Approval Gates; an Engineering Loop with a long GATE-1 wait time has a long cycle time regardless of its internal execution efficiency.

### Engineering Throughput

The count of Engineering Loop runs that reached `completed` status with a LOOP-006 Accepted outcome within a declared time window (e.g., per week, per sprint). Throughput is measured at the Engineering Loop level, not at the task level — a task that requires three rework cycles counts as one engineering throughput unit.

### Verification Success Rate

The proportion of LOOP-006 invocations that produce an Accepted outcome without a Requires Rework cycle, measured across all runs of an Engineering Loop type over a time window. A low verification success rate for a specific Engineering Loop type signals a planning or implementation effectiveness gap for that capability.

### Defect Escape Rate

The proportion of Engineering Loop runs with an Accepted LOOP-006 outcome that subsequently produce a defect report (a new `BUG` task traced to the output of a prior run). A defect escape occurs when LOOP-006 accepted an implementation that later produced observable incorrect behaviour. This metric is computed retrospectively; LOOP-007 contributes to it when it traces a new `BUG` task to a prior run.

### Rework Rate

The proportion of Engineering Loop runs that require at least one Requires Rework outcome from LOOP-006 before reaching Accepted. Rework rate measures the effectiveness of the planning and implementation phases. A high rework rate for a specific capability type signals that LOOP-004 planning constraints for that capability need calibration.

### Documentation Completeness Score

The proportion of documentation obligations declared in LOOP-005's execution plan that were fulfilled within the same implementation step (not deferred). Deferred documentation obligations reduce the score. An Engineering Loop capability that consistently produces low documentation completeness scores signals that the capability's LOOP-002 context tier requirements need to include documentation context more prominently.

### Reflection Completion Rate

The proportion of Engineering Loop runs that invoked LOOP-007 and received a completed LOOP-007 run with at least one lesson recorded. A reflection completion rate below 100% indicates that Engineering Loop runs are closing without capturing the knowledge produced in the cycle.

### Automation Coverage

The proportion of LOOP-006 quality gate criteria that are evaluated by automated tooling (deterministic tests, linters, static analysis) versus manual or agent-judgment evaluation. Higher automation coverage produces more reliable verification confidence scores and reduces the cost of each verification run.

### Verification Confidence Score

The LOOP-006 confidence score (0–100) for this run. Engineering Loops aggregate this across runs to identify capability types or module types where verification consistently produces incomplete evidence — signals of missing test coverage, missing contract tests, or non-deterministic test suites.

---

## Engineering Artefacts

The following table defines each standard artefact in full. Artefacts marked Mandatory must appear in every Engineering Loop specification; Conditional artefacts must appear in specifications where the declared conditions apply; Optional artefacts may be added by individual Engineering Loop specifications.

### Engineering Analysis (Mandatory)

The structured, capability-specific analysis produced in Step 4 of the workflow. The analysis is the Engineering Loop's primary intellectual contribution — the reasoning that transforms a task record into the inputs for LOOP-004 planning.

**Required fields (all Engineering Loops):**
- Task ID and classification
- Capability being applied
- Summary of the analysis
- Affected modules (confirmed by analysis, not merely inherited from the task record)
- Assumptions made during analysis (with basis and impact-if-wrong)
- Risks identified by the analysis
- Confidence level of the analysis (High / Medium / Low) with rationale

**Required fields (Defect Management loops, additionally):**
- Reproduction steps
- Root cause statement
- Defect location (file path, line range, component)
- Scope of impact (modules and behaviours affected)
- Regression risk assessment (which adjacent behaviours could be affected by the fix)

**Required fields (Structural Quality loops, additionally):**
- Structural problems identified (with specific file paths and metrics)
- Behaviour invariants that must be preserved
- Architectural constraints that must be respected
- Estimated size of change set

**Checker review:** The analysis must be reviewed by a Checker agent before LOOP-004 is invoked. The Checker validates: evidence accuracy, scope accuracy, and assumption completeness. An analysis that has not been Checker-reviewed must not proceed to planning.

### Engineering Summary (Mandatory)

A concise document, produced at Step 9, that captures the complete run for human review without requiring the reader to examine all artefacts.

**Required sections:**
- Run overview: task ID, capability, Engineering Loop ID and version, trigger, dates
- Orchestration record: which Core Loops were invoked, in what order, and their outcomes
- Capability analysis headline: the one-paragraph summary of the analysis finding
- Outcome: the LOOP-006 outcome, or the stopping reason if the run did not reach verification
- Top three lessons (from LOOP-007, when available)
- Open items: deferred steps, unresolved assumptions, outstanding limitations

### Engineering Metadata (Mandatory)

The provenance record for the run.

**Required fields:**
- Engineering Loop ID and version
- Run ID
- Task ID and task classification
- LOOP-002 run ID consumed
- LOOP-003 task record version consumed
- LOOP-004 run ID consumed (if invoked)
- LOOP-005 run ID consumed (if invoked)
- LOOP-006 run ID consumed (if invoked)
- LOOP-007 run ID consumed (if invoked)
- HEAD SHA at run start
- HEAD SHA at run end
- Total elapsed duration in seconds
- Outcome
- Rework cycle count (how many LOOP-006 Requires Rework outcomes before final outcome)

### Execution Plan (Conditional — when LOOP-004 is invoked)

Produced by LOOP-004, not the Engineering Loop directly. Referenced in the Engineering Metadata by LOOP-004 run ID. The Engineering Loop's specification must declare the capability-specific constraints it provides to LOOP-004; the plan itself is LOOP-004's artefact.

### Implementation Summary (Conditional — when LOOP-005 is invoked)

Produced by LOOP-005. Referenced in the Engineering Metadata by LOOP-005 run ID. The Engineering Loop monitors LOOP-005's execution but does not produce this artefact.

### Verification Report (Conditional — when LOOP-006 is invoked)

Produced by LOOP-006. Referenced in the Engineering Metadata by LOOP-006 run ID. The Engineering Loop provides the capability-specific verification profile but does not produce this artefact.

### Root Cause Record (Conditional — Defect Management loops only)

A structured document produced by the Engineering Loop's capability-specific analysis step, before LOOP-004 is invoked.

**Required fields:**
- Root cause statement (the specific, falsifiable condition that caused the defect)
- Mechanism description (the causal path from root cause to observable symptom)
- Evidence (the specific code location, test output, or log excerpt that confirms the root cause)
- Similar defects (references to prior `BUG` tasks with related root causes, from the engineering knowledge base)
- Prevention observation (what engineering practice, if present, would have prevented this defect — fed to LOOP-007)

---

## Integration with Shared Specifications

Engineering Loops interact with ten shared specifications. This section describes the nature of each interaction; it does not reproduce the specifications' contents.

### SPEC-001 — Loop Contracts

Every Engineering Loop specification is a LOOP-NNN document and is therefore fully subject to SPEC-001. The conformance checklist in SPEC-001 §14 applies without exception. Engineering Loops that require an exception to a SPEC-001 normative clause must declare it using the `Contract Exception:` header format defined in SPEC-001 §15.C2.

Engineering Loops have one SPEC-001 interaction unique to their category: they are both consumers and producers in the same chain. As consumers, they declare dependencies on LOOP-001 through LOOP-007 outputs. As producers, they produce artefacts that may be declared as optional context by other Engineering Loops (e.g., a prior bug-fixing run's root cause record may be relevant context for a refactoring run in the same module). Engineering Loop specifications should declare these cross-capability context relationships in their `Depends On` field.

### SPEC-002 — State Management

Engineering Loops interact with state at two levels: the Engineering Loop's own STATE file (STATUS-1XX.md), and the Core Loop STATE files of the loops they orchestrate. Engineering Loops read Core Loop STATE files to monitor invocation outcomes; they do not write to Core Loop STATE files. Each Engineering Loop owns exactly one STATUS file and one SKILL file; these are its sole mutable state artefacts.

### SPEC-003 — Dependency Map

Engineering Loops declare their dependency graph in conformance with SPEC-003. For Engineering Loops, the dependency graph includes both upstream Core Loop dependencies (LOOP-001 through LOOP-007 as applicable) and peer Engineering Loop dependencies (e.g., a security remediation loop may depend on a prior code review loop's findings). SPEC-003 governs the freshness validation and circular dependency prohibition for the complete graph.

### SPEC-004 — Lifecycle

Engineering Loop lifecycle states (Draft, Active, Deprecated, Retired) and transition rules are governed by SPEC-004, which extends the SPEC-001 §1.C4 rules with repository-wide lifecycle coordination. An Engineering Loop that is set to Deprecated must have its entry in SPEC-010 (Loop Catalog) updated simultaneously. SPEC-004 governs the deprecation window, migration guidance requirements, and the chain update obligations when an Engineering Loop is retired.

### SPEC-005 — Metrics

The cross-cutting metrics defined in this guide are formally defined — including storage format, aggregation rules, and dashboard integration guidance — in SPEC-005. Engineering Loop specifications reference these metrics by name; SPEC-005 governs their implementation. Capability-specific metrics declared in individual Engineering Loop specifications are registered in SPEC-005's extension registry.

### SPEC-006 — Governance

SPEC-006 extends the governance model defined in SPEC-001 §9 with operational governance: who can approve changes to Engineering Loop specifications, how new Engineering Loops are commissioned, how Engineering Loops in high-risk capabilities (Security, Compliance) are subject to additional oversight, and how Engineering Loop audit records are retained. Engineering Loops that operate in regulated domains must comply with SPEC-006's regulatory overlay.

### SPEC-007 — Versioning

SPEC-007 extends the SPEC-001 §8 versioning rules with co-revision rules for Engineering Loops: when an Engineering Loop specification changes in a way that requires a corresponding change to a Core Loop's specification (e.g., a new capability-specific verification category requires LOOP-006 to add a new category type), the two version increments are coordinated as a co-revision. SPEC-007 defines the co-revision declaration format and the compatibility window.

### SPEC-008 — Error Handling

Engineering Loops encounter errors both in their own capability-specific steps and in the Core Loop invocations they orchestrate. SPEC-008 defines the error classification taxonomy (transient, structural, governance, external), the escalation ladder, and the integration between Engineering Loop failure records and the organisation's incident management system. Engineering Loop specifications declare their failure classes and reference SPEC-008 for the handling protocol.

### SPEC-009 — Security

SPEC-009 governs the security posture of Engineering Loops as executors: how they handle credentials, how they isolate execution between tenants, what scanning is required before and after capability-specific analysis, and what constitutes a security-sensitive Engineering Loop operation. Security Engineering loops (LOOP-170–179) have additional obligations under SPEC-009 as loops that directly handle vulnerability information.

### SPEC-010 — Loop Catalog

SPEC-010 is the registry of all loops in the AEOS framework. Every Engineering Loop specification, upon reaching Active status, must have an entry in SPEC-010's catalog. The catalog entry includes: Loop ID, Name, Category, Status, Version, primary capability, input classification, output classification, and the routing conditions that determine when this Engineering Loop is selected for a given task. SPEC-010 is the authoritative routing reference; task dispatchers consult SPEC-010, not individual loop specifications, to determine which Engineering Loop to invoke.

---

## Design Principles

The eight design principles below govern every decision in Engineering Loop specification authorship. A design decision that violates any of these principles requires an explicit justification in the specification.

### Reusability

Every Engineering Loop specification is written to be adopted by any repository that uses the AEOS framework, regardless of technology stack, team size, or domain. Repository-specific details are isolated in `.loop-1XX.yml` configuration and in the outputs of LOOP-001 (which the Engineering Loop consumes at runtime). An Engineering Loop specification that hardcodes repository-specific paths, commands, or conventions is not reusable.

### Composability

Engineering Loops are composable with each other and with the Core Loops. A sequence of Engineering Loop runs — bug fixing followed by test generation in the same module, or security remediation followed by refactoring — should produce a coherent total result. Composability requires that Engineering Loops do not assume they are the first or last activity in a module's history, that they respect and carry forward prior loop outputs as inputs, and that they leave the repository in a state that the next loop can consume cleanly.

### Determinism

An Engineering Loop run with identical inputs should produce equivalent outputs. Where determinism is structurally impossible (natural language generation in analysis, non-deterministic test suites), the Engineering Loop declares the non-deterministic components, applies independent corroboration requirements, and reduces its verification confidence score to reflect the reduced evidence reliability. Determinism is a design goal, not an absolute constraint; but every deviation from it must be declared and managed.

### Traceability

Every artefact produced by an Engineering Loop run carries a complete provenance chain: the task ID, the run ID, the Engineering Loop ID and version, the HEAD SHA at start and end, and the run IDs of all Core Loops invoked. Given an Engineering Loop artefact from any point in the repository's history, it must be possible to identify exactly what triggered the run, what the repository state was, and what every Core Loop produced. This traceability chain is the foundation for audit, root cause investigation, and knowledge base reconstruction.

### Verifiability

Every claim made in an Engineering Loop artefact must be verifiable by a reader who has access to the cited evidence. An analysis that concludes "the root cause is X" must cite the specific code location, test output, or log excerpt that supports that conclusion. A verification report that marks a criterion as passed must cite the specific evidence that demonstrates passing. Claims without evidence are opinions; opinions are not artefacts.

### Extensibility

New Engineering Loop capabilities are added without modifying existing Engineering Loop specifications or Core Loop specifications. The LOOP-100 series numbering scheme provides space for 99 capability loops. New loop specifications conform to SPEC-001 and this guide; the framework extends automatically. An Engineering Loop specification that can only function correctly if another Engineering Loop is present (beyond the declared dependencies in `Depends On`) is an extensibility failure — it has a hidden coupling that will break when the depended-upon loop evolves.

### Maintainability

Engineering Loop specifications are living documents. They will be revised as the repository's technology stack evolves, as the team's engineering practices mature, and as the AEOS framework itself is refined. Maintainability requires that each specification's normative content is clearly separated from its informational content, that revision history is recorded in the Version History section, and that the SKILL file captures calibration observations that reduce the gap between the specification and the observed runtime behaviour.

### Separation of Responsibilities

Engineering Loops are responsible for capability-specific analysis and orchestration. Core Loops are responsible for planning, implementation, verification, and reflection. This separation is not a convention — it is a design invariant. An Engineering Loop that implements planning logic (deciding what to build and how to sequence it) has violated the boundary with LOOP-004. An Engineering Loop that verifies its own output has violated the boundary with LOOP-006. The boundaries between responsibilities are enforced by the contract structure: Engineering Loops invoke Core Loops; they do not replace them.

---

## Adding New Engineering Loops

When a new engineering capability is identified that is not covered by an existing Engineering Loop, the process for adding it is:

1. **Identify the capability domain** — determine which LOOP-100 sub-range is appropriate (see Scope of Engineering Capabilities table). If no range is appropriate, request a range allocation from the Principal Architecture function.

2. **Assign the Loop ID** — the next available integer in the sub-range. Verify against SPEC-010 that the ID is not already in use.

3. **Author the specification** — using `docs/loops/templates/LOOP-TEMPLATE.md` as the starting structure. The specification must conform to SPEC-001, this guide, and LOOP-STANDARD.md. It must declare which Core Loops it invokes and the parameters it provides to each.

4. **Define the capability-specific analysis** — the primary intellectual contribution of the new Engineering Loop. This is the step that cannot be delegated to a Core Loop because it requires capability-specific domain knowledge.

5. **Define the capability-specific verification profile** — which LOOP-006 categories are mandatory, the minimum confidence score threshold, and any additional verification criteria.

6. **Define the capability-specific LOOP-004 constraints** — the mandatory step types, ordering rules, and prohibited step types that specialise LOOP-004's planning for this capability.

7. **Conformance review** — verify the specification satisfies all items in SPEC-001 §14 and all items in this guide's Conformance section.

8. **Register in SPEC-010** — add the catalog entry including routing conditions.

9. **Set Status to Active** — after conformance review passes.

No existing Engineering Loop or Core Loop is modified in this process.

---

## Conformance for Engineering Loop Specifications

An Engineering Loop specification is conformant with this guide when all of the following are true, in addition to satisfying the SPEC-001 conformance checklist:

**Philosophy and Scope**
- [ ] The specification does not redefine the behaviour of any Core Loop
- [ ] The specification declares which Core Loops it invokes and with what parameters
- [ ] The specification declares which Core Loop invocations are skipped and why
- [ ] The specification is written without repository-specific commands, paths, or tool names (these belong in `.loop-1XX.yml`)

**Terminology**
- [ ] All terms from the Shared Terminology section are used with their declared meanings
- [ ] Any term used with a different meaning is explicitly redefined in the Scope section

**Workflow**
- [ ] The specification follows the canonical nine-step workflow (Receive → Validate Architecture → Invoke LOOP-002 → Capability Analysis → Invoke LOOP-004 → Invoke LOOP-005 [if applicable] → Invoke LOOP-006 → Invoke LOOP-007 → Publish Summary)
- [ ] The capability-specific analysis step (Step 4) is defined with its required fields
- [ ] The capability-specific analysis step declares a Checker review before planning

**Gates**
- [ ] All eight common Human Approval Gate conditions are declared as Hard Gate triggers, or their inapplicability is explicitly declared with rationale
- [ ] Any additional capability-specific gate conditions are declared

**Artefacts**
- [ ] All mandatory artefacts are declared as outputs
- [ ] All applicable conditional artefacts are declared as outputs
- [ ] The Engineering Analysis artefact declares all required fields for the Engineering Loop's domain

**Metrics**
- [ ] All nine cross-cutting metrics are declared and referenced
- [ ] No shared metric is redefined with a different calculation

**Verification Profile**
- [ ] The capability-specific LOOP-006 verification profile is declared, including mandatory categories and minimum confidence score threshold

**LOOP-004 Constraints**
- [ ] The capability-specific planning constraints provided to LOOP-004 are declared, including mandatory step types and ordering rules

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes the ENGINEERING-LOOP-GUIDE as the canonical architectural reference for all LOOP-1XX Engineering Loop specifications. Defines philosophy, shared terminology, standard inputs and outputs, canonical workflow, common gate conditions, cross-cutting metrics, standard artefact definitions, integration model with shared specifications, design principles, and the conformance checklist for Engineering Loop authorship.

