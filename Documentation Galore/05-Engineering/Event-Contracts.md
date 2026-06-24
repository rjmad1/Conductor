# Event Contracts — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Technical Layers, Capability definitions  
**Last Updated:** June 2026

---

## Purpose
Defines all events published to the NATS event bus: their schemas, subjects, publishers, and consumers.

---

## Event Envelope (Universal)

All events share this envelope structure:

```json
{
  "event_id": "evt-uuid",
  "event_type": "order.created",
  "tenant_id": "t-uuid",
  "source": "shopify",
  "schema_version": "1.0",
  "occurred_at": "2026-06-15T10:30:00Z",
  "ingested_at": "2026-06-15T10:30:01Z",
  "payload": { ... }
}
```

**Field rules:**
- `event_id`: UUID v4, unique per event
- `event_type`: dot-notation, always `{domain}.{action}` (e.g., `order.created`)
- `source`: originating system (`shopify`, `razorpay`, `conductor`, `google_calendar`)
- `occurred_at`: when the event happened in the source system (ISO 8601)
- `ingested_at`: when Conductor received/created the event

---

## NATS Subject Naming

```
conductor.{tenantId}.{domain}.{action}

Examples:
conductor.t-001.order.created
conductor.t-001.message.delivered
conductor.t-001.workflow.completed
```

**Wildcard subscriptions:**
- `conductor.t-001.*` — All events for a specific tenant
- `conductor.*.message.inbound` — All inbound messages across all tenants (platform admin use only)

---

## Domain: Customer Events

### `customer.created`
**Publisher:** customer-service  
**Consumers:** analytics-service, workflow-service (triggers)

```json
{
  "event_type": "customer.created",
  "source": "conductor",
  "payload": {
    "customer_id": "c-uuid",
    "phone": "+919999999999",
    "name": "Priya Sharma",
    "source": "csv_import",
    "wa_opt_in_status": "opted_in"
  }
}
```

### `customer.updated`
**Publisher:** customer-service  
**Consumers:** analytics-service, workflow-service

```json
{
  "event_type": "customer.updated",
  "payload": {
    "customer_id": "c-uuid",
    "changed_fields": ["name", "email"],
    "previous_values": { "name": "Priya" },
    "new_values": { "name": "Priya Sharma" }
  }
}
```

### `customer.opted_out`
**Publisher:** customer-service (triggered by STOP keyword or manual opt-out)  
**Consumers:** workflow-service (must pause active workflows), analytics-service

```json
{
  "event_type": "customer.opted_out",
  "payload": {
    "customer_id": "c-uuid",
    "channel": "whatsapp",
    "trigger": "stop_keyword",
    "message_received": "STOP"
  }
}
```

### `customer.opted_in`
**Publisher:** customer-service  
**Consumers:** analytics-service, workflow-service (may trigger welcome sequence)

---

## Domain: Messaging Events

### `message.inbound`
**Publisher:** whatsapp-adapter  
**Consumers:** conversation-service, analytics-service

```json
{
  "event_type": "message.inbound",
  "source": "whatsapp",
  "payload": {
    "message_id": "msg-uuid",
    "wa_message_id": "wamid.XXXX",
    "from_phone": "+919999999999",
    "to_phone_number_id": "wa-number-id",
    "message_type": "text",
    "content": {
      "text": "Hello, I'd like to book an appointment"
    },
    "customer_id": "c-uuid",
    "timestamp": "2026-06-15T10:30:00Z"
  }
}
```

### `message.sent`
**Publisher:** whatsapp-adapter (after successful API call to Meta)  
**Consumers:** analytics-service, billing-service (usage tracking)

```json
{
  "event_type": "message.sent",
  "payload": {
    "message_id": "msg-uuid",
    "wa_message_id": "wamid.XXXX",
    "to_phone": "+919999999999",
    "message_type": "template",
    "template_id": "appt_reminder_24h",
    "workflow_execution_id": "exec-uuid",
    "campaign_id": null
  }
}
```

### `message.delivered`
**Publisher:** whatsapp-adapter (on Meta delivery webhook)

```json
{
  "event_type": "message.delivered",
  "payload": {
    "wa_message_id": "wamid.XXXX",
    "to_phone": "+919999999999",
    "delivered_at": "2026-06-15T10:30:05Z"
  }
}
```

### `message.read`
**Publisher:** whatsapp-adapter (on Meta read receipt)

### `message.failed`
**Publisher:** whatsapp-adapter  
**Consumers:** workflow-service (handle failure), analytics-service

```json
{
  "event_type": "message.failed",
  "payload": {
    "wa_message_id": "wamid.XXXX",
    "to_phone": "+919999999999",
    "error_code": "131026",
    "error_message": "Message undeliverable — recipient is not on WhatsApp",
    "workflow_execution_id": "exec-uuid"
  }
}
```

