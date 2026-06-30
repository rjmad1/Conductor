---
# PROVENANCE METADATA
Original Path: docs/loops/shared/engineering-principles.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Core engineering principles governing the platform.
Generalized Purpose: Core engineering principles governing the platform.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Engineering Principles

**Version:** 1.0  
**Status:** Active  
**Type:** Reference Document  
**Authority:** Principal AI Engineering Architect  
**Applies To:** All loop authoring, review, and execution in this repository

---

## Purpose

This document defines the foundational engineering principles that govern all loop execution, specification design, and code creation in this repository. AI agents and human engineers must use these principles as their architectural compass.

---

## Principles

### 1. Simplicity Over Complexity (YAGNI)
- Choose the simplest design and implementation that meets requirements. Do not build abstractions for hypothetical future needs.
- Every line of code or configuration is a liability. Focus on deletion before addition, refactoring existing patterns rather than creating new ones.

### 2. Maker/Checker Separation
- Safety and reliability require independent validation. The agent or human that creates a code modification or documentation artifact (the Maker) must not be the same entity that verifies it (the Checker).
- Conformance criteria must be falsifiable and independently checkable without relying on self-report.

### 3. Fail Fast and Safely
- Loops must detect errors and validation failures at startup (via preconditions) or immediately during execution.
- If a loop cannot complete successfully, it must halt, roll back any partial or side-effect writes, and record its failure state in the STATUS file and Reflection artifact. Avoid silent failures or partial writes.

### 4. Idempotency of Execution
- Re-running a loop with the same inputs must produce equivalent outputs and must not corrupt or duplicate external state.
- Write operations to the repository or external services must be guarded by checks that verify if the change has already been applied.

---

## Application

- **Loop Design:** Every loop must incorporate these principles into its structure. Preconditions check for failure conditions immediately; verification criteria enforce Maker/Checker rules; the external state table ensures rollback and idempotency are documented.
- **Code Reviews:** All pull requests are evaluated against these principles. Code that introduces unnecessary abstraction or violates Maker/Checker separation will be rejected.

---

## Exceptions

Exceptions to these principles are rare and require explicit justification. Any exception must be declared in the loop specification's header block as a `Contract Exception` and must be approved by the Principal Architecture function before the loop can transition to `Active` status.

