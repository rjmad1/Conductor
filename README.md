# Conductor

Conductor is a platform designed to manage and run complex workflows and integrations. This repository houses the complete program documentation suite, architecture patterns, and engineering standards.

## Documentation Structure

All primary documentation is located inside the [docs](file:///c:/Users/rajaj/Projects/Conductor/docs) directory, organized as follows:

*   **[00-Executive-Summary.md](file:///c:/Users/rajaj/Projects/Conductor/docs/00-Executive-Summary.md)**: High-level overview of the entire Conductor program and objectives.
*   **[vision/](file:///c:/Users/rajaj/Projects/Conductor/docs/vision)**: Long-term vision, strategic thesis, and product concepts.
*   **[business/](file:///c:/Users/rajaj/Projects/Conductor/docs/business)**: Pricing strategy, business models, user personas, and customer segments/journeys.
*   **[product/](file:///c:/Users/rajaj/Projects/Conductor/docs/product)**: Capabilities, product requirements, user stories, and development roadmap.
*   **[architecture/](file:///c:/Users/rajaj/Projects/Conductor/docs/architecture)**: Comprehensive system architectures including Data, AI, Integration, Security, and Agent models.
*   **[api/](file:///c:/Users/rajaj/Projects/Conductor/docs/api)**: API and Event contracts.
*   **[standards/](file:///c:/Users/rajaj/Projects/Conductor/docs/standards)**: Technical coding standards, repository setup, compliance, and governance frameworks.
*   **[adr/](file:///c:/Users/rajaj/Projects/Conductor/docs/adr)**: Architectural decision records (ADRs) tracking structural and business choices.
*   **[runbooks/](file:///c:/Users/rajaj/Projects/Conductor/docs/runbooks)**: SRE guidelines, runbooks, monitoring, and incident management procedures.
*   **[ai/](file:///c:/Users/rajaj/Projects/Conductor/docs/ai)**: RAG system architecture, LLM agent frameworks, prompt libraries, and evaluation models.
*   **[program/](file:///c:/Users/rajaj/Projects/Conductor/docs/program)**: Delivery timelines, resource planning, and release milestones.
*   **[gaps/](file:///c:/Users/rajaj/Projects/Conductor/docs/gaps)**: Detailed gap matrices across product, business, technology, and compliance.
*   **[onboarding/](file:///c:/Users/rajaj/Projects/Conductor/docs/onboarding)**: Glossary, onboarding guides, domain model references, and system context.

---

## Synchronization & Automation

To keep the local repository and the GitHub remote (`https://github.com/rjmad1/Conductor`) synchronized seamlessly, several helpers have been set up:

### 1. Automatic Post-Commit Push Hook
A Git `post-commit` hook is configured locally in `.git/hooks/post-commit`. 
*   **Behavior**: Every time you create a local commit (via terminal, VS Code, or other IDEs), it automatically pushes the changes to the GitHub remote repository main branch. 
*   **Benefit**: You never have to manually run `git push` after committing.

### 2. Synchronization Scripts
If you want to sync all local additions and modifications in a single command, two utility scripts are provided in the root directory:

#### PowerShell (Windows)
Run the script to pull remote updates with a rebase (to avoid merge conflicts), add all changed documentation files, commit them with a timestamp, and push:
```powershell
./sync.ps1
```

#### Bash (Git Bash / Linux / macOS)
Run the shell script counterpart:
```bash
./sync.sh
```