---

## Domain: Workflow Events

### `workflow.triggered`
**Publisher:** workflow-service  
**Consumers:** analytics-service

```json
{
  "event_type": "workflow.triggered",
  "payload": {
    "workflow_id": "wf-uuid",
    "execution_id": "exec-uuid",
    "customer_id": "c-uuid",
    "trigger_event_type": "appointment.created",
    "triggered_at": "2026-06-15T09:00:00Z"
  }
}
```

### `workflow.completed`
**Publisher:** workflow-service (Temporal worker on completion)

```json
{
  "event_type": "workflow.completed",
  "payload": {
    "workflow_id": "wf-uuid",
    "execution_id": "exec-uuid",
    "customer_id": "c-uuid",
    "duration_ms": 1240,
    "actions_executed": 2
  }
}
```

### `workflow.failed`
**Publisher:** workflow-service  
**Consumers:** analytics-service, notification-service (alert tenant)

```json
{
  "event_type": "workflow.failed",
  "payload": {
    "workflow_id": "wf-uuid",
    "execution_id": "exec-uuid",
    "customer_id": "c-uuid",
    "error_type": "MESSAGE_DELIVERY_FAILED",
    "error_message": "WhatsApp template not approved",
    "failed_at_action": 1
  }
}
```

---

## Domain: Connector Events (Normalized from External Systems)

### `order.created` (from Shopify)
```json
{
  "event_type": "order.created",
  "source": "shopify",
  "payload": {
    "external_id": "shopify_order_12345",
    "order_number": "#1001",
    "customer_phone": "+919999999999",
    "customer_name": "Priya Sharma",
    "customer_email": "priya@example.com",
    "order_value_paise": 129900,
    "currency": "INR",
    "items": [
      { "name": "T-Shirt Blue", "quantity": 2, "price_paise": 64950 }
    ],
    "status": "confirmed",
    "order_url": "https://store.myshopify.com/orders/..."
  }
}
```

### `cart.abandoned` (from Shopify)
```json
{
  "event_type": "cart.abandoned",
  "source": "shopify",
  "payload": {
    "external_id": "shopify_checkout_abc123",
    "customer_phone": "+919999999999",
    "customer_name": "Priya Sharma",
    "cart_value_paise": 129900,
    "item_count": 3,
    "checkout_url": "https://store.myshopify.com/checkouts/...",
    "abandoned_at": "2026-06-15T10:00:00Z"
  }
}
```

### `payment.completed` (from Razorpay)
```json
{
  "event_type": "payment.completed",
  "source": "razorpay",
  "payload": {
    "external_id": "pay_XXXXXXXXXX",
    "order_id": "order_XXXXXXXXXX",
    "customer_phone": "+919999999999",
    "amount_paise": 499900,
    "currency": "INR",
    "payment_method": "upi",
    "paid_at": "2026-06-15T10:30:00Z"
  }
}
```

### `payment.failed` (from Razorpay)
```json
{
  "event_type": "payment.failed",
  "source": "razorpay",
  "payload": {
    "external_id": "pay_XXXXXXXXXX",
    "customer_phone": "+919999999999",
    "amount_paise": 499900,
    "error_code": "BAD_REQUEST_ERROR",
    "error_description": "Payment failed due to insufficient funds"
  }
}
```

### `appointment.created` (from Google Calendar)
```json
{
  "event_type": "appointment.created",
  "source": "google_calendar",
  "payload": {
    "external_id": "google_event_abc123",
    "title": "Dr. Sunita - Consultation",
    "start_time": "2026-06-20T15:00:00+05:30",
    "end_time": "2026-06-20T15:30:00+05:30",
    "customer_phone": "+919999999999",
    "customer_name": "Priya Sharma",
    "calendar_id": "clinic@drsuniita.in"
  }
}
```

---

## Event Schema Versioning

Events include a `schema_version` field. When the payload schema changes:
- Minor changes (adding optional fields): version stays the same
- Breaking changes (removing or renaming fields): bump schema version (e.g., `1.0` → `2.0`)
- Consumers MUST handle unknown fields gracefully (ignore, don't fail)
- Old schema versions supported for 90 days after new version introduction

---

## Event Retention

NATS JetStream configuration:
- Stream: `CONDUCTOR_EVENTS`
- Subjects: `conductor.>`
- Retention: Work queue (messages deleted after all consumers ACK)
- Max age: 7 days (for replay/debugging)
- Storage: File (persistent, not memory-only)

---

## Cross-References
- `04-Architecture/Application-Architecture.md` — Publisher and consumer services
- `04-Architecture/Integration-Architecture.md` — Connector event normalization
- `05-Engineering/API-Contracts.md` — REST API complements event API
