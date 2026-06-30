---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-001-Architecture-Discovery.md
Original Version: 1.1
Extraction Date: 2026-06-27
Original Purpose: Discover and index repository modules and architectural boundaries.
Generalized Purpose: Discover and index repository modules and architectural boundaries.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-001 — Architecture Discovery

**Loop ID:** LOOP-001  
**Name:** Architecture Discovery  
**Version:** 1.1  
**Status:** Active  
**Category:** Core  
**Depends On:** None  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-001 builds and continuously maintains a verified, structured understanding of this repository's architecture. It produces a canonical set of architecture documents that all other loops must treat as authoritative context before performing engineering work. It is the entry point of the AI Engineering Operating System: no loop may make structural assumptions about the codebase that are not grounded in this loop's outputs.

---

## Problem Statement

AI agents operating on a codebase without verified architectural context produce outputs that are locally correct but globally inconsistent. They miss module boundaries, duplicate abstractions, violate layering rules, misattribute dependencies, and generate documentation that contradicts the actual system. As a repository evolves, documentation drifts, unknown components accumulate, and the cost of re-establishing context at the start of each task compounds. Without a repeatable, auditable discovery mechanism, every loop begins from an uncertain baseline.

---

## Why This Loop Exists

Architecture discovery is inherently exploratory and produces outputs that decay over time. Codifying it as a loop makes the discovery process repeatable, its outputs verifiable, its gaps explicit, and its evolution traceable. Every loop in the Engineering Operating System depends on knowing what exists before deciding what to do. This loop provides that foundation. It must be run to completion, or its outputs must be current, before any Core, Engineering, Platform, Governance, or Release loop executes.

---

## Scope

**In scope:**
- Scanning all files within the repository root and its submodules
- Classifying repository structure: modules, services, libraries, packages, build artifacts
- Identifying technology stack, runtime architecture, and deployment model
- Cataloguing public and internal APIs, event flows, database schemas, and external integrations
- Extracting architectural decisions from ADRs, README files, and inline documentation
- Identifying domain model boundaries and coding conventions
- Assessing CI/CD pipeline, test strategy, observability, and security model
- Recording unknown areas, missing documentation, and technical debt
- Detecting drift between the current state of the repository and the previously recorded architecture

**Out of scope:**
- Modifying source code
- Creating or modifying CI/CD pipelines
- Executing tests
- Making architectural recommendations (that belongs to LOOP-004 — Planning)
- Resolving unknowns (discovery records them; resolution requires human input or a dedicated loop)
- Infrastructure provisioning or deployment

**Maximum run duration:** 4 hours. If the loop has not reached a Stop Condition within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| Repository root path | Directory path | Executing environment | Required |
| Previous discovery outputs | Directory (`docs/architecture/`) | Prior run artifacts | Optional — absence indicates first run |
| Previous `STATUS-001.md` | File (`docs/loops/core/STATUS-001.md`) | Prior run artifact | Optional |
| Discovery configuration | File (`.loop-001.yml` at repo root if present) | Repository | Optional |
| Explicit exclusion list | Configuration key `exclude_paths` in `.loop-001.yml` | Repository | Optional |
| Human-provided annotations | Inline `# ARCH:` comments in source files | Repository source | Optional |
| ADR directory | Directory (`docs/adr/` or configured equivalent) | Repository | Optional |

### Input Validation

Before any scan step begins, the loop must verify:
- The repository root is readable and is a valid git repository.
- If prior outputs exist, they are parseable and their recorded version is known.
- No exclusive lock on `docs/architecture/` exists from a concurrent run.

If any required input fails validation, the loop must halt with a `precondition_failed` status and must not modify any artifact.

---

## Outputs

All outputs are written to `docs/architecture/`. On first run, this directory is created. On subsequent runs, each file is updated in place; prior content is preserved in git history.

