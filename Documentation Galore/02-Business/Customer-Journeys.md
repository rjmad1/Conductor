# Customer Journeys — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Business Needs, Persona analysis  
**Last Updated:** June 2026

---

## Purpose
Maps the end-to-end customer journeys for key personas across the lifecycle: awareness → purchase → activation → value realization → expansion → advocacy.

---

## Journey 1: Healthcare Clinic Owner (Dr. Sunita)

### Stage 1: Awareness
**Trigger:** Colleague mentions patient no-show rate dropped after implementing WhatsApp reminders  
**Action:** Googles "WhatsApp appointment reminder software India"  
**Touchpoint:** Conductor blog post "How to reduce patient no-shows by 30%"  
**Emotion:** Curious, skeptical ("another software I'll buy and not use")  
**Platform need:** SEO content, clear before/after value story

### Stage 2: Consideration
**Action:** Visits Conductor website, reads Healthcare use cases, watches 2-min demo video  
**Key questions:**
- Will this connect to my existing patient management system?
- How long does setup take?
- What happens if WhatsApp API goes down?
- Is this secure for patient data?

**Touchpoint:** Website, demo video, pricing page, healthcare case study  
**Emotion:** Interested but uncertain about complexity  
**Platform need:** Healthcare-specific landing page, DPDP/data security statement, clear integration list

### Stage 3: Trial
**Action:** Signs up for 14-day free trial  
**Day 0:** Selects "Healthcare" industry → pre-loaded templates appear  
**Day 1:** Connects WhatsApp Business number (5-step guided flow)  
**Day 2:** Activates "Appointment Reminder" capability pack  
**Day 3:** First automated reminder sent to test patient  
**Emotion:** Excited when first message works. Frustrated if setup is unclear.  
**Platform need:** Zero-to-first-value in <30 minutes. Healthcare-specific onboarding flow.

### Stage 4: Activation
**Day 7:** 15 appointment reminders sent automatically  
**Day 7:** Dashboard shows "2 patients rescheduled from reminder" (averted no-shows)  
**Emotion:** "This is actually working"  
**Platform need:** Success celebration moment in dashboard. Attribution of outcomes to platform.

### Stage 5: Conversion
**Day 12:** Approaching message limit → upgrade prompt  
**Action:** Upgrades to Growth plan (₹4,999/month)  
**Emotion:** Justified (saw ROI in trial)  
**Platform need:** Clear ROI statement at upgrade prompt ("Your platform saved you X hours this month")

### Stage 6: Expansion
**Month 3:** Adds lab report delivery workflow  
**Month 5:** Adds prescription refill reminder  
**Month 8:** Upgrades to Business plan (added second doctor, second WA number)  
**Platform need:** In-product suggestions for next automation. Workflow library with more packs.

### Stage 7: Advocacy
**Month 6:** Mentions platform at local IMA meeting  
**Month 9:** Agrees to be featured in case study  
**Action:** Refers 2 colleague clinics via referral link  
**Platform need:** Easy referral mechanism, case study co-creation program, NPS survey

---

## Journey 2: E-Commerce Founder (Rohan) — Shopify Abandoned Cart

### End-to-End Flow

```
[Customer abandons cart on Shopify]
        ↓
[Shopify webhook fires → Conductor Event Bus]
        ↓
[Conductor workflow: CartAbandoned trigger fires]
        ↓
[Condition check: Cart value > ₹500 AND customer has WA number]
        ↓
[Action: Send WhatsApp template "cart_recovery_v1" after 30min delay]
        ↓
[Customer receives WhatsApp: "Hey Priya, you left ₹1,200 worth in your cart..."]
        ↓
[Customer clicks payment link in message]
        ↓
[Order completes on Shopify]
        ↓
[Conductor workflow: OrderCompleted → Send order confirmation WA message]
        ↓
[Analytics: Cart recovery rate tracked, revenue attributed]
```

### Rohan's Setup Journey
**Day 1 (30 min setup):**
1. Connect Shopify store → OAuth flow
2. Review pre-built "Abandoned Cart Recovery" pack
3. Customize message template (name, cart contents are auto-populated)
4. Set trigger: 30-minute delay after cart abandonment
5. Set condition: Cart value > ₹500
6. Activate

