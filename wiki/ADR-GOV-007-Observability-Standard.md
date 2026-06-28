# ADR-GOV-007: Observability Standard

## Status
ACCEPTED

## Context
A production platform must be observable to maintain high availability, detect performance bottlenecks, and verify service level objectives (SLOs).

## Decision
We enforce the following observability standards:
1.  **Metrics**: Services must expose metrics in Prometheus format. Essential metrics include the Golden Signals (Latency, Traffic, Errors, Saturation), plus Temporal workflow execution rates and NATS message queue lags.
2.  **Distributed Tracing**: OpenTelemetry must be integrated. Every internal and external HTTP request must include trace headers to map spans in a tracing platform (Jaeger/Zipkin).
3.  **Dashboards**: Standardized Grafana dashboards must be created for each microservice.

## Rationale
*   **System Visibility**: Speeds up incident resolution by identifying the exact service or queue bottleneck.
*   **SLO Tracking**: Allows continuous monitoring of uptime and latency budgets.

## Consequences
*   Services must include the Spring Boot Actuator or equivalent telemetry library.
*   Central monitoring infra (Prometheus + Grafana) is required in the deployment environment.
