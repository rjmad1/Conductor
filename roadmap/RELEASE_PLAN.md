# Release Plan & Deployment Roadmap — Conductor

This document details the build order waves, release schedule from version 0.1 to 1.0, and the multi-environment deployment strategy for the Conductor MVP.

---

## 1. Implementation Waves (Build Order)

The build order is divided into 5 waves. Each wave is designed to compile, deploy, and remain operational.

```
+---------------------------------------------------------------------------------------------------+
|  Wave 1: Foundation (W1-4)  -->  Wave 2: Messaging (W5-8)   -->  Wave 3: Workflows (W9-12)        |
|  - Keycloak & DB Migration       - Node Webhook & Outbound       - Temporal Workers & JSON DSL    |
|  - API Gateway Setup             - Consent Gate Interceptors     - Multi-Step Orchestration       |
+---------------------------------------------------------------------------------------------------+
                                                                     |
+--------------------------------------------------------------------+
|
v
+---------------------------------------------------------------------------------------------------+
|  Wave 4: Integrations (W13-14) -->  Wave 5: Analytics & Launch (W15-16)                           |
|  - Shopify, Zoho Webhooks           - Metabase Iframe & Logging                                   |
|  - Razorpay Subscription Webhooks   - DPDP Compliance & Prod Launch                               |
+---------------------------------------------------------------------------------------------------+
```

### Wave 1: Foundation (Weeks 1-4)
*   **Target:** Establish basic database schema tables, identity provider configurations, API Gateway routes, and Docker setups.
*   **Compile & Deploy:** Spring Boot monolith jar packages successfully with empty endpoints. Docker-compose runs Keycloak, PostgreSQL, NATS, and API Gateway.
*   **Exit Gate:** Call API endpoints; security filters intercept queries and parse `X-Tenant-ID` headers.

### Wave 2: Messaging (Weeks 5-8)
*   **Target:** Setup WhatsApp Adapter, NATS communication, outbound API wrappers, and consent management.
*   **Compile & Deploy:** Node.js adapter service joins the local composer network. Monolith wraps outbound Graph API requests.
*   **Exit Gate:** Trigger a test inbound webhook signature validation; watch it update consent registers in PostgreSQL and dispatch template responses.

### Wave 3: Workflows (Weeks 9-12)
*   **Target:** Implement workflow DSL schema validations, and configure Temporal Java worker flows.
*   **Compile & Deploy:** Monolith worker registers with Temporal. Temporal cluster deploys to AWS ECS staging environment.
*   **Exit Gate:** Launch a multi-step campaign; verify Temporal handles timer delays and resumes executions after simulated worker node restarts.

### Wave 4: Integrations (Weeks 13-14)
*   **Target:** Configure Shopify webhook maps, Zoho CRM lead updates, and Razorpay payment tracking.
*   **Compile & Deploy:** Webhook routes configured at Kong gateway. Outbound integrations route through the egress Squid forward proxy.
*   **Exit Gate:** Simulated Shopify order triggers a NATS event that initializes an active message campaign.

### Wave 5: Analytics & Production Launch (Weeks 15-16)
*   **Target:** Connect Metabase, embed dashboards, setup audit triggers, verify DPDP customer erasure schedules, and scale production infrastructure.
*   **Compile & Deploy:** Run database migrations creating write-once trigger restrictions. Configure Metabase JWT signing keys.
*   **Exit Gate:** Embed analytics dashboard. Run DPDP anonymization script verify contact PII fields hash successfully.

---

## 2. Release Milestones

### Release 0.1: Foundation
*   **Scope:** Keycloak realms, Flyway migrations (tenant, user tables), Kong Gateway JWT routes.
*   **Exit Criteria:** Secure routes reject requests lacking valid Keycloak tokens.
*   **Risks:** Keycloak configuration complexity.
*   **Mitigation:** Utilize standard Docker templates for local setup and AWS-managed Keycloak configurations in staging.

### Release 0.2: Messaging
*   **Scope:** Inbound WhatsApp webhooks Node.js adapter, outbound template dispatchers, immutable consent ledger.
*   **Exit Criteria:** HMAC validation handles inbound Meta spikes; consent checks intercept campaigns.
*   **Risks:** High webhook volumes overload NATS.
*   **Mitigation:** Enable queue boundaries and consumer rate limits in NATS configurations.

### Release 0.3: Workflows
*   **Scope:** JSON DSL parser, Temporal Spring Boot setup, activity workers.
*   **Exit Criteria:** Campaigns execute branches, delay times, and retries.
*   **Risks:** Temporal operational issues.
*   **Mitigation:** Run Temporal with a dedicated schema in the shared RDS PostgreSQL.

### Release 0.4: Integrations
*   **Scope:** Shopify and Zoho CRM adapters, Razorpay billing hook receiver, Squid egress proxy configurations.
*   **Exit Criteria:** Outbound requests target allowed integrations; private cloud endpoints are blocked.
*   **Risks:** Egress proxy configuration blocks valid traffic.
*   **Mitigation:** Verify allowed domains in staging before pushing changes to production configurations.

### Release 0.5: Analytics & Launch
*   **Scope:** Metabase embeddings, audit triggers, DPDP anonymizer scripts.
*   **Exit Criteria:** Metabase embeds filter records by tenant ID; audit triggers log changes.
*   **Risks:** Signed URL parameter tampering.
*   **Mitigation:** Configure parameters as "Locked" in Metabase settings.

### Release 1.0: Production MVP
*   **Scope:** Load testing, production AWS configurations, final compliance verification.
*   **Exit Criteria:** Handles load tests simulating 100 concurrent tenants, 100% consent check coverage.
*   **Risks:** Scaling bottlenecks under real production load.
*   **Mitigation:** Enable autoscaling for ECS tasks and monitoring alerts for RDS CPU usage.

---

## 3. Environment Strategy

The environments are configured across five tiers:

| Attribute | Local | Dev | QA | Staging | Production |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Infrastructure** | Developer Laptop / Docker Compose | Single AWS EC2 Instance | Shared AWS ECS Cluster | AWS ECS Fargate + RDS PostgreSQL | Multi-AZ AWS ECS Fargate + RDS (Multi-AZ) |
| **Deployment Method** | Local Maven / Gradle & docker-compose | Manual git deploy | CI/CD GitHub Actions deploy | GitHub Actions automated deploy | GitHub Actions manual gate approval deploy |
| **Data Strategy** | Local PostgreSQL container | Shared Dev Database | Shared QA DB | RDS Single-Instance (no replicas) | RDS Multi-AZ + PostgreSQL Read Replica |
| **Secrets Strategy** | Local `.env` variables | AWS Parameter Store | AWS Parameter Store | AWS Secrets Manager | AWS Secrets Manager (rotated) |
| **Monitoring Strategy** | Terminal outputs | Log files | Grafana | Prometheus + Grafana metrics | Prometheus + Alertmanager + PagerDuty |
