# Platform Capabilities — Conductor

**Status:** Extracted & Extended  
**Source:** Technical Layers, Initiative Brief, Business Needs  
**Last Updated:** June 2026

---

## Purpose
Defines every platform capability: what it does, its components, MVP inclusion, and specification requirements.

---

## The 15 Core Platform Capabilities

### Capability 1: Tenant Management

**What it does:** Enables multi-business isolation. Each SMB customer is a tenant with their own data, configurations, users, and billing.

**Components:**
- Tenant provisioning (onboarding flow)
- Business profile: name, industry, timezone, currency, logo
- Subscription plan assignment
- Feature flag management per plan
- Tenant-scoped data isolation
- Tenant suspension / termination workflows

**Configuration schema:**
```json
{
  "tenant_id": "uuid",
  "name": "Dr. Sunita's Clinic",
  "industry": "healthcare",
  "plan": "growth",
  "timezone": "Asia/Kolkata",
  "currency": "INR",
  "wa_numbers": ["91XXXXXXXXXX"],
  "status": "active",
  "created_at": "2026-01-15T10:00:00Z"
}
```

**MVP:** Yes  
**Dependency:** All other capabilities depend on this

---

### Capability 2: User & Access Management (IAM)

**What it does:** Authenticates users, enforces role-based access, supports multi-user teams within a tenant.

**Roles:**
- `OWNER` — Full access, billing, user management
- `ADMIN` — Configuration access, no billing
- `MANAGER` — Workflow management, analytics
- `AGENT` — Conversation inbox, no configuration access
- `ANALYST` — Read-only analytics access

**Components:**
- Login (email/password + OTP)
- SSO (Google, future)
- Role assignment and management
- Team creation (Sales, Support, Operations)
- Permission matrix enforcement
- Session management
- API key management for integrations

**MVP:** Yes (Keycloak-based)  
**Technology:** Keycloak (Apache 2.0)

---

### Capability 3: Customer Registry

**What it does:** Stores and manages the canonical customer record for every contact of a tenant's business.

**This is NOT a full CRM. It is a Customer Registry.**

**Customer Record:**
```json
{
  "id": "uuid",
  "tenant_id": "uuid",
  "phone": "+91XXXXXXXXXX",
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "tags": ["vip", "healthcare"],
  "segments": ["high-value-patient"],
  "consents": {
    "whatsapp": { "status": "opted_in", "date": "2026-01-10" },
    "email": { "status": "not_set" }
  },
  "channel_preferences": { "primary": "whatsapp" },
  "custom_attributes": { "doctor": "Dr. Sunita", "patient_id": "P-1234" },
  "last_interaction": "2026-06-01T14:30:00Z",
  "created_at": "2026-01-10T09:00:00Z"
}
```

**Components:**
- Contact CRUD
- Import (CSV, API)
- Manual tagging and segmentation
- Custom attribute management
- Consent tracking
- Contact merge (deduplication)
- Contact history view (conversations, workflows triggered)

**MVP:** Yes  
**Technology:** PostgreSQL (custom service)

---

### Capability 4: Workflow Engine

**What it does:** The heart of the platform. Executes automation rules in the form Trigger → Condition(s) → Action(s).

**Core Concept (Everything is configuration):**
```json
{
  "workflow_id": "wf-001",
  "tenant_id": "t-001",
  "name": "Abandoned Cart Recovery",
  "status": "active",
  "trigger": {
    "type": "event",
    "event_name": "cart.abandoned",
    "source": "shopify",
    "delay_minutes": 30
  },
  "conditions": [
    { "field": "cart.value", "operator": "gt", "value": 500 },
    { "field": "customer.consents.whatsapp", "operator": "eq", "value": "opted_in" }
  ],
  "actions": [
    {
      "type": "send_whatsapp_template",
      "template_id": "cart_recovery_v1",
      "variables": { "name": "{{customer.name}}", "cart_url": "{{cart.url}}" }
    }
  ],
  "execution_limits": { "max_per_customer_per_day": 1 }
}
```

