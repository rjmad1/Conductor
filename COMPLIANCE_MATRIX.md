# Compliance Matrix — Conductor Platform

This document maps the regulatory requirements of the **Digital Personal Data Protection (DPDP) Act India 2023**, **GDPR**, **Meta WhatsApp Business Policy**, **SOC 2 Type II**, **ISO 27001**, **OWASP ASVS**, and **OWASP Top 10** to the structural controls of the Conductor Platform.

---

## 1. Regulatory & Compliance Control Matrix

| Regulation / Standard | Section / Domain Requirement | Conductor Architectural Control | Responsible Domain | Verification & Testing |
| :--- | :--- | :--- | :--- | :--- |
| **DPDP India 2023** | Section 6: Data Residency (India Boundary) | All PostgreSQL, Redis, ClickHouse database servers and backup stores are restricted to AWS Mumbai region (`ap-south-1`). | Deployment / Infra | AWS IAM/VPC configuration audit. |
| **DPDP India 2023** | Section 12: Right to Erasure (SLA: 30 days) | Automated async hard deletion script triggers SQL cascading purges and Qdrant vector deletion. | Customer | Daily automated verification scans in Qdrant & Postgres. |
| **DPDP India 2023** | Section 6: Immutable Consent Tracker | Append-only database table (`consent_records`) tracking opt-in date, IP, and channel source. | Customer | Trigger-based audit verification check. |
| **GDPR** | Article 17: Right to Be Forgotten | Matches DPDP erasure controls; complete PII purging within the 30-day window. | Customer | Cascadable delete integration tests. |
| **GDPR** | Article 32: Cryptographic Security | TLS 1.3 in transit; AES-256 GCM envelope encryption for dynamic tenant credentials. | Security | Nightly automated SSL configuration tests. |
| **Meta WhatsApp Policy**| Official API Enforcement | Prohibits unofficial scraping tools. Enforces Meta Cloud API endpoints through the monolith wrapper. | Messaging | Code review gates; compile-time check blocking dependencies like Baileys/OpenWA. |
| **Meta WhatsApp Policy**| User Opt-Out Handling | Processes "STOP" opt-out responses within 5 seconds via JetStream event pipeline. | Messaging / Customer | Integration mock response test validating rapid consent updates. |
| **SOC 2 Type II** | CC6.1: Logical Access Controls | Authenticates users via Keycloak realm-level OIDC; verifies JWT signature at Kong API Gateway. | Identity | Nightly penetration testing on authentication endpoints. |
| **SOC 2 Type II** | CC6.3: Transmission Integrity | TLS 1.3 mandatory; restricts insecure legacy cipher suites. | Security / Infra | Automated daily test using `sslyze`. |
| **SOC 2 Type II** | CC6.8: System Alteration Auditing | Database-level triggers logging inserts/updates/deletes onto immutable partition table `audit_logs`. | Audit | Automated database injection checks testing trigger constraints. |
| **ISO 27001:2022** | Annex A.8.12: Data Leakage Prevention | Squid Egress Proxy blocks outbound direct connections, routing through static whitelist. | Integrations | Automated integration tests trying to connect to local IP subnets. |
| **ISO 27001:2022** | Annex A.8.20: Network Security | Separates internal components (Databases, NATS, Temporal) on isolated docker network namespaces. | Deployment / Infra | Docker Network configuration scans. |
| **OWASP ASVS v4.0.3** | V2: Authentication Verification | Implements Keycloak brute-force lockouts, session timeouts, and password length checks. | Identity | Automation scripts evaluating lockout behaviors. |
| **OWASP ASVS v4.0.3** | V4: Access Control Verification | Hibernate filters automatically inject `tenant_id = :tenantId` parameter to JPA entities. | Multi-Tenancy | Integration test trying to query cross-tenant datasets. |

---

## 2. OWASP Top 10 Mitigations Mappings

We map the primary engineering mitigations for the **OWASP Top 10 (2021)**:

*   **A01:2021-Broken Access Control:**
    *   *Mitigation:* Hibernate query filters append `tenant_id` context to prevent cross-tenant exposure. Spring Security RBAC annotations enforce method-level scopes checks.
*   **A02:2021-Cryptographic Failures:**
    *   *Mitigation:* AES-256-GCM envelope encryption blocks cleartext storage of secrets. Log wrappers mask PII fields (phone numbers, emails) before Loki forwarding.
*   **A03:2021-Injection (SQL / NoSQL / Command):**
    *   *Mitigation:* Standardizes on Hibernate JPA Parameterized Queries (native SQL is banned). Sanitizes inputs. Vector filters inside Qdrant enforce exact metadata matches.
*   **A04:2021-Insecure Design:**
    *   *Mitigation:* Architectural design rules lock domain isolation and secure egress through Squid. High-priority threat models govern design.
*   **A05:2021-Security Misconfiguration:**
    *   *Mitigation:* Production credentials are not committed to Git. Helm configs and ECS templates pull variables dynamically from AWS Secrets Manager.
*   **A06:2021-Vulnerable and Outdated Components:**
    *   *Mitigation:* CI/CD pipelines run OWASP Dependency-Check scans on code, and container registry checks scan base image dependencies daily.
*   **A07:2021-Identification and Authentication Failures:**
    *   *Mitigation:* Standardizes on Keycloak OIDC with Refresh Token Rotation (RTR). Basic authentication is disabled on REST paths (API keys use signature lookups).
*   **A08:2021-Software and Data Integrity Failures:**
    *   *Mitigation:* Commits must be GPG signed. Container images are signed via Cosign. Updates enforce validation rules on inputs.
*   **A09:2021-Security Logging and Monitoring Failures:**
    *   *Mitigation:* DB Triggers record mutable logs. Logs route to Grafana Loki. Security alerts trigger Slack/PagerDuty events on thresholds (e.g. 5x access failures).
*   **A10:2021-Server-Side Request Forgery (SSRF):**
    *   *Mitigation:* Bypassing the Squid Egress Proxy is blocked. Egress routing maps only to an approved target domain directory.

This compliance matrix governs the platform's verification mechanisms.
