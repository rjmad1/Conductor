---
# PROVENANCE METADATA
Original Path: docs/loops/core/LOOP-003-Task-Discovery.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Identify, parse, and decompose task requirements into actionable items.
Generalized Purpose: Identify, parse, and decompose task requirements into actionable items.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# LOOP-003 — Task Discovery

**Loop ID:** LOOP-003  
**Name:** Task Discovery  
**Version:** 1.0  
**Status:** Active  
**Category:** Core  
**Depends On:** LOOP-001 — Architecture Discovery, LOOP-002 — Context Assembly  
**Human Gates:** Hard, Soft  
**Owner:** Principal Architecture Function  
**Maintainer:** Principal Architecture Function  

---

## Purpose

LOOP-003 discovers, normalises, classifies, deduplicates, and prioritises all candidate engineering work available to the repository at a given point in time. It converts unstructured signals — business goals, architecture findings, defects, technical debt, documentation gaps, security findings, roadmap items, and operational issues — into a single, verified, prioritised task backlog that LOOP-004 (Planning) consumes to select and plan the next unit of engineering work.

Every Planning loop must consume the outputs of this loop. No task may enter planning without a task record produced or validated by LOOP-003.

---

## Problem Statement

Engineering work arrives through many channels simultaneously: issue trackers, inline code annotations, CI/CD failure reports, ADR proposals, security scans, and verbal stakeholder requests. Without a unifying discovery process, the same work is duplicated across channels, high-risk items are deprioritised by accident, dependency order is ignored, and the relationship between individual tasks and architectural or regulatory obligations is invisible. The result is a backlog that is large, internally inconsistent, and unreliable as a planning input.

---

## Why This Loop Exists

Task discovery, when performed ad-hoc, produces a backlog that reflects whoever last touched it rather than the actual state of the system and its obligations. Codifying it as a loop makes the discovery repeatable, the prioritisation auditable, and the backlog a verified artefact with a known provenance. LOOP-004 can trust a backlog produced by this loop in a way it cannot trust an informally maintained list. Regulators, auditors, and stakeholders can trace any task back to the signal that originated it.

---

## Scope

**In scope:**
- Scanning all declared task sources (see Inputs) for candidate engineering work
- Normalising heterogeneous task representations into the canonical task record format
- Deduplicating tasks that represent the same underlying work, regardless of which source reported them
- Classifying each task using the canonical taxonomy
- Analysing dependencies between tasks and between tasks and architectural components
- Estimating task complexity and risk using observable signals, not subjective scoring
- Prioritising the backlog using the multi-factor priority framework
- Detecting circular dependencies within the task graph
- Producing a verified, deterministic, prioritised backlog in `docs/tasks/`
- Updating `STATUS-003.md` and `SKILL-003.md`

**Out of scope:**
- Planning the implementation of any individual task (LOOP-004)
- Assembling implementation context for any task (LOOP-002)
- Executing, reviewing, or closing tasks
- Integrating directly with external issue tracker APIs at runtime (task source files are the integration boundary; API synchronisation is a pre-run concern)
- Defining business value or regulatory priority (these are inputs from human-maintained sources, not derived by this loop)
- Assigning tasks to specific engineers or agents

**Maximum run duration:** 1 hour. If the loop has not reached a Stop Condition within this window, it must halt, record partial outputs, and produce a Reflection with status `stopped`.

---

## Inputs

| Input | Type | Source | Required |
|-------|------|--------|----------|
| LOOP-001 architecture outputs | Directory (`docs/architecture/`) | LOOP-001 | Required |
| `docs/architecture/technical-debt.md` | File | LOOP-001 output | Required |
| `docs/architecture/unknowns.md` | File | LOOP-001 output | Required |
| `docs/architecture/module-catalog.md` | File | LOOP-001 output | Required |
| `docs/loops/core/STATUS-001.md` | File | LOOP-001 | Required — freshness check |
| `docs/loops/core/SKILL-001.md` | File | LOOP-001 | Required |
| LOOP-002 context package | Directory (`docs/context/`) | LOOP-002 | Optional — enriches task context when present |
| `docs/loops/core/STATUS-002.md` | File | LOOP-002 | Optional |
| Task source files | Declared in `.loop-003.yml` | Repository or synchronised exports | Required — at least one source |
| Prior task backlog | `docs/tasks/task-backlog.md` | Prior LOOP-003 run | Optional — enables incremental discovery |
| Prior task catalog | `docs/tasks/task-catalog.md` | Prior LOOP-003 run | Optional — enables deduplication against existing records |
| Discovery configuration | `.loop-003.yml` at repo root | Repository | Optional |
| ADR directory | `docs/adr/` or configured equivalent | Repository | Optional |

### Task Sources

Task sources are the channels from which candidate work is discovered. Each source must be declared in `.loop-003.yml`. The loop supports the following source types:

| Source Type | Description | Default Location |
|-------------|-------------|-----------------|
| `issue-export` | Exported snapshot of an issue tracker (JSON, CSV, or Markdown) | `docs/tasks/sources/issues/` |
| `roadmap` | Human-maintained product roadmap document | `docs/tasks/sources/roadmap.md` |
| `adr-proposals` | ADRs in `proposed` status older than a configured threshold | `docs/adr/` |
| `technical-debt` | LOOP-001 technical debt register | `docs/architecture/technical-debt.md` |
| `code-annotations` | `TODO`, `FIXME`, `HACK`, `XXX`, `DEBT`, `ARCH:` markers in source | Repository source files |
| `test-failures` | Exported CI/CD test failure reports | `docs/tasks/sources/test-failures/` |
| `security-findings` | Security scan reports | `docs/tasks/sources/security/` |
| `documentation-gaps` | LOOP-001 documentation coverage gaps | `docs/architecture/unknowns.md` |
| `human-request` | Free-text task submissions from engineers | `docs/tasks/sources/requests/` |
| `ci-failures` | Build or pipeline failure reports | `docs/tasks/sources/ci-failures/` |
| `compliance` | Regulatory or compliance obligation documents | `docs/tasks/sources/compliance/` |

At least one source must be declared and readable. If all declared sources are empty or unreadable, the loop halts with `precondition_failed`.

### Input Validation

Before Step 1 begins, the loop must verify:
- All required LOOP-001 outputs are present and readable.
- `STATUS-001.md` records a completed LOOP-001 run with a timestamp no older than the configured freshness threshold (default: 7 days). If stale, trigger GATE-1.
- At least one task source is declared in `.loop-003.yml` and contains readable content.
- No exclusive lock on `docs/tasks/` exists from a concurrent LOOP-003 run.

---

## Outputs

All outputs are written to `docs/tasks/`. On first run, this directory is created. On subsequent runs, each file is replaced in full; prior content is preserved in git history.

| Artefact | Path | Description |
|----------|------|-------------|
| Task Backlog | `docs/tasks/task-backlog.md` | The primary deliverable: the complete, prioritised, verified list of tasks ready for planning, ordered by priority rank with supporting rationale |
| Priority Matrix | `docs/tasks/priority-matrix.md` | The factor-by-factor priority assessment for every accepted task, recording the input signals that drove each task's rank |
| Dependency Graph | `docs/tasks/dependency-graph.md` | The directed task dependency graph in Mermaid format, showing blocking relationships between tasks and between tasks and architectural components |
| Task Catalog | `docs/tasks/task-catalog.md` | The complete registry of all tasks discovered in this run: accepted, rejected, and deduplicated, with full normalised task records |
| Task Metadata | `docs/tasks/task-metadata.md` | Run provenance: run ID, LOOP-001 run ID consumed, sources scanned, task counts by category and status, priority distribution, confidence score, and discovery duration |
| Loop Status | `docs/loops/core/STATUS-003.md` | Run status, metrics, and open blockers |
| Loop Skill | `docs/loops/core/SKILL-003.md` | Updated skill profile for this loop |
| Reflection | `docs/tasks/reflections/REFLECTION-003-{run-id}.md` | Per-run structured reflection |

