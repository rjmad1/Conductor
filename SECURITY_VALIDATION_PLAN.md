# Security Validation Plan — Conductor Platform

This document outlines the automated and manual security validation strategy for the Conductor Platform. It details the scanning pipeline, tools selection, quality gates, and automated tenant isolation validation protocols.

---

## 1. CI/CD Security Pipeline

Security scans are integrated directly into the GitHub Actions CI/CD workflows, executing at various lifecycle stages:

```
[Developer Commit] ──► [Pull Request] ──► [Merge to Main] ──► [Nightly Staging]
   Pre-Commit Gates        SAST Scan         SBOM Generate       DAST Execution
   GitLeaks Scanning       IaC Scanning      Container Scan      Penetration Tests
```

---

## 2. Automated Security Scans Configuration

We define the tools, execution frequency, and failure criteria for each verification channel:

### 2.1 Static Application Security Testing (SAST)
*   **Tool:** **Semgrep** (standard security ruleset) / **SonarQube Developer Edition**.
*   **Trigger:** Executed on every pull request targeting `main` or `release/*` branches.
*   **Quality Gate:** Fail build if any `error` level SAST rules violate security guidelines (e.g., hardcoded crypt keys, dynamic SQL query strings).

### 2.2 Dynamic Application Security Testing (DAST)
*   **Tool:** **OWASP ZAP** (Baseline scan on staging URL) / **Nikto** for configuration checks.
*   **Trigger:** Executed nightly on the active staging environment.
*   **Quality Gate:** Fail build on any unresolved `High` or `Critical` severity vulnerabilities. Generate ZAP report artifacts automatically.

### 2.3 Container Security Scanning
*   **Tool:** **Trivy** (Aqua Security) / **Grype**.
*   **Trigger:** Executed during the Docker build stage before images are pushed to registries.
*   **Quality Gate:** Block registry push if base layers contain unresolved vulnerabilities with a CVSS score $\ge 9.0$ (Critical).

### 2.4 Dependency Vulnerability Scanning
*   **Tool:** **OWASP Dependency-Check** / **Snyk**.
*   **Trigger:** Executed weekly and on any modifications to `pom.xml` or `package.json`.
*   **Quality Gate:** Fail build if direct dependencies contain vulnerabilities lacking patches that match high severity CVSS limits.

### 2.5 Secret Scanning
*   **Tool:** **GitLeaks** / **TruffleHog**.
*   **Trigger:** 
    *   *Local:* Pre-commit hook blocking commits if plaintext keys or private certificates are detected.
    *   *CI:* Repository history scan on PR validation gates.
*   **Quality Gate:** Fail build instantly on any hit.

### 2.6 Infrastructure as Code (IaC) Scanning
*   **Tool:** **tfsec** (Terraform) / **Checkov** (Dockerfiles, Compose, and Helm manifests).
*   **Trigger:** Executed on changes to files in `infrastructure/` or `docker-compose.local.yml`.
*   **Quality Gate:** Fail PR if security rules (e.g., ports exposed globally, running containers as root) are violated.

### 2.7 Software Bill of Materials (SBOM) Generation
*   **Tool:** **Syft** / **Trivy sbom**.
*   **Trigger:** Executed on release tags build.
*   **Action:** Produce CycloneDX JSON SBOM artifacts and sign using **Cosign**, uploading files directly to the OCI container registry.

---

## 3. Automated Tenant Isolation Testing

To guarantee the Logical Row-Level Security (RLS) database filters and Qdrant vector isolation rules cannot be bypassed, the platform executes a dedicated **Tenant Isolation Test Suite** in the integration pipeline:

### 3.1 Test Protocol
1.  **Seed Data:** The pipeline inserts mock data for two distinct tenants: `tenant-alpha-uuid` and `tenant-beta-uuid`.
2.  **Generate Auth Tokens:** Test framework requests standard OIDC JWT tokens for both tenants from Keycloak mock servers.
3.  **Cross-Tenant Verification Scans:**
    *   **Test Case A (Header Forgery):** Test calls `/api/v1/workflows` presenting `Tenant-Alpha`'s JWT access token but passing the header `X-Tenant-ID: tenant-beta-uuid`.
        *   *Assert:* The Monolith filter or API Gateway detects the realm identifier mismatch and aborts with a `403 Forbidden` response.
    *   **Test Case B (Data Injection attempt):** Test calls `/api/v1/customers` presenting `Tenant-Alpha`'s JWT token, trying to insert a customer payload containing `"tenantId": "tenant-beta-uuid"`.
        *   *Assert:* The Monolith JPA lifecycle interceptor intercepts the insert request and overrides the tenant value with `tenant-alpha-uuid` before SQL commit.
    *   **Test Case C (Qdrant Leak Attempt):** Test calls `/api/v1/ai/chat` presenting `Tenant-Alpha`'s JWT token and submits a semantic search vector matching content in `Tenant-Beta`'s workspace.
        *   *Assert:* Qdrant results array returns empty or filters out Tenant-Beta's records.

---

## 4. Manual Verification & Penetration Testing

*   **Frequency:** Done bi-annually (every 6 months) by an independent CREST-accredited security auditing firm.
*   **Target Scope:** External gateway network configurations, Keycloak realm settings, privilege escalation verification, and SSRF proxy validation.
*   **Rule:** Vulnerabilities identified during penetration testing must be logged in the MVP backlog and remediated based on severity SLAs (P0: 48h, P1: 7d, P2: 30d).

This validation plan is active.
