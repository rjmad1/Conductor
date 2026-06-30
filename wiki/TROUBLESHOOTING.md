# Troubleshooting Guide — Conductor Platform

This guide outlines common issues, diagnostic scripts, and remediation patterns for the local Conductor Platform.

---

## 1. Port Collisions

*   **Symptoms:** Containers fail to bind to host network interfaces, printing: `bind: address already in use`.
*   **Remediation:** 
    1. Check which process is occupying the target port (e.g. `netstat -ano` on Windows or `lsof -i :<PORT>` on macOS/Linux).
    2. Shut down any local instances of PostgreSQL, Redis, or Keycloak.
    3. Rerun `make bootstrap` to verify port state.

---

## 2. Resource Starvation

*   **Symptoms:** Docker Desktop containers exit unexpectedly with code `137` (Out Of Memory).
*   **Remediation:**
    1. Open Docker Desktop Settings -> Resources.
    2. Increase allocated memory to at least 16GB (preferably 24GB).
    3. Run `make restart` to reset container state.

---

## 3. Configuration & Authentication Issues

*   **Symptoms:** Kong proxy returns `502 Bad Gateway` or `OIDC token validation failed` on downstream endpoints.
*   **Remediation:**
    1. Verify Keycloak availability via `http://localhost:8080/health/ready`.
    2. Confirm realm-export files were successfully loaded by analyzing Keycloak container logs: `docker compose logs keycloak`.
    3. Re-run `make reset` to rebuild clean database volume tables and reimport configurations.