---

## Dependencies

- **LOOP-001 — Architecture Discovery:** Provides the technical debt register, documentation gaps, module catalog, and unknowns register that constitute primary task sources. LOOP-001 must be Active. Its outputs must satisfy the freshness threshold before LOOP-003 may run.
- **LOOP-002 — Context Assembly:** Optional. When a LOOP-002 context package is present and current, it enriches task records with implementation context estimates and dependency intelligence. LOOP-003 does not require LOOP-002 to have run for the same task; it uses LOOP-002 outputs opportunistically.

---

## Trigger

A run is initiated by any of the following:

1. **Manual invocation** — An engineer or agent explicitly triggers task discovery.
2. **Scheduled execution** — A recurring schedule (recommended: once per sprint or release cycle).
3. **Upstream signal** — LOOP-001 completes a run and its drift magnitude exceeds 10 items, suggesting the task backlog may be materially stale.
4. **Source event** — A new file appears in any declared task source directory (new issue export, new security finding, new CI failure report).
5. **LOOP-004 request** — The Planning loop requests a fresh backlog because the existing one is older than the configured reuse threshold (default: 24 hours) or is empty.

Trigger source and timestamp must be recorded in `STATUS-003.md` at run start.

---

## Preconditions

All of the following must be true before the loop begins Step 1:

| ID | Precondition | Check Method |
|----|-------------|--------------|
| PRE-1 | All required LOOP-001 output files are present and readable | Verify existence and readability of each file listed in Inputs as Required |
| PRE-2 | LOOP-001 outputs are within the configured freshness threshold | Read `STATUS-001.md` last-run timestamp; compare against threshold |
| PRE-3 | At least one task source contains readable content | Iterate declared sources; assert at least one is non-empty |
| PRE-4 | No concurrent LOOP-003 run is active | Check `STATUS-003.md` for `status: running`; if present, halt with `skipped_concurrent` |
| PRE-5 | The executing agent has write access to `docs/tasks/` | Probe write; remove probe on success |
| PRE-6 | `docs/loops/shared/LOOP-STANDARD.md` is readable | File must exist at declared path |

If PRE-2 fails, the loop triggers GATE-1 rather than halting with `precondition_failed`. If PRE-3 fails, the loop halts with `precondition_failed` and records which sources were empty or unreadable.

---

## External State

| System | Operation | Scope | Auth | Isolation | Rollback | Idempotent |
|--------|-----------|-------|------|-----------|----------|------------|
| `docs/architecture/` directory | Read | LOOP-001 output files | Filesystem permissions of executing agent | Read-only; LOOP-001 outputs are not modified | N/A | Yes |
| Task source directories | Read | Files in declared source directories under `docs/tasks/sources/` | Same as executing agent | Read-only; source files are not modified | N/A | Yes |
| Repository source files | Read | Source files scanned for code annotations | Same as executing agent | Read-only; limited to annotation extraction | N/A | Yes |
| `docs/tasks/` directory | Write | All files listed in the Outputs table | Same as executing agent | All writes confined to this directory | `git checkout docs/tasks/` restores prior state | Yes — full regeneration on each run; re-running against identical inputs produces an equivalent backlog |
| `docs/loops/core/STATUS-003.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/STATUS-003.md` | Yes |
| `docs/loops/core/SKILL-003.md` | Read-Write | Single file | Same as executing agent | Single file | `git checkout docs/loops/core/SKILL-003.md` | Yes |

This loop makes no writes to any external system outside the repository. It does not call issue tracker APIs, push notifications, or modify CI/CD configuration.

---

## Required Context

Before beginning Step 1, the executing agent must have loaded:

1. `docs/loops/shared/LOOP-STANDARD.md` — governing standard
2. `docs/loops/core/LOOP-003-Task-Discovery.md` — this document
3. `docs/loops/core/STATUS-003.md` — prior run state (if it exists)
4. `docs/architecture/architecture-overview.md` — system context
5. `docs/architecture/module-catalog.md` — module registry
6. `docs/architecture/technical-debt.md` — debt register
7. `docs/architecture/unknowns.md` — open unknowns
8. `docs/loops/core/SKILL-001.md` — repository technology profile
9. `docs/tasks/task-catalog.md` — prior task catalog (if it exists, for deduplication)
10. `.loop-003.yml` — discovery configuration (if it exists)

---

## Task Classification

### Canonical Taxonomy

Every task must be assigned exactly one primary category and at most two secondary categories from the following canonical taxonomy. No task may enter the backlog without a primary category assignment.

| Category | Code | Description | Primary Signal |
|----------|------|-------------|---------------|
| Feature | `FEAT` | New capability or behaviour added to the system | Roadmap item, issue labelled `enhancement`, human request describing new behaviour |
| Bug | `BUG` | Defect in existing behaviour that causes incorrect system output or failure | Issue labelled `bug`, test failure report, CI failure, user-reported defect |
| Refactor | `RFCT` | Internal restructuring with no intended change to observable behaviour | Technical debt entry, code review finding, architecture finding, `REFACTOR:` annotation |
| Documentation | `DOCS` | Creation or correction of documentation, README, ADR, or inline comment | Documentation gap in LOOP-001 unknowns, `TODO(docs):` annotation, missing README |
| Architecture | `ARCH` | Structural change affecting module boundaries, cross-cutting concerns, or system topology | ADR in `proposed` status, architecture drift finding from LOOP-001, explicit architectural decision required |
| Testing | `TEST` | Addition or correction of test coverage | Coverage gap, `TODO(test):` annotation, module with no test files identified by LOOP-001 |
| Security | `SEC` | Security control addition, hardening, vulnerability remediation, or security audit | Security scan finding, `SECURITY:` annotation, ADR imposing a security constraint, compliance obligation |
| Performance | `PERF` | Latency, throughput, or resource usage improvement | Performance finding, `PERF:` annotation, operational metric breach |
| Infrastructure | `INFRA` | Changes to deployment model, CI/CD pipeline, containerisation, or infrastructure configuration | Infrastructure gap, pipeline failure, dependency update requiring infra change |
| Compliance | `COMP` | Work required to satisfy a regulatory, legal, or contractual obligation | Compliance source document, audit finding, regulatory change notification |
| Research | `RSRCH` | Exploratory investigation to reduce uncertainty before engineering work begins | Open unknown in LOOP-001 unknowns register, high-uncertainty task, architectural question without a clear answer |
| Operational | `OPS` | Ongoing operational concern: monitoring, alerting, runbook creation, incident follow-up | CI failure with no clear root cause, missing health check, operational gap identified in LOOP-001 |
| Dependency Update | `DEP` | Library, runtime, or platform dependency version change | Outdated dependency in dependency map, security advisory on a dependency, compatibility obligation |

### Classification Rules

A task is classified by the primary signal that generated it. When a task could plausibly belong to more than one category, apply the following precedence:

```
SEC > COMP > BUG > ARCH > FEAT > PERF > RFCT > INFRA > DEP > TEST > DOCS > OPS > RSRCH
```

Precedence does not determine priority rank. A `DOCS` task may have higher priority than a `FEAT` task. Precedence only resolves category ambiguity.

### Extending the Taxonomy

Future categories may be added to this taxonomy by updating this section and the canonical taxonomy table. A new category requires:
- A unique two-to-six character code not already in use
- A description that is mutually exclusive with all existing categories
- A defined primary signal
- A declared position in the classification precedence order
- A MAJOR version bump to this loop document
- A review record conforming to `templates/REVIEW-TEMPLATE.md`

Repositories may define local subcategories (e.g., `FEAT:api`, `BUG:data`) using a colon-delimited suffix. Local subcategories do not require a loop version bump but must be declared in `.loop-003.yml`.

---

## Task Record Format

Every task in the backlog and catalog must be represented as a normalised task record with the following fields. A task record with any Required field absent may not enter the backlog.

