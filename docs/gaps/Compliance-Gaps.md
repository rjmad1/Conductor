# Compliance Gaps — Conductor

**Status:** Review Board Assessment  
**Source:** Gap analysis against compliance requirements  
**Last Updated:** June 2026

---

## Compliance Readiness Overview

**VERDICT: Not launch-ready for compliance.**

The source documents have **zero compliance content**. A platform that processes personal data of Indian citizens, sends WhatsApp messages on behalf of businesses, and charges for a subscription service has mandatory legal and regulatory obligations that are entirely undocumented.

**All items below are required before public launch.**

---

## DPDP India Compliance Gaps

### DPDP-C1: No Consent Capture Mechanism Designed
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** No UI flow, API design, or data model for collecting and storing customer consent.  
**Risk:** DPDP Section 6 requires explicit, purpose-specific consent. Sending marketing messages without valid consent = violation.  
**Generated solution:** `07-Governance/Compliance.md` — consent requirements. `04-Architecture/Data-Architecture.md` — consent_records table.  
**Required implementation:**
- Opt-in collection at customer touchpoints (forms, WhatsApp, in-person)
- Consent text displayed to customer at opt-in
- Consent record stored with: text, date, method, IP
- Double opt-in recommended for marketing messages

### DPDP-C2: No Data Erasure Implementation
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** DPDP Section 12 gives data principals the right to erasure. No implementation exists.  
**Risk:** Failure to comply with an erasure request is a violation.  
**Generated solution:** `06-Operations/Runbooks.md` — Runbook 8: Data Deletion.  
**Required implementation:**
- "Delete Customer" function in tenant UI
- Anonymization of PII fields within 30 days
- Confirmation workflow (tenant → Conductor → audit log)

### DPDP-C3: No Privacy Notice
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** DPDP Section 5 requires clear notice before data processing.  
**Required:** Privacy policy published at conductor.io/privacy  
**Minimum content:** What data is collected, purpose, how long retained, how to request deletion, who to contact  
**Status:** Not drafted — legal counsel required

### DPDP-C4: No Data Breach Notification Plan
**Status:** Critical  
**Gap:** DPDP requires notification to DPBI within 72 hours of a data breach.  
**Generated context:** `04-Architecture/Security-Architecture.md` — incident response section.  
**Required implementation:**
- Breach detection alerting
- Documented escalation to legal counsel
- DPBI notification template prepared
- Affected tenant notification template

### DPDP-C5: Data Localization Not Confirmed
**Status:** Critical  
**Gap:** All customer PII must remain in India.  
**Required confirmation:** AWS ap-south-1 region for RDS (customer data) confirmed as primary region. No replication to non-India regions.  
**Status:** Documented as recommendation — must be enforced at infrastructure level

---

## WhatsApp Policy Compliance Gaps

### WA-C1: STOP Handling Not Tested
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** STOP handling is implemented in design but has not been tested at the message processing level.  
**Required:** End-to-end test: customer sends STOP → opt-out recorded → no further messages sent → verified within 5 seconds.  
**Status:** Implementation documented — testing required

### WA-C2: No Prohibited Industry Check at Onboarding
**Status:** Critical  
**Gap:** No check prevents a weapons dealer, gambling operator, or adult content business from onboarding.  
**Risk:** Meta can suspend ALL WABAs on Conductor's platform if a prohibited business is found.  
**Required implementation:**
- Prohibited industry checklist during onboarding (self-certification)
- Industry verification for high-risk categories
- Account review process for suspicious tenants

### WA-C3: Message Quality Rating Not Monitored
**Status:** Major  
**Gap:** No monitoring of Meta's message quality rating per WABA. Poor quality (Red rating) → WABA restriction.  
**Required implementation:**
- Regular polling of quality rating via Meta Graph API
- Alert tenant when rating drops to Yellow
- Alert engineering when rating drops to Red (risk of suspension)

### WA-C4: Template Compliance Review
**Status:** Major  
**Gap:** No internal review process for templates before submission to Meta.  
**Risk:** Prohibited templates (alcohol, gambling, political) submitted to Meta → template rejection + quality rating impact.  
**Required implementation:**
- Template review checklist in UI before submission
- Internal moderation queue for templates from new tenants

---

## Legal Gaps

### LEGAL-C1: No Terms of Service
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** No Terms of Service document published.  
**Required:** T&Cs covering: service description, acceptable use, payment terms, data processing, liability limitations, termination.  
**Status:** Not drafted

### LEGAL-C2: No Data Processing Agreement Template
**Status:** Critical  
**Gap:** Conductor processes data on behalf of tenants (Data Processor). A DPA is required.  
**Required:** DPA template available for tenants to execute, covering: processing activities, security measures, subprocessors, deletion obligations.  
**Status:** Not drafted — legal counsel required

### LEGAL-C3: GSTIN Registration
**Status:** CRITICAL — Pre-launch blocker  
**Gap:** Conductor must be registered under GST to charge 18% GST on subscriptions.  
**Required:** GSTIN registration with Indian GST authorities before issuing invoices.  
**Status:** Not confirmed

### LEGAL-C4: No Anti-Spam Policy
**Status:** Major  
**Gap:** No documented policy on what constitutes spam/abuse on the Conductor platform.  
**Required:** Acceptable use policy covering message volume, content standards, and consequences for violations.  
**Status:** Not drafted

---

## Pre-Launch Compliance Checklist

All items below MUST be complete before public launch:

**DPDP India:**
- [ ] Consent capture mechanism implemented and tested
- [ ] Data erasure workflow implemented
- [ ] Privacy policy published at conductor.io/privacy
- [ ] Data localization confirmed (AWS ap-south-1)
- [ ] Data breach notification plan documented and rehearsed
- [ ] DPA template drafted and available for tenants

**WhatsApp Business Policy:**
- [ ] STOP handling tested end-to-end (<5 seconds)
- [ ] Prohibited industry check at onboarding
- [ ] Frequency cap (1 marketing message/day) implemented and tested
- [ ] Template compliance review process established
- [ ] Official WhatsApp Cloud API used (no unofficial libraries)

**Legal:**
- [ ] Terms of Service published
- [ ] Privacy Policy published
- [ ] GSTIN registration complete
- [ ] Acceptable use policy documented
- [ ] Legal counsel review of all policies

**Tax:**
- [ ] GST-compliant invoice generation working
- [ ] GST filing process established
- [ ] Accounting software integrated

---

## Compliance Gap Summary

| Gap | Severity | Status |
|---|---|---|
| No consent mechanism | DPDP-C1 | Design documented — not implemented |
| No data erasure | DPDP-C2 | Runbook written — not implemented |
| No privacy notice | DPDP-C3 | Not drafted |
| No breach notification plan | DPDP-C4 | Partially documented |
| Data localization unconfirmed | DPDP-C5 | Recommended — not confirmed |
| STOP handling untested | WA-C1 | Implementation documented — untested |
| No prohibited industry check | WA-C2 | Not implemented |
| Quality rating unmonitored | WA-C3 | Not implemented |
| No template review process | WA-C4 | Not designed |
| No Terms of Service | LEGAL-C1 | Not drafted |
| No DPA template | LEGAL-C2 | Not drafted |
| No GSTIN | LEGAL-C3 | Not confirmed |
| No anti-spam policy | LEGAL-C4 | Not drafted |

---

## Cross-References
- `07-Governance/Compliance.md` — Full compliance requirements
- `07-Governance/Risk-Register.md` — Compliance risks
- `04-Architecture/Security-Architecture.md` — Security controls enabling compliance
