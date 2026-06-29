# Contributing to Conductor Platform

Thank you for contributing to the Conductor Platform! As a regulated enterprise platform (AI-EOS Level 4), we enforce strict SDLC governance, code quality controls, and architectural review gates. 

---

## 1. Specification-Driven Development (Spec-Kit Alignment)

Before writing any source code, you must ensure that matching specifications are established:
*   **Requirements First**: No implementation is accepted without a corresponding Business Requirement Specification (BRS) or Feature Specification.
*   **Traceability Mapping**: Every pull request must explicitly link code changes back to the business objective, requirement, and specification:
    $$\text{Objective} \rightarrow \text{Requirement} \rightarrow \text{Specification} \rightarrow \text{ADR} \rightarrow \text{Implementation}$$
*   **Contract-First Thinking**: API and Event schemas must be declared in `/docs/api/` and approved before starting service implementations.

---

## 2. Git & Branching Strategy

### Branch Naming Conventions
*   **Protected Production Branch**: `main` (No direct pushes allowed, except for automated release scripts).
*   **Feature Branches**: `feature/<ticket-id>-<description>`
*   **AI Agent Branches**: `agent/<agent-id>-<description>`
*   **Bug Fix Branches**: `bugfix/<ticket-id>-<description>`

### Commit Guidelines
*   **Signed Commits**: All commits MUST be cryptographically signed (GPG/SSH). Unsigned commits will be rejected by the GitHub pre-receive hooks.
*   **Commit Messages**: Keep commit messages concise, starting with a conventional type (e.g., `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`).

---

## 3. Pull Request Process

1.  **Use the PR Template**: Ensure your Pull Request description uses the template guidelines defined in the [Engineering Wisdom Operating System](file:///c:/Users/rajaj/Projects/Conductor/docs/standards/Engineering-Wisdom-OS.md).
2.  **Pass CI Gates**:
    *   Code must compile successfully with zero warnings.
    *   Unit and integration tests must pass. **Note:** Docker or Docker Desktop must be running locally for Testcontainers-based Acceptance tests to execute successfully.
    *   Code coverage must satisfy the 80% minimum service-layer requirement.
    *   Linter and formatting checks (`.editorconfig`) must pass.
    *   Static analysis (SonarQube) must have zero CRITICAL or BLOCKER issues.
3.  **Obtain Approvals**:
    *   **Tier 0 (Informational)**: Auto-merge allowed after passing CI.
    *   **Tier 1-2 (Low-Moderate)**: 1 reviewer approval from the component owners defined in `CODEOWNERS`.
    *   **Tier 3-4 (High-Critical)**: 2+ approvals including the Engineering Decision Review Board (EDRB) gate.

---

## 4. Coding & Security Standards

*   **JPA Entities**: Every database entity must include a `tenantId` field and scope queries accordingly.
*   **Logging**: Use SLF4J structured logging. Never log Personal Identifiable Information (PII) like phone numbers or message payloads.
*   **Secrets**: Never commit credentials, tokens, or API keys. Use environment variables or Secrets Manager.
*   **Error Handling**: Throw typed domain exceptions in the service layer, and translate them to uniform API responses in the controller layer.

For detailed guidelines, refer to the [Coding Standards](file:///c:/Users/rajaj/Projects/Conductor/docs/standards/Coding-Standards.md) and [Security Architecture](file:///c:/Users/rajaj/Projects/Conductor/docs/architecture/Security-Architecture.md).
