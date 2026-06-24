# AI-EOS Memory Architecture

## Document Metadata
* **id:** EOS-07-MEM-ARCH
* **title:** AI-EOS Memory Architecture
* **description:** Defines the four-layer memory structure (short-term, episodic, semantic, long-term) for system agents.
* **owner:** AgentOps Lead & AI Systems Architect
* **domain:** AI Platform
* **tags:** [memory, agent, state, episodic, RAG, long-term]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:42:00Z
* **updated:** 2026-06-24T16:42:00Z
* **related_artifacts:** [08-agent-architecture.md, 10-agent-orchestration-framework.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [GDPR-Art-32, DPDP-India-Sec-8, SOC2-CC6.3]
* **quality_score:** 1.00

---

## Purpose
This document defines the storage, retrieval, and retention architectures for agent memory. It ensures that agents maintain necessary execution history, learning feedback, and system context without violating data sovereignty or privacy boundaries.

---

## The Four Memory Layers

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          LLM Context Window                             │
│  ┌────────────────────────┐             ┌────────────────────────────┐  │
│  │ 1. Short-Term Memory   │             │ 2. Episodic Memory         │  │
│  │ (Active session state) │             │ (Past run traces & steps)  │  │
│  └────────────────────────┘             └────────────────────────────┘  │
└──────────────────────▲──────────────────────────────▲─────────────────────┘
                       │                              │
┌──────────────────────▼──────────────────────────────▼─────────────────────┐
│                            Persistent Storage                           │
│  ┌────────────────────────┐             ┌────────────────────────────┐  │
│  │ 3. Semantic Memory     │             │ 4. Long-Term Memory        │  │
│  │ (Architecture / Specs) │             │ (Learned preferences / UI) │  │
│  └────────────────────────┘             └────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1. Short-Term Memory
* **Definition:** The active context and workspace during a single session or execution loop (e.g., loaded source code files, current compile errors, terminal buffer).
* **Storage:** Volatile RAM, local scratch files, or active LLM context window.
* **Sovereignty Boundary:** Erased upon session termination.

### 2. Episodic Memory
* **Definition:** Historical logs of prior agent runs, specific task trajectories, decisions made, and tool execution history.
* **Storage:** Append-only trace logging database (e.g., OpenTelemetry logs, Langfuse tracing backend).
* **Sovereignty Boundary:** PII fields must be dynamically masked. Traces are stored in AWS ap-south-1 for India tenant runs. Retained for 90 days.

### 3. Semantic Memory
* **Definition:** Static, structured knowledge representing architectural rules, compliance goals, schema contracts, and developer guides.
* **Storage:** Git repository documentation (vectorized into a semantic index/embeddings store).
* **Sovereignty Boundary:** Read-only for agents. No dynamic writes are allowed directly from agent runtimes to the semantic index.

### 4. Long-Term Memory
* **Definition:** Persistent preferences, learned workflows, and optimized execution heuristics acquired across multiple sessions (e.g., custom coding style rules, system component layouts).
* **Storage:** Key-value metadata database, encrypted user/agent profiles.
* **Sovereignty Boundary:** Bound by user preferences. Requires a manual reset option for users (Right to Erasure).

---

## Memory Governance & Consent Policies

### Data Isolation
Agent memory must be isolated per tenant. An agent operating on Tenant A's workflows must have zero visibility or access to the episodic or long-term memory of Tenant B.

### PII Scrubbing
* Every transaction written to episodic or long-term memory must pass through a regex-based PII scrubber that replaces names, phone numbers, and keys with `[MASKED]`.
* Consent records (e.g., DPDP opt-ins) are kept in a separate, secure, encrypted DB and not combined with general agent conversation histories.

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Modifications must be approved by the AgentOps Lead.

## Validation Rules
* Vector storage configurations must enforce tenancy isolation keys on all search operations.

## Audit Requirements
* Weekly automated validation runs verify that no unmasked PII exists in active vector indexes or trace databases.