| Artifact | Path | Description |
|----------|------|-------------|
| Architecture Overview | `docs/architecture/architecture-overview.md` | Narrative summary of system purpose, runtime topology, and primary architectural patterns |
| Repository Map | `docs/architecture/repository-map.md` | Canonical directory tree with annotations: module type, ownership, and status |
| Technology Stack | `docs/architecture/technology-stack.md` | All languages, frameworks, runtimes, build tools, and infrastructure technologies with versions |
| Module Catalog | `docs/architecture/module-catalog.md` | Every module with boundaries, responsibilities, and inter-module dependencies |
| Service Catalog | `docs/architecture/service-catalog.md` | Every deployable service with runtime contract, dependencies, and deployment model |
| API Catalog | `docs/architecture/api-catalog.md` | All public and internal APIs with protocol, stability classification, and owning module |
| Event Catalog | `docs/architecture/event-catalog.md` | All event types, producers, consumers, and transport mechanisms |
| Dependency Map | `docs/architecture/dependency-map.md` | Build-time and runtime dependency graph, including external dependencies with versions |
| Architecture Diagrams | `docs/architecture/architecture-diagrams.md` | Textual diagram sources (Mermaid) for system context, container, and component views |
| Technical Debt | `docs/architecture/technical-debt.md` | Catalogued debt items with location, description, estimated severity, and age |
| Unknowns | `docs/architecture/unknowns.md` | Components, patterns, or decisions that could not be classified with confidence |
| Loop Status | `docs/loops/core/STATUS-001.md` | Run status, metrics, and open blockers for this loop |
| Loop Skill | `docs/loops/core/SKILL-001.md` | Updated skill profile derived from this run's discovery |
| Run Metadata | `docs/architecture/metadata/METADATA-001-{run-id}.md` | Provenance record: run ID, HEAD SHA at start and end, upstream dependency run IDs (none for LOOP-001), elapsed duration seconds, final status; consumed by downstream loops for freshness checks |
| Reflection | `docs/architecture/reflections/REFLECTION-001-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

None. This is the foundational loop. All other loops depend on its outputs; it depends on none.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers the loop (first run, ad-hoc refresh).
2. **Scheduled execution** — A recurring schedule (recommended: once per release cycle or once per week, whichever is more frequent).
3. **Repository event** — A pull request is merged to the main branch that modifies any of: `build.gradle`, `pom.xml`, `package.json`, `Dockerfile`, `docker-compose.yml`, `*.tf`, `*.yml` in CI/CD directories, or any file in `docs/adr/`.
4. **Upstream signal** — Another loop declares a dependency on fresh LOOP-001 outputs and the current outputs are older than 7 days.

Trigger source and timestamp must be recorded in `STATUS-001.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | The executing agent has read access to the entire repository | Verify `git status` exits cleanly from the root |
| PRE-2 | The executing agent has write access to `docs/architecture/` | Attempt write of a temporary probe file; remove on success |
| PRE-3 | No other instance of LOOP-001 is currently running | Check `STATUS-001.md` for status `running`; if present, halt |
| PRE-4 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist at declared path |
| PRE-5 | Git is available and the repository has at least one commit | `git log -1` exits successfully |

If PRE-3 fails (a concurrent run is detected), the loop must not start. It records a `skipped_concurrent` entry in `STATUS-001.md` and exits cleanly.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| Repository filesystem | Read | All files under repo root | Filesystem permissions of executing agent | Scoped to this repository's path; no cross-repo reads | No writes to source; read-only | Yes |
| `docs/architecture/` directory | Write | All files listed in Outputs table | Same as executing agent | All writes confined to this directory | Prior file content preserved in git; `git checkout docs/architecture/` restores prior state | Yes — each file is fully regenerated from source; re-running produces equivalent output |
| `docs/loops/core/STATUS-001.md` | Read-Write | Single file | Same as executing agent | Single file; no cross-loop state | `git checkout docs/loops/core/STATUS-001.md` | Yes |
| `docs/loops/core/SKILL-001.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-001.md` | Yes |
| Git history | Read | Current branch log and blame | Filesystem permissions | Read-only; no commits made by this loop | N/A | Yes |

This loop makes no writes to any external system outside the repository. It does not call external APIs, write to databases, or trigger deployments.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — the governing standard for loop execution
2. `docs/loops/core/LOOP-001-Architecture-Discovery.md` — this document
3. `docs/loops/core/STATUS-001.md` — prior run state (if it exists)
4. `docs/architecture/architecture-overview.md` — prior architecture summary (if it exists)
5. `docs/architecture/unknowns.md` — prior unknowns list (if it exists)
6. `.loop-001.yml` at repo root — discovery configuration (if it exists)
7. Output of `git log --oneline -20` — recent commit context
8. Output of `git diff --name-only HEAD~1 HEAD` — files changed since last commit

The agent must not begin scanning until all available context files have been read.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `ARCH-SCANNER` | Maker | Steps 1–8: load state, scan repository, classify artifacts, discover architecture, compute drift and gate conditions | Filesystem read, git CLI, directory traversal, file parsing | Reports to GATE-1 and GATE-2 |
| `ARCH-CHECKER` | Checker | Step 6: independently validates scanner outputs against all verification criteria before documentation is written | Filesystem read, cross-reference of scanner outputs against source | Independent of ARCH-SCANNER; finding reviewed at GATE-1 if checker triggers retry |
| `DOC-WRITER` | Maker | Steps 9–10: writes all output artifacts to `docs/architecture/` based on validated findings | Filesystem write | Must not proceed until gate conditions from Step 8 are cleared |
| `STATUS-WRITER` | Maker | Steps 11–12: updates STATUS-001.md and SKILL-001.md | Filesystem write | None — status writes occur after all gates are cleared |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews architecture findings and drift report before documentation is written | Human judgment | Sole authority to approve or deny GATE-1 |

