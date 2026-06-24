# Infrastructure Architecture — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** Technical Layers (partial), MVP Scope  
**Last Updated:** June 2026

---

## Purpose
Defines cloud infrastructure, deployment architecture, networking, and operational environment for MVP and beyond.

---

## Cloud Strategy

**Primary Cloud:** AWS (recommended) — strong India presence (ap-south-1 Mumbai), managed services for RDS/ElastiCache, good Kubernetes support via EKS.  
**Alternative:** GCP (asia-south1 Mumbai) — comparable offering, strong Kubernetes (GKE).  
**Decision required by:** Engineering lead before Phase 0 start.

**Regions:**
- MVP: Single region (ap-south-1) with multi-AZ for RDS and ElastiCache
- Phase 2: Add disaster recovery region (ap-south-2 Hyderabad)

---

## MVP Infrastructure Architecture

```
                            ┌─────────────────┐
                            │   CloudFlare    │
                            │ (DNS, CDN, WAF) │
                            └────────┬────────┘
                                     │
                            ┌────────▼────────┐
                            │  AWS ALB        │
                            │ (Load Balancer) │
                            └────────┬────────┘
                                     │
                    ┌────────────────▼─────────────────┐
                    │         VPC: 10.0.0.0/16         │
                    │                                   │
                    │  ┌─────────────────────────────┐  │
                    │  │   Public Subnet             │  │
                    │  │   Kong API Gateway (2x)     │  │
                    │  └──────────────┬──────────────┘  │
                    │                 │                   │
                    │  ┌──────────────▼──────────────┐  │
                    │  │   Private Subnet: App Tier  │  │
                    │  │                             │  │
                    │  │  ┌─────────┐ ┌───────────┐  │  │
                    │  │  │ ECS /   │ │ Temporal  │  │  │
                    │  │  │ K8s Pod │ │ Workers   │  │  │
                    │  │  │ Services│ │           │  │  │
                    │  │  └─────────┘ └───────────┘  │  │
                    │  │                             │  │
                    │  │  ┌─────────┐ ┌───────────┐  │  │
                    │  │  │ Keycloak│ │ Temporal  │  │  │
                    │  │  │         │ │ Server    │  │  │
                    │  │  └─────────┘ └───────────┘  │  │
                    │  │                             │  │
                    │  │  ┌─────────┐ ┌───────────┐  │  │
                    │  │  │ NATS    │ │ Metabase  │  │  │
                    │  │  │ Cluster │ │           │  │  │
                    │  │  └─────────┘ └───────────┘  │  │
                    │  └──────────────┬──────────────┘  │
                    │                 │                   │
                    │  ┌──────────────▼──────────────┐  │
                    │  │   Private Subnet: Data Tier  │  │
                    │  │                             │  │
                    │  │  PostgreSQL RDS (Multi-AZ)  │  │
                    │  │  ElastiCache Redis Cluster  │  │
                    │  └─────────────────────────────┘  │
                    │                                   │
                    └───────────────────────────────────┘
                                     │
                            ┌────────▼────────┐
                            │   AWS S3        │
                            │ (Object Storage)│
                            └─────────────────┘
```

---

## Compute

### MVP: Amazon ECS (Fargate) — Recommended
- **Why:** Simpler than Kubernetes, no cluster management, pay per task
- **Alternative:** EKS if team has Kubernetes expertise

**Services (ECS Tasks):**
| Service | vCPU | Memory | Instances |
|---|---|---|---|
| Kong API Gateway | 0.5 | 1GB | 2 (min) |
| tenant-service | 0.5 | 1GB | 2 |
| customer-service | 0.5 | 1GB | 2 |
| workflow-service | 0.5 | 1GB | 2 |
| whatsapp-adapter | 0.5 | 1GB | 2 |
| conversation-service | 0.5 | 1GB | 2 |
| campaign-service | 0.5 | 1GB | 1 |
| template-service | 0.25 | 512MB | 1 |
| connector-service | 0.5 | 1GB | 2 |
| analytics-service | 0.5 | 1GB | 1 |
| billing-service | 0.25 | 512MB | 1 |
| Temporal Server | 1 | 2GB | 2 |
| Temporal Workers | 1 | 2GB | 3 |
| Keycloak | 1 | 2GB | 2 |
| NATS | 0.5 | 1GB | 3 (cluster) |
| Metabase | 1 | 2GB | 1 |

**Auto-scaling:** CPU > 70% triggers scale-out; target: 80% CPU utilization

