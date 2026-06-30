# Architecture Overview

## System Context
Conductor is a Conversational Business Automation Platform designed for Small and Medium Businesses (SMBs). It abstracts 200+ business automations into ~15 reusable platform capabilities orchestrated by a universal workflow runtime.

## Core Architectural Principles
1. **Business Capability Layer:** Abstraction over raw workflow primitives.
2. **Configuration over Code:** Everything becomes configuration.
3. **Channel-Agnostic Abstraction:** WhatsApp-first, but extensible.

## 10 Core Domains
1. **Tenant:** Multi-tenancy registry.
2. **Identity:** Keycloak-backed user session security and IAM.
3. **Customer:** Contact registry and DPDP consent controls.
4. **Workflow:** Temporal-backed DSL execution state machine.
5. **Messaging:** Outbound broker and webhook ingestion (NATS).
6. **Integration:** External CRM/eCommerce sync adapters.
7. **Analytics:** ClickHouse-backed telemetry and reporting.
8. **AI:** LLM integration, copilots, and vector search.
9. **Audit:** Immutable row-based regulatory compliance logging.
10. **Observability:** OTel-backed metrics, traces, and logs.

## Current Baseline Architecture
- **Workflow Engine:** Temporal (Java SDK)
- **Event Bus:** NATS
- **Identity:** Keycloak
- **Database:** PostgreSQL (transactional), ClickHouse (analytics)