`ARCH-SCANNER` and `ARCH-CHECKER` must be separate agent instances. `ARCH-CHECKER` must not have produced any of the content it reviews. No single agent may act as both Maker and Checker for the same artifact.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

### Step 1 — Load Previous Discovery State

**Agent:** `ARCH-SCANNER`  
**Inputs:** `STATUS-001.md`, all files in `docs/architecture/` (if present)  
**Outputs:** In-memory prior state snapshot  

Read `STATUS-001.md`. Extract: last run ID, last run date, last known module count, last known service count, last coverage percentage. If no prior state exists, record `first_run = true`. Read all existing architecture documents and construct a prior-state index: modules known, services known, APIs known, unknowns previously recorded. Record the git commit SHA of HEAD at this moment; this SHA is used in Step 8 to detect concurrent repository changes.

---

### Step 2 — Detect Repository Changes Since Last Run

**Agent:** `ARCH-SCANNER`  
**Inputs:** Git history, prior run date from Step 1  
**Outputs:** Change manifest (list of changed files, change categories)  

Run `git log` from the last run date to HEAD. Classify each changed file into one of: `build-system`, `source-code`, `infrastructure`, `ci-cd`, `documentation`, `adr`, `configuration`, `test`, `generated`, `unknown`. Compute a change magnitude score: the ratio of changed files to total repository files. Record the change manifest for use in Step 7. If `first_run = true`, the change manifest covers all files.

---

### Step 3 — Scan Repository

**Agent:** `ARCH-SCANNER`  
**Inputs:** Repository root, exclusion list from configuration  
**Outputs:** Raw file inventory with metadata  

Traverse the repository from root. For every file, record: path, extension, size, last-modified date, owning module (inferred from directory structure), and file category (source, test, config, doc, generated, infra, build). Apply the exclusion list. Record the total file count, directory count, and any files that could not be read. Flag any directories with no readable files as `scan_gap`. Enforce limits: maximum directory depth of 20, maximum file count of 500,000; flag `scan_ceiling_reached` if either limit is hit.

---

### Step 4 — Classify Artifacts

**Agent:** `ARCH-SCANNER`  
**Inputs:** Raw file inventory from Step 3  
**Outputs:** Classified artifact inventory  

Assign each file to an artifact category:

- **Module boundary files:** `build.gradle`, `pom.xml`, `package.json`, `go.mod`, `Cargo.toml`, `pyproject.toml`, and equivalents
- **Service entry points:** `main` classes, `Application` classes, service bootstrap files, Dockerfile targets
- **API definitions:** OpenAPI specs, GraphQL schemas, gRPC proto files, REST controller annotations, interface declarations marked as public API
- **Event definitions:** event classes, message schema files, queue/topic configuration
- **Schema files:** SQL migrations, ORM entity definitions, schema registry entries
- **Infrastructure:** Terraform files, Helm charts, Kubernetes manifests, Docker Compose files
- **CI/CD:** pipeline definitions, workflow files, Makefile targets with deploy or release semantics
- **ADRs:** files matching `ADR-NNN-*.md` pattern in the configured ADR directory
- **Documentation:** README files, architecture docs, inline documentation blocks
- **Test files:** unit, integration, end-to-end, and contract test files
- **Generated files:** files with generation headers, files in `generated/` or `build/` directories
- **Configuration:** environment-specific config files; secrets references (key names recorded, values never read)
- **Unknown:** files that do not match any category above

---

### Step 5 — Discover Architecture

**Agent:** `ARCH-SCANNER`  
**Inputs:** Classified artifact inventory from Step 4  
**Outputs:** Architecture findings document (in-memory)  

For each classified artifact category, extract structured findings:

**Modules and Services:** Parse build files to extract module names, declared dependencies, and plugin configurations. Infer module type (library, service, batch job, CLI, shared kernel) from entry points and build targets. Record each module with: ID, type, primary language, declared dependencies, and inferred responsibility derived from package names, README content, and class naming patterns.

**APIs:** Extract all API definitions. For each: protocol (REST, gRPC, GraphQL, messaging), stability classification (public/internal/experimental, inferred from annotations or documentation), owning module, and available operation names or HTTP methods. Record endpoints without specifications as `undocumented_api`.

**Events:** Extract event type names, producers (classes or components that publish or emit), consumers (classes or components that subscribe or listen), and transport mechanism (topic name, exchange name, in-process bus). Record event flows as directed producer-to-consumer pairs.

**Database Schemas:** Extract entity names, table names from migrations, and ORM mappings. Record database technology and owning module for each schema. Flag schema files with no corresponding entity or migration as `schema_orphan`.

**Technology Stack:** Extract all declared dependency versions from build files. Classify into: language runtime, web framework, persistence, messaging, security, observability, testing, build tooling, infrastructure tooling. Record the version for each.

