# AI Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Technical Layers (partial mention of Dify), Initiative Brief  
**Last Updated:** June 2026

---

## Purpose
Defines the AI capabilities, model strategy, and technical architecture for AI features in Conductor — from intent detection to AI agent workflows.

---

## AI Strategy

**Principle:** AI augments the platform; it does not replace the workflow engine. AI is a capability that can be invoked as an action within a workflow or as a classifier before routing.

**Phase approach:**
- **MVP (V1):** No AI. Platform works entirely on deterministic logic.
- **Phase 2:** Basic AI (intent classification, FAQ bot). AI runs in isolated Dify instance.
- **Phase 3:** Full AI agents (appointment scheduling, lead qualification, autonomous conversations).

**Model sourcing:**
- Commercial APIs: OpenAI GPT-4o, Anthropic Claude (for high-quality tasks requiring reasoning)
- Open-source: Mistral 7B (for cost-sensitive classification tasks at scale)
- Embeddings: OpenAI `text-embedding-3-small` or open-source alternatives (sentence-transformers)

---

## AI Capability Roadmap

| Capability | Phase | Model | Trigger |
|---|---|---|---|
| Intent classification | Phase 2 | Mistral 7B (fine-tuned) | Inbound WhatsApp message |
| FAQ / knowledge base bot | Phase 2 | RAG over knowledge docs | Customer question |
| Sentiment detection | Phase 2 | Classification model | All inbound messages |
| Conversation summarization | Phase 3 | GPT-4o / Claude | Agent handoff |
| Lead qualification | Phase 3 | GPT-4o with structured output | Inbound lead conversation |
| Appointment scheduling | Phase 3 | GPT-4o with calendar tools | Customer scheduling request |
| Product recommendations | Phase 3 | Embeddings + similarity search | Customer purchase intent |
| Workflow suggestion | Phase 3 | GPT-4o | Tenant onboarding / analytics |

---

## AI Platform Architecture (Phase 2)

### Dify Integration

**Dify** is an open-source LLM application development platform. It provides:
- Visual workflow builder for LLM chains
- RAG (Retrieval-Augmented Generation) pipeline
- Model management (switch between OpenAI, Anthropic, open-source)
- API exposure for integration

**Deployment:** Self-hosted Dify instance in Conductor's private VPC

**Integration Points:**
```
conversation-service → Dify API → AI Response → conversation-service
```

**Intent Classification Flow:**
```
Inbound message: "Hello, I'd like to book an appointment for next week"
    ↓
POST https://dify.internal/v1/chat-messages
{ "inputs": { "message": "Hello, I'd like to book..." }, "query": "", "app_id": "intent_classifier" }
    ↓
Response: { "intent": "appointment_booking", "confidence": 0.94, "entities": { "timeframe": "next week" } }
    ↓
conversation-service routes to appointment booking workflow
```

---

## AI Module: Intent Classification

**Purpose:** Classify the intent of an inbound customer message to route it to the correct workflow.

**Intent Categories (MVP — Healthcare vertical):**
```json
{
  "intents": [
    "appointment_booking",
    "appointment_cancellation",
    "appointment_reschedule",
    "test_results_inquiry",
    "prescription_refill",
    "payment_inquiry",
    "general_query",
    "complaint",
    "stop_messages",
    "other"
  ]
}
```

**Classification Architecture:**
```
Input: Customer message text + conversation history (last 3 turns)
    ↓
Preprocessing: Language detection, normalization
    ↓
Classification: Fine-tuned Mistral 7B (or GPT-4o for accuracy)
    ↓
Output: { intent, confidence, entities }
    ↓
Routing: if confidence > 0.8 → route to intent workflow
         if confidence < 0.8 → fallback to human agent
```

---

## AI Module: FAQ / Knowledge Base Bot

**Purpose:** Answer common customer questions from a business's knowledge base without human intervention.

**Setup flow (tenant perspective):**
1. Tenant uploads FAQ document (PDF or Google Doc)
2. Conductor chunks and embeds the document
3. Embeddings stored in vector database (Qdrant or pgvector)
4. FAQ bot app configured in Dify with this knowledge base

**RAG Pipeline:**
```
Customer question: "What are your clinic hours?"
    ↓
Embed question → vector
    ↓
Similarity search in knowledge base (top 3 chunks)
    ↓
Prompt: "You are an assistant for [Clinic Name]. Answer using only the context below.
         Context: {retrieved_chunks}
         Question: {customer_question}"
    ↓
LLM generates answer
    ↓
Send answer as WhatsApp message
```

**Hallucination guard:**
- LLM instructed: "If the answer is not in the context, say: I don't have that information. Let me connect you with our team."
- Low-confidence answers routed to human agent instead of sent

**Vector Database:** pgvector (PostgreSQL extension) for MVP — avoids additional infrastructure. Migrate to Qdrant at scale.

---

## AI Module: Sentiment Analysis

**Purpose:** Detect negative sentiment in conversations and trigger escalation or priority routing.

**Implementation:**
- Zero-shot classification using small language model
- Labels: `positive`, `neutral`, `negative`, `urgent`
- Runs on every inbound message asynchronously (does not block response)

**Trigger action:**
```
if sentiment == "negative" AND confidence > 0.85:
    → add_tag(customer, "negative_sentiment")
    → notify_agent(team="support", priority="high")
```

---

## AI Module: Conversation Summarization (Phase 3)

**Purpose:** When a conversation is handed off to a human agent, generate a 3-5 sentence summary so the agent has instant context.

**Input:** Full conversation history (all messages, both directions)
**Output:** Summary with: customer name, intent, what they've already said, current status, recommended next action

**Prompt template:**
```
You are summarizing a customer service conversation for a support agent.
Write a 3-5 sentence summary covering: customer identity, their request, 
key information shared, current status, and recommended agent action.

Conversation:
{conversation_history}

Summary:
```

---

## AI Module: Lead Qualification (Phase 3)

**Purpose:** Conduct a natural language conversation to qualify a lead before routing to a sales agent.

**Qualification criteria (configurable per tenant):**
- Budget range
- Decision timeline
- Use case/requirements
- Company size

**Implementation:**
- Multi-turn conversation agent built in Dify
- Structured output extraction: JSON with qualification fields
- Lead score computed from answers
- High-score leads → immediate agent routing
- Low-score leads → nurture workflow

---

## AI Safety Controls

| Control | Implementation |
|---|---|
| Hallucination prevention | RAG with explicit "only use context" prompting |
| PII in prompts | Strip PII before sending to external LLM APIs |
| Content filtering | Detect abusive/harmful output before sending to customer |
| Confidence thresholds | Only act on AI output if confidence > configurable threshold |
| Human fallback | Any AI uncertainty → route to human agent |
| Audit trail | All AI actions logged with model version, prompt hash, and response |

---

## Model Cost Management ⚡

| Model | Use Case | Estimated Cost |
|---|---|---|
| GPT-4o | Lead qualification, summarization | $0.005/1K input tokens |
| Mistral 7B (self-hosted) | Intent classification at scale | ~$0.0001/call (infra cost) |
| OpenAI embeddings | Document embedding | $0.00002/1K tokens |
| Estimated per tenant per month | 10,000 messages with AI | ~₹500–2,000/tenant |

AI costs should be included in Growth/Business plan pricing; not exposed as a separate line to tenants.

---

## Cross-References
- `08-AI/Knowledge-Model.md` — Knowledge base structure
- `08-AI/RAG-Architecture.md` — RAG pipeline details
- `08-AI/Prompt-Library.md` — System prompts for each AI module
- `04-Architecture/Agent-Architecture.md` — Autonomous agent design
