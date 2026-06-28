# Prompt Generator — Context File Reference
> Which markdown files to provide, when, and why.  
> Protocol version: 1.0 | Last updated: 2026-06-24

Select files by session type. Always start with Tier 1. Add tiers based on the work being done.

---

## TIER 1 — Always Include (Every Session)
*Minimum viable context. Gives any prompt generator enough to understand what the project is, what it's for, and how to work in it.*

| File | Purpose |
|---|---|
| `docs/00-Executive-Summary.md` | Single-page authoritative project overview — the first file any agent reads |
| `docs/vision/Strategic-Thesis.md` | Why this product exists; the founding logic behind every decision |
| `docs/onboarding/System-Overview.md` | What the system is, its components, and how they relate |
| `docs/onboarding/Domain-Model.md` | Canonical entity definitions — prevents hallucinated terminology |
| `docs/onboarding/Context-Pack.md` | Key decisions, anti-patterns, and guard rails — the most important file for avoiding mistakes |
| `docs/standards/Engineering-Wisdom-OS.md` | Condensed operating manual: architecture, AI-dev workflow, governance |
| `AGENTS.md` | Loop Engineering rules — governs agent behavior in every session |

**Total: 7 files. Include all 7, every time.**

---

## TIER 2A — Include for Product & Roadmap Work
*Add these when the session involves feature planning, prioritization, spec writing, or roadmap items.*

| File | Purpose |
|---|---|
| `docs/product/Product-Vision.md` | What the product must become; shapes feature scope decisions |
| `docs/product/Roadmap.md` | Current Now/Next/Later; prevents work on already-decided items |
| `docs/product/Product-Requirements.md` | Functional requirements and acceptance criteria baseline |
| `docs/product/User-Stories.md` | Story-level scope; grounds agents in real user needs |
| `docs/product/Capabilities.md` | What the system can do today vs. what's planned |
| `docs/business/Personas.md` | Who the users are; prevents generic, persona-blind outputs |
| `docs/business/Customer-Journeys.md` | End-to-end flows; critical for cross-cutting feature work |
| `ROADMAP_PROMPT.md` | Session execution template — fill and attach for every roadmap work item |

---

## TIER 2B — Include for Architecture & Technical Work
*Add these when the session involves system design, integration, data modeling, or infrastructure decisions.*

| File | Purpose |
|---|---|
| `docs/architecture/Solution-Architecture.md` | High-level blueprint; the primary technical reference |
| `docs/architecture/Application-Architecture.md` | Application layer structure and component breakdown |
| `docs/architecture/Data-Architecture.md` | Data models, stores, and flow |
| `docs/architecture/Integration-Architecture.md` | External integrations and system boundaries |
| `docs/api/API-Contracts.md` | Interface contracts — prevents breaking changes |
| `docs/api/Event-Contracts.md` | Event schemas and async patterns |
| `docs/onboarding/Glossary.md` | Shared language; eliminates ambiguous terminology in technical prompts |

---

## TIER 2C — Include for AI / Agent Feature Work
*Add these when the session involves building, modifying, or evaluating AI agents or ML components.*

| File | Purpose |
|---|---|
| `docs/ai/Agent-Framework.md` | How agents are structured and orchestrated in this project |
| `docs/ai/Evaluation-Framework.md` | How AI outputs are evaluated — defines the quality bar |
| `docs/ai/Knowledge-Model.md` | How the system represents and retrieves knowledge |
| `docs/ai/RAG-Architecture.md` | Retrieval-augmented generation design — critical for AI feature work |
| `docs/ai/Prompt-Library.md` | Existing prompt patterns — prevents duplication and drift |
| `docs/architecture/AI-Architecture.md` | AI system architecture and component boundaries |
| `docs/architecture/Agent-Architecture.md` | Agent topology and communication patterns |

---

## TIER 3 — Include for Governance, Standards & Compliance Work
*Add these only when the session involves ADR reviews, audit prep, compliance checks, or engineering standards changes.*

| File | Purpose |
|---|---|
| `docs/standards/Engineering-Governance-System.md` | Governance model and decision authority |
| `docs/standards/Coding-Standards.md` | Language and style rules — for code generation sessions |
| `config/schemas/Schema-Definitions.md` | Canonical data schemas — for any data or API work |
| `docs/standards/Compliance.md` | Regulatory and compliance constraints |
| `docs/standards/Risk-Register.md` | Known risks — prevents repeating flagged decisions |
| `docs/adr/Decision-Records.md` | Index of all architecture decisions |
| `docs/adr/ADR-GOV-010-Specification-Governance-Model.md` | How specs are owned and changed |

---

## TIER 4 — Situational (Pull Only When Directly Relevant)
*Do not include by default. Pull only when the specific topic is in scope for the session.*

| File | When to include |
|---|---|
| `docs/gaps/Technical-Gaps.md` | Session is specifically addressing a known technical gap |
| `docs/gaps/Product-Gaps.md` | Session is filling a known product gap |
| `docs/gaps/AI-Gaps.md` | Session targets a known AI/ML capability gap |
| `docs/gaps/Infrastructure-Gaps.md` | Session involves infrastructure changes |
| `docs/gaps/Business-Gaps.md` | Session has a business model or monetization dimension |
| `docs/gaps/Compliance-Gaps.md` | Session has a regulatory or security dimension |
| `docs/business/Business-Model.md` | Session has commercial or pricing implications |
| `docs/business/Go-To-Market.md` | Session involves launch, positioning, or GTM work |
| `docs/business/Pricing-Strategy.md` | Session has pricing or packaging implications |
| `docs/program/Implementation-Plan.md` | Session requires cross-team coordination or dependency mapping |
| `docs/program/Release-Plan.md` | Session is release-gated |
| `docs/program/Resource-Plan.md` | Session requires capacity or staffing decisions |
| `docs/standards/EDRB-2026-06-Conductor-MVP.md` | MVP-scope validation only |
| `docs/architecture/Security-Architecture.md` | Session has security or auth implications |
| `docs/architecture/Infrastructure-Architecture.md` | Session changes deployment or infra topology |
| `CHANGELOG.md` | Session needs recent change history for continuity |

---

## Session Type → File Set Quick Reference

| Session type | Tiers to include |
|---|---|
| Roadmap item / feature spec | 1 + 2A |
| Technical design / architecture | 1 + 2B |
| AI agent / ML feature | 1 + 2A + 2C |
| Full product session (roadmap + design) | 1 + 2A + 2B |
| AI feature + architecture | 1 + 2A + 2B + 2C |
| Governance / ADR review | 1 + 3 |
| Cold start / onboarding a new agent | 1 only (then add tiers as needed) |

---

## Missing Files — Create Before Prompt Generation Depends On Them

These files are referenced or implied in your existing docs but do not yet exist as standalone files:

| Missing file | Why it matters |
|---|---|
| `docs/product/Acceptance-Criteria.md` | Rubric source for evaluating feature completeness |
| `docs/onboarding/Decision-Log.md` | Running log of session-level decisions with rationale (supports AGENTS.md §3.1) |
| `docs/ai/Prompt-Versioning.md` | Prompt version registry (supports AGENTS.md §4) |
| `docs/standards/Observability-Contracts.md` | Defines leading/lagging indicators per system component (supports AGENTS.md §5.2) |
| `spec.md` (root) | Per-task spec file required by EWOS workflow (AGENTS.md §12.1) |
| `plan.md` (root) | Per-task plan file required by EWOS workflow (AGENTS.md §12.1) |

---

*This file itself should be included in Tier 1 after it is populated and validated.*
