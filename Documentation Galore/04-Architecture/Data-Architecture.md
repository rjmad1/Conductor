# Data Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Technical Layers document, Capability definitions  
**Last Updated:** June 2026

---

## Purpose
Defines the complete data model for the Conductor platform: PostgreSQL schemas, Redis data structures, and data lifecycle policies.

---

## Design Principles

1. **Tenant isolation first:** `tenant_id` on every row, every query
2. **Soft delete preferred:** Use `deleted_at` timestamp instead of physical deletion (except for DPDP erasure requests)
3. **Audit trail:** Critical tables have created_at, updated_at, created_by, updated_by
4. **Schema evolution:** Use Flyway for migrations; no destructive schema changes without a migration plan
5. **Separate operational and analytical data:** Analytics tables are denormalized for query performance

---

## PostgreSQL Schema

### Core: Tenants & Plans

```sql
CREATE TABLE plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL,           -- 'starter', 'growth', 'business', 'enterprise'
    price_inr       INTEGER NOT NULL,               -- Monthly price in paise (₹1499 = 149900)
    message_limit   INTEGER NOT NULL,               -- Monthly message quota
    workflow_limit  INTEGER NOT NULL,               -- Max active workflows
    wa_number_limit INTEGER NOT NULL,               -- Max WhatsApp numbers
    user_limit      INTEGER NOT NULL,               -- Max platform users
    features        JSONB NOT NULL DEFAULT '{}',    -- Feature flags for this plan
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    industry        VARCHAR(50),                    -- 'healthcare', 'retail', 'professional_services', etc.
    plan_id         UUID REFERENCES plans(id),
    status          VARCHAR(20) DEFAULT 'active',   -- 'active', 'trial', 'suspended', 'deleted'
    trial_ends_at   TIMESTAMP,
    timezone        VARCHAR(50) DEFAULT 'Asia/Kolkata',
    currency        VARCHAR(10) DEFAULT 'INR',
    logo_url        VARCHAR(500),
    gst_number      VARCHAR(20),
    billing_email   VARCHAR(200),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP                       -- NULL if active
);

CREATE TABLE subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    plan_id         UUID NOT NULL REFERENCES plans(id),
    status          VARCHAR(20) NOT NULL,           -- 'active', 'cancelled', 'past_due', 'paused'
    starts_at       TIMESTAMP NOT NULL,
    ends_at         TIMESTAMP,
    auto_renew      BOOLEAN DEFAULT true,
    razorpay_sub_id VARCHAR(200),                   -- Razorpay subscription ID
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE usage_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    metric          VARCHAR(50) NOT NULL,           -- 'messages_sent', 'workflows_executed'
    quantity        INTEGER NOT NULL,
    recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    billing_period  VARCHAR(7) NOT NULL             -- 'YYYY-MM'
);

CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    amount_paise    INTEGER NOT NULL,
    gst_amount_paise INTEGER NOT NULL,
    status          VARCHAR(20) DEFAULT 'pending',  -- 'pending', 'paid', 'overdue', 'void'
    due_date        DATE NOT NULL,
    paid_at         TIMESTAMP,
    invoice_number  VARCHAR(50) UNIQUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_usage_records_tenant_period ON usage_records(tenant_id, billing_period);
```

---

### IAM: Users

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    email           VARCHAR(200) NOT NULL,
    name            VARCHAR(200),
    role            VARCHAR(20) NOT NULL,           -- 'OWNER', 'ADMIN', 'MANAGER', 'AGENT', 'ANALYST'
    status          VARCHAR(20) DEFAULT 'active',   -- 'active', 'invited', 'deactivated'
    keycloak_id     VARCHAR(200) UNIQUE,            -- Keycloak user ID
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, email)
);

CREATE TABLE api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(100) NOT NULL,
    key_hash        VARCHAR(200) NOT NULL UNIQUE,   -- Hashed API key (never store plain)
    scopes          JSONB DEFAULT '[]',             -- ['read:customers', 'write:workflows']
    last_used_at    TIMESTAMP,
    expires_at      TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at      TIMESTAMP
);
```

---

### Customer Registry

```sql
CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    phone           VARCHAR(20) NOT NULL,
    name            VARCHAR(200),
    email           VARCHAR(200),
    tags            TEXT[] DEFAULT '{}',
    custom_attributes JSONB DEFAULT '{}',
    channel_preferences JSONB DEFAULT '{"primary": "whatsapp"}',
    wa_opt_in_status VARCHAR(20) DEFAULT 'not_set', -- 'opted_in', 'opted_out', 'not_set'
    wa_opt_in_date  TIMESTAMP,
    wa_opt_out_date TIMESTAMP,
    last_interaction_at TIMESTAMP,
    source          VARCHAR(50),                   -- 'csv_import', 'api', 'manual', 'whatsapp_inbound'
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP,
    UNIQUE(tenant_id, phone)
);

