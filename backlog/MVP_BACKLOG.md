# MVP Backlog — Conductor

This document details the product backlog for the Conductor MVP. It follows the hierarchy: Epic ➔ Feature ➔ Story ➔ Task.

---

## EPIC-001: Tenant & Identity Foundation

### FEAT-101: Multi-Tenancy Resolution & Authentication
*   **Description:** Setup gateway-level tenant verification and identity isolation.

#### STORY-101: API Gateway Tenant Injection
*   **Task T-101: Setup Kong config and JWT decrypt plugin**
    *   **Description:** Configure Kong gateway to parse incoming Keycloak JWTs, extract the `tenant_id` claim, and inject it as `X-Tenant-ID` downstream.
    *   **Dependencies:** None
    *   **Estimate:** S
    *   **Risk:** Low
    *   **Acceptance Criteria:** A request with a valid JWT has the header `X-Tenant-ID` injected; requests with missing/invalid JWTs are rejected at the gateway.
*   **Task T-102: Spring Tenant Context Interceptor**
    *   **Description:** Write a Spring Web filter that extracts `X-Tenant-ID` from request headers, binds it to a thread-local context, and registers it with the Hibernate session filter.
    *   **Dependencies:** T-101
    *   **Estimate:** S
    *   **Risk:** Low
    *   **Acceptance Criteria:** Hibernate SQL select statements automatically append `tenant_id = :currentTenantId`.

#### STORY-102: Keycloak Identity Provider Integration
*   **Task T-103: Keycloak Realm Configuration**
    *   **Description:** Set up realm templates, client definitions, and default role maps (`OWNER`, `ADMIN`, `MANAGER`, `AGENT`) in Keycloak.
    *   **Dependencies:** None
    *   **Estimate:** M
    *   **Risk:** Medium (Configuration complexity)
    *   **Acceptance Criteria:** Test realm provisioned; user creation and credentials validation perform successfully via standard OIDC endpoint.
*   **Task T-104: Spring Security Filter Chain Setup**
    *   **Description:** Implement Spring Security OAuth2 resource server filters, verifying Keycloak signatures (RS256) and mapping roles to path permissions.
    *   **Dependencies:** T-103
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** API endpoints restrict access based on user role annotations (e.g. `@PreAuthorize("hasRole('ADMIN')")`).

---

## EPIC-002: Customer Registry & Compliance

### FEAT-201: Unified Contact Registry & Segment Builder
*   **Description:** Create customer tables, bulk upload capabilities, and segmentation queries.

#### STORY-201: Customer Profile API & Bulk Import
*   **Task T-201: Customer CRUD Database Migrations & Controllers**
    *   **Description:** Implement `customers` table DDL, JPA mappings, and REST API CRUD endpoints.
    *   **Dependencies:** T-102
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** Customers can be created, updated, and listed. Tenant queries return only current tenant customers.
*   **Task T-202: Asynchronous CSV Contact Parser**
    *   **Description:** Develop a parser handling CSV file uploads. Executes parsing in a background thread, writing contacts to PostgreSQL in batches.
    *   **Dependencies:** T-201
    *   **Estimate:** L
    *   **Risk:** Medium (Heavy files can saturate RAM)
    *   **Acceptance Criteria:** Importing a CSV list containing 10,000 rows executes asynchronously in < 10 seconds without memory leaks.

#### STORY-202: DPDP India 2023 Compliance
*   **Task T-203: Immutable Consent Trigger**
    *   **Description:** Write database migration setting up the `consent_records` table and database triggers to block all `UPDATE` and `DELETE` requests.
    *   **Dependencies:** T-201
    *   **Estimate:** S
    *   **Risk:** Low
    *   **Acceptance Criteria:** Issuing an UPDATE or DELETE query against `consent_records` results in a database exception error.
*   **Task T-204: Scheduled Contact Anonymization Task**
    *   **Description:** Implement a scheduled Spring Boot worker that processes erasure requests and overwrites name, email, and phone fields with random hashes.
    *   **Dependencies:** T-201, T-203
    *   **Estimate:** M
    *   **Risk:** Medium (Preserving statistics while erasing raw identity)
    *   **Acceptance Criteria:** Contact PII is erased; campaign and workflow count metrics remain unchanged.

---

## EPIC-003: Messaging Engine & Webhooks

### FEAT-301: WhatsApp Official Cloud Channel
*   **Description:** Webhook ingestion adapters and outbound Graph API wrappers.

#### STORY-301: Inbound Webhook Ingestion
*   **Task T-301: Node.js Webhook Adapter**
    *   **Description:** Implement a Node.js API verifying Meta's webhook HMAC signature, extracting delivery status or message body, publishing to NATS JetStream, and returning HTTP 200.
    *   **Dependencies:** None
    *   **Estimate:** L
    *   **Risk:** High (Peak traffic spikes from Meta)
    *   **Acceptance Criteria:** Handles webhook signatures, publishes events to NATS within < 1 second.
