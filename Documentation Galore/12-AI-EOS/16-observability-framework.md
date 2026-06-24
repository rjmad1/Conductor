# AI-EOS Observability Framework

## Document Metadata
* **id:** EOS-16-OBS-FW
* **title:** AI-EOS Observability Framework
* **description:** Outlines standard telemetry collection, metric specifications, and LLM/Agent-specific monitoring parameters.
* **owner:** Platform Engineering Lead & AgentOps Lead
* **domain:** Platform Operations
* **tags:** [observability, metrics, logs, traces, opentelemetry, monitoring]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:00:00Z
* **updated:** 2026-06-24T16:00:00Z
* **related_artifacts:** [01-constitution.md, 17-agentops-framework.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [ISO-27001-A.12.4, SOC2-CC7.2]
* **quality_score:** 1.00

---

## Purpose
This framework defines how telemetry is structured, gathered, and analyzed. It establishes standards for OpenTelemetry logs, metrics, and traces, and defines specific telemetry rules for monitoring LLM and agent runs.

---

## OpenTelemetry Instrumentation Standards

All microservices must integrate the OpenTelemetry SDK to output:

### 1. Distributed Traces
* Every inbound HTTP request or NATS message must check for an active trace header (W3C Trace Context). If none exists, a new trace root must be generated.
* Trace headers must be propagated through all downstream calls, including Temporal workflow actions and database queries.
* Spans must include attributes: `tenant_id`, `service_name`, and `span_kind`.

### 2. Metrics
Services must emit standard SRE metrics:
* **Latency:** Server-side request duration histogram.
* **Traffic:** Rate of requests per second.
* **Errors:** Counter of failed requests, categorized by HTTP/gRPC status codes.
* **Saturation:** Database pool utilization and CPU/Memory usage.

---

## LLM and Agent-Specific Telemetry

For any LLM interaction or agent execution, the following metrics must be recorded:

```
                  ┌────────────────────────────────────────┐
                  │          Inference Telemetry           │
                  └───────────────────┬────────────────────┘
                                      │
         ┌────────────────────────────┼───────────────────────────┐
         ▼                            ▼                           ▼
┌──────────────────┐         ┌──────────────────┐        ┌──────────────────┐
│ LLM Cost Metrics │         │ Quality Metrics  │        │ Runtime Metrics  │
│ - Input Tokens   │         │ - Toxicity       │        │ - Latency (TTFT) │
│ - Output Tokens  │         │ - Hallucination  │        │ - Total Duration │
│ - Cache Hits     │         │ - Prompt Drift   │        │ - Queue Delay    │
└──────────────────┘         └──────────────────┘        └──────────────────┘
```

* **Cost Metrics:** `llm.tokens.input`, `llm.tokens.output`, `llm.tokens.cache_hit`.
* **Latency Metrics:** Time to first token (TTFT), complete inference duration.
* **Quality Metrics:** Prompt version ID, temperature, output compliance validation result (Success/Fail).
* **Agent Diagnostics:** `agent.trajectory.steps` (counter of ReAct loops), `agent.tool.invocations` (count of tool executions per task).

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Approved by the Platform Engineering Lead.

## Validation Rules
* CI/CD pipelines check that microservices compile with the required OpenTelemetry instrumentation libraries.

## Audit Requirements
* Traces and operational metrics are retained for 30 days in active storage, and archived to long-term storage for 1 year.
