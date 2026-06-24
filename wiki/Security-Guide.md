# Conductor Security Guide

## A. Purpose
This security guide defines the cryptographic protocols, data protection standards, ingress/egress firewalls, threat models, and compliance constraints (DPDP / WABA) that govern the Conductor platform.

## B. Intended Audience
- Security Officers and Auditors
- Platform Architects
- DevOps Engineers

## C. Scope
Details IAM verification layers, webhook cryptographic checks, egress proxies, secrets isolation, and regulatory matrices.

## D. Prerequisites
- Review of the repository [Threat Model](file:///c:/Users/rajaj/Projects/Conductor/THREAT_MODEL.md) and [Trust Boundaries](file:///c:/Users/rajaj/Projects/Conductor/TRUST_BOUNDARIES.md).

---

## E. Detailed Content

### 1. Identity & Access Management (IAM)
Platform access control is built entirely on open OpenID Connect (OIDC) standards.

- **Central Identity Provider**: Keycloak manages user identities, passwords, and sessions.
- **Tenant Separated Realms**: One realm represents one tenant domain context. Users inside realm A cannot authenticate or query resources in realm B.
- **Token Claims**: Access tokens generated contain `tenant_id` and `realm_roles`.
- **Role-Based Access Control (RBAC)**: Enforced via Spring Security annotations (`@PreAuthorize` checked at runtime on REST endpoints):
  - `ROLE_PLATFORM_ADMIN`: Global operations (can provision/suspend tenants).
  - `ROLE_TENANT_ADMIN`: Local tenant administrator (can invite users, adjust integrations).
  - `ROLE_TENANT_OWNER`: Has billing and administrative permissions.
  - `ROLE_TENANT_AGENT`: Read-write access to conversation workflows and inboxes.

---

### 2. Inbound Webhook Cryptographic Signatures
To prevent spoofing or unauthorized event injection, all external webhook ingress callbacks undergo signature validation.

- **Check Flow**: When a payload arrives, [WebhookIngressController](file:///c:/Users/rajaj/Projects/Conductor/platform/integrations/src/main/java/com/conductor/integrations/webhooks/WebhookIngressController.java) intercepts the request headers.
- **Shopify HMAC**: Validates that `X-Shopify-Hmac-SHA256` header matches the computed HMAC-SHA256 of the raw body payload using Shopify’s client secret.
- **Razorpay HMAC**: Checks `X-Razorpay-Signature` against computed values.
- **Zoho HMAC**: Checks `X-Zoho-Signature` signature maps correctly.
- **Replay Mitigation**: Redis stores incoming message identifiers for 24 hours to block replay injection attacks.

---

### 3. Outbound Egress Isolation (SSRF Protection)
Because the integration framework allows workflows to dispatch custom HTTP requests to tenant-specified webhook URLs, the platform is vulnerable to Server-Side Request Forgery (SSRF).

- **Egress Proxy**: All outgoing HTTP traffic originating from the integration engine is forced through a Squid forward proxy.
- **Strict Allowlist**: Squid proxy settings block access to:
  - Private IP addresses (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`).
  - Link-Local Address (`169.254.169.254` to block AWS Metadata token theft).
  - Internal Kubernetes DNS resolution namespaces.
- **Static IPs**: The egress proxy is routed through static AWS NAT Gateways to allow tenants to whitelist Conductor egress IPs in their external firewalls.

---

### 4. Secrets Management Standard
- **No committed secrets**: Git hooks run validation scans to block check-ins containing keys.
- **Runtime Injection**: All credentials, database passwords, and client keys must be injected as environment variables (`${DATABASE_PASSWORD}`) at runtime.
- **Envelope Encryption**: Sensitive tenant credentials (such as Zoho Client Secrets) are stored encrypted in PostgreSQL using AES-256 with keys managed by AWS KMS (envelope encryption model).

---

### 5. Compliance Matrix

| Regulation | Platform Mapping | Implementation Strategy |
|---|---|---|
| **DPDP India** (Data Protection) | Localized Storage | AWS Mumbai region deployment; strict separation of tenant databases; PII data encrypted using `PiiEncryptedConverter`. |
| **Meta WhatsApp Business Policy** | Rapid Opt-Out Process | Automatic processing of `STOP` keywords. Immediate database consent update in [ConsentService](file:///c:/Users/rajaj/Projects/Conductor/platform/customer/src/main/java/com/conductor/customer/service/ConsentService.java) within 5 seconds. |
| **SOC2 Common Criteria** | Immutable Audit Trail | Database-level triggers logging modifications to an immutable `audit_logs` table partition. |

---

## F. References
- [System Context](System-Context)
- [Decision Records Index](Decision-Records-Index)

## G. Related Wiki Pages
- [Implementation Guide](Implementation-Guide)
- [Operations Guide](Operations-Guide)
