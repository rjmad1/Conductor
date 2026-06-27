---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-011-LOOP-AUTHORING-GUIDE.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Developer guidelines for creating conformant loops.
Generalized Purpose: Developer guidelines for creating conformant loops.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-011 — Loop Authoring Guide

**Version:** 1.0  
**Status:** Active  
**Type:** Engineering Specification  
**Governs:** The creation, review, versioning, publication, and maintenance of all LOOP-XXX documents in the AI Engineering Operating System  
**Authority:** This document is subordinate to SPEC-001 (Loop Contracts). Where this guide and SPEC-001 conflict on a normative requirement, SPEC-001 prevails.

---

## Purpose

This document defines the canonical process for authoring Loop specifications in the AI Engineering Operating System (AEOS). It answers the full set of questions an author encounters when creating a new loop or revising an existing one: when a loop should exist and when it should not, how loops differ from other framework documents, how to number and name a loop, how to author it to satisfy the governing contracts, how it is reviewed and approved, how it is versioned over time, and how it is retired when no longer needed.

Every loop specification in the AEOS — from LOOP-001 to the last loop ever created — was authored by following rules. This document makes those rules explicit, consistent, and durable so that the framework's quality and coherence survive changes in authorship, team composition, and tooling.

---

## Audience

This guide is written for the following readers:

**Software Architects and Technical Leads** — evaluating whether a proposed capability warrants a new loop, authoring loop specifications, or reviewing proposed specifications before publication.

**AI Agents and Autonomous Engineering Systems** — generating or revising loop specifications programmatically, needing a machine-interpretable authoring protocol that produces conformant specifications without human scaffolding.

**Repository Maintainers** — managing the loop registry, approving lifecycle transitions, and ensuring the framework catalog remains coherent.

**Contributors** — proposing new loops for capabilities not yet covered by the AEOS, or identifying improvements to existing specifications.

---

## Document Taxonomy

Before deciding whether to create a new loop, authors must understand the distinction between the document types in the AEOS. Creating the wrong document type for a need produces confusion, duplication, and governance gaps.

### Loop (LOOP-NNN)

A loop is a self-contained engineering process unit. It has a declared purpose, a finite set of inputs, a finite set of outputs, a structured workflow, declared agents, human approval gates, a risk register, and a verification criteria set. A loop can be invoked in isolation or composed with other loops in a chain. A loop produces observable artefacts that downstream loops consume. A loop has a lifecycle (Draft, Active, Deprecated, Archived) and is versioned independently.

A loop exists to **do** something: discover architecture, assemble context, fix a bug, verify an implementation, capture reflection.

### Specification (SPEC-NNN)

A specification defines a contract, standard, protocol, or canonical structure that other documents (loops, guides, templates) must conform to. A specification does not do engineering work; it defines what conformant engineering work looks like. Specifications are normative: they use `shall` and `must not`. They are the source of authority that other documents implement.

A specification exists to **govern** something: what a loop must declare, how versioning works, what a metric means.

### Guide (ENGINEERING-LOOP-GUIDE, similar category guides)

A guide provides architectural context, design rationale, shared terminology, and authoring orientation for a category of loops or documents. A guide is informative and normative but subordinate to specifications. A guide eliminates duplication across a category of loop specifications by centralising the shared content that each loop would otherwise repeat.

A guide exists to **explain** something: the philosophy of Engineering Loops, the shared workflow pattern, the common gate conditions for a category.

### Standard (LOOP-STANDARD.md)

The standard is the single prescriptive structural template that every loop specification must follow. It defines the required sections, their names, their order, and their minimum content. The standard is the implementation of SPEC-001's structural requirements in a form an author can directly use.

A standard exists to **template** something: what sections a loop document must contain, in what order.

### Template (LOOP-TEMPLATE.md and similar)

A template is a blank-form starting point for authoring a specific document type. It contains the section headings, placeholder prompts, and format guidance that an author fills in. Templates are not normative; they are tools.

A template exists to **accelerate** something: giving an author a starting structure so they do not build from a blank page.

### Decision Rule

Before creating any new document:

| If the need is to… | Create a… |
|-------------------|-----------|
| Define a new engineering process that produces artefacts | Loop (LOOP-NNN) |
| Govern how a set of documents must behave | Specification (SPEC-NNN) |
| Explain a category of documents and eliminate duplication across them | Guide |
| Define the required structure for a document type | Standard |
| Provide a blank starting form for authoring | Template |

The most common authoring error is creating a loop when a specification or guide is needed, or creating a guide when a loop is needed. Use the decision rule explicitly before beginning any new document.

---

## When a New Loop Should Exist

A new loop is warranted when **all** of the following are true:

**W1 — Distinct Purpose**  
The proposed loop performs an engineering activity that no existing loop performs. The activity has a clear start, a clear end, a defined set of inputs, and a defined set of outputs. It is not a variant of an existing loop's activity — it is a different activity.

**W2 — Reusable Across Repositories**  
The loop can be adopted by any repository in the target domain without modification to the specification itself. Repository-specific behaviour is isolated in configuration files and in the outputs of LOOP-001 and LOOP-002 that the loop consumes at runtime.

**W3 — Composable with Existing Loops**  
The loop fits into the existing dependency graph without creating circular dependencies. It either consumes outputs from existing loops, produces outputs that existing or planned loops consume, or both.

**W4 — Independently Verifiable Outputs**  
The loop produces artefacts whose correctness can be verified by a separate agent or human without re-executing the loop. A loop whose outputs can only be evaluated by the loop that produced them provides no independent assurance.

