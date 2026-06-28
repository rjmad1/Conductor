# Engineering Scorecard — Conductor Platform

This scorecard defines the key performance indicators (KPIs) and metrics used to evaluate engineering quality, architectural boundary health, security posture, operational reliability, learning velocity, and AI agent efficiency on the Conductor platform.

---

## 1. Scorecard Metric Categories

### 1.1 Engineering Metrics
*   **Code Review Acceptance Rate:** Percentage of PRs approved without structural rework. Target: $\ge 80\%$.
*   **Build Failure Rate:** Percentage of CI runs encountering compile or lint errors. Target: $\le 5\%$.
*   **Unit Test Coverage:** Percentage of codebase paths covered by automated test suites. Target: $\ge 85\%$.

### 1.2 Architecture Metrics
*   **Boundary Violation Rate:** Number of package import boundary violations flagged by ArchUnit tests. Target: $0$.
*   **State Leak Count:** Occurrences of services implementing stateful variables rather than deferring state to Temporal or NATS. Target: $0$.
*   **Schema Consistency Score:** Ratio of active database schemas matching registered API specifications. Target: $100\%$.

### 1.3 Security Metrics
*   **Committed Plaintext Secrets:** Plaintext credentials committed to the repository (flagged by GitLeaks). Target: $0$ (blocking check).
*   **Outbound Egress Violations:** Number of outbound web calls bypassing the Squid egress forward proxy. Target: $0$.
*   **High/Critical Vulnerabilities:** CVSS score $\ge 9.0$ dependencies in docker images (flagged by Trivy). Target: $0$ (blocking check).

### 1.4 Operational Metrics
*   **Database Trigger Latency Overhead:** Benchmark overhead introduced by trigger-based audit logging on transactional tables. Target: $\le 15\text{ms}$.
*   **Workflow Completion Rate:** Percentage of campaign workflows executing to completion without Temporal timeouts. Target: $\ge 99.5\%$.
*   **Trace Context Propagation Rate:** Ratio of API and event requests carrying valid OpenTelemetry trace context headers. Target: $100\%$.

### 1.5 Learning Metrics
*   **Memory Utility Score:** Ratio of agent working memory entries referenced by subsequent loops to solve recurring tasks. Target: $\ge 60\%$.
*   **Incident Recurrence Rate:** Frequency of identical build failures or linter errors recurring after retro ingestion. Target: $\le 5\%$.

### 1.6 Agent Metrics
*   **Success Accuracy Rate:** Ratio of accepted agent PRs to total proposed agent commits. Target: $\ge 85\%$.
*   **Refinement Loop Count:** Average number of refinement cycles (Generate $\to$ Evaluate $\to$ Decide) required to solve a task. Target: $\le 2.0$.
*   **Autonomy Violation Count:** Occurrences of agents editing files classified as "Restricted" without human approvals. Target: $0$.

---

## 2. Evaluation Scorecard Template

| Category | Metric Identifier | Metric Name | Current Score | Target KPI | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Engineering** | `MET-ENG-01` | Unit Test Coverage | -- | $\ge 85\%$ | Pending |
| **Architecture**| `MET-ARC-01` | Boundary Violations | -- | $0$ | Pending |
| **Security** | `MET-SEC-01` | Committed Secrets | -- | $0$ | Pending |
| **Operational** | `MET-OPS-01` | Trigger Audit Latency| -- | $\le 15\text{ms}$ | Pending |
| **Learning** | `MET-LRN-01` | Memory Utility | -- | $\ge 60\%$ | Pending |
| **Agent** | `MET-AGT-01` | Success Accuracy | -- | $\ge 85\%$ | Pending |

---

## LOOP-502 Health Assessment

### Overall Health
The platform architecture is fundamentally sound, adhering to a well-considered Modular Monolith pattern using Spring Boot and Loom. Key infrastructure choices (Temporal, NATS, PostgreSQL) provide high reliability.

### Current Weaknesses
- **Operational Complexity:** A heavy infrastructure footprint (12-16GB RAM locally) causes friction for engineers.
- **Compliance Gaps:** The 30-day contact erasure SLA under DPDP regulations is not yet fully automated.
- **Security Gaps:** Potential tenant isolation vulnerabilities in the Qdrant vector database layer.
- **Database Strain:** Heavy write loads on PostgreSQL master require offloading to ClickHouse.
