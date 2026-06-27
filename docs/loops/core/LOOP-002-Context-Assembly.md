---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-002-Context-Assembly.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Collect and assemble relevant source file contexts for a task.
Generalized Purpose: Collect and assemble relevant source file contexts for a task.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-002 — Context Assembly

**Loop ID:** LOOP-002  
**Name:** Context Assembly  
**Version:** 1.0  
**Status:** Active  
**Category:** Core  
**Depends On:** LOOP-001 — Architecture Discovery  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-002 gathers, filters, validates, and assembles the minimum necessary context required for an AI agent to perform a specific engineering task accurately. It transforms raw repository knowledge — produced by LOOP-001 — into a task-scoped, validated context package that is sufficient for precise engineering work and free of material that would increase token cost, ambiguity, or cognitive load without improving output quality.

Every implementation, verification, planning, and documentation loop must consume a context package produced by this loop before beginning work. The context package is the agent's authorised view of the repository for the duration of a single task.

---

## Problem Statement

AI agents given unrestricted repository access select context arbitrarily. They over-include unrelated files, producing prompts that exceed useful token budgets and introduce noise from unrelated modules. They under-include critical files — missing dependency contracts, ADRs that constrain a decision, or tests that define acceptance criteria — producing outputs that are coherent in isolation but incorrect in the system. Without a principled, repeatable selection process, context quality is a function of prompt construction skill rather than engineering rigour, and the gap between agent intent and agent output grows with repository size.

---

## Why This Loop Exists

Context assembly is a prerequisite for every substantive engineering loop. Its quality determines the accuracy ceiling of every downstream loop that depends on it. Codifying it as a loop — with declared selection rules, validation criteria, and human gates — makes context quality auditable, reproducible, and improvable over time. A context package produced by this loop is a signed artefact: its contents, scope, and confidence level are recorded and traceable to the task that requested it.

---

## Scope

**In scope:**
- Receiving and classifying the engineering task
- Loading and interpreting LOOP-001 architecture outputs as the knowledge base
- Identifying modules, services, files, APIs, events, schemas, and ADRs directly relevant to the task
- Identifying and including dependency contracts for all relevant modules
- Selecting existing tests that define or constrain the task's acceptance criteria
- Selecting documentation directly referenced by relevant modules or the task description
- Applying context selection rules to produce a minimal, sufficient context package
- Validating the package for completeness, coherence, and size compliance
- Producing a reproducible, versioned context package in `docs/context/`
- Updating `STATUS-002.md` and `SKILL-002.md`

**Out of scope:**
- Making engineering decisions based on the assembled context (that belongs to LOOP-004 — Planning)
- Modifying source code or documentation
- Executing tests
- Resolving architectural ambiguities (ambiguities are recorded as unknowns and escalated)
- Fetching context from external systems outside the repository
- Generating embeddings or semantic indexes (this loop operates on explicit file selection, not vector retrieval)

**Maximum run duration:** 30 minutes. If the loop has not reached a Stop Condition within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Task description | Text (issue, ticket, PR description, or inline instruction) | Caller (engineer or upstream loop) | Required |
| Task type | Enumerated classification (see Step 2) | Derived in Step 2 or provided by caller | Optional — derived if absent |
| LOOP-001 outputs | Directory (`docs/architecture/`) | LOOP-001 | Required |
| `docs/architecture/architecture-overview.md` | File | LOOP-001 output | Required |
| `docs/architecture/module-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/service-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/api-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/event-catalog.md` | File | LOOP-001 output | Required |
| `docs/architecture/dependency-map.md` | File | LOOP-001 output | Required |
| `docs/architecture/repository-map.md` | File | LOOP-001 output | Required |
| `docs/architecture/technology-stack.md` | File | LOOP-001 output | Required |
| `docs/architecture/unknowns.md` | File | LOOP-001 output | Required |
| `docs/loops/core/SKILL-001.md` | File | LOOP-001 | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 | Required — freshness check |
| Context configuration | File (`.loop-002.yml` at repo root if present) | Repository | Optional |
| Prior context package | Directory (`docs/context/`) | Prior run of LOOP-002 | Optional — enables reuse detection |
| ADR directory | Directory (`docs/adr/` or configured equivalent) | Repository | Optional |

### Input Validation

Before Step 1 begins, the loop must verify:

- The task description is non-empty and contains sufficient signal to classify the task type.
- All required LOOP-001 outputs are present and readable.
- `STATUS-001.md` records a completed LOOP-001 run with a timestamp no older than the freshness threshold declared in `.loop-002.yml` (default: 7 days). If the LOOP-001 outputs are stale, the loop triggers GATE-1.
- No exclusive lock on `docs/context/` exists from a concurrent run of LOOP-002.

If any required input fails validation, the loop halts with `precondition_failed` and modifies no artefact.

---

## Outputs

All outputs are written to `docs/context/`. On first run, this directory is created. On subsequent runs, each file is replaced in full; prior content is preserved in git history.

