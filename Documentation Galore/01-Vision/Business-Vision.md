# Business Vision — Conductor

**Status:** Partially Extracted + Remediated (⚡ = inferred)  
**Source:** Initiative Brief, Founder Strategic Lens  
**Last Updated:** June 2026

---

## Purpose
Defines the commercial vision: the market opportunity, the business Conductor intends to build, the category it will create, and the commercial outcomes it must achieve.

---

## The Market Opportunity

### Problem Scale
- **500M+ SMBs globally** operate without meaningful business automation
- **India alone has ~63M registered MSMEs** — the majority use WhatsApp as primary customer communication
- **WhatsApp Business** has 200M+ business users globally, yet <5% use any form of automation
- Existing automation tools (WATI, AiSensy, Interakt) address the *channel* (WhatsApp messaging) but not the *business process* layer
- Enterprise BPA platforms (ServiceNow, Pega) are priced out of reach for SMBs (>$50K/year)

### The Gap
No platform exists today that:
1. Delivers pre-built business outcome automation (not raw workflow primitives)
2. Works with SMB-grade technical sophistication (zero-code setup in <30 minutes)
3. Connects to the systems SMBs already use (Shopify, Razorpay, Zoho)
4. Scales from ₹1,500/month to ₹50,000/month as the business grows

### Market Size Estimate ⚡
*[Inferred — not confirmed in source documents. Must be validated with primary research.]*

| Segment | Count (India) | SAM @ ₹3K/mo | Notes |
|---|---|---|---|
| Digital-first SMBs (target) | ~5M | ₹1,800 Cr/year | Primary target |
| Traditional SMBs (future) | ~20M | ₹7,200 Cr/year | Requires simpler onboarding |
| International (Year 3+) | TBD | TBD | South Asia, MENA, SEA |

---

## Business Model ⚡

*[Not documented in source artifacts. The following is the recommended model based on platform characteristics. Must be validated and finalized by leadership.]*

### Model: Multi-Tier SaaS Subscription

Conductor generates revenue through **tiered subscription plans** charged per tenant (business), with overage charges for high-volume messaging.

**Recommended Tier Structure:**

| Plan | Target | Price (INR/month) | Core Limits |
|---|---|---|---|
| Starter | Solo operators, freelancers | ₹1,499 | 1 WA number, 2 workflows, 1,000 messages |
| Growth | SMBs 5-50 employees | ₹4,999 | 2 WA numbers, 20 workflows, 10,000 messages |
| Business | SMBs 50-200 employees | ₹12,999 | 5 WA numbers, unlimited workflows, 50,000 messages |
| Enterprise | Multi-location, 200+ staff | Custom | Custom numbers, SLA, dedicated support |

**Additional Revenue Streams:**
- **Connector Marketplace Commission:** 20-30% of connector/pack revenue from third-party ISVs
- **Message Overage:** ₹0.08 per message above plan limit
- **Professional Services:** Onboarding, custom capability pack development
- **White-Label licensing:** For channel partners and resellers ⚡

### Unit Economics Targets ⚡
| Metric | Target (Year 2) |
|---|---|
| Average Revenue Per Customer (ARPC) | ₹6,000/month |
| Customer Acquisition Cost (CAC) | <₹15,000 |
| LTV:CAC Ratio | >8:1 |
| Gross Margin | >75% |
| Net Revenue Retention | >110% |
| Monthly Churn | <2% |

---

## Commercial Strategy

### Phase 1: Validate in One Vertical (Months 1–6)
Choose one vertical with the highest signal-to-noise ratio. Recommendation: **Healthcare** (appointment reminders + payment collection have immediate, measurable ROI).

**Validation criteria:**
- 20 paying customers
- NPS > 40
- 80%+ monthly retention
- At least 2 customers expand from Starter to Growth

### Phase 2: Land and Expand (Months 7–18)
Expand to 3 verticals. Introduce connector marketplace. Build partner channel.

