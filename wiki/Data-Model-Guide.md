# Data Model Guide

## A. Purpose
This page documents the PostgreSQL transactional schemas (managed by Flyway migrations) and the ClickHouse OLAP schemas that store operational metrics.

---

## B. PostgreSQL Schema (Ground Truth)

Transactional data is logically isolated by `tenant_id`. Here are the primary database tables:

### 1. Tenant & Audit Domain
- **`tenants`**: Stores core tenant profiles.
  - Columns: `id` (UUID, PK), `name` (VARCHAR), `domain` (VARCHAR, Unique), `subscription_status` (VARCHAR), `subscription_tier` (VARCHAR), `created_at`, `updated_at`.
- **`audit_logs`**: Capture database trigger modifications. Physically partitioned by monthly ranges on `created_at`.
  - Columns: `id` (BIGINT, PK), `tenant_id` (UUID), `table_name` (VARCHAR), `action` (VARCHAR - `INSERT`/`UPDATE`/`DELETE`), `old_data` (JSONB), `new_data` (JSONB), `executed_by` (VARCHAR), `created_at` (TIMESTAMP).

### 2. Identity & Access Domain
- **`users`**: Identity registration metadata.
  - Columns: `id` (UUID, PK), `email` (VARCHAR, Unique), `password_hash` (VARCHAR), `status` (VARCHAR), `created_at`, `updated_at`.
- **`memberships`**: Connects users to tenants.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `user_id` (UUID, FK to users), `role` (VARCHAR), `created_at`.
- **`api_keys`**: REST tokens for automated integrations.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `user_id` (UUID, FK), `hashed_key` (VARCHAR, Index), `scopes` (VARCHAR), `expires_at`, `created_at`.

### 3. Customer & Consent Domain
- **`customers`**: Unified contact profile.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `email` (VARCHAR, Encrypted), `phone` (VARCHAR, Encrypted), `name` (VARCHAR, Encrypted), `created_at`.
- **`consent_records`**: Compliance opt-in/opt-out registry.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `customer_id` (UUID, FK), `channel` (VARCHAR - e.g., `WHATSAPP`), `status` (VARCHAR - `OPT_IN`/`OPT_OUT`), `source` (VARCHAR), `created_at`, `updated_at`.
- **`tags`** & **`customer_tags`**: Manage labeling queries.
- **`segments`** & **`customer_segments`**: Logical groups mapped via JSON logic rules.

### 4. Integration Hub Domain
- **`connectors`**: Supported integration adapters (e.g. `shopify`, `zoho`, `razorpay`).
- **`integrations`**: Tenant-specific configuration of a connector.
- **`connections`** / **`credentials`**: Securely store OAuth access tokens and HMAC verification secrets (encrypted using KMS keys).

### 5. Workflow Execution Domain
- **`workflow_definitions`**: Workflow logic templates defined in JSON DSL.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `name` (VARCHAR), `trigger_event` (VARCHAR), `definition_json` (JSONB), `status` (VARCHAR), `created_at`.
- **`workflow_executions`**: Tracks individual running runs in Temporal.
  - Columns: `id` (UUID, PK), `tenant_id` (UUID), `workflow_definition_id` (UUID, FK), `status` (VARCHAR - `RUNNING`/`COMPLETED`/`FAILED`), `input_payload` (JSONB), `started_at`, `completed_at`.

---

## C. ClickHouse OLAP Schema (Operational Logs)

Used for high-speed metrics ingestion and Metabase dashboard outputs:

- **`conductor_events`**: Stores raw event payload logs stream.
  ```sql
  CREATE TABLE conductor_events (
      event_id UUID,
      tenant_id UUID,
      domain LowCardinality(String),
      connector LowCardinality(String),
      action LowCardinality(String),
      version String,
      timestamp DateTime64(3),
      payload String
  ) ENGINE = MergeTree()
  ORDER BY (tenant_id, domain, action, timestamp);
  ```
- **Materialized Views**: Aggregates records for near-instant reporting:
  - `workflow_metrics_hourly`: Rollup of running/failed executions.
  - `messaging_metrics_hourly`: Outbound and inbound WhatsApp message metrics.
  - `tenant_metrics_daily`: System consumption totals.

---

## D. Related Pages
- [Implementation Guide](Implementation-Guide)
- [Service Catalog](Service-Catalog)
- [Developer & API Guide](Developer-and-API-Guide)
