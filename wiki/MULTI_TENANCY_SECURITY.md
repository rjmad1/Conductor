# Multi-Tenancy Security Specification — Conductor Platform

This document details the multi-tenant isolation, data separation, and validation protocols for the Conductor Platform. It evaluates architectural options for database tenancy, recommends a strategy, and specifies isolation validation gates across all layers of the system.

---

## 1. Database Tenancy Evaluation

We evaluated three models of database multi-tenancy for the Conductor MVP:

### Option A: Shared Schema with Row-Level Isolation (Recommended)
*   **Description:** All tenants share the same database tables. A mandatory `tenant_id` column partitions all data logically. Query interceptors automatically append `WHERE tenant_id = :activeTenant` to all read, write, and delete operations.
*   **Pros:** Very low operational overhead; minimal database licensing/resource costs; simple schema updates; scales to thousands of small-to-medium tenants on a single PostgreSQL instance.
*   **Cons:** Highest risk of developer-induced cross-tenant data leaks if query interceptors are bypassed (e.g., in native SQL queries).
*   **Cost Estimate:** Low ($\approx \$50-\$200$/month for RDS Master + Replica).

### Option B: Schema per Tenant (Single Database)
*   **Description:** Every tenant has a dedicated PostgreSQL schema (namespace) inside a shared database. Connections dynamically alter the schema search path (`SET search_path TO tenant_x`) on execution.
*   **Pros:** Logical separation at the database engine level; custom tables or extensions can be added per tenant.
*   **Cons:** Schema migration complexity increases with tenant count (running 100+ separate DDL migrations); connection pooling management is complex; PostgreSQL performance degrades with thousands of schemas (catalog bloat).
*   **Cost Estimate:** Medium ($\approx \$200-\$500$/month due to memory/catalog sizing).

### Option C: Database per Tenant
*   **Description:** Each tenant runs on a physically separate database instance or a distinct database within the server cluster.
*   **Pros:** Perfect physical isolation; zero risk of cross-tenant database leaks; distinct backup/restore capabilities per tenant.
*   **Cons:** High resource waste for idle tenants; slow tenant provisioning (requires spinning up database instances); extremely high operational maintenance and cost.
*   **Cost Estimate:** High ($\ge \$2,000$/month for multi-tenant scaling).

### Comparison Matrix & Recommendation

| Criteria | Shared Schema (A) | Schema per Tenant (B) | Database per Tenant (C) |
| :--- | :--- | :--- | :--- |
| **Operational Simplicity**| **High** (1 DB, standard CI/CD) | Low (Multi-schema DDL migrations) | Very Low (Instance management overhead) |
| **Resource Efficiency** | **High** (Shared buffers, max density) | Medium (Connection pool fragmentation) | Low (Idle container capacity waste) |
| **Isolation Strength** | Medium (Relies on app filters/RLS) | High (Database namespace isolation) | **Very High** (Physical/Process isolation) |
| **Provisioning Speed** | **Instant** (DB INSERT statement) | Medium (DDL creation lag) | Slow (Provisioning RDS/Container) |
| **Cost Profile** | **Low** | Medium | High |

**Recommendation:** **Option A: Shared Schema with Row-Level Isolation** is the approved choice for the Conductor MVP. It aligns with our team size (3-4 engineers) and cost budget. To eliminate the risk of developer bypasses, it must be paired with mandatory compiler checks (ArchUnit) and automated database query interception.

---

## 2. Multi-Tenant Isolation Architecture

The platform enforces strict logical separation at every tier of the request lifecycle:

```
[Request Ingress] ──► [Kong API Gateway] ──► [Spring Boot App] ──► [PostgreSQL/Vector DB]
  User presents        Validates JWT &         Appends Thread-Local    Automatic RLS /
  OIDC JWT Token       Injects X-Tenant-ID     Tenant Context          Metadata Filtering
```

### 2.1 Tenant Identification
*   **Subdomain Resolution:** The Kong API Gateway identifies the tenant based on the incoming request subdomain (e.g. `tenant-abc.conductor.com`) or OIDC ID token claims.
*   **Keycloak Realms Mapping:** Each tenant runs in a dedicated Keycloak Realm. The Gateway verifies the JWT issuer URL (`https://iam.conductor.com/realms/{tenantId}`) to resolve the tenant identifier.

