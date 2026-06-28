# Architecture Decision Records — Conductor

**Status:** Partially Extracted + Extended  
**Source:** Technical Layers, Initiative Brief, Gap Analysis  
**Last Updated:** June 2026

---

## ADR Format
Each ADR follows this structure:
- **Status:** Proposed | Accepted | Deprecated | Superseded
- **Context:** Why this decision was needed
- **Decision:** What was decided
- **Rationale:** Why this option was chosen
- **Alternatives Considered:** Other options and why rejected
- **Consequences:** What this decision implies

---

## ADR-001: Workflow Runtime — Temporal over Camunda 8

**Status:** ACCEPTED

**Context:**
Source documents contain conflicting recommendations for the workflow execution runtime:
- Technical Layers document recommends Camunda 8 (workflow engine) + Kafka (event bus)
- Founder/Product Strategic Lens recommends Temporal + NATS for V1

This conflict must be resolved before engineering begins.

**Decision:**
Use **Temporal** (Temporal.io) as the workflow execution runtime for MVP and foreseeable future.

**Rationale:**
- Temporal is purpose-built for durable workflow execution — the exact use case (multi-step, long-running automations)
- Better Java SDK maturity for our Spring Boot stack
- Simpler operational model than Camunda 8 (which requires a separate server platform)
- Lower licensing complexity (Temporal Cloud or self-hosted OSS)
- Strong community and startup adoption

**Alternatives Considered:**
- Camunda 8: Enterprise-grade BPMN engine. Powerful, but heavyweight. BPMN visualization not needed for our DSL model. Licensing costs at scale.
- Apache Airflow: Data pipeline focused, not suited for real-time business automations
- Custom cron-based scheduler: Cannot handle durable long-running workflows

**Consequences:**
- Engineers must learn Temporal SDK (Java)
- Temporal Server must be deployed and operated (additional infrastructure)
- Temporal Cloud available as managed option (cost: ~$0.00025/workflow action)

---

## ADR-002: Event Bus — NATS over Kafka (MVP)

**Status:** ACCEPTED

**Context:**
Technical Layers document recommends Kafka. Kafka is powerful but operationally complex for a seed-stage startup.

**Decision:**
Use **NATS 2.x with JetStream** for MVP. Evaluate Kafka migration at 100+ tenants or 1M+ events/day.

**Rationale:**
- NATS is dramatically simpler to operate (single binary, low resource usage)
- JetStream provides at-least-once delivery and persistence needed for reliability
- NATS is battle-tested at high scale
- Migration path to Kafka is clear when needed (same pub/sub semantics)

**Alternatives Considered:**
- Kafka: Industry standard, highly scalable. But requires ZooKeeper/KRaft, complex consumer groups, high operational burden for a 3-person team.
- RabbitMQ: Good for task queues, but less suited for event streaming patterns
- AWS SQS/SNS: Managed, but ties to AWS and complicates local development

**Consequences:**
- NATS JetStream must be configured for persistence (not in-memory only)
- If Kafka migration is needed later, event consumers need updating
- NATS clustering required for high availability (3-node cluster recommended)

---

## ADR-003: WhatsApp Channel — Official Cloud API Only (No OpenWA)

**Status:** ACCEPTED

**Context:**
Source documents recommend evaluating OpenWA (unofficial WhatsApp web scraping library) for the WhatsApp integration layer.

