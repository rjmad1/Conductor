# Dependency Map

## Inter-Module Dependencies
- `workflow` depends on `identity`, `messaging`, `customer`
- `messaging` depends on `tenant`, `identity`
- `integration` depends on `tenant`, `customer`
- `analytics` depends on `tenant` (ingests from all)
- `audit` is cross-cutting (called by all state-mutating modules)
- `observability` is cross-cutting

## Third-Party Dependencies
- **Temporal:** Workflow engine backend
- **NATS:** Message broker backend
- **Keycloak:** Identity provider
- **PostgreSQL:** Primary relational store
- **ClickHouse:** Analytics store
- **WhatsApp Cloud API:** Primary outbound messaging channel
