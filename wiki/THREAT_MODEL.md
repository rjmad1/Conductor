# Threat Model — Conductor Platform

This document details the threat model for the Conductor Platform across its core architectural domains. The analysis uses the **STRIDE** methodology (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, and Elevation of Privilege) to identify security risks, define their impact, establish mitigations, and assess residual risks.

---

## 1. STRIDE Threat Model Matrix

### 1.1 Tenant Management Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Rogue actor registers a tenant with a spoofed identifier or impersonates an existing tenant. | Low | High | Use cryptographically secure UUIDv4 for tenant IDs. Enforce validation of tenant registration requests against an approved domain schema list. | Low |
| **Tampering** | Tenant modifies their feature flags, limits, or billing tier records directly in the DB. | Low | Medium | Restrict Tenant DB updates to the internal billing agent (triggered via secure webhook from payment broker like Razorpay). Enforce DB-level row validation. | Low |
| **Repudiation** | Tenant claims they did not deactivate their subscription or alter account configurations. | Low | Medium | Generate immutable logs for all tenant lifecycle changes (provision, update, disable) in the central audit ledger. | Low |
| **Information Disclosure** | An authenticated tenant queries the system and retrieves metadata belonging to another tenant. | Medium | High | Enforce automated row-level security (RLS) on all database tables via Spring Security and Hibernate filtering. Cross-tenant joins are blocked. | Low |
| **Denial of Service** | A single tenant overwhelms the tenant provisioner with infinite signup calls, exhausting database pools. | Medium | Medium | Implement rate-limiting at Kong API Gateway for registration endpoints. Add CAPTCHA/proof-of-work on public sign-up paths. | Low |
| **Elevation of Privilege** | A standard tenant user accesses the Tenant Global Settings API to change platform-wide system values. | Low | High | Enforce strict RBAC checks at Kong Gateway and Spring Boot filters. Validate OAuth2 scopes (`tenant:write` vs `system:admin`). | Low |

---

### 1.2 Identity Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker executes credential stuffing or brute-forces user login to spoof active sessions. | Medium | High | Enforce MFA on all user accounts. Integrate Keycloak rate-limiting/brute-force detection policies for login endpoints. | Low |
| **Tampering** | Attacker intercepts a JWT token in transit and tampers with scopes/roles inside the token payload. | Low | High | Sign JWTs using RS256/ES256 signatures validated by Kong API Gateway. Enforce HTTPS only (TLS 1.3). Block tokens with invalid signatures. | Low |
| **Repudiation** | User claims their API key was generated without their authorization or by a system bug. | Low | Medium | Log all API key lifecycle events (generation, rotation, deletion) to the immutable audit database table. | Low |
| **Information Disclosure** | API Key hashes are stored in plaintext in the DB, exposing customer workspaces if the DB is compromised. | Medium | High | Hash API keys using bcrypt or PBKDF2 before storing. Never store the raw key in the database (only present once on creation). | Low |
| **Denial of Service** | High volume of login attempts exhausts Keycloak resources, preventing legitimate users from accessing the system. | Medium | High | Configure rate limits on `/auth/login` endpoints at Kong. Scale Keycloak pods independently with virtual thread allocations. | Low |
| **Elevation of Privilege** | Attacker compromises a tenant user session and updates their role mapping to Admin within Keycloak. | Low | High | Standardize role mappings to Keycloak database tables. Require MFA step-up authentication for any role updates. | Low |

---

### 1.3 Workflow Engine (Temporal Domain)

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker registers a rogue worker polling the Temporal queue to hijack campaign executions. | Low | High | Enforce TLS mutual authentication (mTLS) between Temporal server and Spring Boot worker processes. Use client certificates. | Low |
| **Tampering** | Attacker tampers with the JSON DSL definition in transit or storage to inject malicious script tasks. | Low | High | Sign DSL payloads using KMS keys. Validate DSL schemas against strict schemas at ingestion. Ban arbitrary shell execution in workers. | Low |
| **Repudiation** | Operator denies executing a workflow execution run that sent spam messages. | Low | Medium | Log workflow dispatch commands containing user identity claims (`userId`, `tenantId`) in the audit ledger. | Low |
| **Information Disclosure** | Temporal Web UI or console logs expose sensitive customer PII context in workflow history logs. | Medium | High | Cryptographically encrypt workflow input/output payloads at the SDK wrapper level before sending to Temporal server. | Low |
| **Denial of Service** | Infinite loops in JSON DSL workflows consume all worker threads, blocking other campaigns. | Medium | High | Set execution timeouts, step counts, and worker thread quotas. Limit maximum steps per campaign definition to 100. | Low |
| **Elevation of Privilege** | Attacker exploits a workflow worker to access system properties, reading database credentials. | Low | High | Run worker containers as non-root users. Restrict file system write access in Docker runtimes. Inject secrets via environment variables only. | Low |