| Field | Required | Description |
|-------|----------|-------------|
| `task_id` | Required | Globally unique identifier within this repository, format: `TASK-{YYYYMMDD}-{NNN}` |
| `title` | Required | One-sentence description of the work; must be actionable (begins with a verb) |
| `primary_category` | Required | One code from the canonical taxonomy |
| `secondary_categories` | Optional | Up to two additional codes; comma-separated |
| `source` | Required | Source type and source file path that originated this task |
| `source_reference` | Optional | Issue number, annotation location (file:line), or document section |
| `description` | Required | Full description of the work; minimum two sentences; must include the observable outcome of completing the task |
| `affected_modules` | Required | List of module IDs from the module catalog; `unknown` if modules cannot be identified |
| `dependencies` | Optional | List of `task_id` values this task is blocked by; empty list if none |
| `architectural_constraints` | Optional | List of ADR numbers or architectural rules that constrain this task |
| `complexity_signals` | Required | Observable signals used to estimate complexity (see Complexity Estimation) |
| `risk_signals` | Required | Observable signals used to estimate risk (see Risk Estimation) |
| `priority_rank` | Required | Integer rank within the backlog; lower is higher priority; assigned in Step 9 |
| `priority_factors` | Required | The factor assessments that determined the priority rank (see Priority Framework) |
| `confidence` | Required | Confidence score (0–100) for the task record's completeness and accuracy |
| `status` | Required | `accepted` \| `rejected` \| `duplicate` \| `deferred` |
| `duplicate_of` | Conditional | Required if `status: duplicate`; the `task_id` of the canonical record |
| `rejection_reason` | Conditional | Required if `status: rejected`; the reason the task was not accepted into the backlog |
| `first_seen` | Required | Run ID of the LOOP-003 run in which this task was first discovered |
| `last_updated` | Required | Run ID of the LOOP-003 run that last modified this record |

---

## Priority Framework

Tasks are prioritised using a multi-factor framework. Each factor is assessed independently. The resulting priority rank is determined by the combined weight of all factor assessments, applied in a deterministic order. The framework does not prescribe a numeric scoring formula; it prescribes the factors, their assessment criteria, and the resolution rules when factors conflict.

### Priority Factors

**Factor 1 — Safety and Regulatory Obligation**  
Tasks that are legally, contractually, or regulatorily mandated have the highest claim on priority. Assessment: does a compliance source document, audit finding, or active ADR impose an obligation with a stated deadline? If yes, the task is a priority-ceiling candidate. Tasks with active deadlines are placed above all non-obligated tasks regardless of other factors.

**Factor 2 — Security Severity**  
Security vulnerabilities are assessed by the severity of the worst-case exploitation scenario. Assessment: does the finding include a severity classification (Critical, High, Medium, Low)? Critical and High findings are placed above all non-security tasks that are not compliance-obligated.

**Factor 3 — Blocking Status**  
A task that is a declared dependency of one or more other accepted tasks is elevated in priority relative to its own factor assessment. Assessment: count the number of accepted tasks that declare this task as a dependency. Tasks with two or more blocked dependents are elevated above tasks with zero or one.

**Factor 4 — Architectural Importance**  
Tasks that resolve open unknowns, address architecture drift, or implement accepted ADRs affect the reliability of all other loops. Assessment: is the task listed as a resolution candidate in `docs/architecture/unknowns.md` or `docs/architecture/technical-debt.md`? Does the task implement an accepted ADR? If yes, the task carries architectural importance weight.

**Factor 5 — Risk Exposure**  
Tasks that reduce active risk — unhandled failure modes, missing error boundaries, unmonitored services — are prioritised over tasks that add capabilities. Assessment: is the task's risk signal sourced from a test failure, a CI failure, an unmonitored service, or a known unhandled exception path? If yes, risk exposure weight applies.

**Factor 6 — User and Business Impact**  
Features and bug fixes that affect a known user-facing capability or a declared business metric carry impact weight. Assessment: does the task description reference a user-facing flow, a business-critical service, or a declared SLA? Impact weight is sourced from human-maintained roadmap and issue tracker inputs; this loop records the impact assessment as stated in the source, not as derived by the agent.

**Factor 7 — Complexity and Effort**  
High-complexity tasks carry risk of extended duration and mid-task scope changes. Lower-complexity tasks are preferred when factors 1–6 produce equivalent rankings. Assessment: see Complexity Estimation below. High-complexity tasks are not penalised for important work; they are acknowledged as a tie-breaker.

**Factor 8 — Technical Debt Accumulation**  
Tasks that resolve technical debt in frequently modified modules reduce the ongoing cost of future work. Assessment: does the affected module appear frequently in recent git history? Is the debt item annotated as blocking a future feature or refactor?

### Prioritisation Process

1. All tasks with Factor 1 (regulatory obligation with active deadline) are placed at the top of the backlog, sorted by deadline date ascending.
2. Within the remaining tasks, Factor 2 (security severity Critical or High) places tasks above all non-Factor-1 tasks.
3. Remaining tasks are sorted by a combined assessment of Factors 3–8, applied in order. Each factor is assessed as `high`, `medium`, or `low`. The sort is stable: tasks with identical combined assessments retain their discovery order.
4. When two tasks have identical combined assessments across all eight factors, they are ordered alphabetically by `task_id` to produce a deterministic, reproducible ranking.

### Conflict Resolution

When two or more tasks have conflicting priority signals (e.g., a high-complexity Feature with high user impact versus a low-complexity Bug with high architectural importance), both priority rankings are recorded in `priority-matrix.md` with their factor breakdown. The loop does not resolve the conflict autonomously. If more than three such conflicts exist in a backlog, GATE-2 fires. If a conflict involves a Security or Compliance task against any other category, GATE-1 fires.

---

## Complexity Estimation

Complexity is estimated from observable signals, not subjective assessment. No agent may assert that a task is "complex" without citing at least one of the following signals.

| Signal | Complexity Indicator |
|--------|---------------------|
| Number of affected modules | 1 module → Low; 2–3 modules → Medium; 4+ modules → High |
| Presence of external dependency changes | External API change → adds Medium; external schema change → adds High |
| Presence of open architectural unknowns in affected modules | Any open unknown → adds High |
| Number of accepted ADRs constraining the task | 0 → no addition; 1–2 → adds Low; 3+ → adds Medium |
| Number of test files that would require modification | 0–5 → Low; 6–15 → Medium; 16+ → High |
| Presence of circular dependencies in affected modules | Any cycle → adds High |
| Task category | `ARCH`, `SEC`, `COMP` → starts at Medium regardless of other signals |

Complexity is recorded as: `Low`, `Medium`, `High`, or `Undetermined`. `Undetermined` is used when insufficient signals are available. A task with `Undetermined` complexity triggers GATE-2 unless the task category is `DOCS` or `RSRCH`.

---

## Risk Estimation

Risk is estimated from the potential consequence of the task going wrong — not the likelihood of the task itself being difficult.

| Signal | Risk Indicator |
|--------|---------------|
| Task category | `SEC`, `COMP` → High; `ARCH`, `INFRA` → Medium; all others → Low baseline |
| Affected module has no test coverage | → adds Medium |
| Task modifies a shared kernel or cross-cutting component | → adds High |
| Task modifies an external-facing API or event schema | → adds High |
| Task modifies security configuration, authentication, or authorisation | → adds High |
| Affected module is a dependency of 3 or more other modules | → adds Medium |
| Affected module has open unknowns | → adds Medium |
| Task is sourced from a security finding or compliance document | → adds High |

Risk is recorded as: `Low`, `Medium`, `High`, or `Critical`. `Critical` is assigned only when three or more High signals apply simultaneously.

---

