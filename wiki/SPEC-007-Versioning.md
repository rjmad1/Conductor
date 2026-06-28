---
# PROVENANCE METADATA
Original Path: docs/loops/shared/SPEC-007-Versioning.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Semantic versioning and lifecycle rules for loop specifications.
Generalized Purpose: Semantic versioning and lifecycle rules for loop specifications.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SPEC-007 — Versioning

**Version:** 1.0
**Status:** Active
**Type:** Engineering Specification
**Governs:** Co-revision protocol and version compatibility guarantees for all LOOP-XXX documents
**Authority:** Principal AI Engineering Architect

---

## Purpose

This specification defines the co-revision protocol and version compatibility guarantees that allow individual loops in the AEOS framework to evolve independently while maintaining chain integrity. A loop chain in which upstream and downstream loops have incompatible versions is unsafe: the downstream loop's assumptions about its inputs may be violated, producing incorrect outputs, failed verifications, or silent errors.

SPEC-007 answers three questions for every loop author and maintainer:

1. When must two loops be revised simultaneously (co-revision)?
2. What backward-compatibility guarantees does a loop provide to its downstream consumers?
3. How long does a downstream loop have to adapt after an upstream MAJOR version increment?

---

## §1 — Semantic Versioning Recap

AEOS loop specifications use a two-component semantic version: `MAJOR.MINOR`.

The authoritative versioning rules are in `SPEC-001-LOOP-CONTRACTS.md` §8. This document adds the co-revision protocol layer on top of those rules. A brief summary for co-revision analysis:

- **MAJOR** increments signal a breaking change: a change to inputs, outputs, gate surface area, or any contract element that downstream consumers rely on. Downstream loops must be assessed for impact after any MAJOR increment.
- **MINOR** increments signal additive or clarifying changes: changes that do not alter existing input/output contracts, do not add or remove gates, and do not change the loop's behavior for consumers who were already conformant.
- There is no patch component. Bug fixes that do not alter contracts are MINOR increments.
- Draft versions use `0.MINOR`. The first Active version is `1.0`.

---

## §2 — Co-Revision Definition

A **co-revision** is required when a MAJOR change to Loop A changes an output that Loop B declares as a required input, such that Loop B would produce incorrect results or fail input validation without a simultaneous update to Loop B.

More precisely, a co-revision is required between Loop A (upstream) and Loop B (downstream) when at least one of the following is true after Loop A's proposed MAJOR change:

1. An artifact that Loop B declares as a required input in its Inputs section is no longer produced by Loop A at the same path, name, or schema.
2. A field that Loop B reads from Loop A's output has been renamed, removed, or changed to an incompatible type.
3. A postcondition that Loop A previously guaranteed (and that Loop B's preconditions rely on) is no longer guaranteed.
4. A gate that Loop B expected to have cleared before it begins is no longer present in Loop A, causing Loop B to receive unreviewed material.

A co-revision is **not** required when:
- Loop A adds a new output that Loop B does not consume.
- Loop A clarifies a step without changing its outputs.
- Loop A changes internal implementation details that are not visible to consumers.

When in doubt, the Maintainer of Loop A should consult the Maintainer of Loop B to assess impact before merging the MAJOR increment.

---

## §3 — Co-Revision Protocol

When a co-revision is required between Loop A (upstream, MAJOR change) and Loop B (downstream, required update), the following protocol must be followed:

### §3.1 — Simultaneous Commit

Both loop specifications (Loop A and Loop B) must be updated in the same commit. A MAJOR increment to Loop A that requires a co-revision of Loop B may not be merged until Loop B's update is also ready to merge.

If simultaneous merge is not feasible (e.g., Loop B is maintained by a different team), Loop A's MAJOR increment must be kept in a release branch and not merged to the main branch until Loop B's update is confirmed ready. The grace period (§6) begins from the date Loop A's MAJOR increment is published to the main branch, not from when Loop B's update is needed.

### §3.2 — Version History Annotation

Both loop specifications must record the co-revision in their Version History sections:

```
| 2.0 | 2026-07-01 | Maintainer Name | MAJOR: ... Co-revised with LOOP-002 v1.1 |
```

The annotation must name the co-revised loop and its version. This makes the co-revision relationship visible in the document history without requiring inspection of the catalog.

### §3.3 — Catalog Update

The SPEC-010 Loop Catalog must be updated to reflect:

1. The new version of Loop A.
2. The new version of Loop B.
3. The co-revision relationship in the Notes column of both entries.
4. The `Depends On` entry in Loop B must declare the minimum compatible upstream version (see §3.4).

The catalog update must occur within 5 business days of the co-revision being merged (per SPEC-010 §7).

### §3.4 — Minimum Compatible Version Declaration

The downstream loop (Loop B) must declare the minimum compatible upstream version in its Dependencies section:

```
LOOP-001 — Architecture Discovery — provides MODULE-CATALOG artifact
  Minimum compatible version: 2.0
```

This declaration informs any engineer running Loop B which version of Loop A must be present before Loop B is safe to execute.

---

## §4 — Compatibility Matrix

The compatibility matrix records which versions of each loop are compatible with which versions of their dependencies. The matrix is updated at each MAJOR version increment.

### §4.1 — Core Loop Chain Compatibility (LOOP-001 through LOOP-007)

| Loop | Depends On | Compatible With | Minimum Version | Notes |
|---|---|---|---|---|
| LOOP-001 | None | — | — | Chain head; no upstream |
| LOOP-002 | LOOP-001 | LOOP-001 v1.x | 1.0 | Any LOOP-001 v1 MINOR |
| LOOP-003 | LOOP-001, LOOP-002 | LOOP-001 v1.x, LOOP-002 v1.x | 1.0, 1.0 | |
| LOOP-004 | LOOP-002, LOOP-003 | LOOP-002 v1.x, LOOP-003 v1.x | 1.0, 1.0 | |
| LOOP-005 | LOOP-004 | LOOP-004 v1.x | 1.0 | |
| LOOP-006 | LOOP-005 | LOOP-005 v1.x | 1.0 | |
| LOOP-007 | LOOP-006 | LOOP-006 v1.x | 1.0 | |

### §4.2 — Matrix Maintenance

This matrix must be updated within 5 business days of any MAJOR version increment to any loop in the framework. The Maintainer of the incremented loop is responsible for initiating the update. The Principal Architecture function approves the updated matrix.

---

## §5 — Backward Compatibility Guarantee

### §5.1 — MINOR Compatibility Guarantee

A loop at version `N.M` provides the following guarantee to all downstream loops that declared a dependency on version `N.x` (any MINOR within the same MAJOR):

> All outputs declared in version N.0 remain present, at the same paths and schemas, in version N.M. Downstream loops that were conformant with N.0 will be conformant with N.M without modification.

This guarantee applies to all declared outputs in the loop's Outputs section, all postconditions declared in the loop's Verification section, and all gate positions declared in the loop's Human Approval Gates section.

### §5.2 — MAJOR Compatibility Disclaimer

A loop at version `N+1.0` provides **no compatibility guarantee** relative to version `N.x`. Downstream loops that depend on version `N.x` must be assessed for impact (§2) and, if affected, co-revised (§3) before they may execute against an upstream loop at `N+1.0`.

### §5.3 — Communication Obligation

The author of a MAJOR increment must, before merging to the main branch:

1. Identify all known downstream loops (from the SPEC-010 catalog's "Known Downstream Consumers" column).
2. Notify the Maintainer of each downstream loop of the pending MAJOR increment.
3. Provide a summary of the breaking changes to each downstream Maintainer.
4. Record this notification in the governance event log (SPEC-006 §5.2, event GE-07).

---

## §6 — Grace Period

A downstream loop that was conformant with upstream version `N.x` has a **90-day grace period** from the date the upstream `N+1.0` version is published to the main branch to update its dependency declaration.

During the grace period:

- The downstream loop may continue to execute against the upstream loop at version `N.x` if that version remains available (e.g., via a maintained release branch).
- The downstream loop's STATUS file must record a `compatibility_warning` noting the pending upstream MAJOR increment and the grace period deadline.
- If the downstream loop cannot be updated within the grace period, the downstream Maintainer must request an extension from the Principal Architecture function. Extensions are granted in 30-day increments and must be documented in the catalog.

After the grace period expires without an update, the downstream loop must be flagged `Stale` (SPEC-006 §3.2) and must not be used in new work until it is updated.

---

## §7 — Version Pinning

A loop may pin to a specific upstream version range to explicitly declare which versions it is compatible with. Version pinning is permitted and sometimes necessary (e.g., when an upstream MAJOR increment introduced changes that the downstream loop is not yet ready to consume).

### §7.1 — Pin Declaration Syntax

Version pins are declared in the Dependencies section:

```
LOOP-001 — Architecture Discovery — provides MODULE-CATALOG artifact
  Compatible: >= 1.0, < 2.0
```

This syntax means: this loop requires LOOP-001 at version 1.0 or higher, but not 2.0 or higher.

### §7.2 — Pin Review Obligation

A pinned dependency must be reviewed whenever the pinned upstream releases a new MAJOR version. The reviewing Maintainer must:

1. Assess whether the new MAJOR version requires a co-revision (§2).
2. Either update the pin to include the new MAJOR (after co-revising if needed), or document why the pin remains at the current range.
3. Record the review outcome in the downstream loop's Version History.

A version pin that has not been reviewed within 90 days of an upstream MAJOR increment is treated as a grace period expiry (§6).

---

## References

- `docs/loops/shared/SPEC-001-LOOP-CONTRACTS.md` §8 — Authoritative semantic versioning rules
- `docs/loops/shared/SPEC-006-Governance.md` — Governance event recording for MAJOR increments
- `docs/loops/shared/SPEC-010-Loop-Catalog.md` — Loop registry and compatibility matrix maintenance
- `docs/loops/shared/SPEC-011-LOOP-AUTHORING-GUIDE.md` — Authoring guidance that references this specification

---

## Version History

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-06-27 | Principal AI Engineering Architect | Initial Active version |

