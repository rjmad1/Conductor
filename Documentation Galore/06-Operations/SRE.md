# Site Reliability Engineering — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## SRE Philosophy

> "Reliability is the most important feature." — SRE Golden Rule

For Conductor, reliability directly impacts customer revenue. A 5-minute outage during business hours means appointment reminders not sent, carts not recovered, payments not followed up. Every hour of downtime costs tenants money and erodes trust.

---

## Service Level Objectives (SLOs)

### MVP (V1) SLOs

| Service | Availability SLO | Latency SLO (p95) | Error Rate SLO |
|---|---|---|---|
| API Gateway (Kong) | 99.5% | < 100ms | < 0.5% |
| Business APIs | 99.5% | < 300ms | < 1% |
| WhatsApp Adapter (sending) | 99.5% | < 5s end-to-end | < 2% |
| WhatsApp Webhook processing | 99.9% | < 5s | < 0.1% (must return 200) |
| Workflow Engine | 99.0% | < 30s completion | < 1% |
| Database (RDS) | 99.9% | < 50ms query p95 | - |

### Phase 2 SLOs (after scale)

| Service | Availability SLO |
|---|---|
| All APIs | 99.9% |
| WhatsApp end-to-end | 99.9% |

**Error Budget:**
- 99.5% SLO → 3.65 hours downtime budget per month
- 99.9% SLO → 43.8 minutes downtime budget per month

---

## Capacity Planning

### Current Capacity (MVP, 0-100 tenants)

| Resource | Current Limit | Bottleneck? |
|---|---|---|
| API requests/sec | 500 rps | Kong (2 instances) |
| Messages/sec | 80 msg/sec | WhatsApp API limit |
| Concurrent workflows | 500 | Temporal workers |
| DB connections | 200 | PgBouncer pool |
| Redis memory | 2GB | ElastiCache instance |

### Scale Triggers

| Metric | Action |
|---|---|
| API CPU > 70% sustained 10min | Add Kong or service instances |
| DB CPU > 60% | Add read replica or upgrade instance |
| Message rate approaching 80/sec | Register additional WhatsApp numbers |
| Temporal worker queue > 1000 | Add Temporal workers |
| Redis memory > 70% | Increase Redis instance size |

### Message Volume Capacity (WhatsApp)

WhatsApp Cloud API limits per phone number:
- Tier 1 WABA: 1,000 business-initiated conversations/day
- Tier 2 WABA: 10,000/day (automatically upgrade with usage)
- Tier 3 WABA: 100,000/day
- Unlimited Tier: With Meta partner status

**Strategy:** Register additional WhatsApp numbers when approaching limits. Each number can send 1,000 conversations/day (Tier 1 start).

---

## Reliability Patterns Implemented

### 1. Circuit Breaker
Implemented for WhatsApp API calls (Resilience4j):
- CLOSED → open if 50% failure rate in 30 seconds
- HALF-OPEN after 60 seconds
- Sends are paused during OPEN state; queued in NATS

```java
@CircuitBreaker(name = "whatsapp-api", fallbackMethod = "queueMessageForRetry")
public void sendMessage(String to, String content) {
    metaApiClient.send(to, content);
}
```

### 2. Retry with Exponential Backoff
Temporal workflow retries:
- Max attempts: 3
- Initial interval: 10 seconds
- Max interval: 120 seconds
- Backoff coefficient: 2.0

### 3. Idempotency
- All message sends include an `idempotency_key` (derived from execution_id + action index)
- WhatsApp API is not idempotent — Conductor deduplicates at the `messages` table level
- Campaign sends tracked per customer to prevent duplicate sends on retry

### 4. Graceful Degradation
If a non-critical service fails:
- Analytics service down → metrics collection paused, all other operations continue
- Notification service down → in-app alerts not sent, other operations continue
- Metabase down → dashboards unavailable, core platform continues

