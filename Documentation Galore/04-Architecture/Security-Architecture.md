# Security Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None in source documents (critical gap)  
**Last Updated:** June 2026

---

## Purpose
Defines all security controls, threat mitigations, and compliance implementations for the Conductor platform.

---

## Security Domains

### 1. Identity & Access Management

**Authentication:**
- All users authenticate via Keycloak (email/password + optional OTP)
- JWTs issued by Keycloak, signed with RS256
- JWT expiry: 15 minutes (access token) + 30 days (refresh token)
- All API requests require a valid JWT in `Authorization: Bearer {token}` header
- Kong validates JWT signature against Keycloak's JWKS endpoint at `/.well-known/jwks.json`

**Authorization (RBAC):**
```
OWNER:   ALL permissions (including billing, user management, delete)
ADMIN:   All except billing and owner management
MANAGER: workflow.*, campaign.*, customer.read, analytics.read
AGENT:   conversation.read, conversation.write, customer.read
ANALYST: analytics.read, *.read (no write)
```

**API Keys:**
- Hashed with bcrypt (cost 12) before storage
- Never logged in application logs
- Scoped: each API key has an explicit scope list
- Auto-expire supported (TTL field)

---

### 2. Network Security

**Ingress Path:**
```
Internet → CloudFlare (WAF + DDoS) → Load Balancer → Kong API Gateway → Internal services
```

**Kong Security Plugins:**
- `jwt` — Validate JWT on all protected routes
- `rate-limiting` — Per tenant: 1,000 req/min (configurable by plan)
- `ip-restriction` — Block known malicious IPs (CloudFlare feeds)
- `cors` — Restrict to allowed origins

**Internal Network:**
- Services communicate over private VPC — no public exposure
- PostgreSQL, Redis, NATS: not exposed to internet
- Service-to-service: no auth in MVP (mTLS in Phase 2)

**Webhook Endpoints:**
- All inbound webhooks (WhatsApp, Shopify, etc.) validate HMAC signatures before processing
- Invalid signature → 401 immediately, request dropped
- Replay protection: timestamp check (reject if > 5 minutes old)

---

### 3. Data Security

**Encryption at Rest:**
- PostgreSQL disk encryption: AES-256 (AWS RDS encryption enabled)
- Column-level encryption for sensitive fields:
  - `customers.phone` — optional masking in logs
  - `connector_configs.credentials` — encrypted with platform key (AES-256-GCM)
  - `whatsapp_numbers.access_token` — encrypted with platform key
  - `api_keys.key_hash` — bcrypt hash (not reversible)
- S3 buckets: SSE-S3 encryption

**Encryption in Transit:**
- TLS 1.3 for all external traffic (Kong terminates SSL)
- Internal service communication: TLS 1.2+ (MVP: HTTP inside VPC, TLS in Phase 2)
- Database connections: SSL mode required

**Secrets Management:**
- No secrets in code or environment variables
- Secrets stored in AWS Secrets Manager or HashiCorp Vault
- Services retrieve secrets at startup via SDK
- Secret rotation: quarterly (automated for DB passwords)

---

### 4. Multi-Tenant Data Isolation

**Database Level:**
- `tenant_id` column on every table with PII or business data
- Row-level security (RLS) — implemented via application-level checks (not PostgreSQL RLS in MVP; consider for Phase 2)
- Every ORM query builder injects `WHERE tenant_id = :currentTenantId`
- Queries without tenant_id filter will fail in code review

**Application Level:**
- `tenant_id` extracted from JWT claim (not from user-supplied input)
- Service layer validates that the resource being accessed belongs to the authenticated tenant
- Cross-tenant access: returns 404 (not 403) — do not reveal that the resource exists

---

### 5. Application Security

**OWASP Top 10 Mitigations:**

