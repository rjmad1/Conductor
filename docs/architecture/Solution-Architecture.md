# Solution Architecture — Conductor

**Status:** Partially Extracted + Extended (⚡ where inferred)  
**Source:** Technical Layers document, MVP Scope  
**Last Updated:** June 2026

---

## Architecture Style

Conductor uses a **modular monolith with event-driven async processing** for MVP. This is not a microservices architecture — services are logically separated but deployed as a small number of processes initially, with the ability to split into independent microservices as scale demands.

**Rationale:** Microservices require disproportionate infrastructure complexity for a seed-stage startup. The event-driven core (NATS) allows service decoupling without the deployment overhead of full microservices. Each module can be extracted to its own service when scale justifies it.

---

## Platform Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│  LAYER 5: Presentation                                               │
│  React Web App (SPA) · Mobile (Phase 3)                            │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 4: API Gateway                                               │
│  Kong Gateway: Auth, Rate Limiting, Routing, Request Logging        │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 3: Application Services (Spring Boot)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐│
│  │Tenant    │ │Workflow  │ │Customer  │ │Campaign  │ │Analytics ││
│  │Service   │ │Service   │ │Service   │ │Service   │ │Service   ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘│
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │WhatsApp  │ │Template  │ │Connector │ │Billing   │            │
│  │Adapter   │ │Service   │ │Service   │ │Service   │            │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 2: Event & Workflow Execution                                 │
│  NATS Event Bus · Temporal Workflow Server + Workers                │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 1: Data                                                       │
│  PostgreSQL (primary store) · Redis (cache + session + state)       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Component Inventory

### Frontend
| Component | Technology | Responsibility |
|---|---|---|
| Conductor Web App | React 18, TypeScript, TailwindCSS | All tenant-facing UI: dashboard, workflow designer, contacts, analytics, settings |
| Workflow Designer | React Flow | Visual drag-and-drop workflow builder (canvas-based) |

### API Gateway
| Component | Technology | Responsibility |
|---|---|---|
| API Gateway | Kong (open-source) | JWT validation, rate limiting, request routing, CORS, SSL termination |

### Application Services (Spring Boot 3, Java 21)
| Service | Port | Responsibility |
|---|---|---|
| `tenant-service` | 8080 | Tenant CRUD, plan management, feature flags |
| `auth-service` | 8081 | Delegates to Keycloak; API key management |
| `customer-service` | 8082 | Customer registry, segments, consent tracking |
| `workflow-service` | 8083 | Workflow CRUD, activation, execution logging |
| `conversation-service` | 8084 | Conversation state machine, inbound message handling |
| `campaign-service` | 8085 | Campaign creation, scheduling, execution |
| `template-service` | 8086 | Template CRUD, Meta submission, approval tracking |
| `connector-service` | 8087 | Connector authentication, webhook normalization |
| `whatsapp-adapter` | 8088 | WhatsApp Cloud API client, inbound webhook, send/receive |
| `analytics-service` | 8089 | Metrics aggregation, dashboard data |
| `billing-service` | 8090 | Subscription management, usage tracking, invoice generation |
| `notification-service` | 8091 | Internal platform notifications (email, in-app alerts) |

### Event & Workflow Layer
| Component | Technology | Responsibility |
|---|---|---|
| Event Bus | NATS 2.x | Publish/subscribe messaging backbone; all inter-service events |
| Workflow Runtime | Temporal | Durable workflow execution; long-running and scheduled workflows |
| Temporal Workers | Spring Boot + Temporal Java SDK | Execute workflow activities; deployed as separate worker processes |

### Data Layer
| Component | Technology | Responsibility |
|---|---|---|
| Primary Database | PostgreSQL 15 | All persistent business data (tenants, customers, workflows, conversations, events) |
| Cache / Session | Redis 7 | Session state, conversation context, rate limiting counters, feature flags cache |
| Object Storage | AWS S3 / GCS | Media files (images, PDFs sent in WhatsApp messages), template assets |
| Search (Phase 2) | OpenSearch | Customer search, conversation search |

