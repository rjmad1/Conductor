# Rollback Plan

## Phase 1 Rollback
- Revert the `tenant.sql` and `customer.sql` additions from version control.
- Revert `identity-claims.json` additions.

## Phase 2 Rollback
- Delete `WorkflowDSL.java` and `events-asyncapi.yaml`.

## Full Plan Rollback
- Execute `git checkout -- docs/planning/` to reset planning states.
- Execute `git clean -fd` to remove new schema/DSL files if generated.