| Risk | Mitigation |
|---|---|
| A01 Broken Access Control | RBAC enforced at every endpoint, tenant isolation at DB level |
| A02 Cryptographic Failures | AES-256 at rest, TLS 1.3 in transit, bcrypt for passwords |
| A03 Injection | Parameterized queries (JPA/Hibernate), no string concatenation in SQL |
| A04 Insecure Design | Security design review before each service goes to production |
| A05 Security Misconfiguration | Hardened base images, security scanning in CI/CD |
| A06 Vulnerable Components | Dependabot alerts, quarterly dependency updates |
| A07 Auth Failures | Keycloak with brute-force protection, account lockout after 5 failures |
| A08 Integrity Failures | Signed JWTs, HMAC webhook verification |
| A09 Logging Failures | Centralized logging, security events tagged, no PII in logs |
| A10 SSRF | Allowlist for external API calls; no user-supplied URLs fetched by server |

**Input Validation:**
- All inputs validated at API gateway (Kong request validation plugin)
- Backend re-validates all inputs (defense in depth)
- Phone number validation: E.164 format enforcement
- SQL injection: parameterized queries via Hibernate, no native query string concatenation

**PII in Logs:**
- Phone numbers in logs: masked to `+91XXXXX99999`
- Customer names: not logged at DEBUG level
- API keys: never logged (only last 4 characters of hash)
- WhatsApp message content: not logged by default (configurable opt-in for debugging)

---

### 6. Compliance

**DPDP India (Digital Personal Data Protection Act 2023):**
- Explicit consent captured before any marketing message
- Consent records are immutable and auditable
- Right to erasure: customer data deletion within 30 days of verified request
- Data residency: India region hosting recommended (AWS ap-south-1 or GCP asia-south1)
- Privacy notice: plain-language consent text with each opt-in
- Data breach notification: to DPBI within 72 hours of becoming aware

**WhatsApp Business Policy Compliance:**
- Use only approved templates for marketing messages
- Frequency cap: max 1 marketing conversation per customer per day
- STOP handling: immediate opt-out, no further messages
- No political content, adult content, prohibited goods
- Meta's prohibited industries: not served (weapons, gambling, tobacco — checked at tenant onboarding)

**GDPR (for future international expansion):**
- Same consent model as DPDP applies
- Data Processing Agreement (DPA) template available for tenants
- Data subject access requests supported (export customer data as JSON)

---

### 7. Security Monitoring

**Audit Events Logged:**
- User login success / failure
- Admin actions (user role changes, workflow activation)
- Customer data access (with data classification)
- API key creation / revocation
- Connector connect / disconnect
- Data deletion requests

**Alerting Triggers (PagerDuty / Grafana Alerts):**
- 5+ failed login attempts for a user in 5 minutes → alert + account lockout
- API rate limit repeatedly hit by a tenant → alert (potential abuse)
- Unusual message volume spike (>3x normal) → alert
- Webhook HMAC failures > 10/minute → alert (potential replay attack)

---

### 8. Security Testing

**Pre-Launch (MVP):**
- OWASP ZAP automated scan against staging environment
- Manual penetration testing of authentication flows and multi-tenancy isolation
- Dependency vulnerability scan (OWASP Dependency-Check)

**Ongoing:**
- SAST: SonarQube in CI/CD pipeline
- DAST: Weekly automated scan against staging
- Dependency alerts: Dependabot
- Annual third-party penetration test (Phase 2+)

---

## Security Incident Response

**Severity Classification:**
| Severity | Definition | Response Time |
|---|---|---|
| P0 | Data breach, unauthorized cross-tenant access | Immediate (< 1 hour) |
| P1 | Authentication bypass, privilege escalation | < 4 hours |
| P2 | Rate limiting bypass, injection vulnerability | < 24 hours |
| P3 | Information disclosure, UI vulnerability | < 72 hours |

**Response Steps (P0/P1):**
1. Detect → PagerDuty alert to on-call engineer
2. Contain → Revoke affected tokens, isolate affected tenant if needed
3. Investigate → Audit logs, access logs
4. Communicate → Notify affected tenant within 24h
5. Remediate → Patch and deploy
6. Post-mortem → Root cause analysis within 5 business days

---

## Cross-References
- `07-Governance/Compliance.md` — Full compliance requirements
- `04-Architecture/Data-Architecture.md` — Data encryption at field level
- `06-Operations/Incident-Management.md` — Incident response playbook
