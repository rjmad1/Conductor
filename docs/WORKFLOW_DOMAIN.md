# Workflow Domain

## Overview

The Workflow domain (`platform/workflow/`) implements the Conductor Workflow Runtime and Business Automation Engine. It provides multi-tenant, durable workflow execution built on Temporal Server, enabling business process automation through a JSON DSL.

## Domain Boundaries

The workflow domain owns:
- `platform/workflow/` — runtime service
- `shared/workflow/` — shared enums and constants
- `shared/execution/` — execution context and retry policy
- `shared/rules/` — condition evaluation engine
- `shared/templates/` — reusable workflow templates

It does **not** import from: `identity`, `customer`, `integrations`, `messaging`, `analytics`, `ai`.

Cross-domain communication occurs exclusively via NATS JetStream events.

## Core Concepts

| Concept | Description |
|:---|:---|
| **WorkflowDefinition** | The JSON DSL specification: triggers, steps, conditions, and variables |
| **WorkflowExecution** | A running instance of a definition. Backed by a Temporal workflow. |
| **WorkflowStep** | A single action within an execution (SEND_EVENT, ASSIGN_USER, etc.) |
| **WorkflowHistory** | Immutable audit trail of all execution events |
| **TriggerService** | Routes incoming triggers to matching published definitions |
| **ActionExecutor** | Dispatches step actions to the appropriate handler |
| **CompensationService** | Executes rollback actions in reverse order on failure |

## Version Lifecycle

```
DRAFT → PUBLISHED → DEPRECATED → ARCHIVED
```

- Only DRAFT definitions can be modified or deleted.
- Only PUBLISHED definitions can be executed.
- Cloning creates a new DRAFT with version N+1.

## State Machine

```
PENDING → RUNNING → COMPLETED
                  → FAILED → COMPENSATED
                  → CANCELLED
                  → WAITING → RUNNING
                  → PAUSED  → RUNNING
```

## Technology Stack

- **Runtime:** Java 21, Spring Boot 3.2.5
- **Durable Execution:** Temporal Server 1.24 + Java SDK 1.22.3
- **Database:** PostgreSQL with Flyway migrations, JSONB for DSL storage
- **Events:** NATS JetStream via shared EventPublisher
- **Observability:** Micrometer + Prometheus (port 8090/actuator/metrics)