| Artefact | Path | Description |
|----------|------|-------------|
| Context Package | `docs/context/context-package.md` | The primary deliverable: the assembled, validated, task-scoped context in a single navigable document, with a table of contents and section-level confidence annotations |
| Task Context | `docs/context/task-context.md` | Task classification, scope statement, affected modules, and the selection rationale for every included file |
| Architecture Context | `docs/context/architecture-context.md` | The subset of LOOP-001 architecture outputs relevant to this task: module boundaries, service topology, ADRs, and architectural constraints that apply |
| Dependency Context | `docs/context/dependency-context.md` | The dependency contracts — internal and external — that the task must respect: module interfaces, API contracts, event schemas, and build dependencies |
| Implementation Context | `docs/context/implementation-context.md` | The source files, configuration files, and domain model definitions directly required for the task |
| Verification Context | `docs/context/verification-context.md` | The test files, acceptance criteria, and verification standards that define what a correct implementation must satisfy |
| Context Metadata | `docs/context/context-metadata.md` | Package identifier, task ID, run ID, timestamp, LOOP-001 run ID consumed, file selection counts, token estimate, confidence score, and context size in bytes |
| Loop Status | `docs/loops/core/STATUS-002.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/core/SKILL-002.md` | Updated skill profile for this loop |
| Reflection | `docs/context/reflections/REFLECTION-002-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** This loop consumes the complete set of LOOP-001 outputs as its knowledge base. LOOP-001 must be in `Active` status. Its outputs must satisfy the freshness threshold. If LOOP-001 outputs are absent, this loop cannot run.

---

## Trigger

A run is initiated by any of the following:

1. **Upstream loop invocation** — A Core, Engineering, Platform, Governance, or Release loop requests a context package before beginning work. The requesting loop provides the task description and optionally the task type.
2. **Manual invocation** — An engineer explicitly triggers context assembly for a task.
3. **Task system event** — A configured integration delivers a task description from an issue tracker, PR, or work item system.
4. **Stale context detection** — A downstream loop detects that the context package in `docs/context/` is older than the configured reuse threshold (default: 1 hour) and requests a fresh package for the same task.

Trigger source, task description, and timestamp must be recorded in `STATUS-002.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | The task description is non-empty and classifiable | Attempt classification in Step 2; if classification fails, halt with `precondition_failed` |
| PRE-2 | All required LOOP-001 output files are present and readable | Verify existence and readability of each file listed in Inputs table as Required |
| PRE-3 | LOOP-001 outputs are within the configured freshness threshold | Read `STATUS-001.md` last-run timestamp; compare against threshold |
| PRE-4 | No concurrent LOOP-002 run is active for the same task | Check `STATUS-002.md` for `status: running`; if present, halt with `skipped_concurrent` |
| PRE-5 | The executing agent has write access to `docs/context/` | Attempt write of a temporary probe file; remove on success |
| PRE-6 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist at declared path |

If PRE-3 fails (stale LOOP-001 outputs), the loop records the failure in `STATUS-002.md` and triggers GATE-1 rather than halting with `precondition_failed` — a human must decide whether to accept stale architecture knowledge or require a LOOP-001 re-run first.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| `docs/architecture/` directory | Read | All LOOP-001 output files | Filesystem permissions of executing agent | Read-only; no modification to LOOP-001 outputs | N/A — read-only | Yes |
| Repository source files | Read | Files selected by context assembly steps | Same as executing agent | Read-only; scoped to selected files only | N/A — read-only | Yes |
| `docs/context/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/context/` restores prior state | Yes — each file is fully regenerated; re-running for the same task produces equivalent output |
| `docs/loops/core/STATUS-002.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/STATUS-002.md` | Yes |
| `docs/loops/core/SKILL-002.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-002.md` | Yes |
| ADR directory | Read | All ADR files | Same as executing agent | Read-only | N/A | Yes |

