# Resource Plan — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Team Philosophy

> Hire for trust and range. Small teams move faster. Every hire must earn their place in the burn rate.

Conductor is building a platform product. The biggest risk is hiring too early (burning cash on people before achieving product-market fit) or too late (founder engineering can't keep up with customer demand).

---

## Phase 0-1: Founding Team (Months 1-6)

**Target headcount:** 4-5 people

| Role | Count | Responsibility | Hire type |
|---|---|---|---|
| Founder/CEO | 1 | Product vision, sales, fundraising | Founder |
| Founder/CTO | 1 | Architecture, engineering leadership, early coding | Founder |
| Senior Full-Stack Engineer | 1 | Frontend + backend (React + Spring Boot) | Hire Month 1 |
| Backend Engineer | 1 | Spring Boot services, connectors, Temporal | Hire Month 1-2 |
| DevOps/SRE | 0.5 | Infrastructure, CI/CD, monitoring (part-time or contract) | Contract Month 1 |

**Total: 4 people (2 founders + 2 hires) + 1 contract**

### Hiring Criteria (Phase 1)
- Engineers: 4+ years experience, Spring Boot + React or fullstack
- Strong preference for engineers who have built SaaS products before
- Comfort with ambiguity and small-team operating model
- India-based preferred (timezone alignment + cost efficiency)

---

## Phase 2: Growth Team (Months 7-18)

**Target headcount:** 8-10 people  
**Trigger:** 50 paying customers, MRR ₹3L+, product-market fit validated

| Role | Count | When to Hire | Rationale |
|---|---|---|---|
| Product Manager | 1 | Month 7 | CTO needs to focus on engineering, not product ops |
| Full-Stack Engineer #2 | 1 | Month 8 | Frontend workload increasing |
| Backend Engineer #2 | 1 | Month 9 | Scale: more services, more connectors |
| Customer Success Manager | 1 | Month 8 | Onboarding and retention of growing customer base |
| Sales/BD | 1 | Month 10 | Founder can't do sales + engineering sustainably |
| DevOps/SRE (full-time) | 1 | Month 10 | Infrastructure complexity increases |
| AI/ML Engineer | 1 | Month 12 | Phase 2 AI features (intent classification, FAQ bot) |
| QA Engineer | 1 | Month 14 | Product quality at scale |

**Total by Month 18:** ~10 people

---

## Phase 3: Scale Team (Months 19-36)

**Target headcount:** 20-25 people  
**Trigger:** 200+ customers, MRR ₹10L+, Series A funding

New hires at scale:
- Marketing Lead (content + paid + community)
- Business Development (partnerships)
- 2-3 more Backend Engineers
- Frontend Lead
- Data Analyst
- Finance/Ops
- HR/People (when team > 15)

---

## Compensation Benchmarks (India, 2026) ⚡

| Role | Level | CTC Range (LPA) |
|---|---|---|
| Full-Stack Engineer | Senior (5-8 years) | ₹25-40L |
| Backend Engineer | Senior (5-8 years) | ₹25-40L |
| Backend Engineer | Mid (3-5 years) | ₹15-25L |
| DevOps/SRE | Senior | ₹25-35L |
| Product Manager | Mid-Senior | ₹20-35L |
| AI/ML Engineer | Senior | ₹30-50L |
| Customer Success | Entry-Mid | ₹6-12L |
| Sales/BD | Mid | ₹10-20L + commission |

*Equity offered at all levels — ESOP pool of 15% recommended*

---

## Monthly Burn Rate Projection ⚡

### Phase 1 (Month 1-6)
| Cost Category | Monthly Budget |
|---|---|
| Salaries (2 hires + contractor) | ₹3,00,000 |
| Infrastructure (AWS) | ₹60,000 (~$700) |
| Software licenses (Metabase, tools) | ₹40,000 |
| GTM activities | ₹1,25,000 |
| Legal + compliance | ₹50,000 |
| Office / co-working | ₹30,000 |
| Misc / contingency | ₹45,000 |
| **Total burn** | **₹6,50,000/month** |

### Revenue by Month 6 (target)
- 30 paying customers × avg ₹5,000/month = ₹1,50,000 MRR
- Net burn: ₹6,50,000 - ₹1,50,000 = **₹5,00,000/month**

### Funding Required (Phase 1)
- 12-month runway at ₹5,00,000 net burn = **₹60,00,000 (~₹60L)**
- Recommended raise: ₹1-1.5Cr seed to cover Phase 1 + Phase 2 start

---

## Skills Gap Assessment

| Skill | Current Status | Risk | Mitigation |
|---|---|---|---|
| Java / Spring Boot | Founders + hires | LOW | Core skill, can hire |
| Temporal workflow engine | Learning required | MEDIUM | Temporal University (free), good docs |
| Keycloak IAM | Learning required | MEDIUM | Bitnami AMI + Keycloak docs |
| WhatsApp Cloud API | Learning required | LOW | Good Meta documentation |
| React / TypeScript | Hire for | LOW | Common skill, hireable |
| DevOps / Kubernetes | Contract initially | MEDIUM | Hire FT by Month 10 |
| AI/ML (RAG, LLMs) | Not in founding team | HIGH | Hire Month 12, use Dify as abstraction |
| Product Management | Founder handles initially | MEDIUM | Hire Month 7 |

---

## Contractor Roles (MVP Phase)

Some functions can be contracted rather than hired full-time:

| Function | Contract | When |
|---|---|---|
| Legal (privacy policy, DPA, T&Cs) | Law firm / freelance lawyer | Month 1-2 |
| Design (UI/UX) | Freelance designer | Month 2-4 (for launch) |
| DevOps | Contract SRE / consultant | Month 1-4 (transition to hire Month 5) |
| Content / copywriting | Freelancer | Ongoing |
| Accountant (GST, payroll) | CA firm | Month 1 onwards |

---

## Hiring Plan Timeline

| Month | Hire | CTC Estimate |
|---|---|---|
| Month 1 | Senior Full-Stack Engineer | ₹30L |
| Month 2 | Backend Engineer | ₹25L |
| Month 7 | Product Manager | ₹25L |
| Month 8 | Customer Success Manager | ₹10L |
| Month 8 | Full-Stack Engineer #2 | ₹28L |
| Month 10 | Backend Engineer #2 | ₹22L |
| Month 10 | DevOps/SRE (FT) | ₹28L |
| Month 12 | AI/ML Engineer | ₹40L |
| Month 14 | QA Engineer | ₹18L |

---

## Cross-References
- `09-Program/Implementation-Plan.md` — Sprint tasks mapped to team members
- `02-Business/Go-To-Market.md` — GTM budget that affects resource needs
- `10-Gap-Analysis/Business-Gaps.md` — Team gaps identified in review
