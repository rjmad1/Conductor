# Welcome to the Conductor Platform Wiki

Conductor is a **Conversational Business Automation Platform** designed for Small and Medium Businesses (SMBs). It provides a multi-tenant SaaS infrastructure that reduces hundreds of common business automations into a set of highly reusable platform capabilities, orchestrated via a universal workflow execution engine.

---

## 📖 Wiki Table of Contents

Navigate through the comprehensive Conductor documentation suite:

### 1. General Guides
*   **[Product Guide](Product-Guide)**: Strategic thesis, user personas, capabilities, and product roadmap.
*   **[User Guide](User-Guide)**: Getting started, managing campaigns, viewing analytics, and configuring integrations.
*   **[Best Practices Guide](Best-Practices-Guide)**: Standards for workflow design, event schema contracts, and multi-tenant security.
*   **[FAQ](FAQ)**: Frequently asked questions.
*   **[Glossary](Glossary)**: Standard domain vocabulary.

### 2. Architecture & Design
*   **[Architecture Overview](Architecture-Overview)**: Core architectural styles, layers, and service design.
*   **[System Context](System-Context)**: C4 Level 1 diagram, external boundaries, and actors.
*   **[Service Catalog](Service-Catalog)**: Detailed catalog of Spring Boot microservices, ports, and responsibilities.
*   **[Component Catalog](Component-Catalog)**: Infrastructure tier components (PostgreSQL, Redis, ClickHouse, NATS, etc.).
*   **[Data Model Guide](Data-Model-Guide)**: Full PostgreSQL entity-relationship model and ClickHouse OLAP schema definitions.
*   **[Integration Guide](Integration-Guide)**: Outbound connectors (Shopify, Razorpay, Zoho) and egress security models.
*   **[Decision Records Index](Decision-Records-Index)**: Governance and runtime Architecture Decision Records (ADRs).

### 3. Developer & Operator Resources
*   **[Implementation Guide](Implementation-Guide)**: Core logic, multi-tenancy implementation, and framework details.
*   **[Developer & API Guide](Developer-and-API-Guide)**: API contracts, webhook endpoints, event schema formats, and local sandbox setup.
*   **[CI/CD Guide](CI-CD-Guide)**: Build pipelines, PR checks, and release automation.
*   **[Operations Guide (Runbook)](Operations-Guide)**: Setup guides, deployment topology, backups, and SRE runbooks.
*   **[Troubleshooting Guide](Troubleshooting-Guide)**: Debugging NATS streams, Temporal failures, and Keycloak integrations.
*   **[Security Guide](Security-Guide)**: OIDC configuration, threat models, cryptographic verification, and compliance matrices (DPDP/WABA).
*   **[Release Notes](Release-Notes)**: Version history and feature releases.

---

## ⚡ Quick Start Links for personae
*   **New Developer?** Read [Developer Onboarding](Developer-and-API-Guide#local-sandbox-setup) and [Repository Structure](Repository-Structure).
*   **System Operator?** Read [Operations Guide](Operations-Guide) and [Observability Guide](Observability-Guide).
*   **Compliance Officer?** Read [Security Guide](Security-Guide) and [Compliance Matrix](Security-Guide#compliance-matrix).
*   **AI Agent?** Read [AI Agent Guide](AI-Agent-Guide) and the [System Context](System-Context).

---

## 📊 Core Platform Readiness

*   **Architectural Style**: Modular Monolith + Event-Driven Async Core
*   **Primary Technologies**: Java 21 / Spring Boot 3.x, Temporal, NATS JetStream, Keycloak, PostgreSQL, ClickHouse
*   **Target Cloud Environment**: AWS ECS Fargate, Mumbai Region (`ap-south-1`) for Indian geographical data residency.
