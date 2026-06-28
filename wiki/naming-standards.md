---
# PROVENANCE METADATA
Original Path: docs/loops/shared/naming-standards.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Identifiers naming and numbering standards.
Generalized Purpose: Identifiers naming and numbering standards.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Naming Standards — Canonical Reference

**Version:** 1.0
**Status:** Active
**Type:** Reference Document
**Authority:** Principal AI Engineering Architect
**Applies To:** All identifiers, files, and artifacts in the AEOS framework

---

## Purpose

This document is the canonical naming convention reference for all identifiers used within the AEOS framework. Consistent naming is a prerequisite for automated tooling, unambiguous cross-references, and reliable catalog maintenance. Deviations from these conventions require a documented rationale and approval from the Principal Architecture function.

---

## Loop File Names

### Format

```
LOOP-XXX-Meaningful-Name.md
```

### Rules

- `XXX` is a zero-padded three-digit integer within the category range (see Loop ID section below). Examples: `001`, `101`, `304`.
- The name portion uses **Title-Case-With-Hyphens**: each word is capitalized, words are separated by hyphens, no underscores.
- No abbreviations in the file name unless the abbreviation is the canonical name of the thing being named. `ADR` is canonical (Architecture Decision Record); `impl` is not (spell out `Implementation`).
- The name must be self-explanatory without reading the file. `LOOP-005-Work.md` is not acceptable; `LOOP-005-Implementation.md` is.
- The `.md` extension is required. No other extension is permitted.

### Examples

```
LOOP-001-Architecture-Discovery.md    ✓ correct
LOOP-101-Bug-Fixing.md               ✓ correct
LOOP-301-ADR-Generation.md           ✓ correct (ADR is canonical)
LOOP-005-impl.md                     ✗ abbreviation not permitted
LOOP-005-do_work.md                  ✗ underscores not permitted
loop-005-implementation.md           ✗ must be uppercase LOOP
```

---

## Loop IDs

### Format

```
LOOP-NNN
```

Where `NNN` is a zero-padded three-digit integer (001 through 999).

### Category Ranges

| Range | Category | Description |
|---|---|---|
| 001–099 | Core | Foundational loops forming the base execution chain |
| 100–199 | Engineering | Domain-specific engineering work loops |
| 200–299 | Platform | Platform validation and runtime assessment loops |
| 300–399 | Governance | Framework governance and compliance loops |
| 400–499 | Release | Release, deployment, and post-release verification loops |
| 500–599 | AI | AI-specific workflow loops (reserved) |
| 600–699 | Research | Research and exploration loops (reserved) |
| 700–799 | Experimental | Experimental loops pending promotion or retirement |
| 800–999 | Reserved | Reserved by Principal Architecture for future categories |

### Assignment Rules

- IDs are assigned sequentially within the applicable range by the Principal Architecture function.
- No team may self-assign a Loop ID. All IDs must appear in SPEC-010 before being used.
- A retired or archived loop's ID is never reused.

---

## Agent IDs

### Format

```
ROLE-DESCRIPTOR
```

SCREAMING-KEBAB-CASE: all uppercase, words separated by hyphens.

### Rules

- Total length: 4–12 characters (excluding hyphens).
- Must be role-descriptive: an Agent ID should indicate what the agent does, not who operates it.
- The first component is the role or domain; the second is the function. Additional components are permitted for disambiguation.
- No personal names, project names, or version numbers in Agent IDs.

### Standard Agent IDs

| Agent ID | Role | Primary Loop(s) |
|---|---|---|
| `ARCH-SCANNER` | Architecture Discovery Maker | LOOP-001 |
| `CTX-ASSEMBLER` | Context Assembly Maker | LOOP-002 |
| `TASK-ANALYST` | Task Discovery Maker | LOOP-003 |
| `PLAN-MAKER` | Planning Maker | LOOP-004 |
| `IMPL-EXECUTOR` | Implementation Maker | LOOP-005 |
| `VERIF-AGENT` | Verification Checker | LOOP-006 |
| `REFL-RECORDER` | Reflection Maker | LOOP-007 |
| `BUG-ANALYST` | Bug Fixing Maker | LOOP-101 |
| `REFAC-MAKER` | Refactoring Maker | LOOP-102 |
| `TEST-GEN` | Test Generation Maker | LOOP-103 |
| `DOC-WRITER` | Documentation Maker | LOOP-104 |
| `REVIEW-AGENT` | Code Review Checker | LOOP-105 |
| `SEC-VALIDATOR` | Security Validation Checker | LOOP-207 |
| `GOV-AUDITOR` | Governance Compliance Checker | LOOP-303 |

New Agent IDs must be approved by the Principal Architecture function before use.

---

## Artifact Naming

### STATUS Files

```
STATUS-NNN.md
```

Where `NNN` matches the Loop ID number. One STATUS file per loop, stored alongside the loop specification.

### SKILL Files

```
SKILL-NNN.md
```

Where `NNN` matches the Loop ID number. One SKILL file per loop.

### Reflection Artifacts

```
REFLECTION-NNN-{run-id}.md
```

Where:
- `NNN` is the Loop ID number (zero-padded three digits).
- `{run-id}` follows the Run ID format below.