**W5 — Not Addressable by Existing Mechanism**  
The need cannot be satisfied by: (a) adding a new step to an existing loop, (b) creating a new capability configuration (`.loop-1XX.yml`) for an existing Engineering Loop, (c) creating a Guide that instructs authors to combine existing loops, or (d) adding a new optional section to an existing specification.

If W5 is not satisfied, the correct action is the less-invasive option, not a new loop.

---

## When a New Loop Should NOT Exist

A new loop must not be created when any of the following are true:

**N1 — The Need is a Configuration Variant**  
A new loop should not be created because an existing loop behaves differently for one repository or task type. The correct mechanism is a capability configuration file (`.loop-NNN.yml`) or a conditional section within the existing specification.

**N2 — The Need is a Composition of Existing Loops**  
If the proposed activity is entirely explained as "run LOOP-A, then LOOP-B, then LOOP-C," the composition is better expressed in a Guide or an orchestration document than as a new loop with its own identity.

**N3 — The Need is a Naming Preference**  
A new loop should not be created because authors prefer a different name for an existing capability. Loop renaming follows the versioning and deprecation process; it does not produce a new loop.

**N4 — The Loop Would Reimplement a Core Loop Responsibility**  
Planning, implementation, verification, and reflection are Core Loop responsibilities. A loop that duplicates these responsibilities — even partially — violates the Separation of Responsibilities principle and must not be created. The correct approach is to parameterise the relevant Core Loop from an Engineering Loop.

**N5 — The Loop Has No Verifiable Outputs**  
If the proposed loop's outputs cannot be independently verified — if the only evidence of correctness is "it ran without error" — the loop design is incomplete. Fix the design before creating the loop; do not create the loop in hope that verification will be defined later.

**N6 — The Loop Fits an Existing Range But an Existing Loop Covers It**  
Before assigning a new Loop ID, verify that the SPEC-010 catalog contains no existing loop with overlapping scope. Partial overlap requires the author to either: (a) extend the existing loop's specification to cover the additional scope, or (b) precisely define the boundary between the two loops in both specifications.

---

## Loop ID Numbering Policy

### Reserved Ranges

Loop IDs are assigned from the following reserved ranges. An ID from one range may not be assigned to a loop in a different category. The Principal Architecture function maintains the assignment registry; SPEC-010 is the authoritative published record.

| Range | Category | Description |
|-------|----------|-------------|
| LOOP-001 – LOOP-009 | Core | The foundational process chain. Defines the universal engineering process. Managed exclusively by Principal Architecture. Maximum nine Core Loops; additions require SPEC-001 MAJOR amendment. |
| LOOP-010 – LOOP-099 | Reserved — Core Extensions | Reserved for future Core Loop additions that the LOOP-001–009 range cannot accommodate. No loop may be created in this range without explicit Principal Architecture authorisation. |
| LOOP-100 – LOOP-199 | Engineering | Capability loops for software engineering activities (bug fixing, refactoring, test generation, etc.). Governed by the Engineering Loop Guide. |
| LOOP-200 – LOOP-299 | Platform | Loops that manage the AEOS framework itself: catalog maintenance, specification linting, conformance scanning, framework health checks. |
| LOOP-300 – LOOP-399 | Governance | Loops that support engineering governance: compliance assessment, policy validation, regulatory reporting, audit trail management. |
| LOOP-400 – LOOP-499 | Release | Loops that manage the release lifecycle: release planning, release preparation, deployment, post-deployment verification, rollback. |
| LOOP-500 – LOOP-599 | AI | Loops specific to AI system engineering: model evaluation, prompt engineering quality, AI system integration testing, AI safety assessment. |
| LOOP-600 – LOOP-699 | Research | Loops for exploratory and research activities: proof-of-concept, technology assessment, architectural spike, feasibility study. |
| LOOP-700 – LOOP-799 | Experimental | Loops in active experimentation. An Experimental loop may not be placed in a production engineering chain. Once validated, it is either promoted to a permanent range (with a new ID assignment) or archived. |
| LOOP-800 – LOOP-999 | Reserved — Future | Reserved for categories not yet defined. No loop may be created in this range without a new category declaration approved by Principal Architecture. |

### ID Assignment Rules

**IA1 — Sequential Assignment Within Range**  
Within a range, IDs are assigned sequentially from the lowest available integer. The assignment sequence is determined by the date the Proposal is approved (Step 2 of the Loop Lifecycle), not by the date authoring begins. Concurrent proposals in the same range receive IDs in approval-date order.

**IA2 — ID Immutability**  
Once a Loop ID is assigned (at Proposal approval), it is permanent. The ID remains associated with the loop's purpose — even after the loop is Deprecated or Archived — for historical reference. An Archived loop's ID is never reassigned to a new loop.

**IA3 — Sub-Range Allocation Within Engineering Loops**  
Within the LOOP-100–199 range, the Engineering Loop Guide defines sub-ranges by capability domain (LOOP-101–109 for Defect Management, etc.). New Engineering Loop proposals must be placed in the sub-range matching their capability domain. If no sub-range exists for a new domain, the author must request a sub-range allocation before the ID is assigned.

**IA4 — ID Reservation for Planned Loops**  
A Loop ID may be reserved for a loop that is planned but not yet in authoring, provided the Proposal has been approved. A reserved ID that has not entered the Draft authoring phase within 180 days of reservation is released back to the pool.

### Deprecation and Replacement Numbering

