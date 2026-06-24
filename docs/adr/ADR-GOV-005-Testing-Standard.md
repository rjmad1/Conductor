# ADR-GOV-005: Testing Standard

## Status
ACCEPTED

## Context
Regulated environments require auditable evidence of software quality and compliance verification. Unreliable, missing, or slow tests block the delivery pipeline and hide software defects.

## Decision
We enforce a structured test suite layout and minimum quality gates:
1.  **Test Types**:
    *   **Unit Tests**: Test single classes in isolation, mock all external dependencies.
    *   **Integration Tests**: Test repository/data layers and integrations against real Docker databases (using Testcontainers).
    *   **Contract Tests**: Verify API provider and consumer matches (using Pact).
    *   **E2E Tests**: Walk through critical happy paths using Playwright.
2.  **Quality Gates**:
    *   **Code Coverage**: Enforce a minimum of 80% line coverage in the business/service layer.
    *   **Traceability**: Unit and integration tests verify compliance control points (e.g., DPDP consent capture, deletion traces).

## Rationale
*   **Trustworthy SDLC**: Assures that changes do not introduce functional regressions.
*   **Regulatory Compliance**: High coverage and mapped tests provide the necessary compliance evidence.

## Consequences
*   Pull requests that lower the overall coverage below 80% or fail unit tests are blocked from merging.
*   Test suites must run in the CI/CD pipeline on every push.
