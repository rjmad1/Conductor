# Memory Architecture Standard — Conductor Platform

This standard defines the multi-tiered memory architecture for AI agents operating on the Conductor platform. Memory is managed as a structured, versioned engineering asset to prevent drift and ensure compliance.

---

## 1. Memory Tiers

Memory is partitioned into three distinct tiers with isolated access permissions:

| Memory Tier | Purpose | Persistence | Write Access |
| :--- | :--- | :--- | :--- |
| **Working Memory** | Task-specific variables, local file paths, and current execution trajectories. | Ephemeral (cleared at task termination). | Active Agent |
| **Organizational Memory** | Verified architectural choices, design standards, API definitions, and decision logs. | Persistent (requires human approval and verification). | Restricted (requires EDRB approval). |
| **Experiment Memory** | Hypothesis test results, sandbox performance runs, and temporary settings. | Isolated (never bleeds into production memory tiers). | Active Agent / Developer |

---

## 2. Storage Rules

*   **Format:** Memory entries must be structured using standard markdown, JSON, or YAML formats. Free-text entries without structured metadata are banned.
*   **Directory Layout:**
    *   Working memory logs: `/memory/working/`
    *   Organizational memory artifacts: `/memory/organizational/`
    *   Experiment records: `/memory/experiment/`
*   **Metadata Requirements:** Every memory entry must record:
    1.  *Timestamp:* UTC ISO format.
    2.  *Author:* Agent ID or human developer username.
    3.  *Rationale:* Detailed explanation of the decision or finding.
    4.  *Evidence Reference:* Commit hash, test execution run, or validation benchmark log.

---

## 3. Promotion Rules

Promotion from Working/Experiment memory to Organizational memory is governed by a strict gatekeeper process:

```
[ Working / Experiment Memory ] ──► [ Validation Gate ] ──► [ Human Review ] ──► [ Organizational Memory ]
```

1.  **Validation Gate:** The memory proposal must compile cleanly and pass all validation checks ($100\%$ scoring on the standard scorecard).
2.  **Reproducibility Verification:** The evidence must demonstrate reproducible results from the same inputs.
3.  **Explicit Approval:** Promotions require human approval via PR merge. Autonomous promotion by AI agents is strictly prohibited.

---

## 4. Retention & Pruning Rules

To keep the memory workspace clean and prevent token bloat, the following retention schedules apply:

*   **Working Memory:** Automatically purged when the corresponding task reaches a stop condition or the parent agent loop completes.
*   **Experiment Memory:** Retained for a maximum of 30 days, after which it is archived to CSV data stores or deleted.
*   **Organizational Memory:** Monitored weekly. Deprecated standards are marked as such. Items showing no access hits or references for 90 days are pruned or archived.
*   **Contradiction Resolution:** If a proposed memory contradicts an existing organizational memory entry, the agent must halt and escalate the conflict to the EDRB.
