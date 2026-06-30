# Customer Registry & Customer 360 — Implementation Report

## Summary
The Customer Registry and Customer 360 platform has been successfully designed, implemented, and verified in the Conductor project. This represents a complete greenfield implementation of the customer domain, built to adhere to modular boundaries, multi-tenancy rules, and compliance standards.

## 1. Scope and Implementation Details

### Shared Module (`:shared:customer`)
Provides core models, enums, NATS subject constants, and the JPA attribute converter:
- **PII Encryption:** `PiiEncryptedConverter` encrypts first/last name, phone, and email at rest using AES-256-GCM (enforces `CUSTOMER_PII_ENCRYPTION_KEY` env var validation at startup).
- **Domain Constants:** Enums for `CustomerStatus`, `ConsentType`, `ConsentAction`, `ContactType`, `SegmentType`, `TimelineEventType`, and `CustomerEvents` subject routing keys.

### Platform Module (`:platform:customer`)
Built the Spring Boot application structure:
- **Port:** Configured on `8084`.
- **Database Migrations:** Created 10 Flyway migration files (`V001` through `V010`) detailing schemas for customers, contacts, identifiers, consent, tags, segments, timeline, attributes, preferences, relationships, and tsvector full-text search index.
- **Trigger Immutability:** Migrated a PostgreSQL database trigger ensuring `consent_records` can only be appended (blocks `UPDATE` and `DELETE`).
- **Domain Layer:** Entities extending `TenantAwareEntity` for automatic row-level multi-tenant isolation.
- **Repository & Service Layers:** Fully implemented CRUD, normalization, SHA-256 hashing, identity resolution, merge rules, and cached segment counting.
- **API Controllers:** Structured controllers for customer profiles, contacts, consent ledger, tags, segments, and timeline history.

## 2. Test Verification Summary
All 9 test classes have been implemented successfully under `platform/customer/src/test/java/com/conductor/customer/`:
1. `CustomerArchBoundaryTest` - Verifies boundary isolation via ArchUnit rules.
2. `IdentityResolutionServiceTest` - Verifies email/phone normalization and SHA-256 resolution lookups.
3. `ConsentServiceTest` - Validates grant, revoke check, and validation constraints.
4. `CustomerServiceTest` - Tests customer CRUD operations, soft delete, and deactivation.
5. `CustomerMergeServiceTest` - Validates profile merging and relationship migrations.
6. `SegmentServiceTest` - Validates static and tag-based segmentations.
7. `CustomerSearchServiceTest` - Verifies multi-field search and de-duplication.
8. `TenantIsolationTest` - Confirms that cross-tenant queries return a `404 Not Found`.
9. `CustomerTimelineServiceTest` - Verifies append-only activity recording.

## 3. Compliance and Security Architecture
- **Multi-Tenancy:** Auto row-level query filtering based on context tenant ID. Cross-tenant reads hide resource existence by returning a 404 response.
- **Consent Compliance:** Append-only ledger for GDPR and DPDP compliance, validated in code and database layers.
- **PII Integrity:** Purely SHA-256 hash-based resolution checks without exposing or decrypting raw sensitive details.
