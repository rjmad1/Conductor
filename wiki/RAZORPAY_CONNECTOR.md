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
