# Security Execution Plan — Conductor

This document details the security implementation backlog required for the Conductor MVP. All items are classified by implementation order, priority, and risk profile.

---

## 1. Security Implementation Backlog

### Tenant Isolation

*   **Task:** Gateway Header Injection & Hibernate Query Filter Binding.
*   **Priority:** **Critical**
*   **Implementation Wave:** Wave 1 (Foundation)
*   **Target:** Create Spring Monolith context filters to extract `X-Tenant-ID` headers from Kong API Gateway and bind them to the Hibernate session execution context automatically.
*   **Risk:** Data Leakage. Developers bypassing filters via native SQL.
*   **Mitigation:** Configure strict ArchUnit tests to fail builds if native JDBC or SQL calls bypass the Hibernate session boundary.

---

### Authentication

*   **Task:** Keycloak Realm Setup & Spring Boot Security Configuration.
*   **Priority:** **Critical**
*   **Implementation Wave:** Wave 1 (Foundation)
*   **Target:** Setup Realm configurations in Keycloak. Integrate Spring Boot OIDC security filters validating incoming RS256 JWT signatures.
*   **Risk:** Keycloak configuration errors.
*   **Mitigation:** Store all realm and client configurations as JSON templates in the repository, deploying them via automated configuration jobs in CI/CD.

*   **Task:** Bcrypt API Key Implementation.
*   **Priority:** **High**
*   **Implementation Wave:** Wave 2 (Messaging)
*   **Target:** Build the API Key creation service. Implement 1-way Bcrypt hashing on credentials. Only expose the key to the creator once.
*   **Risk:** CPU saturation from verifying bcrypt hashes under high API load.
*   **Mitigation:** Store a plaintext prefix (`ck_XXXXXX`) and perform a preliminary lookup on it before verifying the bcrypt hash.

---

### Authorization

*   **Task:** RBAC Claim Mapping & Controller Method Restrictions.
*   **Priority:** **High**
*   **Implementation Wave:** Wave 2 (Messaging)
*   **Target:** Configure Spring Security `@PreAuthorize` annotation hooks on API routes mapping to JWT claims (`OWNER`, `ADMIN`, `MANAGER`, `AGENT`).
*   **Risk:** Incorrect claim mapping.
*   **Mitigation:** Develop unit tests validating that each controller route rejects requests lacking the appropriate role claim.

---

### Secrets Management

*   **Task:** AWS Parameter Store Config Setup & Gitleaks Scan.
*   **Priority:** **Critical**
*   **Implementation Wave:** Wave 1 (Foundation)
*   **Target:** Configure Spring Cloud AWS Parameter Store integrations to inject database passwords and Meta tokens. Setup Gitleaks pre-commit hooks to scan code.
*   **Risk:** Leakage of secrets in git history.
*   **Mitigation:** Configure a GitHub Actions workflow that executes a Gitleaks scan on every commit and block PR merges on failure.

---

### Encryption

*   **Task:** RDS Disk Encryption & AWS KMS Integration.
*   **Priority:** **High**
*   **Implementation Wave:** Wave 1 (Foundation)
*   **Target:** Enable storage encryption on the RDS PostgreSQL instance using an AWS KMS Customer Managed Key (CMK). Configure TLS 1.3 requirements for all internal connections.
*   **Risk:** High CPU usage from TLS handshake limits.
*   **Mitigation:** Utilize persistent connection pools (HikariCP) to reduce connection handshake overhead.

---

### Audit

*   **Task:** Write-Once PostgreSQL Audit Triggers.
*   **Priority:** **High**
*   **Implementation Wave:** Wave 1 (Foundation)
*   **Target:** Add triggers on `users`, `workflows`, and `api_keys` redirecting changes to the partitioned `audit_logs` table. Apply rules rejecting `UPDATE` or `DELETE` on `audit_logs`.
*   **Risk:** Write performance degradation.
*   **Mitigation:** Avoid placing triggers on high-frequency tables like `messages` or `workflow_executions`.

---

### Compliance

*   **Task:** DPDP Automated Erasure Jobs.
*   **Priority:** **Critical**
*   **Implementation Wave:** Wave 3 (Workflows)
*   **Target:** Implement a scheduled Spring Boot worker that checks for erasure requests in the queue and hashes PII columns (`name`, `email`, `phone`) within the 30-day compliance SLA window.
*   **Risk:** Integrity issues if dependent keys are orphaned.
*   **Mitigation:** Retain non-PII transaction references (such as invoice balances or subscription totals) to support financial audits.

---

## 2. Security Execution Matrix Summary

| Backlog Item | Priority | Wave | Primary Risk | Mitigation |
| :--- | :--- | :--- | :--- | :--- |
| **Tenant Isolation Filter** | Critical | Wave 1 | Cross-tenant data leakage. | ArchUnit tests blocking raw native query formats in backend code. |
| **AWS Secret Injection** | Critical | Wave 1 | Leakage of access credentials in git. | Gitleaks scanning in pre-commit hooks and GitHub CI pipeline checks. |
| **Keycloak OIDC Integration**| Critical | Wave 1 | Configuration errors bypass security. | Version-controlled realm configurations deployed automatically. |
| **PostgreSQL Write Audit Triggers** | High | Wave 1 | Audit ledger modification or deletion.| Database trigger that rejects UPDATE and DELETE queries. |
| **Bcrypt API Keys** | High | Wave 2 | High CPU usage from Bcrypt evaluations. | Use prefix-based lookups before checking Bcrypt hashes. |
| **RBAC Route Annotations** | High | Wave 2 | Privilege escalation bugs. | Integration tests simulating unauthorized role requests. |
| **DPDP Customer Anonymization**| Critical | Wave 3 | Incomplete data removal (SLA violation). | Automated batch job replacing PII with salted hashes. |
