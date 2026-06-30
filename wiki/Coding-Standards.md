# Coding Standards

## A. Purpose
This page documents the programming standards, package boundary constraints (ArchUnit rules), cryptographic patterns, and logging styles required for code commits in the Conductor project.

---

## B. Java Coding Style
- **Naming Conventions**:
  - Class Names: PascalCase (`TenantService`, `ClickHouseWriter`).
  - Function Names: camelCase starting with an action verb (`createUser`, `getTenantById`).
  - Constant Names: SCREAMING_SNAKE_CASE (`SYSTEM_TASK_QUEUE`, `NAMESPACE`).
- **Input Verification**:
  - Annotate API request controller objects with Jakarta validation attributes (`@NotBlank`, `@Email`, `@Min`, `@Valid`).
  - Check query boundaries early; throw custom exceptions (e.g. `CustomerNotFoundException`) rather than returning null.

---

## C. Architectural Decoupling (ArchUnit Rules)
To prevent the modular monolith from degenerating into a "spaghetti codebase", strict package import boundaries are validated during testing.

- **Check Rule**: Core service modules are isolated. They are forbidden from directly importing classes from other service packages.
- **Example Constraint**: ArchUnit tests (such as [CustomerArchBoundaryTest.java](file:///c:/Users/rajaj/Projects/Conductor/platform/customer/src/test/java/com/conductor/customer/CustomerArchBoundaryTest.java)) assert that packages in `com.conductor.customer` must not import classes from `com.conductor.workflow` or `com.conductor.analytics`.
- **Decoupled Messaging**: Service-to-service communication must route through the NATS event bus publisher APIs or clean REST interfaces, not internal function calls.

---

## D. Cryptographic PII Protection
In accordance with user data protection policies, all columns containing PII (such as phone numbers, emails, and full names) must be encrypted at rest:

- **Converter Pattern**: Apply `@Convert(converter = PiiEncryptedConverter.class)` to JPA entity fields.
- **Under the Hood**: The converter intercepts JPA queries, encrypting field strings with AES-256 before writing to PostgreSQL, and decrypts them during row reads.
- **Indexed Search (Phase 2)**: Encrypted fields cannot be searched using standard SQL `LIKE`. Searches will leverage blind index columns or separate lookup hashes.

---

## E. Logging & Diagnostics
- **Structured JSON**: All application logs in staging and production export in JSON format (governed by `ADR-GOV-006`).
- **Trace Context Propagation**: Developers must use SLF4J loggers. Ensure that MDC (Mapped Diagnostic Context) is populated with `trace_id` and `tenant_id` tags before execution begins to support log tracing in Loki.
  - *Anti-Pattern*: Never print sensitive PII values (such as customer phone numbers or email addresses) in log strings.

---

## F. Related Pages
- [Best Practices Guide](Best-Practices-Guide)
- [Developer & API Guide](Developer-and-API-Guide)
- [Repository Structure](Repository-Structure)
