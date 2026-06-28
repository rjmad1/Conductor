# Business Model — Conductor

**Status:** MISSING from source → Fully Generated (⚡ = inferred/recommended)  
**Source:** None (gap-filled by review board)  
**Last Updated:** June 2026

---

## Purpose
Defines how Conductor creates, delivers, and captures value. This document is a critical gap in the original artifacts — without a business model, the platform cannot commercialize.

---

## Value Creation

Conductor creates value by **converting SMB operational inefficiency into automated business outcomes**:

| Input | Output |
|---|---|
| Manual WhatsApp follow-up (1 hour/day) | Automated follow-up (0 minutes/day) |
| 40% lead response rate | 90%+ automated response rate |
| 60% appointment show rate | 80%+ show rate with reminder sequences |
| 30-day payment collection cycle | 15-day cycle with automated reminders |

**Quantified value per customer (median SMB):** ₹15,000–₹40,000/month in time savings and revenue recovery, delivered at ₹1,500–₹12,999/month.

---

## Business Model Canvas

### Customer Segments
- SMBs with 5–500 employees in India (primary)
- Retail, Healthcare, Education, Professional Services, Real Estate (initial verticals)
- WhatsApp-dependent businesses (high communication volume on WhatsApp)

### Value Propositions
1. **Time saved:** Automate repetitive customer communication tasks
2. **Revenue recovered:** Never miss a lead, reminder, or follow-up
3. **Consistency:** Every customer gets the same experience
4. **Zero code:** No developer required to set up or modify

### Channels
- Direct: Self-serve website + trial
- Partners: Digital agencies, WhatsApp BSPs, vertical SaaS companies
- Content: Case studies, vertical-specific landing pages
- Product-led: Free trial with usage-based upgrade prompts

### Customer Relationships
- Self-serve onboarding for Starter/Growth
- Assisted onboarding for Business/Enterprise
- Community: Template/pack sharing marketplace
- Automated success: In-platform guidance, workflow performance reports

### Revenue Streams
1. **Subscription (primary):** Monthly/annual plans (see Pricing-Strategy.md)
2. **Connector Marketplace:** 20-30% commission on third-party connector/pack revenue
3. **Message Overage:** ₹0.08–₹0.15 per message over plan limit
4. **Professional Services:** Onboarding, custom pack development
5. **White-label licensing:** For resellers and channel partners ⚡

### Key Resources
- Workflow runtime engine
- Business Capability Pack library
- Connector ecosystem
- WhatsApp Cloud API access (Meta WABA)
- Engineering team (platform + connectors)
- Customer success team

### Key Activities
- Platform engineering (runtime, designer, AI layer)
- Capability pack development (vertical packs)
- Connector development and certification
- Customer onboarding and success
- Compliance (WhatsApp policy, DPDP India, GDPR)

### Key Partnerships
- **Meta (WhatsApp):** WABA access — existential dependency, must be managed
- **WhatsApp BSPs:** Gupshup, Twilio, Kaleyra — potential distribution or resale
- **System integrators / digital agencies:** For SMB customer acquisition
- **Vertical SaaS platforms:** Clinic management, school ERP, retail POS — for integration and co-selling
- **Payment gateways:** Razorpay, Cashfree — for payment collection flows

### Cost Structure
- **Engineering:** Platform development, maintenance (~50% of opex)
- **Infrastructure:** Cloud hosting, Kafka/NATS, PostgreSQL, Redis (~15%)
- **Meta API costs:** WhatsApp message charges (variable, passed to customer via overage pricing)
- **Customer acquisition:** Sales, marketing, content (~20%)
- **Operations:** Support, compliance, legal (~15%)

---

## Revenue Model Detail

### Subscription Revenue Model

```
MRR = (Starter customers × ₹1,499) 
    + (Growth customers × ₹4,999)
    + (Business customers × ₹12,999)
    + (Enterprise customers × custom)
    + Message overage revenue
    + Marketplace commission
```

