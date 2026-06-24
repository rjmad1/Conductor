# AI-EOS Knowledge Quality Framework

## Document Metadata
* **id:** EOS-18-KNOW-QUAL
* **title:** AI-EOS Knowledge Quality Framework
* **description:** Outlines formulas and metrics to measure documentation completeness, freshness, authority, and retrieval quality.
* **owner:** Knowledge Systems Architect
* **domain:** Enterprise Knowledge Management
* **tags:** [knowledge, quality, freshness, authority, metrics, retrieval]
* **version:** 1.0.0
* **status:** Approved
* **created:** 2026-06-24T16:04:00Z
* **updated:** 2026-06-24T16:04:00Z
* **related_artifacts:** [01-constitution.md, 06-knowledge-architecture.md]
* **source_of_truth:** Git Repository
* **authority_level:** L2 - Governance
* **risk_tier:** Tier 3 — High
* **compliance_tags:** [ISO-27001-A.8, NIST-AI-RMF-Gov]
* **quality_score:** 1.00

---

## Purpose
To prevent intelligence decay, this framework mandates quality scores for every knowledge artifact. It defines how those scores are calculated and specifies refresh and deprecation policies to maintain high vector retrieval accuracy.

---

## Quality Metrics & Formulas

For every knowledge artifact, the pipeline calculates a global **Quality Score ($Q$)**:

$$Q = 0.25 \times S_{\text{fresh}} + 0.25 \times S_{\text{auth}} + 0.25 \times S_{\text{comp}} + 0.25 \times S_{\text{conf}}$$

### 1. Freshness Score ($S_{\text{fresh}}$)
Measures the time elapsed since the last modification.
* **Formula:**
  $$S_{\text{fresh}} = \max\left(0, 1 - \frac{\text{Days Since Last Update}}{180}\right)$$
* **Staleness Indicator:** If $S_{\text{fresh}} < 0.20$ (older than 144 days), the document is marked as `STALE`.

### 2. Authority Score ($S_{\text{auth}}$)
Determined by the designated owner's level of authority.
* **L1 - Constitutional:** $S_{\text{auth}} = 1.00$
* **L2 - Governance:** $S_{\text{auth}} = 0.85$
* **L3 - Execution:** $S_{\text{auth}} = 0.70$
* **L4 - Operational / Draft:** $S_{\text{auth}} = 0.50$

### 3. Completeness Score ($S_{\text{comp}}$)
Evaluates if all required frontmatter elements and structural headers are defined.
* **Formula:**
  $$S_{\text{comp}} = \frac{\text{Defined Keys in Frontmatter}}{16}$$

### 4. Confidence Score ($S_{\text{conf}}$)
Represents verification and deployment status.
* **Active Production Verified:** $S_{\text{conf}} = 1.00$
* **Staging Tested:** $S_{\text{conf}} = 0.80$
* **Draft / Unverified:** $S_{\text{conf}} = 0.40$

---

## Refresh and Deprecation Policies

```
[Document Active] ──(>144 days idle)──> [Marked STALE] ──(Auto-Notify Owner)──> [Review/Refresh]
                                             │
                                         (No action)
                                             │
                                             ▼
                                     [Marked DEPRECATED] ──(Remove vector store index)
```

### Refresh Policy
* STALE documents automatically create a maintenance ticket in the owner's team backlog.
* The owner must review the document and either update the timestamp (certifying verification) or flag it for deprecation.

### Deprecation Policy
* A document is marked `DEPRECATED` if it is superseded by a newer version or if the associated capability is decommissioned.
* DEPRECATED documents are excluded from active vector DB search indexes to prevent RAG hallucinations.

---

## Duplication Detection & Retrieval Quality Controls
* **Vector Similarity Checks:** The ingestion tool calculates Cosine Similarity across all document chunk embeddings. If a chunk exceeds `0.88` similarity with an existing chunk, it is flagged as a potential duplicate.
* **Relevance Filtering:** Vector searches (Retrieval Augmented Generation) must apply a minimum cosine distance threshold of `0.75` to filter out irrelevant contexts.

---

## Lifecycle Policy
* **Review Cycle:** Annually.
* **Revision Process:** Approved by the Knowledge Systems Architect.

## Validation Rules
* Ingestion pipelines calculate and inject the `quality_score` into the frontmatter. Documents with $Q < 0.60$ cannot be promoted to `Approved` status.

## Audit Requirements
* Semi-annual audits scan the vector store to confirm that all inactive or deprecated documents have been successfully removed from active indices.
