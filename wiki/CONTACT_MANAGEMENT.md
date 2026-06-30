# Contact Management

Tracks all communication channels associated with a customer.

## Contact Channels
Supported types defined in `ContactType`:
- `EMAIL`
- `PHONE`
- `WHATSAPP`
- `ADDRESS`
- `SOCIAL`
- `CUSTOM`

## Primary Validation Rules
1. A customer can have multiple contacts of the same type.
2. Only one contact per type can be marked as `isPrimary = true`.
3. Setting a new primary contact of type X automatically resets the previous primary contact of type X to `false`.
4. Values are encrypted at rest using AES-256-GCM.