**Trigger Types:**
- Event-based (from Event Bus)
- Schedule-based (cron)
- Manual (API or button)
- Webhook (external system)

**Condition Operators:** equals, not_equals, greater_than, less_than, contains, starts_with, in_list, is_empty, is_not_empty

**Action Types:**
- `send_whatsapp_template` — Send approved WhatsApp template
- `send_whatsapp_message` — Send free-form message (within 24h window)
- `send_sms` — Send SMS (Phase 2)
- `send_email` — Send email (Phase 2)
- `create_crm_record` — Create lead/opportunity in connected CRM
- `create_support_ticket` — Create ticket in helpdesk
- `assign_agent` — Assign conversation to team/agent
- `generate_invoice` — Trigger invoice generation
- `trigger_api` — Call external webhook/API
- `update_customer` — Update customer record attributes
- `add_tag` — Add tag to customer
- `delay` — Wait N minutes/hours before next action
- `branch` — Conditional branching (if/else)

**MVP:** Yes  
**Technology:** Temporal (workflow execution runtime)

---

### Capability 5: Conversation Engine

**What it does:** Manages interactive multi-turn conversations on WhatsApp (and other channels). Enables menu-based flows, slot filling, and context-aware responses.

**Components:**
- Conversation state machine
- Menu management (numbered responses)
- Context storage (slot filling for data collection)
- Intent detection (keyword + NLU)
- Fallback handling ("I didn't understand, please choose 1, 2, or 3")
- Agent handoff trigger
- Conversation history persistence

**Example state:**
```json
{
  "conversation_id": "conv-001",
  "customer_id": "c-001",
  "workflow": "lead_capture",
  "current_step": "collect_email",
  "collected_data": { "name": "Rohan", "phone": "+91XXXXXXXXXX" },
  "waiting_for": "email",
  "started_at": "2026-06-15T10:00:00Z",
  "last_message_at": "2026-06-15T10:05:00Z"
}
```

**MVP:** Partial (menu-based flows; NLU in Phase 2)  
**Technology:** Custom state machine on Redis + PostgreSQL

---

### Capability 6: WhatsApp Adapter

**What it does:** Abstracts all WhatsApp Business API interactions. Workflows never know which BSP or API version is used.

**Interface contract:**
```java
interface ChannelAdapter {
  void sendTextMessage(String to, String text);
  void sendTemplateMessage(String to, String templateId, Map<String, String> variables);
  void sendMediaMessage(String to, MediaType type, String url);
  void sendInteractiveButtons(String to, String header, String body, List<Button> buttons);
  void sendInteractiveList(String to, String header, String body, List<ListSection> sections);
  ConversationEvent parseWebhook(WebhookPayload payload);
}
```

**WhatsApp-specific implementation uses:** WhatsApp Cloud API (official Meta API)  
**Production requirement:** WhatsApp Business Account (WABA) approved by Meta

**MVP:** Yes  
**Technology:** WhatsApp Cloud API (direct Meta API — NOT unofficial libraries like OpenWA)

> ⚠️ **RISK NOTE:** OpenWA and similar unofficial libraries violate Meta's Terms of Service. For production use, only the official WhatsApp Cloud API or a certified BSP (Gupshup, Twilio, Kaleyra) should be used.

---

### Capability 7: Template Management

**What it does:** Manages WhatsApp message templates through their lifecycle: creation → Meta approval → activation → variable binding → retirement.

**Template record:**
```json
{
  "template_id": "cart_recovery_v1",
  "tenant_id": "t-001",
  "name": "Cart Recovery Reminder",
  "category": "MARKETING",
  "language": "en",
  "status": "APPROVED",
  "body": "Hi {{1}}, you left {{2}} items worth ₹{{3}} in your cart. Complete your purchase: {{4}}",
  "variables": ["customer_name", "item_count", "cart_value", "checkout_url"],
  "approved_at": "2026-01-20T00:00:00Z",
  "meta_template_id": "meta_tpl_xyz123"
}
```

