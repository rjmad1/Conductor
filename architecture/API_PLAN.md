# API Plan — Conductor

This document details the complete REST API and Webhook endpoint inventory designed for the Conductor MVP. All endpoints are grouped by their functional domains. All routes are prefixed with `/api/v1` except for vendor-facing ingress webhooks.

---

## 1. Tenant Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/tenants` | Provisions a new tenant workspace and default configuration. | Public Onboarding / Admin | `plans` lookup, Keycloak Realm generator |
| `GET` | `/api/v1/tenants/current` | Retrieves tenant metadata, usage limits, and active subscription status. | React Dashboard | `X-Tenant-ID` header validation |
| `PUT` | `/api/v1/tenants/current` | Updates tenant metadata (GST, name, timezone settings). | React Dashboard | Database write |

---

## 2. Identity Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/users` | Lists all platform users active in the tenant workspace. | React Dashboard | Keycloak Realm API |
| `POST` | `/api/v1/users` | Invites a new user (admin, manager, agent) and sends email invite link. | React Dashboard | Keycloak user creation, SMTP mail dispatch |
| `DELETE` | `/api/v1/users/{id}` | Revokes workspace access for a user. | React Dashboard | Keycloak user deletion |
| `POST` | `/api/v1/api-keys` | Creates a new developer API key with custom permissions. | React Dashboard | Bcrypt hashing |
| `GET` | `/api/v1/api-keys` | Lists active API keys (showing only `key_prefix`). | React Dashboard | Database lookup |
| `DELETE` | `/api/v1/api-keys/{id}`| Revokes an active developer API key. | React Dashboard | Database revoke action |

---

## 3. Customer Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/customers` | Searches and lists contacts with tag/attribute filters. | React Dashboard / API | PostgreSQL GIN index |
| `POST` | `/api/v1/customers` | Registers a single customer contact. | React Dashboard / API | Database write |
| `PUT` | `/api/v1/customers/{id}` | Updates details or tags of an existing customer contact. | React Dashboard / API | Database write |
| `DELETE` | `/api/v1/customers/{id}` | Triggers DPDP right-to-erasure (hashes PII contact data). | React Dashboard / API | `consent_records` audit log, DB update |
| `POST` | `/api/v1/customers/import`| Initiates async CSV import of contact data lists. | React Dashboard | Asynchronous CSV parser task, NATS |
| `POST` | `/api/v1/customers/{id}/consent` | Appends a new opt-in/opt-out status to consent ledger. | React Dashboard / API | `consent_records` write-only ledger |
| `GET` | `/api/v1/segments` | Lists defined dynamic/static customer segment groups. | React Dashboard | Database query |
| `POST` | `/api/v1/segments` | Creates a new segment with JSON condition definitions. | React Dashboard | Segment validator |

---

## 4. Workflow Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/workflows` | Lists all defined automation workflows. | React Dashboard | Database query |
| `POST` | `/api/v1/workflows` | Submits a workflow design configuration (JSON DSL). | React Dashboard | Workflow DSL validator |
| `PUT` | `/api/v1/workflows/{id}` | Modifies a workflow definition (re-validates DSL configuration). | React Dashboard | Workflow DSL validator |
| `POST` | `/api/v1/workflows/{id}/activate` | Activates the workflow. Registers NATS trigger hooks. | React Dashboard | Temporal workflow client |
| `POST` | `/api/v1/workflows/{id}/deactivate` | Pauses execution of the workflow. | React Dashboard | Temporal cancellation |
| `GET` | `/api/v1/workflows/{id}/executions` | Fetches historical runs and step durations for the workflow. | React Dashboard | `workflow_executions` query |

---

## 5. Messaging Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/messages/outbound` | Dispatches an outgoing WhatsApp template message manually. | Workflow Engine / API | Meta Cloud API, `consent_records` |
| `GET` | `/api/v1/messages` | Retrieves conversation log history (paginated). | React Dashboard | Partitioned `messages` table |
| `GET` | `/api/v1/conversations` | Lists current active support sessions. | React Dashboard | `conversations` table |
| `POST` | `/api/v1/conversations/{id}/assign` | Assigns an active support session to a platform agent. | React Dashboard | User context validation |
| `POST` | `/api/v1/templates` | Creates and submits a template structure to Meta. | React Dashboard | Meta Templates Graph API |
| `GET` | `/api/v1/templates` | Lists template catalogs synced from Meta. | React Dashboard | Database query |

---

## 6. Integrations Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/webhooks/shopify/orders` | Ingress Shopify order updates webhook. | Shopify SaaS Platform | Shopify HMAC verification, NATS event publisher |
| `POST` | `/webhooks/zoho/leads` | Ingress Zoho CRM lead changes webhook. | Zoho CRM Platform | Zoho validation, NATS event publisher |
| `POST` | `/webhooks/razorpay` | Ingress Razorpay billing events (updates sub status). | Razorpay Platform | Razorpay signature verification, plan updater |
| `POST` | `/webhooks/whatsapp` | Ingress WhatsApp webhooks (receives statuses/replies). | Meta Cloud Platform | HMAC signature check, NATS event publisher |

---

## 7. Analytics Domain

| Method | Path | Purpose | Consumer | Dependencies |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/analytics/embed-url` | Generates a signed JWT iframe URL for Metabase. | React Dashboard | JWT signature helper, Metabase locked params |
