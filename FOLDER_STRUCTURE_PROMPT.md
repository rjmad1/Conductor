# Folder Structure Setup — Antigravity IDE Prompt
> Paste the block below into Antigravity. Do not modify it before pasting.  
> Protocol version: 1.0 | Last updated: 2026-06-24

---

## PROMPT (copy everything inside the fence)

```
You are restructuring the Conductor project repository to be maintainable, explainable, and scalable.
Follow the Loop Engineering rules in AGENTS.md exactly.
This is a two-phase task. Do NOT create or move any files until I approve Phase 1.

---

### CONTEXT

This is the Conductor project — an enterprise AI-assisted IDE platform.
The repo currently has:
- Root-level markdown control files (AGENTS.md, README.md, CHANGELOG.md, etc.)
- A docs/ tree with ~70 markdown files across 13 subdirectories
- Two loose scripts at root: sync.sh, sync.ps1
- One manifest at root: eos-manifest.yaml
- No src/, tests/, scripts/, config/, memory/, prompts/, specs/, or plans/ directories yet

Guiding principles for this restructure:
1. Audience-first organization — every folder name must be self-explanatory to a new engineer or agent in under 3 seconds.
2. Lifecycle separation — documentation, code, memory, prompts, specs, and configuration are separate trees. Never mixed.
3. Loop Engineering memory tiers — working, organizational, and experiment memory each get an isolated location.
4. Spec-first workflow — every feature produces a spec and a plan before code. These live in dedicated folders.
5. Versioned prompts — prompt templates are versioned artifacts, not loose files.
6. No depth beyond 4 levels — any folder requiring a 5th level of nesting signals a design problem.
7. Preserve all existing files — restructure means move and organize, never delete.
8. Every new folder gets a README.md stub — one paragraph explaining what belongs there and what does not.

---

### PHASE 1 — PLAN (do not touch the filesystem yet)

Produce a complete proposed folder tree using the structure below as your target.
For every proposed move, show: [current path] → [new path].
For every new folder with no existing files, mark it: (NEW — stub README only).
Flag any ambiguous placement decisions and your reasoning.

TARGET STRUCTURE:

conductor/                          ← repo root
│
├── [ROOT CONTROL FILES]            ← no subfolder; these stay at root
│   README.md
│   AGENTS.md
│   CONTEXT_FILES.md
│   ROADMAP_PROMPT.md
│   CHANGELOG.md
│   CONTRIBUTING.md
│   CODEOWNERS
│   LICENSE
│   .editorconfig
│   .gitignore
│
├── docs/                           ← all human-authored documentation; no code
│   ├── 00-Executive-Summary.md     ← stays at docs root; entry point for every reader
│   ├── vision/                     ← why the project exists (Strategic-Thesis, Product-Vision, Business-Vision)
│   ├── product/                    ← what we build (Roadmap, PRD, User-Stories, Capabilities, Acceptance-Criteria*)
│   ├── business/                   ← market and commercial context (Personas, GTM, Pricing, Segments, Journeys)
│   ├── architecture/               ← how it is built (all Architecture .md files + System-Context)
│   ├── ai/                         ← AI-specific design (Agent-Framework, Evaluation-Framework, RAG, etc.)
│   ├── api/                        ← interface contracts (API-Contracts, Event-Contracts)
│   ├── onboarding/                 ← how to get up to speed fast (System-Overview, Domain-Model, Context-Pack, Glossary, Developer-Onboarding)
│   ├── standards/                  ← engineering rules and governance (Coding-Standards, Compliance, Wisdom-OS, etc.)
│   ├── adr/                        ← architecture decision records (all ADR-GOV-* files + Decision-Records)
│   ├── gaps/                       ← known gaps by domain (all *-Gaps.md files)
│   ├── program/                    ← delivery planning (Implementation-Plan, Release-Plan, Resource-Plan)
│   └── runbooks/                   ← operational procedures (Runbooks, SRE, Monitoring, Incident-Management)
│
├── memory/                         ← Loop Engineering memory tiers (AGENTS.md §3.2)
│   ├── README.md
│   ├── working/                    ← ephemeral session artifacts; listed in .gitignore
│   │   └── README.md
│   ├── organizational/             ← validated, evidence-gated, human-approved artifacts
│   │   ├── README.md
│   │   ├── decisions/              ← decision records with rationale (what + why + evidence + timestamp)
│   │   ├── patterns/               ← reusable code and design patterns promoted from sessions
│   │   └── learnings/              ← post-session learning captures from closeout prompts
│   └── experiments/                ← isolated trial environments; no writes to organizational/
│       └── README.md
│
├── prompts/                        ← versioned prompt templates (AGENTS.md §4)
│   ├── README.md
│   ├── registry.yaml               ← prompt version index: name, version, path, status (active/deprecated)
│   └── v1/                         ← version 1 prompt set
│       ├── session-start.md        ← moved from ROADMAP_PROMPT.md §"Start of Session"
│       ├── course-correction.md    ← moved from ROADMAP_PROMPT.md §"Mid-Session"
│       ├── session-closeout.md     ← moved from ROADMAP_PROMPT.md §"End of Session"
│       └── quick-rules.md          ← moved from ROADMAP_PROMPT.md §"Quick Rules"
│
├── specs/                          ← per-feature spec files; written before any code (AGENTS.md §12.1)
│   ├── README.md
│   ├── _template.md                ← canonical spec template
│   ├── active/                     ← specs for work items currently in flight
│   └── archived/                   ← specs for completed or cancelled work items
│
├── plans/                          ← per-feature execution plans; produced in Phase 1 of every session
│   ├── README.md
│   ├── _template.md                ← canonical plan template
│   ├── active/                     ← plans for work items currently in flight
│   └── archived/                   ← plans for completed or cancelled work items
│
├── evaluation/                     ← rubrics and scorecards (AGENTS.md §5)
│   ├── README.md
│   ├── rubrics/                    ← per-feature or per-workflow evaluation rubrics (locked before execution)
│   └── scorecards/                 ← historical evaluation results with evidence
│
├── src/                            ← all source code; no documentation here
│   ├── README.md
│   └── shared/                     ← cross-cutting utilities and types
│
├── tests/                          ← all test code; mirrors src/ structure
│   ├── README.md
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── config/                         ← environment and schema configuration
│   ├── README.md
│   ├── environments/
│   │   ├── local.yaml
│   │   ├── staging.yaml
│   │   └── production.yaml
│   └── schemas/                    ← move Schema-Definitions content here as machine-readable schemas
│
├── scripts/                        ← automation and build scripts
│   ├── README.md
│   ├── build/
│   ├── deploy/
│   └── sync/                       ← move sync.sh and sync.ps1 here
│
└── .github/                        ← CI/CD workflows (or .gitlab/ if applicable)
    └── workflows/
        ├── ci.yml                  ← (NEW stub)
        ├── cd.yml                  ← (NEW stub)
        └── pr-checks.yml           ← (NEW stub)


* docs/product/Acceptance-Criteria.md — create as stub (currently missing, required by AGENTS.md evidence gate)

---

STOP after Phase 1. Show me the full move list and flag any ambiguities. Wait for my approval before touching anything.

---

### PHASE 2 — EXECUTE (only after my approval)

Rules:
1. Move files first, create new stubs second, update references third.
2. Move files one directory at a time, confirm each directory before moving to the next.
3. For every new README.md stub, write exactly: folder purpose (1 sentence), what belongs here (3–5 bullets), what does NOT belong here (2–3 bullets).
4. For every new template stub (_template.md), include the section headers only — no filled-in content.
5. Update .gitignore to exclude memory/working/ and memory/experiments/.
6. Update CONTEXT_FILES.md file paths if any docs/ paths have changed.
7. Do not modify any file's internal content — only location.
8. After all moves: run a broken-link check across all .md files and report findings.

At completion, produce the end-of-session structured output (YAML) per ROADMAP_PROMPT.md §"End-of-Session Closeout".
```

---

## What This Prompt Will Produce

| Artifact | Description |
|---|---|
| `memory/` tree | Enforces Loop Engineering memory tier isolation |
| `prompts/v1/` | Extracts the 4 prompt templates from ROADMAP_PROMPT.md into versioned, individually addressable files |
| `specs/` + `plans/` | Gives the spec-first workflow (AGENTS.md §12.1) a dedicated home |
| `evaluation/` | Separates rubrics (pre-execution, locked) from scorecards (post-execution, historical) |
| `src/` + `tests/` | Scaffolds the code tree before any feature work begins |
| `config/` | Separates environment config from documentation |
| `scripts/sync/` | Removes loose scripts from root; keeps root clean and scannable |
| `.github/workflows/` | Stubs the CI/CD surface so it exists before it is needed |
| README stubs | Every new folder is self-documenting from day one |

## Files That Stay at Root (Never Move)

`README.md`, `AGENTS.md`, `CONTEXT_FILES.md`, `ROADMAP_PROMPT.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `CODEOWNERS`, `LICENSE`, `.editorconfig`, `.gitignore`, `eos-manifest.yaml`

These are repo-identity files. IDEs, GitHub, and agents all look for them at root.
