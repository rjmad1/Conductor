# ADR 001: Use Temporal and NATS for MVP

**Status:** Accepted
**Date:** 2026-06-28

## Context
Conductor requires a highly resilient, stateful execution engine to model complex, long-running business workflows, as well as a decoupled messaging backbone to route inbound and outbound events. The original ideation documents contained conflicting recommendations: Camunda 8 + Kafka vs. Temporal + NATS. A definitive architectural path must be chosen to begin implementation.

## Decision
We will use **Temporal (Java SDK)** as the workflow execution engine and **NATS** as the event bus for the MVP.

## Consequences
### Positive
- Temporal provides out-of-the-box state persistence, retries, and timers without requiring complex database state machines.
- NATS is lighter-weight and simpler to operate than Kafka, reducing MVP infrastructure overhead.
- Reduces engineering effort significantly for complex async processes.

### Negative
- Team must learn the Temporal programming model and deterministic constraints.
- NATS lacks the persistent log compaction features of Kafka, which may require a migration at massive scale.

### Neutral
- Forces the use of Java for core workflow components, which aligns with enterprise standards.

## Alternatives Considered
- **Camunda 8 + Kafka:** Rejected because Camunda 8 introduces high licensing costs and operational complexity too early, and Kafka's footprint is too heavy for an MVP.
- **Custom State Machine on Postgres:** Rejected due to high engineering effort required to build a reliable distributed state machine.

## Rationale
Temporal aligns perfectly with the goal of abstracting workflow primitives, and NATS provides the simplest path to a robust publish/subscribe messaging layer. This combination provides the best balance of power, operational simplicity, and cost for the MVP.

## Related Decisions
None.