---

### 1.4 Messaging Domain (Meta WhatsApp API)

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker spoofs Meta Webhook requests, posting fake status updates (`delivered`/`read`) into Conductor. | Medium | High | Validate the Meta signature header (`X-Hub-Signature-256`) against the configured Webhook App Secret. Reject unsigned requests. | Low |
| **Tampering** | Message payload is tampered with in transit before dispatching to Meta API. | Low | High | Use TLS 1.3 for all outgoing calls. Validate payloads against schema definitions before dispatch. | Low |
| **Repudiation** | Customer claims they never gave consent, but the system sent a campaign message. | Low | High | Query Customer Consent Ledger (`consent_records`) before sending. Log consent status, source, and IP to an immutable table. | Low |
| **Information Disclosure** | Outbound messages containing Sensitive PII are leaked in system console logs or Redis caching layers. | Medium | High | Automatically filter/anonymize logs. Implement key-level Redis encryption for cached customer message bodies. | Low |
| **Denial of Service** | Inbound Meta Webhooks spike during active campaigns, exhausting server connection pools. | High | High | Implement a webhook ingestion queue (Kong -> NATS JetStream). Acknowledge webhooks in <1s, then process events asynchronously. | Low |
| **Elevation of Privilege** | Attacker modifies the WhatsApp Business Account credentials to dispatch unauthorized global campaigns. | Low | High | Restrict credentials editing to authorized admins. Encrypt credentials at rest using AWS KMS envelope encryption. | Low |

---

### 1.5 Integrations Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker registers a rogue endpoint as a tenant CRM system to hijack exported lead data. | Medium | High | Enforce domain whitelist rules in Squid proxy. Require HTTPS for all integrations endpoints. Use webhook signatures. | Low |
| **Tampering** | Attacker intercepts webhook callbacks to external CRM endpoints (Zoho, Shopify) and alters parameters. | Low | High | Require OAuth2 or HMAC-SHA256 handshake signing for all egress webhook dispatches. | Low |
| **Repudiation** | Attacker executes outbound SSRF queries and claims the platform performed unauthorized scans. | Medium | High | Route all outbound HTTP requests through the outbound Squid forward proxy. Explicitly ban local subnets (`10.0.0.0/8`, `169.254.169.254`). | Low |
| **Information Disclosure** | OAuth credentials or API keys for Zoho/Shopify are leaked in plaintext database dumps. | Medium | High | Encrypt all third-party integration credentials at rest using AES-256 with KMS-managed customer keys. | Low |
| **Denial of Service** | Malfunctioning external CRM endpoint retries webhook calls infinitely, exhausting worker threads. | High | Medium | Enforce circuit breakers (Resilience4j) and exponential backoff. Cap max connection timeouts to 5 seconds. | Low |
| **Elevation of Privilege** | Exploitation of an integration mapper allows execution of arbitrary Java code within the integration container. | Low | High | Disable dynamic script execution frameworks (e.g., Groovy/JS mappers). Restrict mapping rules to declarative JSON/XML mappings. | Low |

---

### 1.6 Analytics Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker requests Metabase dashboards by spoofing JWT parameters (changing `tenant_id`). | Medium | High | Sign Metabase embed JWT tokens on the backend using RS256 with a secure private key. Validate JWT signatures at Metabase. | Low |
| **Tampering** | Attacker manipulates embedded Metabase client-side charts parameters to run arbitrary SQL. | Low | High | Restrict Metabase read-replica user role privileges to read-only views. Disable ad-hoc SQL editing features in embed views. | Low |
| **Repudiation** | User downloads a full report of PII, and there is no record of the data export. | Low | Medium | Audit all exports and embeds generation requests at the API layer, recording the user ID, timestamp, and query parameters. | Low |
| **Information Disclosure** | Metabase database queries access tables containing customer phone numbers or message bodies. | Medium | High | Anonymize or redact all Tier 1 Sensitive PII from the analytics database schema/replica. Provide aggregated metrics only. | Low |
| **Denial of Service** | Long-running queries run against the transactional PostgreSQL master, lock tables, and take down the system. | High | High | Run all analytics queries exclusively against the Postgres read-replica or ClickHouse. Enforce query timeouts (e.g., 30s). | Low |
| **Elevation of Privilege** | Attacker bypasses React dashboards and logs directly into Metabase console as Administrator. | Low | High | Disable standard local credentials login on Metabase. Enforce SSO/OIDC integration tied to Keycloak root admin rules. | Low |

---

