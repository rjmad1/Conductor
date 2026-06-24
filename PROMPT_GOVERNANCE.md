# Prompt Governance Standard — Conductor Platform

This standard establishes prompt versioning, testing, and lifecycle policies, ensuring that instructions for AI agents and LLM prompts are treated as first-class codebase assets.

---

## 1. Prompt Lifecycle

All prompts must transition through a standardized lifecycle to prevent regression and ensure traceability:

```
[ Draft ] ──► [ Proposed ] ──► [ Approved ] ──► [ Deprecated ] ──► [ Retired ]
```

*   **Draft:** The prompt is created or modified by a developer or agent in an experiment branch.
*   **Proposed:** The prompt is submitted via Pull Request, accompanied by evaluation benchmark runs and validation evidence.
*   **Approved:** EDRB or Tech Lead approves the PR. The prompt is tagged with a SemVer version and written to `/prompts`.
*   **Deprecated:** The prompt remains active but is flagged for replacement. Stale or duplicate templates are scheduled for removal.
*   **Retired:** The prompt file is removed from active paths. Any invocation calls fail compile-time validation.

---

## 2. Prompt Review & Approval Process

All prompts must be version-controlled in the `/prompts` folder. Modifying a prompt requires:
1.  **Peer Review:** Standard code review rules apply. Reviewers audit for instruction compliance, potential injection vectors, and model compatibility.
2.  **EDRB Gate:** Changes to critical system prompts (e.g. `AGENTS.md` and core agent instructions) require two human senior leads / EDRB approval.
3.  **No Direct Overwrites:** Prompts must be versioned separately. A prompt named `generate_adapter_v1.md` must not be overwritten; updates require creating `generate_adapter_v2.md` or incrementing the patch level in the manifest.

---

## 3. Prompt Testing

Prompts must undergo automated testing before promotion:
*   **Regression Suite:** The proposed prompt must process a standard validation dataset to verify outputs remain consistent with requirements.
*   **Boundary Audits:** Test prompts against edge-case scenarios, malicious input parameters, and cross-tenant forgery triggers.
*   **Linting Checks:** Check markdown structure, verifying all paths and links compile correctly and that variables match defined configurations.

---

## 4. Prompt Metrics

The performance and reliability of prompts are evaluated using the following scorecard metrics:

*   **Instruction Adherence Rate:** Percentage of agent execution runs that fully conform to output schemas and constraints. Target: $\ge 95\%$.
*   **Output Variance (Semantic Stability):** Measure semantic deviation across multiple runs. High variance triggers retirement.
*   **Cost Efficiency Ratio:** Ratio of tokens consumed to successful execution outcomes. Prompts exceeding efficiency budgets are flagged for refactoring.
*   **Error Generation Rate:** Percentage of LLM calls returning invalid JSON, formatting exceptions, or broken code snippets. Target: $\le 1\%$.

---

## 5. Prompt Retirement Policy

A prompt version must be deprecated and retired if:
*   **Version Deprecation:** A newer SemVer version is approved and deployed.
*   **Failure Threshold:** The error generation rate exceeds $5\%$ over a rolling 100-run history.
*   **Model Shift:** The underlying LLM is updated, rendering previous prompts suboptimal or incompatible.
*   **Compliance Shift:** Regulatory changes (e.g., updates to DPDP India 2023 or GDPR) necessitate altering context boundaries or data classification rules.
