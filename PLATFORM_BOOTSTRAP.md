# Platform Bootstrap Guide — Conductor Platform

This guide explains how to bootstrap the Conductor developer platform environment from a clean machine state.

---

## 1. System Prerequisites

Before running the bootstrap automation, make sure you have installed:
*   **Docker Desktop** (version 24.0.0 or later) with Docker Compose v2 support.
*   **Git** client.
*   **System Resources:** Min 16GB RAM allocated to Docker VM (24GB recommended to run all services concurrently).

---

## 2. One-Command Bootstrap Execute

Run the following command at the repository root:

### Bash (macOS/Linux/WSL2)
```bash
make bootstrap
```
*Under the hood, this executes `./bootstrap.sh` checking RAM constraints, port collisions, dependencies, provisioning configs, and starting the Compose cluster.*

### PowerShell (Windows Native)
```powershell
powershell -File ./bootstrap.ps1
```

---

## 3. Post-Bootstrap Validation

Once the container stack is active, verify connectivity to OIDCs, relational databases, caches, and gateways:
```bash
make health
```
All diagnostic targets (Keycloak, Postgres, NATS, Qdrant, Prometheus, etc.) should print `[PASS]`.
