# Schema Definitions — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Data Architecture, Capability definitions  
**Last Updated:** June 2026

---

## Purpose
Complete SQL DDL for all PostgreSQL tables in the Conductor platform. Use these as the source of truth for schema migrations.

---

## Migration Strategy

- Tool: **Flyway**
- Location: `services/{service-name}/src/main/resources/db/migration/`
- Naming: `V{sequence}__{description}.sql`
- Environment: Each service manages its own schema migrations
- Shared schema tables are in `services/tenant-service/` (as the foundational service)

---

## Complete DDL

### 01: Extensions

```sql
-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Enable full-text search
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Enable vector search (Phase 2 — AI embeddings)
-- CREATE EXTENSION IF NOT EXISTS "vector";
```

---

### 02: Plans & Subscriptions (tenant-service)

```sql
CREATE TABLE plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL UNIQUE,     -- 'starter', 'growth', 'business', 'enterprise'
    name            VARCHAR(100) NOT NULL,
    price_paise     INTEGER NOT NULL,               -- Monthly price (₹1499 = 149900 paise)
    annual_price_paise INTEGER,                     -- Annual price (if different from monthly*12)
    message_limit   INTEGER NOT NULL,               -- Monthly message quota (-1 = unlimited)
    workflow_limit  INTEGER NOT NULL,               -- Max active workflows (-1 = unlimited)
    wa_number_limit INTEGER NOT NULL,               -- Max WhatsApp numbers
    user_limit      INTEGER NOT NULL,               -- Max platform users (-1 = unlimited)
    features        JSONB NOT NULL DEFAULT '{}',    -- { "ai_intent": false, "campaigns": true, ... }
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed data
INSERT INTO plans (code, name, price_paise, message_limit, workflow_limit, wa_number_limit, user_limit, features) VALUES
('trial',      'Free Trial',  0,        500,    5,  1, 3,  '{"campaigns": false, "ai_intent": false, "api_access": false}'),
('starter',    'Starter',     149900,   1000,   3,  1, 2,  '{"campaigns": true, "ai_intent": false, "api_access": false}'),
('growth',     'Growth',      499900,   10000,  20, 2, 10, '{"campaigns": true, "ai_intent": true, "api_access": false}'),
('business',   'Business',    1299900,  50000,  -1, 5, 50, '{"campaigns": true, "ai_intent": true, "api_access": true}'),
('enterprise', 'Enterprise',  -1,       -1,     -1, -1,-1, '{"campaigns": true, "ai_intent": true, "api_access": true}');
```

```sql
CREATE TABLE tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    industry        VARCHAR(50),
    plan_id         UUID REFERENCES plans(id),
    status          VARCHAR(20) DEFAULT 'trial'
                        CHECK (status IN ('trial', 'active', 'suspended', 'deleted')),
    trial_ends_at   TIMESTAMP WITH TIME ZONE,
    timezone        VARCHAR(50) DEFAULT 'Asia/Kolkata',
    currency        CHAR(3) DEFAULT 'INR',
    logo_url        VARCHAR(500),
    gst_number      VARCHAR(20),
    billing_email   VARCHAR(200),
    keycloak_realm  VARCHAR(100) UNIQUE,          -- Keycloak realm name for this tenant
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_tenants_status ON tenants(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenants_email ON tenants(email);
```

```sql
CREATE TABLE subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    plan_id         UUID NOT NULL REFERENCES plans(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('trial', 'active', 'cancelled', 'past_due', 'paused')),
    billing_cycle   VARCHAR(10) DEFAULT 'monthly'
                        CHECK (billing_cycle IN ('monthly', 'annual')),
    starts_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at         TIMESTAMP WITH TIME ZONE,
    auto_renew      BOOLEAN DEFAULT true,
    razorpay_sub_id VARCHAR(200),
    cancelled_at    TIMESTAMP WITH TIME ZONE,
    cancel_reason   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_tenant ON subscriptions(tenant_id, status);
```

