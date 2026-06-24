# AI Assisted Development Standard — Conductor Platform

This standard defines the operational modes, context boundaries, prompt construction rules, validation requirements, and approval gates for AI-assisted development on the Conductor platform.

---

## 1. AI Agent Operating Modes

AI agents must operate in one of six defined modes, each carrying specific tool access and file permissions:

### 1.1 Planner Mode
*   **Focus:** Problem analysis, scoping, design proposals, and task decomposition.
*   **Permissions:** Read-only access to files and directory structures. Writing is restricted to planning artifacts (`spec.md`, `plan.md`, `implementation_plan.md`).
*   **Gate:** Human approval of the plan is required before transitioning to Executor Mode.

### 1.2 Executor Mode
*   **Focus:** Writing code modifications, implementing features, and creating test files.
*   **Permissions:** Write access to specific domain packages assigned in the Agent Ownership Matrix.
*   **Constraint:** Code modifications must align with the approved plan.

### 1.3 Reviewer Mode
*   **Focus:** Auditing code modifications, linting check verification, and test coverage assessments.
*   **Permissions:** Read-only access.
*   **Constraint:** Reviews must score proposed code against the target scorecard.

### 1.4 Architect Mode
*   **Focus:** Defining domain boundaries, package layouts, and ADR compliance.
*   **Permissions:** Write access restricted to the `/ADRs/` folder and `/docs/` configuration directories.

### 1.5 Refactoring Mode
*   **Focus:** Simplifying code structures, removing technical debt, and updating libraries safely.
*   **Permissions:** Write access to target codebase modules.
*   **Constraint:** Modifications must be followed by immediate regression tests.

### 1.6 Security Mode
*   **Focus:** Threat scanning, secret leak detection, checking Checkov rules compliance, and egress routing audits.
*   **Permissions:** Read-only access to codebase files. Writes restricted to security validation artifacts.

---

## 2. Context Boundaries

To optimize performance and prevent token overflow:
*   **Narrow Scopes:** Prompts must feed agents only the direct files and symbols required for the micro-task. Eagerly dumping unrelated packages or log files is prohibited.
*   **Just-in-Time Retrieval:** Agents must search the codebase using ripgrep or CodeGraph explore to find references dynamically rather than preloading entire modules.
*   **Compaction Rules:** When context thresholds are breached, agents must compact their working memory to retain only changed files, key decisions, remaining subtasks, and validation outcomes.

---

## 3. Prompt Construction Rules

Prompts constructed for LLMs or agents must contain:
1.  *Objective:* A one-line measurable outcome.
2.  *Context References:* Links to relevant spec files or dependencies.
3.  *Constraints:* Specific coding rules (e.g. "Use virtual threads; enforce logical tenancy").
4.  *Validation Requirements:* The specific test commands that will be run.
5.  *Acceptance Criteria:* The checklist elements that must be satisfied.

---

## 4. Verification Rules & Approval Gates

No agent code proposal may merge to the main branch without passing the following gates:

```
[ Code Generation ] ──► [ Lint & Compile ] ──► [ Test Suite Pass ] ──► [ Human Review ] ──► [ Merge ]
```

1.  **Compilation Gate:** Code must compile cleanly with $0$ linter warnings.
2.  **Test Gate:** Automated integration and unit tests must verify $100\%$ success.
3.  **Human Gate:** Code changes must be submitted via PR and receive manual sign-off from a developer or EDRB board member depending on the risk score tier.
