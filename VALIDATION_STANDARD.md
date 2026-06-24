# Validation Standard — Conductor Platform

This standard defines the validation requirements, tools, and outcome states for verifying code changes and architectural proposals on the Conductor platform.

---

## 1. Mandatory Validation Types

Every proposed code change or environment configuration must undergo the following 8 validation checks:

| Validation Type | Verification Focus | Primary Verification Tools |
| :--- | :--- | :--- |
| **1. Architecture** | Verifies modular monolith boundaries, imports, and package separation rules. | ArchUnit compile tests, git call-graph audits. |
| **2. Security** | Audits code for secrets, CVE vulnerabilities, SQL injections, and egress routing. | GitLeaks, Semgrep, Checkov, Trivy. |
| **3. Code** | Verifies type safety, syntax, formatting, and formatting rules. | EditorConfig linter, Maven/npm build configurations. |
| **4. Integration** | Verifies communication between services, NATS events, and API interfaces. | Spring integration test suites, NATS mock streams. |
| **5. Performance** | Measures system latency, CPU, and DB triggers execution overhead. | Gatling load benchmarks, pgbench triggers testing. |
| **6. Observability** | Checks log formats, sensitive parameter masking, and trace contexts. | OpenTelemetry collector validations, Logback masker tests. |
| **7. Compliance** | Verifies local region storage rules (Mumbai) and consent checking logic. | Terraform subnet audits, consent API route testing. |
| **8. Documentation**| Ensures documentation reflects current code configurations and lists correct links. | Markdown linting suites, markdown link checkers. |

---

## 2. Outcome Status & Actions

The validation pipeline outputs one of four outcome states:

### 2.1 Pass
*   **Definition:** All validation tests execute cleanly. There are zero violations, warnings, or errors.
*   **Action:** The code proposal is approved. If autonomous merging is enabled (Tiers 0-1), changes merge automatically.

### 2.2 Fail
*   **Definition:** An error is encountered during test suite execution, or a validation limit is breached (e.g. compilation error or failing test).
*   **Action:** The pipeline terminates immediately. The agent must pause and refine the code candidate, or discard changes if the failure is unresolvable.

### 2.3 Warn
*   **Definition:** Code conventions are met, but non-critical violations are detected (e.g. missing docstrings or deprecated warning tags).
*   **Action:** The build passes, but details are logged in the pipeline report. The agent must log these items in the working memory log.

### 2.4 Escalate
*   **Definition:** Critical, ambiguous boundary failures are discovered (e.g. cross-tenant logic verification failure, unauthorized egress attempts, or port collisions).
*   **Action:** The execution halts immediately. The task state is saved, and a notification is sent to the Human Owner and EDRB with diagnostics.

---

## 3. Enforcement Matrix

Validation must run in the following sequence during the development cycle:

```
[ Pre-Commit: GitLeaks ] ──► [ Local Make test/verify ] ──► [ CI Build & Scans ] ──► [ CD Sandbox Validation ]
```
