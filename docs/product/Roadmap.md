# Product Roadmap — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** MVP Scope, Technical Layers, Founder Strategic Lens  
**Last Updated:** June 2026

---

## Roadmap Philosophy

> Build the smallest thing that validates the thesis. Expand only where customer data confirms demand.

The roadmap is organized into three phases, each with a clear validation gate before proceeding.

---

## Phase 0: Foundation (Months 1–2)

**Theme:** Core infrastructure, no customer-facing features

| Deliverable                                                 | Owner       | Status |
| ----------------------------------------------------------- | ----------- | ------ |
| Repository structure and monorepo setup                     | Engineering | TBD    |
| Infrastructure: AWS/GCP baseline (VPC, RDS, Redis, S3)      | DevOps      | TBD    |
| Keycloak setup (auth, RBAC, multi-tenancy)                  | Engineering | TBD    |
| PostgreSQL schema (tenant, user, customer, workflow tables) | Engineering | TBD    |
| NATS event bus baseline                                     | Engineering | TBD    |
| WhatsApp Cloud API integration (sandbox)                    | Engineering | TBD    |
| CI/CD pipeline (GitHub Actions → staging)                   | DevOps      | TBD    |
| Basic health monitoring (Prometheus + Grafana)              | DevOps      | TBD    |

**Gate:** Can send a WhatsApp message from the platform via API call.

---

## Phase 1: MVP (Months 3–5)

**Theme:** Healthcare + Professional Services Beachhead
**Goal:** 10 paying customers, NPS > 40

### Core Platform

| Feature                                   | Capability           | Priority |
| ----------------------------------------- | -------------------- | -------- |
| Business onboarding flow                  | Tenant Management    | P0       |
| WhatsApp number connection (WABA)         | WhatsApp Adapter     | P0       |
| Customer contact import (CSV + manual)    | Customer Registry    | P0       |
| Workflow designer (visual, no-code)       | Workflow Engine      | P0       |
| Trigger: Event-based (inbound WA message) | Workflow Engine      | P0       |
| Trigger: Schedule-based (time/cron)       | Workflow Engine      | P0       |
| Actions: Send WhatsApp template           | WhatsApp Adapter     | P0       |
| Actions: Delay, branch, update customer   | Workflow Engine      | P0       |
| Template management + variable binding    | Template Management  | P0       |
| Basic customer segmentation (tags)        | Customer Registry    | P0       |
| Consent management (opt-in/opt-out/STOP)  | Consent & Compliance | P0       |
| Audit log (basic)                         | Audit Layer          | P0       |
| Analytics dashboard (messages, workflows) | Analytics            | P0       |

### Initial Connectors

| Connector                                           | Priority |
| --------------------------------------------------- | -------- |
| Google Calendar (appointment trigger)               | P0       |
| Razorpay (payment events + payment link generation) | P0       |
| Shopify (order, cart events)                        | P1       |
| Zoho CRM (lead sync)                                | P1       |

### Pre-Built Capability Packs (MVP)

| Pack                    | Vertical              |
| ----------------------- | --------------------- |
| Appointment Reminder    | Healthcare            |
| Lab Report Delivery     | Healthcare            |
| Payment Reminder        | All                   |
| Abandoned Cart Recovery | Retail/E-Commerce     |
| Lead Follow-up          | Professional Services |
| Order Status Updates    | Retail                |

**Gate:** 10 paying customers, 80% activation rate (first message sent within 24h of signup), NPS > 40

---

## Phase 2: Growth (Months 6–12)

**Theme:** Multi-vertical, marketplace foundation
**Goal:** 100 paying customers, MRR ₹5L, NPS > 50

### Platform Enhancements

| Feature                                    | Capability                     |
| ------------------------------------------ | ------------------------------ |
| Multi-step drip campaigns                  | Campaign Engine                |
| NLU intent detection (basic)               | AI Layer / Conversation Engine |
| FAQ bot (knowledge base Q&A)               | AI Layer                       |
| Advanced segmentation (behavior-based)     | CDP                            |
| Workflow templates marketplace (community) | Workflow Engine                |
| Campaign A/B testing                       | Campaign Engine                |
| Two-way interactive conversations          | Conversation Engine            |
| Full agent inbox (Chatwoot integration)    | Conversation Engine            |
| SMS channel (Twilio/Exotel)                | Channel Adapters               |
| Webhook trigger (custom webhooks)          | Workflow Engine                |
| Connector SDK (public)                     | Integration Hub                |
| API-first access (REST + webhooks)         | Integration Hub                |