## Agents

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|
| `TASK-DISCOVERER` | Maker | Steps 1–9: loads state, scans sources, normalises, deduplicates, classifies, analyses dependencies, and prioritises tasks | Filesystem read, source file parsing, git annotation extraction, dependency graph traversal | Reports to GATE-1 and GATE-2 |
| `TASK-CHECKER` | Checker | Step 10: independently validates the backlog against all verification criteria | Filesystem read, cross-reference of task records against module catalog and source files | Independent of TASK-DISCOVERER; cannot have participated in task record construction |
| `BACKLOG-WRITER` | Maker | Step 11: writes all output artefacts to `docs/tasks/` | Filesystem write | Must not proceed until gate conditions from Step 10 are cleared |
| `STATUS-WRITER` | Maker | Steps 12–13: updates STATUS-003.md and SKILL-003.md | Filesystem write | None — status writes occur after all gates are cleared |
| `HUMAN-REVIEWER` | Hard Gate Approver | GATE-1: reviews high-risk architectural or security/compliance prioritisation decisions | Human judgment | Sole authority to approve or deny GATE-1 |

`TASK-DISCOVERER` and `TASK-CHECKER` must be separate agent instances. `TASK-CHECKER` must not have participated in constructing any task record it reviews.

---


**Role Context:** You are a highly precise, deterministic Agent executing this loop. You must strictly adhere to the Workflow and output schemas. You must not deviate from the defined scope. All actions must be auditable and verifiable.
## Workflow

### Step 1 — Load Repository State

**Agent:** `TASK-DISCOVERER`  
**Inputs:** `STATUS-003.md`, prior task catalog (if present), `docs/architecture/` LOOP-001 outputs  
**Outputs:** In-memory repository state snapshot  

Read `STATUS-003.md` to extract: last run ID, last run date, last known backlog size, last known source list. If no prior state exists, record `first_run = true`. Read the prior `task-catalog.md` and index all existing task records by `task_id` and by normalised title hash (for deduplication in Step 6). Record the git HEAD SHA at this moment; this SHA is compared in Step 10 to detect concurrent repository changes.

---

### Step 2 — Load Architecture Knowledge

**Agent:** `TASK-DISCOVERER`  
**Inputs:** All required LOOP-001 output files  
**Outputs:** In-memory architecture knowledge base  

Load `module-catalog.md`, `technical-debt.md`, `unknowns.md`, and `dependency-map.md` into an indexed knowledge base. For each module: record its ID, type, declared dependencies, and the count of open unknowns that reference it. For each technical debt item: record its ID, severity, affected module, and age. For each open unknown: record its ID, description, affected area, and whether it is a candidate for a `RSRCH` task. This knowledge base is used throughout Steps 4–8.

---

### Step 3 — Load Current Context Package

**Agent:** `TASK-DISCOVERER`  
**Inputs:** `docs/context/` LOOP-002 outputs (if present and current)  
**Outputs:** Context enrichment record  

If a current LOOP-002 context package exists (`context-metadata.md` is present and its recorded HEAD SHA matches the current HEAD SHA), read `dependency-context.md` and `task-context.md` from the package. Record the enrichment as available for use in Steps 4 and 8 to improve dependency identification and module attribution. If no current package exists, record `context_enrichment_available = false` and proceed without it; LOOP-002 is not required for this loop to run.

---

### Step 4 — Discover Candidate Tasks

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Declared task sources from `.loop-003.yml`, architecture knowledge base from Step 2, context enrichment from Step 3  
**Outputs:** Raw candidate task list  

Scan each declared and readable task source in turn. For each source, extract all candidate task signals.

**Issue export (`issue-export`):** Parse each issue record. Extract: title, description, labels, status (open/closed), and any referenced file paths or module names. Discard closed issues unless they are reopened or have a resolution that requires follow-up work.

**Roadmap (`roadmap`):** Parse each initiative or milestone. Extract: initiative title, description, target outcome, and any named modules or capabilities. Each initiative produces one or more candidate tasks; do not conflate an initiative with a single task if it clearly spans multiple workstreams.

**ADR proposals (`adr-proposals`):** Identify all ADRs with status `proposed`. For each, produce a candidate `ARCH` task: the work required to evaluate, accept, or supersede the ADR.

**Technical debt (`technical-debt`):** Each item in `docs/architecture/technical-debt.md` is a candidate task. Extract: debt item ID, description, affected module, severity, and age.

**Code annotations (`code-annotations`):** Scan all source files in affected modules for the following annotation patterns: `TODO`, `FIXME`, `HACK`, `XXX`, `DEBT:`, `ARCH:`, `SECURITY:`, `PERF:`, `TODO(docs):`, `TODO(test):`. For each annotation: record the file path, line number, annotation type, and annotation text. Group annotations by file and module.

**Test failures (`test-failures`):** Parse each test failure report. Extract: failing test name, affected class or module, failure message, and duration of failure (first seen date).

**Security findings (`security-findings`):** Parse each security finding. Extract: finding ID, severity classification, affected component, description, and recommended remediation. Treat each unique finding as a candidate `SEC` task.

**Documentation gaps (`documentation-gaps`):** Inspect `docs/architecture/unknowns.md` for unknowns with category `documentation_gap`. Each gap is a candidate `DOCS` task.

**Human requests (`human-request`):** Parse each request file. Extract: submitter, date, description, and any named modules or capabilities.

**CI failures (`ci-failures`):** Parse each CI failure report. Extract: pipeline stage, failure message, affected module (if determinable), and failure frequency.

**Compliance (`compliance`):** Parse each compliance obligation document. Extract: obligation ID, regulatory reference, description, deadline (if stated), and affected modules.

For each candidate signal, create a preliminary task record with all extractable fields populated and all unknown fields marked `unknown`. Record the source type and source file for every candidate task. Total candidate count is recorded in `task-metadata.md`.

---

### Step 5 — Normalise Task Information

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Raw candidate task list from Step 4  
**Outputs:** Normalised candidate task list  

Apply normalisation rules to each candidate task record:

**Title normalisation:** Ensure the title is a single sentence beginning with an imperative verb. If the raw title is a noun phrase (e.g., "Login timeout bug"), rewrite it as an action ("Fix login session timeout causing premature user logout"). Record the original raw title in the task description.

**Description normalisation:** Ensure the description states both what the work is and what the observable outcome of completing it will be. If the raw description provides only symptoms (for bugs) or feature names (for features), extend it with the expected outcome. Mark extended descriptions with a `[synthesised]` annotation so that the human reviewer can identify inference.

**Module attribution:** For each candidate task, resolve affected module IDs by cross-referencing named components in the task description and source reference against the module catalog. If no module can be identified, set `affected_modules: unknown`. Record the evidence for each module attribution.

**Source reference normalisation:** Ensure every task has a `source_reference` that allows the raw signal to be located without rerunning the discovery scan (file path and line number for annotations; issue number for issue exports; ADR number for ADR proposals).

**Complexity and risk signal collection:** Populate `complexity_signals` and `risk_signals` using the observable signal tables in `## Complexity Estimation` and `## Risk Estimation`. Do not yet assign final complexity or risk ratings; those are computed in Step 9 after dependencies are known.

---

### Step 6 — Remove Duplicates

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Normalised candidate task list from Step 5, prior task catalog index from Step 1  
**Outputs:** Deduplicated candidate task list, duplicate registry  

Apply deduplication in two passes:

**Pass 1 — Exact match deduplication:** Two tasks are exact duplicates if they share the same `source_reference`. The task from the higher-priority source type is retained (priority order: `compliance` > `security-findings` > `issue-export` > `human-request` > `technical-debt` > `adr-proposals` > `test-failures` > `ci-failures` > `roadmap` > `code-annotations` > `documentation-gaps`). The lower-priority duplicate is marked `status: duplicate` with `duplicate_of` pointing to the retained task's ID.

**Pass 2 — Semantic deduplication:** Two tasks are semantic duplicates if they have the same primary category, the same affected module list, and a normalised title similarity above the configured threshold (default: 85%). Semantic duplicates are flagged for human review rather than automatically resolved; they are marked `status: duplicate` tentatively and included in the GATE-2 notification if GATE-2 fires, or in the GATE-1 presentation if GATE-1 fires.

**Prior catalog cross-reference:** Check all remaining candidates against the prior task catalog. If a candidate matches an existing accepted task (by source reference or semantic similarity), it is not added as a new task; instead, the existing task record is updated with the new source signal (adding to its evidence base) and its `last_updated` field is refreshed.

