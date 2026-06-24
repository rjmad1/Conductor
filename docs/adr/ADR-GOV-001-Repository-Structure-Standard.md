# ADR-GOV-001: Repository Structure Standard

## Status
ACCEPTED

## Context
As Conductor transitions from ideation to development, we need a standard, discoverable repository structure. A non-standard layout reduces developer onboarding speed, leads to file dispersion, and limits the integration of automated CI/CD and linting tools.

## Decision
We enforce a standardized root-level and documentation directory structure:
*   Root directories: `/docs`, `/config`, `/infra`, `/tests`, `/.github`, `/.agents`.
*   Standard root files: `README.md`, `LICENSE`, `CODEOWNERS`, `CONTRIBUTING.md`, `CHANGELOG.md`, `.gitignore`, `.editorconfig`.
*   Sub-directory layouts are strictly defined (e.g., `/docs/architecture`, `/docs/adr`, `/docs/api`, `/docs/standards`, `/docs/runbooks`, `/docs/onboarding`).

## Rationale
*   **Onboarding Efficiency**: Standard structures align with common developer expectations.
*   **Tooling Compatibility**: Code quality tools, static analyzers, and build tools expect standard locations.
*   **Governance Clarity**: Separates product-level code, operations scripts, and architectural policies.

## Consequences
*   All future files and subfolders must be placed within this layout.
*   The `eos-manifest.yaml` and `README.md` must be updated to refer to these new locations.
*   CI/CD pipelines will run formatting and validation checks based on this folder structure.
