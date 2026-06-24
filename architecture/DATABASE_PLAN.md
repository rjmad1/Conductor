# Database Plan — Conductor

This document details the database execution plan for the Conductor MVP. Since the platform is a greenfield system (0% implementation complete, zero databases currently provisioned), there are no existing tables to reuse or extend. All specified tables represent **New** tables to be created during Phase 0 and Phase 1.

---

## 1. Table Categories

### Reuse
*   **None** (Greenfield repository; no existing tables are provisioned).

### Extend
*   **None** (Greenfield repository).

### New (Tables to Create)

Below is the complete table inventory mapped to execution details:

| Table Name | Domain | Purpose | Ownership | Internal/External Dependencies | Migration Risk |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `plans` | Tenant | Defines pricing tiers, quotas, and capabilities. | Tenant Monolith Module | None | **Low** |
| `tenants` | Tenant | Primary customer profile registry. | Tenant Monolith Module | `plans` | **Low** |
| `subscriptions` | Tenant | Links tenants to plans and matches Razorpay records. | Tenant Monolith Module | `tenants`, `plans` | **Medium** (Razorpay sync drift) |
| `usage_records` | Tenant | Tracks messages, workflows, and API calls. | Tenant Monolith Module | `tenants` | **Medium** (High write volume) |
| `invoices` | Tenant | Records billing receipts linked to Razorpay payouts. | Tenant Monolith Module | `tenants` | **Low** |
| `users` | Identity | Local credentials and role mapping mapping. | Identity Monolith Module | `tenants` | **Medium** (Sync drift with Keycloak) |
| `api_keys` | Identity | Token credentials for external API access. | Identity Monolith Module | `tenants`, `users` | **Low** |
| `customers` | Customer | Unified customer contacts database. | Customer Monolith Module | `tenants` | **High** (Dynamic index scale) |
| `consent_records` | Customer | Immutable records tracking user opt-ins/opt-outs. | Customer Monolith Module | `tenants`, `customers` | **Low** (Immutable write-only) |
| `segments` | Customer | Groups contacts dynamically or statically. | Customer Monolith Module | `tenants`, `users` | **Medium** (Heavy evaluation queries) |
| `workflows` | Workflow | Holds automation definitions and JSON DSL. | Workflow Monolith Module | `tenants`, `users` | **Low** |
| `workflow_executions`| Workflow | Tracks Temporal runs, execution logs, and states. | Workflow Monolith Module | `tenants`, `workflows`, `customers` | **High** (Large data scale) |
| `wa_numbers` | Messaging | Stores credentials for active sender WhatsApp numbers. | Messaging Adapter Module | `tenants` | **Low** |
| `messages` | Messaging | Message delivery logs (partitioned monthly). | Messaging Monolith Module | `tenants`, `customers`, `workflow_executions` | **High** (Extreme write speed) |
| `conversations` | Messaging | Groups messages into sessions (24h customer window). | Messaging Monolith Module | `tenants`, `customers`, `users` | **Medium** (Locking during chats) |
| `templates` | Messaging | Stores custom text templates synced with Meta. | Messaging Monolith Module | `tenants`, `users` | **Low** |
| `audit_logs` | Audit | Immutable logging trace (partitioned quarterly). | Security Monolith Module | `tenants` (Logical reference only) | **Low** |

---

## 2. Table-by-Table Technical Specifications & Migration Risks

### 1. `plans`
*   **Purpose:** Stores subscription plans.
*   **Ownership:** `com.conductor.tenant`
*   **Dependencies:** None.
*   **Migration Risk:** **Low**. Schema structure is static. Simple insert seeds for 'trial', 'starter', etc.

### 2. `tenants`
*   **Purpose:** Holds client organizations using Conductor.
*   **Ownership:** `com.conductor.tenant`
*   **Dependencies:** `plans(id)`.
*   **Migration Risk:** **Low**. Core table. Indexes on `status` and `email` ensure fast lookup.

### 3. `subscriptions`
*   **Purpose:** Represents actual active plans and contains billing identifiers.
*   **Ownership:** `com.conductor.tenant`
*   **Dependencies:** `tenants(id)`, `plans(id)`.
*   **Migration Risk:** **Medium**. High dependency on external billing states (Razorpay). Webhook processing lag can create billing state drift.

### 4. `usage_records`
*   **Purpose:** Captures usage data for quota restrictions.
*   **Ownership:** `com.conductor.tenant`
*   **Dependencies:** `tenants(id)`.
*   **Migration Risk:** **Medium**. This table experiences high write volume. Requires strict composite indexing (`tenant_id`, `billing_period`, `metric`) to optimize lookups.