```sql
CREATE TABLE usage_records (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    metric          VARCHAR(50) NOT NULL
                        CHECK (metric IN ('messages_sent', 'workflows_executed', 'api_calls')),
    quantity        INTEGER NOT NULL DEFAULT 1,
    recorded_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    billing_period  CHAR(7) NOT NULL              -- Format: YYYY-MM
);

CREATE INDEX idx_usage_tenant_period ON usage_records(tenant_id, billing_period, metric);
```

```sql
CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    invoice_number  VARCHAR(50) UNIQUE NOT NULL,  -- COND-2026-001
    amount_paise    INTEGER NOT NULL,
    gst_amount_paise INTEGER NOT NULL DEFAULT 0,
    total_paise     INTEGER NOT NULL,
    status          VARCHAR(20) DEFAULT 'pending'
                        CHECK (status IN ('pending', 'paid', 'overdue', 'void')),
    due_date        DATE NOT NULL,
    paid_at         TIMESTAMP WITH TIME ZONE,
    razorpay_payment_id VARCHAR(200),
    line_items      JSONB NOT NULL DEFAULT '[]',   -- [ { description, amount_paise } ]
    pdf_url         VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_tenant ON invoices(tenant_id, status);
```

---

### 03: Users & IAM (auth-service)

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    email           VARCHAR(200) NOT NULL,
    name            VARCHAR(200),
    role            VARCHAR(20) NOT NULL
                        CHECK (role IN ('OWNER', 'ADMIN', 'MANAGER', 'AGENT', 'ANALYST')),
    status          VARCHAR(20) DEFAULT 'active'
                        CHECK (status IN ('invited', 'active', 'deactivated')),
    keycloak_user_id VARCHAR(200) UNIQUE,
    invite_token    VARCHAR(200),
    invite_expires  TIMESTAMP WITH TIME ZONE,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, email)
);

CREATE TABLE api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(100) NOT NULL,
    key_prefix      CHAR(8) NOT NULL,             -- First 8 chars (for display: "ck_XXXXXX...")
    key_hash        VARCHAR(200) NOT NULL UNIQUE,  -- bcrypt hash of full key
    scopes          TEXT[] NOT NULL DEFAULT '{}',  -- ['customers:read', 'workflows:write']
    last_used_at    TIMESTAMP WITH TIME ZONE,
    expires_at      TIMESTAMP WITH TIME ZONE,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at      TIMESTAMP WITH TIME ZONE
);
```

---

### 04: Customer Registry (customer-service)

```sql
CREATE TABLE customers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    phone               VARCHAR(20) NOT NULL,        -- E.164 format: +919999999999
    name                VARCHAR(200),
    email               VARCHAR(200),
    tags                TEXT[] DEFAULT '{}',
    custom_attributes   JSONB DEFAULT '{}',
    channel_preferences JSONB DEFAULT '{"primary": "whatsapp"}',
    wa_opt_in_status    VARCHAR(20) DEFAULT 'not_set'
                            CHECK (wa_opt_in_status IN ('opted_in', 'opted_out', 'not_set')),
    wa_opt_in_date      TIMESTAMP WITH TIME ZONE,
    wa_opt_out_date     TIMESTAMP WITH TIME ZONE,
    last_interaction_at TIMESTAMP WITH TIME ZONE,
    source              VARCHAR(50) DEFAULT 'manual',
    lifecycle_stage     VARCHAR(50) DEFAULT 'unknown', -- 'lead', 'customer', 'at_risk', 'churned'
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP WITH TIME ZONE,
    UNIQUE(tenant_id, phone)
);

