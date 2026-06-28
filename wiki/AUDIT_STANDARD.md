# Audit Standard — Conductor Platform

This standard defines the architecture, events list, schema design, and engine trigger requirements for the Conductor Platform's immutable trigger-based audit ledger.

---

## 1. Audit System Architecture

The platform implements a **PostgreSQL Trigger-Based Database Audit Ledger** (complying with ADR-009). This ensures that any change made directly to database tables (whether via the Spring Boot application or a database administrator executing manual SQL queries) is captured securely.

```
 [Application Write] OR [Manual SQL Query]
          │
          ▼
    [Target Table]
          │
          ├──► (EXECUTE Trigger BEFORE INSERT/UPDATE/DELETE)
          ▼
   [audit_logs table]
          │
          └──► Trigger Enforces: IF UPDATE OR DELETE ON audit_logs THEN RAISE EXCEPTION
```

### Key Integrity Rules
*   **Immutability:** The `audit_logs` table has database triggers that block `UPDATE` and `DELETE` queries. It is strictly append-only.
*   **Decoupled Auditing:** Application-level code logs events to log files, but the regulatory record of state changes is generated at the database transaction layer.

---

## 2. Audit Database Schema (PostgreSQL DDL)

All audits populate a centralized schema partitioned physically by `tenant_id` and monthly ranges:

```sql
CREATE TABLE audit_logs (
    event_id UUID DEFAULT gen_random_uuid() NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,       -- INSERT, UPDATE, DELETE, AUTH, EXPORT
    entity_type VARCHAR(100) NOT NULL,  -- e.g. WORKFLOW, CONSENT, USER_ROLE
    entity_id VARCHAR(100) NOT NULL,
    pre_image JSONB,                    -- Row state before modification (NULL for INSERT)
    post_image JSONB,                   -- Row state after modification (NULL for DELETE)
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (tenant_id, event_id, created_at)
) PARTITION BY RANGE (created_at);
```

### 2.1 Immutability Trigger Code
To guarantee the ledger cannot be altered, the following trigger is applied to `audit_logs`:

```sql
CREATE OR REPLACE FUNCTION block_audit_log_alteration()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Alteration of audit logs is strictly prohibited. Violates compliance constraints.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_immutable_audit
BEFORE UPDATE OR DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION block_audit_log_alteration();
```

---

## 3. Auditable Events Registry

The platform captures the following operational events:

| Event Name | Trigger Source | Classification | Captured Metadata Details |
| :--- | :--- | :--- | :--- |
| **User Login / Logout** | Keycloak Authentication Webhook | Identity | Realm ID, user UUID, login status (success/failure), IP Address, User Agent. |
| **Role Change** | Trigger on `user_roles` mapping table | Authorization | Target user ID, changed role, scope variables, authorizer user ID. |
| **Workflow DSL Change** | Trigger on `workflow_definitions` table | Workflow | Workflow UUID, change type (created, updated, deleted), diff of DSL JSON template. |
| **WhatsApp Template Sync**| Trigger on `message_templates` table | Messaging | Meta template ID, category, approval state, variables schema. |
| **Campaign Message Dispatched**| Trigger on `messages` write operations | Messaging | Message ID, target channel (WhatsApp), campaign ID. (PII is hashed/tokenized). |
| **Integration Configured** | Trigger on `integration_settings` table| Integrations | Integration type (Zoho/Shopify), config keys altered. (Secrets are encrypted JSON). |
| **Consent Granted** | Trigger on `consent_records` insert | Compliance | Customer ID token, channel, source of consent, validation IP. |
| **Consent Revoked** | Trigger on `consent_records` update | Compliance | Opt-out keyword matched ("STOP"), timestamp, opt-out channel. |
| **AI Invocation** | API Controller Filter on `/api/v1/ai` | AI Context | Prompt category, execution time, token count. (Excludes raw user prompt if containing PII). |
| **Data Export Triggered** | API Controller Filter on `/api/v1/analytics/export` | Data Export | Exporter user ID, query query params, target format (CSV/PDF), row count. |
| **Data Deletion Executed**| Trigger on hard purge runs | Data Deletion | Entity type, target entity UUID, deletion status, trigger timestamp. |

---

## 4. Integrity and Compliance Verification

1.  **Tamper Detection (Hash Chaining):** Every audit log record can store a hash linking it to the previous record (`previous_row_hash`). A daily verification task recalculates the SHA-256 chain. If a database administrator attempts to drop a database partition or force-edit a row bypass-trigger, the chain breaks, raising an immediate high-priority security alert.
2.  **Compliance SLA (DPDP Section 11):** Audit logs documenting opt-out requests and data erasure actions must remain accessible for 36 months to verify compliance with erasure requests, after which they are archived to S3 Glacier Vault Locks.

This standard is approved for all system audits.