CREATE TABLE consent_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL REFERENCES customers(id),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    channel         VARCHAR(20) NOT NULL,           -- 'whatsapp', 'sms', 'email'
    status          VARCHAR(20) NOT NULL,           -- 'opted_in', 'opted_out'
    consent_type    VARCHAR(20) DEFAULT 'marketing',
    collected_via   VARCHAR(50),                   -- 'form', 'whatsapp_reply', 'import', 'api'
    consent_text    TEXT,
    ip_address      VARCHAR(45),
    collected_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    withdrawn_at    TIMESTAMP
);

CREATE TABLE segments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    type            VARCHAR(20) DEFAULT 'static',  -- 'static' (tag-based) or 'dynamic' (query-based)
    conditions      JSONB,                          -- Dynamic segment condition definition
    customer_count  INTEGER DEFAULT 0,
    last_computed   TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_tenant_phone ON customers(tenant_id, phone);
CREATE INDEX idx_customers_tenant_tags ON customers USING gin(tags);
CREATE INDEX idx_customers_tenant_opt_in ON customers(tenant_id, wa_opt_in_status);
```

---

### Workflow Engine

```sql
CREATE TABLE workflows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    status          VARCHAR(20) DEFAULT 'inactive', -- 'active', 'inactive', 'draft', 'archived'
    trigger_config  JSONB NOT NULL,                 -- Trigger definition
    conditions      JSONB DEFAULT '[]',             -- Condition array
    actions         JSONB NOT NULL DEFAULT '[]',    -- Action array
    execution_limits JSONB DEFAULT '{"max_per_customer_per_day": 50}',
    version         INTEGER DEFAULT 1,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    activated_at    TIMESTAMP,
    deactivated_at  TIMESTAMP
);

CREATE TABLE workflow_executions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id     UUID NOT NULL REFERENCES workflows(id),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    customer_id     UUID REFERENCES customers(id),
    trigger_event   JSONB,                          -- The event that triggered this execution
    status          VARCHAR(20) DEFAULT 'running',  -- 'running', 'completed', 'failed', 'cancelled'
    steps_executed  JSONB DEFAULT '[]',             -- Trace of each step with outcome
    error_message   TEXT,
    temporal_run_id VARCHAR(200),
    started_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    duration_ms     INTEGER
);

CREATE INDEX idx_workflows_tenant_status ON workflows(tenant_id, status);
CREATE INDEX idx_executions_workflow ON workflow_executions(workflow_id, started_at DESC);
CREATE INDEX idx_executions_customer ON workflow_executions(customer_id, started_at DESC);
```

---

### Messaging

```sql
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    customer_id     UUID REFERENCES customers(id),
    conversation_id UUID,
    direction       VARCHAR(10) NOT NULL,            -- 'inbound', 'outbound'
    channel         VARCHAR(20) DEFAULT 'whatsapp',
    message_type    VARCHAR(20) NOT NULL,            -- 'text', 'template', 'media', 'interactive'
    content         JSONB NOT NULL,                  -- Message body/payload
    template_id     UUID,                            -- If template message
    wa_message_id   VARCHAR(200),                    -- Meta's message ID
    status          VARCHAR(20) DEFAULT 'sent',      -- 'sent', 'delivered', 'read', 'failed'
    status_updated_at TIMESTAMP,
    error_code      VARCHAR(50),
    error_message   TEXT,
    workflow_execution_id UUID REFERENCES workflow_executions(id),
    campaign_id     UUID,
    sent_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE conversations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    customer_id     UUID NOT NULL REFERENCES customers(id),
    status          VARCHAR(20) DEFAULT 'active',    -- 'active', 'agent_assigned', 'resolved'
    assigned_agent_id UUID REFERENCES users(id),
    workflow_state  JSONB,                            -- Current state in multi-step flow (also in Redis)
    last_message_at TIMESTAMP,
    opened_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMP
);

