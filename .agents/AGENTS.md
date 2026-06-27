---
# PROVENANCE METADATA
Original Path: docs/loops/templates/AGENTS-TEMPLATE.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Loop specification or framework asset.
Generalized Purpose: Loop specification or framework asset.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# AGENTS — LOOP-XXX Name

## Agent Roster

| Agent ID | Role | Responsibilities | Tools | Human Oversight |
|----------|------|-----------------|-------|-----------------|

## Agent Descriptions

### Agent: [ID]

**Role:**

**Responsibilities:**

**Tools Available:**

**Input Contract:**

**Output Contract:**

**Human Oversight Gate:**

**Failure Mode:**

---

## Coordination Protocol

## Handoff Sequence

## Escalation Path

---

## Workspace Rules for Conductor Project

### Loop Engineering & Synchronization Rules
- **Automatic Loop Verification:** Always check for the presence of useful, updated loops soon after starting development of any folder/subproject in this workspace.
- **Upstream Repository:** The source of truth for loops is `https://github.com/rjmad1/RajaJeevanLoopEngineering`.
- **Sync/Port Loop Process:** Ensure the local loops under `docs/loops/` and the rule execution engine under `RajaJeevanLoopEngineering/code/` match the remote repo. If newer loops exist, port them using `port-loops.ps1` or run the Java bootstrap script to retrieve them.