**ADRs:** Parse each ADR file. Extract: ADR number, title, status (proposed/accepted/deprecated/superseded), and the decision summary. Record superseded ADRs with their replacement reference.

**Security Model:** Identify authentication and authorization mechanisms from dependency names, filter class names, and security configuration files. Record: auth mechanism type, multi-tenancy approach if applicable, and all security configuration file paths.

**Observability:** Identify logging framework, metrics library, tracing library, and health check endpoints from dependency declarations and source patterns.

**CI/CD:** Parse pipeline files. Extract: pipeline stages, test stages, build stages, deployment stages, environment targets, and required secret names (names only — values are never read or recorded).

**Coding Conventions:** Identify linting configuration files, formatter configuration, checkstyle rules, and any architectural fitness function tests present in the test suite.

**Technical Debt:** Identify: `TODO`, `FIXME`, `HACK`, `XXX` annotations in source code; deprecated dependency usage; modules with no test files; services with no README; public APIs with no specification file; ADRs in `proposed` status older than 30 days.

**Unknowns:** Record any component, pattern, or file that cannot be classified or whose purpose cannot be inferred. Unknowns are first-class outputs; suppressing them to improve coverage metrics is a verification failure.

Compute a **discovery confidence score** (0–100) for each major finding category: modules, services, APIs, events, schemas. Confidence reflects evidence quality: explicit declarations score high, inference from naming patterns scores medium, inference from directory structure alone scores low.

---

### Step 6 — Validate Findings

**Agent:** `ARCH-CHECKER`  
**Inputs:** Architecture findings document from Step 5, raw file inventory from Step 3  
**Outputs:** Checker validation report  

`ARCH-CHECKER` independently verifies:

1. Every build file in the inventory has a corresponding module entry in the findings.
2. Every service entry point has a corresponding service record.
3. Every API definition file has a corresponding API catalog entry.
4. Every event class has a producer and at least one consumer recorded, or is explicitly flagged as `orphan_event`.
5. The technology stack list is consistent with all dependency files parsed — no dependency file is unread.
6. No classified item with evidence strength above 80% has been left in the `unknown` category.
7. The total classified files in findings equals total files in inventory minus excluded files.
8. No secrets values appear in any in-memory findings that would be written to output.

The Checker produces a written validation report with: pass/fail per criterion, discrepancy list, and an overall finding of `accepted` or `rejected`. If `rejected`, the report must enumerate every unresolved discrepancy.

If the Checker returns `rejected`, control returns to `ARCH-SCANNER` for a Step 5 re-execution (maximum one retry). If the second attempt also produces `rejected`, the loop triggers GATE-1.

---

### Step 7 — Compare Against Previous State

**Agent:** `ARCH-SCANNER`  
**Inputs:** Validated architecture findings from Step 6, prior state snapshot from Step 1  
**Outputs:** Drift report  

If `first_run = true`, skip this step and proceed with an empty drift report.

Otherwise, compute a drift report:

- **Added:** items present in current findings but absent from prior state
- **Removed:** items present in prior state but absent from current findings
- **Changed:** items present in both states where recorded metadata differs (version, type, dependency list, protocol, stability)
- **Drift magnitude:** count of added + removed + changed items across all categories
- **Coverage delta:** current documentation coverage percentage minus prior coverage percentage

Flag the run as `significant_change = true` if any of the following conditions holds:
- A module has been removed
- A public API has been removed or its protocol has changed
- Drift magnitude exceeds 20 items
- A previously documented service no longer has a discoverable entry point

---

### Step 8 — Evaluate Gate Conditions

**Agent:** `ARCH-SCANNER`  
**Inputs:** Drift report from Step 7, Checker validation report from Step 6, architecture findings, git HEAD SHA  
**Outputs:** Gate decision  

Verify the current git HEAD SHA matches the SHA recorded in Step 1. If it has changed, flag `concurrent_change_detected = true` and trigger GATE-1 unconditionally.

Otherwise evaluate in priority order (highest priority first):

1. If any discovery confidence score is below 60, trigger **GATE-1**.
2. If `significant_change = true`, trigger **GATE-1**.
3. If unknown count increased by more than 10 since the prior run, trigger **GATE-1**.
4. If the Checker validation required a Step 5 retry, trigger **GATE-2**.
5. If unknown count increased by 1–10 since the prior run, trigger **GATE-2**.
6. If none of the above, proceed directly to Step 9.

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

**[GATE-1 — Hard Gate: Significant Change or Low Confidence]**

The loop halts. `STATUS-001.md` is updated to `status: awaiting_approval`. No documentation artifact is written until human approval is received and recorded. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: Minor Unknowns Increase or Checker Retry]**

The loop notifies and sets a 24-hour timer. If no objection is received, it proceeds to Step 9 with `soft_gate_auto_proceeded` recorded. See `## Human Approval Gates` — GATE-2.

---

### Step 9 — Update Documentation