### 2.2 Tenant Context Propagation
*   **Ingress Header Validation:** Once authenticated, the Gateway injects the validated tenant identifier as an immutable downstream header: `X-Tenant-ID: tenant-123-uuid`.
*   **Monolith Context Filter:** A Spring Boot `WebMvcConfigurer` interceptor extracts this header, validates its format, and populates a thread-local context object (`TenantContext`).
*   **Asynchronous Context (Temporal/NATS):**
    *   **NATS JetStream:** Every published event subject includes the tenant ID in the subject route: `conductor.tenant-123.customer.contact.opt_out`.
    *   **Temporal Workflows:** The Temporal SDK Interceptor propagates the `tenant_id` context through workflow metadata envelopes, ensuring workers execute under the same isolation scope.

### 2.3 Tenant Authorization
*   **Realm-Level Access:** Keycloak realms prevent users from Realm A from acquiring tokens for Realm B.
*   **Spring Security RBAC:** Checks permissions matching oauth2 scopes (`contacts:read`, `workflows:write`) mapped explicitly to the validated tenant scope.

### 2.4 Tenant Data Isolation (Database)
*   **JPA Hibernate Filter Interceptor:** All tenant entities inherit from the base `TenantAwareEntity` class containing the `@Filter` definition:
    ```java
    @MappedSuperclass
    @FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
    @Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
    public class TenantAwareEntity {
        @Column(name = "tenant_id", nullable = false, updatable = false)
        private String tenantId;
    }
    ```
    An aspect-oriented programming (AOP) interceptor binds to active database sessions and automatically runs `session.enableFilter("tenantFilter").setParameter("tenantId", TenantContext.getCurrentTenantId())` before query execution.
*   **Qdrant Vector Isolation:** Every semantic search vector contains a metadata payload tag: `{"tenant_id": "tenant-123-uuid"}`. The AI search wrapper must inject this payload match condition as an absolute query constraint:
    ```json
    {
      "filter": {
        "must": [
          { "key": "tenant_id", "match": { "value": "tenant-123-uuid" } }
        ]
      }
    }
    ```

### 2.5 Tenant Event Isolation
*   **Stream Namespaces:** The NATS JetStream server partitions consumer access. Wildcard subscriptions are blocked for tenant-specific workers.
*   **Access Control Lists (ACLs):** NATS user tokens generated for adapters restrict pub/sub permissions to subjects containing their matching tenant ID suffix.

### 2.6 Tenant Audit Isolation
*   **Immutable Trigger Recording:** All row changes are captured via triggers and routed directly to the centralized `audit_logs` table.
*   **Partitioning:** The table is partitioned physically by `tenant_id` and monthly ranges to prevent cross-tenant performance bottlenecks during exports.

### 2.7 Tenant Analytics Isolation
*   **JWT Token Signatures:** Embedded Metabase charts are secured using a JWT containing a signed payload of parameter filters:
    ```json
    {
      "resource": {"dashboard": 12},
      "params": {
        "tenant_id": "tenant-123-uuid"
      }
    }
    ```
    Metabase validates the JWT signature and restricts the SQL query parameters to the specified `tenant_id` without exposing raw database connections.

---

## 3. Isolation Validation Plan

To ensure multi-tenant isolation cannot be bypassed, the following automated gates are defined in the pipeline:

1.  **Static Code Analysis (ArchUnit):** Run tests ensuring no database repositories bypass `TenantAwareEntity` unless whitelisted (e.g. system configurations).
2.  **Cross-Tenant Injection Tests:** Integration tests that attempt to execute HTTP operations utilizing Token A but passing `X-Tenant-ID: tenant-B` in headers or payloads. The gateway or interceptor must abort with a `403 Forbidden` error.
3.  **Vector Leak Audit Tests:** Automated daily tests injecting unique mock strings into Qdrant for Tenant A, and validating that search requests from Tenant B fail to retrieve them.

This specification governs the multi-tenant architecture.