### OSS Platform Components (Pre-integrated)
| Component | OSS Project | License | Responsibility |
|---|---|---|---|
| IAM | Keycloak | Apache 2.0 | Authentication, SSO, RBAC, multi-tenant identity |
| Agent Inbox | Chatwoot | MIT | Human agent conversation inbox |
| Analytics | Metabase | Proprietary (free tier) | Embedded analytics dashboards |
| API Gateway | Kong | Apache 2.0 | API management |
| AI Workflows | Dify | Open-source | LLM workflow execution (Phase 2) |
| Monitoring | Prometheus + Grafana | Apache 2.0 | Metrics and dashboards |

---

## Service Communication Patterns

### Synchronous (REST)
Used for: User-initiated actions, CRUD operations, queries
- Frontend → Kong → Services: REST over HTTPS
- Service-to-service (when immediate response required): REST over internal network

### Asynchronous (Event Bus)
Used for: Workflow triggers, notification delivery, cross-service state changes
- Publisher: Any service posts event to NATS subject `conductor.{tenant_id}.{event_type}`
- Consumer: Workflow Engine subscribes to relevant event subjects and triggers workflows

### Event Subjects (NATS)
```
conductor.{tenantId}.customer.created
conductor.{tenantId}.customer.updated
conductor.{tenantId}.order.created
conductor.{tenantId}.cart.abandoned
conductor.{tenantId}.payment.completed
conductor.{tenantId}.appointment.created
conductor.{tenantId}.message.inbound
conductor.{tenantId}.message.sent
conductor.{tenantId}.message.delivered
conductor.{tenantId}.workflow.triggered
conductor.{tenantId}.workflow.completed
```

---

## Multi-Tenancy Implementation

**Tenant isolation is enforced at two levels:**

1. **Application Level:** Every service validates `tenant_id` from the JWT claim. Queries always include `WHERE tenant_id = ?`
2. **Database Level:** All tables include a `tenant_id` column. Future migration path: schema-per-tenant or database-per-tenant for Enterprise tier

**Tenant context propagation:**
- JWT issued by Keycloak contains `tenant_id` claim
- Kong extracts and forwards `X-Tenant-ID` header to downstream services
- Services use `tenant_id` for all data operations

---

## Deployment Architecture (MVP)

**MVP Target:** 2-AZ deployment on AWS/GCP with managed databases

```
Internet
    ↓
CloudFlare (DNS + DDoS protection)
    ↓
Load Balancer (AWS ALB)
    ↓
Kong API Gateway (2 instances, auto-scaling)
    ↓
┌───────────────────────────────────────────────────────┐
│  Application Tier (Kubernetes / ECS — TBD)            │
│  Services: tenant, customer, workflow, wa-adapter ... │
│  Temporal Workers                                      │
└───────────────────────────────────────────────────────┘
    ↓
┌───────────────────────────────────────────────────────┐
│  Data Tier                                            │
│  PostgreSQL (RDS — Multi-AZ)                         │
│  Redis (ElastiCache)                                  │
│  S3 (object storage)                                  │
└───────────────────────────────────────────────────────┘
```

---

## Technology Decision Summary

| Decision | Choice | Alternatives Considered | Rationale |
|---|---|---|---|
| Workflow Engine | Temporal | Camunda 8, custom cron | Durable execution, Java SDK, open-source |
| Event Bus | NATS | Kafka, RabbitMQ | Simpler ops, lighter weight for MVP |
| API Gateway | Kong | Nginx, AWS API Gateway | Rich plugin ecosystem, open-source |
| Database | PostgreSQL | MySQL, MongoDB | ACID compliance, JSON support, strong ecosystem |
| IAM | Keycloak | Auth0, AWS Cognito | Open-source, multi-tenant, full RBAC |
| Frontend | React | Vue, Next.js | Ecosystem, team familiarity |
| Backend | Spring Boot / Java | Node.js, Go | Enterprise patterns, Temporal Java SDK maturity |
| WhatsApp | Cloud API (Meta) | OpenWA, Gupshup | Official API, no ToS risk |

---

## Cross-References
- `04-Architecture/System-Context.md` — External actors and boundaries
- `04-Architecture/Application-Architecture.md` — Service-level detail
- `04-Architecture/Infrastructure-Architecture.md` — Infrastructure and deployment
- `04-Architecture/Data-Architecture.md` — Database design and schemas