### 5. Rate Limiting
- Per-tenant API rate limiting via Kong
- WhatsApp send rate: 80 messages/second per number (Redis token bucket)
- Campaign batch sends: paced at 30 messages/second to stay within Meta limits

---

## On-Call Rotation (MVP)

**MVP (initial team):** Engineering founders on rotation  
**Tooling:** PagerDuty (Phase 2) / WhatsApp group + email (Phase 0 MVP)

**On-call responsibilities:**
- Respond to P0 alerts within 15 minutes
- Resolve or escalate P0 incidents within 1 hour
- Write post-mortem for all P0 and P1 incidents
- Hand off open incidents to next on-call with written summary

**Escalation path:**
```
On-Call Engineer
    ↓ (15 min no response)
Engineering Lead
    ↓ (30 min, P0 only)
CTO / Founder
```

---

## Change Management

### Deployment Windows
- **Standard deploys:** Monday–Thursday, 10 AM – 4 PM IST (business hours, low risk window)
- **Avoid:** Fridays, public holidays, major Indian shopping events (Diwali, festive sales)
- **Emergency hotfixes:** Anytime with on-call + Engineering Lead approval

### Change Risk Classification

| Risk Level | Example | Approval Required |
|---|---|---|
| Low | UI text change, minor bug fix | 1 reviewer |
| Medium | New feature, API addition | 1 reviewer + team lead |
| High | Schema migration, dependency upgrade | 2 reviewers + QA sign-off |
| Critical | Auth system change, data migration | Engineering Lead + CTO |

---

## Disaster Recovery

### Recovery Time Objective (RTO) and Recovery Point Objective (RPO)

| Scenario | RTO | RPO |
|---|---|---|
| Single service crash | < 2 minutes (ECS auto-restart) | 0 (stateless) |
| Database failover (Multi-AZ) | < 2 minutes (automatic) | < 1 minute |
| AZ failure | < 5 minutes | < 5 minutes |
| Full region failure | < 4 hours (manual DR activation) | < 1 hour |
| Accidental data deletion | < 4 hours | Up to 24 hours (last snapshot) |

### Backup Schedule

| Resource | Backup Frequency | Retention | Location |
|---|---|---|---|
| RDS PostgreSQL | Automated daily snapshot + 5min transaction logs | 7 days | AWS S3 (same region) |
| S3 media bucket | Cross-region replication | Indefinite | Secondary region |
| Redis | AOF persistence enabled | - | EBS volume |
| NATS | JetStream file storage | 7 days | EBS volume |

### DR Procedure (Region Failure)

```
1. Declare disaster with Engineering Lead + CTO
2. Restore latest RDS snapshot to secondary region (ap-south-2)
3. Update DNS (CloudFlare) to point to secondary region load balancer
4. Verify all services healthy in secondary region
5. Update Meta webhook URL to secondary region endpoint
6. Notify tenants of maintenance and data recovery status
7. Estimate RPO and inform affected tenants
```

---

## Post-Mortem Process

Required for: All P0 incidents, P1 incidents with > 30 min impact

**Post-mortem template:**
```markdown
## Incident Summary
Date, duration, impact (tenants affected, messages lost)

## Timeline
- HH:MM — Alert fired
- HH:MM — On-call acknowledged
- HH:MM — Root cause identified
- HH:MM — Mitigation applied
- HH:MM — Incident resolved

## Root Cause
What specifically failed and why

## Impact
- Tenants affected: N
- Messages not delivered: N
- Revenue impact: ₹ estimated

## Contributing Factors
What made this incident possible or worse

## Action Items
| Action | Owner | Due Date |
| Fix the bug | @engineer | 2026-07-01 |
| Add alert | @devops | 2026-07-05 |

## Lessons Learned
What we learned and what changes we're making
```

**Blameless culture:** Post-mortems focus on systems and processes, not individuals.

---

## Cross-References
- `06-Operations/Monitoring.md` — Alert thresholds and dashboards
- `06-Operations/Incident-Management.md` — Incident response process
- `06-Operations/Runbooks.md` — Operational procedures
