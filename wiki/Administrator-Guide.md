# Conductor Administrator Guide

## A. Purpose
This guide defines operational procedures for provisioning tenants, managing Keycloak security realms, configuring WhatsApp Business accounts, and administrating subscription plans on the Conductor platform.

## B. Intended Audience
- Platform Administrators
- Customer Success Operations
- Technical Support Teams

## C. Scope
Covers administrative processes from new tenant registration to Keycloak realm configuration, Meta WABA verification, and billing state management.

## D. Prerequisites
- Access to the Platform Admin panel or administrative REST endpoints.
- Administrator credentials for the Keycloak master realm.
- Verification access on the Meta Business Manager console.

---

## E. Detailed Content

### 1. Tenant Provisioning Workflow
When a new SMB signs up, the tenant provisioning process must be completed sequentially:

1. **Create Tenant Record**:
   Call the Platform Admin API to establish the logical tenant partition.
   ```http
   POST /api/v1/tenants
   Authorization: Bearer <platform_admin_jwt>
   Content-Type: application/json

   {
     "name": "Acme Electronics",
     "domain": "acme.conductor.app",
     "subscriptionTier": "GROWTH"
   }
   ```
2. **Execute Schema Migrations**:
   Flyway runs database migrations automatically. Ensure database connections succeed and row structures are verified.
3. **Provision Keycloak Realm**:
   [KeycloakAdminService](file:///c:/Users/rajaj/Projects/Conductor/platform/tenant/src/main/java/com/conductor/tenant/service/KeycloakAdminService.java) creates a dedicated Keycloak security realm for the tenant, mapping the primary roles (`ROLE_TENANT_ADMIN`, `ROLE_TENANT_OWNER`, `ROLE_TENANT_AGENT`).
4. **Configure DNS Domain Mappings**:
   Register custom CNAME records on the Cloudflare router pointing to the Kong API Gateway ALB.

---

### 2. Keycloak Realm Administration
Every tenant runs inside a logically isolated realm to enforce SSO and RBAC configuration standard.

- **Admin Account**: During provisioning, the primary tenant contact is invited as `ROLE_TENANT_ADMIN` to configure users in their dedicated realm.
- **Client Configuration**: Every realm registers:
  - `conductor-web-app`: Public client for the SPA React frontend.
  - `conductor-backend`: Bearer-only client for JWT validation in API services.
- **Federated Login (Phase 2)**: Enable Google OAuth sign-in within the Keycloak realm settings.

---

### 3. Meta WhatsApp Business Account (WABA) Integration
To send messages on WhatsApp, the tenant's phone number must be officially registered on the Meta Developer Console.

#### Provisioning Checklist:
1. **Business Verification**: The SMB must undergo Meta Business verification.
2. **Create Meta Developer App**: Establish a System User with permission to generate long-lived access tokens.
3. **Register Phone Number**: Save the phone ID and WABA ID inside the Conductor Integration settings page.
4. **Configure Meta Inbound Webhook**:
   - URL: `https://api.conductor.app/api/v1/integrations/webhooks/ingress/whatsapp/{tenantId}`
   - Verification Token: Use the cryptographic token generated for the tenant's integration connector.
   - Subscriptions: Enable `messages` and `message_deliveries` events.

---

### 4. Subscription & Billing Administration
The platform supports subscription tiers: `TRIAL`, `GROWTH`, `ENTERPRISE`.

#### Billing State Transitions:
- **Active**: Tenant accounts have full write-read API access.
- **Suspended**: Accounts are locked out at the API Gateway level (Kong yields a `403 Forbidden` response for the `tenant_id` namespace). Outbound automated campaign workers are paused.
- **Cancelled**: Soft-deletion is executed. The tenant domain is marked inactive. After a 30-day retention grace period, databases purge rows containing the deleted `tenant_id`.

---

## F. References
- [Service Catalog](Service-Catalog)
- [Integration Guide](Integration-Guide)

## G. Related Wiki Pages
- [User Guide](User-Guide)
- [Operations Guide](Operations-Guide)
- [Security Guide](Security-Guide)
