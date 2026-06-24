# Domain Model — Conductor IDE Knowledge Pack

**Purpose:** Complete entity definitions for engineers and AI coding agents.

---

## Entity Relationship Overview

```
Tenant ←──── Users (many)
Tenant ←──── WhatsApp Numbers (many)
Tenant ←──── Customers (many)
Tenant ←──── Workflows (many)
Tenant ←──── Templates (many)
Tenant ←──── Connector Configs (many)
Tenant ←──── Campaigns (many)
Tenant ←──── Subscriptions (1-active)

Customer ←──── Consent Records (many, append-only)
Customer ←──── Messages (many)
Customer ←──── Conversations (many)
Customer ←──── Workflow Executions (many)

Workflow ─────► Workflow Executions (many)
```

---

## Entity: Tenant

**Represents:** A business customer of Conductor (1 company = 1 tenant)

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `name` | String | Business display name |
| `email` | String | Primary contact email (unique) |
| `industry` | Enum | healthcare, retail, professional_services, education, real_estate |
| `plan_id` | UUID → Plan | Current subscription plan |
| `status` | Enum | trial, active, suspended, deleted |
| `trial_ends_at` | DateTime | Null if not on trial |
| `timezone` | String | Default: Asia/Kolkata |
| `currency` | String(3) | Default: INR |
| `gst_number` | String | For B2B invoice compliance |
| `keycloak_realm` | String | Keycloak realm ID for this tenant |

**Business rules:**
- A new tenant always starts in `trial` status
- Trial expires after 14 days
- `deleted` tenants have data retained for 60 days then purged

---

## Entity: Customer

**Represents:** An end customer (person) of a Conductor tenant's business

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `tenant_id` | UUID | Owning tenant (mandatory) |
| `phone` | String | E.164 format (+91XXXXXXXXXX), unique per tenant |
| `name` | String | Optional |
| `email` | String | Optional |
| `tags` | String[] | Free-form labels |
| `custom_attributes` | JSONB | Tenant-defined key-value attributes |
| `wa_opt_in_status` | Enum | opted_in, opted_out, not_set |
| `wa_opt_in_date` | DateTime | When they opted in |
| `wa_opt_out_date` | DateTime | When they opted out (STOP) |
| `last_interaction_at` | DateTime | Last message (either direction) |

**Business rules:**
- `phone` is the primary identifier (deduplicated per tenant)
- Marketing messages MUST NOT be sent if `wa_opt_in_status != 'opted_in'`
- STOP reply → immediately set `wa_opt_in_status = 'opted_out'`
- Customer can only be accessed by their owning tenant (tenant isolation)

---

## Entity: Workflow

**Represents:** An automation rule (Trigger → Conditions → Actions)

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `tenant_id` | UUID | Owning tenant |
| `name` | String | Business-friendly display name |
| `status` | Enum | draft, inactive, active, archived |
| `trigger_config` | JSONB | Trigger definition |
| `conditions` | JSONB[] | Array of condition objects |
| `actions` | JSONB[] | Array of action objects |
| `execution_limits` | JSONB | e.g., max_per_customer_per_day: 50 |
| `version` | Integer | Incremented on each update |

**Trigger config shape:**
```json
{
  "type": "event|schedule|manual|webhook",
  "event_name": "order.created",     // if type=event
  "source": "shopify",               // if type=event
  "delay_minutes": 30,               // optional delay after event
  "cron": "0 9 * * 1-5"             // if type=schedule
}
```

**Condition shape:**
```json
{ "field": "cart.value", "operator": "gt", "value": 500 }
```

**Action shapes:**
```json
{ "type": "send_whatsapp_template", "template_id": "...", "variables": {} }
{ "type": "delay", "minutes": 30 }
{ "type": "branch", "condition": {...}, "if_true": [...], "if_false": [...] }
{ "type": "update_customer", "field": "...", "value": "..." }
{ "type": "add_tag", "tag": "..." }
{ "type": "trigger_api", "url": "...", "method": "POST", "body": {} }
```

**Business rules:**
- Only `active` workflows receive events and execute
- `execution_limits.max_per_customer_per_day` prevents spam (default: 50)
- Version is incremented on any field change; executions reference the version at trigger time

---

## Entity: Workflow Execution