When a loop is deprecated and replaced by a new loop:
- The deprecated loop retains its original ID and is set to `Deprecated` status.
- The replacement loop receives a new ID in the appropriate range.
- The replacement loop's specification must reference the deprecated loop's ID in its Version History and in a `Replaces:` field in its identity block.
- The deprecated loop's specification must reference the replacement loop's ID in a `Replaced By:` field added to its identity block at the time of deprecation.

---

## Authoring Rules

Every loop specification must satisfy the following rules before it may advance beyond Draft status. These rules extend and do not replace the SPEC-001 conformance checklist.

### AR1 — Satisfy LOOP-STANDARD.md

The specification must contain all sections defined in LOOP-STANDARD.md in the declared order. Section headings must match exactly. Optional sections defined in LOOP-STANDARD.md must be present with explicit content or an explicit "Not applicable for this loop" statement — they may not be silently omitted.

### AR2 — Satisfy SPEC-001 Loop Contracts

The specification must pass all items in the SPEC-001 §14 conformance checklist. The author must complete the checklist explicitly and include it with the specification submission for Architecture Review. A specification submitted without a completed conformance checklist is returned to Draft without review.

### AR3 — Reference Required SPEC Documents

The specification must reference, in its References section, every SPEC document whose contracts it implements. At minimum: SPEC-001 (Loop Contracts). Engineering Loops must additionally reference the Engineering Loop Guide. Loops that declare metrics must reference SPEC-005. Loops with governance obligations must reference SPEC-006. The References section is a declaration of which contracts the specification is written against; missing references imply missing compliance.

### AR4 — Declare All Dependencies

The `Depends On` field in the identity block must list every upstream loop whose outputs the loop consumes, by Loop ID and Name. The Dependencies section of the specification must classify each dependency as mandatory or optional, declare the freshness requirement, and confirm the absence of circular dependencies. A loop submitted with an incomplete `Depends On` field is returned to Draft.

### AR5 — Declare All Outputs

The Outputs section must enumerate every artefact the loop produces. No output that the loop writes may be absent from the table. For each output: the path must be a complete relative path from the repository root, the description must identify the primary consumer. Outputs declared but not produced by the workflow are specification gaps; outputs produced by the workflow but not declared are undeclared writes (SPEC-001 §11.C3 violation).

### AR6 — Declare Ownership

The identity block must declare an Owner (the role responsible for engineering correctness) and a Maintainer (the role responsible for keeping the specification current). These may be the same role. They may not be vacant. If the author does not have authority to assign ownership, they must escalate to the Principal Architecture function before submission.

### AR7 — Declare Metrics

The Metrics section must declare all twelve metrics required by LOOP-STANDARD.md plus any loop-specific metrics. Each metric must be named, described, and keyed according to the format defined in SPEC-005. A specification without a Metrics section is returned to Draft. A specification with fewer than twelve metrics is returned to Draft.

### AR8 — Declare Risks

The Risks section must assess all eight mandatory risk categories defined in SPEC-001 §10.C1. Each risk must be either assessed (with likelihood, impact, controls, detection, and response) or explicitly declared Not Applicable with a stated reason. A risk category left blank is a conformance failure.

### AR9 — Language and Style

**Normative language:** Use `shall` for requirements that apply to every run. Use `should` for strong recommendations with documented alternatives. Use `may` for permitted options. Do not use `must` — it is ambiguous between normative obligation and physical constraint in engineering specifications.

**Tense:** Write workflow steps in the present tense ("The agent reads the plan") not the future tense ("The agent will read the plan"). Specification language describes what happens, not what will happen.

**Agent references:** Refer to agents by their declared role ID (e.g., `VERIF-AGENT`) in workflow sections. Do not use pronouns for agents; pronouns are ambiguous in multi-agent specifications.

**Evidence claims:** Every normative statement about what a step produces or validates must be specific enough to be tested. "The agent verifies the plan is correct" is not testable. "The agent verifies that every step in the execution plan has a `files_modified` field with at least one entry" is testable.

**Section naming:** Section headings must match LOOP-STANDARD.md exactly. Do not rename sections for stylistic preference. Capability-specific additional sections must follow the naming convention in SPEC-001 §13.C2.

### AR10 — Prohibition on Copying Core Loop Behaviour

A loop specification may reference Core Loop behaviour ("LOOP-005 executes the plan") but may not reproduce Core Loop workflow steps as its own steps. If a specification contains a step that is substantively identical to a step in a Core Loop, the step belongs in the Core Loop (or is an invocation of it), not in this specification. Copied Core Loop steps create divergent definitions of the same behaviour.

---

## Authoring the Loop Specification: Step by Step

### Phase 0 — Pre-Authoring Validation

Before writing a single line of the specification:

**Step 0.1 — Confirm the need for a loop (not a guide, specification, or template).**  
Apply the Document Taxonomy decision rule. If the need maps to a document type other than a loop, stop and create the correct document type.

**Step 0.2 — Confirm the loop does not already exist.**  
Search SPEC-010 (Loop Catalog) for loops with similar purpose. If a partial overlap exists, document the boundary precisely before proceeding.

**Step 0.3 — Confirm the loop satisfies When a New Loop Should Exist (W1–W5).**  
All five conditions must be true. Document the rationale for each.

**Step 0.4 — Confirm the loop does not fall under When a New Loop Should NOT Exist (N1–N6).**  
If any condition applies, stop and apply the correct alternative.