This loop makes no writes outside the repository. It does not call external APIs, write to databases, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/core/LOOP-002-Context-Assembly.md` — this document
3. `docs/loops/core/STATUS-002.md` — prior run state (if it exists)
4. `docs/architecture/architecture-overview.md` — system overview
5. `docs/architecture/module-catalog.md` — module registry
6. `docs/architecture/repository-map.md` — directory structure
7. `docs/loops/core/SKILL-001.md` — repository technology profile
8. `docs/context/context-metadata.md` — prior context package metadata (if it exists, for reuse detection)
9. `.loop-002.yml` — context configuration (if it exists)

The agent must not begin task classification until all available required context files have been read.

---

## Context Selection Rules

These rules govern which files and documents are included in the context package. They apply in order; a later rule may override an earlier one. The selection rationale for every file must be recorded in `task-context.md`.

### Priority Tiers

**Tier 1 — Required:** Must be included regardless of task type. Omitting any Tier 1 item is a verification failure.

| Item | Source |
|------|--------|
| Task description | Provided by caller |
| Affected module definitions (from module catalog) | `docs/architecture/module-catalog.md` |
| Module boundary declarations for all affected modules | Build files for affected modules |
| Dependency contracts for all direct dependencies of affected modules | `docs/architecture/dependency-map.md` + dependency build files |
| All ADRs in `accepted` status that name an affected module or its domain | `docs/adr/` |
| Coding convention files that apply to affected modules | Linting, formatter, checkstyle configs |
| Security constraint files that apply to affected modules | Security configuration, tenant filter declarations |

**Tier 2 — Recommended:** Included by default for the task type. May be excluded if the context configuration explicitly suppresses them and the confidence score remains above 80.

| Item | Applicable Task Types |
|------|--------------------|
| Existing tests for affected modules | All |
| API contracts for APIs owned by affected modules | Feature, Bug, Refactor, API change |
| Event schemas for events produced or consumed by affected modules | Feature, Bug, Event change |
| Database schema files for schemas owned by affected modules | Feature, Bug, Schema change |
| README files for affected modules | All |
| CI/CD pipeline stages that build or test affected modules | Release, Verification |
| Infrastructure configuration for affected services | Release, Deployment |
| Related ADRs in `proposed` status | Architecture, Governance |

**Tier 3 — Optional:** Included only when the context configuration enables them, or when Step 5 (dependency discovery) identifies a non-obvious coupling.

| Item | Condition for Inclusion |
|------|------------------------|
| Tests for modules that depend on affected modules | When a change to an affected module's interface is likely |
| Documentation for upstream dependencies | When the task requires understanding a dependency's behaviour, not just its contract |
| Historical implementation context (git log for affected files) | When the task description references a regression, a prior decision, or a known constraint |
| ADRs in `deprecated` or `superseded` status | When the task description references a past decision or when a currently accepted ADR supersedes one |
| Architecture diagrams | When the task involves structural changes or cross-service interaction |

**Tier 4 — Excluded:** Must never be included regardless of apparent relevance. Including any Tier 4 item is a verification failure.

| Item | Reason for Exclusion |
|------|---------------------|
| Source files from modules not identified as affected or as direct dependencies | Noise; increases token cost without improving output accuracy |
| Test files for unaffected modules | Noise |
| Generated build artefacts | Not authoritative; derived from source |
| Secrets values | Security; key names may be included but never values |
| Compiled binaries or lock file contents beyond dependency names and versions | Not human-readable; not useful for engineering decisions |
| LOOP-001 raw scan data (intermediate outputs) | Not a stable contract; use only the published architecture documents |
| Context packages from unrelated prior tasks | Cross-task contamination risk |

### Context Size Limits

The assembled context package must not exceed the size limits declared in `.loop-002.yml`. Defaults:

| Limit | Default Value |
|-------|---------------|
| Maximum total context size | 200,000 tokens (estimated) |
| Maximum number of source files | 50 |
| Maximum number of test files | 30 |
| Maximum number of documentation files | 20 |
| Warning threshold (triggers GATE-2) | 80% of any limit |

When a limit is approached, Step 9 (Remove Irrelevant Context) applies the pruning protocol before triggering a gate.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `CONTEXT-ASSEMBLER` | Maker | Steps 1–9: receives task, classifies it, loads architecture knowledge, discovers affected modules and dependencies, gathers files, prunes irrelevant context | Filesystem read, LOOP-001 output parsing, dependency graph traversal | Reports to GATE-1 and GATE-2 |
| `CONTEXT-CHECKER` | Checker | Step 10: independently validates completeness, coherence, and size compliance of the assembled context against all verification criteria | Filesystem read, cross-reference of selected files against module catalog and dependency map | Independent of CONTEXT-ASSEMBLER; cannot have participated in selection |
| `PACKAGE-WRITER` | Maker | Step 11: writes all output artefacts to `docs/context/` based on validated assembly | Filesystem write | Must not proceed until all gate conditions from Step 10 are cleared |
| `STATUS-WRITER` | Maker | Steps 12–13: updates STATUS-002.md and SKILL-002.md | Filesystem write | None — status writes occur after all gates are cleared |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews context confidence score, stale architecture concern, or missing critical files | Human judgment | Sole authority to approve or deny GATE-1 |

`CONTEXT-ASSEMBLER` and `CONTEXT-CHECKER` must be separate agent instances. `CONTEXT-CHECKER` must not have participated in file selection. No single agent may act as both Maker and Checker for the same artefact.

---

## Workflow

### Step 1 — Receive Task

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Task description, trigger metadata, prior context metadata (if present)  
**Outputs:** Normalised task record  

Read the task description provided by the caller. Normalise it into a task record containing: raw description, source (caller identity and trigger type), timestamp, and a unique task ID generated for this run. If a prior context package exists in `docs/context/` for a task with an identical or near-identical description, record the prior package metadata for reuse evaluation in Step 2. Write the task ID and trigger metadata to `STATUS-002.md` as the first act of the run.

---

### Step 2 — Classify Task

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Normalised task record from Step 1, `SKILL-001.md`  
**Outputs:** Task classification record  

Assign the task a primary type from the canonical taxonomy:

| Type | Description |
|------|-------------|
| `feature` | New capability being added to the system |
| `bug` | Defect in existing behaviour being corrected |
| `refactor` | Internal restructuring with no intended behaviour change |
| `test` | Test coverage being added or corrected |
| `documentation` | Documentation being created or updated |
| `architecture` | Structural or cross-cutting concern being changed |
| `security` | Security control being added, changed, or audited |
| `release` | Release preparation, packaging, or deployment |
| `dependency-update` | A library or platform dependency being changed |
| `schema` | Database or event schema being changed |
| `api-change` | A public or internal API contract being changed |
| `configuration` | Environment or runtime configuration being changed |
| `investigation` | Exploratory task; output is a findings document, not code |
| `unknown` | Cannot be classified; triggers GATE-1 |

A task may have one primary type and at most two secondary types. If the task cannot be classified with confidence above 60, assign `unknown` as the primary type and trigger GATE-1.

Evaluate context reuse: if a prior context package exists for the same task type and the same set of affected modules, and LOOP-001 outputs have not changed since the prior package was assembled, record `reuse_eligible = true`. Reuse does not bypass verification; it accelerates Steps 3–8.

Record: primary type, secondary types, classification confidence, reuse eligibility, and the keywords from the task description that drove each classification decision.

---

### Step 3 — Load Architecture Knowledge

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** All required LOOP-001 output files  
**Outputs:** In-memory architecture knowledge base  

Load all required LOOP-001 outputs into a structured in-memory knowledge base. Index by: module name, service name, API name, event name, schema name, ADR number. Record the LOOP-001 run ID from which this knowledge is drawn; this ID appears in `context-metadata.md` as the architecture knowledge provenance.

Check `docs/architecture/unknowns.md` for any open unknowns (`status: open`) that intersect with the task description. If any are found, record them as `architecture_unknowns_affecting_task`. These unknowns become candidates for GATE-1 escalation in Step 10.

---

### Step 4 — Discover Affected Modules

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Architecture knowledge base from Step 3, task description and classification from Step 2  
**Outputs:** Affected module list  

Identify all modules directly affected by the task. A module is directly affected if any of the following hold:

- The task description explicitly names the module, a class within it, a package within it, or a capability it owns.
- The task type is `bug` and the bug's described symptom is associated with the module's declared responsibilities in the module catalog.
- The task type is `api-change` or `schema` and the API or schema is owned by the module.
- The task type is `security` and the module owns a declared security boundary.
- The task type is `feature` and the module's declared responsibilities include the domain of the feature.

For each directly affected module, also identify all modules that declare a direct dependency on it in the dependency map (`dependent modules`). These are candidates for inclusion as Tier 2 or Tier 3 context depending on whether the task changes the affected module's interface.

Record: affected module IDs, the evidence for each selection, and the list of dependent modules identified.

---

### Step 5 — Identify Dependencies

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Affected module list from Step 4, `docs/architecture/dependency-map.md`, `docs/architecture/api-catalog.md`, `docs/architecture/event-catalog.md`  
**Outputs:** Dependency context record  

For each affected module, traverse the dependency map to identify:

**Outbound dependencies (what the affected module depends on):**
- Internal modules declared as dependencies: include their interface definitions as Tier 1 required context.
- External libraries declared as dependencies: include their name and version as Tier 1; include their documentation only if Tier 3 conditions apply.
- APIs consumed by the affected module: include their contracts as Tier 2.
- Events consumed by the affected module: include their schemas as Tier 2.
- Database schemas owned or read by the affected module: include schema files per task type rules.

**Inbound dependencies (what depends on the affected module):**
- Identify modules that consume the affected module's APIs or events: flag them as `potentially_impacted`.
- If the task type is `api-change`, `schema`, or `refactor`, include the contracts for all potentially impacted consumers as Tier 2 context.

Detect circular dependencies: if traversal of the dependency graph returns to an already-visited module, record the cycle and flag it as `circular_dependency_detected`. Do not traverse into a cycle; record the entry point of the cycle in `docs/context/dependency-context.md` and add it to the architecture unknowns list for this run.

Record: all selected dependencies with tier classification and selection rationale.

---

### Step 6 — Gather Implementation Files

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Affected module list, dependency context record, task classification, repository map  
**Outputs:** Implementation file list  

Select source files, configuration files, and domain model files for inclusion. Apply the Context Selection Rules strictly.

**Source files:** For each affected module, include all source files within the module's declared boundary that own or implement the capability named in the task. Do not include all files in the module; include only those with a documented or inferable relationship to the task. If the module contains more than 20 source files and the task scope is narrow (one class or one API endpoint), include only the files directly referenced.

**Configuration files:** Include environment configuration files that affect the affected modules. Include security configuration files for all affected modules. Include build files for all affected modules. Never include secret values.

**Domain model files:** Include ORM entity definitions, schema migration files, and value object definitions owned by affected modules, subject to task type. Include for task types: `feature`, `bug`, `schema`, `api-change`. Exclude for task types: `documentation`, `test`, `refactor` (unless the refactor changes the model).

**Historical context:** For task types `bug` and `refactor`, include the output of `git log --oneline -10 <affected_files>` for each affected file. This is Tier 3 context; include only if the token budget allows.

Record: each selected file with its tier, module ownership, and selection rationale.

---

### Step 7 — Gather Verification Assets

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Affected module list, task classification, repository map  
**Outputs:** Verification asset list  

Select test files and acceptance criteria for inclusion.

**Unit tests:** Include all unit test files that directly test classes or functions within the affected modules. These are Tier 2 for all task types.

**Integration tests:** Include integration test files that test the boundaries of affected modules (their APIs or event contracts). These are Tier 2 for task types `feature`, `bug`, `api-change`, `schema`. These are Tier 3 for task types `refactor`, `documentation`.

**Contract tests:** Include all contract test files that verify the affected module's compliance with declared API or event contracts. These are Tier 1 for task types `api-change`, `schema`, `feature` that adds a new API or event.

**End-to-end tests:** Include end-to-end test files only if the task description explicitly names a user-facing flow. These are Tier 3. Apply the file count limit strictly; prefer the most specific end-to-end test file over broader suites.

**Verification standards:** Always include `docs/loops/shared/verification-standards.md` as Tier 1. This defines the minimum verification bar that the task's output must meet.

Record: each selected test file with its tier, test type, module coverage, and selection rationale.

---

### Step 8 — Gather Documentation

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Affected module list, dependency context record, task classification, ADR directory  
**Outputs:** Documentation selection list  

Select documentation for inclusion.

**ADRs:** Include all ADRs in `accepted` status that name an affected module, its domain, or any technology the task will use. These are Tier 1. Include ADRs in `proposed` status for task types `architecture` and `governance`. Include superseded ADRs only if the task description references a prior decision.

**README files:** Include the README for each affected module (Tier 2, all task types). Include the repository root README if the task type is `architecture`, `release`, or `documentation`.

**Architecture documents:** Include `architecture-overview.md` and the sections of `module-catalog.md` and `service-catalog.md` pertaining to affected modules. These are Tier 1 for task types `feature`, `architecture`, `api-change`. These are Tier 2 for task types `bug`, `refactor`, `test`.

**API and event documentation:** Include the API catalog entries and event catalog entries for all APIs and events within the affected modules' scope. These are Tier 1 for task types `api-change`, `feature`, `schema`. These are Tier 2 for task types `bug`, `refactor`.

**Technical debt records:** Include `technical-debt.md` entries that reference affected modules. Tier 3 for all task types.

Record: each selected document with its tier, source path, and rationale.

---

### Step 9 — Remove Irrelevant Context

**Agent:** `CONTEXT-ASSEMBLER`  
**Inputs:** Implementation file list, verification asset list, documentation selection list  
**Outputs:** Pruned context assembly, token estimate, size flags  

Compute a token estimate for the assembled context using the estimated token count per file. Compare against the configured limits.

**If within limits:** Proceed to Step 10.

**If at or above the warning threshold (80% of any limit):** Apply the pruning protocol in order:

1. Remove all Tier 3 items. If now within limits, proceed.
2. Remove Tier 2 test files for modules that are `potentially_impacted` but not directly affected. If now within limits, proceed.
3. Remove Tier 2 documentation files beyond the README and directly applicable ADRs. If now within limits, proceed.
4. Remove Tier 2 source files for modules that are `potentially_impacted` but not directly affected. If now within limits, proceed.
5. If the context is still above the limit after all Tier 3 and non-critical Tier 2 items have been removed, trigger GATE-1. Do not remove Tier 1 items.

For every item removed, record the item path, its original tier, and the reason for removal in the pruning log. The pruning log is included in `context-metadata.md`.

Apply a final Tier 4 exclusion check: scan the assembled list against the Tier 4 exclusion criteria. Any item matching a Tier 4 criterion must be removed regardless of tier assignment. Record any Tier 4 removals separately.

---

### Step 10 — Validate Completeness

**Agent:** `CONTEXT-CHECKER`  
**Inputs:** Pruned context assembly from Step 9, architecture knowledge base from Step 3, all verification criteria  
**Outputs:** Checker validation report  

`CONTEXT-CHECKER` independently verifies all criteria in `## Verification`. The Checker must assess the assembly directly — not via the Assembler's self-report — by cross-referencing selected files against the module catalog and dependency map.

