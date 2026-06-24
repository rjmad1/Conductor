# Agent Security Rules — Conductor Platform

This specification defines the security constraints, restricted directory boundaries, mandatory test requirements, and approval gates that all future autonomous implementation agents must adhere to when executing tasks within this repository.

---

## 1. Restricted Directory Boundaries

Autonomous agents are strictly prohibited from modifying, deleting, or bypassing configurations in the following directory paths without explicit, written approval from the human supervisor:

*   **GitHub Pipelines:** `.github/workflows/` (CI/CD definitions).
*   **Security Certs:** `infrastructure/certs/` / `security/certs/` (Keys, TLS profiles).
*   **Access Control:** `config/keycloak/` (Keycloak realms definitions, OIDC scopes).
*   **Egress Infrastructure:** `infrastructure/squid/` (Squid proxy whitelist maps).
*   **Database Triggers:** `src/main/resources/db/migration/` (DDL migration scripts altering trigger properties or the `audit_logs` table schema).
*   **Governance Policies:** Root-level markdown governance documents (`AGENTS.md`, `ARCHITECTURE_GUARDRAILS.md`, `DATA_GOVERNANCE.md`, `SECURITY_GUARDRAILS.md`, and all `*_STANDARD.md` files).

---

## 2. Required Security Reviews

Implementation agents must request human review and code validation when proposing changes that touch the following code blocks:

1.  **Security Configurations:** Any alteration to Java classes extending `WebSecurityConfigurerAdapter` or utilizing `@EnableWebSecurity`.
2.  **JPA Tenancy Interceptors:** Any changes in classes implementing tenant context interception or Hibernate `@FilterDef` bindings.
3.  **New Third-Party Dependencies:** Adding entries to `pom.xml` or `package.json` (requires supply chain risk qualification against `SUPPLY_CHAIN_SECURITY.md`).
4.  **SSRF Proxy Settings:** Modifications to HTTP Client initializations (RestTemplates, WebClients) that bypass the Squid egress proxy.

---

## 3. Mandatory Security Verification Tests

Agents implementing new backend features (endpoints, message handlers, or database mappers) must generate the following test templates:

### 3.1 Tenancy Isolation Tests
*   Every new controller endpoint must contain a unit test confirming that request executions carrying `Tenant-A` access tokens fail to retrieve, alter, or inject data belonging to `Tenant-B`.
*   Every repository entity mapping must contain an integration test asserting that query runs fail to bypass the automatic `tenant_id` context filter.

### 3.2 Input Sanitization Tests
*   Endpoints receiving user inputs (custom text fields, contact properties mappings, prompt variables) must contain tests validating that input payloads containing HTML strings, SQL commands, or javascript scripts are successfully blocked, escaped, or sanitized.

---

## 4. Pipeline Approval Gates

To promote agent code to production repositories, the following verification gates must pass:

```
[Agent Code Commit]
         │
         ▼
[Gate 1: Pre-Commit Security Checks] ──► (GitLeaks + ArchUnit boundary tests)
         │
         ▼
[Gate 2: Integration Verification]   ──► (Trivy Container Scan + Dependency Alert check)
         │
         ▼
[Gate 3: Isolation Validation Suite] ──► (Automated cross-tenant injection simulation)
         │
         ▼
[Gate 4: Human Review & Sign-Off]    ──► (Security Lead approval check)
```

1.  **Gate 1 (Pre-Commit):** Continuous scanning for plaintext secrets using GitLeaks. Any warning halts commit.
2.  **Gate 2 (Integration):** Trivy container scanning must return zero Critical vulnerabilities.
3.  **Gate 3 (Isolation):** Custom automated tenant isolation tests must compile and pass without assertion failures.
4.  **Gate 4 (Human Sign-Off):** Merging agent-generated code requires approval from at least one human developer and the Security Lead.

---

## 5. Security Anomaly Escalation Protocol

If an agent discovers a security vulnerability in the existing codebase (e.g., an unauthenticated endpoint, bypassable tenant filter, or exposed API credentials):
1.  **Halt Action:** Stop making edits to the affected files.
2.  **Escalate:** Immediately notify the human supervisor by detailing the vulnerability description, location (file basename and line numbers), severity, and proposed mitigation steps.
3.  **Contain:** Do not attempt to fix or test the vulnerability in a public branch. Keep validation code in isolated local debug streams.

This agent standard is active.
