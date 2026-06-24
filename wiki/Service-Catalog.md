# Service Catalog

## A. Purpose
This page catalogs the Spring Boot modules, ports, and operational states of the services that make up the Conductor platform codebase.

---

## B. Active Service Inventory (Ground Truth)

These services exist in the current codebase as functional Gradle modules:

| Service Module | Actual Port | Gradle Module | Key Responsibilities |
|---|---|---|---|
| **`tenant-service`** | `8081` | `:platform:tenant` | Tenant onboarding, subscription updates, metadata configs, Keycloak realm provisioning. |
| **`identity-service`** | `8082` | `:platform:identity` | User profiles, tenant memberships, REST API key creation and validation. |
| **`customer-service`** | `8084` | `:platform:customer` | Customer database, attributes, preferences, consent registry, and segments. |
| **`connector-service`** | `8087` | `:platform:integrations` | Connector credentials, Webhook ingress, cryptographic signature checking. |
| **`workflow-service`** | `8090` | `:platform:workflow` | Temporal workers setup, workflow triggers, and custom JSON DSL executor engine. |
| **`analytics-service`** | `8085` | `:platform:analytics` | Metabase iframe embed tokens, scheduled reporting, ClickHouse OLAP writers. |
| **`events-service`** | N/A | `:platform:events` | Dead-Letter Queue (DLQ) tracking and event replays. |

---

## C. Deferred/Unimplemented Services (Gaps)

The following components are defined in architectural design documents (such as `Solution-Architecture.md`) but do not exist in the Gradle project definition:

| Service | Intended Port | Status | Ground Truth Implementation |
|---|---|---|---|
| `auth-service` | `8081` (in docs) | **Deferred** | Identity/IAM calls are handled directly via Keycloak realms and API key validations in `:platform:identity`. |
| `campaign-service` | `8085` (in docs) | **Deferred** | Campaign triggers are executed within `:platform:workflow`. |
| `template-service` | `8086` (in docs) | **Deferred** | Meta message template submissions are stubbed or deferred. |
| `whatsapp-adapter` | `8088` (in docs) | **Deferred** | Inbound webhooks route directly to `:platform:integrations` at `/webhooks/ingress/whatsapp/{tenantId}` and publish to NATS JetStream. Outbound calls are managed as workflow Activities in `:platform:workflow`. |
| `billing-service` | `8090` (in docs) | **Deferred** | Platform subscription billing is not implemented in code yet. |
| `notification-service` | `8091` (in docs) | **Deferred** | Platform notifications are deferred. |
| `conversation-service` | `8084` (in docs) | **Deferred** | Handled directly by Chatwoot embeds and NATS events. |

---

## D. Related Pages
- [Component Catalog](Component-Catalog)
- [Developer & API Guide](Developer-and-API-Guide)
- [Operations Guide](Operations-Guide)