The Checker produces a validation report with: pass/fail per criterion, a list of any files that should be present but are absent, a list of any files present that violate Tier 4 exclusion rules, the measured token estimate, and an overall finding of `accepted` or `rejected`.

If the overall finding is `rejected`, the report must enumerate every unresolved violation. Control returns to `CONTEXT-ASSEMBLER` for one re-attempt at Steps 6–9. If the second attempt also produces a `rejected` finding, the loop triggers GATE-1.

Evaluate gate conditions after the Checker's finding:

- If overall confidence score is below 70, trigger GATE-1.
- If `architecture_unknowns_affecting_task` is non-empty and any unknown is in `open` status, trigger GATE-1.
- If stale LOOP-001 outputs were accepted at PRE-3 (via GATE-1 override), re-check here; if the task touches modules that changed after the LOOP-001 run date, trigger GATE-1.
- If token estimate exceeds 80% of any limit after pruning, trigger GATE-2.
- If classification confidence is below 80, trigger GATE-2.

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

**[GATE-1 — Hard Gate: Low Confidence, Missing Critical Files, or Stale Architecture]**

The loop halts. `STATUS-002.md` is updated to `status: awaiting_approval`. No context package is written until human approval is received. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: High Context Volume or Marginal Classification Confidence]**

The loop notifies and waits 4 hours. If no objection is received, it proceeds to Step 11. See `## Human Approval Gates` — GATE-2.

