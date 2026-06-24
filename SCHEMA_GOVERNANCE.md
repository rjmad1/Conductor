# Schema Governance Guidelines

This document governs the creation, migration, and compatibility validation rules for asynchronous event schemas in the Conductor Platform.

---

## 1. Compatibility Enforcement

To prevent breaking consumer processes, all schema alterations must adhere to strict **Backwards Compatibility** rules:

1.  **Additive Changes Only**: You may append new properties to a JSON schema.
2.  **No Deletions**: You must not delete existing properties or alter their types (e.g., changing an integer field to a string).
3.  **Mandatory Defaulting**: Any new properties added to a schema must either:
    *   Be marked as optional (not included in the `required` fields array).
    *   Have a default value defined in the schema.

---

## 2. Breaking Changes Lifecycle

If a breaking change is unavoidable:
1.  A new major version of the schema must be declared (e.g. `identity.user.created.v2.json`).
2.  The publisher will emit the new event version.
3.  Consumers must maintain dual handlers (handling both `v1` and `v2`) for a grace period of 3 months to allow active downstream nodes to transition.

---

## 3. Schema Ingestion Gateway

Spring Boot microservices validate incoming data payload segments against registered Draft-07 schemas using `SchemaValidator` before execution logic. Any malformed payloads are rejected and routed to the Dead Letter Queue (DLQ).
