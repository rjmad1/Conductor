# Tenant Context Propagation — Conductor Platform

This specification describes the downstream propagation of tenant context parameters across the application, database, and message broker tiers.

---

## 1. Context Lifecycle Flow

```
[Inbound HTTP Ingress] ──► [Kong API Gateway] ──► [TenantContextFilter] ──► [Hibernate Aspect]
  Extracts JWT Token         Injects header:          Populates:               Enables:
                             X-Tenant-ID              TenantContext            session.enableFilter()
```

### 1.1 Ingress Extraction
The edge gateway (Kong) validates OIDC tokens. On validation success, it maps the token realm to the tenant's UUID and appends it to downstream requests:
`X-Tenant-ID: d6f7340e-26bd-4f51-a96c-b3a5169a9999`

---

## 2. Monolith Middleware

### 2.1 ThreadLocal Binding (`TenantContextFilter`)
A servlet filter interceptor processes incoming requests. It extracts `X-Tenant-ID` and `X-User-ID`, binds them to `TenantContext` ThreadLocals, and clears the context inside a `finally` block to prevent thread pool leakage:
```java
try {
    TenantContext.setCurrentTenantId(UUID.fromString(headerId));
    chain.doFilter(request, response);
} finally {
    TenantContext.clear();
}
```

### 2.2 Database Layer Logical Isolation (`TenantFilterAspect`)
All data entities inherit from the base class `TenantAwareEntity`. A Spring AOP Aspect binds to Spring Data JPA repository invocations, intercepts the active transaction Session, and enables the filter:
```java
Session session = entityManager.unwrap(Session.class);
session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
```
As a consequence, Hibernate appends `AND tenant_id = ?` to all dynamic SQL operations.

---

## 3. Asynchronous Context Propagation

### 3.1 NATS JetStream Events
Event publishers append the tenant ID directly to NATS subjects to preserve route namespaces:
`conductor.{tenantId}.customer.created`

### 3.2 Temporal Workflows
Temporal interceptors capture `TenantContext` parameters and inject them into the workflow's header metadata envelope before dispatch, ensuring asynchronous workers execute under the same isolation scope.
