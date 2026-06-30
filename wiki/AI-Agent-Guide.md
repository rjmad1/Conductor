# AI Agent Guide

## A. Purpose
This page documents operating instructions, context mapping strategies, and prompt libraries for AI Coding Agents and LLM developer tools working on the Conductor repository.

---

## B. Agent Onboarding & Context Files

AI agents must locate key entry points to understand the repository structure quickly:

- **Executive Context**: Read the central documentation dashboard:
  - [Executive Summary](file:///c:/Users/rajaj/Projects/Conductor/docs/00-Executive-Summary.md)
  - [System Overview](file:///c:/Users/rajaj/Projects/Conductor/docs/onboarding/System-Overview.md)
- **Code Index (CodeGraph)**: If a `.codegraph/` index exists at the repository root, leverage CodeGraph queries to resolve symbol references (classes, methods, variables) rather than running global grep commands.
- **Agent Guidelines**: Read [AGENTS.md](file:///c:/Users/rajaj/Projects/Conductor/AGENTS.md) to understand execution loops and feedback parameters.

---

## C. AI-Agent Development Workflows (EWOS)
Conductor requires agents to operate under strict context rules:

1. **Spec & Plan First**: Establish `spec.md` and `plan.md` tasks prior to executing code changes.
2. **Narrow Context**: Feeds only target classes and immediate dependencies into the LLM context window. Avoid dumping entire packages.
3. **Verification First**: Set up verification scripts and assert criteria before modifying production code.
4. **Immediate Lint & Test Validation**: Execute local gradle test validations immediately after modifying classes to identify compiler or test regression drops.

---

## D. Agent Artifact folders
- **`/agents/`**: Stores agent-specific context profiles, ownership metrics, templates, and handoff files.
- **`/prompts/`**: Houses reusable developer instruction templates (e.g., `FOLDER_STRUCTURE_PROMPT.md`, `ROADMAP_PROMPT.md`).
- **`/memory/`**: Isolated directory storing developer decision records and test outputs.

---

## E. Related Pages
- [Repository Structure](Repository-Structure)
- [Home](Home)
