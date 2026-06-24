# Glossary

This glossary defines standard architectural, technical, and domain-specific terminology used in the Conductor project.

---

| Term | Definition |
|---|---|
| **Tenant** | A business customer (SMB) running on the Conductor platform. All tenant data is logically isolated. |
| **WABA (WhatsApp Business Account)** | Meta's enterprise WhatsApp account structure required to run automated, official messaging campaigns. |
| **Modular Monolith** | A software architecture style where services are logically decoupled into packages (modules) but compiled and run inside a single process. |
| **Temporal Worker** | A Spring Boot process that polls task queues from the Temporal Server to execute workflow activities. |
| **NATS JetStream** | The event broker middleware providing persistent, at-least-once asynchronous event streams. |
| **OIDC (OpenID Connect)** | The identity validation standard layer built on OAuth 2.0, managed on Conductor via Keycloak. |
| **Squid Egress Proxy** | The forward proxy gateway filtering all outbound connector HTTP requests to mitigate SSRF vulnerabilities. |
| **PII (Personally Identifiable Information)** | Sensitive client details (phone numbers, emails) which Conductor encrypts before committing to PostgreSQL. |
| **DLQ (Dead Letter Queue)** | A dedicated NATS stream or database table (`dlq_records`) where failed, unprocessable events are routed for operator triage. |
| **Audit Ledger** | Database-trigger-driven, read-only partition table that logs all transactional mutations for security audit review. |
| **EWOS (Engineering Wisdom OS)** | The operational standards protocol governing developer workflows and AI agent cycles in the workspace. |

---

### Related Pages
- [Home](Home)
- [System Context](System-Context)
