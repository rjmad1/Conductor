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