**Day 7:** Reviews dashboard — 3 carts recovered, ₹4,200 revenue attributed  
**Platform need:** Revenue attribution dashboard, Shopify order sync confirmation

---

## Journey 3: Patient (End Customer of Healthcare Tenant)

*Note: This is a B2B2C journey — Conductor serves the clinic (B2B) which serves the patient (C).*

### Patient Appointment Reminder Journey

```
[Patient books appointment at Dr. Sunita's clinic]
        ↓
[Clinic staff adds appointment to calendar / clinic system]
        ↓
[Conductor event: AppointmentCreated (manual or via integration)]
        ↓
[Workflow: Send confirmation WhatsApp immediately]
        ↓
[Patient receives: "Confirmed: Dr. Sunita, Friday 3pm. Reply RESCHEDULE if needed."]
        ↓
[Workflow: Send reminder 24 hours before]
        ↓
[Patient receives: "Reminder: Your appointment tomorrow at 3pm. Reply YES to confirm."]
        ↓
[If patient replies YES → mark confirmed]
[If patient replies RESCHEDULE → workflow starts rescheduling flow]
[If no reply after 2 hours → send second reminder]
        ↓
[Day of appointment: 2-hour reminder sent]
        ↓
[Post-appointment: Feedback collection message sent]
```

**Patient experience requirements:**
- Messages feel personalized (name, doctor name, time)
- Easy to respond (YES / NO / RESCHEDULE)
- Not spammy (max 3 messages per appointment)
- STOP opt-out must work instantly

---

## Journey 4: New Platform User Onboarding

### Activation Funnel

| Step | Action | Target Completion Rate |
|---|---|---|
| 1 | Sign up | 100% |
| 2 | Choose industry | 95% |
| 3 | Connect WhatsApp number | 70% |
| 4 | Activate first workflow | 55% |
| 5 | Send first automated message | 45% |
| 6 | View dashboard with first results | 40% |
| 7 | Complete trial → upgrade | 25% |

**Drop-off risk points:**
- Step 3 (WA number connection) — WhatsApp Business API verification takes 1-3 days. Must set expectations clearly. Mitigation: Allow sandbox testing during verification.
- Step 4 (First workflow) — Users with no templates get lost. Mitigation: Pre-loaded industry templates as default.

---

## Journey 5: Support Escalation (Tenant Agent)

### Support Ticket Automation Flow

```
[Customer sends WhatsApp to business: "My order is damaged"]
        ↓
[Conductor NLU: Detects complaint intent]
        ↓
[Workflow: Create support ticket, assign ticket ID]
        ↓
[Auto-reply: "Your complaint #TK-1234 has been registered. Our team will contact you within 2 hours."]
        ↓
[Agent receives ticket in shared inbox (Chatwoot integration)]
        ↓
[Agent reviews, responds within SLA]
        ↓
[Workflow monitors SLA breach — auto-escalate at T+2h if unresolved]
        ↓
[Resolution: Agent marks resolved]
        ↓
[Workflow: Send resolution confirmation + feedback request to customer]
```

---

## Journey: Edge Cases That Must Be Handled

| Scenario | Required Behavior |
|---|---|
| Customer sends STOP | Immediately opt-out from ALL messages for that tenant. Log consent withdrawal. |
| WhatsApp message delivery fails | Retry logic (3 retries, exponential backoff). Alert workflow as failed. |
| Customer replies in different language | Route to human agent. Log language preference. |
| Customer replies abusive message | Detect with NLU, escalate to human agent, log incident. |
| Workflow loops infinitely | Max execution count per customer per workflow (50/day). Circuit breaker. |
| Tenant exceeds message limit mid-month | Alert tenant, pause new outbound (but allow inbound), offer overage pack. |
| WhatsApp template rejected by Meta | Alert tenant, disable workflow, log template status. |

---

## Cross-References
- `02-Business/Personas.md` — Persona definitions
- `03-Product/User-Stories.md` — User stories derived from journeys
- `04-Architecture/Solution-Architecture.md` — Technical realization of flows
- `03-Product/Capabilities.md` — Capability pack specifications

## Maintenance Guidance
Update journeys after user research sessions. Add new journey for each new vertical. Edge cases section must be reviewed by engineering before each release.