**Step 0.5 — Identify the range and request ID assignment.**  
Determine the correct numbering range. Submit a Proposal (see Loop Lifecycle §Proposal) to reserve the Loop ID. Do not begin writing until the Proposal is approved and an ID is assigned.

### Phase 1 — Structural Authoring

**Step 1.1 — Copy LOOP-TEMPLATE.md to the target path.**  
The target path follows the convention: `docs/loops/{category}/LOOP-NNN-{Name}.md` where `{category}` is the lowercase category name (`core`, `engineering`, `platform`, etc.) and `{Name}` is the loop's name in Title-Case-Hyphenated format.

**Step 1.2 — Complete the identity block.**  
Fill all mandatory identity fields from SPEC-001 §1.C1. Set Status to `Draft`. Set Version to `0.1`.

**Step 1.3 — Write Purpose.**  
One to three paragraphs. State what the loop does, why it exists, and what the system would lose if this loop did not exist. Do not describe the workflow in the Purpose section.

**Step 1.4 — Write Problem Statement.**  
One to two paragraphs describing the engineering problem this loop solves. Frame the problem without presupposing the solution.

**Step 1.5 — Write Why This Loop Exists.**  
One to two paragraphs explaining the structural reason this is a loop rather than a step in an existing loop or a human-performed activity.

**Step 1.6 — Write Scope.**  
Two lists: In scope and Out of scope. Be specific. The Out of scope list is as important as the In scope list — it defines the boundaries that prevent scope creep.

**Step 1.7 — Write the Inputs table.**  
Every input. For each: Name, Type, Source, Required/Optional. Include the input validation requirements immediately after the table.

**Step 1.8 — Write the Outputs table.**  
Every output. For each: Artefact name, Path (complete relative path), Description (with primary consumer identified).

**Step 1.9 — Write Dependencies.**  
For each dependency: Loop ID, mandatory or optional, freshness requirement. Confirm no circular dependency exists.

**Step 1.10 — Write Trigger.**  
Enumerate all conditions that may initiate a run. Be exhaustive.

**Step 1.11 — Write Preconditions.**  
One row per precondition with ID (PRE-N), condition statement, and check method.

**Step 1.12 — Write External State.**  
One row per system accessed with all seven required columns from SPEC-001 §11.C2.

**Step 1.13 — Write Required Context.**  
List all documents and artefacts the executing agent must load before beginning Step 1 of the workflow.

### Phase 2 — Process Authoring

**Step 2.1 — Write the Agents section.**  
List all agents in a table: Agent ID, Role (Maker or Checker), Responsibilities, Tools, Human Oversight. For every artefact produced by the loop, assign exactly one Maker and one Checker. Verify the Maker/Checker anti-pattern is not present (same agent cannot be both).

**Step 2.2 — Write the Workflow.**  
Write each step as a subsection. Each step subsection must declare: responsible agent, inputs consumed, outputs produced. Steps that modify files or state must additionally declare the modification scope and the rollback action. Write workflow steps in present tense. Do not reproduce Core Loop internals — reference Core Loop invocations.

**Step 2.3 — Write the Verification section.**  
Minimum eight VER-N criteria. Each criterion: a condition statement and a check method. Include the mandatory types from SPEC-001 §6.C2. Write criteria that are independently executable — a reader with access to the artefacts must be able to evaluate each criterion without the agent that produced them.

**Step 2.4 — Write Human Approval Gates.**  
One subsection per gate. Each gate: Gate ID, Gate Type (Hard or Soft), Position in Workflow, Artefact Under Review, Approver, Timeout (for Soft Gates), Approval Denied Action, Audit Trail. List all trigger conditions. Verify the loop satisfies the minimum gate requirement for its category (SPEC-001 §9.C1).

**Step 2.5 — Write Failure Recovery.**  
One subsection per failure class. Each: Detection method, Immediate Action, Recovery path, Rollback scope, Reporting obligation. Every failure class encountered in the workflow must have a procedure.

### Phase 3 — Governance Authoring

**Step 3.1 — Write the Metrics section.**  
Complete the twelve LOOP-STANDARD required metrics table, then the loop-specific metrics table. Key naming follows SPEC-005 conventions: `{loop-abbreviation}.{metric-name}` in snake_case.

**Step 3.2 — Write the Risks section.**  
Assess all eight mandatory risk categories from SPEC-001 §10.C1. For each assessed risk: Description, Likelihood (using the SPEC-001 §10.C2 scale), Impact (same scale), Trigger Condition, Control, Detection, Response. For N/A risks: state the category, declare N/A, and provide a one-sentence rationale.

**Step 3.3 — Write Stop Conditions.**  
Two subsections: Normal completion (all conditions that must be true for `status: completed`) and Normal termination without completion (conditions that produce `status: stopped`). Be exhaustive — every exit from the loop must be covered.

**Step 3.4 — Write Deliverables.**  
A checklist. One item per artefact, per verification class, per gate type, per state update. The Deliverables checklist is what a reviewer uses to confirm a run is closeable.

### Phase 4 — Closing Authoring

**Step 4.1 — Write Reflection.**  
Specify the per-run Reflection artefact: the path, the responsible agent, the required sections (all ten from LOOP-STANDARD plus any loop-specific additions), and the timing (before run closure).

**Step 4.2 — Write Future Improvements.**  
Three to six specific, scoped improvement ideas. Not "make it better" — named improvements with a mechanism. Future Improvements are read by the Maintainer at each MINOR revision; they are the backlog for the loop's evolution.

