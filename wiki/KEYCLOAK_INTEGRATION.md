# Keycloak Integration Specification — Conductor Platform

This specification describes Keycloak integration, covering OIDC configurations, client mappings, and the dynamic realm-per-tenant provisioning model.

---

## 1. Multi-Tenant Realm Strategy

Each onboarded tenant is assigned a physically and logically isolated user registry represented as a dedicated **Keycloak Realm**:
$$\text{Realm Name Pattern: } \texttt{conductor-\{tenantId\}}$$

- **Isolation Benefit**: Prevents user credential exposure across namespaces, allows custom enterprise SSO SAML/AD integrations per tenant, and conforms to SOC 2 Type II controls.
- **Dynamic Provisioning**: Handled via Keycloak Admin Java APIs executing requests with the master admin client credentials.

---

## 2. Client Configurations

Two standard clients are provisioned in each realm:

### 2.1 Public SPA Client (`conductor-frontend`)
- **Flow**: Authorization Code Flow with PKCE.
- **Redirect URIs**: Whitelisted to the tenant's workspace subdomain (e.g. `https://*.conductor.com/*` or local `http://localhost:3000/*`).
- **Secret**: None (public client).

### 2.2 Confidential Client (`conductor-backend`)
- **Flow**: Client Credentials Grant.
- **Client Authenticator**: Client Secret rotated quarterly.
- **Purpose**: System-to-system auth (messaging webhook callback mappings, outbound Temporal worker execution).

---

## 3. JWT Claims Structure

The token issuer sets the following claims in the OIDC Access Token:

```json
{
  "iss": "http://localhost:8080/realms/conductor-d6f734...",
  "sub": "keycloak-user-uuid-9999",
  "email": "user@tenant.com",
  "realm_access": {
    "roles": [
      "Tenant Admin",
      "Campaign Editor"
    ]
  },
  "tenant_id": "d6f7340e-26bd-4f51-a96c-b3a5169a9999"
}
```
Spring Boot oauth2 converters extract these parameters and bind them to active SecurityContext authorities.