---

### Step 11 — Produce Context Package

**Agent:** `PACKAGE-WRITER`  
**Inputs:** Validated context assembly, Checker validation report, gate clearance  
**Outputs:** All files listed in the Outputs table  

Write each output artefact in full. No partial writes. Write in the following order so that the package document can reference the completed component documents:

1. `task-context.md` — task record, classification, affected modules, and complete file selection log with tier and rationale for every item
2. `dependency-context.md` — all dependency contracts, API contracts, event schemas, and the inbound/outbound dependency graph for affected modules
3. `implementation-context.md` — all selected source files, configuration files, and domain model definitions, assembled in dependency order (dependencies before dependents)
4. `verification-context.md` — all selected test files and acceptance criteria, grouped by test type
5. `architecture-context.md` — relevant excerpts from LOOP-001 architecture outputs: module boundaries, service topology, applicable ADRs, and architectural constraints
6. `context-metadata.md` — package identifier, task ID, run ID, LOOP-001 run ID consumed, file counts by tier, token estimate, confidence score, context size in bytes, pruning log, and the Checker validation report reference
7. `context-package.md` — the primary deliverable: a single navigable document with a table of contents, section-level confidence annotations, and the complete assembled context ordered for optimal reading by a downstream engineering agent. Each section must include its source path and tier classification.

Each file must include at its header: package ID, task ID, generation timestamp, run ID, and a note that it is a generated artefact maintained by LOOP-002.

---

### Step 12 — Update STATUS-002.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, gate outcomes, Checker report, final output file list  
**Outputs:** Updated `docs/loops/core/STATUS-002.md`  

Record all metrics declared in `## Metrics`. Record gate outcomes (gate ID, type, outcome, approver if applicable, timestamp). Record run status: `completed`, `failed`, or `stopped`. Record the package ID so downstream loops can reference the package that was current at the time of their execution. Record any open blockers: architecture unknowns that remain unresolved, denied gates with reason.

---

### Step 13 — Update SKILL-002.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Task classification, context assembly metrics, validation outcomes  
**Outputs:** Updated `docs/loops/core/SKILL-002.md`  

Update the skill profile with observations that improve future runs:

- Task types most frequently assembled (drives pre-computation opportunities)
- Modules most frequently included (candidates for permanent Tier 1 in configuration)
- Average token estimate by task type
- Pruning frequency by tier (high pruning of Tier 2 suggests limits are too tight; never pruning suggests limits are too loose)
- Context reuse rate: percentage of runs where `reuse_eligible = true` was honoured
- Validation pass rate on first attempt versus after re-try

---

## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `CONTEXT-CHECKER` without relying on `CONTEXT-ASSEMBLER`'s self-report.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | All Tier 1 items for the task type are present in the context package | Cross-reference assembled files against the Tier 1 table for the classified task type; assert no Tier 1 item is absent |
| VER-2 | No Tier 4 excluded items are present in the context package | Scan assembled file list against all Tier 4 exclusion criteria; assert zero matches |
| VER-3 | All modules named in the task description appear in the affected module list | Parse task description for module names; cross-reference against affected module list; assert all named modules are present |
| VER-4 | All direct dependencies of affected modules are represented in the dependency context | Cross-reference affected module dependency declarations against `dependency-context.md`; assert no declared dependency is absent |
| VER-5 | All applicable accepted ADRs are included | Query ADR directory for ADRs naming any affected module; assert all are present in `architecture-context.md` |
| VER-6 | Token estimate is within configured limits | Compute token estimate; assert below maximum limit |
| VER-7 | No secrets values are present in any output artefact | Scan all output files against secrets pattern list; assert zero matches |
| VER-8 | `context-metadata.md` records the LOOP-001 run ID from which architecture knowledge was drawn | Read metadata file; assert `architecture_run_id` field is present and non-empty |
| VER-9 | `context-package.md` contains a table of contents and section-level confidence annotations | Parse context package; assert presence of TOC and at least one confidence annotation per section |
| VER-10 | `STATUS-002.md` has been updated with the current run ID and a timestamp within 5 minutes of the current time | Read STATUS file; assert run ID and timestamp within tolerance |
| VER-11 | Every file in the context package has a recorded selection rationale in `task-context.md` | Cross-reference file list in package against selection log in task-context; assert one-to-one correspondence |
| VER-12 | `verification-context.md` includes at least one test file or acceptance criterion for all task types except `investigation` and `documentation` | Parse verification context; assert non-empty for applicable task types |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/context/reflections/REFLECTION-002-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Selection Summary:** count of files selected by tier, count pruned, final token estimate, and percentage of the limit consumed
- **Reuse Summary:** whether reuse was eligible, whether it was used, and the delta between the reused package and the freshly assembled package (if both were computed)
- **Confidence Summary:** overall context confidence score, classification confidence, and any per-section confidence annotations that fell below 80
- **Unknown Impact:** list of architecture unknowns that intersected with this task, their current status, and whether they caused a gate to fire
- **Gate Narrative:** for each gate that fired, the reason it fired and the human decision or auto-proceed outcome

---

## Human Approval Gates

### GATE-1 — Hard Gate: Low Confidence, Missing Critical Files, or Stale Architecture

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 10, before Step 11 |
| Artefact Under Review | Pruned context assembly, Checker validation report, confidence score |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; partial metadata written to `context-metadata.md` only; Reflection produced; human must either re-trigger LOOP-001 to refresh architecture knowledge, provide missing files, or resolve the blocking unknown before a new run |
| Audit Trail | Approval record written to `STATUS-002.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, decision, and any override rationale recorded |

**Fires when:**
- Overall context confidence score is below 70
- Task type is `unknown` (classification failed)
- Any open architecture unknown in `docs/architecture/unknowns.md` intersects with the task and remains unresolved
- LOOP-001 outputs are stale (older than freshness threshold) and a human decision is required on whether to proceed
- Checker validation rejected on both initial and retry attempts
- Token limit would be breached even after removing all Tier 3 and non-critical Tier 2 items
- Any Tier 1 required item cannot be located after exhaustive search

**Reviewer guidance:** Inspect the Checker validation report and the confidence score breakdown. If the assembly is correct despite the low score (e.g., the task is well-understood and the missing items are genuinely not applicable), approve with a written rationale. If architecture knowledge is stale, require a LOOP-001 re-run before approving. Record any file paths or ADR numbers that should be force-included in the next run in `STATUS-002.md` as correction notes.

---

### GATE-2 — Soft Gate: High Context Volume or Marginal Classification Confidence

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 10, before Step 11 |
| Artefact Under Review | Token estimate, classification confidence score |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-002.yml`; defaults to writing a summary to `STATUS-002.md` and creating a draft PR with the context metadata |
| Timeout | 4 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 11; `soft_gate_auto_proceeded: true` recorded in `STATUS-002.md` |
| Audit Trail | Notification timestamp, outcome (auto-proceeded or manually acted on), recorded under `gate_outcomes.GATE-2` |

