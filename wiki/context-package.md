# Context Package: Task T-203 Immutable Consent Trigger

- **Task ID:** T-203
- **Run ID:** LOOP-002-20260627-001
- **Domain:** Customer & Compliance
- **Scope:** Immutable database constraints for DPDP India 2023 compliance.

---

## 1. Backlog Specification

From `backlog/MVP_BACKLOG.md`:
* **Task T-203: Immutable Consent Trigger**
    * **Description:** Write database migration setting up the `consent_records` table and database triggers to block all `UPDATE` and `DELETE` requests.
    * **Dependencies:** T-201
    * **Estimate:** S
    * **Risk:** Low
    * **Acceptance Criteria:** Issuing an UPDATE or DELETE query against `consent_records` results in a database exception error.

---

## 2. Identified Repository Context Files

The following files constrain, configure, or implement the requirements for Task T-203:

### 2.1 [V004__create_consent_records_table.sql](file:///c:/Users/rajaj/Projects/Conductor/platform/customer/src/main/resources/db/migration/V004__create_consent_records_table.sql)
This Flyway SQL migration creates the `consent_records` table and binds the `enforce_immutable_consent` trigger to prevent updates and deletions.

```sql
CREATE TABLE consent_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    channel VARCHAR(255),
    legal_basis VARCHAR(255),
    consent_version VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_consent_customer_type ON consent_records(tenant_id, customer_id, consent_type);
CREATE INDEX idx_consent_customer_created ON consent_records(tenant_id, customer_id, created_at);

-- Enforce immutability trigger: reject UPDATE/DELETE on consent_records
CREATE OR REPLACE FUNCTION block_consent_record_alteration()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Alteration of consent records is strictly prohibited. Violates compliance constraints.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_immutable_consent
BEFORE UPDATE OR DELETE ON consent_records
FOR EACH ROW EXECUTE FUNCTION block_consent_record_alteration();
```

---

## 3. Verification Criteria & Test Plan

1. **Database Schema Verification:** Confirm the database migrations run clean and apply the trigger.
2. **Behavioral Constraint Verification:** 
   - Execute an INSERT statement on the `consent_records` table to ensure record creation is allowed.
   - Execute an UPDATE statement targeting any row in the `consent_records` table; it must fail with the database exception: `Alteration of consent records is strictly prohibited. Violates compliance constraints.`.
   - Execute a DELETE statement targeting any row in the `consent_records` table; it must fail with the same database exception.
