# Risk Register — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Gap analysis, compliance review  
**Last Updated:** June 2026

---

## Risk Scoring Matrix

**Likelihood:** 1 (Rare) → 5 (Almost Certain)  
**Impact:** 1 (Negligible) → 5 (Catastrophic)  
**Risk Score:** Likelihood × Impact

| Score | Rating |
|---|---|
| 1-4 | Low |
| 5-9 | Medium |
| 10-16 | High |
| 17-25 | Critical |

---

## Critical Risks (Score 17-25)

### R-001: WhatsApp Business Account (WABA) Suspension
**Category:** Technology / Compliance  
**Likelihood:** 3 | **Impact:** 5 | **Score: 15 → HIGH**  
**Description:** Meta suspends Conductor's WABA or a tenant's WABA due to policy violations, spam complaints, or quality rating degradation. Core platform functionality is lost immediately.  
**Mitigations:**
- Strict consent enforcement (no messaging opted-out customers)
- STOP handling implemented within 5 seconds
- Message quality monitoring and proactive tenant alerts
- Prohibited industry verification at onboarding
- Official WhatsApp Cloud API only (no unofficial libraries)
- Diversify with BSP partnership (Gupshup/Kaleyra as backup)

**Residual Risk:** HIGH — fully dependent on Meta's policy decisions  
**Owner:** Engineering + Product

---

### R-002: DPDP India Non-Compliance at Launch
**Category:** Regulatory  
**Likelihood:** 4 | **Impact:** 4 | **Score: 16 → HIGH**  
**Description:** Platform launches without full DPDP compliance (consent capture, erasure, breach notification). Regulatory audit results in ₹250Cr maximum penalty.  
**Mitigations:**
- Complete DPDP compliance checklist (07-Governance/Compliance.md) before launch
- Legal counsel review of privacy policy and DPA
- DPDP-compliant consent capture on Day 1
- Data erasure workflow implemented pre-launch

**Residual Risk:** MEDIUM after mitigations  
**Owner:** Engineering + Legal

---

### R-003: WhatsApp API Dependency (Single Point of Failure)
**Category:** Technology  
**Likelihood:** 3 | **Impact:** 5 | **Score: 15 → HIGH**  
**Description:** Meta's WhatsApp platform experiences outages (historically 2-3x/year with significant disruptions). Conductor has no fallback communication channel in MVP.  
**Mitigations:**
- Temporal retry handles short outages automatically
- SMS fallback channel (Phase 2 mitigation)
- Tenant communication during Meta outages
- Queue messages for delivery when service restores

**Residual Risk:** HIGH in MVP — structural dependency  
**Owner:** Engineering

---

## High Risks (Score 10-16)

### R-004: Unauthorized WhatsApp Library Use
**Category:** Technology / Legal  
**Likelihood:** 2 | **Impact:** 5 | **Score: 10 → HIGH**  
**Description:** Source documents recommend OpenWA (unofficial). If this library is used, Meta can immediately terminate all associated WABAs.  
**Status:** RESOLVED — identified and documented. Architecture mandates WhatsApp Cloud API only.  
**Owner:** Engineering (code review enforcement)

---

### R-005: Multi-Tenant Data Breach
**Category:** Security  
**Likelihood:** 2 | **Impact:** 5 | **Score: 10 → HIGH**  
**Description:** Bug causes cross-tenant data access — Tenant A can see Tenant B's customer data.  
**Mitigations:**
- tenant_id on every table and every query (mandatory, enforced in code review)
- Tenant isolation tests in integration test suite
- JWT-based tenant context (not user-supplied)
- Security penetration test before launch

**Residual Risk:** MEDIUM after mitigations  
**Owner:** Engineering

---

### R-006: Key Person Dependency
**Category:** Operational  
**Likelihood:** 3 | **Impact:** 4 | **Score: 12 → HIGH**  
**Description:** With a small founding team, loss of 1-2 key engineers causes significant delivery risk.  
**Mitigations:**
- Documentation (this repository) reduces knowledge concentration
- No single engineer owns all critical systems
- On-call rotation ensures multiple people know production
- Cross-training plan for critical components

