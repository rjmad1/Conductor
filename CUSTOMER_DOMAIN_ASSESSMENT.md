# Customer Domain Assessment

## Executive Summary
This assessment establishes the architectural domain boundaries, security controls, and design decisions for the Customer Registry & Customer 360 platform in the Conductor project.

## 1. Domain Scope & Boundaries
The Customer Registry is the canonical source of truth for customer identities, contact channels, consent tracking, preferences, and custom attributes. 

### Data Ownership
- **Owns:**
  - `customers` (profile details: names, status, source)
  - `customer_contacts` (phone numbers, emails, addresses, WhatsApp)
  - `customer_identifiers` (SHA-256 hashes of identifiers for resolution)
  - `consent_records` (append-only ledger of opt-in/opt-out statements)
  - `tags` & `customer_tags` (labels and assignments)
  - `segments` & `customer_segments` (static and dynamic lists)
  - `customer_timeline` (append-only lifecycle event ledger)
  - `customer_attributes` & `customer_preferences`
  - `customer_relationships`
- **Forbidden Boundaries:**
  - Cannot query `platform:identity` tables or records directly.
  - Cannot query `platform:workflow` databases.
  - No direct cross-module database joins.

### Consumer Access Pattern
- **Workflow & Messaging:** Interact strictly via REST APIs and NATS JetStream event subscriptions.
- **AI Engine:** Explicitly forbidden from querying SQL tables directly. Must use APIs.

## 2. Multi-Tenancy & Isolation
- All entities extend `TenantAwareEntity` which injects a `tenant_id` at persist time.
- Row-level isolation is enforced via Hibernate `@FilterDef` + `@Filter`.
- If a user attempts to access a cross-tenant customer, the system returns `404 Not Found` (rather than a 403 Forbidden) to hide resource existence.

## 3. PII & Compliance Architecture
- **DPDP Act §6 / GDPR Compliance:**
  - Consent records are append-only. UPDATE and DELETE actions are blocked by a PostgreSQL DB trigger.
  - Hashed lookup values (`value_hash`) are generated from normalized contact values using SHA-256 to allow O(1) searches without decrypting raw PII.
  - PII (first name, last name, email, phone) is encrypted at rest using AES-256-GCM via a JPA Attribute Converter.

## 4. Scalability Risks & Mitigations
- **Full-Text Search:** PostgreSQL GIN indexes on display names are used. For >10M customers per tenant, external indexing (e.g., Elasticsearch) should be evaluated.
- **Dynamic Segments:** Segment memberships are recomputed in batches or on request to prevent write amplification.
