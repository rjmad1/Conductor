# Integration Architecture — Conductor

**Status:** Partially Extracted + Extended (⚡ where inferred)  
**Source:** Technical Layers, MVP Scope  
**Last Updated:** June 2026

---

## Purpose
Defines how Conductor integrates with external systems: WhatsApp Cloud API, business connectors (Shopify, Zoho CRM, Razorpay, Google Calendar), and the connector framework.

---

## Integration Patterns

### Pattern 1: Webhook Inbound (External → Conductor)
External systems post events to Conductor's webhook endpoints.

```
External System → POST /webhooks/{connector_id} → Conductor
1. Validate HMAC signature
2. Parse payload
3. Normalize to Conductor Event Envelope
4. Publish to NATS
5. Return HTTP 200 (immediately — async processing)
```

### Pattern 2: API Pull (Conductor → External)
Conductor fetches data or performs actions in external systems.

```
Temporal Worker → connector-service → External REST API
1. Retrieve OAuth token for tenant
2. Call external API endpoint
3. Handle response / map to internal model
4. Return result to workflow action
```

### Pattern 3: OAuth 2.0 Authorization (Setup)
How a tenant connects their external system to Conductor.

```
Tenant (Browser) → Conductor UI → /auth/connect/{connector_id}
→ Redirect to external OAuth authorization URL
→ User approves in external system
→ External system redirects to Conductor callback URL
→ Conductor exchanges auth code for access + refresh token
→ Store encrypted tokens in connector_configs table
```

---

## Connector 1: WhatsApp Cloud API

**Authentication:** Meta App access token (system user token — long-lived)

**Inbound Webhook:**
```http
POST /webhooks/whatsapp
Content-Type: application/json
X-Hub-Signature-256: sha256={hmac}

{
  "object": "whatsapp_business_account",
  "entry": [{
    "id": "{WABA_ID}",
    "changes": [{
      "value": {
        "messaging_product": "whatsapp",
        "metadata": { "phone_number_id": "{PHONE_NUMBER_ID}" },
        "messages": [{
          "from": "919999999999",
          "id": "wamid.XXXX",
          "timestamp": "1710000000",
          "type": "text",
          "text": { "body": "Hello" }
        }]
      }
    }]
  }]
}
```

**HMAC Validation:**
```java
String computedSignature = "sha256=" + HmacSHA256(appSecret, rawBody);
boolean valid = MessageDigest.isEqual(
    computedSignature.getBytes(),
    requestHeader.getBytes()
);
```

**Outbound: Send Template Message:**
```http
POST https://graph.facebook.com/v18.0/{phone-number-id}/messages
Authorization: Bearer {access_token}

{
  "messaging_product": "whatsapp",
  "recipient_type": "individual",
  "to": "919999999999",
  "type": "template",
  "template": {
    "name": "cart_recovery_v1",
    "language": { "code": "en" },
    "components": [{
      "type": "body",
      "parameters": [
        { "type": "text", "text": "Priya" },
        { "type": "text", "text": "₹1,200" }
      ]
    }]
  }
}
```

**Rate Limits:**
- 1,000 business-initiated conversations per phone number per day (Tier 1)
- 80 messages per second per phone number
- Conductor MUST implement backpressure (queue + rate limiter in Redis)

---

## Connector 2: Shopify

**Authentication:** OAuth 2.0 (recommended) or Private App API key

**OAuth Setup:**
```
Scopes: read_orders, read_products, read_customers, read_checkouts
Callback URL: https://app.conductor.io/auth/callback/shopify
```

**Webhook Events Subscribed:**
```
orders/created     → conductor event: order.created
orders/updated     → conductor event: order.updated
orders/fulfilled   → conductor event: order.shipped
checkouts/create   → conductor event: cart.created
checkouts/update   → conductor event: cart.updated
app/uninstalled    → disconnect connector for tenant
```

**Webhook Registration (Shopify API):**
```http
POST https://{shop}.myshopify.com/admin/api/2024-01/webhooks.json
X-Shopify-Access-Token: {token}

{
  "webhook": {
    "topic": "orders/created",
    "address": "https://app.conductor.io/webhooks/shopify",
    "format": "json"
  }
}
```

**HMAC Validation:** `X-Shopify-Hmac-Sha256` header (base64-encoded HMAC-SHA256)

**Outbound Actions Supported:**
- `get_product_details` — Fetch product by ID
- `get_order_details` — Fetch order by ID
- `get_customer_by_phone` — Find customer record

---

## Connector 3: Razorpay

**Authentication:** Key ID + Key Secret (Basic Auth)