**Decision:**
Use **WhatsApp Cloud API (Meta's official API)** exclusively. OpenWA and all unofficial libraries are prohibited.

**Rationale:**
- OpenWA violates Meta's Terms of Service
- Meta actively blocks and bans accounts using unofficial automation
- Risk: All tenant WABAs could be permanently suspended
- This is an existential risk to the entire platform

**Alternatives Considered:**
- OpenWA / Baileys / WWebJS: Unofficial. High risk of account suspension. NOT acceptable.
- BSP (Business Solution Provider) like Gupshup/Kaleyra: Higher per-message cost, but adds resiliency. Can be adopted as backup option alongside direct Cloud API.

**Consequences:**
- Each tenant must obtain and connect their own WhatsApp Business Account (WABA)
- WABA approval process takes 1-3 business days (impacts onboarding speed)
- Rate limits apply (1,000 conversations/day Tier 1; upgrades automatically)
- Must use Meta-approved message templates for business-initiated messages

---

## ADR-004: Database — PostgreSQL (Not MongoDB or MySQL)

**Status:** ACCEPTED

**Context:**
Platform requires both structured relational data (tenants, users, billing) and semi-structured data (workflow configurations, event payloads, custom attributes).

**Decision:**
Use **PostgreSQL 15** as the primary data store.

**Rationale:**
- ACID compliance critical for billing and workflow state
- Native JSONB support for semi-structured data (workflow definitions, event payloads, custom attributes)
- Excellent ecosystem, managed service on AWS (RDS), team familiarity
- Full-text search, array types, and advanced indexing all available natively
- Row-level security available for future multi-tenant isolation enhancement

**Alternatives Considered:**
- MySQL: No native JSONB support; text-only JSON field
- MongoDB: Strong for document data but lack of ACID transactions creates risk for billing and workflow state
- DynamoDB: Managed, scalable, but high learning curve and limited query flexibility

**Consequences:**
- Schema migrations required for structure changes (Flyway)
- Horizontal scaling requires read replicas and eventually sharding (well-understood path)
- PostgreSQL expertise required on the team

---

## ADR-005: IAM — Keycloak (Self-Hosted)

**Status:** ACCEPTED

**Context:**
Authentication, SSO, RBAC, and multi-tenant identity management must be implemented. Options range from build-from-scratch to managed services.

**Decision:**
Use **Keycloak** (self-hosted, Apache 2.0 license).

**Rationale:**
- Full-featured: Authentication, authorization, SSO, RBAC, multi-tenancy
- Open-source: No per-user pricing; can evolve freely
- Standard: OpenID Connect and OAuth 2.0 implementation
- Multi-tenant: Each tenant gets its own Keycloak realm for isolation

**Alternatives Considered:**
- Auth0: Excellent managed service, but expensive at scale (per-MAU pricing adds up to $1,000+/month at 10,000 users)
- AWS Cognito: Managed, but limited RBAC flexibility; multi-tenancy requires complex pool management
- Build custom auth: High risk, security-critical; not recommended

**Consequences:**
- Keycloak must be deployed and operated (additional infrastructure overhead)
- Keycloak has steep initial configuration curve
- Consider Keycloak managed hosting (Bitnami AMI on AWS) to reduce ops burden

---

## ADR-006: Frontend — React SPA (Not Next.js)

**Status:** ACCEPTED

**Context:**
Web application for tenant business owners and staff. Considering React SPA vs. Next.js (SSR).

**Decision:**
Use **React 18 SPA** with Vite as build tool.

**Rationale:**
- Conductor is a B2B application dashboard — SEO is not a primary concern
- SPA reduces complexity (no server-side rendering to operate)
- Vite is fast in development (vs. CRA/webpack)
- Platform is authenticated behind login — no public-facing pages requiring SSR

**Alternatives Considered:**
- Next.js: SSR useful for SEO (landing pages). For the authenticated app, SSR adds complexity without benefit. Landing page can be a separate Next.js site.
- Vue/Angular: React has stronger ecosystem fit with the existing library choices

**Consequences:**
- Marketing landing page should be separate (Next.js or static site) for SEO
- Client-side rendering means API response time directly affects user-perceived performance

---

## ADR-007: Analytics — Metabase (Embedded)

**Status:** ACCEPTED

**Context:**
Tenants need analytics dashboards showing message performance, workflow metrics, and customer growth.

**Decision:**
Use **Metabase** embedded in the tenant dashboard for analytics visualization.

**Rationale:**
- Eliminates need to build charts from scratch (saves 4-6 weeks of engineering)
- Rich charting library (line, bar, funnel, table)
- Can be embedded using signed JWTs (tenant-scoped)
- Open-source version available; Pro version required for embedding (~$500/month)

**Alternatives Considered:**
- Custom charts (Chart.js/Recharts): Full control but significant development investment
- Grafana: Excellent for engineering dashboards; less suited for business user UX
- Looker/Tableau: Enterprise cost, not appropriate for SaaS embedding

**Consequences:**
- Metabase Pro license required for embedding (~₹40,000/month)
- Analytics data must be in a PostgreSQL schema that Metabase can query
- Metabase server must be deployed and operated

---

## ADR-008: OSS Assembly Strategy

**Status:** ACCEPTED

**Context:**
Platform requires many capabilities that can be built or bought/assembled from open-source components.

**Decision:**
Use the **OSS Assembly Strategy** — integrate proven open-source components rather than building from scratch.

**Assembly:**
| Component | OSS Tool | License |
|---|---|---|
| Workflow runtime | Temporal | MIT |
| Agent inbox | Chatwoot | MIT |
| Analytics | Metabase | OSS / Pro |
| IAM | Keycloak | Apache 2.0 |
| Event bus | NATS | Apache 2.0 |
| AI workflows | Dify | Apache 2.0 |
| API gateway | Kong | Apache 2.0 |
| Monitoring | Prometheus + Grafana | Apache 2.0 |

**Rationale:**
- Eliminates 60-70% of engineering effort
- Each component is battle-tested and maintained by active communities
- Faster time-to-market: team focuses on business differentiation (workflow DSL, capability packs, connectors)

**Consequences:**
- Team must learn multiple technologies
- Operational burden of self-hosting multiple systems
- Upgrade coordination complexity
- Some components may not integrate perfectly — custom glue code required

---

## ADR Review Process

- New ADRs: Created by Engineering Lead or Architect, reviewed by EDRB (Engineering Decision Review Board) per the [Engineering Governance System](file:///c:/Users/rajaj/Projects/Conductor/docs/standards/Engineering-Governance-System.md).
- Status updates: Subject to triggered review gates and EDRB sign-off.
- Frequency: Reviewed quarterly or when triggered by architectural changes.

---

## EDRB Evaluations

- **[EDRB-2026-06-Conductor-MVP.md](file:///c:/Users/rajaj/Projects/Conductor/docs/standards/EDRB-2026-06-Conductor-MVP.md)**: Go/No-Go evaluation of the core MVP stack decisions (Temporal, NATS JetStream, WhatsApp Cloud API, and self-hosted Keycloak).

---

## Cross-References
- `01-Vision/Strategic-Thesis.md` — Strategic rationale for OSS assembly
- `04-Architecture/Solution-Architecture.md` — Technology decisions implemented
- `docs/standards/Risk-Register.md` — Risks from architecture decisions
- `docs/standards/Engineering-Governance-System.md` — Enterprise Engineering Governance System rules and review gates