**Fires when:**
- Token estimate is between 80% and 100% of the configured maximum (and GATE-1 did not also fire)
- Classification confidence is between 60 and 80 (and GATE-1 did not also fire)

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-002.md`. The executing agent must read `STATUS-002.md` at the start of each step and halt immediately if this value is present. On emergency stop: no further writes to `docs/context/` are made beyond the current step; a partial Reflection is produced; the Reflection records the step at which the stop was received and the state of all outputs at that moment.

---

## Failure Recovery

### FR-1 — Missing Source Files

**Detection:** Step 6 selects a file by path from the module catalog or repository map, but the file does not exist at that path in the repository.  
**Immediate Action:** Record the missing file path in a `missing_files` list. Do not halt.  
**Recovery:** If the missing file is Tier 1, trigger GATE-1. If the missing file is Tier 2 or Tier 3, continue assembly without it; record the gap in `context-metadata.md` and note it in `task-context.md` as a selection gap.  
**Rollback:** No output artefacts are affected; the missing file is simply absent from the package.

### FR-2 — Conflicting Documentation

**Detection:** Two documents make contradictory claims about the same module, API contract, or architectural decision (e.g., the module catalog and a README disagree on a module's responsibilities; two ADRs accept contradictory positions on the same concern).  
**Immediate Action:** Record the conflict as a context conflict entry. Include both conflicting documents in the context package with a conflict annotation. Do not silently resolve the conflict.  
**Recovery:** Record the conflict in `context-metadata.md`. If the conflict affects a Tier 1 item for the current task type, trigger GATE-1. Otherwise trigger GATE-2.  
**Rollback:** The conflicting items are included with annotation; downstream agents are informed of the conflict via the conflict entry in `context-package.md`.

### FR-3 — Unknown Architecture

**Detection:** Step 4 cannot identify any affected modules because no module in the catalog matches the task description.  
**Immediate Action:** Record `no_modules_identified = true`. Classify task type as `investigation` if not already classified.  
**Recovery:** Proceed with the architecture overview and repository map as the primary context. Trigger GATE-1. The human reviewer must either identify the correct modules or confirm that the task is exploratory and no module context is required.  
**Rollback:** The context package produced, if approved, uses only Tier 1 cross-cutting documents (architecture overview, technology stack, coding conventions) with no module-specific content.

### FR-4 — Circular Dependencies

**Detection:** Step 5 detects that dependency graph traversal returns to an already-visited module.  
**Immediate Action:** Record the cycle. Terminate traversal at the cycle entry point; do not traverse into the cycle.  
**Recovery:** Include the modules already traversed before the cycle was detected. Record the cycle in `dependency-context.md` as a `circular_dependency` annotation. Add the cycle to the architecture unknowns list for this task. If the task type is `refactor` or `architecture`, trigger GATE-1 (the circular dependency may be the subject of the task or may produce incorrect context).  
**Rollback:** The dependency context is complete for all modules outside the cycle; the cycle is explicitly documented.

### FR-5 — Excessive Context Size

**Detection:** After Step 9 pruning, the token estimate still exceeds the configured maximum.  
**Immediate Action:** Record `size_limit_exceeded = true`.  
**Recovery:** Trigger GATE-1. The human reviewer must either increase the limit in `.loop-002.yml`, narrow the task scope, or explicitly approve assembly with a reduced Tier 1 set. Do not silently drop Tier 1 items.  
**Rollback:** No context package is written until GATE-1 is resolved.

### FR-6 — Incomplete Discovery (LOOP-001 Outputs Stale or Partial)

**Detection:** PRE-3 fails (LOOP-001 outputs older than freshness threshold) or LOOP-001 `STATUS-001.md` records `status: stopped` or `status: failed` for its last run.  
**Immediate Action:** Record `architecture_knowledge_incomplete = true`. Trigger GATE-1 immediately.  
**Recovery:** The human reviewer decides whether to: (a) trigger a fresh LOOP-001 run and wait for its completion, (b) proceed with the understanding that architecture knowledge may be stale, or (c) abort this task until LOOP-001 is re-run. Record the decision in `STATUS-002.md`.  
**Rollback:** No context package is written until the GATE-1 decision is recorded.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-002.md` at Step 12.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 13) |
| `run.steps_total` | 13 |
| `gate.hard.count` | Hard gates reached during this run |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-12 criteria that passed |
| `verification.level1.fail` | Count of VER-1 through VER-12 criteria that failed |
| `reflection.produced` | Boolean — was the Reflection artefact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `task.type_primary` | Primary task type classification |
| `task.classification_confidence` | Classification confidence score (0–100) |
| `context.files_selected_tier1` | Count of Tier 1 files included |
| `context.files_selected_tier2` | Count of Tier 2 files included |
| `context.files_selected_tier3` | Count of Tier 3 files included |
| `context.files_pruned_tier2` | Count of Tier 2 files removed during pruning |
| `context.files_pruned_tier3` | Count of Tier 3 files removed during pruning |
| `context.files_excluded_tier4` | Count of Tier 4 violations detected and removed |
| `context.token_estimate` | Estimated token count of final context package |
| `context.token_limit_pct` | Token estimate as percentage of configured maximum |
| `context.size_bytes` | Total byte size of all output artefacts |
| `context.modules_affected` | Count of directly affected modules |
| `context.modules_potentially_impacted` | Count of potentially impacted (inbound dependency) modules |
| `context.missing_files` | Count of files referenced but not found in repository |
| `context.conflict_count` | Count of documentation conflicts detected |
| `context.unknown_count` | Count of open architecture unknowns intersecting this task |
| `context.reuse_eligible` | Boolean — was this package eligible for reuse |
| `context.reuse_used` | Boolean — was a prior package reused rather than fully assembled |
| `context.confidence_score` | Overall context confidence score (0–100) |
| `discovery.duration_seconds` | Time spent on Steps 3–8 (file discovery and selection) |

---

## Risks

### RISK-1 — Context Drift

- **Description:** The assembled context package is accurate at assembly time but becomes stale as the repository evolves. Downstream loops that cache or reuse the package after repository changes may operate on incorrect context.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Repository files included in the context package are modified after the package is assembled.
- **Control:** `context-metadata.md` records the git HEAD SHA at assembly time. Downstream loops must compare the current HEAD SHA against the recorded SHA before consuming a cached package. The reuse threshold (default: 1 hour) limits the window of staleness.
- **Detection:** HEAD SHA mismatch between assembly time and consumption time.
- **Response:** Downstream loop triggers a fresh LOOP-002 run for the same task. The prior package is not consumed.

### RISK-2 — Overloaded Context (Token Budget Exceeded)

- **Description:** An excessively large context package degrades agent performance, increases cost, and reduces the signal-to-noise ratio of the instructions received by the downstream agent.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Task scope is broad; module catalog is large; many dependencies are identified.
- **Control:** Token estimation in Step 9; tiered pruning protocol; configurable limits in `.loop-002.yml`; GATE-1 if limits cannot be met.
- **Detection:** Token estimate exceeds 80% of limit.
- **Response:** FR-5 procedure; GATE-1 if pruning cannot resolve.

### RISK-3 — Missing Dependencies

- **Description:** The dependency graph in LOOP-001 outputs is incomplete, causing LOOP-002 to omit a dependency contract from the context package. The downstream agent violates the omitted contract.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** LOOP-001 discovery confidence for dependencies was low; generated code introduces non-obvious runtime dependencies.
- **Control:** VER-4 cross-references selected dependencies against the module catalog declarations. Dependency confidence is inherited from LOOP-001 confidence scores and reported in `context-metadata.md`.
- **Detection:** VER-4 failure; downstream agent reports an unexpected dependency error.
- **Response:** FR-1 procedure for missing files; trigger LOOP-001 re-run if dependency confidence is low.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads only the local repository filesystem and writes only to `docs/context/`. It accesses no tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Secrets Exposure in Context Package

