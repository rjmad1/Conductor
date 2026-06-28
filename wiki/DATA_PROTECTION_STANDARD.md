# Data Protection Standard — Conductor Platform

This standard defines the data classification framework, encryption standards, retention windows, masking and tokenization rules, and deletion and archival processes for the Conductor Platform. It is designed to satisfy the Digital Personal Data Protection (DPDP) Act India 2023, GDPR, and SOC 2 Type II controls.

---

## 1. Data Classification Matrix

The platform classifies data into nine categories, mapping each to specific security and compliance rules:

| Classification | Data Elements | Encryption (Transit / Rest) | Masking / Tokenization | Retention Policy | Deletion SLA (Purge) | Archival Target |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Public** | API docs, static assets, logos, public landing pages. | TLS 1.3 / None (Public S3) | None | Indefinite | N/A | None |
| **Internal** | Monolith configs, dockerfiles, helm values templates. | TLS 1.3 / AWS KMS (EBS) | None | Indefinite | N/A | S3 Glacier |
| **Confidential**| Campaign settings, workflow DSL structures, list names. | TLS 1.3 / AES-256 (KMS) | None | Active subscription | 30 days on churn | S3 Archive |
| **Restricted** | DB passwords, encryption keys, integration tokens. | TLS 1.3 / Envelope (KMS) | Hashed lookup keys | Active subscription | Instant (Revoke/delete) | None |
| **PII** | Customer names, user email addresses. | TLS 1.3 / AES-256 | Masked in logs | Active subscription | 30 days (Erasure request) | S3 Encrypted |
| **Sensitive PII**| Customer phone numbers, WhatsApp chats body. | TLS 1.3 / AES-256 GCM | Masked in logs / Tokenized | 90 Days (Delivery logs) | **30 Days** (Erasure request) | None (Permanent delete) |
| **Financial** | Billing records, subscription state, invoice logs. | TLS 1.3 / AES-256 | Masked except last 4 digits | 7 Years (Tax law mandate)| 30 days on churn | S3 Glacier (Vault Lock) |
| **Audit Data** | Trigger-based audit logs, configuration changes. | TLS 1.3 / AES-256 | None | 36 Months | Retention expiration | S3 Glacier (Vault Lock) |
| **AI Data** | Qdrant semantic vectors, prompt templates, memories. | TLS 1.3 / AES-256 | Metadata tenant filtered | Active campaign | 30 days on churn | None |

---

## 2. Cryptographic Security Standards

### 2.1 Encryption in Transit
*   **API Ingress:** All HTTP and gRPC ingress traffic is secured using **TLS 1.3** with approved secure ciphers (e.g., `TLS_AES_256_GCM_SHA384`). TLS 1.1 and 1.0 are disabled.
*   **Service-to-Service:** mTLS (Mutual TLS) is enforced using internal certificate authorities (CA) for all gRPC connections between workers and Temporal servers.

### 2.2 Encryption at Rest
*   **Database Encryption:** Amazon RDS instances housing PostgreSQL run with storage encryption enabled via AWS KMS managed keys.
*   **File Storage:** All backup objects and log streams exported to Amazon S3 utilize AES-256 server-side encryption with KMS keys (SSE-KMS).
*   **Key Rotation:** KMS cryptographic keys undergo automatic annual rotation.

---

## 3. Data Masking & Tokenization Guidelines

### 3.1 Masking in Logs
*   To prevent exposure of Sensitive PII, application loggers (Logback/Log4j2 wrappers) apply pattern regex masking to all console streams before forwarding to Loki:
    *   **Phone Numbers:** Masked to expose only the country code and last 4 digits: `+91 ******1234`.
    *   **Email Addresses:** Masked to obscure the username prefix: `u*****r@domain.com`.
    *   **Passwords/Tokens:** Completely replaced with `[REDACTED]`.

### 3.2 Tokenization (Secure Pseudonymization)
*   Recipient phone numbers loaded into high-speed Redis message delivery caches are tokenized using a hashing function:
    $$\text{Token ID: } \texttt{token:\{tenantId\}:sha256(phoneNumber + tenantSalt)}$$
    The delivery service utilizes this token ID for index matching, resolving the raw phone number only at the immediate Meta WhatsApp API dispatch boundary.

---

## 4. Deletion & Right to Erasure (DPDP Act Section 12)

1.  **Immediate Logical Deletion:** When a contact requests deletion, the customer record is marked as inactive (`status = DELETED`) instantly, removing it from active campaign filters.
2.  **Hard Deletion Schedule:** An asynchronous Temporal workflow runs daily to execute hard deletes:
    *   Executes `DELETE` statements on tables containing the contact.
    *   Invokes vector deletion APIs in Qdrant matching the contact's payload IDs.
    *   Purges cached elements in Redis.
3.  **Audit Exception:** Database triggers log the deletion event (`action = DELETE`, `entity = customer`, `id = contact-uuid`), capturing *only* the system metadata log. The customer's raw name/phone number is excluded from the audit log record to satisfy the right to erasure.
4.  **30-Day SLA:** All steps must complete physical purging within the **30-day compliance SLA** from request registration.

---

## 5. Retention & Archival Controls

### 5.1 Partitioned Database Archiving (Audit Logs)
*   The `audit_logs` table in PostgreSQL is physically partitioned by month (e.g. `audit_logs_y2026_m06`).
*   An automated cron task runs on the first day of each month:
    1.  Detaches partitions older than 36 months (e.g., $N-36$).
    2.  Exports the partition records to a secure CSV file.
    3.  Encrypts and writes the file to an **AWS S3 Glacier Vault** configured with a **Vault Lock Policy** (WORM - Write Once, Read Many) blocking deletion for 7 years.
    4.  Drops the detached partition from the active database.

### 5.2 Purging Non-Transactional Delivery Logs
*   ClickHouse stores messaging delivery logs (`sent`/`delivered`/`read` details) to populate metrics.
*   After **90 days**, a ClickHouse TTL policy automatically purges columns containing Tier 1 Sensitive PII (raw phone numbers), keeping aggregated statistics (total counts, rates) for long-term trends.

This standard is approved for all data lifecycle configurations.
