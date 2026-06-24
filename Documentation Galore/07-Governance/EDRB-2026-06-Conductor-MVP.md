# Engineering Decision Review Board (EDRB) Report

**Report ID:** EDRB-2026-06-Conductor-MVP  
**Status:** APPROVED  
**Review Date:** June 24, 2026  
**Board Chair:** Chief Engineering Economist  

---

## 1. Executive Summary

This report evaluates three critical architectural choices for the Conductor MVP (V1):
1. **Durable Workflow Execution & Eventing Stack**: Temporal (Java SDK) + NATS JetStream versus Camunda 8 + Apache Kafka.
2. **WhatsApp Channel Integration Layer**: Official WhatsApp Cloud API versus OpenWA (unofficial web-scraping client).
3. **Identity & Access Management (IAM)**: Self-hosted Keycloak versus Auth0 (Managed SaaS).

Using the strict governance framework defined in the [Engineering Governance System](file:///c:/Users/rajaj/Projects/Conductor/Documentation%20Galore/07-Governance/Engineering-Governance-System.md), the board has delivered **GO / GO WITH CONDITIONS** determinations for all three components. By deploying Temporal, NATS, the WhatsApp Cloud API, and Keycloak, the platform optimizes for cash-flow preservation, operational simplicity, compliance safety, and legal viability.

---

## 2. Evidence

- **Temporal durable execution maturity**: Case studies from Uber, Netflix, and HashiCorp show Temporal handles billions of executions with high availability. The Java SDK is stable and integrates natively with Spring Boot.
- **NATS resource utilization**: NATS operates as a single static binary (~20MB) with minimal memory footprint, whereas Apache Kafka requires KRaft or ZooKeeper, running JVM-heavy processes that demand substantial CPU and RAM.
- **Meta Platform Terms of Service**: Section 1.c of the WhatsApp Business terms explicitly bans unauthorized automated access. Unofficial wrappers (OpenWA, Baileys) result in immediate IP blocks and permanent account suspension.
- **Keycloak vs. Auth0 pricing models**: Auth0 pricing scales on Monthly Active Users (MAU). At 10,000 MAUs, Auth0 Enterprise/Developer costs exceed ₹80,000 ($1,000) per month. Keycloak is Apache 2.0 licensed, costing only the underlying VM hosting (~₹1,600/month).

---

## 3. Assumptions

- **A1**: The initial engineering team consists of 3 developers, making operational simplicity a primary economic driver.
- **A2**: The initial launch target is India (INR billing, GST compliance, and DPDP compliance are mandatory).
- **A3**: Multi-tenant isolation is a non-negotiable security requirement.
- **A4**: Message volume will begin at under 100,000 events/day and scale to 1,000,000+ events/day within the first year.

---

## 4. Economic Analysis

### EDRB Economics Scorecard

| Decision | Dimension | Assessment | Evidence |
|----------|-----------|------------|----------|
| **Stack: Temporal + NATS** | Business Value | Accelerates MVP launch by 6-8 weeks by avoiding custom orchestration. | [00-Executive-Summary.md](file:///c:/Users/rajaj/Projects/Conductor/Documentation%20Galore/00-Executive-Summary.md#L88-L90) |
| | Engineering Cost | Low-to-medium. Standard Spring Boot integration. | Temporal Java SDK & Spring Boot libraries. |
| | Maintenance Cost | Low. No Kafka partition rebalancing or Camunda licensing. | Operational runbooks. |
| | Governance Cost | Low. Auditing workflow state is native in Temporal Web UI. | Temporal UI audit logs. |
| | TCO | ~₹8,000/month self-hosted, or ~$0.00025/workflow action on Temporal Cloud. | Temporal Cloud pricing page. |
| | ROI | High. Avoids 2 FTE months of custom scheduler development (~₹4,00,000). | Market salary figures. |
| **Channel: WhatsApp Cloud API** | Business Value | Essential. Eliminates account suspension risk, preserving product value. | Meta Platform Policy. |
| | Engineering Cost | Moderate. Requires integrating with Meta Graph API. | Meta Developer docs. |
| | Maintenance Cost | Low. Official API changes are versioned with 2-year lifespans. | Meta API changelogs. |
| | Governance Cost | Medium. Requires template compliance and opt-in validation. | [07-Governance/Compliance.md](file:///c:/Users/rajaj/Projects/Conductor/Documentation%20Galore/07-Governance/Compliance.md#L80-L121) |
| | TCO | Meta conversation fees + hosting. | Meta rate charts. |
| | ROI | Indefinite. An unofficial solution has a -100% ROI if suspended. | [07-Governance/Risk-Register.md:R-001](file:///c:/Users/rajaj/Projects/Conductor/Documentation%20Galore/07-Governance/Risk-Register.md#L26-L40) |
| **IAM: Keycloak Self-Hosted** | Business Value | Zero licensing fees allows competitive MVP SaaS pricing. | Open-source Apache 2.0 license. |
| | Engineering Cost | Moderate. Keycloak configuration has a steep learning curve. | OIDC integration specifications. |
| | Maintenance Cost | Moderate. Requires database migration and security patching. | Keycloak CVE logs. |
| | Governance Cost | Low. Native multi-tenant realm isolation simplifies compliance. | Keycloak Realm architecture. |
| | TCO | ~₹1,600/month hosting (AWS EC2 t3.medium). | AWS On-Demand pricing. |
| | ROI | Payback period achieved within 1 month compared to Auth0 pricing. | Auth0 SaaS pricing sheet. |

---

## 5. Complexity Analysis

### Decision 1: Workflow Execution & Eventing
- **Simpler Alternative**: Custom cron-scheduler + database-backed polling.
- **Proposed Solution**: Temporal Server + NATS JetStream.
- **Complexity Delta**: Higher operational footprint (managing two external runtimes) but significantly lower application codebase complexity. Avoids writing custom state management, distributed retry logic, and race-condition handling.
- **Complexity Classification**: **Moderate**
- **Justification**: Custom schedulers fail to guarantee durability, leading to corrupted workflows and severe operational support overhead.

### Decision 2: WhatsApp Channel Integration
- **Simpler Alternative**: OpenWA wrapper (runs locally, scrapes WhatsApp Web).
- **Proposed Solution**: Official WhatsApp Cloud API.
- **Complexity Delta**: Slightly higher initial integration setup (Facebook developer account, business verification) but zero maintenance complexity related to scraping workarounds.
- **Complexity Classification**: **Low**
- **Justification**: OpenWA violates Meta ToS, creating an immediate existential risk of permanent account bans.

### Decision 3: Identity & Access Management (IAM)
- **Simpler Alternative**: Auth0 (Managed SaaS).
- **Proposed Solution**: Keycloak (Self-Hosted).
- **Complexity Delta**: Higher operational complexity. Keycloak must be deployed, backed up, and updated.
- **Complexity Classification**: **Moderate**
- **Justification**: Saves over ₹8,000/month per tenant in license costs, which is critical for early-stage SMB SaaS margins.

---

## 6. Risk Analysis

### R-001: Keycloak Server Outage
- **Probability**: Low | **Impact**: High | **Mitigation**: Deploy Keycloak behind an AWS Application Load Balancer with auto-scaling (minimum 2 nodes) pointing to Amazon RDS Aurora PostgreSQL.

### R-002: Temporal State Database Growth
- **Probability**: Medium | **Impact**: Moderate | **Mitigation**: Configure strict workflow retention policies (e.g., delete history after 7 days) and offload analytical payloads to the PostgreSQL metrics store.

### R-003: WhatsApp Template Rejections
- **Probability**: High | **Impact**: Moderate | **Mitigation**: Implement template validation checks in the user interface to ensure variables and category choices adhere to Meta policies before submission.

---

## 7. Validation Mechanism

- **Operational Health**: Datadog/Prometheus dashboards tracking Keycloak CPU, NATS message backlog, and Temporal schedule latency.
- **Success Metric**: Zero multi-tenant data leaks (verified via automated SQL parser tests in CI/CD).
- **Compliance Audit**: Append-only log of customer consent records matched against WhatsApp outbound message payloads daily.

---

## 8. Rollback Strategy

### Workflow Stack
- **Threshold**: Platform failure rate >5% due to Temporal engine errors.
- **Action**: Fall back to direct NATS message handlers with stateless consumer databases.

### WhatsApp Channel
- **Threshold**: Meta Cloud API outage exceeds 4 hours.
- **Action**: Implement fallback SMS routing via Twilio or Gupshup SMS APIs.

### Identity Management
- **Threshold**: Keycloak deployment blockages delay release by >2 weeks.
- **Action**: Migrate realm configuration to a managed hosting provider (e.g., CloudEntity or Bitnami managed stack).

---

## 9. Governance Assessment

The governance mechanisms proposed (consent capture, template validation, and access logs) are proportional to the regulatory and platform policies Conductor faces. Non-compliance results in business termination (WABA ban or DPDP fines). Implementing these controls now adds ~5% to initial delivery effort while mitigating 100% of these existential risks.

---

## 10. Final Recommendation & Go/No-Go Decision

### Decision 1: Durable Workflow Execution & Eventing Stack
- **Recommendation**: **GO**
- **Decision**: Adopt Temporal + NATS JetStream.

### Decision 2: WhatsApp Channel Integration Layer
- **Recommendation**: **GO**
- **Decision**: Adopt official WhatsApp Cloud API exclusively; enforce OpenWA ban.

### Decision 3: Identity & Access Management (IAM)
- **Recommendation**: **GO WITH CONDITIONS**
- **Decision**: Adopt self-hosted Keycloak, subject to database auto-backups and multi-node container orchestration (ECS/Docker Compose) configurations being finalized prior to beta launch.

---

## EDRB Sign-Off

- **Confidence Score**: **High**
- **Final Verdict**: **GO WITH CONDITIONS**

*Signed,*  
**Chief Engineering Economist & Enterprise Architect, Conductor Review Board**
