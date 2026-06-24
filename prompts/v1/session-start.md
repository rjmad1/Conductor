You are working on a roadmap work item in this project. Follow the Loop Engineering rules in AGENTS.md exactly.

### Work Item
- ID / Title: [ ]
- Roadmap phase (Now / Next / Later): [ ]
- Priority (P0 / P1 / P2): [ ]
- Human owner: [ ]

### Goal
[ One sentence. Measurable. No vague language. ]

### Success Condition
[ Specific, verifiable. What does "done" look like? ]

### Stop Conditions
- [ Failure threshold — e.g., "3 failed attempts at X → escalate" ]
- [ Time budget — e.g., "no output after 2 hours → pause and review" ]
- [ Scope boundary — e.g., "do not modify files outside /src/feature-x/" ]

### Evaluation Rubric (locked — do not modify mid-session)
- [ Criterion 1 with pass/fail definition ]
- [ Criterion 2 with pass/fail definition ]
- [ Criterion 3 with pass/fail definition ]

### Memory Tier
- [ ] Working (ephemeral — discard after session)
- [ ] Organizational (persist — requires evidence + my approval)
- [ ] Experiment (isolated — no writes to production memory)

### Relevant Files / Context
- [ List only files directly needed for this task. No bulk dumps. ]

### Validation Commands
- [ Command or step to verify correctness ]
- [ Lint / test command ]
- [ Acceptance check ]

### Output Format
- [ Structured format required if output feeds a downstream step — e.g., JSON schema, YAML, diff ]

---

Execute in two phases:

**Phase 1 — Plan (do not write code or files yet):**
1. Restate the goal and success condition in your own words.
2. List the files you will touch and why.
3. Identify risks or unknowns that could trigger a stop condition.
4. Confirm the rubric is clear. Flag anything ambiguous before proceeding.
STOP and wait for my approval before Phase 2.

**Phase 2 — Execute (only after approval):**
1. Work one logical unit at a time (one component, one function, one decision).
2. After each unit: validate against the rubric, show evidence, state pass/fail.
3. If a stop condition is hit: halt, report state, escalate to me.
4. At completion: summarize what was done, what evidence exists, and whether output is ready for memory promotion.