Record: count of exact duplicates removed, count of semantic duplicates flagged, count of prior catalog matches updated.

---

### Step 7 — Classify Tasks

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Deduplicated candidate task list from Step 6  
**Outputs:** Classified task list  

Assign each task a primary category and up to two secondary categories from the canonical taxonomy using the classification precedence rules. For each classification:

- Record the classification rationale: which signal drove the primary category assignment.
- Record the classification confidence: High (explicit category signal, e.g., security scan with severity), Medium (category inferred from description and module context), Low (category assigned by default due to insufficient signal).
- If confidence is Low for a task that is not `DOCS` or `RSRCH`, flag the task for GATE-2 review.
- If a task cannot be classified at all (no recognisable signal), assign primary category `RSRCH` and record `classification_failure = true`.

Aggregate classification statistics: count of tasks per primary category, count of High/Medium/Low confidence classifications. Record in `task-metadata.md`.

---

### Step 8 — Analyse Dependencies

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Classified task list from Step 7, architecture knowledge base from Step 2, context enrichment from Step 3  
**Outputs:** Task dependency graph  

For each accepted task, identify task-level and architecture-level dependencies.

**Task-level dependencies:** A task B depends on task A if completing A is a prerequisite for B to begin. The following signals indicate a dependency:
- Task B's description explicitly references the outcome of Task A.
- Task B's affected module depends on a module whose interface will be changed by Task A.
- Task B is a `TEST` task for a feature being implemented by Task A.
- Task B is a `DOCS` task for an API being introduced by Task A.

**Architecture-level dependencies:** Record the ADR numbers and architectural constraints that govern each task. A task that violates an accepted ADR is flagged as `adr_conflict` and triggers GATE-1.

**Circular dependency detection:** Apply topological sort to the task dependency graph. If a cycle is detected, record the cycle. Do not attempt to resolve cycles autonomously. Flag all tasks within the cycle for GATE-1 review. A backlog containing a dependency cycle may not be published without human resolution of the cycle (except for cycles involving only `RSRCH` or `DOCS` tasks, which may be published with the cycle documented in `dependency-graph.md` and a GATE-2 notification).

Construct the dependency graph as a Mermaid directed graph. Write it to `docs/tasks/dependency-graph.md`. Every task in the backlog must appear as a node, even if it has no declared dependencies.

---

### Step 9 — Prioritise Tasks

**Agent:** `TASK-DISCOVERER`  
**Inputs:** Classified and dependency-analysed task list from Step 8  
**Outputs:** Prioritised task list  

Apply the Priority Framework to all accepted tasks. For each task:

1. Assess each of the eight priority factors and record the assessment (`high`, `medium`, `low`, or `not_applicable`) in the task record's `priority_factors` field.
2. Apply the prioritisation process: compliance-obligated tasks first (sorted by deadline), then security Critical/High, then remaining tasks by combined factor assessment.
3. Assign a `priority_rank` integer to each accepted task. Ranks are assigned from 1 (highest priority) upward. No two tasks share the same rank; ties are broken by the deterministic rule in the Priority Framework.
4. Compute final complexity rating (`Low`, `Medium`, `High`, `Undetermined`) and final risk rating (`Low`, `Medium`, `High`, `Critical`) for each task using all signals now available including dependency count from Step 8.
5. Compute an overall confidence score for each task record (0–100) based on: field completeness, classification confidence, module attribution confidence, and dependency identification confidence. Tasks with confidence below 60 are flagged for GATE-1. Tasks with confidence between 60 and 79 are flagged for GATE-2.
6. Identify priority conflicts: pairs of tasks where the combined factor assessment produces ambiguous ordering (see Conflict Resolution). Record each conflict in `priority-matrix.md`.

Evaluate gate conditions:
- If any accepted task has `affected_modules: unknown` and category is `SEC` or `COMP`, trigger GATE-1.
- If any task has an `adr_conflict` flag, trigger GATE-1.
- If a dependency cycle exists involving non-`RSRCH`/non-`DOCS` tasks, trigger GATE-1.
- If more than three priority conflicts exist, trigger GATE-2.
- If more than 20% of accepted tasks have confidence below 80, trigger GATE-2.
- If the backlog contains more than the configured maximum task count (default: 100), trigger GATE-2.

Only the highest-priority gate fires. GATE-1 supersedes GATE-2.

---

**[GATE-1 — Hard Gate: High-Risk, Conflicting Compliance/Security Priority, or Dependency Cycle]**

The loop halts. `STATUS-003.md` is updated to `status: awaiting_approval`. The backlog is not published until human approval is received. See `## Human Approval Gates` — GATE-1.

---

**[GATE-2 — Soft Gate: Low Confidence, High Volume, or Minor Conflicts]**

The loop notifies and waits 8 hours. If no objection is received, it proceeds to Step 10. See `## Human Approval Gates` — GATE-2.

---

### Step 10 — Validate the Backlog

**Agent:** `TASK-CHECKER`  
**Inputs:** Prioritised task list from Step 9, architecture knowledge base, prior task catalog, gate clearance  
**Outputs:** Checker validation report  

`TASK-CHECKER` independently verifies all criteria in `## Verification`. The Checker must cross-reference task records directly against module catalog and source files — not against the Discoverer's summaries.

The Checker produces a validation report with: pass/fail per criterion, a list of task records failing any criterion, a list of detected Tier 4 items that should not be in the backlog, and an overall finding of `accepted` or `rejected`.

If `rejected`, the report enumerates every unresolved violation. Control returns to `TASK-DISCOVERER` for one re-attempt at Steps 7–9. If the second attempt also produces `rejected`, the loop triggers GATE-1.

After the Checker's accepted finding, verify the current git HEAD SHA against the SHA recorded in Step 1. If changed, flag `concurrent_change_detected = true` and trigger GATE-1 (source files may have been modified during discovery, invalidating code annotation results).

---

### Step 11 — Publish Prioritised Task Queue

**Agent:** `BACKLOG-WRITER`  
**Inputs:** Validated task list, priority matrix, dependency graph, Checker report, gate clearance  
**Outputs:** All files listed in the Outputs table  

Write each output artefact in full. No partial writes. Write in the following order:

1. `task-catalog.md` — complete registry of all task records (accepted, rejected, duplicate, deferred) with full normalised fields
2. `dependency-graph.md` — Mermaid directed graph of all task dependencies
3. `priority-matrix.md` — factor-by-factor assessment for every accepted task, priority conflicts, and ordering rationale
4. `task-backlog.md` — accepted tasks only, ordered by `priority_rank`, with title, category, affected modules, complexity, risk, confidence, and a one-sentence priority rationale for each
5. `task-metadata.md` — run provenance: run ID, LOOP-001 run ID consumed, sources scanned and task counts per source, totals by status and category, priority distribution histogram, average confidence score, discovery duration, and Checker report reference

Each file must include at its header: generation date, run ID, loop version, and a note that it is a generated artefact maintained by LOOP-003.

---

### Step 12 — Update STATUS-003.md

**Agent:** `STATUS-WRITER`  
**Inputs:** All run metrics, gate outcomes, Checker report, final output file list  
**Outputs:** Updated `docs/loops/core/STATUS-003.md`  

Record all metrics declared in `## Metrics`. Record gate outcomes. Record final status: `completed`, `failed`, or `stopped`. Record the backlog version (run ID) so that LOOP-004 can reference the specific backlog it consumed. Record open blockers: unresolved dependency cycles, tasks with `affected_modules: unknown`, unresolved priority conflicts pending human review.

---

### Step 13 — Update SKILL-003.md

**Agent:** `STATUS-WRITER`  
**Inputs:** Discovery metrics, task catalog statistics, classification outcomes  
**Outputs:** Updated `docs/loops/core/SKILL-003.md`  

Update the skill profile with observations that improve future runs:

