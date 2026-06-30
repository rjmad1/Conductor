# Implementation Breakdown

**Task ID:** TASK-001 (Define Workflow DSL and Data Schemas)

## Atomic Steps
- **STEP-001:** Define Tenant Schema (PostgreSQL). *Inputs: architecture context. Outputs: tenant.sql.*
- **STEP-002:** Define Customer Schema (PostgreSQL). *Inputs: tenant.sql. Outputs: customer.sql.*
- **STEP-003:** Define Identity Claims Schema. *Inputs: architecture context. Outputs: identity-claims.json.*
- **STEP-004:** Define Workflow DSL Interface. *Inputs: ADR-001. Outputs: WorkflowDSL.java.*
- **STEP-005:** Define Event Contracts for NATS. *Inputs: ADR-001. Outputs: events-asyncapi.yaml.*
- **STEP-006:** Validate DSL against Core Schemas. *Inputs: all previous outputs. Outputs: Validation Report.*

## Parallelisation Groups
- **Group 1.A** runs in sequence (STEP-001 then STEP-002).
- **Group 1.B** (STEP-003) runs parallel to Group 1.A.
- **Group 2.A** (STEP-004, STEP-005) runs concurrently.

## Assumptions
- `ASSM-001`: Tenant multi-tenancy model relies on foreign-key separation rather than isolated databases.
- `ASSM-002`: Workflow engine will use standard Java records for payload serialization.

## Risks
- `PLAN-RISK-001`: Data loss if schema migration fails during execution (Impact: High, Likelihood: Low).