*   **Task T-302: Priority STOP Unsubscribe Interceptor**
    *   **Description:** Write keyword parsing rules in the Node.js adapter. If matching "STOP", invoke contact opt-out update in PostgreSQL directly.
    *   **Dependencies:** T-301, T-203
    *   **Estimate:** M
    *   **Risk:** High (Strict 5-second compliance rule)
    *   **Acceptance Criteria:** Replying "STOP" updates contact consent status in PostgreSQL in < 5 seconds.

#### STORY-302: Outbound campaign dispatcher
*   **Task T-303: Outbound Meta API Client**
    *   **Description:** Build the Spring Boot HTTP Graph API wrapper to send template messages.
    *   **Dependencies:** T-102
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** Test messages deploy to verified phones, status events populate the database log.
*   **Task T-304: Campaign Consent Gate Interceptor**
    *   **Description:** Create a pre-send validation step querying the contact's consent status.
    *   **Dependencies:** T-303, T-203
    *   **Estimate:** S
    *   **Risk:** Medium (Accidental spam risk)
    *   **Acceptance Criteria:** Prevents message dispatch if contact wa_opt_in_status is not opted_in.

---

## EPIC-004: Workflow Runtime & Orchestration

### FEAT-401: Workflow DSL Engine & Temporal Orchestrator
*   **Description:** Parse campaign configurations and coordinate long-running stateful executions.

#### STORY-401: Workflow DSL Parser
*   **Task T-401: JSON DSL Parser & Schema Validator**
    *   **Description:** Create a validation class ensuring workflow design payloads match triggers, condition boundaries, and valid action nodes.
    *   **Dependencies:** T-102
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** Valid JSON DSL is accepted, invalid syntax is rejected with details of the constraint violation.

#### STORY-402: Temporal Workers Setup
*   **Task T-402: Temporal Spring Boot Setup**
    *   **Description:** Initialize the Temporal Java SDK client, configuring namespaces and worker loops.
    *   **Dependencies:** T-401
    *   **Estimate:** L
    *   **Risk:** High (Temporal cluster deployment complexity)
    *   **Acceptance Criteria:** Local Spring Boot workers register successfully with the Temporal Server task queue.
*   **Task T-403: Workflow Activities Implementation**
    *   **Description:** Code the worker task activity implementations (`SendWhatsAppActivity`, `EvaluateConditionActivity`).
    *   **Dependencies:** T-402, T-303, T-201
    *   **Estimate:** L
    *   **Risk:** Medium (Dynamic variable injections)
    *   **Acceptance Criteria:** Executing a multi-step Temporal workflow successfully parses variables and executes activities in order.

---

## EPIC-005: Integrations & Payments

### FEAT-501: Egress Proxies and Core Connectors
*   **Description:** Secure outbound traffic boundaries and handle Shopify, Zoho, and Razorpay interactions.

#### STORY-501: Webhook Egress Security
*   **Task T-501: Squid Forward Proxy Configuration**
    *   **Description:** Deploy a Squid proxy instance with rules rejecting private cloud ranges, and routing monolith outbound API connections through it.
    *   **Dependencies:** None
    *   **Estimate:** S
    *   **Risk:** Medium (Blocks integration calls if misconfigured)
    *   **Acceptance Criteria:** Monolith HTTP client calls target public endpoints successfully, attempts targeting localhost or VPC IP blocks return a 403 Forbidden.

#### STORY-502: Shopify & Zoho CRM Connectors
*   **Task T-502: Shopify Ingress Webhook Controller**
    *   **Description:** Create webhook receiver validating Shopify signatures, and publishing order updates as NATS events.
    *   **Dependencies:** T-102, T-402
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** Valid Shopify payloads publish events; invalid signatures return HTTP 401.
*   **Task T-503: Zoho CRM Auth & Lead Sync Adapter**
    *   **Description:** Implement Zoho OAuth2 token management. Expose lead sync endpoints updating the Customer Registry.
    *   **Dependencies:** T-102, T-201
    *   **Estimate:** L
    *   **Risk:** Medium (Token expiration handling)
    *   **Acceptance Criteria:** Contacts update automatically when Zoho leads trigger updates.

#### STORY-503: Razorpay Subscriptions
*   **Task T-504: Razorpay Billing Webhook Integration**
    *   **Description:** Implement webhook receiver processing payment captures and updates tenant subscription plans.
    *   **Dependencies:** T-102
    *   **Estimate:** M
    *   **Risk:** Low
    *   **Acceptance Criteria:** Invoice is marked paid and tenant plan level upgrades automatically on receiving subscription payment confirmations.
