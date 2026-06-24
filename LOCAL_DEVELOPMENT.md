# Local Development Guide — Conductor Platform

This guide outlines developer workflows, workspace directories, container setups, and tooling configurations for local platform development.

---

## 1. Directory Segregation & Boundaries

All configuration assets, infrastructure overrides, and telemetry configurations reside inside the `/workspace` folder:
*   `workspace/infrastructure/helm/`: Helm values files per environment.
*   `workspace/platform/identity/`: Keycloak realms config and Kong configs.
*   `workspace/observability/`: Otel collector, Loki, Tempo, Prometheus, and Grafana provision assets.

Microservices code bases reside under the `/src` folder at the root.

---

## 2. Start & Stop Environment

To command the local Docker Compose backend stack:
*   **Start Stack:** `make start` (starts all 14 backend systems in background)
*   **Stop Stack:** `make stop` (stops containers without destroying volumes data)
*   **View Logs:** `make logs` (shows real-time stream logs)
*   **Hard Reset:** `make reset` (deletes volumes, rebuilds state, runs bootstrap checks)

---

## 3. Developer Tooling Integration

A custom `.devcontainer/devcontainer.json` configuration is provided. Opening the workspace inside VS Code DevContainers mounts:
*   Java Development Kit (JDK 17)
*   Go and Python compilers
*   `docker`, `kubectl`, and `helm` CLIs
*   `temporal` and `nats` network query CLIs
*   `otel-cli` for trace assertions.
