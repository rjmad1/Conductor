# Authorization Engine Specification — Conductor Platform

This specification describes the design and operations of the dynamic Authorization Engine (`authz` bean) in Conductor.

---

## 1. Engine Core Flow

The Authorization Engine intercepts requests at the Spring Method Security layer. It evaluates token authorities, scopes, and target resource attributes.

```
Request ──► Spring Security ──► @PreAuthorize("@authz.hasPermission('...')")
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      ▼                                               ▼
          Possesses Global Admin?                          Evaluates Wildcard Match?
          - `*:*` returns true                             - `workflows:*` matches `workflows:read`
```

---

## 2. Dynamic Wildcard Matching Rules

Permissions are mapped to strings separated by colons: `{domain}:{action}`. The engine resolves wildcard matches hierarchically:

1. **Global Superuser (`*:*`)**: Posessors bypass all specific authority checks (except logical database tenant isolation rules).
2. **Domain Superuser (`{domain}:*`)**: Matches any action inside the specified domain boundary:
   - Example: `contacts:*` grants `contacts:read`, `contacts:write`, `contacts:delete`.
3. **Specific Permission (`{domain}:{action}`)**: Exact string comparison check.

---

## 3. Resource Ownership ABAC Validations

For operations targeting specific resource IDs (e.g. updating a workflow definition), the controller verifies ownership constraints:
```java
@PreAuthorize("@authz.isResourceOwner(#resource.tenantId, #context.tenantId)")
```
If the resource is owned by a different tenant, access is denied (returning 404 to avoid exposing resource existence).
