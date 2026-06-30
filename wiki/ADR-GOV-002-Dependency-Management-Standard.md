# ADR-GOV-002: Dependency Management Standard

## Status
ACCEPTED

## Context
Regulated SaaS environments require strict controls over open-source and third-party dependencies. Undocumented dependencies introduce compliance risks (e.g., GPL licenses), security vulnerabilities, and build instability.

## Decision
We enforce the following dependency governance rules:
1.  **Pinning**: All library versions in Maven (`pom.xml`) and npm (`package.json`) must be pinned to exact versions. No dynamic ranges (`*`, `^`, `~`) are permitted in production.
2.  **Licensing**: Only permissive open-source licenses (MIT, Apache 2.0, BSD) are allowed. Copyleft licenses (GPL, AGPL) are prohibited unless explicitly approved by the EDRB.
3.  **Scanning**: Software Composition Analysis (SCA) scans must run on every commit. High or critical vulnerabilities are build-blocking.
4.  **Updates**: Dependabot will submit monthly automated update PRs.

## Rationale
*   **Compliance**: Meets SOC2 and ISO 27001 requirements for supply chain security.
*   **Reproducibility**: Guarantees consistent builds across development, staging, and production.
*   **Risk Mitigation**: Minimizes zero-day vulnerability exposure.

## Consequences
*   Adding any new dependency requires an SCA scan check.
*   Pull requests introducing unapproved licenses will fail CI build checks.
