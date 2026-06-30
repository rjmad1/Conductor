# Component Catalog

## A. Purpose
This component catalog lists the infrastructure services, versions, ports, and configuration parameters used by the Conductor platform, mapped directly from the local development compose files.

---

## B. Core Data Tier

### 1. PostgreSQL (Relational Core)
- **Image**: `postgres:15-alpine`
- **Container Name**: `conductor-postgres`
- **Internal Port**: `5432`
- **Volumes**: `postgres_data:/var/lib/postgresql/data`
- **Default Database**: `conductor_db`
- **Healthcheck**: `pg_isready -U conductor -d conductor_db`

### 2. Redis (Cache & Session)
- **Image**: `redis:7.4-alpine`
- **Container Name**: `conductor-redis`
- **Internal Port**: `6379`
- **Volumes**: `redis_data:/data`
- **Healthcheck**: `redis-cli ping`

### 3. ClickHouse (OLAP Stream)
- **Image**: `clickhouse/clickhouse-server:24.8-alpine`
- **Container Name**: `conductor-clickhouse`
- **Internal Ports**: `8123` (HTTP), `9000` (Native client)
- **Volumes**: `clickhouse_data:/var/lib/clickhouse`
- **Healthcheck**: `wget --spider -q http://localhost:8123/ping`

### 4. Qdrant (Vector DB)
- **Image**: `qdrant/qdrant:v1.10.0`
- **Container Name**: `conductor-qdrant`
- **Internal Ports**: `6333` (HTTP), `6334` (gRPC)
- **Volumes**: `qdrant_data:/qdrant/storage`
- **Healthcheck**: `curl -f http://localhost:6333/readyz`

---

## C. Middleware & IAM Tier

### 1. NATS JetStream (Asynchronous Event Broker)
- **Image**: `nats:2.10-alpine`
- **Container Name**: `conductor-nats`
- **Command**: `-js -p 4222 -m 8222` (JetStream enabled, HTTP monitoring enabled)
- **Internal Ports**: `4222` (Client), `8222` (HTTP monitoring)
- **Healthcheck**: `nc -z 127.0.0.1 4222`

### 2. Keycloak (OIDC IAM Provider)
- **Image**: `quay.io/keycloak/keycloak:24.0.0`
- **Container Name**: `conductor-keycloak`
- **Command**: `start-dev --import-realm`
- **Internal Port**: `8080`
- **Volumes**: Imports `realm-export.json` on boot
- **Depends On**: `postgres` (service healthy)

### 3. Temporal Server (Workflow Runtime)
- **Image**: `temporalio/auto-setup:1.24.0`
- **Container Name**: `conductor-temporal`
- **Internal Ports**: `7233` (gRPC server), `8233` (Web UI dashboard)
- **Environment**: Configured with postgres backend adapter
- **Depends On**: `postgres` (service healthy)

---

## D. Gateway & Analytics

### 1. Kong API Gateway
- **Image**: `kong:3.7-alpine`
- **Container Name**: `conductor-kong`
- **Internal Ports**: `8000` (HTTP proxy), `8001` (Admin API), `8443` (HTTPS proxy)
- **Config**: Declarative config imported from `kong.yml`
- **Database**: Off (DB-less mode)

### 2. Metabase (Embedded Reports)
- **Image**: `metabase/metabase:v0.49.0`
- **Container Name**: `conductor-metabase`
- **Internal Port**: `3000`
- **Database**: Uses postgres as application state database
- **Depends On**: `postgres` (service healthy)

---

## E. Observability Stack

- **Prometheus** (Port `9090`): Telemetry aggregator.
- **Grafana** (Port `3001`): Dashboard visualizer (provisioned datasources for Loki/Tempo).
- **Loki** (Port `3100`): Log aggregation server.
- **Tempo** (Port `3200`, `4317`): Distributed trace logging engine (OTel receiver).
- **OTel-Collector** (Port `13133`, `8888`): OpenTelemetry metrics parser pipeline.

---

## F. Related Pages
- [Service Catalog](Service-Catalog)
- [Operations Guide](Operations-Guide)
