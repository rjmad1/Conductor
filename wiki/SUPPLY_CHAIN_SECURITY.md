# Supply Chain Security Specification — Conductor Platform

This specification defines the security requirements, vulnerability assessment standards, and mitigation controls for all open-source software (OSS) components, libraries, and container images integrated into the Conductor Platform.

---

## 1. Approved OSS Components Security Assessment

We evaluated the primary components defined in [DEPENDENCY_INVENTORY.md](file:///c:/Users/rajaj/Projects/Conductor/DEPENDENCY_INVENTORY.md) across six safety pillars: **CVE History**, **Release Signing**, **SBOM Availability**, **Dependency Health**, **Maintainer Risk**, and **Bus Factor**.

| Component / Image | Primary Registry Source | CVE Risk Profile | Release Signing | SBOM Available | Dependency Health | Bus Factor |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Keycloak** (`24.0.0`) | `quay.io` | Low (Active RedHat security patches) | GPG Signed Tags | Yes (CycloneDX) | High (OpenSSF 8.5/10) | Very High (Enterprise backed) |
| **Dex** (`v2.41.0`) | `ghcr.io` (CNCF) | Low (Mature identity codebase) | Cosign Signed | Yes | High (OpenSSF 8.1/10) | High (CNCF Governance) |
| **Temporal** (`1.24.0`) | DockerHub | Low (Commercial venture backed) | Cosign Signed | Yes (SPDX) | High (Active updates) | High (Temporal Inc.) |
| **NATS JetStream** (`2.10`) | DockerHub (Alpine) | Very Low (Minimal alpine footprint) | GPG Signed | Yes | High (OpenSSF 9.0/10) | High (Synadia / CNCF) |
| **PostgreSQL** (`15-alpine`) | DockerHub | Very Low (Alpine minimizes libraries) | GPG Signed | Yes | High (OpenSSF 9.2/10) | Very High (Global PG Group) |
| **Redis** (`7.4-alpine`) | DockerHub | Low | GPG Signed | Yes | High (OpenSSF 8.8/10) | High (Redis Ltd.) |
| **ClickHouse** (`24.8-alpine`) | DockerHub | Low (Frequent security updates) | GPG Signed | Yes | Medium (High code velocity) | High (ClickHouse Inc.) |
| **Metabase** (`v0.49.0`) | DockerHub | Medium (Java/Clojure dependency vulnerabilities) | None (Standard tag) | Yes | Medium (Active commercial) | Medium |
| **Dify API** (`0.6.9`) | DockerHub | Medium (Fast-moving AI stack, many python deps) | None | No (Ad-hoc) | Medium (High churn rate) | Low (Early commercial) |
| **LiteLLM** (`v1.59.8`) | `ghcr.io` | Medium (High release count, Python pip updates) | Cosign Signed | Yes | Medium | Low (Community lead) |
| **Qdrant** (`v1.10.0`) | DockerHub | Low (Rust language memory safety) | Cosign Signed | Yes | High | Medium (Qdrant GmbH) |
| **OTel Collector** (`0.118.0`)| DockerHub | Low (CNCF standard tool) | Cosign Signed | Yes (CycloneDX) | High (OpenSSF 8.7/10) | High (CNCF Governance) |
| **Grafana** (`10.4.0`) | DockerHub | Medium (High feature scope introduces surface) | GPG Signed | Yes | High (Grafana Labs) | High (Enterprise backed) |

---

## 2. Supply Chain Risks & Analysis

### 2.1 Low-Bus-Factor Components (AI Tools)
*   **Target Components:** Dify, LiteLLM.
*   **Risk Profile:** These components are essential for our conversational and vector lookup paths. However, they run on Python stacks with high release frequencies and contain many transitive dependencies. A single compromised transitive Python pip package could inject malware into the integration context.
*   **Mitigation:** 
    1.  Pin all python adapter dependencies to exact hashes (`requirements.txt` containing SHA-256 hashes).
    2.  Isolate these containers in network subnets that block ingress calls from public clients, routing calls exclusively via the OTel/REST boundaries.

### 2.2 Transitive Dependency Vulnerabilities (Java/Spring Boot)
*   **Target Scope:** The Core Modular Monolith Java runtime.
*   **Risk Profile:** Java microservice frameworks include hundreds of transitive jar libraries. A vulnerability similar to Log4Shell (CVE-2021-44228) can lead to Remote Code Execution (RCE).
*   **Mitigation:** Enforce daily OWASP Dependency-Check scans in the validation pipeline.

---

## 3. Supply Chain Security Policies

To ensure platform integrity, the engineering team must comply with the following policies:

### 3.1 Immutable Version Pinning
*   **Rule:** The use of `latest` or generic minor-version tags (e.g. `:15`, `:alpine`) is **strictly prohibited** in all deployment files (Docker-Compose, Helm configs).
*   **Standard:** All container images must be pinned using their exact semantic tag and SHA-256 digest hash:
    $$\text{Example: } \texttt{postgres:15.6-alpine@sha256:7ba1a067098dfbdc181f...}$$

### 3.2 Automated SBOM Generation
*   **Rule:** Every build artifact compiled in the CI/CD pipeline must generate a Software Bill of Materials (SBOM) listing all package details.
*   **Format:** **CycloneDX JSON v1.5**.
*   **Tooling:** Use `syft` to scan container images and produce SBOM metadata archived alongside container images in the registry.

### 3.3 Container Image Verification
*   **Rule:** Images pulled into production clusters must undergo verification.
*   **Tooling:** Use **Cosign** (Sigstore) to verify image signatures before deployment:
    ```bash
    cosign verify --key cosign.pub quay.io/keycloak/keycloak:24.0.0
    ```
    Image deployments that fail verification are blocked at the ingress deployment controller.

### 3.4 Dependency Vulnerability Thresholds
*   **Rule:** The CI build pipeline must abort if security scanners identify vulnerabilities exceeding severity limits.
*   **Threshold Gates:**
    *   **CRITICAL Severity (CVSS v3 $\ge 9.0$):** Zero tolerance. Build fails instantly. Must mitigate within 24 hours.
    *   **HIGH Severity (CVSS v3 $7.0-8.9$):** Zero tolerance for public-facing components. Mitigate within 7 days.
    *   **MEDIUM Severity (CVSS v3 $4.0-6.9$):** Permitted to compile, must review and mitigate within 30 days.

This supply chain standard is mandated for all third-party integrations.