**Agent:** `DOC-WRITER`  
**Inputs:** Validated architecture findings, Checker validation report, drift report, gate clearance  
**Outputs:** All files listed in the Outputs table  

Write each output artifact in full. Partial updates are not permitted; each file must be complete and internally consistent when written. Write in dependency order so that the overview can reference the completed catalogs:

1. `technology-stack.md`
2. `module-catalog.md`
3. `service-catalog.md`
4. `api-catalog.md`
5. `event-catalog.md`
6. `dependency-map.md`
7. `architecture-diagrams.md` (Mermaid diagram sources derived from module and service catalogs)
8. `repository-map.md`
9. `technical-debt.md`
10. `unknowns.md`
11. `architecture-overview.md` (written last; synthesises all other documents)

Each file must include at its header: generation date, run ID, loop version, and a note that it is a generated artifact maintained by LOOP-001.

---

### Step 10 — Record Unknowns

**Agent:** `DOC-WRITER`  
**Inputs:** Unknowns list from architecture findings  
**Outputs:** Updated `unknowns.md`  

`unknowns.md` is the authoritative registry of architectural uncertainty. Each unknown must be recorded with:

- Unknown ID (`UNK-NNN`, sequential, never reused)
- Description of what could not be classified or inferred
- Location: file path or directory
- Discovery confidence: why this could not be resolved automatically
- First seen: run ID in which this unknown was first recorded
- Status: `open` | `in-progress` | `resolved`
- Resolution: populated only when status changes to `resolved`

Unknowns from prior runs whose subjects are now classifiable must be updated to `resolved` with the current run ID as resolution reference. Unknowns must never be silently deleted.

---

### Step 11 — Update STATUS-001.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, gate outcomes, Checker report, final output file list  
**Outputs:** Updated `docs/loops/core/STATUS-001.md`  

Record all metrics declared in `## Metrics`. Record gate outcomes (gate ID, type, outcome, approver, timestamp). Record run status: `completed`, `failed`, or `stopped`. Record run end timestamp. Record any open blockers: unresolved unknowns that require human input, denied gates with reason.

---

### Step 12 — Update SKILL-001.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Architecture findings, technology stack, module catalog  
**Outputs:** Updated `docs/loops/core/SKILL-001.md`  

`SKILL-001.md` is the skill profile that communicates repository characteristics to other loops. Update:

- Primary languages and versions
- Web and application frameworks
- Build system and tooling
- Module count and service count
- Test frameworks present
- CI/CD platform and stages
- Infrastructure type (containerised, serverless, bare metal)
- Security model summary (auth mechanism, multi-tenancy approach)
- Observability stack (logging, metrics, tracing)
- Architectural patterns identified (hexagonal, event-driven, monolith, microservices, etc.)
- Any patterns that other loops must account for when generating code, tests, or documentation

---

### Step 13 — Evaluate Stopping Conditions

**Agent:** `STATUS-WRITER`  
**Inputs:** All outputs from Steps 9–12  
**Outputs:** Final run status and Reflection artifact  

Verify all Stop Conditions (see `## Stop Conditions`). If all are met, record status `completed` and produce the Reflection. If any Stop Condition is unmet due to a non-failure condition (maximum run duration reached, GATE-1 denied, concurrent run detected), record status `stopped` with reason and produce a partial Reflection. If an unrecoverable error has occurred, record status `failed` and produce a Reflection that includes root cause.

---


**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `ARCH-CHECKER` without relying on `ARCH-SCANNER`'s self-report.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Every build file in the repository has a corresponding module entry in `module-catalog.md` | Count build files in inventory; count entries in module catalog; assert equal |
| VER-2 | Every service entry point has a corresponding entry in `service-catalog.md` | Count service entry point files in inventory; count service catalog entries; assert equal |
| VER-3 | Every API definition file has a corresponding entry in `api-catalog.md` | Count API definition files in inventory; count API catalog entries; assert equal |
| VER-4 | `technology-stack.md` contains at least one entry each for: language runtime, build tool, and test framework | Parse technology stack file; assert presence of each category |
| VER-5 | All entries in `unknowns.md` have a valid status value (`open`, `in-progress`, or `resolved`) | Parse unknowns file; assert every entry has a non-empty, valid status field |
| VER-6 | `architecture-overview.md` references at least one entry from each of: module catalog, service catalog, and technology stack | Cross-reference overview against other catalog files for named entries |
| VER-7 | No secrets values appear in any output artifact | Scan all output files against a pattern list of known secrets shapes: API key patterns, base64-encoded strings longer than 40 characters, `password=` or `secret=` assignments with non-placeholder values |
| VER-8 | `STATUS-001.md` has been updated with the current run ID and a timestamp within 5 minutes of the current time | Read STATUS file; assert run ID present and timestamp within tolerance |
| VER-9 | `architecture-diagrams.md` contains at least one valid Mermaid diagram block | Parse diagrams file; assert presence of at least one fenced code block with `mermaid` language tag |
| VER-10 | All unknowns from prior runs that are now classifiable have been updated to `resolved` in `unknowns.md` | Cross-reference prior unknowns list against current findings; assert no prior unknown whose subject appears in current findings retains `open` status |

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.
## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/architecture/reflections/REFLECTION-001-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Coverage Summary:** documentation coverage percentage achieved, by category (modules, services, APIs, events, schemas)
- **Confidence Summary:** discovery confidence score per category at run end
- **Drift Summary:** count of added, removed, and changed items; drift magnitude; whether `significant_change` was flagged
- **Unknown Delta:** count of new unknowns added; count resolved; count remaining open
- **Gate Narrative:** for each gate that fired, the reason it fired and the human decision or auto-proceed outcome

