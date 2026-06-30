# Configuration Guide

## A. Purpose
This page documents the environment variables and application configurations required to boot and run Conductor services, parsed directly from the system configurations.

---

## B. Core Environment Variables

These variables must be injected into target containers at runtime:

### 1. Database Connections
- **`SPRING_DATASOURCE_URL`**: Transaction database connection string.
  - *Example*: `jdbc:postgresql://postgres:5432/conductor_db`
- **`SPRING_DATASOURCE_USERNAME`**: PostgreSQL username.
  - *Example*: `conductor`
- **`SPRING_DATASOURCE_PASSWORD`**: PostgreSQL access password.
  - *Example*: `conductor_password`

### 2. IAM & Authentication (Keycloak)
- **`KEYCLOAK_SERVER_URL`**: Core Keycloak server instance HTTP address.
  - *Example*: `http://keycloak:8080`
- **`KEYCLOAK_ADMIN_USERNAME`**: Admin API operations username.
  - *Example*: `admin`
- **`KEYCLOAK_ADMIN_PASSWORD`**: Admin API access credentials password.
  - *Example*: `admin_password`

### 3. Asynchronous Messaging (NATS)
- **`NATS_SERVER_URL`**: JetStream message broker server URL.
  - *Example*: `nats://nats:4222`

### 4. Workflow Orchestration (Temporal)
- **`TEMPORAL_SERVICE_ADDRESS`**: gRPC server address for Temporal Client connections.
  - *Example*: `temporal:7233`
- **`TEMPORAL_NAMESPACE`**: Target execution namespace.
  - *Default*: `conductor`

### 5. Outbound HTTP Egress (Squid Proxy)
- **`INTEGRATION_PROXY_ENABLED`**: Toggle to route egress integration requests through Squid.
  - *Default*: `false` (local/dev), `true` (prod/stage)
- **`INTEGRATION_PROXY_HOST`**: Forward proxy hostname.
  - *Example*: `squid`
- **`INTEGRATION_PROXY_PORT`**: Egress proxy listener port.
  - *Default*: `3128`

### 6. Analytics Tier (ClickHouse & Metabase)
- **`CLICKHOUSE_URL`**: JDBC ClickHouse OLAP connection URL.
  - *Example*: `jdbc:ch://clickhouse:8123/default`
- **`METABASE_EMBEDDING_SECRET`**: Cryptographic secret key used to generate signed iframe JWT widgets.
  - *Example*: `change-me-in-production-long-secret-key`

---

## C. Related Pages
- [Component Catalog](Component-Catalog)
- [Operations Guide](Operations-Guide)