### 5. `invoices`
*   **Purpose:** Tracks tenant payments and line items.
*   **Ownership:** `com.conductor.tenant`
*   **Dependencies:** `tenants(id)`.
*   **Migration Risk:** **Low**. Write-once model upon payment completion webhook ingestion.

### 6. `users`
*   **Purpose:** Resolves workspace users and correlates roles with Keycloak IDs.
*   **Ownership:** `com.conductor.identity`
*   **Dependencies:** `tenants(id)`.
*   **Migration Risk:** **Medium**. Storing local references to Keycloak IDs can lead to sync discrepancies (e.g. if user is deleted directly in Keycloak console, local DB record becomes orphaned). 
*   **Mitigation:** Keycloak webhook handlers must capture realm user deletions and update local status to `deactivated`.

### 7. `api_keys`
*   **Purpose:** Authorizes developer HTTP requests.
*   **Ownership:** `com.conductor.identity`
*   **Dependencies:** `tenants(id)`, `users(id)`.
*   **Migration Risk:** **Low**. Key credentials use bcrypt hashing. Only the first 8 characters are stored plain-text for indexing.

### 8. `customers`
*   **Purpose:** Houses all contact data, opt-in metrics, and dynamic properties.
*   **Ownership:** `com.conductor.customer`
*   **Dependencies:** `tenants(id)`.
*   **Migration Risk:** **High**. Largest table by row volume. Dynamic custom attributes (JSONB) can inflate disk space.
*   **Mitigation:** Enforce strict partial indexes on phone numbers and tags using Gin indexes to avoid full table scans.

### 9. `consent_records`
*   **Purpose:** Compliance record detailing contact marketing approvals.
*   **Ownership:** `com.conductor.customer`
*   **Dependencies:** `customers(id)`, `tenants(id)`.
*   **Migration Risk:** **Low**. Handled via a database trigger that rejects all `UPDATE` and `DELETE` queries, protecting audit data.

### 10. `segments`
*   **Purpose:** Holds search parameters for target contact demographics.
*   **Ownership:** `com.conductor.customer`
*   **Dependencies:** `tenants(id)`, `users(id)`.
*   **Migration Risk:** **Medium**. Evaluating dynamic JSONB query conditions regularly can impact DB CPU usage under load.

### 11. `workflows`
*   **Purpose:** Holds active JSON configurations and branching maps.
*   **Ownership:** `com.conductor.workflow`
*   **Dependencies:** `tenants(id)`, `users(id)`.
*   **Migration Risk:** **Low**. The table uses JSONB to store the trigger conditions and step limits without forcing schema alters when fields evolve.

### 12. `workflow_executions`
*   **Purpose:** Traces active and complete Temporal runs.
*   **Ownership:** `com.conductor.workflow`
*   **Dependencies:** `workflows(id)`, `tenants(id)`, `customers(id)`.
*   **Migration Risk:** **High**. Records are created for every trigger check. Can swell to millions of entries.
*   **Mitigation:** Set up partition policies by timestamp and configure vacuum metrics to reclaim deleted rows.

### 13. `wa_numbers`
*   **Purpose:** Registers approved WhatsApp numbers.
*   **Ownership:** `com.conductor.messaging`
*   **Dependencies:** `tenants(id)`.
*   **Migration Risk:** **Low**. Access tokens must be stored encrypted using KMS-backed column-level encryption.

### 14. `messages`
*   **Purpose:** Logs all individual chat logs.
*   **Ownership:** `com.conductor.messaging`
*   **Dependencies:** `tenants(id)`, `customers(id)`, `workflow_executions(id)`.
*   **Migration Risk:** **High**. High throughput write target. 
*   **Mitigation:** Must use range-based physical partitioning (monthly tables like `messages_2026_06`). Application queries must specify a time bound (`sent_at`) to hit target partition tables directly.

### 15. `conversations`
*   **Purpose:** Tracks the active 24-hour support sessions.
*   **Ownership:** `com.conductor.messaging`
*   **Dependencies:** `tenants(id)`, `customers(id)`, `users(id)`.
*   **Migration Risk:** **Medium**. Lock contention can occur when multiple workers attempt to increment the `message_count` concurrently.

### 16. `templates`
*   **Purpose:** Stores copy, tags, and status of campaign messages.
*   **Ownership:** `com.conductor.messaging`
*   **Dependencies:** `tenants(id)`, `users(id)`.
*   **Migration Risk:** **Low**. Synced via Meta Cloud webhook hooks.

### 17. `audit_logs`
*   **Purpose:** Captures security events and updates.
*   **Ownership:** `com.conductor.audit`
*   **Dependencies:** `tenants` (Logical context, no physical FK to survive deletions).
*   **Migration Risk:** **Low**. Physically partitioned by calendar quarter (`audit_logs_2026_q2`). Triggers reject modifications to guarantee log integrity.
