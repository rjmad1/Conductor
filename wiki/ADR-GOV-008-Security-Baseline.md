# ADR-GOV-008: Security Baseline

## Status
ACCEPTED

## Context
Conductor handles sensitive business and customer information, requiring compliance with DPDP India, GDPR, and HIPAA. A robust security baseline must be enforced from day one.

## Decision
We enforce the following security controls:
1.  **Identity & Access**: Authentication must go through Keycloak. Tenant data must be isolated using tenant-specific Keycloak realms. Client requests must include valid OIDC JWTs.
2.  **Encryption**:
    *   **In Transit**: TLS 1.3 must be forced for all public endpoints.
    *   **At Rest**: Storage volumes (PostgreSQL, Redis, NATS disks) must be encrypted using AES-256 keys.
3.  **Input Sanity**: Parameterized SQL queries must be used exclusively to prevent injection. Outbound integrations must validate URLs against an allowlist to prevent Server-Side Request Forgery (SSRF).
4.  **Static Analysis**: SAST (Static Application Security Testing) and dependency vulnerability checks run on every commit.

## Rationale
*   **Regulatory Compliance**: Required for SOC2, HIPAA, and DPDP validation.
*   **Leakage Prevention**: Strong multi-tenant boundary checks protect tenant confidentiality.

## Consequences
*   Services must validate JWT signatures and extract `tenant_id` for all requests.
*   Outbound API clients (e.g. integrations hub) must route through an outbound proxy that enforces allowlists.