### Additional Connectors (Phase 2)

WooCommerce, Freshdesk, HubSpot, Tally, ERPNext, Instamojo, Google Sheets, WhatsApp catalog

### Capability Packs (Phase 2)

Customer re-engagement, Win-back campaigns, Customer onboarding, KYC collection, Loyalty program, Real estate lead qualification, Restaurant reservation management

### Business Features

| Feature                                                  | Notes                  |
| -------------------------------------------------------- | ---------------------- |
| Annual billing with discount                             | 2 months free          |
| Agency / reseller accounts                               | Multi-client dashboard |
| White-label option (custom domain, branding)             | Enterprise tier        |
| Billing integration (Chargebee / Razorpay subscriptions) | Automated billing      |
| Referral program (in-product)                            | ₹1,000 credit          |

**Gate:** 100 customers, MRR ₹5L, expansion revenue from Phase 1 customers, 3 published case studies

---

## Phase 3: Platform (Months 13–24)

**Theme:** Ecosystem, AI, international
**Goal:** 500+ customers, MRR ₹25L, marketplace live

### AI Layer (Phase 3)

| Feature                    | Description                                   |
| -------------------------- | --------------------------------------------- |
| AI lead qualification      | NLU-powered prospect scoring in conversation  |
| AI appointment scheduling  | Natural language booking without fixed menus  |
| Conversation summarization | Auto-summarize long conversations for agents  |
| AI workflow suggestions    | "Based on your business, try this automation" |
| Product recommendations    | E-commerce AI-powered product suggestions     |
| Predictive churn           | Identify at-risk customers before they leave  |

### Platform / Ecosystem

| Feature                        | Description                                     |
| ------------------------------ | ----------------------------------------------- |
| Connector marketplace (public) | Third-party connectors, certified by Conductor  |
| Capability pack marketplace    | ISV-built vertical packs, revenue share         |
| Public API                     | Full REST + webhook API for custom integrations |
| Embeddable widget              | Web chat widget powered by Conductor            |
| Mobile app                     | Conductor mobile dashboard for business owners  |
| Email channel                  | Transactional and campaign email                |
| Voice channel                  | IVR integration (Exotel / Knowlarity)           |

### International Expansion (Phase 3)

- Southeast Asia (Indonesia, Philippines — high WhatsApp usage)
- MENA (UAE, Saudi Arabia)
- Multi-language platform (English, Hindi, Telugu, Tamil)

**Gate:** Connector marketplace live with 10+ third-party connectors, ARR ₹3Cr+

---

## Feature Backlog (Parking Lot — Not Scheduled)

The following features are documented but NOT on the roadmap until a phase gate is passed:

- Full CDP with real-time behavioral segmentation
- AI agent autonomous workflows (no human in loop)
- Voice bot integration
- Full built-in CRM (pipeline, deals, contacts)
- Built-in helpdesk (avoid — use Freshdesk/Zendesk connector)
- Social media listening and automation
- Predictive analytics and forecasting
- No-code mobile app builder

---

## Roadmap Governance

### Feature Prioritization Framework (RICE)

| Score Component | Definition                                                |
| --------------- | --------------------------------------------------------- |
| Reach           | How many customers will this affect in 3 months?          |
| Impact          | How significantly? (3=massive, 2=high, 1=medium, 0.5=low) |
| Confidence      | How confident are we in R and I? (100%, 80%, 50%)         |
| Effort          | Engineering weeks to build and ship                       |
| **RICE Score**  | **(Reach × Impact × Confidence) / Effort**                |

### Change Control

- P0 items can only be removed from a phase with founding team approval
- P1 items can be reprioritized within a phase
- New features enter backlog; require RICE scoring before scheduling
- Customer-requested features weighted 3x in RICE Reach calculation

---

## Cross-References

- `03-Product/Product-Requirements.md` — Detailed requirements for each roadmap item
- `03-Product/Capabilities.md` — Capability specifications
- `09-Program/Implementation-Plan.md` — Engineering delivery plan
- `09-Program/Release-Plan.md` — Release cadence and versioning
