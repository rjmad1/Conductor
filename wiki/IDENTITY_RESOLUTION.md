# Identity Resolution

Identity resolution automatically identifies duplicates and resolves them within the same tenant.

## Normalization Process
Normalization formats values before hashing to ensure consistent matching:
- **Email:** Stripped of leading/trailing spaces and lower-cased.
- **Phone:** Non-digits (except leading `+`) are stripped.

## SHA-256 Hashed Matching
- Hashed identifiers are stored in the `customer_identifiers` table.
- A unique index on `(tenant_id, identifier_type, identifier_hash)` prevents duplicates and facilitates O(1) lookups.
- Raw PII is never used or exposed during resolution operations.