**Step 4.3 — Write References.**  
Every SPEC document whose contracts the loop implements. LOOP-STANDARD.md. Every upstream loop the loop depends on. Any shared documents from `docs/loops/shared/` that the loop's workflow references.

**Step 4.4 — Write Version History.**  
One entry for the initial version: version number (`0.1` for Draft, `1.0` at first Active), date, author role, and a description of what this version establishes.

---

## Loop Lifecycle

The lifecycle governs every loop from conception to retirement. Each phase has defined entry conditions, activities, exit conditions, and artefacts. A loop may not advance to the next phase without satisfying the exit conditions of the current phase.

### Phase 1 — Idea

**Entry condition:** An engineering capability gap has been identified that may warrant a new loop.

**Activities:**
- Articulate the proposed loop's purpose in one paragraph.
- Apply the Document Taxonomy decision rule to confirm a loop is the appropriate document type.
- Apply W1–W5 (When a New Loop Should Exist) to confirm all conditions are met.
- Apply N1–N6 (When a New Loop Should NOT Exist) to confirm none apply.
- Identify the candidate numbering range.

**Artefacts:** None formal. An internal note or issue in the repository's task tracker is sufficient.

**Exit condition:** Author is confident the need is real, the document type is correct, and the numbering range is appropriate. Advance to Proposal.

### Phase 2 — Proposal

**Entry condition:** The Idea phase is complete.

**Activities:**
- Write a Proposal document (maximum two pages) containing:
  - Proposed Loop ID and Name
  - Proposed Category and numbering range
  - Purpose statement (one paragraph)
  - Problem Statement (one paragraph)
  - The five W-conditions with stated rationale for each
  - Preliminary dependency list
  - Preliminary output list
  - Proposed Owner and Maintainer
- Submit the Proposal to the Principal Architecture function for ID assignment.

**Artefacts:** Proposal document.

**Exit condition:** Principal Architecture approves the Proposal and assigns the Loop ID. The assigned ID is registered in SPEC-010 with status `Proposed`. Advance to Draft.

### Phase 3 — Draft

**Entry condition:** Loop ID is assigned and registered.

**Activities:**
- Author the full loop specification following the Authoring Rules (AR1–AR10) and the step-by-step authoring guide.
- Complete the SPEC-001 §14 conformance checklist.
- Set Version to `0.1` (or increment MINOR for each significant revision during drafting: `0.2`, `0.3`, etc.).
- Conduct internal review with at least one peer (a second author who did not write the specification).

**Artefacts:** Draft specification document. Completed conformance checklist.

**Exit condition:** Author and peer reviewer are satisfied the specification satisfies AR1–AR10 and passes the Review Checklist in this document. Advance to Architecture Review.

### Phase 4 — Architecture Review

**Entry condition:** Draft is complete; conformance checklist is completed; peer review is done.

**Activities:**
- Submit the specification and conformance checklist to the Architecture Review.
- Architecture Reviewer assesses: dependency graph correctness, external state declarations, gate design, risk completeness, and integration with existing loops.
- Architecture Reviewer either: (a) approves for Engineering Review, (b) returns to Draft with specific revision requirements, or (c) rejects with rationale (the proposal is not appropriate as a loop).
- Typical Architecture Review duration: 5 business days.

**Artefacts:** Architecture Review decision record (approved, returned, or rejected; with reviewer identity and rationale).

**Exit condition:** Architecture Review approved. Advance to Engineering Review.

### Phase 5 — Engineering Review

**Entry condition:** Architecture Review is approved.

**Activities:**
- Submit the specification to the Engineering Review panel (at minimum: a Technical Lead and a repository maintainer, neither of whom authored the specification).
- Engineering Reviewers assess: workflow correctness, agent assignments, verification criteria completeness, failure recovery adequacy, metrics completeness, and practical executability.
- Engineering Reviewers either: (a) approve for Publication, (b) return to Draft with specific revision requirements.
- Typical Engineering Review duration: 5 business days.
- If the specification is returned to Draft after Engineering Review, the Architecture Review decision remains valid (re-review by Architecture is not required unless changes are architectural in nature).

**Artefacts:** Engineering Review decision record.

**Exit condition:** Engineering Review approved. Advance to Publication.

### Phase 6 — Publication

**Entry condition:** Both Architecture Review and Engineering Review are approved.

**Activities:**
- Increment Version to `1.0`.
- Set Status to `Active`.
- Update the SPEC-010 catalog entry: change status from `Proposed` to `Active`, add version, add publication date, add Owner and Maintainer.
- Update `docs/loops/README.md` to include the new loop in the loop index table.
- If the loop is an Engineering Loop: verify the Engineering Loop Guide's sub-range table covers this loop's domain; update if a new sub-range was created.
- Notify any loops declared as upstream or downstream dependencies that the new loop is now Active.

**Artefacts:** Published specification at `1.0 Active`. Updated SPEC-010 entry. Updated README.

**Exit condition:** Specification is published and registered. The loop may now be invoked in engineering chains. Advance to Stable.

### Phase 7 — Stable (Maintenance)

**Entry condition:** Publication is complete.

**Activities (ongoing):**
- The Maintainer reviews SKILL-NNN.md observations accumulated across runs and identifies specification improvements.
- The Maintainer evaluates Future Improvements entries at each review cycle.
- The Maintainer authors MINOR version increments for clarifications and non-breaking additions.
- The Owner authors or approves MAJOR version increments for breaking changes.
- All version increments follow the Versioning Contract in this document.

**Artefacts:** Revised specification versions (MINOR or MAJOR increments). Revised SPEC-010 catalog entry (version field updated at each increment).