**Represents:** One instance of a workflow running for one customer

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `workflow_id` | UUID | Which workflow |
| `tenant_id` | UUID | Owning tenant |
| `customer_id` | UUID | Target customer |
| `status` | Enum | running, completed, failed, cancelled, timeout |
| `trigger_event_type` | String | What event triggered this |
| `steps_executed` | JSONB[] | Trace of each step |
| `temporal_workflow_id` | String | Temporal run ID for lookup |
| `started_at` | DateTime | |
| `completed_at` | DateTime | |
| `duration_ms` | Integer | |

---

## Entity: Message

**Represents:** A single WhatsApp message (sent or received)

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `tenant_id` | UUID | |
| `customer_id` | UUID | |
| `direction` | Enum | inbound, outbound |
| `message_type` | Enum | text, template, media, interactive |
| `content` | JSONB | Message body (type-dependent structure) |
| `wa_message_id` | String | Meta's message ID (wamid.XXXX) |
| `status` | Enum | pending, sent, delivered, read, failed |
| `workflow_execution_id` | UUID | If sent as part of a workflow |
| `campaign_id` | UUID | If sent as part of a campaign |

**Billing note:** `billing-service` counts `messages` where `direction=outbound` per billing period.

---

## Entity: Consent Record

**Represents:** An immutable record of a consent event (opt-in or opt-out)

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `customer_id` | UUID | |
| `tenant_id` | UUID | |
| `channel` | Enum | whatsapp, sms, email |
| `status` | Enum | opted_in, opted_out |
| `consent_type` | String | marketing, transactional |
| `collected_via` | String | form, whatsapp_reply, csv_import, api |
| `consent_text` | Text | Exact consent text shown to customer |
| `created_at` | DateTime | Immutable timestamp |

**CRITICAL:** Consent records are APPEND-ONLY. Never UPDATE or DELETE consent records. Read the latest record per customer/channel to determine current status.

---

## Entity: Template

**Represents:** A WhatsApp Business message template

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `tenant_id` | UUID | |
| `name` | String | Internal name |
| `category` | Enum | MARKETING, UTILITY, AUTHENTICATION |
| `language` | String | en, hi, te, ta, etc. |
| `status` | Enum | draft, pending, approved, rejected, disabled |
| `body_text` | Text | Template body with {{1}}, {{2}} variables |
| `variables` | String[] | Variable names in order |
| `meta_template_id` | String | Meta's template ID after approval |

**Business rules:**
- Only `approved` templates can be used in workflows and campaigns
- Cannot update an `approved` template — must create new version
- Meta approval takes 1-3 business days

---

## Entity: Connector Config

**Represents:** A connected external integration for a tenant

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `tenant_id` | UUID | |
| `connector_id` | String | shopify, zoho_crm, razorpay, google_calendar |
| `status` | Enum | active, error, disconnected |
| `credentials` | JSONB (encrypted) | OAuth tokens or API keys |
| `config` | JSONB | Connector-specific config (e.g., shop domain) |

**Security:** `credentials` field is encrypted at rest. Never return credentials in API responses.

---

## Event Subjects (NATS)

```
conductor.{tenantId}.customer.created
conductor.{tenantId}.customer.opted_out
conductor.{tenantId}.order.created          (from Shopify)
conductor.{tenantId}.cart.abandoned         (from Shopify)
conductor.{tenantId}.payment.completed      (from Razorpay)
conductor.{tenantId}.appointment.created    (from Google Calendar)
conductor.{tenantId}.message.inbound
conductor.{tenantId}.message.sent
conductor.{tenantId}.message.delivered
conductor.{tenantId}.message.failed
conductor.{tenantId}.workflow.triggered
conductor.{tenantId}.workflow.completed
conductor.{tenantId}.workflow.failed
```

---

## RBAC Permission Matrix

| Permission | OWNER | ADMIN | MANAGER | AGENT | ANALYST |
|---|---|---|---|---|---|
| billing.* | ✅ | ❌ | ❌ | ❌ | ❌ |
| users.manage | ✅ | ❌ | ❌ | ❌ | ❌ |
| workflows.* | ✅ | ✅ | ✅ | ❌ | ❌ |
| customers.write | ✅ | ✅ | ✅ | ❌ | ❌ |
| customers.read | ✅ | ✅ | ✅ | ✅ | ✅ |
| conversations.* | ✅ | ✅ | ✅ | ✅ | ❌ |
| analytics.read | ✅ | ✅ | ✅ | ❌ | ✅ |
| connectors.* | ✅ | ✅ | ❌ | ❌ | ❌ |
