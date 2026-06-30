# Agent Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None in source documents  
**Last Updated:** June 2026

---

## Purpose
Defines the architecture for autonomous AI agents within Conductor — agents that can conduct multi-turn conversations, make decisions, and complete tasks without human intervention.

---

## Agent vs. Workflow: When to Use Each

| Scenario | Use Workflow Engine | Use AI Agent |
|---|---|---|
| Send appointment reminder at T-24h | ✅ Deterministic, scheduled | ❌ Overkill |
| Collect customer name/email in sequence | ✅ Slot-filling in conversation engine | ❌ Unnecessary |
| Answer "What time is the appointment?" | ❌ Too dynamic for static flow | ✅ FAQ bot / agent |
| Schedule appointment via natural language | ❌ Too complex for static flow | ✅ Agent with calendar tool |
| Qualify a lead through open-ended conversation | ❌ Can't handle open-ended | ✅ Lead qualification agent |

**Rule:** Use workflows for deterministic, predictable processes. Use agents for open-ended, language-based tasks that require reasoning.

---

## Agent Design Principles

1. **Bounded scope:** Each agent has a single, narrow job (scheduling, qualification, FAQ)
2. **Graceful handoff:** Every agent can say "I don't know" and hand off to a human
3. **Auditable:** Every agent decision is logged with the prompt, model response, and action taken
4. **Timeout:** Agents have a maximum conversation turns limit (default: 10 turns)
5. **PII-safe:** PII is not sent to external LLM APIs; only relevant business context is sent

---

## Agent Framework Architecture

```
Customer WhatsApp Message
         ↓
conversation-service
         ↓
  Is an agent active for this conversation?
         ↓
   YES → Agent Controller
         ↓
   Load agent context (history, state, tools)
         ↓
   Build prompt from template
         ↓
   Call LLM API (via Dify or direct)
         ↓
   Parse structured response
         ↓
   Tool call? → Execute tool (calendar, CRM, etc.)
         ↓
   Generate reply message
         ↓
   Send via whatsapp-adapter
         ↓
   Update agent state in Redis
```

---

## Agent Definition Model

Each agent is defined as a configuration object:

```json
{
  "agent_id": "appointment_scheduler",
  "name": "Appointment Scheduling Assistant",
  "trigger": "intent.appointment_booking",
  "model": "gpt-4o",
  "system_prompt": "system_prompts/appointment_scheduler_v2",
  "tools": ["get_availability", "book_appointment", "cancel_appointment"],
  "max_turns": 8,
  "timeout_seconds": 300,
  "handoff_trigger": "intent.escalate OR turns_exceeded OR low_confidence",
  "handoff_action": "route_to_agent",
  "output_schema": {
    "appointment_confirmed": "boolean",
    "appointment_time": "datetime",
    "patient_name": "string"
  }
}
```

---

## Agent Tools (Phase 3)

Tools are functions the agent can invoke. Implemented as Spring Boot service methods exposed to the agent framework.

### Tool: `get_availability`
Fetches open appointment slots from Google Calendar connector.
```
Input: { date_range: "next 3 days", duration_minutes: 30 }
Output: [ { start: "2026-07-01T10:00:00", end: "2026-07-01T10:30:00" }, ... ]
```

### Tool: `book_appointment`
Creates an appointment in the connected calendar and returns confirmation.
```
Input: { start_time: "2026-07-01T10:00:00", patient_name: "Priya", patient_phone: "+91..." }
Output: { confirmation_id: "CAL-123", event_url: "..." }
```

### Tool: `lookup_customer`
Retrieves customer record from the Customer Registry.
```
Input: { phone: "+91XXXXXXXXXX" }
Output: { name, email, tags, last_interaction, appointments: [...] }
```

### Tool: `create_lead`
Creates a lead record in the connected CRM (Zoho, HubSpot).
```
Input: { name, phone, email, qualification_data: {...} }
Output: { lead_id: "LEAD-456", status: "created" }
```

### Tool: `search_knowledge_base`
RAG search against the tenant's knowledge base documents.
```
Input: { query: "what are your clinic hours" }
Output: { answer: "...", source_chunks: [...], confidence: 0.91 }
```

---

## Agent State Management

Agent state is maintained in Redis during the conversation:

```json
{
  "agent_id": "appointment_scheduler",
  "conversation_id": "conv-001",
  "customer_id": "cust-001",
  "turn_count": 3,
  "collected_slots": {
    "preferred_date": "tomorrow",
    "doctor_preference": "any"
  },
  "available_slots": [...],
  "started_at": "2026-06-15T10:00:00Z",
  "last_turn_at": "2026-06-15T10:05:00Z"
}
```

**TTL:** 30 minutes of inactivity → agent session expires → next message restarts conversation

---

## Multi-Agent Orchestration (Phase 3 — Advanced)

For complex scenarios requiring multiple specialized agents working together:

```
Inbound: "I need to see a doctor and also want to pay my last bill"
         ↓
Orchestrator Agent: classify multiple intents
         ↓
→ Delegate to Appointment Agent (for appointment)
→ Delegate to Payment Agent (for bill payment)
         ↓
Both complete → Orchestrator synthesizes summary response
```

**Implementation:** Dify multi-agent workflow (Dify supports agent chaining natively)

---

## Agent Evaluation (Quality Control)

Before any agent goes to production:

1. **Offline evaluation:** 100 test conversations with known correct outcomes
2. **Accuracy target:** >90% task completion rate (appointment booked / lead qualified)
3. **Escalation rate target:** <20% (agent should handle 80% without escalation)
4. **Harmful output test:** Red-teaming for prompt injection, jailbreak attempts
5. **Shadow mode:** Run agent in parallel with human agent for 2 weeks; compare outcomes

---

## Agent Monitoring (Production)

| Metric | Alert Threshold |
|---|---|
| Task completion rate | < 80% in any hour |
| Escalation rate | > 40% in any hour |
| Average turns to completion | > 8 turns (agent getting stuck) |
| LLM error rate | > 5% |
| Agent session timeout rate | > 20% (customers abandoning) |

---

## Cross-References
- `04-Architecture/AI-Architecture.md` — AI model and platform architecture
- `08-AI/Agent-Framework.md` — Agent framework implementation guide
- `08-AI/Prompt-Library.md` — System prompts for each agent type
- `08-AI/Evaluation-Framework.md` — Agent quality evaluation process