### Projected Revenue Mix (Year 2) ⚡
| Stream | % of Revenue |
|---|---|
| Subscriptions | 75% |
| Message overage | 12% |
| Professional services | 8% |
| Marketplace commission | 5% |

### Unit Economics Model ⚡
| Metric | Starter | Growth | Business |
|---|---|---|---|
| MRR | ₹1,499 | ₹4,999 | ₹12,999 |
| Gross margin | 80% | 82% | 85% |
| Avg contract length | 8 months | 14 months | 20 months |
| LTV | ₹11,992 | ₹69,986 | ₹2,59,980 |
| CAC target | ₹3,000 | ₹10,000 | ₹25,000 |
| LTV:CAC | 4:1 | 7:1 | 10:1 |

---

## Billing Architecture Requirements

*This section identifies what must be built. Billing architecture is a CRITICAL GAP.*

### Required Billing Components
1. **Subscription management:** Create, upgrade, downgrade, pause, cancel subscriptions
2. **Usage metering:** Count messages sent per tenant per billing period
3. **Overage calculation:** Alert at 80% threshold, auto-charge or block at 100%
4. **Invoice generation:** GST-compliant invoices (Indian market)
5. **Payment processing:** Razorpay integration for auto-debit / card
6. **Trial management:** 14-day free trial with feature limits
7. **Dunning:** Automated payment failure recovery sequences
8. **Marketplace payouts:** Commission calculation and ISV payout for connector marketplace

### Recommended Billing Stack ⚡
- **Subscription engine:** Chargebee or Stripe Billing (India-compatible)
- **Payment gateway:** Razorpay (primary India), Stripe (international)
- **GST compliance:** Zoho Books integration or custom invoice generation
- **Metering:** Custom service that reads from event bus (message count events)

### Billing Data Model (Minimum Required)
```sql
tenants (id, name, plan_id, trial_ends_at, billing_email, gst_number)
plans (id, name, price_inr, message_limit, workflow_limit, wa_number_limit)
subscriptions (id, tenant_id, plan_id, status, starts_at, ends_at, auto_renew)
usage_records (id, tenant_id, metric, quantity, recorded_at, billing_period)
invoices (id, tenant_id, amount, gst_amount, status, due_date, paid_at)
payments (id, invoice_id, gateway_ref, amount, status, paid_at)
```

---

## Operating Model

### Customer Lifecycle
1. **Awareness** → Organic (SEO/content), paid (Meta Ads ironically), partner referral
2. **Trial** → 14-day free, full features, 500 message limit
3. **Activation** → First workflow live within 24 hours of signup (target)
4. **Conversion** → Trial-to-paid conversion target: >25%
5. **Expansion** → Plan upgrade when usage approaches limit
6. **Advocacy** → Referral program, case study participation
7. **Renewal** → Annual discount offer at 11-month mark

### Support Model
| Tier | Plan | Channel | SLA |
|---|---|---|---|
| Self-serve | Starter | In-app docs, community | No SLA |
| Email | Growth | Email + chat | 24h response |
| Priority | Business | Email + chat + phone | 4h response |
| Dedicated | Enterprise | Named CSM | 1h response |

---

## Cross-References
- `02-Business/Pricing-Strategy.md` — Detailed pricing tiers and packaging
- `01-Vision/Business-Vision.md` — Commercial targets and market sizing
- `04-Architecture/Infrastructure-Architecture.md` — Infrastructure cost model
- `07-Governance/Compliance.md` — GST, invoicing compliance requirements

## Maintenance Guidance
Update after each pricing experiment or business model change. Financial projections must be reviewed by founding team monthly during Year 1.

---

## Key Drivers (Synthesized via LOOP-502)
- **Compliance as a Feature:** Targeting regions with strict data laws like India (DPDP) and Europe (GDPR) via immutable audit logs and data residency (Mumbai region).
- **Scale:** Handling high-volume events seamlessly.
- **Enterprise Features:** Multi-tenancy, SSO (Keycloak), and deep integration into existing business tools (Zoho, Shopify, WhatsApp).
