# Identity and Tenant API Specification — Conductor

This specification outlines the REST API contracts, validation constraints, and error mappings for the Tenant and Identity domains.

---

## 1. Tenant Lifecycle REST APIs

### 1.1 Create Tenant
- **HTTP Method**: `POST`
- **Path**: `/api/v1/tenants`
- **Authentication Required**: Platform Admin (`ROLE_PLATFORM_ADMIN`)
- **Request Body**:
```json
{
  "name": "Acme Marketing",
  "domain": "acme",
  "subscriptionTier": "STANDARD"
}
```
- **Response (201 Created)**:
```json
{
  "id": "c7a8b419-f538-4228-982d-b45df2bfd834",
  "name": "Acme Marketing",
  "domain": "acme",
  "subscriptionStatus": "ACTIVE",
  "subscriptionTier": "STANDARD"
}
```

### 1.2 Get Tenant
- **HTTP Method**: `GET`
- **Path**: `/api/v1/tenants/{id}`
- **Authentication**: `ROLE_PLATFORM_ADMIN` or matching `ROLE_TENANT_ADMIN`.
- **Response (200 OK)**: Returns profile JSON. Mismatched tenant queries return `404 Not Found`.

---

## 2. Identity & User Management REST APIs

### 2.1 Invite User
- **HTTP Method**: `POST`
- **Path**: `/api/v1/identity/users/invite`
- **Authentication**: `ROLE_TENANT_ADMIN`
- **Request Body**:
```json
{
  "email": "editor@acme.com",
  "role": "Campaign Editor"
}
```
- **Response (222 Accepted)**: User skeleton generated. Returns invitation status.

### 2.2 Generate API Key
- **HTTP Method**: `POST`
- **Path**: `/api/v1/identity/api-keys`
- **Request Body**:
```json
{
  "userId": "d7b9c512-c2e3-4b6e-a22d-a45df2bfe345",
  "scopes": "contacts:read,workflows:write",
  "expiresAt": "2027-12-31T23:59:59Z"
}
```
- **Response (201 Created)**:
```json
{
  "apiKey": "cond_live_56f7a8b9c2d3e4f5..."
}
```
*Security Constraint*: The plaintext API key is returned exactly once. Subsequent calls query only hashed records.
