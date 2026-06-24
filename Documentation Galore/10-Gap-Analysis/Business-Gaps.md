# Business Gaps — Conductor

**Status:** Review Board Assessment  
**Source:** Gap analysis of source documents against business requirements  
**Last Updated:** June 2026

---

## Gap Assessment Framework

| Severity | Definition |
|---|---|
| **C (Critical)** | Missing — platform cannot launch without addressing |
| **M (Major)** | Significant weakness — will cause problems within 90 days of launch |
| **Mo (Moderate)** | Addressable in Phase 2 — significant but not immediately blocking |
| **Mi (Minor)** | Nice-to-have — low urgency |

---

## C1: No Pricing Strategy in Source Documents
**Severity:** Critical  
**Gap:** Source documents contain no pricing model, tier definitions, or billing mechanics.  
**Risk:** Cannot launch without pricing. Investors, customers, and the billing system all need this.  
**Generated solution:** `02-Business/Pricing-Strategy.md` — full 4-tier pricing model, overage, free trial.  
**Remaining work:** Founder must validate pricing against market research and 3-5 customer conversations before launch.  
**Status:** Documented (⚡ inferred) — requires founder validation

---

## C2: No Go-To-Market Strategy
**Severity:** Critical  
**Gap:** Source documents do not specify any customer acquisition strategy, channels, messaging, or launch plan.  
**Risk:** Without a GTM strategy, even a technically perfect product will have no customers.  
**Generated solution:** `02-Business/Go-To-Market.md` — healthcare beachhead, 4 acquisition channels, PLG design, launch plan.  
**Remaining work:** Founder must validate vertical choice and channel assumptions with actual customer discovery.  
**Status:** Documented (⚡ inferred) — requires founder validation

---

## C3: No Compliance Framework
**Severity:** Critical  
**Gap:** Source documents do not address DPDP India compliance, WhatsApp Business Policy compliance, or GST requirements.  
**Risk:** Non-compliance could result in regulatory penalties (DPDP: up to ₹250 crore), WABA suspension (loss of platform), or tax penalties.  
**Generated solution:** `07-Governance/Compliance.md` — full compliance checklist and implementation requirements.  
**Remaining work:** Legal counsel must review before launch. Pre-launch compliance checklist must be 100% complete.  
**Status:** Documented — URGENT — requires legal review

---

## C4: OpenWA Risk Not Addressed in Source
**Severity:** Critical  
**Gap:** Source documents recommend evaluating OpenWA (unofficial WhatsApp library). This is a ToS violation.  
**Risk:** Using OpenWA in production would risk permanent suspension of all WABAs on the platform.  
**Resolution:** Documented in `07-Governance/Decision-Records.md` ADR-003. WhatsApp Cloud API mandated.  
**Status:** Resolved in documentation — engineering must enforce in code

---

## C5: No Customer Persona Documentation
**Severity:** Critical → now Major (resolved)  
**Gap:** Source documents identify verticals but have no persona profiles.  
**Risk:** Without personas, product decisions, UI design, and sales messaging will be misaligned.  
**Generated solution:** `02-Business/Personas.md` — 5 detailed personas with goals, frustrations, decision criteria.  
**Remaining work:** Personas must be validated with 5+ real customer conversations per vertical.  
**Status:** Documented (⚡ inferred) — requires validation

---

## M1: No Customer Journey Maps
**Severity:** Major  
**Gap:** Source documents have no end-to-end customer journey documentation.  
**Risk:** UI and automation design will miss critical moments without journey context.  
**Generated solution:** `02-Business/Customer-Journeys.md` — 5 detailed journeys including edge cases.  
**Status:** Documented — requires product review and usability testing

---

## M2: Revenue and Unit Economics Not Validated
**Severity:** Major  
**Gap:** Source documents have no financial model, unit economics, or revenue projections.  
**Risk:** Without validated unit economics, pricing could be set incorrectly (too low → unsustainable; too high → no customers).  
**Generated solution:** `02-Business/Business-Model.md` — unit economics model, MRR projections.  
**Key assumptions to validate:**
- Gross margin: 80% at Growth tier (need to validate with actual Meta API costs)
- Trial-to-paid conversion: 25% (need market data or A/B test)
- Monthly churn: 5% (needs industry benchmark validation)  
**Status:** Documented — financial model requires founder + accountant review

---

## M3: Competitor Landscape Incomplete
**Severity:** Major  
**Gap:** Source documents identify some competitors (WATI, AiSensy) but have no structured competitive analysis.  
**Risk:** Sales team cannot articulate differentiation clearly; pricing may be mispositioned.  
**Partial coverage:** `01-Vision/Business-Vision.md` — competitive landscape table.  
**Remaining gaps:**
- No feature comparison matrix
- No pricing differentiation analysis
- No battle cards for sales
**Status:** Partially addressed — competitive brief should be commissioned (use product-management skill)

---

## M4: No Partnership Strategy Detail
**Severity:** Major  
**Gap:** Source documents mention partnerships but have no structured partner program.  
**Risk:** Missing key distribution channel (agency partners, Shopify App Store) delays growth.  
**Generated solution:** `02-Business/Go-To-Market.md` — partner tiers and structure.  
**Remaining work:** Legal: Partner agreements. Engineering: Partner portal (Phase 2).  
**Status:** Partially documented — partner agreements not written

---

## Mo1: International Expansion Not Defined
**Severity:** Moderate  
**Gap:** Source documents focus on India only. No international expansion criteria or timeline.  
**Generated context:** `03-Product/Roadmap.md` — SEA and MENA as Phase 3 targets.  
**Status:** Acknowledged — not required for MVP

---

## Mo2: Reseller / White-Label Model Underspecified
**Severity:** Moderate  
**Gap:** Agency reseller and white-label option mentioned but not specified (pricing, legal, technical requirements).  
**Generated context:** `02-Business/Pricing-Strategy.md` — reseller pricing table.  
**Status:** Partially addressed — full specification needed for Phase 2

---

## Mi1: No Brand Guidelines
**Severity:** Minor  
**Gap:** No brand identity, logo, color palette, or messaging guidelines documented.  
**Impact:** Inconsistent visual identity across marketing and product.  
**Status:** Not addressed — needed before public launch

---

## Mi2: No Customer Advisory Board Structure
**Severity:** Minor  
**Gap:** Design partner program mentioned but no structured advisory board.  
**Status:** Not critical for MVP

---

## Gap Summary

| Gap | Severity | Status |
|---|---|---|
| No pricing strategy | C1 | Documented ⚡ |
| No GTM strategy | C2 | Documented ⚡ |
| No compliance framework | C3 | Documented — legal review needed |
| OpenWA ToS risk | C4 | Resolved |
| No customer personas | C5 | Documented ⚡ |
| No customer journeys | M1 | Documented ⚡ |
| Unvalidated unit economics | M2 | Documented ⚡ |
| Incomplete competitive analysis | M3 | Partial |
| No partnership strategy detail | M4 | Partial |
| International expansion undefined | Mo1 | Acknowledged |
| Reseller model underspecified | Mo2 | Partial |
| No brand guidelines | Mi1 | Not addressed |

---

## Cross-References
- `00-Executive-Summary.md` — Full gap summary at strategic level
- `10-Gap-Analysis/Product-Gaps.md` — Product-specific gaps
- `10-Gap-Analysis/Technical-Gaps.md` — Technical gaps