- Most frequent primary task categories in this repository
- Most frequently affected modules (candidates for standing Tier 1 context in LOOP-002)
- Sources with the highest yield of accepted tasks (candidates for elevated scanning priority)
- Sources with the highest rate of duplicates or rejections (candidates for improved normalisation or source hygiene)
- Average complexity and risk by category
- Classification confidence distribution by category
- Backlog growth rate and composition trends across runs

---


**Self-Verification Chain:**
1. **Format Check:** Verify all outputs against the strict schema.
2. **Dependency Check:** Ensure all dependencies were satisfied.
3. **Logic Check:** Confirm no contradictory statements or unresolved placeholders remain.
4. **Final Affirmation:** The Checker Agent must explicitly affirm "Verification Passed" before clearing any Soft or Hard Gate.

**Execution Constraints:** Execution must be purely deterministic. The agent must proceed sequentially from step 1 to the final step. Parallel execution of sequential steps is forbidden. If a step fails, the agent must immediately proceed to the Failure Recovery procedure.
## Verification

All postconditions must be true before the run is marked `completed`. Each is independently checkable by `TASK-CHECKER`.

| ID | Criterion | Check Method |
|----|-----------|-------------|
| VER-1 | Every accepted task has a non-null `primary_category` from the canonical taxonomy | Iterate accepted tasks; assert `primary_category` is a valid taxonomy code for each |
| VER-2 | Every accepted task has a non-empty `description` of at least two sentences | Iterate accepted tasks; assert description length and sentence count |
| VER-3 | Every accepted task has `affected_modules` populated (may be `unknown` but must be explicitly stated) | Iterate accepted tasks; assert `affected_modules` field is non-null |
| VER-4 | Every task with `status: duplicate` has a valid `duplicate_of` reference to an accepted task | Iterate duplicates; assert `duplicate_of` resolves to an accepted task ID |
| VER-5 | No two accepted tasks share the same `priority_rank` | Sort accepted tasks by rank; assert no rank appears twice |
| VER-6 | The dependency graph contains no cycles among accepted tasks (except documented `RSRCH`/`DOCS` cycles) | Apply topological sort to the dependency graph; assert acyclicity for non-exempt categories |
| VER-7 | Every task's `source_reference` resolves to a readable file or a valid issue number | Verify each source reference; assert the referenced file exists or the issue format is valid |
| VER-8 | `task-metadata.md` total accepted count equals the count of tasks with `status: accepted` in `task-catalog.md` | Compare metadata total against catalog count; assert equal |
| VER-9 | `priority-matrix.md` contains one entry for every accepted task | Count priority matrix entries; assert equal to accepted task count |
| VER-10 | `dependency-graph.md` contains one node for every accepted task | Count graph nodes; assert equal to accepted task count |
| VER-11 | No task from a closed or resolved source (closed issue, resolved debt item) appears as `status: accepted` | Cross-reference accepted tasks against source status; assert no accepted task derives from a closed source |
| VER-12 | `STATUS-003.md` has been updated with the current run ID and a timestamp within 5 minutes of the current time | Read STATUS file; assert run ID and timestamp within tolerance |

---

## Reflection

At the end of every run — completed, failed, or stopped — the highest-active agent produces a Reflection at `docs/tasks/reflections/REFLECTION-003-{run-id}.md`.

The Reflection must contain all ten sections required by LOOP-STANDARD.md §10, plus the following loop-specific additions:

- **Source Summary:** for each declared source, the count of candidates discovered, accepted, rejected, and deduplicated
- **Classification Summary:** task count by primary category, confidence distribution (High/Medium/Low count)
- **Priority Summary:** distribution of accepted tasks across priority ranks by factor; count of compliance-obligated tasks; count of security Critical/High tasks; count of tasks elevated by blocking status
- **Dependency Summary:** total dependency edges in the graph; count of tasks with no dependencies; count of tasks with three or more dependencies; count of cycles detected
- **Gate Narrative:** for each gate that fired, the specific trigger condition, the artefacts presented for review, and the human decision or auto-proceed outcome

---

## Human Approval Gates

### GATE-1 — Hard Gate: High-Risk Work, Security/Compliance Conflict, or Dependency Cycle

| Field | Value |
|-------|-------|
| Gate ID | GATE-1 |
| Gate Type | Hard Gate |
| Position in Workflow | After Step 9, before Step 10 (gate evaluation); after Step 10 if Checker rejects twice |
| Artefact Under Review | Prioritised task list, priority matrix, dependency graph, Checker validation report |
| Approver | Principal Engineer, Architecture Owner, or designated Security/Compliance lead (depending on trigger condition) |
| Timeout | None — explicit written approval required |
| Approval Denied — Action | Loop terminates with status `stopped`; `task-metadata.md` written with partial statistics; Reflection produced; human must resolve blocking condition before re-triggering |
| Audit Trail | Approval record written to `STATUS-003.md` under `gate_outcomes.GATE-1`; reviewer name, role, timestamp, decision, and resolution notes recorded |

**Fires when:**
- Any accepted task has `affected_modules: unknown` and category is `SEC` or `COMP`
- Any task has an `adr_conflict` flag (task would violate an accepted ADR)
- A dependency cycle exists among accepted tasks with categories other than `RSRCH` or `DOCS`
- LOOP-001 outputs are stale (older than freshness threshold) and the human must decide whether to proceed
- Checker validation rejected on both initial and retry attempts
- `concurrent_change_detected = true` (HEAD SHA changed during discovery)
- A `SEC` or `COMP` task and any other task have a genuine ordering conflict that cannot be resolved by the factor precedence rules

**Reviewer guidance:** Inspect the specific trigger condition. For dependency cycles: review the cycle and either break it by introducing a new sequencing task or by deferring one task in the cycle. For ADR conflicts: either update the ADR, defer the task, or explicitly override the constraint with a documented rationale. For security/compliance ordering conflicts: the human reviewer's decision is authoritative and must be recorded. Write resolution notes into `STATUS-003.md` so the next run can incorporate the decision.

---

### GATE-2 — Soft Gate: Low Confidence, High Volume, or Minor Conflicts

| Field | Value |
|-------|-------|
| Gate ID | GATE-2 |
| Gate Type | Soft Gate |
| Position in Workflow | After Step 9, before Step 10 |
| Artefact Under Review | Task backlog draft, confidence distribution, priority conflicts |
| Approver | Any engineer with repository write access |
| Notification Channel | Declared in `.loop-003.yml`; defaults to writing a notification to `STATUS-003.md` and creating a draft summary document |
| Timeout | 8 hours from notification timestamp |
| Auto-Proceed Action | Loop proceeds to Step 10; `soft_gate_auto_proceeded: true` recorded in `STATUS-003.md` |
| Audit Trail | Notification timestamp and outcome recorded under `gate_outcomes.GATE-2` |

**Fires when (and GATE-1 did not also fire):**
- More than 20% of accepted tasks have confidence below 80
- More than three priority conflicts exist in the backlog
- The backlog exceeds the configured maximum task count
- A dependency cycle exists among `RSRCH` or `DOCS` tasks only
- `Undetermined` complexity assigned to tasks in categories other than `DOCS` or `RSRCH`

---

### Emergency Stop

Any human principal may terminate a running loop at any step by setting `status: emergency_stopped` in `STATUS-003.md`. The executing agent must check `STATUS-003.md` at the start of each step and halt immediately if this value is present. On emergency stop: no further writes to `docs/tasks/` are made beyond the current step; a partial Reflection is produced noting the step at which the stop was received.

---

## Failure Recovery

### FR-1 — Missing Task Metadata

**Detection:** A candidate task record is missing one or more Required fields after Step 5 normalisation.  
**Immediate Action:** Record the task with `status: deferred` and the missing fields enumerated. Do not insert a task with missing required fields into the accepted backlog.  
**Recovery:** For each missing field, attempt a second-pass inference from the source document. If inference fails, mark the field value as `requires_human_input`. Include the deferred task in the GATE-2 notification summary. A deferred task becomes a candidate `RSRCH` task if the missing fields cannot be resolved: "Clarify task requirements for: [original title]."  
**Rollback:** No backlog artefact is affected; deferred tasks are catalogued but not ranked.

