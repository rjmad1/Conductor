# Incident Management — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Incident Severity Classification

| Severity | Definition | Customer Impact | Response SLA |
|---|---|---|---|
| **P0 — Critical** | Platform down or core function broken for multiple tenants | Many tenants cannot send messages or use the platform | 15 min acknowledgment, 1 hour resolution target |
| **P1 — High** | Significant degradation for 1+ tenants | Messages failing, workflows broken, data loss | 30 min acknowledgment, 4 hour resolution target |
| **P2 — Medium** | Partial degradation, workaround exists | Reduced performance, some features unavailable | Next business day |
| **P3 — Low** | Minor issue, cosmetic, or single user affected | Minimal impact | Next sprint |

---

## Incident Response Process

### Step 1: Detection
- **Automated:** Prometheus alert fires → Grafana alert → Slack `#alerts` channel
- **Manual:** Customer reports via support ticket, email, WhatsApp to support line
- **Internal:** Engineer discovers during development or monitoring

### Step 2: Triage
The on-call engineer who receives the alert must within 15 minutes:
1. Assess severity (P0/P1/P2/P3)
2. Acknowledge the alert
3. Post initial notice in `#incidents` Slack channel:
   ```
   🚨 [INCIDENT-001] P{X} — {Brief description}
   Status: Investigating
   Impact: {What is broken, estimated tenants affected}
   Owner: @{your name}
   ```

### Step 3: Investigate
- Use Grafana dashboards to identify scope
- Check Sentry for error details
- Check application logs for root cause
- Check status pages of dependencies (Meta, Razorpay, Google)
- Reference relevant runbooks in `06-Operations/Runbooks.md`

### Step 4: Communicate
- **P0/P1:** Update `#incidents` channel every 15 minutes
- **P0:** Notify Engineering Lead and CTO immediately
- **Customer notification:** For P0 lasting > 30 minutes or P1 lasting > 2 hours
  - Post on Status Page
  - In-app banner if platform is reachable
  - Direct email to affected tenants if data loss is possible

### Step 5: Resolve
- Apply fix or workaround
- Verify resolution by confirming metrics return to baseline
- Post all-clear in `#incidents`:
  ```
  ✅ [INCIDENT-001] RESOLVED
  Duration: {X minutes}
  Root cause: {1 sentence}
  Next steps: Post-mortem within 24h
  ```

### Step 6: Post-Mortem
- Required for all P0 and P1 incidents
- Due within 24 hours for P0, 72 hours for P1
- Follow post-mortem template in `06-Operations/SRE.md`
- Share with all engineers in `#incidents` Slack channel

---

## Incident Communication Templates

### Customer Status Page Update (P0 Active)
```
[ACTIVE INCIDENT] We are currently experiencing issues with {WhatsApp message delivery / 
platform access / workflow execution}. Our team is actively investigating. 

Affected: All tenants / Tenants using {specific feature}
Started: {HH:MM IST}
Status: Investigating

Next update: {HH:MM IST}
```

### Customer Status Page Update (Resolved)
```
[RESOLVED] The issue with {description} has been resolved as of {HH:MM IST}.

Duration: {X hours Y minutes}
Impact: {What was affected}
Root cause: {1 sentence, non-technical}

We apologize for the disruption and are implementing measures to prevent recurrence.
```

### Tenant Email (Data Loss or Extended Outage > 4h)
```
Subject: Conductor Service Disruption — {Date}

Dear {Tenant Name},

We experienced a service disruption affecting your account between {start} and {end} IST.

What was affected: {specific description}
Duration: {X hours Y minutes}

Impact on your account:
- {X} messages that should have been delivered were not sent
- {Y} workflow executions were delayed by Z minutes

What we're doing: {brief explanation of fix and prevention}

We sincerely apologize for this disruption to your business operations. As a goodwill gesture, we are adding {X} additional messages to your account this month.

If you have questions, reply to this email or contact us at support@conductor.io.

— Conductor Engineering Team
```

---

## Common Incidents and Runbooks

| Incident Type | Runbook |
|---|---|
| High message failure rate | Runbooks.md → Runbook 5 |
| Meta WhatsApp platform down | Runbooks.md → Runbook 6 |
| Database unavailable | Runbooks.md → Runbook 7 |
| Production deployment failure | Runbooks.md → Runbook 3 |
| Tenant data issue (wrong data sent) | Engineering Lead + CTO immediately |
| WABA suspended | Engineering Lead + CTO + Customer Success immediately |
| Security incident | Security-Architecture.md → Incident Response section |

---

## Incident Tracking

All incidents are tracked as GitHub Issues with the label `incident`:
- Title: `[INCIDENT-{number}] {Brief description}`
- Body: Incident timeline, root cause, resolution
- Labels: `incident`, `p0`/`p1`/`p2`, affected service
- Linked PR: Fix PR referenced in issue

Post-mortem documents stored in: `conductor-docs/06-Operations/post-mortems/YYYY-MM-DD-incident-description.md`

---

## Incident Metrics to Track

| Metric | Target |
|---|---|
| Mean Time to Detection (MTTD) | < 5 minutes |
| Mean Time to Acknowledge (MTTA) | < 15 minutes |
| Mean Time to Resolution (MTTR) — P0 | < 1 hour |
| MTTR — P1 | < 4 hours |
| P0 incidents per month | < 1 |
| Post-mortem completion rate | 100% for P0, > 80% for P1 |

---

## Cross-References
- `06-Operations/Runbooks.md` — Step-by-step resolution procedures
- `06-Operations/Monitoring.md` — Alert definitions
- `06-Operations/SRE.md` — SLOs, on-call rotation, post-mortem process
