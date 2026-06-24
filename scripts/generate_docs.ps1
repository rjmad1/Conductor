# generate_docs.ps1
# Create CONNECTOR_FRAMEWORK.md
$conn_framework = @"
# Connector Framework

Conductor exposes a canonical interface structure `ConnectorAdapter` which maps to third-party CRM, e-commerce, and billing platforms.

## Interface Signature

```java
public interface ConnectorAdapter {
    String getConnectorType();
    String getVersion();
    void connect(UUID tenantId, Map<String, Object> params);
    void disconnect(UUID tenantId);
    boolean testConnection(UUID tenantId, Map<String, Object> credentials);
    Object execute(UUID tenantId, String action, Map<String, Object> payload);
    void subscribe(UUID tenantId, String eventName, String webhookUrl);
    void unsubscribe(UUID tenantId, String eventName);
    void refreshToken(UUID tenantId);
    boolean healthCheck(UUID tenantId);
}
```

## Registry Injection
The registry dynamic scanner auto-binds all instances on boot:
```java
@Component
public class ConnectorRegistry { ... }
```
"@
Set-Content -Path "docs/CONNECTOR_FRAMEWORK.md" -Value $conn_framework -Encoding utf8

# Create SHOPIFY_CONNECTOR.md
$shopify = @"
# Shopify Connector

Integrates with Shopify store resources.

## Supported Operations
- **sync-customers:** Imports store customer registries, Normalizes inputs, and publishes `customer_created` events.
- **sync-orders:** Queries order lists and dispatches NATS events.
- **lookup-product:** Fetches details of target products.
- **lookup-inventory:** Checks inventory quantities.

## Inbound Webhooks
- `orders/create` -> publishes `shopify.order.created`
- `orders/update` -> publishes `shopify.order.updated`
- `customers/create` -> publishes `shopify.customer.created`
- `customers/update` -> publishes `shopify.customer.updated`
"@
Set-Content -Path "docs/SHOPIFY_CONNECTOR.md" -Value $shopify -Encoding utf8

# Create ZOHO_CONNECTOR.md
$zoho = @"
# Zoho CRM Connector

Synchronizes sales entities.

## Supported Operations
- **create-lead:** Submits lead profile details to Zoho CRM.
- **update-lead:** Modifies status or properties of existing Zoho leads.
- **sync-contacts:** Syncs customer contacts.
- **sync-opportunities:** Feeds opportunity milestones.
- **sync-activities:** Logs communication touchpoints.

## Webhooks
- `lead.create` -> publishes `zoho.lead.created`
- `lead.update` -> publishes `zoho.lead.updated`
- `contact.create` -> publishes `zoho.contact.created`
- `contact.update` -> publishes `zoho.contact.updated`
"@
Set-Content -Path "docs/ZOHO_CONNECTOR.md" -Value $zoho -Encoding utf8

# Create RAZORPAY_CONNECTOR.md
$razorpay = @"
# Razorpay Connector

Handles transactional billing cycles.

## Supported Operations
- **create-payment-link:** Generates short urls for payments.
- **lookup-payment-status:** Fetches state captures.
- **track-refund:** Monitors refunded invoices.
- **create-invoice:** Sets up invoices.

## Webhooks
- `payment.created` -> publishes `payment.created`
- `payment.captured` / `payment.completed` -> publishes `payment.completed`
- `payment.failed` -> publishes `payment.failed`
- `refund.created` -> publishes `refund.created`
- `refund.processed` / `refund.completed` -> publishes `refund.completed`
"@
Set-Content -Path "docs/RAZORPAY_CONNECTOR.md" -Value $razorpay -Encoding utf8

# Create WEBHOOK_FRAMEWORK.md
$webhook = @"
# Webhook Ingress Framework

Ingests vendor webhook streams securely.

## Pipeline Lifecycle
1. **Header Resolution:** Ingress parses target tenant boundaries from request parameters.
2. **Signature Verification:** Cryptographic matches verify payloads (Shopify Base64, Razorpay Hex).
3. **Replay Protection:** Deduplication blocks duplicate deliveries.
4. **JetStream Dispatch:** Translates bodies into Conductor event envelopes and dispatches to NATS.
"@
Set-Content -Path "docs/WEBHOOK_FRAMEWORK.md" -Value $webhook -Encoding utf8

# Create CREDENTIAL_MANAGEMENT.md
$cred = @"
# Credential Management

Securely stores API keys, webhook secrets, and OAuth metadata.

## Security Controls
- **AES-256-GCM Encryption:** Column values are encrypted using the dynamic environment key `INTEGRATION_ENCRYPTION_KEY`.
- **Validation Probes:** Validates presence and check connection validity.
- **Auditing:** Rotations or reads trigger standard log audits.
"@
Set-Content -Path "docs/CREDENTIAL_MANAGEMENT.md" -Value $cred -Encoding utf8

# Create TRANSFORMATION_ENGINE.md
$trans = @"
# Transformation Engine

Normalizes vendor JSON payloads.

## Mapping Engine
The engine uses nested dot-notation mappings (e.g. `"email": "customer.email"`) to traverse source payloads, map target shapes, and fill default constants without calling complex external libraries.
"@
Set-Content -Path "docs/TRANSFORMATION_ENGINE.md" -Value $trans -Encoding utf8

# Create INTEGRATION_API.md
$api = @"
# Integration APIs

Exposes administration endpoints.

## Route Catalog
- `GET /api/v1/integrations/connectors` -> Lists connectors.
- `POST /api/v1/integrations/credentials` -> Saves API secrets.
- `POST /api/v1/integrations/oauth/authorize` -> Directs to OAuth login.
- `GET /api/v1/integrations/oauth/callback` -> Handles tokens callback.
- `POST /api/v1/integrations/webhooks` -> Registers subscriptions.
- `POST /api/v1/integrations/{id}/execute` -> Triggers synchronous actions.
- `GET /api/v1/integrations/{id}/history` -> Triggers history logs.
- `GET /api/v1/integrations/{id}/health` -> Triggers health checks.
"@
Set-Content -Path "docs/INTEGRATION_API.md" -Value $api -Encoding utf8

# Create INTEGRATION_EVENTS.md
$events = @"
# Integration Events

Describes JetStream events emitted under the `conductor.*.integration.>` namespace.

## Schema Registry
All events strictly validate against JSON Schemas loaded on boot:
- `integration.integration.created`
- `integration.integration.connected`
- `integration.integration.disconnected`
- `integration.credential.updated`
- `integration.shopify.order_created`
- `integration.shopify.customer_created`
- `integration.zoho.lead_created`
- `integration.zoho.contact_created`
- `integration.razorpay.payment_created`
- `integration.razorpay.payment_completed`
- `integration.razorpay.refund_created`
"@
Set-Content -Path "docs/INTEGRATION_EVENTS.md" -Value $events -Encoding utf8
