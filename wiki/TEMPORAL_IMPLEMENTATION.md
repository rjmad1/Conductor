# Temporal Implementation

## Architecture

Conductor uses Temporal Server 1.24 for durable workflow execution. The workflow service runs as a Temporal worker that polls for tasks and executes them.

## Namespace Strategy

**Single namespace:** `conductor`
**Task queue naming:** `workflow-{tenantId}`

This provides logical isolation per tenant without the operational overhead of namespace-per-tenant. Workers are registered lazily via `TenantWorkerRegistrar` on first execution for each tenant.

## Workflow and Activity Design

```
ConductorWorkflow (interface)
    └── ConductorWorkflowImpl  (deterministic — only Temporal-safe APIs)
            └── ConductorActivities (interface)
                    └── ConductorActivitiesImpl  (Spring-managed, side effects)
                            ├── loadWorkflowSteps     → DB read
                            ├── executeStep           → ActionExecutor dispatch
                            ├── markExecutionCompleted → WorkflowStateService
                            ├── markExecutionFailed    → WorkflowStateService
                            ├── compensateExecution    → CompensationService
                            └── markExecutionCompensated → WorkflowStateService
```

## Determinism Rules

The `ConductorWorkflowImpl` adheres to Temporal determinism requirements:
- Uses `Workflow.getLogger()` not SLF4J
- Uses `Workflow.sleep()` for DELAY steps
- Uses `Workflow.await()` for WAIT steps (signal-based)
- No direct DB access — all side effects via activities
- No `System.currentTimeMillis()` or `Random` — use `Workflow.currentTimeMillis()`

## State Recovery

Temporal automatically replays workflow history to recover state after worker restart. The `ConductorWorkflowImpl` is deterministic by design, ensuring replay produces identical results.

## Retry Configuration

Default activity retry policy:
- Max attempts: 3
- Initial interval: 2 seconds
- Backoff coefficient: 2.0
- Max interval: 120 seconds

Per-step override is supported via the `onFailure` config in the JSON DSL (planned for Phase 2).

## Signal Handling

Workflows support signals for WAIT steps:

```java
// Send a signal to resume a waiting workflow
WorkflowStub stub = workflowClient.newUntypedWorkflowStub("wf-{executionId}");
stub.signal("continue");
```

## Testing

All workflow tests use `TestWorkflowEnvironment` (in-memory Temporal) with mocked activities:

```java
TestWorkflowEnvironment testEnv = TestWorkflowEnvironment.newInstance();
Worker worker = testEnv.newWorker("test-queue");
worker.registerWorkflowImplementationTypes(ConductorWorkflowImpl.class);
worker.registerActivitiesImplementations(mockActivities);
testEnv.start();
```