### Phase 3: Platform Play (Months 19–36)
Open ecosystem. Third-party capability packs. ISV marketplace. International expansion.

---

## Competitive Positioning

### Competitive Landscape ⚡

| Competitor | Category | Weakness vs Conductor |
|---|---|---|
| WATI | WhatsApp BSP + Automation | Channel-centric, not BPA. No business capability packs. |
| AiSensy | WhatsApp Marketing | Broadcast-focused. No workflow engine. |
| Interakt | WhatsApp CRM | CRM-centric. Not extensible. No event bus. |
| Zoho (suite) | SMB Suite | Complex, expensive, WhatsApp is an afterthought. |
| Make / Zapier | General Automation | Technical users. Not SMB-friendly. No WhatsApp native. |
| Twilio | CPaaS | Developer tool, not SMB product. |
| Freshworks / HubSpot | CRM + Sales | Enterprise-priced. WhatsApp is bolt-on. |

**Conductor's defensible position:** The only platform that abstracts business outcomes from communication channels, with a connector ecosystem and a marketplace model.

### Positioning Statement
> For SMB owners who need to automate customer engagement without hiring a developer, Conductor is the Business Process Automation Platform that delivers pre-built, outcome-focused automation packs for WhatsApp and beyond — unlike WhatsApp BSPs that only automate messaging, Conductor automates the business process behind the message.

---

## Business Benefits Delivered to Customers

### Revenue Impact
- Faster lead response → Higher conversion rate (industry benchmark: 5x improvement in <5 minute response)
- Automated nurturing → Reduced opportunity leakage
- Abandoned cart recovery → 10-15% recovery rate on automatable carts

### Cost Impact
- Administrative workload reduction: 30-50% estimated for appointment-heavy businesses
- Staff time reallocation: Support agents handle escalations, not FAQs
- Reduced no-shows: 25-40% reduction with automated reminder sequences

### Customer Experience Impact
- Response time: From hours to seconds
- Consistency: Every customer gets the same quality follow-up
- Personalization: Automated but contextual (name, order ID, appointment time)

---

## Success Metrics

### Adoption Metrics
| Metric | M3 Target | M6 Target | M12 Target |
|---|---|---|---|
| Active tenants | 10 | 50 | 200 |
| Monthly active users | 30 | 150 | 600 |
| Workflow creation rate | 3/tenant | 5/tenant | 8/tenant |

### Business Outcome Metrics
| Metric | Target |
|---|---|
| Messages automated/month/tenant | >5,000 |
| Lead conversion improvement | >20% |
| Appointment attendance rate | >80% |
| Payment collection improvement | >15% |

### Commercial Metrics
| Metric | M6 | M12 | M24 |
|---|---|---|---|
| MRR | ₹1.5L | ₹10L | ₹60L |
| Customer count | 50 | 200 | 800 |
| ARR | ₹18L | ₹120L | ₹720L |
| Retention | >85% | >88% | >90% |

---

## Cross-References
- See `02-Business/Pricing-Strategy.md` for detailed pricing architecture
- See `02-Business/Go-To-Market.md` for acquisition strategy
- See `01-Vision/Strategic-Thesis.md` for platform defensibility analysis
- See `09-Program/Resource-Plan.md` for funding and team requirements

---

## Missing Content (⚠️ Must Complete)
1. **Formal TAM/SAM/SOM analysis** with primary research
2. **Investor narrative** (problem → solution → market → traction → ask)
3. **Regulatory filing requirements** for operating a messaging platform in India
4. **Financial model** (P&L projection, cash flow, runway requirements)
5. **Partnership strategy** with BSPs (Gupshup, WATI, AiSensy as distribution partners vs. competitors)

## Maintenance Guidance
Review quarterly against actual customer data. Update commercial targets with board approval. Competitive analysis must be refreshed every 6 months minimum.