**Components:**
- Template CRUD and preview
- Meta submission workflow
- Approval status tracking
- Variable mapping to workflow data
- Version management
- Multi-language support
- Template performance analytics

**MVP:** Yes (manual Meta submission initially; auto-submit via API in Phase 2)

---

### Capability 8: Campaign Engine

**What it does:** Manages broadcast and drip campaigns to customer segments.

**Campaign types:**
- **Broadcast:** One-time message to a segment (promotions, announcements)
- **Drip:** Timed sequence of messages (onboarding, nurture, re-engagement)
- **Triggered:** Behavior-based (birthday, anniversary, restock)

**Campaign record:**
```json
{
  "campaign_id": "camp-001",
  "name": "Diwali Promotion 2026",
  "type": "broadcast",
  "audience": { "segment_id": "seg-premium-customers" },
  "message": { "template_id": "diwali_promo_v1" },
  "schedule": { "type": "once", "send_at": "2026-10-20T09:00:00Z" },
  "frequency_cap": { "max_per_customer_per_day": 1 },
  "status": "scheduled"
}
```

**MVP:** Broadcast only  
**Phase 2:** Drip campaigns, A/B testing, AI-optimized send time

---

### Capability 9: Integration Hub

**What it does:** Provides the connector framework and manages all external system integrations.

**MVP Connectors:**
- **Shopify:** Orders, customers, carts, products
- **Zoho CRM:** Contacts, leads, deals
- **Razorpay:** Payments, invoices, payment links
- **Google Calendar:** Appointments, availability

**Connector interface:**
```java
interface Connector {
  String getId();                          // "shopify"
  List<TriggerDefinition> getTriggers();   // What events this connector can produce
  List<ActionDefinition> getActions();     // What this connector can do
  void configure(Map<String, String> config);  // OAuth / API keys
  ConnectorEvent parseWebhook(Payload p);  // Convert incoming webhook to platform event
}
```

**MVP:** Yes (Shopify, Zoho CRM, Razorpay)  
**Phase 2:** Connector marketplace, ISV SDK, 20+ connectors

---

### Capability 10: Event Bus

**What it does:** The nervous system of the platform. Every significant action produces an event. Events trigger workflows and enable loose coupling between services.

**Event envelope:**
```json
{
  "event_id": "evt-uuid",
  "tenant_id": "t-001",
  "event_type": "order.created",
  "source": "shopify",
  "timestamp": "2026-06-15T10:30:00Z",
  "payload": { "order_id": "ORD-999", "value": 1299, "customer_phone": "+91XXXXXXXXXX" },
  "schema_version": "1.0"
}
```

**Core event types:**
- `customer.created`, `customer.updated`, `customer.opted_out`
- `lead.created`, `lead.qualified`, `lead.assigned`
- `order.created`, `order.shipped`, `order.delivered`, `order.cancelled`
- `payment.initiated`, `payment.completed`, `payment.failed`
- `appointment.created`, `appointment.confirmed`, `appointment.cancelled`
- `ticket.created`, `ticket.escalated`, `ticket.resolved`
- `workflow.triggered`, `workflow.completed`, `workflow.failed`
- `message.sent`, `message.delivered`, `message.read`, `message.failed`

**MVP:** Yes  
**Technology:** NATS (lightweight, Apache 2.0) for MVP; Kafka for Phase 2 at scale

---

### Capability 11: Analytics Layer

**What it does:** Provides metrics, dashboards, and reports on platform activity.

**MVP Metrics:**
- Messages: Sent, delivered, read, failed, response rate
- Workflows: Triggered, completed, failed, execution time
- Campaigns: Sent, delivery rate, response rate, conversion
- Customers: Total contacts, active, opted-in, opted-out, new
- Revenue: Payments initiated, completed, failed (if Razorpay connected)

**MVP:** Basic dashboards  
**Technology:** Metabase (embedded analytics)

---

### Capability 12: Consent & Compliance

**What it does:** Manages customer opt-in/opt-out consent, required for WhatsApp Business Policy, DPDP India, and GDPR compliance.

