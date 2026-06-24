# ADR-GOV-009: API Design Standard

## Status
ACCEPTED

## Context
A clean, consistent API surface is critical for frontend development, partner integrations, and developer platform adoption. Inconsistencies in naming, path parameters, and error responses slow down integrations.

## Decision
We enforce the following API design guidelines:
1.  **Conventions**: REST API endpoints must use plural nouns and kebab-case paths (e.g., `/api/v1/workflow-executions`).
2.  **Versioning**: All public APIs must include a version prefix (e.g., `/api/v1/`).
3.  **Error Payloads**: Error responses must follow a unified structure:
    ```json
    {
      "error_code": "RESOURCE_NOT_FOUND",
      "message": "Detailed developer message",
      "timestamp": "2026-06-24T12:00:00Z",
      "request_id": "req-uuid"
    }
    ```
4.  **OpenAPI Specs**: Every service must document its REST endpoints using OpenAPI 3.0 specifications stored in `/docs/api/`.

## Rationale
*   **Developer Experience**: Simplifies API client generation and frontend integration.
*   **Consistency**: A single standard prevents ad-hoc formatting choices across different service teams.

## Consequences
*   Code review check: PRs that introduce new endpoints must include corresponding OpenAPI contract modifications.
