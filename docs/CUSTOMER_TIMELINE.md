# Customer Timeline (Customer 360)

The customer timeline is a chronological log of all interactions and updates regarding a customer's profile.

## Event Types
Defined in `TimelineEventType`:
- `CUSTOMER_CREATED`
- `PROFILE_UPDATED`
- `CONTACT_ADDED`
- `CONTACT_REMOVED`
- `CONSENT_GRANTED`
- `CONSENT_REVOKED`
- `TAG_ASSIGNED`
- `TAG_REMOVED`
- `SEGMENT_ASSIGNED`
- `SEGMENT_REMOVED`
- `CUSTOMER_MERGED`
- `CUSTOMER_ARCHIVED`
- etc.

## Recording Policy
- The timeline is append-only.
- Every major service call records a corresponding timeline entry with service context and custom JSON metadata.
