# ADR-GOV-004: Configuration Standard

## Status
ACCEPTED

## Context
Misconfigured software, leaked API keys, and environment deviations represent significant operational risks. We require a uniform configuration standard across all services.

## Decision
We enforce the following configuration standards:
1.  **Configuration Separation**: Environment-specific configurations must live in `/config/environments/` and never be hardcoded in application source code.
2.  **No Secrets in Git**: No password, token, certificate, or API key may be committed to the repository. The `.gitignore` must block secret files. Secrets must be injected via environment variables (in local development) or AWS Secrets Manager / Vault (in production).
3.  **Feature Flags**: Features in development must be protected by feature flags configured under `/config/feature-flags/`.

## Rationale
*   **Security Baseline**: Prevents catastrophic credential leaks.
*   **Operational Agility**: Allows changing environment behavior without rebuilding application binaries.
*   **Safe Releases**: Enables dark launching and decoupling deployment from release via feature flags.

## Consequences
*   Pre-commit hooks will scan for committed secrets and block push events if any are detected.
*   Application code must use dependency injection or environment utilities to load configs.
