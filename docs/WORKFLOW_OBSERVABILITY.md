# Workflow Observability

## Metrics (Micrometer / Prometheus)

All metrics are available at `GET /actuator/prometheus` and `GET /actuator/metrics`.

| Metric | Type | Tags | Description |
|:---|:---|:---|:---|
| `workflow.definitions.created` | Counter | tenant | Number of definitions created |
| `workflow.executions.started` | Counter | tenant | Number of executions started |
| `workflow.executions.completed` | Counter | tenant, status | Executions by final status |
| `workflow.executions.duration` | Timer | tenant, status | Execution duration distribution |
| `workflow.retries.total` | Counter | tenant | Total retry attempts |
| `workflow.compensations.total` | Counter | tenant | Total compensation executions |
| `workflow.actions.executed` | Counter | tenant, type, success | Actions by type and outcome |
| `workflow.triggers.fired` | Counter | tenant, type | Triggers by type |

## Structured Audit Logging

All mutations write to the `CONDUCTOR_AUDIT_LOG` logger via `AuditLogger`:

```
timestamp=... tenantId=... userId=... serviceId=workflow-service requestId=... 
action=WORKFLOW_EXECUTION_STARTED resource=execution:uuid outcome=SUCCESS details="definition=..."
```

### Audit Actions

| Action | Trigger |
|:---|:---|
| `WORKFLOW_DEFINITION_CREATED` | Definition created |
| `WORKFLOW_DEFINITION_UPDATED` | Definition updated |
| `WORKFLOW_DEFINITION_PUBLISHED` | Definition published |
| `WORKFLOW_DEFINITION_CLONED` | Definition cloned |
| `WORKFLOW_DEFINITION_DELETED` | Definition deleted |
| `WORKFLOW_EXECUTION_STARTED` | Execution started |
| `WORKFLOW_EXECUTION_CANCELLED` | Execution cancelled |
| `WORKFLOW_EXECUTION_REPLAYED` | Execution replayed |
| `ACTION_EXECUTED` | Step action executed |
| `TRIGGER_FIRED` | Trigger fired |
| `COMPENSATION_COMPLETED` | Compensation finished |

## Domain Events (NATS JetStream)

Events published to `WORKFLOW_STREAM` on `conductor.{tenantId}.workflow.*`:

| Subject | Trigger |
|:---|:---|
| `conductor.{tenantId}.workflow.definition.created` | Definition created |
| `conductor.{tenantId}.workflow.definition.updated` | Definition updated |
| `conductor.{tenantId}.workflow.definition.published` | Definition published |
| `conductor.{tenantId}.workflow.execution.started` | Execution started |
| `conductor.{tenantId}.workflow.execution.completed` | Execution completed |
| `conductor.{tenantId}.workflow.execution.failed` | Execution failed |
| `conductor.{tenantId}.workflow.execution.cancelled` | Execution cancelled |
| `conductor.{tenantId}.workflow.execution.compensated` | Compensation applied |

## Health Endpoints

- `GET /actuator/health` — liveness and readiness
- `GET /actuator/metrics` — all Micrometer metrics
- `GET /actuator/prometheus` — Prometheus scrape endpoint

## Temporal Observability

The Temporal Web UI is available at `http://localhost:8233` in the local Docker environment. It shows:
- All workflow executions per namespace
- Step-by-step execution history
- Retry attempts and failure details
- Signal history