### 1.7 AI Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker accesses AI Chat API endpoints by bypassing OIDC checks. | Low | High | Enforce standard OIDC JWT validation at Kong API Gateway for all `/api/v1/ai/...` paths. | Low |
| **Tampering** | Attacker injects prompt injections to manipulate LLM outputs or bypass alignment rules. | High | Medium | Implement prompt guardrails and validation wrappers. Restrict prompt templates to fixed schemas. Sanitize inputs. | Medium |
| **Repudiation** | Attacker uses the Copilot to extract configuration details, claiming it was standard system functionality. | Low | Medium | Log all LLM prompt and response payloads to the audit trail along with the requesting user context. | Low |
| **Information Disclosure** | Customer context search (Qdrant RAG) leaks cross-tenant vectors because of missing isolation logic. | Medium | High | Validate and enforce metadata-based tenant filters (`tenant_id = :activeTenant`) on every vector query in Qdrant. | Low |
| **Denial of Service** | Repeated, expensive AI chat requests exhaust LiteLLM connection slots or exceed OpenAI API limits. | High | High | Implement strict API rate limiting (Token Bucket) on AI endpoints at Kong API Gateway. Set monthly budget ceilings. | Low |
| **Elevation of Privilege** | Exploitation of AI tool-calling allows executing local shell commands or calling restricted administrative APIs. | Low | High | Disable direct shell execution tool capabilities. Restrict tool calling to approved, authenticated mock API integrations. | Low |

---

### 1.8 Audit Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker writes spoofed audit logs to cover their tracks after modifying system configurations. | Low | High | Enforce database-level triggers that automatically write to the audit ledger. The application user cannot write directly. | Low |
| **Tampering** | Attacker modifies or deletes audit logs from the database table to erase records of an attack. | Low | High | Implement DB triggers that raise exceptions on `UPDATE` or `DELETE` operations against the `audit_logs` table. | Low |
| **Repudiation** | System fails to log an administrative role change, allowing an attacker to deny making the change. | Low | High | Use database triggers on Keycloak tables or Spring Security context audits to guarantee record capture. | Low |
| **Information Disclosure** | Audit logs contain plaintext customer PII, violating DPDP erasure mandates. | Medium | High | Encrypt log payloads. Exclude Tier 1 PII data from the audit logs; reference entity UUIDs instead of actual strings. | Low |
| **Denial of Service** | Audit database table expands excessively, consuming all available system storage. | Medium | Medium | Partition the `audit_logs` table monthly. Periodically archive older partitions to read-only, immutable S3 Glacier stores. | Low |
| **Elevation of Privilege** | A standard tenant user accesses the audit endpoints to view administrative log tracks. | Low | High | Restrict `/api/v1/audit-logs` endpoint access to authorized security roles via Spring Security RBAC. | Low |

---

### 1.9 Observability Domain

| STRIDE Category | Threat Description | Likelihood | Impact | Mitigation Strategy | Residual Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Attacker sends false telemetry metrics or log packets to Prometheus/Loki to confuse operations. | Medium | Medium | Require API token authentication or TLS client certificates for ingestion endpoints on the OTel Collector. | Low |
| **Tampering** | Attacker alters dashboard configurations in Grafana to hide system anomalies. | Low | Medium | Provision Grafana dashboards via declarative JSON configuration files loaded read-only in the container. | Low |
| **Repudiation** | Attacker clears Grafana system alert logs, deleting operational historical context. | Low | Low | Forward alert logs to the central read-only Loki storage engine. | Low |
| **Information Disclosure** | Application logs forwarded to Loki contain customer names, phone numbers, or passwords. | High | High | Implement Logback masking filters to strip phone numbers, email strings, and auth tokens before logs leave the container. | Low |
| **Denial of Service** | Attacker sends massive logs stream to Loki, crashing the observability storage layer. | Medium | Medium | Set limits on rate/volume per tenant at the OTel Collector level. Configure Loki buffer ceilings. | Low |
| **Elevation of Privilege** | Attacker exploits Grafana dashboard access to read host metrics, identifying infrastructure vulnerabilities. | Low | High | Restrict Grafana access to IT/Operations roles using OIDC Keycloak mappings. Disable public dashboard sharing. | Low |

---

## 2. High-Priority Threat Summary

1.  **Cross-Tenant Data Leakage (Information Disclosure):** Mitigated by automated RLS filters on PostgreSQL queries and metadata filters on Qdrant vector databases.
2.  **Server-Side Request Forgery (SSRF) via Webhooks (Information Disclosure/DoS):** Mitigated by routing all integration traffic through Squid forward proxies that block local subnets.
3.  **Audit Trail Erasure (Tampering/Repudiation):** Mitigated by database-level triggers enforcing insert-only properties on the audit logs schema.
4.  **PII Leakage in Observability Logs (Information Disclosure):** Mitigated by wrapper filters in Java logging profiles masking patterns of phone numbers and emails.

This threat model is verified as comprehensive for the current modular design.
