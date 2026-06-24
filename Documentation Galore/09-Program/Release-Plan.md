# Release Plan — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Implementation Plan, Roadmap  
**Last Updated:** June 2026

---

## Release Philosophy

> Ship small, ship often. Get feedback, iterate.

- **Release cadence:** Biweekly (every 2 weeks) for feature releases
- **Hotfix:** Anytime as needed for P0/P1 fixes
- **Major versions:** When significant architecture or breaking changes are introduced

---

## Versioning: Semantic Versioning (SemVer)

```
MAJOR.MINOR.PATCH

Examples:
1.0.0  — MVP launch
1.1.0  — Shopify connector added (new feature, backward compatible)
1.1.1  — Bug fix in STOP handling
2.0.0  — Breaking API change (requires tenant migration)
```

**API versioning:** URL-based (`/api/v1/`, `/api/v2/`)  
**Breaking changes:** Require new API version + 6-month deprecation period

---

## Release Environments

| Environment | Purpose | Deploy Trigger | Audience |
|---|---|---|---|
| `dev` | Developer sandbox | Push to feature branch | Developers only |
| `staging` | QA + integration testing | Merge to `main` | Internal team + beta users |
| `prod` | Production | Manual gate after staging QA | All paying customers |

---

## Release Cadence

### Sprint-Based Release Cycle (2 Weeks)

```
Week 1:
  Mon: Sprint planning — define release scope
  Mon-Thu: Feature development
  Fri: Feature complete → merge to main → auto-deploy to staging

Week 2:
  Mon-Tue: QA testing on staging
  Wed: Bug fixes for QA issues
  Thu: Release candidate cut
  Thu: Pre-release checklist (see below)
  Fri: Production deployment (deploy window: 10am-2pm IST)
  Fri PM: Smoke testing on production
  Fri: Release notes published
```

---

## Pre-Release Checklist

Must be completed before any production deployment:

**Engineering:**
- [ ] All CI tests passing on release candidate
- [ ] No CRITICAL or BLOCKER SonarQube issues
- [ ] Database migrations tested on staging with production-like data
- [ ] Performance test: no regression in API latency p95
- [ ] OWASP ZAP scan on staging: no HIGH or CRITICAL new findings
- [ ] Rollback plan documented (what version to roll back to if issues arise)
- [ ] On-call engineer briefed on what's changing

**Product:**
- [ ] QA sign-off: all P0 and P1 stories tested
- [ ] Release notes written
- [ ] Any feature flags configured correctly

**DevOps:**
- [ ] Monitoring dashboards updated if new metrics added
- [ ] Alert thresholds reviewed for new features
- [ ] Deployment window confirmed (avoid Fridays before long weekends)

---

## Planned Releases (Phase 1)

### v0.1.0 — Developer Preview (Internal)
**Target:** Week 6  
**Scope:** Infrastructure only, WhatsApp sandbox message working  
**Audience:** Engineering team only

### v0.5.0 — Design Partner Alpha
**Target:** Week 10  
**Scope:** Tenant registration, customer import, basic workflow, appointment reminder template  
**Audience:** 5 design partner healthcare clinics (not public)

### v0.8.0 — Private Beta
**Target:** Week 14  
**Scope:** Full MVP feature set, Razorpay + Google Calendar connectors  
**Audience:** 20 invited tenants (healthcare + retail)

### v1.0.0 — Production Launch
**Target:** Week 18  
**Scope:** Complete MVP — all Phase 1 features, production infrastructure  
**Audience:** Public (managed onboarding)  
**Announcement:** ProductHunt, press release, healthcare community posts

### v1.1.0 — Shopify + Zoho Connectors
**Target:** Week 22  
**Scope:** Shopify connector, Zoho CRM connector, abandoned cart recovery pack

### v1.2.0 — Campaign Engine
**Target:** Week 24  
**Scope:** Broadcast campaigns, frequency cap, campaign analytics

### v1.3.0 — Conversation Engine
**Target:** Week 28  
**Scope:** Interactive menus, multi-step flows, Chatwoot agent inbox

---

## Hotfix Process

For P0/P1 bugs that cannot wait for the next regular release:

```
1. Engineer identifies and reproduces bug on staging
2. Engineering Lead approves hotfix (bypassing normal sprint cycle)
3. Hotfix branch created from production tag: hotfix/1.0.1-{description}
4. Fix developed and reviewed (minimum 2 reviewers for hotfix)
5. Deploy to staging → smoke test (30 minutes)
6. Deploy to production (any time, any day for P0)
7. Monitor for 2 hours
8. Tag: v1.0.1
9. Merge hotfix back to main
```

---

## Feature Flags

Used to deploy code before it's ready to enable for all users:

```java
@Service
public class FeatureFlagService {
    
    // Flags in Redis (per tenant or global)
    public boolean isEnabled(String flagName, String tenantId) {
        // Check tenant-specific override first
        // Fall back to global feature flag
        // Fall back to plan-based feature gate
    }
}
```

**Current feature flags:**
| Flag | Default | Description |
|---|---|---|
| `ai_intent_detection` | false | Enable NLU intent detection |
| `campaign_engine` | true | Enable campaign module |
| `advanced_analytics` | false | Enable advanced Metabase dashboards |
| `connector_shopify` | true | Enable Shopify connector |
| `api_access` | false | Enable public API (Business plan+) |

---

## Release Communication

### Internal (Every Release)
- Slack `#releases` channel: summary of what's in this release
- Link to release notes in GitHub

### External (Major Releases / New Features)
- In-app notification to tenants (for features relevant to them)
- Email newsletter (monthly)
- Blog post for v1.0.0 and significant milestones

### Breaking Changes (API)
- 6 months notice before deprecating any API endpoint
- Deprecation header in API responses: `Deprecation: true, Sunset: {date}`
- Migration guide published with every breaking change

---

## Cross-References
- `09-Program/Implementation-Plan.md` — Sprint-level delivery plan
- `05-Engineering/Repositories.md` — Branching strategy
- `06-Operations/Runbooks.md` — Deployment runbook