**Webhook Events:**
```
payment.captured   → conductor event: payment.completed
payment.failed     → conductor event: payment.failed
subscription.charged → conductor event: subscription.renewed
subscription.halted  → conductor event: subscription.payment_failed
```

**Webhook Signature Validation:**
```java
String signature = request.getHeader("X-Razorpay-Signature");
String computed = HmacSHA256(webhookSecret, rawBody);
boolean valid = computed.equals(signature);
```

**Outbound Actions:**
```
generate_payment_link:
POST https://api.razorpay.com/v1/payment_links
{
  "amount": 129900,          // paise
  "currency": "INR",
  "description": "Invoice #INV-001",
  "customer": {
    "name": "Priya Sharma",
    "contact": "+919999999999"
  },
  "notify": { "sms": false, "email": false },
  "callback_url": "https://app.conductor.io/payment-complete"
}
```

---

## Connector 4: Google Calendar

**Authentication:** OAuth 2.0

**OAuth Scopes:**
```
https://www.googleapis.com/auth/calendar.readonly
https://www.googleapis.com/auth/calendar.events
```

**Watch / Webhook Setup (Google Push Notifications):**
```http
POST https://www.googleapis.com/calendar/v3/calendars/{calendarId}/events/watch
{
  "id": "{unique_channel_id}",
  "type": "web_hook",
  "address": "https://app.conductor.io/webhooks/google_calendar"
}
```

**On Notification:** Conductor polls calendar API to get the changed event details (Google only sends a notification that something changed, not the event data itself).

**Events Generated:**
- `appointment.created` — New calendar event with patient/customer attendee
- `appointment.updated` — Event time changed
- `appointment.cancelled` — Event deleted or cancelled

---

## Connector Interface (Framework)

All connectors implement this Java interface:

```java
public interface Connector {

    // Identity
    String getId();              // "shopify"
    String getDisplayName();     // "Shopify"
    String getCategory();        // "ecommerce"

    // What events this connector can produce
    List<TriggerDefinition> getSupportedTriggers();

    // What actions this connector can execute
    List<ActionDefinition> getSupportedActions();

    // OAuth setup
    String getAuthorizationUrl(String tenantId, String state);
    ConnectorCredentials exchangeCode(String code, String state);
    ConnectorCredentials refreshToken(ConnectorCredentials current);

    // Inbound webhook processing
    boolean verifyWebhookSignature(HttpRequest request, ConnectorConfig config);
    List<ConnectorEvent> parseWebhook(HttpRequest request, ConnectorConfig config);

    // Outbound action execution
    ActionResult executeAction(String actionType, Map<String, Object> params, ConnectorConfig config);
}

// Event normalized from external system
public record ConnectorEvent(
    String eventType,           // "order.created"
    String externalId,          // Source system's event/resource ID
    Map<String, Object> payload, // Normalized payload
    String tenantId,
    Instant occurredAt
) {}
```

---

## Event Normalization

Every connector event is normalized before publishing to NATS:

```json
{
  "event_id": "evt-uuid",
  "tenant_id": "t-uuid",
  "event_type": "order.created",
  "source": "shopify",
  "occurred_at": "2026-06-15T10:30:00Z",
  "ingested_at": "2026-06-15T10:30:01Z",
  "schema_version": "1.0",
  "payload": {
    "external_id": "shopify_order_12345",
    "customer_phone": "+919999999999",
    "customer_name": "Priya Sharma",
    "order_value": 129900,
    "order_number": "#1001",
    "items": [{ "name": "T-Shirt", "quantity": 2, "price": 64950 }]
  }
}
```

---

## Connector Development Guide (Phase 2 SDK)

For ISV developers building custom connectors:

1. Implement the `Connector` interface
2. Register trigger and action definitions with metadata (types, required params, output schema)
3. Submit for Conductor certification review
4. Once certified, connector appears in the Integration Hub for all tenants

**Connector manifest (`connector.json`):**
```json
{
  "id": "my_connector",
  "display_name": "My SaaS Tool",
  "version": "1.0.0",
  "author": "My Company",
  "category": "crm",
  "logo_url": "https://...",
  "oauth_config": {
    "auth_url": "https://myapp.com/oauth/authorize",
    "token_url": "https://myapp.com/oauth/token",
    "scopes": ["read", "write"]
  },
  "triggers": [...],
  "actions": [...]
}
```

---

## Cross-References
- `04-Architecture/Application-Architecture.md` — connector-service design
- `05-Engineering/API-Contracts.md` — Connector webhook API contracts
- `05-Engineering/Event-Contracts.md` — Normalized event schemas
