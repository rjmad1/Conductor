# Implementation Plan — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** MVP Scope, Technical Layers  
**Last Updated:** June 2026

---

## Team Assumption (MVP)
- 3-4 engineers (1-2 senior fullstack + 1 backend + 1 DevOps/infra)
- 1 product/design lead
- Founders handle business, sales, customer success

---

## Phase 0: Foundation (Weeks 1-6)
**Goal:** Working infrastructure, can send a WhatsApp message end-to-end.

### Week 1-2: Infrastructure Setup
| Task | Owner | Notes |
|---|---|---|
| AWS/GCP account setup, VPC, IAM roles | DevOps | Choose cloud provider first |
| Domain registration: conductor.io | Founder | Or use existing domain |
| GitHub organization + monorepo setup | Engineering Lead | `conductor-platform` repo |
| CI/CD pipeline skeleton (GitHub Actions) | Engineering Lead | Build → Test → Deploy |
| Keycloak deployment (staging) | DevOps | Docker/ECS |
| PostgreSQL RDS (staging) | DevOps | t3.medium, Multi-AZ off in staging |
| Redis ElastiCache (staging) | DevOps | |
| NATS deployment (staging) | DevOps | JetStream enabled |

### Week 3-4: Core Services Skeleton
| Task | Owner | Notes |
|---|---|---|
| Monorepo structure per Repositories.md | Engineering Lead | |
| Database schema migrations (V001-V010) | Backend | Flyway setup |
| tenant-service: CRUD APIs | Backend | |
| auth-service + Keycloak integration | Backend | JWT validation middleware |
| Kong API Gateway setup (routing + JWT) | DevOps | |
| Spring Boot service template | Backend | Shared config, logging, health |
| React app skeleton + auth flow | Frontend | Login, basic layout |

### Week 5-6: WhatsApp Integration
| Task | Owner | Notes |
|---|---|---|
| WhatsApp Cloud API integration | Backend | Send template message |
| Inbound webhook endpoint + HMAC validation | Backend | |
| Webhook verification (Meta challenge) | Backend | |
| Send test message end-to-end | All | Gate milestone |
| NATS event bus integration (first event) | Backend | Publish `message.sent` event |
| Temporal server deployment (staging) | DevOps | |

**Phase 0 Gate:** Send a WhatsApp message from platform, receive it on a test phone.

---

## Phase 1: MVP Core (Weeks 7-18)
**Goal:** 10 paying healthcare customers using the platform.

### Week 7-9: Customer Registry + Tenant Onboarding
| Task | Owner | Notes |
|---|---|---|
| customer-service: CRUD + bulk import | Backend | CSV import endpoint |
| Consent management (opt-in/out, STOP) | Backend | P0 compliance requirement |
| Tenant onboarding flow (frontend) | Frontend | Industry selection + WA connection |
| WhatsApp number connection (WABA setup) | Backend + Frontend | 5-step guided flow |
| User invitation + role assignment | Backend + Frontend | |
| Basic dashboard (welcome, navigation) | Frontend | |

### Week 10-12: Workflow Engine
| Task | Owner | Notes |
|---|---|---|
| Workflow DSL parser + validator | Backend | Parse JSON trigger/conditions/actions |
| Temporal workflow implementation | Backend | AutomationWorkflow + Activities |
| Event → workflow trigger wiring (NATS) | Backend | Subscribe to events, trigger workflows |
| Condition evaluator | Backend | All operators per requirements |
| Actions: send_whatsapp_template, delay, branch, update_customer | Backend | |
| Workflow visual designer (frontend) | Frontend | React Flow canvas |
| Workflow execution logging | Backend | |

### Week 13-15: Connectors + Templates
| Task | Owner | Notes |
|---|---|---|
| Google Calendar connector | Backend | OAuth + webhook events |
| Razorpay connector | Backend | Webhook + generate_payment_link action |
| template-service: CRUD + Meta submission | Backend | |
| Template approval tracking | Backend | Webhook from Meta |
| Template variable binding in workflow | Backend | |
| Connector UI (connect/disconnect) | Frontend | |
| Templates UI (create, status, list) | Frontend | |

