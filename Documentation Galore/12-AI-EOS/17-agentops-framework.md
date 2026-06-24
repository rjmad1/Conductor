# AI-EOS AgentOps Framework

## Document Metadata
* **id:** EOS-17-AGENT-OPS
* **title:** AI-EOS AgentOps Framework
* **description:** Governs the tracking, monitoring, tracing, and debugging protocols for agentic runtimes.
* **owner:** AgentOps Lead & AI Systems Architect
* **domain:** AI Platform Operations
* **tags:** [agentops, traces, trajectories, cost-tracking, logs, debugging]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:02:00Z
* **updated:** 2026-06-24T16:02:00Z
* **related_artifacts:** [08-agent-architecture.md, 10-agent-orchestration-framework.md, 16-observability-framework.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [NIST-AI-RMF-Measure, SOC2-CC7.2]
* **quality_score:** 1.00

---

## Purpose
This document establishes operational rules for managing agent runs in production (AgentOps). It details how execution trajectories are recorded, how token usage and infrastructure costs are tracked, and how debugging is performed.

---

## Trajectory Tracing

An agent trajectory consists of the chronological series of thoughts, tool selections, inputs, outputs, and intermediate states produced during a single task run.
* **Trace Schema:** Every trajectory must be recorded in JSON format and sent to a central logging store (e.g., Langfuse, LangSmith, or OpenTelemetry agent span registry).
* **Span Attributes:** Each step in the trajectory must log:
  ```json
  {
    "trajectory_id": "UUID-V4",
    "step_number": 3,
    "agent_id": "code-generation-bot-22",
    "thought": "The code requires the math module to run calculations.",
    "tool_name": "execute_python_script",
    "tool_input": { "code": "import math\nprint(math.sqrt(16))" },
    "tool_output": { "stdout": "4.0\n", "exit_code": 0 },
    "tokens_consumed": 1450,
    "step_duration_ms": 235
  }
  ```

---

## Cost Tracking & Alert Gates
To prevent runaway agents from generating excessive API costs:
* **Token Budget:** Daily token allocations are assigned to each active agent deployment (e.g., maximum 5,000,000 input tokens per day).
* **Automated Cost Thresholds:**
  - **Soft Limit ($15.00/day):** Sends a Slack/email alert to the SRE channel.
  - **Hard Limit ($50.00/day):** Suspends the agent instance, returning a `LIMIT_EXCEEDED` error code to the caller.
* **Cost Allocation tags:** All API queries sent to models (OpenAI, Anthropic, local hosts) must pass headers attributing the call to a specific `agent_id` and `tenant_id`.

---

## Debugging Protocols (Incident Response)

When an agent fails, locks in a loop, or generates poor quality outputs:
1. **Freeze State:** The running state machine halts and logs an active memory core dump.
2. **Replay Mode:** SREs can load the trajectory UUID into a local sandbox emulator to replay the exact token payloads step-by-step.
3. **Prompt Hotfix Registry:** Emergency overrides to prompt templates are checked in as a new Git commit (Tier 3 approval required).

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Modifications must be approved by the AgentOps Lead.

## Validation Rules
* Deployed agent engines must implement automated cost trackers that evaluate constraints before each API call.

## Audit Requirements
* Trajectory databases are audited monthly to check for run anomalies, high repetition rates (indicating agent loops), or security violations.