---

## Human Approval Gates

### GATE-1 — Hard Gate: Significant Structural Change or Low Discovery Confidence

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 8, before Step 9 |
| Artifact Under Review | Architecture findings document and drift report |
| Approver | Principal Engineer or Architecture Owner |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; partial findings written to `unknowns.md` only; Reflection produced; human must schedule a new run with adjusted scope or provide annotations to resolve low-confidence areas |
| Audit Trail | Approval record written to `STATUS-001.md` under `gate_outcomes.GATE-1`; reviewer name, timestamp, and decision recorded |

**Fires when:**
- Any discovery confidence score is below 60
- `significant_change = true` (module removed, public API removed or protocol changed, drift magnitude > 20, service entry point missing)
- Unknown count increased by more than 10 since prior run
- `concurrent_change_detected = true`
- Checker validation rejected on both the initial attempt and the one permitted retry

**Reviewer guidance:** Confirm that architectural findings reflect the current state of the repository before they are written as authoritative documentation. Inspect the drift report and unknowns list. If findings are correct, approve in writing. If findings contain errors, deny and record correction notes in `STATUS-001.md`; these notes become required context for the next run.

---

### GATE-2 — Soft Gate: Minor Unknowns Increase or Checker Retry Required

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 8, before Step 9 |
| Artifact Under Review | Unknowns delta and Checker validation report |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-001.yml`; defaults to creating a draft PR with the findings summary |
| Timeout | 24 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 9; `soft_gate_auto_proceeded: true` recorded in `STATUS-001.md` |
| Audit Trail | Notification timestamp, outcome (auto-proceeded or manually approved/objected), recorded under `gate_outcomes.GATE-2` |

**Fires when:**
- Unknown count increased by 1–10 since prior run (and GATE-1 did not also fire)
- Checker validation required a Step 5 retry (and GATE-1 did not also fire)

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-001.md`. The executing agent must read `STATUS-001.md` at the start of each step and halt immediately if this value is present. On emergency stop: no further writes to `docs/architecture/` are made beyond the current step; a partial Reflection is produced; the Reflection records the step at which the stop was received and the state of all outputs at that moment.

---

## Failure Recovery

### FR-1 — Incomplete Repository Scan

**Detection:** Step 3 produces fewer files than the prior run's file count, and no corresponding deletions appear in the git log.  
**Immediate Action:** Record the scan gap in findings. Flag `scan_incomplete = true`.  
**Recovery:** If the gap exceeds 5% of the prior file count, trigger GATE-1. Otherwise continue with `scan_incomplete` recorded in `STATUS-001.md` and prior entries for unscanned areas preserved and flagged `unverified_this_run`.  
**Rollback:** No documentation written for gap areas; prior entries are not removed.

### FR-2 — Conflicting Architecture Evidence

**Detection:** Two or more source artifacts make contradictory claims about the same architectural element (e.g., two build files declare the same module name; two ADRs accept contradictory decisions about the same concern).  
**Immediate Action:** Record the conflict as an unknown (`UNK-NNN`) with both conflicting sources cited.  
**Recovery:** Do not resolve the conflict programmatically. If the conflict affects a module boundary or public API, trigger GATE-1. For other item types, record in `unknowns.md` and continue.  
**Rollback:** The conflicting element is excluded from all catalogs until a human resolves it.

### FR-3 — Previously Documented Item Has No Current Evidence

**Detection:** A module, service, or API present in prior state has no supporting evidence in the current scan.  
**Immediate Action:** Flag the item as `evidence_missing` in current findings.  
**Recovery:** Do not remove the item from the catalog automatically. Add it to the drift report as a candidate removal. Trigger GATE-1 if the item is a module or public API; otherwise proceed with the item marked `evidence_missing` and document it in `unknowns.md`.  
**Rollback:** If a run is abandoned after documentation writes have begun, `git checkout docs/architecture/` restores the prior state.

### FR-4 — Generated Code Obscuring Architecture