**Consent record:**
```json
{
  "customer_id": "c-001",
  "tenant_id": "t-001",
  "channel": "whatsapp",
  "status": "opted_in",
  "consent_type": "marketing",
  "collected_via": "form",
  "consent_text": "I agree to receive WhatsApp messages from Dr. Sunita's Clinic",
  "ip_address": "103.x.x.x",
  "collected_at": "2026-01-10T09:00:00Z",
  "withdrawn_at": null
}
```

**STOP handling:** Any customer replying "STOP", "Unsubscribe", "Stop sending", or similar MUST be immediately opted out. This is a WhatsApp policy requirement.

**MVP:** Yes (mandatory)

---

### Capability 13: Audit Layer

**What it does:** Logs every significant platform action with immutable audit trails.

**Audit record:**
```json
{
  "audit_id": "aud-uuid",
  "tenant_id": "t-001",
  "actor_type": "user",
  "actor_id": "usr-001",
  "action": "workflow.updated",
  "resource_type": "workflow",
  "resource_id": "wf-001",
  "old_value": { "status": "inactive" },
  "new_value": { "status": "active" },
  "timestamp": "2026-06-15T11:00:00Z",
  "ip_address": "103.x.x.x"
}
```

**MVP:** Yes (basic audit logging)  
**Phase 2:** Full audit API, compliance reports, data export

---

### Capability 14: Customer Data Platform (CDP)

**What it does:** Enables dynamic customer segmentation based on behavior, attributes, and events.

**Example segment:**
```json
{
  "segment_id": "seg-001",
  "name": "High-value patients not seen in 90 days",
  "conditions": [
    { "field": "total_appointments", "operator": "gte", "value": 5 },
    { "field": "last_appointment_date", "operator": "lt", "days_ago": 90 }
  ],
  "customer_count": 145,
  "last_computed": "2026-06-15T00:00:00Z"
}
```

**MVP:** Basic (tag-based segments)  
**Phase 2:** Dynamic behavioral segments, real-time membership

---

### Capability 15: AI Layer

**What it does:** Adds AI-powered capabilities to conversations and workflows.

**MVP (Phase 2):**
- Intent detection (classify customer message intent)
- FAQ bot (answer frequently asked questions from knowledge base)
- Sentiment analysis (detect negative sentiment → escalate)

**Phase 3:**
- Lead qualification agent
- Appointment scheduling agent
- Product recommendation engine
- Conversation summarization for agents
- AI-powered workflow suggestions

**Technology:** Dify (LLM workflow platform, open-source)

---

## Capability → MVP Mapping

| Capability | MVP (V1) | Phase 2 | Phase 3 |
|---|---|---|---|
| Tenant Management | ✅ Full | — | — |
| User & IAM | ✅ Full | SSO | — |
| Customer Registry | ✅ Full | Import API | CDP merge |
| Workflow Engine | ✅ Full | Advanced branching | AI-powered |
| Conversation Engine | ✅ Basic menus | NLU, AI intents | Agent AI |
| WhatsApp Adapter | ✅ Full | — | — |
| Template Management | ✅ Full | Auto-submit | A/B testing |
| Campaign Engine | ✅ Broadcast | Drip campaigns | AI optimization |
| Integration Hub | ✅ 3 connectors | 10+ connectors | Marketplace |
| Event Bus | ✅ NATS | Kafka migration | — |
| Analytics | ✅ Basic | Full suite | Predictive |
| Consent & Compliance | ✅ Full | GDPR exports | — |
| Audit Layer | ✅ Basic | Full API | — |
| CDP | ❌ (tag-based only) | ✅ Full | — |
| AI Layer | ❌ | ✅ Intent/FAQ | ✅ Agents |

---

## Cross-References
- `03-Product/Product-Requirements.md` — Detailed requirements per capability
- `04-Architecture/Application-Architecture.md` — Service design per capability
- `05-Engineering/API-Contracts.md` — API definitions
- `05-Engineering/Schema-Definitions.md` — Data model definitions
