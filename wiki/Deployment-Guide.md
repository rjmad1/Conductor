# Deployment Guide

## A. Purpose
This page details setup, packaging, and deployment procedures for the Conductor platform across local sandboxes and staging/production cloud clusters.

---

## B. Local Compose Sandbox Setup
For developer sandboxes, the system is fully configured in `docker-compose.local.yml`.

### Launch commands:
```bash
# Start all platform infrastructure containers in detached mode
docker-compose -f docker-compose.local.yml up -d

# Verify container health status
docker-compose -f docker-compose.local.yml ps

# Tail log files
docker-compose -f docker-compose.local.yml logs -f
```

---

## C. Kubernetes (EKS Cluster) Deployment
Workloads in staging and production are managed using Kubernetes configurations located in `/infrastructure/kubernetes` and Helm templates under `/infrastructure/helm`.

### Helm Rollout Sequence:
1. **Provision IAM Roles & Secrets**: Ensure AWS SSM/KMS values are mapped to the cluster namespace.
2. **Execute Helm Upgrade**:
   ```bash
   helm upgrade --install conductor-core ./infrastructure/helm/conductor-monolith \
     --namespace production \
     --create-namespace \
     -f ./environments/prod/values.yaml
   ```
3. **Verify Deployment Rollout**:
   ```bash
   kubectl rollout status deployment/conductor-monolith -n production
   ```

---

## D. Target Cloud Topology (AWS ECS Fargate)
The enterprise target environment is localized in **AWS Mumbai (ap-south-1)**.

- **Multi-AZ Availability**: Containers run serverless tasks on AWS ECS Fargate, scaling dynamically across 2 Availability Zones (AZs) behind an Application Load Balancer.
- **Relational Storage**: Multi-AZ Amazon RDS PostgreSQL cluster with automated daily snapshots.
- **Durable Events & Workflows**: Self-hosted NATS JetStream 3-node cluster and a Temporal Server backend cluster running on AWS ECS.

---

## E. Related Pages
- [Operations Guide](Operations-Guide)
- [Configuration-Guide](Configuration-Guide)
- [Component Catalog](Component-Catalog)