### FR-2 — Conflicting Priorities

**Detection:** Step 9 produces two or more tasks where the combined factor assessment is identical and the deterministic tie-breaking rule (alphabetical by `task_id`) would produce a ranking that contradicts an explicit stakeholder priority recorded in the roadmap source.  
**Immediate Action:** Record the conflict in `priority-matrix.md` with both orderings and the source of each.  
**Recovery:** If three or fewer conflicts: record and proceed; conflicts are surfaced in the Reflection. If more than three conflicts: trigger GATE-2.  
**Rollback:** The backlog is published with the deterministic ranking; the conflict records in `priority-matrix.md` serve as the audit trail for any subsequent human override.

### FR-3 — Circular Task Dependencies

**Detection:** Step 8 topological sort detects a cycle.  
**Immediate Action:** Record the cycle. Mark all tasks in the cycle with `dependency_cycle: true`.  
**Recovery:** For cycles involving `RSRCH` or `DOCS` tasks only: proceed with cycle documented and GATE-2 triggered. For cycles involving any other category: trigger GATE-1. The human reviewer must break the cycle (see GATE-1 guidance) before the backlog is published.  
**Rollback:** No backlog is published while a blocking cycle is unresolved. Partial artefacts (`task-catalog.md` with cycle annotation) may be written for human review.

### FR-4 — Incomplete Discovery (Source Unreadable or Empty)

**Detection:** A declared task source is unreadable or returns zero candidates.  
**Immediate Action:** Record the source as `unreadable` or `empty` in `task-metadata.md`. Do not halt the run.  
**Recovery:** Continue discovery with all remaining sources. If the unreadable source is the only declared source, halt with `precondition_failed` (PRE-3 failure). Otherwise, record the gap and note it in the Reflection.  
**Rollback:** Not required; the backlog is produced from available sources. Missing source coverage is explicit in `task-metadata.md`.

### FR-5 — Duplicate Work Detected Post-Publication

**Detection:** After a backlog has been published, a new LOOP-003 run discovers that two tasks in the prior backlog were semantic duplicates that were not caught in Step 6.  
**Immediate Action:** In the new run, mark the lower-priority of the two as `status: duplicate` during Step 6 prior catalog cross-reference.  
**Recovery:** Record the late-detected duplicate in the Reflection under Failures and Anomalies. Update the `task-catalog.md` to reflect the deduplication. If the duplicated task was already in planning (LOOP-004), record a note in `STATUS-003.md` as an open blocker for human resolution.  
**Rollback:** The prior backlog is superseded by the new run; git history preserves both states.

### FR-6 — Unknown Task Ownership

**Detection:** Step 5 module attribution fails: `affected_modules: unknown` is assigned to a task because no module in the catalog matches the described component.  
**Immediate Action:** Record the task with `affected_modules: unknown`. Do not block the run.  
**Recovery:** For `SEC` or `COMP` tasks with unknown modules, trigger GATE-1. For all other categories, proceed and include the task in the GATE-2 notification if GATE-2 fires; otherwise, note in the Reflection. These tasks are strong candidates for a preceding `RSRCH` task to identify the relevant module.  
**Rollback:** No output artefact is affected; the task is accepted into the backlog with its module field explicitly `unknown`.

---

## Metrics

All metrics are recorded in the Reflection and in `STATUS-003.md` at Step 12.

### Required by LOOP-STANDARD

| Metric | Description |
|--------|-------------|
| `run.duration_seconds` | Wall-clock seconds from trigger to termination |
| `run.status` | `completed` \| `failed` \| `stopped` |
| `run.steps_completed` | Count of steps completed (of 13) |
| `run.steps_total` | 13 |
| `gate.hard.count` | Hard gates reached |
| `gate.hard.approved` | Hard gates approved |
| `gate.hard.denied` | Hard gates denied |
| `gate.soft.count` | Soft gates reached |
| `gate.soft.auto_proceeded` | Soft gates that timed out and auto-proceeded |
| `verification.level1.pass` | Count of VER-1 through VER-12 criteria passed |
| `verification.level1.fail` | Count of VER-1 through VER-12 criteria failed |
| `reflection.produced` | Boolean — was the Reflection artefact written |

### Loop-Specific

| Metric | Description |
|--------|-------------|
| `discovery.sources_scanned` | Count of declared sources scanned |
| `discovery.sources_empty` | Count of declared sources that returned zero candidates |
| `discovery.sources_unreadable` | Count of declared sources that could not be read |
| `discovery.candidates_raw` | Total candidate signals extracted before normalisation |
| `tasks.accepted` | Count of tasks with `status: accepted` |
| `tasks.rejected` | Count of tasks with `status: rejected` |
| `tasks.duplicate_exact` | Count of exact duplicates removed in Step 6 Pass 1 |
| `tasks.duplicate_semantic` | Count of semantic duplicates flagged in Step 6 Pass 2 |
| `tasks.deferred` | Count of tasks with `status: deferred` due to missing required fields |
| `tasks.prior_catalog_updated` | Count of existing task records refreshed from new source signals |
| `tasks.per_category` | Count of accepted tasks per primary category code |
| `tasks.compliance_count` | Count of accepted tasks with category `COMP` |
| `tasks.security_critical_high` | Count of accepted `SEC` tasks with Critical or High risk |
| `tasks.unknown_modules` | Count of accepted tasks with `affected_modules: unknown` |
| `tasks.low_confidence` | Count of accepted tasks with confidence below 80 |
| `backlog.size` | Count of accepted tasks in final backlog |
| `backlog.growth_delta` | Backlog size minus prior run backlog size |
| `priority.conflicts` | Count of priority conflicts recorded in `priority-matrix.md` |
| `dependency.edge_count` | Total directed edges in the dependency graph |
| `dependency.cycles_detected` | Count of cycles found during topological sort |
| `classification.high_confidence` | Count of tasks classified with High confidence |
| `classification.medium_confidence` | Count of tasks classified with Medium confidence |
| `classification.low_confidence` | Count of tasks classified with Low confidence |
| `confidence.average` | Mean confidence score across all accepted tasks |

---

## Risks

### RISK-1 — Incorrect Prioritisation

- **Description:** The priority framework produces a ranking that misrepresents actual business or engineering priorities, causing the wrong task to be planned first.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Business value or user impact assessments in human-maintained sources are absent, outdated, or inconsistently stated.
- **Control:** Factor 6 (User and Business Impact) is sourced exclusively from human-maintained documents; the loop does not infer impact from code. Priority conflicts trigger GATE-2 or GATE-1 for human resolution. The priority matrix is a durable record of the ranking rationale.
- **Detection:** GATE-1 conflict trigger; post-run human review of `priority-matrix.md`.
- **Response:** Human reviewer overrides ranking in `STATUS-003.md` as a correction note; the override is applied in the next run.

### RISK-2 — Duplicate Work Entering the Backlog

- **Description:** Two task records representing the same underlying work are both accepted, causing the same work to be planned and executed twice.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Semantic similarity threshold is too high (excluding real duplicates) or two sources describe the same work with very different language.
- **Control:** Two-pass deduplication in Step 6; prior catalog cross-reference; `TASK-CHECKER` VER-4 check.
- **Detection:** FR-5 detection in subsequent LOOP-003 runs.
- **Response:** FR-5 procedure.

### RISK-3 — Hidden Task Dependencies

- **Description:** A dependency between two tasks is not detected, causing them to be planned in the wrong order. The downstream task fails or produces incorrect output because its prerequisite has not been completed.
- **Likelihood:** Medium
- **Impact:** High
- **Trigger Condition:** Dependency is implicit (module A is assumed to be stable before module B is modified) rather than declared in the task descriptions.
- **Control:** Step 8 uses architecture knowledge (module dependency map) to infer task dependencies from module-level coupling, not only from explicit task descriptions. Context enrichment from LOOP-002 improves dependency detection when available.
- **Detection:** Task execution failure reported in LOOP-005 or LOOP-006 Reflection.
- **Response:** Hidden dependency is added as an explicit edge in the next LOOP-003 run via a correction note in `STATUS-003.md`.

