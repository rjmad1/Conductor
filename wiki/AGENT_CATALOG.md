# Agent Catalog — Conductor Platform

This catalog lists the taxonomy, responsibilities, and operational profiles of the 14 specialized AI agents working within the Conductor Platform.

---

## 1. Platform Bootstrap Agent
*   **Mission:** Setup, verify, and maintain the local and cloud environment bootstrap files, dependencies, and port mappings.
*   **Inputs:** `bootstrap.sh`, `bootstrap.ps1`, `docker-compose.local.yml`, workspace config.
*   **Outputs:** Local environment setup validation reports, port collision overrides, network provisioning scripts.
*   **Success Conditions:** Developers can boot all container dependencies in under 30 minutes with 100% successful health check passes.
*   **Stop Conditions:** System resources (RAM/CPU) are below minimum thresholds; unresolved port collisions block booting.
*   **Ownership:** DevSecOps / SRE Team
*   **Dependencies:** Docker, local OS commands
*   **Escalation Path:** Platform Engineering Lead

---

## 2. Architecture Governance Agent
*   **Mission:** Enforce Modular Monolith directory separation and validate decisions against accepted ADRs.
*   **Inputs:** `ADRs/`, package directory listings, import lists, `ARCHITECTURE_GUARDRAILS.md`.
*   **Outputs:** ArchUnit test configurations, architecture violation reports, PR status check logs.
*   **Success Conditions:** Build passes compile checks with 0 modular monolith package boundary violations.
*   **Stop Conditions:** Ambiguous cross-domain dependency mapping; unrecognized file layout structures.
*   **Ownership:** Architecture Design Review Board (EDRB)
*   **Dependencies:** Java packages, ArchUnit, git history
*   **Escalation Path:** Chief Architect

---

## 3. Security Agent
*   **Mission:** Scan workspace code, dependencies, and environments for leaks, secrets, and structural vulnerabilities.
*   **Inputs:** Git history, code files, Semgrep patterns, Trivy container outputs, `SECURITY_GUARDRAILS.md`.
*   **Outputs:** Vulnerability scans, GitLeaks warning messages, container security metrics.
*   **Success Conditions:** 0 plaintext secrets, 0 CVSS $\ge 9.0$ vulnerabilities, and 0 illegal egress routes in proposed code changes.
*   **Stop Conditions:** Discovering committed active credentials/tokens in source files; third-party scanner errors.
*   **Ownership:** DevSecOps / Security Lead
*   **Dependencies:** Trivy, GitLeaks, Semgrep, Checkov
*   **Escalation Path:** Chief Security Officer (CSO)

---

## 4. Identity Agent
*   **Mission:** Manage Keycloak realm rules, client scopes, and roles configurations.
*   **Inputs:** Keycloak config files, client credentials parameters, user scope matrices.
*   **Outputs:** Keycloak provisioning files, realm authorization rules.
*   **Success Conditions:** 100% successful Keycloak realm configurations mapping to active tenants with logical access controls.
*   **Stop Conditions:** Tenant ID mapping conflicts; missing OAuth 2.0 configuration variables.
*   **Ownership:** IAM / Security Team
*   **Dependencies:** Keycloak API, identity configuration directory
*   **Escalation Path:** Security Lead

---

## 5. Workflow Agent
*   **Mission:** Manage and validate Temporal workflow schemas, JSON DSL definitions, and workers.
*   **Inputs:** Campaign templates, Temporal configuration files, JSON DSL specifications.
*   **Outputs:** Java workflow definition workers, JSON campaign schema checks.
*   **Success Conditions:** JSON campaign files compile, parse, and execute cleanly within the Temporal worker runtime.
*   **Stop Conditions:** Temporal worker gRPC connection timeouts; invalid DSL JSON schemas.
*   **Ownership:** Workflow Core Team
*   **Dependencies:** Temporal Server, Java SDK
*   **Escalation Path:** Platform Engineering Lead

---

## 6. Messaging Agent
*   **Mission:** Maintain WhatsApp channel client integrations, webhooks, and template consent checks.
*   **Inputs:** Meta WhatsApp Cloud API specs, inbound webhook JSON formats, opt-out triggers.
*   **Outputs:** Meta API connector clients, consent validation filters.
*   **Success Conditions:** Delivery of WhatsApp messages with immediate consent verification and opt-out processing within 5 seconds.
*   **Stop Conditions:** Official Meta API credentials missing; customer opt-out trigger fails to register.
*   **Ownership:** Integration Channel Team
*   **Dependencies:** Meta API service, Customer Consent Ledger
*   **Escalation Path:** Integration Tech Lead

---

## 7. Customer Agent
*   **Mission:** Maintain customer profiles, logical database tables, and the opt-in/opt-out consent ledger.
*   **Inputs:** Customer profile models, consent ledger schemas, GDPR/DPDP deletion requests.
*   **Outputs:** JPA models, data erasure routines, consent check API routes.
*   **Success Conditions:** Complete logical isolation of tenant customer lists with immutable consent ledgers.
*   **Stop Conditions:** Attempted database joins across customer database boundaries; failed PII encryption mapping.
*   **Ownership:** Platform Core Team
*   **Dependencies:** PostgreSQL DB, AES Encryption Key Provider
*   **Escalation Path:** Platform Engineering Lead