CREATE INDEX idx_customers_tenant ON customers(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_tenant_phone ON customers(tenant_id, phone);
CREATE INDEX idx_customers_tags ON customers USING gin(tags);
CREATE INDEX idx_customers_opt_in ON customers(tenant_id, wa_opt_in_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_last_interaction ON customers(tenant_id, last_interaction_at DESC);

CREATE TABLE consent_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL REFERENCES customers(id),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    channel         VARCHAR(20) NOT NULL CHECK (channel IN ('whatsapp', 'sms', 'email')),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('opted_in', 'opted_out')),
    consent_type    VARCHAR(20) DEFAULT 'marketing',
    collected_via   VARCHAR(50),
    consent_text    TEXT,
    ip_address      INET,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- IMPORTANT: Consent records are immutable. Never UPDATE or DELETE.
-- Add new rows for changes. Read status from latest record per customer/channel.

CREATE INDEX idx_consent_customer_channel ON consent_records(customer_id, channel, created_at DESC);

CREATE TABLE segments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    type            VARCHAR(20) DEFAULT 'static' CHECK (type IN ('static', 'dynamic')),
    conditions      JSONB,
    customer_count  INTEGER DEFAULT 0,
    last_computed   TIMESTAMP WITH TIME ZONE,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
```

---

### 05: Workflow Engine (workflow-service)

```sql
CREATE TABLE workflows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    status          VARCHAR(20) DEFAULT 'inactive'
                        CHECK (status IN ('active', 'inactive', 'draft', 'archived')),
    trigger_config  JSONB NOT NULL,
    conditions      JSONB DEFAULT '[]',
    actions         JSONB NOT NULL DEFAULT '[]',
    execution_limits JSONB DEFAULT '{"max_per_customer_per_day": 50}',
    version         INTEGER DEFAULT 1,
    template_source VARCHAR(100),              -- If created from a template
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    activated_at    TIMESTAMP WITH TIME ZONE,
    deactivated_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_workflows_tenant_status ON workflows(tenant_id, status);

CREATE TABLE workflow_executions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id         UUID NOT NULL REFERENCES workflows(id),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    customer_id         UUID REFERENCES customers(id),
    trigger_event_type  VARCHAR(100),
    trigger_event_id    VARCHAR(200),          -- External event ID for deduplication
    status              VARCHAR(20) DEFAULT 'running'
                            CHECK (status IN ('running', 'completed', 'failed', 'cancelled', 'timeout')),
    steps_executed      JSONB DEFAULT '[]',    -- [{step, action, status, duration_ms, error?}]
    conditions_result   JSONB,
    error_message       TEXT,
    temporal_workflow_id VARCHAR(200),
    temporal_run_id     VARCHAR(200),
    started_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP WITH TIME ZONE,
    duration_ms         INTEGER
);

CREATE INDEX idx_executions_workflow ON workflow_executions(workflow_id, started_at DESC);
CREATE INDEX idx_executions_customer ON workflow_executions(customer_id, started_at DESC);
CREATE INDEX idx_executions_tenant_status ON workflow_executions(tenant_id, status, started_at DESC);
```

---

### 06: Messaging (whatsapp-adapter + conversation-service)

```sql
CREATE TABLE wa_numbers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    phone_number    VARCHAR(20) NOT NULL,
    display_name    VARCHAR(200),
    waba_id         VARCHAR(200),
    phone_number_id VARCHAR(200) NOT NULL,     -- Meta's phone number ID (used in API calls)
    status          VARCHAR(20) DEFAULT 'active'
                        CHECK (status IN ('active', 'disconnected', 'error')),
    access_token    TEXT,                      -- Encrypted
    token_expires   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE messages (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    customer_id           UUID REFERENCES customers(id),
    conversation_id       UUID,
    direction             CHAR(8) NOT NULL CHECK (direction IN ('inbound', 'outbound')),
    channel               VARCHAR(20) DEFAULT 'whatsapp',
    message_type          VARCHAR(20) NOT NULL,
    content               JSONB NOT NULL,
    template_id           UUID,
    wa_message_id         VARCHAR(200),               -- Meta's message ID
    status                VARCHAR(20) DEFAULT 'sent'
                              CHECK (status IN ('pending', 'sent', 'delivered', 'read', 'failed')),
    status_updated_at     TIMESTAMP WITH TIME ZONE,
    error_code            VARCHAR(50),
    error_message         TEXT,
    workflow_execution_id UUID REFERENCES workflow_executions(id),
    campaign_id           UUID,
    sent_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (sent_at);

-- Partition by month
CREATE TABLE messages_2026_06 PARTITION OF messages
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE INDEX idx_messages_tenant ON messages(tenant_id, sent_at DESC);
CREATE INDEX idx_messages_customer ON messages(customer_id, sent_at DESC);
CREATE INDEX idx_messages_wa_id ON messages(wa_message_id);

CREATE TABLE conversations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    customer_id       UUID NOT NULL REFERENCES customers(id),
    channel           VARCHAR(20) DEFAULT 'whatsapp',
    status            VARCHAR(20) DEFAULT 'active'
                          CHECK (status IN ('active', 'agent_assigned', 'resolved', 'expired')),
    assigned_agent_id UUID REFERENCES users(id),
    workflow_state    JSONB,                          -- Current multi-step flow state
    message_count     INTEGER DEFAULT 0,
    last_message_at   TIMESTAMP WITH TIME ZONE,
    opened_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMP WITH TIME ZONE,
    resolved_by       UUID REFERENCES users(id)
);