### RISK-4 — Tenant Isolation Breach

- **Description:** Not applicable. This loop reads only local repository files and task source exports. It accesses no tenant-scoped runtime data.
- **Likelihood:** N/A
- **Impact:** N/A

### RISK-5 — Stale Task Information

- **Description:** A task in the backlog references a source signal (issue, debt item, annotation) that has since been resolved, causing the task to be planned for work that no longer needs to be done.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Task source files are not refreshed between LOOP-003 runs; issues are closed in the tracker but the export is not updated.
- **Control:** VER-11 cross-references accepted tasks against source status. Source exports must be refreshed before each LOOP-003 run; the freshness of source files is recorded in `task-metadata.md`.
- **Detection:** VER-11 failure; post-run engineer review.
- **Response:** Task is marked `status: rejected` with `rejection_reason: source_resolved` in the next run.

### RISK-6 — Overloaded Backlog

- **Description:** The backlog grows indefinitely as new tasks are discovered faster than they are completed, causing LOOP-004 to operate on an unmanageably large input.
- **Likelihood:** Medium
- **Impact:** Medium
- **Trigger Condition:** Task completion rate is lower than discovery rate over multiple cycles; many `DOCS` and `RSRCH` tasks accumulate without resolution.
- **Control:** Configured maximum backlog size triggers GATE-2. `backlog.growth_delta` metric tracks growth rate across runs. Deferral status allows deprioritisation without rejection.
- **Detection:** `backlog.growth_delta` consistently positive across three or more runs.
- **Response:** Human reviewer uses GATE-2 to defer low-priority tasks before the next LOOP-004 run.

### RISK-7 — Misclassification

- **Description:** A task is assigned an incorrect primary category, causing it to be prioritised incorrectly or to receive inappropriate context in LOOP-002.
- **Likelihood:** Low
- **Impact:** Medium
- **Trigger Condition:** Task description is ambiguous; source signal does not map cleanly to a single category.
- **Control:** Classification precedence rules are deterministic; Low classification confidence triggers GATE-2. `TASK-CHECKER` independently verifies that each category code is valid.
- **Detection:** Downstream LOOP (LOOP-004 or LOOP-005) Reflection notes unexpected category mismatch.
- **Response:** Task record is corrected in the next LOOP-003 run via a correction note in `STATUS-003.md`.

### RISK-8 — Non-Idempotent External Write

- **Description:** Not applicable. All writes are to the local repository filesystem and are fully idempotent; re-running against the same inputs produces an equivalent backlog.
- **Likelihood:** N/A
- **Impact:** N/A

---

## Stop Conditions

**Normal completion** (status `completed`) — all of the following must be true:

| ID | Condition |
|----|-----------|
| SC-1 | All declared task sources have been scanned (empty or unreadable sources are explicitly recorded, not silently skipped) |
| SC-2 | All accepted tasks have been classified with a valid primary category |
| SC-3 | All verification criteria VER-1 through VER-12 have passed |
| SC-4 | All five output artefacts listed in the Outputs table have been written |
| SC-5 | The dependency graph has been written and contains no unresolved blocking cycles |
| SC-6 | `STATUS-003.md` has been updated with run metrics and final status |
| SC-7 | `SKILL-003.md` has been updated |
| SC-8 | The Reflection artefact has been written to `docs/tasks/reflections/` |

**Normal termination without completion** (status `stopped`) — any of the following:

| ID | Condition |
|----|-----------|
| SC-9 | Maximum run duration (1 hour) reached before SC-1 through SC-8 are met |
| SC-10 | GATE-1 is denied by the human reviewer |
| SC-11 | PRE-4 detects a concurrent run; this instance exits without modifying any artefact |
| SC-12 | An Emergency Stop signal is received in `STATUS-003.md` |

---

## Deliverables

A run may not be marked closed until every applicable item is confirmed:

**Backlog Artefacts:**
- [ ] `docs/tasks/task-backlog.md` written with accepted tasks ordered by priority rank
- [ ] `docs/tasks/priority-matrix.md` written with one entry per accepted task
- [ ] `docs/tasks/dependency-graph.md` written as a valid Mermaid directed graph with one node per accepted task
- [ ] `docs/tasks/task-catalog.md` written with all task records (accepted, rejected, duplicate, deferred)
- [ ] `docs/tasks/task-metadata.md` written with run provenance and all discovery statistics

**Verification:**
- [ ] All VER-1 through VER-12 criteria assessed and outcomes recorded in Reflection
- [ ] Checker validation report produced by `TASK-CHECKER`
- [ ] No dependency cycles unresolved in `dependency-graph.md` (or cycles are explicitly documented and human-approved)

**Gates:**
- [ ] Gate outcome recorded in `STATUS-003.md` for every gate that fired

**State:**
- [ ] `docs/loops/core/STATUS-003.md` updated with all required metrics, backlog version, and final status
- [ ] `docs/loops/core/SKILL-003.md` updated with current discovery profile

**Reflection:**
- [ ] `docs/tasks/reflections/REFLECTION-003-{run-id}.md` produced
- [ ] Reflection contains all ten LOOP-STANDARD required sections plus five loop-specific sections

---


**Strict Output Schema:** All deliverables must be strictly formatted. Markdown artifacts must comply with GitHub Flavored Markdown (GFM). Data payloads must be strictly typed JSON matching the expected schema. No extraneous conversational text is permitted in final artifacts.
## Future Improvements

- **Source freshness enforcement:** Introduce a per-source freshness timestamp check; warn or block when a source export is older than a configurable threshold, independent of the LOOP-001 freshness check.
- **Velocity-aware prioritisation:** Feed task completion data (from LOOP-006 and LOOP-007 Reflections) back into Factor 7 (Complexity and Effort) to calibrate complexity estimates against observed implementation durations.
- **Stakeholder weighting profiles:** Allow `.loop-003.yml` to declare named stakeholder weighting profiles (e.g., `security-first`, `feature-velocity`) that adjust the relative weight of priority factors without changing the framework itself.
- **Task aging and decay:** Introduce an age-based relevance signal: tasks that have been in the backlog for more than a configurable number of cycles without being planned are automatically flagged for deferral review, preventing backlog staleness.
- **Cross-repository task awareness:** For multi-repository organisations, allow task sources to reference sibling repository issue exports, enabling dependency edges between tasks in different repositories.
- **Natural language normalisation quality:** Track the proportion of task descriptions that required `[synthesised]` annotation (indicating the raw source was insufficient) and report this as a source quality signal in `SKILL-003.md`.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — governing standard; all conformance requirements derive from this document
- `docs/loops/core/LOOP-001-Architecture-Discovery.md` — produces the technical debt register, documentation gaps, and module catalog consumed by this loop
- `docs/loops/core/LOOP-002-Context-Assembly.md` — produces the optional context enrichment consumed in Steps 3 and 8
- `docs/loops/shared/verification-standards.md` — verification level definitions
- `docs/loops/shared/human-oversight-gates.md` — Emergency Stop protocol and gate type definitions
- `docs/loops/shared/risk-controls.md` — mandatory risk category definitions
- `docs/loops/shared/metrics-definitions.md` — metric storage and aggregation conventions
- `docs/loops/shared/naming-standards.md` — task ID format and artefact naming conventions
- `docs/loops/templates/STATUS-TEMPLATE.md` — STATUS document structure
- `docs/loops/templates/SKILL-TEMPLATE.md` — SKILL document structure
- `docs/loops/templates/REVIEW-TEMPLATE.md` — review record structure

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Establishes LOOP-003 as the task discovery and prioritisation loop for the AI Engineering Operating System, consuming LOOP-001 and optional LOOP-002 outputs and producing the verified task backlog consumed by LOOP-004.