**Exit condition:** The loop is superseded by a new loop, declared redundant, or no longer appropriate for the engineering chain. Advance to Deprecation.

### Phase 8 — Deprecation

**Entry condition:** The Owner or Principal Architecture has determined the loop is to be archived.

**Deprecation triggers:**
- The loop has been replaced by a new loop with a different ID.
- The loop's capability is subsumed by changes to existing loops.
- The loop is no longer needed in any active engineering chain.
- The loop is structurally broken in a way that requires complete redesign (the new design receives a new ID).

**Activities:**
- Set Status to `Deprecated`.
- If there is a replacement loop: add `Replaced By: LOOP-NNN — Name` to the identity block.
- Update the replacement loop's identity block with `Replaces: LOOP-NNN — Name`.
- Notify all known downstream loops and orchestrators that reference this loop.
- Publish migration guidance (see Deprecation Workflow section).
- Begin the 90-day deprecation window. During this window: the Deprecated loop continues to function for existing chain configurations; new chain configurations may not declare a dependency on the Deprecated loop.

**Artefacts:** Updated specification with `Deprecated` status and replacement reference. Migration guidance document. Updated SPEC-010 entry.

**Exit condition:** 90-day window expires AND no active engineering chains declare a dependency on this loop. Advance to Archived.

### Phase 9 — Archived

**Entry condition:** Deprecation window complete; no active dependencies.

**Activities:**
- Set Status to `Archived`.
- Move the specification to `docs/loops/archive/LOOP-NNN-{Name}.md` (the file moves; all prior content is preserved).
- Update SPEC-010 to record the loop as `Archived` with the archiving date and the archive path.
- Update `docs/loops/README.md` to move the loop from the active index to the archived index.
- The Loop ID is permanently reserved (never reassigned).

**Artefacts:** Archived specification at its final version. Updated SPEC-010. Updated README archive section.

**Exit condition:** Archiving complete. The loop is no longer invocable. Its record is preserved indefinitely.

---

## Versioning Contract

### Semantic Versioning Format

Loop specifications use `MAJOR.MINOR` versioning. Draft specifications use `0.MINOR`. Active specifications use `MAJOR.MINOR` where MAJOR ≥ 1. There is no Patch component at the specification level — what would be a patch fix in software (correcting a typo, fixing a link) is a MINOR increment for specifications, because every change to a normative document requires a version record.

### MAJOR Version Increment

Increment MAJOR when any change is breaking — when a downstream loop, orchestrator, or consumer must be updated to remain compatible with the new version. Breaking changes include:

| Change Type | Example |
|-------------|---------|
| Required input added | A new file must be present before the loop can begin |
| Required input removed | A file previously required is no longer consumed |
| Required input renamed | A file's declared path or name changes |
| Required output added | A new artefact must be produced |
| Required output removed | A previously required artefact is no longer produced |
| Required output path changed | An artefact moves to a different directory |
| Workflow step added | A new step is inserted in the workflow sequence |
| Workflow step removed | An existing step is removed |
| Workflow step reordered | Steps execute in a different sequence |
| Gate type changed | A Hard Gate becomes Soft, or a Soft Gate becomes Hard |
| Gate trigger condition added | A new condition causes a gate to fire |
| Gate trigger condition removed | A previously triggering condition no longer fires a gate |
| Maker/Checker assignment changed | A different agent is assigned to produce or check an artefact |
| External state write added or removed | The loop begins or stops writing to a system |

### MINOR Version Increment

Increment MINOR when changes are non-breaking — a consumer on the prior MINOR version continues to work correctly with the new specification without modification. Non-breaking changes include:

| Change Type | Example |
|-------------|---------|
| Documentation clarification | A step description is made more precise without changing behaviour |
| Optional input added | A new optional input is declared |
| Optional output added | A new optional artefact is declared |
| Optional verification criterion added | A new VER-N criterion that does not affect the pass/fail determination |
| Future Improvements updated | Items added or completed |
| References updated | A new reference is added |
| Risk assessment updated | An existing risk's description is refined (not its controls or severity) |
| Informational section added | A non-normative section is added |
| Failure Recovery procedure refined | Wording is improved without changing the procedure |
| Metrics added | New loop-specific metrics are declared |
| Version History updated | A new entry is added |

### Patch Consideration

There is no Patch version. Changes that would be patches in software (typo corrections, dead link repairs, formatting corrections) are still MINOR increments for specification documents, because every change to a specification must be traceable in the Version History. The discipline of recording every change — however small — is the mechanism that keeps the Version History useful as an audit trail.

### Version History Entry Requirements

Every version increment must produce a Version History entry with:
- Version number
- Date (ISO 8601: YYYY-MM-DD)
- Author or authority (role, not personal name, unless policy requires it)
- For MAJOR increments: a list of each breaking change, classified by the MAJOR change type table above
- For MINOR increments: a concise description of what was added or clarified

Version History entries may not be modified retroactively. An error in a Version History entry is corrected by adding a new MINOR increment that states the correction.

### Co-Revision Protocol

When a change to Loop A requires a simultaneous change to Loop B (the loops must be updated together for the system to remain coherent), the two version increments are declared as a co-revision:

- Both specifications are updated simultaneously.
- Both Version History entries reference the other with: `Co-revised with LOOP-NNN v{new version}`.
- SPEC-010 is updated to record the co-revision relationship.
- The minimum compatible version of each loop references the co-revised version of the other.

Co-revisions are declared in SPEC-007. Engineering Loops that depend on Core Loop outputs are the most common source of co-revisions.

