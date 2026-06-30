# Knowledge Model — Conductor AI

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** AI Architecture, Initiative Brief  
**Last Updated:** June 2026

---

## Purpose
Defines how Conductor's AI layer organizes, stores, and retrieves knowledge to power AI capabilities (FAQ bots, intent classification, agent workflows).

---

## Knowledge Domains

Conductor's AI layer manages three types of knowledge:

### 1. Tenant Knowledge Base
Business-specific knowledge uploaded by the tenant.

**Examples:**
- Clinic FAQ: "What are your opening hours?" → "Mon-Sat 9am-6pm"
- E-commerce policy: "What is your return policy?" → "30-day returns..."
- Product catalog: names, prices, descriptions (for recommendations)

**Storage:** Vector embeddings in pgvector (PostgreSQL extension) or Qdrant  
**Format:** Chunked documents with embeddings per chunk  
**Scope:** Isolated per tenant — Tenant A's knowledge never used for Tenant B

### 2. Platform Capability Knowledge
Conductor's own knowledge about what it can do — used by AI workflow suggestions.

**Examples:**
- "Tenants in retail with Shopify connected often benefit from abandoned cart recovery"
- "Healthcare tenants typically activate appointment reminder workflows first"

**Storage:** Static configuration / fine-tuned model  
**Update:** Quarterly with usage data analysis

### 3. Conversation Context
Short-term memory maintained during an active agent conversation.

**Examples:**
- Customer has already said their name is "Priya"
- Customer wants to book on "Friday afternoon"
- Customer has already been told the available slots

**Storage:** Redis (24h TTL per conversation)  
**Format:** Structured slots JSON

---

## Knowledge Base Schema

### Document (Tenant Knowledge)
```json
{
  "id": "doc-uuid",
  "tenant_id": "t-uuid",
  "title": "Frequently Asked Questions",
  "source_type": "pdf",
  "source_url": "s3://conductor-media/t-uuid/faq.pdf",
  "status": "indexed",
  "chunk_count": 24,
  "created_at": "2026-06-15T10:00:00Z",
  "last_indexed_at": "2026-06-15T10:30:00Z"
}
```

### Chunk (Indexed Text Block)
```json
{
  "id": "chunk-uuid",
  "document_id": "doc-uuid",
  "tenant_id": "t-uuid",
  "content": "Our clinic is open Monday to Saturday from 9:00 AM to 6:00 PM. We are closed on Sundays and public holidays.",
  "embedding": [0.023, -0.156, ...],  // 1536-dim vector (OpenAI) or 768-dim (Mistral)
  "chunk_index": 3,
  "metadata": {
    "section": "Opening Hours",
    "page": 1
  }
}
```

### pgvector Table
```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_chunks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES knowledge_documents(id),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    content     TEXT NOT NULL,
    embedding   VECTOR(1536),         -- OpenAI text-embedding-3-small dimension
    chunk_index INTEGER NOT NULL,
    metadata    JSONB DEFAULT '{}',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunks_tenant ON knowledge_chunks(tenant_id);
CREATE INDEX idx_chunks_embedding ON knowledge_chunks 
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

---

## Knowledge Ingestion Pipeline

### Step 1: Document Upload
```
Tenant uploads PDF/Google Doc → S3 storage → Metadata saved to knowledge_documents table
```

### Step 2: Chunking
```
Document → Text extraction (PyMuPDF for PDF, Google Docs API for docs)
         → Split into chunks (512 tokens, 50-token overlap)
         → Each chunk stored in knowledge_chunks table (without embedding)
```

**Chunking strategy:**
- Max chunk size: 512 tokens
- Overlap: 50 tokens (maintains context at boundaries)
- Sentence-aware splitting (don't split mid-sentence)
- Metadata extraction: section headers, page numbers

### Step 3: Embedding
```
Each chunk → OpenAI text-embedding-3-small API → 1536-dim vector
           → Stored in knowledge_chunks.embedding
```

**Batch processing:** Chunks embedded in batches of 100 (API efficiency)  
**Cost:** $0.00002/1K tokens × 512 tokens/chunk × N chunks ≈ negligible

### Step 4: Indexing
```
Embeddings stored → ivfflat index updated → Chunk available for RAG queries
```

---

## Retrieval (RAG Query)

When a customer asks a question:

```
1. Embed the customer question:
   vector = embed("What are your opening hours?")

2. Similarity search (cosine similarity):
   SELECT content, metadata
   FROM knowledge_chunks
   WHERE tenant_id = '{tenant_id}'
   ORDER BY embedding <=> '{vector}'::vector
   LIMIT 3;

3. Retrieve top 3 chunks by similarity score

4. Filter by similarity threshold (score > 0.7 — below threshold = "I don't know")

5. Build prompt with retrieved context:
   "Answer the customer question using ONLY the context below.
    Context: {retrieved chunks}
    Question: {customer question}"

6. Generate response via LLM

7. Apply hallucination guard:
   If model responds with information not in context → replace with escalation message
```

---

## Knowledge Management UI (Tenant-Facing)

Tenants can:
- Upload documents (PDF, DOCX, Google Doc link)
- View indexed documents with chunk count and status
- Add manual Q&A pairs (question + answer text, directly indexed)
- Test the FAQ bot with sample questions before activating
- Delete documents (all associated chunks deleted)

---

## Knowledge Quality Metrics

| Metric | Target | Measurement |
|---|---|---|
| FAQ bot answer accuracy | > 85% | Manual evaluation on test set |
| Retrieval relevance (MRR@3) | > 0.8 | Offline evaluation |
| Hallucination rate | < 5% | Answer contains facts not in context |
| Escalation rate | < 20% | % of questions routed to human |
| Customer satisfaction with bot | > 3.5/5 | Post-conversation rating |

---

## Cross-References
- `08-AI/RAG-Architecture.md` — RAG pipeline implementation details
- `04-Architecture/AI-Architecture.md` — AI platform overview
- `08-AI/Evaluation-Framework.md` — Knowledge quality evaluation