CREATE INDEX idx_messages_tenant_sent ON messages(tenant_id, sent_at DESC);
CREATE INDEX idx_messages_customer ON messages(customer_id, sent_at DESC);
CREATE INDEX idx_conversations_tenant_status ON conversations(tenant_id, status);
```

---

### Templates

```sql
CREATE TABLE templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(50) NOT NULL,            -- 'MARKETING', 'UTILITY', 'AUTHENTICATION'
    language        VARCHAR(10) DEFAULT 'en',
    status          VARCHAR(20) DEFAULT 'pending',   -- 'pending', 'approved', 'rejected', 'disabled'
    body_text       TEXT NOT NULL,
    variables       JSONB DEFAULT '[]',              -- ['customer_name', 'appointment_time']
    meta_template_id VARCHAR(200),                   -- Template ID from Meta
    approval_reason TEXT,                            -- If rejected, Meta's reason
    submitted_at    TIMESTAMP,
    approved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

### Connectors & Integrations

```sql
CREATE TABLE connector_configs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    connector_id    VARCHAR(50) NOT NULL,            -- 'shopify', 'zoho_crm', 'razorpay', 'google_calendar'
    status          VARCHAR(20) DEFAULT 'active',    -- 'active', 'error', 'disconnected'
    credentials     JSONB NOT NULL,                  -- Encrypted OAuth tokens or API keys
    config          JSONB DEFAULT '{}',              -- Connector-specific config (shop domain, etc.)
    last_sync_at    TIMESTAMP,
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, connector_id)
);

CREATE TABLE whatsapp_numbers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    phone_number    VARCHAR(20) NOT NULL,
    display_name    VARCHAR(200),
    waba_id         VARCHAR(200),                    -- WhatsApp Business Account ID
    phone_number_id VARCHAR(200),                    -- Meta phone number ID for API calls
    status          VARCHAR(20) DEFAULT 'active',
    access_token    TEXT,                            -- Encrypted
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

### Audit Log

```sql
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    actor_type      VARCHAR(20) NOT NULL,            -- 'user', 'system', 'api_key'
    actor_id        VARCHAR(200),
    action          VARCHAR(100) NOT NULL,           -- 'workflow.activated', 'customer.deleted', etc.
    resource_type   VARCHAR(50),
    resource_id     VARCHAR(200),
    old_value       JSONB,
    new_value       JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- Partition by month for retention management
CREATE TABLE audit_logs_2026_01 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
```

---

## Redis Data Structures

| Key Pattern | Type | TTL | Purpose |
|---|---|---|---|
| `session:{userId}` | Hash | 24h | User session data |
| `conv:{tenantId}:{phone}` | JSON | 24h | Active conversation state |
| `freq_cap:{tenantId}:{customerId}:{date}` | Counter | 48h | Frequency cap (max 1 marketing msg/day) |
| `ratelimit:{tenantId}:messages` | Counter | 1s | API rate limiting |
| `feature_flags:{tenantId}` | Hash | 5m | Cached feature flags |
| `wa_rate:{phoneNumberId}` | Counter | 1s | WhatsApp sending rate limiter |
| `workflow_lock:{workflowId}:{customerId}` | String | 30min | Prevent duplicate workflow execution |

---

## Data Lifecycle & Retention

| Data Type | Retention | Deletion Method |
|---|---|---|
| Active tenant data | Indefinite while active | N/A |
| Deleted tenant data | 60 days after deletion | Hard delete |
| Messages | 2 years | Archive to cold storage after 1 year |
| Audit logs | 5 years (compliance) | Partition drop |
| Conversation state (Redis) | 24h TTL | Auto-expire |
| Usage records | 7 years (tax compliance) | Archive |
| Customer PII (on erasure request) | 30 days after request | Anonymize/delete |

---

## DPDP India Compliance Notes
- Customer PII fields: phone, name, email — must be encrypted at rest (AES-256 column-level or disk encryption)
- Data deletion: must execute within 30 days of verified erasure request
- Consent records must be immutable (append-only) — do NOT delete consent records on opt-out, add a withdrawal record
- Data residency: India (recommended) — explicitly document your cloud region

---

## Cross-References
- `05-Engineering/Schema-Definitions.md` — Full SQL DDL scripts
- `04-Architecture/Security-Architecture.md` — Encryption, access controls on data
- `07-Governance/Compliance.md` — DPDP, GDPR data handling requirements
