# Roadmap Work Item — Recurring Execution Prompt
> Copy this template at the start of every roadmap session. Fill in the `[ ]` fields. Do not skip sections.

---

## PROMPT: Start of Session

```
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
```

---

## Mid-Session Course-Correction Prompt

Use this if the agent drifts, over-generates, or touches out-of-scope files.

```
STOP. Do not continue generating.

Review what you have produced so far against the rubric and success condition defined at session start.

Answer:
1. What have you completed that passes the rubric?
2. What is in progress or incomplete?
3. Have you touched any files outside the declared scope? If so, list them.
4. Are you on track to meet the success condition, or should we adjust the plan?

Wait for my response before continuing.
```

---

## End-of-Session Closeout Prompt

Run this at the end of every session to capture learning.

```
Session complete. Before closing:

1. **Evidence summary** — List all outputs produced and the validation result for each (pass / fail / partial).
2. **Decisions made** — For each significant decision: what was decided, why, and what evidence supported it.
3. **Failures captured** — List anything that did not work, with the reason.
4. **Memory recommendation** — For each output: recommend Working (discard), Organizational (promote — needs my approval), or Experiment (isolate).
5. **Reusable pattern** — Did this session produce anything that should become a project standard? If yes, describe it in one paragraph.
6. **Next session setup** — What is the smallest, most focused goal for the next work item that follows from this one?

Format the above as structured output (YAML or numbered sections).
```

---

## Quick Rules (Agent Reminders — Paste If Needed)

```
Loop Engineering hard rules for this session:
- Do not modify the evaluation rubric mid-session.
- Do not promote anything to organizational memory without my explicit approval.
- Do not store a decision without recording WHY it was made.
- Do not use tokens generated or files touched as evidence of progress.
- If you are uncertain about a decision that is outside agent-owned scope, stop and escalate.
- One goal per session. Do not mix unrelated work items.
- Phase 1 (Plan) must complete before Phase 2 (Execute) begins.
```

---

## Scaling Gate (Before Automating Any Recurring Loop)

Before scheduling or automating a loop derived from this work item, confirm all of the following:

- [ ] This loop has run manually at least 3 times with consistent results
- [ ] Failure modes are documented
- [ ] Feedback signals are stable and machine-readable
- [ ] Outcome metrics (not activity metrics) show improvement
- [ ] Human owner has approved automation in writing

If any box is unchecked, the loop is not eligible for automation.
