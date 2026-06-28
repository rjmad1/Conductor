# Role-Based Access Control (RBAC) Model — Conductor

This document details the scopes, role hierarchies, and permission validations implemented in Conductor.

---

## 1. Role Hierarchies & Scopes

Conductor organizes authorization permissions into Global (management) and Tenant (workspace) layers.

### 1.1 Tenant Workspace Roles

| Role | Scope Patterns | Allowed Actions |
| :--- | :--- | :--- |
| **Tenant Owner** | `*:*` | All write, read, delete, billing, and credential operations. |
| **Campaign Editor**| `workflows:*`, `contacts:*`, `messages:send` | Create and edit campaign configurations, write templates, trigger message dispatches. |
| **Campaign Viewer**| `workflows:read`, `contacts:read`, `analytics:read` | Read-only access to campaigns and metrics. |
| **Integration Manager**| `integrations:*`, `workflows:read` | Link Shopify/Zoho platforms, rotate connector credentials. |

### 1.2 Global Management Roles

| Role | Scope Patterns | Description |
| :--- | :--- | :--- |
| **Platform Admin** | `system:*` | Create/Deactivate tenants, update system schema, configure global gateway profiles. |
| **Support Agent** | `system:telemetry:read` | Read system metrics to troubleshoot integration pipelines. |

---

## 2. Permissions Validation

Enforcement is applied programmatically via Spring Security method annotations:
```java
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
public ResponseEntity<Void> executeOperation() { ... }
```
Dynamic logical operations (wildcard validation) evaluate checks against the `authz` bean:
```java
@PreAuthorize("@authz.hasPermission('workflows:write')")
```
- **Deny by Default**: Requests without matching roles/scopes are aborted at the filter chain with a `403 Forbidden` error.
- **Tenant Context Isolation**: Interceptors confirm that the active user possesses a valid `Membership` in the queried tenant.
