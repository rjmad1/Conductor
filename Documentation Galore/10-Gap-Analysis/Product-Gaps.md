# Product Gaps — Conductor

**Status:** Review Board Assessment  
**Source:** Gap analysis of source documents against product requirements  
**Last Updated:** June 2026

---

## Critical Product Gaps

### P-C1: No Product Requirements Document
**Gap:** Source documents have no PRD or feature specifications.  
**Generated solution:** `03-Product/Product-Requirements.md` — full requirements for all 11 modules.  
**Risk if unresolved:** Engineers build the wrong features; stakeholders have no formal agreement on scope.  
**Status:** Documented ⚡ — requires stakeholder review and sign-off

### P-C2: No User Stories
**Gap:** Source documents have no user stories or acceptance criteria.  
**Generated solution:** `03-Product/User-Stories.md` — 20 user stories across 8 epics.  
**Risk if unresolved:** QA cannot write test cases; no definition of done for features.  
**Status:** Documented ⚡ — requires product and engineering review

### P-C3: Capability Pack Format Undefined
**Gap:** Source documents mention "200+ documented business automations" and "capability packs" but provide no specification for what a capability pack is technically.  
**Generated context:** `01-Vision/Strategic-Thesis.md` — capability pack JSON structure.  
**Risk:** Engineering cannot build the Capability Pack system without a formal spec.  
**Status:** Partially documented — formal spec required before engineering starts on capability layer

### P-C4: Workflow DSL Not Formally Specified
**Gap:** Source documents show the concept but not the complete DSL specification: all trigger types, condition operators, action types, and validation rules.  
**Generated solution:** `03-Product/Capabilities.md` and `03-Product/Product-Requirements.md` — comprehensive trigger/condition/action specification.  
**Status:** Documented — engineering should review before implementation

---

## Major Product Gaps

### P-M1: Onboarding Flow Not Designed
**Gap:** Source documents mention self-serve onboarding but provide no wireframes, step specifications, or conversion metrics.  
**Generated context:** `02-Business/Customer-Journeys.md` — activation funnel with 7 steps and conversion targets.  
**Risk:** Poor onboarding is the #1 cause of SaaS churn. This needs UX design before engineering.  
**Status:** Journey documented ⚡ — UX wireframes required

### P-M2: Error States and Empty States Undefined
**Gap:** No specifications for what users see when: no workflows are active, no customers are imported, messages fail, connectors are disconnected.  
**Risk:** Poor empty states and error handling create a frustrating first experience.  
**Status:** Not documented — UX design required

### P-M3: Mobile Experience Not Addressed
**Gap:** Source documents assume web-only. Conductor's target users (clinic owners, shop owners) often operate primarily from mobile.  
**Risk:** Web-only product with poor mobile experience loses a large customer segment.  
**Generated context:** `03-Product/Roadmap.md` — mobile app in Phase 3.  
**Recommendation:** Ensure web app is responsive (mobile browser) as minimum; native app in Phase 3.  
**Status:** Acknowledged — minimum: responsive web design

### P-M4: Template Approval Time Is a UX Problem
**Gap:** WhatsApp template approval takes 1-3 business days (Meta review). Source documents don't address how users experience this wait and what they can do in the meantime.  
**Risk:** Users who sign up expecting instant messaging will churn during the waiting period.  
**Mitigations to design:**
- Clear progress indicator during approval process
- Sandbox mode: allow testing with a Conductor-managed approved template library
- In-product guidance: "Your template is being reviewed. Here's what to prepare..."
**Status:** Not designed — UX work required

### P-M5: Connector Error Recovery UX Missing
**Gap:** When a connector (e.g., Shopify) token expires or is revoked, workflows silently fail. No UX for how tenants discover and resolve this.  
**Risk:** Silent workflow failures cause tenants to think Conductor is broken.  
**Recommended design:** Connector status dashboard, proactive alerts on connector errors, guided re-authentication flow.  
**Status:** Not documented — UX and engineering work required

---

## Moderate Product Gaps

### P-Mo1: A/B Testing Not in MVP
**Severity:** Moderate  
**Gap:** No A/B testing capability for templates or workflows.  
**Context:** Scheduled for Phase 2 (`03-Product/Roadmap.md`).  
**Status:** Acknowledged — Phase 2

### P-Mo2: Multi-Language Support Undefined
**Severity:** Moderate  
**Gap:** India has 22 official languages. WhatsApp is heavily used in Hindi, Telugu, Tamil, Kannada, Marathi. Platform is English-only.  
**Risk:** Limits adoption in non-English-dominant markets (Tier 2/3 cities, manufacturing verticals).  
**Status:** Acknowledged — Phase 3

### P-Mo3: Workflow Versioning Not Specified
**Severity:** Moderate  
**Gap:** What happens when a workflow configuration is changed while it's executing? Are there historical versions?  
**Recommendation:** Increment `version` field on any workflow update; executions reference the version at trigger time.  
**Status:** Partially addressed in data model — UI not designed

### P-Mo4: Billing Self-Service Upgrade / Downgrade Edge Cases
**Severity:** Moderate  
**Gap:** Pricing strategy documents billing cycles but not all edge cases: What happens to overage if upgrading mid-month? What if a tenant downgrades and has more workflows than the lower tier allows?  
**Status:** Not documented — requires product specification

---

## Minor Product Gaps

### P-Mi1: No In-App Help System
**Gap:** No contextual help, tooltips, or in-app documentation planned.  
**Recommendation:** Intercom or Chatwoot as in-app chat for support + Docs site (GitBook/Notion).  
**Status:** Not planned — Phase 2

### P-Mi2: Notification Preferences Not Specified
**Gap:** Which events should notify tenants and via which channel (email, in-app)?  
**Examples:** Template approved, message limit 80% reached, workflow failed.  
**Status:** Partially covered in monitoring section — product spec needed

### P-Mi3: Data Export Not Fully Specified
**Gap:** Tenants should be able to export their own data (DPDP compliance + data portability).  
**Status:** Mentioned in compliance — not designed as a product feature

---

## Product Gap Summary

| Gap | Severity | Status |
|---|---|---|
| No PRD | P-C1 | Documented ⚡ |
| No user stories | P-C2 | Documented ⚡ |
| Capability pack format undefined | P-C3 | Partially documented |
| Workflow DSL not formally specified | P-C4 | Documented ⚡ |
| Onboarding flow not designed | P-M1 | Journey documented, UX needed |
| Error/empty states undefined | P-M2 | Not addressed |
| Mobile experience not addressed | P-M3 | Acknowledged |
| Template approval UX problem | P-M4 | Not designed |
| Connector error recovery UX | P-M5 | Not designed |
| A/B testing | P-Mo1 | Phase 2 |
| Multi-language support | P-Mo2 | Phase 3 |
| Workflow versioning | P-Mo3 | Partial |
| Billing edge cases | P-Mo4 | Not documented |

---

## Cross-References
- `10-Gap-Analysis/Business-Gaps.md` — Business-level gaps
- `10-Gap-Analysis/Technical-Gaps.md` — Technical gaps
- `03-Product/Product-Requirements.md` — Requirements that address these gaps
