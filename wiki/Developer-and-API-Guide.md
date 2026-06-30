# Conductor Developer & API Guide

## A. Purpose
This guide helps software developers set up their local development environments, build and run Conductor services, and integrate with the platform APIs and event streams.

## B. Intended Audience
- Software Developers (Java / React)
- API Integration Engineers
- Third-Party Developers (ISVs)

## C. Scope
Covers local sandbox setup, REST API standards, Webhook ingress verification, NATS event schemas, and unit/integration testing standards.

## D. Prerequisites
- Docker & Docker-Compose installed.
- JDK 17 (CI compilation truth) or JDK 21 (Target runtime runtime).
- Gradle build tools.

---

## E. Detailed Content

### 1. Local Sandbox Setup
A full developer suite is configured to spin up local infrastructure in Docker.

1. **Spin Up Containers**:
   ```bash
   docker-compose -f docker-compose.local.yml up -d
   ```
   This starts PostgreSQL, Keycloak, Temporal, Redis, NATS JetStream, Kong, and Metabase.
2. **Keycloak Realm Import**:
   Keycloak auto-imports the `realm-export.json` on start (port `8080`).
3. **Compile Backend Services**:
   Validate that the codebase compiles using the Gradle wrapper:
   ```bash
   ./gradlew compileJava compileTestJava
   ```
4. **Boot Monolith Services**:
   Run services individually or as a group. For example, run the tenant service:
   ```bash
   ./gradlew :platform:tenant:bootRun
   ```

---

### 2. REST API Standards
Conductor APIs follow REST architectural design standards:
- Base path: `/api/v1`
- Request/Response payloads: `application/json`
- Dates and Timestamps: ISO-8601 UTC format (`YYYY-MM-DDTHH:mm:ssZ`)
- Standard Pagination Query Params: `page` (0-indexed) and `size` (default 20, max 100)

#### Authentication Headers:
Every API call requires a Bearer JWT token issued by Keycloak:
```http
GET /api/v1/tenants/00000000-0000-0000-0000-000000000000
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 3. Webhook Ingress & Normalization
External webhook callbacks are ingested via `platform:integrations` [WebhookIngressController](file:///c:/Users/rajaj/Projects/Conductor/platform/integrations/src/main/java/com/conductor/integrations/webhooks/WebhookIngressController.java).

- **Ingress Endpoints**: `POST /api/v1/integrations/webhooks/ingress/{connectorType}/{tenantId}`
- **Signature Verification**: Validates cryptographic signature headers (HMAC SHA-256 check) using the secret configured on the webhook subscription record.
- **Replay Protection**: Stores message hashes in Redis to reject duplicate events.
- **Normalization**: Normalized data is published to the NATS event bus.

#### Normalized NATS Envelope Format:
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "tenantId": "d0be649b-7345-42df-b769-cfad4589255a",
  "domain": "integration",
  "connector": "shopify",
  "action": "order_created",
  "version": "v1",
  "timestamp": "2026-06-24T12:00:00Z",
  "payload": {
    "order_id": "123456789",
    "total_price": "150.00",
    "email": "customer@email.com",
    "phone": "+919876543210"
  }
}
```

---

### 4. Testing Standards
Developers must run verification suites locally to guarantee test compliance.

- **Pyramid Coverage**: Code must maintain a minimum of 80% coverage.
- **Unit Tests**: Run mock-based fast tests:
  ```bash
  ./gradlew test --no-daemon
  ```
- **Integration Tests**: Leverage `Testcontainers` for database and event broker assertions. For example, [EventPlatformIntegrationTest.java](file:///c:/Users/rajaj/Projects/Conductor/shared/messaging/src/test/java/com/conductor/shared/messaging/EventPlatformIntegrationTest.java) checks connection logic against real NATS containers.
- **Architecture Validation**: Enforces package import constraints using ArchUnit. Review `ArchModuleBoundaryTest.java` in workflow/analytics projects to verify that code boundaries are not bypassed.

---

## F. References
- [Service Catalog](Service-Catalog)
- [Coding Standards](Coding-Standards)

## G. Related Wiki Pages
- [Implementation Guide](Implementation-Guide)
- [CI/CD Guide](CI-CD-Guide)