---

## Review Checklist

The following checklist must be completed before a specification is submitted for Architecture Review. The author completes it; the reviewer verifies it. Items marked (AR) are assessed primarily by the Architecture Reviewer; items marked (ER) are assessed primarily by the Engineering Reviewer; items marked (Both) are assessed by both.

### Identity and Document Structure

- [ ] Loop ID is correctly formatted (`LOOP-NNN`) and assigned by Proposal approval (Both)
- [ ] Loop Name is unique, descriptive, and matches the file name (Both)
- [ ] Status is `Draft` for submission; no specification is submitted with `Active` status (Both)
- [ ] Version is in `0.MINOR` format for Draft (Both)
- [ ] Category is declared and matches the assigned ID range (AR)
- [ ] `Depends On` field is complete and accurate (AR)
- [ ] `Human Gates` field lists all gate types present (Both)
- [ ] Owner and Maintainer are declared and not vacant (Both)

### Document Type Validation

- [ ] The W1–W5 rationale is documented and all five conditions are satisfied (AR)
- [ ] The N1–N6 conditions were checked and none apply (AR)
- [ ] The specification is a Loop and not better expressed as a Guide, Specification, or Template (AR)

### Inputs

- [ ] All inputs are listed in the Inputs table (ER)
- [ ] Every row has Name, Type, Source, and Required/Optional columns (ER)
- [ ] Input validation requirements are declared, including HEAD SHA checks for all provenance-carrying inputs (ER)
- [ ] HEAD SHA mismatch is declared as a Hard Gate condition, not a precondition failure (AR)
- [ ] Optional inputs declare the behaviour when absent (ER)

### Outputs

- [ ] All outputs are listed in the Outputs table with complete paths (ER)
- [ ] The STATUS file and SKILL file are declared as outputs (ER)
- [ ] A metadata artefact with all required provenance fields is declared (ER)
- [ ] A Reflection artefact is declared (ER)
- [ ] No output path conflicts with another loop's output directory (AR)
- [ ] If the loop is not LOOP-005 or a designated Release deploy loop: no source file modification is declared (AR)

### Execution

- [ ] A preconditions table is present with PRE-N IDs, condition statements, and check methods (ER)
- [ ] A Trigger section is present with exhaustive enumeration (ER)
- [ ] Each workflow step declares the responsible agent, inputs consumed, and outputs produced (ER)
- [ ] Emergency Stop is declared and handled at each step boundary (AR)
- [ ] Maximum run duration is declared (ER)
- [ ] Idempotency behaviour is declared with any exemptions noted (ER)

### Agents and Maker/Checker

- [ ] All agents are listed in the Agents table with Role, Responsibilities, Tools, and Human Oversight columns (ER)
- [ ] Every artefact has exactly one designated Maker (ER)
- [ ] Every artefact has exactly one designated Checker (ER)
- [ ] No agent is designated as both Maker and Checker for any artefact (AR)

### Verification

- [ ] At least eight VER-N criteria are declared (ER)
- [ ] Each criterion has a condition statement and a check method (ER)
- [ ] At minimum: one criterion for output existence, one for no unauthorised modifications, one for STATUS file update, one for Reflection artefact production (ER)
- [ ] All criteria are independently executable without re-running the loop (ER)

### Human Approval Gates

- [ ] The loop satisfies the minimum Hard Gate requirement for its Category (SPEC-001 §9.C1) (AR)
- [ ] At least one Soft Gate is declared, or the omission is explicitly justified (AR)
- [ ] Each Hard Gate declares: Gate ID, Position, Artefact Under Review, Approver, Timeout (N/A), Denied Action, Audit Trail (AR)
- [ ] Each Soft Gate declares: Gate ID, Position, Artefact Under Review, Approver, Timeout, Notification Channel, Auto-Proceed Action, Audit Trail (AR)
- [ ] All gate trigger conditions are listed exhaustively for each gate (AR)

### Failure Recovery

- [ ] A Failure Recovery section is present (ER)
- [ ] Every failure class that can occur in the workflow has a named procedure (ER)
- [ ] Each procedure declares: detection, immediate action, recovery path, rollback scope, reporting (ER)

### Risks

- [ ] All eight mandatory risk categories (SPEC-001 §10.C1) are assessed or declared N/A with rationale (AR)
- [ ] Assessed risks include: likelihood, impact, trigger, controls, detection, response (AR)
- [ ] Risk likelihood and impact use the SPEC-001 §10.C2 scale (AR)

### External State

- [ ] An External State table is present (ER)
- [ ] Every row has all seven required columns (System, Operation, Scope, Auth, Isolation, Rollback, Idempotent) (ER)
- [ ] No undeclared external writes exist in the workflow (ER)
- [ ] No secret values are written to any tracked file (AR)

### Metrics

- [ ] All twelve LOOP-STANDARD required metrics are declared in the required table (ER)
- [ ] Loop-specific metrics are declared in a separate table (ER)
- [ ] Metric keys follow SPEC-005 naming conventions (ER)

### Stop Conditions

- [ ] Normal completion conditions are exhaustive (every path to `completed` is covered) (ER)
- [ ] Normal termination conditions are exhaustive (every path to `stopped` is covered) (ER)

### Compliance

- [ ] SPEC-001 §14 conformance checklist is completed and attached (AR)
- [ ] References section lists all SPEC documents whose contracts are implemented (Both)
- [ ] Version History contains an entry for the current Draft version (Both)
- [ ] If this is an Engineering Loop: the Engineering Loop Guide conformance checklist is completed (ER)

