# Verification Plan

| Checkpoint ID | Position | Level | Criterion | Method | Agent | Blocking |
|---------------|----------|-------|-----------|--------|-------|----------|
| `CHK-001` | After Phase 1 | 1-automated | SQL schemas are valid | `psql -c` syntax check | Test Agent | True |
| `CHK-002` | After Phase 2 | 1-automated | DSL code compiles | `mvn compile` | Test Agent | True |
| `CHK-003` | After Phase 3 | 2-checker | Schemas match DSL expectations | Human/Checker Review | Review Agent | True |
