# Developer Onboarding — Conductor IDE Knowledge Pack

**Purpose:** Everything a new engineer needs to go from zero to productive in one day.

---

## Prerequisites

Before starting, ensure you have:

- **Java 21** (`java -version` should show 21+)
- **Node.js 20 LTS** (`node -v` should show v20+)
- **Docker Desktop** (latest stable)
- **Git** with SSH key configured for GitHub
- **IntelliJ IDEA** (recommended) or VS Code with Java Extension Pack
- Access to the team's AWS account (ask founder)
- Access to Conductor's GitHub organization (ask founder)
- Invitation to team Slack workspace

---

## Repository Setup

```bash
# Clone the monorepo
git clone git@github.com:your-org/conductor-platform.git
cd conductor-platform

# Clone documentation (this repo)
git clone git@github.com:your-org/conductor-docs.git

# Clone connectors
git clone git@github.com:your-org/conductor-connectors.git
```

---

## Local Environment: Start Infrastructure

All infrastructure runs via Docker Compose. This brings up: PostgreSQL, Redis, NATS, Keycloak, Temporal Server, and Kong.

```bash
cd conductor-platform/infrastructure/docker-compose
docker compose up -d

# Verify all services are healthy
docker compose ps
```

Expected services running:
- `postgres` on port 5432
- `redis` on port 6379
- `nats` on port 4222 (client), 8222 (monitoring)
- `keycloak` on port 8180
- `temporal` on port 7233 (gRPC), 8233 (web UI)
- `kong` on port 8000 (proxy), 8001 (admin)

---

## Database Setup

```bash
# Run Flyway migrations for all services
cd conductor-platform
./gradlew :services:tenant-service:flywayMigrate
./gradlew :services:customer-service:flywayMigrate
./gradlew :services:workflow-service:flywayMigrate
# ... repeat for each service

# Or: migrate all at once (if gradle task exists)
./gradlew flywayMigrateAll

# Seed development data
./gradlew :services:tenant-service:seedDevData
```

---

## Environment Variables

Copy the template and fill in development values:

```bash
cp conductor-platform/.env.example conductor-platform/.env
```

Required variables (development values, never commit real secrets):

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=conductor_dev
DB_USER=conductor_dev
DB_PASSWORD=dev_password_only

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# NATS
NATS_URL=nats://localhost:4222

# Keycloak
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=conductor-admin
KEYCLOAK_CLIENT_ID=conductor-backend
KEYCLOAK_CLIENT_SECRET=dev-secret-only

# Temporal
TEMPORAL_HOST=localhost
TEMPORAL_PORT=7233
TEMPORAL_NAMESPACE=conductor-dev

# WhatsApp (use sandbox for development)
WHATSAPP_API_BASE_URL=https://graph.facebook.com/v18.0
WHATSAPP_API_TOKEN=<from-meta-developer-dashboard>
WHATSAPP_PHONE_NUMBER_ID=<sandbox-phone-number-id>
WHATSAPP_WABA_ID=<sandbox-waba-id>
WHATSAPP_WEBHOOK_SECRET=<generate-random-secret>
```

---

## Running a Service

```bash
# Run a specific service
cd conductor-platform/services/workflow-service
./gradlew bootRun

# Or from IntelliJ: Open the project, find the main class, run it
# Main class: io.conductor.workflow.WorkflowServiceApplication

# The service will start on its configured port (e.g., workflow-service on 8083)
```

---

## Running the Frontend

```bash
cd conductor-platform/apps/web
npm install
npm run dev

# Frontend runs at http://localhost:3000
# API requests proxy to Kong at http://localhost:8000
```

---

## Running the Temporal Worker

The Temporal worker executes all workflow activities. It must be running for any workflow execution to work.

```bash
cd conductor-platform/workers/workflow-worker
./gradlew bootRun

# Worker connects to Temporal at localhost:7233
# Watch for "Worker started" log line
```

---

## Verifying Your Setup

Send a test WhatsApp message (sandbox):

```bash
# 1. Start: tenant-service, customer-service, workflow-service, whatsapp-adapter, workflow-worker

# 2. Create a test tenant via API
curl -X POST http://localhost:8000/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Business", "email": "test@example.com"}'

# 3. Log in with the test tenant owner credentials
# Keycloak admin console: http://localhost:8180/admin (admin/admin)

