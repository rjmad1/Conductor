# AI Gaps — Conductor

**Status:** Review Board Assessment  
**Source:** Gap analysis against AI architecture requirements  
**Last Updated:** June 2026

---

## Summary Assessment

The source documents mention AI ("conversational AI") and Dify as a tool, but provide zero AI design, no model selection rationale, no safety controls, no evaluation framework, and no phased approach. The AI layer is essentially undocumented in source materials.

**Positive note:** AI is correctly positioned as Phase 2, not MVP. The core workflow engine (deterministic) is the right MVP strategy. The AI gaps are real but not launch-blocking.

---

## Critical AI Gaps

### AI-C1: No AI Architecture
**Gap:** Source documents mention "AI-powered conversations" and Dify as a tool but provide zero technical architecture.  
**Generated solution:** `04-Architecture/AI-Architecture.md` — full AI platform design.  
**Status:** Documented ⚡ — Phase 2 implementation required

### AI-C2: No AI Safety Controls Defined
**Gap:** Source documents have no discussion of hallucination prevention, content filtering, PII handling in prompts, or escalation thresholds.  
**Risk:** Unsupervised AI in customer-facing conversations creates reputational and legal risk.  
**Generated solution:** `04-Architecture/AI-Architecture.md` — AI safety controls section.  
**Status:** Documented ⚡ — must be implemented before any AI feature goes live

### AI-C3: No Evaluation Framework
**Gap:** Source documents have no methodology for testing AI component quality before deployment.  
**Risk:** Poor-quality AI responses reaching customers damage brand and increase churn.  
**Generated solution:** `08-AI/Evaluation-Framework.md` — full evaluation methodology.  
**Status:** Documented ⚡ — test datasets must be built before Phase 2 AI launches

---

## Major AI Gaps

### AI-M1: No Prompt Engineering Documentation
**Gap:** Source documents have no prompt specifications for any AI capability.  
**Generated solution:** `08-AI/Prompt-Library.md` — 7 production prompts with versioning.  
**Status:** Documented ⚡ — prompts require testing before deployment

### AI-M2: No RAG Pipeline Design
**Gap:** Source documents mention knowledge base FAQ but provide no RAG architecture.  
**Generated solution:** `08-AI/RAG-Architecture.md` — full RAG design with pgvector.  
**Status:** Documented ⚡ — Phase 2 implementation required

### AI-M3: No Intent Classification Design
**Gap:** Source documents mention "intent detection" as a feature but provide no model choice, training data strategy, or accuracy targets.  
**Generated solution:** `04-Architecture/AI-Architecture.md` — intent classification module.  
**Status:** Documented ⚡ — test set curation required before Phase 2

### AI-M4: AI Cost Model Not Analyzed
**Gap:** Source documents do not analyze the cost of AI capabilities at scale (LLM API costs per tenant per month).  
**Generated solution:** `04-Architecture/AI-Architecture.md` — cost estimation table.  
**Key finding:** ~₹500-2,000/tenant/month for AI at Growth plan usage. Fits within plan margins.  
**Status:** Documented ⚡ — requires validation with actual API cost testing

### AI-M5: Dify Self-Hosted vs. Cloud Not Decided
**Gap:** Source documents mention Dify but don't specify whether to self-host or use Dify Cloud.  
**Recommendation:** Self-host for production (data stays in India, cost control). Use Dify Cloud for development/experimentation.  
**Status:** Partially addressed in architecture — decision needs to be made before Phase 2

---

## Moderate AI Gaps

### AI-Mo1: No AI Monitoring / Observability
**Gap:** No plan for monitoring AI quality in production (hallucination rate, escalation rate, latency).  
**Generated solution:** `08-AI/Evaluation-Framework.md` — production metrics section.  
**Status:** Documented

### AI-Mo2: Vertical-Specific Intent Models Not Planned
**Gap:** A single intent classifier for all verticals (healthcare, retail, professional services) will have lower accuracy than vertical-specific models.  
**Recommendation:** Start with a universal classifier; fine-tune per-vertical as data accumulates.  
**Status:** Partially acknowledged

### AI-Mo3: Multi-Language AI Not Addressed
**Gap:** LLMs generally perform worse in Hindi, Telugu, Tamil vs. English. No plan for non-English customers.  
**Risk:** Significant portion of Conductor's target customers may communicate in regional languages.  
**Recommendation:** Use multilingual models (GPT-4o, Gemini Pro) which have better regional language support. Test explicitly.  
**Status:** Not addressed — Phase 2 consideration

### AI-Mo4: No Agent Training / Improvement Loop
**Gap:** No mechanism for agents to learn from feedback (customer ratings, agent corrections).  
**Recommendation:** Log all agent conversations with outcomes. Use low-quality outcomes to improve prompt and test data.  
**Status:** Not documented — Phase 3 consideration

---

## Minor AI Gaps

### AI-Mi1: No Token Budget Management
**Gap:** Long conversation histories could exceed model context windows.  
**Recommendation:** Implement context compression: summarize old turns before they exceed context limits.  
**Status:** Not documented

### AI-Mi2: Model Fallback Strategy
**Gap:** If primary model (GPT-4o) is unavailable, no fallback to secondary model defined.  
**Recommendation:** Circuit breaker: if OpenAI API fails → escalate conversation to human agent.  
**Status:** Not documented

---

## AI Readiness Assessment

| AI Capability | Source Doc Coverage | Generated Coverage | Phase |
|---|---|---|---|
| Intent classification | None | Full architecture | Phase 2 |
| FAQ / RAG bot | Mention only | Full RAG design | Phase 2 |
| Sentiment analysis | None | Design documented | Phase 2 |
| Appointment scheduling agent | Concept only | Agent architecture | Phase 3 |
| Lead qualification agent | Concept only | Agent architecture | Phase 3 |
| Conversation summarization | None | Design documented | Phase 3 |
| AI safety controls | None | Documented | Phase 2 (mandatory) |
| AI evaluation | None | Full framework | Phase 2 (mandatory) |

---

## Cross-References
- `04-Architecture/AI-Architecture.md` — AI platform design
- `04-Architecture/Agent-Architecture.md` — Agent design
- `08-AI/Knowledge-Model.md` — Knowledge base design
- `08-AI/RAG-Architecture.md` — RAG pipeline
- `08-AI/Prompt-Library.md` — Prompt templates
- `08-AI/Agent-Framework.md` — Agent implementation
- `08-AI/Evaluation-Framework.md` — AI quality evaluation