- **Description:** Configuration files included in the context package contain committed secrets that are then passed to a downstream agent, amplifying their exposure.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Repository contains secrets committed to source files that are selected as implementation context.
- **Control:** VER-7 scans all output artefacts for secrets patterns before any file is written. Configuration file values are never written verbatim; only structure and key names are included.
- **Detection:** VER-7 failure.
- **Response:** Halt Step 11. Do not write the affected artefact. Trigger GATE-1. Record the detection event in `STATUS-002.md` without reproducing the secret value.

### RISK-6 — Incorrect Assumptions from Stale Architecture

- **Description:** If LOOP-001 outputs are stale, the module catalog, dependency map, or API catalog may not reflect the current repository, causing the context package to reference modules or contracts that have changed or been removed.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 has not run since a significant merge; the freshness threshold has been exceeded.
- **Control:** PRE-3 enforces the freshness threshold. FR-6 procedure triggers GATE-1 on stale detection.
- **Detection:** Freshness check failure in PRE-3.
- **Response:** FR-6 procedure; human decides whether to force a LOOP-001 re-run.

### RISK-7 — Hidden Architectural Coupling

- **Description:** Two modules share a runtime coupling (shared database table, shared cache key, implicit event consumer) that is not recorded in the dependency map. The context package omits the coupled module, and the downstream agent's change breaks the coupling.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 discovery of hidden coupling was incomplete; modules communicate through shared infrastructure rather than declared interfaces.
- **Control:** Step 5 explicitly checks for inbound dependencies (modules that consume the affected module). `docs/architecture/unknowns.md` should contain hidden-coupling unknowns if they were detected by LOOP-001. These unknowns trigger GATE-1 when they intersect with the task.
- **Detection:** Open unknown in `unknowns.md` describing a coupling involving an affected module.
- **Response:** GATE-1; human reviewer confirms whether the hidden coupling must be resolved before the task proceeds.

### RISK-8 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent; re-running for the same task produces an equivalent package.
- **Likelihood:** N/A
- **Impact:** N/A

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | Task has been classified with a primary type other than `unknown` |
| SC-2 | All verification criteria VER-1 through VER-12 have passed |
| SC-3 | All seven output artefacts listed in the Outputs table have been written |
| SC-4 | Token estimate is within the configured maximum limit |
| SC-5 | `STATUS-002.md` has been updated with run metrics and final status |
| SC-6 | `SKILL-002.md` has been updated |
| SC-7 | The Reflection artefact has been written to `docs/context/reflections/` |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (30 minutes) reached before SC-1 through SC-7 are met |
| SC-9 | GATE-1 is denied by the human reviewer |
| SC-10 | PRE-4 detects a concurrent run; this instance exits without modifying any artefact |
| SC-11 | An Emergency Stop signal is received in `STATUS-002.md` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Context Artefacts:**
- [ ] `docs/context/context-package.md` written with table of contents and section-level confidence annotations
- [ ] `docs/context/task-context.md` written with complete file selection log and rationale
- [ ] `docs/context/architecture-context.md` written with relevant LOOP-001 excerpts and ADRs
- [ ] `docs/context/dependency-context.md` written with all dependency contracts and inbound/outbound graph
- [ ] `docs/context/implementation-context.md` written with source and configuration files in dependency order
- [ ] `docs/context/verification-context.md` written with test files and acceptance criteria by test type
- [ ] `docs/context/context-metadata.md` written with package ID, provenance, metrics, pruning log, and Checker report reference

**Verification:**
- [ ] All VER-1 through VER-12 criteria assessed and outcomes recorded in Reflection
- [ ] Checker validation report produced by `CONTEXT-CHECKER`
- [ ] VER-7 (secrets scan) passed on all output artefacts
- [ ] VER-2 (Tier 4 exclusion) passed — no prohibited items in package

**Gates:**
- [ ] Gate outcome recorded in `STATUS-002.md` for every gate that fired

**State:**
- [ ] `docs/loops/core/STATUS-002.md` updated with all required metrics, package ID, and final status
- [ ] `docs/loops/core/SKILL-002.md` updated with current assembly profile

**Reflection:**
- [ ] `docs/context/reflections/REFLECTION-002-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---

## Future Improvements

- **Semantic relevance scoring:** Supplement explicit tier classification with a semantic similarity score between each candidate file and the task description, using the repository's own test suite as a calibration corpus.
- **Context fingerprinting:** Generate a deterministic fingerprint of the context package inputs (task description hash, HEAD SHA, affected module list) to enable fast reuse validation without full re-assembly.
- **Task decomposition:** When a task description contains multiple independent concerns (e.g., a bug fix and a documentation update), automatically decompose it into sub-tasks and assemble a separate context package for each, subject to aggregate token limits.
- **Dependency graph caching:** Cache the traversed dependency graph between runs within a session to eliminate redundant traversal for tasks affecting overlapping modules.
- **Pruning intelligence:** Track which Tier 2 and Tier 3 items were actually referenced by downstream agents (via Reflection feedback) and use this signal to calibrate tier assignments and pruning order.
- **Context diff mode:** When reuse is eligible, produce a delta document listing only what changed between the prior package and the freshly assembled one, enabling downstream agents to perform incremental updates rather than full context re-ingestion.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — the dependency loop whose outputs are the primary knowledge base for this loop
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure
- `docs/loops/templates/REVIEW-TEMPLATE.md` — review record structure

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-002 as the context assembly loop for the AI Engineering Operating System, consuming LOOP-001 outputs and producing task-scoped context packages for all downstream engineering loops.

