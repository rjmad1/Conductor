# Consent Management

The consent service provides a secure audit ledger for user consent, built to comply with data protection regulations.

## Compliance Policy
- **Immutability:** Consent records are strictly append-only. A DB-level trigger blocks `UPDATE` and `DELETE` queries.
- **Auditing:** Every grant or revocation logs an audit event and publishes a NATS event.
- **Policy Versioning:** Records link to specific privacy policies (`consent_version`).

## Status Resolution
The active consent state is determined by fetching the latest record for a given `(customer_id, consent_type)`. If the latest record is `GRANTED`, consent is active.