### Metadata Artifacts

```
METADATA-NNN-{run-id}.md
```

Same structure as Reflection artifacts.

### Run ID Format

```
LOOP-NNN-YYYYMMDD-NNN
```

Where:
- `LOOP-NNN` is the full Loop ID (e.g., `LOOP-001`).
- `YYYYMMDD` is the ISO date of the run (e.g., `20260627`).
- The trailing `NNN` is a zero-padded sequence number for the day, starting at `001`. The first run of LOOP-001 on 2026-06-27 is `LOOP-001-20260627-001`; the second is `LOOP-001-20260627-002`.

**Example Run IDs:**

```
LOOP-001-20260627-001
LOOP-101-20260627-003
LOOP-304-20260628-001
```

---

## Specification Files

### Format

```
SPEC-NNN-Name.md
```

Where:
- `NNN` is a zero-padded three-digit integer (001 through 999).
- `Name` uses kebab-case (all lowercase, hyphen-separated words). No Title Case for the name portion of SPEC files.
- The `.md` extension is required.

### Examples

```
SPEC-001-LOOP-CONTRACTS.md     ✓ correct (kebab uppercase is acceptable when the words are acronyms)
SPEC-005-Metrics.md            ✓ correct
SPEC-010-Loop-Catalog.md       ✓ correct
SPEC-005_metrics.md            ✗ underscores not permitted
spec-005-metrics.md            ✗ SPEC prefix must be uppercase
```

SPEC IDs are assigned by the Principal Architecture function.

---

## Guide and Reference Files

Shared guide and reference files in `docs/loops/shared/` do not use numeric IDs unless they are SPEC-NNN files. They use descriptive names in one of two styles:

- **SCREAMING-KEBAB-CASE** for standards that are referenced by machine tooling or that serve as normative references: `LOOP-STANDARD.md`, `ENGINEERING-LOOP-GUIDE.md`.
- **Title-Case** for human-readable reference documents that are cited but not processed by tooling: `human-oversight-gates.md`, `verification-standards.md`, `risk-controls.md`, `metrics-definitions.md`, `naming-standards.md`.

The lowercase style for reference documents is intentional: it visually distinguishes them from normative SPEC-NNN and LOOP-NNN documents.

---

## Branch and PR Naming

### Loop Specification Changes

```
loop/LOOP-NNN-brief-description
```

Examples:
```
loop/LOOP-001-add-tenant-context-input
loop/LOOP-207-major-v2-security-boundary
```

### Framework/Specification Changes

```
framework/SPEC-NNN-brief-description
framework/standard-brief-description
```

Examples:
```
framework/SPEC-005-add-runtime-metrics
framework/naming-standards-initial
```

### Rules

- All branch names are lowercase-kebab-case after the prefix.
- Branch names must not contain spaces, underscores, or special characters other than hyphens and forward slashes.
- The description component should be brief (2–5 words) and describe the change, not the ticket number.
- PR titles must begin with the conventional commit type and scope: `feat(LOOP-001): add tenant context input`.

---

## Versioning

### Specification and Loop Versions

All AEOS loop specifications and SPEC-NNN documents use a two-component semantic version:

```
MAJOR.MINOR
```

- No patch component. Bug fixes that do not alter contracts are MINOR increments.
- **Draft** documents: version begins at `0.1` and increments as `0.MINOR` until the document is promoted to Active.
- **First Active version:** always `1.0`. The transition from Draft to Active always sets the version to `1.0`, regardless of the Draft version number.
- **MAJOR increment:** `1.0 → 2.0`, `2.3 → 3.0`. Breaking changes to inputs, outputs, or gate surface.
- **MINOR increment:** `1.0 → 1.1`, `1.4 → 1.5`. Additive or clarifying changes. No break to existing conformant consumers.

### Version in File Names

Versions are never embedded in file names. The version is declared in the file's header block and tracked in the Version History section. Storing a version in the file name would require renaming files on every version increment, breaking cross-references.

---

## Governance Event Log Files

```
docs/governance/events/GOVEVENT-{LOOP-ID}-{date}-{NNN}.md
```

Where:
- `{LOOP-ID}` is the full loop identifier (e.g., `LOOP-001`).
- `{date}` is the ISO date of the event (e.g., `20260627`).
- `{NNN}` is a zero-padded sequence number for governance events on that date for that loop.

Example: `GOVEVENT-LOOP-001-20260627-001.md`

---

## Metric Keys

Metric keys follow SPEC-005 §1. Summary:

- Format: `{prefix}.{metric_name}` where prefix is one of `run.`, `gate.`, `verification.`, `reflection.`, `runtime.`, `framework.`, or a loop-abbreviation-qualified variant.
- `metric_name` is `snake_case`, all lowercase, underscores as separators, no hyphens.
- Full metric key examples: `run.duration_seconds`, `gate.hard.count`, `framework.loops.active_count`.

---

## References

- `docs/loops/shared/SPEC-005-Metrics.md` — Metric key naming conventions
- `docs/loops/shared/SPEC-010-Loop-Catalog.md` — Loop ID assignment and registry
- `docs/loops/shared/LOOP-STANDARD.md` §5 — File naming conventions for loop documents

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

