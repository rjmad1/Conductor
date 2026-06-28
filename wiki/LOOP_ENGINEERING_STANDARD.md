# Loop Engineering Standard — Conductor Platform

This standard defines the lifecycle, execution model, metrics, and governance rules for AI agent execution loops within the Conductor platform. Every autonomous agent task must follow these non-negotiable operational boundaries.

---

## 1. Loop Lifecycle

Every execution loop must progress through five distinct, separate phases:

```
[ Generate ] ──► [ Evaluate ] ──► [ Decide ] ──► [ Learn ] ──► [ Scale ]
```

### 1.1 Generate
*   **Purpose:** Produce candidate system outputs (code edits, test cases, integrations, API schema designs).
*   **Constraint:** Agents must work in sandboxed, non-destructive workspaces (e.g. `platform/` submodules, isolated unit test branches) and avoid editing files outside their assigned scopes.

### 1.2 Evaluate
*   **Purpose:** Measure candidate quality, validation metrics, and safety compliance against predefined rubrics.
*   **Constraint:** The evaluation logic must execute independently from the generation code. Agents are prohibited from altering test expectations or lint standards during an active execution loop.

### 1.3 Decide
*   **Purpose:** Accept, reject, refine, or escalate the generated output.
*   **Gateways:** 
    *   *High-Confidence/Low-Risk:* Automated merge when unit tests and linting check out with $100\%$ validation scoring.
    *   *Borderline/Moderate-Risk:* Escalate to human reviewers via PR review hooks.
    *   *Low-Confidence/High-Risk:* Immediate halt and escalation.

### 1.4 Learn
*   **Purpose:** Capture execution outcomes, performance issues, and developer corrections to update organizational memory.
*   **Constraint:** Learnings must specify the context, outcome, rationale, and reproducible evidence. Unverified assumptions must not write to memory.

### 1.5 Scale
*   **Purpose:** Automate verified agent workflows once manual success criteria have been consistently met over multiple iterations.
*   **Constraint:** Do not scale loops that have not been manually validated and instrumented with clear operational indicators.

---

## 2. Loop Ownership & Roles

| Role | Responsibility | Authority / Scope |
| :--- | :--- | :--- |
| **Human Developer (Owner)** | Defines success conditions, goals, and evaluation rubrics. Approves memory promotion. | Workspace-wide / Arbitrator |
| **Execution Agent** | Executes the Generate and Evaluate steps. Proposes decisions. | Sandbox directories / Local scopes |
| **EDRB Gatekeeper** | Reviews escalated decisions, schema changes, and high-risk PR promotions. | Platform-wide / EDRB Board |

---

## 3. Loop Metrics

Agents and pipelines must measure outcomes rather than activity levels:

*   **Outcome Metrics:**
    *   *Success Accuracy Rate:* Ratio of accepted PRs to total proposed agent commits. Target: $\ge 85\%$.
    *   *Validation Defect Rate:* Percentage of agent-submitted code triggering post-merge bugs. Target: $0\%$.
    *   *Learning Delta:* Improvement in agent success rates on recurring tasks after memory ingestion.
*   **Prohibited Activity Metrics:**
    *   Do not use raw token usage, total lines written, or execution time as success indicators.

---

## 4. Loop Validation

Every loop execution must satisfy the following checks before promotion:
1.  **Linter Pass:** Formatting, imports, and syntax check must pass without warnings.
2.  **Compilation & Testing:** Code must compile clean and pass all local test suites (including ArchUnit boundary tests).
3.  **Security Scans:** Pre-commit hooks (GitLeaks) and container scans must return $0$ high-critical vulnerability flags.

---

## 5. Loop Escalation

Execution must pause and notify the Human Owner under the following escalation events:
*   **Uncertainty Events:** Agent confidence scoring falls below $75\%$ or matching patterns are ambiguous.
*   **Boundary Collisions:** The task requires altering configurations or files mapped as "Restricted" in the Agent Ownership Matrix (e.g. `docker-compose.local.yml`, Keycloak security files).
*   **Iterative Drift:** The loop fails to compile or pass tests after 3 consecutive refinement attempts.
*   **Metric Degradation:** Performance benchmarks (memory leak checks, database trigger latencies) fail verification.

---

## 6. Loop Retirement

An automated execution loop must be retired or paused if:
*   **Security Vulnerabilities:** A change in dependencies results in security checks failing continuously.
*   **API Deprecation:** Integration APIs (such as Meta WhatsApp Cloud API or Keycloak endpoints) undergo breaking changes.
*   **Operational Stash:** The loop experiences a drop in Success Accuracy Rate below $60\%$ over a rolling 10-run window.
