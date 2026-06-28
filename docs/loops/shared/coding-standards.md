---
# PROVENANCE METADATA
Original Path: docs/loops/shared/coding-standards.md
Original Version: 1.0
Extraction Date: 2026-06-27
Original Purpose: Code style and quality rules for agent execution.
Generalized Purpose: Code style and quality rules for agent execution.
Dependencies Removed: RajaJeevanLoopEngineering business workflow configurations
Dependencies Retained: None
Compatibility Notes: Fully compatible with standard loop orchestrators and documentation frameworks.
Migration Notes: Direct copy of the general loop framework specification.
---
# Coding Standards

**Version:** 1.0  
**Status:** Active  
**Type:** Reference Document  
**Authority:** Principal AI Engineering Architect  
**Applies To:** All source code and configuration files generated or modified in this repository

---

## Purpose

This document defines the canonical coding standards that must be followed by AI agents and human engineers when producing or refactoring implementation artifacts in this repository. These standards ensure consistency, readability, maintainability, safety, and correctness across all languages and configuration formats used in the project.

---

## Language-Specific Standards

### Java

1. **Code Structure & Layout**
   - Follow standard Oracle/OpenJDK formatting guidelines.
   - Organize imports: java.*, javax.*, external libraries, project imports (alphabetical within groups).
   - Use standard class layout: static fields, instance fields, constructors, public methods, private helper methods.

2. **Types & Variables**
   - Explicitly declare types where possible. Avoid unnecessary type inferencing if it reduces readability.
   - Use descriptive, camelCase names for variables and methods. Use PascalCase for classes and interfaces.
   - Declare variables `final` where applicable to enforce immutability.

3. **Exceptions & Error Handling**
   - Never swallow exceptions silently. Always log or wrap them in custom exceptions.
   - Use specific runtime exception classes rather than generic `RuntimeException` or `Exception`.
   - Provide meaningful contextual messages in exception constructors.

4. **Testing**
   - Write comprehensive unit tests for all new classes using JUnit 5 and Mockito.
   - Maintain minimum 80% line coverage for new source code modifications.

### SQL

1. **Syntax & Case Rules**
   - All SQL keywords must be in UPPERCASE (e.g., `SELECT`, `FROM`, `WHERE`, `JOIN`, `GROUP BY`, `ORDER BY`).
   - Identifiers (table names, column names) must be in snake_case, lowercase.

2. **Queries & Safety**
   - Always use prepared statements and parameterized queries to prevent SQL injection.
   - Never use string interpolation or concatenation to build queries containing user input.
   - Avoid `SELECT *`. Explicitly list the columns required.
   - Use explicit table aliases when joining tables to avoid ambiguity.

3. **Schema Migrations**
   - All schema modifications must be scripted as repeatable migration steps (e.g., via Liquibase or Flyway).
   - Avoid manual schema manipulation in any execution loop.

### YAML / JSON

1. **Syntax & Formatting**
   - Indentation must be exactly 2 spaces. Do not use tab characters.
   - YAML files must end with a single newline character.
   - Keys must be written in snake_case (preferred) or camelCase depending on the context, but must remain consistent within the same file.

2. **Safety & Anchors**
   - Avoid complex YAML anchors and aliases unless explicitly necessary for DRY structure, as they reduce readability for agents.
   - Ensure all configuration values are typed correctly: wrap string values in double quotes when they contain special characters or represent numeric strings (e.g., version strings `"1.0"`).

---

## General Rules

1. **DRY (Don't Repeat Yourself)**
   - Extract duplicated logic into common helper classes or shared utilities in `shared/`.
   - Prioritize readability over extreme normalization: if extracting logic makes it hard to understand the flow, write simple inline code.

2. **Single Responsibility**
   - Classes and methods must have exactly one reason to change.
   - Prefer small, focused helper classes over large monolithic handlers.

3. **Comments & Documentation**
   - Write clean, self-documenting code. Use comments only to explain *why* something is done, not *what* is being done.
   - Maintain accurate Javadoc blocks for all public API interfaces and classes.

---

## Formatting

- **Line Length:** Limit source code lines to a maximum of 120 characters.
- **Whitespace:** Use empty lines to separate logical blocks within methods. Avoid redundant consecutive blank lines.
- **File Encoding:** All files must be saved using UTF-8 encoding.

---

## Review Checklist

- [ ] Formatting aligns with standard guidelines (line length, indentation).
- [ ] No secrets, credentials, or personal keys are hardcoded in source or configuration.
- [ ] Prepared statements are used for all database queries.
- [ ] Proper error handling is implemented; exceptions are not swallowed.
- [ ] Unit test files are created and pass successfully.

