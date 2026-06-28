# Security Guardrails — Conductor Platform

This specification defines the mandatory security guardrails and engineering constraints for the Conductor Platform. These rules are enforced at compile time, pre-commit, and deploy-time gates. Bypassing any guardrail requires formal Security Board approval.

---

## 1. Mandatory Engineering Guardrails

### SG-001: No Plaintext Secrets
*   **Rule:** Plaintext tokens, passwords, private keys, or API credentials must never be committed to repository files.
*   **Enforcement:** Pre-commit GitLeaks hook blocks the commit if credential patterns are matched. CI/CD pulls secrets dynamically at runtime from AWS Secrets Manager.

### SG-002: No Hardcoded Database Credentials
*   **Rule:** Application connection parameters (passwords, usernames, hostnames) must not exist as literal values in `application.yml` or code files.
*   **Enforcement:** Config files retrieve variables via Spring environment properties placeholders (e.g. `${DB_PASSWORD}`) injected at runtime.

### SG-003: All APIs Authenticated by Default
*   **Rule:** Every exposed REST/gRPC endpoint must enforce identity validation. The only exceptions are public `/healthz`, `/metrics`, and Meta Webhook receivers, which must implement rate-limiting and domain-specific filters.
*   **Enforcement:** Ingress controller blocks unauthenticated paths. Spring Security filters require authentication scopes check on all controllers.

### SG-004: All Events Tenant-Scoped
*   **Rule:** Asynchronous events published to the NATS JetStream bus must match the canonical namespace format and must contain the active tenant identifier.
*   **Enforcement:** Event publisher wrapper validates subjects against `conductor.{tenantId}.domain.entity.action` before sending.

### SG-005: Mandatory Audit Trails
*   **Rule:** Any API request or process that alters database states (customer records, consent flags, campaign steps, OAuth parameters) must trigger a trigger-level audit log record.
*   **Enforcement:** Database schema applies triggers to target tables, preventing DDL operations from modifying active data without audit log execution.

### SG-006: PII Encrypted at Rest
*   **Rule:** Customer names, email strings, phone numbers, and chat message bodies (classified as PII and Sensitive PII) must be encrypted before database write.
*   **Enforcement:** JPA models apply Hibernate `@Convert` annotations mapping attributes to AES-256-GCM encryption converters utilizing KMS keys.

### SG-007: Service-to-Service Authentication Mandatory
*   **Rule:** Internal communications must authenticate both ends. Anonymous microservices connections are blocked.
*   **Enforcement:** mTLS (mutual TLS 1.3) is enforced for gRPC. Webhook adapters invoke endpoints using tokens with scoped service client roles.

### SG-008: Mandatory Squid Proxy for Egress
*   **Rule:** Outbound HTTP client integrations must route calls through the Squid forward proxy. Direct WAN egress is blocked.
*   **Enforcement:** Network configuration blocks direct egress routing from the Spring Boot container. Restricts outgoing connections to the Squid proxy IP address.

### SG-009: Strict Input Sanitization
*   **Rule:** Input parameters from requests, APIs, or metadata properties must be sanitized to block injection vectors.
*   **Enforcement:** Spring Boot request filters sanitize input structures. JPA repositories enforce parameterized queries.

### SG-010: Banned Cross-Module Database Joins
*   **Rule:** Services are prohibited from joining database tables across domain boundaries. Cross-domain queries must go through API requests or NATS event subscriptions.
*   **Enforcement:** ArchUnit rules fail build if repositories violate package boundaries (e.g. `com.conductor.workflow` importing `com.conductor.customer.model`).

---

## 2. Enforcement Matrix

| Guardrail ID | Type | Enforcement Mechanism | Failure Action |
| :--- | :--- | :--- | :--- |
| **SG-001** | Secrets | GitLeaks Pre-Commit Hook / CI scan | Block Commit / Fail Build |
| **SG-002** | Configs | CI check on `application.yml` defaults | Fail Build |
| **SG-003** | APIs | Spring Security annotation checks | HTTP 401/403 Reject |
| **SG-004** | Events | NATS client validation wrapper | Throw Runtime Exception |
| **SG-005** | Auditing | PostgreSQL engine trigger check | SQL Write Fails |
| **SG-006** | Data | JPA Converter checking active KMS keys | Throw DB Write Exception |
| **SG-007** | Network | TLS handshake validation (gRPC worker) | Connection Aborted |
| **SG-008** | Egress | Docker Network / VPC Gateway tables | Connection Timeout (SSRF block) |
| **SG-009** | Injection| Parameterized query check | Fail compilation (AOP wrapper) |
| **SG-010** | Monolith| ArchUnit tests executed in CI | Fail Build |

This standard is approved for all Conductor Engineering activities.
