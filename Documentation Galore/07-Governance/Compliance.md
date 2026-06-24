# Compliance — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None in source documents (critical gap)  
**Last Updated:** June 2026

---

## Compliance Scope

Conductor must comply with the following regulatory and policy frameworks:

| Framework | Applicability | Risk if Non-Compliant |
|---|---|---|
| **DPDP India (Digital Personal Data Protection Act 2023)** | Mandatory — handles Indian citizen data | Regulatory penalties up to ₹250 crore per violation |
| **WhatsApp Business Policy** | Mandatory — WhatsApp is core channel | WABA suspension — loss of entire platform functionality |
| **Meta Platform Terms of Service** | Mandatory | API access revoked |
| **GSTIN / GST Compliance (India)** | Mandatory — B2B SaaS product | Tax penalties |
| **GDPR** | Future — international expansion | €20M / 4% global turnover fine |
| **ISO 27001** | Aspirational — enterprise sales | Not mandatory MVP, recommended Phase 3 |

---

## DPDP India Compliance

### Key Requirements

**1. Consent (Section 6)**
- Requirement: Obtain free, specific, informed, and unambiguous consent before processing personal data
- Conductor implementation:
  - Consent captured at opt-in with explicit consent text
  - Consent record stored (immutable append-only table)
  - Pre-checked consent boxes are NOT permitted
  - Bundled consent is NOT permitted (must be separate for each purpose)

**2. Notice (Section 5)**
- Requirement: Give a clear notice about what data is being collected and why
- Conductor implementation:
  - Conductor provides a Privacy Notice template for tenants to share with their customers
  - Tenants are contractually required to provide notice to their customers

**3. Right to Erasure (Section 12)**
- Requirement: Data principal may request deletion of their personal data
- Conductor implementation:
  - Tenant receives deletion request from their customer
  - Tenant triggers "Delete Customer" in Conductor
  - Conductor anonymizes customer PII within 30 days
  - Audit trail maintained (without PII)
  - Exception: Data required for legal/audit purposes retained per applicable law

**4. Right to Grievance Redressal (Section 13)**
- Requirement: Mechanism for data principal to raise grievances
- Conductor implementation:
  - Tenants must have a grievance email
  - Conductor's own DPO contact: dpo@conductor.io (Phase 2)
  - Grievance resolution within 30 days

**5. Data Localization (Proposed)**
- Requirement: Certain data categories must be stored in India
- Conductor implementation:
  - Primary database in AWS ap-south-1 (Mumbai) — India data residency
  - Customer PII not replicated to non-India regions
  - Exception: Disaster recovery copy in ap-south-2 (Hyderabad) — still India

**6. Data Breach Notification (Section 8)**
- Requirement: Notify DPBI (Data Protection Board of India) within 72 hours of becoming aware of a breach
- Conductor implementation:
  - Incident severity P0 if potential breach
  - Engineering Lead + CTO notified immediately
  - Legal counsel engaged within 2 hours of confirmed breach
  - DPBI notification within 72 hours

**7. Data Minimization**
- Collect only what is necessary for the stated purpose
- Conductor stores only: phone, name, email, consent, tags, custom attributes defined by tenant
- Does NOT collect: location, biometric, financial account details by default

---

## WhatsApp Business Policy Compliance

### Mandatory Requirements

**1. Consent for Marketing Messages**
- Only send marketing templates to customers who have explicitly opted in
- Opt-in must be clear and unambiguous — not buried in T&Cs
- Implementation: `wa_opt_in_status = 'opted_in'` required before sending marketing messages

**2. STOP Handling**
- Mandatory: Immediately stop messaging any customer who replies STOP/Unsubscribe
- SLA: Must stop within the same messaging session (< 5 seconds in Conductor)
- Implementation: STOP keyword detector in conversation-service

