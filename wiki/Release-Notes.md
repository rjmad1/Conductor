# Conductor Release Notes

## A. Purpose
This page documents the release notes, version history, and changelog milestones for the Conductor platform.

## B. Intended Audience
- All Stakeholders
- Business Owners (Tenants)
- Platform Engineers and Developers

## C. Scope
Tracks system-wide changes, version updates, feature additions, modifications, and deprecations.

## D. Prerequisites
No technical prerequisites.

---

## E. Detailed Content

### [Unreleased]
This version includes the latest unreleased features spanning platform security, analytics, Loop engineering, and compliance tasks.

#### Added
- **Tenant-Scoped Security**: Implemented tenant isolation and action execution frameworks.
- **Provider Health Probes**: Replaced placeholder health checks with real provider connection probes for connectors.
- **DPDP Section 12 compliance**: Scheduled anonymization for PII erasure.
- **STOP keyword opt-out**: NATS JetStream listener for marketing consent revocation within SLA.
- **Loop Engineering Framework**: Agent templates, Loop engineering workspace configs, and `docs/loops/` catalogs.

#### Changed
- Standardized all REST error responses to conform to RFC 7807 ProblemDetail format.
- Resolved RC1 P1 release blockers (OAuth CSRF state validation, webhook encryption at rest).
- Reorganized codebase to support `@EnableScheduling` on CustomerApplication.

---

### [1.0.0] - 2026-06-24
This is the initial release of the remediated, implementation-ready Conductor platform and documentation suite.

#### Added
- **Directory Governance**: Standardized repository layout separating code (`/platform`, `/shared`), deployment assets (`/infrastructure`, `/environments`), and documentation (`/docs`).
- **Target Runtime Architecture**: Approved 10 core runtime ADRs (`ADR-001` through `ADR-010`) covering Virtual Threads, logically isolated multi-tenancy, Temporal orchestration, NATS JetStream, Keycloak IAM, and Squid egress proxies.
- **Analytics Service implementation**: Greenfield Spring Boot module `:platform:analytics` supporting ClickHouse OLAP schema ingestion, Metabase embedded dashboard generation, and scheduled reporting.
- **Event stream configurations**: Stream and replica parameters for NATS JetStream connection management and subscriber frameworks.
- **Compliance boundaries**: Design verification for 5-second WhatsApp unsubscribe processing and DPDP India data storage residency in the AWS Mumbai region.

#### Changed
- Consolidated Confluence exports and initiative briefs into a structured `/docs` directory.
- Reorganized `settings.gradle` build definitions to incorporate analytics, customer registry, event, and identity modules.

#### Removed
- Obsolete unstructured `/Documentation Galore/` directory files.

---

## F. References
- [Repository Structure](Repository-Structure)
- [Home](Home)

## G. Related Wiki Pages
- [Developer & API Guide](Developer-and-API-Guide)
- [Operations Guide](Operations-Guide)
