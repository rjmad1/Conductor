# Developer Experience (DX) Standard — Conductor Platform

This standard defines the environment setup, local development workflow, CLI commands, and debugging tools designed to ensure frictionless developer onboarding and safe code verification.

---

## 1. Environment Setup & Requirements

### 1.1 Prerequisites
Developers and execution agents must configure their local workstations according to the following baseline requirements:
*   **Operating Systems:** Windows (using WSL2), macOS, or Linux.
*   **Memory Allocations:** Minimum 16GB of RAM allocated to the Docker VM (24GB recommended to run all container dependency blocks concurrently).
*   **Required Tools:** Git, Docker Desktop, Docker-Compose (v2 support), and Make.

### 1.2 Local Setup Sequence
To bootstrap a clean local machine configuration in under 30 minutes, execute the following sequence:

```bash
# 1. Clone the repository
git clone https://github.com/rjmad1/Conductor.git
cd Conductor

# 2. Run the bootstrap automation
make bootstrap
```
*Note: `make bootstrap` runs `./bootstrap.sh` (or `powershell -File ./bootstrap.ps1` under Windows) to perform memory availability checks, identify local port collisions, create configuration files, and initialize the docker container cluster.*

---

## 2. CLI Automation Standards

All platform automation tasks must route through standard `Make` commands to ensure consistency:

| Command | Action / Verification Target | Expected Success Criteria |
| :--- | :--- | :--- |
| `make bootstrap` | Boots configuration check and compose stack | Core environments initialized. |
| `make dev-up` | Starts all container dependency blocks | 100% active container stack. |
| `make health` | Checks API responses and database connectivity | Keycloak, Postgres, NATS, Qdrant print `[PASS]`. |
| `make test` | Runs ArchUnit and local test suites | Build compilation is successful. |
| `make clean` | Prunes containers and local build artifacts | Clean git status is restored. |

---

## 3. Local Development & Port Overrides

To mitigate local port conflicts on standard developer machines (e.g. database ports 5432, 6379, 8080), environment configs support dynamic port overrides:
*   **Local Properties File:** Create a `.env.local` config in the root. If present, the make scripts override default values with active entries:
    ```properties
    LOCAL_DB_PORT=5433
    LOCAL_KEYCLOAK_PORT=8082
    LOCAL_NATS_PORT=4223
    ```
*   **Egress Whitelisting:** Local connectors must access external endpoints via the outbound Squid proxy running locally. Egress routing rules block requests attempting to reach local loopback addresses or internal AWS subnets.

---

## 4. Debugging & Testing Protocols

### 4.1 Debug Settings
*   **Monolith Remote Debugging:** Spring Boot dev tasks run with remote debugger flags enabled on port `5005` by default:
    ```bash
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    ```
*   **Telemetry Trace Audits:** Developers can trace API transaction pathways by loading the Jaeger UI dashboard on `http://localhost:16686` to audit telemetry trace IDs.

### 4.2 Local Testing
*   **Mock Infrastructures:** Tests must utilize mock configurations or local testcontainers rather than querying global staging resources.
*   **Logs Masking Validation:** Verify that application logs do not record plaintext PII by testing Logback filter runs.
