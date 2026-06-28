# Authorization Standard — Conductor Platform

This standard defines the authorization models, permission policies, role definitions, and access rules governing the Conductor Platform. It integrates Role-Based Access Control (RBAC) and Attribute-Based Access Control (ABAC) to enforce strict tenant boundary controls.

---

## 1. Role-Based Access Control (RBAC)

The platform defines scopes at two levels: **Global System Admin** level and **Tenant Workspace** level. Keycloak maps these roles into standard JWT access tokens.

### 1.1 Tenant Workspace Roles

These roles apply strictly within a single tenant realm boundary:

| Role Name | Scope Privileges | Description |
| :--- | :--- | :--- |
| **Tenant Owner** | `*:*` (All scopes within tenant) | Full administrative access to the tenant workspace, billing details, API keys creation, integrations, and user invites. |
| **Campaign Editor** | `workflows:*`, `contacts:*`, `messages:send`, `analytics:read` | Can create/edit workflow DSL templates, upload customer lists, run/stop campaigns, and view delivery metrics. Cannot edit billing, integration secrets, or invite users. |
| **Campaign Viewer** | `workflows:read`, `contacts:read`, `messages:read`, `analytics:read` | Read-only access to campaign parameters, reports, and delivery charts. Cannot modify files, templates, or settings. |
| **Integration Manager** | `integrations:*`, `workflows:read` | Can modify third-party app connections (Zoho, Shopify), set credentials, and map data fields. |

### 1.2 Global Platform Roles (Conductor System Operators)

These roles operate at the global management level, isolated from standard tenant business transactions:

| Role Name | Scope Privileges | Description |
| :--- | :--- | :--- |
| **Global Ops Admin** | `system:*` | Can view platform telemetry, manage system resources, trigger schema upgrades, and provision/deactivate tenants. Banned from viewing Tier 1 Customer PII inside tenant tables. |
| **Support Engineer** | `system:telemetry:read`, `tenant:config:read` | Read-only access to system metrics, connection logs, and tenant feature configurations to assist with troubleshooting. |

---

## 2. Attribute-Based Access Control (ABAC)

Where RBAC lacks granularity, ABAC rules evaluate dynamic attributes at runtime:

*   **Attribute: IP Range Restriction:** Access to administrative endpoints (e.g. `/api/v1/tenants/{id}/settings`) can be restricted to specific CIDR block IP ranges whitelisted in the tenant settings.
*   **Attribute: Time-of-Day Access:** Support accounts can only access tenant support scopes within scheduled maintenance windows (e.g., standard business hours).
*   **Attribute: API Key Scope Restriction:** Developer API keys contain a metadata string limiting execution to specific methods (e.g., `cond_live_123` with scope `contacts:write` can only invoke contact upload paths).
*   **Attribute: Subscription Limits Enforcer:** Active campaign triggers check dynamic tenant attributes (active message count, billing status) against subscription tiers before NATS event dispatches.

---

## 3. Resource Ownership & Tenant Isolation Guardrails

```
Request Context ──► Extract JWT ──► Resolve Tenant Context (tenant_id)
                                             │
      ┌──────────────────────────────────────┴──────────────────────────────────────┐
      ▼                                                                             ▼
Read Entity (Database)                                                        API Resource Call
Validate: Entity.tenant_id == activeTenant                                    Validate: Token.scopes contain Resource.scope
```

1.  **Logical Tenancy Checks:** Every database query must incorporate the tenant identifier. Database models must extend `TenantAwareEntity`. Direct database lookups by ID (e.g., `findById(uuid)`) are wrapped in custom repositories enforcing `findByIdAndTenantId(uuid, tenantId)`.
2.  **Cross-Tenant Access Rejection:** If a user authenticated for Tenant A attempts to access a resource (workflow, contact, report) owned by Tenant B, the Spring Boot resource controller must reject the request immediately with a `403 Forbidden` error, logging a security audit event.
3.  **Owner-Only Deletions:** Destructive changes (e.g., clearing customer database tables) require the initiator to possess the `Tenant Owner` role.

---

## 4. Service-to-Service Authorization

Internal modular boundary calls must follow strict access permissions:

*   **REST API Calls:** Downstream endpoints authenticate calls using JSON Web Tokens containing service identity scopes (e.g., `Service: Integration` presents a token with client scope `workflows:write` to trigger a workflow execution).
*   **NATS JetStream Event Access Control:**
    *   **Adapter Permissions:** The `whatsapp-adapter` container connects to NATS using a user profile restricted to pub/sub on `conductor.*.messaging.>` subjects.
    *   **Cross-Subject Blocks:** An adapter attempting to publish on a subject belonging to the `customer` or `audit` domains (e.g. `conductor.system.audit`) is blocked by the NATS JetStream server ACLs.

---

## 5. Administrative Authorization Safeguards

1.  **Dual-Authorization Gates (Four-Eyes Principle):** Critical system settings updates (e.g. modifying global OAuth gateway setups, triggering mass tenant purges) require confirmation by two distinct operators possessing the `Global Ops Admin` role.
2.  **No Direct Support DB Writes:** Support engineers are strictly prohibited from executing manual UPDATE or DELETE statements on tenant databases. Troubleshooting must be executed via audit-logged platform APIs.

This standard is approved for all Conductor authorization models.