**3. Template Compliance**
- Only use Meta-approved templates for business-initiated conversations
- Templates must match the approved content — do not deviate at send time
- Prohibited template categories: political advocacy, adult content, illegal goods

**4. Prohibited Industries**
Conductor must NOT onboard tenants from these industries:
- Weapons and ammunition
- Tobacco and vaping
- Adult content/services
- Gambling (unless licensed)
- Online pharmacies without verification
- MLM / pyramid schemes
- Cryptocurrency (without proper licensing)

**Implementation:** Tenant onboarding includes industry verification + self-certification of prohibited use compliance.

**5. Frequency Limits**
- Meta does not specify a hard limit, but high opt-out rates can result in WABA downgrade
- Conductor enforces: max 1 marketing conversation per customer per day
- Recommend: max 4 marketing messages per customer per month (best practice)

**6. Message Quality Rating**
- Meta rates each business's message quality (Green / Yellow / Red)
- Red rating → account review → possible suspension
- Conductor monitors quality rating via Meta API and alerts tenants when below Green

---

## GST Compliance (India)

**Conductor's GST Registration:** Required (SaaS is taxable at 18% GST)

**Invoicing requirements:**
- Invoice must include: Conductor's GSTIN, customer GSTIN (if B2B), HSN code, GST breakdown (CGST + SGST or IGST)
- HSN Code for SaaS: 998313 (Information Technology services)
- Invoices must be issued within the same month as the subscription renewal

**Implementation:**
- billing-service generates GST-compliant invoices
- Invoice template includes: GSTIN, SAC code, base amount, 18% GST, total
- TDS: Tenants paying >₹30,000/year may be required to deduct TDS — Conductor must support Form 16A

---

## Data Processing Agreement (DPA)

Conductor must execute a DPA with:
- All tenants (Conductor is a Data Processor for tenant customer data)
- All third-party services that handle personal data (AWS, Meta, Razorpay, Google)

**Conductor's DPA with tenants covers:**
- Nature of processing (message delivery, workflow execution)
- Data categories (customer contact details, consent data)
- Security measures
- Subprocessor list (Meta, AWS, Razorpay)
- Deletion and return of data on contract termination
- Breach notification obligations

---

## Compliance Checklist (Pre-Launch)

| Item | Status | Owner |
|---|---|---|
| DPDP-compliant consent capture | MISSING ⚠️ | Engineering |
| STOP keyword handler (< 5s) | MISSING ⚠️ | Engineering |
| Data deletion workflow (30-day) | MISSING ⚠️ | Engineering |
| GST-compliant invoice generation | MISSING ⚠️ | Engineering |
| Prohibited industry checklist in onboarding | MISSING ⚠️ | Product |
| Privacy policy published | MISSING ⚠️ | Legal |
| Terms of Service published | MISSING ⚠️ | Legal |
| Data Processing Agreement template | MISSING ⚠️ | Legal |
| GSTIN registration | MISSING ⚠️ | Finance |
| Meta WABA approved | MISSING ⚠️ | Business |
| WhatsApp policy compliance self-audit | MISSING ⚠️ | Legal |

**All items above are PRE-LAUNCH BLOCKERS.**

---

## Ongoing Compliance

| Activity | Frequency | Owner |
|---|---|---|
| Review consent records for integrity | Monthly | Engineering |
| Review opt-out processing SLA | Monthly | Engineering |
| WhatsApp quality rating review | Weekly | Operations |
| DPDP erasure request processing | On-demand (30-day SLA) | Engineering + Legal |
| GST filing | Monthly | Finance |
| Privacy policy review | Annually | Legal |
| DPA review with subprocessors | Annually | Legal |

---

## Cross-References
- `07-Governance/Risk-Register.md` — Compliance risks
- `04-Architecture/Security-Architecture.md` — Security controls enabling compliance
- `04-Architecture/Data-Architecture.md` — Data handling implementation
- `06-Operations/Runbooks.md` — Runbook 8: Data deletion procedure
