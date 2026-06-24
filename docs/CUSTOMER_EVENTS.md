# Customer Domain Events

The customer registry publishes domain events to NATS JetStream for asynchronous integration.

## Subjects
Subject constants are defined in `CustomerEvents`:
- Domain: `customer`
- Entities: `profile`, `contact`, `consent`, `tag`, `segment`
- Actions: `created`, `updated`, `deleted`, `archived`, `merged`, `granted`, `revoked`, `assigned`, `removed`

Subject pattern: `<domain>.<entity>.<action>`
Examples:
- `customer.profile.created`
- `customer.profile.merged`
- `customer.consent.granted`
- `customer.tag.assigned`

## Payload Strategy
To comply with security and data governance standards, event payloads carry IDs only and exclude any PII.
Example:
```json
{
  "customerId": "8f8303f8-8547-497d-aa74-b52b314954f6",
  "consentType": "MARKETING",
  "version": "v1"
}
```
Consumer services must fetch the details from the REST APIs.