# 4. Create a test customer with opt-in
curl -X POST http://localhost:8000/api/v1/customers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"phone": "+919999999999", "name": "Test Customer", "wa_opt_in_status": "opted_in"}'

# 5. Create and activate a simple workflow
# (See: docs/tutorials/first-workflow.md)

# 6. Trigger the workflow manually
# Temporal web UI: http://localhost:8233
```

---

## Code Structure

Every Spring Boot service follows this package structure:

```
io.conductor.{service}/
├── api/                    # REST controllers, request/response DTOs
│   ├── {Entity}Controller.java
│   └── dto/
├── domain/                 # Domain entities (JPA @Entity classes)
│   └── {Entity}.java
├── service/                # Business logic
│   └── {Entity}Service.java
├── repository/             # JPA repositories
│   └── {Entity}Repository.java
├── event/                  # NATS event publishers and consumers
│   ├── {Entity}EventPublisher.java
│   └── {Entity}EventConsumer.java
├── client/                 # Feign clients for other services
│   └── {Service}Client.java
└── config/                 # Spring configs, beans
    └── AppConfig.java
```

---

## Adding a New API Endpoint

1. Define the request/response DTO in `api/dto/`
2. Add the endpoint to the controller in `api/`
3. Add the business logic to the service in `service/`
4. Write a unit test in `src/test/java/.../service/`
5. Write an integration test in `src/test/java/.../api/`
6. Update `shared/api-contracts/` OpenAPI spec

---

## Adding a New Database Table

1. Create a Flyway migration: `src/main/resources/db/migration/V{next}__description.sql`
2. Ensure the table has `tenant_id UUID NOT NULL REFERENCES tenants(id)`
3. Ensure the table has `created_at`, `updated_at`, and `deleted_at` columns
4. Create the JPA entity in `domain/`
5. Create the Spring Data repository in `repository/`
6. Add repository test coverage

---

## Adding a New NATS Event

1. Define the event schema in `05-Engineering/Event-Contracts.md`
2. Create the event class in `shared/api-contracts/src/main/java/.../events/`
3. Add publisher in the producing service: `event/{Domain}EventPublisher.java`
4. Add consumer in the consuming service: `event/{Domain}EventConsumer.java`
5. Register the NATS subject in the consumer configuration

---

## Testing Standards

```
src/test/java/io/conductor/{service}/
├── service/            # Unit tests (mock repositories)
├── api/                # Integration tests (MockMvc, real DB in-memory)
└── event/              # Event consumer tests
```

Test naming convention:
```java
@Test
void {methodUnderTest}_when{Condition}_then{ExpectedBehavior}() {
    // GIVEN
    // ...

    // WHEN
    // ...

    // THEN
    // ...
}
```

Run tests:
```bash
./gradlew test               # All tests
./gradlew :services:workflow-service:test  # Service-specific
```

Coverage threshold: 70% minimum (enforced in CI).

---

## First Week Checklist

- [ ] Repository access: conductor-platform, conductor-docs, conductor-connectors
- [ ] Local infrastructure running (docker compose up)
- [ ] At least one service running locally
- [ ] Frontend running locally
- [ ] Read `11-IDE-Knowledge-Pack/System-Overview.md`
- [ ] Read `11-IDE-Knowledge-Pack/Context-Pack.md` (the 10 rules)
- [ ] Read `07-Governance/Decision-Records.md` (ADRs)
- [ ] Read `04-Architecture/Solution-Architecture.md`
- [ ] Set up IntelliJ with code style settings (checkstyle config in repo root)
- [ ] Run full test suite: `./gradlew test` (should be green)
- [ ] Assigned first task in GitHub Issues

---

## Getting Help

- **Architecture questions:** `04-Architecture/` directory in conductor-docs
- **Domain questions:** `11-IDE-Knowledge-Pack/Domain-Model.md` and `Glossary.md`
- **API reference:** `05-Engineering/API-Contracts.md` or OpenAPI specs at `/docs/api` when services are running
- **Runbooks (production):** `06-Operations/Runbooks.md`
- **ADRs (why we made a decision):** `07-Governance/Decision-Records.md`
- **Slack channels:** `#engineering` (general), `#backend`, `#frontend`, `#infra`
