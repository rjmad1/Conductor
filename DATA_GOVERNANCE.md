# Data Governance Specification — Conductor Platform

This specification defines the data governance, protection, classification, isolation, and compliance standards for the Conductor Platform. It is designed to satisfy the requirements of the **Digital Personal Data Protection (DPDP) Act India 2023** and **SOC 2 Type II**.

---

## 1. Multi-Tenant Data Isolation Strategy

1.  **Logical Isolation Model (Row-Level Security):** Every table housing tenant-specific business data must include a `tenant_id` column as a foreign key targeting the `tenants` catalog.
2.  **JPA ORM Interception:** Developers must inherit from the shared `TenantAwareEntity` base class. Database operations are automatically filtered using Hibernate `@FilterDef` + Spring Interceptors injecting the active thread context's `X-Tenant-ID`.
3.  **Cross-Tenant Leak Prevention:**
    *   Direct database table joins between modules are prohibited.
    *   No raw SQL queries (e.g. native JPA queries) may execute without an explicit `tenant_id` check.
4.  **Keycloak Realms:** User records and login sessions are isolated at the IAM level using separate Keycloak Realms per tenant.

---

## 2. Personal Data (PII) Classification & Compliance

We enforce a 3-tier data classification system to protect Personally Identifiable Information (PII):

| Classification Tier | Data Elements | Encryption/Security Rule | Compliance SLA |
| :--- | :--- | :--- | :--- |
| **Tier 1: Sensitive PII** | Customer names, phone numbers, email addresses, chats/messages body. | Encrypted-at-rest. Anonymized in logs. Hashed indexes. | Subject to **Right to Erasure** (30-day purge SLA). |
| **Tier 2: Business Metadata** | Campaign names, workflow DSLs, schedules, pricing rules, logs structure. | Encrypted-at-rest. Stored as plain-text JSONB templates. | Retained based on subscription lifecycle (deleted on churn). |
| **Tier 3: Anonymous Metrics** | CTRs, deliverability ratios, trace IDs, system resource gauges. | Aggregated, non-PII metrics. Excluded from RLS rules. | Retained for 36 months for platform trends analysis. |

---

## 3. DPDP Act India 2023 & GDPR Compliance Rules

1.  **Right to Erasure (DPDP Section 12):** When a contact requests deletion, or a tenant churns, the platform must execute a complete scrub of Tier 1 PII within the **30-day compliance SLA**.
2.  **Consent Tracking:** The Customer domain maintains an immutable, append-only `consent_records` ledger detailing opt-in/opt-out status, source of consent, and UTC timestamp. Outbound campaign engines must check this table before sending messages.
3.  **Local Data Residency (DPDP Mumbai Mandate):** All Indian tenant data, including transaction PostgreSQL servers, Redis cache nodes, and backups, must reside within the **AWS Mumbai (`ap-south-1`) region**.

---

## 4. Encryption Standards

1.  **In Transit:** All HTTP/gRPC ingress and inter-container traffic must utilize **TLS 1.3** with approved cipher suites.
2.  **At Rest:** 
    *   PostgreSQL and ClickHouse filesystems must run AES-256 block-level encryption managed via AWS KMS keys.
    *   Redis caches must run database-level encryption for keys containing user context scopes.
3.  **Logs:** Logs must be filtered at the application wrapper layer to strip Tier 1 details (phone numbers/emails) before forwarding to Loki.

---

## 5. Retention Policies

| Data Category | Target Store | Retention Period | Pruning Action |
| :--- | :--- | :--- | :--- |
| **Audit Logs** | PostgreSQL (Central Partition) | 36 Months | Archive monthly partitions to S3 glacier. |
| **Workflow Histories** | Temporal Metadata DB | 7 Days | Automated Temporal history pruning task. |
| **Message Delivery Logs** | ClickHouse OLAP | 90 Days | Prune columns with Tier 1 data; roll up metrics. |
| **Transactional Metrics** | Prometheus TSDB | 15 Days | Automatic Prometheus retention rules. |

---

## 6. Backup, Disaster Recovery & SLA

### Backup Architecture
*   **Snapshots:** Amazon RDS PostgreSQL automated daily snapshots with a 30-day retention window.
*   **WAL Archiving:** Continuous Write-Ahead Log (WAL) archiving via WAL-G to S3 bucket stores.
*   **Location:** Backup buckets must reside in the `ap-south-1` Mumbai region to conform to DPDP data residency requirements.

### Recovery Objectives
*   **RTO (Recovery Time Objective):** $< 4$ Hours (Time to reconstruct core platform services following a regional disaster).
*   **RPO (Recovery Point Objective):** $< 1$ Hour (Maximum acceptable window of data loss for transactions).
