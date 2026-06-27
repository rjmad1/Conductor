---
# PROVENANCE METADATA
Original Path: docs/loops/SKILL-002.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Loop skill documentation.
Generalized Purpose: Loop skill documentation.
Dependencies Removed: None
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# SKILL — Context Assembly

## Skill ID
SKILL-002

## Description
This skill governs the collection, filtering, and packaging of repository files to build a minimal, sufficient context for AI execution.

## Applicable Loops
LOOP-002

## Inputs
- Task description
- Project file tree
- Component/module schemas

## Outputs
- `docs/context/context-package.md` containing the assembled context files.

## Preconditions
- The repository structure is discoverable.

## Steps
1. Parse the task description.
2. Select target files, tests, and configuration assets.
3. Validate total tokens are within budget limits.

## Postconditions
- Context package generated and verified.

## Error Handling
- Pause and escalate if critical dependent files are missing.

## Examples
- Run for task T-203.

## Version History
- 1.0 Initial setup.
