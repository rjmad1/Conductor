# Healthcheck Specifications — Conductor Platform

This document details the health verification endpoints, diagnostic ports, and expected responses for the Conductor Platform.

---

## 1. Diagnostics Directory

The platform relies on `scripts/healthcheck.sh` and `scripts/healthcheck.ps1` to query service accessibility:

| Component | Target Address | Diagnostic Port | Expected Status |
| :--- | :--- | :--- | :--- |
| **PostgreSQL** | `127.0.0.1` | 5432 | TCP Connection accepted |
| **Redis** | `127.0.0.1` | 6379 | TCP Connection accepted |
| **NATS** | `127.0.0.1` | 4222 | TCP Connection accepted |
| **Temporal** | `127.0.0.1` | 7233 | TCP Connection accepted |
| **Keycloak** | `http://localhost:8080/health/ready` | 8080 | HTTP 200 |
| **Kong** | `http://localhost:8000` | 8000 | HTTP 404 (No route matches) |
| **ClickHouse** | `http://localhost:8123/ping` | 8123 | HTTP 200 |
| **Metabase** | `http://localhost:3000/api/health` | 3000 | HTTP 200 |
| **Qdrant** | `http://localhost:6333/readyz` | 6333 | HTTP 200 |
| **Grafana** | `http://localhost:3001/api/health` | 3001 | HTTP 200 |
| **Prometheus** | `http://localhost:9090/-/healthy` | 9090 | HTTP 200 |
| **Loki** | `http://localhost:3100/ready` | 3100 | HTTP 200 |
| **Tempo** | `http://localhost:3200/ready` | 3200 | HTTP 200 |
| **OTel Collector** | `http://localhost:13133/` | 13133 | HTTP 200 |

---

## 2. Automated Trigger

Run `make health` at the repository root to execute all checks. If any endpoint returns `FAIL`, the script will terminate with exit code `1`.
