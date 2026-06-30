# RAG Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** AI Architecture  
**Last Updated:** June 2026

---

## RAG (Retrieval-Augmented Generation) Overview

RAG is the foundation of Conductor's FAQ bot capability. Instead of relying on the LLM's internal knowledge (which may be outdated or hallucinate business-specific facts), RAG retrieves relevant context from the tenant's own knowledge base and provides it to the LLM as grounding.

**Why RAG over fine-tuning:**
- Fine-tuning requires hundreds of examples per tenant — impractical
- RAG is updateable in real-time (upload a new FAQ doc and it's immediately available)
- RAG is auditable (we can see exactly what context was retrieved)
- RAG prevents hallucination better than fine-tuning

---

## RAG Pipeline Architecture

```
Customer Message
       ↓
  [Embedding Service]
  Embed message → vector (1536-dim)
       ↓
  [Vector Store — pgvector]
  Semantic search: top-K chunks by cosine similarity
  Filter: tenant_id = current tenant
  Threshold: similarity > 0.7
       ↓
  [Context Assembly]
  Combine retrieved chunks with message history
  Format as prompt with clear context boundaries
       ↓
  [LLM Inference]
  Send to OpenAI GPT-4o-mini or Mistral
  System prompt: "Answer ONLY from context. If not in context, say so."
       ↓
  [Response Validation]
  Check: does response contain only information from context?
  If hallucination detected: replace with escalation message
       ↓
  [Output]
  Send to customer via WhatsApp
  Log retrieval trace for audit
```

---

## Embedding Strategy

### Model: OpenAI text-embedding-3-small
- Dimensions: 1536
- Cost: $0.00002 per 1K tokens
- Use: Both indexing (at document upload) and querying (at response time)

### Chunking Parameters
```python
CHUNK_SIZE = 512        # tokens
CHUNK_OVERLAP = 50      # tokens (overlap for context continuity)
MIN_CHUNK_SIZE = 100    # discard very small chunks
```

### Chunking Algorithm
```python
def chunk_document(text: str) -> list[str]:
    """
    1. Split by paragraph boundaries
    2. If paragraph > CHUNK_SIZE, split by sentence boundaries
    3. Merge small paragraphs until they reach CHUNK_SIZE
    4. Apply CHUNK_OVERLAP between consecutive chunks
    """
    ...
```

---

## Retrieval Configuration

### Similarity Search
```sql
-- pgvector cosine similarity search
SELECT 
    content,
    metadata,
    1 - (embedding <=> query_embedding::vector) AS similarity
FROM knowledge_chunks
WHERE tenant_id = :tenantId
    AND 1 - (embedding <=> query_embedding::vector) > 0.7   -- threshold filter
ORDER BY embedding <=> query_embedding::vector
LIMIT 3;                                                      -- top-K = 3
```

### Retrieval Parameters
| Parameter | Value | Rationale |
|---|---|---|
| Top-K | 3 | Balance between context richness and prompt size |
| Similarity threshold | 0.70 | Below this = likely irrelevant; escalate to agent |
| Max context tokens | 2,048 | Fits in model context window with room for response |

---

## Prompt Engineering

### System Prompt (FAQ Bot)
```
You are a helpful assistant for {business_name}. Your job is to answer customer 
questions accurately and helpfully.

IMPORTANT RULES:
1. Answer ONLY using information from the context below. 
2. If the answer is not in the context, respond EXACTLY with: 
   "I don't have that information. Let me connect you with our team."
3. Keep answers concise — 1-3 sentences unless more detail is genuinely needed.
4. Be friendly and professional.
5. Do not make up prices, phone numbers, addresses, or policies.

CONTEXT:
{retrieved_chunks}
```

### Message Assembly
```
[System]: {system_prompt with context}
[User (history)]: {last 3 customer messages}
[User (current)]: {current customer message}
```

---

## Hallucination Detection

**Challenge:** LLMs sometimes generate plausible-sounding but incorrect information, especially when the context doesn't contain the answer.

**Primary defense:** System prompt instructs model to say "I don't have that information" when context is insufficient.

**Secondary defense:** Response validation layer:
```python
def validate_response(response: str, context_chunks: list[str]) -> bool:
    """
    Check if response contains facts not grounded in context.
    Simple implementation: check for phone numbers, prices, dates in response
    that don't appear in context.
    """
    # Extract numerical facts from response
    # Verify each fact exists in at least one context chunk
    # Return False if ungrounded fact detected
    ...
```

**Fallback:** If validation fails → replace response with:
"I want to make sure I give you accurate information. Let me connect you with our team who can help."

---

## Multi-Turn Conversation RAG

For follow-up questions, the retrieval query must incorporate conversation history:

```python
def build_retrieval_query(current_message: str, conversation_history: list) -> str:
    """
    Combine last 2 conversation turns with current message
    to produce a rich query that captures the full context.
    
    Example:
    Previous: "What are your clinic hours?"
    Current: "And what about on weekends?"
    Combined: "clinic hours on weekends"
    """
    # Summarize last 2 turns + current message
    # Use LLM to create condensed search query
    ...
```

---

## Dify Integration

Conductor uses **Dify** as the RAG workflow orchestration layer:

**Dify setup:**
1. Create a "Knowledge Base" in Dify pointing to our pgvector store
2. Create an "Agent" app with the FAQ bot system prompt
3. Configure retrieval settings (top-K, threshold)
4. Expose as API endpoint: `POST /v1/chat-messages`

**Conductor integration:**
```java
// In conversation-service
DifyResponse response = difyClient.chat(
    DifyChatRequest.builder()
        .appId(tenant.getFaqBotAppId())
        .query(customerMessage)
        .conversationId(conversationId)
        .inputs(Map.of("business_name", tenant.getName()))
        .build()
);
```

---

## Performance Characteristics ⚡

| Operation | Latency Target | Notes |
|---|---|---|
| Embedding (query) | < 200ms | OpenAI API call |
| Vector search (pgvector) | < 50ms | With ivfflat index, 10K chunks |
| LLM inference | < 2,000ms | GPT-4o-mini; 500 tokens output |
| Total RAG pipeline | < 2,500ms | End-to-end, customer message → response |

WhatsApp customers expect quick responses. If RAG pipeline > 3 seconds, send "Please wait a moment..." first.

---

## Cross-References
- `08-AI/Knowledge-Model.md` — Knowledge base data model
- `08-AI/Prompt-Library.md` — System prompts for each AI module
- `04-Architecture/AI-Architecture.md` — Platform AI architecture