---

## 8. Integration Agent
*   **Mission:** Maintain third-party adapters (Zoho, Shopify) routing outbound traffic strictly through Squid.
*   **Inputs:** Egress proxy configuration settings, third-party API properties, Squid whitelist targets.
*   **Outputs:** Squid proxy route parameters, integration adapter code.
*   **Success Conditions:** Webhook egress calls execute through the Squid proxy proxying loopbacks and blocking local subnets.
*   **Stop Conditions:** Direct outbound connection attempted outside the Squid gateway; proxy connectivity timeout.
*   **Ownership:** Integration Tech Lead
*   **Dependencies:** Squid Egress Proxy, external network interfaces
*   **Escalation Path:** SRE Platform Lead

---

## 9. Analytics Agent
*   **Mission:** Maintain Metabase dashboards, signed JWT frames, and read-replica database roles.
*   **Inputs:** Metabase dashboard schemas, JWT signing keys, read-replica SQL tables.
*   **Outputs:** Signed JWT frame templates, read-only analytical database schema queries.
*   **Success Conditions:** Correct analytical metrics render inside the dashboard frame without permission errors or SQL write capability.
*   **Stop Conditions:** Analytics user roles containing write or DDL privileges; JWT validation failure.
*   **Ownership:** Data Platform Team
*   **Dependencies:** Metabase Server, PostgreSQL Read-Replica
*   **Escalation Path:** Data Platform Lead

---

## 10. AI Foundation Agent
*   **Mission:** Manage Qdrant vector database configurations, RAG embedding integrations, and LLM providers.
*   **Inputs:** Vector schema definitions, RAG queries, provider parameters, `tenant_id` context tags.
*   **Outputs:** Qdrant search filters, embedding templates, LLM model parameters.
*   **Success Conditions:** RAG operations include active `tenant_id` payload filters to prevent cross-tenant vector data exposure.
*   **Stop Conditions:** AI API timeouts; search returns vectors without a matching tenant payload filter.
*   **Ownership:** AI Platform Team
*   **Dependencies:** Qdrant DB, LLM APIs (LiteLLM/Dify)
*   **Escalation Path:** AI Platform Architect

---

## 11. Testing Agent
*   **Mission:** Autonomously generate unit, integration, performance, and boundary isolation test suites.
*   **Inputs:** Main source code files, API specifications, performance metrics thresholds.
*   **Outputs:** JUnit tests, integration test mocks, Gatling benchmark code.
*   **Success Conditions:** Test coverage goals satisfied with 0 regression impacts on target modules.
*   **Stop Conditions:** Test script loop crashes or locks system resources; build compilation failures.
*   **Ownership:** QA & Quality Assurance Team
*   **Dependencies:** JUnit 5, Mockito, Gatling
*   **Escalation Path:** SRE Platform Lead

---

## 12. Documentation Agent
*   **Mission:** Maintain repository READMEs, ADR catalogs, API specs, and onboarding docs.
*   **Inputs:** Commit diff logs, system modifications, documentation templates.
*   **Outputs:** Markdown pages, architecture charts, API guides.
*   **Success Conditions:** Current documentation perfectly represents the files and APIs in the repository with correct file links.
*   **Stop Conditions:** Discrepancy detected between documented API and actual code schema.
*   **Ownership:** Technical Documentation Team
*   **Dependencies:** Markdown linters, git tree
*   **Escalation Path:** Technical Writer Lead

---

## 13. Release Agent
*   **Mission:** Coordinate versions, compile changelogs, generate SBOMs, and manage ArgoCD GitOps templates.
*   **Inputs:** Git tags, release notes, Helm configurations, ArgoCD states.
*   **Outputs:** SemVer tags, SBOM cycloneDX outputs, Kubernetes Helm deployments.
*   **Success Conditions:** Flawless compilation and deployment of system containers to target ECS cluster.
*   **Stop Conditions:** Failed pre-deploy validation scans; Helm templating errors.
*   **Ownership:** Release Management Team
*   **Dependencies:** ArgoCD, Helm, Git
*   **Escalation Path:** Chief Architect

---

## 14. Observability Agent
*   **Mission:** Maintain OpenTelemetry configurations, Prometheus metrics collections, and Logback masking filters.
*   **Inputs:** OTEL configuration rules, Logback XML formats, Grafana dashboard provisions.
*   **Outputs:** OTEL collector setups, masked logging rules, Grafana dashboard metrics.
*   **Success Conditions:** Masking of sensitive PII (emails, phone numbers) in logs while maintaining trace headers.
*   **Stop Conditions:** Trace header propagation failures; discovery of unmasked PII values in Loki.
*   **Ownership:** SRE Team
*   **Dependencies:** OpenTelemetry Collector, Prometheus, Grafana, Loki
*   **Escalation Path:** SRE Platform Lead
