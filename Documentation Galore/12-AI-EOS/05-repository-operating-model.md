# AI-EOS Repository Operating Model

## Document Metadata
* **id:** EOS-05-REPO-OPS
* **title:** AI-EOS Repository Operating Model
* **description:** Defines the branch structures, PR workflows, and codebase governance for Conductor's repository-centric delivery.
* **owner:** Platform Engineering Lead
* **domain:** Platform Operations
* **tags:** [repository, branching, git, workflow, PR]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:38:00Z
* **updated:** 2026-06-24T16:38:00Z
* **related_artifacts:** [01-constitution.md, 14-sdlc-governance.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [ISO-27001-A.12.4, SOC2-CC8.1]
* **quality_score:** 1.00

---

## Purpose
In AI-EOS, the repository is the single source of truth and the runtime system of record. This document establishes standard patterns for directory layout, branching rules, PR templates, and automated validation policies.

---

## Workspace Directory Layout
```
/
├── .github/                 # CI/CD Workflows (GitHub Actions)
├── .agents/                 # Workspace Customizations, rules, and skills
│   ├── AGENTS.md            # Workspace Rules
│   └── skills/              # Custom agent skills
├── Documentation Galore/    # Complete documentation suite (System of Record)
│   ├── 00-Executive-Summary.md
│   ├── 04-Architecture/
│   ├── 07-Governance/
│   └── 12-AI-EOS/           # AI-EOS Specifications
├── src/                     # Source Code
│   ├── services/            # Microservices (Java/Temporal, Node.js)
│   └── common/              # Shared libraries
├── tests/                   # Test suites (Unit, Integration, E2E)
├── eos-manifest.yaml        # AI-EOS Root Configuration & Registry
├── sync.ps1                 # Local sync script (Windows)
└── sync.sh                  # Local sync script (Unix/Bash)
```

---

## Branching & Commit Governance

### Git Branch Strategy
* **Protected Branch:** `main` (No direct pushes allowed, except for Tier 0 documentation hotfixes by authorized release scripts).
* **Feature Branches:** `feature/<ticket-id>-<description>` or `agent/<agent-id>-<description>`.
* **Fix Branches:** `bugfix/<ticket-id>-<description>`.

### Branch Protection Rules
For the `main` branch, the following rules are active in GitHub:
- Require pull request reviews before merging.
- Require status checks to pass before merging (CI build, unit tests, SAST scan).
- Require signed commits (GPG signatures validated).
- Block force pushes.
- Block deletions.

---

## Pull Request Template

All human and agent pull requests must utilize the following structured format:

```markdown
# Pull Request: [Brief Description]

## Core Metadata
- **Ticket Reference:** #TICKET_ID
- **Proposed Risk Tier:** Tier [0 | 1 | 2 | 3 | 4]
- **Target Component:** [e.g., Temporal Workflow, API Contract]
- **Owner Group:** [e.g., Platform Team, AI Team]

## Economic & Complexity Evaluation
- **Business Value:** [Brief summary of the outcome]
- **TCO Delta:** [Infrastructure cost estimation]
- **Complexity Delta:** [Simpler alternatives considered and why this is necessary]

## Evidence of Verification
- **Test Pass Log:** [Link to CI output]
- **Security Check:** [Link to SAST/SCA output]
- **AI Evaluation Score (If applicable):** [e.g., Quality: 0.92, Safety: 1.00]

## Compliance & Traceability Mapping
- **Traceability Link:** [Business Objective ID] -> [Requirement ID] -> [Spec ID]
- **Affected Data Assets:** [e.g., customer_pii]
```

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Modifications must be approved by the Platform Engineering Lead.

## Validation Rules
* CI pipelines validate that PR descriptions match the Pull Request Template.
* Commit signature validation must run on every push.

## Audit Requirements
* Security audits quarterly verify that no administrator bypassed branch protection rules, and that all commits in the release history are cryptographically signed.
