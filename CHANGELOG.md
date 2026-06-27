# Changelog

All notable changes to the Conductor platform will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **DPDP Section 12 compliance**: `ScheduledAnonymizationTask` — daily scheduled PII erasure for customers with `DELETED` status (T-204).
- **STOP keyword opt-out**: `StopUnsubscribeListener` — NATS JetStream listener that intercepts inbound "STOP" messages and revokes marketing consent within SLA (WA-C1).
- Inbound message event schema (`config/schemas/messaging.message.inbound.v1.json`).
- Unit tests for `ScheduledAnonymizationTask` (2 cases) and `StopUnsubscribeListener` (4 cases).
- Loop engineering workspace configuration (`.agents/`, `loop-control.ps1`).
- Governance, security, verification, and loop documentation under `docs/`.

### Changed
- Enabled `@EnableScheduling` on `CustomerApplication` to support scheduled background tasks.
- Added `@MockBean` declarations for new services in `TenantIsolationTest` to maintain Spring context compatibility.

---

## [1.0.0] - 2026-06-24

### Added
- Standardized directory layout under `/docs/` for clean repository organization.
- Root repository configurations: `.gitignore`, `.editorconfig`, `CODEOWNERS`, and `CONTRIBUTING.md`.
- Candidate Governance Architectural Decision Records: `ADR-GOV-001` through `ADR-GOV-010`.
- Canonical **Enterprise Architecture & Modernization Report** mapping TOGAF layers, GAP analysis, target blueprints, and execution roadmaps.
- Greenfield implementation of the **Analytics Platform Module** (`:platform:analytics`) supporting near real-time OLAP dashboards with ClickHouse.
- Secure Metabase dashboard integration via signed JWT embedding (ADR-008).
- Flyway database migration schemas for reporting, dashboards, and KPI tracking.

### Changed
- Reorganized documentation suite from `/Documentation Galore/` into structured subdirectories under `/docs/`.
- Updated root `README.md` file with the modernized directory layout.
- Updated `eos-manifest.yaml` configuration to point to the new canonical file locations.

### Removed
- Redundant and non-standard `/Documentation Galore/` folder.