**Detection:** Files classified as `generated` exceed 50% of total file count, reducing effective source coverage below useful thresholds.  
**Immediate Action:** Record all generated file directories. Adjust coverage calculation to exclude generated files from denominator.  
**Recovery:** Identify generator configuration files and record the generator as the authoritative architectural source. Flag in `unknowns.md` if the generator cannot be identified.

### FR-5 — Maximum Run Duration Exceeded

**Detection:** Wall-clock time since trigger exceeds 4 hours.  
**Immediate Action:** Complete the current atomic step; do not begin the next step.  
**Recovery:** Write all output artifacts produced so far. Write `unknowns.md` with all unclassified areas recorded. Write `STATUS-001.md` with `status: stopped`, `reason: max_duration_exceeded`. Produce a partial Reflection. The next run picks up from the last stable state.  
**Rollback:** Not required; partial outputs are valid inputs for the subsequent run.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-001.md` at Step 11.

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
| `verification.level1.pass` | Count of VER-1 through VER-10 criteria that passed |
| `verification.level1.fail` | Count of VER-1 through VER-10 criteria that failed |
| `reflection.produced` | Boolean — was the Reflection artifact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `discovery.files_analyzed` | Total files read during Step 3 |
| `discovery.files_excluded` | Files skipped due to exclusion list |
| `discovery.scan_gaps` | Count of directories with no readable files |
| `discovery.modules_found` | Distinct modules recorded in module catalog |
| `discovery.services_found` | Distinct services recorded in service catalog |
| `discovery.apis_found` | Distinct API entries in API catalog |
| `discovery.events_found` | Distinct event types in event catalog |
| `discovery.adrs_found` | ADR files successfully parsed |
| `discovery.technical_debt_items` | Items recorded in technical debt catalog |
| `discovery.unknowns_open` | Unknowns with status `open` at run end |
| `discovery.unknowns_resolved_this_run` | Unknowns transitioned to `resolved` during this run |
| `discovery.unknowns_new_this_run` | Unknowns first recorded during this run |
| `coverage.repository_pct` | Percentage of non-generated, non-excluded files that were classified |
| `coverage.documentation_pct` | Percentage of discovered modules that have a README or inline documentation |
| `confidence.modules` | Discovery confidence score for modules (0–100) |
| `confidence.services` | Discovery confidence score for services (0–100) |
| `confidence.apis` | Discovery confidence score for APIs (0–100) |
| `confidence.events` | Discovery confidence score for events (0–100) |
| `confidence.schemas` | Discovery confidence score for schemas (0–100) |
| `drift.added` | Items added since prior run |
| `drift.removed` | Items removed since prior run |
| `drift.changed` | Items changed since prior run |
| `drift.magnitude` | Total drift count (added + removed + changed) |

---

## Risks

### RISK-1 — Stale Documentation Treated as Authoritative

- **Description:** If this loop does not run after significant repository changes, downstream loops consume outdated architecture documents and produce incorrect outputs.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** LOOP-001 has not run within 7 days, or a structural merge has occurred since the last run.
- **Control:** Trigger condition 3 (repository event trigger) forces a run on structural file changes. Trigger condition 4 (upstream signal) forces a run when outputs are older than 7 days.
- **Detection:** `STATUS-001.md` last-run timestamp is more than 7 days old; downstream loops must check this before consuming outputs.
- **Response:** FR-1 procedure; downstream loops must refuse to consume outputs older than their declared freshness threshold.

### RISK-2 — Hidden Dependencies from Generated Code

- **Description:** Generated code may import or instantiate components not visible in source, causing the dependency map to be incomplete.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Repository contains significant generated code directories.
- **Control:** FR-4 procedure; generator configurations are treated as authoritative rather than generated output.
- **Detection:** Large `generated` classification volume in Step 4 output.
- **Response:** Flag in `unknowns.md`; flag generator identification as `open` unknown.

### RISK-3 — Incorrect Module Boundary Inference

- **Description:** In monorepos or mixed-language repositories, module boundaries inferred from directory structure alone may not match the intended architecture.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** No explicit build file declares module boundaries; structure is inferred from directories.
- **Control:** Confidence score for modules set to low when boundary evidence is directory-only; GATE-1 fires if confidence < 60.
- **Detection:** Confidence score for modules below 60.
- **Response:** GATE-1; human reviewer provides explicit module annotations via `# ARCH:` inline comments or `.loop-001.yml` overrides.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads only the local repository filesystem and writes only to `docs/architecture/`. It accesses no tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Secrets Exposure in Output Artifacts

- **Description:** Secrets values embedded in configuration files or source code are inadvertently included in output documentation artifacts.
- **Likelihood:** Low
- **Impact:** High
- **Trigger Condition:** Repository contains secrets committed to source (a separate governance concern, but this loop must not amplify exposure).
- **Control:** VER-7 scans all output artifacts for secrets patterns before any file is written. Configuration file values are never written to outputs; only key names are recorded.
- **Detection:** VER-7 failure.
- **Response:** Halt Step 9. Do not write the affected artifact. Trigger GATE-1. Record the detection event in `STATUS-001.md` without reproducing the secret value.

