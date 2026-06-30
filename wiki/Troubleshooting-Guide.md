# Conductor Troubleshooting Guide

## A. Purpose
This troubleshooting guide acts as an incident resolution runbook to diagnose and remediate common platform runtime failures, including NATS stream lags, Temporal workflow locks, Keycloak authentication errors, and ClickHouse OLAP ingestion delays.

## B. Intended Audience
- Site Reliability Engineers (SREs)
- Technical Support Engineers
- Backend Developers on call

## C. Scope
Covers diagnostics for infrastructure, database sync pipelines, token verification steps, and task queues.

## D. Prerequisites
- Administrative CLI access to the target deployment namespace.
- Installed telemetry utilities: `nats-cli`, `tctl` (Temporal CLI), and database administration tools.

---

## E. Detailed Content

### 1. NATS JetStream Consumer Lags
#### Symptom:
Automated campaigns or webhook ingresses are delayed. High NATS consumer lag alerts trigger.

#### Diagnostics:
1. Log in to the application pod and check connection status:
   ```bash
   nats consumer report
   ```
2. Inspect the specific stream queue status:
   ```bash
   nats stream info integration
   ```
3. Locate active log messages in `customer-service` or `connector-service` pointing to `NatsConnectionManager` connection drops.

#### Remediation:
- **Consumer Restart**: If a consumer is stuck, recreate the JetStream consumer context:
  ```bash
  nats consumer delete integration <consumer-name>
  ```
  The Spring Boot service will auto-recreate the consumer group during restart.
- **Scale Consumers**: If throughput exceeds limits, scale up the Spring Boot subscriber pod replicas.

---

### 2. Temporal Workflow Failures
#### Symptom:
Workflows are stuck in "Running" state indefinitely or failing with activity timeouts.

#### Diagnostics:
1. Access the Temporal Web UI (Port `8233` locally).
2. Look up the specific tenant task queue (e.g. `workflow-<tenantId>`).
3. If workflows are locked in a pending loop, check for "Non-Deterministic Error" logs. This indicates that the workflow code definition was updated without using Temporal's `Workflow.getVersion` history guards, causing history replays to crash.

#### Remediation:
- **Deploy Workers**: Ensure that [TenantWorkerRegistrar](file:///c:/Users/rajaj/Projects/Conductor/platform/workflow/src/main/java/com/conductor/workflow/temporal/TenantWorkerRegistrar.java) is running active workers on the tenant’s queue.
- **Terminate Stuck Workflows**: For broken versions, terminate and replay:
  ```bash
  tctl workflow terminate --workflow_id <id> --reason "Deploy migration code fix"
  ```

---

### 3. Keycloak IAM Token Rejections
#### Symptom:
Clients receive `401 Unauthorized` or `403 Forbidden` responses for all REST API endpoints.

#### Diagnostics:
1. Check the Spring Monolith container logs for JWT signature validation failures:
   ```
   JwtDecodeException: The JWT signature verification failed
   ```
2. Verify that the Kong API Gateway declarative config `kong.yml` JWKS URL matches the Keycloak endpoint:
   `http://conductor-keycloak:8080/realms/conductor/.well-known/openid-configuration/jwks`
3. Confirm that the Keycloak instance is healthy and reachable from the Kong gateway container.

#### Remediation:
- If Keycloak reboots, verify OIDC connection caches.
- Clear the token validation caches in the Spring context by restarting the application gateway:
  ```bash
  docker-compose -f docker-compose.local.yml restart kong
  ```

---

### 4. ClickHouse Ingestion Backlogs
#### Symptom:
Real-time embedded metabase charts show stale metrics (lagging by >15 minutes).

#### Diagnostics:
1. Inspect ClickHouse logs for disk quota limits or table write-block errors.
2. Query ClickHouse ingestion performance:
   ```sql
   SELECT name, num_parts, active FROM system.parts WHERE table = 'conductor_events';
   ```
   An active part count > 300 indicates that ClickHouse is executing too many small inserts instead of batched uploads.

#### Remediation:
- Verify that [ClickHouseWriter](file:///c:/Users/rajaj/Projects/Conductor/platform/analytics/src/main/java/com/conductor/analytics/ingestion/ClickHouseWriter.java) configurations inside the Spring Boot application configuration map have `batch-size` set to at least 1,000 and `flush-interval-ms` to 5,000ms to allow proper batching.

---

## F. References
- [Operations Guide](Operations-Guide)
- [Data Model Guide](Data-Model-Guide)

## G. Related Wiki Pages
- [Developer & API Guide](Developer-and-API-Guide)
- [Security Guide](Security-Guide)
