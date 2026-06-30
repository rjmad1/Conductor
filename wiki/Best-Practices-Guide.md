# Conductor Best Practices Guide

## A. Purpose
This guide compiles core software design patterns, database modeling principles, and security coding guardrails that developers and architects must follow when writing code for the Conductor platform.

## B. Intended Audience
- Software Engineers (Java/React)
- Integration Architects
- Code Review Board Members

## C. Scope
Provides actionable guidelines for Temporal workflow design, NATS event publisher logic, database indexes, and access control checks.

## D. Prerequisites
- Familiarity with the [Coding Standards](Coding-Standards) and [Security Guide](Security-Guide).

---

## E. Detailed Content

### 1. Temporal Workflow Design Patterns
Because Temporal executes workflows using event-sourced history replays, workflow definitions must be **strictly deterministic**.

#### Forbidden Workflow Actions:
- **No Direct Thread Control**: Never call `Thread.sleep()`, use `Workflow.sleep()`.
- **No Non-Deterministic Utilities**: Never call `System.currentTimeMillis()`, `LocalDate.now()`, or `UUID.randomUUID()` inside a workflow method. Use `Workflow.currentTimeMillis()` or pass the values in as parameters.
- **No Network / DB I/O**: Do not write JDBC queries or call HTTP services directly inside a workflow method. All external system interactions must be encapsulated inside an **Activity**.

#### Workflow Versioning Rules:
If you modify workflow code, use Temporal version helpers to prevent history replay mismatches:
```java
int version = Workflow.getVersion("addShippingStep", Workflow.DEFAULT_VERSION, 1);
if (version != Workflow.DEFAULT_VERSION) {
    executeShippingActivity();
}
```

---

### 2. Event-Driven Architecture (EDA) Practices
- **At-Least-Once Delivery**: Design event subscribers to be **idempotent**. Since NATS JetStream guarantees at-least-once delivery, events can occasionally be retried. Use a unique message ID (e.g. `messageId` hash) to check for duplicate execution in Redis.
- **Payload Size Control**: Avoid sending large payloads over the event bus. Send references (e.g. `customerId`, `orderId`) instead of embedding full objects.
- **Event Contracts**: Register and validate schemas against the contracts directory before publishing.

---

### 3. Database Modeling & Isolation
- **Composite Indexes**: When creating new tables in the transaction store, always index the composite key `(tenant_id, lookup_column)`.
  ```sql
  CREATE INDEX idx_customers_tenant_email ON customers (tenant_id, email);
  ```
- **Logical Boundary Separation**: Never perform SQL `JOIN` statements between different service schemas (e.g., joining `workflow_definitions` to `customers` table). Data aggregation across services must occur via async event streams or lightweight REST queries.
- **Audit Ledger Compliance**: Ensure any table containing user settings, authorization keys, or billing options has the trigger `audit_trigger` enabled to capture history.

---

### 4. Code Security Checks
- **Input Validation**: Leverage Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Valid`) on all REST request records.
- **PII Storage**: Any contact value or attribute that contains PII (e.g. phone, email, customer name) must use the `@Convert(converter = PiiEncryptedConverter.class)` attribute mapping to encrypt values before writing to disk.

---

## F. References
- [Developer & API Guide](Developer-and-API-Guide)
- [Coding Standards](Coding-Standards)

## G. Related Wiki Pages
- [Implementation Guide](Implementation-Guide)
- [Security Guide](Security-Guide)
