# Dependency Inventory — Conductor Platform

This inventory lists the lowest-maintenance sourcing decisions for all required Conductor Platform OSS components, prioritizing official OCI container images and official Helm charts.

---

## 1. Identity & Access Management

### Keycloak
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `quay.io/keycloak/keycloak:24.0.0`
*   **Helm Chart:** `bitnami/keycloak` (version 22.x)
*   **Justification:** Official Quay image is optimized for production Quarkus-based runtime; Bitnami Helm chart provides native scaling properties.

### Dex
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `ghcr.io/dexidp/dex:v2.41.0`
*   **Helm Chart:** `dex/dex`
*   **Justification:** CNCF managed repository ensures secure container signatures.

### OAuth2 Proxy
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `quay.io/oauth2-proxy/oauth2-proxy:v7.8.1`
*   **Helm Chart:** `oauth2-proxy/oauth2-proxy`
*   **Justification:** Official image backed by the OAuth2 Proxy community.

---

## 2. Workflow Orchestration

### Temporal
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `temporalio/auto-setup:1.24.0` (Local) / `temporalio/server:1.24.0` (Production)
*   **Helm Chart:** `temporalio/temporal`
*   **Justification:** Official containers automate schema migration initialization.

### Camunda
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `camunda/zeebe:8.6.5`
*   **Helm Chart:** `camunda/camunda-platform`
*   **Justification:** Off-the-shelf Zeebe broker images for cluster replication.

### Kestra
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `kestra-io/kestra:v0.19.2`
*   **Helm Chart:** `kestra/kestra`
*   **Justification:** Official images optimized for lightweight Java executions.

---

## 3. Event & Messaging Platform

### NATS JetStream
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `nats:2.10-alpine`
*   **Helm Chart:** `nats/nats`
*   **Justification:** Official Alpine-based single-binary image minimizes resource utilization.

### Kafka
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `apache/kafka:3.9.0`
*   **Helm Chart:** `bitnami/kafka`
*   **Justification:** Official Apache images conform directly to KRaft specifications.

### Redpanda
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `redpanda-data/redpanda:v24.3.1`
*   **Helm Chart:** `redpanda/redpanda`
*   **Justification:** Official C++ Redpanda binary provides direct Kafka-compatibility without JVM footprint.

---

## 4. Databases, Caching & Analytics

### PostgreSQL
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `postgres:15-alpine`
*   **Helm Chart:** `bitnami/postgresql`
*   **Justification:** Lightweight alpine distribution is reliable for standard migrations.

### Redis
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `redis:7.4-alpine`
*   **Helm Chart:** `bitnami/redis`
*   **Justification:** Official alpine Redis caching store.

### ClickHouse
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `clickhouse/clickhouse-server:24.8-alpine`
*   **Helm Chart:** `altinity/clickhouse-operator`
*   **Justification:** Altinity Operator is the enterprise standard for ClickHouse clustering on Kubernetes.

### Metabase
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `metabase/metabase:v0.49.0`
*   **Helm Chart:** `metabase/metabase`
*   **Justification:** Official container JVM distribution runs standalone.

### Apache Superset
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `apache/superset:4.1.0`
*   **Helm Chart:** `superset/superset`
*   **Justification:** Official Apache distribution packages all Python/Celery dependencies cleanly.

---

## 5. AI Platforms & Vector Database

### Dify
*   **Acquisition Method:** Container Image
*   **OCI Source:** `langgenius/dify-api:0.6.9`, `langgenius/dify-web:0.6.9`, `langgenius/dify-sandbox:0.6.9`
*   **Justification:** Deploying official API, Web client, and secure Sandbox images directly.

### LangGraph
*   **Acquisition Method:** Git Dependency
*   **Source:** Python `pip install langgraph` / npm `npm install @langchain/langgraph`
*   **Justification:** Integrated as standard library dependencies inside backend service runtimes.

### OpenWebUI
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `ghcr.io/open-webui/open-webui:v0.5.8`
*   **Helm Chart:** `open-webui/open-webui`
*   **Justification:** Official client interface image.

### LiteLLM
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `ghcr.io/berriai/litellm:v1.59.8`
*   **Helm Chart:** `litellm/litellm`
*   **Justification:** Official Python-based stateless routing proxy container.

### Qdrant
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `qdrant/qdrant:v1.10.0`
*   **Helm Chart:** `qdrant/qdrant`
*   **Justification:** Rust vector database container.

### Weaviate
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `semitechnologies/weaviate:1.24.0`
*   **Helm Chart:** `weaviate/weaviate`
*   **Justification:** Go-based vector database container.

---

## 6. Observability Plane

### OpenTelemetry
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `otel/opentelemetry-collector-contrib:0.118.0`
*   **Helm Chart:** `open-telemetry/opentelemetry-collector`
*   **Justification:** Contrib distribution includes crucial SQL, Kafka, and NATS exporter plugins.

### Prometheus
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `prom/prometheus:v2.52.0`
*   **Helm Chart:** `prometheus-community/prometheus`
*   **Justification:** Official image is highly optimized for metric ingestion.

### Grafana
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `grafana/grafana:10.4.0`
*   **Helm Chart:** `grafana/grafana`
*   **Justification:** Official dashboard server.

### Loki
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `grafana/loki:3.3.0`
*   **Helm Chart:** `grafana/loki`
*   **Justification:** Grafana Labs official single-binary/microservices log storage engine.

### Tempo
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `grafana/tempo:2.7.0`
*   **Helm Chart:** `grafana/tempo`
*   **Justification:** Grafana Labs trace ingestion storage engine.

### Jaeger
*   **Acquisition Method:** Container Image / Helm Chart
*   **OCI Source:** `jaegertracing/all-in-one:1.65.0`
*   **Helm Chart:** `jaegertracing/jaeger`
*   **Justification:** All-in-one distribution is ideal for simple single-command debugging.