**Residual Risk:** MEDIUM  
**Owner:** CTO

---

### R-007: Camunda/Temporal Architecture Conflict
**Category:** Technology  
**Likelihood:** 2 | **Impact:** 4 | **Score: 8 → MEDIUM**  
**Description:** Source documents have conflicting recommendations (Camunda 8 vs. Temporal). Wrong choice could require expensive re-architecture.  
**Status:** RESOLVED — Temporal chosen. Decision documented in Strategic-Thesis.md.  
**Owner:** Engineering Lead

---

### R-008: Tenant Churn Due to Poor WhatsApp API Delivery
**Category:** Business  
**Likelihood:** 3 | **Impact:** 3 | **Score: 9 → MEDIUM**  
**Description:** Tenants see poor delivery rates due to WABA tier limits (1,000 msg/day Tier 1) and churn because the platform "doesn't work."  
**Mitigations:**
- Clear communication of WABA tier limits during onboarding
- Help tenants apply for Tier 2 WABA upgrade (needs 1,000+ conversations)
- Dashboard shows "Messages failed due to Meta limits" distinctly from platform errors

**Residual Risk:** MEDIUM  
**Owner:** Product + Engineering

---

### R-009: Pricing Below Cost at Scale
**Category:** Financial  
**Likelihood:** 2 | **Impact:** 4 | **Score: 8 → MEDIUM**  
**Description:** At Growth tier (₹4,999/month, 10,000 messages), infrastructure + Meta API costs could exceed revenue for high-usage tenants.  
**Cost components per 10,000 messages:**
- Infrastructure: ~₹200
- Meta API conversation cost: ~₹800 (₹0.08/conversation average)
- Total COGS: ~₹1,000 on ₹4,999 revenue — 80% gross margin (acceptable)

**Mitigations:**
- Overage pricing at ₹0.30/message covers marginal cost
- Monitor gross margin per plan per cohort

**Residual Risk:** LOW  
**Owner:** Finance + Product

---

### R-010: Connector API Breaking Changes
**Category:** Technology  
**Likelihood:** 3 | **Impact:** 3 | **Score: 9 → MEDIUM**  
**Description:** Shopify, Razorpay, or Google Calendar change their APIs, breaking Conductor's connectors.  
**Mitigations:**
- Subscribe to each connector's developer changelog
- Integration tests against connector sandboxes in CI/CD
- Version-pin connector SDKs where possible
- Alert tenants if a connector becomes unavailable

**Residual Risk:** MEDIUM — external dependency inherently unpredictable  
**Owner:** Engineering

---

## Medium Risks (Score 5-9)

| Risk | Likelihood | Impact | Score | Mitigation |
|---|---|---|---|---|
| R-011: GST non-compliance | 2 | 3 | 6 | GSTIN registration before launch, accounting software |
| R-012: Keycloak self-hosted complexity | 2 | 3 | 6 | Use Keycloak as managed service (Bitnami on AWS) |
| R-013: NATS data loss (non-persistent config) | 2 | 4 | 8 | Enable JetStream persistence, 7-day retention |
| R-014: Competitor mimicry of capability packs | 3 | 2 | 6 | Build customer lock-in through data, not just features |
| R-015: Team scaling outpaces process | 3 | 3 | 9 | Document processes now; enforce before team grows |

---

## Risk Review Cadence

| Frequency | Activity |
|---|---|
| Monthly | Review top 5 risks, update mitigation status |
| Quarterly | Full risk register review, add new risks |
| Before each major release | Review release-specific risks |
| Post-incident | Add new risk if gap identified in incident |

---

## Cross-References
- `07-Governance/Compliance.md` — Compliance risk details
- `07-Governance/Decision-Records.md` — Risk-driven architecture decisions
- `10-Gap-Analysis/Technical-Gaps.md` — Gaps that create risks