---

## Deprecation Workflow

### Step 1 — Deprecation Decision

The Owner or Principal Architecture documents the reason for deprecation and the intended disposition: (a) replaced by a new loop, (b) subsumed by an existing loop's evolution, or (c) archived without replacement.

### Step 2 — Replacement Loop Publication (if applicable)

If there is a replacement loop, it must be published as Active before the original loop is set to Deprecated. The 90-day deprecation window does not begin until the replacement is Active and available for adoption.

### Step 3 — Migration Guidance Publication

A migration guidance document is produced (at `docs/loops/archive/LOOP-NNN-migration.md`) containing:
- The reason for deprecation
- The replacement loop's ID and Name (if applicable)
- A mapping of the deprecated loop's inputs to the replacement's inputs
- A mapping of the deprecated loop's outputs to the replacement's outputs
- Any differences in gate behaviour, agent requirements, or run duration
- The recommended timeline for adopting chains to migrate
- Confirmation of which configurations are backward-compatible and which require changes

Migration guidance is a hard requirement for any deprecated loop that has known active consumers. For loops with no known consumers, a single-paragraph notice is sufficient.

### Step 4 — Deprecation Notification

Notify all loops, orchestrators, and repository configurations that reference the deprecated loop. The notification includes the deprecation reason, the replacement loop (if any), a link to the migration guidance, and the 90-day window expiry date.

### Step 5 — Deprecation Window

The loop operates normally during the 90-day window. No new chains may declare a dependency on the Deprecated loop. Existing chains are not required to migrate during the window but should plan to do so before the window closes. The Maintainer monitors for remaining dependencies weekly during this period.

### Step 6 — Archiving

At window expiry, if no active dependencies remain, the loop is set to Archived. If active dependencies remain, the window is extended by 30 days per remaining dependency, and the dependency owners are escalated to with a hard deadline.

### Reference Preservation

An Archived loop's specification is preserved at its archive path indefinitely. The archive path is: `docs/loops/archive/LOOP-NNN-{Name}.md`. The SPEC-010 catalog retains the entry with status `Archived` and the archive path. The Loop ID is never reassigned.

The preservation guarantee means: given any artefact produced by any loop at any point in the repository's history, the specification that governed its production can be retrieved. This is a foundation of auditability and root cause investigation.

---

## Common Authoring Errors

The following errors appear frequently in first drafts. Authors should review this list before submitting for Architecture Review.

**Error 1 — Reproducing Core Loop Behaviour**  
A specification contains workflow steps that are substantively identical to steps in LOOP-004, LOOP-005, or LOOP-006. These steps belong in the Core Loop; the new loop invokes the Core Loop and provides parameters.

**Error 2 — Underspecified Inputs**  
The inputs table lists "LOOP-004 outputs" without naming specific files. Every input must name the specific file path.

**Error 3 — Underspecified Outputs**  
An output is described without a complete path, or an output produced in the workflow is not listed in the table. Trace every write operation in the workflow back to the outputs table.

**Error 4 — Missing Maker/Checker for Artefacts**  
An artefact is declared in the Agents section without a Checker. Every artefact needs both.

**Error 5 — Opinion-Based Verification Criteria**  
A VER-N criterion reads "The output is high quality" or "The analysis is complete." These are not verifiable. Rewrite as a specific, testable condition.

**Error 6 — Blank Risk Entries**  
A risk category is listed without an assessment or an explicit N/A. Silence is not an assessment. Every category needs either a complete assessment or an explicit N/A with rationale.

**Error 7 — Missing Emergency Stop**  
The workflow does not mention Emergency Stop. It must be checked at each step boundary; not mentioned is not the same as not applicable.

**Error 8 — Unlimited Run Duration**  
No maximum run duration is declared. Every loop must declare one. "The loop runs until complete" is not a duration bound.

**Error 9 — Circular Dependencies**  
The `Depends On` field lists a loop that (directly or transitively) depends on this loop. The author did not check the full dependency graph.

**Error 10 — Missing Version History Entry**  
The specification is submitted with no Version History section, or with a Version History section that has no entry for the current draft version.

---

## References

- `docs/loops/shared/LOOP-STANDARD.md` — the prescriptive section structure that every loop must follow
- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` — the Loop Contracts that govern every loop specification; the authority this guide implements
- `docs/loops/engineering/ENGINEERING-LOOP-GUIDE.md` — the category guide for LOOP-100 series specifications
- `docs/loops/templates/LOOP-TEMPLATE.md` — the blank-form starting point for authoring
- `docs/loops/README.md` — the master loop index, updated at each Publication and Retirement
- SPEC-005 — Metrics: metric naming conventions referenced by AR7
- SPEC-006 — Governance: ownership and review obligations referenced by AR6 and the Approval Workflow
- SPEC-007 — Versioning: co-revision protocol referenced by the Versioning section
- SPEC-010 — Loop Catalog: the registry updated at each lifecycle transition

---

## Version History

- **1.0** — 2026-06-26 — Principal AI Engineering Architect — Initial Active version. Defines the canonical authoring guide for the AI Engineering Operating System: document taxonomy, when to create a loop, when not to, numbering policy with reserved ranges, authoring rules (AR1–AR10), the step-by-step authoring procedure, the nine-phase loop lifecycle, semantic versioning with MAJOR/MINOR change type tables, the complete review checklist, the deprecation workflow with reference preservation guarantee, and the common authoring errors catalogue.

