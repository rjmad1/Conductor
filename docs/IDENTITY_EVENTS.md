# Identity & Tenant Event Specification — Conductor

This specification lists the asynchronous NATS events emitted by the Identity and Tenant domains, documenting namespaces and payload schemas.

---

## 1. Event Subject Namespaces

Events follow a standardized naming structure:
$$\text{Namespace: } \texttt{conductor.\{tenantId\}.\{domain\}.\{entity\}.\{action\}}$$

- **Purpose**: Restricts subscriber listener scopes, simplifies tracking pipelines, and enables Loki/Prometheus analytics tracking.

---

## 2. Event Catalog & Payload Schemas

All events are published wrapped inside the `EventEnvelope`:

```json
{
  "eventId": "a7b8c4d2-f678-4a9f-b98d-c45df2bf9999",
  "eventType": "conductor.d6f7340e-26bd-4f51-a96c-b3a5169a9999.tenant.profile.created",
  "tenantId": "d6f7340e-26bd-4f51-a96c-b3a5169a9999",
  "timestamp": "2026-06-24T18:00:00.000Z",
  "version": "1.0.0",
  "payload": { ... }
}
```

### 2.1 tenant.profile.created
- **Subject**: `conductor.{tenantId}.tenant.profile.created`
- **Payload**:
```json
{
  "id": "d6f7340e-26bd-4f51-a96c-b3a5169a9999",
  "name": "Acme Marketing",
  "domain": "acme"
}
```

### 2.2 user.invited
- **Subject**: `conductor.{tenantId}.identity.user.invited`
- **Payload**:
```json
{
  "id": "e9c8b762-c2e3-4f9e-a22d-b45df2bf8888",
  "email": "editor@acme.com",
  "role": "Campaign Editor"
}
```

### 2.3 role.assigned
- **Subject**: `conductor.{tenantId}.identity.role.assigned`
- **Payload**:
```json
{
  "userId": "e9c8b762-c2e3-4f9e-a22d-b45df2bf8888",
  "role": "Tenant Admin"
}
```