### Week 16-17: Campaign Engine + Analytics
| Task | Owner | Notes |
|---|---|---|
| Campaign creation + scheduling | Backend | |
| Broadcast campaign execution (batch) | Backend | Rate-limited send queue |
| Frequency cap enforcement | Backend | Redis counter |
| Metabase setup + embed | DevOps + Backend | Analytics tables, dashboard config |
| Analytics dashboard (frontend) | Frontend | Embed Metabase |
| Message delivery tracking (sent/delivered/read/failed) | Backend | |

### Week 18: Production Readiness
| Task | Owner | Notes |
|---|---|---|
| Production infrastructure setup | DevOps | Multi-AZ RDS, ECS cluster |
| SSL certificates | DevOps | |
| Monitoring + alerting (Prometheus + Grafana) | DevOps | Key alerts from Monitoring.md |
| Security review: OWASP scan + manual test | Engineering Lead | |
| DPDP compliance audit | Engineering + Legal | Checklist in Compliance.md |
| Load testing (100 concurrent tenants) | Engineering | |
| Runbooks written and tested | All | |

**Phase 1 Gate:** 10 paying healthcare tenants, NPS > 40, no P0 incidents in first 2 weeks.

---

## Phase 2: Growth (Weeks 19-42)
**Goal:** 100 paying customers, MRR ₹5L, multi-vertical.

### Weeks 19-24: Conversation Engine + Agent Inbox
| Task | Owner |
|---|---|
| Multi-step conversation state machine | Backend |
| Interactive button/list message support | Backend |
| Chatwoot integration (agent inbox) | Backend |
| Agent handoff workflow | Backend |
| Shopify connector | Backend |
| Zoho CRM connector | Backend |

### Weeks 25-30: AI Foundation
| Task | Owner |
|---|---|
| Dify deployment | DevOps |
| Intent classifier integration | Backend + AI |
| FAQ bot with RAG (pgvector) | Backend + AI |
| Knowledge base upload UI | Frontend |
| Sentiment analysis (background) | Backend + AI |

### Weeks 31-36: Platform Expansion
| Task | Owner |
|---|---|
| Drip campaign support | Backend |
| Webhook trigger in workflows | Backend |
| REST API (public, v1) | Backend |
| Connector SDK (v0.1) | Backend |
| Advanced customer segmentation | Backend |

### Weeks 37-42: Scale + Ecosystem
| Task | Owner |
|---|---|
| Kafka migration (if NATS is bottleneck) | Backend |
| Connector marketplace (basic) | Backend + Frontend |
| Annual billing support | Backend |
| Agency/reseller accounts | Backend + Frontend |
| SMS channel (Twilio/Exotel) | Backend |

---

## Dependencies and Critical Path

```
Phase 0 (Infra) → Phase 1 Week 7 (Customer Registry) → Week 10 (Workflow Engine) → Week 13 (Connectors) → Week 18 (Production)
```

**Critical path blockers:**
- WhatsApp WABA approval (Meta): Submit in Week 1, takes 1-3 days. Non-blocking for sandbox.
- Meta App review (for public apps): Submit in Week 16.
- GSTIN registration: Week 1.
- Legal: Privacy policy + ToS: Week 16.

---

## Key Milestones

| Milestone | Target Week | Success Criteria |
|---|---|---|
| Can send WhatsApp message (sandbox) | Week 6 | End-to-end test passes |
| First design partner onboarded | Week 8 | Real tenant using sandbox |
| First automated message to real customer | Week 12 | Workflow execution logged |
| Production launch | Week 18 | 0 P0 issues in first 48h |
| First paying customer | Week 19 | Credit card charged |
| 10 paying customers | Week 22 | Phase 1 gate |
| 50 paying customers | Week 30 | Expansion proof |
| 100 paying customers | Week 42 | Phase 2 gate |

---

## Cross-References
- `09-Program/Release-Plan.md` — Release versioning and cadence
- `09-Program/Resource-Plan.md` — Team and hiring plan
- `03-Product/Roadmap.md` — Feature roadmap aligned to implementation
