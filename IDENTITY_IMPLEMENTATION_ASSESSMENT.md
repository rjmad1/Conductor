# Identity, Tenant & Access Management Implementation Assessment

This document maps the architectural review of current authentication, user management, and security components in the Conductor platform workspace, identifying reuse, wrap, and build opportunities.

---

## 1. Current State Assessment

### 1.1 Authentication & IAM
- **Status**: The repository currently provisions a **Keycloak v24.0.0** container inside `docker-compose.local.yml` and references a volume mount targeting `./platform/identity/realm-export.json`.
- **Configured State**: The directory `platform/identity` is empty by default; the bootstrap script generates a bare-minimum placeholder `realm-export.json` containing only `{"realm": "conductor", "enabled": true}` if not present. However, a template configuration exists at `workspace/platform/identity/realm-export.json` mapping a client (`conductor-frontend` and `conductor-backend`) and default roles (`admin`, `user`).
- **Gaps**: There is no active code implementing user profiles, invitations, token translation, or machine service identity exchanges.

### 1.2 User & Tenant Models
- **Status**: No Java code or SQL databases schemas exist in the workspace. All directories under `platform/` are empty.
- **Gaps**: Complete absence of database schemas for:
  - Users, user profiles, and platform memberships.
  - Tenants, subscription scopes, and environments.
  - Active developer API keys and scope permissions.

### 1.3 Authorization (RBAC & ABAC)
- **Status**: Keycloak contains definitions for `admin` and `user` roles in its template JSON.
- **Gaps**: Spring Security context parsing and RBAC evaluation are missing. Predefined roles requested by the product team (Platform Admin, Tenant Admin, Workflow Admin, Operations Manager, Support Agent, Read Only User, API Client, Service Account) must be mapped dynamically to Keycloak client roles and validated at the controller endpoint layer.

### 1.4 Tenant Context Propagation & Database Isolation
- **Status**: No propagation logic exists.
- **Gaps**: Need to build:
  - Gateway mappings resolving tenant identifiers (from OIDC URLs or subdomain headers).
  - Downstream context resolution filter capturing `X-Tenant-ID` header.
  - Thread-Local request scope propagating tenant identity to asynchronous event channels (NATS) and durable execution handlers (Temporal).
  - Hibernate `@FilterDef` AOP configurations modifying database transaction SQL scopes dynamically to prevent cross-tenant data access.

---

## 2. Reuse, Wrap, Extend & Build Decisions Ledger

| Area | Component | Treat Strategy | Rationale & Action Plan |
| :--- | :--- | :--- | :--- |
| **User IAM** | Keycloak | **ADOPT / REUSE** | Reuse Keycloak standard docker container and import configurations. Avoid custom authentication code. |
| **API Gateway** | Kong Gateway | **EXTEND** | Configure Kong to route traffic and execute Edge JWT signature validations. |
| **Audit Trails** | PostgreSQL Triggers | **BUILD** | Create database-level triggers to record modifications in an immutable, partitioned audit log table. |
| **Tenant Context** | Spring Interceptor & AOP | **BUILD** | Implement thread-local propagation and custom Hibernate AOP filters to enforce tenant logical isolation. |
| **Service Identity**| Keycloak OIDC | **WRAP** | Expose machine authentication using Keycloak's OAuth 2.0 Client Credentials Grant. |
| **API Key Engine** | Spring Security Filter | **BUILD** | Develop hash validation filters mapping bcrypt tokens against database-stored hashes. |
| **Webhook Auth** | HMAC Validation | **BUILD** | Write thread-safe request interceptor executing constant-time signature validations. |

---

## 3. Recommended Tech Stack & Versions

- **Java Development Kit**: JDK 17 (compatibility target matching host workstation).
- **Spring Boot**: 3.2.5 (including Virtual Threads compatibility targets).
- **Security Engine**: Spring Security + OAuth2 Resource Server.
- **Database Migrations**: Flyway.
- **Database Driver**: PostgreSQL.
- **Messaging Event Client**: NATS Java Client (`jnats`).
- **Boundaries Verification**: ArchUnit.