### RISK-6 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent; re-running produces equivalent output.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-7 — Large-Scale Refactoring During Discovery

- **Description:** A large merge occurring while the loop runs can make scan state inconsistent between steps.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** The git HEAD SHA changes between Step 1 and Step 8.
- **Control:** SHA comparison in Step 8 detects this condition and triggers GATE-1 before any documentation is written.
- **Detection:** `concurrent_change_detected = true` flag in Step 8.
- **Response:** GATE-1; loop is re-triggered after the refactor stabilises.

### RISK-8 — Runaway Execution

- **Description:** A deeply nested or circular directory structure, or a repository exceeding scan limits, could cause Step 3 to run beyond its time budget.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Repository depth exceeds 20 levels or file count exceeds 500,000.
- **Control:** Maximum directory depth and file count enforced in Step 3; maximum run duration enforced by FR-5.
- **Detection:** `scan_ceiling_reached` flag or `max_duration_exceeded` status.
- **Response:** FR-5 procedure; partial outputs preserved for subsequent run.

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | Repository scan produced a classified inventory with no `scan_gap` left unresolved |
| SC-2 | All verification criteria VER-1 through VER-10 have passed |
| SC-3 | All unknowns have been recorded in `unknowns.md` with a valid status assigned |
| SC-4 | All eleven output artifacts listed in the Outputs table have been written |
| SC-5 | `STATUS-001.md` has been updated with run metrics and final status |
| SC-6 | `SKILL-001.md` has been updated |
| SC-7 | The Reflection artifact has been written to `docs/architecture/reflections/` |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-8 | Maximum run duration (4 hours) reached before SC-1 through SC-7 are met |
| SC-9 | GATE-1 is denied by the human reviewer |
| SC-10 | PRE-3 detects a concurrent run; this instance exits without modifying any artifact |
| SC-11 | An Emergency Stop signal is received in `STATUS-001.md` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Discovery Artifacts:**
- [ ] `docs/architecture/architecture-overview.md` written and internally consistent
- [ ] `docs/architecture/repository-map.md` written
- [ ] `docs/architecture/technology-stack.md` written
- [ ] `docs/architecture/module-catalog.md` written
- [ ] `docs/architecture/service-catalog.md` written
- [ ] `docs/architecture/api-catalog.md` written
- [ ] `docs/architecture/event-catalog.md` written
- [ ] `docs/architecture/dependency-map.md` written
- [ ] `docs/architecture/architecture-diagrams.md` written with at least one Mermaid diagram
- [ ] `docs/architecture/technical-debt.md` written
- [ ] `docs/architecture/unknowns.md` written with all entries status-assigned

**Verification:**
- [ ] All VER-1 through VER-10 criteria assessed and outcomes recorded in Reflection
- [ ] Checker validation report produced by `ARCH-CHECKER`
- [ ] VER-7 (secrets scan) passed on all output artifacts

**Gates:**
- [ ] Gate outcome recorded in `STATUS-001.md` for every gate that fired

**State:**
- [ ] `docs/loops/core/STATUS-001.md` updated with all required metrics and final status
- [ ] `docs/loops/core/SKILL-001.md` updated with current repository profile

**Reflection:**
- [ ] `docs/architecture/reflections/REFLECTION-001-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Incremental scanning:** When drift magnitude is below a threshold, limit Step 3 to files changed since the last run rather than full traversal, reducing run duration on large repositories.
- **Confidence calibration:** Build a historical model for confidence score accuracy based on how often low-confidence findings were subsequently corrected, enabling evidence-based threshold tuning.
- **Structured annotation protocol:** Formalise the `# ARCH:` inline annotation syntax with a defined grammar so engineers can provide machine-readable authoritative classifications for components that resist automated inference.
- **Cross-repository discovery:** Extend scope to sibling repositories in a multi-repo organisation, producing a unified architecture index across service boundaries.
- **Diagram syntax validation:** Assert that all Mermaid diagram sources in `architecture-diagrams.md` are syntactically valid using a Mermaid parser, not merely present.
- **Dependency vulnerability overlay:** Cross-reference the dependency map against a known-vulnerability database and surface critical findings in `technical-debt.md`.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure
- `docs/loops/templates/REVIEW-TEMPLATE.md` — review record structure used for gate approval audit trail

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-001 as the foundational architecture discovery loop for the AI Engineering Operating System.
- **1.1** — 2026-06-27 — Principal AI Engineering Architect — Added: `Loop ID`, `Name`, `Owner`, and `Maintainer` fields to header block (SPEC-001 §1.C1 conformance). Added: `Run Metadata` artifact to Outputs table (SPEC-001 §3.C5 conformance). No behavioral changes; all existing downstream consumers are unaffected.

