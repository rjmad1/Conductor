# Architecture Overview

## A. Purpose
This page details Conductor's architectural style, system layering, and core design decisions.

## B. Intended Audience
- Software Architects
- Engineering Leads
- Security & Platform Review Board members

---

## C. Architectural Style
Conductor adopts a **Modular Monolith** pattern with an **Event-Driven Asynchronous Processing** core for its MVP.

### Why Modular Monolith?
Startups often face "microservices tax" — disproportionate operational overhead for deployment, inter-service networking, and observability. 
By maintaining a clean separation of packages (modules) in a single Spring Boot application, Conductor keeps deployment simple while allowing individual microservices to be extracted later when scale requires.

```
┌─────────────────────────────────────────────────────────────────────┐
│  LAYER 5: Presentation (React Web App)                              │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 4: API Gateway (Kong: Auth, Rate Limiting, CORS, Gateway)    │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 3: Application Services (Spring Boot Modular Monolith)        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐│
│  │Tenant    │ │Workflow  │ │Customer  │ │Campaign  │ │Analytics ││
│  │Module    │ │Module    │ │Module    │ │Module    │ │Module    ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘│
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 2: Execution Tier (NATS JetStream & Temporal Workflows)      │
├─────────────────────────────────────────────────────────────────────┤
│  LAYER 1: Database Tier (PostgreSQL, Redis, ClickHouse, Qdrant)    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## D. Technology Decision Summary

| Component | Choice | Alternatives | Rationale |
|---|---|---|---|
| **Workflow Engine** | **Temporal** | Camunda 8, custom scheduler | Stateful, durable execution; rich Java SDK; open source. |
| **Event Bus** | **NATS JetStream** | Apache Kafka, RabbitMQ | Lightweight footprint (<50MB RAM); simple single-binary operation; high throughput. |
| **Identity Provider (IAM)** | **Keycloak** | Auth0, AWS Cognito | Open source; strong multi-tenancy configurations; standards-based RBAC. |
| **Primary Database** | **PostgreSQL** | MySQL, MongoDB | ACID transactions; JSONB support; trigger auditing. |
| **OLAP Storage** | **ClickHouse** | Elasticsearch, PostgreSQL | Columnar high-speed queries for logs and billing logs. |
| **Vector Storage** | **Qdrant** | Pinecone, pgvector | High scalability; logical metadata namespace filters. |
| **API Gateway** | **Kong Gateway** | Nginx, Spring Cloud Gateway | Declarative config; OIDC JWT plugins; open source. |
| **Shared Agent Inbox** | **Chatwoot** | Zendesk, Help Scout | Open-source MIT; pre-built visual agent inbox. |

---

## E. Cross-References
- [System Context](System-Context)
- [Service Catalog](Service-Catalog)
- [Component Catalog](Component-Catalog)
- [Decision Records Index](Decision-Records-Index)
