# Secrets Management Standard — Conductor Platform

This standard defines the policies, tools, and processes for managing cryptographic keys, database credentials, API keys, certificates, and third-party integration secrets across the Conductor Platform.

---

## 1. Secrets Storage Architecture

The platform splits secrets management into two distinct layers based on ownership:

```
                  ┌─────────────────────────────────────────┐
                  │          CONDUCTOR CRYPTO CORE          │
                  └─────────────────────────────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
┌───────────────────────────┐                         ┌───────────────────────────┐
│     Platform Secrets      │                         │ Tenant Integration Secrets│
│ (DB, TLS, OAuth, NATS...) │                         │ (Zoho Keys, WhatsApp...)  │
├───────────────────────────┤                         ├───────────────────────────┤
│ AWS Secrets Manager / KMS │                         │ Envelope Encrypted GCM    │
│ Managed via IAM Roles     │                         │ Stored in Postgres DB     │
└───────────────────────────┘                         └───────────────────────────┘
```

### 1.1 Platform Infrastructure Secrets
*   **Target Scope:** Database credentials (Postgres, ClickHouse, Redis), NATS security tokens, Keycloak admin keys, session JWT signing keys, TLS private certificates.
*   **Storage standard:** Locked in a dedicated secure secret store. Run-time injection via environment variables.

### 1.2 Tenant-Specific Integration Secrets
*   **Target Scope:** Tenant Zoho CRM client secrets, Shopify OAuth tokens, Meta WhatsApp API credentials.
*   **Storage standard:** Since cloud secrets managers cannot scale cost-effectively to house thousands of dynamic tenant credentials, these secrets are stored in the shared PostgreSQL database. They are **envelope encrypted** at rest using AES-256-GCM with a Master Key managed in AWS KMS.

---

## 2. Secrets Technologies Evaluation

We evaluated four secret management solutions for the Conductor Platform:

### 2.1 Google Secret Manager (GSM)
*   **Pros:** Fully managed, simple IAM integration, native automatic secret versioning.
*   **Cons:** Not native to AWS Mumbai environment where ECS Fargate is running. Accessing across clouds introduces latency and security egress complexities.
*   **Verdict:** Rejected for production application runtime (AWS native preferred); acceptable if future modules deploy on GCP.

### 2.2 HashiCorp Vault
*   **Pros:** Industry standard, multi-cloud capability, dynamic secret generation, robust audit logging.
*   **Cons:** Very high operational burden. A 3-4 engineer team cannot justify the maintenance of a highly available, unsealed Vault cluster.
*   **Verdict:** Rejected for MVP due to high maintenance overhead.

### 2.3 Kubernetes External Secrets Operator (ESO)
*   **Pros:** Native Kubernetes CRD mapping. Syncs external secrets (like AWS/GCP Secrets Manager) directly into Kubernetes Secrets.
*   **Cons:** Only applicable when deploying on Kubernetes. Does not apply to local Docker-Compose or raw ECS Fargate runtimes.
*   **Verdict:** Approved for staging/production Kubernetes environments (mapping AWS Secrets Manager to Pod environments).

### 2.4 SOPS (Secrets on Kubernetes / Files)
*   **Pros:** Integrates with GitOps. Encrypts files (JSON/YAML) using KMS/PGP. Can commit encrypted secret files directly to Git. Decrypted at deploy time.
*   **Cons:** Relies on files. Key rotation requires manual re-encryption.
*   **Verdict:** Approved for storing configuration secrets in Git repositories.

---

## 3. Canonical Secrets Strategy

The platform implements a **Hybrid Secrets Strategy**:

*   **Local Developer Stack:** Evaluated parameters injected via encrypted `.env.local` files or decrypted using **SOPS** with local PGP keys.
*   **AWS Production Environment:** Standardized on **AWS Secrets Manager** (native to AWS Mumbai `ap-south-1`) integrated with KMS. ECS Fargate tasks pull environment properties directly from Secrets Manager using AWS Execution IAM Roles.
*   **Kubernetes Staging Stack:** **External Secrets Operator (ESO)** pulls values from AWS Secrets Manager and populates Kubernetes Secrets dynamically.

---

## 4. Specific Secrets Standard Protocols

### 4.1 API Keys (Developer Keys)
*   **Storage:** Keys are never stored in plaintext in the database.
*   **Hashing:** API keys are hashed using **SHA-256** with a salt. Only the hashed signature is stored.
*   **Verification:** The gateway hashes the incoming header key and performs a constant-time lookup.

### 4.2 Webhook Signature Secrets
*   **Storage:** Webhook signing secrets are generated uniquely per integration.
*   **Security:** Stored in the PostgreSQL database under AES-256-GCM envelope encryption.

### 4.3 Database & Cloud Credentials
*   **Storage:** AWS Secrets Manager.
*   **Rotation:** Automatic credential rotation enabled every 60 days via AWS Lambda triggers syncing RDS PostgreSQL with Secrets Manager.

### 4.4 OAuth Credentials (WABA / CRM Tokens)
*   **Access:** Retrieved at runtime by the integration engine wrapper.
*   **Lifecycle:** Short-lived access tokens are cached in Redis (with TTL matched to token lifetime). Long-lived refresh tokens are stored database-encrypted.

### 4.5 TLS Certificates
*   **Ingress Gateway:** Managed at the Kong Gateway container, provisioned/renewed automatically via Let's Encrypt or integrated with AWS Certificate Manager (ACM).
*   **Internal Service mTLS:** Temporal mTLS certificates generated via an internal CA (HashiCorp Vault or cert-manager) and rotated automatically.

### 4.6 Encryption Keys (Data at Rest)
*   **Master Key:** AWS KMS Symmetric Key (`ap-south-1`) with rotation policy enabled.
*   **Key Isolation:** Platform data volumes (EBS/RDS) use AWS managed CMKs. Tenant integration fields use a custom customer-managed KMS key.

---

## 5. Security Guardrails for Secret Handling

1.  **No Plaintext Secrets in Code:** Committing plaintext passwords, tokens, or API keys to the repository is blocked. The pre-commit hook runs `gitleaks` scans on all commits.
2.  **No Secrets in Logs:** Application logs must be filtered to block credentials strings. Logging frameworks must mask headers containing `Authorization` or `X-API-Key`.
3.  **Default Values Block:** Spring Boot configuration keys representing passwords must not contain default plaintext values in `application.yml`. If missing, the application must fail to boot.

This standard is approved for all secret handling practices.