CREATE INDEX idx_conversations_tenant_status ON conversations(tenant_id, status);
CREATE INDEX idx_conversations_customer ON conversations(customer_id);
```

---

### 07: Templates (template-service)

```sql
CREATE TABLE templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(50) NOT NULL CHECK (category IN ('MARKETING', 'UTILITY', 'AUTHENTICATION')),
    language        VARCHAR(10) DEFAULT 'en',
    status          VARCHAR(20) DEFAULT 'draft'
                        CHECK (status IN ('draft', 'pending', 'approved', 'rejected', 'disabled')),
    body_text       TEXT NOT NULL,
    header_text     TEXT,
    footer_text     TEXT,
    buttons         JSONB DEFAULT '[]',              -- CTA buttons config
    variables       TEXT[] DEFAULT '{}',             -- ['customer_name', 'appointment_time']
    meta_template_id VARCHAR(200),
    meta_template_name VARCHAR(200),
    approval_reason TEXT,
    submitted_at    TIMESTAMP WITH TIME ZONE,
    approved_at     TIMESTAMP WITH TIME ZONE,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_templates_tenant_status ON templates(tenant_id, status);
```

---

### 08: Audit Log (audit-service / shared)

```sql
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,                   -- No FK (logs must survive tenant deletion)
    actor_type      VARCHAR(20) NOT NULL CHECK (actor_type IN ('user', 'system', 'api_key')),
    actor_id        VARCHAR(200),
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(50),
    resource_id     VARCHAR(200),
    old_value       JSONB,
    new_value       JSONB,
    ip_address      INET,
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

CREATE TABLE audit_logs_2026_q2 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-04-01') TO ('2026-07-01');

CREATE INDEX idx_audit_tenant_resource ON audit_logs(tenant_id, resource_type, resource_id);
CREATE INDEX idx_audit_tenant_action ON audit_logs(tenant_id, action, created_at DESC);
```

---

## DDL File Locations

```
services/tenant-service/src/main/resources/db/migration/
  V001__create_plans.sql
  V002__seed_plans.sql
  V003__create_tenants.sql
  V004__create_subscriptions.sql
  V005__create_usage_records.sql
  V006__create_invoices.sql

services/auth-service/src/main/resources/db/migration/
  V001__create_users.sql
  V002__create_api_keys.sql

services/customer-service/src/main/resources/db/migration/
  V001__create_customers.sql
  V002__create_consent_records.sql
  V003__create_segments.sql

services/workflow-service/src/main/resources/db/migration/
  V001__create_workflows.sql
  V002__create_workflow_executions.sql
  ...
```

---

## Cross-References
- `04-Architecture/Data-Architecture.md` — Data model rationale and lifecycle
- `05-Engineering/API-Contracts.md` — API contracts built on these schemas
- `07-Governance/Compliance.md` — DPDP data handling requirements