---

## Database (AWS RDS)

**PostgreSQL 15 on RDS:**
- Instance type: `db.t3.medium` (MVP) → `db.r6g.large` (100+ tenants)
- Multi-AZ: Yes (standby in second AZ for failover)
- Storage: 100GB gp3, auto-scaling to 1TB
- Backups: Automated daily snapshots, 7-day retention
- Read replica: 1 read replica for Metabase analytics queries

**Connection pooling: PgBouncer** (deployed as sidecar or shared service)
- Pool mode: Transaction pooling
- Max connections: 200

---

## Cache (AWS ElastiCache)

**Redis 7 on ElastiCache:**
- Instance: `cache.t3.micro` (MVP) → `cache.r6g.large` (scale)
- Cluster mode: Disabled for MVP (single primary + 1 replica)
- Data: Conversation sessions, rate limiters, feature flags cache

---

## Object Storage (AWS S3)

**Buckets:**
| Bucket | Purpose | Access |
|---|---|---|
| `conductor-media-{env}` | WhatsApp media files (images, PDFs) | Private, pre-signed URLs |
| `conductor-exports-{env}` | Analytics exports, contact exports | Private, time-limited URL |
| `conductor-logs-{env}` | Archived application logs | Private, operations only |

---

## Networking

**VPC Design:**
- CIDR: `10.0.0.0/16`
- Public subnets (Kong, Load Balancer): `10.0.1.0/24`, `10.0.2.0/24` (2 AZs)
- Private subnets (App tier): `10.0.10.0/24`, `10.0.11.0/24`
- Private subnets (Data tier): `10.0.20.0/24`, `10.0.21.0/24`

**Security Groups:**
- Kong SG: Inbound 443 from internet, outbound to App tier only
- App SG: Inbound from Kong SG only, outbound to Data SG and internet (for external APIs)
- Data SG: Inbound from App SG only, no internet access

**DNS:**
- `app.conductor.io` → CloudFlare → ALB → Kong
- `api.conductor.io` → Same path (API traffic)
- Internal: AWS Route53 private hosted zone for service discovery

---

## CI/CD Pipeline

```
Developer PR → GitHub Actions
    ↓
  Unit Tests + Linting + SAST (SonarQube)
    ↓
  Build Docker image
    ↓
  Push to ECR (Elastic Container Registry)
    ↓
  Deploy to Staging (ECS service update)
    ↓
  Integration Tests (against staging)
    ↓
  Manual gate: QA approval
    ↓
  Deploy to Production (Blue/Green deployment)
```

**Deployment strategy:** Blue/Green via ECS (zero-downtime deployments)

**Environments:**
| Environment | Purpose | Auto-deploy |
|---|---|---|
| `dev` | Developer sandbox | On push to feature branch |
| `staging` | QA + integration testing | On merge to `main` |
| `prod` | Production | Manual gate after staging approval |

---

## Cost Estimation (MVP) ⚡

| Resource | Specification | Monthly Cost (USD) |
|---|---|---|
| ECS Fargate (all services) | ~20 tasks average | ~$400 |
| RDS PostgreSQL | db.t3.medium Multi-AZ | ~$150 |
| ElastiCache Redis | cache.t3.micro | ~$25 |
| Load Balancer (ALB) | 1 ALB | ~$20 |
| S3 | 100GB | ~$5 |
| CloudFront / CloudFlare | Free tier / ~$20/mo | ~$20 |
| NAT Gateway | 2 AZs | ~$70 |
| Data transfer | 100GB/month | ~$10 |
| **Total (MVP)** | | **~$700/month** |

*Scale target: $700/month supports 100–200 tenants; scales linearly.*

---

## Infrastructure as Code

**Tooling:** Terraform (preferred) or AWS CDK  
**Repository:** `conductor-infra` (separate from application code)  
**State:** Remote state in S3 + DynamoDB locking

**Modules:**
- `vpc` — VPC, subnets, security groups
- `rds` — PostgreSQL cluster
- `elasticache` — Redis cluster
- `ecs` — ECS cluster, task definitions, services
- `kong` — Kong deployment and configuration
- `s3` — Buckets and IAM policies

---

## Cross-References
- `04-Architecture/Solution-Architecture.md` — Platform components
- `06-Operations/SRE.md` — SLA targets, capacity planning
- `06-Operations/Monitoring.md` — Observability stack
- `09-Program/Implementation-Plan.md` — Phase 0 infrastructure setup tasks
