# ADR-GOV-003: Module Ownership Standard

## Status
ACCEPTED

## Context
In a multi-agent and multi-developer repository, clear ownership of files and folders is necessary to prevent unauthorized modifications, regression bugs, and compliance gaps.

## Decision
We enforce role-based module ownership using the GitHub `CODEOWNERS` system:
*   Every subdirectory must have at least one designated owner role.
*   Changes affecting core architecture (`/docs/architecture/`), API contracts (`/docs/api/`), or risk configurations (`/eos-manifest.yaml`) require approval from the `Platform Engineering Lead` and an `EDRB` representative.
*   Infrastructure code (`/infra/` and workflows) is owned by the `DevSecOps Lead`.
*   AI models and prompt files (`/docs/ai/`) are owned by the `AI Platform Architect`.

## Rationale
*   **Traceability**: Clarifies who is responsible for maintaining and reviewing specific platform layers.
*   **Security & Compliance**: Prevents unauthorized modifications to security or regulatory configurations.

## Consequences
*   GitHub will automatically request reviews from the assigned owner role for any PR affecting their module.
*   Branch protection rules will block merges until the designated owner approves.
