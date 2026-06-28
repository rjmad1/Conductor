# Repository Structure — Conductor

**Status:** MISSING from source → Fully Generated (⚡)  
**Source:** None  
**Last Updated:** June 2026

---

## Repository Strategy: Modular Monorepo

Conductor uses a **modular monorepo** approach for MVP — all services in one repository, deployed and versioned together. This reduces coordination overhead and is appropriate for a small team.

**Monorepo tooling:** Gradle multi-project build (Java services) + npm/pnpm workspaces (frontend)

**Migration path:** Extract services to separate repos when: the team exceeds 15 engineers, or CI build time exceeds 20 minutes.

---

## Repository: `conductor-platform` (Primary)

```
conductor-platform/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml             # Build, test, lint on every PR
│   │   ├── cd-staging.yml     # Deploy to staging on merge to main
│   │   └── cd-prod.yml        # Deploy to prod (manual trigger)
│   └── CODEOWNERS             # Code ownership by module
│
├── apps/
│   ├── web/                   # React frontend (TypeScript, TailwindCSS)
│   │   ├── src/
│   │   │   ├── components/    # Reusable UI components
│   │   │   ├── pages/         # Route-level page components
│   │   │   ├── hooks/         # Custom React hooks
│   │   │   ├── services/      # API client layer (axios)
│   │   │   ├── store/         # Zustand state management
│   │   │   └── types/         # TypeScript types/interfaces
│   │   ├── public/
│   │   ├── package.json
│   │   └── vite.config.ts
│   │
│   └── admin-portal/          # Internal Conductor admin UI (Phase 2)
│
├── services/
│   ├── tenant-service/        # Spring Boot service
│   │   ├── src/main/java/io/conductor/tenant/
│   │   │   ├── api/           # REST controllers
│   │   │   ├── domain/        # Domain models, JPA entities
│   │   │   ├── service/       # Business logic
│   │   │   ├── repository/    # JPA repositories
│   │   │   └── event/         # Event publishers
│   │   ├── src/main/resources/
│   │   │   ├── application.yml
│   │   │   └── db/migration/  # Flyway SQL migrations
│   │   └── build.gradle
│   │
│   ├── customer-service/      # Same structure as above
│   ├── workflow-service/
│   ├── whatsapp-adapter/
│   ├── conversation-service/
│   ├── campaign-service/
│   ├── template-service/
│   ├── connector-service/
│   ├── analytics-service/
│   ├── billing-service/
│   └── notification-service/
│
├── workers/
│   └── workflow-worker/       # Temporal worker process
│       ├── src/main/java/io/conductor/worker/
│       │   ├── workflow/      # Temporal workflow implementations
│       │   ├── activities/    # Temporal activity implementations
│       │   └── config/
│       └── build.gradle
│
├── shared/
│   ├── common-lib/            # Shared Java library
│   │   ├── events/            # Event envelope definitions (shared)
│   │   ├── exceptions/        # Common exceptions
│   │   ├── security/          # JWT validation utils
│   │   └── validation/        # Common validators
│   └── api-contracts/         # OpenAPI specs (shared between frontend and backend)
│       ├── tenant-api.yaml
│       ├── customer-api.yaml
│       └── workflow-api.yaml
│
├── infrastructure/
│   ├── terraform/             # Infrastructure as code
│   │   ├── modules/
│   │   ├── environments/
│   │   │   ├── staging/
│   │   │   └── prod/
│   │   └── main.tf
│   ├── k8s/ (or ecs/)        # Kubernetes manifests or ECS task definitions
│   └── scripts/              # Deployment scripts
│
├── config/
│   ├── kong/                  # Kong route and plugin configuration
│   ├── keycloak/              # Keycloak realm export
│   └── nats/                  # NATS configuration
│
├── docs/                      # This documentation repository (or symlink)
│
├── build.gradle               # Root Gradle build file
├── settings.gradle            # Multi-project settings
├── pnpm-workspace.yaml       # Frontend workspace config
└── README.md
```

---

## Repository: `conductor-docs` (This Repo)

All architecture, product, and operational documentation. Stored as Markdown.

```
conductor-docs/
├── 00-Executive-Summary.md
├── 01-Vision/
├── 02-Business/
├── 03-Product/
├── 04-Architecture/
├── 05-Engineering/
├── 06-Operations/
├── 07-Governance/
├── 08-AI/
├── 09-Program/
├── 10-Gap-Analysis/
└── 11-IDE-Knowledge-Pack/
```

---

## Repository: `conductor-connectors` (Phase 2)

Public SDK repository where third-party developers build connectors.

```
conductor-connectors/
├── sdk/                       # Java connector SDK (published to Maven Central)
├── examples/
│   ├── simple-webhook/
│   └── oauth-connector/
├── connectors/
│   ├── shopify/               # Reference implementation
│   └── zoho-crm/
└── docs/
    ├── getting-started.md
    ├── connector-manifest.md
    └── certification-guide.md
```

---

## Branching Strategy

**Model:** Trunk-based development with short-lived feature branches

```
main (production)
  └── feature/{ticket-id}-short-description    # Feature branches (max 2-3 days)
  └── fix/{ticket-id}-bug-description          # Bug fix branches
  └── hotfix/{ticket-id}-critical-fix          # Emergency production fixes
```

**Rules:**
- No direct commits to `main`
- All changes via Pull Request with minimum 1 reviewer
- Feature branches must be merged or deleted within 5 days
- Hotfixes can bypass staging with 2 reviewer approval and incident ticket

---

## Code Ownership (CODEOWNERS)

```
# apps/web/           @frontend-lead
# services/           @backend-lead
# infrastructure/     @devops-lead
# shared/common-lib/  @architecture-lead
# docs/               @product-lead
```

---

## Cross-References
- `05-Engineering/Coding-Standards.md` — Code quality rules
- `05-Engineering/API-Contracts.md` — OpenAPI specs location
- `09-Program/Implementation-Plan.md` — Repository setup in Phase 0
