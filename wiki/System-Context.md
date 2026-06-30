# System Context

## A. Purpose
This page defines the boundaries of the Conductor platform, outlining its actors, external interfaces, and core integration points (equivalent to C4 Level 1).

---

## B. System Context Diagram

```mermaid
graph TD
    subgraph Users [Human Actors]
        Owner[Business Owner / Tenant Admin]
        Agent[Staff Agent]
        EndUser[End Customer / WhatsApp User]
    end

    subgraph ExternalSystems [External Systems & APIs]
        Meta[Meta WhatsApp Cloud API]
        Keycloak[Keycloak OAuth 2.0 / OIDC]
        Squid[Squid Egress Proxy]
        Shopify[Shopify E-commerce]
        Zoho[Zoho CRM]
        Razorpay[Razorpay Gateway]
    end

    subgraph Conductor [Conductor Boundary]
        Gateway[Kong API Gateway]
        Monolith[Spring Boot Monolith Application]
        Workers[Temporal Workflow Workers]
        NATS[NATS JetStream Event Bus]
        DB[(PostgreSQL Database)]
        Cache[(Redis Cache)]
    end

    Owner -->|Configures and monitors| Gateway
    Agent -->|Chats with customers| Gateway
    EndUser <-->|WhatsApp messages| Meta
    Meta <-->|Webhooks & messaging API| Gateway
    Gateway --> Monolith
    Monolith <--> NATS
    Monolith <--> DB
    Monolith <--> Cache
    Workers <--> Monolith
    Workers <--> NATS
    
    Monolith -->|Secure egress call| Squid
    Squid --> Shopify
    Squid --> Zoho
    Squid --> Razorpay
    
    Gateway -->|Verify JWT tokens| Keycloak
```

---

## C. System Boundary & External Integrations

### 1. Human Actors
- **Business Owner**: Configures settings, manages customer segments, and builds marketing campaigns.
- **Staff Agent**: Responds to end customers via the Chatwoot workspace when support is escalated.
- **End Customer**: Receives notification updates and interacts with the business over WhatsApp.

### 2. External Integration Interfaces
- **Meta WhatsApp Cloud API**: Routes messages via JSON REST payloads (`POST /v18.0/{phone-number-id}/messages`). Receives status callbacks on inbound webhook routes.
- **OIDC Identity Provider (Keycloak)**: Validates logins, returns JSON Web Tokens containing the tenant context, and manages SSO profiles.
- **External Connectors (Shopify, Zoho, Razorpay)**: Dispatches webhooks to Conductor's webhook ingress when events trigger. Integrations route OAuth tokens to read and write CRM data.
- **Squid Forward Proxy**: acts as a gateway for all integrations HTTP egress to block Server-Side Request Forgery (SSRF) to link-local or internal subnets.

---

## D. Related Pages
- [Architecture Overview](Architecture-Overview)
- [Integration Guide](Integration-Guide)
- [Developer & API Guide](Developer-and-API-Guide)
