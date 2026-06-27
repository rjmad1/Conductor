# Non-Compliant Findings

- **Loop ID:** LOOP-303
- **Run ID:** LOOP-303-20260627-001
- **Timestamp:** 2026-06-27T13:45:00Z

The following compliance gaps require remediation before production launch.

---

## 1. Critical Gaps (Launch Blockers)

### NC-001: STOP Keyword Opt-Out Handler Missing (WA-C1)
- **Severity:** Critical / High
- **Description:** No active consumer processes "STOP" keyword inbound messages and records customer opt-outs in the database within the Meta-mandated 5-second SLA.
- **Recommended Remediation:** Implement `Task T-302` in the messaging/webhook pipeline.

### NC-002: Missing Privacy Notice (DPDP-C3)
- **Severity:** Critical / High
- **Description:** No privacy notice is drafted or published.
- **Recommended Remediation:** Legal counsel to draft and publish the policy at conductor.io/privacy.

### NC-003: Missing Terms of Service (LEGAL-C1)
- **Severity:** Critical / High
- **Description:** No terms of service are published for tenants.
- **Recommended Remediation:** Legal counsel to draft and publish terms of service.

### NC-004: GSTIN Registration Pending (LEGAL-C3)
- **Severity:** Critical / High
- **Description:** Billing service cannot generate legal invoices without GSTIN.
- **Recommended Remediation:** Confirm GSTIN registration with tax authorities.

---

## 2. Major Gaps (Remediate within 90 days)

### NC-005: Data Erasure (30-day SLA) Not Automated (DPDP-C2)
- **Severity:** Major
- **Description:** Right to erasure is currently manual via Runbook 8; automated Spring Boot anonymization task is missing.
- **Recommended Remediation:** Implement `Task T-204` scheduled worker.

### NC-006: Prohibited Industry Checks Missing in Onboarding (WA-C2)
- **Severity:** Major
- **Description:** No verification checklist blocks prohibited vertical clients from onboarding.
- **Recommended Remediation:** Add self-certification check to signup flow.
