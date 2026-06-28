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
