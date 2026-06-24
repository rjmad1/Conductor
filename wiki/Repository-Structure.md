# Repository Structure

## A. Purpose
This page documents the top-level repository directories, Java build modules, configuration files, and system layouts of the Conductor project. It is governed by `ADR-GOV-001`.

---

## B. Top-Level Directory Map

The Conductor repository separates source code, operational deployments, and architecture documentation:

```
Conductor/
├── .github/                   # GitHub Actions CI/CD workflows
├── ADRs/                      # Core runtime Architecture Decision Records (001-010)
├── agents/                    # AI Agent configurations, prompts, and templates
├── architecture/              # Legacy diagrams and architectural reviews
├── backlog/                   # Project issue tracker backlog maps
├── config/                    # Global configurations (e.g. IDE config)
├── docs/                      # Central documentation suite (TOGAF phases, standards)
│   ├── adr/                   # Repository governance ADRs
│   ├── api/                   # API and Event JSON contract definitions
│   ├── architecture/          # Multi-layered System Context, Solution blueprints
│   ├── gaps/                  # Gap analysis metrics across business & tech
│   ├── onboarding/            # Glossary, domain reference, developer setup
│   └── runbooks/              # Observability plans and operator checklists
├── environments/              # Environment configurations (dev, stage, prod, qa, local)
├── evaluation/                # LLM agent evaluation test suites
├── experiments/               # Sandbox experimental scripts
├── gradle/                    # Gradle wrapper binaries
├── infrastructure/            # Deploy configurations (docker, helm, kubernetes)
├── memory/                    # AI agent context-retrieval memories
├── observability/             # Prometheus, Grafana, Loki configurations
├── operations/                # Runbooks and backup schedules
├── platform/                  # Transactional Service Modules (Spring Boot)
│   ├── analytics/             # Metabase embedding, reporting, ClickHouse ingestion
│   ├── customer/              # Customer registry, segments, consent table updates
│   ├── events/                # DLQ record management and event replay services
│   ├── identity/              # JWT user creation, memberships, API keys
│   ├── integrations/          # Webhook ingress gateway and validation APIs
│   └── workflow/              # Temporal workers, JSON DSL parsing engine
├── plans/                     # Implementation target plans
├── prompts/                   # LLM coding prompts
├── roadmap/                   # Project milestone plans (releases, sprints)
├── scripts/                   # Workspace shell helper scripts
├── shared/                    # Reusable Java Libraries
│   ├── auth/                  # Spring Security OAuth2 configurations
│   ├── contracts/             # JSON Schema validation helpers
│   ├── events-model/          # Common event envelope models
│   ├── execution/             # Retry configurations
│   ├── messaging/             # NATS JetStream client publisher libraries
│   ├── middleware/            # ThreadLocal tenant filters, JPA audit aspects
│   ├── rules/                 # In-memory conditional rule evaluators
│   ├── security/              # Cryptographic HMAC validators
│   ├── templates/             # JSON workflow message templates
│   └── workflow/              # Workflow definitions model
├── specs/                     # API specification yaml contracts
├── src/                       # Top-level legacy Java wrappers (if any)
├── templates/                 # General project code templates
├── tests/                     # System integration tests
├── tools/                     # Code quality CLI tools
├── wiki/                      # GitHub Wiki Markdown source files
├── WORKSPACE_LAYOUT.md        # Architectural review of file configurations
└── README.md                  # Master project index and sync guide
```

---

## C. Governance Files in Root

- `CODEOWNERS`: Maps directories to specific team members to enforce pull request approval gates (governed by `ADR-GOV-003`).
- `Makefile`: Script shortcuts to build code (`make build`), run tests (`make test`), and start the local environment (`make up`).
- `sync.sh` / `sync.ps1`: Sync script to pull changes, add modified documentation, commit, and push automatically.
- `bootstrap.sh` / `bootstrap.ps1`: Automated installation script to configure git hooks and dependencies.

---

## D. Related Pages
- [Architecture Overview](Architecture-Overview)
- [Coding Standards](Coding-Standards)
- [Developer & API Guide](Developer-and-API-Guide)
