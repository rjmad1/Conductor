# Agent Handoff Protocol — Conductor Platform

This protocol establishes the communication standards, metadata schemas, and coordination boundaries for interactions between agents, human developers, and memory stores.

---

## 1. Protocol Pathways

Interaction transitions must conform to the following schemas to prevent context loss:

### 1.1 Agent ──► Agent
*   **Trigger:** An agent completes its scope of work and passes the task to a downstream agent (e.g. Implementation Agent passes code to validation/Testing Agent).
*   **Handoff Manifest Structure:** Handoffs must be documented in a markdown format in `/memory/working/handoff_[task_id].md` containing:
    1.  *Task Status:* Completed changes and modified file links.
    2.  *Pending Tasks:* Remaining checklist items.
    3.  *Verification Status:* Test execution outputs and verification logs.

### 1.2 Agent ──► Human
*   **Trigger:** Stop conditions are reached, uncertainty falls below thresholds, or high-risk paths require manual approvals.
*   **Escalation Schema:** The agent must present a structured report containing:
    *   *Issue Description:* Details of the block or decision conflict.
    *   *System State:* Modified files, compile results, and logs.
    *   *Proposed Options:* 2-3 explicit options for how the human can resolve the issue.

### 1.3 Human ──► Agent
*   **Trigger:** A task is assigned or initialized, or the human responds to an escalation request.
*   **Requirement:** Humans must configure context parameters before invoking the agent:
    *   Define goals, success criteria, and verification commands.
    *   Reference target directories, files, or specific domain boundaries.

### 1.4 Agent ──► Memory
*   **Trigger:** Successful validation check completion and decision logs generation.
*   **Action:** The agent writes the decision record (What, Why, Evidence, Author, and Timestamp) to ephemeral Working Memory or submits a promotion PR.

### 1.5 Memory ──► Agent
*   **Trigger:** Agent session initialization.
*   **Action:** The agent loads the Organizational Memory, active project guidelines (`AGENTS.md`), and ADR catalogs to align its execution constraints.

---

## 2. Standard Handoff Schema (JSON Representation)

For programmatic execution gates, handoffs must serialize to the following schema:

```json
{
  "handoff_id": "HND-2026-06-001",
  "timestamp": "2026-06-24T18:03:20Z",
  "source_agent": "implementation-agent-01",
  "target_agent": "testing-agent-01",
  "task_id": "TSK-WF-098",
  "status": "VALIDATION_PENDING",
  "modified_files": [
    "c:/Users/rajaj/Projects/Conductor/platform/workflow/TemporalWorker.java"
  ],
  "validation_report": {
    "compilation": "PASS",
    "unit_tests": "PASS",
    "lint": "PASS"
  },
  "context_references": [
    "c:/Users/rajaj/Projects/Conductor/ADRs/ADR-003.md"
  ]
}
```
